package com.cc.fileManage.module.arsc.data;

import androidx.annotation.NonNull;

import com.cc.fileManage.module.stream.BoundedInputStream;

import java.io.IOException;

/**
 *
 * Created by xueqiulxq on 26/07/2017.
 *
 * @author bilux (i.bilux@gmail.com)
 *
 */

public class ResTableValueEntry extends ResTableEntry {

    public ResValue resValue;       

    public static ResTableValueEntry parseFrom(BoundedInputStream stream) throws IOException {
        ResTableValueEntry entry = new ResTableValueEntry();
        ResTableEntry.parseFrom(stream, entry);
        entry.resValue = ResValue.parseFrom(stream);
        //entry.entryId = entry.resValue.data;
        //entry.entryId = (int) entry.key.index;
        return entry;
    }

    @Override
    public void translateValues(ResStringPoolChunk globalStringPool,
                                ResStringPoolChunk typeStringPool,
                                ResStringPoolChunk keyStringPool) {
        super.translateValues(globalStringPool, typeStringPool, keyStringPool);
        resValue.translateValues(globalStringPool, typeStringPool, keyStringPool);
    }
    
    @NonNull
    @Override
    public String toString() {
        return super.toString() + "name=\""+keyStr+"\"  data=\""+resValue.toString()+"\"";
    }
    
}
