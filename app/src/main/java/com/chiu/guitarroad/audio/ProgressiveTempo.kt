package com.chiu.guitarroad.audio

/**
 * 渐进加速模式：每 [barsPerStep] 小节提速 [deltaBpm] BPM，上限 [maxBpm]。
 * 由 MetronomeEngine 在每小节完成时调用 [onBarComplete]。
 */
class ProgressiveTempo(
    private val barsPerStep: Int = 4,
    private val deltaBpm: Int = 5,
    private val maxBpm: Int = 240
) {

    private var barCount = 0

    /**
     * 每完成一小节调用一次。达到 [barsPerStep] 小节时返回提速后的 BPM
     *（不超过 [maxBpm]），否则返回原值。
     */
    fun onBarComplete(currentBpm: Int): Int {
        barCount++
        return if (barCount >= barsPerStep) {
            barCount = 0
            minOf(currentBpm + deltaBpm, maxBpm)
        } else {
            currentBpm
        }
    }

    /** 清零小节计数（如开始新的渐进加速练习时调用）。 */
    fun reset() {
        barCount = 0
    }
}
