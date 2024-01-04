package com.hoddmimes.jsql;

import com.google.gson.JsonObject;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

public class JStatementInsert extends JPreparedStatement
{

    public JStatementInsert(JSDBCollection pCollection) throws JSDBException{
        super( pCollection );
    }

    @Override
    String getSqlStatement()
    {
        String sqlString = "INSERT INTO " + mCollection.getName() + "(" + mCollection.getKeyList() + ") VALUES (" + mCollection.buildPrepareParameters() + ");";
        return sqlString;
    }

    @Override
    public void setParameters( Object pParameterObject ) throws JSDBException {
        if (!(pParameterObject instanceof JsonObject)) {
            throw new JSDBException("Invalid parameter, must be JsonObject when calling SetParameter method for JStatementInsert");
        }
        try {
            mPreparedStatement.clearParameters();
            JsonObject jObject = (JsonObject) pParameterObject;
            for (int i = 0; i < mCollection.mKeys.length; i++)
            {
                setKeyParameter((i+1), mCollection.mKeys[i], jObject);
            }
            if (JSDB.USE_JCODEC) {
                JEncoder jEncoder = new JEncoder();
                mPreparedStatement.setBytes(mCollection.mKeys.length + 1, jEncoder.encode(jObject));
            } else {
                mPreparedStatement.setString(mCollection.mKeys.length + 1, jObject.toString().replace('"', '\''));
            }
        }
        catch( SQLException e) {
            throw new JSDBException(e.getMessage(), e);
        }
    }
}
