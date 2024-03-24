package com.example.myapplication.screen

import android.util.Log
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.atomic.AtomicInteger

class NormalSendQueue: ISendQueue {

    private val NORMAL_FRAME_BUFFER_SIZE = 150 //缓存区大小

    private val SCAN_MAX_TIME = 5 //仲裁次数,每循环SCAN_MAX_TIME 次,每次sleep(DEFAULT_SLEEP_TIME),会执行一次检查网速的代码

    private val DEFAULT_SLEEP_TIME = 200L //

    private val DEFAULT_NEGATIVE_COUNT =
        3 //循环SCAN_MAX_TIME 次,有 DEFAULT_NEGATIVE_COUNT 次输入queue的帧小于取走的帧

    //-----------------------------------------------------------
    private var mFrameBuffer: ArrayBlockingQueue<Frame<Chunk>>? = null
    private val mFullQueueCount = NORMAL_FRAME_BUFFER_SIZE
    private val mTotalFrameCount = AtomicInteger(0) //总个数

    private val mGiveUpFrameCount = AtomicInteger(0) //总个数

    private val mKeyFrameCount = AtomicInteger(0) //队列里Key帧的总个数...


    private val mInFrameCount = AtomicInteger(0) //进入总个数

    private val mOutFrameCount = AtomicInteger(0) //输出总个数

    @Volatile
    private var mScanFlag = false
    private var mSendQueueListener: SendQueueListener? = null
    private var mScanThread: ScanThread? = null
    private val isDebug = false

    constructor() {
        mFrameBuffer = ArrayBlockingQueue(mFullQueueCount, true)
    }

    override fun start() {
        mScanFlag = true
        mScanThread = ScanThread()
        mScanThread?.start()
    }

    override fun stop() {
        mScanFlag = false
        mInFrameCount.set(0)
        mOutFrameCount.set(0)
        mTotalFrameCount.set(0)
        mGiveUpFrameCount.set(0)
        mFrameBuffer?.clear()
    }


    override fun putFrame(frame: Frame<Chunk>) {
        if (mFrameBuffer == null) {
            return
        }
        if (frame.frameType == Frame.FRAME_TYPE_KEY_FRAME) {
            mKeyFrameCount.getAndIncrement()
        }
        abandonData()
        try {
            mFrameBuffer?.put(frame)
            mInFrameCount.getAndIncrement()
            mTotalFrameCount.getAndIncrement()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun takeFrame(): Frame<Chunk>? {
        if (mFrameBuffer == null) {
            return null
        }
        var frame: Frame<Chunk>? = null
        try {
            frame = mFrameBuffer?.take()
        } catch (e: InterruptedException) {
            Log.e("frame.take", "" + e.message)
        }
        try {

            if (frame?.frameType == Frame.FRAME_TYPE_KEY_FRAME) {
                mKeyFrameCount.getAndDecrement()
            }
            mOutFrameCount.getAndIncrement()
            mTotalFrameCount.getAndDecrement()
        } catch (e: Exception) {
            Log.e("frame.take", "" + e.message)
            e.printStackTrace()
        }
        return frame
    }

    override fun setSendQueueListener(listener: SendQueueListener) {
        mSendQueueListener = listener
    }

    private fun abandonData() {
        if (mTotalFrameCount.get() >= (mFullQueueCount / 3)) {
            var pFrameDelete = false
            var start = false
            if ((mFrameBuffer?.size?:0) > 0) {
                for (frame in mFrameBuffer!!) {
                    if (frame.frameType == Frame.FRAME_TYPE_INTER_FRAME) {
                        start = true
                    }
                    if (start) {
                        if (frame.frameType == Frame.FRAME_TYPE_INTER_FRAME) {
                            mFrameBuffer?.remove(frame)
                            mTotalFrameCount.getAndDecrement()
                            mGiveUpFrameCount.getAndIncrement()
                            pFrameDelete = true
                        }else if (frame.frameType == Frame.FRAME_TYPE_KEY_FRAME){
                            if (mKeyFrameCount.get() > 5) {
                                mFrameBuffer?.remove(frame)
                                mKeyFrameCount.getAndDecrement()
                                continue
                            }
                            break
                        }
                    }
                }
            }
            var kFrameDelete = false
            //从队列头部开始搜索，删除最早发现的I帧
            if (!pFrameDelete) {
                for (frame in mFrameBuffer!!) {
                    if (frame.frameType === Frame.FRAME_TYPE_KEY_FRAME) {
                        mFrameBuffer!!.remove(frame)
                        mTotalFrameCount.getAndDecrement()
                        mGiveUpFrameCount.getAndIncrement()
                        mKeyFrameCount.getAndDecrement()
                        kFrameDelete = true
                        break
                    }
                }
            }
        }
    }

    inner class ScanThread: Thread() {
        private var mCurrentScanTime = 0
        private val mScanSnapShotList: ArrayList<ScanSnapShot> = ArrayList()

        override fun run() {
            super.run()
            while (mScanFlag) {
                if (mCurrentScanTime == SCAN_MAX_TIME) {
                    var averageDif = 0
                    var negativeCounter = 0
                    for (i in 0 until SCAN_MAX_TIME) {
                        val dif = (mScanSnapShotList[i].mOutCount?:0) - (mScanSnapShotList[i].mInCount?:0)
                        if (dif < 0) {
                            negativeCounter++
                        }
                        averageDif += dif
                    }

                    if (negativeCounter >= DEFAULT_NEGATIVE_COUNT || averageDif < -100) {
                        mSendQueueListener?.bad()
                    }else {
                        mSendQueueListener?.good()
                    }
                    mScanSnapShotList.clear()
                    mCurrentScanTime = 0
                }
                mScanSnapShotList.add(ScanSnapShot(mInFrameCount.get(), mOutFrameCount.get()))
                mInFrameCount.set(0)
                mOutFrameCount.set(0)
                mCurrentScanTime++
                try {
                    sleep(DEFAULT_SLEEP_TIME)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }
    }

    class ScanSnapShot(inCount: Int?, outCount: Int?) {
        var mInCount: Int? = inCount
        var mOutCount: Int? = outCount
    }
}