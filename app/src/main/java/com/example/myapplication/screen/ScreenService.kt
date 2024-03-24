package com.example.myapplication.screen

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.util.Log
import com.example.myapplication.Media.NotificationHelper
import com.example.myapplication.ScreenRecordActivity.Companion.RECORD_REQUEST_CODE

class ScreenService : Service() {


    private var mediaRecorder: MediaRecorder? = null
    private var mediaProjection: MediaProjection? = null

    private var isMediaRecording = false

    private var mController: StreamController? = null
    private var mHandler : Handler? = null


    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    class MediaProjectionBinder : Binder() {
        fun getService(): ScreenService? {
            return instance
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        return MediaProjectionBinder()
    }

    companion object MediaProjectionServiceInstance {

        fun bindService(context: Context, serviceConnection: ServiceConnection) {
            val intent = Intent(context, ScreenService::class.java)
            context.bindService(intent, serviceConnection, BIND_AUTO_CREATE)
        }

        fun unbindService(context: Context?, serviceConnection: ServiceConnection?) {
            serviceConnection?.let { context?.unbindService(it) }
        }

        private var instance: ScreenService? = null
        fun instance(): ScreenService? = instance
    }

    fun setStreamController(controller: StreamController?) {
        this.mController = controller
    }


    fun showNotification(context: Context, manager: MediaProjectionManager?, resultCode: Int, data: Intent?) {

        val mMediaProjection = data?.let { manager?.getMediaProjection(resultCode, it) }
        mHandler = Handler(context.mainLooper)
        mMediaProjection?.registerCallback(object : MediaProjection.Callback() {
            override fun onStop() {
                stopRecording();
                super.onStop()
            }
        }, mHandler)

        val title = "投屏"
        val not = NotificationHelper.instance.createSystem()
                        .setOngoing(true) // 常驻通知栏
                        .setTicker(title)
                        .setContentText(title)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .build()
        startForeground(RECORD_REQUEST_CODE, not)
        mController?.start(mMediaProjection)
    }


    override fun onDestroy() {
        destroy()
        super.onDestroy()
    }

    private fun destroy() {
        stopRecording()

        mediaProjection?.stop()
        mediaProjection = null

        stopForeground(true)
    }

    fun stopRecording() {
        mediaRecorder?.stop()
        mediaRecorder?.reset()
        mediaRecorder?.release()

        mediaRecorder = null
        isMediaRecording = false

    }
}