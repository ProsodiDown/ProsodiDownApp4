package com.example.prosodidownapp4.ui.detection

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.prosodidownapp4.ui.components.BottomNavBar
import com.example.prosodidownapp4.ui.components.BottomNavItem
import com.example.prosodidownapp4.ui.components.AppTopHeader
import com.example.prosodidownapp4.ui.home.HomeNavMenu
import com.example.prosodidownapp4.ui.theme.ProsodiDownApp4Theme
import com.example.prosodidownapp4.ui.theme.ProsodiPrimary
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds


@Composable
fun DetectionScreen(
    viewModel: DetectionViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(
            LocalContext.current.applicationContext as Application
        )
    ),
    isLoggedIn: Boolean = true,
    userId: Long = -1L,
    onNavigateToHome: () -> Unit = {},
    onNavigateToRiwayat: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onLogout: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    onMenuClick: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.onRekamClicked()
        } else {
            Toast.makeText(context, "Izin mikrofon dibutuhkan untuk merekam.", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(state.justFinishedSaving) {
        if (state.justFinishedSaving) {
            delay(3.seconds)
            viewModel.onSaveBannerDismissed()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(bottom = 88.dp),
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            AppTopHeader(
                onBackClick = onNavigateBack,
                onMenuClick = onMenuClick
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Deteksi Emosi",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 12.dp)
            )
            Text(
                text = "Rekam suara anak untuk mendeteksi kondisi emosi",
                fontSize = 12.sp,
                color = if (MaterialTheme.colorScheme.primary == ProsodiPrimary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(visible = state.justFinishedSaving) {
                SavedBanner()
            }

            if (state.justFinishedSaving) {
                Spacer(modifier = Modifier.height(12.dp))
            }

            WaveformCard(state = state)

            Spacer(modifier = Modifier.height(16.dp))

            StatRow(state = state)

            if (state.status == DetectionStatus.RECORDING || state.status == DetectionStatus.PAUSED) {
                Spacer(modifier = Modifier.height(12.dp))
                AnalysisCountdownCard(secondsUntilNext = state.secondsUntilNextAnalysis)
            }

            Spacer(modifier = Modifier.height(16.dp))

            ControlButtons(
                state = state,
                viewModel = viewModel,
                userId = userId,
                onNavigateToRiwayat = onNavigateToRiwayat,
                onRekamClick = {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                        viewModel.onRekamClicked()
                    } else {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }
            )

            if (state.status != DetectionStatus.IDLE && state.status != DetectionStatus.FINISHED) {
                Spacer(modifier = Modifier.height(20.dp))
                LogDeteksiSection(entries = state.logEntries)
            }
        }

        BottomNavBar(
            items = listOf(
                BottomNavItem(HomeNavMenu.BERANDA, Icons.Filled.Home, "Beranda"),
                BottomNavItem(HomeNavMenu.DETEKSI_EMOSI, Icons.Filled.Mic, "Deteksi Emosi"),
                BottomNavItem(HomeNavMenu.RIWAYAT, Icons.Filled.History, "Riwayat"),
                BottomNavItem(
                    id = HomeNavMenu.AUTH,
                    icon = if (isLoggedIn) Icons.AutoMirrored.Filled.Logout else Icons.AutoMirrored.Filled.Login,
                    contentDescription = if (isLoggedIn) "Keluar" else "Masuk",
                ),
            ),
            activeItemId = HomeNavMenu.DETEKSI_EMOSI,
            onItemSelected = { id ->
                when (id) {
                    HomeNavMenu.BERANDA -> onNavigateToHome()
                    HomeNavMenu.DETEKSI_EMOSI -> Unit
                    HomeNavMenu.RIWAYAT -> onNavigateToRiwayat()
                    HomeNavMenu.AUTH -> if (isLoggedIn) onLogout() else onNavigateToLogin()
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun SavedBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFDFF5E1), shape = RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = Color(0xFF2E7D32),
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Rekaman berhasil disimpan ke Riwayat.",
            color = Color(0xFF2E7D32),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun WaveformCard(state: DetectionUiState) {
    val barColor = when (state.status) {
        DetectionStatus.PAUSED -> Color(0xFFFF7A59)
        DetectionStatus.RECORDING -> MaterialTheme.colorScheme.primary
        else -> Color(0xFFBDBDBD)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(16.dp))
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Gelombang Suara",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            when (state.status) {
                DetectionStatus.RECORDING -> StatusTag(text = "LIVE", color = Color(0xFF2E7D32))
                DetectionStatus.PAUSED -> StatusTag(text = "DIJEDA", color = Color(0xFFFF7A59))
                else -> Unit
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        WaveformCanvas(
            levels = state.waveformLevels,
            barColor = barColor,
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
        )
    }
}

@Composable
private fun StatusTag(text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .background(color, shape = CircleShape),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = text, fontSize = 11.sp, color = color, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun WaveformCanvas(
    levels: List<Float>,
    barColor: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        if (levels.isEmpty()) return@Canvas
        val gapPx = 1.dp.toPx()
        val barWidth = (size.width - gapPx * (levels.size - 1)) / levels.size
        val maxBarHeight = size.height

        levels.forEachIndexed { index, level ->
            val barHeight = (level.coerceIn(0.05f, 1f)) * maxBarHeight
            val left = index * (barWidth + gapPx)
            val top = (maxBarHeight - barHeight) / 2

            drawRoundRect(
                color = barColor,
                topLeft = Offset(left, top),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2, barWidth / 2),
            )
        }
    }
}

@Composable
private fun StatRow(state: DetectionUiState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StatCard(modifier = Modifier.weight(1f).fillMaxHeight(), label = "Durasi Rekaman") {
            Text(
                text = formatDuration(state.elapsedSeconds),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = if (MaterialTheme.colorScheme.primary == ProsodiPrimary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
            )
        }
        StatCard(modifier = Modifier.weight(1f).fillMaxHeight(), label = "Emosi Saat Ini") {
            Text(text = state.currentEmotion.emoji, fontSize = 26.sp)
            Text(
                text = state.currentEmotion.displayName,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (MaterialTheme.colorScheme.primary == ProsodiPrimary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    label: String,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(16.dp))
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Spacer(modifier = Modifier.height(6.dp))
        content()
    }
}

private fun formatDuration(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

@Composable
private fun AnalysisCountdownCard(secondsUntilNext: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = "Analisis berikutnya dalam", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Text(
            text = "${secondsUntilNext}s",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (MaterialTheme.colorScheme.primary == ProsodiPrimary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
private fun ControlButtons(
    state: DetectionUiState,
    viewModel: DetectionViewModel,
    userId: Long,
    onNavigateToRiwayat: () -> Unit,
    onRekamClick: () -> Unit,
) {
    when (state.status) {
        DetectionStatus.IDLE -> {
            PrimaryActionButton(
                text = "Rekam",
                icon = Icons.Filled.Mic,
                containerColor = MaterialTheme.colorScheme.primary,
                onClick = onRekamClick,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        DetectionStatus.RECORDING -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                PrimaryActionButton(
                    modifier = Modifier.weight(1f),
                    text = "Jeda",
                    icon = Icons.Filled.Pause,
                    containerColor = Color(0xFFFF7A59),
                    onClick = viewModel::onJedaClicked,
                )
                PrimaryActionButton(
                    modifier = Modifier.weight(1f),
                    text = "Selesai",
                    icon = Icons.Filled.Stop,
                    containerColor = MaterialTheme.colorScheme.primary,
                    onClick = { viewModel.onSelesaiClicked(userId) },
                )
            }
        }

        DetectionStatus.PAUSED -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                PrimaryActionButton(
                    modifier = Modifier.weight(1f),
                    text = "Lanjutkan",
                    icon = Icons.Filled.PlayArrow,
                    containerColor = MaterialTheme.colorScheme.secondary,
                    onClick = viewModel::onLanjutkanClicked,
                )
                PrimaryActionButton(
                    modifier = Modifier.weight(1f),
                    text = "Selesai",
                    icon = Icons.Filled.Stop,
                    containerColor = MaterialTheme.colorScheme.primary,
                    onClick = { viewModel.onSelesaiClicked(userId) },
                )
            }
        }

        DetectionStatus.FINISHED -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                PrimaryActionButton(
                    modifier = Modifier.weight(1f),
                    text = "Rekam",
                    icon = Icons.Filled.Mic,
                    containerColor = MaterialTheme.colorScheme.primary,
                    onClick = onRekamClick,
                )
                PrimaryActionButton(
                    modifier = Modifier.weight(1f),
                    text = "Riwayat",
                    icon = Icons.Filled.History,
                    containerColor = MaterialTheme.colorScheme.secondary,
                    onClick = onNavigateToRiwayat,
                )
            }
        }
    }
}

@Composable
private fun PrimaryActionButton(
    modifier: Modifier = Modifier,
    text: String,
    icon: ImageVector,
    containerColor: Color,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = containerColor),
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
    }
}

@Composable
private fun LogDeteksiSection(entries: List<DetectionLogEntry>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(16.dp))
            .padding(16.dp),
    ) {
        Text(
            text = "Log Deteksi",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (MaterialTheme.colorScheme.primary == ProsodiPrimary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (entries.isEmpty()) {
            Text(
                text = "Menunggu siklus analisis pertama (10 detik).",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
        } else {
            LazyColumn(modifier = Modifier.height((entries.size.coerceAtMost(5) * 40).dp)) {
                items(entries) { entry ->
                    LogDeteksiItem(entry = entry)
                }
            }
        }
    }
}

@Composable
private fun LogDeteksiItem(entry: DetectionLogEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = formatDuration(entry.elapsedSeconds), fontSize = 12.sp, color = Color(0xFF888888))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = entry.emotion.emoji, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = entry.emotion.displayName,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = emotionColor(entry.emotion),
            )
        }
    }
}

@Composable
private fun emotionColor(emotion: EmotionLabel): Color = when (emotion) {
    EmotionLabel.SENANG -> Color(0xFF4A90E2)
    EmotionLabel.SEDIH -> if (MaterialTheme.colorScheme.primary == ProsodiPrimary) ProsodiPrimary else MaterialTheme.colorScheme.onBackground
    EmotionLabel.MARAH -> Color(0xFFFF7A59)
    EmotionLabel.NETRAL -> Color(0xFF888888)
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun DetectionScreenPreview() {
    ProsodiDownApp4Theme {
        DetectionScreen()
    }
}