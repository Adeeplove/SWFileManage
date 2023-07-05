package com.cc.fileManage.module.manifest.data;

import androidx.annotation.NonNull;

import com.cc.fileManage.module.stream.PositionInputStream;
import com.cc.fileManage.module.stream.PrintUtils;
import com.cc.fileManage.module.stream.Utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * Created by xueqiulxq on 16/07/2017.
 */

public class StartNamespaceChunk {

    public long chunkType;
    public long chunkSize;
    public long lineNumber;
    public long unknown;
    public long prefixIdx;
    public long uriIdx;

    public String prefixStr;
    public String uriStr;
    public Map<String, String> uri2prefixMap;
    public Map<String, String> prefix2UriMap;

    public static StartNamespaceChunk parseFrom(PositionInputStream stream, StringChunk stringChunk) throws IOException {
        StartNamespaceChunk chunk = new StartNamespaceChunk();
        // Meta data
        chunk.chunkType = Utils.readInt(stream);
        chunk.chunkSize = Utils.readInt(stream);
        chunk.lineNumber = Utils.readInt(stream);
        chunk.unknown = Utils.readInt(stream);
        chunk.prefixIdx = Utils.readInt(stream);
        chunk.uriIdx = Utils.readInt(stream);
        // Fill data
        chunk.prefixStr = stringChunk.getString((int) chunk.prefixIdx);
        chunk.uriStr = stringChunk.getString((int) chunk.uriIdx);
        chunk.uri2prefixMap = new HashMap<>();
        chunk.prefix2UriMap = new HashMap<>();
        chunk.uri2prefixMap.put(chunk.uriStr, chunk.prefixStr);
        chunk.prefix2UriMap.put(chunk.prefixStr, chunk.uriStr);
        return chunk;
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(256);
        String form2 = "%-16s %s\n";
        String form3 = "%-16s %s   %s\n";

        builder.append("-- StartNamespace Chunk --").append('\n');
        builder.append(String.format(form2, "chunkType", PrintUtils.hex4(chunkType)));
        builder.append(String.format(form2, "chunkSize", PrintUtils.hex4(chunkSize)));
        builder.append(String.format(form2, "lineNumber", PrintUtils.hex4(lineNumber)));
        builder.append(String.format(form2, "unknown", PrintUtils.hex4(unknown)));
        builder.append(String.format(form3, "prefixIdx", PrintUtils.hex4(prefixIdx), prefixStr));
        builder.append(String.format(form3, "uriIdx", PrintUtils.hex4(uriIdx), uriStr));

        builder.append("--------------------------\n");
        for (Map.Entry<String, String> entry : prefix2UriMap.entrySet()) {
            builder.append("xmlns:").append(entry.getKey()).append("=").append(entry.getValue()).append('\n');
        }
        return builder.toString();
    }
}
