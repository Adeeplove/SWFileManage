package com.cc.fileManage.module.manifest.data;

import androidx.annotation.NonNull;

import com.cc.fileManage.module.stream.PositionInputStream;
import com.cc.fileManage.module.stream.PrintUtils;
import com.cc.fileManage.module.stream.Utils;

import java.io.IOException;


/**
 *
 * Created by xueqiulxq on 16/07/2017.
 */

public class MfHeader {

    public static final int LENGTH = 8;
    public long magicNumber;
    public long fileLength;

    public static MfHeader parseFrom(PositionInputStream stream) throws IOException {
        MfHeader header = new MfHeader();
        header.magicNumber = Utils.readInt(stream);
        header.fileLength = Utils.readInt(stream);
        return header;
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("-- File Header --").append('\n');
        builder.append("Magic Number: ").append(PrintUtils.hex4(magicNumber)).append('\n');
        builder.append("File Length: ").append(PrintUtils.hex4(fileLength)).append('\n');
        return builder.toString();
    }
}
