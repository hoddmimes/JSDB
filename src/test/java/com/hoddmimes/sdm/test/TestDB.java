package com.hoddmimes.sdm.test;

import com.google.gson.JsonObject;
import com.hoddmimes.jsql.JSDBCollection;
import com.hoddmimes.jsql.JSDBException;
import com.hoddmimes.jsql.JSDB;
import com.hoddmimes.jsql.JSDBKey;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Random;

public class TestDB
{
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    Random mRandom = new Random( System.nanoTime());
    JSDB db = null;

    public static void main(String[] args) {
        TestDB cdb = new TestDB();
        //cdb.create_test_db();
        cdb.opend_test_db();
        //cdb.test_insert_many();
        cdb.test_insert();
        //cdb.tstfind();
    }

    private void opend_test_db() {
        db = new JSDB();
        try {
            db.openDatabase("./testdb.sql");
            List<JSDBCollection> tCollections = db.getCollections();
            for( JSDBCollection c : tCollections ) {
                System.out.println( c.toString());
            }

        } catch (JSDBException e) {
            e.printStackTrace();
        }
    }

    private void test_insert_many() {
        try {
            JSDBCollection tCollection = db.getCollections("TEST");
            for (int i = 0; i < 200; i++) {
                tCollection.insert( createObject((long) (i+1), String.format("KEY%03d", (i+1) )));
            }
        }
        catch( JSDBException e ) {
            e.printStackTrace();
        }
    }

    private void test_insert() {
        try {
            JSDBCollection tCollection = db.getCollections("TEST");
            tCollection.insert(createObject( 100L, "etthundra", "The test record"));
            tCollection.insert(createObject( 301L, "kalle", "insert number one"));
            tCollection.insert(createObject( 301L, "kalle", "insert number two"));

            List<JsonObject> jObjects = tCollection.findAll();
            for( JsonObject jobj : jObjects) {
                System.out.println( jobj.toString());
            }




        }
        catch( JSDBException e ) {
            e.printStackTrace();
        }
    }

    private void create_test_db() {
        db = new JSDB();
        try {
            db.createDatabase("./testdb.sql");

            db.createCollection("TEST",
                                new JSDBKey("k1", Long.class),
                                new JSDBKey("k2", String.class, false, false));
        } catch (JSDBException e) {
            e.printStackTrace();
        }
    }

    private String createTestData() {

        int tSize = 10 + mRandom.nextInt(100);
        byte[] tBytes = new byte[ tSize ];
        for (int i = 0; i < tSize; i++) {
            tBytes[i] = (byte) (65 + mRandom.nextInt(25));
        }
        return new String( tBytes, StandardCharsets.UTF_8 );
    }

    private  JsonObject createObject(Long key1, String key2, String testData)
    {
        JsonObject obj = new JsonObject();
        obj.addProperty("k1", key1);
        obj.addProperty("k2", key2);
        obj.addProperty("rndnum", mRandom.nextInt(10));
        obj.addProperty("time", SDF.format( System.currentTimeMillis()));
        obj.addProperty("tstdata", testData);
        return obj;

    }

    private  JsonObject createObject(Long key1, String key2) {
        return createObject(key1, key2, createTestData());
    }

}
