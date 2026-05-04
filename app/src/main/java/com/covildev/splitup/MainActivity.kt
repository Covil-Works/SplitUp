package com.covildev.splitup

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.CircleShape
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.covildev.splitup.check.ui.CheckDetailViewModel
import com.covildev.splitup.check.ui.CheckViewModel
import com.covildev.splitup.friend.ui.FriendViewModel
import com.covildev.splitup.history.ui.PaidCheckDetailViewModel
import com.covildev.splitup.ui.theme.MediumContrastText
import com.covildev.splitup.ui.theme.SplitUpTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

val MainTopBarMinHeight = 70.dp
val MainTopBarFadeHeight = 16.dp

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val friendViewModel: FriendViewModel by viewModels()
    private val checkViewModel: CheckViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            val scope = rememberCoroutineScope()

            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            var showPaidCheckMenu by rememberSaveable(currentRoute) { mutableStateOf(false) }

            SplitUpTheme {
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        AppDrawerContent(
                            onHelpClick = {
                                scope.launch { drawerState.close() }
                                openSupportEmail()
                            }
                        )
                    }
                ) {
                    Scaffold(
                        topBar = {
                            when (currentRoute) {
                                Screen.Friends.route,
                                Screen.Checks.route,
                                Screen.History.route -> {
                                    MainListTopBar(onMenuClick = { scope.launch { drawerState.open() } })
                                }

                                Screen.CheckDetail.route -> {
                                    navBackStackEntry?.let { entry ->
                                        val detailViewModel: CheckDetailViewModel = hiltViewModel(entry)
                                        val detailUiState by detailViewModel.uiState.collectAsStateWithLifecycle()

                                        CheckDetailTopBar(
                                            checkName = detailUiState.check?.name ?: "Carregando...",
                                            onBackClick = { navController.navigateUp() },
                                            onEditNameClick = detailViewModel::onShowEditNameDialog
                                        )
                                    }
                                }

                                Screen.PaidCheckDetail.route -> {
                                    navBackStackEntry?.let { entry ->
                                        val paidDetailViewModel: PaidCheckDetailViewModel = hiltViewModel(entry)
                                        val paidUiState by paidDetailViewModel.uiState.collectAsStateWithLifecycle()

                                        PaidCheckTopBar(
                                            checkName = paidUiState.check?.name ?: "Detalhes",
                                            onBackClick = { navController.navigateUp() },
                                            isMenuExpanded = showPaidCheckMenu,
                                            onShareClick = paidDetailViewModel::onExportClicked,
                                            onMenuClick = { showPaidCheckMenu = true },
                                            onMenuDismiss = { showPaidCheckMenu = false },
                                            onReopenClick = {
                                                showPaidCheckMenu = false
                                                paidDetailViewModel.onReopenClicked()
                                            },
                                            onDeleteClick = {
                                                showPaidCheckMenu = false
                                                paidDetailViewModel.onDeleteClicked()
                                            }
                                        )
                                    }
                                }
                            }
                        },
                        bottomBar = { AppBottomNavigation(navController) }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            AppNavHost(
                                navController = navController,
                                friendViewModel = friendViewModel,
                                checkViewModel = checkViewModel
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun MainActivity.openSupportEmail() {
    val supportIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("https://covildev.com")
    )

    runCatching { startActivity(supportIntent) }
        .onFailure {
            if (it is ActivityNotFoundException) {
                Toast.makeText(
                    this,
                    "Nenhum navegador encontrado neste dispositivo.",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    this,
                    "Nao foi possivel abrir o site agora.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
}

@Composable
private fun AppDrawerContent(
    onHelpClick: () -> Unit
) {
    val versionName = BuildConfig.VERSION_NAME

    ModalDrawerSheet(
        modifier = Modifier
            .fillMaxWidth(0.82f)
            .fillMaxHeight()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Text(
                text = "InformaÃ§Ãµes",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            DrawerInfoRow(
                text = "Versao $versionName",
                badgeText = "i"
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "\u00A9 2026 Covil",
                style = MaterialTheme.typography.labelMedium,
                color = MediumContrastText
            )

            Spacer(modifier = Modifier.height(18.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Ajuda",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(10.dp))
            DrawerActionRow(
                title = "Fale conosco",
                titleStyle = MaterialTheme.typography.labelMedium,
                icon = {
                    DrawerBadgeCircle(text = "?")
                },
                onClick = onHelpClick
            )
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun DrawerActionRow(
    title: String,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    titleStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyLarge,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            color = titleColor,
            style = titleStyle
        )
    }
}

@Composable
private fun DrawerInfoRow(
    text: String,
    badgeText: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DrawerBadgeCircle(text = badgeText)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
private fun DrawerBadgeCircle(
    text: String
) {
    Box(
        modifier = Modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MainListTopBar(onMenuClick: () -> Unit) {
    UniversalTopBar {
        Image(
            painter = painterResource(id = R.drawable.icone_topbar),
            contentDescription = "Logo do app",
            modifier = Modifier
                .padding(start = 16.dp)
                .size(30.dp)
        )
        Text(
            text = "SplitUp",
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(start = 8.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
        IconButton(
            onClick = onMenuClick,
            modifier = Modifier.padding(end = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Menu",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
private fun CheckDetailTopBar(
    checkName: String,
    onBackClick: () -> Unit,
    onEditNameClick: () -> Unit
) {
    UniversalTopBar {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.padding(start = 4.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Voltar",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
        Text(
            text = checkName,
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
        )
        IconButton(
            onClick = onEditNameClick,
            modifier = Modifier.padding(end = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Editar nome da comanda",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
private fun PaidCheckTopBar(
    checkName: String,
    onBackClick: () -> Unit,
    isMenuExpanded: Boolean,
    onShareClick: () -> Unit,
    onMenuClick: () -> Unit,
    onMenuDismiss: () -> Unit,
    onReopenClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    UniversalTopBar {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.padding(start = 4.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Voltar",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
        Text(
            text = checkName,
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
        )

        IconButton(onClick = onShareClick) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Compartilhar comanda",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }

        Box {
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier.padding(end = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Mais opcoes",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }

            DropdownMenu(
                expanded = isMenuExpanded,
                onDismissRequest = onMenuDismiss
            ) {
                DropdownMenuItem(
                    text = { Text("Reabrir Comanda") },
                    onClick = onReopenClick
                )
                DropdownMenuItem(
                    text = { Text("Apagar Comanda", color = MaterialTheme.colorScheme.error) },
                    onClick = onDeleteClick
                )
            }
        }
    }
}

@Composable
private fun UniversalTopBar(
    content: @Composable RowScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .heightIn(min = MainTopBarMinHeight)
                .background(MaterialTheme.colorScheme.primary),
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(MainTopBarFadeHeight)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background.copy(alpha = 0.9f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

@Composable
fun AppBottomNavigation(navController: NavHostController) {
    val items = listOf(Screen.Friends, Screen.Checks, Screen.History)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceBright,
        tonalElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        windowInsets = WindowInsets(0.dp)
    ) {
        items.forEach { screen ->
            val isSelected = when (screen) {
                Screen.Checks -> currentRoute == Screen.Checks.route || currentRoute?.startsWith(
                    "check_detail"
                ) == true

                Screen.History -> currentRoute == Screen.History.route || currentRoute?.startsWith(
                    "paid_check_detail"
                ) == true

                else -> currentDestination?.hierarchy?.any { it.route == screen.route } == true
            }

            NavigationBarItem(
                modifier = Modifier.padding(bottom = 5.dp),
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                    selectedTextColor = MaterialTheme.colorScheme.onPrimary,
                    indicatorColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MediumContrastText,
                    unselectedTextColor = MediumContrastText,
                    disabledIconColor = MediumContrastText,
                    disabledTextColor = MediumContrastText,
                ),
                icon = { Icon(screen.icon, contentDescription = null) },
                label = { Text(screen.label) },
                selected = isSelected,
                onClick = {
                    if (isSelected && currentRoute != screen.route) {
                        navController.popBackStack(screen.route, inclusive = false)
                    } else if (!isSelected) {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}



