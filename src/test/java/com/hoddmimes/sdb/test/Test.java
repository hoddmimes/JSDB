package com.hoddmimes.sdm.test;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.hoddmimes.jsql.JSDBFilter;
import com.hoddmimes.jsql.JsonTester;

import java.util.Iterator;

public class Test {
    public static void main(String[] args) {
        Test t = new Test();
        t.test();
    }

    private JsonObject createObject() {
        JsonObject jSubObj = new JsonObject();
        jSubObj.addProperty("strValue", "sub-xyz");
        jSubObj.addProperty("intValue", 101);
        jSubObj.addProperty("doubleValue", 3.14159);
        jSubObj.addProperty("longValue", 12345678901L);
        jSubObj.addProperty("boolValue", true);

        JsonObject jObj = new JsonObject();
        jObj.add("subObj", jSubObj);
        jObj.addProperty("intValue", 1);
        jObj.addProperty("boolValue", true);
        jSubObj.addProperty("strValue", "xyz");
        return jObj;
    }

    private void dbgfid(String fid, JsonElement je, int level) {
        final String blanks = "                                         ";
        System.out.println( blanks.substring(0, (level*3)) +
                            " fid: " + fid +
                            " value: " + je.getAsString() +
                            " type: " + dbgfidtype( je ));
    }

    private String dbgfidtype( JsonElement je) {
        if (je.isJsonObject()) {
            return "OBJECT";
        } else if (je.isJsonArray()) {
            return "ARRAY";
        } else if (je.isJsonPrimitive()) {
            JsonPrimitive jp = je.getAsJsonPrimitive();
            if (jp.isNumber()) {
                return "NUMBER";
            } else if (jp.isString()) {
                return "STRING";
            } else if (jp.isBoolean()) {
                return "BOOLEAN";
            } else {
                return "UNKNOWN";
         }
        } else {
            return "UNKNOWN";
        }
    }





    private void traverseAndUpdate( JsonObject pDeltaObject, JsonObject pObject, int pLevel ) {
        Iterator<String> itr = pDeltaObject.keySet().iterator();
        while (itr.hasNext()) {
            String fid = itr.next();
            JsonElement je = pDeltaObject.get(fid);
            if (!je.isJsonObject()) {
                dbgfid(fid, je, pLevel);
                pObject.add(fid, je);
                System.out.println("updated-object: "+ pObject.toString());
            } else {
                traverseAndUpdate( je.getAsJsonObject(), pObject.get( fid).getAsJsonObject(),  pLevel+1);
            }
        }
    }


    private void test() {
        Long xl = 12345678901L;

        JsonObject jObject = createObject();
        JsonObject jObj = JsonParser.parseString("{'intValue': 999, 'subObj': {'strValue' : 'new-value'}}").getAsJsonObject();
        traverseAndUpdate( jObj, jObject, 0);
        System.out.println("END-OBJECT: " + jObject.toString());



    }
}
