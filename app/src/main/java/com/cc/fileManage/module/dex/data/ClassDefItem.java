package com.cc.fileManage.module.dex.data;

import androidx.annotation.NonNull;

import com.cc.fileManage.module.stream.PositionInputStream;
import com.cc.fileManage.module.stream.PrintUtils;
import com.cc.fileManage.module.stream.Utils;

import java.io.IOException;

/**
 *
 * Created by xueqiulxq on 18/09/2017.
 */

public class ClassDefItem {

    public static final int LENGTH = 4 * 8;

    public long classIdx;
    public long accessFlags;
    public long superClassIdx;
    public long interfacesOff;      // -> type_list
    public long sourceFileIdx;
    public long annotationsOff;     // -> annotations_directory_item
    public long classDataOff;       // -> class_data_item  {@link ClassDataItem}
    public long staticValueOff;     // -> encoded_array_item
    // Assistant
    public String classStr;
    public String superClassStr;
    public String sourceFileStr;

    public ClassInterfaceItem interfaceItem;
    public ClassDataItem dataItem;

    public static ClassDefItem parseFrom(PositionInputStream racFile, PositionInputStream s,
                                         StringPool stringPool, TypePool typePool, ProtoPool protoPool) throws IOException {
        ClassDefItem item = new ClassDefItem();

        item.classIdx = Utils.readInt(s);
        item.accessFlags = Utils.readInt(s);
        item.superClassIdx = Utils.readInt(s);
        item.interfacesOff = Utils.readInt(s);
        item.sourceFileIdx = Utils.readInt(s);
        item.annotationsOff = Utils.readInt(s);
        item.classDataOff = Utils.readInt(s);
        item.staticValueOff = Utils.readInt(s);

        item.classStr = typePool.getType(item.classIdx);
        item.superClassStr = typePool.getType(item.superClassIdx);
        item.sourceFileStr = stringPool.getString(item.sourceFileIdx);

        racFile.seek(item.classDataOff);
        item.dataItem = ClassDataItem.parseFrom(racFile, stringPool, typePool, protoPool);

        if (item.interfacesOff != 0) {
            racFile.seek(item.interfacesOff);
            item.interfaceItem = ClassInterfaceItem.parseFrom(racFile, stringPool, typePool, protoPool);
        } else {
            item.interfaceItem = new ClassInterfaceItem();
        }

        return item;
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        String form3 = "%-16s%-16s%s\n";
        builder.append(String.format(form3, "classIdx", PrintUtils.hex4(classIdx), classStr));
        builder.append(String.format(form3, "accessFlags", PrintUtils.hex4(accessFlags), AccessFlags.accClassStr(accessFlags)));
        builder.append(String.format(form3, "superClassIdx", PrintUtils.hex4(superClassIdx), superClassStr));
        builder.append(String.format(form3, "interfacesOff", PrintUtils.hex4(interfacesOff), "-> ClassInterfaceItem"));
        builder.append(String.format(form3, "sourceFileIdx", PrintUtils.hex4(sourceFileIdx), sourceFileStr));
        builder.append(String.format(form3, "annotationsOff", PrintUtils.hex4(annotationsOff), "->"));
        builder.append(String.format(form3, "classDataOff", PrintUtils.hex4(classDataOff), "-> ClassDataItem"));
        builder.append(String.format(form3, "staticValueOff", PrintUtils.hex4(staticValueOff), "->"));

        String itfStr = interfaceItem.toString();
        builder.append(PrintUtils.indent(itfStr));
        String dataItemStr = dataItem.toString();
        builder.append(PrintUtils.indent(dataItemStr));

        return builder.toString();
    }
}
