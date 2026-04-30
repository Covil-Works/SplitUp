package com.thaicrew.splitup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.thaicrew.splitup.ui.theme.MediumContrastText
import com.thaicrew.splitup.check.ui.CheckViewModel
import com.thaicrew.splitup.friend.ui.FriendViewModel
import com.thaicrew.splitup.ui.theme.SplitUpTheme
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

            val mainScreenRoutes = setOf(
                Screen.Checks.route,
                Screen.Friends.route,
                Screen.History.route
            )
            val showTopBar = currentRoute in mainScreenRoutes

            SplitUpTheme {
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet { }
                    }
                ) {
                    Scaffold(
                        topBar = {
                            if (showTopBar) {
                                MainTopBar(onMenuClick = { scope.launch { drawerState.open() } })
                            }
                        },
                        bottomBar = {
                            AppBottomNavigation(navController)
                        }
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

@Composable
fun MainTopBar(onMenuClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Barra solida
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = MainTopBarMinHeight)
                .background(MaterialTheme.colorScheme.primary)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.icone_topbar),
                contentDescription = "Logo do app",
                modifier = Modifier
                    .padding(start = 16.dp, top = 30.dp)
                    .size(30.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // RIGHT: Menu icon (three horizontal lines)
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier.padding(end = 8.dp, top = 30.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu"
                )
            }
        }
        // Fade/sombra abaixo da barra
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
