package com.example.myapplication.screen

interface TcpConnectListener {
    fun onSocketConnectSuccess()
    fun onSocketConnectFail()
    fun onTcpConnectSuccess()
    fun onTcpConnectFail()
    fun onPublishSuccess()
    fun onPublishFail()
    fun onSocketDisconnect()
}