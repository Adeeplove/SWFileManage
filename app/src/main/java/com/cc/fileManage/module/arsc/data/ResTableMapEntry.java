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
public class ResTableMapEntry extends ResTableEntry {

    public ResTableRef parent;  // Reference parent ResTableMapEntry pkgId, if parent not exists the value should be zero.
    public long count;          // Num of ResTableMap following.
    public ResTableMap[] resTableMaps;

    public static ResTableMapEntry parseFrom(BoundedInputStream stream) throws IOException {
        ResTableMapEntry entry = new ResTableMapEntry();
        ResTableEntry.parseFrom(stream, entry);

        entry.parent = ResTableRef.parseFrom(stream);
        entry.count = stream.readIntLow();

        entry.resTableMaps = new ResTableMap[(int) entry.count];
        for (int i = 0; i < entry.count; ++i) {
            entry.resTableMaps[i] = ResTableMap.parseFrom(stream);
        }

        return entry;
    }

    @Override
    public void translateValues(ResStringPoolChunk globalStringPool,
            ResStringPoolChunk typeStringPool,
            ResStringPoolChunk keyStringPool) {
        super.translateValues(globalStringPool, typeStringPool, keyStringPool);
        for (ResTableMap resTableMap : resTableMaps) {
            resTableMap.translateValues(globalStringPool, typeStringPool, keyStringPool);
        }
    }
}
