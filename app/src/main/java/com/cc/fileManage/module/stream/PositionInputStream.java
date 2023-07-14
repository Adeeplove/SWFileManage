package com.cc.fileManage.module.stream;

import java.io.ByteArrayInputStream;


public class PositionInputStream extends ByteArrayInputStream {

    public PositionInputStream(byte[] buf) {
        super(buf);
    }

    public PositionInputStream(byte[] buf, int offset, int length) {
        super(buf, offset, length);
    }

    public long length() {
        return this.buf.length;
    }

    public void setData(byte[] buf, int offset, int length) {
        this.buf = buf;
        this.pos = offset;
        this.count = Math.min(offset + length, buf.length);
        this.mark = offset;
    }

    public synchronized long getPointer() {
        return this.pos;
    }

    public final void readFully(byte[] b) {
        readFully(b, 0, b.length);
    }

    public final void readFully(byte[] b, int off, int len) {
        int n = 0;
        do {
            int count = this.read(b, off + n, len - n);
            n += count;
        } while (n < len);
    }

    public synchronized long seek(long n) {
        this.pos = 0;
        return skip(n);
    }

    @Override
    public synchronized void reset() {
        seek(this.mark);
        this.mark = 0;
    }

    @Override
    public synchronized void mark(int mark) {
        this.mark = mark;
    }
}
