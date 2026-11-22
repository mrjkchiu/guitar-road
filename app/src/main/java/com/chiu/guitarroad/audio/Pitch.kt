package com.chiu.guitarroad.audio

import kotlin.math.log2
import kotlin.math.round
import kotlin.math.sqrt

/**
 * YIN 音高检测结果。
 *
 * @param frequency 频率 Hz，静音时为 0
 * @param noteName 音名，如 "A"、"C#"
 * @param octave 八度，如 4
 * @param cents 与最近标准音的音分偏差 [-50, 50]
 * @param isSilent 是否静音
 */
data class PitchResult(
    val frequency: Float,
    val noteName: String,
    val octave: Int,
    val cents: Float,
    val isSilent: Boolean
) {
    companion object {
        val SILENT = PitchResult(0f, "-", 0, 0f, true)

        private val NAMES = arrayOf(
            "C", "C#", "D", "D#", "E", "F",
            "F#", "G", "G#", "A", "A#", "B"
        )

        fun fromFrequency(freq: Float): PitchResult {
            if (freq <= 0f) return SILENT
            // 频率 → MIDI 音号（A4 = 440 Hz = 69）
            val midi = 69f + 12f * log2(freq / 440f)
            val rounded = round(midi).toInt()
            val cents = (midi - rounded) * 100f
            return PitchResult(
                frequency = freq,
                noteName = NAMES[((rounded % 12) + 12) % 12],
                octave = rounded / 12 - 1,
                cents = cents,
                isSilent = false
            )
        }
    }
}

/**
 * YIN 音高检测器：差分函数 → 累积均值归一化（CMND）→ 找谷值 → 抛物线插值细化周期。
 *
 * 覆盖吉他全音域：最低检测 70 Hz（覆盖低音 E2 82.41 Hz），最高检测 1500 Hz。
 */
class YinPitchDetector(private val sampleRate: Int) {

    private val threshold = 0.12f
    private val maxLag = sampleRate / 70 // 最低检测 70 Hz
    private val minLag = sampleRate / 1500 // 最高检测 1500 Hz

    fun detect(samples: FloatArray): PitchResult {
        if (samples.isEmpty()) return PitchResult.SILENT

        // 1. 去直流
        val mean = samples.average().toFloat()
        val x = FloatArray(samples.size) { samples[it] - mean }

        // 2. 能量门限（静音检测）
        val rms = sqrt(x.sumOf { (it * it).toDouble() } / x.size).toFloat()
        if (rms < 0.01f) return PitchResult.SILENT

        // 3. YIN 差分函数 d(τ)，只用前半段，保证 lag 窗口有足够重叠
        val n = x.size / 2
        val d = FloatArray(maxLag)
        for (tau in minLag until maxLag) {
            var sum = 0f
            for (j in 0 until n) {
                val diff = x[j] - x[j + tau]
                sum += diff * diff
            }
            d[tau] = sum
        }

        // 4. 累积均值归一化 CMND
        val cmnd = FloatArray(maxLag)
        var runningSum = 0f
        for (tau in minLag until maxLag) {
            runningSum += d[tau]
            cmnd[tau] = if (runningSum > 0f) d[tau] * tau / runningSum else 1f
        }

        // 5. 找第一个低于阈值的谷值。
        // 循环只到 maxLag - 2，保证 cmnd[tau + 1] 不越界。
        var tauBest = -1
        for (tau in minLag until maxLag - 1) {
            if (cmnd[tau] < threshold && cmnd[tau] < cmnd[tau - 1] && cmnd[tau] < cmnd[tau + 1]) {
                tauBest = tau
                break
            }
        }
        if (tauBest == -1) {
            // 没找到低于阈值的，取全局最小；CMND 仍大于 0.5 说明无可靠周期，判静音
            tauBest = (minLag until maxLag).minByOrNull { cmnd[it] } ?: return PitchResult.SILENT
            if (cmnd[tauBest] > 0.5f) return PitchResult.SILENT
        }

        // 6. 抛物线插值，细化周期（提高精度）；边界处无邻点则不插值
        val refinedTau: Float = if (tauBest > minLag && tauBest < maxLag - 1) {
            val y1 = cmnd[tauBest - 1]
            val y2 = cmnd[tauBest]
            val y3 = cmnd[tauBest + 1]
            val denominator = 2f * (2f * y2 - y1 - y3)
            val delta = if (denominator != 0f) (y3 - y1) / denominator else 0f
            tauBest + delta
        } else {
            tauBest.toFloat()
        }
        if (refinedTau <= 0f) return PitchResult.SILENT

        // 7. 频率 → 音名
        val freq = sampleRate / refinedTau
        return PitchResult.fromFrequency(freq)
    }
}

/**
 * 中值滤波平滑：窗口内按 cents 排序取中值，防止调音指针乱跳。
 * 静音帧直接返回，不进入窗口。
 */
class MedianSmoother(private val windowSize: Int = 5) {

    private val window = ArrayDeque<PitchResult>()

    fun push(value: PitchResult): PitchResult {
        if (value.isSilent) return value
        window.addLast(value)
        if (window.size > windowSize) window.removeFirst()
        val sorted = window.sortedBy { it.cents }
        return sorted[sorted.size / 2]
    }
}
