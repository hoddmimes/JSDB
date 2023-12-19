package com.hoddmimes.jsql;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JSDB
{
    private Connection mDbConnection = null;


    public void openDatabase( String pSqlFile) throws JSDBException {
        String url = "jdbc:sqlite:" + pSqlFile;

        File dbFile = new File( pSqlFile );
        if (!dbFile.exists()) {
            throw new JSDBException("Database file" + pSqlFile + " is not found");
        }

        try {
            mDbConnection = DriverManager.getConnection(url);
            if (mDbConnection != null) {
                DatabaseMetaData meta = mDbConnection.getMetaData();
                //System.out.println("The driver name is " + meta.getDriverName());
                //System.out.println("Database has been opened.");
            }

        } catch (SQLException e) {
            throw new JSDBException(e);
        }
    }

    public static void createDatabase(String pSqlFile) throws JSDBException {
        String url = "jdbc:sqlite:" + pSqlFile;

        File dbFile = new File( pSqlFile );
        if (dbFile.exists()) {
            throw new JSDBException("Database file" + pSqlFile + " already exists");
        }

        try {
            Connection tDbConnection = DriverManager.getConnection(url);
            if (tDbConnection != null) {
                createCollectionTables( tDbConnection);
                tDbConnection.close();
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void createCollectionTables( Connection pConnection) throws JSDBException
    {
        String sql = "CREATE TABLE " + JSDBCollection.COL_SQL_TABLE + " (" + JSDBCollection.COL_SQL_NAME + " STRING PRIMARY KEY);";
        try {
            Statement stmt = pConnection.createStatement();
            // create a new table
            stmt.execute(sql.toString());
        }
        catch (SQLException e){
            throw new JSDBException(e.getMessage(), e);
        }

        sql = "CREATE TABLE " + JSDBKey.KEY_SQL_TABLE + " (" + JSDBKey.KEY_SQL_COLLECTION + " STRING, " + JSDBKey.KEY_SQL_ID + " STRING, " + JSDBKey.KEY_SQL_CLASS +" STRING, " +
            JSDBKey.KEY_SQL_IS_UNIQUE + " INTEGER, " + JSDBKey.KEY_SQL_IS_PRIMARY + " INTEGER);" ;
        try {
            Statement stmt = pConnection.createStatement();
            // create a new table
            stmt.execute(sql.toString());
        }
        catch (SQLException e){
            throw new JSDBException(e.getMessage(), e);
        }
    }


    public void closeDatabase() {
        try {
            if (mDbConnection != null) {
                mDbConnection.close();
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }


    private boolean checkIfCollectionExists(String pName ) throws JSDBException {
        String sql = "SELECT NAME FROM " + JSDBCollection.COL_SQL_TABLE + " WHERE NAME = '" + pName + "'";
        try {
            Statement stmt = mDbConnection.createStatement();
            // create a new table
            ResultSet tResult = stmt.executeQuery(sql.toString());
            return tResult.next();
        }
        catch (SQLException e){
            throw new JSDBException(e.getMessage(), e);
        }
    }
    public JSDBCollection createCollection(String pName, JSDBKey... pKeys ) throws JSDBException {
        StringBuilder sql = new StringBuilder();
        StringBuilder sqlKeys = new StringBuilder();


        if (checkIfCollectionExists( pName )){
            throw new JSDBException("Collection \"" + pName + "\" already exists");
        }
        JSDBCollection tCollection = new JSDBCollection( this.mDbConnection, pName, pKeys );

        sql.append("CREATE TABLE " + pName + "( ");
        for( JSDBKey k : pKeys ) {
            if (k.isPrimaryKey()) {
                sqlKeys.append( k.getId() + " " + k.getSqlType() + " PRIMARY KEY, ");
            } else {
                sqlKeys.append( k.getId() + " " + k.getSqlType());
                if (k.isUnique()) {
                    sqlKeys.append( " UNIQUE");
                }
                sqlKeys.append(" NOT NULL, ");
            }
        }
        sql.append( sqlKeys );
        sql.append ( "DATA BLOB NOT NULL );");
        try {
            Statement stmt = mDbConnection.createStatement();
            // create a new table
            stmt.execute(sql.toString());
        }
        catch (SQLException e){
            throw new JSDBException(e.getMessage(), e);
        }
        // Create index for alternative keys
        for(JSDBKey k : pKeys) {
            sql = new StringBuilder("CREATE ");
            if (k.isUnique()) {
                sql.append("UNIQUE ");
            }
            sql.append(" INDEX idx_"  + k.getId() + " ON " + pName +"( " + k.getId() + " )" );
            try {
                Statement stmt = mDbConnection.createStatement();
                // create a new table
                stmt.execute(sql.toString());
            }
            catch (SQLException e){
                JSDBException jse = new JSDBException(e.getMessage(), e);
                jse.fillInStackTrace();
                throw jse;
            }
        }
        saveCollection( tCollection );
        return tCollection;
    }

    public JSDBCollection getCollections(String pName) throws JSDBException {
        String sql = "SELECT * FROM " + JSDBCollection.COL_SQL_TABLE + " WHERE " + JSDBCollection.COL_SQL_NAME+ "=\"" + pName + "\"";
        JSDBCollection tCollection;
        try {
            Statement stmt = mDbConnection.createStatement();
            ResultSet tResultCollection = stmt.executeQuery(sql);
            if (!tResultCollection.next()) {
                throw new JSDBException("Collection \"" +pName + "\" is not found" );
            }
            sql = "SELECT * FROM " + JSDBKey.KEY_SQL_TABLE + " WHERE " + JSDBKey.KEY_SQL_COLLECTION + "=\"" + pName + "\"";
            stmt = mDbConnection.createStatement();
            ResultSet tResultkeys = stmt.executeQuery(sql);
            List<JSDBKey> tKeys = new ArrayList<>();
            while(tResultkeys.next()) {
                tKeys.add( new JSDBKey(tResultkeys));
            }
            return new JSDBCollection( this.mDbConnection, pName, tKeys.toArray(new JSDBKey[0]));
        }
        catch (SQLException e){
            throw new JSDBException(e.getMessage(), e);
        }
    }

    public List<JSDBCollection> getCollections() throws JSDBException {
        String sql = "SELECT * FROM " + JSDBCollection.COL_SQL_TABLE + ";";
        ResultSet tResultCollections,tResultkeys;
        ArrayList<JSDBCollection> tCollections = new ArrayList<>();

        try {
            Statement stmt = mDbConnection.createStatement();
            tResultCollections = stmt.executeQuery(sql);
            while(tResultCollections.next()) {
                String tName = tResultCollections.getString(JSDBCollection.COL_SQL_NAME);
                sql = "SELECT * FROM " + JSDBKey.KEY_SQL_TABLE + " WHERE " + JSDBKey.KEY_SQL_COLLECTION + "=\"" + tName + "\"";
                stmt = mDbConnection.createStatement();
                tResultkeys = stmt.executeQuery(sql);
                List<JSDBKey> tKeys = new ArrayList<>();
                while(tResultkeys.next()) {
                    tKeys.add( new JSDBKey(tResultkeys));
                }
                tCollections.add( new JSDBCollection( this.mDbConnection, tName,tKeys.toArray(new JSDBKey[0])));
            }
        }
        catch (SQLException e){
            throw new JSDBException(e.getMessage(), e);
        }
        return tCollections;
    }

    private void saveCollection(JSDBCollection pCollection ) throws JSDBException {
        String sql = "INSERT INTO " + JSDBCollection.COL_SQL_TABLE + " (NAME) VALUES (\"" + pCollection.mName + "\");";
        try {
            Statement stmt = mDbConnection.createStatement();
            // create a new table
            int sts = stmt.executeUpdate(sql.toString());
        } catch (SQLException e) {
            throw new JSDBException(e.getMessage(), e);
        }

        for (JSDBKey k : pCollection.mKeys) {
            sql = "INSERT INTO " + JSDBKey.KEY_SQL_TABLE + " ( " + JSDBKey.KEY_SQL_COLLECTION + "," + JSDBKey.KEY_SQL_ID + ","  + JSDBKey.KEY_SQL_CLASS + "," + JSDBKey.KEY_SQL_IS_UNIQUE + "," + JSDBKey.KEY_SQL_IS_PRIMARY + ") VALUES (" +
                    "\"" + pCollection.mName + "\", " +
                    "\"" + k.getId() + "\", " +
                    "\"" + k.getType().getName() + "\", " +
                    getSqlBooleanValue(k.isUnique()) + ", " +
                    getSqlBooleanValue(k.isUnique()) + ");";
            try {
                Statement stmt = mDbConnection.createStatement();
                // create a new table
                int sts = stmt.executeUpdate(sql.toString());
            } catch (SQLException e) {
                throw new JSDBException(e.getMessage(), e);
            }
        }
    }


    private String getSqlBooleanValue( boolean pValue) {
        return (pValue) ? "TRUE" : "FALSE";
    }

}
