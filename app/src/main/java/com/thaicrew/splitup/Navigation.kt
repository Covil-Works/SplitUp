package com.thaicrew.splitup

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History // Adicione este import
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.thaicrew.splitup.check.ui.CheckDetailScreen
import com.thaicrew.splitup.check.ui.CheckDetailViewModel
import com.thaicrew.splitup.check.ui.CheckScreen
import com.thaicrew.splitup.check.ui.CheckViewModel
import com.thaicrew.splitup.friend.ui.FriendScreen
import com.thaicrew.splitup.friend.ui.FriendViewModel
import com.thaicrew.splitup.history.ui.HistoryScreen // Importe a tela
import com.thaicrew.splitup.history.ui.HistoryViewModel // Importe o VM

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Checks : Screen("checks_list", "Comandas", Icons.Default.ReceiptLong)
    object Friends : Screen("friends_list", "Amigos", Icons.Default.People)
    object History : Screen("history_list", "Histórico", Icons.Default.History) // Nova rota
    object CheckDetail : Screen("check_detail/{checkId}", "Detalhes", Icons.Default.ReceiptLong) {
        fun createRoute(checkId: Int) = "check_detail/$checkId"
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    friendViewModel: FriendViewModel,
    checkViewModel: CheckViewModel
) {
    // IMPORTANTE: Mudei o startDestination para Friends, já que é o primeiro da ordem solicitada?
    // Se quiser manter Comandas como tela inicial, mantenha Screen.Checks.route.
    // Pela ordem visual "Amigos, Comandas, Historico", geralmente a primeira aba é a inicial.
    // Vou manter Checks como inicial por ser a feature principal, mas a ordem visual mudará na BottomBar.
    NavHost(navController = navController, startDestination = Screen.Checks.route) {

        composable(Screen.Checks.route) {
            CheckScreen(
                viewModel = checkViewModel,
                onNavigateToCreate = { id ->
                    navController.navigate(Screen.CheckDetail.createRoute(id))
                }
            )
        }

        composable(Screen.Friends.route) {
            FriendScreen(viewModel = friendViewModel)
        }

        // Nova composição para o Histórico
        composable(Screen.History.route) {
            val historyViewModel: HistoryViewModel = hiltViewModel()
            HistoryScreen(
                viewModel = historyViewModel,
                onCheckClick = { id ->
                    navController.navigate(Screen.CheckDetail.createRoute(id))
                }
            )
        }

        composable(
            route = Screen.CheckDetail.route,
            arguments = listOf(navArgument("checkId") { type = NavType.IntType })
        ) {
            val detailViewModel: CheckDetailViewModel = hiltViewModel()
            CheckDetailScreen(
                viewModel = detailViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}