package com.example.prosodidownapp4.ui.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.prosodidownapp4.data.local.db.AppDatabase
import com.example.prosodidownapp4.data.repository.SessionRepository
import com.example.prosodidownapp4.ui.detection.EmotionLabel
import com.example.prosodidownapp4.ui.theme.EmotionMarah
import com.example.prosodidownapp4.ui.theme.EmotionNetral
import com.example.prosodidownapp4.ui.theme.EmotionSedih
import com.example.prosodidownapp4.ui.theme.EmotionSenang
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: SessionRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = SessionRepository(database.sessionDao())
    }

    private val userId = MutableStateFlow(-1L)

    fun setUserId(id: Long) {
        userId.value = id
    }

    // Filter States
    val filterType = MutableStateFlow(FilterType.HARI)
    val selectedDate = MutableStateFlow(LocalDate.now())
    val selectedMonth = MutableStateFlow(LocalDate.now().monthValue)
    val selectedYear = MutableStateFlow(LocalDate.now().year)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val availableYears: StateFlow<List<Int>> = userId.flatMapLatest { id ->
        repository.getAvailableYearsByUser(id).map { years ->
            years.mapNotNull { it.toIntOrNull() }.sortedDescending()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private val filteredEntities = combine(
        userId.flatMapLatest { repository.getSessionsByUser(it) },
        filterType,
        selectedDate,
        selectedMonth,
        selectedYear
    ) { entities, type, date, month, year ->
        entities.filter { entity ->
            val entityDate = Instant.ofEpochMilli(entity.timestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            
            when (type) {
                FilterType.HARI -> entityDate == date
                FilterType.BULAN -> entityDate.year == year && entityDate.monthValue == month
                FilterType.TAHUN -> entityDate.year == year
            }
        }
    }

    val sessionLogs: StateFlow<List<SessionLog>> = filteredEntities.map { entities ->
        // ... (sama seperti sebelumnya)
        entities.map { entity ->
            val emotions = mutableListOf<EmotionDetail>()
            try {
                val jsonArray = JSONArray(entity.logJson)
                val emotionLabels = mutableSetOf<EmotionLabel>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val emotionName = obj.optString("e", "")
                    if (emotionName.isNotEmpty()) {
                        try {
                            emotionLabels.add(EmotionLabel.valueOf(emotionName))
                        } catch (e: IllegalArgumentException) {}
                    }
                }

                if (emotionLabels.isEmpty()) {
                    val dominantLabel = EmotionLabel.valueOf(entity.dominantEmotion)
                    emotionLabels.add(dominantLabel)
                }

                emotionLabels.forEach { label ->
                    emotions.add(
                        EmotionDetail(
                            label = label.displayName,
                            emoji = label.emoji,
                            color = when (label) {
                                EmotionLabel.SENANG -> EmotionSenang
                                EmotionLabel.SEDIH -> EmotionSedih
                                EmotionLabel.MARAH -> EmotionMarah
                                EmotionLabel.NETRAL -> EmotionNetral
                            }
                        )
                    )
                }
            } catch (e: Exception) {
                val dominantLabel = EmotionLabel.valueOf(entity.dominantEmotion)
                emotions.add(
                    EmotionDetail(
                        label = dominantLabel.displayName,
                        emoji = dominantLabel.emoji,
                        color = when (dominantLabel) {
                            EmotionLabel.SENANG -> EmotionSenang
                            EmotionLabel.SEDIH -> EmotionSedih
                            EmotionLabel.MARAH -> EmotionMarah
                            EmotionLabel.NETRAL -> EmotionNetral
                        }
                    )
                )
            }

            SessionLog(
                sessionId = entity.id.toInt(),
                tanggal = formatTimestamp(entity.timestamp),
                durasi = formatDuration(entity.totalDurationSeconds),
                emotions = emotions,
                totalDeteksi = entity.totalDurationSeconds / 10
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val emotionStats: StateFlow<List<EmotionStat>> = filteredEntities.map { entities ->
        // ... (sama seperti sebelumnya)
        val allEmotions = mutableListOf<EmotionLabel>()
        entities.forEach { entity ->
            try {
                val jsonArray = JSONArray(entity.logJson)
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val emotionName = obj.optString("e", "")
                    if (emotionName.isNotEmpty()) {
                        try {
                            allEmotions.add(EmotionLabel.valueOf(emotionName))
                        } catch (e: IllegalArgumentException) {}
                    }
                }
            } catch (e: Exception) {
                try {
                    allEmotions.add(EmotionLabel.valueOf(entity.dominantEmotion))
                } catch (ex: Exception) {}
            }
        }
        
        val counts = allEmotions.groupingBy { it }.eachCount()
        listOf(
            EmotionStat("Senang", counts[EmotionLabel.SENANG] ?: 0, EmotionSenang),
            EmotionStat("Sedih", counts[EmotionLabel.SEDIH] ?: 0, EmotionSedih),
            EmotionStat("Marah", counts[EmotionLabel.MARAH] ?: 0, EmotionMarah),
            EmotionStat("Netral", counts[EmotionLabel.NETRAL] ?: 0, EmotionNetral)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistoryByUser(userId.value)
        }
    }

    private fun formatTimestamp(millis: Long): String {
        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
        return Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).format(formatter)
    }

    private fun formatDuration(seconds: Int): String {
        val m = seconds / 60
        val s = seconds % 60
        return "${m}m ${s}s"
    }

    // Filter update methods
    fun updateFilterType(type: FilterType) { filterType.value = type }
    fun updateSelectedDate(date: LocalDate) { selectedDate.value = date }
    fun updateSelectedMonth(month: Int) { selectedMonth.value = month }
    fun updateSelectedYear(year: Int) { selectedYear.value = year }

    fun generateCsvContent(): String {
        val logs = sessionLogs.value
        val sb = StringBuilder()
        // Header
        sb.append("Session ID,Tanggal,Durasi,Emosi Terdeteksi,Total Deteksi\n")

        logs.forEach { log ->
            val emotionsStr = log.emotions.joinToString(" | ") { it.label }
            sb.append("${log.sessionId},${log.tanggal},${log.durasi},\"$emotionsStr\",${log.totalDeteksi}\n")
        }

        return sb.toString()
    }
}
