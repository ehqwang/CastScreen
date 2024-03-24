package com.example.myapplication.screen

class Video: ContentData {

    constructor(header: ChunkHeader?): super(header) {

    }

    constructor():super(ChunkHeader(ChunkType.TYPE_0_FULL,
        SessionInfo.RTMP_VIDEO_CHANNEL.toInt(), MessageType.VIDEO)) {
    }
}