package com.example.myapplication.Media

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Binder
import android.os.Environment
import android.os.IBinder
import android.util.DisplayMetrics
import android.util.Log
import android.view.SurfaceHolder
import java.io.File

class MediaProjectionService : Service() {

    private var notificationEngine: MediaProjectionNotificationEngine? = null
    private var virtualDisplayMediaRecorder: VirtualDisplay? = null
    private var callback: MediaRecorderCallback? = null

    private var displayMetrics: DisplayMetrics? = null
    private var mediaFile: File? = null
    private var mediaRecorder: MediaRecorder? = null
    private var mediaProjection: MediaProjection? = null

    private var isMediaRecording = false

    private var isScreenCaptureEnable = false // 是否可以屏幕截图
    private var isMediaRecorderEnable = false // 是否可以媒体录制


    private var mediaProjectionManager: MediaProjectionManager? = null

    private var surfaceHolder: SurfaceHolder? = null

    private val ID_MEDIA_PROJECTION: Int = MediaManagerHelper.mediaManagerHelper.REQUEST_CODE


    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    class MediaProjectionBinder : Binder() {
        fun getService(): MediaProjectionService? {
            return instance
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        return MediaProjectionBinder()
    }

    companion object MediaProjectionServiceInstance {

        fun bindService(context: Context, serviceConnection: ServiceConnection) {
            val intent = Intent(context, MediaProjectionService::class.java)
            context.bindService(intent, serviceConnection, BIND_AUTO_CREATE)
        }

        fun unbindService(context: Context?, serviceConnection: ServiceConnection?) {
            serviceConnection?.let { context?.unbindService(it) }
        }

        private var instance: MediaProjectionService? = null
        fun instance(): MediaProjectionService? = instance
    }


    fun setNotificationEngine(notificationEngine: MediaProjectionNotificationEngine?) {
        this.notificationEngine = notificationEngine
    }

    fun setSurfaceHolder(holder: SurfaceHolder?) {
        this.surfaceHolder = holder
    }

    fun startRecording(callback: MediaRecorderCallback) {
        this.callback = callback

        serviceCreateMediaRecorder()
//        object : Thread() {
//            override fun run() {
//                super.run()
//
//            }
//
//        }
        try {
            mediaRecorder?.start()
        } catch (e: Exception) {
            Log.e("mediaRecorder.start", "报错" + e.message)
        }
        isMediaRecording = true

    }

    private fun serviceCreateMediaRecorder() {
        val width: Int = displayMetrics?.widthPixels ?: 0
        val height: Int = displayMetrics?.heightPixels ?: 0
        val densityDpi: Int = displayMetrics?.densityDpi ?: 0

        // 创建保存路径

        val dir = Environment.DIRECTORY_MOVIES
        // 创建保存路径
        val dirFile = File(externalCacheDir, dir)
//        val dirFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), dir)
//        val dirFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)

        val mkdirs = dirFile.mkdirs()
        // 创建保存文件
        mediaFile = File(dirFile, "media" + System.currentTimeMillis() + ".mp4")

        // 调用顺序不能乱
        mediaRecorder = MediaRecorder()
        mediaRecorder?.setPreviewDisplay(surfaceHolder?.surface)
        mediaRecorder!!.setVideoSource(MediaRecorder.VideoSource.SURFACE)
        mediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)


        mediaRecorder!!.setOutputFile(mediaFile!!.absolutePath)
//        val pfd = ParcelFileDescriptor.fromSocket(SocketServer.socket)
//        mediaRecorder?.setOutputFile(pfd.fileDescriptor)


        mediaRecorder!!.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
        mediaRecorder!!.setVideoSize(width, height)
        mediaRecorder!!.setVideoFrameRate(30)
        mediaRecorder!!.setVideoEncodingBitRate(width * height)


//        mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL)
//        mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
//        mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)


//        mediaRecorder?.setVideoSource(MediaRecorder.VideoSource.SURFACE)
//        mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
//        mediaRecorder?.setOutputFile(mediaFile?.absolutePath)
//
//
////        val pfd = ParcelFileDescriptor.fromSocket(SocketServer.socket)
////        mediaRecorder?.setOutputFile(pfd.fileDescriptor)
//
//        mediaRecorder?.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
//        mediaRecorder?.setVideoSize(width, height)
//        mediaRecorder?.setVideoFrameRate(30)
//        mediaRecorder?.setVideoEncodingBitRate(5 * width * height)



//        mediaRecorder?.setPreviewDisplay(surfaceHolder?.surface)
////        mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.VOICE_UPLINK)
//        mediaRecorder?.setVideoSource(MediaRecorder.VideoSource.SURFACE)
//        mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
////        mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
//        mediaRecorder?.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
////        mediaRecorder?.setMaxDuration(50000)
////        mediaRecorder?.setMaxFileSize(50000)
////
////
////        mediaRecorder?.setVideoSize(320, 240)
////        mediaRecorder?.setVideoFrameRate(30)
////        mediaRecorder?.setVideoEncodingBitRate(5 * 320 * 240)
//
//
////        Log.e("录制服务socket", "" + SocketServer.socket)
////        val pfd = ParcelFileDescriptor.fromSocket(SocketServer.socket)
////        mediaRecorder?.setOutputFile(pfd.fileDescriptor)
//
//        mediaRecorder?.setOutputFile(mediaFile?.absolutePath)


        mediaRecorder?.setOnErrorListener { mr, what, extra ->

            Log.e("error======", "" +  what + extra)
            callback?.onFail("serviceCreateMediaRecorder执行报错")
        }


        try {
            mediaRecorder?.prepare()
        } catch (e: Exception) {
            Log.e("mediaRecorder.prepare", "" + e.message)
            e.printStackTrace()
        }


        if (virtualDisplayMediaRecorder == null) {
            virtualDisplayMediaRecorder = mediaProjection?.createVirtualDisplay(
                "MediaRecorder",
                width, height, densityDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                surfaceHolder?.surface, null, null
            )
        } else {
//            virtualDisplayMediaRecorder?.surface = mediaRecorder?.surface
            virtualDisplayMediaRecorder?.surface = surfaceHolder?.surface
        }
    }


