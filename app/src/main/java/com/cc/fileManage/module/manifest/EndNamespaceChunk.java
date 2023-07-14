package com.cc.fileManage.module.manifest;

import androidx.annotation.NonNull;

import com.cc.fileManage.module.stream.IUtils;
import com.cc.fileManage.utils.PrintUtils;
import com.cc.fileManage.module.stream.RandomInputStream;

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

    public static EndNamespaceChunk parseFrom(RandomInputStream.CutStream stream, StringChunk stringChunk) throws IOException {
        EndNamespaceChunk chunk = new EndNamespaceChunk();
        chunk.chunkType = IUtils.readIntLow(stream);
        chunk.chunkSize = IUtils.readIntLow(stream);
        chunk.lineNumber = IUtils.readIntLow(stream);
        chunk.unknown = IUtils.readIntLow(stream);
        chunk.prefixIdx = IUtils.readIntLow(stream);
        chunk.uriIdx = IUtils.readIntLow(stream);

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
