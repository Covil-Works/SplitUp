package com.thaicrew.splitup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.thaicrew.splitup.check.ui.CheckViewModel
import com.thaicrew.splitup.friend.ui.FriendViewModel
import com.thaicrew.splitup.ui.theme.SplitUpTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val friendViewModel: FriendViewModel by viewModels()
    private val checkViewModel: CheckViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            SplitUpTheme {
                Scaffold(
                    bottomBar = {
                        AppBottomNavigation(navController)
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = innerPadding.calculateTopPadding())
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

@Composable
fun AppBottomNavigation(navController: NavHostController) {
    val items = listOf(Screen.Friends, Screen.Checks, Screen.History)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    NavigationBar(
        // Aspecto flutuante
        modifier = Modifier
            .navigationBarsPadding()
            .padding(start = 24.dp, end = 24.dp, bottom = 24.dp)
            .shadow(elevation = 12.dp, shape = RoundedCornerShape(32.dp))
            .clip(RoundedCornerShape(50.dp)),
        windowInsets = WindowInsets(0.dp)
    ) {
        items.forEach { screen ->
            val isSelected = when (screen) {
                Screen.Checks -> currentRoute == Screen.Checks.route || currentRoute?.startsWith("check_detail") == true
                Screen.History -> currentRoute == Screen.History.route || currentRoute?.startsWith("paid_check_detail") == true
                else -> currentDestination?.hierarchy?.any { it.route == screen.route } == true
            }

            NavigationBarItem(
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