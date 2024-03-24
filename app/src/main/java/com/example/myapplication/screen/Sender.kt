package com.example.myapplication.screen

interface Sender {
    fun start()
    fun onData(data: ByteArray?, type: Int)
    fun stop()
}