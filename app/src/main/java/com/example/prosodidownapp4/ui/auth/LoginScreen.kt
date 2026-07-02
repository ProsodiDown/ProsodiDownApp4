package com.example.prosodidownapp4.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prosodidownapp4.R
import com.example.prosodidownapp4.ui.theme.ProsodiPrimary
import com.example.prosodidownapp4.ui.theme.ProsodiSecondary

/**
 * AuthTab dijadikan PUBLIC (sebelumnya private) supaya bisa dipakai sebagai
 * tipe parameter [LoginScreen.initialTab] dari composable lain (HomeScreen,
 * NavGraph) -- diperlukan untuk fitur "buka langsung tab Daftar" via tombol
 * "Gabung Sekarang"/"Mulai Sekarang" di HomeScreen.
 */
enum class AuthTab { MASUK, DAFTAR }

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: (role: String) -> Unit = {},
    /** Tab yang ditampilkan saat halaman ini pertama dibuka. Default MASUK
     * (perilaku asli) -- kirim AuthTab.DAFTAR dari pemanggil yang ingin
     * langsung menampilkan form pendaftaran (lihat HomeScreen "Gabung Sekarang"). */
    initialTab: AuthTab = AuthTab.MASUK,
) {
    val authState by viewModel.state.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            val role = (authState as AuthState.Success).user.role
            onLoginSuccess(role)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(ProsodiPrimary, ProsodiSecondary),
                )
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo1),
                contentDescription = "Logo Prosodi Down",
                modifier = Modifier.size(90.dp),
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Prosodi Down",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Deteksi Emosi Anak Down Syndrome",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 12.sp,
            )
        }

        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(28.dp),
                    ambientColor = Color.Black.copy(alpha = 0.15f),
                    spotColor = Color.Black.copy(alpha = 0.15f),
                )
                .background(
                    color = Color(0xFFFAFAFA),
                    shape = RoundedCornerShape(28.dp),
                )
                .padding(horizontal = 24.dp, vertical = 28.dp),
        ) {
            // selectedTab diinisialisasi dari parameter initialTab (sebelumnya
            // selalu hardcode AuthTab.MASUK) -- ini perubahan utama untuk
            // mendukung "buka langsung tab Daftar".
            var selectedTab by remember { mutableStateOf(initialTab) }

            AuthTabSelector(
                selectedTab = selectedTab,
                onTabSelected = {
                    selectedTab = it
                    viewModel.resetState()
                },
            )

            Spacer(modifier = Modifier.height(28.dp))

            when (selectedTab) {
                AuthTab.MASUK -> MasukForm(viewModel = viewModel)
                AuthTab.DAFTAR -> DaftarForm(viewModel = viewModel)
            }
        }
    }
}

// =============================================================================
// Form Masuk
// =============================================================================

@Composable
private fun MasukForm(viewModel: AuthViewModel) {
    val authState by viewModel.state.collectAsState()
    val isLoading = authState is AuthState.Loading

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    AuthLabel(text = "Username")
    Spacer(modifier = Modifier.height(6.dp))
    AuthTextField(
        value = username,
        onValueChange = { username = it },
        placeholder = "Masukkan username",
        enabled = !isLoading,
    )

    Spacer(modifier = Modifier.height(16.dp))

    AuthLabel(text = "Password", highlight = true)
    Spacer(modifier = Modifier.height(6.dp))
    AuthTextField(
        value = password,
        onValueChange = { password = it },
        placeholder = "Masukkan password",
        isPassword = true,
        passwordVisible = passwordVisible,
        onTogglePassword = { passwordVisible = !passwordVisible },
        keyboardType = KeyboardType.Password,
        enabled = !isLoading,
    )

    Spacer(modifier = Modifier.height(28.dp))

    Button(
        onClick = { viewModel.login(username, password) },
        enabled = !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = ProsodiPrimary),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.5.dp,
            )
        } else {
            Text(
                text = "Masuk",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
            )
        }
    }

    if (authState is AuthState.Error) {
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = (authState as AuthState.Error).message,
            color = Color(0xFFD32F2F),
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(id = android.R.drawable.ic_btn_speak_now),
            contentDescription = null,
            tint = ProsodiPrimary,
            modifier = Modifier.size(14.dp),
        )
        Text(
            text = "  Alat bantu deteksi emosi anak Down syndrome",
            color = ProsodiPrimary,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
        )
    }

    Spacer(modifier = Modifier.height(8.dp))
}

