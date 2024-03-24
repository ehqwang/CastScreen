package com.example.myapplication.screen

import android.media.projection.MediaProjection
import com.example.myapplication.screen.listener.OnVideoEncodeListener

interface IVideoController {
    fun start(mediaProjection: MediaProjection?)
    fun stop()
    fun pause()
    fun resume()
    fun setVideoEncoderListener(listener: OnVideoEncodeListener?)
    fun setVideoConfiguration(configuration: VideoConfiguration)

}