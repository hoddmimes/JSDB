package com.hoddmimes.jsql;

import com.google.gson.JsonObject;

import java.sql.SQLException;

public class JStatementUpdate extends JPreparedStatement
{

    public JStatementUpdate(JSDBCollection pCollection) throws JSDBException{
        super( pCollection );
    }

    @Override
    String getSqlStatement()
    {
        String sqlStatement = "UPDATE " + mCollection.mName + " SET " + JSDBCollection.COL_SQL_DATA + " = ? WHERE  " + mCollection.buildPrepareSelectParameters() + ";";
        return  sqlStatement;
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
                setKeyParameter((i+2), mCollection.mKeys[i], jObject);
            }
            if (JSDB.USE_JCODEC) {
                JEncoder jEncoder = new JEncoder();
                mPreparedStatement.setBytes(1, jEncoder.encode(jObject));
            } else {
                mPreparedStatement.setString(1, jObject.toString().replace('"', '\''));
            }
        }
        catch( SQLException e) {
            throw new JSDBException(e.getMessage(), e);
        }
    }
}
