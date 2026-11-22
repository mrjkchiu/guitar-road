package com.chiu.guitarroad.audio

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder

/**
 * 麦克风采集 + YIN 音高识别 + 中值滤波，回调稳定的 [PitchResult]。
 *
 * 音频仅在本机实时处理，不上传、不存储、不录音。
 * 调用方负责在离开页面时调用 [stop]，并在获得 RECORD_AUDIO 权限后调用 [start]。
 */
class AudioPitchRecorder(
    private val sampleRate: Int = 44100,
    private val bufferSize: Int = 4096
) {

    private var audioRecord: AudioRecord? = null
    private var recordThread: Thread? = null
    private val buffer = FloatArray(bufferSize)

    @Volatile
    var isRunning: Boolean = false

    var onPitch: (PitchResult) -> Unit = {}

    fun start() {
        // 防止重复启动：先停掉旧的采集
        stop()

        val record: AudioRecord
        try {
            record = AudioRecord.Builder()
                .setAudioSource(MediaRecorder.AudioSource.MIC)
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize * 2)
                .build()
        } catch (e: Exception) {
            isRunning = false
            return
        }

        if (record.state != AudioRecord.STATE_INITIALIZED) {
            try {
                record.release()
            } catch (ignored: Exception) {
            }
            isRunning = false
            return
        }

        try {
            record.startRecording()
        } catch (e: Exception) {
            try {
                record.release()
            } catch (ignored: Exception) {
            }
            isRunning = false
            return
        }

        audioRecord = record
        isRunning = true

        val thread = Thread {
            val shortBuffer = ShortArray(bufferSize)
            val detector = YinPitchDetector(sampleRate)
            val smoother = MedianSmoother(windowSize = 5)
            while (isRunning) {
                val current = audioRecord ?: break
                val read: Int
                try {
                    read = current.read(shortBuffer, 0, bufferSize)
                } catch (e: Exception) {
                    break
                }
                if (read > 0) {
                    // Short → Float，归一化到 [-1, 1]
                    for (i in 0 until read) {
                        buffer[i] = shortBuffer[i] / 32768f
                    }
                    val raw = detector.detect(buffer.copyOf(read))
                    val stable = smoother.push(raw)
                    try {
                        onPitch(stable)
                    } catch (ignored: Exception) {
                        // 回调异常不中断采集线程
                    }
                }
            }
        }
        recordThread = thread
        thread.start()
    }

    fun stop() {
        isRunning = false
        recordThread = null
        val record = audioRecord
        audioRecord = null
        if (record != null) {
            try {
                record.stop()
            } catch (ignored: Exception) {
            }
            try {
                record.release()
            } catch (ignored: Exception) {
            }
        }
    }
}
