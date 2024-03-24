package com.example.myapplication.screen

import java.io.BufferedOutputStream

class TcpWriteThread : Thread {
    private var bos: BufferedOutputStream? = null
    private var iSendQueue: ISendQueue? = null

    @Volatile
    private var startFlag = false
    private var mListener: OnTcpWriteListener? = null
    private val TAG = "TcpWriteThread"

    constructor(bos: BufferedOutputStream, sendQueue: ISendQueue, listener: OnTcpWriteListener) {
        this.bos = bos
        startFlag = true
        this.iSendQueue = sendQueue
        this.mListener = listener
    }

    override fun run() {
        while (startFlag) {
            val frame: Frame<Chunk> = iSendQueue?.takeFrame() ?: continue
            if (frame.data is Video) {
                val data1 = frame.data as Video
                data1.data?.let { sendData(it) }
            }
        }
    }

    fun sendData(buff: ByteArray) {
        try {
            val encodeV1 = EncodeV1(ScreenImageApi.encodeVersion1,
                ScreenImageApi.RECORD.MAIN_CMD,
                ScreenImageApi.RECORD.SEND_BUFF,
                buff)
            bos?.write(encodeV1.buildSendContent())
            bos?.flush()
        } catch (e: Exception) {
            startFlag = false
            mListener?.socketDisconnect()
        }
    }

    fun shutDown() {
        startFlag = false
        interrupt()
    }
}