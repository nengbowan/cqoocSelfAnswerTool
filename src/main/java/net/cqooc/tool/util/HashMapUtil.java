package net.cqooc.tool.util;

import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class HashMapUtil<K,V> {
    public HashMapUtil(){
        map = new HashMap();
    }
    private HashMap<K,V> map = null;
    public HashMapUtil put(K key , V value){
        map.put(key,value);
        return this;
    }
    public String toJsonStr(){
        return JSONObject.toJSONString(this.map);
    }
    public static String getCookieByMap(HashMap<String, String> cookieMap) {
        StringBuffer cookieStr = new StringBuffer();
        if (cookieMap != null && cookieMap.size() != 0) {
            for (Map.Entry<String, String> entry : cookieMap.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                cookieStr.append(key + "=" + value + ";");
            }
        }
        return cookieStr.toString();
    }

    public HashMap<K, V> map(){
        return this.map;
    }

}
