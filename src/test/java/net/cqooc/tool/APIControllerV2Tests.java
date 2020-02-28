package net.cqooc.tool;

import org.junit.Test;

import java.io.IOException;

public class APIControllerV2Tests {
    //测验
    @Test
    public void testTest(){
        String username = "127580314180113";
        String passwd = "Zym2580123";
        String courseId = "334565250";
        String testId = "16839"; //测验id
        API api = new API(username , passwd );
        api.getTestPage("coursenmae ",courseId , testId , "test title'" ,false);
    }
    @Test
    public void getNonceTest() throws IOException {

        String username = "127581201190414";
        String passwd = "Xmer0320";
        new API(username , passwd).run("334566550");
//        String username = "127580314180220";
//        String passwd = "ylx959...";
//        new API(username , passwd).run("334566955");

//        String username = "127581104190128";
//        String passwd = "20010824cj.";
//        new API(username , passwd).run("334566875");

//        String username = "127580517190102";
//        String passwd = "zyy.0526";
//        new API(username , passwd).run("334566955");
//        new String(username.getBytes("utf8"), Charset.forName("GBK"));


//
//        HttpGet get = new HttpGet("http://www.cqooc.net/user/login?ts="+System.currentTimeMillis());
//        HttpClient client = HttpClients.createDefault();
//        System.out.println(client.execute(get));


//        URL url = new URL("http://www.cqooc.net/user/login?ts="+System.currentTimeMillis());
//        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
//        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//        String cache = "";
//        while((cache = br.readLine()) != null){
//            System.out.println(cache);
//        }
    }
}
