package net.cqooc.tool.util;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpUrlConnectionUtil {
    public static String cookies = "";
    public static String get(String requestUrl,String refererValue){
        String resp = "";
        try {
            URL url = new URL(requestUrl);
            HttpURLConnection conn = null;
            conn = (HttpURLConnection)url.openConnection();
            conn.setRequestProperty("Cookie",cookies);
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
            if(cookie!=null && !cookie.equals("") && !cookies.contains(cookie)){
                System.out.println("增加cookie:"+cookie+",requestUrl:"+requestUrl);
                cookies+=cookie;
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
            conn.setRequestProperty("Cookie",cookies);
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
            if(cookie!=null && !cookie.equals("") && !cookies.contains(cookie)){
                System.out.println("增加cookie:"+cookie+",requestUrl:"+requestUrl);
                cookies+=cookie;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resp;
    }


}
