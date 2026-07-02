package com.example.prosodidownapp4.ui.history

import android.app.Application
import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.prosodidownapp4.ui.components.BottomNavBar
import com.example.prosodidownapp4.ui.components.BottomNavItem
import com.example.prosodidownapp4.ui.home.HomeNavMenu
import com.example.prosodidownapp4.ui.theme.EmotionMarah
import com.example.prosodidownapp4.ui.theme.EmotionNetral
import com.example.prosodidownapp4.ui.theme.EmotionSedih
import com.example.prosodidownapp4.ui.theme.EmotionSenang
import com.example.prosodidownapp4.ui.theme.ProsodiDownApp4Theme
import com.example.prosodidownapp4.ui.theme.ProsodiPrimary
import com.example.prosodidownapp4.ui.theme.ProsodiSecondary
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

// =============================================================================
// Data classes
// =============================================================================

enum class FilterType { HARI, BULAN, TAHUN }

data class EmotionStat(
    val label: String,
    val count: Int,
    val color: Color,
)

data class SessionLog(
    val sessionId: Int,
    val tanggal: String,
    val durasi: String,
    val emotions: List<EmotionDetail>,
    val totalDeteksi: Int,
)

data class EmotionDetail(
    val label: String,
    val emoji: String,
    val color: Color,
)

