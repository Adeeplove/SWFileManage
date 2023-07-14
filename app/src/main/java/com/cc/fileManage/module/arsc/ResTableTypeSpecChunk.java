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

public class ResTableTypeSpecChunk extends BaseTypeChunk {

    public ChunkHeader header;
    public int typeId;      // 1byte
    public int res0;    // 1byte
    public int res1;    // 2byte
    public long entryCount;
    public long[] entryConfig;

    public static ResTableTypeSpecChunk parseFrom(RandomInputStream.CutStream stream, ResStringPoolChunk stringChunk) throws IOException {
        ResTableTypeSpecChunk chunk = new ResTableTypeSpecChunk();
        chunk.header = ChunkHeader.parseFrom(stream);
        chunk.typeId = IUtils.readUInt8(stream);
        chunk.res0 = IUtils.readUInt8(stream);
        chunk.res1 = IUtils.readShortLow(stream);
        chunk.entryCount = IUtils.readIntLow(stream);
        chunk.entryConfig = new long[(int) chunk.entryCount];

        for (int i=0; i<chunk.entryCount; ++i) {
            chunk.entryConfig[i] = IUtils.readIntLow(stream);
        }
        return chunk;
    }

    @Override
    public String getChunkName() {
        return "ResTableTypeSpecChunk";
    }

    @Override
    public long getEntryCount() {
        return entryCount;
    }

    @Override
    public String getType() {
        return String.format("0x%s", (typeId));
    }

    public int getTypeId() {
        return typeId;
    }

    @Override
    public void translateValues(ResStringPoolChunk globalStringPool, ResStringPoolChunk typeStringPool, ResStringPoolChunk keyStringPool) {
        // Ignored
    }
}
