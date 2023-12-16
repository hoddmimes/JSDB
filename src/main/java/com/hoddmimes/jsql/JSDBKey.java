package com.hoddmimes.jsql;

import java.sql.ResultSet;
import java.sql.SQLException;

public class JSDBKey
{
        static final String KEY_SQL_ID = "ID";
        static final String KEY_SQL_CLASS = "CLASS_IS";
        static final String KEY_SQL_IS_UNIQUE = "IS_UNIQUE";
        static final String KEY_SQL_IS_PRIMARY = "IS_PRIMARY";
        static final String KEY_SQL_COLLECTION = "COLLECTION";
        static final String KEY_SQL_TABLE = "JSQL_COLLECTION_KEYS";

        static final String KEY_SQL_COLUMNS = "COLLECTION, ID, CLASS_IS, IS_UNIQUE, IS_PRIMARY";


        private final String mId;
        private final Class mType;
        private final boolean mIsUnique;
        private final boolean mIsPrimaryKey;

        public JSDBKey(String pId, Class pType, boolean pUnique, boolean pPrimaryKey) {
            mId = pId;
            mIsUnique = pUnique;
            mIsPrimaryKey = pPrimaryKey;
            if ((pType == Integer.class) || (pType == String.class) || (pType == Long.class) ||
                    (pType == Double.class) || (pType == Boolean.class)  || (pType == Float.class)) {
                mType = pType;
            } else {
                throw new IllegalArgumentException("Invalid Key Type");
            }
        }

        public JSDBKey(ResultSet rs) throws JSDBException {
            try {
                mId = rs.getString( JSDBKey.KEY_SQL_ID);
                mIsUnique = (rs.getInt(JSDBKey.KEY_SQL_IS_UNIQUE) == 1) ? true : false;
                mIsPrimaryKey = (rs.getInt(JSDBKey.KEY_SQL_IS_PRIMARY) == 1) ? true : false;
                try {mType = Class.forName(rs.getString(JSDBKey.KEY_SQL_CLASS));}
                catch(ClassNotFoundException cnf ) { throw new RuntimeException(cnf.getMessage(), cnf);}
            }
            catch(SQLException e) {
                JSDBException jse = new JSDBException(e.getMessage(), e);
                jse.fillInStackTrace();
                throw jse;
            }
        }


        public JSDBKey(String pId, Class pType) {
            this(pId, pType, true, false);
        }

        Class getType() {
            return mType;
        }

        String getSqlType() {
            if (mType == Integer.class) {
                return "INTEGER";
            }
            if (mType == String.class) {
                return "TEXT";
            }
            if (mType == Long.class) {
                return "INTEGER";
            }
            if (mType == Double.class) {
                return "REAL";
            }
            if (mType == Float.class) {
                return "REAL";
            }
            if (mType == Boolean.class) {
                return "INTEGER";
            }
            throw new IllegalArgumentException("Invalid Key Type");
        }



         public String getId() {
            return mId;
        }

        public boolean isUnique() {
            return mIsUnique;
        }


        public boolean isPrimaryKey() {
            return mIsPrimaryKey;
        }

        @Override
        public String toString() {
            return "id: " + mId + "  type: " + mType.getName() + " primary key: " + mIsPrimaryKey + " unique: " + mIsUnique;
        }
}
