package com.chiu.guitarroad.audio

import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * 预生成节拍器 click 声的 PCM（16-bit 单声道）。
 * 正弦波 × exp(-8t) 快速衰减包络，避免"叮"的拖尾音。
 */
object ClickGenerator {

    /**
     * @param sampleRate 采样率，如 44100
     * @param freq click 频率 Hz（强拍 1320，弱拍 880）
     * @param durationMs click 时长毫秒（30）
     * @param volume 音量 0..1（强拍 0.9，弱拍 0.6）
     */
    fun generate(sampleRate: Int, freq: Int, durationMs: Int, volume: Float): ShortArray {
        val count = sampleRate * durationMs / 1000
        return ShortArray(count) { i ->
            val t = i.toFloat() / sampleRate
            val envelope = exp(-8f * t)
            (sin(2f * PI.toFloat() * freq * t) * envelope * volume * Short.MAX_VALUE).toInt().toShort()
        }
    }
}
