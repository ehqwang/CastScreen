package com.example.myapplication.screen

import android.os.SystemClock
import android.text.TextUtils
import java.io.BufferedInputStream
import java.io.IOException

class TcpReadThread: Thread {
    private val TAG = "TcpReadThread"
    private var bis: BufferedInputStream? = null
    private var mListener: OnTcpReadListener? = null

    @Volatile
    private var startFlag = false

    constructor(bis: BufferedInputStream?, listener: OnTcpReadListener?) {
        this.bis = bis
        mListener = listener
        startFlag = true
    }

    override fun run() {
        super.run()
        while (startFlag) {
            SystemClock.sleep(50)
            try {
                acceptMsg()
            } catch (e: IOException) {
                startFlag = false
                if (mListener != null) mListener!!.socketDisconnect()
                //                Log.e(TAG, "read data Exception = " + e.toString());
            }
        }
    }

    fun shutDown() {
        startFlag = false
        interrupt()
    }

    @Throws(IOException::class)
    fun acceptMsg() {
        if (mListener == null) return
        if ((bis?.available()?:0) <= 0) {
            return
        }
        val bytes = ByteArray(2)
        bis!!.read(bytes)
        val s = String(bytes)
        if (TextUtils.isEmpty(s)) {
            return
        }
        if (TextUtils.equals(s, "OK")) {
            mListener?.connectSuccess()
        }
    }
}