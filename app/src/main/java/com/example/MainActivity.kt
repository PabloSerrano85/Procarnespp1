package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.automirrored.outlined.NoteAdd
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.local.OperadorEntity
import com.example.domain.EstadoRegistral
import com.example.domain.RolUsuario
import com.example.ui.components.PerfilUsuarioDialog
import com.example.ui.components.RoccaInstitutionalHeader
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.CartonF102Dialog
import com.example.ui.screens.CatalogoActividadesScreen
import com.example.ui.screens.FormularioF102Screen
import com.example.ui.screens.JerarquiaScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.OperadorDetailDialog
import com.example.ui.screens.PadronScreen
import com.example.ui.screens.PersonaDashboardScreen
import com.example.ui.screens.SupervisorDashboardScreen
import com.example.ui.theme.RoccaBlanco
import com.example.ui.theme.RoccaFondo
import com.example.ui.theme.RoccaRojo
import com.example.ui.theme.RoccaRojoClaro
import com.example.ui.theme.RoccaTextoApagado
import com.example.ui.theme.RoccaTheme
import com.example.ui.viewmodel.RoccaViewModel
import kotlinx.serialization.Serializable

@Serializable object RouteDashboard
@Serializable object RoutePadron
@Serializable object RouteJerarquia
@Serializable object RouteInscripcion
@Serializable object RouteCatalogo

private data class NavTabItem(
    val route: Any,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val badgeCount: Int? = null,
    val testTag: String,
)

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RoccaTheme {
                val windowSize = calculateWindowSizeClass(this)
                RoccaApp(useNavRail = windowSize.widthSizeClass != WindowWidthSizeClass.Compact)
            }
        }
    }
}

