package net.cqooc.tool.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class JsonObjectUtil {
    public static String getAttrValue(String json , String key){
        return JSONObject.parseObject(json).getString(key);
    }

    public static String getAttrValue(String json , String key , String sonKey){
        JSONObject obj = JSONObject.parseObject(json).getJSONObject(key);
        if(obj != null){
            return obj.getString(sonKey);
        }else{
            return null;
        }
    }


    public static String getArray(String json,String key,  int index , String attrKey){
        JSONObject obj = JSONObject.parseObject(json);
        if(obj != null){
            JSONArray arr = obj.getJSONArray(key);
            JSONObject sonObj = arr.getJSONObject(index);
            if(sonObj != null){
                return sonObj.get(attrKey).toString();
            }
        }
        return null;
    }
}
