package com.cc.fileManage.module.manifest;

import androidx.annotation.NonNull;

import com.cc.fileManage.module.stream.IUtils;
import com.cc.fileManage.utils.PrintUtils;
import com.cc.fileManage.module.stream.RandomInputStream;

import java.io.IOException;


/**
 *
 * Created by xueqiulxq on 16/07/2017.
 */

public class MfHeader {

    public static final int LENGTH = 8;
    public long magicNumber;
    public long fileLength;

    public static MfHeader parseFrom(RandomInputStream.CutStream stream) throws IOException {
        MfHeader header = new MfHeader();
        header.magicNumber = IUtils.readIntLow(stream);
        header.fileLength = IUtils.readIntLow(stream);
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
