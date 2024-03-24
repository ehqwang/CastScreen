package com.example.myapplication.screen

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

abstract class Chunk {

    protected var header: ChunkHeader? = null

    constructor()

    constructor(header: ChunkHeader?) {
        this.header = header
    }

    open fun getChunkHeader(): ChunkHeader? {
        return header
    }

    @Throws(IOException::class)
    abstract fun readBody(`in`: InputStream?)

    @Throws(IOException::class)
    protected abstract fun writeBody(out: OutputStream?)

    @Throws(IOException::class)
    open fun writeTo(out: OutputStream, sessionInfo: SessionInfo) {
        val chunkSize = sessionInfo.getTxChunkSize()
        val baos = ByteArrayOutputStream()
        writeBody(baos)
        val body = baos.toByteArray()
        header?.packetLength = body.size
        // Write header for first chunk
        header!!.writeTo(out, ChunkType.TYPE_0_FULL, sessionInfo)
        var remainingBytes = body.size
        var pos = 0
        while (remainingBytes > chunkSize) {
            // Write packet for chunk
            out.write(body, pos, chunkSize)
            remainingBytes -= chunkSize
            pos += chunkSize
            // Write header for remain chunk
            header!!.writeTo(out, ChunkType.TYPE_3_NO_BYTE, sessionInfo)
        }
        out.write(body, pos, remainingBytes)
    }
}