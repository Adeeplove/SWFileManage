package com.cc.fileManage.module.arsc.data;

import com.cc.fileManage.module.stream.BoundedInputStream;
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

public class ResTableRef {

    public long ident;

    public static ResTableRef parseFrom(BoundedInputStream stream) throws IOException {
        ResTableRef ref = new ResTableRef();
        ref.ident = stream.readIntLow();
        return ref;
    }

    @Override
    public String toString() {
        return String.format("%s: 0x%s", "ident", (ident));
    }
}
