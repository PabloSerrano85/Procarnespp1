package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PersonaEntity
import com.example.data.local.UsuarioEntity
import com.example.data.remote.GeorefSuggestion
import com.example.domain.EstadoRegistral
import com.example.domain.RolUsuario
import com.example.ui.theme.JetBrainsMono
import com.example.ui.theme.RoccaAvisoBorde
import com.example.ui.theme.RoccaAvisoFondo
import com.example.ui.theme.RoccaAvisoPill
import com.example.ui.theme.RoccaAvisoTexto
import com.example.ui.theme.RoccaAzulHeader
import com.example.ui.theme.RoccaAzulOscuro
import com.example.ui.theme.RoccaBlanco
import com.example.ui.theme.RoccaBorde
import com.example.ui.theme.RoccaBordeInput
import com.example.ui.theme.RoccaErrorPill
import com.example.ui.theme.RoccaErrorTexto
import com.example.ui.theme.RoccaExitoPill
import com.example.ui.theme.RoccaExitoTexto
import com.example.ui.theme.RoccaHeaderFin
import com.example.ui.theme.RoccaRojo
import com.example.ui.theme.RoccaRojoBorde
import com.example.ui.theme.RoccaRojoClaro
import com.example.ui.theme.RoccaTarjeta
import com.example.ui.theme.RoccaTexto
import com.example.ui.theme.RoccaTextoApagado
import com.example.ui.theme.RoccaTextoMedio

@Composable
fun RoccaInstitutionalHeader(
    usuario: UsuarioEntity?,
    onOpenPerfil: (() -> Unit)? = null,
    onLogout: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val gradient = Brush.horizontalGradient(
        colors = listOf(RoccaAzulHeader, RoccaAzulOscuro, RoccaHeaderFin)
    )
    val rol = usuario?.let { RolUsuario.fromString(it.rol) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(gradient)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(RoccaRojo)
                        .border(1.dp, RoccaRojoBorde, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "R",
                        color = RoccaBlanco,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        color = Color(0x33F59E0B),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, Color(0x4DFBBF24)),
                    ) {
                        Text(
                            text = "GOBIERNO DE SANTIAGO DEL ESTERO",
                            color = Color(0xFFFCD34D),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "ROCCA",
                            color = RoccaBlanco,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        if (rol != null) {
                            Surface(
                                onClick = { onOpenPerfil?.invoke() },
                                color = Color(0x33FFFFFF),
                                shape = CircleShape,
                                modifier = Modifier.testTag("header_role_pill"),
                            ) {
                                Text(
                                    text = rol.label.uppercase(),
                                    color = RoccaBlanco,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        } else {
                            Text(
                                text = "| Control Cadena Cárnica",
                                color = Color(0xFFCBD5E1),
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    Text(
                        text = usuario?.let { "${it.nombre} • ${it.email}" }
                            ?: "Dirección General de Agricultura y Ganadería • ProCarne",
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            if (usuario != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onOpenPerfil != null) {
                        IconButton(
                            onClick = onOpenPerfil,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("profile_button"),
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Mi Perfil y Rol",
                                tint = RoccaBlanco,
                            )
                        }
                    }
                    if (onLogout != null) {
                        IconButton(
                            onClick = onLogout,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("logout_button"),
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "Cerrar sesión (${usuario.email})",
                                tint = RoccaBlanco,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PerfilUsuarioDialog(
    usuario: UsuarioEntity,
    persona: PersonaEntity?,
    onDismiss: () -> Unit,
    onSwitchDemoRole: (RolUsuario) -> Unit,
    onLogout: () -> Unit,
) {
    val rolActual = RolUsuario.fromString(usuario.rol)
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = RoccaBlanco,
        title = {
            Text(
                text = "Mi Perfil (CU-10)",
                fontWeight = FontWeight.Bold,
                color = RoccaAzulOscuro,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    color = RoccaTarjeta,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, RoccaBorde),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = usuario.nombre,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = RoccaAzulOscuro,
                        )
                        Text(
                            text = usuario.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = RoccaTextoApagado,
                        )
                        Text(
                            text = "Rol activo: ${rolActual.label}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = RoccaRojo,
                        )
                        if (persona != null) {
                            HorizontalDivider(
                                color = RoccaBorde,
                                modifier = Modifier.padding(vertical = 4.dp),
                            )
                            Text(
                                text = "Entidad Persona vinculada:",
                                style = MaterialTheme.typography.labelSmall,
                                color = RoccaTextoApagado,
                            )
                            Text(
                                text = "${persona.nombre} (${persona.tipo})",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = "${persona.tipoDocumento}: ${persona.identificador}",
                                fontFamily = JetBrainsMono,
                                style = MaterialTheme.typography.bodySmall,
                                color = RoccaTextoMedio,
                            )
                        }
                    }
                }

                Text(
                    text = "Cambiar vista de Dashboard (3 roles demo):",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = RoccaAzulOscuro,
                )

                RolUsuario.entries.forEach { rol ->
                    val selected = rol == rolActual
                    Surface(
                        onClick = {
                            onDismiss()
                            if (!selected) onSwitchDemoRole(rol)
                        },
                        shape = RoundedCornerShape(10.dp),
                        color = if (selected) RoccaRojoClaro else RoccaBlanco,
                        border = BorderStroke(1.dp, if (selected) RoccaRojo else RoccaBordeInput),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("switch_role_${rol.key}"),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = null,
                                tint = if (selected) RoccaRojo else RoccaTextoApagado,
                                modifier = Modifier.size(18.dp),
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = rol.label,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) RoccaRojo else RoccaAzulOscuro,
                                )
                                Text(
                                    text = rol.descripcion,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = RoccaTextoApagado,
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onDismiss()
                    onLogout()
                },
                colors = ButtonDefaults.buttonColors(containerColor = RoccaRojo),
            ) {
                Text("Cerrar sesión")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar", color = RoccaTextoMedio)
            }
        },
    )
}

@Composable
fun EstadoRegistralBadge(
    estadoRaw: String,
    modifier: Modifier = Modifier,
) {
    val estado = EstadoRegistral.fromString(estadoRaw)
    val (bg, fg, icon) = when (estado) {
        EstadoRegistral.ACTIVO -> Triple(RoccaExitoPill, RoccaExitoTexto, Icons.Default.CheckCircle)
        EstadoRegistral.PENDIENTE -> Triple(RoccaAvisoPill, RoccaAvisoTexto, Icons.Default.Schedule)
        EstadoRegistral.SUSPENDIDO -> Triple(RoccaErrorPill, RoccaErrorTexto, Icons.Default.Warning)
        EstadoRegistral.BAJADO -> Triple(RoccaErrorPill, RoccaErrorTexto, Icons.Default.Close)
    }

    Surface(
        color = bg,
        shape = CircleShape,
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = fg,
                modifier = Modifier.size(13.dp),
            )
            Text(
                text = estado.labelUi,
                color = fg,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
fun SelloVerdeBadge(modifier: Modifier = Modifier) {
    Surface(
        color = RoccaExitoPill,
        shape = RoundedCornerShape(4.dp),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Eco,
                contentDescription = null,
                tint = RoccaExitoTexto,
                modifier = Modifier.size(11.dp),
            )
            Text(
                text = "Sello Verde",
                color = RoccaExitoTexto,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
fun RoccaSectionCard(
    numero: String,
    titulo: String,
    descripcion: String,
    modifier: Modifier = Modifier,
    accion: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = RoccaBlanco),
        border = BorderStroke(1.dp, RoccaBorde),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(RoccaTarjeta)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(RoccaRojo),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = numero,
                            color = RoccaBlanco,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Black,
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = titulo,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = RoccaAzulOscuro,
                        )
                        Text(
                            text = descripcion,
                            style = MaterialTheme.typography.bodySmall,
                            color = RoccaTextoApagado,
                        )
                    }
                }
                if (accion != null) {
                    accion()
                }
            }
            HorizontalDivider(color = RoccaBorde)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                content = content,
            )
        }
    }
}

