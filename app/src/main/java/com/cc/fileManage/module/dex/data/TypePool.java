package com.cc.fileManage.module.dex.data;

import androidx.annotation.NonNull;

import com.cc.fileManage.module.stream.PositionInputStream;
import com.cc.fileManage.module.stream.PrintUtils;
import com.cc.fileManage.module.stream.Utils;

import java.io.IOException;

/**
 *
 * Created by xueqiulxq on 17/09/2017.
 */

public class TypePool {

    public int[] descriptorIdx;     // index in StringPool
    public String[] descriptors;    // String value of type

    public static TypePool parseFrom(PositionInputStream racFile, DexHeader dexHeader, StringPool stringPool) throws IOException {
        TypePool pool = new TypePool();
        racFile.seek(dexHeader.typeIdsOff);
        byte[] content = new byte[(int) dexHeader.typeIdsSize * 4];
        racFile.read(content, 0, content.length);

        //
        PositionInputStream in = PositionInputStream.getInstance(content);
        int[] descIdxs = pool.descriptorIdx = new int[(int) dexHeader.typeIdsSize];
        for (int i=0; i<dexHeader.typeIdsSize; ++i) {
            descIdxs[i] = (int) Utils.readInt(in);
        }

        String[] descStrs = pool.descriptors = new String[descIdxs.length];
        for (int i=0; i<descStrs.length; ++i) {
            descStrs[i] = stringPool.getString(descIdxs[i]);
        }
        return pool;
    }

    public String getType(long idx) {
        return idx < descriptors.length ? descriptors[(int) idx] : null;
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("-- Type pool --\n");
        for (int i=0; i<descriptorIdx.length; ++i) {
            builder.append(String.format("t%d. idx=%s  \"%s\"\n", i, PrintUtils.hex4(descriptorIdx[i]), descriptors[i]));
        }
        return builder.toString();
    }
}
