package com.example.myapplication.test;


import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

public class Test {

    public void testHandler() {
        Handler mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 2:
                        break;
                }
            }
        };

        Bundle bundle = new Bundle();
        bundle.putInt("", 1);
        Message msg = new Message();
        msg.setData(bundle);
        msg.what = 2;



        mHandler.sendMessage(msg);
        mHandler.sendMessageAtTime(msg, 1000);

    }
}
