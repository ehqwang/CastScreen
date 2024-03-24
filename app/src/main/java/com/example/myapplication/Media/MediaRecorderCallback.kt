package com.example.myapplication.Media

import java.io.File

public abstract class MediaRecorderCallback {
    open fun onSuccess(file: File) {

    }

    open fun onFail(text: String){}
}