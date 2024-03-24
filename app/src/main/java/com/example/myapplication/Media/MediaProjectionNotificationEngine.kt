package com.example.myapplication.Media

import android.app.Notification

interface MediaProjectionNotificationEngine {
    fun getNotification(): Notification
}