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
public class ResTableConfig {

    public long size;    // 4    size of config object

    // union 4bytes
    public int mcc, mnc; // 2 + 2
    public long imsi;    // 4

    // union 4bytes
    public int language, country;   // 2 + 2
    public long locale;             // 4

    // union 4bytes
    public int orientation, touchScreen, density;   // 1 + 1 + 2
    public long screenType;                         // 4

    // union 4bytes
    public int keyboard, navigation, inputFlags, inputPad0; // 1 + 1 + 1 + 1
    public long input;                                      // 4

    // union 4bytes
    public int screenWidth, screenHeight;   // 2 + 2
    public long screenSize;                 // 4

    // union 4bytes
    public int sdkVersion, minorVersion;    // 2 + 2
    public long version;                    // 4

    // union 4bytes
    public int screenLayout, uiModeByte, smallestScreenWidthDp; // 1 + 1 + 2
    public long screenConfig;                                   // 4

    // union 4bytes
    public int screenWidthDp, screenHeightDp;   // 2 + 2
    public long screenSizeDp;                   // 4

    public byte[] localeScript;   // 4
    public byte[] localeVariant;  // 8

    public static ResTableConfig parseFrom(RandomInputStream.CutStream stream) throws IOException {
        ResTableConfig config = new ResTableConfig();
        long cursor = stream.getPointer();
        long start = cursor;

        config.size = IUtils.readIntLow(stream);
        cursor += 4;

        config.mcc = IUtils.readShortLow(stream);
        config.mnc = IUtils.readShortLow(stream);
        stream.seek(cursor);     // Reset cursor to get union value.
        config.imsi = IUtils.readIntLow(stream);
        cursor += 4;

        config.language = IUtils.readShortLow(stream);
        config.country = IUtils.readShortLow(stream);
        stream.seek(cursor);
        config.locale = IUtils.readIntLow(stream);
        cursor += 4;

        config.orientation = IUtils.readUInt8(stream);
        config.touchScreen = IUtils.readUInt8(stream);
        config.density = IUtils.readShortLow(stream);
        stream.seek(cursor);
        config.screenType = IUtils.readIntLow(stream);
        cursor += 4;

        config.keyboard = IUtils.readUInt8(stream);
        config.navigation = IUtils.readUInt8(stream);
        config.inputFlags = IUtils.readUInt8(stream);
        config.inputPad0 = IUtils.readUInt8(stream);
        stream.seek(cursor);
        config.input = IUtils.readIntLow(stream);
        cursor += 4;

        config.screenWidth = IUtils.readShortLow(stream);
        config.screenHeight = IUtils.readShortLow(stream);
        stream.seek(cursor);
        config.screenSize = IUtils.readIntLow(stream);
        cursor += 4;

        config.sdkVersion = IUtils.readShortLow(stream);
        config.minorVersion = IUtils.readShortLow(stream);
        stream.seek(cursor);
        config.version = IUtils.readIntLow(stream);
        cursor += 4;

        config.screenLayout = IUtils.readUInt8(stream);
        config.uiModeByte = IUtils.readUInt8(stream);
        config.smallestScreenWidthDp = IUtils.readShortLow(stream);
        stream.seek(cursor);
        config.screenConfig = IUtils.readIntLow(stream);
        cursor += 4;

        config.screenWidthDp = IUtils.readShortLow(stream);
        config.screenHeightDp = IUtils.readShortLow(stream);
        stream.seek(cursor);
        config.screenSizeDp = IUtils.readIntLow(stream);
        {
            byte[] buf;
            buf = new byte[4];
            stream.read(buf);
            config.localeScript = buf;
            buf = new byte[8];
            stream.read(buf);
            config.localeVariant = buf;
        }
        stream.seek(start + config.size);
        return config;
    }

    public String getDensityString() {
        String ver = this.sdkVersion == 0 ? "" : "-v"+this.sdkVersion;
        switch(this.density) {
            case 0:
                return "";
            case 120:
                return "-ldpi" + ver;
            case 160:
                return "-mdpi" + ver;
            case 213:
                return "-tvdpi" + ver;
            case 240:
                return "-hdpi" + ver;
            case 320:
                return "-xhdpi" + ver;
            case 480:
                return "-xxhdpi" + ver;
            case 640:
                return "-xxxhdpi" + ver;
            case 65534:
                return "-anydpi" + ver;
            case 65535:
                return "-nodpi";
            default:
                return '-' + this.density + "dpi" + ver;
        }
    }
}
