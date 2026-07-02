package com.example.prosodidownapp4.ml

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.*

/**
 * Menangani AudioRecord + ekstraksi fitur:
 * MFCC(40) + Delta-MFCC(40) + RMS(1) = 81 fitur per frame.
 * Config sesuai model_config.json.
 */
class AudioProcessor {

    companion object {
        const val SAMPLE_RATE = 16000
        const val SEGMENT_SAMPLES = 160000   // 10 detik * 16000
        const val N_MFCC = 40
        const val N_FFT = 2048
        const val HOP_LENGTH = 512
        const val N_FRAMES = 313
        const val N_FEATURES = 81            // 40 + 40 + 1
        const val TRIM_TOP_DB = 25f

        private const val NUM_MEL_FILTERS = 128
    }

    private var audioRecord: AudioRecord? = null
    private var isRecording = false

    // Buffer akumulasi audio mentah selama sesi
    private val audioBuffer = mutableListOf<Short>()

    // ── AudioRecord ──────────────────────────────────────────────────

    fun startRecording() {
        try {
            val minBuffer = AudioRecord.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
            )
            if (minBuffer <= 0) {
                isRecording = false
                return
            }
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                minBuffer * 4,
            )
            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                isRecording = false
                return
            }
            audioBuffer.clear()
            isRecording = true
            audioRecord?.startRecording()
        } catch (e: Exception) {
            e.printStackTrace()
            isRecording = false
        }
    }

    /**
     * Baca chunk audio dari mic secara suspend (dipanggil dari coroutine loop di ViewModel).
     * Mengembalikan RMS chunk untuk update waveform.
     */
    suspend fun readChunk(): Float = withContext(Dispatchers.IO) {
        val chunkSize = HOP_LENGTH  // baca per hop agar waveform responsif
        val chunk = ShortArray(chunkSize)
        val read = audioRecord?.read(chunk, 0, chunkSize) ?: 0
        if (read > 0) {
            audioBuffer.addAll(chunk.take(read))
            rms(chunk.take(read).toShortArray())
        } else 0f
    }

    fun pauseRecording() {
        isRecording = false
        audioRecord?.stop()
    }

    fun resumeRecording() {
        isRecording = true
        audioRecord?.startRecording()
    }

    fun stopRecording() {
        isRecording = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
    }

    fun clearBuffer() {
        audioBuffer.clear()
    }

    // ── Fitur Extraction ─────────────────────────────────────────────

    /**
     * Ambil 10 detik terakhir dari audioBuffer, ekstrak fitur,
     * kembalikan FloatArray shape [N_FRAMES * N_FEATURES] siap di-reshape ke [1, 313, 81].
     * Kembalikan null jika audio belum cukup.
     */
    fun extractFeaturesFromLastSegment(): Array<FloatArray>? {
        if (audioBuffer.size < SEGMENT_SAMPLES) return null

        // Ambil 10 detik terakhir
        val samples = audioBuffer
            .takeLast(SEGMENT_SAMPLES)
            .map { it / 32768f }
            .toFloatArray()

        // Trim silence
        val trimmed = trimSilence(samples)

        // Pad/truncate ke SEGMENT_SAMPLES
        val padded = FloatArray(SEGMENT_SAMPLES)
        trimmed.copyInto(padded, 0, 0, minOf(trimmed.size, SEGMENT_SAMPLES))

        // Ekstrak MFCC
        val mfcc = computeMfcc(padded)              // [N_FRAMES][N_MFCC]
        val deltaMfcc = computeDelta(mfcc)          // [N_FRAMES][N_MFCC]
        val rmsPerFrame = computeRmsPerFrame(padded) // [N_FRAMES][1]

        // Gabung jadi [N_FRAMES][81]
        val features = Array(N_FRAMES) { frame ->
            FloatArray(N_FEATURES).also { feat ->
                mfcc[frame].copyInto(feat, 0)
                deltaMfcc[frame].copyInto(feat, N_MFCC)
                feat[N_MFCC * 2] = rmsPerFrame[frame]
            }
        }

        return features
    }

    // ── DSP helpers ──────────────────────────────────────────────────

    private fun trimSilence(samples: FloatArray): FloatArray {
        val frameSize = N_FFT
        val threshold = 10f.pow(-TRIM_TOP_DB / 20f)
        var start = 0
        var end = samples.size

        outer@ for (i in samples.indices step HOP_LENGTH) {
            val frame = samples.slice(i until minOf(i + frameSize, samples.size))
            if (rmsFloat(frame) >= threshold) { start = i; break@outer }
        }
        outer@ for (i in samples.indices.reversed().step(HOP_LENGTH)) {
            val frame = samples.slice(maxOf(0, i - frameSize) until i)
            if (rmsFloat(frame) >= threshold) { end = i; break@outer }
        }
        return if (start < end) samples.sliceArray(start until end) else samples
    }

    private fun rmsFloat(samples: List<Float>): Float {
        if (samples.isEmpty()) return 0f
        return sqrt(samples.sumOf { (it * it).toDouble() }.toFloat() / samples.size)
    }

    private fun rms(samples: ShortArray): Float {
        if (samples.isEmpty()) return 0f
        val sum = samples.sumOf { s -> val f = s / 32768.0; f * f }
        return sqrt(sum / samples.size).toFloat()
    }

    // ── MFCC ─────────────────────────────────────────────────────────

    private fun computeMfcc(samples: FloatArray): Array<FloatArray> {
        val frames = Array(N_FRAMES) { FloatArray(N_MFCC) }
        val melFilters = buildMelFilterbank()

        for (t in 0 until N_FRAMES) {
            val start = t * HOP_LENGTH
            val frame = FloatArray(N_FFT)
            for (i in 0 until N_FFT) {
                val idx = start + i
                frame[i] = if (idx < samples.size) samples[idx] * hannWindow(i) else 0f
            }
            val spectrum = powerSpectrum(frame)
            val melEnergies = FloatArray(NUM_MEL_FILTERS) { m ->
                var e = 0f
                for (k in spectrum.indices) e += melFilters[m][k] * spectrum[k]
                ln(maxOf(e, 1e-10f))
            }
            frames[t] = dct(melEnergies)
        }
        return frames
    }

    private fun computeDelta(features: Array<FloatArray>): Array<FloatArray> {
        val delta = Array(features.size) { FloatArray(features[0].size) }
        val w = 2
        for (t in features.indices) {
            for (k in features[0].indices) {
                var num = 0f; var denom = 0f
                for (n in 1..w) {
                    val tPlus = (t + n).coerceIn(0, features.size - 1)
                    val tMinus = (t - n).coerceIn(0, features.size - 1)
                    num += n * (features[tPlus][k] - features[tMinus][k])
                    denom += n * n
                }
                delta[t][k] = if (denom > 0) num / (2 * denom) else 0f
            }
        }
        return delta
    }

    private fun computeRmsPerFrame(samples: FloatArray): FloatArray {
        return FloatArray(N_FRAMES) { t ->
            val start = t * HOP_LENGTH
            var sum = 0.0
            var count = 0
            for (i in 0 until N_FFT) {
                val idx = start + i
                if (idx < samples.size) {
                    sum += samples[idx] * samples[idx]; count++
                }
            }
            if (count > 0) sqrt(sum / count).toFloat() else 0f
        }
    }

    // ── FFT & Mel helpers ────────────────────────────────────────────

    private fun hannWindow(n: Int): Float =
        (0.5f * (1f - cos(2.0 * PI * n / (N_FFT - 1)))).toFloat()

    private fun powerSpectrum(frame: FloatArray): FloatArray {
        val n = frame.size
        val re = frame.copyOf()
        val im = FloatArray(n)
        fft(re, im)
        return FloatArray(n / 2 + 1) { k -> re[k] * re[k] + im[k] * im[k] }
    }

    /** Cooley-Tukey FFT in-place (power of 2 only). */
    private fun fft(re: FloatArray, im: FloatArray) {
        val n = re.size
        var j = 0
        for (i in 1 until n) {
            var bit = n shr 1
            while (j and bit != 0) { j = j xor bit; bit = bit shr 1 }
            j = j xor bit
            if (i < j) { re[i] = re[j].also { re[j] = re[i] }; im[i] = im[j].also { im[j] = im[i] } }
        }
        var len = 2
        while (len <= n) {
            val ang = -2.0 * PI / len
            val wRe = cos(ang).toFloat(); val wIm = sin(ang).toFloat()
            var i = 0
            while (i < n) {
                var curRe = 1f; var curIm = 0f
                for (k in 0 until len / 2) {
                    val uRe = re[i + k]; val uIm = im[i + k]
                    val vRe = re[i + k + len / 2] * curRe - im[i + k + len / 2] * curIm
                    val vIm = re[i + k + len / 2] * curIm + im[i + k + len / 2] * curRe
                    re[i + k] = uRe + vRe; im[i + k] = uIm + vIm
                    re[i + k + len / 2] = uRe - vRe; im[i + k + len / 2] = uIm - vIm
                    val newCurRe = curRe * wRe - curIm * wIm
                    curIm = curRe * wIm + curIm * wRe; curRe = newCurRe
                }
                i += len
            }
            len = len shl 1
        }
    }

    private fun buildMelFilterbank(): Array<FloatArray> {
        val fMin = 0.0; val fMax = SAMPLE_RATE / 2.0
        val melMin = hzToMel(fMin); val melMax = hzToMel(fMax)
        val melPoints = DoubleArray(NUM_MEL_FILTERS + 2) { i ->
            melToHz(melMin + i * (melMax - melMin) / (NUM_MEL_FILTERS + 1))
        }
        val freqBins = N_FFT / 2 + 1
        return Array(NUM_MEL_FILTERS) { m ->
            FloatArray(freqBins) { k ->
                val freq = k.toDouble() * SAMPLE_RATE / N_FFT
                when {
                    freq < melPoints[m] || freq > melPoints[m + 2] -> 0f
                    freq <= melPoints[m + 1] ->
                        ((freq - melPoints[m]) / (melPoints[m + 1] - melPoints[m])).toFloat()
                    else ->
                        ((melPoints[m + 2] - freq) / (melPoints[m + 2] - melPoints[m + 1])).toFloat()
                }
            }
        }
    }

    private fun hzToMel(hz: Double) = 2595.0 * log10(1.0 + hz / 700.0)
    private fun melToHz(mel: Double) = 700.0 * (10.0.pow(mel / 2595.0) - 1.0)

    private fun dct(input: FloatArray): FloatArray {
        val n = input.size
        return FloatArray(N_MFCC) { k ->
            var sum = 0.0
            for (i in 0 until n) sum += input[i] * cos(PI * k * (2 * i + 1) / (2 * n))
            (sum * sqrt(2.0 / n)).toFloat()
        }
    }
}