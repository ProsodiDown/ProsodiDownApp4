package com.example.prosodidownapp4.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prosodidownapp4.R
import com.example.prosodidownapp4.ui.components.BottomNavBar
import com.example.prosodidownapp4.ui.components.BottomNavItem
import com.example.prosodidownapp4.ui.components.MarqueeTagRow
import com.example.prosodidownapp4.ui.theme.EmotionMarah
import com.example.prosodidownapp4.ui.theme.EmotionNetral
import com.example.prosodidownapp4.ui.theme.EmotionSedih
import com.example.prosodidownapp4.ui.theme.EmotionSenang
import com.example.prosodidownapp4.ui.theme.ProsodiAccent
import com.example.prosodidownapp4.ui.theme.ProsodiDownApp4Theme
import com.example.prosodidownapp4.ui.theme.ProsodiPrimary
import com.example.prosodidownapp4.ui.theme.ProsodiSecondary

// =============================================================================
// Data classes
// =============================================================================

private data class EmosiItem(
    val emoji: String,
    val label: String,
    val color: Color,
    val bgColor: Color,
)

private data class StepItem(
    val number: String,
    val icon: ImageVector,
    val title: String,
    val description: String,
)

object HomeNavMenu {
    const val BERANDA       = "beranda"
    const val DETEKSI_EMOSI = "deteksi_emosi"
    const val RIWAYAT       = "riwayat"
    const val AUTH          = "auth"
}

// =============================================================================
// HomeScreen
// =============================================================================

@Composable
fun HomeScreen(
    isLoggedIn: Boolean = true,
    onMulaiRekam: () -> Unit = {},
    onNavigateToDeteksi: () -> Unit = {},
    onNavigateToRiwayat: () -> Unit = {},
    onNavigateToDaftar: () -> Unit = {},
    onLogout: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F6FA)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            HeroSection(onMulaiRekam = onMulaiRekam)
            Spacer(modifier = Modifier.height(28.dp))
            EmosiGridSection()
            Spacer(modifier = Modifier.height(8.dp))
            MarqueeSection()
            Spacer(modifier = Modifier.height(28.dp))
            StepsSection()
            Spacer(modifier = Modifier.height(28.dp))
            CtaSection(onGabungSekarang = onNavigateToDaftar)
            Spacer(modifier = Modifier.height(28.dp))
            AppFooterSection()
        }

        BottomNavBar(
            items = listOf(
                BottomNavItem(HomeNavMenu.BERANDA,       Icons.Filled.Home,                "Beranda"),
                BottomNavItem(HomeNavMenu.DETEKSI_EMOSI, Icons.Filled.Mic,                "Deteksi Emosi"),
                BottomNavItem(HomeNavMenu.RIWAYAT,       Icons.Filled.History,            "Riwayat"),
                BottomNavItem(HomeNavMenu.AUTH,          Icons.AutoMirrored.Filled.Logout, if (isLoggedIn) "Keluar" else "Masuk"),
            ),
            activeItemId   = HomeNavMenu.BERANDA,
            onItemSelected = { id ->
                when (id) {
                    HomeNavMenu.DETEKSI_EMOSI -> onNavigateToDeteksi()
                    HomeNavMenu.RIWAYAT       -> onNavigateToRiwayat()
                    HomeNavMenu.AUTH          -> if (isLoggedIn) onLogout() else onNavigateToLogin()
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

// =============================================================================
// Hero Section
// =============================================================================

@Composable
private fun HeroSection(onMulaiRekam: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(480.dp),
    ) {
        Image(
            painter = painterResource(id = R.drawable.bg_hero1),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f  to Color(0xFF07111F).copy(alpha = 0.35f),
                            0.45f to Color(0xFF07111F).copy(alpha = 0.55f),
                            1.0f  to Color(0xFF07111F).copy(alpha = 0.92f),
                        ),
                    )
                ),
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(Color.White.copy(alpha = 0.15f))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
            ) {
                Text(
                    text = "✦  PKM-RSH · Universitas Negeri Malang",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Mengungkap\nRasa di Balik\nSuara",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 38.sp,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Teknologi deep learning untuk memahami emosi anak Down Syndrome secara real-time.",
                color = Color.White.copy(alpha = 0.80f),
                fontSize = 13.sp,
                lineHeight = 20.sp,
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onMulaiRekam,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ProsodiAccent),
                modifier = Modifier.height(50.dp),
                contentPadding = PaddingValues(horizontal = 24.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Mic,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Mulai Rekam",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                )
            }
        }
    }
}

// =============================================================================
// Emosi Grid — 2×2
// =============================================================================

