package com.example.myapplication.screen

import android.media.MediaCodec
import java.nio.ByteBuffer

class AnnexbHelper {

    companion object {
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
    }

    private var mListener: AnnexbNaluListener? = null

    private var mPps: ByteArray? = null
    private var mSps: ByteArray? = null
    private var mUploadPpsSps = true


    /**
     * the search result for annexb.
     */
    internal class AnnexbSearch {
        var startCode = 0
        var match = false
    }

    interface AnnexbNaluListener {
        fun onSpsPps(sps: ByteArray, pps: ByteArray)
        fun onVideo(data: ByteArray, isKeyFrame: Boolean)
    }

    fun setAnnexbNaluListener(listener: AnnexbNaluListener?) {
        mListener = listener
    }


    fun stop() {
        mListener = null
        mPps = null
        mSps = null
        mUploadPpsSps = true
    }

    /**
     * 处理h264数据,确保视频最开始是sps,pps,
     *
     */
    private val header = byteArrayOf(0x00, 0x00, 0x00, 0x01)
    fun analyseVideoDataonlyH264(bb: ByteBuffer, bi: MediaCodec.BufferInfo) {
        bb.position(bi.offset)
        bb.limit(bi.offset + bi.size)
        val frames = ArrayList<ByteArray>()
        var isKeyFrame = false
        while (bb.position() < bi.offset + bi.size) {
            val frame = annexbDemux(bb, bi)
            if (frame == null) {
                break
            }
            // ignore the nalu type aud(9)
            if (isAccessUnitDelimiter(frame)) {
                continue
            }
            // for pps
            if (isPps(frame)) {
                mPps = frame
                continue
            }
            // for sps
            if (isSps(frame)) {
                mSps = frame
                continue
            }
            // for IDR frame
            isKeyFrame = isKeyFrame(frame)
            frames.add(header)
            frames.add(frame)
        }
        if (mPps != null && mSps != null && mListener != null && mUploadPpsSps) {
            mListener?.onSpsPps(mSps!!, mPps!!)
            mUploadPpsSps = false
        }
        if (frames.size == 0 || mListener == null) {
            return
        }
        var size = 0
        for (i in frames.indices) {
            val frame = frames[i]
            size += frame.size
        }
        val data = ByteArray(size)
        var currentSize = 0
        for (i in frames.indices) {
            val frame = frames[i]
            System.arraycopy(frame, 0, data, currentSize, frame.size)
            currentSize += frame.size
        }
        mListener?.onVideo(data, isKeyFrame)
    }

    /**
     * 从硬编出来的数据取出一帧nal
     *
     * @param bb
     * @param bi
     * @return
     */
    private fun annexbDemux(bb: ByteBuffer, bi: MediaCodec.BufferInfo): ByteArray? {
        val annexbSearch = AnnexbSearch()
        avcStartWithAnnexb(annexbSearch, bb, bi)
        if (!annexbSearch.match || annexbSearch.startCode < 3) {
            return null
        }
        for (i in 0 until annexbSearch.startCode) {
            bb.get()
        }
        val frameBuffer = bb.slice()
        val pos = bb.position()
        while (bb.position() < bi.offset + bi.size) {
            avcStartWithAnnexb(annexbSearch, bb, bi)
            if (annexbSearch.match) {
                break
            }
            bb.get()
        }
        val size = bb.position() - pos
        val frameBytes = ByteArray(size)
        frameBuffer[frameBytes]
        return frameBytes
    }


    /**
     * 从硬编出来的byteBuffer中查找nal
     *
     * @param as
     * @param bb
     * @param bi
     */
    private fun avcStartWithAnnexb(`as`: AnnexbSearch, bb: ByteBuffer, bi: MediaCodec.BufferInfo) {
        `as`.match = false
        `as`.startCode = 0
        var pos = bb.position()
        while (pos < bi.offset + bi.size - 3) {
            // not match.
            if (bb[pos].toInt() != 0x00 || bb[pos + 1].toInt() != 0x00) {
                break
            }

            // match N[00] 00 00 01, where N>=0
            if (bb[pos + 2].toInt() == 0x01) {
                `as`.match = true
                `as`.startCode = pos + 3 - bb.position()
                break
            }
            pos++
        }
    }

    private fun buildNaluHeader(length: Int): ByteArray {
        val buffer = ByteBuffer.allocate(4)
        buffer.putInt(length)
        return buffer.array()
    }


    private fun isSps(frame: ByteArray): Boolean {
        if (frame.size < 1) {
            return false
        }
        // 5bits, 7.3.1 NAL unit syntax,
        // H.264-AVC-ISO_IEC_14496-10.pdf, page 44.
        //  7: SPS, 8: PPS, 5: I Frame, 1: P Frame
        val nal_unit_type: Int = frame[0].toInt() and 0x1f
        return nal_unit_type == SPS
    }

    private fun isPps(frame: ByteArray): Boolean {
        if (frame.size < 1) {
            return false
        }
        // 5bits, 7.3.1 NAL unit syntax,
        // H.264-AVC-ISO_IEC_14496-10.pdf, page 44.
        //  7: SPS, 8: PPS, 5: I Frame, 1: P Frame
        val nal_unit_type: Int = frame[0].toInt() and 0x1f
        return nal_unit_type == PPS
    }

    private fun isKeyFrame(frame: ByteArray): Boolean {
        if (frame.size < 1) {
            return false
        }
        // 5bits, 7.3.1 NAL unit syntax,
        // H.264-AVC-ISO_IEC_14496-10.pdf, page 44.
        //  7: SPS, 8: PPS, 5: I Frame, 1: P Frame
        val nal_unit_type: Int = frame[0].toInt() and 0x1f
        return nal_unit_type == IDR
    }

    private fun isAccessUnitDelimiter(frame: ByteArray): Boolean {
        if (frame.size < 1) {
            return false
        }
        // 5bits, 7.3.1 NAL unit syntax,
        // H.264-AVC-ISO_IEC_14496-10.pdf, page 44.
        //  7: SPS, 8: PPS, 5: I Frame, 1: P Frame
        val nal_unit_type: Int = frame[0].toInt() and 0x1f
        return nal_unit_type == AccessUnitDelimiter
    }
}