@Composable
fun RoccaApp(
    useNavRail: Boolean,
    vm: RoccaViewModel = viewModel(),
) {
    val context = LocalContext.current
    val authState by vm.authState.collectAsStateWithLifecycle()
    val currentRole by vm.currentRole.collectAsStateWithLifecycle()
    val currentPersona by vm.currentPersona.collectAsStateWithLifecycle()
    val adjuntosPersona by vm.adjuntosPersona.collectAsStateWithLifecycle()
    val snackbarMessage by vm.snackbarMessage.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        val msg = snackbarMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            vm.clearSnackbar()
        }
    }

    if (authState.checkingSession) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(RoccaFondo),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = RoccaRojo)
        }
        return
    }

    val usuarioActual = authState.usuario
    if (usuarioActual == null) {
        LoginScreen(
            authState = authState,
            onLogin = { email, password -> vm.login(email, password) },
            onLoginDemoRole = { rol -> vm.loginAsDemoRole(rol) },
            onSignup = { nombre, tipoDoc, doc, email, pwd, confirm, rol ->
                vm.signup(nombre, tipoDoc, doc, email, pwd, confirm, rol)
            },
            onClearError = { vm.clearAuthError() },
            onOpenPortalPublico = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://procarnesgo.netlify.app/"))
                context.startActivity(intent)
            },
        )
        return
    }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDest = navBackStackEntry?.destination

    val operadoresVisibles by vm.operadoresVisibles.collectAsStateWithLifecycle()
    val operadoresFiltrados by vm.operadoresFiltrados.collectAsStateWithLifecycle()
    val gruposJerarquia by vm.gruposJerarquia.collectAsStateWithLifecycle()
    val personas by vm.personas.collectAsStateWithLifecycle()
    val allRequisitos by vm.allRequisitos.collectAsStateWithLifecycle()
    val allAdjuntos by vm.allAdjuntos.collectAsStateWithLifecycle()
    val allCertificados by vm.allCertificados.collectAsStateWithLifecycle()

    val busqueda by vm.busqueda.collectAsStateWithLifecycle()
    val filtroEstado by vm.filtroEstado.collectAsStateWithLifecycle()
    val filtroCategoria by vm.filtroCategoria.collectAsStateWithLifecycle()
    val formState by vm.formState.collectAsStateWithLifecycle()
    val depSuggestions by vm.depSuggestions.collectAsStateWithLifecycle()
    val locSuggestions by vm.locSuggestions.collectAsStateWithLifecycle()

    var cartonOperador by remember { mutableStateOf<OperadorEntity?>(null) }
    var detailOperadorId by remember { mutableStateOf<Long?>(null) }
    var showPerfilDialog by remember { mutableStateOf(false) }
    var catalogoInitialTab by remember { mutableIntStateOf(0) }

    // Volver al Dashboard inicial al cambiar de rol/usuario
    LaunchedEffect(usuarioActual.email) {
        navController.navigate(RouteDashboard) {
            popUpTo(0) { inclusive = true }
            launchSingleTop = true
        }
    }

    val pendientesCount = operadoresVisibles.count {
        EstadoRegistral.fromString(it.estadoRegistral) == EstadoRegistral.PENDIENTE
    }

    val navItems = when (currentRole) {
        RolUsuario.PERSONA -> listOf(
            NavTabItem(
                route = RouteDashboard,
                label = "Mi Panel",
                selectedIcon = Icons.Filled.Dashboard,
                unselectedIcon = Icons.Outlined.Dashboard,
                badgeCount = null,
                testTag = "nav_dashboard",
            ),
            NavTabItem(
                route = RoutePadron,
                label = "Sucursales",
                selectedIcon = Icons.Filled.Storefront,
                unselectedIcon = Icons.Outlined.Storefront,
                badgeCount = operadoresVisibles.size,
                testTag = "nav_padron",
            ),
            NavTabItem(
                route = RouteInscripcion,
                label = "Alta F 102",
                selectedIcon = Icons.AutoMirrored.Filled.NoteAdd,
                unselectedIcon = Icons.AutoMirrored.Outlined.NoteAdd,
                badgeCount = null,
                testTag = "nav_inscripcion",
            ),
            NavTabItem(
                route = RouteCatalogo,
                label = "Catálogo",
                selectedIcon = Icons.Filled.Category,
                unselectedIcon = Icons.Outlined.Category,
                badgeCount = null,
                testTag = "nav_catalogo",
            ),
        )

        RolUsuario.SUPERVISOR -> listOf(
            NavTabItem(
                route = RouteDashboard,
                label = "Panel",
                selectedIcon = Icons.Filled.Dashboard,
                unselectedIcon = Icons.Outlined.Dashboard,
                badgeCount = pendientesCount.takeIf { it > 0 },
                testTag = "nav_dashboard",
            ),
            NavTabItem(
                route = RoutePadron,
                label = "Padrón",
                selectedIcon = Icons.Filled.Storefront,
                unselectedIcon = Icons.Outlined.Storefront,
                badgeCount = operadoresVisibles.size,
                testTag = "nav_padron",
            ),
            NavTabItem(
                route = RouteJerarquia,
                label = "Jerarquía",
                selectedIcon = Icons.Filled.AccountTree,
                unselectedIcon = Icons.Outlined.AccountTree,
                badgeCount = gruposJerarquia.size,
                testTag = "nav_jerarquia",
            ),
            NavTabItem(
                route = RouteCatalogo,
                label = "Catálogo",
                selectedIcon = Icons.Filled.Category,
                unselectedIcon = Icons.Outlined.Category,
                badgeCount = null,
                testTag = "nav_catalogo",
            ),
        )

        RolUsuario.ADMIN -> listOf(
            NavTabItem(
                route = RouteDashboard,
                label = "Panel",
                selectedIcon = Icons.Filled.Dashboard,
                unselectedIcon = Icons.Outlined.Dashboard,
                badgeCount = pendientesCount.takeIf { it > 0 },
                testTag = "nav_dashboard",
            ),
            NavTabItem(
                route = RoutePadron,
                label = "Padrón",
                selectedIcon = Icons.Filled.Storefront,
                unselectedIcon = Icons.Outlined.Storefront,
                badgeCount = operadoresVisibles.size,
                testTag = "nav_padron",
            ),
            NavTabItem(
                route = RouteJerarquia,
                label = "Jerarquía",
                selectedIcon = Icons.Filled.AccountTree,
                unselectedIcon = Icons.Outlined.AccountTree,
                badgeCount = null,
                testTag = "nav_jerarquia",
            ),
            NavTabItem(
                route = RouteInscripcion,
                label = "F 102",
                selectedIcon = Icons.AutoMirrored.Filled.NoteAdd,
                unselectedIcon = Icons.AutoMirrored.Outlined.NoteAdd,
                badgeCount = null,
                testTag = "nav_inscripcion",
            ),
            NavTabItem(
                route = RouteCatalogo,
                label = "Más",
                selectedIcon = Icons.Filled.Category,
                unselectedIcon = Icons.Outlined.Category,
                badgeCount = null,
                testTag = "nav_catalogo",
            ),
        )
    }

    fun navigateToTab(route: Any) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        containerColor = RoccaFondo,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            RoccaInstitutionalHeader(
                usuario = usuarioActual,
                onOpenPerfil = { showPerfilDialog = true },
                onLogout = { vm.logout() },
            )
        },
        bottomBar = {
            if (!useNavRail) {
                NavigationBar(
                    containerColor = RoccaBlanco,
                    windowInsets = WindowInsets.navigationBars,
                ) {
                    navItems.forEach { item ->
                        val selected = when (item.route) {
                            RouteDashboard -> currentDest?.hasRoute<RouteDashboard>() == true
                            RoutePadron -> currentDest?.hasRoute<RoutePadron>() == true
                            RouteJerarquia -> currentDest?.hasRoute<RouteJerarquia>() == true
                            RouteInscripcion -> currentDest?.hasRoute<RouteInscripcion>() == true
                            RouteCatalogo -> currentDest?.hasRoute<RouteCatalogo>() == true
                            else -> false
                        }
                        NavigationBarItem(
                            selected = selected,
                            onClick = { navigateToTab(item.route) },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (item.badgeCount != null && item.badgeCount > 0) {
                                            Badge(
                                                containerColor = if (selected) RoccaRojo else RoccaTextoApagado,
                                                contentColor = RoccaBlanco,
                                            ) {
                                                Text(item.badgeCount.toString())
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.label,
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = item.label,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = RoccaRojo,
                                selectedTextColor = RoccaRojo,
                                indicatorColor = RoccaRojoClaro,
                                unselectedIconColor = RoccaTextoApagado,
                                unselectedTextColor = RoccaTextoApagado,
                            ),
                            modifier = Modifier.testTag(item.testTag),
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            if (useNavRail) {
                NavigationRail(containerColor = RoccaBlanco) {
                    navItems.forEach { item ->
                        val selected = when (item.route) {
                            RouteDashboard -> currentDest?.hasRoute<RouteDashboard>() == true
                            RoutePadron -> currentDest?.hasRoute<RoutePadron>() == true
                            RouteJerarquia -> currentDest?.hasRoute<RouteJerarquia>() == true
                            RouteInscripcion -> currentDest?.hasRoute<RouteInscripcion>() == true
                            RouteCatalogo -> currentDest?.hasRoute<RouteCatalogo>() == true
                            else -> false
                        }
                        NavigationRailItem(
                            selected = selected,
                            onClick = { navigateToTab(item.route) },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (item.badgeCount != null && item.badgeCount > 0) {
                                            Badge(containerColor = RoccaRojo, contentColor = RoccaBlanco) {
                                                Text(item.badgeCount.toString())
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.label,
                                    )
                                }
                            },
                            label = { Text(item.label) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = RoccaRojo,
                                selectedTextColor = RoccaRojo,
                                indicatorColor = RoccaRojoClaro,
                            ),
                            modifier = Modifier.testTag(item.testTag),
                        )
                    }
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                NavHost(
                    navController = navController,
                    startDestination = RouteDashboard,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    composable<RouteDashboard> {
                        when (currentRole) {
                            RolUsuario.PERSONA -> PersonaDashboardScreen(
                                usuario = usuarioActual,
                                persona = currentPersona,
                                misOperadores = operadoresVisibles,
                                allRequisitos = allRequisitos,
                                allCertificados = allCertificados,
                                adjuntosPersona = adjuntosPersona,
                                onAltaNuevaSucursal = {
                                    vm.prefillSucursalParaCurrentPersona()
                                    navigateToTab(RouteInscripcion)
                                },
                                onGestionarSucursal = { detailOperadorId = it.id },
                                onVerCarton = { cartonOperador = it },
                                onAgregarAdjuntoPersona = vm::agregarAdjuntoPersona,
                                onEliminarAdjuntoPersona = vm::eliminarAdjuntoPersona,
                                onNotify = vm::showSnackbar,
                            )

                            RolUsuario.SUPERVISOR -> SupervisorDashboardScreen(
                                usuario = usuarioActual,
                                operadores = operadoresVisibles,
                                allRequisitos = allRequisitos,
                                allAdjuntos = allAdjuntos,
                                onRevisarLegajo = { detailOperadorId = it.id },
                                onCambiarEstado = vm::cambiarEstadoOperador,
                                onIrAPadron = { navigateToTab(RoutePadron) },
                            )

                            RolUsuario.ADMIN -> AdminDashboardScreen(
                                usuario = usuarioActual,
                                operadores = operadoresVisibles,
                                allCertificados = allCertificados,
                                onAutorizarYEmitir = vm::autorizarYEmitirCertificado,
                                onSuspenderCertificado = vm::suspenderCertificado,
                                onReactivarCertificado = vm::reactivarCertificado,
                                onGestionarOperador = { detailOperadorId = it.id },
                                onVerCarton = { cartonOperador = it },
                                onIrAPadron = { navigateToTab(RoutePadron) },
                                onIrAInscripcion = { navigateToTab(RouteInscripcion) },
                                onIrAModulos = {
                                    catalogoInitialTab = 1
                                    navigateToTab(RouteCatalogo)
                                },
                            )
                        }
                    }

                    composable<RoutePadron> {
                        PadronScreen(
                            currentRole = currentRole,
                            operadores = operadoresFiltrados,
                            busqueda = busqueda,
                            filtroEstado = filtroEstado,
                            filtroCategoria = filtroCategoria,
                            onBusquedaChange = vm::setBusqueda,
                            onFiltroEstadoChange = vm::setFiltroEstado,
                            onFiltroCategoriaChange = vm::setFiltroCategoria,
                            onVerCarton = { cartonOperador = it },
                            onGestionarOperador = { detailOperadorId = it.id },
                            onNotify = vm::showSnackbar,
                        )
                    }

                    composable<RouteJerarquia> {
                        JerarquiaScreen(
                            grupos = gruposJerarquia,
                            personas = personas,
                            onVerCarton = { cartonOperador = it },
                            onGestionarOperador = { detailOperadorId = it.id },
                            onAgregarSucursal = { persona ->
                                vm.prefillSucursalParaPersona(persona)
                                navigateToTab(RouteInscripcion)
                            },
                        )
                    }

                    composable<RouteInscripcion> {
                        FormularioF102Screen(
                            currentRole = currentRole,
                            currentPersona = currentPersona,
                            formState = formState,
                            personas = personas,
                            depSuggestions = depSuggestions,
                            locSuggestions = locSuggestions,
                            onUpdateForm = vm::updateForm,
                            onDepQueryChange = vm::onDepartamentoQueryChanged,
                            onLocQueryChange = vm::onLocalidadQueryChanged,
                            onToggleCategoria = vm::toggleCategoriaForm,
                            onToggleActividad = vm::toggleActividadForm,
                            onToggleCarne = vm::toggleCarneForm,
                            onToggleRequisito = vm::toggleRequisitoForm,
                            onCargarEjemplo = vm::cargarDatosEjemplo,
                            onSubmit = vm::submitFormularioF102,
                            onCancelToPadron = {
                                vm.resetFormulario()
                                navigateToTab(
                                    if (currentRole == RolUsuario.PERSONA) RouteDashboard else RoutePadron
                                )
                            },
                            onVerCartonCreated = { op ->
                                cartonOperador = op
                                navigateToTab(RoutePadron)
                            },
                            onDismissCreatedDialog = vm::clearLastCreatedOperador,
                        )
                    }

                    composable<RouteCatalogo> {
                        CatalogoActividadesScreen(
                            showModulosTab = currentRole == RolUsuario.ADMIN,
                            initialTab = catalogoInitialTab,
                        )
                    }
                }
            }
        }
    }

    if (showPerfilDialog) {
        PerfilUsuarioDialog(
            usuario = usuarioActual,
            persona = currentPersona,
            onDismiss = { showPerfilDialog = false },
            onSwitchDemoRole = { rol -> vm.loginAsDemoRole(rol) },
            onLogout = { vm.logout() },
        )
    }

    cartonOperador?.let { op ->
        val certs by vm.observeCertificados(op.id).collectAsStateWithLifecycle()
        val vigente = certs.firstOrNull { it.estado == "ACTIVO" } ?: certs.firstOrNull()
        CartonF102Dialog(
            operador = op,
            certificadoVigente = vigente,
            onDismiss = { cartonOperador = null },
            onNotify = vm::showSnackbar,
        )
    }

    detailOperadorId?.let { opId ->
        val opFlow = remember(opId) { vm.observeOperador(opId) }
        val opActual by opFlow.collectAsStateWithLifecycle(initialValue = null)
        val reqs by vm.observeRequisitos(opId).collectAsStateWithLifecycle()
        val adjs by vm.observeAdjuntos(opId).collectAsStateWithLifecycle()
        val certs by vm.observeCertificados(opId).collectAsStateWithLifecycle()

        opActual?.let { op ->
            OperadorDetailDialog(
                currentRole = currentRole,
                operador = op,
                requisitos = reqs,
                adjuntos = adjs,
                certificados = certs,
                onDismiss = { detailOperadorId = null },
                onCambiarEstado = { nuevo -> vm.cambiarEstadoOperador(op.id, nuevo) },
                onActualizarRequisito = vm::actualizarRequisito,
                onAgregarAdjunto = { codigo, filename, mime, uri, size ->
                    vm.agregarAdjunto(op.id, codigo, filename, mime, uri, size)
                },
                onEliminarAdjunto = vm::eliminarAdjunto,
                onAutorizarYEmitir = { vm.autorizarYEmitirCertificado(op.id) },
                onEmitirCertificado = { vm.emitirNuevoCertificado(op.id) },
                onSuspenderCertificado = vm::suspenderCertificado,
                onReactivarCertificado = vm::reactivarCertificado,
                onVerCarton = {
                    detailOperadorId = null
                    cartonOperador = op
                },
                onNotify = vm::showSnackbar,
            )
        }
    }
}
