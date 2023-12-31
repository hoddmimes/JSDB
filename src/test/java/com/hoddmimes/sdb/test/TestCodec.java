package com.hoddmimes.sdb.test;

import com.google.gson.*;

import java.util.Random;

public class TestCodec
{
    private static final String STRDATA = "ABCXYZ";
    private Random mRandom = new Random();



    public static void main(String[] args) {
        TestCodec t = new TestCodec();
        t.test();
        t.testJencoder( 10000 );
        t.testJencoder( 10000000 );
        t.testStringEncoder( 10000 );
        t.testStringEncoder( 10000000 );
        t.testJdecoder( 10000 );
        t.testJdecoder( 10000000 );
        t.testStringDecoder( 10000 );
        t.testStringDecoder( 10000000 );


    }


    private void test() {
        JsonObject jObject = createObject( 42,"kalle");
        JEncoder jEncoder = new JEncoder();
        byte[] jBuffer = jEncoder.encode( jObject );

        JDecoder jDecoder = new JDecoder();
        JsonObject tObject = jDecoder.decode( jBuffer );
        if (jObject.toString().contentEquals( tObject.toString())) {
            System.out.println(" Objects are same");
        } else {
            System.out.println(" Objects are not same");
        }
    }

    private void testStringEncoder( int pCount ) {
        JsonObject jObject = createObject( 42, "Kalle");
        long tStart = System.nanoTime();
        for (int i = 0; i < pCount; i++) {
            String jStr = jObject.toString();
        }
        System.out.println("string-encode [ " + pCount + " ] avg time: " + (((System.nanoTime() - tStart)) / pCount) + " nano");
    }

    private void testStringDecoder( int pCount ) {
        JsonObject jObject = createObject( 42, "Kalle");
        String jString = jObject.toString();

        long tStart = System.nanoTime();
        for (int i = 0; i < pCount; i++) {
            JsonElement jElement = JsonParser.parseString( jString );
            jElement.getAsJsonObject();
        }
        System.out.println("string-decoder [ " + pCount + " ] avg time: " + (((System.nanoTime() - tStart)) / pCount) + " nano");
    }

    private void testJencoder( int pCount ) {

        JsonObject jObject = createObject( 42, "Kalle");
        long tStart = System.nanoTime();
        for (int i = 0; i < pCount; i++) {
            JEncoder jEncoder = new JEncoder();
            jEncoder.encode( jObject );
        }
        System.out.println("jencode [ " + pCount + " ] avg time: " + (((System.nanoTime() - tStart)) / pCount) + " nano");
    }

    private void testJdecoder( int pCount ) {
        JsonObject jObject = createObject( 42, "Kalle");
        JEncoder jEncoder = new JEncoder();
        byte[] jBuffer = jEncoder.encode( jObject );
        long tStart = System.nanoTime();
        for (int i = 0; i < pCount; i++) {
            JDecoder jDecoder = new JDecoder();
            jDecoder.decode( jBuffer );
        }
        System.out.println("jdecoder [ " + pCount + " ] avg time: " + (((System.nanoTime() - tStart)) / pCount) + " nano");
    }

    private JsonObject createObject(int k1, String k2, int pIntValue, String pStrValue ) {
        JsonObject jSub  = new JsonObject();

        jSub.addProperty("subStrValue", createStringData( mRandom.nextInt(100) + 1));
        for (int i = 0; i < mRandom.nextInt(5); i++) {
            jSub.addProperty(String.format("subIntValue%02d".formatted(i)), k1 + 1000000);
        }
        jSub.addProperty("subIntValue", k1 + 1000000);

        JsonObject jObj = new JsonObject();
        jObj.addProperty("k1", k1);
        jObj.addProperty("k2", k2);
        jObj.addProperty("intValue", pIntValue);
        jObj.addProperty("strValue", pStrValue);
        jObj.add("sub", jSub);
        jObj.add("nullValue", JsonNull.INSTANCE);
        jObj.addProperty("boolValue", true);
        JsonArray jArr = new JsonArray();
        for (int i = 0; i < 5; i++) {
            jArr.add( createStringData(8));
        }
        jObj.add("arrValue", jArr);
        return jObj;
    }

    private JsonObject createObject(int k1, String k2 ) {
        return createObject(k1,k2, (1 + mRandom.nextInt(100)), createStringData( mRandom.nextInt(123)));
    }

    private String createStringData( int pSize ) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < pSize; i++) {
            sb.append( STRDATA.charAt( mRandom.nextInt(6)));
        }
        return sb.toString();
    }
}
