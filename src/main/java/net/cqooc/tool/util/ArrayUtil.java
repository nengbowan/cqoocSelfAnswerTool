package net.cqooc.tool.util;

public class ArrayUtil {

    public static String getArrStr(String [] arr){
        String res = "";
        if(arr != null && arr.length >0){
            int index = 0;
            for(String str:arr){
                if(index == 0){
                    res+="[";
                    res+="\"";
                    res+=str;
                    res+="\",";
                    index++;
                }else if(index == arr.length -1 ){
                    //去除冗余的,
                    if(res.lastIndexOf("," )==res.length() -1){
                        res = res.substring(0,res.lastIndexOf(","));
                    }
                    res+="]";
                    return res;
                }else{
                    res+="\"";
                    res+=str;
                    res+="\",";
                    index++;
                }

            }
            return res;
        }
        return null;
    }
}
