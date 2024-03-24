package com.example.myapplication.player

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.os.Handler

/**
 * @author wanghq
 * @date 2023/1/25
 */
class ScreenRecordController {
    var mContext: Context? = null
    var tcpServer: TcpServer? = null


    private var acceptBuffListener: OnAcceptBuffListener? = null
    private var mVideoPlayController: VideoPlayController? = null



    companion object {
        var port = 11111
        var mServerStateChangeListener: OnServerStateChangeListener? = null
        var mHandler: Handler? = null

        val instance by lazy(LazyThreadSafetyMode.NONE) {
            ScreenRecordController()
        }
    }

    fun init(application: Application): ScreenRecordController {
        mHandler = Handler(application.mainLooper)
        mContext = application
        return instance
    }

    //开启server
    fun startServer(): ScreenRecordController {
        if (tcpServer == null) {
            tcpServer = TcpServer()
        }
        tcpServer?.startServer()
        if (acceptBuffListener != null) tcpServer?.setOnAccepttBuffListener(acceptBuffListener)
        return instance
    }

    fun setPort(port: Int): ScreenRecordController {
        ScreenRecordController.port = port
        return instance
    }

    fun stopServer(): ScreenRecordController {
        mVideoPlayController = null
        acceptBuffListener = null
        tcpServer?.stopServer()
        tcpServer = null
        return instance
    }

    fun setOnAcceptTcpStateChangeListener(listener: OnServerStateChangeListener?) {
        mServerStateChangeListener = listener
    }

    fun setVideoPlayController(videoPlayController: VideoPlayController?): ScreenRecordController {
        mVideoPlayController = videoPlayController
        acceptBuffListener = mVideoPlayController?.getAcceptBuffListener()
        tcpServer?.setOnAccepttBuffListener(acceptBuffListener)
        return instance
    }
}