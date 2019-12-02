package net.cqooc.tool.util;

import net.cqooc.tool.dto.ResultDTO;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HttpClientUtil {

    public static String getOrPost( HttpRequestBase getOrPost , HttpClient httpClient){
        try{
            HttpResponse response = httpClient.execute(getOrPost);
            HttpEntity httpEntity = response.getEntity();
            String result = EntityUtils.toString(httpEntity , Charset.defaultCharset());
            return result;
        }catch (Exception e){
            e.printStackTrace();
        }
       return null;
    }

    public static String getPageByURLAndCookie(String url,Map<String,String> headerParams,Map<String,String> postParams){
        try{
            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(url);
            if(headerParams != null && headerParams.size()>0){
                for(Map.Entry<String,String> entry : headerParams.entrySet()){
                    if(entry.getKey().equalsIgnoreCase("Cookie")){
                        httpGet.addHeader(new BasicHeader(entry.getKey() , "xsid="+entry.getValue()));
                    }else{
                        httpGet.addHeader(new BasicHeader(entry.getKey() , entry.getValue()));
                    }
                }
            }

            CloseableHttpResponse response = httpclient.execute(httpGet);
            HttpEntity entity2 = response.getEntity();
            String result = EntityUtils.toString(entity2 , Charset.defaultCharset());
            return result;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    public static String getResByUrlAndCookie(String url , Map<String,String> headerParams , String cookie , boolean getCookie)  {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        Header header = new BasicHeader("Cookie",cookie);
        httpGet.addHeader(header);
        if(headerParams != null && headerParams.size() >0){
            for(Map.Entry<String, String> entry : headerParams.entrySet()){
                Header basicHeader = new BasicHeader(entry.getKey() , entry.getValue());
                httpGet.addHeader(basicHeader);
            }
        }
        CloseableHttpResponse response2 = null;
        try {
            response2 = httpclient.execute(httpGet);

            HttpEntity entity2 = response2.getEntity();
            String respStr = EntityUtils.toString(entity2 , Charset.defaultCharset());
            Header[] cookies = response2.getHeaders("Set-Cookie");
            StringBuffer cookieStr = new StringBuffer();
            if(cookies != null && cookies.length != 0){
                for(Header cookHeader : cookies){
                    cookieStr.append(cookHeader.getValue() + ";");
                }
            }
            response2.close();

            if(getCookie){
                return respStr + "#" + cookieStr.toString();
            }else{
                return respStr;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }


//    public static ResultDTO post(String url , HttpRequestBase getOrPost , Map<String,String> postParams) {
//        CloseableHttpClient httpclient = HttpClients.createDefault();
//        HttpPost httPost = new HttpPost(url);
//        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
//
//        if(headerParams != null && headerParams.size()>0){
//            for(Map.Entry<String,String> entry : headerParams.entrySet()){
//                httPost.addHeader(new BasicHeader(entry.getKey() , entry.getValue()));
//            }
//        }
//        if(postParams != null && postParams.size() != 0){
//            for(Map.Entry entry : postParams.entrySet()){
//                nvps.add(new BasicNameValuePair(entry.getKey().toString() , entry.getValue().toString()));
//            }
//        }
////        Header header = new BasicHeader("Content-Type","application/x-www-form-urlencoded;charset=UTF-8");
////
////
////        Header referHeader = new BasicHeader("Referer","http://sso.njcedu.com/login.htm");
////        httPost.addHeader(header);
////
////        httPost.addHeader(referHeader);
////        httPost.addHeader(header);
//        try {
//            httPost.setEntity(new UrlEncodedFormEntity(nvps , Charset.forName("UTF-8")));
//            CloseableHttpResponse res = httpclient.execute(httPost);
//            Header[] cookies = res.getHeaders("Set-Cookie");
//            StringBuffer cookieStr = new StringBuffer();
//            if(cookies != null && cookies.length != 0){
//                for(Header cookHeader : cookies){
//                    cookieStr.append(cookHeader.getValue() + ";");
//                }
//            }
//            HttpEntity entity2 = res.getEntity();
//            // do something useful with the response body
//            // and ensure it is fully consumed
//            String respStr = EntityUtils.toString(entity2 , Charset.defaultCharset());
//            return ResultDTO.builder()
//                    .response(respStr)
//                    .cookie(cookieStr.toString())
//                    .build();
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        } catch (ClientProtocolException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//
//        return null;
//
//    }


    public static ResultDTO postByURLJSON(String url , Map<String,String> headerParams , String postJson) {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httPost = new HttpPost(url);
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();

        if(headerParams != null && headerParams.size()>0){
            for(Map.Entry<String,String> entry : headerParams.entrySet()){
                if(entry.getKey().equalsIgnoreCase("Cookie")){
                    httPost.addHeader(new BasicHeader(entry.getKey() , "xsid="+entry.getValue()));
                }else{
                    httPost.addHeader(new BasicHeader(entry.getKey() , entry.getValue()));
                }

            }
        }

//        Header header = new BasicHeader("Content-Type","application/x-www-form-urlencoded;charset=UTF-8");
//
//
//        Header referHeader = new BasicHeader("Referer","http://sso.njcedu.com/login.htm");
//        httPost.addHeader(header);
//
//        httPost.addHeader(referHeader);
//        httPost.addHeader(header);
        try {
            httPost.setEntity(new StringEntity(postJson , Charset.forName("UTF-8")));
            CloseableHttpResponse res = httpclient.execute(httPost);
            Header[] cookies = res.getHeaders("Set-Cookie");
            StringBuffer cookieStr = new StringBuffer();
            if(cookies != null && cookies.length != 0){
                for(Header cookHeader : cookies){
                    cookieStr.append(cookHeader.getValue() + ";");
                }
            }
            HttpEntity entity2 = res.getEntity();
            // do something useful with the response body
            // and ensure it is fully consumed
            String respStr = EntityUtils.toString(entity2 , Charset.defaultCharset());
            return ResultDTO.builder()
                    .response(respStr)
                    .cookie(cookieStr.toString())
                    .build();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return null;

    }

    public static String getPageByURL(String url) {
        return null;
    }

    public static ResultDTO postResByUrl(String url, Object o, Object o1) {
        return null;
    }
}