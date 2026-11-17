package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.AdjuntoEntity
import com.example.data.local.CertificadoEntity
import com.example.data.local.OperadorEntity
import com.example.data.local.RequisitoEntity
import com.example.domain.EstadoCertificado
import com.example.domain.EstadoRegistral
import com.example.domain.RoccaCatalogos
import com.example.domain.RolUsuario
import com.example.ui.components.EstadoRegistralBadge
import com.example.ui.theme.JetBrainsMono
import com.example.ui.theme.RoccaAvisoFondo
import com.example.ui.theme.RoccaAvisoTexto
import com.example.ui.theme.RoccaAzulOscuro
import com.example.ui.theme.RoccaBlanco
import com.example.ui.theme.RoccaBorde
import com.example.ui.theme.RoccaBordeInput
import com.example.ui.theme.RoccaCheckbox
import com.example.ui.theme.RoccaError
import com.example.ui.theme.RoccaErrorFondo
import com.example.ui.theme.RoccaExitoBorde
import com.example.ui.theme.RoccaExitoClaro
import com.example.ui.theme.RoccaExitoTexto
import com.example.ui.theme.RoccaFondo
import com.example.ui.theme.RoccaRojo
import com.example.ui.theme.RoccaTarjeta
import com.example.ui.theme.RoccaTextoApagado
import com.example.ui.theme.RoccaTextoMedio

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OperadorDetailDialog(
    currentRole: RolUsuario,
    operador: OperadorEntity,
    requisitos: List<RequisitoEntity>,
    adjuntos: List<AdjuntoEntity>,
    certificados: List<CertificadoEntity>,
    onDismiss: () -> Unit,
    onCambiarEstado: (EstadoRegistral) -> Unit,
    onActualizarRequisito: (RequisitoEntity, Boolean, String?) -> Unit,
    onAgregarAdjunto: (requisitoCodigo: String, filename: String, contentType: String, uri: String, sizeBytes: Long) -> Unit,
    onEliminarAdjunto: (Long) -> Unit,
    onAutorizarYEmitir: () -> Unit,
    onEmitirCertificado: () -> Unit,
    onSuspenderCertificado: (certificadoId: Long, motivo: String) -> Unit,
    onReactivarCertificado: (certificadoId: Long) -> Unit,
    onVerCarton: () -> Unit,
    onNotify: (String) -> Unit,
) {
    val context = LocalContext.current
    var pendingReqCode by remember { mutableStateOf("habilitacion_municipal") }
    var editingReq by remember { mutableStateOf<RequisitoEntity?>(null) }
    var obsDraft by remember { mutableStateOf("") }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            var filename = "documento_adjunto"
            var sizeBytes = 0L
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (nameIdx >= 0) filename = cursor.getString(nameIdx) ?: filename
                    if (sizeIdx >= 0) sizeBytes = cursor.getLong(sizeIdx)
                }
            }
            val mime = context.contentResolver.getType(uri) ?: "application/pdf"
            onAgregarAdjunto(pendingReqCode, filename, mime, uri.toString(), sizeBytes)
        }
    }

    val estadoActual = EstadoRegistral.fromString(operador.estadoRegistral)
    val esPersona = currentRole == RolUsuario.PERSONA
    val esAdmin = currentRole == RolUsuario.ADMIN

    editingReq?.let { req ->
        AlertDialog(
            onDismissRequest = { editingReq = null },
            containerColor = RoccaBlanco,
            title = { Text("Observación de Requisito (CU-2)", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(req.nombre, style = MaterialTheme.typography.bodySmall, color = RoccaTextoMedio)
                    OutlinedTextField(
                        value = obsDraft,
                        onValueChange = { obsDraft = it },
                        label = { Text("Observaciones / N° de Comprobante") },
                        colors = roccaTextFieldColors(),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onActualizarRequisito(req, req.cumplido, obsDraft)
                        editingReq = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoccaRojo),
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingReq = null }) {
                    Text("Cancelar", color = RoccaTextoMedio)
                }
            },
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            color = RoccaFondo,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(RoccaTarjeta)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = operador.nombreEstablecimiento,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = RoccaAzulOscuro,
                        )
                        Text(
                            text = "F 102 N° ${operador.numeroCertificadoF102} • CUIT ${operador.cuit}",
                            fontFamily = JetBrainsMono,
                            style = MaterialTheme.typography.bodySmall,
                            color = RoccaTextoApagado,
                        )
                    }
                    EstadoRegistralBadge(estadoRaw = operador.estadoRegistral)
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("close_detail_dialog"),
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                HorizontalDivider(color = RoccaBorde)

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    // Ciclo de Vida del Operador (CU-6): Solo Supervisor y Admin
                    if (!esPersona) {
                        item {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = RoccaBlanco),
                                border = BorderStroke(1.dp, RoccaBorde),
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    Text(
                                        text = "Ciclo de Vida del Operador (CU-6 • ${currentRole.label})",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = RoccaAzulOscuro,
                                    )
                                    Text(
                                        text = "Transiciones permitidas: Pendiente → Activo ↔ Suspendido → Bajado.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = RoccaTextoApagado,
                                    )
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        if (estadoActual == EstadoRegistral.PENDIENTE) {
                                            Button(
                                                onClick = { onCambiarEstado(EstadoRegistral.ACTIVO) },
                                                colors = ButtonDefaults.buttonColors(containerColor = RoccaExitoTexto),
                                                modifier = Modifier
                                                    .heightIn(min = 48.dp)
                                                    .testTag("btn_activar_operador"),
                                            ) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Activar")
                                            }
                                        }
                                        if (estadoActual == EstadoRegistral.ACTIVO) {
                                            OutlinedButton(
                                                onClick = { onCambiarEstado(EstadoRegistral.SUSPENDIDO) },
                                                border = BorderStroke(1.dp, RoccaAvisoTexto),
                                                modifier = Modifier
                                                    .heightIn(min = 48.dp)
                                                    .testTag("btn_suspender_operador"),
                                            ) {
                                                Icon(Icons.Default.Block, contentDescription = null, tint = RoccaAvisoTexto, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Suspender", color = RoccaAvisoTexto)
                                            }
                                        }
                                        if (estadoActual == EstadoRegistral.SUSPENDIDO) {
                                            Button(
                                                onClick = { onCambiarEstado(EstadoRegistral.ACTIVO) },
                                                colors = ButtonDefaults.buttonColors(containerColor = RoccaExitoTexto),
                                                modifier = Modifier
                                                    .heightIn(min = 48.dp)
                                                    .testTag("btn_reactivar_operador"),
                                            ) {
                                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Reactivar")
                                            }
                                        }
                                        if (estadoActual == EstadoRegistral.ACTIVO || estadoActual == EstadoRegistral.SUSPENDIDO) {
                                            OutlinedButton(
                                                onClick = { onCambiarEstado(EstadoRegistral.BAJADO) },
                                                border = BorderStroke(1.dp, RoccaError),
                                                modifier = Modifier
                                                    .heightIn(min = 48.dp)
                                                    .testTag("btn_bajar_operador"),
                                            ) {
                                                Text("Dar de Baja", color = RoccaError)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Requisitos de Inscripción (CU-2)
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = RoccaBlanco),
                            border = BorderStroke(1.dp, RoccaBorde),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                val cumplidos = requisitos.count { it.cumplido }
                                Text(
                                    text = "Requisitos de Inscripción ($cumplidos/${requisitos.size})",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = RoccaAzulOscuro,
                                )
                                Text(
                                    text = if (esPersona) {
                                        "Marcá los requisitos presentados y adjuntá la documentación JPG/PDF por cada ítem."
                                    } else {
                                        "Verificá cada requisito y agregá observaciones de fiscalización."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = RoccaTextoApagado,
                                )
                                requisitos.forEach { req ->
                                    Surface(
                                        onClick = { onActualizarRequisito(req, !req.cumplido, req.observaciones) },
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (req.cumplido) RoccaExitoClaro else RoccaBlanco,
                                        border = BorderStroke(1.dp, if (req.cumplido) RoccaExitoBorde else RoccaBorde),
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Checkbox(
                                                checked = req.cumplido,
                                                onCheckedChange = {
                                                    onActualizarRequisito(req, !req.cumplido, req.observaciones)
                                                },
                                                colors = CheckboxDefaults.colors(checkedColor = RoccaCheckbox),
                                            )
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = req.nombre,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Medium,
                                                )
                                                if (!req.observaciones.isNullOrBlank()) {
                                                    Text(
                                                        text = "Obs.: ${req.observaciones}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = RoccaTextoApagado,
                                                    )
                                                }
                                            }
                                            IconButton(
                                                onClick = {
                                                    editingReq = req
                                                    obsDraft = req.observaciones.orEmpty()
                                                },
                                                modifier = Modifier.size(44.dp),
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.EditNote,
                                                    contentDescription = "Observaciones de ${req.nombre}",
                                                    tint = RoccaTextoMedio,
                                                )
                                            }
                                            IconButton(
                                                onClick = {
                                                    pendingReqCode = req.codigo
                                                    filePicker.launch(arrayOf("application/pdf", "image/*"))
                                                },
                                                modifier = Modifier.size(44.dp),
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.AttachFile,
                                                    contentDescription = "Adjuntar documento para ${req.nombre}",
                                                    tint = RoccaRojo,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Adjuntos Documentales del Operador (CU-3)
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = RoccaBlanco),
                            border = BorderStroke(1.dp, RoccaBorde),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(
                                        text = "Adjuntos del Operador (${adjuntos.size})",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = RoccaAzulOscuro,
                                    )
                                    OutlinedButton(
                                        onClick = {
                                            pendingReqCode = "habilitacion_municipal"
                                            filePicker.launch(arrayOf("application/pdf", "image/*"))
                                        },
                                        border = BorderStroke(1.dp, RoccaBordeInput),
                                        modifier = Modifier
                                            .heightIn(min = 48.dp)
                                            .testTag("adjuntar_documento_button"),
                                    ) {
                                        Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Adjuntar")
                                    }
                                }
                                if (adjuntos.isEmpty()) {
                                    Text(
                                        text = "No hay documentos adjuntos aún. Tocá Adjuntar o el clip junto a un requisito.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = RoccaTextoApagado,
                                    )
                                } else {
                                    adjuntos.forEach { adj ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = RoccaTarjeta,
                                            border = BorderStroke(1.dp, RoccaBorde),
                                            modifier = Modifier.fillMaxWidth(),
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = adj.filename,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        fontWeight = FontWeight.SemiBold,
                                                    )
                                                    Text(
                                                        text = "Requisito: ${adj.requisitoCodigo}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = RoccaTextoApagado,
                                                    )
                                                }
                                                IconButton(
                                                    onClick = {
                                                        try {
                                                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                                                setDataAndType(Uri.parse(adj.uriString), adj.contentType)
                                                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                            }
                                                            context.startActivity(intent)
                                                        } catch (_: Exception) {
                                                            onNotify("Archivo registrado: ${adj.filename}")
                                                        }
                                                    },
                                                    modifier = Modifier.size(44.dp),
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.OpenInNew,
                                                        contentDescription = "Abrir adjunto",
                                                        tint = RoccaAzulOscuro,
                                                    )
                                                }
                                                IconButton(
                                                    onClick = { onEliminarAdjunto(adj.id) },
                                                    modifier = Modifier.size(44.dp),
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Eliminar adjunto",
                                                        tint = RoccaError,
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Certificados de Habilitación F 102 (CU-7)
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = RoccaBlanco),
                            border = BorderStroke(1.dp, RoccaBorde),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Text(
                                    text = "Certificados de Habilitación F 102 (${certificados.size})",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = RoccaAzulOscuro,
                                )

                                if (certificados.isEmpty()) {
                                    Text(
                                        text = if (esAdmin) {
                                            "Este operador aún no tiene certificado emitido. Autorizá el operador para emitir el Cartón F 102."
                                        } else {
                                            "Pendiente de autorización y emisión por el rol Administrador."
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = RoccaAvisoTexto,
                                    )
                                }

                                certificados.forEach { cert ->
                                    val isRelevado = cert.estado == EstadoCertificado.RELEVADO.name
                                    val isActivo = cert.estado == EstadoCertificado.ACTIVO.name
                                    val isSusp = cert.estado == EstadoCertificado.SUSPENDIDO.name
                                    val dias = RoccaCatalogos.diasParaVencimiento(cert.fechaVencimiento)

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isActivo) RoccaExitoClaro else if (isRelevado) RoccaAvisoFondo else RoccaErrorFondo,
                                        border = BorderStroke(1.dp, RoccaBorde),
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp),
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = cert.numeroCertificado,
                                                        fontFamily = JetBrainsMono,
                                                        fontWeight = FontWeight.Bold,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                    )
                                                    Text(
                                                        text = "Emisión: ${RoccaCatalogos.formatearFechaCorta(cert.fechaEmision)} • Vence: ${RoccaCatalogos.formatearFechaCorta(cert.fechaVencimiento)}" +
                                                            (dias?.let { " ($it días)" } ?: ""),
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = RoccaTextoMedio,
                                                    )
                                                }
                                                Text(
                                                    text = cert.estado,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isActivo) RoccaExitoTexto else if (isRelevado) RoccaAvisoTexto else RoccaError,
                                                )
                                            }
                                            if (esAdmin && (isActivo || isSusp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.End,
                                                ) {
                                                    TextButton(
                                                        onClick = {
                                                            if (isActivo) {
                                                                onSuspenderCertificado(cert.id, "Suspendido desde ficha de operador")
                                                            } else {
                                                                onReactivarCertificado(cert.id)
                                                            }
                                                        },
                                                    ) {
                                                        Text(
                                                            text = if (isActivo) "Suspender certificado" else "Reactivar certificado",
                                                            color = if (isActivo) RoccaError else RoccaExitoTexto,
                                                            fontWeight = FontWeight.Bold,
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    if (esAdmin) {
                                        if (estadoActual == EstadoRegistral.PENDIENTE) {
                                            Button(
                                                onClick = onAutorizarYEmitir,
                                                colors = ButtonDefaults.buttonColors(containerColor = RoccaExitoTexto),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .heightIn(min = 48.dp)
                                                    .testTag("admin_autorizar_detail_button"),
                                            ) {
                                                Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Autorizar y Emitir", fontWeight = FontWeight.Bold)
                                            }
                                        } else if (estadoActual == EstadoRegistral.ACTIVO) {
                                            OutlinedButton(
                                                onClick = onEmitirCertificado,
                                                border = BorderStroke(1.dp, RoccaRojo),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .heightIn(min = 48.dp)
                                                    .testTag("renovar_certificado_button"),
                                            ) {
                                                Icon(Icons.Default.Verified, contentDescription = null, tint = RoccaRojo, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Renovar F 102", color = RoccaRojo, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    if (certificados.isNotEmpty() || esAdmin) {
                                        Button(
                                            onClick = onVerCarton,
                                            colors = ButtonDefaults.buttonColors(containerColor = RoccaRojo),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .heightIn(min = 48.dp)
                                                .testTag("detail_ver_carton_button"),
                                        ) {
                                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Ver Cartón", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
