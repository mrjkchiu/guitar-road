package com.chiu.guitarroad.audio

import kotlin.math.log2
import kotlin.math.pow

/**
 * 调弦模式。降半音 / 降全音的 semitoneOffset 用于频率换算；
 * Open G 为开放调弦，频率直接给出，semitoneOffset 为 0。
 */
enum class TuningMode(val displayName: String, val semitoneOffset: Int) {
    STANDARD("标准调弦", 0),
    HALF_STEP_DOWN("降半音", -1),
    WHOLE_STEP_DOWN("降全音", -2),
    OPEN_G("Open G", 0)
}

data class GuitarString(
    val stringNumber: Int, // 6 = 最粗的低音弦，1 = 最细的高音弦
    val noteName: String,
    val octave: Int,
    val frequency: Float // Hz
)

object Tuning {

    // 标准调弦：6→1 弦，音名 + 八度 + 频率（Hz）
    private val STANDARD_STRINGS = listOf(
        GuitarString(6, "E", 2, 82.41f),
        GuitarString(5, "A", 2, 110.00f),
        GuitarString(4, "D", 3, 146.83f),
        GuitarString(3, "G", 3, 196.00f),
        GuitarString(2, "B", 3, 246.94f),
        GuitarString(1, "E", 4, 329.63f)
    )

    // Open G 调弦：6→1 弦
    private val OPEN_G_STRINGS = listOf(
        GuitarString(6, "D", 2, 73.42f),
        GuitarString(5, "G", 2, 98.00f),
        GuitarString(4, "D", 3, 146.83f),
        GuitarString(3, "G", 3, 196.00f),
        GuitarString(2, "B", 3, 246.94f),
        GuitarString(1, "D", 4, 293.66f)
    )

    // 降调后的音名用降号表示（Eb / Ab / Db / Gb / Bb），与升号写法区分
    private val FLAT_NAMES = arrayOf(
        "C", "Db", "D", "Eb", "E", "F",
        "Gb", "G", "Ab", "A", "Bb", "B"
    )

    // 标准调弦各弦对应的 MIDI 音号（E2=40, A2=45, D3=50, G3=55, B3=59, E4=64）
    private val STANDARD_MIDI = listOf(40, 45, 50, 55, 59, 64)

    /**
     * 返回指定调弦模式下 6→1 弦顺序的弦列表。
     */
    fun stringsFor(mode: TuningMode): List<GuitarString> {
        return when (mode) {
            TuningMode.STANDARD -> STANDARD_STRINGS
            TuningMode.OPEN_G -> OPEN_G_STRINGS
            TuningMode.HALF_STEP_DOWN, TuningMode.WHOLE_STEP_DOWN -> {
                val offset = mode.semitoneOffset
                val factor = 2.0.pow(offset / 12.0)
                STANDARD_STRINGS.mapIndexed { index, string ->
                    val midi = STANDARD_MIDI[index] + offset
                    GuitarString(
                        stringNumber = string.stringNumber,
                        noteName = FLAT_NAMES[((midi % 12) + 12) % 12],
                        octave = midi / 12 - 1,
                        frequency = (string.frequency * factor).toFloat()
                    )
                }
            }
        }
    }

    /**
     * 在 strings 里找频率最接近 pitch.frequency 的弦。
     * 用 log2 比值算半音距离；最近距离超过 6 个半音返回 null（不在吉他音域附近）。
     */
    fun targetFor(pitch: PitchResult, strings: List<GuitarString>): GuitarString? {
        if (pitch.isSilent || pitch.frequency <= 0f || strings.isEmpty()) return null
        var best: GuitarString? = null
        var bestDistance = Float.MAX_VALUE
        for (string in strings) {
            val distance = kotlin.math.abs(12f * log2(pitch.frequency / string.frequency))
            if (distance < bestDistance) {
                bestDistance = distance
                best = string
            }
        }
        if (bestDistance > 6f) return null
        return best
    }

    /**
     * pitch 相对目标弦的音分偏差，限制在 [-50, 50] 内。
     */
    fun centsToTarget(pitch: PitchResult, target: GuitarString): Float {
        if (pitch.isSilent || pitch.frequency <= 0f || target.frequency <= 0f) return 0f
        val cents = 1200f * log2(pitch.frequency / target.frequency)
        return cents.coerceIn(-50f, 50f)
    }
}
