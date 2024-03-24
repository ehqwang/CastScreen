package com.example.myapplication.screen

import android.os.Build
import android.os.Looper

class SopCastUtils {

    interface INotProcessor{
        fun process()
    }

    companion object {
        fun processNotUI(processor: INotProcessor) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                Thread {
                    processor.process()
                }.start()
            }else {
                processor.process()
            }
        }

        fun isOverLOLLIPOP(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
        }
    }
}