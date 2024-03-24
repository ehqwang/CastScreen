package com.example.myapplication.screen

enum class MessageType {
    SET_CHUNK_SIZE,
    ABORT,
    ACKNOWLEDGEMENT,
    USER_CONTROL_MESSAGE,
    WINDOW_ACKNOWLEDGEMENT_SIZE,
    SET_PEER_BANDWIDTH,
    AUDIO,
    VIDEO,
    DATA_AMF3,
    SHARED_OBJECT_AMF3,
    COMMAND_AMF3,
    DATA_AMF0,
    COMMAND_AMF0,
    SHARED_OBJECT_AMF0,
    AGGREGATE_MESSAGE;

    private var value: Byte = 0

    companion object {
        private var quickLookupMap: HashMap<Byte, MessageType> = HashMap()

        init {
            for (messageTypeId in values()) {
                quickLookupMap[messageTypeId.value] = messageTypeId
            }
        }
        fun valueOf(messageTypeId: Byte): MessageType {
            if (quickLookupMap.containsKey(messageTypeId)) {
                return quickLookupMap.getValue(messageTypeId)
            }else {
                throw java.lang.IllegalArgumentException("Unknown message type byte")
            }
        }
    }

    constructor()

    constructor(value: Int) {
        this.value = value.toByte()
    }

    fun getValue(): Byte {
        return value
    }
}