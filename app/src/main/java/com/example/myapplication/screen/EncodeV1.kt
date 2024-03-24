package com.example.myapplication.screen

import java.nio.ByteBuffer

class EncodeV1 {
    private var encodeVersion: Byte = 0
    private var mainCmd = 0
    private var subCmd = 0
    private var body: String? = null
    private var buff : ByteArray? = null

    constructor(encodeVersion: Byte, mainCmd: Int, subCmd: Int, buff: ByteArray?) {
        this.encodeVersion = encodeVersion
        this.mainCmd = mainCmd
        this.subCmd = subCmd
        body = null
        this.buff = buff
    }

    fun buildSendContent(): ByteArray {
        var bodyLength: Int = 0
        var buffLength: Int = 0
        var bb: ByteBuffer? = null
        bodyLength = if (body == null || body!!.length == 0) {
            0
        } else {
            body!!.length
        }
        buffLength = if (buff == null || buff!!.size == 0) {
            0
        } else {
            buff!!.size
        }
        bb = ByteBuffer.allocate(17 + bodyLength + buffLength)
        bb.put(encodeVersion) //编码版本1     0,1
        bb.put(int2Bytes(mainCmd)) //1-4   主指令
        bb.put(int2Bytes(subCmd)) //5-8   子指令
        bb.put(int2Bytes(bodyLength)) //9 -12位,数据长度
        bb.put(int2Bytes(buffLength)) //13 -16位,数据长度
        if (bodyLength != 0) {
            bb.put(body!!.toByteArray())
        }
        if (buffLength != 0) {
            bb.put(buff)
        }
        return bb.array()
    }

    fun int2Bytes(length: Int): ByteArray {
        val result = ByteArray(4)
        result[0] = length.toByte()
        result[1] = (length shr 8).toByte()
        result[2] = (length shr 16).toByte()
        result[3] = (length shr 24).toByte()
        return result
    }
}