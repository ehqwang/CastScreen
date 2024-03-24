package com.example.myapplication.screen

class ScreenImageApi {

    companion object {
        val encodeVersion1: Byte = 0x00 //版本号1
    }

    object RECORD {
        //录屏指令
        const val MAIN_CMD = 1 //录屏主指令
        const val SEND_BUFF = 11 //发送声音的BUFF
    }
}