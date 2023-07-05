package com.cc.fileManage.module.manifest.data;

import androidx.annotation.NonNull;

import com.cc.fileManage.module.stream.PositionInputStream;
import com.cc.fileManage.module.stream.PrintUtils;
import com.cc.fileManage.module.stream.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * Created by xueqiulxq on 17/07/2017.
 */

public class StartTagChunk extends TagChunk {

    public long flags;          // Flags indicating start tag or end tag.
    public long attributeCount; // Count of attributes in tag
    public long classAttribute;
    public List<AttributeEntry> attributes;

    public static StartTagChunk parseFrom(PositionInputStream stream, StringChunk stringChunk) throws IOException {
        StartTagChunk chunk = new StartTagChunk();
        chunk.chunkType = Utils.readInt(stream);
        chunk.chunkSize = Utils.readInt(stream);
        chunk.lineNumber = Utils.readInt(stream);
        chunk.unknown = Utils.readInt(stream);
        chunk.nameSpaceUri = Utils.readInt(stream);
        chunk.name = Utils.readInt(stream);
        chunk.flags = Utils.readInt(stream);
        chunk.attributeCount = Utils.readInt(stream);
        chunk.classAttribute = Utils.readInt(stream);

        chunk.attributes = new ArrayList<>((int) chunk.attributeCount);
        for (int i=0; i<chunk.attributeCount; ++i) {
            chunk.attributes.add(AttributeEntry.parseFrom(stream, stringChunk));
        }

        // Fill data
        chunk.nameSpaceUriStr = stringChunk.getString((int) chunk.nameSpaceUri);
        chunk.nameStr = stringChunk.getString((int) chunk.name);
        return chunk;
    }

    @NonNull
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
        builder.append(String.format(form2, "flags", PrintUtils.hex4(flags)));
        builder.append(String.format(form2, "attributeCount", PrintUtils.hex4(attributeCount)));
        builder.append(String.format(form2, "classAttribute", PrintUtils.hex4(classAttribute)));
        for (int i=0; i<attributeCount; ++i) {
            builder.append(" <AttributeEntry.").append(i).append(" />").append('\n');
            builder.append(attributes.get(i));
        }
        return builder.toString();
    }
}
