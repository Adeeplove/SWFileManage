package com.cc.fileManage.module.dex.data;

import com.cc.fileManage.module.stream.PositionInputStream;
import com.cc.fileManage.module.stream.PrintUtils;
import com.cc.fileManage.module.stream.Utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 *
 * Created by xueqiulxq on 17/09/2017.
 */

public class StringPool {

    public long[] stringDataOffsets;
    public StringDataItem[] stringDataItems;

    public static StringPool parseFrom(PositionInputStream racFile, DexHeader dexHeader) throws IOException {

        byte[] bodyBytes = new byte[(int)dexHeader.stringIdsSize * 4];
        racFile.seek(dexHeader.stringIdsOff);
        racFile.read(bodyBytes, 0, bodyBytes.length);

        //
        PositionInputStream in = PositionInputStream.getInstance(bodyBytes);
        ///
        StringPool pool = new StringPool();
        long[] offsets = pool.stringDataOffsets = new long[(int) dexHeader.stringIdsSize];
        for (int i=0; i<dexHeader.stringIdsSize; ++i) {
            offsets[i] = Utils.readInt(in);
        }

        StringDataItem[] items = pool.stringDataItems = new StringDataItem[offsets.length];
        for (int i=0; i<offsets.length; ++i) {
            items[i] = StringDataItem.parseFrom(racFile, offsets[i]);
        }
        return pool;
    }

    public String getString(long idx) {
        return idx < stringDataItems.length ? stringDataItems[(int) idx].data : null;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("-- String pool --\n");
        for (int i=0; i<stringDataOffsets.length; ++i) {
            builder.append(String.format("s%d. offsets=%s\t %s\n", i, PrintUtils.hex4(stringDataOffsets[i]), stringDataItems[i].toString()));
        }
        return builder.toString();
    }
}
