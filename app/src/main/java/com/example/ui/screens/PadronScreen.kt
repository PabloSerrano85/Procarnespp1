package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.OperadorEntity
import com.example.domain.EstadoRegistral
import com.example.domain.RoccaCatalogos
import com.example.domain.RolUsuario
import com.example.export.PadronCsvExporter
import com.example.ui.components.EstadoRegistralBadge
import com.example.ui.components.InfoPillButton
import com.example.ui.components.SelloVerdeBadge
import com.example.ui.theme.JetBrainsMono
import com.example.ui.theme.RoccaAzulOscuro
import com.example.ui.theme.RoccaBlanco
import com.example.ui.theme.RoccaBorde
import com.example.ui.theme.RoccaBordeInput
import com.example.ui.theme.RoccaFondo
import com.example.ui.theme.RoccaRojo
import com.example.ui.theme.RoccaRojoClaro
import com.example.ui.theme.RoccaTarjeta
import com.example.ui.theme.RoccaTexto
import com.example.ui.theme.RoccaTextoApagado
import com.example.ui.theme.RoccaTextoMedio
import com.example.ui.theme.RoccaTextoSuave

@Composable
fun PadronScreen(
    currentRole: RolUsuario,
    operadores: List<OperadorEntity>,
    busqueda: String,
    filtroEstado: EstadoRegistral?,
    filtroCategoria: String?,
    onBusquedaChange: (String) -> Unit,
    onFiltroEstadoChange: (EstadoRegistral?) -> Unit,
    onFiltroCategoriaChange: (String?) -> Unit,
    onVerCarton: (OperadorEntity) -> Unit,
    onGestionarOperador: (OperadorEntity) -> Unit,
    onNotify: (String) -> Unit,
) {
    val context = LocalContext.current
    val esPersona = currentRole == RolUsuario.PERSONA

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(RoccaFondo),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
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
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = if (esPersona) "CU-5 • Mis Legajos y Sucursales por CUIT" else "Dirección General de Agricultura y Ganadería • SDE",
                        style = MaterialTheme.typography.bodySmall,
                        color = RoccaTextoApagado,
                    )
                    Text(
                        text = if (esPersona) "Mis Sucursales Registradas" else "Padrón de Operadores de Carne",
                        style = MaterialTheme.typography.headlineMedium,
                        color = RoccaAzulOscuro,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "${operadores.size} establecimiento${if (operadores.size != 1) "s" else ""} • listado por sucursal",
                        style = MaterialTheme.typography.bodyMedium,
                        color = RoccaTextoSuave,
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        val primerActivo = operadores.firstOrNull {
                            EstadoRegistral.fromString(it.estadoRegistral) == EstadoRegistral.ACTIVO
                        }
                        if (primerActivo != null) {
                            InfoPillButton(
                                text = "Ver F 102",
                                icon = Icons.Default.PictureAsPdf,
                                onClick = { onVerCarton(primerActivo) },
                                testTag = "header_ver_carton_button",
                                modifier = Modifier.weight(1f),
                            )
                        }
                        OutlinedButton(
                            onClick = {
                                if (operadores.isEmpty()) {
                                    onNotify("No hay operadores para exportar.")
                                } else {
                                    val file = PadronCsvExporter.exportCsv(context, operadores)
                                    val uri = PadronCsvExporter.getShareUri(context, file)
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/csv"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        putExtra(Intent.EXTRA_SUBJECT, "Padrón Operadores ROCCA")
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Exportar Padrón CSV"))
                                    onNotify("CSV generado: ${file.name}")
                                }
                            },
                            border = BorderStroke(1.dp, RoccaBordeInput),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("export_csv_button"),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                tint = RoccaTextoMedio,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Exportar CSV",
                                style = MaterialTheme.typography.labelMedium,
                                color = RoccaTextoMedio,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }
        }

        // Búsqueda y filtros (Estado + Categoría per CU-5)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = busqueda,
                    onValueChange = onBusquedaChange,
                    placeholder = { Text("Buscar por N° Cartón, CUIT, Establecimiento…") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (busqueda.isNotEmpty()) {
                            IconButton(onClick = { onBusquedaChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpiar búsqueda")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = roccaTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("padron_search_input"),
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        selected = filtroEstado == null,
                        onClick = { onFiltroEstadoChange(null) },
                        label = { Text("Todos los Estados") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RoccaRojoClaro,
                            selectedLabelColor = RoccaRojo,
                        ),
                        modifier = Modifier.testTag("filter_estado_todos"),
                    )
                    EstadoRegistral.entries.forEach { estado ->
                        FilterChip(
                            selected = filtroEstado == estado,
                            onClick = {
                                onFiltroEstadoChange(if (filtroEstado == estado) null else estado)
                            },
                            label = { Text(estado.labelUi) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RoccaRojoClaro,
                                selectedLabelColor = RoccaRojo,
                            ),
                            modifier = Modifier.testTag("filter_estado_${estado.name.lowercase()}"),
                        )
                    }
                }

                // Filtro por Categoría (CU-5)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        selected = filtroCategoria == null,
                        onClick = { onFiltroCategoriaChange(null) },
                        label = { Text("Todas las Categorías") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RoccaRojoClaro,
                            selectedLabelColor = RoccaRojo,
                        ),
                    )
                    RoccaCatalogos.CATEGORIAS_CARNICAS.forEach { cat ->
                        FilterChip(
                            selected = filtroCategoria == cat.id,
                            onClick = {
                                onFiltroCategoriaChange(if (filtroCategoria == cat.id) null else cat.id)
                            },
                            label = { Text(cat.nombre) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RoccaRojoClaro,
                                selectedLabelColor = RoccaRojo,
                            ),
                            modifier = Modifier.testTag("filter_cat_${cat.id.lowercase()}"),
                        )
                    }
                }
            }
        }

        if (operadores.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = RoccaBlanco),
                    border = BorderStroke(1.dp, RoccaBorde),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = null,
                            tint = RoccaTextoApagado,
                            modifier = Modifier.size(40.dp),
                        )
                        Text(
                            text = "No hay operadores que coincidan",
                            style = MaterialTheme.typography.titleMedium,
                            color = RoccaAzulOscuro,
                        )
                        Text(
                            text = "Probá cambiando los filtros o registrá una nueva sucursal en Formulario F 102.",
                            style = MaterialTheme.typography.bodySmall,
                            color = RoccaTextoApagado,
                        )
                    }
                }
            }
        } else {
            items(operadores, key = { it.id }) { operador ->
                OperadorPadronCard(
                    operador = operador,
                    currentRole = currentRole,
                    onVerCarton = { onVerCarton(operador) },
                    onGestionar = { onGestionarOperador(operador) },
                )
            }
        }
    }
}

