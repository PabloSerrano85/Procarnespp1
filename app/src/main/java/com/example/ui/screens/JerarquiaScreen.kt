package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.OperadorEntity
import com.example.data.local.PersonaEntity
import com.example.domain.RoccaCatalogos
import com.example.ui.components.EstadoRegistralBadge
import com.example.ui.theme.JetBrainsMono
import com.example.ui.theme.RoccaAzulOscuro
import com.example.ui.theme.RoccaBlanco
import com.example.ui.theme.RoccaBorde
import com.example.ui.theme.RoccaFondo
import com.example.ui.theme.RoccaRojo
import com.example.ui.theme.RoccaRojoClaro
import com.example.ui.theme.RoccaTarjeta
import com.example.ui.theme.RoccaTextoApagado
import com.example.ui.theme.RoccaTextoMedio
import com.example.ui.theme.RoccaTextoSuave
import com.example.ui.viewmodel.GrupoPersonaUi

@Composable
fun JerarquiaScreen(
    grupos: List<GrupoPersonaUi>,
    personas: List<PersonaEntity>,
    onVerCarton: (OperadorEntity) -> Unit,
    onGestionarOperador: (OperadorEntity) -> Unit,
    onAgregarSucursal: (PersonaEntity) -> Unit,
) {
    val totalSucursales = grupos.sumOf { it.sucursales.size }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(RoccaFondo),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
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
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "Jerarquía: Personas y Sucursales",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = RoccaAzulOscuro,
                    )
                    Text(
                        text = "${grupos.size} persona${if (grupos.size != 1) "s" else ""} titular${if (grupos.size != 1) "es" else ""} con $totalSucursales sucursal${if (totalSucursales != 1) "es" else ""} en total.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = RoccaTextoSuave,
                    )
                }
            }
        }

        if (grupos.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = RoccaBlanco),
                    border = BorderStroke(1.dp, RoccaBorde),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "No hay operadores registrados en la jerarquía.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = RoccaTextoApagado,
                        )
                    }
                }
            }
        } else {
            items(grupos, key = { it.cuit }) { grupo ->
                val personaMatch = personas.firstOrNull { it.id == grupo.personaId }
                    ?: personas.firstOrNull { it.identificador == grupo.cuit }
                GrupoPersonaCard(
                    grupo = grupo,
                    onVerCarton = onVerCarton,
                    onGestionarOperador = onGestionarOperador,
                    onAgregarSucursal = personaMatch?.let { p -> { onAgregarSucursal(p) } },
                )
            }
        }
    }
}

@Composable
private fun GrupoPersonaCard(
    grupo: GrupoPersonaUi,
    onVerCarton: (OperadorEntity) -> Unit,
    onGestionarOperador: (OperadorEntity) -> Unit,
    onAgregarSucursal: (() -> Unit)?,
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = RoccaBlanco),
        border = BorderStroke(1.dp, RoccaBorde),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Cabecera Persona Titular
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(RoccaTarjeta)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(RoccaRojoClaro),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = RoccaRojo,
                        modifier = Modifier.size(20.dp),
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = grupo.nombreTitular,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = RoccaAzulOscuro,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "CUIT: ${grupo.cuit}",
                        fontFamily = JetBrainsMono,
                        style = MaterialTheme.typography.bodySmall,
                        color = RoccaTextoApagado,
                    )
                }

                Surface(
                    color = RoccaBorde,
                    shape = CircleShape,
                ) {
                    Text(
                        text = "${grupo.sucursales.size} sucursal${if (grupo.sucursales.size != 1) "es" else ""}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = RoccaTextoMedio,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }

            HorizontalDivider(color = RoccaBorde)

            // Lista de sucursales
            grupo.sucursales.forEachIndexed { index, sucursal ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onGestionarOperador(sucursal) }
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                        .testTag("jerarquia_sucursal_${sucursal.id}"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Apartment,
                        contentDescription = null,
                        tint = RoccaTextoApagado,
                        modifier = Modifier.size(18.dp),
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${sucursal.nombreEstablecimiento} (N° ${sucursal.numeroCertificadoF102})",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = RoccaAzulOscuro,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = "${RoccaCatalogos.nombreCategoria(sucursal.categoria)} • ${sucursal.domicilioFormateado}",
                            style = MaterialTheme.typography.bodySmall,
                            color = RoccaTextoApagado,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    EstadoRegistralBadge(estadoRaw = sucursal.estadoRegistral)
                    IconButton(
                        onClick = { onVerCarton(sucursal) },
                        modifier = Modifier.size(48.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "Ver Cartón F 102",
                            tint = RoccaRojo,
                        )
                    }
                }
                if (index < grupo.sucursales.lastIndex) {
                    HorizontalDivider(color = RoccaBorde)
                }
            }

            if (onAgregarSucursal != null) {
                HorizontalDivider(color = RoccaBorde)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(
                        onClick = onAgregarSucursal,
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("agregar_sucursal_${grupo.personaId}"),
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddBusiness,
                            contentDescription = null,
                            tint = RoccaRojo,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Agregar Sucursal (+ F 102)",
                            color = RoccaRojo,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}