// =============================================================================
// HistoryScreen
// =============================================================================

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(
            LocalContext.current.applicationContext as Application
        )
    ),
    onNavigateToBeranda: () -> Unit = {},
    onNavigateToDeteksi: () -> Unit = {},
    onLogout: () -> Unit = {},
) {
    val filterType by viewModel.filterType.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()
    val availableYears by viewModel.availableYears.collectAsState()

    val sessionLogs by viewModel.sessionLogs.collectAsState()
    val emotionStats by viewModel.emotionStats.collectAsState()

    val totalDeteksi = emotionStats.sumOf { it.count }
    val totalSesi    = sessionLogs.size

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F6FA)),
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 96.dp),
        ) {
            item { HistoryHeader() }
            item {
                FilterDownloadSection(
                    filterType     = filterType,
                    onFilterChange = { viewModel.updateFilterType(it) },
                    selectedDate   = selectedDate,
                    onDateChange   = { viewModel.updateSelectedDate(it) },
                    selectedMonth  = selectedMonth,
                    onMonthChange  = { viewModel.updateSelectedMonth(it) },
                    selectedYear   = selectedYear,
                    onYearChange   = { viewModel.updateSelectedYear(it) },
                    availableYears = availableYears,
                    onDownload     = { },
                )
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item { StatCardsRow(totalDeteksi = totalDeteksi, totalSesi = totalSesi) }
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item { EmotionBarChartSection(stats = emotionStats, total = totalDeteksi) }
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item { EmotionDonutChartSection(stats = emotionStats, total = totalDeteksi) }
            item { Spacer(modifier = Modifier.height(20.dp)) }
            item { SessionLogHeader() }
            item { Spacer(modifier = Modifier.height(10.dp)) }
            if (sessionLogs.isEmpty()) {
                item { EmptyLogSection() }
            } else {
                items(sessionLogs) { log ->
                    SessionLogItem(log = log)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }

        BottomNavBar(
            items = listOf(
                BottomNavItem(HomeNavMenu.BERANDA,       Icons.Filled.Home,                 "Beranda"),
                BottomNavItem(HomeNavMenu.DETEKSI_EMOSI, Icons.Filled.Mic,                  "Deteksi Emosi"),
                BottomNavItem(HomeNavMenu.RIWAYAT,       Icons.Filled.History,              "Riwayat"),
                BottomNavItem(HomeNavMenu.AUTH,          Icons.AutoMirrored.Filled.Logout,  "Keluar"),
            ),
            activeItemId   = HomeNavMenu.RIWAYAT,
            onItemSelected = { id ->
                when (id) {
                    HomeNavMenu.BERANDA       -> onNavigateToBeranda()
                    HomeNavMenu.DETEKSI_EMOSI -> onNavigateToDeteksi()
                    HomeNavMenu.AUTH          -> onLogout()
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

// =============================================================================
// Header
// =============================================================================

@Composable
private fun HistoryHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF4F6FA))
            .padding(horizontal = 16.dp)
            .padding(top = 60.dp, bottom = 16.dp),
    ) {
        Text(
            text = "Riwayat",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A2E),
        )
        Text(
            text = "Statistik deteksi emosi dari waktu ke waktu",
            fontSize = 12.sp,
            color = ProsodiPrimary,
        )
    }
}

// =============================================================================
// Filter + Download
// =============================================================================

@Composable
private fun FilterDownloadSection(
    filterType: FilterType,
    onFilterChange: (FilterType) -> Unit,
    selectedDate: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    selectedMonth: Int,
    onMonthChange: (Int) -> Unit,
    selectedYear: Int,
    onYearChange: (Int) -> Unit,
    availableYears: List<Int>,
    onDownload: () -> Unit,
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF4F6FA))
            .padding(horizontal = 20.dp, vertical = 20.dp),
    ) {
        Text(
            text = "Unduh Riwayat",
            color = Color(0xFF1A1A2E),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(14.dp))

        // ── Segmented control tanpa shadow — tidak kedip ──────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFEBEBEB))
                .padding(4.dp),
        ) {
            FilterType.entries.forEach { type ->
                val isActive = type == filterType
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isActive) Color.White else Color.Transparent
                        )
                        .clickable { onFilterChange(type) }
                        .padding(vertical = 9.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = when (type) {
                            FilterType.HARI  -> "Hari"
                            FilterType.BULAN -> "Bulan"
                            FilterType.TAHUN -> "Tahun"
                        },
                        fontSize = 13.sp,
                        fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isActive) ProsodiPrimary else Color(0xFF8A94A6),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── Sub-filter ────────────────────────────────────────────────────
        when (filterType) {
            FilterType.HARI -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                        .background(Color(0xFFFAFAFA))
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = selectedDate.format(
                            DateTimeFormatter.ofPattern("dd MMMM yyyy")
                        ),
                        modifier = Modifier.weight(1f),
                        fontSize = 13.sp,
                        color = Color(0xFF1A1A2E),
                    )
                    IconButton(
                        onClick = {
                            val cal = Calendar.getInstance()
                            DatePickerDialog(
                                context,
                                { _, y, m, d -> onDateChange(LocalDate.of(y, m + 1, d)) },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH),
                            ).show()
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CalendarMonth,
                            contentDescription = "Pilih Tanggal",
                            tint = ProsodiPrimary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }

            FilterType.BULAN -> {
                val bulanList = listOf(
                    "Januari","Februari","Maret","April","Mei","Juni",
                    "Juli","Agustus","September","Oktober","November","Desember",
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    DropdownSelector(
                        modifier = Modifier.weight(1f),
                        label    = bulanList[selectedMonth - 1],
                        options  = bulanList,
                        onSelect = { idx -> onMonthChange(idx + 1) },
                    )
                    DropdownSelector(
                        modifier = Modifier.weight(1f),
                        label    = selectedYear.toString(),
                        options  = availableYears.map { it.toString() },
                        onSelect = { idx -> onYearChange(availableYears[idx]) },
                    )
                }
            }

            FilterType.TAHUN -> {
                DropdownSelector(
                    modifier = Modifier.fillMaxWidth(),
                    label    = selectedYear.toString(),
                    options  = availableYears.map { it.toString() },
                    onSelect = { idx -> onYearChange(availableYears[idx]) },
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = onDownload,
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ProsodiPrimary),
        ) {
            Icon(
                imageVector = Icons.Filled.Download,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Unduh CSV",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
            )
        }
    }
}

@Composable
private fun DropdownSelector(
    modifier: Modifier = Modifier,
    label: String,
    options: List<String>,
    onSelect: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                .background(Color(0xFFFAFAFA))
                .clickable { expanded = true }
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = label, fontSize = 13.sp, color = Color(0xFF1A1A2E))
            Text(text = "▾", fontSize = 12.sp, color = Color(0xFF8A94A6))
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEachIndexed { idx, opt ->
                DropdownMenuItem(
                    text = { Text(text = opt, fontSize = 13.sp) },
                    onClick = {
                        onSelect(idx)
                        expanded = false
                    },
                )
            }
        }
    }
}

// =============================================================================
// Stat Cards
// =============================================================================

