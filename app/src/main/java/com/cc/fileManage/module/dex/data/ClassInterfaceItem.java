package com.cc.fileManage.module.dex.data;

import com.cc.fileManage.module.stream.PositionInputStream;
import com.cc.fileManage.module.stream.PrintUtils;
import com.cc.fileManage.module.stream.Utils;

import java.io.IOException;

/**
 *
 * Created by xueqiulxq on 24/09/2017.
 */

public class ClassInterfaceItem {

    public long size;                // 4B
    public int[] typeIdx;           // 2B * size
    // Assistant
    public String[] interfaceStrs;

    public static ClassInterfaceItem parseFrom(PositionInputStream racFile,
                                               StringPool stringPool, TypePool typePool, ProtoPool protoPool) throws IOException {
        ClassInterfaceItem item = new ClassInterfaceItem();

        byte[] sizeBytes = new byte[4];
        racFile.read(sizeBytes, 0, sizeBytes.length);
        //
        item.size = Utils.getInt(sizeBytes);

        int[] typeIdxs = item.typeIdx = new int[(int) item.size];
        byte[] idxBytes = new byte[typeIdxs.length * 2];
        racFile.read(idxBytes, 0, idxBytes.length);

        PositionInputStream in = PositionInputStream.getInstance(idxBytes);
        for (int i=0; i<typeIdxs.length; ++i) {
            typeIdxs[i] = Utils.readShort(in);
        }

        String[] itfStrs = item.interfaceStrs = new String[typeIdxs.length];
        for (int i=0; i<itfStrs.length; ++i) {
            itfStrs[i] = typePool.getType(typeIdxs[i]);
        }

        return item;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("-- ClassInterfaceItem --\n");

        if (interfaceStrs != null) {
            for (int i=0; i<interfaceStrs.length; ++i) {
                builder.append(String.format("\t%d. idx=%s  %s\n", i, PrintUtils.hex4(typeIdx[i]), interfaceStrs[i]));
            }
        } else {
            builder.append("\tNo any interface.\n");
        }

        return builder.toString();
    }
}
