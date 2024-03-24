package com.example.myapplication.screen

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketAddress

class TcpConnection: OnTcpReadListener, OnTcpWriteListener {
    private var listener: TcpConnectListener? = null
    private val TAG = "TcpConnection"
    private var socket: Socket? = null
    private var state: State = State.INIT
    private var mSendQueue: ISendQueue? = null
    private var mWrite: TcpWriteThread? = null
    private var mRead: TcpReadThread? = null
    private var input: BufferedInputStream? = null
    private var out: BufferedOutputStream? = null
    private var width = 0
    private  var height: Int = 0
    private var maxBps = 0
    private var fps = 0
    private var mSpsPps: ByteArray? = null

    enum class State {
        INIT, CONNECTING, LIVING
    }

    fun setConnectListener(listener: TcpConnectListener?) {
        this.listener = listener
    }

    fun setSendQueue(sendQueue: ISendQueue) {
        mSendQueue = sendQueue
    }

    fun connect(ip: String?, port: Int) {
        socket = Socket()
        val socketAddress: SocketAddress = InetSocketAddress(ip, port)
        try {
            socket?.connect(socketAddress, 20000)
            socket?.soTimeout = 60000
        } catch (e: IOException) {
            e.printStackTrace()
            if (listener != null) {
                listener?.onSocketConnectFail()
                return
            }
        }
        listener?.onSocketConnectSuccess()
        if (listener == null || socket == null || socket?.isConnected == false) {
            listener?.onSocketConnectFail()
            return
        }
        try {
            input = BufferedInputStream(socket?.getInputStream())
            out = BufferedOutputStream(socket?.getOutputStream())
            mWrite = TcpWriteThread(out!!, mSendQueue!!, this)
            mRead = TcpReadThread(input, this)
            mRead?.start()
            state = State.LIVING
            listener?.onTcpConnectSuccess()
            mWrite?.sendData(byteArrayOf()) //先发一个空的数据..用于让播放端相应,知道这是一个投屏请求
        } catch (e: IOException) {
            e.printStackTrace()
            listener!!.onTcpConnectFail()
        }
    }

    fun setVideoParams(videoConfiguration: VideoConfiguration?) {
        width = videoConfiguration?.width?:0
        height = videoConfiguration?.height?:0
        fps = videoConfiguration?.fps?:0
        maxBps = videoConfiguration?.maxBps?:0
    }

    fun setSpsPps(spsPps: ByteArray?) {
        mSpsPps = spsPps
    }

    override fun socketDisconnect() {
        listener?.onSocketDisconnect()
    }

    override fun connectSuccess() {
        mWrite?.start()
    }

    private fun clearSocket() {
        if (socket != null && socket!!.isConnected) {
            try {
                socket?.close()
                socket = null
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun stop() {
        object : Thread() {
            override fun run() {
                super.run()
                listener = null
                mWrite?.shutDown()
                mRead?.shutDown()
                try {
                    out?.close()
                    input?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                clearSocket()
            }
        }.start()
        state = State.INIT
    }

    fun getState(): State {
        return state
    }
}