    fun createVirtualDisplay(
        resultCode: Int,
        data: Intent?,
        displayMetrics: DisplayMetrics?,
        isScreenCaptureEnable: Boolean,
        isMediaRecorderEnable: Boolean
    ) {
        this.displayMetrics = displayMetrics
        this.isScreenCaptureEnable = isScreenCaptureEnable
        this.isMediaRecorderEnable = isMediaRecorderEnable
        if (data == null) {
            stopSelf()
            return
        }
        showNotification()
        mediaProjectionManager =
            getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        if (mediaProjectionManager == null) {
            stopSelf()
            return
        }
        mediaProjection = mediaProjectionManager?.getMediaProjection(resultCode, data)
        if (mediaProjection == null) {
            stopSelf()
            return
        }
//        if (isScreenCaptureEnable) {
//            createImageReader()
//        }
    }

    private fun showNotification() {
        Log.e("notification=====", "notificationEngine = " + notificationEngine)
        if (notificationEngine == null) {
            return
        }
        var notification = notificationEngine?.getNotification()
//        notification = NotificationCompat.Builder(applicationContext, "system")
//            .setContentTitle("11")
//            .setSmallIcon(R.mipmap.ic_launcher)
//            .build().apply {
//                visibility = Notification.VISIBILITY_PUBLIC
//            }

        Log.e("notification=====", "" + notification)

        startForeground(ID_MEDIA_PROJECTION, notification)

    }


    override fun onDestroy() {
        destroy()
        super.onDestroy()
    }

    private fun destroy() {
        stopRecording()

        mediaProjection?.stop()
        mediaProjection = null

        virtualDisplayMediaRecorder?.release()
        virtualDisplayMediaRecorder = null

        stopForeground(true)
    }

    fun stopRecording() {
        if (callback != null) {
            callback?.onFail("暂停异常")
        }

        if (mediaRecorder == null) {
            callback?.onFail("mediaRecorder 为空")
            return
        }
        if (!isMediaRecording) {
            callback?.onFail("isMediaRecording 运行状态异常")
            return
        }

        mediaRecorder?.stop()
        mediaRecorder?.reset()
        mediaRecorder?.release()

        mediaRecorder = null

        mediaFile?.let { callback?.onSuccess(it) }
        mediaFile = null

        isMediaRecording = false

        callback = null
    }
}