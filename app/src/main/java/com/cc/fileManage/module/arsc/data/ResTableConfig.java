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

    public static ResTableConfig parseFrom(BoundedInputStream stream) throws IOException {
        ResTableConfig config = new ResTableConfig();
        long cursor = stream.getPointer();
        long start = cursor;

        config.size = stream.readIntLow();
        cursor += 4;

        config.mcc = stream.readShortLow();
        config.mnc = stream.readShortLow();
        stream.seek(cursor);     // Reset cursor to get union value.
        config.imsi = stream.readIntLow();
        cursor += 4;

        config.language = stream.readShortLow();
        config.country = stream.readShortLow();
        stream.seek(cursor);
        config.locale = stream.readIntLow();
        cursor += 4;

        config.orientation = stream.readUInt8();
        config.touchScreen = stream.readUInt8();
        config.density = stream.readShortLow();
        stream.seek(cursor);
        config.screenType = stream.readIntLow();
        cursor += 4;

        config.keyboard = stream.readUInt8();
        config.navigation = stream.readUInt8();
        config.inputFlags = stream.readUInt8();
        config.inputPad0 = stream.readUInt8();
        stream.seek(cursor);
        config.input = stream.readIntLow();
        cursor += 4;

        config.screenWidth = stream.readShortLow();
        config.screenHeight = stream.readShortLow();
        stream.seek(cursor);
        config.screenSize = stream.readIntLow();
        cursor += 4;

        config.sdkVersion = stream.readShortLow();
        config.minorVersion = stream.readShortLow();
        stream.seek(cursor);
        config.version = stream.readIntLow();
        cursor += 4;

        config.screenLayout = stream.readUInt8();
        config.uiModeByte = stream.readUInt8();
        config.smallestScreenWidthDp = stream.readShortLow();
        stream.seek(cursor);
        config.screenConfig = stream.readIntLow();
        cursor += 4;

        config.screenWidthDp = stream.readShortLow();
        config.screenHeightDp = stream.readShortLow();
        stream.seek(cursor);
        config.screenSizeDp = stream.readIntLow();
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
