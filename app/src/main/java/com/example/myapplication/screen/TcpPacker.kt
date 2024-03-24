package com.example.myapplication.screen

import android.media.MediaCodec
import java.nio.ByteBuffer

class TcpPacker: Packer, AnnexbHelper.AnnexbNaluListener {

    companion object {
        const val FIRST_VIDEO = 2
        const val AUDIO = 4
        const val KEY_FRAME = 5
        const val INTER_FRAME = 6
    }

    private var packetListener: Packer.OnPacketListener? = null
    private var isHeaderWrite = false
    private var isKeyFrameWrite = false

    private var mAudioSampleRate = 0
    private  var mAudioSampleSize: Int = 0
    private var mIsStereo = false
    private var mSendAudio = false

    private var mAnnexbHelper: AnnexbHelper? = null


    constructor() {
        mAnnexbHelper = AnnexbHelper()
    }

    override fun setPacketListener(listener: Packer.OnPacketListener?) {
        packetListener = listener
    }


    override fun start() {
        mAnnexbHelper?.setAnnexbNaluListener(this)
    }

    override fun onVideoData(bb: ByteBuffer?, bi: MediaCodec.BufferInfo?) {
        mAnnexbHelper?.analyseVideoDataonlyH264(bb!!, bi!!)
    }

    override fun onAudioData(bb: ByteBuffer?, bi: MediaCodec.BufferInfo?) {

    }

    override fun stop() {
        isHeaderWrite = false
        isKeyFrameWrite = false
        mAnnexbHelper?.stop()
    }

    private var mSpsPps: ByteArray? = null
    private val header = byteArrayOf(0x00, 0x00, 0x00, 0x01) //H264的头文件


    override fun onSpsPps(sps: ByteArray, pps: ByteArray) {
        val byteBuffer = ByteBuffer.allocate(sps.size + 4)
        byteBuffer.put(header)
        byteBuffer.put(sps)
        mSpsPps = byteBuffer.array()
        packetListener?.onPacket(mSpsPps, FIRST_VIDEO)
        val byteBuffer1 = ByteBuffer.allocate(pps.size + 4)
        byteBuffer1.put(header)
        byteBuffer1.put(pps)
        packetListener?.onPacket(byteBuffer1.array(), FIRST_VIDEO)
        isHeaderWrite = true
    }

    override fun onVideo(video: ByteArray, isKeyFrame: Boolean) {
        if (packetListener == null || !isHeaderWrite) {
            return
        }
        var packetType: Int = INTER_FRAME
        if (isKeyFrame) {
            isKeyFrameWrite = true
            packetType = KEY_FRAME
        }
        //确保第一帧是关键帧，避免一开始出现灰色模糊界面
        if (!isKeyFrameWrite) {
            return
        }
        val bb: ByteBuffer
        if (isKeyFrame) {
            bb = ByteBuffer.allocate(video.size)
            video.let { bb.put(it) }
        } else {
            bb = ByteBuffer.allocate(video.size)
            video.let { bb.put(it) }
        }
        packetListener?.onPacket(bb.array(), packetType)
    }
}