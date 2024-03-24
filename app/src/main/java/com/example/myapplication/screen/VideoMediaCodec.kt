package com.example.myapplication.screen

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.os.Build
import kotlin.math.ceil

class VideoMediaCodec {

    companion object {
        fun getVideoMediaCodec(videoConfiguration: VideoConfiguration):MediaCodec? {

            val videoWidth = getVideoSize(videoConfiguration.width)
            val videoHeight = getVideoSize(videoConfiguration.height)
            if (Build.MANUFACTURER.equals("XIAOMI", ignoreCase = true)) {
                videoConfiguration.maxBps = 500
                videoConfiguration.fps = 10
                videoConfiguration.ifi = 3
            }
            val format =
                MediaFormat.createVideoFormat(videoConfiguration.mime, videoWidth, videoHeight)
            format.setInteger(
                MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
            )
            format.setInteger(MediaFormat.KEY_BIT_RATE, videoConfiguration.maxBps * 1024)
            var fps = videoConfiguration.fps

            format.setInteger(MediaFormat.KEY_FRAME_RATE, fps)
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, videoConfiguration.ifi)
            // -----------------ADD BY XU.WANG 当画面静止时,重复最后一帧--------------------------------------------------------
            format.setLong(MediaFormat.KEY_REPEAT_PREVIOUS_FRAME_AFTER, (1000000 / 45).toLong())
            //------------------MODIFY BY XU.WANG 为解决MIUI9.5花屏而增加...-------------------------------
            if (Build.MANUFACTURER.equals("XIAOMI", ignoreCase = true)) {
                format.setInteger(
                    MediaFormat.KEY_BITRATE_MODE,
                    MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CQ
                )
            } else {
                format.setInteger(
                    MediaFormat.KEY_BITRATE_MODE,
                    MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR
                )
            }
            format.setInteger(
                MediaFormat.KEY_COMPLEXITY,
                MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR
            )
            var mediaCodec: MediaCodec? = null

            try {
                mediaCodec = MediaCodec.createEncoderByType(videoConfiguration.mime)
                mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            } catch (e: Exception) {
                e.printStackTrace()
                if (mediaCodec != null) {
                    mediaCodec.stop()
                    mediaCodec.release()
                    mediaCodec = null
                }
            }

            return mediaCodec
        }

        fun getVideoSize(size: Int): Int {
            val multiple = ceil(size / 16.0)
            return (multiple * 16).toInt()
        }
    }
}