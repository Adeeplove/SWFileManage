package com.cc.fileManage.module.dex.data;

import com.cc.fileManage.module.stream.PositionInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 *
 * Created by xueqiulxq on 17/09/2017.
 */

public class ProtoPool {

    public ProtoDataItem[] protoItems;

    public static ProtoPool parseFrom(PositionInputStream racFile,
                                      DexHeader dexHeader, StringPool stringPool, TypePool typePool) throws IOException {
        ProtoPool pool = new ProtoPool();

        ProtoDataItem[] items = pool.protoItems = new ProtoDataItem[(int) dexHeader.protoIdsSize];
        byte[] itemBytes = new byte[ProtoDataItem.LENGTH];
        for (int i=0; i<dexHeader.protoIdsSize; ++i) {
            long offset = dexHeader.protoIdsOff + (long) ProtoDataItem.LENGTH * i;
            racFile.seek(offset);
            racFile.read(itemBytes, 0, itemBytes.length);
            items[i] = ProtoDataItem.parseFrom(racFile,
                    PositionInputStream.getInstance(itemBytes), stringPool, typePool);
        }

        return pool;
    }

    public ProtoDataItem getProto(long idx) {
        return idx < protoItems.length ? protoItems[(int) idx] : null;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("-- Proto pool --\n");
        for (int i=0; i<protoItems.length; ++i) {
            builder.append('p').append(i).append(". ").append(protoItems[i]).append('\n');
        }
        return builder.toString();
    }
}
