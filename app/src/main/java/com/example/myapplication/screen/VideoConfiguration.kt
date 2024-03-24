package com.example.myapplication.screen

class VideoConfiguration {
    val DEFAULT_HEIGHT = 640
    val DEFAULT_WIDTH = 320
    val DEFAULT_FPS = 15
    val DEFAULT_MAX_BPS = 1500
    val DEFAULT_MIN_BPS = 400
    val DEFAULT_IFI = 1 //关键帧间隔时间 单位s

    val DEFAULT_MIME = "video/avc"

    var height = 0
    var width = 0
    var minBps = 0
    var maxBps = 0
    var fps = 0
    var ifi = 0
    var mime: String = ""

    constructor() {

    }

    constructor(builder: Builder) {
        height = builder.height
        width = builder.width
        minBps = builder.minBps
        maxBps = builder.maxBps
        fps = builder.fps
        ifi = builder.ifi
        mime = builder.mime
    }

    companion object {
        fun createDefault() : VideoConfiguration {
            return VideoConfiguration().Builder().build()
        }

    }

    inner class Builder {
        var height: Int = DEFAULT_HEIGHT
        var width: Int = DEFAULT_WIDTH
        var minBps: Int = DEFAULT_MIN_BPS
        var maxBps: Int = DEFAULT_MAX_BPS
        var fps: Int = DEFAULT_FPS
        var ifi: Int = DEFAULT_IFI
        var mime: String = DEFAULT_MIME
        fun setSize(width: Int, height: Int): Builder {
            this.width = width
            this.height = height
            return this
        }

        fun setBps(minBps: Int, maxBps: Int): Builder {
            this.minBps = minBps
            this.maxBps = maxBps
            return this
        }

        fun setFps(fps: Int): Builder {
            this.fps = fps
            return this
        }

        fun setIfi(ifi: Int): Builder {
            this.ifi = ifi
            return this
        }

        fun setMime(mime: String): Builder {
            this.mime = mime
            return this
        }

        fun build(): VideoConfiguration {
            return VideoConfiguration(this)
        }
    }

//    inner class SecondBuilder {
//        val height: Int = SECOND_HEIGHT
//        val width: Int = SECOND_WIDTH
//        val minBps: Int = SECOND_MIN_BPS
//        val maxBps: Int = SECOND_MAX_BPS
//        val fps: Int = SECOND_FPS
//        val ifi: Int = SECOND_IFI
//        val mime: String = SECOND_MIME
//
//        fun build(): VideoConfiguration {
//            return VideoConfiguration(this)
//        }
//    }
}