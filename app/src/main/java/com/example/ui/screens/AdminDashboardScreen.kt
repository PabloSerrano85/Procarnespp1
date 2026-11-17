package com.example.ui.screens

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.CertificadoEntity
import com.example.data.local.OperadorEntity
import com.example.data.local.UsuarioEntity
import com.example.domain.EstadoCertificado
import com.example.domain.EstadoRegistral
import com.example.domain.RoccaCatalogos
import com.example.ui.components.EstadoRegistralBadge
import com.example.ui.components.RoccaMetricTile
import com.example.ui.theme.JetBrainsMono
import com.example.ui.theme.RoccaAvisoBorde
import com.example.ui.theme.RoccaAvisoFondo
import com.example.ui.theme.RoccaAvisoTexto
import com.example.ui.theme.RoccaAzulOscuro
import com.example.ui.theme.RoccaBlanco
import com.example.ui.theme.RoccaBorde
import com.example.ui.theme.RoccaBordeInput
import com.example.ui.theme.RoccaError
import com.example.ui.theme.RoccaErrorFondo
import com.example.ui.theme.RoccaExitoClaro
import com.example.ui.theme.RoccaExitoTexto
import com.example.ui.theme.RoccaFondo
import com.example.ui.theme.RoccaHeaderFin
import com.example.ui.theme.RoccaRojo
import com.example.ui.theme.RoccaTarjeta
import com.example.ui.theme.RoccaTextoApagado
import com.example.ui.theme.RoccaTextoMedio

