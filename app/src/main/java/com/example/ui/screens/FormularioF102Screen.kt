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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.OperadorEntity
import com.example.data.local.PersonaEntity
import com.example.data.remote.GeorefSuggestion
import com.example.domain.EstadoRegistral
import com.example.domain.RoccaCatalogos
import com.example.domain.RolUsuario
import com.example.ui.components.GeorefAutocompleteField
import com.example.ui.components.RoccaSectionCard
import com.example.ui.theme.JetBrainsMono
import com.example.ui.theme.RoccaAviso
import com.example.ui.theme.RoccaAvisoBorde
import com.example.ui.theme.RoccaAvisoFondo
import com.example.ui.theme.RoccaAvisoPill
import com.example.ui.theme.RoccaAvisoTextoOscuro
import com.example.ui.theme.RoccaAzulOscuro
import com.example.ui.theme.RoccaBlanco
import com.example.ui.theme.RoccaBorde
import com.example.ui.theme.RoccaBordeInput
import com.example.ui.theme.RoccaCheckbox
import com.example.ui.theme.RoccaError
import com.example.ui.theme.RoccaErrorFondo
import com.example.ui.theme.RoccaExitoBorde
import com.example.ui.theme.RoccaExitoClaro
import com.example.ui.theme.RoccaExitoTextoOscuro
import com.example.ui.theme.RoccaFondo
import com.example.ui.theme.RoccaHeaderFin
import com.example.ui.theme.RoccaRojo
import com.example.ui.theme.RoccaRojoClaro
import com.example.ui.theme.RoccaTarjeta
import com.example.ui.theme.RoccaTexto
import com.example.ui.theme.RoccaTextoApagado
import com.example.ui.theme.RoccaTextoMedio
import com.example.ui.theme.RoccaTextoSuave
import com.example.ui.viewmodel.FormF102UiState

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FormularioF102Screen(
    currentRole: RolUsuario,
    currentPersona: PersonaEntity?,
    formState: FormF102UiState,
    personas: List<PersonaEntity>,
    depSuggestions: List<GeorefSuggestion>,
    locSuggestions: List<GeorefSuggestion>,
    onUpdateForm: ((FormF102UiState) -> FormF102UiState) -> Unit,
    onDepQueryChange: (String) -> Unit,
    onLocQueryChange: (String) -> Unit,
    onToggleCategoria: (String) -> Unit,
    onToggleActividad: (String) -> Unit,
    onToggleCarne: (String) -> Unit,
    onToggleRequisito: (String) -> Unit,
    onCargarEjemplo: () -> Unit,
    onSubmit: () -> Unit,
    onCancelToPadron: () -> Unit,
    onVerCartonCreated: (OperadorEntity) -> Unit,
    onDismissCreatedDialog: () -> Unit,
) {
    val requisitosAplicables = remember(formState.categorias) {
        RoccaCatalogos.requisitosParaCategorias(formState.categorias)
    }
    val esPersona = currentRole == RolUsuario.PERSONA

    if (formState.lastCreatedOperador != null) {
        val created = formState.lastCreatedOperador
        val esActivo = EstadoRegistral.fromString(created.estadoRegistral) == EstadoRegistral.ACTIVO
        AlertDialog(
            onDismissRequest = onDismissCreatedDialog,
            title = {
                Text(
                    text = if (esActivo) "Cartón F 102 Emitido" else "Sucursal Registrada (Pendiente)",
                    fontWeight = FontWeight.Bold,
                    color = RoccaAzulOscuro,
                )
            },
            text = {
                Text(
                    if (esActivo) {
                        "Se registró ${created.nombreEstablecimiento} (CUIT ${created.cuit}) con N° de Inscripto ${created.numeroCertificadoF102} en estado Habilitado."
                    } else {
                        "Se dio de alta la sucursal ${created.nombreEstablecimiento} (CUIT ${created.cuit}) en estado PENDIENTE. El Administrador revisará los requisitos para autorizar y emitir el Certificado F 102."
                    }
                )
            },
            confirmButton = {
                if (esActivo) {
                    Button(
                        onClick = {
                            onDismissCreatedDialog()
                            onVerCartonCreated(created)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RoccaRojo),
                    ) {
                        Text("Ver Cartón F 102")
                    }
                } else {
                    Button(
                        onClick = {
                            onDismissCreatedDialog()
                            onCancelToPadron()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RoccaRojo),
                    ) {
                        Text("Ir a Mis Sucursales")
                    }
                }
            },
            dismissButton = {
                if (esActivo) {
                    TextButton(
                        onClick = {
                            onDismissCreatedDialog()
                            onCancelToPadron()
                        }
                    ) {
                        Text("Volver al Padrón", color = RoccaTextoMedio)
                    }
                }
            },
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(RoccaFondo),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            val bannerGradient = Brush.horizontalGradient(
                colors = listOf(RoccaAzulOscuro, RoccaHeaderFin, RoccaAzulOscuro)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(bannerGradient)
                    .padding(18.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = Color(0xFFFCD34D),
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = "Formulario Oficial F 102 • Alta de Operador (CU-1)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFCD34D),
                        )
                    }
                    Text(
                        text = if (esPersona) "Alta de Nueva Sucursal / Carnicería" else "Alta de Operador Cárnico y Emisión F 102",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = RoccaBlanco,
                    )
                    Text(
                        text = if (esPersona) {
                            "Registrá un nuevo establecimiento vinculado a tu entidad Persona. Quedará en estado Pendiente hasta la emisión del certificado por el Administrador."
                        } else {
                            "Registra la Persona (Física/Jurídica), su establecimiento/sucursal, sus actividades económicas (CU-8) y requisitos."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFCBD5E1),
                    )
                    Button(
                        onClick = onCargarEjemplo,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RoccaAviso,
                            contentColor = Color(0xFF020617),
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("cargar_ejemplo_f102_button"),
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoFixHigh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Cargar Datos de Ejemplo",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }

        // Sección 1: Persona Titular
        item {
            RoccaSectionCard(
                numero = "1",
                titulo = "Persona Titular (CUIT vinculado)",
                descripcion = if (esPersona) {
                    "Tu cuenta Persona se vincula automáticamente como titular de esta nueva sucursal."
                } else {
                    "Una persona puede poseer múltiples establecimientos (un cartón F 102 por cada sucursal)."
                },
                accion = if (!esPersona) {
                    {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = !formState.personaExistente,
                                onClick = {
                                    onUpdateForm {
                                        it.copy(personaExistente = false, personaSeleccionadaId = null)
                                    }
                                },
                                label = { Text("Nueva Persona") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = RoccaRojo,
                                    selectedLabelColor = RoccaBlanco,
                                ),
                                modifier = Modifier.testTag("toggle_nueva_persona"),
                            )
                            FilterChip(
                                selected = formState.personaExistente,
                                onClick = {
                                    onUpdateForm { it.copy(personaExistente = true) }
                                },
                                label = { Text("Persona Existente (+ Sucursal)") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = RoccaRojo,
                                    selectedLabelColor = RoccaBlanco,
                                ),
                                modifier = Modifier.testTag("toggle_persona_existente"),
                            )
                        }
                    }
                } else null,
            ) {
                if (esPersona && currentPersona != null) {
                    Surface(
                        color = RoccaTarjeta,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, RoccaBorde),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = RoccaRojo)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentPersona.nombre,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = RoccaAzulOscuro,
                                )
                                Text(
                                    text = "${currentPersona.tipoDocumento}: ${currentPersona.identificador} • ${currentPersona.email ?: ""}",
                                    fontFamily = JetBrainsMono,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = RoccaTextoApagado,
                                )
                            }
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (formState.personaExistente && personas.isNotEmpty()) {
                            var personaDropdown by remember { mutableStateOf(false) }
                            val selectedPersona = personas.firstOrNull { it.id == formState.personaSeleccionadaId }
                            ExposedDropdownMenuBox(
                                expanded = personaDropdown,
                                onExpandedChange = { personaDropdown = it },
                            ) {
                                OutlinedTextField(
                                    value = selectedPersona?.let { "${it.nombre} (${it.identificador})" } ?: "",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Seleccionar Titular Existente") },
                                    placeholder = { Text("Elegir persona titular…") },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = personaDropdown)
                                    },
                                    colors = roccaTextFieldColors(),
                                    modifier = Modifier
                                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                        .fillMaxWidth()
                                        .testTag("persona_existente_dropdown"),
                                )
                                ExposedDropdownMenu(
                                    expanded = personaDropdown,
                                    onDismissRequest = { personaDropdown = false },
                                    modifier = Modifier.background(RoccaBlanco),
                                ) {
                                    personas.forEach { persona ->
                                        DropdownMenuItem(
                                            text = { Text("${persona.nombre} • ${persona.identificador}") },
                                            onClick = {
                                                personaDropdown = false
                                                onUpdateForm {
                                                    it.copy(
                                                        personaSeleccionadaId = persona.id,
                                                        cuit = persona.identificador,
                                                        titular = persona.nombre,
                                                        tipoPersona = if (persona.tipo == "PJ") "juridica" else "fisica",
                                                        telefono = persona.telefono ?: it.telefono,
                                                        email = persona.email ?: it.email,
                                                    )
                                                }
                                            },
                                        )
                                    }
                                }
                            }
                        }

                        var tipoPersonaExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = tipoPersonaExpanded,
                            onExpandedChange = { tipoPersonaExpanded = it },
                        ) {
                            OutlinedTextField(
                                value = if (formState.tipoPersona == "fisica") {
                                    "Persona Física (Humana / Titular)"
                                } else {
                                    "Persona Jurídica (Empresa / Sociedad)"
                                },
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Tipo de Persona *") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = tipoPersonaExpanded)
                                },
                                colors = roccaTextFieldColors(),
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                    .fillMaxWidth()
                                    .testTag("tipo_persona_dropdown"),
                            )
                            ExposedDropdownMenu(
                                expanded = tipoPersonaExpanded,
                                onDismissRequest = { tipoPersonaExpanded = false },
                                modifier = Modifier.background(RoccaBlanco),
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Persona Física (Humana / Titular)") },
                                    onClick = {
                                        tipoPersonaExpanded = false
                                        onUpdateForm { it.copy(tipoPersona = "fisica") }
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text("Persona Jurídica (Empresa / Sociedad)") },
                                    onClick = {
                                        tipoPersonaExpanded = false
                                        onUpdateForm { it.copy(tipoPersona = "juridica") }
                                    },
                                )
                            }
                        }

                        OutlinedTextField(
                            value = formState.cuit,
                            onValueChange = { v -> onUpdateForm { it.copy(cuit = v) } },
                            label = {
                                Text(
                                    if (formState.personaExistente) "C.U.I.T. (opcional — sesión/titular)"
                                    else "C.U.I.T. * (ej.: 23-30917912-9)"
                                )
                            },
                            placeholder = { Text("23-30917912-9") },
                            isError = formState.cuitError != null,
                            supportingText = formState.cuitError?.let { err ->
                                { Text(err, color = RoccaError) }
                            },
                            singleLine = true,
                            colors = roccaTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("f102_cuit_input"),
                        )

                        OutlinedTextField(
                            value = formState.titular,
                            onValueChange = { v -> onUpdateForm { it.copy(titular = v) } },
                            label = {
                                Text(
                                    if (formState.tipoPersona == "fisica") "Apellido y Nombre del Titular *"
                                    else "Razón Social *"
                                )
                            },
                            placeholder = {
                                Text(
                                    if (formState.tipoPersona == "fisica") "OLIVEIRA DANIEL ALBERTO"
                                    else "LA NORITA S.R.L."
                                )
                            },
                            singleLine = true,
                            colors = roccaTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("f102_titular_input"),
                        )

                        if (formState.tipoPersona == "fisica") {
                            OutlinedTextField(
                                value = formState.dni,
                                onValueChange = { v -> onUpdateForm { it.copy(dni = v) } },
                                label = { Text("D.N.I.") },
                                placeholder = { Text("30.917.912") },
                                singleLine = true,
                                colors = roccaTextFieldColors(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("f102_dni_input"),
                            )
                        }

                        OutlinedTextField(
                            value = formState.telefono,
                            onValueChange = { v -> onUpdateForm { it.copy(telefono = v) } },
                            label = { Text("Teléfono") },
                            placeholder = { Text("(0385) 431-8910") },
                            singleLine = true,
                            colors = roccaTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("f102_telefono_input"),
                        )

                        OutlinedTextField(
                            value = formState.email,
                            onValueChange = { v -> onUpdateForm { it.copy(email = v) } },
                            label = { Text("Correo Electrónico") },
                            placeholder = { Text("contacto@procarne.gob.ar") },
                            singleLine = true,
                            colors = roccaTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("f102_email_input"),
                        )
                    }
                }
            }
        }

        // Sección 2: Datos del Establecimiento / Sucursal
        item {
            RoccaSectionCard(
                numero = "2",
                titulo = "Datos del Establecimiento / Sucursal",
                descripcion = "Información física que figurará en el Cartón F 102 para esta sucursal.",
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = formState.nombreEstablecimiento,
                        onValueChange = { v -> onUpdateForm { it.copy(nombreEstablecimiento = v) } },
                        label = { Text("Nombre del Establecimiento / Fantasía *") },
                        placeholder = { Text("LA NORITA SUC. CENTRO") },
                        singleLine = true,
                        colors = roccaTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("f102_establecimiento_input"),
                    )

                    GeorefAutocompleteField(
                        label = "Departamento Provincial *",
                        value = formState.departamento,
                        onValueChange = onDepQueryChange,
                        suggestions = depSuggestions,
                        placeholder = "Escribí para buscar (ej. Capital, Banda)…",
                        testTag = "f102_departamento_input",
                    )

                    GeorefAutocompleteField(
                        label = "Localidad *",
                        value = formState.localidad,
                        onValueChange = onLocQueryChange,
                        suggestions = locSuggestions,
                        placeholder = "Escribí para buscar (ej. Santiago del Estero)…",
                        testTag = "f102_localidad_input",
                    )

                    OutlinedTextField(
                        value = formState.domicilio,
                        onValueChange = { v -> onUpdateForm { it.copy(domicilio = v) } },
                        label = { Text("Domicilio Completo * (Calle, N° y Barrio)") },
                        placeholder = { Text("JOSE BENJAMIN ABALOS N° 4582, B° ALMIRANTE BROWN") },
                        colors = roccaTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("f102_domicilio_input"),
                    )

                    OutlinedTextField(
                        value = formState.habilitacionSanitaria,
                        onValueChange = { v -> onUpdateForm { it.copy(habilitacionSanitaria = v) } },
                        label = { Text("N° Habilitación Sanitaria Oficial") },
                        placeholder = { Text("Municipal N° 419/2026 • ProCarne F 102") },
                        singleLine = true,
                        colors = roccaTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("f102_habilitacion_input"),
                    )
                }
            }
        }

        // Sección 3: Categorías, Actividades Económicas (CU-1/CU-8) y Especies
        item {
            RoccaSectionCard(
                numero = "3",
                titulo = "Categorías, Actividades Económicas y Especies",
                descripcion = "Asigná las categorías cárnicas y actividades DGR/AFIP (CU-1 / CU-8).",
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Categorías del establecimiento:",
                        style = MaterialTheme.typography.labelLarge,
                        color = RoccaTexto,
                    )

                    RoccaCatalogos.CATEGORIAS_CARNICAS.forEach { cat ->
                        val active = cat.id in formState.categorias
                        Surface(
                            onClick = { onToggleCategoria(cat.id) },
                            shape = RoundedCornerShape(10.dp),
                            color = if (active) RoccaRojoClaro else RoccaBlanco,
                            border = BorderStroke(1.dp, if (active) RoccaRojo else RoccaBorde),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("categoria_${cat.id.lowercase()}"),
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Checkbox(
                                    checked = active,
                                    onCheckedChange = { onToggleCategoria(cat.id) },
                                    colors = CheckboxDefaults.colors(checkedColor = RoccaRojo),
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = cat.nombre,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = RoccaTexto,
                                    )
                                    Text(
                                        text = "Código AFIP: ${cat.codigoAfip}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = RoccaTextoApagado,
                                    )
                                    Text(
                                        text = cat.descripcion,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = RoccaTextoSuave,
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Actividades Económicas (N:M código DGR / AFIP):",
                        style = MaterialTheme.typography.labelLarge,
                        color = RoccaTexto,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        RoccaCatalogos.ACTIVIDADES_ECONOMICAS.forEach { act ->
                            val selected = act.codigo in formState.actividadesCodigos
                            FilterChip(
                                selected = selected,
                                onClick = { onToggleActividad(act.codigo) },
                                label = { Text("${act.codigo} • ${act.nombre.take(26)}") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = RoccaRojoClaro,
                                    selectedLabelColor = RoccaRojo,
                                ),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Tipo de Carne Comercializada:",
                        style = MaterialTheme.typography.labelLarge,
                        color = RoccaTexto,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        RoccaCatalogos.TIPOS_CARNE.forEach { carne ->
                            val selected = carne in formState.tiposCarne
                            FilterChip(
                                selected = selected,
                                onClick = { onToggleCarne(carne) },
                                label = { Text(carne) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = RoccaAvisoPill,
                                    selectedLabelColor = RoccaAvisoTextoOscuro,
                                ),
                                modifier = Modifier.testTag("carne_${carne.lowercase()}"),
                            )
                        }
                    }
                }
            }
        }

        // Sección 4: Requisitos de Papeleo Exigidos
        item {
            RoccaSectionCard(
                numero = "4",
                titulo = "Requisitos Generales y de Categoría",
                descripcion = "Se generan automáticamente según las categorías seleccionadas.",
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    requisitosAplicables.forEach { req ->
                        val cumplido = formState.requisitosCumplidos[req.codigo] == true
                        Surface(
                            onClick = { onToggleRequisito(req.codigo) },
                            shape = RoundedCornerShape(10.dp),
                            color = if (cumplido) RoccaExitoClaro else RoccaBlanco,
                            border = BorderStroke(1.dp, if (cumplido) RoccaExitoBorde else RoccaBorde),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("requisito_${req.codigo}"),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Checkbox(
                                    checked = cumplido,
                                    onCheckedChange = { onToggleRequisito(req.codigo) },
                                    colors = CheckboxDefaults.colors(checkedColor = RoccaCheckbox),
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = req.nombre,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = if (cumplido) RoccaExitoTextoOscuro else RoccaTextoMedio,
                                    )
                                }
                                if (req.obligatorio) {
                                    Surface(
                                        color = RoccaErrorFondo,
                                        shape = RoundedCornerShape(4.dp),
                                    ) {
                                        Text(
                                            text = "OBLIGATORIO",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = RoccaError,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (esPersona) {
                    Surface(
                        color = RoccaAvisoFondo,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, RoccaAvisoBorde),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = "Nota: Luego de dar de alta la sucursal podrás adjuntar los archivos PDF/JPG desde tu panel; el Administrador emitirá el certificado F 102.",
                            style = MaterialTheme.typography.bodySmall,
                            color = RoccaAvisoTextoOscuro,
                            modifier = Modifier.padding(12.dp),
                        )
                    }
                }

                if (!formState.submitError.isNullOrBlank()) {
                    Surface(
                        color = RoccaErrorFondo,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, RoccaError.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = formState.submitError,
                            color = RoccaError,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(12.dp),
                        )
                    }
                }

                Button(
                    onClick = onSubmit,
                    enabled = !formState.submitting,
                    colors = ButtonDefaults.buttonColors(containerColor = RoccaRojo),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("submit_f102_button"),
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when {
                            formState.submitting -> "Registrando…"
                            esPersona -> "Dar de Alta Sucursal (Operador)"
                            else -> "Registrar Operador F 102"
                        },
                        fontWeight = FontWeight.Bold,
                    )
                }

                OutlinedButton(
                    onClick = onCancelToPadron,
                    border = BorderStroke(1.dp, RoccaBordeInput),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("cancel_f102_button"),
                ) {
                    Text(
                        text = "Volver",
                        color = RoccaTextoMedio,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
