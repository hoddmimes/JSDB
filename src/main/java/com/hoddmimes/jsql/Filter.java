package com.hoddmimes.jsql;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Filter
{
        enum FILT_LOGIC {$AND,$OR,$GT,$GTE,$LT,$LTE,$EQ,$NE,$DATA};
        private Pattern FILTER_LOGIC_PATTERN = Pattern.compile("(^\\$AND:|^\\$OR:|^\\$GT:|^\\$GTE:|^\\$LT:|^\\$LTE:|^\\$EQ:|^\\$NE:)", Pattern.CASE_INSENSITIVE);
        private Pattern FILTER_NAME_VALUE = Pattern.compile("(\\w+)\\s*,(.+)");

        public static void main(String[] args) {
            Filter f = new Filter();
            try {
                Node n = f.parse("($and: ($eq: (foo, 'kalle kula')) ($gt: (bar, 100)))");
                f.displayNode(n);
                List<String> fields = f.getFields(n);
                fields.stream().forEach( fid -> System.out.println("   field: " + fid));
            }
            catch( JSDBException e) {
                e.printStackTrace();
            }
        }

        private void displayNode(Node pNode) {
            System.out.println(pNode.toString());
            for (Node n : pNode.mChildren) {
                displayNode(n);
            }
        }



        Node parse(String pPattern) throws JSDBException {
            int pos = 0;
            String tPattern = pPattern.trim();
            if (tPattern.charAt(0) != '(') {
                throw new JSDBException("Invalid filter syntax, must start with '('");
            }
            Node n = parse(pPattern.substring(1), 0);
            parseLogicalExpression(n);
            return n;
        }

        private void parseLogicalExpression( Node n) {
            Matcher m = FILTER_LOGIC_PATTERN.matcher(n.mData.toString().trim());
            if (m.find()) {
                n.mLogic = FILT_LOGIC.valueOf(n.mData.toString().trim().substring(m.regionStart(), m.regionEnd() - 1).toUpperCase());
            } else {
                n.mLogic = FILT_LOGIC.$DATA;
            }
            System.out.println(n);
            for (Node cn : n.mChildren) {
                parseLogicalExpression( cn );
            }

        }

        private String extractString( String pPattern ) {
            int pos = 0;
            char endChar = pPattern.charAt(0);
            while (pPattern.charAt(++pos) != endChar);
            return pPattern.substring(0, pos+1);
        }

        List<String> getFields( Node pNode ) throws JSDBException {
            List<String> tFields = new ArrayList<>();
            if (pNode.mLogic == FILT_LOGIC.$DATA) {
                tFields.add( pNode.getFieldId());
            }
            for (Node cn : pNode.mChildren) {
                tFields.addAll( getFields( cn ));
            }
            return tFields;
        }

        Node parse(String pPattern, int pLevel) throws JSDBException {
            char tWithInString = 0;
            Node n = new Node(pLevel);
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
                        Node cn = parse(pPattern.substring(++pos), pLevel + 1);
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






        class Node
        {
            int mLevel;
            StringBuilder mData;
            List<Node> mChildren;
            int mLength;
            FILT_LOGIC mLogic;


            Node(int pLevel) {
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
                if (mLogic != FILT_LOGIC.$DATA) {
                    throw new JSDBException("Filter node is not a data node");
                }
                Matcher m = FILTER_NAME_VALUE.matcher(mData.toString().trim());
                if (m.matches()) {
                    return m.group(1);
                } else {
                    throw new JSDBException("Invalid data name/value (" + mData.toString() + ")");
                }
            }

            String getFieldValue() throws JSDBException {
                if (mLogic != FILT_LOGIC.$DATA) {
                    throw new JSDBException("Filter node is not a data node");
                }
                Matcher m = FILTER_NAME_VALUE.matcher(mData.toString().trim());
                if (!m.matches()) {
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
