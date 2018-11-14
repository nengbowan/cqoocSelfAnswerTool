package net.cqooc.tool.util;

import java.util.Collections;
import java.util.List;

public class CollectionUtils {
    public static Boolean isEmpty(List list){
        return list ==null ||  list.isEmpty();
    }
    public static Boolean isNotEmpty(List list){
        return !isEmpty(list);
    }
}
