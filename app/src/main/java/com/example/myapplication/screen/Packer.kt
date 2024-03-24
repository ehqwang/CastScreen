package com.example.myapplication.screen

import android.media.MediaCodec
import java.nio.ByteBuffer

interface Packer {

    interface OnPacketListener {
        fun onPacket(data: ByteArray?, packetType: Int)

        fun onSpsPps(mSpsPps: ByteArray?)
    }

    //设置打包监听器
    fun setPacketListener(listener: OnPacketListener?)

    //处理视频硬编编码器输出的数据
    fun onVideoData(bb: ByteBuffer?, bi: MediaCodec.BufferInfo?)

    //处理音频硬编编码器输出的数据
    fun onAudioData(bb: ByteBuffer?, bi: MediaCodec.BufferInfo?)

    //开始打包，一般进行打包的预处理
    fun start()

    //结束打包，一般进行打包器的状态恢复
    fun stop()
}