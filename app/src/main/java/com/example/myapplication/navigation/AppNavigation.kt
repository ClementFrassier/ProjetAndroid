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

object Routes {
    const val LOGIN = "login"
    const val FESTIVALS = "festivals"
    const val FESTIVAL_DETAIL = "festivals/{festivalId}"
    const val RESERVATIONS = "reservations/{festivalId}"

    fun festivalDetail(id: Int) = "festivals/$id"
    fun reservations(festivalId: Int) = "reservations/$festivalId"
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
                onBack = { navController.popBackStack() }
            )
        }
    }
}
