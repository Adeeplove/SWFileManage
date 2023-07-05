package com.cc.fileManage.utils;

public class LSUtil {

    /**
     * 将long转为低字节在前，高字节在后的byte数组
     * @param n long
     * @return byte[]
     */
    public static byte[] toLH(long n) {
        byte[] b = new byte[8];
        b[0] = (byte) (n & 0xff);
        b[1] = (byte) (n >> 8 & 0xff);
        b[2] = (byte) (n >> 16 & 0xff);
        b[3] = (byte) (n >> 24 & 0xff);
        b[4] = (byte) (n >> 32 & 0xff);
        b[5] = (byte) (n >> 40 & 0xff);
        b[6] = (byte) (n >> 48 & 0xff);
        b[7] = (byte) (n >> 56 & 0xff);
        return b;
    }

    /**
     * 将long转为高字节在前，低字节在后的byte数组
     * @param n long
     * @return byte[]
     */
    public static byte[] toHH(long n) {
        byte[] b = new byte[8];
        b[7] = (byte) (n & 0xff);
        b[6] = (byte) (n >> 8 & 0xff);
        b[5] = (byte) (n >> 16 & 0xff);
        b[4] = (byte) (n >> 24 & 0xff);
        b[3] = (byte) (n >> 32 & 0xff);
        b[2] = (byte) (n >> 40 & 0xff);
        b[1] = (byte) (n >> 48 & 0xff);
        b[0] = (byte) (n >> 56 & 0xff);
        return b;
    }

    /**
     * 将int转为低字节在前，高字节在后的byte数组
     * @param n int
     * @return byte[]
     */
    public static byte[] toLH(int n) {
        byte[] b = new byte[4];
        b[0] = (byte) (n & 0xff);
        b[1] = (byte) (n >> 8 & 0xff);
        b[2] = (byte) (n >> 16 & 0xff);
        b[3] = (byte) (n >> 24 & 0xff);
        return b;
    }

    /**
     * 将int转为高字节在前，低字节在后的byte数组
     * @param n int
     * @return byte[]
     */
    public static byte[] toHH(int n) {
        byte[] b = new byte[4];
        b[3] = (byte) (n & 0xff);
        b[2] = (byte) (n >> 8 & 0xff);
        b[1] = (byte) (n >> 16 & 0xff);
        b[0] = (byte) (n >> 24 & 0xff);
        return b;
    }

    /**
     * 将short转为低字节在前，高字节在后的byte数组
     * @param n short
     * @return byte[]
     */
    public static byte[] toLH(short n) {
        byte[] b = new byte[2];
        b[0] = (byte) (n & 0xff);
        b[1] = (byte) (n >> 8 & 0xff);
        return b;
    }

    /**
     * 将short转为高字节在前，低字节在后的byte数组
     * @param n short
     * @return byte[]
     */
    public static byte[] toHH(short n) {
        byte[] b = new byte[2];
        b[1] = (byte) (n & 0xff);
        b[0] = (byte) (n >> 8 & 0xff);
        return b;
    }

    /**
     * 将float转为低字节在前，高字节在后的byte数组
     */
    public static byte[] toLH(float f) {
        return toLH(Float.floatToRawIntBits(f));
    }

    /**
     * 将float转为高字节在前，低字节在后的byte数组
     */
    public static byte[] toHH(float f) {
        return toHH(Float.floatToRawIntBits(f));
    }

    /**
     * 将String转为byte数组
     */
    public static byte[] stringToBytes(String s, int length) {
        StringBuilder sBuilder = new StringBuilder(s);
        while (sBuilder.toString().getBytes().length < length) {
            sBuilder.append(" ");
        }
        s = sBuilder.toString();
        return s.getBytes();
    }


    /**
     * 将字节数组转换为String
     * @param b byte[]
     * @return String
     */
    public static String bytesToString(byte[] b) {
        StringBuilder result = new StringBuilder();
        for (byte value : b) {
            result.append((char) (value & 0xff));
        }
        return result.toString();
    }

    /**
     * 将字符串转换为byte数组
     * @param s String
     * @return byte[]
     */
    public static byte[] stringToBytes(String s) {
        return s.getBytes();
    }

    /**
     * 将高字节数组转换为long
     * @param b byte[]
     * @return long
     */
    public static long hBytesToLong(byte[] b) {
        int s = 0;
        for (int i = 0; i < 7; i++) {
            if (b[i] >= 0) {
                s = s + b[i];
            } else {
                s = s + 256 + b[i];
            }
            s = s * 256;
        }
        if (b[7] >= 0) {
            s = s + b[7];
        } else {
            s = s + 256 + b[7];
        }
        return s;
    }

