package com.example.myapplication.screen

import android.media.MediaCodec
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import com.example.myapplication.screen.listener.OnVideoEncodeListener
import java.util.concurrent.locks.ReentrantLock

class ScreenRecordEncoder {

    private var mMediaCodec: MediaCodec? = null
    private var mListener: OnVideoEncodeListener? = null
    private var mPause = false
    private var mHandlerThread: HandlerThread? = null
    private var mEncoderHandler: Handler? = null
    private var mConfiguration: VideoConfiguration? = null
    private var mBufferInfo: MediaCodec.BufferInfo? = null

    @Volatile
    private var isStarted = false
    private val encodeLock = ReentrantLock()

    constructor(configuration: VideoConfiguration?) {
        if (mMediaCodec == null) {
            mConfiguration = configuration
            mConfiguration?.let {
                Log.e("mConfiguration", "" + it)
                mMediaCodec = VideoMediaCodec.getVideoMediaCodec(it)
                Log.e("mConfiguration", "" + mMediaCodec)
            }
        }
    }

    fun setOnVideoEncodeListener(listener: OnVideoEncodeListener?) {
        mListener = listener
    }

    fun setPause(pause: Boolean) {
        mPause = pause
    }

    fun start() {
        mHandlerThread = HandlerThread("LFEncode")
        mHandlerThread?.start()
        mEncoderHandler = mHandlerThread?.looper?.let { Handler(it) }
        mBufferInfo = MediaCodec.BufferInfo()
        mMediaCodec?.start()
        mEncoderHandler?.post(swapDataRunnable)
        isStarted = true
    }

    fun getSurface(): Surface? {
        return mMediaCodec?.createInputSurface()
    }

    fun stop() {
        try {
            isStarted = false
            mEncoderHandler?.removeCallbacks(swapDataRunnable)
            mHandlerThread?.quit()
            encodeLock.lock()
            mMediaCodec?.signalEndOfInputStream()
            releaseEncoder()
            encodeLock.unlock()
        } catch (e: Exception) {
        }

    }

    private fun releaseEncoder() {
        mMediaCodec?.stop()
        mMediaCodec?.release()
        mMediaCodec = null
    }

    private val swapDataRunnable = Runnable {
        drainEncoder()
    }

    private fun drainEncoder() {
        while (isStarted) {
            encodeLock.lock()
            if (mMediaCodec != null) {
                val outBufferIndex = mBufferInfo?.let { mMediaCodec?.dequeueOutputBuffer(it, 12000) }
                if ((outBufferIndex?:0) >= 0) {
                    val bb = mMediaCodec?.getOutputBuffer(outBufferIndex?:0)
                    if (!mPause) {
                        mListener?.onVideoEncode(bb, mBufferInfo)
                    }
                    mMediaCodec?.releaseOutputBuffer(outBufferIndex?:0, true)
                }else {
                    try {
                        Thread.sleep(10)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                encodeLock.unlock()
            }else {
                encodeLock.unlock()
                break
            }
        }
    }
}