package com.hoddmimes.sdb.test;

import com.google.gson.JsonObject;
import com.hoddmimes.jsql.*;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Test {
    private static final String DB_FILE = "./sdb.sql";
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private static final String STRDATA = "ABCXYZ";

    private List<JSDBCollection> mCollections = new ArrayList<JSDBCollection>();
    private JSDB mDb;
    private Random mRandom = new Random();

    public static void main(String[] args) {
        Test t = new Test();
        t.test();
    }

    private JsonObject createObject(int k1, String k2,  int pIntValue, String pStrValue ) {
        JsonObject jObj = new JsonObject();
        jObj.addProperty("k1", k1);
        jObj.addProperty("k2", k2);
        jObj.addProperty("intValue", pIntValue);
        jObj.addProperty("strValue", pStrValue);
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
        loadDatabase( 1000 );
        //find( "($gt: (k1, 970))", 2000 );
        //find( "($and: ($gte: (k1,250)) ($eq: (k2,'XYZ')))", 2000 );
        find( "($and: ($gte: (k1,250)) ($gt: (intValue, 50)))", 2000 );

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
            log("Find [" + (tCount/pCount) + "] \"" + pFilterString + " \"time: " + (System.currentTimeMillis() - tStartTime) + " ms. Avg Insert: " + (tFindUsec / pCount) + " usec");
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
            log("Loaded database records: " +pRecords+ " load time: " + (System.currentTimeMillis() - tStartTime) + " Avg Insert: " + (tInsertUsec / pRecords) + " usec");
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
            mDb = new JSDB();
            mDb.openDatabase(DB_FILE);

            mDb.createCollection("TEST",
                    new JSDBKey("k1", Integer.class, true, true ),
                    new JSDBKey("k2", String.class, false, false ));

            mCollections = mDb.getCollections();
            log("Open database \"" + DB_FILE + "\"");
            for( JSDBCollection c : mCollections) {
                System.out.println("    collection: " + c);
            }
        }
        catch( JSDBException e) {
            e.printStackTrace();
        }

    }



    private void log( String pMsg ) {
        System.out.println( SDF.format(System.currentTimeMillis()) + " " + pMsg );
        System.out.flush();
    }
}
