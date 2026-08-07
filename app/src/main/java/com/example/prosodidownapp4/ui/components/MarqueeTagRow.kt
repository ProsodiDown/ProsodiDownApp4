package com.example.prosodidownapp4.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prosodidownapp4.ui.theme.ProsodiPrimary
import kotlin.math.roundToInt

/**
 * Satu Row tag yang berjalan kiri tanpa jeda.
 * Menggunakan SubcomposeLayout untuk mengukur lebar satu set tag,
 * lalu menduplikat konten tepat sebanyak yang dibutuhkan agar loop seamless.
 */
@Composable
fun MarqueeTagRow(
    modifier   : Modifier = Modifier,
    tags       : List<String>,
    durationMs : Int   = 12000,
    tagColor   : Color = ProsodiPrimary,
) {
    var singleSetWidth by remember { mutableIntStateOf(1) }

    val infiniteTransition = rememberInfiniteTransition(label = "marquee")
    val offsetX by infiniteTransition.animateFloat(
        initialValue  = 0f,
        targetValue   = -singleSetWidth.toFloat(),
        animationSpec = infiniteRepeatable(
            animation  = tween(durationMillis = durationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "marqueeOffset",
    )

    SubcomposeLayout(
        modifier = modifier
            .fillMaxWidth()
            .clipToBounds(),
    ) { constraints ->
        // Langkah 1: ukur lebar satu set tag tanpa batasan lebar
        val freeConstraints = Constraints()
        val singlePlaceables = subcompose("measure") {
            TagSet(tags = tags, color = tagColor)
        }.map { it.measure(freeConstraints) }

        val measuredWidth = singlePlaceables.sumOf { it.width }
        if (measuredWidth > 0) singleSetWidth = measuredWidth

        val height = singlePlaceables.maxOfOrNull { it.height } ?: 0

        // Langkah 2: hitung berapa kali set harus diulang agar memenuhi lebar layar + 1
        val repeatCount = if (measuredWidth > 0)
            (constraints.maxWidth / measuredWidth) + 2
        else 2

        // Langkah 3: ukur semua item yang akan ditampilkan
        val allPlaceables = subcompose("content") {
            repeat(repeatCount) {
                TagSet(tags = tags, color = tagColor)
            }
        }.map { it.measure(freeConstraints) }

        layout(constraints.maxWidth, height) {
            var xPos = offsetX.roundToInt()
            // Wrap agar selalu dalam rentang [-singleSetWidth, 0]
            if (singleSetWidth > 0) {
                xPos = ((xPos % singleSetWidth) - singleSetWidth) % singleSetWidth
            }
            allPlaceables.forEach { placeable ->
                placeable.placeRelative(xPos, 0)
                xPos += placeable.width
            }
        }
    }
}

/** Satu set semua tag dalam Row tanpa padding luar */
@Composable
private fun TagSet(tags: List<String>, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        tags.forEach { tag ->
            Text(
                text       = "#$tag",
                color      = color,
                fontSize   = 11.sp,
                fontWeight = FontWeight.Medium,
                modifier   = Modifier
                    .padding(end = 8.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .background(color.copy(alpha = 0.08f))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            )
        }
    }
}