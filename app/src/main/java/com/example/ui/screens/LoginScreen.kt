package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.RolUsuario
import com.example.ui.components.RoccaInstitutionalHeader
import com.example.ui.theme.RoccaAzulOscuro
import com.example.ui.theme.RoccaBlanco
import com.example.ui.theme.RoccaBorde
import com.example.ui.theme.RoccaBordeInput
import com.example.ui.theme.RoccaError
import com.example.ui.theme.RoccaErrorFondo
import com.example.ui.theme.RoccaFondo
import com.example.ui.theme.RoccaRojo
import com.example.ui.theme.RoccaRojoClaro
import com.example.ui.theme.RoccaTarjeta
import com.example.ui.theme.RoccaTexto
import com.example.ui.theme.RoccaTextoApagado
import com.example.ui.viewmodel.AuthUiState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LoginScreen(
    authState: AuthUiState,
    onLogin: (email: String, password: String) -> Unit,
    onLoginDemoRole: (RolUsuario) -> Unit,
    onSignup: (
        nombre: String,
        tipoDoc: String,
        documento: String,
        email: String,
        password: String,
        passwordConfirm: String,
        rol: RolUsuario,
    ) -> Unit,
    onClearError: () -> Unit,
    onOpenPortalPublico: () -> Unit,
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = login, 1 = signup
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordConfirm by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var tipoDoc by remember { mutableStateOf("CUIT") }
    var documento by remember { mutableStateOf("") }
    var selectedRol by remember { mutableStateOf(RolUsuario.PERSONA) }
    var showPassword by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RoccaFondo)
            .imePadding()
            .navigationBarsPadding(),
    ) {
        RoccaInstitutionalHeader(usuario = null)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = 520.dp)
                    .fillMaxWidth()
                    .height(128.dp)
                    .clip(RoundedCornerShape(14.dp)),
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_rocca_banner_1794924138420),
                    contentDescription = "Banner institucional ProCarne SDE",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0x330F172A), Color(0xCC0F172A))
                            )
                        )
                        .padding(14.dp),
                    contentAlignment = Alignment.BottomStart,
                ) {
                    Column {
                        Text(
                            text = "Módulo I • Registro Provincial",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFFFCD34D),
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "Operadores de la Cadena Cárnica",
                            style = MaterialTheme.typography.titleMedium,
                            color = RoccaBlanco,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Card(
                modifier = Modifier
                    .widthIn(max = 520.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = RoccaBlanco),
                border = BorderStroke(1.dp, RoccaBorde),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
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
                            onClick = {
                                selectedTab = 0
                                onClearError()
                            },
                            text = {
                                Text(
                                    "Iniciar sesión",
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                                )
                            },
                            modifier = Modifier.testTag("tab_login"),
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = {
                                selectedTab = 1
                                onClearError()
                            },
                            text = {
                                Text(
                                    "Crear cuenta",
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                                )
                            },
                            modifier = Modifier.testTag("tab_signup"),
                        )
                    }

                    HorizontalDivider(color = RoccaBorde)

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = if (selectedTab == 0) {
                                "Ingresá con tu email o probá directamente los dashboards de los 3 roles asignados."
                            } else {
                                "Registrate con usuario, email y CUIT: se generará automáticamente tu entidad Persona para dar de alta tus sucursales."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = RoccaTextoApagado,
                        )

                        AnimatedVisibility(visible = selectedTab == 1) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedTextField(
                                    value = nombre,
                                    onValueChange = { nombre = it },
                                    label = { Text("Usuario / Nombre o Razón Social *") },
                                    placeholder = { Text("Juan Pérez / LA NORITA S.R.L.") },
                                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                    singleLine = true,
                                    colors = roccaTextFieldColors(),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("signup_name_input"),
                                )

                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "Rol asignado *",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = RoccaTexto,
                                    )
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        RolUsuario.entries.forEach { rol ->
                                            FilterChip(
                                                selected = selectedRol == rol,
                                                onClick = { selectedRol = rol },
                                                label = { Text(rol.label, fontWeight = FontWeight.SemiBold) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = RoccaRojoClaro,
                                                    selectedLabelColor = RoccaRojo,
                                                ),
                                                modifier = Modifier.testTag("signup_rol_${rol.key}"),
                                            )
                                        }
                                    }
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "Tipo de identificación tributaria *",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = RoccaTexto,
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        listOf("CUIT", "DNI").forEach { tipo ->
                                            FilterChip(
                                                selected = tipoDoc == tipo,
                                                onClick = { tipoDoc = tipo },
                                                label = { Text(tipo, fontWeight = FontWeight.Bold) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = RoccaRojoClaro,
                                                    selectedLabelColor = RoccaRojo,
                                                ),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(48.dp)
                                                    .testTag("doc_type_${tipo.lowercase()}"),
                                            )
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = documento,
                                    onValueChange = { documento = it },
                                    label = {
                                        Text(
                                            if (tipoDoc == "CUIT") "Número de CUIT * (genera entidad Persona)"
                                            else "Número de DNI * (8 dígitos)"
                                        )
                                    },
                                    placeholder = {
                                        Text(if (tipoDoc == "CUIT") "23-30917912-9" else "30917912")
                                    },
                                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    colors = roccaTextFieldColors(),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("signup_document_input"),
                                )
                            }
                        }

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email *") },
                            placeholder = { Text("titular@lanorita.com") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            colors = roccaTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_email_input"),
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Contraseña *") },
                            placeholder = {
                                Text(if (selectedTab == 1) "Mínimo 8 caracteres" else "••••••••")
                            },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    Icon(
                                        imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Mostrar contraseña",
                                    )
                                }
                            },
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            colors = roccaTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_password_input"),
                        )

                        AnimatedVisibility(visible = selectedTab == 1) {
                            OutlinedTextField(
                                value = passwordConfirm,
                                onValueChange = { passwordConfirm = it },
                                label = { Text("Confirmar contraseña *") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                singleLine = true,
                                colors = roccaTextFieldColors(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("signup_confirm_password_input"),
                            )
                        }

                        if (!authState.error.isNullOrBlank()) {
                            Surface(
                                color = RoccaErrorFondo,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, RoccaError.copy(alpha = 0.35f)),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(
                                    text = authState.error,
                                    color = RoccaError,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(12.dp),
                                )
                            }
                        }

                        Button(
                            onClick = {
                                if (selectedTab == 0) {
                                    onLogin(email, password)
                                } else {
                                    onSignup(
                                        nombre,
                                        tipoDoc,
                                        documento,
                                        email,
                                        password,
                                        passwordConfirm,
                                        selectedRol,
                                    )
                                }
                            },
                            enabled = !authState.loading,
                            colors = ButtonDefaults.buttonColors(containerColor = RoccaRojo),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("submit_auth_button"),
                        ) {
                            if (authState.loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = RoccaBlanco,
                                    strokeWidth = 2.dp,
                                )
                            } else {
                                Text(
                                    text = if (selectedTab == 0) "Ingresar al Sistema" else "Registrar y Crear Entidad Persona",
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }

                        if (selectedTab == 0) {
                            HorizontalDivider(
                                color = RoccaBorde,
                                modifier = Modifier.padding(vertical = 4.dp),
                            )
                            Text(
                                text = "Acceso rápido por rol (Frontend Demo • 3 Dashboards):",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = RoccaAzulOscuro,
                            )

                            DemoRoleQuickCard(
                                rol = RolUsuario.PERSONA,
                                subtitulo = "titular@lanorita.com • Gestiona sus carnicerías, sube requisitos y ve vencimientos",
                                icon = Icons.Default.Storefront,
                                onClick = { onLoginDemoRole(RolUsuario.PERSONA) },
                                testTag = "demo_role_persona",
                            )
                            DemoRoleQuickCard(
                                rol = RolUsuario.SUPERVISOR,
                                subtitulo = "supervisor@procarne.gob.ar • Fiscaliza requisitos, adjuntos y ciclo de vida",
                                icon = Icons.Default.FactCheck,
                                onClick = { onLoginDemoRole(RolUsuario.SUPERVISOR) },
                                testTag = "demo_role_supervisor",
                            )
                            DemoRoleQuickCard(
                                rol = RolUsuario.ADMIN,
                                subtitulo = "admin@procarne.gob.ar • Padrón completo, autoriza y emite certificados F 102",
                                icon = Icons.Default.AdminPanelSettings,
                                onClick = { onLoginDemoRole(RolUsuario.ADMIN) },
                                testTag = "demo_role_admin",
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = onOpenPortalPublico,
                modifier = Modifier
                    .height(48.dp)
                    .testTag("open_public_portal_button"),
            ) {
                Icon(
                    imageVector = Icons.Default.Public,
                    contentDescription = null,
                    tint = RoccaRojo,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Ir al Portal Público ProCarne",
                    color = RoccaRojo,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun DemoRoleQuickCard(
    rol: RolUsuario,
    subtitulo: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    testTag: String,
) {
    Surface(
        onClick = onClick,
        color = RoccaTarjeta,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, RoccaBordeInput),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .testTag(testTag),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = RoccaRojo,
                modifier = Modifier.size(22.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Entrar como ${rol.label}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = RoccaAzulOscuro,
                )
                Text(
                    text = subtitulo,
                    style = MaterialTheme.typography.labelSmall,
                    color = RoccaTextoApagado,
                )
            }
        }
    }
}

@Composable
fun roccaTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = RoccaRojo,
    unfocusedBorderColor = RoccaBordeInput,
    focusedContainerColor = RoccaBlanco,
    unfocusedContainerColor = RoccaBlanco,
)
