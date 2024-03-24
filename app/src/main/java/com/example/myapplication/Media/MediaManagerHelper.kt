package com.example.myapplication.Media

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.IBinder
import android.util.DisplayMetrics
import android.util.Log
import android.view.SurfaceHolder

class MediaManagerHelper {

    val REQUEST_CODE = 10086

    private var mediaProjectionManager: MediaProjectionManager? = null
    private var displayMetrics: DisplayMetrics? = null
    private var serviceConnection: ServiceConnection? = null
    private var mediaProjectionService: MediaProjectionService? = null
    private var notificationEngine: MediaProjectionNotificationEngine? = null

    public var holder: SurfaceHolder? = null

    companion object {
        val mediaManagerHelper by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            MediaManagerHelper()
        }
    }

    fun setNotificationEngine(notificationEngine: MediaProjectionNotificationEngine) {
        this.notificationEngine = notificationEngine
    }

    fun startService(activity: Activity) {
        if (mediaProjectionManager != null) {
            return
        }

        mediaProjectionManager = activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager?
        mediaProjectionManager?.let {
            activity.startActivityForResult(it.createScreenCaptureIntent(), REQUEST_CODE)
        }

        displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getRealMetrics(displayMetrics)


        // 绑定服务
        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                if (service is MediaProjectionService.MediaProjectionBinder) {
                    mediaProjectionService = service.getService()
                    mediaProjectionService?.setNotificationEngine(notificationEngine)
                    mediaProjectionService?.setSurfaceHolder(holder)
                }
            }

            override fun onServiceDisconnected(name: ComponentName) {
                mediaProjectionService = null
            }
        }

        serviceConnection?.let {
            MediaProjectionService.bindService(activity, it)
        }
    }

    fun startMediaRecorder(callback: MediaRecorderCallback) {
        if (mediaProjectionService == null) {
            callback.onFail("startMediaRecorder判空异常")
            return
        }
        mediaProjectionService?.startRecording(callback)
    }

    fun stopMediaRecorder() {
        mediaProjectionService?.stopRecording()
    }

    fun stopService(context: Context?) {
        mediaProjectionService = null
        if (serviceConnection != null) {
            MediaProjectionService.unbindService(context, serviceConnection)
            serviceConnection = null
        }
        displayMetrics = null
        mediaProjectionManager = null
    }

    fun createVirtualDisplay(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        isScreenCaptureEnable: Boolean,
        isMediaRecorderEnable: Boolean
    ) {
        if (mediaProjectionService == null) {
            return
        }
        if (requestCode != REQUEST_CODE) {
            return
        }
        if (resultCode != Activity.RESULT_OK) {
            return
        }

        mediaProjectionService?.createVirtualDisplay(
            resultCode,
            data,
            displayMetrics,
            isScreenCaptureEnable,
            isMediaRecorderEnable
        )
    }
}