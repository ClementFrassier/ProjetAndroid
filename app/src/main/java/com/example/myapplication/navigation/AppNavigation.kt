package com.example.myapplication.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.AwiApplication
import com.example.myapplication.ui.auth.LoginScreen
import com.example.myapplication.ui.festival.FestivalDetailScreen
import com.example.myapplication.ui.festival.FestivalListScreen
import com.example.myapplication.ui.reservation.ReservationListScreen
import com.example.myapplication.ui.viewmodel.AuthViewModel
import com.example.myapplication.ui.viewmodel.FestivalViewModel
import com.example.myapplication.ui.viewmodel.ReservationViewModel
import com.example.myapplication.ui.viewmodel.EditorViewModel
import com.example.myapplication.ui.viewmodel.GameViewModel
import com.example.myapplication.ui.editor.EditorListScreen
import com.example.myapplication.ui.editor.EditorDetailScreen
import com.example.myapplication.ui.game.GameListScreen
import com.example.myapplication.ui.game.GameDetailScreen
import com.example.myapplication.ui.reservation.ReservationDetailScreen
import com.example.myapplication.ui.invoice.InvoiceListScreen
import com.example.myapplication.ui.invoice.InvoiceDetailScreen
import com.example.myapplication.ui.viewmodel.InvoiceViewModel

object Routes {
    const val LOGIN = "login"
    const val FESTIVALS = "festivals"
    const val FESTIVAL_DETAIL = "festivals/{festivalId}"
    const val RESERVATIONS = "reservations/{festivalId}"
    const val RESERVATION_DETAIL = "reservations/{festivalId}/detail/{reservationId}"
    const val RESERVATION_CREATE = "reservations/{festivalId}/create"

    const val INVOICES = "invoices/{festivalId}"
    const val INVOICE_DETAIL = "invoice_detail/{invoiceId}"
    const val INVOICE_CREATE = "invoice_create/{reservationId}"

    const val EDITORS = "editors"
    const val EDITOR_DETAIL = "editors/{editorId}"
    const val EDITOR_CREATE = "editor_create"
    const val GAMES = "games"
    const val GAME_DETAIL = "games/{gameId}"
    const val GAME_CREATE = "game_create"

    fun festivalDetail(id: Int) = "festivals/$id"
    fun reservations(festivalId: Int) = "reservations/$festivalId"
    fun reservationDetail(festivalId: Int, reservationId: Int) = "reservations/$festivalId/detail/$reservationId"
    fun reservationCreate(festivalId: Int) = "reservations/$festivalId/create"
    
    fun invoices(festivalId: Int) = "invoices/$festivalId"
    fun invoiceDetail(id: Int) = "invoice_detail/$id"
    fun invoiceCreate(reservationId: Int) = "invoice_create/$reservationId"

    fun editorDetail(id: Int) = "editors/$id"
    fun gameDetail(id: Int) = "games/$id"
}

