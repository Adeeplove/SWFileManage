package com.cc.fileManage.module.dex.data;

import com.cc.fileManage.module.stream.PositionInputStream;
import com.cc.fileManage.module.stream.PrintUtils;
import com.cc.fileManage.module.stream.Utils;

import java.io.IOException;

/**
 *
 * Created by xueqiulxq on 19/09/2017.
 */

public class EncodedField {

    public byte[] fieldIdxDiff;     // uleb128: index into field_ids for ID of this filed
    public byte[] accessFlags;      // uleb128: access flags like public, static etc.
    // Assistant
    public long fieldIdxDiffInt;
    public long accessFlagsInt;

    public static EncodedField parseFrom(PositionInputStream racFile,
                                         StringPool stringPool, TypePool typePool, ProtoPool protoPool) throws IOException {
        EncodedField field = new EncodedField();
        field.fieldIdxDiff = Utils.readULEB128BytesFrom(racFile);
        field.accessFlags = Utils.readULEB128BytesFrom(racFile);

        field.fieldIdxDiffInt = Utils.parseULEB128Int(field.fieldIdxDiff);
        field.accessFlagsInt = Utils.parseULEB128Int(field.accessFlags);
        return field;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        String form3 = "%-20s0x%s \t%s\n";
        builder.append(String.format(form3, "fieldIdxDiff", PrintUtils.hex(fieldIdxDiff), ""));
        builder.append(String.format(form3, "accessFlags", PrintUtils.hex(accessFlags), AccessFlags.accFieldStr(accessFlagsInt)));
        return builder.toString();
    }
}
