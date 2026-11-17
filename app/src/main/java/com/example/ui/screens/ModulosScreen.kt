package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.RoccaAvisoBorde
import com.example.ui.theme.RoccaAvisoFondo
import com.example.ui.theme.RoccaAvisoTextoOscuro
import com.example.ui.theme.RoccaAzulOscuro
import com.example.ui.theme.RoccaBlanco
import com.example.ui.theme.RoccaBorde
import com.example.ui.theme.RoccaFondo
import com.example.ui.theme.RoccaRojo
import com.example.ui.theme.RoccaRojoClaro
import com.example.ui.theme.RoccaTextoApagado
import com.example.ui.theme.RoccaTextoSuave

private data class ModuloInfo(
    val titulo: String,
    val descripcion: String,
    val icon: ImageVector,
    val estado: String,
    val portalUrl: String? = null,
)

private val MODULOS_PROCARNE = listOf(
    ModuloInfo(
        titulo = "Monitor Estratégico",
        descripcion = "Panel de indicadores estratégicos, tableros de control y trazabilidad de la cadena cárnica provincial.",
        icon = Icons.Default.Analytics,
        estado = "En desarrollo • Sprint 2",
    ),
    ModuloInfo(
        titulo = "Remitos REC y Tránsito",
        descripcion = "Gestión de remitos de la Red de Comercialización (REC) y tránsitos de ganado y carnes entre jurisdicciones.",
        icon = Icons.Default.Description,
        estado = "En desarrollo • Sprint 2",
    ),
    ModuloInfo(
        titulo = "Fiscalización en Ruta",
        descripcion = "Control y fiscalización de transporte de carnes y ganado en ruta, con geolocalización y verificación de remitos.",
        icon = Icons.Default.LocalShipping,
        estado = "En desarrollo • Sprint 3",
    ),
    ModuloInfo(
        titulo = "Faena en Origen",
        descripcion = "Registro y control de faena en origen (establecimientos de cría y engorde) con trazabilidad de origen.",
        icon = Icons.Default.Factory,
        estado = "En desarrollo • Sprint 3",
    ),
    ModuloInfo(
        titulo = "Portal Público ProCarne",
        descripcion = "Consulta pública de operadores habilitados, cartones F 102 vigentes y datos abiertos de la cadena cárnica.",
        icon = Icons.Default.Public,
        estado = "Disponible en línea",
        portalUrl = "https://procarnesgo.netlify.app/",
    ),
)

@Composable
fun ModulosScreen() {
    val context = LocalContext.current

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
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "Ecosistema ROCCA (Módulos II–VII)",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = RoccaAzulOscuro,
                    )
                    Text(
                        text = "El Módulo I (Operadores, Jerarquía y F 102) está activo. Los módulos complementarios forman parte de los siguientes sprints de ProCarne.",
                        style = MaterialTheme.typography.bodySmall,
                        color = RoccaTextoSuave,
                    )
                }
            }
        }

        items(MODULOS_PROCARNE, key = { it.titulo }) { modulo ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = RoccaBlanco),
                border = BorderStroke(1.dp, RoccaBorde),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(RoccaRojoClaro),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = modulo.icon,
                                contentDescription = null,
                                tint = RoccaRojo,
                                modifier = Modifier.size(22.dp),
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = modulo.titulo,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = RoccaAzulOscuro,
                            )
                            Surface(
                                color = RoccaAvisoFondo,
                                shape = CircleShape,
                                border = BorderStroke(1.dp, RoccaAvisoBorde),
                            ) {
                                Text(
                                    text = modulo.estado,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = RoccaAvisoTextoOscuro,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                )
                            }
                        }
                    }

                    Text(
                        text = modulo.descripcion,
                        style = MaterialTheme.typography.bodySmall,
                        color = RoccaTextoApagado,
                    )

                    if (modulo.portalUrl != null) {
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(modulo.portalUrl))
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RoccaRojo),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("abrir_portal_publico_button"),
                        ) {
                            Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Abrir Portal Público ProCarne", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
