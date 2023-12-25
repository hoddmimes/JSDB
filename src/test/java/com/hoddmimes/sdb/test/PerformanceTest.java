package com.hoddmimes.sdb.test;

import com.google.gson.JsonObject;
import com.hoddmimes.jsql.*;

import javax.swing.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PerformanceTest {
    private static final String DB_FILE = "./performance.sql";
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private static final String STRDATA = "ABCXYZ";

    private List<JSDBCollection> mCollections = new ArrayList<JSDBCollection>();
    private JSDB mDb;
    private Random mRandom = new Random();

    public static void main(String[] args) {
        PerformanceTest t = new PerformanceTest();
        t.test();
    }

    private JsonObject createObject(int k1, String k2,  int pIntValue, String pStrValue ) {
        JsonObject jSub  = new JsonObject();

        jSub.addProperty("subStrValue", createStringData( mRandom.nextInt(100) + 1));
        for (int i = 0; i < mRandom.nextInt(66); i++) {
            jSub.addProperty(String.format("subIntValue%02d".formatted(i)), k1 + 1000000);
        }
        jSub.addProperty("subIntValue", k1 + 1000000);

        JsonObject jObj = new JsonObject();
        jObj.addProperty("k1", k1);
        jObj.addProperty("k2", k2);
        jObj.addProperty("intValue", pIntValue);
        jObj.addProperty("strValue", pStrValue);
        jObj.add("sub", jSub);
        return jObj;
    }

    private JsonObject createObject(int k1, String k2 ) {
        return createObject(k1,k2, (1 + mRandom.nextInt(100)), createStringData( mRandom.nextInt(123)));
    }



    private String getK2() {
        return  String.valueOf( (STRDATA.charAt(mRandom.nextInt(3) + 3))) +
                String.valueOf( (STRDATA.charAt(mRandom.nextInt(3) + 3))) +
                String.valueOf( (STRDATA.charAt(mRandom.nextInt(3) + 3)));
    }

    private String createStringData( int pSize ) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < pSize; i++) {
            sb.append( STRDATA.charAt( mRandom.nextInt(6)));
        }
        return sb.toString();
    }

    private void test() {
        createDatabase();
        openDatabase();
        loadDatabase( 100000 );
        closeDatabase();
        //find( "($gt: (k1, 970))", 2000 );
        //find( "($and: ($gte: (k1,250)) ($eq: (k2,'XYZ')))", 2000 );
        openDatabase();
        findCount();
        //find( "($eq: (k1,250))", 1000 );
        //find( "($and: ($eq: (k1,250)) ($like: (k2,'%X%')))", 1000 );
        find( "($and: ($and: ($lt: (k1,66000)) ($gt: (k1,65000))) ($eq: (intValue, 50)))", 100 );

    }


    private void findCount() {
        try {
            JSDBCollection tCollection = mDb.getCollections("TEST");
            long tStartTime = System.currentTimeMillis();
            log("Number of objects in collection " + tCollection.getName() + ": " + tCollection.getCount() + " exec-time: " + (System.currentTimeMillis() - tStartTime));
        }
        catch (JSDBException e) {
            e.printStackTrace();
        }
    }


    private void find(String pFilterString, int pCount) {
        try {
            long tCount = 0;
            long tFindUsec = 0;
            long tStartTime = System.currentTimeMillis();
            JSDBCollection tCollection = mDb.getCollections("TEST");
            for (int i = 0; i < pCount; i++) {
                long s = System.nanoTime();
                List<JsonObject> tObjects = tCollection.find(pFilterString);
                //tObjects.stream().forEach( j -> {System.out.println(j.toString());});
                tCount += tObjects.size();
                tFindUsec += ((System.nanoTime() - s) / 1000L);
            }
            log("Find [" + (tCount/pCount) + "] \"" + pFilterString + " \"time: " + (System.currentTimeMillis() - tStartTime) + " ms. Avg find: " + (tFindUsec / pCount) + " usec");
        }
        catch( JSDBException e ) {
            e.printStackTrace();
        }
    }

    private void loadDatabase( int pRecords) {
        try {
            long tInsertUsec = 0;
            long tStartTime = System.currentTimeMillis();
            JSDBCollection tCollection = mDb.getCollections("TEST");
            for (int i = 0; i < pRecords; i++) {
                JsonObject jObject = createObject(i, getK2());
                long s = System.nanoTime();
                tCollection.insert( jObject );
                tInsertUsec += ((System.nanoTime() - s) / 1000L);
            }
            log("Loaded database records: " +pRecords+ " load time: " + (System.currentTimeMillis() - tStartTime) + " ms. Avg Insert: " + (tInsertUsec / pRecords) + " usec");
        }
        catch( JSDBException e ) {
            e.printStackTrace();
        }
    }

    private void createDatabase() {
        File tFile = new File(DB_FILE);
        if (tFile.exists()) {
            boolean sts = tFile.delete();
            if (sts) {
                log("Old database \"" + DB_FILE + "\" deleted");
            }
        }
        try {
            JSDB.createDatabase(DB_FILE);
            JSDB tDb = new JSDB();
            tDb.openDatabase(DB_FILE);

            tDb.createCollection("TEST",
                    new JSDBKey("k1", Integer.class, true, true ),
                    new JSDBKey("k2", String.class, false, false ));

            tDb.closeDatabase();
        }
        catch( JSDBException e) {
            e.printStackTrace();
        }

    }

    private void openDatabase() {
        mDb = new JSDB();
        try {
            mDb.openDatabase(DB_FILE);
            mCollections = mDb.getCollections();
            log("Open database \"" + DB_FILE + "\"");
        }
        catch( Exception e ) {
            e.printStackTrace();
        }
    }

    private void closeDatabase() {
        try {
            mDb.closeDatabase();
            log("Closing database \"" + DB_FILE + "\"");
        }
        catch( Exception e ) {
            e.printStackTrace();
        }
    }

    private void log( String pMsg ) {
        System.out.println( SDF.format(System.currentTimeMillis()) + " " + pMsg );
        System.out.flush();
    }
}
