package com.example.myapplication.player

/**
 * @author wanghq
 * @date 2023/1/25
 */
class ByteUtil {
    companion object {
        /**
         * 将int转为长度为4的byte数组
         *
         * @param length
         * @return
         */
        fun int2Bytes(length: Int): ByteArray? {
            val result = ByteArray(4)
            result[0] = length.toByte()
            result[1] = (length shr 8).toByte()
            result[2] = (length shr 16).toByte()
            result[3] = (length shr 24).toByte()
            return result
        }

        //转成2个字节
        fun short2Bytes(size: Short): ByteArray? {
            val result = ByteArray(2)
            result[0] = size.toByte()
            result[1] = (size.toInt() shr 8).toByte()
            return result
        }

        /**
         * byte数组中取int数值，本方法适用于(低位在前，高位在后)的顺序，和和intToBytes（）配套使用
         *
         * @param src byte数组
         * @return int数值
         */
        fun bytesToInt(src: ByteArray): Int {
            val value: Int
            value = (src[0].toInt() and 0xFF
                    or (src[1].toInt() and 0xFF shl 8)
                    or (src[2].toInt() and 0xFF shl 16)
                    or (src[3].toInt() and 0xFF shl 24))
            return value
        }

        // TODO: 2018/6/11 wt byte转short
        fun bytesToShort(src: ByteArray): Short {
            val value: Short
            value = (src[0].toInt() and 0xFF
                    or (src[1].toInt() and 0xFF shl 8)).toShort()
            return value
        }


        /**
         * 获得校验码
         *
         * @param bytes 根据通讯协议的前12个字节
         * @return
         */
        fun getCheckCode(bytes: ByteArray): Byte {
            var b: Byte = 0x00
            for (i in bytes.indices) {
                b = (b.toInt() xor bytes[i].toInt()).toByte()
            }
            return b
        }
    }
}