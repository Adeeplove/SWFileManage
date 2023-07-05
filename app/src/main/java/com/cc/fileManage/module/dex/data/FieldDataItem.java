package com.cc.fileManage.module.dex.data;

import com.cc.fileManage.module.stream.PositionInputStream;
import com.cc.fileManage.module.stream.Utils;

import java.io.IOException;

/**
 *
 * Created by xueqiulxq on 17/09/2017.
 */

public class FieldDataItem {

    public static final int LENGTH = 8;

    public int classIdx;    // 2B The class index this field belongs to in type_ids
    public int typeIdx;     // 2B Field type index in type_ids
    public long nameIdx;    // 4B The name index in string_ids

    // Assistant
    public String classStr;
    public String typeStr;
    public String nameStr;

    public static FieldDataItem parseFrom(PositionInputStream s, StringPool stringPool, TypePool typePool) throws IOException {
        FieldDataItem item = new FieldDataItem();
        item.classIdx = Utils.readShort(s);
        item.typeIdx = Utils.readShort(s);
        item.nameIdx = Utils.readInt(s);

        item.classStr = typePool.getType(item.classIdx);
        item.typeStr = typePool.getType(item.typeIdx);
        item.nameStr = stringPool.getString(item.nameIdx);
        return item;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(classStr).append(" -> ").append(typeStr).append(" ").append(nameStr);
        return builder.toString();
    }
}
