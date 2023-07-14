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

public class ResTableRef {

    public long ident;

    public static ResTableRef parseFrom(RandomInputStream.CutStream stream) throws IOException {
        ResTableRef ref = new ResTableRef();
        ref.ident = IUtils.readIntLow(stream);
        return ref;
    }

    @Override
    public String toString() {
        return String.format("%s: 0x%s", "ident", (ident));
    }
}