@Composable
private fun OperadorPadronCard(
    operador: OperadorEntity,
    currentRole: RolUsuario,
    onVerCarton: () -> Unit,
    onGestionar: () -> Unit,
) {
    val isActivo = EstadoRegistral.fromString(operador.estadoRegistral) == EstadoRegistral.ACTIVO

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = RoccaBlanco),
        border = BorderStroke(1.dp, RoccaBorde),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("operador_card_${operador.id}"),
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Surface(
                        color = RoccaAzulOscuro,
                        shape = RoundedCornerShape(6.dp),
                    ) {
                        Text(
                            text = "F102 • N° ${operador.numeroCertificadoF102}",
                            fontFamily = JetBrainsMono,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = RoccaBlanco,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }
                    if (isActivo) {
                        SelloVerdeBadge()
                    }
                }
                EstadoRegistralBadge(estadoRaw = operador.estadoRegistral)
            }

            HorizontalDivider(color = RoccaBorde)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = operador.razonSocial,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = RoccaAzulOscuro,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Badge,
                        contentDescription = null,
                        tint = RoccaTextoApagado,
                        modifier = Modifier.size(15.dp),
                    )
                    Text(
                        text = "${operador.titular} • CUIT ${operador.cuit}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = RoccaTextoMedio,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Business,
                        contentDescription = null,
                        tint = RoccaTextoApagado,
                        modifier = Modifier.size(15.dp),
                    )
                    val especies = operador.tiposCarneCsv.ifBlank {
                        RoccaCatalogos.especiesParaCategoria(operador.categoria)
                    }
                    Text(
                        text = "${RoccaCatalogos.nombreCategoria(operador.categoria)} • Carnes: $especies",
                        style = MaterialTheme.typography.bodySmall,
                        color = RoccaTexto,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = RoccaTextoApagado,
                        modifier = Modifier.size(15.dp),
                    )
                    Text(
                        text = operador.domicilioFormateado.ifBlank { "Santiago del Estero" },
                        style = MaterialTheme.typography.bodySmall,
                        color = RoccaTextoApagado,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = onGestionar,
                        border = BorderStroke(1.dp, RoccaBordeInput),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("gestionar_operador_${operador.id}"),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = RoccaTextoMedio,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (currentRole == RolUsuario.PERSONA) "Requisitos y Legajo" else "Gestionar",
                            style = MaterialTheme.typography.labelMedium,
                            color = RoccaTextoMedio,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    Button(
                        onClick = onVerCarton,
                        colors = ButtonDefaults.buttonColors(containerColor = RoccaRojo),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("ver_f102_${operador.id}"),
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Ver F102",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}
