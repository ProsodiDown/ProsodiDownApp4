package com.example.prosodidownapp4.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.prosodidownapp4.ui.theme.ProsodiDownApp4Theme
import com.example.prosodidownapp4.ui.theme.ProsodiPrimary

// =============================================================================
// Data class
// =============================================================================

data class BottomNavItem(
    val id: String,
    val icon: ImageVector,
    val contentDescription: String,
)

// =============================================================================
// BottomNavBar
// =============================================================================

@Composable
fun BottomNavBar(
    items: List<BottomNavItem>,
    activeItemId: String,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    barColor: Color = ProsodiPrimary,
    barHeight: Dp = 64.dp,
    fabRadius: Dp = 28.dp,
    notchMargin: Dp = 6.dp,
) {
    val activeIndex = items.indexOfFirst { it.id == activeItemId }.coerceAtLeast(0)
    val density = LocalDensity.current
    val notchRadiusDp = fabRadius + notchMargin
    val totalHeight = barHeight + fabRadius

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(totalHeight),
    ) {
        val slotWidth = maxWidth / items.size
        val targetCenterX = slotWidth * activeIndex + slotWidth / 2

        val animatedCenterX by animateDpAsState(
            targetValue = targetCenterX,
            animationSpec = tween(durationMillis = 280),
            label = "notchCenterX",
        )

        val barShape = remember(animatedCenterX, density) {
            NotchedBarShape(
                notchCenterXPx = with(density) { animatedCenterX.toPx() },
                notchRadiusPx  = with(density) { notchRadiusDp.toPx() },
                curveSweepPx   = with(density) { 32.dp.toPx() },
            )
        }

        // ── Layer 1: shadow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .align(Alignment.BottomCenter)
                .shadow(
                    elevation = 16.dp,
                    shape = RectangleShape,
                    clip = false,
                ),
        )

        // ── Layer 2: bar dengan notch
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .align(Alignment.BottomCenter)
                .clip(barShape)
                .background(barColor),
        )

        // ── Ikon non-aktif
        items.forEachIndexed { index, item ->
            if (index != activeIndex) {
                val slotCenterX = slotWidth * index + slotWidth / 2
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .offset(
                            x = slotCenterX - 24.dp,
                            y = -(barHeight / 2 - 20.dp),
                        )
                        .size(48.dp)
                        .clickable { onItemSelected(item.id) },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.contentDescription,
                        tint = Color.White.copy(alpha = 0.70f),
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
        }

        // ── FAB lingkaran aktif
        val fabSize = fabRadius * 2
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(
                    x = animatedCenterX - fabRadius,
                    y = -(barHeight - fabRadius + notchMargin / 2),
                )
                .size(fabSize)
                .shadow(elevation = 6.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(barColor)
                .clickable { onItemSelected(items[activeIndex].id) },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = items[activeIndex].icon,
                contentDescription = items[activeIndex].contentDescription,
                tint = Color.White,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

// =============================================================================
// Shape — pinggir lurus, notch smooth & membulat di shoulder
// =============================================================================

private class NotchedBarShape(
    private val notchCenterXPx: Float,
    private val notchRadiusPx: Float,
    private val curveSweepPx: Float,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val w     = size.width
        val h     = size.height
        val cx    = notchCenterXPx
        val nr    = notchRadiusPx
        val sw    = curveSweepPx
        val depth = nr  // kedalaman notch tetap sama seperti radius

        val path = Path().apply {
            moveTo(0f, h)
            lineTo(0f, 0f)
            lineTo(cx - nr - sw, 0f)

            // ── Shoulder kiri: bezier membulat masuk ke notch ──
            cubicTo(
                cx - nr - sw * 0.2f, 0f,    // CP1: hampir di garis atas, agak ke kiri
                cx - nr * 0.8f, depth,       // CP2: mendekati dasar notch
                cx, depth,                   // endpoint: dasar notch
            )

            // ── Shoulder kanan: bezier membulat keluar dari notch ──
            cubicTo(
                cx + nr * 0.8f, depth,       // CP1: mendekati dasar notch
                cx + nr + sw * 0.2f, 0f,    // CP2: hampir di garis atas, agak ke kanan
                cx + nr + sw, 0f,            // endpoint: balik ke garis atas
            )

            lineTo(w, 0f)
            lineTo(w, h)
            close()
        }

        return Outline.Generic(path)
    }
}

// =============================================================================
// Preview
// =============================================================================

@Preview(showBackground = true, backgroundColor = 0xFFEEEEEE, showSystemUi = true)
@Composable
private fun BottomNavBarPreview() {
    ProsodiDownApp4Theme {
        val items = listOf(
            BottomNavItem("home",      Icons.Filled.Home,                "Beranda"),
            BottomNavItem("detection", Icons.Filled.Mic,                 "Deteksi Emosi"),
            BottomNavItem("history",   Icons.Filled.History,             "Riwayat"),
            BottomNavItem("auth",      Icons.AutoMirrored.Filled.Logout, "Keluar"),
        )
        Box(modifier = Modifier.fillMaxSize()) {
            BottomNavBar(
                items          = items,
                activeItemId   = "home",
                onItemSelected = {},
                modifier       = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}