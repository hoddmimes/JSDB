package com.hoddmimes.jsql;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class JPreparedStatement
{
    protected PreparedStatement mPreparedStatement;
    protected JSDBCollection mCollection;

    public JPreparedStatement( JSDBCollection pCollection ) throws JSDBException {
        mCollection = pCollection;
        try {
            if (getSqlStatement() != null) {
                mPreparedStatement = pCollection.mDbConnection.prepareStatement(getSqlStatement());
            } else {
                mPreparedStatement = null;
            }
        } catch (SQLException e) {
            throw new JSDBException(e.getMessage(), e);
        }
    }

    abstract String getSqlStatement();

    // A parameterObject is either a JsonObject or a JSDBFilter
    public abstract void setParameters( Object pParameterObject ) throws JSDBException;


    void setKeyParameter( int pIndex, JSDBKey pKey, JsonObject jObject ) throws JSDBException {
        if (!(jObject.has( pKey.getId()))) {
            throw new JSDBException("Object does not contain key \"" + pKey.getId() + "\"");
        }
        try {
            JsonElement jValue = jObject.get(pKey.getId());
            if (pKey.getType() == Integer.class) {
                mPreparedStatement.setInt(pIndex, jValue.getAsInt());
            } else if (pKey.getType() == String.class) {
                mPreparedStatement.setString(pIndex, jValue.getAsString());
            } else if (pKey.getType() == Double.class) {
                mPreparedStatement.setDouble(pIndex, jValue.getAsDouble());
            } else if (pKey.getType() == Boolean.class) {
                mPreparedStatement.setBoolean(pIndex, jValue.getAsBoolean());
            } else if (pKey.getType() == Long.class) {
                mPreparedStatement.setLong(pIndex, jValue.getAsLong());
            }  else if (pKey.getType() == Float.class) {
                mPreparedStatement.setFloat(pIndex, jValue.getAsFloat());
            }
        }
        catch( SQLException e) {
            throw new JSDBException(e);
        }
    }


    public boolean execute() throws SQLException
    {
        return mPreparedStatement.execute();
    }

    public int executeUpdate() throws SQLException
    {
        return mPreparedStatement.executeUpdate();
    }

    public ResultSet executeQuery() throws SQLException
    {
        return mPreparedStatement.executeQuery();
    }
}
