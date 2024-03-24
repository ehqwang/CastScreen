package com.example.myapplication.screen

interface IAudioController {
    fun start()
    fun stop()
    fun pause()
    fun resume()
    fun mute(mute: Boolean)
    fun getSessionId(): Int
//    fun setAudioConfiguration(audioConfiguration: AudioConfiguration?)
//    fun setAudioEncodeListener(listener: OnAudioEncodeListener?)
}