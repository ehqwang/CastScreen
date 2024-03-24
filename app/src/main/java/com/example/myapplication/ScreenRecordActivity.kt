package com.example.myapplication

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.os.IBinder
import android.widget.EditText
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.example.myapplication.screen.*


class ScreenRecordActivity : ComponentActivity(), OnSenderListener {

    companion object {
        val RECORD_REQUEST_CODE = 101
    }
    private var mStreamController: StreamController? = null
    private var mVideoConfiguration: VideoConfiguration? = null
    private var tcpSender: TcpSender? = null

    private var ip: String? = null
    private var etIp: EditText? = null
    private var tvConnect: TextView? = null

    private var recording = false

    private var mMediaProjectionManager: MediaProjectionManager? = null
    private var mService: ScreenService? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_screen)
        ip = "10.168.1.120"
        initView()
    }

    private fun initView() {
        etIp = findViewById(R.id.et_ip)
        tvConnect = findViewById(R.id.tv_connect)

        tvConnect?.setOnClickListener {
            if (!recording) {
                ip = etIp?.text.toString().trim()
                requestRecording()
                tvConnect?.text = "投屏中"
            }else {
                mStreamController?.stop()
                tvConnect?.text = "暂停投屏"
            }
        }
    }

    private fun requestRecording() {
        mMediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val intent = mMediaProjectionManager?.createScreenCaptureIntent()
        startActivityForResult(intent, RECORD_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RECORD_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

//                val videoController = ScreenVideoController(mMediaProjectionManager, resultCode, data)
                val videoController = ScreenVideoController(this)
                mStreamController = StreamController(videoController)
                recording = true
                startRecord(resultCode, data)
            }
        }
    }

    private fun startRecord(resultCode: Int, data: Intent?) {
        val packer = TcpPacker()
        mVideoConfiguration = VideoConfiguration.createDefault().Builder().build()
        mStreamController?.setVideoConfiguration(mVideoConfiguration)
        mStreamController?.setPacker(packer)

        tcpSender = TcpSender(ip, 11111)
        tcpSender?.setSenderListener(this)
        tcpSender?.setVideoParams(mVideoConfiguration)

        ScreenService.instance()?.setStreamController(mStreamController)
        val serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                if (service is ScreenService.MediaProjectionBinder) {

                    tcpSender?.connect()
                    mStreamController?.setSender(tcpSender)
                    mService = service.getService()
                    mService?.setStreamController(mStreamController)
                    mService?.showNotification(this@ScreenRecordActivity, mMediaProjectionManager, resultCode, data)

                }
            }

            override fun onServiceDisconnected(name: ComponentName) {
                mService = null
            }
        }
        ScreenService.bindService(this, serviceConnection)
    }

    override fun onConnecting() {
        TODO("Not yet implemented")
    }

    override fun onConnected() {
        TODO("Not yet implemented")
    }

    override fun onDisConnected() {
        TODO("Not yet implemented")
    }

    override fun onPublishFail() {
        TODO("Not yet implemented")
    }

    override fun onNetGood() {
        TODO("Not yet implemented")
    }

    override fun onNetBad() {
        TODO("Not yet implemented")
    }


}