@Composable
fun AppNavigation(application: AwiApplication) {
    val navController = rememberNavController()

    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModel.Factory(application.authRepository, application.authManager)
    )
    val festivalViewModel: FestivalViewModel = viewModel(
        factory = FestivalViewModel.Factory(application.festivalRepository)
    )
    val reservationViewModel: ReservationViewModel = viewModel(
        factory = ReservationViewModel.Factory(application.reservationRepository)
    )
    val editorViewModel: EditorViewModel = viewModel(
        factory = EditorViewModel.Factory(application.editorRepository)
    )
    val gameViewModel: GameViewModel = viewModel(
        factory = GameViewModel.Factory(application.gameRepository)
    )
    val invoiceViewModel: InvoiceViewModel = viewModel(
        factory = InvoiceViewModel.Factory(application.invoiceRepository)
    )

    val startDestination = Routes.FESTIVALS

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Routes.LOGIN) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Routes.FESTIVALS) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.FESTIVALS) {
            FestivalListScreen(
                festivalViewModel = festivalViewModel,
                authViewModel = authViewModel,
                onFestivalClick = { id ->
                    navController.navigate(Routes.festivalDetail(id))
                },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onLogin = {
                    navController.navigate(Routes.LOGIN)
                },
                onNavigateToEditors = {
                    navController.navigate(Routes.EDITORS)
                },
                onNavigateToGames = {
                    navController.navigate(Routes.GAMES)
                }
            )
        }

        composable(
            route = Routes.FESTIVAL_DETAIL,
            arguments = listOf(navArgument("festivalId") { type = NavType.IntType })
        ) { backStackEntry ->
            val festivalId = backStackEntry.arguments?.getInt("festivalId") ?: return@composable
            FestivalDetailScreen(
                festivalId = festivalId,
                viewModel = festivalViewModel,
                onBack = { navController.popBackStack() },
                onViewReservations = { id ->
                    navController.navigate(Routes.reservations(id))
                },
                onViewInvoices = { id ->
                    navController.navigate(Routes.invoices(id))
                }
            )
        }

        composable(
            route = Routes.RESERVATIONS,
            arguments = listOf(navArgument("festivalId") { type = NavType.IntType })
        ) { backStackEntry ->
            val festivalId = backStackEntry.arguments?.getInt("festivalId") ?: return@composable
            ReservationListScreen(
                festivalId = festivalId,
                viewModel = reservationViewModel,
                onReservationClick = { resId ->
                    navController.navigate(Routes.reservationDetail(festivalId, resId))
                },
                onCreateReservation = {
                    navController.navigate(Routes.reservationCreate(festivalId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.RESERVATION_DETAIL,
            arguments = listOf(
                navArgument("festivalId") { type = NavType.IntType },
                navArgument("reservationId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val festivalId = backStackEntry.arguments?.getInt("festivalId") ?: return@composable
            val reservationId = backStackEntry.arguments?.getInt("reservationId") ?: return@composable
            ReservationDetailScreen(
                festivalId = festivalId,
                reservationId = reservationId,
                viewModel = reservationViewModel,
                festivalViewModel = festivalViewModel,
                editorViewModel = editorViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.RESERVATION_CREATE,
            arguments = listOf(navArgument("festivalId") { type = NavType.IntType })
        ) { backStackEntry ->
            val festivalId = backStackEntry.arguments?.getInt("festivalId") ?: return@composable
            ReservationDetailScreen(
                festivalId = festivalId,
                reservationId = null,
                viewModel = reservationViewModel,
                festivalViewModel = festivalViewModel,
                editorViewModel = editorViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.INVOICES,
            arguments = listOf(navArgument("festivalId") { type = NavType.IntType })
        ) { backStackEntry ->
            val festivalId = backStackEntry.arguments?.getInt("festivalId") ?: return@composable
            InvoiceListScreen(
                festivalId = festivalId,
                viewModel = invoiceViewModel,
                onInvoiceClick = { id -> navController.navigate(Routes.invoiceDetail(id)) },
                onCreateInvoice = { 
                    // Pour simplifier, la création manuelle nécessitera de passer la reservationId.
                    // Optionnellement on peut afficher une erreur ou rediriger, ou demander la réservation.
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.INVOICE_DETAIL,
            arguments = listOf(navArgument("invoiceId") { type = NavType.IntType })
        ) { backStackEntry ->
            val invoiceId = backStackEntry.arguments?.getInt("invoiceId") ?: return@composable
            InvoiceDetailScreen(
                invoiceId = invoiceId,
                reservationId = null,
                viewModel = invoiceViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.INVOICE_CREATE,
            arguments = listOf(navArgument("reservationId") { type = NavType.IntType })
        ) { backStackEntry ->
            val reservationId = backStackEntry.arguments?.getInt("reservationId") ?: return@composable
            InvoiceDetailScreen(
                invoiceId = null,
                reservationId = reservationId,
                viewModel = invoiceViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.EDITORS) {
            EditorListScreen(
                viewModel = editorViewModel,
                onEditorClick = { id ->
                    navController.navigate(Routes.editorDetail(id))
                },
                onCreateEditor = {
                    navController.navigate(Routes.EDITOR_CREATE)
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.EDITOR_DETAIL,
            arguments = listOf(navArgument("editorId") { type = NavType.IntType })
        ) { backStackEntry ->
            val editorId = backStackEntry.arguments?.getInt("editorId") ?: return@composable
            EditorDetailScreen(
                editorId = editorId,
                viewModel = editorViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.EDITOR_CREATE) {
            EditorDetailScreen(
                editorId = null,
                viewModel = editorViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.GAMES) {
            GameListScreen(
                viewModel = gameViewModel,
                onGameClick = { id ->
                    navController.navigate(Routes.gameDetail(id))
                },
                onCreateGame = {
                    navController.navigate(Routes.GAME_CREATE)
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.GAME_DETAIL,
            arguments = listOf(navArgument("gameId") { type = NavType.IntType })
        ) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getInt("gameId") ?: return@composable
            GameDetailScreen(
                gameId = gameId,
                viewModel = gameViewModel,
                editorViewModel = editorViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.GAME_CREATE) {
            GameDetailScreen(
                gameId = null,
                viewModel = gameViewModel,
                editorViewModel = editorViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