    /**
     * 将低字节数组转换为long
     * @param b byte[]
     * @return long
     */
    public static long lBytesToLong(byte[] b) {
        int s = 0;
        for (int i = 0; i < 7; i++) {
            if (b[3-i] >= 0) {
                s = s + b[7-i];
            } else {
                s = s + 256 + b[7-i];
            }
            s = s * 256;
        }
        if (b[0] >= 0) {
            s = s + b[0];
        } else {
            s = s + 256 + b[0];
        }
        return s;
    }

    /**
     * 将高字节数组转换为int
     * @param b byte[]
     * @return int
     */
    public static int hBytesToInt(byte[] b) {
        int s = 0;
        for (int i = 0; i < 3; i++) {
            if (b[i] >= 0) {
                s = s + b[i];
            } else {
                s = s + 256 + b[i];
            }
            s = s * 256;
        }
        if (b[3] >= 0) {
            s = s + b[3];
        } else {
            s = s + 256 + b[3];
        }
        return s;
    }

    /**
     * 将低字节数组转换为int
     * @param b byte[]
     * @return int
     */
    public static int lBytesToInt(byte[] b) {
        int s = 0;
        for (int i = 0; i < 3; i++) {
            if (b[3-i] >= 0) {
                s = s + b[3-i];
            } else {
                s = s + 256 + b[3-i];
            }
            s = s * 256;
        }
        if (b[0] >= 0) {
            s = s + b[0];
        } else {
            s = s + 256 + b[0];
        }
        return s;
    }


    /**
     * 高字节数组到short的转换
     * @param b byte[]
     * @return short
     */
    public static short hBytesToShort(byte[] b) {
        int s = 0;
        if (b[0] >= 0) {
            s = s + b[0];
        } else {
            s = s + 256 + b[0];
        }
        s = s * 256;
        if (b[1] >= 0) {
            s = s + b[1];
        } else {
            s = s + 256 + b[1];
        }
        return (short)s;
    }

    /**
     * 低字节数组到short的转换
     * @param b byte[]
     * @return short
     */
    public static short lBytesToShort(byte[] b) {
        int s = 0;
        if (b[1] >= 0) {
            s = s + b[1];
        } else {
            s = s + 256 + b[1];
        }
        s = s * 256;
        if (b[0] >= 0) {
            s = s + b[0];
        } else {
            s = s + 256 + b[0];
        }
        return (short)s;
    }

    /**
     * 高字节数组转换为float
     * @param b byte[]
     * @return float
     */
    public static float hBytesToFloat(byte[] b) {
        int i = ((((b[0]&0xff)<<8 | (b[1]&0xff))<<8) | (b[2]&0xff))<<8 | (b[3]&0xff);
        return Float.intBitsToFloat(i);
    }

    /**
     * 低字节数组转换为float
     * @param b byte[]
     * @return float
     */
    public static float lBytesToFloat(byte[] b) {
        int i = ((((b[3]&0xff)<<8 | (b[2]&0xff))<<8) | (b[1]&0xff))<<8 | (b[0]&0xff);
        return Float.intBitsToFloat(i);
    }

    /**
     * 将byte数组中的元素倒序排列
     */
    public static byte[] bytesReverseOrder(byte[] b) {
        int length = b.length;
        byte[] result = new byte[length];
        for(int i=0; i<length; i++) {
            result[length-i-1] = b[i];
        }
        return result;
    }

    /**
     * 将long类型的值转换为字节序颠倒过来对应的int值
     * @param i int
     * @return int
     */
    public static long reverseLong(long i) {
        return hBytesToLong(toLH(i));
    }

    /**
     * 将int类型的值转换为字节序颠倒过来对应的int值
     * @param i int
     * @return int
     */
    public static int reverseInt(int i) {
        return hBytesToInt(toLH(i));
    }

    /**
     * 将short类型的值转换为字节序颠倒过来对应的short值
     * @param s short
     * @return short
     */
    public static short reverseShort(short s) {
        return hBytesToShort(toLH(s));
    }

    /**
     * 将float类型的值转换为字节序颠倒过来对应的float值
     * @param f float
     * @return float
     */
    public static float reverseFloat(float f) {
        return hBytesToFloat(toLH(f));
    }

    public static int getUnsignedByte (byte data){
        //将data字节型数据转换为0~255 (0xFF 即BYTE)。
        return data&0x0FF;
    }

    public static int getUnsignedByte (short data){
        //将data字节型数据转换为0~65535 (0xFFFF 即 WORD)。
        return data & 0x0FFFF;
    }

    public static long getUnsignedInt (int data){
        //将int数据转换为0~4294967295 (0xFFFFFFFF即DWORD)。
        return data & 0x0FFFFFFFFL;
    }

    public static long getUnsignedLong (long data){
        //将int数据转换为0~4294967295 (0xFFFFFFFF即DWORD)。
        return data & 0x0fffffffffffffffL;
    }
}