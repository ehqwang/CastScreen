package com.example.myapplication.server

interface ServerCallback {
    fun receiveClient(success: Boolean, msg: String)

    fun otherMsg(msg: String)
}