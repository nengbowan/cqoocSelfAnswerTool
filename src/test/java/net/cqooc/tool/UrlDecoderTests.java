package net.cqooc.tool;

import net.cqooc.tool.util.HtmlUtil;
import org.junit.Assert;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class UrlDecoderTests {
    public static void main(String[] args) throws UnsupportedEncodingException {
        String content = "<p>个体的人生活动不仅是具有满足自我需要的价值属性，还具有满足社会需要的价值属性。个人的需要能不能从社会中得到满足，在多大程度上得到满足，取决于他的（&nbsp; ）。</p>";
        content = HtmlUtil.filterTag(content );

        String match = "个体的人生活动不仅是具有满足自我需要的价值属性，还具有满足社会需要的价值属性。个人的需要能不能从社会中得到满足，在多大程度上得到满足，取决于他的（  ）。\t社会价值";
        match = match.replaceAll("\t" , "");

        int end =  match.lastIndexOf("。");
        String title = match.substring(0 , end + 1 );
        String answer = match.substring( end + 1  );

        Assert.assertEquals(content , title);

    }
}
