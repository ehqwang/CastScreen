package com.example.myapplication.screen

interface OnTcpReadListener {

    fun socketDisconnect() //断开连接


    fun connectSuccess() //收到server消息,连接成功.

}