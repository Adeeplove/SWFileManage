package com.cc.fileManage.module.arsc;

import androidx.annotation.NonNull;

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
public class ChunkHeader {

    public static final int LENGTH = 2 + 2 + 4;
    public int type;
    public int headerSize;
    public long chunkSize;

    public static ChunkHeader parseFrom(RandomInputStream.CutStream stream) throws IOException {
        ChunkHeader chunk = new ChunkHeader();
        chunk.type = IUtils.readShortLow(stream);
        chunk.headerSize = IUtils.readShortLow(stream);
        chunk.chunkSize = IUtils.readIntLow(stream);
        return chunk;
    }

    @NonNull
    @Override
    public String toString() {
        return "ChunkHeader{" +
                "type=" + type +
                ", headerSize=" + headerSize +
                ", chunkSize=" + chunkSize +
                '}';
    }
}
