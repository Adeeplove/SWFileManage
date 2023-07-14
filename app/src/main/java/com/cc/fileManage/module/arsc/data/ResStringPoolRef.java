package com.cc.fileManage.module.arsc.data;

import com.cc.fileManage.module.stream.BoundedInputStream;

import java.io.IOException;

/**
 *
 * Created by xueqiulxq on 26/07/2017.
 *
 * @author bilux (i.bilux@gmail.com)
 *
 */

public class ResStringPoolRef {

    public long index;

    public static ResStringPoolRef parseFrom(BoundedInputStream mStreamer) throws IOException {
        ResStringPoolRef ref = new ResStringPoolRef();
        ref.index = mStreamer.readIntLow();
        return ref;
    }
}
