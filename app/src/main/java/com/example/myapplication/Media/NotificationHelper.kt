package com.example.myapplication.Media

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.myapplication.BaseApplication
import com.example.myapplication.R

class NotificationHelper() {
    private val CHANNEL_ID_OTHER = "other"
    private val CHANNEL_NAME_OTHER = "其他消息"

    @TargetApi(Build.VERSION_CODES.O)
    private val CHANNEL_IMPORTANCE_OTHER = NotificationManager.IMPORTANCE_MIN

    private val CHANNEL_ID_SYSTEM = "system"
    private val CHANNEL_NAME_SYSTEM = "系统通知"

    @TargetApi(Build.VERSION_CODES.O)
    private val CHANNEL_IMPORTANCE_SYSTEM = NotificationManager.IMPORTANCE_HIGH

    companion object InstanceHolder {
        val instance = NotificationHelper()
    }

    fun getInstance(): NotificationHelper? {
        return instance
    }

    init {
        createChannel()
    }

    /**
     * 创建通知渠道
     */
    private fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }
        createChannel(CHANNEL_ID_OTHER, CHANNEL_NAME_OTHER, CHANNEL_IMPORTANCE_OTHER, false)
        createChannel(CHANNEL_ID_SYSTEM, CHANNEL_NAME_SYSTEM, CHANNEL_IMPORTANCE_SYSTEM, true)
    }

    /**
     * 创建通知渠道
     *
     * @param channelId   channelId
     * @param channelName channelName
     * @param importance  importance
     * @param isShowBadge 是否显示角标
     */
    @TargetApi(Build.VERSION_CODES.O)
    private fun createChannel(
        channelId: String,
        channelName: String,
        importance: Int,
        isShowBadge: Boolean
    ) {
        val channel = NotificationChannel(channelId, channelName, importance)
        channel.setShowBadge(isShowBadge)
        val notificationManager : NotificationManager = BaseApplication.instance()?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager?.createNotificationChannel(channel)
    }

    /**
     * 创建通知栏 Builder
     *
     * @return NotificationCompat.Builder
     */
    private fun create(channelId: String?): NotificationCompat.Builder {
        val context: Context = BaseApplication.instance() as Context
        return NotificationCompat.Builder(context, channelId!!)
            .setContentTitle(context.getString(R.string.app_name))
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.mipmap.ic_launcher)
            .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher))
    }

    /**
     * 创建通知栏 Builder
     *
     * @return NotificationCompat.Builder
     */
    fun createOther(): NotificationCompat.Builder? {
        return create(CHANNEL_ID_OTHER)
    }

    /**
     * 创建通知栏 Builder
     *
     * @return NotificationCompat.Builder
     */
    fun createSystem(): NotificationCompat.Builder {
        return create(CHANNEL_ID_SYSTEM)
    }

    /**
     * 显示通知栏
     *
     * @param id           id
     * @param notification notification
     */
    fun show(id: Int, notification: Notification?) {
        val context: Context = BaseApplication.instance() as Context
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager?.notify(id, notification)
    }

}