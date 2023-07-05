package com.cc.fileManage.module.dex.data;

import com.cc.fileManage.module.stream.PositionInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 *
 * Created by xueqiulxq on 17/09/2017.
 */

public class FieldPool {

    public FieldDataItem[] fields;

    public static FieldPool parseFrom(PositionInputStream racFile, DexHeader dexHeader,
                                      StringPool stringPool, TypePool typePool) throws IOException {
        FieldPool pool = new FieldPool();
        FieldDataItem[] fields = pool.fields = new FieldDataItem[(int) dexHeader.fieldIdsSize];
        byte[] itemByte = new byte[FieldDataItem.LENGTH];
        for (int i=0; i<dexHeader.fieldIdsSize; ++i) {
            racFile.seek(dexHeader.fieldIdsOff + (long) i * FieldDataItem.LENGTH);
            racFile.read(itemByte, 0, itemByte.length);
            ///
            fields[i] = FieldDataItem.parseFrom(PositionInputStream.getInstance(itemByte), stringPool, typePool);
        }
        racFile.seek(dexHeader.fieldIdsOff);
        return pool;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("-- Field pool --\n");
        for (int i=0; i<fields.length; ++i) {
            builder.append('f').append(i).append(". ").append(fields[i]).append('\n');
        }
        return builder.toString();
    }
}
