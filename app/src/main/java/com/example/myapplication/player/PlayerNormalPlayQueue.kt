package com.example.myapplication.player

import android.util.Log
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.atomic.AtomicInteger

/**
 * @author wanghq
 * @date 2023/1/25
 */
class PlayerNormalPlayQueue {
    private var mPlayQueue: ArrayBlockingQueue<Frame>? = null
    private val TAG = "NormalPlayQueue"

    companion object {

        private val SCAN_MAX_TIME =
            5 //仲裁次数,每循环SCAN_MAX_TIME 次,每次sleep(DEFAULT_SLEEP_TIME),会执行一次检查网速的代码

        private val DEFAULT_SLEEP_TIME = 200 //

        private val DEFAULT_NEGATIVE_COUNT =
            3 //循环SCAN_MAX_TIME 次,有 DEFAULT_NEGATIVE_COUNT 次输入queue的帧小于取走的帧


        private val NORMAL_FRAME_BUFFER_SIZE = 800 //缓存区大小
    }

    private val mFullQueueCount = NORMAL_FRAME_BUFFER_SIZE
    private val mTotalFrameCount = AtomicInteger(0) //总个数

    private val mGiveUpFrameCount = AtomicInteger(0) //总个数

    private val mKeyFrameCount = AtomicInteger(0) //队列里Key帧的总个数...


    private val mInFrameCount = AtomicInteger(0) //进入总个数

    private val mOutFrameCount = AtomicInteger(0) //输出总个数


    private var mScanThread: ScanThread? = null

    @Volatile
    var mScanFlag = false
    private val isDebug = false

    init {
        mScanFlag = true
        mScanThread = ScanThread()
        mScanThread?.start()
        mPlayQueue = ArrayBlockingQueue(NORMAL_FRAME_BUFFER_SIZE, true)
    }


    fun takeByte(): Frame? {
        return try {
            val frame = mPlayQueue?.take()
            if ((frame?.type ?: 0) == Frame.KEY_FRAME) {
                mKeyFrameCount.getAndDecrement()
            }
            mOutFrameCount.getAndIncrement()
            mTotalFrameCount.getAndDecrement()
            frame
        } catch (e: InterruptedException) {
            showLog("take bytes exception$e")
            null
        }
    }

    fun putByte(frame: Frame) {
        if (frame.type == Frame.KEY_FRAME) {
            mKeyFrameCount.getAndIncrement()
        }
        abandonData()
        try {
            mPlayQueue?.put(frame)
            mInFrameCount.getAndIncrement()
            mTotalFrameCount.getAndIncrement()
        } catch (e: InterruptedException) {
            showLog("put bytes exception$e")
        }
    }

    fun stop() {
        mScanFlag = false
        mTotalFrameCount.set(0)
        mGiveUpFrameCount.set(0)
        mPlayQueue?.clear()
    }

    private fun abandonData() {
        if (mTotalFrameCount.get() >= mFullQueueCount / 3) {
            showLog("队列里的帧数太多,开始丢帧..")
            //从队列头部开始搜索，删除最早发现的连续P帧
            var pFrameDelete = false
            var start = false
            for (frame in mPlayQueue!!) {
                if (!start) showLog("丢掉了下一个KEY_FRAME前的所有INTER_FRAME..")
                if (frame.type == Frame.NORMAL_FRAME) {
                    start = true
                }
                if (start) {
                    if (frame.type == Frame.NORMAL_FRAME) {
                        mPlayQueue?.remove(frame)
                        mTotalFrameCount.getAndDecrement()
                        mGiveUpFrameCount.getAndIncrement()
                        pFrameDelete = true
                    } else if (frame.type == Frame.KEY_FRAME) {
                        if (mKeyFrameCount.get() > 5) {
                            Log.d(TAG, "丢掉了一个关键帧.. total" + mKeyFrameCount.get())
                            mPlayQueue!!.remove(frame)
                            mKeyFrameCount.getAndDecrement()
                            continue
                        }
                        break
                    }
                }
            }
            var kFrameDelete = false
            //从队列头部开始搜索，删除最早发现的I帧
            if (!pFrameDelete) {
                for (frame in mPlayQueue!!) {
                    if (frame.type == Frame.KEY_FRAME) {
                        mPlayQueue?.remove(frame)
                        Log.d(TAG, "丢掉了一个关键帧..")
                        mTotalFrameCount.getAndDecrement()
                        mGiveUpFrameCount.getAndIncrement()
                        mKeyFrameCount.getAndDecrement()
                        kFrameDelete = true
                        break
                    }
                }
            }
            //从队列头部开始搜索，删除音频
            if (!pFrameDelete && !kFrameDelete) {
                for (frame in mPlayQueue!!) {
                    if (frame.type == Frame.AUDIO_FRAME) {
                        mPlayQueue?.remove(frame)
                        mTotalFrameCount.getAndDecrement()
                        mGiveUpFrameCount.getAndIncrement()
                        break
                    }
                }
            }
        }
    }

    inner class ScanThread : Thread() {
        private var mCurrentScanTime = 0
        private val mScanSnapShotList = ArrayList<ScanSnapShot>()
        override fun run() {
            while (mScanFlag) {
                //达到仲裁次数了
                if (mCurrentScanTime == SCAN_MAX_TIME) {
                    var averageDif = 0
                    var negativeCounter = 0
                    var strLog = ""
                    for (i in 0 until SCAN_MAX_TIME) {
                        val dif = mScanSnapShotList[i].outCount - mScanSnapShotList[i].inCount
                        if (dif < 0) {
                            negativeCounter++
                        }
                        averageDif += dif
                        strLog = strLog + String.format("n%d:%d  ", i, dif)
                    }
                    if (negativeCounter >= DEFAULT_NEGATIVE_COUNT || averageDif < -100) {
                        //坏
                        showLog("Bad Send Speed.")
                    } else {
                        //好
                        showLog("Good Send Speed.")
                    }
                    //清空
                    mScanSnapShotList.clear()
                }
                mScanSnapShotList.add(ScanSnapShot(mInFrameCount.get(), mOutFrameCount.get()))
                mInFrameCount.set(0)
                mOutFrameCount.set(0)
                mCurrentScanTime++
                try {
                    sleep(DEFAULT_SLEEP_TIME.toLong())
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private class ScanSnapShot(var inCount: Int, var outCount: Int)

    private fun showLog(msg: String) {
        if (isDebug) Log.i("NormalSendQueue", "" + msg)
    }

}