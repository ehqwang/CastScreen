package com.example.myapplication.screen

interface OnSenderListener {
    fun onConnecting()
    fun onConnected()
    fun onDisConnected()
    fun onPublishFail()
    fun onNetGood()
    fun onNetBad()
}