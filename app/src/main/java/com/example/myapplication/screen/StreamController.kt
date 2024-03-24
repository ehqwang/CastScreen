package com.example.myapplication.screen

import android.media.MediaCodec
import android.media.projection.MediaProjection
import android.util.Log
import com.example.myapplication.screen.listener.OnVideoEncodeListener
import java.nio.ByteBuffer

class StreamController(val mVideoController: IVideoController) : OnVideoEncodeListener,
    Packer.OnPacketListener {
    private var mPacker: Packer? = null
    private var mSender: Sender? = null

    fun setVideoConfiguration(videoConfiguration: VideoConfiguration?) {
        videoConfiguration?.let { mVideoController.setVideoConfiguration(it) }
    }

    fun setPacker(packer: Packer) {
        mPacker = packer
        mPacker?.setPacketListener(this)
    }

    fun setSender(sender: Sender?) {
        mSender = sender
    }

    @Synchronized
    fun start(mediaProjection: MediaProjection?) {
        SopCastUtils.processNotUI(object : SopCastUtils.INotProcessor {
            override fun process() {
                if (mPacker == null) {
                    return
                }
                if (mSender == null) {
                    return
                }
                mPacker?.start()
                mSender?.start()
                mVideoController.setVideoEncoderListener(this@StreamController)
                mVideoController.start(mediaProjection)
            }

        })
    }

    @Synchronized
    fun stop() {
        SopCastUtils.processNotUI(object : SopCastUtils.INotProcessor {
            override fun process() {
                mVideoController.setVideoEncoderListener(null)
                mVideoController.stop()

                mSender?.stop()
                mPacker?.stop()

            }
        })
    }

    override fun onPacket(data: ByteArray?, packetType: Int) {
        Log.e("onPacket", "data=" + data + "pa=  " + packetType)


        mSender?.onData(data, packetType)
    }

    override fun onSpsPps(mSpsPps: ByteArray?) {
        Log.e("onSpsPps", "data=" + mSpsPps)
        if (mSpsPps != null && mSender is TcpSender) {
            val ss = mSender as TcpSender
            ss.setSpsPps(mSpsPps)
        }
    }

    override fun onVideoEncode(bb: ByteBuffer?, bi: MediaCodec.BufferInfo?) {
        Log.e("onVideoEncode", "data=" + bb)
        mPacker?.onVideoData(bb, bi)
    }
}