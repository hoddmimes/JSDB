package com.hoddmimes.sdb.test;



import com.google.gson.*;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class JEncoder extends JCodec
{

    public JEncoder() {
        super(1024);
    }

    public byte[] encode( JsonObject pObject )
    {
        Map<String, JsonElement> jMap = pObject.asMap();
        for (Map.Entry<String, JsonElement> entry : jMap.entrySet()) {
            add( entry.getKey(), entry.getValue());
        }
        return this.getBytes();
    }


    public void add( String pKey, JsonElement jsonElement) {
        if (jsonElement.isJsonPrimitive()) {
            JsonPrimitive jsonPrimitive = jsonElement.getAsJsonPrimitive();
            if (jsonPrimitive.isNumber()) {
                add(pKey, jsonPrimitive.getAsNumber());
            } else if (jsonPrimitive.isString()) {
                add(pKey, jsonPrimitive.getAsString());
            } else if (jsonPrimitive.isBoolean()) {
                add(pKey, jsonPrimitive.getAsBoolean());
            } else if (jsonPrimitive.isJsonNull()) {
                add( pKey, jsonPrimitive.getAsJsonNull());
            }
        } else if (jsonElement.isJsonObject()) {
            add( pKey, jsonElement.getAsJsonObject());
        } else if (jsonElement.isJsonArray()) {
            add( pKey, jsonElement.getAsJsonArray());
        } else if (jsonElement.isJsonNull()) {
            add( pKey, jsonElement.getAsJsonNull());
        }
    }

    private void ensureCapacity( String pKey, int pSize) {
        ByteBuffer tBuffer = null;

        int tSize = ((pKey != null) ? pKey.length() : 0) + Byte.BYTES + pSize;

        if (mBuffer.remaining() < tSize) {
            if (tSize > 1024) {
                tBuffer = ByteBuffer.allocate(mBuffer.capacity() + pSize);
            } else {
                tBuffer = ByteBuffer.allocate(mBuffer.capacity() + 1024);
            }
            mBuffer.flip();
            tBuffer.put(mBuffer);
            mBuffer = tBuffer;
        }
    }


    private void add( String pKey, Number pNumber) {
        if (pNumber instanceof Integer) {
            add(pKey, pNumber.intValue());
        } else if (pNumber instanceof Double) {
            add(pKey, pNumber.doubleValue());
        } else if (pNumber instanceof Long) {
            add(pKey, pNumber.longValue());
        } else if (pNumber instanceof Float) {
            add(pKey, pNumber.floatValue());
        } else if (pNumber instanceof Byte) {
            add(pKey, pNumber.byteValue());
        } else if (pNumber instanceof Short) {
            add(pKey, pNumber.shortValue());
        }
    }

    private void addKey( String pKey ) {
        if (pKey != null) {
            mBuffer.putShort((short) pKey.length());
            mBuffer.put(pKey.getBytes(StandardCharsets.UTF_8));
        }
    }

    public void add( String pKey, byte pValue)
    {
        ensureCapacity( pKey, Byte.BYTES );
        mBuffer.put( TYPE_BYTE );
        addKey( pKey );
        mBuffer.put(pValue);
    }

    public void add( String pKey, short pValue)
    {
        ensureCapacity( pKey, Short.BYTES );
        mBuffer.put( TYPE_SHORT );
        addKey( pKey );
        mBuffer.putShort(pValue);
    }
    public void add( String pKey, int pValue)
    {
        ensureCapacity( pKey, Integer.BYTES);
        mBuffer.put( TYPE_INT );
        addKey( pKey );
        mBuffer.putInt(pValue);
    }

    public void add( String pKey, long pValue)
    {
        ensureCapacity( pKey, Long.BYTES);
        mBuffer.put( TYPE_LONG );
        addKey( pKey );
        mBuffer.putLong(pValue);
    }

    public void add( String pKey, float pValue)
    {
        ensureCapacity( pKey, Float.BYTES);
        mBuffer.put( TYPE_FLOAT );
        addKey( pKey );
        mBuffer.putFloat(pValue);
    }

    public void add( String pKey, double pValue)
    {
        ensureCapacity( pKey, Double.BYTES);
        mBuffer.put( TYPE_DOUBLE );
        addKey( pKey );
        mBuffer.putDouble(pValue);
    }

    public void add(String pKey, JsonNull pValue) {
        ensureCapacity(pKey, 0);
        mBuffer.put(TYPE_NULL);
        addKey( pKey );
    }

    public void add(String pKey, boolean pValue)
    {
        ensureCapacity( pKey,  1);
        mBuffer.put( TYPE_BOOLEAN );
        addKey( pKey );
        mBuffer.put( ((pValue) ? (byte) 1 : (byte) 0 ));
    }

    public void add(String pKey, String pString)
    {
        ensureCapacity( pKey,  pString.length() + 2);
        mBuffer.put( TYPE_STRING );
        addKey( pKey );

        mBuffer.putShort((short)pString.length());
        if (!pString.isEmpty()) {
            mBuffer.put(pString.getBytes(StandardCharsets.UTF_8));
        }
    }

    public void add(String pKey, JsonArray pArray)
    {
        ensureCapacity( pKey, 2   );
        mBuffer.put( TYPE_ARRAY );
        addKey( pKey );
        mBuffer.putShort((short)pArray.size());

        for (int i = 0; i < pArray.size(); i++) {
            add( null, pArray.get(i));
        }
    }

    public void add(String pKey, JsonObject pObject)
    {
        JEncoder jEncoder = new JEncoder();
        jEncoder.encode( pObject );
        byte[] jObjBytes = jEncoder.getBytes();


        ensureCapacity( pKey, 2 + jObjBytes.length   );
        mBuffer.put( TYPE_OBJECT );
        addKey( pKey );
        mBuffer.putShort((short) jObjBytes.length );
        mBuffer.put( jObjBytes );
    }


    public byte[] getBytes() {
        mBuffer.flip();
        byte[] retbuf = new byte[ mBuffer.limit() ];
        mBuffer.get( retbuf );
        return retbuf;
    }
}


