package com.example.myapplication.screen

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class Util {
    companion object {
        @Throws(IOException::class)
        fun readUnsignedInt24(input: InputStream): Int {
            return input.read() and 0xff shl 16 or (input.read() and 0xff shl 8) or (input.read() and 0xff)
        }

        @Throws(IOException::class)
        fun readBytesUntilFull(input: InputStream, targetBuffer: ByteArray) {
            var totalBytesRead = 0
            var read: Int
            val targetBytes = targetBuffer.size
            do {
                read = input.read(targetBuffer, totalBytesRead, targetBytes - totalBytesRead)
                totalBytesRead += if (read != -1) {
                    read
                } else {
                    throw IOException("Unexpected EOF reached before read buffer was filled")
                }
            } while (totalBytesRead < targetBytes)
        }

        fun toUnsignedInt32LittleEndian(bytes: ByteArray): Int {
            return 0xff and bytes[3].toInt() shl 24 or (0xff and bytes[2].toInt() shl 16) or (0xff and bytes[1].toInt() shl 8) or (0xff and bytes[0].toInt())
        }

        @Throws(IOException::class)
        fun readUnsignedInt32(input: InputStream): Int {
            return input.read() and 0xff shl 24 or (input.read() and 0xff shl 16) or (input.read() and 0xff shl 8) or (input.read() and 0xff)
        }

        @Throws(IOException::class)
        fun writeUnsignedInt24(out: OutputStream, value: Int) {
            out.write((value ushr 16).toByte().toInt())
            out.write((value ushr 8).toByte().toInt())
            out.write(value.toByte().toInt())
        }

        @Throws(IOException::class)
        fun writeUnsignedInt32LittleEndian(out: OutputStream, value: Int) {
            out.write(value.toByte().toInt())
            out.write((value ushr 8).toByte().toInt())
            out.write((value ushr 16).toByte().toInt())
            out.write((value ushr 24).toByte().toInt())
        }

        @Throws(IOException::class)
        fun writeUnsignedInt32(out: OutputStream, value: Int) {
            out.write((value ushr 24).toByte().toInt())
            out.write((value ushr 16).toByte().toInt())
            out.write((value ushr 8).toByte().toInt())
            out.write(value.toByte().toInt())
        }
    }
}