package com.cc.fileManage.module.arsc.data;

import com.cc.fileManage.module.stream.Utils;
import com.cc.fileManage.module.stream.PositionInputStream;

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

    public static ResStringPoolRef parseFrom(PositionInputStream mStreamer) throws IOException {
        ResStringPoolRef ref = new ResStringPoolRef();
        ref.index = Utils.readInt(mStreamer);
        return ref;
    }
}
