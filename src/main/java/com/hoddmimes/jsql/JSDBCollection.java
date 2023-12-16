package com.hoddmimes.jsql;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class JSDBCollection
{
    private Connection mDbConnection;
    static final String COL_SQL_DATA = "DATA";
    static final String COL_SQL_NAME = "NAME";
    static final String COL_SQL_TABLE = "JSQL_COLLECTIONS";

    JSDBKey[]   mKeys; // Note that first key is primary key
    String  mName; // Collection name a.k.a SQL Table

    JSDBCollection(Connection pDbConnection, String pName, JSDBKey[] pKeys ) {
        mKeys = pKeys;
        mName = pName;
        mDbConnection = pDbConnection;
    }

    String getKeyList() {
        StringBuilder sb = new StringBuilder();
        for( JSDBKey k : mKeys ) {
            sb.append( k.getId() + ", ");
        }
        return sb.toString();
    }
    void verifyKeys( JsonObject pObject ) throws Exception
    {
        for ( JSDBKey k : mKeys) {
            if (!pObject.has( k.getId() )) {
                throw new Exception("Object missing key attribute \"" + k.getId() +"\"");
            }
        }
    }

    String getValues( JsonObject pObject ) throws Exception
    {
        StringBuilder sb = new StringBuilder();
        for ( JSDBKey k : mKeys ) {
            if (k.getType() == String.class) {
                sb.append("\"" + pObject.get(k.getId()).getAsString() + "\", ");
            }
            if (k.getType() == Integer.class) {
                sb.append( String.valueOf(pObject.get(k.getId()).getAsInt()) + ", ");
            }
            if (k.getType() == Long.class) {
                sb.append( String.valueOf(pObject.get(k.getId()).getAsLong()) + ", ");
            }
            if (k.getType() == Double.class) {
                sb.append( String.valueOf(pObject.get(k.getId()).getAsDouble()) + ", ");
            }
            if (k.getType() == Boolean.class) {
                if (pObject.get(k.getId()).getAsBoolean()) {
                    sb.append("TRUE, ");
                } else {
                    sb.append("FALSE, ");
                }
            }
        }
        String s = "\"" + pObject.toString().replace('"','\'') + "\"";
        sb.append( s );
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder( this.mName + "   ");
        for( JSDBKey k : this.mKeys) {
            sb.append("[ " + k.toString() + " ] ");
        }
        return sb.toString();
    }

    /* =================================
        Data access methods
     ================================ */

    public List<JsonObject> findAll(int pMaxElements) throws JSDBException {
        String sql = "SELECT * FROM " + this.mName;
        List<JsonObject> tResult = new ArrayList<>();
        try {
            Statement stmt = mDbConnection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while( rs.next() && tResult.size() < pMaxElements ) {
                String jsonString = new String(rs.getBytes( COL_SQL_DATA ), StandardCharsets.UTF_8);
                tResult.add(JsonParser.parseString(jsonString).getAsJsonObject());
            }
        } catch (Exception e) {
            throw new JSDBException(e.getMessage(), e);
        }
        return tResult;
    }

    public List<JsonObject> findAll() throws JSDBException {
        return findAll(Integer.MAX_VALUE);
    }



    public void insert(JsonObject pObject, boolean pUpdate ) throws JSDBException {
        try {
            this.verifyKeys(pObject);
            String sql = null;
            if (pUpdate) {
                sql = "INSERT OR REPLACE INTO " + this.mName + " ( " + this.getKeyList() + "DATA ) VALUES ( " + this.getValues(pObject) + ")";
            } else {
                sql = "INSERT INTO " + this.mName + " ( " + this.getKeyList() + "DATA ) VALUES ( " + this.getValues(pObject) + ")";
            }
            Statement stmt = mDbConnection.createStatement();
            int sts = stmt.executeUpdate(sql);
        } catch (Exception e) {
            throw new JSDBException(e.getMessage(), e);
        }
    }

    public void insert(JsonObject pObject ) throws JSDBException {
        insert(pObject, true);
    }
}