@Composable
private fun StatCardsRow(totalDeteksi: Int, totalSesi: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            label    = "Total Deteksi",
            value    = totalDeteksi.toString(),
            sub      = "per 10 detik",
        )
        StatCard(
            modifier = Modifier.weight(1f),
            label    = "Total Sesi",
            value    = totalSesi.toString(),
            sub      = "sesi rekaman",
        )
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    sub: String,
) {
    Column(
        modifier = modifier
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(16.dp),
    ) {
        Text(
            text = label,
            color = Color(0xFF8A94A6),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value,
            color = ProsodiPrimary,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = sub,
            color = Color(0xFF8A94A6),
            fontSize = 10.sp,
        )
    }
}

// =============================================================================
// Bar Chart
// =============================================================================

@Composable
private fun EmotionBarChartSection(stats: List<EmotionStat>, total: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(16.dp),
    ) {
        Text(
            text = "Distribusi Emosi",
            color = Color(0xFF1A1A2E),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(16.dp))
        stats.forEach { stat ->
            val fraction = if (total > 0) stat.count.toFloat() / total else 0f
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stat.label,
                    modifier = Modifier.width(56.dp),
                    fontSize = 12.sp,
                    color = Color(0xFF1A1A2E),
                    fontWeight = FontWeight.Medium,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(Color(0xFFEEF0F5)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction)
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(stat.color),
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stat.count.toString(),
                    fontSize = 12.sp,
                    color = Color(0xFF8A94A6),
                    modifier = Modifier.width(28.dp),
                    textAlign = TextAlign.End,
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

// =============================================================================
// Donut Chart
// =============================================================================

@Composable
private fun EmotionDonutChartSection(stats: List<EmotionStat>, total: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(16.dp),
    ) {
        Text(
            text = "Proporsi Emosi",
            color = Color(0xFF1A1A2E),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .drawBehind { drawDonut(stats = stats, total = total) },
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = total.toString(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = ProsodiPrimary,
                    )
                    Text(
                        text = "total",
                        fontSize = 10.sp,
                        color = Color(0xFF8A94A6),
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                stats.forEach { stat ->
                    val pct = if (total > 0) (stat.count * 100f / total).toInt() else 0
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(stat.color),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${stat.label}  $pct%",
                            fontSize = 12.sp,
                            color = Color(0xFF1A1A2E),
                        )
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawDonut(stats: List<EmotionStat>, total: Int) {
    if (total == 0) return
    val stroke     = 22.dp.toPx()
    val diameter   = size.minDimension - stroke
    val topLeft    = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
    val arcSize    = Size(diameter, diameter)
    var startAngle = -90f
    stats.forEach { stat ->
        val sweep = stat.count.toFloat() / total * 360f
        drawArc(
            color      = stat.color,
            startAngle = startAngle,
            sweepAngle = sweep,
            useCenter  = false,
            topLeft    = topLeft,
            size       = arcSize,
            style      = Stroke(width = stroke),
        )
        startAngle += sweep
    }
}

// =============================================================================
// Session Log
// =============================================================================

@Composable
private fun SessionLogHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
    ) {
        Text(
            text = "Log Riwayat Sesi",
            color = Color(0xFF1A1A2E),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Sesi yang dilakukan pada periode terpilih",
            color = Color(0xFF8A94A6),
            fontSize = 12.sp,
        )
    }
}

@Composable
private fun EmptyLogSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(vertical = 36.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "📭", fontSize = 32.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Anda belum melakukan\ndeteksi emosi",
                color = Color(0xFF8A94A6),
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
            )
        }
    }
}

@Composable
private fun SessionLogItem(log: SessionLog) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .shadow(1.dp, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(ProsodiPrimary.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "#${log.sessionId}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = ProsodiPrimary,
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = log.tanggal,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A1A2E),
            )
            Text(
                text = "Durasi ${log.durasi}  ·  ${log.totalDeteksi} deteksi",
                fontSize = 11.sp,
                color = Color(0xFF8A94A6),
            )
            if (log.emotions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    log.emotions.forEach { emotion ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(emotion.color.copy(alpha = 0.1f))
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = emotion.emoji, fontSize = 10.sp)
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = emotion.label,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = emotion.color,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// Preview
// =============================================================================

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HistoryScreenPreview() {
    ProsodiDownApp4Theme {
        HistoryScreen()
    }
}