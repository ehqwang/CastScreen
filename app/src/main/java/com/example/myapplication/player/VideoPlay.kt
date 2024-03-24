package com.example.myapplication.player

import android.media.MediaCodec
import android.os.Build
import android.os.SystemClock
import androidx.annotation.RequiresApi
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream

/**
 * @author wanghq
 * @date 2023/1/25
 */
class VideoPlay(private var mVideoMediaCodec: MediaCodec?) {
    private val TAG = "VideoPlay"

    fun decodeH264(buff: ByteArray?) {
        var mStopFlag = false
        //存放目标文件的数据
        val inputBuffers = mVideoMediaCodec?.inputBuffers
        //解码后的数据，包含每一个buffer的元数据信息，例如偏差，在相关解码器中有效的数据大小
        val info = MediaCodec.BufferInfo()
        val timeoutUs: Long = 10000 //微秒
        val marker0 = byteArrayOf(0, 0, 0, 1)
        val dummyFrame = byteArrayOf(0x00, 0x00, 0x01, 0x20)
        var streamBuffer: ByteArray? = null
        streamBuffer = buff
        var bytes_cnt = 0
        while (!mStopFlag) {
            bytes_cnt = streamBuffer?.size?:0
            if (bytes_cnt == 0) {
                streamBuffer = dummyFrame
            }
            var startIndex = 0
            val remaining = bytes_cnt
            while (true) {
                if (remaining == 0 || startIndex >= remaining) {
                    break
                }
                var nextFrameStart = KMPMatch(marker0, streamBuffer, startIndex + 2, remaining)
                if (nextFrameStart == -1) {
                    nextFrameStart = remaining
                } else {
                }
                val inIndex = mVideoMediaCodec!!.dequeueInputBuffer(timeoutUs)
                startIndex = if (inIndex >= 0) {
                    val byteBuffer = inputBuffers?.get(inIndex)
                    byteBuffer?.clear()
                    byteBuffer?.put(streamBuffer, startIndex, nextFrameStart - startIndex)
                    //在给指定Index的inputbuffer[]填充数据后，调用这个函数把数据传给解码器
                    mVideoMediaCodec?.queueInputBuffer(
                        inIndex,
                        0,
                        nextFrameStart - startIndex,
                        0,
                        0
                    )
                    nextFrameStart
                } else {
                    continue
                }
                val outIndex = mVideoMediaCodec?.dequeueOutputBuffer(info, timeoutUs)

                if ((outIndex?:0) >= 0) {

                    SystemClock.sleep(1)
                    val doRender = info.size != 0
                    //对outputbuffer的处理完后，调用这个函数把buffer重新返回给codec类。
                    outIndex?.let { mVideoMediaCodec?.releaseOutputBuffer(it, doRender) }
                } else {
                }
            }
            mStopFlag = true
        }
    }

    @Throws(IOException::class)
    private fun getBytes(input : InputStream): ByteArray? {
        var len: Int
        var size = 1024
        var buf: ByteArray
        if (input is ByteArrayInputStream) {
            size = input.available()
            buf = ByteArray(size)
            len = input.read(buf, 0, size)
        } else {
            val bos = ByteArrayOutputStream()
            buf = ByteArray(size)
            while (input.read(buf, 0, size).also { len = it } != -1) bos.write(buf, 0, len)
            buf = bos.toByteArray()
        }
        return buf
    }

    private fun KMPMatch(pattern: ByteArray, bytes: ByteArray?, start: Int, remain: Int): Int {
        try {
            Thread.sleep(1)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        val lsp = computeLspTable(pattern)
        var j = 0 // Number of chars matched in pattern
        for (i in start until remain) {
            while (j > 0 && (bytes?.get(i) ?: 0) != pattern[j]) {
                // Fall back in the pattern
                j = lsp[j - 1] // Strictly decreasing
            }
            if ((bytes?.get(i) ?: 0) == pattern[j]) {
                // Next char matched, increment position
                j++
                if (j == pattern.size) return i - (j - 1)
            }
        }
        return -1 // Not found
    }

    private fun computeLspTable(pattern: ByteArray): IntArray {
        val lsp = IntArray(pattern.size)
        lsp[0] = 0 // Base case
        for (i in 1 until pattern.size) {
            // Start by assuming we're extending the previous LSP
            var j = lsp[i - 1]
            while (j > 0 && pattern[i] != pattern[j]) j = lsp[j - 1]
            if (pattern[i] == pattern[j]) j++
            lsp[i] = j
        }
        return lsp
    }

    fun release() {
        mVideoMediaCodec?.stop()
        mVideoMediaCodec?.release()
        mVideoMediaCodec = null
    }

    interface OnFrameChangeListener {
        fun onFrameSize(width: Int, height: Int)
    }
}