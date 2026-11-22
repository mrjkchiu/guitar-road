package com.chiu.guitarroad.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Handler
import android.os.Looper

/**
 * 节拍器精准调度引擎。
 *
 * 计时方式：AudioTrack 预写 PCM，提前 200 ms 排程，以 System.nanoTime() 为时间基准；
 * 不用 Thread.sleep / Handler.postDelayed 做节拍计时（调度循环内的 50 ms 休眠除外）。
 * [bpm] 在每轮调度循环重读，变化即时生效。
 */
class MetronomeEngine(private val sampleRate: Int = 44100) {

    private var audioTrack: AudioTrack? = null
    private var scheduleThread: Thread? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private val clickStrong = ClickGenerator.generate(sampleRate, 1320, 30, 0.9f) // 强拍
    private val clickWeak = ClickGenerator.generate(sampleRate, 880, 30, 0.6f) // 弱拍

    private val scheduleAheadMs = 200L

    @Volatile
    private var running = false

    var bpm: Int = 60
    var beatsPerBar: Int = 4

    /** 每拍回调，参数为该拍在小节内的下标 0..beatsPerBar-1（主线程）。 */
    var onBeat: (beatIndex: Int) -> Unit = {}

    /** 每完成一小节回调一次（主线程）。 */
    var onBarComplete: (() -> Unit)? = null

    val isRunning: Boolean
        get() = running

    fun start() {
        // 防止重复启动：先停掉旧的引擎
        stop()

        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        if (bufferSize <= 0) return

        val track: AudioTrack
        try {
            track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize * 2)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
            track.play()
        } catch (e: Exception) {
            return
        }
        audioTrack = track

        running = true
        var nextClickTime = System.nanoTime()
        var beatIndex = 0

        val thread = Thread {
            while (running) {
                val now = System.nanoTime()
                val aheadNanos = scheduleAheadMs * 1_000_000L
                // 排程未来 200 ms 内的所有 click
                while (running && nextClickTime < now + aheadNanos) {
                    // BPM 每轮重读，变化即时生效
                    val currentBpm = bpm.coerceAtLeast(1)
                    val beats = beatsPerBar.coerceAtLeast(1)
                    val intervalNanos = 60_000_000_000L / currentBpm
                    val delayNanos = maxOf(0L, nextClickTime - now)
                    val isStrong = beatIndex % beats == 0
                    val click = if (isStrong) clickStrong else clickWeak

                    // 按采样点精确写入：把 delay 转成静音采样填充
                    val silenceSamples = (delayNanos * sampleRate / 1_000_000_000L).toInt()
                    val pcm = ShortArray(silenceSamples + click.size)
                    System.arraycopy(click, 0, pcm, silenceSamples, click.size)
                    try {
                        audioTrack?.write(pcm, 0, pcm.size)
                    } catch (e: Exception) {
                        break
                    }

                    val beat = beatIndex
                    val barCompleted = beatIndex % beats == 0 && beatIndex > 0
                    // 切回主线程回调 UI
                    mainHandler.post {
                        onBeat(beat % beats)
                        if (barCompleted) {
                            onBarComplete?.invoke()
                        }
                    }

                    nextClickTime += intervalNanos
                    beatIndex++
                }
                // 仅用于调度循环休眠，不参与计时精度
                try {
                    Thread.sleep(50)
                } catch (e: InterruptedException) {
                    break
                }
            }
        }
        scheduleThread = thread
        thread.start()
    }

    fun stop() {
        running = false
        scheduleThread = null
        val track = audioTrack
        audioTrack = null
        if (track != null) {
            try {
                track.stop()
            } catch (ignored: Exception) {
            }
            try {
                track.release()
            } catch (ignored: Exception) {
            }
        }
    }
}
