package com.cc.fileManage.module.dex.data;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import com.cc.fileManage.module.stream.PositionInputStream;
import com.cc.fileManage.module.stream.PrintUtils;
import com.cc.fileManage.module.stream.Utils;

import java.io.IOException;

/**
 *
 * Created by xueqiulxq on 10/08/2017.
 */

public class DexHeader {

    public static final int LENGTH = 0x70;

    public byte[] magic;        // 8B
    public long checksum;       // 4B
    public byte[] signature;    // 20B
    public long fileSize;       // 4B
    public long headerSize;     // 4B
    public long endianTag;      // 4B
    public long linkSize;       // 4B
    public long linkOff;        // 4B
    public long mapOff;         // 4B

    public long stringIdsSize;  // 4B
    public long stringIdsOff;   // 4B
    public long typeIdsSize;    // 4B
    public long typeIdsOff;     // 4B
    public long protoIdsSize;   // 4B
    public long protoIdsOff;    // 4B
    public long fieldIdsSize;   // 4B
    public long fieldIdsOff;    // 4B
    public long methodIdsSize;  // 4B
    public long methodIdsOff;   // 4B
    public long classDefsSize;  // 4B
    public long classDefsOff;   // 4B
    public long dataSize;       // 4B
    public long dataOff;        // 4B

    public static DexHeader parseFrom(PositionInputStream s) throws IOException {
        DexHeader dh = new DexHeader();
        dh.magic = Utils.read(s,8);
        dh.checksum = Utils.readInt(s);
        dh.signature = Utils.read(s, 20);
        dh.fileSize = Utils.readInt(s);
        dh.headerSize = Utils.readInt(s);
        dh.endianTag = Utils.readInt(s);
        dh.linkSize = Utils.readInt(s);
        dh.linkOff = Utils.readInt(s);
        dh.mapOff = Utils.readInt(s);

        dh.stringIdsSize = Utils.readInt(s);
        dh.stringIdsOff = Utils.readInt(s);
        dh.typeIdsSize = Utils.readInt(s);
        dh.typeIdsOff = Utils.readInt(s);
        dh.protoIdsSize = Utils.readInt(s);
        dh.protoIdsOff = Utils.readInt(s);
        dh.fieldIdsSize = Utils.readInt(s);
        dh.fieldIdsOff = Utils.readInt(s);
        dh.methodIdsSize = Utils.readInt(s);
        dh.methodIdsOff = Utils.readInt(s);
        dh.classDefsSize = Utils.readInt(s);
        dh.classDefsOff = Utils.readInt(s);
        dh.dataSize = Utils.readInt(s);
        dh.dataOff = Utils.readInt(s);
        return dh;
    }

    @SuppressLint("DefaultLocale")
    @NonNull
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        String form = "|%-14s |0x%s\n";
        String form3 = "|%-14s |0x%s     |%d\n";

        builder.append("-- Dex File Header --\n");
        builder.append("|    Fields     |     Hex       |   Decimal\n");
        builder.append(String.format(form, "magic", PrintUtils.hex(magic)));
        builder.append(String.format(form, "checksum", PrintUtils.hex4(checksum)));
        builder.append(String.format(form, "signature", PrintUtils.hex(signature)));
        builder.append(String.format(form3, "fileSize", PrintUtils.hex4(fileSize), fileSize));
        builder.append(String.format(form3, "headerSize", PrintUtils.hex4(headerSize), headerSize));
        builder.append(String.format(form3, "endianTag", PrintUtils.hex4(endianTag), endianTag));
        builder.append(String.format(form3, "linkSize", PrintUtils.hex4(linkSize), linkSize));
        builder.append(String.format(form3, "linkOff", PrintUtils.hex4(linkOff), linkOff));
        builder.append(String.format(form3, "mapOff", PrintUtils.hex4(mapOff), mapOff));

        builder.append(String.format(form3, "stringIdsSize", PrintUtils.hex4(stringIdsSize), stringIdsSize));
        builder.append(String.format(form3, "stringIdsOff", PrintUtils.hex4(stringIdsOff), stringIdsOff));
        builder.append(String.format(form3, "typeIdsSize", PrintUtils.hex4(typeIdsSize), typeIdsSize));
        builder.append(String.format(form3, "typeIdsOff", PrintUtils.hex4(typeIdsOff), typeIdsOff));
        builder.append(String.format(form3, "protoIdsSize", PrintUtils.hex4(protoIdsSize), protoIdsSize));
        builder.append(String.format(form3, "protoIdsOff", PrintUtils.hex4(protoIdsOff), protoIdsOff));
        builder.append(String.format(form3, "fieldIdsSize", PrintUtils.hex4(fieldIdsSize), fieldIdsSize));
        builder.append(String.format(form3, "fieldIdsOff", PrintUtils.hex4(fieldIdsOff), fieldIdsOff));
        builder.append(String.format(form3, "methodIdsSize", PrintUtils.hex4(methodIdsSize), methodIdsSize));
        builder.append(String.format(form3, "methodIdsOff", PrintUtils.hex4(methodIdsOff), methodIdsOff));
        builder.append(String.format(form3, "classDefsSize", PrintUtils.hex4(classDefsSize), classDefsSize));
        builder.append(String.format(form3, "classDefsOff", PrintUtils.hex4(classDefsOff), classDefsOff));
        builder.append(String.format(form3, "dataSize", PrintUtils.hex4(dataSize), dataSize));
        builder.append(String.format(form3, "dataOff", PrintUtils.hex4(dataOff), dataOff));

        return builder.toString();
    }
}
