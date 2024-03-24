package com.example.myapplication

import android.app.Application
import android.os.Handler

class BaseApplication : Application() {

    private val handler = Handler()


    override fun onCreate() {
        super.onCreate()
        instance = this
        onInitData()
        handler.post { onInitDataThread() }
    }

    companion object applicationInstance{
        private var instance: BaseApplication? = null
        fun instance(): BaseApplication? = instance
    }

//    companion object instance {
//        val application by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
//            BaseApplication()
//        }
//    }

    /**
     * 初始化数据
     */
    protected fun onInitData() {
//        LogUtil.i("BaseFrameApplication onInitData")
    }

    /**
     * 线程初始化数据
     */
    protected fun onInitDataThread() {
//        LogUtil.i("BaseFrameApplication onInitDataThread")
    }

}