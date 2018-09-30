package net.cqooc.tool.util;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonUtil {

    static {
        //设置属性不可设置的时候 不报错
//        objectMapper.set
    }
    public static ObjectMapper objectMapper = new ObjectMapper();

}
