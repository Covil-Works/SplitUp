package com.thaicrew.splitup

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.thaicrew.splitup.check.ui.CheckScreen
import com.thaicrew.splitup.check.ui.CheckViewModel
import com.thaicrew.splitup.friend.ui.FriendScreen
import com.thaicrew.splitup.friend.ui.FriendViewModel

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Checks : Screen("checks_list", "Comandas", Icons.Default.ReceiptLong)
    object Friends : Screen("friends_list", "Amigos", Icons.Default.People)
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    friendViewModel: FriendViewModel,
    checkViewModel: CheckViewModel
) {
    NavHost(navController = navController, startDestination = Screen.Checks.route) {
        composable(Screen.Checks.route) {
            CheckScreen(viewModel = checkViewModel, onNavigateToCreate = { /* futuro */ })
        }
        composable(Screen.Friends.route) {
            FriendScreen(viewModel = friendViewModel)
        }
    }
}