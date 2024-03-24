package com.example.myapplication.screen

class Frame<T>(data: T, packetType: Int, frameType: Int) {
    var data: T
    var packetType: Int
    var frameType: Int

    init {
        this.data = data
        this.packetType = packetType
        this.frameType = frameType
    }

    companion object {
        const val FRAME_TYPE_AUDIO = 1
        const val FRAME_TYPE_KEY_FRAME = 2
        const val FRAME_TYPE_INTER_FRAME = 3
        const val FRAME_TYPE_CONFIGURATION = 4
    }
}