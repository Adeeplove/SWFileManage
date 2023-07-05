package com.cc.fileManage.module.manifest.data;

import androidx.annotation.NonNull;

import com.cc.fileManage.module.stream.PositionInputStream;
import com.cc.fileManage.module.stream.PrintUtils;
import com.cc.fileManage.module.stream.Utils;

import java.io.IOException;

/**
 *
 * Created by xueqiulxq on 16/07/2017.
 */

public class ResourceIdChunk {

    public long chunkType;
    public long chunkSize;
    public long[] resourceIds;
    // Assistant
    public int numIds = 0;

    public static ResourceIdChunk parseFrom(PositionInputStream stream) throws IOException {
        ResourceIdChunk chunk = new ResourceIdChunk();
        chunk.chunkType = Utils.readInt(stream);
        chunk.chunkSize = Utils.readInt(stream);
        chunk.numIds = (int)((chunk.chunkSize - 8) / 4);
        chunk.resourceIds = new long[chunk.numIds];
        for (int i=0; i<chunk.numIds; ++i) {
            chunk.resourceIds[i] = Utils.readInt(stream);
        }
        return chunk;
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(512);
        String formH = "%-16s %s\n";

        builder.append("-- ResourceId Chunk --").append('\n');
        builder.append(String.format(formH, "chunkType", PrintUtils.hex4(chunkType)));
        builder.append(String.format(formH, "chunkSize", PrintUtils.hex4(chunkSize)));

        builder.append("|----|------------|----------").append('\n');
        builder.append("| Id |     hex    |    dec   ").append('\n');
        builder.append("|----|------------|----------").append('\n');
        String formC = "|%-4d| 0x%-8s | %8d\n";
        for (int i=0; i<numIds; ++i) {
            builder.append(String.format(formC, i, PrintUtils.hex4(resourceIds[i]), resourceIds[i]));
        }

        return builder.toString();
    }
}
