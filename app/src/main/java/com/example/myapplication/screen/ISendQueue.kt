package com.example.myapplication.screen

interface ISendQueue {
    fun start()
    fun stop()
    fun putFrame(frame: Frame<Chunk>)
    fun takeFrame(): Frame<Chunk>?
    fun setSendQueueListener(listener: SendQueueListener)
}