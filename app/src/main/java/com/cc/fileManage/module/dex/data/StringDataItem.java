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

public class StringDataItem {

    public long size;
    public String data;
    // Assistant
    public int ulebLen;

    public static StringDataItem parseFrom(PositionInputStream racFile, long offset) throws IOException {
        StringDataItem data = new StringDataItem();
        // Test the length of uleb
        racFile.seek(offset);
        byte[] leb128 = new byte[5];
        racFile.read(leb128, 0, leb128.length);

        ///
        PositionInputStream in = PositionInputStream.getInstance(leb128);
        leb128 = Utils.readULEB128Bytes(in);

        // Parse data item
        data.size = Utils.parseULEB128Int(leb128);
        racFile.seek(offset + leb128.length);   // Move cursor to the start position of string
        byte[] strBytes = new byte[(int) data.size];
        ///
        racFile.read(strBytes, 0, strBytes.length);
        data.data = new String(strBytes);
        return data;
    }

    @Override
    public String toString() {
        return String.format("size=%s\t \"%s\"", PrintUtils.hex4(size), data);
    }
}
