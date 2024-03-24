package com.example.myapplication.screen

class TcpSender: Sender, SendQueueListener{
    private val mSendQueue: ISendQueue = NormalSendQueue()
    private val TAG = "TcpSender"
    private var sendListener: OnSenderListener? = null
    private var mTcpConnection: TcpConnection? = null
    private val weakHandler: WeakHandler = WeakHandler()
    private var ip: String? = null
    private var port = 0

    constructor(ip:String?, port: Int) {
        mTcpConnection = TcpConnection()
        this.ip = ip
        this.port = port
    }

    override fun good() {
        weakHandler.post { sendListener?.onNetGood() }
    }

    override fun bad() {
        weakHandler.post { sendListener?.onNetBad() }
    }

    override fun start() {
        mSendQueue.setSendQueueListener(this)
        mSendQueue.start()
    }

    fun setVideoParams(videoConfiguration: VideoConfiguration?) {
        mTcpConnection?.setVideoParams(videoConfiguration)
    }

    override fun onData(data: ByteArray?, type: Int) {
        var frame: Frame<Chunk>? = null
        val video = Video()
        video.data = data
        when (type) {
            TcpPacker.FIRST_VIDEO -> {
                frame = Frame(video, type, Frame.FRAME_TYPE_CONFIGURATION)
            }
            TcpPacker.KEY_FRAME -> {
                frame = Frame(video, type, Frame.FRAME_TYPE_KEY_FRAME)
            }
            TcpPacker.INTER_FRAME -> {
                frame = Frame(video, type, Frame.FRAME_TYPE_INTER_FRAME)
            }
            TcpPacker.AUDIO -> {
                frame = Frame(video, type, Frame.FRAME_TYPE_AUDIO)
            }
        }
        if (frame == null) return
        mSendQueue.putFrame(frame)
    }

    override fun stop() {
        mTcpConnection?.stop()
        mSendQueue.stop()
    }

    fun connect() {
        mTcpConnection?.setSendQueue(mSendQueue)
        Thread { connectNotInUi() }.start()
    }

    @Synchronized
    private fun connectNotInUi() {
        mTcpConnection?.setConnectListener(mTcpListener)
        mTcpConnection?.connect(ip, port)
    }

    private val mTcpListener: TcpConnectListener = object : TcpConnectListener {
        override fun onSocketConnectSuccess() {
//            Log.e(TAG, "onSocketConnectSuccess");
        }

        override fun onSocketConnectFail() {
//            Log.e(TAG, "onSocketConnectFail");
            disConnected()
        }

        override fun onTcpConnectSuccess() {
//            Log.e(TAG, "onTcpConnectSuccess");
        }

        override fun onTcpConnectFail() {
//            Log.e(TAG, "onTcpConnectFail");
            disConnected()
        }

        override fun onPublishSuccess() {
//            Log.e(TAG, "onPublishSuccess");
            weakHandler.post { sendListener!!.onConnected() }
        }

        override fun onPublishFail() {
//            Log.e(TAG, "onPublishFail");
            weakHandler.post { sendListener!!.onPublishFail() }
        }

        override fun onSocketDisconnect() {
//            Log.e(TAG, "onSocketDisconnect");
            disConnected()
        }
    }

    private fun disConnected() {
        weakHandler.post { sendListener?.onDisConnected() }
    }

    fun setSenderListener(listener: OnSenderListener?) {
        sendListener = listener
    }

    fun setSpsPps(spsPps: ByteArray?) {
        mTcpConnection?.setSpsPps(spsPps)
    }
}