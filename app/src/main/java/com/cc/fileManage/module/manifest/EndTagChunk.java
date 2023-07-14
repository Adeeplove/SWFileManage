package com.cc.fileManage.module.manifest;

import com.cc.fileManage.module.stream.IUtils;
import com.cc.fileManage.utils.PrintUtils;
import com.cc.fileManage.module.stream.RandomInputStream;

import java.io.IOException;

/**
 *
 * Created by xueqiulxq on 19/07/2017.
 */

public class EndTagChunk extends TagChunk {

    public static EndTagChunk parseFrom(RandomInputStream.CutStream stream, StringChunk stringChunk) throws IOException {
        EndTagChunk chunk = new EndTagChunk();
        chunk.chunkType = IUtils.readIntLow(stream);
        chunk.chunkSize = IUtils.readIntLow(stream);
        chunk.lineNumber = IUtils.readIntLow(stream);
        chunk.unknown = IUtils.readIntLow(stream);
        chunk.nameSpaceUri = IUtils.readIntLow(stream);
        chunk.name = IUtils.readIntLow(stream);

        // Fill data
        chunk.nameSpaceUriStr = stringChunk.getString((int) chunk.nameSpaceUri);
        chunk.nameStr = stringChunk.getString((int) chunk.name);
        return chunk;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(512);
        String form2 = "%-16s %s\n";
        String form3 = "%-16s %-16s %s\n";

        builder.append(String.format(form2, "chunkType", PrintUtils.hex4(chunkType)));
        builder.append(String.format(form2, "chunkSize", PrintUtils.hex4(chunkSize)));
        builder.append(String.format(form2, "lineNumber", PrintUtils.hex4(lineNumber)));
        builder.append(String.format(form2, "unknown", PrintUtils.hex4(unknown)));
        builder.append(String.format(form3, "nameSpaceUri", PrintUtils.hex4(nameSpaceUri), nameSpaceUriStr));
        builder.append(String.format(form3, "name", PrintUtils.hex4(name), nameStr));
        return builder.toString();
    }
}
