package com.example.myapplication.screen

import com.example.myapplication.screen.Util.Companion.readBytesUntilFull
import com.example.myapplication.screen.Util.Companion.readUnsignedInt24
import com.example.myapplication.screen.Util.Companion.readUnsignedInt32
import java.io.EOFException
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import kotlin.experimental.and

class ChunkHeader {
    private val TAG = "ChunkHeader"
    private var chunkType: ChunkType? = null
    private var chunkStreamId = 0
    private var absoluteTimestamp = 0
    private var timestampDelta = -1
    var packetLength = 0
    private var messageType: MessageType? = null
    private var messageStreamId = 0
    private var extendedTimestamp = 0

    constructor()

    constructor(chunkType: ChunkType, chunkStreamId: Int, messageType: MessageType) {
        this.chunkType = chunkType
        this.chunkStreamId = chunkStreamId
        this.messageType = messageType
    }

    companion object {
        @Throws(IOException::class)
        fun readHeader(input: InputStream, sessionInfo: SessionInfo): ChunkHeader {
            val rtmpHeader = ChunkHeader()
            rtmpHeader.readHeaderImpl(input, sessionInfo)
            return rtmpHeader
        }
    }

    @Throws(IOException::class)
    fun readHeaderImpl(input: InputStream, sessionInfo: SessionInfo) {
        var basicHeaderByte = input.read()
        if (basicHeaderByte == -1) {
            throw EOFException("")
        }
        parseBasicHeader(basicHeaderByte.toByte())

        when (chunkType) {
            ChunkType.TYPE_0_FULL -> {
                //  b00 = 12 byte header (full header)
                // Read bytes 1-3: Absolute timestamp
                absoluteTimestamp = readUnsignedInt24(input)
                timestampDelta = 0
                // Read bytes 4-6: Packet length
                packetLength = readUnsignedInt24(input)
                // Read byte 7: Message type ID
                messageType = MessageType.valueOf(input.read().toByte())
                // Read bytes 8-11: Message stream ID (apparently little-endian order)
                val messageStreamIdBytes = ByteArray(4)
                readBytesUntilFull(input, messageStreamIdBytes)
                messageStreamId = Util.toUnsignedInt32LittleEndian(messageStreamIdBytes)
                // Read bytes 1-4: Extended timestamp
                extendedTimestamp =
                    if (absoluteTimestamp >= 0xffffff) readUnsignedInt32(input) else 0
                if (extendedTimestamp != 0) {
                    absoluteTimestamp = extendedTimestamp
                }
                sessionInfo.putPreReceiveChunkHeader(chunkStreamId, this)
            }
            ChunkType.TYPE_1_LARGE -> {
                // Read bytes 1-3: Timestamp delta
                // b01 = 8 bytes - like type 0. not including message stream ID (4 last bytes)
                timestampDelta = readUnsignedInt24(input)
                // Read bytes 4-6: Packet length
                packetLength = readUnsignedInt24(input)
                // Read byte 7: Message type ID
                messageType = MessageType.valueOf(input.read().toByte())
                // Read bytes 1-4: Extended timestamp delta
                extendedTimestamp = if (timestampDelta >= 0xffffff) readUnsignedInt32(input) else 0
                val prevHeader: ChunkHeader? = sessionInfo.getPreReceiveChunkHeader(chunkStreamId)
                if (prevHeader != null) {
                    messageStreamId = prevHeader.messageStreamId
                    absoluteTimestamp =
                        if (extendedTimestamp != 0) extendedTimestamp else prevHeader.absoluteTimestamp + timestampDelta
                } else {
                    messageStreamId = 0
                    absoluteTimestamp =
                        if (extendedTimestamp != 0) extendedTimestamp else timestampDelta
                }
                sessionInfo.putPreReceiveChunkHeader(chunkStreamId, this)
            }
            ChunkType.TYPE_2_TIMESTAMP_ONLY -> {
                // b10 = 4 bytes - Basic Header and timestamp (3 bytes) are included
                // Read bytes 1-3: Timestamp delta
                timestampDelta = readUnsignedInt24(input)
                // Read bytes 1-4: Extended timestamp delta
                extendedTimestamp = if (timestampDelta >= 0xffffff) readUnsignedInt32(input) else 0
                val prevHeader: ChunkHeader? = sessionInfo.getPreReceiveChunkHeader(chunkStreamId)
                packetLength = prevHeader?.packetLength?:0
                messageType = prevHeader?.messageType
                messageStreamId = prevHeader?.messageStreamId?:0
                absoluteTimestamp =
                    if (extendedTimestamp != 0) extendedTimestamp else (prevHeader?.absoluteTimestamp?:0) + timestampDelta
                sessionInfo.putPreReceiveChunkHeader(chunkStreamId, this)
            }
            ChunkType.TYPE_3_NO_BYTE -> {
                // b11 = 1 byte: basic header only
                val prevHeader: ChunkHeader = sessionInfo.getPreReceiveChunkHeader(chunkStreamId)!!
                // Read bytes 1-4: Extended timestamp
                extendedTimestamp =
                    if (prevHeader.timestampDelta >= 0xffffff) readUnsignedInt32(input) else 0
                timestampDelta = if (extendedTimestamp != 0) 0xffffff else prevHeader.timestampDelta
                packetLength = prevHeader.packetLength
                messageType = prevHeader.messageType
                messageStreamId = prevHeader.messageStreamId
                absoluteTimestamp =
                    if (extendedTimestamp != 0) extendedTimestamp else prevHeader.absoluteTimestamp + timestampDelta
                sessionInfo.putPreReceiveChunkHeader(chunkStreamId, this)
            }
            else -> {}
        }
    }

