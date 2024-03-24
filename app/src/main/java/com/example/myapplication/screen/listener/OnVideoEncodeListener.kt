package com.example.myapplication.screen.listener

import android.media.MediaCodec
import java.nio.ByteBuffer

interface OnVideoEncodeListener {
    fun onVideoEncode(bb : ByteBuffer?, bi: MediaCodec.BufferInfo?)
}