package com.example.myapplication.player

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.*

/**
 * @author wanghq
 * @date 2023/1/25
 */
class AnalyticDataUtils {
    private var mListener: OnAnalyticDataListener? = null

    @Volatile
    private var readLength = 0
    private var timer: Timer? = null
    private var isCalculate = false

    /**
     * 分析头部数据
     */
    fun analysisHeader(header: ByteArray?): ReceiveHeader {
        //实现数组之间的复制
        //bytes：源数组
        //srcPos：源数组要复制的起始位置
        //dest：目的数组
        //destPos：目的数组放置的起始位置
        //length：复制的长度
        var buff = ByteArray(4)
        System.arraycopy(header, 1, buff, 0, 4)
        val mainCmd: Int = ByteUtil.bytesToInt(buff) //主指令  1`4
        buff = ByteArray(4)
        System.arraycopy(header, 5, buff, 0, 4)
        val subCmd: Int = ByteUtil.bytesToInt(buff) //子指令  5`8
        buff = ByteArray(4)
        System.arraycopy(header, 9, buff, 0, 4)
        val stringBodyLength: Int = ByteUtil.bytesToInt(buff) //文本数据 9 ~ 12;
        buff = ByteArray(4)
        System.arraycopy(header, 13, buff, 0, 4)
        val byteBodySize: Int = ByteUtil.bytesToInt(buff) //byte数据 13^16
        return ReceiveHeader(mainCmd, subCmd, header?.get(0) ?: 0, stringBodyLength, byteBodySize)
    }


    /**
     * 解析数据
     *
     * @param input
     * @param receiveHeader
     * @return
     * @throws IOException
     */
    @Throws(IOException::class)
    fun analyticData(input: InputStream?, receiveHeader: ReceiveHeader): ReceiveData? {
        var sendBody: ByteArray? = null
        var buff: ByteArray? = null
        //文本长度
        if (receiveHeader.getStringBodylength() !== 0) {
            sendBody = input?.let { readByte(it, receiveHeader.getStringBodylength()) }
        }
        //音视频长度
        if (receiveHeader.getBuffSize() !== 0) {
            buff = input?.let { readByte(it, receiveHeader.getBuffSize()) }
        }
        val data = ReceiveData()
        data.setHeader(receiveHeader)
        //        data.setSendBody(sendBody == null ? "" : new String(sendBody));
        buff?.let { data.setBuff(it) }
        return data
    }

    /**
     * 保证从流里读到指定长度数据
     *
     * @param input
     * @param readSize
     * @return
     * @throws Exception
     */
    @Throws(IOException::class)
    fun readByte(input: InputStream?, readSize: Int): ByteArray? {
        var buff = ByteArray(readSize)
        var len = 0
        var eachLen = 0
        val baos = ByteArrayOutputStream()
        while (len < readSize) {
            eachLen = input?.read(buff)?: 0
            if (eachLen != -1) {
                if (isCalculate) readLength += eachLen
                len += eachLen
                baos.write(buff, 0, eachLen)
            } else {
                baos.close()
                throw IOException()
            }
            if (len < readSize) {
                buff = ByteArray(readSize - len)
            }
        }
        val b = baos.toByteArray()
        baos.close()
        return b
    }


    interface OnAnalyticDataListener {
        //        void onSuccess(ReceiveData data);
        fun netSpeed(msg: String?)
    }

    fun setOnAnalyticDataListener(listener: OnAnalyticDataListener?) {
        mListener = listener
    }

    fun startNetSpeedCalculate() {
        stop()
        readLength = 0
        isCalculate = true
        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                if (mListener != null) {
                    mListener?.netSpeed((readLength / 1024).toString() + " kb/s")
                    readLength = 0
                }
            }
        }, 1000, 1000)
    }

    fun stop() {
        isCalculate = false
        try {
            timer?.cancel()
        } catch (e: Exception) {
        }
    }
}