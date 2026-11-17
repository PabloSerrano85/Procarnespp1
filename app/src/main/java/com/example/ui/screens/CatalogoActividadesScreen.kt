package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.RoccaCatalogos
import com.example.ui.theme.JetBrainsMono
import com.example.ui.theme.RoccaAzulOscuro
import com.example.ui.theme.RoccaBlanco
import com.example.ui.theme.RoccaBorde
import com.example.ui.theme.RoccaFondo
import com.example.ui.theme.RoccaRojo
import com.example.ui.theme.RoccaRojoClaro
import com.example.ui.theme.RoccaTarjeta
import com.example.ui.theme.RoccaTextoApagado
import com.example.ui.theme.RoccaTextoSuave

@Composable
fun CatalogoActividadesScreen(
    showModulosTab: Boolean = false,
    initialTab: Int = 0,
) {
    var selectedTab by remember(initialTab) { mutableIntStateOf(if (showModulosTab) initialTab else 0) }
    var selectedGrupo by remember { mutableStateOf<String?>(null) }
    var selectedSubgrupo by remember { mutableStateOf<String?>(null) }
    var query by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RoccaFondo),
    ) {
        if (showModulosTab) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = RoccaBlanco,
                contentColor = RoccaAzulOscuro,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = RoccaRojo,
                        height = 3.dp,
                    )
                },
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Actividades (CU-8)", fontWeight = FontWeight.Bold) },
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Módulos II–VII", fontWeight = FontWeight.Bold) },
                )
            }
            HorizontalDivider(color = RoccaBorde)
        }

        if (selectedTab == 1 && showModulosTab) {
            ModulosScreen()
            return@Column
        }

        val subgruposFiltrados = remember(selectedGrupo) {
            if (selectedGrupo == null) RoccaCatalogos.SUBGRUPOS_ACTIVIDAD
            else RoccaCatalogos.SUBGRUPOS_ACTIVIDAD.filter { it.grupoCodigo == selectedGrupo }
        }

        val actividadesFiltradas = remember(selectedGrupo, selectedSubgrupo, query) {
            val q = query.trim().lowercase()
            RoccaCatalogos.ACTIVIDADES_ECONOMICAS.filter { act ->
                val matchGrupo = selectedGrupo == null || act.grupoCodigo == selectedGrupo
                val matchSub = selectedSubgrupo == null || act.subgrupoCodigo == selectedSubgrupo
                val matchQ = q.isEmpty() ||
                    act.codigo.lowercase().contains(q) ||
                    act.nombre.lowercase().contains(q)
                matchGrupo && matchSub && matchQ
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
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
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = "Catálogo de Actividades Económicas (CU-8)",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = RoccaAzulOscuro,
                        )
                        Text(
                            text = "Jerarquía oficial Grupo → Subgrupo → Subsubgrupo → Actividad DGR/AFIP para el registro de operadores.",
                            style = MaterialTheme.typography.bodySmall,
                            color = RoccaTextoSuave,
                        )
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("Buscar por código AFIP/DGR o descripción…") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { query = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = roccaTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("catalogo_search_input"),
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        FilterChip(
                            selected = selectedGrupo == null,
                            onClick = {
                                selectedGrupo = null
                                selectedSubgrupo = null
                            },
                            label = { Text("Todos los Grupos") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RoccaRojoClaro,
                                selectedLabelColor = RoccaRojo,
                            ),
                        )
                        RoccaCatalogos.GRUPOS_ACTIVIDAD.forEach { grupo ->
                            FilterChip(
                                selected = selectedGrupo == grupo.codigo,
                                onClick = {
                                    selectedGrupo = if (selectedGrupo == grupo.codigo) null else grupo.codigo
                                    selectedSubgrupo = null
                                },
                                label = { Text("${grupo.codigo} • ${grupo.nombre}") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = RoccaRojoClaro,
                                    selectedLabelColor = RoccaRojo,
                                ),
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        subgruposFiltrados.forEach { sub ->
                            FilterChip(
                                selected = selectedSubgrupo == sub.codigo,
                                onClick = {
                                    selectedSubgrupo = if (selectedSubgrupo == sub.codigo) null else sub.codigo
                                },
                                label = { Text("Subgrupo ${sub.codigo}") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = RoccaRojoClaro,
                                    selectedLabelColor = RoccaRojo,
                                ),
                            )
                        }
                    }
                }
            }

            items(actividadesFiltradas, key = { it.codigo }) { act ->
                val grupoNombre = RoccaCatalogos.GRUPOS_ACTIVIDAD.firstOrNull { it.codigo == act.grupoCodigo }?.nombre ?: act.grupoCodigo
                val subNombre = RoccaCatalogos.SUBGRUPOS_ACTIVIDAD.firstOrNull { it.codigo == act.subgrupoCodigo }?.nombre ?: act.subgrupoCodigo

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = RoccaBlanco),
                    border = BorderStroke(1.dp, RoccaBorde),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(RoccaTarjeta)
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Surface(color = RoccaAzulOscuro, shape = RoundedCornerShape(6.dp)) {
                                Text(
                                    text = "COD. ${act.codigo}",
                                    fontFamily = JetBrainsMono,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = RoccaBlanco,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                )
                            }
                            Text(
                                text = "Grupo ${act.grupoCodigo} › ${act.subgrupoCodigo} › ${act.subsubgrupoCodigo}",
                                fontFamily = JetBrainsMono,
                                style = MaterialTheme.typography.labelSmall,
                                color = RoccaTextoApagado,
                            )
                        }
                        HorizontalDivider(color = RoccaBorde)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = act.nombre,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = RoccaAzulOscuro,
                            )
                            Text(
                                text = "$grupoNombre • $subNombre",
                                style = MaterialTheme.typography.bodySmall,
                                color = RoccaTextoApagado,
                            )
                        }
                    }
                }
            }
        }
    }
}
