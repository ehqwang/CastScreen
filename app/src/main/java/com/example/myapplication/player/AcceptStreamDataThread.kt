package com.example.myapplication.player

import android.os.SystemClock
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket

/**
 * @author wanghq
 * @date 2023/1/25
 */
class AcceptStreamDataThread: Thread, AnalyticDataUtils.OnAnalyticDataListener {
    private var InputStream: InputStream? = null
    private var outputStream: OutputStream? = null
    private var socket: Socket? = null

    @Volatile
    private var startFlag = true
    private var listener: OnAcceptBuffListener? = null
    private var mTcpListener: OnTcpChangeListener? = null

    private var mDecoderUtils: DecodeUtils? = null
    private var mAnalyticDataUtils: AnalyticDataUtils? = null

    //当前投屏线程
    private val TAG = "AcceptStreamDataThread"

    constructor(
        socket: Socket?,
        listener: OnAcceptBuffListener?,
        tcpListener: OnTcpChangeListener?
    ) {
        this.socket = socket
        try {
            InputStream = socket?.getInputStream()
            outputStream = socket?.getOutputStream()
        } catch (e: Exception) {
            Log.e(TAG, "get InputStream and OutputStream exception$e")
        }
        this.listener = listener
        mTcpListener = tcpListener
        mDecoderUtils = DecodeUtils()
        mAnalyticDataUtils = AnalyticDataUtils()
        mAnalyticDataUtils?.setOnAnalyticDataListener(this)
        startFlag = true
        mDecoderUtils?.setOnVideoListener(object : DecodeUtils.OnVideoListener {
            override fun onSpsPps(sps: ByteArray?, pps: ByteArray?) {
                val spsPpsFrame = Frame()
                spsPpsFrame.type = Frame.SPSPPS
                spsPpsFrame.sps = sps
                spsPpsFrame.pps = pps
                Log.d("AcceptH264MsgThread", "sps pps ...")
                listener?.acceptBuff(spsPpsFrame)
            }

            override fun onVideo(video: ByteArray?, type: Int) {
                val frame = Frame()
                when (type) {
                    Frame.KEY_FRAME -> {
                        frame.type = Frame.KEY_FRAME
                        frame.bytes = video
                        Log.d("AcceptH264MsgThread", "key frame ...")
                        listener?.acceptBuff(frame)
                    }
                    Frame.NORMAL_FRAME -> {
                        frame.type = Frame.NORMAL_FRAME
                        frame.bytes = video
                        Log.d("AcceptH264MsgThread", "normal frame ...")
                        listener?.acceptBuff(frame)
                    }
                    Frame.AUDIO_FRAME -> {
                        frame.type = Frame.AUDIO_FRAME
                        frame.bytes = video
                        Log.d("AcceptH264MsgThread", "audio frame ...")
                        listener?.acceptBuff(frame)
                    }
                    else -> Log.e("AcceptH264MsgThread", "other video...")
                }
            }
        })
    }

    fun sendStartMessage() {
        //告诉客户端,可以开始投屏了
        try {
            outputStream?.write("OK".toByteArray())
            Log.i(TAG, "send start message")
        } catch (e: IOException) {
            mTcpListener?.disconnect(e)
        }
    }

    override fun run() {
        super.run()
        mTcpListener?.connect()
        sendStartMessage()
        mAnalyticDataUtils?.startNetSpeedCalculate()
        readMessage()
        mAnalyticDataUtils?.stop()
    }

    //读取数据
    private fun readMessage() {
        try {
            while (startFlag) {
                //开始接收客户端发过来的数据
                val header = mAnalyticDataUtils?.readByte(InputStream, 17)
                //数据如果为空，则休眠，防止cpu空转,  0.0 不可能会出现的,会一直阻塞在之前
                if (header == null || header.size == 0) {
                    SystemClock.sleep(1)
                    continue
                }
                // 根据协议分析数据头
                val receiveHeader = mAnalyticDataUtils?.analysisHeader(header)
                if ((receiveHeader?.getStringBodylength()?:0) == 0 && (receiveHeader?.getBuffSize()?:0) == 0) {
                    SystemClock.sleep(1)
                    continue
                }
                if (receiveHeader?.getEncodeVersion() != ScreenRecordApi.encodeVersion1) {
                    Log.e(TAG, "接收到的数据格式不对...")
                    continue
                }
                val receiveData = mAnalyticDataUtils?.analyticData(
                    InputStream,
                    receiveHeader
                )
                if (receiveData?.getBuff() == null) {
                    continue
                }
                //区分音视频
                mDecoderUtils?.isCategory(receiveData.getBuff())
            }
        } catch (e: Exception) {
            mTcpListener?.disconnect(e)
        } finally {
            startFlag = false
            try {
                socket?.close()
            } catch (e: IOException) {
                mTcpListener?.disconnect(e)
            }
        }
    }

    fun shutdown() {
        try {
            socket?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        startFlag = false
        mAnalyticDataUtils?.stop()
        interrupt()
    }

    override fun netSpeed(msg: String?) {
        mTcpListener?.netSpeed(msg)
    }

    interface OnTcpChangeListener {
        fun disconnect(e: Exception?)
        fun connect()
        fun netSpeed(netSpeed: String?)
    }
}