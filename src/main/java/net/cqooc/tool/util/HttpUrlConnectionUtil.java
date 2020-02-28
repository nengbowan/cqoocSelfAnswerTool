package net.cqooc.tool.util;

import net.cqooc.tool.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

public class HttpUrlConnectionUtil {

    private static Logger logger = LoggerFactory.getLogger(HttpUrlConnectionUtil.class);
    //threadlocal 多线程
//    public static String cookies = "";
    public static ThreadLocal<String> threadLocal = new ThreadLocal<String>(

    ).withInitial(
            ()-> ""
    );
    public static String get(String requestUrl,String refererValue){
        String resp = "";
        try {
            URL url = new URL(requestUrl);
            HttpURLConnection conn = null;
            conn = (HttpURLConnection)url.openConnection();
            conn.setRequestProperty("Cookie",threadLocal.get());
            if(StringUtils.isEmpty(refererValue)){
                conn.setRequestProperty("Referer","http://www.cqooc.net/my/learn");
            }else{
                conn.setRequestProperty("Referer",refererValue);
            }
            conn.setRequestProperty("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10.13; rv:73.0) Gecko/20100101 Firefox/73.0");

            //指定页面编码读取　否则乱码
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF8"));
            String cache = "";

            while((cache = br.readLine()) != null){
                resp+=cache;
            }
            //处理cookie 头
            String cookie = conn.getHeaderField("Set-Cookie");
            if(cookie!=null && !cookie.equals("") && !threadLocal.get().contains(cookie)){
                logger.info("增加cookie:"+cookie+",requestUrl:"+requestUrl);
                threadLocal.set(threadLocal.get() + cookie);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resp;
    }


    public static String post(String requestUrl,String body,String referer , String userAgent){
        String resp = "";
        try {
            URL url = new URL(requestUrl);
            HttpURLConnection conn = null;
            conn = (HttpURLConnection)url.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestProperty("Cookie",threadLocal.get());
            if(StringUtils.isEmpty(referer) ){
                String defaultReferer = "http://www.cqooc.net/learn/mooc/structure?id=334566120";
                conn.setRequestProperty("Referer",defaultReferer);
            }else{
                conn.setRequestProperty("Referer",referer);
            }
            if(StringUtils.isEmpty(userAgent) ){
                String defaultAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.13; rv:70.0) Gecko/20100101 Firefox/70.0";
                conn.setRequestProperty("User-Agent",defaultAgent);
            }else{
                conn.setRequestProperty("User-Agent",userAgent);
            }
            conn.setRequestProperty("Content-Type","application/json");

            PrintWriter pw = new PrintWriter(new OutputStreamWriter(conn.getOutputStream()));
            pw.println(body);
            pw.flush();
            //指定页面编码读取　否则乱码
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF8"));
            String cache = "";

            while((cache = br.readLine()) != null){
                resp+=cache;
            }
            //处理cookie 头
            String cookie = conn.getHeaderField("Set-Cookie");
            if(cookie!=null && !cookie.equals("") && !threadLocal.get().contains(cookie)){

                System.out.println("增加cookie:"+cookie+",requestUrl:"+requestUrl);
               threadLocal.set(threadLocal.get() + cookie);
            }
        } catch (IOException e) {
            //服务器异常

            if(e.getMessage().contains("503")){
//                logger.error("服务器异常, 触发重试机制 ,,,");

                return post(requestUrl , body , referer , userAgent);
            }
            //不建议在异常里面做逻辑业务 小型系统可以
            e.printStackTrace();

        }
        return resp;
    }


}
