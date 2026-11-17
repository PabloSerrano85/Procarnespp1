package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.AdjuntoPersonaEntity
import com.example.data.local.CertificadoEntity
import com.example.data.local.OperadorEntity
import com.example.data.local.PersonaEntity
import com.example.data.local.RequisitoEntity
import com.example.data.local.UsuarioEntity
import com.example.domain.EstadoCertificado
import com.example.domain.EstadoRegistral
import com.example.domain.RoccaCatalogos
import com.example.domain.TipoAdjuntoPersona
import com.example.ui.components.EstadoRegistralBadge
import com.example.ui.components.RoccaMetricTile
import com.example.ui.theme.JetBrainsMono
import com.example.ui.theme.RoccaAviso
import com.example.ui.theme.RoccaAvisoBorde
import com.example.ui.theme.RoccaAvisoFondo
import com.example.ui.theme.RoccaAvisoTexto
import com.example.ui.theme.RoccaAzulOscuro
import com.example.ui.theme.RoccaBlanco
import com.example.ui.theme.RoccaBorde
import com.example.ui.theme.RoccaBordeInput
import com.example.ui.theme.RoccaError
import com.example.ui.theme.RoccaErrorFondo
import com.example.ui.theme.RoccaExitoBorde
import com.example.ui.theme.RoccaExitoClaro
import com.example.ui.theme.RoccaExitoTexto
import com.example.ui.theme.RoccaFondo
import com.example.ui.theme.RoccaHeaderFin
import com.example.ui.theme.RoccaRojo
import com.example.ui.theme.RoccaRojoClaro
import com.example.ui.theme.RoccaTarjeta
import com.example.ui.theme.RoccaTextoApagado
import com.example.ui.theme.RoccaTextoMedio
import com.example.ui.theme.RoccaTextoSuave