@Composable
private fun EmosiGridSection() {
    val emosi = listOf(
        EmosiItem("😄", "Senang", EmotionSenang, EmotionSenang.copy(alpha = 0.12f)),
        EmosiItem("😢", "Sedih",  EmotionSedih,  EmotionSedih.copy(alpha = 0.10f)),
        EmosiItem("😡", "Marah",  EmotionMarah,  EmotionMarah.copy(alpha = 0.12f)),
        EmosiItem("😐", "Netral", EmotionNetral, EmotionNetral.copy(alpha = 0.15f)),
    )

    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(
            text = "Emosi yang Dideteksi",
            color = Color(0xFF1A1A2E),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "4 kelas emosi yang dikenali sistem",
            color = Color(0xFF8A94A6),
            fontSize = 12.sp,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            EmosiCard(item = emosi[0], modifier = Modifier.weight(1f))
            EmosiCard(item = emosi[1], modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            EmosiCard(item = emosi[2], modifier = Modifier.weight(1f))
            EmosiCard(item = emosi[3], modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun EmosiCard(item: EmosiItem, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(item.bgColor),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = item.emoji, fontSize = 18.sp)
        }
        Column {
            Text(
                text = item.label,
                color = Color(0xFF1A1A2E),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(3.dp))
            Box(
                modifier = Modifier
                    .width(20.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(item.color),
            )
        }
    }
}

// =============================================================================
// Marquee
// =============================================================================

@Composable
private fun MarqueeSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(ProsodiPrimary.copy(alpha = 0.04f))
            .padding(vertical = 10.dp),
    ) {
        MarqueeTagRow(
            tags = listOf(
                "Down Syndrome",
                "Akustik Prosodi",
                "Speech Emotion Recognition",
                "Deep Learning",
                "Real-time",
                "TFLite",
            ),
            durationMs = 10000,
        )
    }
}

// =============================================================================
// Steps — grid 2×2 statis
// =============================================================================

@Composable
private fun StepsSection() {
    val steps = listOf(
        StepItem("01", Icons.Filled.Home,      "Masuk ke Akun",      "Login dengan akun terdaftar untuk mengakses semua fitur."),
        StepItem("02", Icons.Filled.Mic,       "Mulai Rekaman",      "Tekan rekam, minta anak berbicara di ruang yang tenang."),
        StepItem("03", Icons.Filled.Analytics, "Analisis Real-time", "Sistem menganalisis prosodi setiap 10 detik secara langsung."),
        StepItem("04", Icons.Filled.History,   "Pantau Riwayat",     "Lihat statistik emosi dari waktu ke waktu dan unduh CSV."),
    )

    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(
            text = "Cara Penggunaan",
            color = Color(0xFF1A1A2E),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "4 langkah mudah",
            color = Color(0xFF8A94A6),
            fontSize = 12.sp,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StepCard(step = steps[0], modifier = Modifier.weight(1f))
            StepCard(step = steps[1], modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StepCard(step = steps[2], modifier = Modifier.weight(1f))
            StepCard(step = steps[3], modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun StepCard(step: StepItem, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(14.dp),
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = step.number,
                    color = ProsodiPrimary.copy(alpha = 0.12f),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(ProsodiPrimary.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = step.icon,
                        contentDescription = null,
                        tint = ProsodiPrimary,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = step.title,
                color = Color(0xFF1A1A2E),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 17.sp,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = step.description,
                color = Color(0xFF8A94A6),
                fontSize = 10.sp,
                lineHeight = 14.sp,
            )
        }
    }
}

// =============================================================================
// CTA Section
// =============================================================================

@Composable
private fun CtaSection(onGabungSekarang: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(ProsodiPrimary, ProsodiSecondary),
                )
            )
            .padding(horizontal = 24.dp, vertical = 28.dp),
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            Text(
                text = "Mulai perjalanan mengungkap rasa bersama kami?",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Bergabung dengan Aplikasi Prosodi Down dan kenali emosi anak sekarang.",
                color = Color.White.copy(alpha = 0.80f),
                fontSize = 13.sp,
                lineHeight = 19.sp,
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onGabungSekarang,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmotionSenang),
                modifier = Modifier
                    .height(46.dp)
                    .widthIn(min = 160.dp),
                contentPadding = PaddingValues(horizontal = 20.dp),
            ) {
                Text(
                    text = "Gabung Sekarang",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                )
            }
        }
    }
}

// =============================================================================
// App Footer
// =============================================================================

@Composable
private fun AppFooterSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1A1A2E))
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp, bottom = 100.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Tim Prosodi Down",
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "PKM-RSH · Universitas Negeri Malang · 2026",
            color = Color(0xFFB0BAC8),
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(14.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .height(1.dp)
                .background(Color.White.copy(alpha = 0.10f)),
        )
        Spacer(modifier = Modifier.height(14.dp))
        listOf(
            "Instagram  @pkmrsh_prosodidown",
            "TikTok  @pkmrsh_prosodidown",
            "prosodidownum@gmail.com",
        ).forEach { info ->
            Text(
                text = info,
                color = Color(0xFF8A94A6),
                fontSize = 11.sp,
                modifier = Modifier.padding(vertical = 2.dp),
            )
        }
    }
}

// =============================================================================
// Preview
// =============================================================================

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    ProsodiDownApp4Theme {
        HomeScreen()
    }
}