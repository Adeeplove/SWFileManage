package com.cc.fileManage.module.dex.data;

import com.cc.fileManage.module.stream.PositionInputStream;
import com.cc.fileManage.module.stream.Utils;

import java.io.IOException;

/**
 *
 * Created by xueqiulxq on 17/09/2017.
 */

public class ProtoDataItem {

    public static final int LENGTH = 12;
    public long shortyIdx;          // method proto name, like: VL, LI where V for Void, L for class, Z for boolean. Every type was denoted as a uppercase letter
    public long returnTypeIdx;      // return type
    public long parametersOff;      // parameter

    // Assistant
    public String protoStr;
    public String returnTypeStr;
    //
    public long parameterCount = 0;
    public int[] parameterIdx;      // 2B * n
    // Assistant
    public String[] parameters;

    public static ProtoDataItem parseFrom(PositionInputStream racFile, PositionInputStream s, StringPool stringPool, TypePool typePool) throws IOException {
        ProtoDataItem item = new ProtoDataItem();
        item.shortyIdx = Utils.readInt(s);
        item.returnTypeIdx = Utils.readInt(s);
        item.parametersOff = Utils.readInt(s);

        // Fill reference string
        item.protoStr = stringPool.getString(item.shortyIdx);
        item.returnTypeStr = typePool.getType(item.returnTypeIdx);

        // Read method parameters
        if (item.parametersOff != 0) {
            // Read size
            racFile.seek(item.parametersOff);
            byte[] sizeBytes = new byte[4];
            racFile.read(sizeBytes, 0, sizeBytes.length);

            long size = item.parameterCount = Utils.getInt(sizeBytes);

            // Read param string index
            byte[] idxBytes = new byte[(int) size * 2];     // ShortLen * 2
            racFile.read(idxBytes, 0, idxBytes.length);

            //
            PositionInputStream in = PositionInputStream.getInstance(idxBytes);
            int[] parameterIdx = item.parameterIdx = new int[(int) size];
            for (int i=0; i<item.parameterCount; ++i) {
                item.parameterIdx[i] = Utils.readShort(in);
            }
            item.parameters = new String[(int) size];
            for (int i=0; i<size; ++i) {
                item.parameters[i] = typePool.getType(parameterIdx[i]);
            }
        } else {
            item.parameters = new String[0];
        }
        return item;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
//        String form = "%-16s%s\n";
//        builder.append(String.format(form, "shortyIdx", PrintUtil.hex4(shortyIdx)));
//        builder.append(String.format(form, "returnTypeIdx", PrintUtil.hex4(returnTypeIdx)));
//        builder.append(String.format(form, "parametersOff", PrintUtil.hex4(parametersOff)));
        builder.append("(");
        if (parametersOff > 0) {
            for (int i=0; i<parameters.length; ++i) {
                builder.append(parameters[i]);
            }
        }
        builder.append(")");
        builder.append(returnTypeStr);
        builder.append("  proto=").append(protoStr);
        return builder.toString();
    }
}