@Composable
fun PersonaDashboardScreen(
    usuario: UsuarioEntity,
    persona: PersonaEntity?,
    misOperadores: List<OperadorEntity>,
    allRequisitos: List<RequisitoEntity>,
    allCertificados: List<CertificadoEntity>,
    adjuntosPersona: List<AdjuntoPersonaEntity>,
    onAltaNuevaSucursal: () -> Unit,
    onGestionarSucursal: (OperadorEntity) -> Unit,
    onVerCarton: (OperadorEntity) -> Unit,
    onAgregarAdjuntoPersona: (tipoDoc: String, filename: String, mime: String, uri: String, size: Long) -> Unit,
    onEliminarAdjuntoPersona: (Long) -> Unit,
    onNotify: (String) -> Unit,
) {
    val context = LocalContext.current
    var pendingDocTipo by remember { mutableStateOf(TipoAdjuntoPersona.DNI_FRENTE.codigo) }

    val docPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            var filename = "documento_persona"
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
            onAgregarAdjuntoPersona(pendingDocTipo, filename, mime, uri.toString(), sizeBytes)
        }
    }

    val habilitados = misOperadores.count { EstadoRegistral.fromString(it.estadoRegistral) == EstadoRegistral.ACTIVO }
    val pendientes = misOperadores.count { EstadoRegistral.fromString(it.estadoRegistral) == EstadoRegistral.PENDIENTE }
    val porVencer = misOperadores.count { op ->
        val cert = allCertificados.firstOrNull {
            it.operadorId == op.id && it.estado == EstadoCertificado.ACTIVO.name
        }
        val dias = RoccaCatalogos.diasParaVencimiento(cert?.fechaVencimiento)
        dias != null && dias <= 30
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(RoccaFondo),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Bienvenida e Identidad de Persona Titular
        item {
            val bannerGradient = Brush.horizontalGradient(
                colors = listOf(RoccaAzulOscuro, RoccaHeaderFin)
            )
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(bannerGradient),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0x33FFFFFF)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color(0xFFFCD34D),
                                modifier = Modifier.size(22.dp),
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "PANEL DE PERSONA TITULAR",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFCD34D),
                            )
                            Text(
                                text = persona?.nombre ?: usuario.nombre,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = RoccaBlanco,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = "CUIT: ${persona?.identificador ?: "Sin CUIT"} • ${usuario.email}",
                                fontFamily = JetBrainsMono,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFCBD5E1),
                            )
                        }
                    }

                    Button(
                        onClick = onAltaNuevaSucursal,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RoccaAviso,
                            contentColor = Color(0xFF020617),
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("persona_alta_sucursal_button"),
                    ) {
                        Icon(Icons.Default.AddBusiness, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Dar de Alta Nueva Sucursal / Carnicería",
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }

        // KPIs del Titular
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                RoccaMetricTile(
                    label = "Mis Sucursales",
                    value = misOperadores.size.toString(),
                    accent = RoccaAzulOscuro,
                    modifier = Modifier.weight(1f),
                )
                RoccaMetricTile(
                    label = "Habilitadas",
                    value = habilitados.toString(),
                    accent = RoccaExitoTexto,
                    modifier = Modifier.weight(1f),
                )
                RoccaMetricTile(
                    label = "Pendientes",
                    value = pendientes.toString(),
                    accent = RoccaAvisoTexto,
                    modifier = Modifier.weight(1f),
                )
                RoccaMetricTile(
                    label = "Por vencer",
                    value = porVencer.toString(),
                    accent = if (porVencer > 0) RoccaError else RoccaTextoApagado,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // Sección Mis Sucursales + Estados + Vencimientos + Subida de Requisitos
        item {
            Text(
                text = "Mis Carnicerías / Establecimientos y Vencimientos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = RoccaAzulOscuro,
            )
        }

        if (misOperadores.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = RoccaBlanco),
                    border = BorderStroke(1.dp, RoccaBorde),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = null,
                            tint = RoccaTextoApagado,
                            modifier = Modifier.size(36.dp),
                        )
                        Text(
                            text = "Todavía no tenés sucursales registradas",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = RoccaAzulOscuro,
                        )
                        Text(
                            text = "Dá de alta tu primera carnicería o establecimiento para presentar los requisitos y solicitar la habilitación F 102.",
                            style = MaterialTheme.typography.bodySmall,
                            color = RoccaTextoApagado,
                        )
                    }
                }
            }
        } else {
            items(misOperadores, key = { it.id }) { op ->
                val reqsOp = allRequisitos.filter { it.operadorId == op.id }
                val cumplidos = reqsOp.count { it.cumplido }
                val certsOp = allCertificados.filter { it.operadorId == op.id }
                val certVigente = certsOp.firstOrNull { it.estado == EstadoCertificado.ACTIVO.name }
                    ?: certsOp.firstOrNull()

                PersonaSucursalCard(
                    operador = op,
                    cumplidos = cumplidos,
                    totalRequisitos = reqsOp.size,
                    certificado = certVigente,
                    onSubirRequisitos = { onGestionarSucursal(op) },
                    onVerCarton = { onVerCarton(op) },
                )
            }
        }

        // CU-4: Adjuntos del Titular (Persona)
        if (persona != null) {
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.UploadFile,
                                contentDescription = null,
                                tint = RoccaRojo,
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Documentación Personal del Titular (CU-4)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = RoccaAzulOscuro,
                                )
                                Text(
                                    text = "Adjuntá DNI Frente, DNI Dorso, Constancia CUIT y DGR/IIBB vinculados a tu cuenta.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = RoccaTextoApagado,
                                )
                            }
                        }

                        TipoAdjuntoPersona.entries.forEach { tipo ->
                            val adj = adjuntosPersona.firstOrNull { it.tipoDocumento == tipo.codigo }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (adj != null) RoccaExitoClaro else RoccaTarjeta,
                                border = BorderStroke(1.dp, if (adj != null) RoccaExitoBorde else RoccaBorde),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = tipo.label,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = RoccaAzulOscuro,
                                        )
                                        Text(
                                            text = adj?.filename ?: tipo.descripcion,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (adj != null) RoccaExitoTexto else RoccaTextoApagado,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                    if (adj != null) {
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
                                                contentDescription = "Abrir ${adj.filename}",
                                                tint = RoccaExitoTexto,
                                            )
                                        }
                                        IconButton(
                                            onClick = { onEliminarAdjuntoPersona(adj.id) },
                                            modifier = Modifier.size(44.dp),
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Eliminar ${tipo.label}",
                                                tint = RoccaError,
                                            )
                                        }
                                    } else {
                                        OutlinedButton(
                                            onClick = {
                                                pendingDocTipo = tipo.codigo
                                                docPicker.launch(arrayOf("application/pdf", "image/*"))
                                            },
                                            border = BorderStroke(1.dp, RoccaRojo),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier
                                                .heightIn(min = 42.dp)
                                                .testTag("upload_persona_${tipo.codigo}"),
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AttachFile,
                                                contentDescription = null,
                                                tint = RoccaRojo,
                                                modifier = Modifier.size(15.dp),
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Subir", color = RoccaRojo, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
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

@Composable
private fun PersonaSucursalCard(
    operador: OperadorEntity,
    cumplidos: Int,
    totalRequisitos: Int,
    certificado: CertificadoEntity?,
    onSubirRequisitos: () -> Unit,
    onVerCarton: () -> Unit,
) {
    val estado = EstadoRegistral.fromString(operador.estadoRegistral)
    val diasRestantes = RoccaCatalogos.diasParaVencimiento(certificado?.fechaVencimiento)
    val proximoAVencer = diasRestantes != null && diasRestantes <= 30

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = RoccaBlanco),
        border = BorderStroke(1.dp, if (proximoAVencer) RoccaAvisoBorde else RoccaBorde),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("persona_sucursal_card_${operador.id}"),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(RoccaTarjeta)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = operador.nombreEstablecimiento,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = RoccaAzulOscuro,
                    )
                    Text(
                        text = "${RoccaCatalogos.nombreCategoria(operador.categoria)} • ${operador.domicilioFormateado}",
                        style = MaterialTheme.typography.bodySmall,
                        color = RoccaTextoApagado,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                EstadoRegistralBadge(estadoRaw = operador.estadoRegistral)
            }

            HorizontalDivider(color = RoccaBorde)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Certificado y Vencimiento
                Surface(
                    color = when {
                        certificado == null -> RoccaAvisoFondo
                        proximoAVencer -> RoccaErrorFondo
                        certificado.estado == EstadoCertificado.ACTIVO.name -> RoccaExitoClaro
                        else -> RoccaErrorFondo
                    },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, RoccaBorde),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = when {
                                certificado == null -> RoccaAvisoTexto
                                proximoAVencer -> RoccaError
                                else -> RoccaExitoTexto
                            },
                            modifier = Modifier.size(18.dp),
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            if (certificado != null) {
                                Text(
                                    text = "Certificado ${certificado.numeroCertificado} (${certificado.estado})",
                                    fontFamily = JetBrainsMono,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = RoccaAzulOscuro,
                                )
                                val vencStr = RoccaCatalogos.formatearFechaCorta(certificado.fechaVencimiento)
                                val aviso = when {
                                    diasRestantes == null -> "Vence: $vencStr"
                                    diasRestantes < 0 -> "VENCIDO hace ${-diasRestantes} días ($vencStr)"
                                    diasRestantes <= 30 -> "Vence en $diasRestantes días ($vencStr) — ¡Renovar pronto!"
                                    else -> "Vence el $vencStr (en $diasRestantes días)"
                                }
                                Text(
                                    text = aviso,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (proximoAVencer) FontWeight.Bold else FontWeight.Normal,
                                    color = if (proximoAVencer) RoccaError else RoccaTextoMedio,
                                )
                            } else {
                                Text(
                                    text = "Certificado F 102 pendiente de emisión",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = RoccaAvisoTexto,
                                )
                                Text(
                                    text = "Completá y subí los requisitos para que el Administrador autorice y emita tu cartón.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = RoccaTextoSuave,
                                )
                            }
                        }
                    }
                }

                // Progreso de requisitos
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "Requisitos cargados",
                            style = MaterialTheme.typography.labelSmall,
                            color = RoccaTextoApagado,
                        )
                        Text(
                            text = "$cumplidos de $totalRequisitos cumplidos",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = RoccaAzulOscuro,
                        )
                    }
                    val progress = if (totalRequisitos > 0) cumplidos.toFloat() / totalRequisitos else 0f
                    LinearProgressIndicator(
                        progress = { progress },
                        color = if (cumplidos == totalRequisitos && totalRequisitos > 0) RoccaExitoTexto else RoccaRojo,
                        trackColor = RoccaBorde,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = onSubirRequisitos,
                        border = BorderStroke(1.dp, RoccaBordeInput),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("persona_requisitos_btn_${operador.id}"),
                    ) {
                        Icon(Icons.Default.Checklist, contentDescription = null, tint = RoccaRojo, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Requisitos y Adjuntos",
                            color = RoccaAzulOscuro,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    if (certificado != null && estado == EstadoRegistral.ACTIVO) {
                        Button(
                            onClick = onVerCarton,
                            colors = ButtonDefaults.buttonColors(containerColor = RoccaRojo),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("persona_carton_btn_${operador.id}"),
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cartón", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
