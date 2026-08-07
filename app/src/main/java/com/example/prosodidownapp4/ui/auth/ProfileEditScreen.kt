package com.example.prosodidownapp4.ui.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prosodidownapp4.ui.theme.ProsodiPrimary
import com.example.prosodidownapp4.ui.theme.ProsodiSecondary

@Composable
fun ProfileEditScreen(
    viewModel: AuthViewModel,
    onBack: () -> Unit,
    onSaveSuccess: () -> Unit = {}
) {
    val authState by viewModel.state.collectAsState()
    val user = if (authState is AuthState.Success) (authState as AuthState.Success).user else null
    val context = LocalContext.current

    // Form States
    var username by remember { mutableStateOf(user?.username ?: "") }
    var fullName by remember { mutableStateOf(user?.fullName ?: "") }
    var email by remember { mutableStateOf(user?.email ?: "") }
    var absenceNumber by remember { mutableStateOf(user?.absenceNumber ?: "") }
    var studentClass by remember { mutableStateOf(user?.studentClass ?: "") }

    // Password States
    var showPasswordDialog by remember { mutableStateOf(false) }
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(ProsodiPrimary, ProsodiSecondary)
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, start = 8.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali", tint = Color.White)
                }
                Text(
                    "Edit Profil",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.shadow(16.dp, RoundedCornerShape(28.dp))
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        ProfileLabel("Username")
                        ProfileTextField(username, { username = it }, "Username")

                        Spacer(modifier = Modifier.height(16.dp))
                        ProfileLabel("Nama Lengkap")
                        ProfileTextField(fullName, { fullName = it }, "Nama Lengkap")
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        ProfileLabel("Email")
                        ProfileTextField(email, { email = it }, "Email", KeyboardType.Email)
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                ProfileLabel("Nomor Absen")
                                ProfileTextField(absenceNumber, { absenceNumber = it }, "No. Absen", KeyboardType.Number)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                ProfileLabel("Kelas")
                                ProfileTextField(studentClass, { studentClass = it }, "Kelas")
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                        
                        Button(
                            onClick = {
                                if (user != null) {
                                    if (username.isBlank()) {
                                        Toast.makeText(context, "Username tidak boleh kosong", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    viewModel.updateProfile(user.id, username, fullName, email, absenceNumber, studentClass)
                                    Toast.makeText(context, "Profil diperbarui", Toast.LENGTH_SHORT).show()
                                    onSaveSuccess()
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ProsodiPrimary)
                        ) {
                            Text("Simpan Perubahan", fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedButton(
                            onClick = { showPasswordDialog = true },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(width = 1.dp)
                        ) {
                            Icon(Icons.Default.Lock, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Ganti Password")
                        }
                    }
                }
            }
        }
    }

    if (showPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showPasswordDialog = false },
            title = { Text("Ganti Password") },
            text = {
                Column {
                    var passVisible by remember { mutableStateOf(false) }
                    
                    PasswordEditField(oldPassword, { oldPassword = it }, "Password Lama", passVisible) { passVisible = !passVisible }
                    Spacer(modifier = Modifier.height(8.dp))
                    PasswordEditField(newPassword, { newPassword = it }, "Password Baru", passVisible) { passVisible = !passVisible }
                    Spacer(modifier = Modifier.height(8.dp))
                    PasswordEditField(confirmPassword, { confirmPassword = it }, "Konfirmasi Password Baru", passVisible) { passVisible = !passVisible }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newPassword != confirmPassword) {
                        Toast.makeText(context, "Konfirmasi password tidak cocok", Toast.LENGTH_SHORT).show()
                    } else if (newPassword.length < 6) {
                        Toast.makeText(context, "Password minimal 6 karakter", Toast.LENGTH_SHORT).show()
                    } else {
                        val success = viewModel.changePassword(user?.id ?: -1L, oldPassword, newPassword)
                        if (success) {
                            Toast.makeText(context, "Password berhasil diganti", Toast.LENGTH_SHORT).show()
                            showPasswordDialog = false
                            oldPassword = ""; newPassword = ""; confirmPassword = ""
                        } else {
                            Toast.makeText(context, "Password lama salah", Toast.LENGTH_SHORT).show()
                        }
                    }
                }) {
                    Text("Ganti")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPasswordDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun ProfileLabel(text: String) {
    Text(text, fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 6.dp))
}

@Composable
private fun ProfileTextField(value: String, onValueChange: (String) -> Unit, placeholder: String, keyboardType: KeyboardType = KeyboardType.Text) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, color = Color.Gray, fontSize = 14.sp) },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
    )
}

@Composable
private fun PasswordEditField(value: String, onValueChange: (String) -> Unit, label: String, visible: Boolean, onToggle: () -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = onToggle) {
                Icon(if (visible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
            }
        },
        singleLine = true
    )
}
