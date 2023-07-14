package com.cc.fileManage.module.arsc;

import com.cc.fileManage.module.stream.IUtils;
import com.cc.fileManage.module.stream.RandomInputStream;

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

    public static ResStringPoolRef parseFrom(RandomInputStream.CutStream stream) throws IOException {
        ResStringPoolRef ref = new ResStringPoolRef();
        ref.index = IUtils.readIntLow(stream);
        return ref;
    }
}
