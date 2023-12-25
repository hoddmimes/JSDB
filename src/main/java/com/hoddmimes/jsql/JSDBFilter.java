package com.hoddmimes.jsql;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class JSDBFilter
{
        static enum FILTER_LOGIC {$AND,$OR,$GT,$GTE,$LT,$LTE,$EQ,$LIKE,$NE,$DATA};
        private static final Pattern FILTER_LOGIC_PATTERN = Pattern.compile("(^\\$AND:|^\\$OR:|^\\$GT:|^\\$GTE:|^\\$LT:|^\\$LTE:|^\\$LIKE:|^\\$EQ:|^\\$NE:)", Pattern.CASE_INSENSITIVE);
        private static final Pattern FILTER_NAME_VALUE = Pattern.compile( "([\\w|\\.]+)\\s*,(.+)");
         private  String mFilterString;

        private FilterNode mRootNode;


        public static void main(String[] args) {
            try {
                JSDBFilter filter = new JSDBFilter("($and: ($eq: (foo, 'kalle kula')) ($gt: (bar, 100)))");

                System.out.println(filter.toString());
                List<String> fields = filter.getDataFields();
                fields.stream().forEach( fid -> System.out.println("   field: " + fid));
            }
            catch( JSDBException e) {
                e.printStackTrace();
            }
        }

    public JSDBFilter( String pFilterString ) throws JSDBException {
        mFilterString = pFilterString;
        mRootNode = parse( pFilterString );
    }
    
    public String getFilterString() {
      return mFilterString;
    }

        @Override
        public String toString() {
            return displayNode( mRootNode );
        }

        private String displayNode(FilterNode pNode) {
            StringBuilder sb = new StringBuilder();
            sb.append( pNode.toString());
            for (FilterNode n : pNode.mChildren) {
                sb.append(n);
            }
            return sb.toString();
        }



        private FilterNode parse(String pPattern) throws JSDBException {
            int pos = 0;
            String tPattern = pPattern.trim();
            if (tPattern.charAt(0) != '(') {
                throw new JSDBException("Invalid filter syntax, must start with '('");
            }
            FilterNode n = parse(pPattern.substring(1), 0);
            parseLogicalExpression(n);
            return n;
        }

        private void parseLogicalExpression( FilterNode n) {
            Matcher m = FILTER_LOGIC_PATTERN.matcher(n.mData.toString().trim());
            if (m.find()) {
                n.mLogic = FILTER_LOGIC.valueOf(n.mData.toString().trim().substring(m.regionStart(), m.regionEnd() - 1).toUpperCase());
            } else {
                n.mLogic = FILTER_LOGIC.$DATA;
            }
            //System.out.println(n);
            for (FilterNode cn : n.mChildren) {
                parseLogicalExpression( cn );
            }
        }
        
        
        private boolean toPrimarFilter( JSDBCollection pCollection) throws JSDBException {
            this.mRootNode = this.toPrimaryKeys(this.mRootNode, pCollection);
            this.mFilterString = this.updateSqlString(this.mRootNode, pCollection);
            return (this.mRootNode != null) ? true : false;
        }

        private String updateSqlString( FilterNode pNode, JSDBCollection pCollection) throws JSDBException {
            if ((pNode.mLogic == FILTER_LOGIC.$AND) || (pNode.mLogic == FILTER_LOGIC.$OR)) {
                StringBuilder sb = new StringBuilder();
                sb.append("(" + pNode.mLogic.toString() + ": ");
                for( FilterNode cn : pNode.mChildren ) {
                    sb.append( updateSqlString( cn, pCollection ));
                }
                sb.append(")");
                return sb.toString();
            } else {
                StringBuilder sb = new StringBuilder();
                JSDBKey tKey = pCollection.getKey( pNode.mChildren.get(0).getFieldId());
                FilterNode cn = pNode.mChildren.get(0);

                sb.append("(" + pNode.mLogic.toString() + ": ");
                if (tKey.getType() == String.class) {
                    sb.append("(" + cn.getFieldId() + ", '" + cn.getFieldValue() + "'))");
                } else
                    sb.append("(" + cn.getFieldId() + ", " + cn.getFieldValue() + "))");
                return sb.toString();
            }
        }

        private FilterNode toPrimaryKeys( FilterNode pNode, JSDBCollection pCollection) throws JSDBException
        {
            if ((pNode.mLogic == FILTER_LOGIC.$AND) || (pNode.mLogic == FILTER_LOGIC.$OR)) {
                Iterator<FilterNode> tItr = pNode.mChildren.iterator();
                while (tItr.hasNext()) {
                    if (toPrimaryKeys(tItr.next(), pCollection) == null) {
                        tItr.remove();
                    }
                }
                if (pNode.mChildren.size() >= 2) {
                    return pNode;
                } else if (pNode.mChildren.size() == 1) {
                    return pNode.mChildren.get(0);
                }
                return null;
            } else {
                FilterNode cn = pNode.mChildren.get(0);
                return (pCollection.isKeyField(cn.getFieldId())) ? pNode : null;
            }
        }
        

       String createSqlHelperStatement( JSDBCollection pCollection ) throws JSDBException {
           JSDBFilter tFilter = new JSDBFilter(this.getFilterString());
           if(tFilter.toPrimarFilter(pCollection)) {
               return "SELECT DATA FROM " + pCollection.mName + " WHERE " + tFilter.createSqlSelectStatement(pCollection) + ";";
           }
           return "SELECT DATA FROM " + pCollection.mName + ";";
       }
       

       String createSqlSelectStatement( JSDBCollection pCollection ) throws JSDBException
       {
            String sql = createSqlSelectStatement( mRootNode, pCollection );
            return sql;
       }

        private String createSqlSelectStatement(FilterNode pNode, JSDBCollection pCollection ) throws JSDBException {
            if ((pNode.mLogic == FILTER_LOGIC.$AND) || (pNode.mLogic == FILTER_LOGIC.$OR)) {
               if (pNode.mChildren.size() < 2) {
                   throw new JSDBException("Invalid number of AND/OR arguments must be at least 2");
               }
               StringBuilder sb = new StringBuilder("( ");
               for (int i = 0; i < pNode.mChildren.size() - 1; i++) {
                    FilterNode cn = pNode.mChildren.get(i);
                    if (cn.mLogic == FILTER_LOGIC.$DATA) {
                        throw new JSDBException("Invalid AND/OR argument, must not be a DATA node");
                    }
                    sb.append( createSqlSelectStatement(cn, pCollection) + " " + getSqlOperator( pNode.mLogic) + " ");
                }
                sb.append( createSqlSelectStatement(pNode.mChildren.get(pNode.mChildren.size() - 1), pCollection) + " )");
                return sb.toString();
            } else if (pNode.mLogic != FILTER_LOGIC.$DATA) {
                // Verify that there is just one child node which must be a $DATA node
                if ((pNode.mChildren.size() != 1) || (pNode.mChildren.get(0).mLogic != FILTER_LOGIC.$DATA)) {
                    throw new JSDBException("invalid filter syntax, can not create sql key select statement for data node");
                }
                JSDBKey tKey = pCollection.getKey( pNode.mChildren.get(0).getFieldId());
                validateOperatorForDataType( pNode.mLogic,tKey.getType());

                if (tKey.getType() == String.class) {
                    return pNode.mChildren.get(0).getFieldId() + " " + getSqlOperator( pNode.mLogic) + " '" +  pNode.mChildren.get(0).getFieldValue() + "'";
                } else {
                    return pNode.mChildren.get(0).getFieldId() + " " + getSqlOperator( pNode.mLogic) + " " +  pNode.mChildren.get(0).getFieldValue();
                }
            }
            return null;
    }

       private String getSqlOperator( FILTER_LOGIC pLogic ) {
            switch (pLogic) {
                case $AND:
                    return "AND";
                case $EQ:
                    return "=";
                case $GT:
                    return ">";
                case $LT:
                    return "<";
                case $LTE:
                    return "<=";
                case $NE:
                    return "!=";
                case $OR:
                    return "OR";
                case $GTE:
                    return ">=";
                case $LIKE:
                    return "LIKE";
            }
            throw new RuntimeException("Invaliv filter syntax, invalid operator");
       }

       private void validateOperatorForDataType(FILTER_LOGIC pLogic, Class pClass) throws JSDBException {
            if (pClass == Boolean.class) {
                if ((pLogic != FILTER_LOGIC.$NE) && (pLogic != FILTER_LOGIC.$EQ)) {
                  throw new JSDBException("Invalid filter syntax, invalid operator for boolean key");
                }
            } else if ((pClass == Integer.class) || (pClass == Double.class) || (pClass == Long.class) || (pClass == Float.class)) {
                if (pLogic == FILTER_LOGIC.$LIKE) {
                    throw new JSDBException("Invalid filter syntax, invalid operator for numeric key (i.e LIKE is not allowed)");
                }
            }
       }




        private String extractString( String pPattern ) {
            int pos = 0;
            char endChar = pPattern.charAt(0);
            while (pPattern.charAt(++pos) != endChar);
            return pPattern.substring(0, pos+1);
        }
        List<String> getDataFields() throws JSDBException {
            return getDataFields(mRootNode);
        }

        private List<String> getDataFields( FilterNode pNode ) throws JSDBException {
            List<String> tFields = new ArrayList<>();
            if (pNode.mLogic == FILTER_LOGIC.$DATA) {
                tFields.add( pNode.getFieldId());
            }
            for (FilterNode cn : pNode.mChildren) {
                tFields.addAll( getDataFields( cn ));
            }
            return tFields;
        }

        private FilterNode parse(String pPattern, int pLevel) throws JSDBException {
            char tWithInString = 0;
            FilterNode n = new FilterNode(pLevel);
            int pos = 0, start = 1;

            try {
                while (true) {
                    // Check if String marker
                    if ((pPattern.charAt(pos) == '\'') || (pPattern.charAt(pos) == '"')) {
                        String tStr = extractString(pPattern.substring(pos));
                        n.mData.append(tStr);
                        pos += tStr.length();
                    }

                    // Check if Node start
                    else if (pPattern.charAt(pos) == '(') {
                        FilterNode cn = parse(pPattern.substring(++pos), pLevel + 1);
                        n.mChildren.add(cn);
                        pos += cn.mLength;
                    }

                    // Check if Node end
                    else if (pPattern.charAt(pos) == ')') {
                        n.mLength = pos + 1;
                        return n;
                    } else {
                        n.mData.append(pPattern.charAt(pos));
                        pos++;
                    }
                }
            }
            catch( StringIndexOutOfBoundsException e) {
                throw new JSDBException("Invalid filter syntax");
            }
        }




        public boolean jsonMatch( JsonObject jObject ) throws JSDBException {
            return jsonMatch(jObject, this.mRootNode, new JsonTester());
        }

        private boolean jsonMatch(JsonObject jObject, FilterNode pNode, JsonTester jt) throws JSDBException {
            if ((pNode.mLogic == FILTER_LOGIC.$AND) || (pNode.mLogic == FILTER_LOGIC.$OR)) {
                if (pNode.mChildren.size() < 2) {
                    throw new JSDBException("Invalid number of AND/OR arguments must be at least 2");
                }
                boolean tState = pNode.mLogic == FILTER_LOGIC.$AND;
                for( FilterNode cn : pNode.mChildren) {
                    tState = jt.jsonTest( pNode.mLogic, tState, jsonMatch(jObject, cn, jt));
                }
                return tState;
            } else if (pNode.mLogic != FILTER_LOGIC.$DATA) {
                if ((pNode.mChildren.size() != 1) || (pNode.mChildren.get(0).mLogic != FILTER_LOGIC.$DATA)) {
                    throw new JSDBException("invalid filter syntax, can not create sql key select statement for data node");
                }
                String fid = pNode.mChildren.get(0).getFieldId();
                JsonElement je = JsonTester.getJsonElement( fid, jObject);
                if (je == null) {
                    return false;
                }
                Object objValue = castJasonPrimetiveToJavaObject( je.getAsJsonPrimitive());
                Object fltValue = castStringToJavaObject( objValue.getClass(), pNode.mChildren.get(0).getFieldValue());
                return jt.jsonTest(pNode.mLogic, objValue, fltValue);
            }
            return false;
        }

    private static Object castJasonPrimetiveToJavaObject( JsonPrimitive jPrimitive ) {
        if (jPrimitive.isBoolean()) {
            return jPrimitive.getAsBoolean();
        }
        if (jPrimitive.isString()) {
            return jPrimitive.getAsString();
        }
        if (jPrimitive.isNumber()) {
            double numberValue = jPrimitive.getAsDouble();
            if ((int) numberValue== numberValue) {
                return Integer.valueOf((int) numberValue);
            } else if ((long) numberValue== numberValue) {
                return Long.valueOf((long) numberValue);
            } else if ((float) numberValue== numberValue) {
                return Float.valueOf((float) numberValue);
            } else {
                return Double.valueOf((double) numberValue);
            }
        }
        throw new RuntimeException("Invalid JsonPrimitive ");
    }

    private static Object castStringToJavaObject( Class pClass, String pStringValue )
    {
        if (pClass == Long.class) {
            return Long.valueOf(pStringValue);
        }
        if (pClass == Integer.class) {
            return Integer.valueOf(pStringValue);
        }
        if (pClass == Float.class) {
            return Float.valueOf(pStringValue);
        }
        if (pClass == Double.class) {
            return Double.valueOf(pStringValue);
        }
        if (pClass == String.class) {
            return pStringValue;
        }
        if (pClass == Boolean.class) {
            return Boolean.valueOf(pStringValue);
        }
        throw new RuntimeException("Invalid datatype  ");
    }

        static class FilterNode
        {
            int mLevel;
            StringBuilder mData;
            List<FilterNode> mChildren;
            int mLength;
            FILTER_LOGIC mLogic;
            String mSqlCondition = null;


            FilterNode(int pLevel) {
                mLevel = pLevel;
                mData = new StringBuilder();
                mChildren = new ArrayList<>();
                mLength = 0;
            }

            public String toString() {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < mLevel; i++) {
                    sb.append(" ");
                }
                sb.append("Level: " + mLevel + " data: " + mData.toString() + " logic: " + mLogic.toString() );
                return sb.toString();
            }

            String getFieldId() throws JSDBException {
                if (mLogic != FILTER_LOGIC.$DATA) {
                    throw new JSDBException("Filter node is not a data node");
                }
                Matcher m = FILTER_NAME_VALUE.matcher(mData.toString().trim());
                if (m.find()) {
                    return m.group(1);
                } else {
                    throw new JSDBException("Invalid data name/value (" + mData.toString() + ")");
                }
            }

            String getFieldValue() throws JSDBException {
                if (mLogic != FILTER_LOGIC.$DATA) {
                    throw new JSDBException("Filter node is not a data node");
                }
                Matcher m = FILTER_NAME_VALUE.matcher(mData.toString().trim());
                if (m.matches()) {
                    String tStr = m.group(2).trim();
                    if ((tStr.charAt(0) == '\'') && (tStr.charAt( tStr.length() - 1) == '\'')) {
                        return tStr.substring(1, tStr.length() - 1);
                    } else if ((tStr.charAt(0) == '"') && (tStr.charAt( tStr.length() - 1) == '"')) {
                        return tStr.substring(1, tStr.length() - 1);
                    } else {
                        return tStr;
                    }

                } else {
                    throw new JSDBException("Invalid data name/value (" + mData.toString() + ")");
                }
            }
        }

    }
