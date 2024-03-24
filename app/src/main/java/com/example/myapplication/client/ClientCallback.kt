package com.example.myapplication.client

interface ClientCallback {
    fun receiveServerMsg(msg: String)

    fun otherMsg(msg: String)
}