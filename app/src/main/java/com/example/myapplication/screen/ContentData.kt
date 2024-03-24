package com.example.myapplication.screen

import java.io.InputStream
import java.io.OutputStream

open class ContentData: Chunk {

    var data: ByteArray? = null

    constructor()

    constructor(header: ChunkHeader?) {
    }

    override fun readBody(input: InputStream?) {
        data = ByteArray(header?.packetLength?:0)
        Util.readBytesUntilFull(input!!, data!!)
    }

    override fun writeBody(out: OutputStream?) {
        out!!.write(data)
    }
}