// =============================================================================
// Form Daftar
// =============================================================================

@Composable
private fun DaftarForm(viewModel: AuthViewModel) {
    val authState by viewModel.state.collectAsState()
    val isLoading = authState is AuthState.Loading

    var namaLengkap by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val pesanWarna = if (
        authState is AuthState.Error &&
        (authState as AuthState.Error).message.contains("berhasil", ignoreCase = true)
    ) Color(0xFF388E3C) else Color(0xFFD32F2F)

    AuthLabel(text = "Nama Lengkap")
    Spacer(modifier = Modifier.height(6.dp))
    AuthTextField(
        value = namaLengkap,
        onValueChange = { namaLengkap = it },
        placeholder = "Masukkan nama lengkap",
        enabled = !isLoading,
    )

    Spacer(modifier = Modifier.height(16.dp))

    AuthLabel(text = "Email")
    Spacer(modifier = Modifier.height(6.dp))
    AuthTextField(
        value = email,
        onValueChange = { email = it },
        placeholder = "Masukkan email",
        keyboardType = KeyboardType.Email,
        enabled = !isLoading,
    )

    Spacer(modifier = Modifier.height(16.dp))

    AuthLabel(text = "Username")
    Spacer(modifier = Modifier.height(6.dp))
    AuthTextField(
        value = username,
        onValueChange = { username = it },
        placeholder = "Masukkan username",
        enabled = !isLoading,
    )

    Spacer(modifier = Modifier.height(16.dp))

    AuthLabel(text = "Password", highlight = true)
    Spacer(modifier = Modifier.height(6.dp))
    AuthTextField(
        value = password,
        onValueChange = { password = it },
        placeholder = "Masukkan password",
        isPassword = true,
        passwordVisible = passwordVisible,
        onTogglePassword = { passwordVisible = !passwordVisible },
        keyboardType = KeyboardType.Password,
        enabled = !isLoading,
    )

    Spacer(modifier = Modifier.height(28.dp))

    Button(
        onClick = { viewModel.register(username, password, namaLengkap, email) },
        enabled = !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = ProsodiPrimary),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.5.dp,
            )
        } else {
            Text(
                text = "Daftar",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
            )
        }
    }

    if (authState is AuthState.Error) {
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = (authState as AuthState.Error).message,
            color = pesanWarna,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }

    Spacer(modifier = Modifier.height(8.dp))
}

// =============================================================================
// Komponen kecil
// =============================================================================

@Composable
private fun AuthLabel(text: String, highlight: Boolean = false) {
    Text(
        text = text,
        color = if (highlight) ProsodiPrimary else Color(0xFF333333),
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
    )
}

@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePassword: (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    enabled: Boolean = true,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text(text = placeholder, color = Color(0xFFBDBDBD), fontSize = 14.sp)
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = ProsodiPrimary,
            unfocusedBorderColor = Color(0xFFE0E0E0),
            disabledBorderColor = Color(0xFFE0E0E0),
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            disabledContainerColor = Color(0xFFF5F5F5),
            cursorColor = ProsodiPrimary,
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = if (isPassword && !passwordVisible)
            PasswordVisualTransformation() else VisualTransformation.None,
        trailingIcon = if (isPassword && onTogglePassword != null) {
            {
                IconButton(onClick = onTogglePassword, enabled = enabled) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.Visibility
                        else Icons.Filled.VisibilityOff,
                        contentDescription = null,
                        tint = Color(0xFF9E9E9E),
                    )
                }
            }
        } else null,
    )
}

@Composable
private fun AuthTabSelector(
    selectedTab: AuthTab,
    onTabSelected: (AuthTab) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFEBEBEB))
            .padding(4.dp),
    ) {
        AuthTabItem(
            text = "Masuk",
            isSelected = selectedTab == AuthTab.MASUK,
            onClick = { onTabSelected(AuthTab.MASUK) },
            modifier = Modifier.weight(1f),
        )
        AuthTabItem(
            text = "Daftar",
            isSelected = selectedTab == AuthTab.DAFTAR,
            onClick = { onTabSelected(AuthTab.DAFTAR) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun AuthTabItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .then(
                if (isSelected)
                    Modifier.shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp))
                else Modifier
            )
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color(0xFFFAFAFA) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = if (isSelected) ProsodiPrimary else Color(0xFF9E9E9E),
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 14.sp,
        )
    }
}