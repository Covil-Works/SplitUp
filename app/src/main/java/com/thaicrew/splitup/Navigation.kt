package com.thaicrew.splitup

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
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
import com.thaicrew.splitup.history.ui.HistoryScreen
import com.thaicrew.splitup.history.ui.HistoryViewModel
import com.thaicrew.splitup.history.ui.PaidCheckDetailScreen
import com.thaicrew.splitup.history.ui.PaidCheckDetailViewModel

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Checks : Screen("checks_list", "Comandas", Icons.Default.ReceiptLong)
    object Friends : Screen("friends_list", "Amigos", Icons.Default.People)
    object History : Screen("history_list", "Histórico", Icons.Default.History)
    object CheckDetail : Screen("check_detail/{checkId}", "Detalhes", Icons.Default.ReceiptLong) {
        fun createRoute(checkId: Int) = "check_detail/$checkId"
    }
    object PaidCheckDetail : Screen("paid_check_detail/{checkId}", "Detalhes Pagos", Icons.Default.History) {
        fun createRoute(checkId: Int) = "paid_check_detail/$checkId"
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    friendViewModel: FriendViewModel,
    checkViewModel: CheckViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Checks.route
    ) {
        // 1. Tela Principal (Comandas Abertas)
        composable(Screen.Checks.route) {
            CheckScreen(
                viewModel = checkViewModel,
                onNavigateToCreate = { id ->
                    // Vai para a tela de Edição
                    navController.navigate(Screen.CheckDetail.createRoute(id))
                }
            )
        }

        // 2. Tela de Amigos
        composable(Screen.Friends.route) {
            FriendScreen(viewModel = friendViewModel)
        }

        // 3. Tela de Histórico
        composable(Screen.History.route) {
            val historyViewModel: HistoryViewModel = hiltViewModel()
            HistoryScreen(
                viewModel = historyViewModel,
                onCheckClick = { id ->
                    navController.navigate(Screen.PaidCheckDetail.createRoute(id))
                }
            )
        }

        // 4. Detalhes de Comanda Aberta (Edição)
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

        // 5. NOVA TELA: Detalhes de Comanda Paga (Leitura)
        composable(
            route = Screen.PaidCheckDetail.route,
            arguments = listOf(navArgument("checkId") { type = NavType.IntType })
        ) {
            val paidViewModel: PaidCheckDetailViewModel = hiltViewModel()
            PaidCheckDetailScreen(
                viewModel = paidViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}