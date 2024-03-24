package com.example.myapplication.player

/**
 * @author wanghq
 * @date 2023/1/25
 */
class Frame {
    companion object {
        val SPSPPS = 2
        val KEY_FRAME = 4
        val NORMAL_FRAME = 5
        val AUDIO_FRAME = 6
    }
    var bytes: ByteArray? = null
    var type = 0
    var sps: ByteArray? = null
    var pps: ByteArray? = null
}