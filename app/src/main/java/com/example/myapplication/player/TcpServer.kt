package com.example.myapplication.player

import android.util.Log
import java.io.IOException
import java.net.InetSocketAddress
import java.net.ServerSocket

/**
 * @author wanghq
 * @date 2023/1/25
 */
class TcpServer : AcceptStreamDataThread.OnTcpChangeListener  {
    private val TAG = "TcpServer"
    private var serverSocket: ServerSocket? = null
    private var isAccept = true
    private var mListener: OnAcceptBuffListener? = null

    //把线程给添加进来
    private var mAnalyticUtils: AnalyticDataUtils? = null
    private var acceptStreamDataThread: AcceptStreamDataThread? = null

    init {
        mAnalyticUtils = AnalyticDataUtils()
    }

    fun startServer() {
        object : Thread() {
            override fun run() {
                super.run()
                try {
                    serverSocket = ServerSocket()
                    serverSocket?.reuseAddress = true
                    val socketAddress = InetSocketAddress(ScreenRecordController.port)
                    serverSocket?.bind(socketAddress)
                    Log.i(TAG, "ServerSocket start bind port " + ScreenRecordController.port)
                    while (isAccept) {
                        //服务端接收客户端的连接请求
                        try {
                            val socket = serverSocket?.accept()
                            val inputStream = socket?.getInputStream()
                            val temp = mAnalyticUtils?.readByte(inputStream, 17)
                            val receiveHeader = mAnalyticUtils?.analysisHeader(temp)
                            if (receiveHeader?.getMainCmd() == ScreenRecordApi.RECORD.MAIN_CMD) { //投屏请求
                                acceptStreamDataThread?.shutdown()
                                acceptStreamDataThread = null
                                acceptStreamDataThread =
                                    AcceptStreamDataThread(socket, mListener, this@TcpServer)
                                acceptStreamDataThread?.start()
                            } else {
                                Log.e(TAG, "accept other connect and close socket")
                                socket?.close()
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "connect has Exception = $e")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "exception close.$e")
                } finally {
                    Log.e(TAG, "TcpServer: thread close")
                    try {
                        serverSocket?.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }.start()
    }

    fun setOnAccepttBuffListener(listener: OnAcceptBuffListener?) {
        mListener = listener
    }

    fun stopServer() {
        Log.e(TAG, "stopServer: stop server")
        mListener = null
        isAccept = false
        object : Thread() {
            override fun run() {
                super.run()
                try {
                    acceptStreamDataThread?.shutdown()
                    acceptStreamDataThread = null
                    serverSocket?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }.start()
    }

    override fun connect() {
        if (ScreenRecordController.mServerStateChangeListener != null) {
            if (ScreenRecordController.mServerStateChangeListener != null) {
                ScreenRecordController.mHandler?.post(Runnable { ScreenRecordController.mServerStateChangeListener?.acceptH264TcpConnect() })
            }
        }
    }

    override fun disconnect(e: Exception?) {
        if (ScreenRecordController.mServerStateChangeListener != null) {
            ScreenRecordController.mHandler?.post(Runnable {
                ScreenRecordController.mServerStateChangeListener?.acceptH264TcpDisConnect(
                    e
                )
            })
        }
    }

    override fun netSpeed(netSpeed: String?) {
        if (ScreenRecordController.mServerStateChangeListener != null) {
            ScreenRecordController.mHandler?.post(Runnable {
                ScreenRecordController.mServerStateChangeListener?.acceptH264TcpNetSpeed(
                    netSpeed
                )
            })
        }
    }
}