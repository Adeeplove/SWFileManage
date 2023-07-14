package com.cc.fileManage.module.arsc;

import com.cc.fileManage.module.stream.IUtils;
import com.cc.fileManage.module.stream.RandomInputStream;

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

    public static ResFileHeaderChunk parseFrom(RandomInputStream.CutStream stream) throws IOException {
        ResFileHeaderChunk chunk = new ResFileHeaderChunk();
        chunk.header = ChunkHeader.parseFrom(stream);
        chunk.packageCount = IUtils.readIntLow(stream);
        return chunk;
    }
}
