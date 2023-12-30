package com.hoddmimes.sdb.test;



import com.google.gson.*;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class JDecoder extends JCodec
{
    JsonObject jObject;

    public JDecoder() {
        super();
        jObject = null;
    }

    public JsonObject decode( byte[] jbuffer ) {
        mBuffer = ByteBuffer.wrap(jbuffer);
        jObject = new JsonObject();
        while(mBuffer.hasRemaining()) {
            byte tType = mBuffer.get();
            String tKey = getKey();

            switch (tType) {
                case TYPE_BYTE -> jObject.addProperty(tKey, getByte());
                case TYPE_SHORT -> jObject.addProperty(tKey, getShort());
                case TYPE_INT -> jObject.addProperty(tKey, getInt());
                case TYPE_LONG -> jObject.addProperty(tKey, getLong());
                case TYPE_FLOAT -> jObject.addProperty(tKey, getFloat());
                case TYPE_DOUBLE -> jObject.addProperty(tKey, getDouble());
                case TYPE_BOOLEAN -> jObject.addProperty(tKey, getBoolean());
                case TYPE_STRING -> jObject.addProperty(tKey, getString());
                case TYPE_NULL -> jObject.add(tKey, JsonNull.INSTANCE);
                case TYPE_OBJECT -> jObject.add( tKey, getObject());
                case TYPE_ARRAY -> jObject.add(tKey, getArray());
            }
        }
        return jObject;
    }


    private JsonArray getArray() {
        int tSize = getShort();
        JsonArray jArr = new JsonArray(tSize);
        for (int i = 0; i < tSize; i++) {
            byte tType = getByte();
            switch (tType) {
                case TYPE_BYTE -> jArr.add(getByte());
                case TYPE_SHORT -> jArr.add( getShort());
                case TYPE_INT -> jArr.add( getInt());
                case TYPE_LONG -> jArr.add( getLong());
                case TYPE_FLOAT -> jArr.add( getFloat());
                case TYPE_DOUBLE -> jArr.add( getDouble());
                case TYPE_BOOLEAN -> jArr.add( getBoolean());
                case TYPE_STRING -> jArr.add( getString());
                case TYPE_NULL ->  jArr.add( JsonNull.INSTANCE );
                case TYPE_ARRAY -> jArr.add( getArray());
                case TYPE_OBJECT -> jArr.add( getObject());
            }
        }
        return jArr;
    }

    private String getString() {
        int tSize = getShort();
        byte[] tBuffer  = new byte[ tSize ];
        mBuffer.get( tBuffer );
        return new String( tBuffer, StandardCharsets.UTF_8);
    }

    private JsonObject getObject() {
        int tSize = getShort();
        byte[] jBytes = new byte[ tSize ];
        mBuffer.get( jBytes );
        JDecoder jDecoder = new JDecoder();
        return jDecoder.decode( jBytes );
    }

    private JsonObject getJsonObject() {
        return jObject;
    }

    private byte getByte() {
        return mBuffer.get();
    }

    private short getShort() {
        return mBuffer.getShort();
    }

    private int getInt() {
        return mBuffer.getInt();
    }

    private long getLong() {
        return mBuffer.getLong();
    }

    private float getFloat() {
        return mBuffer.getFloat();
    }

    private double getDouble() {
        return mBuffer.getFloat();
    }

    private boolean getBoolean() {
        if (mBuffer.get() == (byte) 1) {
            return true;
        }
        return false;
    }



    String getKey() {
        int tSize = mBuffer.getShort();
        if (tSize == 0) {
            return null;
        }
        byte[] tStrBuf = new byte[ tSize ];
        mBuffer.get( tStrBuf );
        return new String( tStrBuf, StandardCharsets.UTF_8 );
    }

}


