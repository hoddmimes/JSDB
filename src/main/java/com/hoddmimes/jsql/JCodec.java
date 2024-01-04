package com.hoddmimes.jsql;

import java.nio.ByteBuffer;

public abstract class JCodec
{
    protected  static final byte TYPE_NULL = 65;
    protected static final byte TYPE_BYTE = 66;
    protected static final byte TYPE_SHORT = 67;
    protected static final byte TYPE_INT = 68;
    protected static final byte TYPE_FLOAT = 69;
    protected static final byte TYPE_DOUBLE = 70;
    protected static final byte TYPE_LONG = 71;
    protected static final byte TYPE_STRING = 72;
    protected static final byte TYPE_ARRAY = 73;
    protected static final byte TYPE_OBJECT = 74;
    protected static final byte TYPE_BOOLEAN = 75;

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