@Composable
fun AdminDashboardScreen(
    usuario: UsuarioEntity,
    operadores: List<OperadorEntity>,
    allCertificados: List<CertificadoEntity>,
    onAutorizarYEmitir: (Long) -> Unit,
    onSuspenderCertificado: (certificadoId: Long, motivo: String) -> Unit,
    onReactivarCertificado: (certificadoId: Long) -> Unit,
    onGestionarOperador: (OperadorEntity) -> Unit,
    onVerCarton: (OperadorEntity) -> Unit,
    onIrAPadron: () -> Unit,
    onIrAInscripcion: () -> Unit,
    onIrAModulos: () -> Unit,
) {
    val pendientesAutorizacion = operadores.filter { op ->
        val estado = EstadoRegistral.fromString(op.estadoRegistral)
        val hasActiveCert = allCertificados.any { it.operadorId == op.id && it.estado == EstadoCertificado.ACTIVO.name }
        estado == EstadoRegistral.PENDIENTE || (estado == EstadoRegistral.ACTIVO && !hasActiveCert)
    }

    val certificadosVigentes = allCertificados.filter { it.estado == EstadoCertificado.ACTIVO.name }
    val certificadosAlerta = allCertificados.count { cert ->
        val dias = RoccaCatalogos.diasParaVencimiento(cert.fechaVencimiento)
        cert.estado == EstadoCertificado.SUSPENDIDO.name || (dias != null && dias <= 30)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(RoccaFondo),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Banner Administrador
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
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = Color(0xFFFCD34D),
                                modifier = Modifier.size(22.dp),
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "PANEL DE ADMINISTRADOR • AUTORIDAD PROCARNE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFCD34D),
                            )
                            Text(
                                text = usuario.nombre,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = RoccaBlanco,
                            )
                            Text(
                                text = "Padrón provincial completo, autorización de operadores y emisión/gestión de certificados F 102.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFCBD5E1),
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Button(
                            onClick = onIrAPadron,
                            colors = ButtonDefaults.buttonColors(containerColor = RoccaRojo),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 46.dp),
                        ) {
                            Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Padrón", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = onIrAInscripcion,
                            border = BorderStroke(1.dp, Color(0x66FFFFFF)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 46.dp),
                        ) {
                            Icon(Icons.Default.NoteAdd, contentDescription = null, tint = RoccaBlanco, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Alta F 102", color = RoccaBlanco, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                        }
                        OutlinedButton(
                            onClick = onIrAModulos,
                            border = BorderStroke(1.dp, Color(0x66FFFFFF)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.heightIn(min = 46.dp),
                        ) {
                            Icon(Icons.Default.Apps, contentDescription = "Módulos II-VII", tint = RoccaBlanco, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        // KPIs Ejecutivos
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                RoccaMetricTile(
                    label = "Operadores",
                    value = operadores.size.toString(),
                    accent = RoccaAzulOscuro,
                    modifier = Modifier.weight(1f),
                )
                RoccaMetricTile(
                    label = "Por Autorizar",
                    value = pendientesAutorizacion.size.toString(),
                    accent = RoccaAvisoTexto,
                    modifier = Modifier.weight(1f),
                )
                RoccaMetricTile(
                    label = "Cert. Vigentes",
                    value = certificadosVigentes.size.toString(),
                    accent = RoccaExitoTexto,
                    modifier = Modifier.weight(1f),
                )
                RoccaMetricTile(
                    label = "Alertas Cert.",
                    value = certificadosAlerta.toString(),
                    accent = if (certificadosAlerta > 0) RoccaError else RoccaTextoApagado,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // Cola de Autorización y Emisión F 102
        item {
            Text(
                text = "Pendientes de Autorización y Emisión F 102 (${pendientesAutorizacion.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = RoccaAzulOscuro,
            )
        }

        if (pendientesAutorizacion.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = RoccaExitoClaro),
                    border = BorderStroke(1.dp, RoccaBorde),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = RoccaExitoTexto)
                        Text(
                            text = "No hay solicitudes pendientes de autorización.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = RoccaExitoTexto,
                        )
                    }
                }
            }
        } else {
            items(pendientesAutorizacion, key = { "pending_${it.id}" }) { op ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = RoccaBlanco),
                    border = BorderStroke(1.dp, RoccaAvisoBorde),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_pending_card_${op.id}"),
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(RoccaAvisoFondo)
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = op.nombreEstablecimiento,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = RoccaAzulOscuro,
                                )
                                Text(
                                    text = "${op.titular} • CUIT ${op.cuit}",
                                    fontFamily = JetBrainsMono,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = RoccaTextoApagado,
                                )
                            }
                            EstadoRegistralBadge(estadoRaw = op.estadoRegistral)
                        }
                        HorizontalDivider(color = RoccaBorde)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            OutlinedButton(
                                onClick = { onGestionarOperador(op) },
                                border = BorderStroke(1.dp, RoccaBordeInput),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.heightIn(min = 48.dp),
                            ) {
                                Text("Revisar", color = RoccaTextoMedio, style = MaterialTheme.typography.labelMedium)
                            }
                            Button(
                                onClick = { onAutorizarYEmitir(op.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = RoccaRojo),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("admin_autorizar_emitir_${op.id}"),
                            ) {
                                Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Autorizar y Emitir F 102",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                }
            }
        }

        // Gestión de Certificados Emitidos (CU-7)
        item {
            Text(
                text = "Gestión de Certificados F 102 Emitidos (${allCertificados.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = RoccaAzulOscuro,
            )
        }

        items(allCertificados.filter { it.estado != EstadoCertificado.RELEVADO.name }, key = { "cert_${it.id}" }) { cert ->
            val op = operadores.firstOrNull { it.id == cert.operadorId }
            val isActivo = cert.estado == EstadoCertificado.ACTIVO.name
            val dias = RoccaCatalogos.diasParaVencimiento(cert.fechaVencimiento)
            val porVencer = isActivo && dias != null && dias <= 30

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = RoccaBlanco),
                border = BorderStroke(1.dp, if (porVencer) RoccaAvisoBorde else RoccaBorde),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${cert.numeroCertificado} — ${op?.nombreEstablecimiento ?: "Operador #${cert.operadorId}"}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = RoccaAzulOscuro,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = "Emisión: ${RoccaCatalogos.formatearFechaCorta(cert.fechaEmision)} • Vence: ${RoccaCatalogos.formatearFechaCorta(cert.fechaVencimiento)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (porVencer) RoccaError else RoccaTextoApagado,
                                fontWeight = if (porVencer) FontWeight.Bold else FontWeight.Normal,
                            )
                        }
                        Surface(
                            color = if (isActivo) RoccaExitoClaro else RoccaErrorFondo,
                            shape = CircleShape,
                        ) {
                            Text(
                                text = cert.estado,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isActivo) RoccaExitoTexto else RoccaError,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (op != null) {
                            OutlinedButton(
                                onClick = { onVerCarton(op) },
                                border = BorderStroke(1.dp, RoccaBordeInput),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 44.dp),
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = RoccaRojo, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("PDF", color = RoccaAzulOscuro, style = MaterialTheme.typography.labelMedium)
                            }
                        }

                        if (isActivo) {
                            OutlinedButton(
                                onClick = { onSuspenderCertificado(cert.id, "Suspendido por revisión administrativa") },
                                border = BorderStroke(1.dp, RoccaError),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 44.dp),
                            ) {
                                Icon(Icons.Default.Block, contentDescription = null, tint = RoccaError, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Suspender", color = RoccaError, style = MaterialTheme.typography.labelMedium)
                            }
                        } else if (cert.estado == EstadoCertificado.SUSPENDIDO.name) {
                            Button(
                                onClick = { onReactivarCertificado(cert.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = RoccaExitoTexto),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 44.dp),
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Reactivar", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}
