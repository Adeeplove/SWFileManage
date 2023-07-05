package com.cc.fileManage.module.dex.data;

import com.cc.fileManage.module.stream.PositionInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 *
 * Created by xueqiulxq on 18/09/2017.
 */

public class ClassPool {

    public ClassDefItem[] classItems;

    public static ClassPool parseFrom(PositionInputStream racFile, DexHeader dexHeader,
                                      StringPool stringPool, TypePool typePool, ProtoPool protoPool) throws IOException {
        ClassPool pool = new ClassPool();
        ClassDefItem[] items = pool.classItems = new ClassDefItem[(int) dexHeader.classDefsSize];
        byte[] itemBytes = new byte[ClassDefItem.LENGTH];
        for (int i=0; i<items.length; ++i) {
            racFile.seek(dexHeader.classDefsOff + (long) ClassDefItem.LENGTH * i);
            racFile.read(itemBytes, 0, itemBytes.length);
            ///
            items[i] = ClassDefItem.parseFrom(racFile,
                    PositionInputStream.getInstance(itemBytes), stringPool, typePool, protoPool);
        }
        return pool;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("-- Class pool --\n");
        for (int i=0; i<classItems.length; ++i) {
            builder.append("Class #").append(i).append('\n');
            builder.append(classItems[i]);
            builder.append('\n');
        }
        return builder.toString();
    }
}
