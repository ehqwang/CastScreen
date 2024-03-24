package com.example.myapplication.screen

import java.util.concurrent.ConcurrentHashMap

class SessionInfo {
    companion object {

        val RTMP_STREAM_CHANNEL: Byte = 0x05
        val RTMP_COMMAND_CHANNEL: Byte = 0x03
        val RTMP_VIDEO_CHANNEL: Byte = 0x06
        val RTMP_AUDIO_CHANNEL: Byte = 0x07
        val RTMP_CONTROL_CHANNEL: Byte = 0x02
    }

    /** The window acknowledgement size for this RTMP session, in bytes; default to max to avoid unnecessary "Acknowledgment" messages from being sent  */
    private var acknowledgementWindowSize = Int.MAX_VALUE

    /** Used internally to store the total number of bytes read (used when sending Acknowledgement messages)  */
    private val totalBytesRead = 0

    /** Default chunk size is 128 bytes  */
    private var rxChunkSize = 128
    private var txChunkSize = 128
    private val chunkReceiveChannels: MutableMap<Int, ChunkHeader> = HashMap()
    private val chunkSendChannels: MutableMap<Int, ChunkHeader> = HashMap()
    private val invokedMethods: MutableMap<Int, String> = ConcurrentHashMap()

    fun getPreReceiveChunkHeader(chunkStreamId: Int): ChunkHeader? {
        return chunkReceiveChannels[chunkStreamId]
    }

    fun putPreReceiveChunkHeader(chunkStreamId: Int, chunkHeader: ChunkHeader) {
        chunkReceiveChannels[chunkStreamId] = chunkHeader
    }

    fun getPreSendChunkHeader(chunkStreamId: Int): ChunkHeader? {
        return chunkSendChannels[chunkStreamId]
    }

    fun putPreSendChunkHeader(chunkStreamId: Int, chunkHeader: ChunkHeader) {
        chunkSendChannels[chunkStreamId] = chunkHeader
    }

    fun takeInvokedCommand(transactionId: Int): String? {
        return invokedMethods.remove(transactionId)
    }

    fun addInvokedCommand(transactionId: Int, commandName: String): String? {
        return invokedMethods.put(transactionId, commandName)
    }

    fun getRxChunkSize(): Int {
        return rxChunkSize
    }

    fun setRxChunkSize(chunkSize: Int) {
        rxChunkSize = chunkSize
    }

    fun getTxChunkSize(): Int {
        return txChunkSize
    }

    fun setTxChunkSize(chunkSize: Int) {
        txChunkSize = chunkSize
    }

    fun getAcknowledgementWindowSize(): Int {
        return acknowledgementWindowSize
    }

    fun setAcknowledgmentWindowSize(acknowledgementWindowSize: Int) {
        this.acknowledgementWindowSize = acknowledgementWindowSize
    }

    private var sessionBeginTimestamp: Long = 0

    /** Sets the session beginning timestamp for all chunks  */
    fun markSessionTimestampTx() {
        sessionBeginTimestamp = System.nanoTime() / 1000000
    }

    /** Utility method for calculating & synchronizing transmitted timestamps  */
    fun markAbsoluteTimestampTx(): Long {
        return System.nanoTime() / 1000000 - sessionBeginTimestamp
    }
}