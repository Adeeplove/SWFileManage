package com.cc.fileManage.module.stream;

import android.system.ErrnoException;
import android.system.Os;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class BoundedInputStream extends InputStream {
    //
    private final long foot;

    ///
    private final RandomInputStream stream;

    public BoundedInputStream(RandomInputStream stream, long pos, long remaining) {
        this.stream = stream;
        this.foot = pos + remaining;
    }

    public void seek(long pos) throws IOException {
        this.stream.seek(pos);
    }

    public long getPointer() {
        return stream.getPointer();
    }

    @Override
    public int available() {
        long p = getPointer();
        if(foot > p)
            return (int) (foot - p);
        return 0;
    }

    @Override
    public int read() throws IOException {
        return stream.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return stream.read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return stream.read(b, off, len);
    }

    /**
     * 从字节数组中读取8-bit shor值
     * @return              8-bit short值
     * @throws IOException  IO异常
     */
    public short readUInt8() throws IOException {
        return (short) this.read() ;
    }

    public final short readShortLow() throws IOException {
        int ch1 = this.read();
        int ch2 = this.read();
        return (short)((ch1 << 0) + (ch2 << 8));
    }

    public final short readShort() throws IOException {
        int ch1 = this.read();
        int ch2 = this.read();
        return (short)((ch1 << 8) + (ch2 << 0));
    }

    public final int readIntLow() throws IOException {
        int ch1 = this.read();
        int ch2 = this.read();
        int ch3 = this.read();
        int ch4 = this.read();
        return ((ch1 << 0) + (ch2 << 8) + (ch3 << 16) + (ch4 << 24));
    }

    public final int readInt() throws IOException {
        int ch1 = this.read();
        int ch2 = this.read();
        int ch3 = this.read();
        int ch4 = this.read();
        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
    }

    /**
     * 读取并将字符(16-bit)转换为字符串。以0x00结束，填充字节0。
     * @param length            读取的字节长度
     * @return                  字符串
     * @throws IOException      IO异常
     */
    public String readString(int length) throws IOException {
        byte[] bytes = new byte[length];
        if(this.read(bytes) > 0)
            return new String(bytes, StandardCharsets.UTF_8);
        return null;
    }

    /**
     * 读取并将字符(16-bit)转换为字符串。以0x00结束，填充字节0。
     * @param length        读取的字节长度
     * @return              字符串
     * @throws IOException  IO异常
     */
    public String readString16(int length) throws IOException {
        byte[] bytes = new byte[length];
        StringBuilder builder = new StringBuilder();
        if(this.read(bytes) > 0) {
            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            byte[] buf_2 = new byte[2];
            while (in.read(buf_2) != -1) {
                int code = (bytes[1] & 0xff) << 8 | (bytes[0] & 0xff);
                if (code == 0x00)
                    break;  // End of String
                else
                    builder.append((char) code);
            }
        }
        return builder.toString();
    }

    public void skipRemaining() throws IOException{
        seek(foot);
    }
}
