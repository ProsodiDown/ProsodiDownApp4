package com.example.prosodidownapp4.ml

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import kotlin.math.exp

/**
 * Wrapper TFLite untuk model prosodi_down_ser_fp16.tflite.
 * Input  : [1, 313, 81] float32
 * Output : [1, 4]  → softmax → index = EmotionLabel
 */
class EmotionClassifier(private val context: Context) {

    companion object {
        private const val MODEL_FILE = "prosodi_down_ser_fp16.tflite"
        private const val SCALER_FILE = "scaler.json"
    }

    private var interpreter: Interpreter? = null
    private lateinit var scalerMean: FloatArray
    private lateinit var scalerStd: FloatArray

    fun load() {
        interpreter = Interpreter(loadModelFile(), Interpreter.Options().apply { setNumThreads(2) })
        loadScaler()
    }

    fun close() {
        interpreter?.close()
        interpreter = null
    }

    /**
     * Klasifikasi fitur mentah [AudioProcessor.N_FRAMES][AudioProcessor.N_FEATURES].
     * Mengembalikan index label (0=netral,1=senang,2=sedih,3=marah) + confidence.
     */
    fun classify(features: Array<FloatArray>): Pair<Int, Float> {
        val normalized = normalize(features)
        val inputBuffer = toByteBuffer(normalized)

        val outputArray = Array(1) { FloatArray(4) }
        interpreter?.run(inputBuffer, outputArray)

        val probs = softmax(outputArray[0])
        val maxIdx = probs.indices.maxByOrNull { probs[it] } ?: 0
        return Pair(maxIdx, probs[maxIdx])
    }

    // ── Normalisasi Z-score ──────────────────────────────────────────

    private fun normalize(features: Array<FloatArray>): Array<FloatArray> {
        return Array(features.size) { t ->
            FloatArray(features[t].size) { f ->
                val std = scalerStd[f].takeIf { it > 1e-8f } ?: 1f
                (features[t][f] - scalerMean[f]) / std
            }
        }
    }

    // ── Buffer helpers ───────────────────────────────────────────────

    private fun toByteBuffer(features: Array<FloatArray>): ByteBuffer {
        // Shape [1, 313, 81] → 1 * 313 * 81 * 4 bytes
        val buffer = ByteBuffer
            .allocateDirect(1 * AudioProcessor.N_FRAMES * AudioProcessor.N_FEATURES * 4)
            .apply { order(ByteOrder.nativeOrder()) }
        for (frame in features) for (v in frame) buffer.putFloat(v)
        buffer.rewind()
        return buffer
    }

    private fun softmax(logits: FloatArray): FloatArray {
        val max = logits.max()
        val exp = logits.map { exp((it - max).toDouble()).toFloat() }
        val sum = exp.sum()
        return exp.map { it / sum }.toFloatArray()
    }

    // ── Load file ────────────────────────────────────────────────────

    private fun loadModelFile(): ByteBuffer {
        val afd = context.assets.openFd(MODEL_FILE)
        val inputStream = FileInputStream(afd.fileDescriptor)
        val channel = inputStream.channel
        return channel.map(FileChannel.MapMode.READ_ONLY, afd.startOffset, afd.declaredLength)
    }

    private fun loadScaler() {
        val json = context.assets.open(SCALER_FILE).bufferedReader().readText()
        // Parse manual tanpa library JSON agar tidak perlu dependensi tambahan
        scalerMean = parseJsonFloatArray(json, "mean")
        scalerStd = parseJsonFloatArray(json, "std")
    }

    private fun parseJsonFloatArray(json: String, key: String): FloatArray {
        val pattern = Regex(""""$key"\s*:\s*\[([^]]+)]""")
        val match = pattern.find(json) ?: error("Key '$key' tidak ditemukan di scaler.json")
        return match.groupValues[1]
            .split(",")
            .map { it.trim().toFloat() }
            .toFloatArray()
    }
}