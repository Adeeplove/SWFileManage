package com.cc.fileManage.module.arsc.data;

import androidx.annotation.NonNull;

import com.cc.fileManage.module.stream.BoundedInputStream;
import com.cc.fileManage.module.stream.RandomInputStream;
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
public class ChunkHeader {

    public static final int LENGTH = 2 + 2 + 4;
    public int type;
    public int headerSize;
    public long chunkSize;

    public static ChunkHeader parseFrom(BoundedInputStream stream) throws IOException {
        ChunkHeader chunk = new ChunkHeader();
        chunk.type = stream.readShortLow();
        chunk.headerSize = stream.readShortLow();
        chunk.chunkSize = stream.readIntLow();
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
