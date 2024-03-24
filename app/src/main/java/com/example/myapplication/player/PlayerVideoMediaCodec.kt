package com.example.myapplication.player

import android.media.MediaCodec
import android.media.MediaFormat
import android.view.SurfaceHolder
import java.io.IOException
import java.nio.ByteBuffer

/**
 * @author wanghq
 * @date 2023/1/25
 */
class PlayerVideoMediaCodec(private val mHolder: SurfaceHolder?) {
    private var mCodec: MediaCodec? = null

    companion object {
        private val VIDEO_WIDTH = 360
        private val VIDEO_HEIGHT = 640
        private val FrameRate = 30
    }

    private val useSpsPPs = false

    //    private var mHolder: SurfaceHolder? = null
    private var header_sps = byteArrayOf(
        0,
        0,
        0,
        1,
        103,
        66,
        0,
        42,
        149.toByte(),
        168.toByte(),
        30,
        0,
        137.toByte(),
        249.toByte(),
        102,
        224.toByte(),
        32,
        32,
        32,
        64
    )
    var header_pps = byteArrayOf(
        0,
        0,
        0,
        1,
        104,
        206.toByte(),
        60,
        128.toByte(),
        0,
        0,
        0,
        1,
        6,
        229.toByte(),
        1,
        151.toByte(),
        128.toByte()
    )

    init {
        initialCodec()
    }

    private fun initialCodec() {
        try {
            //通过多媒体格式名创建一个可用的解码器
            mCodec = MediaCodec.createDecoderByType("video/avc")
        } catch (e: IOException) {
            e.printStackTrace()
        }
        //初始化编码器
        val mediaformat = MediaFormat.createVideoFormat("video/avc", VIDEO_WIDTH, VIDEO_HEIGHT)
        //获取h264中的pps及sps数据
        if (useSpsPPs) {
            mediaformat.setByteBuffer("csd-0", ByteBuffer.wrap(header_sps))
            mediaformat.setByteBuffer("csd-1", ByteBuffer.wrap(header_pps))
        }
        //设置帧率
        mediaformat.setInteger(MediaFormat.KEY_FRAME_RATE, FrameRate)
        //设置配置参数，参数介绍 ：
        // format	如果为解码器，此处表示输入数据的格式；如果为编码器，此处表示输出数据的格式。
        //surface	指定一个surface，可用作decode的输出渲染。
        //crypto	如果需要给媒体数据加密，此处指定一个crypto类.
        //   flags	如果正在配置的对象是用作编码器，此处加上CONFIGURE_FLAG_ENCODE 标签。
        mCodec?.configure(mediaformat, mHolder?.surface, null, 0)
    }

    fun getCodec(): MediaCodec? {
        return mCodec
    }

    fun start() {
        mCodec?.start()
    }

    fun release() {
        mCodec?.stop()
        mCodec?.release()
    }
}