package com.cc.fileManage.module.manifest.data;

import com.cc.fileManage.module.stream.PositionInputStream;
import com.cc.fileManage.module.stream.PrintUtils;
import com.cc.fileManage.module.stream.Utils;

import java.io.IOException;

/**
 *
 * Created by xueqiulxq on 19/07/2017.
 */

public class EndTagChunk extends TagChunk {

    public static EndTagChunk parseFrom(PositionInputStream stream, StringChunk stringChunk) throws IOException {
        EndTagChunk chunk = new EndTagChunk();
        chunk.chunkType = Utils.readInt(stream);
        chunk.chunkSize = Utils.readInt(stream);
        chunk.lineNumber = Utils.readInt(stream);
        chunk.unknown = Utils.readInt(stream);
        chunk.nameSpaceUri = Utils.readInt(stream);
        chunk.name = Utils.readInt(stream);

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
