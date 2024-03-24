package com.example.myapplication.player

/**
 * @author wanghq
 * @date 2023/1/25
 */
class ScreenRecordApi {
    companion object {
        val encodeVersion1: Byte = 0x00 //版本号1
    }


    object RECORD {
        //录屏指令
        const val MAIN_CMD = 1 //录屏主指令
        const val SEND_BUFF = 11 //发送声音的BUFF
    }

    object SERVER {
        //服务端与客户端交互指令
        const val MAIN_CMD = 0xA0 //投屏回传主指令
        const val INITIAL_SUCCESS = 0x01 //服务端初始化成功
    }
}