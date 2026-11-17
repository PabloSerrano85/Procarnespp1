package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Search
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
import com.example.data.local.AdjuntoEntity
import com.example.data.local.OperadorEntity
import com.example.data.local.RequisitoEntity
import com.example.data.local.UsuarioEntity
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
import com.example.ui.theme.RoccaError
import com.example.ui.theme.RoccaExitoTexto
import com.example.ui.theme.RoccaFondo
import com.example.ui.theme.RoccaHeaderFin
import com.example.ui.theme.RoccaRojo
import com.example.ui.theme.RoccaTarjeta
import com.example.ui.theme.RoccaTextoApagado
import com.example.ui.theme.RoccaTextoMedio

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SupervisorDashboardScreen(
    usuario: UsuarioEntity,
    operadores: List<OperadorEntity>,
    allRequisitos: List<RequisitoEntity>,
    allAdjuntos: List<AdjuntoEntity>,
    onRevisarLegajo: (OperadorEntity) -> Unit,
    onCambiarEstado: (Long, EstadoRegistral) -> Unit,
    onIrAPadron: () -> Unit,
) {
    val pendientes = operadores.filter { EstadoRegistral.fromString(it.estadoRegistral) == EstadoRegistral.PENDIENTE }
    val suspendidos = operadores.filter { EstadoRegistral.fromString(it.estadoRegistral) == EstadoRegistral.SUSPENDIDO }
    val activos = operadores.filter { EstadoRegistral.fromString(it.estadoRegistral) == EstadoRegistral.ACTIVO }

    val conReqIncompletos = operadores.count { op ->
        val reqs = allRequisitos.filter { it.operadorId == op.id && it.obligatorio }
        reqs.any { !it.cumplido }
    }

    // Cola priorizada: pendientes primero, luego con requisitos incompletos, luego el resto
    val colaRevision = operadores.sortedWith(
        compareBy<OperadorEntity> {
            when (EstadoRegistral.fromString(it.estadoRegistral)) {
                EstadoRegistral.PENDIENTE -> 0
                EstadoRegistral.SUSPENDIDO -> 1
                EstadoRegistral.ACTIVO -> 2
                EstadoRegistral.BAJADO -> 3
            }
        }.thenBy { op ->
            val reqs = allRequisitos.filter { it.operadorId == op.id && it.obligatorio }
            if (reqs.any { !it.cumplido }) 0 else 1
        }
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(RoccaFondo),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
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
                    verticalArrangement = Arrangement.spacedBy(8.dp),
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
                                imageVector = Icons.Default.FactCheck,
                                contentDescription = null,
                                tint = Color(0xFFFCD34D),
                                modifier = Modifier.size(22.dp),
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "PANEL DE SUPERVISOR • FISCALIZACIÓN",
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
                                text = "Verificación de requisitos (CU-2), auditoría de adjuntos y control de ciclo de vida (CU-6).",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFCBD5E1),
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onIrAPadron,
                        border = BorderStroke(1.dp, Color(0x66FFFFFF)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 46.dp),
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = RoccaBlanco, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ir al Padrón Completo", color = RoccaBlanco, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                RoccaMetricTile(
                    label = "Padrón Total",
                    value = operadores.size.toString(),
                    accent = RoccaAzulOscuro,
                    modifier = Modifier.weight(1f),
                )
                RoccaMetricTile(
                    label = "Pendientes",
                    value = pendientes.size.toString(),
                    accent = RoccaAvisoTexto,
                    modifier = Modifier.weight(1f),
                )
                RoccaMetricTile(
                    label = "Req. Incompl.",
                    value = conReqIncompletos.toString(),
                    accent = RoccaRojo,
                    modifier = Modifier.weight(1f),
                )
                RoccaMetricTile(
                    label = "Suspendidos",
                    value = suspendidos.size.toString(),
                    accent = RoccaError,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // Resumen por categoría
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = RoccaBlanco),
                border = BorderStroke(1.dp, RoccaBorde),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Operadores por Categoría (${activos.size} habilitados)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = RoccaAzulOscuro,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        RoccaCatalogos.CATEGORIAS_CARNICAS.forEach { cat ->
                            val count = operadores.count { cat.id in it.categoriasLista }
                            if (count > 0) {
                                Surface(
                                    color = RoccaTarjeta,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, RoccaBorde),
                                ) {
                                    Text(
                                        text = "${cat.nombre}: $count",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = RoccaTextoMedio,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Cola de Revisión de Legajos y Requisitos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = RoccaAzulOscuro,
            )
        }

        items(colaRevision, key = { it.id }) { op ->
            val reqs = allRequisitos.filter { it.operadorId == op.id }
            val cumplidos = reqs.count { it.cumplido }
            val adjCount = allAdjuntos.count { it.operadorId == op.id }
            val estado = EstadoRegistral.fromString(op.estadoRegistral)

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = RoccaBlanco),
                border = BorderStroke(1.dp, if (estado == EstadoRegistral.PENDIENTE) RoccaAvisoBorde else RoccaBorde),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("supervisor_card_${op.id}"),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (estado == EstadoRegistral.PENDIENTE) RoccaAvisoFondo else RoccaTarjeta)
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
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        EstadoRegistralBadge(estadoRaw = op.estadoRegistral)
                    }

                    HorizontalDivider(color = RoccaBorde)

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "Requisitos: $cumplidos/${reqs.size} cumplidos • Adjuntos: $adjCount documento${if (adjCount != 1) "s" else ""}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = RoccaTextoMedio,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            OutlinedButton(
                                onClick = { onRevisarLegajo(op) },
                                border = BorderStroke(1.dp, RoccaAzulOscuro),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("supervisor_revisar_${op.id}"),
                            ) {
                                Icon(Icons.Default.FolderOpen, contentDescription = null, tint = RoccaAzulOscuro, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Revisar Legajo", color = RoccaAzulOscuro, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            }

                            if (estado == EstadoRegistral.PENDIENTE || estado == EstadoRegistral.SUSPENDIDO) {
                                Button(
                                    onClick = { onCambiarEstado(op.id, EstadoRegistral.ACTIVO) },
                                    colors = ButtonDefaults.buttonColors(containerColor = RoccaExitoTexto),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .heightIn(min = 48.dp)
                                        .testTag("supervisor_activar_${op.id}"),
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (estado == EstadoRegistral.PENDIENTE) "Activar" else "Reactivar",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            } else if (estado == EstadoRegistral.ACTIVO) {
                                OutlinedButton(
                                    onClick = { onCambiarEstado(op.id, EstadoRegistral.SUSPENDIDO) },
                                    border = BorderStroke(1.dp, RoccaAvisoTexto),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .heightIn(min = 48.dp)
                                        .testTag("supervisor_suspender_${op.id}"),
                                ) {
                                    Icon(Icons.Default.Block, contentDescription = null, tint = RoccaAvisoTexto, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Suspender", color = RoccaAvisoTexto, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
