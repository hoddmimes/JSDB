package com.hoddmimes.sdb.test;

import java.nio.ByteBuffer;

public abstract class JCodec
{
    protected  static final byte TYPE_NULL = 0;
    protected static final byte TYPE_BYTE = 1;
    protected static final byte TYPE_SHORT = 2;
    protected static final byte TYPE_INT = 3;
    protected static final byte TYPE_FLOAT = 4;
    protected static final byte TYPE_DOUBLE = 5;
    protected static final byte TYPE_LONG = 6;
    protected static final byte TYPE_STRING = 7;
    protected static final byte TYPE_ARRAY = 8;
    protected static final byte TYPE_OBJECT = 9;
    protected static final byte TYPE_BOOLEAN = 10;

    protected ByteBuffer mBuffer;

    protected JCodec() {
        mBuffer = null;
    }

    protected JCodec( byte[] jBuffer ) {
        mBuffer = ByteBuffer.wrap( jBuffer );
    }

    protected JCodec( int pInitSize) {
        mBuffer = ByteBuffer.allocate(pInitSize);
    }
}
