package com.example.myapplication.player

import android.media.MediaCodec
import android.os.SystemClock
import android.util.Log
import java.nio.ByteBuffer

/**
 * @author wanghq
 * @date 2023/1/25
 */
class DecodeThread(var mediaCodec: MediaCodec?, private var playQueue: PlayerNormalPlayQueue?) : Thread() {

    private val TAG = "DecodeThread"
    private var isPlaying = true
    private var videoPlay: VideoPlay? = null

    init {
        videoPlay = VideoPlay(mediaCodec)
    }

    override fun run() {
        while (isPlaying) {
            val frame: Frame? = playQueue?.takeByte()
            if (frame == null) {
                SystemClock.sleep(1)
                continue
            }
            when (frame.type) {
                Frame.KEY_FRAME, Frame.NORMAL_FRAME -> try {
                    videoPlay?.decodeH264(frame.bytes)
                    Log.i(TAG, "receive a frame count")
                } catch (e: Exception) {
                    Log.e(TAG, "frame Exception$e")
                }
                Frame.SPSPPS -> try {
                    val bb = ByteBuffer.allocate((frame.pps?.size ?: 0) + (frame.sps?.size ?: 0))
                    frame.sps?.let { bb.put(it) }
                    frame.pps?.let { bb.put(it) }
                    Log.e(TAG, "receive Sps pps")
                    videoPlay?.decodeH264(bb.array())
                } catch (e: Exception) {
                    Log.e(TAG, "sps pps Exception$e")
                }
            }
        }
    }

    fun shutdown() {
        Log.i(TAG, "DecodeThread shutdown")
        isPlaying = false
        interrupt()
        videoPlay?.release()
    }
}