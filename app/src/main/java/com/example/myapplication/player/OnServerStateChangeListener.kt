package com.example.myapplication.player

/**
 * @author wanghq
 * @date 2023/1/25
 */
abstract class OnServerStateChangeListener {
    abstract fun acceptH264TcpConnect()

    /**
     * by wt
     * 接收到客户端的Tcp断开连接
     *
     * @param e           异常提示
     */
    abstract fun acceptH264TcpDisConnect(e: Exception?)

    //读数据的时间
    open fun acceptH264TcpNetSpeed(netSpeed: String?) {}

    abstract fun exception()

}