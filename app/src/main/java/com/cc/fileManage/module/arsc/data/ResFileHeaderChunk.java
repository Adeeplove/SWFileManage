package com.cc.fileManage.module.arsc.data;

import com.cc.fileManage.module.stream.Utils;
import com.cc.fileManage.module.stream.PositionInputStream;

import java.io.IOException;

/**
 *
 * Created by xueqiulxq on 26/07/2017.
 *
 * @author bilux (i.bilux@gmail.com)
 *
 */

public class ResFileHeaderChunk {

    public static final int LENGTH = 12;

    public ChunkHeader header;
    public long packageCount;

    public static ResFileHeaderChunk parseFrom(PositionInputStream mStreamer) throws IOException {
        ResFileHeaderChunk chunk = new ResFileHeaderChunk();
        chunk.header = ChunkHeader.parseFrom(mStreamer);
        chunk.packageCount = Utils.readInt(mStreamer);
        return chunk;
    }
}
