package com.cc.fileManage.module.manifest.data;


import com.cc.fileManage.module.stream.PositionInputStream;
import com.cc.fileManage.module.stream.Utils;

import java.io.IOException;

/**
 * Created by xueqiulxq on 17/07/2017.
 */

public class ChunkInfo {

    public static final int LENGTH = 8;
    public static final int STRING_CHUNK_ID = 0x001C0001;
    public static final int RESOURCE_ID_CHUNK_ID = 0x00080180;
    public static final int START_NAMESPACE_CHUNK_ID = 0x00100100;
    public static final int START_TAG_CHUNK_ID = 0x00100102;
    public static final int EDN_TAG_CHUNK_ID = 0x00100103;
    public static final int CHUNK_ENDNS_CHUNK_ID = 0x00100101;

    public long chunkType;
    public long chunkSize;
    // Assistant
    public int chunkIndex;

    public static ChunkInfo parseFrom(PositionInputStream stream) throws IOException {
        ChunkInfo chunk = new ChunkInfo();
        chunk.chunkType = Utils.readInt(stream);
        chunk.chunkSize = Utils.readInt(stream);
        return chunk;
    }

    @Override
    public String toString() {
        return String.format("%-2d  type=%08x  size=%-6d  %s", chunkIndex, chunkType, chunkSize, getChunkType(chunkType));
    }

    private String getChunkType(long type) {
        String typeStr;
        switch ((int) type) {
            case STRING_CHUNK_ID: typeStr = "StringChunk"; break;
            case RESOURCE_ID_CHUNK_ID: typeStr = "ResourceIdChunk"; break;
            case START_NAMESPACE_CHUNK_ID: typeStr = "StartNamespaceChunk"; break;
            case START_TAG_CHUNK_ID: typeStr = "StartTagChunk"; break;
            case EDN_TAG_CHUNK_ID: typeStr = "EndTagChunk"; break;
            case CHUNK_ENDNS_CHUNK_ID: typeStr = "ChunkEndsChunk"; break;
            default: typeStr = "UnknownChunk";
        }
        return typeStr;
    }
}

