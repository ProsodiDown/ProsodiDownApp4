package com.example.prosodidownapp4.ui.detection

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.prosodidownapp4.data.local.db.AppDatabase
import com.example.prosodidownapp4.data.local.db.SessionEntity
import com.example.prosodidownapp4.data.repository.SessionRepository
import com.example.prosodidownapp4.ml.AudioProcessor
import com.example.prosodidownapp4.ml.EmotionClassifier
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

enum class DetectionStatus { IDLE, RECORDING, PAUSED, FINISHED }

enum class EmotionLabel(val displayName: String, val emoji: String) {
    NETRAL("Netral", "\uD83D\uDE10"),
    SENANG("Senang", "\uD83D\uDE0A"),
    SEDIH("Sedih", "\uD83D\uDE22"),
    MARAH("Marah", "\uD83D\uDE21"),
}

data class DetectionLogEntry(
    val elapsedSeconds: Int,
    val emotion: EmotionLabel,
)

data class DetectionUiState(
    val status: DetectionStatus = DetectionStatus.IDLE,
    val elapsedSeconds: Int = 0,
    val currentEmotion: EmotionLabel = EmotionLabel.NETRAL,
    val secondsUntilNextAnalysis: Int = ANALYSIS_INTERVAL_SECONDS,
    val logEntries: List<DetectionLogEntry> = emptyList(),
    val waveformLevels: List<Float> = List(WAVEFORM_BAR_COUNT) { 0.1f },
    val justFinishedSaving: Boolean = false,
) {
    companion object {
        const val ANALYSIS_INTERVAL_SECONDS = 10
        const val WAVEFORM_BAR_COUNT = 80
    }
}

class DetectionViewModel(application: Application) : AndroidViewModel(application) {

    private val audioProcessor = AudioProcessor()
    private val emotionClassifier = EmotionClassifier(application)
    private val repository: SessionRepository

    private val _uiState = MutableStateFlow(DetectionUiState())
    val uiState: StateFlow<DetectionUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var audioJob: Job? = null

    init {
        val database = AppDatabase.getDatabase(application)
        repository = SessionRepository(database.sessionDao())
        emotionClassifier.load()
    }

    fun onRekamClicked() {
        if (_uiState.value.status == DetectionStatus.FINISHED) {
            _uiState.value = DetectionUiState(status = DetectionStatus.RECORDING)
            audioProcessor.clearBuffer()
        } else {
            _uiState.update { it.copy(status = DetectionStatus.RECORDING) }
        }
        audioProcessor.startRecording()
        startTimer()
        startAudioLoop()
    }

    fun onJedaClicked() {
        _uiState.update { it.copy(status = DetectionStatus.PAUSED) }
        timerJob?.cancel()
        audioJob?.cancel()
        audioProcessor.pauseRecording()
    }

    fun onLanjutkanClicked() {
        _uiState.update { it.copy(status = DetectionStatus.RECORDING) }
        audioProcessor.resumeRecording()
        startTimer()
        startAudioLoop()
    }

    fun onSelesaiClicked() {
        val currentState = _uiState.value
        if (currentState.logEntries.isNotEmpty()) {
            viewModelScope.launch {
                val dominant = currentState.logEntries.groupBy { it.emotion }
                    .maxByOrNull { it.value.size }?.key ?: EmotionLabel.NETRAL

                val json = currentState.logEntries.joinToString(prefix = "[", postfix = "]") { entry ->
                    """{"s":${entry.elapsedSeconds},"e":"${entry.emotion.name}"}"""
                }

                repository.saveSession(
                    SessionEntity(
                        timestamp = System.currentTimeMillis(),
                        totalDurationSeconds = currentState.elapsedSeconds,
                        dominantEmotion = dominant.name,
                        logJson = json
                    )
                )
            }
        }

        timerJob?.cancel()
        audioJob?.cancel()
        audioProcessor.stopRecording()
        _uiState.update {
            it.copy(
                status = DetectionStatus.FINISHED,
                elapsedSeconds = 0,
                currentEmotion = EmotionLabel.NETRAL,
                secondsUntilNextAnalysis = DetectionUiState.ANALYSIS_INTERVAL_SECONDS,
                logEntries = emptyList(),
                waveformLevels = List(DetectionUiState.WAVEFORM_BAR_COUNT) { 0.1f },
                justFinishedSaving = true,
            )
        }
    }

    fun onSaveBannerDismissed() {
        _uiState.update { it.copy(justFinishedSaving = false) }
    }

    // ── Audio loop ────────────────────────────────────────────────────

    private fun startAudioLoop() {
        audioJob?.cancel()
        audioJob = viewModelScope.launch {
            while (true) {
                val amplitude = audioProcessor.readChunk()
                if (amplitude > 0f || _uiState.value.status == DetectionStatus.RECORDING) {
                    _uiState.update { state ->
                        val newLevels = state.waveformLevels.toMutableList().also {
                            if (it.isNotEmpty()) it.removeAt(0)
                            it.add(amplitude.coerceIn(0.01f, 1f))
                        }
                        state.copy(waveformLevels = newLevels)
                    }
                }
                delay(50.milliseconds) // Hindari loop terlalu cepat jika audioRecord belum siap
            }
        }
    }

    // ── Timer + inferensi tiap 10 detik ──────────────────────────────

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1.seconds)
                val newElapsed = _uiState.value.elapsedSeconds + 1
                val newCountdown = _uiState.value.secondsUntilNextAnalysis - 1

                if (newCountdown <= 0) {
                    val features = audioProcessor.extractFeaturesFromLastSegment()
                    val emotion = if (features != null) {
                        val (labelIdx, _) = emotionClassifier.classify(features)
                        EmotionLabel.entries[labelIdx]
                    } else {
                        EmotionLabel.NETRAL // fallback jika audio belum cukup
                    }

                    _uiState.update {
                        it.copy(
                            elapsedSeconds = newElapsed,
                            currentEmotion = emotion,
                            secondsUntilNextAnalysis = DetectionUiState.ANALYSIS_INTERVAL_SECONDS,
                            logEntries = listOf(
                                DetectionLogEntry(newElapsed, emotion)
                            ) + it.logEntries,
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            elapsedSeconds = newElapsed,
                            secondsUntilNextAnalysis = newCountdown,
                        )
                    }
                }
            }
        }
    }

    // ── Lifecycle ─────────────────────────────────────────────────────

    override fun onCleared() {
        timerJob?.cancel()
        audioJob?.cancel()
        audioProcessor.stopRecording()
        emotionClassifier.close()
    }
}
