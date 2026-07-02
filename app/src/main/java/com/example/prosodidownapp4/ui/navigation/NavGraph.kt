package com.example.prosodidownapp4.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.prosodidownapp4.ui.auth.AuthTab
import com.example.prosodidownapp4.ui.auth.AuthViewModel
import com.example.prosodidownapp4.ui.auth.LoginScreen
import com.example.prosodidownapp4.ui.detection.DetectionScreen
import com.example.prosodidownapp4.ui.history.HistoryScreen
import com.example.prosodidownapp4.ui.home.HomeScreen
import com.example.prosodidownapp4.ui.splash.SplashScreen

/**
 * Alur navigasi:
 * Splash → Home (langsung, tanpa login)
 * Home → Detection / History (via BottomNavBar)
 * Home → Login (via tombol Keluar di BottomNavBar)
 * Home → Login/Daftar (via tombol "Gabung Sekarang" di CTA section)
 * Login/Daftar → Home (setelah berhasil masuk/daftar)
 */
object Routes {
    const val SPLASH       = "splash"
    const val LOGIN        = "login"
    const val LOGIN_DAFTAR = "login/daftar"
    const val HOME         = "home"
    const val DETECTION    = "detection"
    const val HISTORY      = "history"
}

@Composable
fun ProsodiDownNavGraph(
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
    ) {
        // ── Splash → langsung Home ────────────────────────────────────────
        composable(Routes.SPLASH) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        // ── Login tab Masuk ───────────────────────────────────────────────
        composable(Routes.LOGIN) {
            val authViewModel: AuthViewModel = viewModel()
            LoginScreen(
                viewModel      = authViewModel,
                initialTab     = AuthTab.MASUK,
                onLoginSuccess = { _ ->
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
            )
        }

        // ── Login tab Daftar — dibuka dari "Gabung Sekarang" ──────────────
        composable(Routes.LOGIN_DAFTAR) {
            val authViewModel: AuthViewModel = viewModel()
            LoginScreen(
                viewModel      = authViewModel,
                initialTab     = AuthTab.DAFTAR,
                onLoginSuccess = { _ ->
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN_DAFTAR) { inclusive = true }
                    }
                },
            )
        }

        // ── Home ──────────────────────────────────────────────────────────
        composable(Routes.HOME) {
            HomeScreen(
                isLoggedIn          = true,
                onMulaiRekam        = { navController.navigate(Routes.DETECTION) },
                onNavigateToDeteksi = { navController.navigate(Routes.DETECTION) },
                onNavigateToRiwayat = { navController.navigate(Routes.HISTORY) },
                onNavigateToDaftar  = { navController.navigate(Routes.LOGIN_DAFTAR) },
                onLogout            = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
            )
        }

        // ── Detection ─────────────────────────────────────────────────────
        composable(Routes.DETECTION) {
            DetectionScreen(
                isLoggedIn          = true,
                onNavigateToHome    = {
                    navController.popBackStack(Routes.HOME, inclusive = false)
                },
                onNavigateToRiwayat = { navController.navigate(Routes.HISTORY) },
                onLogout            = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
            )
        }

        // ── History ───────────────────────────────────────────────────────
        composable(Routes.HISTORY) {
            HistoryScreen(
                onNavigateToBeranda = {
                    navController.popBackStack(Routes.HOME, inclusive = false)
                },
                onNavigateToDeteksi = { navController.navigate(Routes.DETECTION) },
                onLogout            = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
            )
        }
    }
}