package com.example.myapplication.player

import android.util.Log
import android.view.SurfaceHolder

/**
 * @author wanghq
 * @date 2023/1/25
 */
class VideoPlayController {
    private val TAG = "VideoPlayController"

    private var videoMediaCodec: PlayerVideoMediaCodec? = null
    private var mDecodeThread: DecodeThread? = null
    private var mPlayQueue: PlayerNormalPlayQueue? = null

    init {
        mPlayQueue = PlayerNormalPlayQueue()
    }

    fun surfaceCreate(holder: SurfaceHolder?) {
        //初始化解码器
        Log.i(TAG, "create surface, and initial play queue")
        videoMediaCodec = PlayerVideoMediaCodec(holder)
        //开启解码线程
        mDecodeThread = DecodeThread(videoMediaCodec?.getCodec(), mPlayQueue)
        videoMediaCodec?.start()
        mDecodeThread?.start()
    }


    fun surfaceDestroy() {
        mPlayQueue?.stop()
        mDecodeThread?.shutdown()
    }

    fun stop() {
        mPlayQueue?.stop()
        mPlayQueue = null
        mDecodeThread?.shutdown()
    }

    fun getAcceptBuffListener(): OnAcceptBuffListener? {
        return mAcceptBuffListener
    }

    private val mAcceptBuffListener: OnAcceptBuffListener = object : OnAcceptBuffListener {
        override fun acceptBuff(frame: Frame?) {
            frame?.let {
                mPlayQueue?.putByte(it)
            }
        }
    }
}