package com.example.prosodidownapp4.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.prosodidownapp4.MainViewModel
import com.example.prosodidownapp4.ui.auth.AuthState
import com.example.prosodidownapp4.ui.auth.AuthTab
import com.example.prosodidownapp4.ui.auth.AuthViewModel
import com.example.prosodidownapp4.ui.auth.LoginScreen
import com.example.prosodidownapp4.ui.components.ProsodiDrawerScaffold
import com.example.prosodidownapp4.ui.detection.DetectionScreen
import com.example.prosodidownapp4.ui.history.HistoryScreen
import com.example.prosodidownapp4.ui.home.HomeScreen
import com.example.prosodidownapp4.ui.splash.SplashScreen

/**
 * Alur navigasi:
 * Splash → Home (langsung, tanpa login)
 * Home → Detection / History HANYA jika sudah login; jika belum, diarahkan
 *        ke Login dulu, lalu lanjut otomatis ke tujuan semula setelah berhasil.
 * Home → Login/Daftar (via tombol "Gabung Sekarang" di CTA section)
 * BottomNavBar: tombol "Masuk" jika belum login, "Keluar" jika sudah login.
 * Login/Daftar → kembali ke tujuan semula (atau Home) setelah berhasil masuk/daftar.
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
    mainViewModel: MainViewModel = viewModel()
) {
    // AuthViewModel di-hoist di sini (level graph), BUKAN di dalam masing-masing
    // composable(). Dengan begini satu instance yang sama dipakai di semua route,
    // jadi status login benar-benar tersimpan selama app berjalan -- sebelumnya
    // viewModel() dipanggil di dalam composable(Routes.LOGIN) sehingga instance-nya
    // dibuat ulang setiap kali route itu dibuka, dan status login tidak pernah
    // "diingat" oleh layar lain.
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.state.collectAsState()
    val isLoggedIn = authState is AuthState.Success

    // Menyimpan tujuan asli saat user diarahkan ke Login karena mencoba
    // mengakses fitur yang butuh login (Deteksi / Riwayat). null berarti
    // login dibuka biasa (misalnya lewat tombol "Masuk" di navbar) sehingga
    // setelah berhasil cukup kembali ke Home.
    var pendingRoute by remember { mutableStateOf<String?>(null) }

    /** Dipanggil dari Home/Detection/History saat tombol Deteksi/Riwayat ditekan. */
    fun navigateOrRequireLogin(route: String) {
        if (isLoggedIn) {
            navController.navigate(route)
        } else {
            pendingRoute = route
            navController.navigate(Routes.LOGIN)
        }
    }

    /** Dipanggil dari tombol logout di BottomNavBar. */
    fun logout() {
        authViewModel.resetState()
        navController.navigate(Routes.HOME) {
            popUpTo(Routes.HOME) { inclusive = true }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
    ) {
        // ── Splash → langsung Home, tanpa login ───────────────────────────
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
            LoginScreen(
                viewModel      = authViewModel,
                initialTab     = AuthTab.MASUK,
                onLoginSuccess = { _ ->
                    val target = pendingRoute ?: Routes.HOME
                    pendingRoute = null
                    navController.navigate(target) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onBackClick = {
                    pendingRoute = null
                    navController.popBackStack()
                }
            )
        }

        // ── Login tab Daftar — dibuka dari "Gabung Sekarang" ──────────────
        composable(Routes.LOGIN_DAFTAR) {
            LoginScreen(
                viewModel      = authViewModel,
                initialTab     = AuthTab.DAFTAR,
                onLoginSuccess = { _ ->
                    val target = pendingRoute ?: Routes.HOME
                    pendingRoute = null
                    navController.navigate(target) {
                        popUpTo(Routes.LOGIN_DAFTAR) { inclusive = true }
                    }
                },
                onBackClick = {
                    pendingRoute = null
                    navController.popBackStack()
                }
            )
        }

        // ── Home ──────────────────────────────────────────────────────────
        composable(Routes.HOME) {
            ProsodiDrawerScaffold(
                authViewModel = authViewModel,
                mainViewModel = mainViewModel,
                onLogin = { navController.navigate(Routes.LOGIN) },
                onRegister = { navController.navigate(Routes.LOGIN_DAFTAR) },
                onLogout = { logout() }
            ) { openDrawer ->
                HomeScreen(
                    isLoggedIn          = isLoggedIn,
                    onMulaiRekam        = { navigateOrRequireLogin(Routes.DETECTION) },
                    onNavigateToDeteksi = { navigateOrRequireLogin(Routes.DETECTION) },
                    onNavigateToRiwayat = { navigateOrRequireLogin(Routes.HISTORY) },
                    onNavigateToDaftar  = { navController.navigate(Routes.LOGIN_DAFTAR) },
                    onNavigateToLogin   = { navController.navigate(Routes.LOGIN) },
                    onLogout            = { logout() },
                    onMenuClick         = openDrawer
                )
            }
        }

        // ── Detection (butuh login) ───────────────────────────────────────
        composable(Routes.DETECTION) {
            ProsodiDrawerScaffold(
                authViewModel = authViewModel,
                mainViewModel = mainViewModel,
                onLogin = { navController.navigate(Routes.LOGIN) },
                onRegister = { navController.navigate(Routes.LOGIN_DAFTAR) },
                onLogout = { logout() }
            ) { openDrawer ->
                DetectionScreen(
                    isLoggedIn          = isLoggedIn,
                    onNavigateToHome    = {
                        navController.popBackStack(Routes.HOME, inclusive = false)
                    },
                    onNavigateToRiwayat = { navigateOrRequireLogin(Routes.HISTORY) },
                    onNavigateBack      = { navController.popBackStack() },
                    onLogout            = { logout() },
                    onMenuClick         = openDrawer
                )
            }
        }

        // ── History (butuh login) ─────────────────────────────────────────
        composable(Routes.HISTORY) {
            ProsodiDrawerScaffold(
                authViewModel = authViewModel,
                mainViewModel = mainViewModel,
                onLogin = { navController.navigate(Routes.LOGIN) },
                onRegister = { navController.navigate(Routes.LOGIN_DAFTAR) },
                onLogout = { logout() }
            ) { openDrawer ->
                HistoryScreen(
                    onNavigateToBeranda = {
                        navController.popBackStack(Routes.HOME, inclusive = false)
                    },
                    onNavigateToDeteksi = { navigateOrRequireLogin(Routes.DETECTION) },
                    onNavigateBack      = { navController.popBackStack() },
                    onLogout            = { logout() },
                    onMenuClick         = openDrawer
                )
            }
        }
    }
}
