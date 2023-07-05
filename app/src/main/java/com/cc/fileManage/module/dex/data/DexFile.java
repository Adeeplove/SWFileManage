package com.cc.fileManage.module.dex.data;

import androidx.annotation.NonNull;

import com.cc.fileManage.module.stream.PositionInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * Created by xueqiulxq on 07/08/2017.
 */

public class DexFile {

    public DexHeader dexHeader;
    public StringPool stringPool;
    public TypePool typePool;
    public ProtoPool protoPool;
    public FieldPool fieldPool;
    public MethodPool methodPool;
    public ClassPool classPool;

    public void parse(PositionInputStream mStreamer) throws IOException {
        byte[] headerBytes = new byte[DexHeader.LENGTH];
        mStreamer.read(headerBytes, 0, headerBytes.length);
        //
        dexHeader = DexHeader.parseFrom(PositionInputStream.getInstance(headerBytes));

        stringPool = StringPool.parseFrom(mStreamer, dexHeader);
        typePool = TypePool.parseFrom(mStreamer, dexHeader, stringPool);
        protoPool = ProtoPool.parseFrom(mStreamer, dexHeader, stringPool, typePool);
        fieldPool = FieldPool.parseFrom(mStreamer, dexHeader, stringPool, typePool);
        methodPool = MethodPool.parseFrom(mStreamer, dexHeader, stringPool, typePool, protoPool);
        classPool = ClassPool.parseFrom(mStreamer, dexHeader, stringPool, typePool, protoPool);
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(dexHeader);
        builder.append('\n');
        builder.append(stringPool);
        builder.append('\n');
        builder.append(typePool);
        builder.append('\n');
        builder.append(protoPool);
        builder.append('\n');
        builder.append(fieldPool);
        builder.append('\n');
        builder.append(methodPool);
        builder.append('\n');
        builder.append(classPool);
        return builder.toString();
    }
}
