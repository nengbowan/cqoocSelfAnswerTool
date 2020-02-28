package net.cqooc.tool.util;

public class HtmlUtil {
    /**
     * 过滤掉常用标签
     * @return
     */
    public static String filterTag(String content){
        //过滤p标签
        content = content.replaceAll("<p>" , "");
        content = content.replaceAll("</p>" , "");
        //过滤空格
        content = content.replaceAll("&nbsp;" , " ");
        return content;
    }
}
