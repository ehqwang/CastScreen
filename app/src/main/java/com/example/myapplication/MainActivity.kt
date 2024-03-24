package com.example.myapplication

import android.app.Notification
import android.content.Intent
import android.media.projection.MediaProjection
import android.net.wifi.WifiManager
import android.os.*
import android.util.Log
import android.view.SurfaceView
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.example.myapplication.Media.MediaManagerHelper
import com.example.myapplication.Media.MediaProjectionNotificationEngine
import com.example.myapplication.Media.MediaRecorderCallback
import com.example.myapplication.client.ClientCallback
import com.example.myapplication.client.SocketClient
import com.example.myapplication.server.ServerCallback
import com.example.myapplication.server.SocketServer
import java.io.File

class MainActivity : AppCompatActivity(), View.OnClickListener, ServerCallback, ClientCallback {

    private var pathStr: TextView? = null
    private var tvService: TextView? = null
    private var tvServiceTitle: TextView? = null
    private var tvClient: TextView? = null
    private var tvClientTitle: TextView? = null
    private var etClient: EditText? = null
    private var tvIp: TextView? = null
    private var tvMsg: TextView? = null
    private var tvPlayer: TextView? = null

    private var mSurfaceView: SurfaceView? = null

    private var mHandler : MyHandler? = null

    private var openSocket = false
    private var connectSocket = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mHandler = MyHandler(this)

        initViewAndClick()
        initNotificationEngine()
        initSurfaceView()

        tvIp?.text = getIp()
    }

    private fun initViewAndClick() {
        pathStr = findViewById(R.id.tv_path)
        findViewById<TextView>(R.id.tv_start).setOnClickListener(this)
        findViewById<TextView>(R.id.tv_stop).setOnClickListener(this)
        findViewById<TextView>(R.id.tv_start_recorder).setOnClickListener(this)
        tvService = findViewById<TextView>(R.id.tv_service)
        tvService?.setOnClickListener(this)
        tvServiceTitle = findViewById(R.id.tv_service_title)
        tvClient = findViewById(R.id.tv_client)
        tvClient?.setOnClickListener(this)
        tvClientTitle = findViewById(R.id.tv_client_title)
        etClient = findViewById(R.id.et_client)
        tvIp = findViewById(R.id.tv_ip)

        findViewById<TextView>(R.id.tv_client_msg).setOnClickListener(this)
        findViewById<TextView>(R.id.tv_service_msg).setOnClickListener(this)

        tvMsg = findViewById(R.id.tv_msg)

        findViewById<TextView>(R.id.tv_screen).setOnClickListener(this)
        findViewById<TextView>(R.id.tv_player).setOnClickListener(this)

    }

    private fun initSurfaceView() {
        mSurfaceView = findViewById(R.id.surface)
        MediaManagerHelper.mediaManagerHelper.holder =
        mSurfaceView?.holder
    }

    private fun initNotificationEngine() {

        MediaManagerHelper.mediaManagerHelper
            .setNotificationEngine(object : MediaProjectionNotificationEngine {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun getNotification(): Notification {
                    val title = "启动投屏"
                    return NotificationCompat.Builder(this@MainActivity, "system")
                        .setOngoing(true) // 常驻通知栏
                        .setTicker(title)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentText(title)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .build()
                }
            })
    }

    private fun startService() {
        MediaManagerHelper.mediaManagerHelper.startService(this@MainActivity)
    }

    private fun stopService() {
        MediaManagerHelper.mediaManagerHelper.stopMediaRecorder()
    }

    override fun onDestroy() {
        super.onDestroy()
        MediaManagerHelper.mediaManagerHelper.stopService(this@MainActivity)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        MediaManagerHelper.mediaManagerHelper
            .createVirtualDisplay(requestCode, resultCode, data, true, true)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.tv_start -> {
                startService()
            }
            R.id.tv_start_recorder -> {
                startRecorder()
            }
            R.id.tv_stop -> {
                stopService()
            }
            R.id.tv_service -> {
                if (openSocket) stopServer() else startServer()
            }
            R.id.tv_client -> {
                if (connectSocket) closeConnect() else connectServer(etClient?.text.toString().trim())
            }
            R.id.tv_client_msg -> {
                sendToServer("222")
            }
            R.id.tv_service_msg -> {
                sendToClient("111")
            }
            R.id.tv_screen -> {
                startActivity(Intent(this, ScreenRecordActivity::class.java))
            }
            R.id.tv_player -> {
                startActivity(Intent(this, PlayerActivity::class.java))
            }
        }
    }

    private fun startRecorder() {
        MediaManagerHelper.mediaManagerHelper.startMediaRecorder(object : MediaRecorderCallback() {
            override fun onSuccess(file: File) {
                super.onSuccess(file)
                pathStr?.text = file.absolutePath
            }

            override fun onFail(text: String) {
                super.onFail(text)
                pathStr?.text = text
            }
        })
    }

    override fun receiveServerMsg(msg: String) {
        tvMsg?.text = msg
        val message = Message()
        message.what = 1
        val bundle = Bundle()
        bundle.putString("1", msg)
        message.data = bundle
        mHandler?.sendMessage(message)
    }

    override fun receiveClient(success: Boolean, msg: String) {
//        tvMsg?.text = msg

        val message = Message()
        message.what = 1
        val bundle = Bundle()
        bundle.putString("1", msg)
        message.data = bundle
        mHandler?.sendMessage(message)
    }

    override fun otherMsg(msg: String) {
        val message = Message()
        message.what = 1
        val bundle = Bundle()
        bundle.putString("1", msg)
        message.data = bundle
        mHandler?.sendMessage(message)
//        tvMsg?.text = msg
    }

    class MyHandler(val activity: MainActivity) : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when(msg.what) {
                1 -> {
                    activity.tvMsg?.text = msg.data.getString("1")
                }
            }

        }
    }


    /**
     * 开启服务
     */
    fun startServer() {
        openSocket = true
        SocketServer.startServer(this)
        tvService?.text = "关闭服务"
        tvServiceTitle?.text = "服务端: 开启状态"
    }

    /**
     * 停止服务
     */
    fun stopServer() {
        openSocket = false
        SocketServer.stopServer()
        tvService?.text = "开启服务"
        tvServiceTitle?.text = "服务端: 关闭状态"
    }

    /**
     * 连接服务
     */
    fun connectServer(ipAddress: String) {
        connectSocket = true
        SocketClient.connectServer(ipAddress, this)
        tvClient?.text = "关闭连接"
        tvClientTitle?.text = "客户端：连接中"
    }

    /**
     * 关闭连接
     */
    fun closeConnect() {
        connectSocket = false
        SocketClient.closeConnect()
        tvClient?.text = "开启连接"
        tvClientTitle?.text = "客户端：断开"
    }

    /**
     * 发送到客户端
     */
    fun sendToClient(msg: String) {
        SocketServer.sendToClient(msg)
    }

    /**
     * 发送到服务端
     */
    fun sendToServer(msg: String) {
        SocketClient.sendToServer(msg)
    }



    private fun getIp() =
        intToIp((BaseApplication.instance()?.applicationContext?.getSystemService(WIFI_SERVICE) as WifiManager).connectionInfo.ipAddress)

    private fun intToIp(ip: Int) =
        "${ip and 0xFF}.${ip shr 8 and 0xFF}.${(ip shr 16 and 0xFF)}.${ip shr 24 and 0xFF}"

}