package com.example.prosodidownapp4.ui.splash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prosodidownapp4.R
import com.example.prosodidownapp4.ui.theme.ProsodiDownApp4Theme
import com.example.prosodidownapp4.ui.theme.ProsodiPrimary
import kotlinx.coroutines.delay
import kotlin.math.abs

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    var alphaTarget by remember { mutableStateOf(0f) }
    val animatedAlpha by animateFloatAsState(
        targetValue = alphaTarget,
        animationSpec = tween(
            durationMillis = if (alphaTarget == 1f) 400 else 200,
            easing = FastOutSlowInEasing,
        ),
        label = "splashAlpha",
    )

    LaunchedEffect(Unit) {
        alphaTarget = 1f
        delay(1200)
        alphaTarget = 0f
        delay(200)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .graphicsLayer { alpha = animatedAlpha },
    ) {
        // ── Konten utama: logo, teks, titik-titik ──────────────────────
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo1),
                contentDescription = "Logo Prosodi Down",
                modifier = Modifier.size(96.dp),
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Prosodi Down",
                color = ProsodiPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Deteksi Emosi Anak Down Syndrome",
                color = ProsodiPrimary.copy(alpha = 0.55f),
                fontSize = 13.sp,
                fontStyle = FontStyle.Italic,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(48.dp))

            ThreeDotsLoading()
        }

        // ── Footer ────────────────────────────────────────────────────────
        Text(
            text = "2026 · Universitas Negeri Malang",
            color = ProsodiPrimary.copy(alpha = 0.30f),
            fontSize = 10.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
        )
    }
}

@Composable
private fun ThreeDotsLoading() {
    val infiniteTransition = rememberInfiniteTransition(label = "dots_wave")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "dots_wave_progress",
    )

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(3) { index ->
            val dotCenter = (index + 0.5f) / 3f
            val distance = abs(progress - dotCenter)
            val alpha = (1f - (distance / 0.5f).coerceIn(0f, 1f)).coerceIn(0.2f, 1f)
            val scale = 0.7f + (alpha - 0.2f) / 0.8f * 0.5f
            Dot(alpha = alpha, scale = scale)
        }
    }
}

@Composable
private fun Dot(alpha: Float, scale: Float = 1f) {
    Spacer(
        modifier = Modifier
            .size(8.dp)
            .graphicsLayer {
                this.alpha = alpha
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .background(ProsodiPrimary)
    )
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    ProsodiDownApp4Theme {
        SplashScreen(onSplashFinished = {})
    }
}