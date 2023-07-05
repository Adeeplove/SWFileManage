package com.cc.fileManage.module.manifest.data;

import androidx.annotation.NonNull;

import com.cc.fileManage.module.stream.PositionInputStream;
import com.cc.fileManage.module.stream.PrintUtils;
import com.cc.fileManage.module.stream.Utils;

import java.io.IOException;

/**
 *
 * Created by xueqiulxq on 24/07/2017.
 */

public class EndNamespaceChunk {

    public long chunkType;
    public long chunkSize;
    public long lineNumber;
    public long unknown;
    public long prefixIdx;
    public long uriIdx;

    // Assistant
    public String prefixStr;
    public String uriStr;

    public static EndNamespaceChunk parseFrom(PositionInputStream stream, StringChunk stringChunk) throws IOException {
        EndNamespaceChunk chunk = new EndNamespaceChunk();
        chunk.chunkType = Utils.readInt(stream);
        chunk.chunkSize = Utils.readInt(stream);
        chunk.lineNumber = Utils.readInt(stream);
        chunk.unknown = Utils.readInt(stream);
        chunk.prefixIdx = Utils.readInt(stream);
        chunk.uriIdx = Utils.readInt(stream);

        chunk.prefixStr = stringChunk.getString((int) chunk.prefixIdx);
        chunk.uriStr = stringChunk.getString((int) chunk.uriIdx);
        return chunk;
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(128);
        String form2 = "%-16s %s\n";
        String form3 = "%-16s %s   %s\n";

        builder.append("-- EndNamespace Chunk --").append('\n');
        builder.append(String.format(form2, "chunkType", PrintUtils.hex4(chunkType)));
        builder.append(String.format(form2, "chunkSize", PrintUtils.hex4(chunkSize)));
        builder.append(String.format(form2, "lineNumber", PrintUtils.hex4(lineNumber)));
        builder.append(String.format(form2, "unknown", PrintUtils.hex4(unknown)));
        builder.append(String.format(form3, "prefixIdx", PrintUtils.hex4(prefixIdx), prefixStr));
        builder.append(String.format(form3, "uriIdx", PrintUtils.hex4(uriIdx), uriStr));
        return builder.toString();
    }
}
