package com.example.myapplication.screen

import android.content.Context
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.projection.MediaProjection
import com.example.myapplication.screen.listener.OnVideoEncodeListener

class ScreenVideoController(context: Context) : IVideoController {
    private var mVirtualDisplay: VirtualDisplay? = null
    private var mMediaProjection: MediaProjection? = null
    private var mVideoConfiguration: VideoConfiguration = VideoConfiguration.createDefault()
    private var mEncoder: ScreenRecordEncoder? = null
    private var mListener: OnVideoEncodeListener? = null

    override fun start(mediaProjection: MediaProjection?) {
        mMediaProjection = mediaProjection
        mEncoder = ScreenRecordEncoder(mVideoConfiguration)
        val surface = mEncoder?.getSurface()
        mEncoder?.start()
        mEncoder?.setOnVideoEncodeListener(mListener)

        val width = VideoMediaCodec.getVideoSize(mVideoConfiguration.width)
        val height = VideoMediaCodec.getVideoSize(mVideoConfiguration.height)
        mVirtualDisplay = mMediaProjection?.createVirtualDisplay(
            "ScreenRecoder",
            width,
            height,
            1,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            surface,
            null,
            null
        )
    }

    override fun stop() {
        mEncoder?.setOnVideoEncodeListener(null)
        mEncoder?.stop()
        mEncoder = null

        mMediaProjection?.stop()
        mMediaProjection = null

        mVirtualDisplay?.release()
        mVirtualDisplay = null
    }

    override fun pause() {
        mEncoder?.setPause(true)
    }

    override fun resume() {
        mEncoder?.setPause(false)
    }

    override fun setVideoEncoderListener(listener: OnVideoEncodeListener?) {
        mListener = listener
    }

    override fun setVideoConfiguration(configuration: VideoConfiguration) {
        mVideoConfiguration = configuration
    }
}