    @Throws(IOException::class)
    fun writeTo(out: OutputStream, chunkType: ChunkType, sessionInfo: SessionInfo) {

        // Write basic header byte
        out.write(6 shl ((chunkType.getValue().toInt())) or chunkStreamId)
        when (chunkType) {
            ChunkType.TYPE_0_FULL -> {
                //  b00 = 12 byte header (full header)
                absoluteTimestamp = sessionInfo.markAbsoluteTimestampTx().toInt()
                Util.writeUnsignedInt24(out,
                    if (absoluteTimestamp >= 0xffffff) 0xffffff else absoluteTimestamp)
                Util.writeUnsignedInt24(out, packetLength)
                messageType?.getValue()?.toInt()?.let { out.write(it) }
                Util.writeUnsignedInt32LittleEndian(out, messageStreamId)
                if (absoluteTimestamp >= 0xffffff) {
                    extendedTimestamp = absoluteTimestamp
                    Util.writeUnsignedInt32(out, extendedTimestamp)
                }
            }
            ChunkType.TYPE_1_LARGE -> {
                // b01 = 8 bytes - like type 0. not including message ID (4 last bytes)
                absoluteTimestamp = sessionInfo.markAbsoluteTimestampTx().toInt()
                val preChunkHeader: ChunkHeader? = sessionInfo.getPreSendChunkHeader(chunkStreamId)
                timestampDelta = absoluteTimestamp - (preChunkHeader?.absoluteTimestamp?:0)
                Util.writeUnsignedInt24(out,
                    if (absoluteTimestamp >= 0xffffff) 0xffffff else timestampDelta)
                Util.writeUnsignedInt24(out, packetLength)
                messageType?.getValue()?.toInt()?.let { out.write(it) }
                if (absoluteTimestamp >= 0xffffff) {
                    extendedTimestamp = absoluteTimestamp
                    Util.writeUnsignedInt32(out, absoluteTimestamp)
                }
            }
            ChunkType.TYPE_2_TIMESTAMP_ONLY -> {
                // b10 = 4 bytes - Basic Header and timestamp (3 bytes) are included
                absoluteTimestamp = sessionInfo.markAbsoluteTimestampTx().toInt()
                val preChunkHeader: ChunkHeader? =
                    sessionInfo.getPreSendChunkHeader(chunkStreamId)
                timestampDelta = absoluteTimestamp - (preChunkHeader?.absoluteTimestamp?:0)
                Util.writeUnsignedInt24(out,
                    if (absoluteTimestamp >= 0xffffff) 0xffffff else timestampDelta)
                if (absoluteTimestamp >= 0xffffff) {
                    extendedTimestamp = absoluteTimestamp
                    Util.writeUnsignedInt32(out, extendedTimestamp)
                }
            }
            ChunkType.TYPE_3_NO_BYTE -> {
                // b11 = 1 byte: basic header only
                absoluteTimestamp = sessionInfo.markAbsoluteTimestampTx().toInt()
                if (absoluteTimestamp >= 0xffffff) {
                    extendedTimestamp = absoluteTimestamp
                    Util.writeUnsignedInt32(out, extendedTimestamp)
                }
            }
            else -> throw IOException("Invalid chunk type: $chunkType")
        }
    }

    private fun parseBasicHeader(basicHeaderByte: Byte) {
        chunkType = ChunkType.valueOf((0xff and basicHeaderByte.toInt() ushr 6).toByte())
        chunkStreamId = (basicHeaderByte and 0x3F).toInt()
    }

}