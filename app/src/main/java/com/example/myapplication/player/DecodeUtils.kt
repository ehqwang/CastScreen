package com.example.myapplication.player

import android.util.Log

/**
 * @author wanghq
 * @date 2023/1/25
 */
class DecodeUtils {
    private val TAG = "H264AacDecoder"

    // Coded slice of a non-IDR picture slice_layer_without_partitioning_rbsp( )
    val NonIDR = 1

    // Coded slice of an IDR picture slice_layer_without_partitioning_rbsp( )
    val IDR = 5

    // Supplemental enhancement information (SEI) sei_rbsp( )
    val SEI = 6

    // Sequence parameter set seq_parameter_set_rbsp( )
    val SPS = 7

    // Picture parameter set pic_parameter_set_rbsp( )
    val PPS = 8

    // Access unit delimiter access_unit_delimiter_rbsp( )
    val AccessUnitDelimiter = 9

    //
    val AUDIO = -2

    private var mPps: ByteArray? = null
    private var mSps: ByteArray? = null


    fun isCategory(frame: ByteArray?) {
        var isKeyFrame = false
        if (frame == null) {
            Log.e(TAG, "annexb not match.")
            return
        }
        // ignore the nalu type aud(9)
        if (isAccessUnitDelimiter(frame)) {
            return
        }
        // for pps
        if (isPps(frame)) {
            mPps = frame
            if (mPps != null && mSps != null) {
                mListener?.onSpsPps(mSps, mPps)
            }
            return
        }
        // for sps
        if (isSps(frame)) {
            mSps = frame
            if (mPps != null && mSps != null) {
                mListener?.onSpsPps(mSps, mPps)
            }
            return
        }
        if (isAudio(frame)) {
            val temp = ByteArray(frame.size - 4)
            System.arraycopy(frame, 4, temp, 0, frame.size - 4)
            mListener?.onVideo(temp, Frame.AUDIO_FRAME)
            return
        }
        // for IDR frame
        isKeyFrame = isKeyFrame(frame)
        mListener?.onVideo(frame, if (isKeyFrame) Frame.KEY_FRAME else Frame.NORMAL_FRAME)
    }

    private fun isAudio(frame: ByteArray): Boolean {
        return if (frame.size < 5) {
            false
        } else frame[4] == 0xFF.toByte() && frame[5] == 0xF9.toByte()
    }

    private fun isSps(frame: ByteArray): Boolean {
        if (frame.size < 5) {
            return false
        }
        // 5bits, 7.3.1 NAL unit syntax,
        // H.264-AVC-ISO_IEC_14496-10.pdf, page 44.
        //  7: SPS, 8: PPS, 5: I Frame, 1: P Frame
        val nal_unit_type = frame[4].toInt() and 0x1f
        return nal_unit_type == SPS
    }

    private fun isPps(frame: ByteArray): Boolean {
        if (frame.size < 5) {
            return false
        }
        // 5bits, 7.3.1 NAL unit syntax,
        // H.264-AVC-ISO_IEC_14496-10.pdf, page 44.
        //  7: SPS, 8: PPS, 5: I Frame, 1: P Frame
        val nal_unit_type = frame[4].toInt() and 0x1f
        return nal_unit_type == PPS
    }

    private fun isKeyFrame(frame: ByteArray): Boolean {
        if (frame.size < 5) {
            return false
        }
        // 5bits, 7.3.1 NAL unit syntax,
        // H.264-AVC-ISO_IEC_14496-10.pdf, page 44.
        //  7: SPS, 8: PPS, 5: I Frame, 1: P Frame
        val nal_unit_type = frame[4].toInt() and 0x1f
        return nal_unit_type == IDR
    }

    private fun isAccessUnitDelimiter(frame: ByteArray): Boolean {
        if (frame.size < 5) {
            return false
        }
        // 5bits, 7.3.1 NAL unit syntax,
        // H.264-AVC-ISO_IEC_14496-10.pdf, page 44.
        //  7: SPS, 8: PPS, 5: I Frame, 1: P Frame
        val nal_unit_type = frame[4].toInt() and 0x1f
        return nal_unit_type == AccessUnitDelimiter
    }

    var mListener: OnVideoListener? = null

    fun setOnVideoListener(listener: OnVideoListener?) {
        mListener = listener
    }

    interface OnVideoListener {
        fun onSpsPps(sps: ByteArray?, pps: ByteArray?)
        fun onVideo(video: ByteArray?, type: Int)
    }
}