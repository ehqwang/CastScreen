package com.example.myapplication.screen

enum class ChunkType {
    TYPE_0_FULL, TYPE_1_LARGE, TYPE_2_TIMESTAMP_ONLY, TYPE_3_NO_BYTE;

    /** The byte value of this chunk header type  */
    private var value: Byte = 0

    constructor() {}

    constructor(byteValue: Int) {
        this.value = byteValue.toByte()
    }



    fun getValue(): Byte {
        return value
    }

    companion object {
        /** The full size (in bytes) of this RTMP header (including the basic header byte)  */
        var quickLookupMap: HashMap<Byte, ChunkType> = HashMap()

        init {
            for(messageTypId in values()) {
                quickLookupMap[messageTypId.getValue()] = messageTypId
            }
        }

        fun valueOf(chunkHeaderType: Byte): ChunkType? {
            if (quickLookupMap.containsKey(chunkHeaderType)) {
                return quickLookupMap[chunkHeaderType]
            } else {
                throw java.lang.IllegalArgumentException("chunk header type byte:")
            }
        }
    }
}