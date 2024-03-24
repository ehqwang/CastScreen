package com.example.myapplication.player

/**
 * @author wanghq
 * @date 2023/1/25
 */
class ReceiveHeader {
    private var mainCmd = 0
    private var subCmd = 0
    private var encodeVersion: Byte = 0
    private var stringBodylength = 0
    private var buffSize = 0

    constructor(
        mainCmd: Int,
        subCmd: Int,
        encodeVersion: Byte,
        stringBodylength: Int,
        buffSize: Int
    ) {
        this.mainCmd = mainCmd
        this.subCmd = subCmd
        this.encodeVersion = encodeVersion
        this.stringBodylength = stringBodylength
        this.buffSize = buffSize
    }

    fun getMainCmd(): Int {
        return mainCmd
    }

    fun setMainCmd(mainCmd: Int) {
        this.mainCmd = mainCmd
    }

    fun getSubCmd(): Int {
        return subCmd
    }

    fun setSubCmd(subCmd: Int) {
        this.subCmd = subCmd
    }

    fun getEncodeVersion(): Byte {
        return encodeVersion
    }

    fun setEncodeVersion(encodeVersion: Byte) {
        this.encodeVersion = encodeVersion
    }

    fun getStringBodylength(): Int {
        return stringBodylength
    }

    fun setStringBodylength(stringBodylength: Int) {
        this.stringBodylength = stringBodylength
    }

    fun getBuffSize(): Int {
        return buffSize
    }

    fun setBuffSize(buffSize: Int) {
        this.buffSize = buffSize
    }
}