@Composable
fun RoccaMetricTile(
    label: String,
    value: String,
    accent: Color = RoccaAzulOscuro,
    containerColor: Color = RoccaBlanco,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        border = BorderStroke(1.dp, RoccaBorde),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = accent,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = RoccaTextoApagado,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeorefAutocompleteField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    suggestions: List<GeorefSuggestion>,
    placeholder: String,
    testTag: String,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded && suggestions.isNotEmpty(),
        onExpandedChange = { expanded = it },
        modifier = modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                onValueChange(it)
                expanded = true
            },
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            singleLine = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded && suggestions.isNotEmpty())
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = RoccaRojo,
                unfocusedBorderColor = RoccaBordeInput,
                focusedContainerColor = RoccaBlanco,
                unfocusedContainerColor = RoccaBlanco,
            ),
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryEditable)
                .fillMaxWidth()
                .testTag(testTag),
        )

        ExposedDropdownMenu(
            expanded = expanded && suggestions.isNotEmpty(),
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(RoccaBlanco)
                .heightIn(max = 220.dp),
        ) {
            suggestions.forEach { suggestion ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(
                                text = suggestion.nombre,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = RoccaTexto,
                            )
                            Text(
                                text = suggestion.subtitulo,
                                style = MaterialTheme.typography.bodySmall,
                                color = RoccaTextoApagado,
                            )
                        }
                    },
                    onClick = {
                        onValueChange(suggestion.nombre)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
fun InfoPillButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        color = RoccaAvisoFondo,
        border = BorderStroke(1.dp, RoccaAvisoBorde),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
            .heightIn(min = 48.dp)
            .testTag(testTag),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFFB45309),
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF78350F),
            )
        }
    }
}
