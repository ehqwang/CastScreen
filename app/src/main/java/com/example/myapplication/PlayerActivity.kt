package com.example.myapplication

import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.example.myapplication.player.OnServerStateChangeListener
import com.example.myapplication.player.ScreenRecordController
import com.example.myapplication.player.VideoPlayController

/**
 * @author wanghq
 * @date 2023/1/25
 */
class PlayerActivity: ComponentActivity() {

    var tvSpeed: TextView? = null
    private var surface: SurfaceView? = null
    var tvHint: TextView? = null

    var mController: VideoPlayController? = null
    private var mSurfaceHolder: SurfaceHolder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        initView()
        mController = VideoPlayController()
        ScreenRecordController.instance
            .init(application)
            .setPort(11111) //设置端口号
            .startServer() //初始化,并开启server
            .setVideoPlayController(mController) //设置VideoController
            .setOnAcceptTcpStateChangeListener(mStateChangeListener) //设置回调
        mSurfaceHolder = surface?.holder
        mSurfaceHolder?.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                mController?.surfaceCreate(holder)
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                mController?.surfaceDestroy()
            }
        })
    }

    private fun initView() {
        tvSpeed = findViewById(R.id.tv_speed)
        surface = findViewById(R.id.surface)
        tvHint = findViewById(R.id.tv_hint)
    }

    private var mStateChangeListener: OnServerStateChangeListener = object : OnServerStateChangeListener() {
        override fun acceptH264TcpConnect() {
            tvHint?.visibility = View.GONE
        }

        override fun acceptH264TcpDisConnect(e: Exception?) {
            tvHint?.visibility = View.VISIBLE
        }

        override fun exception() {}

        override fun acceptH264TcpNetSpeed(netSpeed: String?) {
            super.acceptH264TcpNetSpeed(netSpeed)
            tvSpeed?.text = netSpeed
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mController?.stop()
        ScreenRecordController.instance.stopServer()
    }

}