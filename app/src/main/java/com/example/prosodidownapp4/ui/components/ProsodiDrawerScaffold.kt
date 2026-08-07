package com.example.prosodidownapp4.ui.components

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.example.prosodidownapp4.MainViewModel
import com.example.prosodidownapp4.ui.auth.AuthState
import com.example.prosodidownapp4.ui.auth.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun ProsodiDrawerScaffold(
    authViewModel: AuthViewModel,
    mainViewModel: MainViewModel,
    onLogin: () -> Unit,
    onRegister: () -> Unit,
    onLogout: () -> Unit,
    onEditProfile: () -> Unit = {},
    content: @Composable (openDrawer: () -> Unit) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    val authState by authViewModel.state.collectAsState()
    val user = if (authState is AuthState.Success) (authState as AuthState.Success).user else null

    val isDarkModeOverride by mainViewModel.isDarkMode.collectAsState()
    val shouldOpenDrawer by mainViewModel.shouldOpenDrawer.collectAsState()

    LaunchedEffect(shouldOpenDrawer) {
        if (shouldOpenDrawer) {
            scope.launch {
                drawerState.open()
                mainViewModel.onDrawerOpened()
            }
        }
    }

    // Membungkus dengan RTL agar drawer muncul dari kanan
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                // Mengembalikan isi drawer ke LTR agar teks tidak terbalik
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    ProsodiDrawer(
                        user = user,
                        isDarkMode = isDarkModeOverride ?: false,
                        onToggleTheme = { mainViewModel.toggleDarkMode(false) },
                        onClose = { scope.launch { drawerState.close() } },
                        onLogin = onLogin,
                        onRegister = onRegister,
                        onLogout = onLogout,
                        onEditProfile = onEditProfile
                    )
                }
            }
        ) {
            // Mengembalikan konten utama ke LTR
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                content { scope.launch { drawerState.open() } }
            }
        }
    }
}
