package com.example.myapplication.client

import android.net.LocalServerSocket
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket

object SocketClient {
    private val TAG = SocketClient::class.java.simpleName

    var socket: Socket? = null
    private var outputStream: OutputStream? = null
    private var inputStreamReader: InputStreamReader? = null
    private lateinit var mCallback: ClientCallback
    private const val SOCKET_PORT = 8888

    fun connectServer(ipAddress: String, callback: ClientCallback) {
        mCallback = callback
        Thread {
            try {
                socket = Socket(ipAddress, SOCKET_PORT)
                socket?.keepAlive = true
                socket?.receiveBufferSize = 500000
                socket?.sendBufferSize = 500000
//                socket = Socket()
//                socket?.connect(InetSocketAddress("192.168.27.229", SOCKET_PORT))
                ClientThread(socket!!, mCallback).start()
            } catch (e: IOException) {
                Log.e("连接失败", "" + e.message)
                e.printStackTrace()
            }
        }.start()
    }

    fun closeConnect() {
        inputStreamReader?.close()
        outputStream?.close()
        socket?.apply {
            shutdownInput()
            shutdownOutput()
            close()
        }
        Log.e(TAG, "关闭连接")
    }

    fun sendToServer(msg: String) {
        Thread {
            if (socket == null) {
                mCallback.otherMsg("客户端还未连接")
                return@Thread
            }
            if (socket!!.isClosed) {
                Log.e(TAG, "sendToServer: Socket is closed")
                return@Thread
            }
            try {
                outputStream = socket?.getOutputStream()
                outputStream?.write(msg.toByteArray())
                outputStream?.flush()
                mCallback.otherMsg("toServer: $msg")
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e(TAG, "向服务器发送消息失败")
            }
        }.start()
    }


    class ClientThread(private val socket: Socket, private val callback: ClientCallback) : Thread() {
        override fun run() {
            super.run()
            val inputStream: InputStream?
            try {
                inputStream = socket.getInputStream()
                val buffer = ByteArray(1024)
                var len: Int
                var receiveStr = ""
                if (inputStream.available() == 0) {
                    Log.e(TAG, "inputStream.available() == 0")
                }
                while (inputStream.read(buffer).also { len = it } != -1) {
                    receiveStr += String(buffer, 0, len, Charsets.UTF_8)
                    if (len <1024) {
                        callback.receiveServerMsg(receiveStr)
                        receiveStr = ""
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                e.message?.let { Log.e("socket error", it) }
                callback.receiveServerMsg("")
            }
        }
    }
}