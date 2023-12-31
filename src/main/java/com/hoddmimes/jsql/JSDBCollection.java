package com.hoddmimes.jsql;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JSDBCollection {
    Connection mDbConnection;
    static final String COL_SQL_DATA = "DATA";
    static final String COL_SQL_NAME = "NAME";
    static final String COL_SQL_TABLE = "JSQL_COLLECTIONS";

    JSDBKey[] mKeys; // Note that first key is primary key
    String mName; // Collection name a.k.a SQL Table

    JStatementInsert stmtInsert;
    JStatementInsertUpdate stmtInsertUpdate;
    JStatementUpdate stmtUpdate;
    JStatementUpdateInsert stmtUpdateInsert;

     JSDBCollection(Connection pDbConnection, String pName, JSDBKey[] pKeys, boolean pPrepareStatement) {
        mKeys = pKeys;
        mName = pName;
        mDbConnection = pDbConnection;
        if (pPrepareStatement) {
            createPreparedStatements();
        }
    }

    JSDBCollection(Connection pDbConnection, String pName, JSDBKey[] pKeys ) {
        this( pDbConnection, pName, pKeys, true);
    }


    void createPreparedStatements() {
        try {
            stmtInsertUpdate = new JStatementInsertUpdate( this );
            stmtUpdate = new JStatementUpdate( this);
            stmtUpdateInsert = new JStatementUpdateInsert( this );
            stmtInsert = new JStatementInsert(this);
        }
        catch( JSDBException e ) {
            throw new RuntimeException( e.getMessage(), e );
        }
    }

    protected String buildPrepareParameters() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mKeys.length; i++) {
            sb.append("?, ");
        }
        sb.append("?");
        return sb.toString();
    }

    protected String buildPrepareSelectParameters() {
        StringBuilder sb = new StringBuilder();
        sb.append( mKeys[0].getId() + " = ?");
        for (int i = 1; i < mKeys.length; i++) {
            sb.append(" AND " + mKeys[i].getId() + " = ?");
        }
        return sb.toString();
    }

    String getKeyList() {
        StringBuilder sb = new StringBuilder();
        for (JSDBKey k : mKeys) {
            sb.append(k.getId() + ", ");
        }
        sb.append( JSDBCollection.COL_SQL_DATA);
        return sb.toString();
    }

    void verifyKeys(JsonObject pObject) throws Exception {
        for (JSDBKey k : mKeys) {
            if (!pObject.has(k.getId())) {
                throw new Exception("Object missing key attribute \"" + k.getId() + "\"");
            }
        }
    }

    String getValues(JsonObject pObject) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (JSDBKey k : mKeys) {
            if (k.getType() == String.class) {
                sb.append("\"" + pObject.get(k.getId()).getAsString() + "\", ");
            }
            if (k.getType() == Integer.class) {
                sb.append(String.valueOf(pObject.get(k.getId()).getAsInt()) + ", ");
            }
            if (k.getType() == Long.class) {
                sb.append(String.valueOf(pObject.get(k.getId()).getAsLong()) + ", ");
            }
            if (k.getType() == Double.class) {
                sb.append(String.valueOf(pObject.get(k.getId()).getAsDouble()) + ", ");
            }
            if (k.getType() == Boolean.class) {
                if (pObject.get(k.getId()).getAsBoolean()) {
                    sb.append("TRUE, ");
                } else {
                    sb.append("FALSE, ");
                }
            }
        }
        String s = "\"" + pObject.toString().replace('"', '\'') + "\"";
        sb.append(s);
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(this.mName + "   ");
        for (JSDBKey k : this.mKeys) {
            sb.append("[ " + k.toString() + " ] ");
        }
        return sb.toString();
    }

    private JsonObject decode( byte[] jByteBuffer) {
        if (JSDB.USE_JCODEC) {
            JDecoder jDecoder = new JDecoder();
            return jDecoder.decode( jByteBuffer );
        } else {
            return JsonParser.parseString( new String(jByteBuffer, StandardCharsets.UTF_8) ).getAsJsonObject();
        }
    }



    private boolean onlyKeyField(JSDBFilter pFilter) throws JSDBException {
        List<String> tFilterKeys = pFilter.getDataFields();
        if (tFilterKeys.size() == 0) {
            throw new JSDBException("Filter contains no data fields");
        }
        for (String fid : tFilterKeys) {
            if (!isKeyField(fid))
                return false;
        }
        return true;
    }


     boolean isKeyField(String fid) throws JSDBException {
        for (JSDBKey k : this.mKeys) {
            if (k.getId().contentEquals(fid)) {
                return true;
            }
        }
        return false;
    }

    JSDBKey getKey(String pFieldId) {
        for (JSDBKey k : mKeys) {
            if (pFieldId.contentEquals(k.getId())) {
                return k;
            }
        }
        // Should not be possible to get here
        throw new RuntimeException("Could not found Collection key \"" + pFieldId + "\"");
    }



    private List<JsonObject> findWithJsonTesterLogic(JSDBFilter pFilter) throws JSDBException {

        String sql = pFilter.createSqlHelperStatement(this);
        //String sql = "SELECT DATA FROM " + mName + ";";

        try {
            Statement stmt = mDbConnection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            List<JsonObject> tResult = new ArrayList<JsonObject>();
            while (rs.next()) {
                JsonObject jObject = decode(rs.getBytes(JSDBCollection.COL_SQL_DATA));
                if (pFilter.jsonMatch(jObject)) {
                    tResult.add(jObject);
                }
            }
            return tResult;
        } catch (Exception e) {
            throw new JSDBException(e.getMessage(), e);
        }
    }

    public int getCount() throws JSDBException {
        try {
            Statement stmt = mDbConnection.createStatement();
            ResultSet rs  = stmt.executeQuery("SELECT COUNT(*) FROM " + this.mName + ";");
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new JSDBException(e.getMessage(), e);
        }
    }

    public String getName() {
        return mName;
    }
    private List<JsonObject> findWithKeyLogic(JSDBFilter pFilter) throws JSDBException {
        // Filter based upon SQL keys
        String sqlSelectString = pFilter.createSqlSelectStatement(this);
        //System.out.println("select-sql: " + sqlSelectString);
        try {
            Statement stmt = mDbConnection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT " + JSDBCollection.COL_SQL_DATA + " FROM " + this.mName + " WHERE " + sqlSelectString + ";");
            List<JsonObject> tResult = new ArrayList<JsonObject>();
            while (rs.next()) {
                tResult.add(decode(rs.getBytes(JSDBCollection.COL_SQL_DATA)));
            }
            return tResult;
        } catch (Exception e) {
            throw new JSDBException(e.getMessage(), e);
        }
    }

    private int count() throws JSDBException {
        try {
            Statement stmt = mDbConnection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT count(*) as rowcount FROM " + this.mName + ";");
            int tCount = rs.getInt("rowcount");
            return tCount;

        } catch (Exception e) {
            throw new JSDBException(e.getMessage(), e);
        }
    }
    private int deleteWithJsonTesterLogic(JSDBFilter pFilter) throws JSDBException {
        // We have to check each storec object against the filter
        int tBeforeCount = this.count();

        try {
            Statement stmt = mDbConnection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT " + JSDBCollection.COL_SQL_DATA + " FROM " + this.mName + ";");
            List<JsonObject> tResult = new ArrayList<JsonObject>();

            while (rs.next()) {
                JsonObject jObject = decode(rs.getBytes(JSDBCollection.COL_SQL_DATA));
                if (pFilter.jsonMatch(jObject)) {
                    stmt = mDbConnection.createStatement();
                    boolean sts = stmt.execute("DELETE FROM " + this.mName + " WHERE ( " + getKeysValues( jObject ) + ");");
                }
            }

        } catch (Exception e) {
            throw new JSDBException(e.getMessage(), e);
        }
        return (tBeforeCount - this.count());
    }

    private int deleteWithKeyLogic(JSDBFilter pFilter) throws JSDBException {
        // Filter based upon SQL keys
        int tBeforeCount = this.count();
        String sqlSelectString = pFilter.createSqlSelectStatement(this);
        //System.out.println("select-sql: " + sqlSelectString);
        try {
            Statement stmt = mDbConnection.createStatement();
            boolean sts = stmt.execute("DELETE FROM " + this.mName + " WHERE (" + sqlSelectString + ");");
        } catch (Exception e) {
            throw new JSDBException(e.getMessage(), e);
        }
        return  tBeforeCount - this.count();
    }

    private String getKeysValues(JsonObject pObject ) throws JSDBException {
        JSDBKey k = null;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.mKeys.length - 1; i++) {
             k = this.mKeys[i];
            sb.append( k.getId() + " = " + k.getSqlValue( pObject ) + " AND ");
        }
        k = this.mKeys[mKeys.length - 1];
        sb.append( k.getId() + " = " + k.getSqlValue( pObject ));
        return sb.toString();
    }

    private void traverseAndUpdate( JsonObject pDeltaObject, JsonObject pObject) {
        Iterator<String> itr = pDeltaObject.keySet().iterator();
        while (itr.hasNext()) {
            String fid = itr.next();
            JsonElement je = pDeltaObject.get(fid);
            if (!je.isJsonObject()) {
                pObject.add(fid, je);
            } else {
                traverseAndUpdate(je.getAsJsonObject(), pObject.get(fid).getAsJsonObject());
            }
        }
    }

    private int updateWithKeyLogic( JSDBFilter pFilter, JsonObject pDeltaObject ) throws JSDBException
    {
        int tUpdatedObjects = 0;
        // Filter based upon SQL keys
        String sqlSelectString = pFilter.createSqlSelectStatement(this);
        //System.out.println("select-sql: " + sqlSelectString);
        try {
            Statement stmt = mDbConnection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT " + JSDBCollection.COL_SQL_DATA + " FROM " + this.mName + " WHERE " + sqlSelectString + ";");
            List<JsonObject> tResult = new ArrayList<JsonObject>();
            while (rs.next()) {
                JsonObject jObject = decode(rs.getBytes(JSDBCollection.COL_SQL_DATA));
                traverseAndUpdate(pDeltaObject, jObject);
                tUpdatedObjects++;
            }
            return tUpdatedObjects;
        } catch (Exception e) {
            throw new JSDBException(e.getMessage(), e);
        }
    }

    private int updateWithJsonTesterLogic( JSDBFilter pFilter, JsonObject pDeltaObject ) throws JSDBException {
        // We have to check each store object against the filter
        int tUpdatedObjects = 0;
        try {
            Statement stmt = mDbConnection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT " + JSDBCollection.COL_SQL_DATA + " FROM " + this.mName + ";");
            List<JsonObject> tResult = new ArrayList<JsonObject>();

            while (rs.next()) {
                JsonObject jObject = decode(rs.getBytes(JSDBCollection.COL_SQL_DATA));
                if (pFilter.jsonMatch(jObject)) {
                    traverseAndUpdate( pDeltaObject, jObject );
                    this.update(jObject);
                    tUpdatedObjects++;
                }
            }

        } catch (Exception e) {
            throw new JSDBException(e.getMessage(), e);
        }
        return tUpdatedObjects;
    }

    /* =================================
        Data access methods
     ================================ */

    public List<JsonObject> findAll(int pOffset, int pMaxElements) throws JSDBException {
        String sql = "SELECT * FROM " + this.mName;
        int tCount = 0;
        List<JsonObject> tResult = new ArrayList<>();
        try {
            Statement stmt = mDbConnection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next() && tResult.size() < pMaxElements) {
                if (tCount >= pOffset) {
                    tResult.add(decode(rs.getBytes(JSDBCollection.COL_SQL_DATA)));
                    if (tResult.size() >= pMaxElements) {
                        return tResult;
                    }
                }
            }
        } catch (Exception e) {
            throw new JSDBException(e.getMessage(), e);
        }
        return tResult;
    }

    public List<JsonObject> findAll() throws JSDBException {
        return findAll(0, Integer.MAX_VALUE);
    }


    public List<JsonObject> find(String pFilterString) throws JSDBException {
        JSDBFilter tFilter = new JSDBFilter(pFilterString);
        if (onlyKeyField(tFilter)) {
            return findWithKeyLogic(tFilter);
        }
        return findWithJsonTesterLogic(tFilter);
    }


    public void insert(JsonObject pObject, boolean pUpdate) throws JSDBException {
        try {
            this.verifyKeys(pObject);
            if (pUpdate) {
                stmtInsertUpdate.setParameters(pObject);
                stmtInsertUpdate.executeUpdate();
            } else {
                stmtInsert.setParameters(pObject);
                stmtInsert.executeUpdate();
            }
        } catch (Exception e) {
            throw new JSDBException(e.getMessage(), e);
        }
    }

    public void insert(JsonObject pObject) throws JSDBException {
        insert(pObject, true);
    }

    public int update(String pFilterString, JsonObject pObject ) throws JSDBException {
        JSDBFilter tFilter = new JSDBFilter(pFilterString);
        if (onlyKeyField(tFilter)) {
            return updateWithKeyLogic(tFilter, pObject);
        }
        return updateWithJsonTesterLogic(tFilter, pObject);
    }




    public int update(JsonObject pObject, boolean insert) throws JSDBException {
        try {
            this.verifyKeys(pObject);
            if (insert) {
                stmtUpdateInsert.setParameters(pObject);
                return stmtUpdateInsert.executeUpdate();
            } else {
                stmtUpdate.setParameters(pObject);
                return stmtUpdate.executeUpdate();
            }
        } catch (Exception e) {
            throw new JSDBException(e.getMessage(), e);
        }
    }

    public void update(JsonObject pObject) throws JSDBException {
        update(pObject, false);
    }

    public void deleteAll() throws JSDBException
    {
        try {
            Statement stmt = mDbConnection.createStatement();
            boolean sts = stmt.execute("DELETE FROM " + this.mName);
        } catch (Exception e) {
            throw new JSDBException(e.getMessage(), e);
        }
    }

    public int delete( String pFilterString) throws JSDBException {
        JSDBFilter tFilter = new JSDBFilter(pFilterString);
        if (onlyKeyField(tFilter)) {
            return deleteWithKeyLogic(tFilter);
        }
        return deleteWithJsonTesterLogic(tFilter);
    }


}