package com.hoddmimes.sdb.test;

import com.google.gson.JsonObject;
import com.hoddmimes.jsql.JSDBCollection;
import com.hoddmimes.jsql.JSDBException;
import com.hoddmimes.jsql.JSDB;
import com.hoddmimes.jsql.JSDBKey;
import org.sqlite.core.DB;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestDB
{
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private static final String DB_NAME = "./testdb.sql";
    Random mRandom = new Random( System.nanoTime());
    JSDB db = null;
    List<JSDBCollection> mCollections;

    public static void main(String[] args) {
        TestDB tdb = new TestDB();
        tdb.deleteTestDbIfExists();
        tdb.create_test_db();
        tdb.open_test_db();
        tdb.test_insert();

        // Test find
        tdb.test_find_all();
        tdb.test_filter("($and: ($and: ($gt: (k1, 100)) ($lte: (k1, 150))) ($like: (k2, 'KEY1%')))");
        tdb.test_filter("($and: ($eq: (rndnum, 5)) ($like: (k2, 'KEY1%')))");

        // Test Update
        tdb.test_update();

    }

    private void test_update() {
        try {
            JSDBCollection tCollection = db.getCollections("TEST");
            List<JsonObject> jObjects = tCollection.find( "($eq: (k1, 500))");
            jObjects.get(0).addProperty("tstdata", "version 3");
            tCollection.update(jObjects.get(0));
            jObjects = tCollection.find( "($eq: (k1, 500))");
            assertEquals(jObjects.get(0).get("tstdata").getAsString(), "version 3");

            JsonObject jUpdateObject = new JsonObject();
            jUpdateObject.addProperty("rndnum", 28);
            jUpdateObject.addProperty("tstdata", "version 4");

            // Update all object where (k1 > 100 and k1 <= 150) and (randnum > 5)
            int tCount = tCollection.update("($and: ($and: ($gt: (k1, 100)) ($lte: (k1, 150))) ($gt: (rndnum, 5)))", jUpdateObject);
            log("filter-update updated records:  " + tCount );

        } catch (JSDBException e) {
            e.printStackTrace();
        }
    }

    private void test_find_all() {
        try {
            JSDBCollection c = db.getCollections("TEST");
            List<JsonObject> jObjects = c.findAll();
            assertEquals(jObjects.size(), 201);
            log("findAll: retrieved " + jObjects.size() + " objects " );
        }
        catch( JSDBException e) {
            e.printStackTrace();
        }
    }


    private void deleteTestDbIfExists() {
        File tDbFile = new File(DB_NAME);
        if (tDbFile.exists()) {
            boolean sts = tDbFile.delete();
            if (sts) {
                log("Old testdatabase (" + DB_NAME + ") is deleted");
            } else {
                log("Old testdatabase (" + DB_NAME + ") could not be deleted");
            }
        }
    }

    private void open_test_db() {
        db = new JSDB();
        try {
            db.openDatabase(DB_NAME );
            mCollections = db.getCollections();
            for( JSDBCollection c : mCollections ) {
                log("    DB-collection: " + c.toString());
            }

        } catch (JSDBException e) {
            e.printStackTrace();
        }
    }

    private void test_filter( String pFilterString ) {
        try {
            JSDBCollection tCollection = db.getCollections("TEST");
            List<JsonObject> tResult = tCollection.find( pFilterString );
            log("test-filter: retreived " + tResult.size() + " objects \n    filter-string: " + pFilterString);
            //tResult.stream().forEach( jo -> {System.out.println("testfind: " + jo.toString());});
        }
        catch( JSDBException e ) {
            e.printStackTrace();
        }
    }

    private void test_insert() {
        try {
            JSDBCollection tCollection = db.getCollections("TEST");
            for (int i = 1; i <= 200; i++) {
                tCollection.insert( createObject((long) (i+1), String.format("KEY%03d", (i+1) )));
            }

            tCollection.insert(createObject(500L, "KEY500", "version 1"));
            List<JsonObject> jObjects = tCollection.find( "($eq: (k1, 500))");
            assertEquals(jObjects.get(0).get("tstdata").getAsString(), "version 1");

            tCollection.insert(createObject(500L, "KEY500", "version 2"));
            jObjects = tCollection.find( "($eq: (k1, 500))");
            assertEquals(jObjects.get(0).get("tstdata").getAsString(), "version 2");

        }
        catch( JSDBException e ) {
            e.printStackTrace();
        }
    }


    private void create_test_db() {
        JSDB db = new JSDB();
        try {
            db.createDatabase(DB_NAME);


            db.createCollection("TEST",
                                new JSDBKey("k1", Long.class),
                                new JSDBKey("k2", String.class, false, false));

            db.closeDatabase();
            log("test database (" + DB_NAME + ") successfully created");
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

    private void log( String pMsg ) {
        System.out.println( SDF.format(System.currentTimeMillis()) + " " + pMsg );
        System.out.flush();
    }

}
