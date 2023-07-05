package com.cc.fileManage.module.manifest.data;

import androidx.annotation.NonNull;

import com.cc.fileManage.module.stream.PositionInputStream;
import com.cc.fileManage.module.stream.PrintUtils;
import com.cc.fileManage.module.stream.Utils;

import java.io.IOException;

/**
 *
 * Created by xueqiulxq on 19/07/2017.
 */

public class AttributeEntry {

    public long namespaceUri;
    public long name;
    public long valueString;
    public long type;
    public long data;

    // Assistant
    public String namespaceUriStr;
    public String nameStr;
    public String valueStringStr;
    public String typeStr;
    public String dataStr;

    public static AttributeEntry parseFrom(PositionInputStream stream, StringChunk stringChunk) throws IOException {
        AttributeEntry entry = new AttributeEntry();
        entry.namespaceUri = Utils.readInt(stream);
        entry.name = Utils.readInt(stream);
        entry.valueString = Utils.readInt(stream);
        entry.type = Utils.readInt(stream) >> 24;
        entry.data = Utils.readInt(stream);
        // Fill data
        entry.namespaceUriStr = stringChunk.getString((int) entry.namespaceUri);
        entry.nameStr = stringChunk.getString((int) entry.name);
        entry.valueStringStr = stringChunk.getString((int) entry.valueString);
        entry.typeStr = AttributeType.getAttributeType(entry);
        entry.dataStr = AttributeType.getAttributeData(entry, stringChunk);
        return entry;
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(128);
        String form3 = "%-16s %-16s %s\n";
        builder.append(String.format(form3, "namespaceUri", PrintUtils.hex4(namespaceUri), namespaceUriStr));
        builder.append(String.format(form3, "name", PrintUtils.hex4(name), nameStr));
        builder.append(String.format(form3, "valueString", PrintUtils.hex4(valueString), valueStringStr));
        builder.append(String.format(form3, "type", PrintUtils.hex4(type), typeStr));
        builder.append(String.format(form3, "data", PrintUtils.hex4(data), dataStr));
        return builder.toString();
    }
}
