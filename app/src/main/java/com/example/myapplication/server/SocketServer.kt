package com.example.myapplication.server

import android.util.Log
import com.example.myapplication.client.SocketClient
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket

object SocketServer {

    private val TAG = SocketServer::class.java.simpleName

    private const val SOCKET_PORT = 8888

    var socket: Socket? = null
    private var serverSocket: ServerSocket? = null
    private lateinit var mCallback: ServerCallback
    private lateinit var outputStream: OutputStream

    var result = true

    fun startServer(callback: ServerCallback): Boolean {
        result = true
        mCallback = callback
        Thread {
            try {
                serverSocket = ServerSocket(SOCKET_PORT)
                while (result) {
                    socket = serverSocket?.accept()
                    socket?.keepAlive = true
                    socket?.receiveBufferSize = 500000
                    socket?.sendBufferSize = 500000
                    Log.e("address", "" + socket?.localAddress)
                    mCallback.otherMsg("${socket?.inetAddress} to connected")
                    ServerThread(socket!!, mCallback).start()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                mCallback.otherMsg("连接失败${socket?.inetAddress} to connected")
                result = false
            }
        }.start()
        return result
    }

    fun stopServer() {
        socket?.apply {
            shutdownInput()
            shutdownOutput()
            close()
        }
        serverSocket?.close()
    }

    fun sendToClient(msg: String) {
        Thread {
            if (socket!!.isClosed) {
                Log.e(TAG, "sendToClient: Socket is closed")
                return@Thread
            }
            outputStream = socket!!.getOutputStream()
            try {
                outputStream.write(msg.toByteArray())
                outputStream.flush()
                mCallback.otherMsg("toClient:$msg")
                Log.d(TAG, "发送客户端成功")
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e(TAG, "向客户端发送消息失败")
            }
        }
    }

    class ServerThread(private val socket: Socket, private val callback: ServerCallback) : Thread() {
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
                    if (len < 1024) {
                        callback.receiveClient(true, receiveStr)
                        receiveStr = ""
                    }
                }
            }catch (e: IOException) {
                e.printStackTrace()
                e.message?.let { Log.e("socket error", it) }
                callback.receiveClient(false, "")
            }
        }
    }
}