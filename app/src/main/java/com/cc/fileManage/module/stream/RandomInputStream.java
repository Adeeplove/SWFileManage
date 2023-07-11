package com.cc.fileManage.module.stream;

import static android.system.OsConstants.SEEK_CUR;
import static android.system.OsConstants.SEEK_SET;
import static android.system.OsConstants.S_ISLNK;
import static android.system.OsConstants.S_ISREG;

import android.os.ParcelFileDescriptor;
import android.system.ErrnoException;
import android.system.Os;
import android.system.StructStat;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;

/**
 * 随机读写流
 * @time 2023/7/11
 * @author sowhat
 */
public class RandomInputStream extends InputStream implements AutoCloseable{

    private final FileDescriptor descriptor;
    private final ParcelFileDescriptor fileDescriptor;

    private long  mark;

    public RandomInputStream(ParcelFileDescriptor fileDescriptor) {
        this.fileDescriptor = fileDescriptor;
        this.descriptor = fileDescriptor.getFileDescriptor();
    }

    @Override
    public synchronized void mark(int mark) {
        this.mark = mark;
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    public void seek(long pos) throws IOException {
        if (pos < 0) {
            throw new IOException("offset < 0: " + pos);
        } else {
            try {
                Os.lseek(descriptor, pos, SEEK_SET);
            } catch (ErrnoException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public synchronized void reset() throws IOException {
        seek(mark);
        this.mark = 0L;
    }

    @Override
    public long skip(long n) throws IOException {
        long pos;
        long len;
        long newpos;

        if (n <= 0) {
            return 0;
        }
        pos = getPointer();
        len = length();
        newpos = pos + n;
        if (newpos > len) {
            newpos = len;
        }
        seek(newpos);

        /* return the actual number of bytes skipped */
        return (int) (newpos - pos);
    }

    public long getPointer() {
        try {
            return Os.lseek(descriptor, 0L, SEEK_CUR);
        } catch (ErrnoException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private final byte[] buf = new byte[1];
    @Override
    public int read() throws IOException {
        try {
            return (Os.read(descriptor, buf, 0, 1) > 0) ? buf[0] & 0xff : -1;
        } catch (ErrnoException e) {
            throw new IOException(e);
        }
    }

    public final void readFully(byte[] b) throws IOException {
        readFully(b, 0, b.length);
    }

    public final void readFully(byte[] b, int off, int len) throws IOException {
        int n = 0;
        do {
            int count = this.read(b, off + n, len - n);
            n += count;
        } while (n < len);
    }

    public final String readLine() throws IOException {
        StringBuilder input = new StringBuilder();
        int c = -1;
        boolean eol = false;

        while (!eol) {
            switch (c = read()) {
                case -1:
                case '\n':
                    eol = true;
                    break;
                case '\r':
                    eol = true;
                    long cur = getPointer();
                    if ((read()) != '\n') {
                        seek(cur);
                    }
                    break;
                default:
                    input.append((char)c);
                    break;
            }
        }

        if ((c == -1) && (input.length() == 0)) {
            return null;
        }
        return input.toString();
    }

    public final int readInt() throws IOException {
        int ch1 = this.read();
        int ch2 = this.read();
        int ch3 = this.read();
        int ch4 = this.read();
        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
    }

    public final long readLong() throws IOException {
        return ((long)(readInt()) << 32) + (readInt() & 0xFFFFFFFFL);
    }

    public final float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }

    public final double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }

    public final boolean readBoolean() throws IOException {
        int ch = this.read();
        return (ch != 0);
    }

    public final byte readByte() throws IOException {
        int ch = this.read();
        return (byte)(ch);
    }

    public final short readShort() throws IOException {
        int ch1 = this.read();
        int ch2 = this.read();
        return (short)((ch1 << 8) + (ch2 << 0));
    }

    public final char readChar() throws IOException {
        int ch1 = this.read();
        int ch2 = this.read();
        return (char)((ch1 << 8) + (ch2 << 0));
    }

    public long length() {
        try {
            final StructStat st = Os.fstat(descriptor);
            if (S_ISREG(st.st_mode) || S_ISLNK(st.st_mode)) {
                return st.st_size;
            } else {
                return -1;
            }
        } catch (ErrnoException e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public int available() throws IOException {
        long l = length();
        long i = getPointer();
        if(l != -1 && i != -1) {
            return (int) (l - i);
        }
        return super.available();
    }

    @Override
    public void close() throws IOException {
        fileDescriptor.close();
    }
}