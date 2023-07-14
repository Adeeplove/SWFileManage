package com.cc.fileManage.utils;

/**
 *
 * Created by xueqiulxq on 07/07/2017.
 */

public class PrintUtils {

    public static char toChar(byte num) {
        return (char) num;
    }

    public static String hex(int num) {
        return String.format("%02x", num);
    }

    public static String hex(byte num) {
        return String.format("%02x", num);
    }

    public static String hex4(int num) {
        return String.format("%04x", num);
    }

    public static String hex4(long num) {
        return String.format("%08x", num);
    }

    public static String hex(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        for (byte aByte : bytes) {
            builder.append(String.format("%02x", aByte & 0xff));
        }
        return builder.toString();
    }

    public static String indent(String src) {
        return "\t" + src.trim().replace("\n", "\n\t") + "\n";
    }
}
