package com.cc.fileManage.module.dex.data;

import androidx.annotation.NonNull;

import com.cc.fileManage.module.stream.PositionInputStream;
import com.cc.fileManage.module.stream.PrintUtils;
import com.cc.fileManage.module.stream.Utils;

import java.io.IOException;

/**
 *
 * Created by xueqiulxq on 19/09/2017.
 */

public class EncodedMethod {

    public byte[] methodIdxDiff;    // uleb128
    public byte[] accessFlags;      // uleb128
    public byte[] codeOff;          // uleb128 -> code_item {@link CodeItem}

    public CodeItem codeItem;
    // Assistant
    public long methodIdxDiffInt;
    public long accessFlagsInt;
    public long codeOffInt;

    public static EncodedMethod parseFrom(PositionInputStream racFile,
                                          StringPool stringPool, TypePool typePool, ProtoPool protoPool) throws IOException {
        EncodedMethod method = new EncodedMethod();
        method.methodIdxDiff = Utils.readULEB128BytesFrom(racFile);
        method.accessFlags = Utils.readULEB128BytesFrom(racFile);
        method.codeOff = Utils.readULEB128BytesFrom(racFile);

        method.methodIdxDiffInt = Utils.parseULEB128Int(method.methodIdxDiff);
        method.accessFlagsInt = Utils.parseULEB128Int(method.accessFlags);
        method.codeOffInt = Utils.parseULEB128Int(method.codeOff);

        if (method.codeOffInt != 0) {
            long cursor = racFile.getPosition();
            racFile.seek(method.codeOffInt);
            method.codeItem = CodeItem.parseFrom(racFile, stringPool, typePool, protoPool);
            racFile.seek(cursor);
        }
        return method;
    }

    public int getLength() {
        return methodIdxDiff.length + accessFlags.length + codeOff.length;
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        String form2 = "%-20s0x%s\n";
        String form3 = "%-20s0x%s \t%s\n";
        builder.append(String.format(form2, "methodIdxDiff", PrintUtils.hex(methodIdxDiff)));
        builder.append(String.format(form3, "accessFlags", PrintUtils.hex(accessFlags), AccessFlags.accMethodStr(accessFlagsInt)));
        builder.append(String.format(form3, "codeOff", PrintUtils.hex(codeOff), "-> CodeItem"));
        if (codeItem != null) {
            String codeItemStr = codeItem.toString();
            builder.append("\t").append(codeItemStr.trim().replace("\n", "\n\t")).append('\n');
        } else {
            builder.append("\tCodeItem: ").append("Not implemented.").append('\n');
        }
        return builder.toString();
    }
}
