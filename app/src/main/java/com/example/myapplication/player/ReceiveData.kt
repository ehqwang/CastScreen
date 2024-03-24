package com.example.myapplication.player

/**
 * @author wanghq
 * @date 2023/1/25
 */
class ReceiveData {
    private var header: ReceiveHeader? = null
    private var buff: ByteArray? = null

    fun getHeader(): ReceiveHeader? {
        return header
    }

    fun setHeader(header: ReceiveHeader?) {
        this.header = header
    }

    fun getBuff(): ByteArray? {
        return buff
    }

    fun setBuff(buff: ByteArray) {
        this.buff = buff
    }
}