package net.cqooc.tool;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import net.cqooc.tool.domain.*;
import net.cqooc.tool.dto.Chapter1;
import net.cqooc.tool.dto.ExamDTO;
import net.cqooc.tool.dto.ResultDTO;
import net.cqooc.tool.dto.ResultDTO0;
import net.cqooc.tool.util.*;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.xml.sax.InputSource;
import sun.awt.image.ImageWatched;

import javax.xml.parsers.SAXParser;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class APIController {

    //页面请求参数的map  id=334564673&cid=11172149 key id value 334564673
    public static HashMap<String,String > requestParamMap = new HashMap<>();


    //key 项目作业所在的标题 value 答案
    public static Map<String, String> answersCache= new HashMap<>();//项目作业 简答题答案

    private HashMap<String, String> cookieMap = null;

    private String userId;

    private CaptchaInfo captchaInfo;

    private String username;

    private String password;

    private String nonce;

    private String cnonce;

    private String baseUrl = "http://www.cqooc.net";

    private String encodePassword;

    private String captchaToken;

    private String xsid; //登陆之后的授权cookie

    private String courseId = "334564673";

    private List<Lesson> lessons = new ArrayList<>();

    private String chapterId ;

    private String realName;

    private String sessionId;

    private List<Chapter> chapters = new ArrayList<>();

    private boolean auth = false;//是否登录需要授权

    private String moocCid ; //选课的ID 应该每个学生 都不一样 等于预约的时候分配的选课ID 因为一个课程 可以被多个学生所共享选课

    private List<Chapter1> task;

    private boolean onlyDoTask;

    private boolean doExamFlag;
    public APIController(){

    }

    //默认只刷任务
    public APIController(String username , String password, boolean isAuth , boolean onlyDoTask , boolean doExamFlag){

        this.username = username;

        this.password = password;

        this.onlyDoTask = onlyDoTask;

        this.auth = isAuth;

        if(auth){
            //读验证码的key 和 验证码的值
            setCapchaInfo();
            //解析成真实图片识别 并放入captchainfo
            String verifyCode = parseImg2String();

            ResultDTO captchaToken = captchaC(verifyCode);

            if(captchaToken !=null ){
                this.captchaToken = JsonObjectUtil.getAttrValue(captchaToken.getResponse() , "captchaToken");
            }
            onLogin();

            doLogin();


        }else{
            onLogin();

            doLogin();

        }
        if(xsid != null && !xsid.equals("")){
            System.out.println(this.username  +"登录授权通过...");
            this.cookieMap = new HashMapUtil().put("Cookie",this.xsid).map();
        }else{
            System.out.println(this.username + " 登录授权未通过...");
            System.exit(0);
        }


        //获取提交试卷时候的唯一识别id码
        String sessionIdUrl = "http://www.cqooc.net/user/session?xsid="+this.xsid+"&ts="+System.currentTimeMillis()+"";
        String sessionIdDto = HttpClientUtil.getPageByURLAndCookie(sessionIdUrl , this.cookieMap , null);
        if(sessionIdDto != null) {
            this.sessionId = JsonObjectUtil.getAttrValue(sessionIdDto, "id");
        }

        //获取选课的ID
        setMoocId();

//        String realNameUrl = "http://www.cqooc.net/account/session/api/profile/get?username="+this.username+"&ts="+System.currentTimeMillis()+"";
//
//
//        String realNameDto = HttpClientUtil.getPageByURLAndCookie(realNameUrl , this.cookieMap , null);
//        if(realNameDto != null){
//            this.realName = JsonObjectUtil.getAttrValue(realNameDto , "name");
//        }

        if(doExamFlag){
            doExam();
            return;
        }

        if(onlyDoTask){
            onTask();
            return;
        }

        getAllChapter();

        //做测试
        if(this.chapters != null){
            int count = 0;
            for(Chapter c : chapters){
                System.out.println(c.getTitle());
                getLessonFun(c.getId());
                count++;
            }
            System.out.println(realName + "的测试 讨论 视频 资源 共"+count+"个,已看完 。。。" );
        }


        //做项目作业
        onTask();

        //做大试卷
        doExam();
    }

    private void doExam() {
        System.out.println("等待三分钟 完成试卷");
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//
//            }
//        }).start();
        try {
            Thread.sleep(3*60*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String getPaperUrl = this.baseUrl + "/json/exams?select=id,title&status=1&courseId="+this.courseId+"&limit=99&sortby=id&reverse=true&ts="+System.currentTimeMillis()+"";
        String resp = HttpClientUtil.getPageByURLAndCookie(getPaperUrl , this.cookieMap , null);
        String examId = JsonObjectUtil.getArray(resp , "data" , 0 , "id");
        if(examId != null && !examId.equals("")){
            System.out.println("正在做大试卷");
            //获取大试卷答案
            String getAnswerUrl = this.baseUrl + "/json/eps?ownerId="+this.sessionId+"&examId="+examId+"&ts="+System.currentTimeMillis()+"";
            String answerResp = HttpClientUtil.getPageByURLAndCookie(getAnswerUrl , this.cookieMap , null);
            //fastjson真好用啊 给个赞!
            ExamDTO examDTO = JSONObject.parseObject(answerResp , ExamDTO.class);



            String realName = this.realName;
            StringBuffer postParam = new StringBuffer();
            //获取ownerId 代表session用户

            String moocId = JsonObjectUtil.getArray(answerResp , "data" , 0 , "id");
            postParam.append("{\n" +
                    "\t\"id\": "+moocId+",\n" +
                    "\t\"ownerId\": "+this.sessionId+",\n" +
                    "\t\"username\": \""+this.username+"\",\n" +
                    "\t\"name\": \""+realName+"\",\n" +
                    "\t\"examId\": \""+examId+"\",\n" +
                    "\t\"courseId\": \""+this.courseId+"\",\n" +
                    "\t\"answers\": {\n");
            //遍历paper
            Paper paper = examDTO.getData().get(0);
            if(paper != null){
                if(paper.getBody() != null) {
                    List<PaperQuestion> paperQuestions = paper.getBody().stream().filter(
                            (PaperQuestion paperQuestion) -> {
                                return paperQuestion.getDesc() != null && !paperQuestion.getDesc().equals("");
                            }
                    ).collect(Collectors.toList());
                    int index = 0 ;
                    for(PaperQuestion paperQuestion : paperQuestions){
                        //单选题 0
                        if("0".equals(paperQuestion.getType())){
                            List<Question> questions = paperQuestion.getQuestions();
                            if(questions != null && questions.size()>0){
                                for(Question question : questions){
                                    //处理下单选题 返回答案为数组类型 ["0"]
                                    postParam.append("\"q" + question.getId() + "\":\"" + question.getBody().getAnswer().replaceAll("\\[\"","").replaceAll("\"]", "") + "\",");
                                }
                            }
                            if(index == paperQuestions.size() -1 ){
                                postParam.replace(postParam.length() -1 , postParam.length() , "");
                            }
                            index++;
                            //多选题 1
                        }else if("1".equals(paperQuestion.getType())){
                            List<Question> questions = paperQuestion.getQuestions();
                            if(questions != null && questions.size()>0){
                                for(Question question : questions){
                                    postParam.append("\"q" + question.getId() + "\":" + question.getBody().getAnswer() + ",");
                                }
                            }
                            if(index == paperQuestions.size() -1 ){
                                postParam.replace(postParam.length() -1 , postParam.length() , "");
                            }
                            index++;
                            //判断题 4
                        }else if("4".equals(paperQuestion.getType())){
                            List<Question> questions = paperQuestion.getQuestions();
                            if(questions != null && questions.size()>0){
                                for(Question question : questions){
                                    if(question.getBody().getAnswer() == null||question.getBody().getAnswer().equals("")){
                                        //没有答案 采用默认的答案
                                        postParam.append("\"q" + question.getId() + "\":\"" + "1" + "\",");
                                    }else
                                        postParam.append("\"q" + question.getId() + "\":\"" + question.getBody().getAnswer().replaceAll("\\[\"","").replaceAll("\"]", "") + "\",");
                                }
                            }
                            if(index == paperQuestions.size() -1 ){
                                postParam.replace(postParam.length() -1 , postParam.length() , "");
                            }
                            index++;
                        }
                    }

                }

            }
            postParam.append("\t}\n" +
                    "}")
            ;

            String doExamUrl = this.baseUrl +  "/exam/api/student/do";
            ResultDTO submitResp = HttpClientUtil.postByURLJSON( doExamUrl , this.cookieMap , postParam.toString());
        System.out.println("大试卷 返回"+submitResp.getResponse());
            return;
        }else{
            System.out.println("大试卷不存在，不做了");
            return;
        }


    }

    private void onTask() {

        getAllTask();

        doTask();

    }

    private void doTask() {
        if(this.task != null && this.task.size()>0){
            for(Chapter1 chapter1 : this.task){
                String title = chapter1.getChapter().getTitle().trim();
                String taskId = chapter1.getId();
                String answer = answersCache.get(title);
//                String answer = "调酒工具、计量与方法";
                if(answer != null && !answer.equals("")){
                    String doTaskUrl = "http://www.cqooc.net/json/task/results";

                    String postParam = new HashMapUtil().put("attachment","")
                            .put("content" , "<p>"+answer + "</p>")
                            .put("courseId",this.courseId)
                            .put("name",this.realName)
                            .put("ownerId",this.sessionId)
                            .put("status","2")
                            .put("taskId",taskId)
                            .put("username",this.username)
                            .toJsonStr();
                    ResultDTO resultDTO = HttpClientUtil.postByURLJSON(doTaskUrl , this.cookieMap , postParam);
                    System.out.println(title  +resultDTO);
                }else{
                    System.out.println("题库不包含这种简答题,所以跳过..." + title);
                    continue;
                }
            }
        }

    }

    private void getAllTask() {

        String url = "http://www.cqooc.net/json/tasks?limit=100&start=1&status=1&courseId="+this.courseId+"&sortby=id&reverse=false&select=id,title,unitId,submitEnd&ts="+System.currentTimeMillis()+"";
        String resp = HttpClientUtil.getPageByURLAndCookie( url , this.cookieMap , null);
        ResultDTO0 result = JSONObject.parseObject(resp , ResultDTO0.class);
        this.task = result.getData();
    }

    private void setMoocId() {
        String url = "http://www.cqooc.net/json/mcs?sortby=id&reverse=true&del=2&courseType=2&ownerId="+sessionId+"&limit=20&ts="+System.currentTimeMillis()+"";
        String resp = HttpClientUtil.getPageByURLAndCookie(url , this.cookieMap , null);
        JSONArray array = JSONObject.parseObject(resp).getJSONArray("data");
        Course0 course0 = array.getObject(0 , Course0.class);
        this.moocCid = course0.getId();
        this.realName = course0.getName();
    }

    private void getAllChapter() {
        String getAllChapterUrl = "http://www.cqooc.net/json/chapters?status=1&select=id,title,level&courseId=334564673&sortby=selfId&reverse=false&limit=200&start=0&ts="+System.currentTimeMillis()+"";
        String chapters = HttpClientUtil.getPageByURLAndCookie(getAllChapterUrl , this.cookieMap , null );
        JSONArray chapterArr = JSONObject.parseObject(chapters).getJSONArray("data");
        if(chapterArr != null && chapterArr.size()>0){
            for(int index = 0 ; index < chapterArr.size() ; index++){
                this.chapters.add(  chapterArr.getObject(index , Chapter.class));
                this.chapters = this.chapters.stream().filter(
                        (Chapter chapter)->{return chapter.getLevel().equalsIgnoreCase("2");}
                ).collect(Collectors.toList());
            }
        }
        return ;
    }

    private void getLessonFun(String lessionId){
//        this.chapterId = lessionId;
        String limit = "100";
        String url = this.baseUrl + "/json/mooc/lessons?parentId=" + lessionId + "&limit=" + limit + "&sortby=selfId&reverse=false";

        String json = HttpClientUtil.getPageByURLAndCookie(url , this.cookieMap , null );

        String total = "0";
        String size = "0";
        if(JsonObjectUtil.getAttrValue(json , "meta") != null){
            total = JsonObjectUtil.getAttrValue(json , "meta" , "total");
            size = JsonObjectUtil.getAttrValue(json , "meta" , "size");
        }

        if(!(JsonObjectUtil.getAttrValue(json , "meta") != null && JsonObjectUtil.getAttrValue(json , "meta" , "total").equals("0"))){
            Integer sizeI = Integer.valueOf(JsonObjectUtil.getAttrValue(json , "meta" , "size"));

            buildLessions(json);

            String [] cat = {"", "资源", "测验", "讨论"};

            Lesson[]  lessonArr = new Lesson[this.lessons.size()];

            if(this.lessons != null && this.lessons.size()> 0 ){
                int index = 0 ;
                for(Lesson lesson : this.lessons){
                    lessonArr[index] = lesson;
                    index++;
                }
            }


            String res = "";




            for (int i = 0; i < sizeI; i++) {
                String id = "";
                String title = "";
                String cls = "";

                //获取该chapter下面所有的 1，测试 2， 所有资源
                if (lessonArr[i].getCategory() == 1) {
                        /*资源*/
                    String viewer = null;
                    if(lessonArr[i].getResource().getViewer() == null){
                        viewer = "1";
                    }
                    cls = 'v' + lessonArr[i].getResource().getViewer();
                    String resourceId = lessonArr[i].getResource().getId();
                    title = lessonArr[i].getTitle();
                    HashMap<String,String> obj = new HashMapUtil<String,String>()
                            .put("resourceId",resourceId)
                            .put("parentLessonId" , lessonArr[i].getParentId() )
                            .put("cat" , lessonArr[i].getCategory()+"")
                            .put("title" , title)
                            .put("currentLessonId",lessonArr[i].getId())
                            .map();
                    getResFun(obj);
                } else if (lessonArr[i].getCategory() == 2) {
                        /*测试*/
                        //TODO
                    String testId = lessonArr[i].getTestId();
                    String pid = lessonArr[i].getId();
                    HashMap<String,String> obj = new HashMapUtil<String,String>()
                            .put("testId",testId)
                            .put("parentLessonId" , lessonArr[i].getParentId() )
                            .put("cat" , lessonArr[i].getCategory()+"")
                            .put("title" , title)
                            .put("currentLessonId",lessonArr[i].getId())
                            .map();
                    //测试id 父id
                    getTestFun(obj);
                } else if (lessonArr[i].getCategory() == 3) {
                    id = lessonArr[i].getForum().getId();
                    title = lessonArr[i].getForum().getTitle();
                    HashMap<String,String> obj = new HashMapUtil()
                            .put("forumId",lessonArr[i].getForumId())
                            .put("currentLessonId" , lessonArr[i].getId() )
                            .put("parentLessonId" , lessonArr[i].getParentId())
                            .put("cat" , cat)
                            .put("title" , title)
                            .map();
                    getForumFun(obj);
                }
            }
        }
    }

    /**
     * 获取最后一条记录留言 无需授权
     */
    public String getRandomLiuYan(String forumId){
        String url = "http://www.cqooc.net/json/forum/posts?forumId="+forumId+"&limit=10&sortby=id&reverse=false&ts="+System.currentTimeMillis()+"";
        String resp = HttpClientUtil.getPageByURL(url);
        String comment = JsonObjectUtil.getArray(resp , "data", 0, "content");
        return comment;
    }
    public void buildLessions(String json) {
        JSONObject obj = JSONObject.parseObject(json);
        JSONArray lessonArr = obj.getJSONArray("data");
        if(lessonArr != null){
            lessons.clear();
            for(int index = 0 ; index < lessonArr.size() ; index++){
                Lesson lesson = lessonArr.getObject(index , Lesson.class);
                lessons.add(lesson);
            }
        }
        return;
    }

    /*获取测试*/
    private void getTestFun(HashMap<String,String> _obj){
        String testId = _obj.get("testId");
        String currentLessionId = _obj.get("currentLessonId");
        String parentLessionId = _obj.get("parentLessonId");
        String url = this.baseUrl + "/json/exam/papers?id="+testId;
        String resp = HttpClientUtil.getPageByURLAndCookie(url , this.cookieMap , null);
        if(!("0").equals(JsonObjectUtil.getAttrValue(resp, "total"))){
//            String res = "";
            String title = JsonObjectUtil.getArray(resp , "data" , 0 , "title");
            //tid 是 testid structure是学科id sid是lession编号
            doTest(testId, this.courseId , currentLessionId ,  parentLessionId , moocCid , title);
            getTestPage(testId);
        }
    }

    public void getTestPage(String testId){
//        this.cookieMap = new HashMapUtil().put("Cookie",this.xsid).map();
        String getPaperUrl = this.baseUrl + "/json/exam/paper?id="+testId;
        String resp = HttpClientUtil.getPageByURLAndCookie(getPaperUrl , this.cookieMap , null);
        //fastjson真好用啊 给个赞!
        Paper paper = JSONObject.parseObject(resp , Paper.class);
        String realName = this.realName;
        StringBuffer postParam = new StringBuffer();
        //获取ownerId 代表session用户
        postParam.append("{\n" +
                "\t\"ownerId\": "+this.sessionId+",\n" +
                "\t\"username\": \""+this.username+"\",\n" +
                "\t\"name\": \""+realName+"\",\n" +
                "\t\"paperId\": \""+paper.getId()+"\",\n" +
                "\t\"courseId\": \""+this.courseId+"\",\n" +
                "\t\"answers\": {\n");
        //遍历paper
        if(paper != null){
            if(paper.getBody() != null) {
                List<PaperQuestion> paperQuestions = paper.getBody().stream().filter(
                        (PaperQuestion paperQuestion) -> {
                            return paperQuestion.getDesc() != null && !paperQuestion.getDesc().equals("");
                        }
                ).collect(Collectors.toList());
                int index = 0 ;
                for(PaperQuestion paperQuestion : paperQuestions){
                    //单选题 0
                    if("0".equals(paperQuestion.getType())){
                        List<Question> questions = paperQuestion.getQuestions();
                        if(questions != null && questions.size()>0){
                            for(Question question : questions){
                                //处理下单选题 返回答案为数组类型 ["0"]
                                postParam.append("\"q" + question.getId() + "\":\"" + question.getBody().getAnswer().replaceAll("\\[\"","").replaceAll("\"]", "") + "\",");
                            }
                        }
                        if(index == paperQuestions.size() -1 ){
                            postParam.replace(postParam.length() -1 , postParam.length() , "");
                        }
                        index++;
                        //多选题 1
                    }else if("1".equals(paperQuestion.getType())){
                        List<Question> questions = paperQuestion.getQuestions();
                        if(questions != null && questions.size()>0){
                            for(Question question : questions){
                                postParam.append("\"q" + question.getId() + "\":" + question.getBody().getAnswer() + ",");
                            }
                        }
                        if(index == paperQuestions.size() -1 ){
                            postParam.replace(postParam.length() -1 , postParam.length() , "");
                        }
                        index++;
                        //判断题 4
                    }else if("4".equals(paperQuestion.getType())){
                        List<Question> questions = paperQuestion.getQuestions();
                        if(questions != null && questions.size()>0){
                            for(Question question : questions){
                                if(question.getBody().getAnswer() == null||question.getBody().getAnswer().equals("")){
                                    //没有答案 采用默认的答案
                                    postParam.append("\"q" + question.getId() + "\":\"" + "1" + "\",");
                                }else
                                postParam.append("\"q" + question.getId() + "\":\"" + question.getBody().getAnswer() + "\",");
                            }
                        }
                        if(index == paperQuestions.size() -1 ){
                            postParam.replace(postParam.length() -1 , postParam.length() , "");
                        }
                        index++;
                    }
                }

            }

        }
        postParam.append("\t}\n" +
                "}")
                ;
        String submitTestUrl = this.baseUrl +  "/json/scoring";
        ResultDTO submitResp = HttpClientUtil.postByURLJSON( submitTestUrl , this.cookieMap , postParam.toString());
//        System.out.println("测试 返回"+submitResp.getResponse());
        return;
    }

    private void doTest(String itemId ,String xqsId, String sid,String cid ,String mcItemId,String title ){

        //&id=' + X.qs.id + '&sid=' + sid + '&cid=' + cid + '&mid=' + mcItem.id + '

            String url = this.baseUrl + "/learn/mooc/testing/do?tid=" + itemId + "&id=" +xqsId  + "&sid=" +sid + "&cid="+cid + "&mid=" + mcItemId;

            String resp = HttpClientUtil.getPageByURLAndCookie(url , this.cookieMap , null);
            return;
    }



    public void onLogin(){
        String url = baseUrl + "/user/login";
        String onGotNonce = HttpClientUtil.getPageByURL(url);
        String nonce = JsonObjectUtil.getAttrValue(onGotNonce , "nonce");
        String pw = nonce;
        pw+=ScriptEngineUtil.encode(password);
        String cn = cnonce();
        pw+= cn ;
        //4CEE9E58809DF22F6BD7273B5782073582541CE0081C585C06ECC37AFC9DB0F5
        String hash = ScriptEngineUtil.encode(pw);
        String captcha = null;
        try {
            captcha = this.captchaToken != null ? "&captchaToken=" + URLEncoder.encode(this.captchaToken , "utf8") : "";
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String cnonce = cn + captcha;
        this.encodePassword = hash;
        this.nonce = nonce;
        this.cnonce = cnonce;
    }

    private String 获取点击进入课堂页面(){
        return null;
    }
    private void getResFun(Map<String,String>_obj) {
//        $("#previewPlayer").html('<p id="playerContent"></p>');
        String url = this.baseUrl + "/json/my/res?id="+ _obj.get("resourceId");
        String res = HttpClientUtil.getPageByURLAndCookie(url , this.cookieMap , null);
        String title = JsonObjectUtil.getAttrValue(res , "title");
      //  displayResFile(JsonObjectUtil.getAttrValue(res , "id"));

        //做好所有看视频等等

        String param = "{\n" +
                "\t\"username\": \""+this.username+"\",\n" +
                "\t\"ownerId\": "+sessionId+",\n" +
                "\t\"parentId\": \""+this.moocCid+"\",\n" +
                "\t\"action\": 0,\n" +
                "\t\"courseId\": \""+this.courseId+"\",\n" +
                "\t\"sectionId\": \""+_obj.get("currentLessonId")+"\",\n" +
                "\t\"chapterId\": \""+ _obj.get("parentLessonId")+"\",\n" +
                "\t\"category\": 2,\n" +
                "\t\"time\": \""+System.currentTimeMillis()+"\"\n" +
                "}";

        String url0 = this.baseUrl + "/json/learnLogs";
        ResultDTO resultDTO = HttpClientUtil.postByURLJSON(url0 , this.cookieMap , param);

//        System.out.println( "视频 返回" + resultDTO);

//        X.get("/json/my/res?id=" + , function(respText) {
//            $('.mr_loading').hide();
//            var item = JSON.parse(respText);
//            if (item.title) {
//                var res = '';
//                $('.file_title').html(_obj.title || item.title);
//                displayResFile(item.id);
//            }
//        });

    }

    private void displayResFile(String id) {
        String url = this.baseUrl + "/json/my/res?id=" + id;
        String resp = HttpClientUtil.getPageByURLAndCookie( url , this.cookieMap , null);
        String newSourceDIR = JsonObjectUtil.getAttrValue(resp , "newSourceDIR");
        String uuid = JsonObjectUtil.getAttrValue(resp , "oid");
        String viewer = JsonObjectUtil.getAttrValue(resp , "viewer") != null ? JsonObjectUtil.getAttrValue(resp , "viewer") : "2" ;
//        X.get('/json/my/res?id=' + id, function(resp) {
//            resp = JSON.parse(resp);
//            resp.newSourceDIR = resp.newSourceDIR || "";
//            //预览文件可以在播放器中显示
//            var width = 725;
//            var length = 740;
//            var uuid = resp.oid; //"ff808081-29263667-0129-26367ec3-118f"; //resp.oid;
//            var viewer = resp.viewer || "2";
//            var flashvars = {
//                    'uuid': uuid,
//                    'oid': uuid + '_pre'
//            };
//            var playerSrc = "/files/DocPlayer.swf"; //viewer=0或1时用此播放器
//            var IService = '/docXML';
//            if (resp.newSourceDIR.length) {
//                IService = '/newdocXML';
//            }
//            if (viewer === "2") {
//                length = 409;
//                IService = '/videoXML';
//                if (resp.newSourceDIR.length) {
//                    IService = '/newvideoXML';
//                    // IService = '/api/resource/play/newvideoXML';
//                    // IService += encodeURIComponent('?resId=' + uuid + '&host=http://convert-resource.baipeng.org' + '&');
//                    // flashvars.resId = uuid;
//                    // flashvars.host = 'http://convert-resource.baipeng.org'; //'http://' + document.location.host;
//                }
//                playerSrc = "/files/FlvPlayer.swf"; //音视频播放器，即viewer=2时用此播放器
//            }
//            flashvars.IService = IService;
//            // var flashvars = {
//            //     'uuid': uuid,
//            //     'oid': uuid + '_pre',
//            //     'IService': IService
//            // };
//            var params = {
//                    quality: "high",
//                    wmode: "opaque",
//                    allowscriptaccess: "always",
//                    allowfullscreen: "true",
//                    bgcolor: "#fff"
//            };
//            var attributes = {
//                    id: "player",
//                    name: "player"
//            };
//            var swf = playerSrc;
//            swfobject.embedSWF(swf, "playerContent", width, length, "9.0.0", false, flashvars, params, attributes);
//
//            setTimeout(function() {
//                setLog(); //设置资源学习log
//            }, 30000);
//        });
    }

//    private void setLog() {
//        String logService = "/json/learnLogs";
//        var item = {};
//        item.username = user.username;
//        item.ownerId = user.id || "";
//        item.parentId = mcItem.id || ""; //选课id
//        item.action = 0;
//        item.courseId = X.qs.id;
//        item.sectionId = sid; //mooc资源id
//        item.chapterId = cid; //章节id
//        item.category = 2;
//        //console.log(item);
//        if (item.ownerId.length == 0 || item.parentId.length == 0) {
//            return;
//        }
//        X.get(logService + '?category=2&sectionId=' + sid + '&ownerId=' + item.ownerId, function(respText) {
//            var resp = JSON.parse(respText);
//            resp.meta = resp.meta || {
//                    total: "0",
//                    size: "0"
//            };
//            if (resp.meta && resp.meta.total === '0') {
//                X.get('/time', function(t) {
//                    t = JSON.parse(t);
//                    var d = new Date();
//                    if (t.time) {
//                        d = new Date(t.time);
//                    }
//                    item.time = d.getFullYear() + '' + (d.getMonth() + 1) + '' + d.getDate();
//                    X.post(logService, item, function(respText) {
//                        //alert(respText);
//                    });
//                });
//            } else {
//                return;
//            }
//        });
//    }

//    private void getContFun(Map<String,String> obj){
//        String sid = obj.get("pid");
//        String  conCat = obj.get("cat");
//
//        String cla = obj.get("cla");
//
//        String id = obj.get("tid");
//
//        if (conCat.equals("1")) {
//            /*资源*/
//            getResFun(obj);
//        } else if (conCat.equals("2")) {
//            /*测试*/
//            String paperId = obj.get("id");
//            getTestFun(paperId  , sid);
//        } else if (conCat.equals("3") ) {
//            /*讨论*/
//            getForumFun(obj);
//        }
//    }

    private void getForumFun(Map<String, String> _obj) {

        //做好所有看视频等等

        String url = this.baseUrl + "/json/forum?id="+_obj.get("forumId")+"&ts="+System.currentTimeMillis()+"";
        String resp = HttpClientUtil.getPageByURLAndCookie(url , this.cookieMap , null);

        String param = "{\n" +
                "\t\"username\": \""+this.username+"\",\n" +
                "\t\"ownerId\": "+sessionId+",\n" +
                "\t\"parentId\": \""+this.moocCid+"\",\n" +
                "\t\"action\": 0,\n" +
                "\t\"courseId\": \""+this.courseId+"\",\n" +
                "\t\"sectionId\": \""+_obj.get("currentLessonId")+"\",\n" +
                "\t\"chapterId\": \""+ _obj.get("parentLessonId")+"\",\n" +
                "\t\"category\": 2,\n" +
                "\t\"time\": \""+System.currentTimeMillis()+"\"\n" +
                "}";

        String url0 = this.baseUrl + "/json/learnLogs";
        ResultDTO resultDTO = HttpClientUtil.postByURLJSON(url0 , this.cookieMap , param);

//        System.out.println( "讨论  返回" + resultDTO);



    }

    public String toHEX(long v){
        //4061789743
        String [] INT2HEX = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};

        String h = "";
        h += INT2HEX[Math.toIntExact(v >>> 28 & 0xF)];
        h += INT2HEX[Math.toIntExact(v >>> 24 & 0xF)];
        h += INT2HEX[Math.toIntExact(v >>> 20 & 0xF)];
        h += INT2HEX[Math.toIntExact(v >>> 16 & 0xF)];
        h += INT2HEX[Math.toIntExact(v >>> 12 & 0xF)];
        h += INT2HEX[Math.toIntExact(v >>> 8 & 0xF)];
        h += INT2HEX[Math.toIntExact(v >>> 4 & 0xF)];
        h += INT2HEX[Math.toIntExact(v >>> 0 & 0xF)];
        return h;
    }
    private String cnonce() {
        long cacheLeft = (long) Math.floor(Math.random() * (long)Math.pow(2, 32));
        long cacheRight = (long) Math.floor(Math.random() * (long)Math.pow(2, 32));
        return toHEX(cacheLeft) + toHEX(cacheRight);
    }

    private ResultDTO captchaC(String verifyCode) {
        String url = this.baseUrl+"/captcha/c";
        String postParam = new HashMapUtil()
                .put("key",this.captchaInfo.getKey())
                .put("captcha" , verifyCode)
                .toJsonStr();


        return HttpClientUtil.postByURLJSON(url , new HashMapUtil().put("Content-Type","application/json").map() , postParam);
    }

    private String parseImg2String() {
        String splitStr = "data:image/jpeg;base64, ";
        String removedCompleteImgData = this.captchaInfo.getImg().split(splitStr)[1];
        String saveFileName = CapchaSaveConfig.SAVE_CONFIG + System.currentTimeMillis()+ ".jpeg";
        ImageUtil.generateImage(removedCompleteImgData , saveFileName);
        String imageXml = RuoKuai.createByPost(saveFileName);

        return getVerifyCode(imageXml);


    }

    private String getVerifyCode(String imageXml) {
        SAXReader reader = new SAXReader();
        try {

            Document doc = reader.read(new ByteArrayInputStream(imageXml
                    .getBytes("UTF8")));
            Element rootEle =  doc.getRootElement().element("Result");

            if(rootEle != null){
                return rootEle.getText();
            }else{
                return null;
            }
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getBase64Pic(String loginPage) {
        try{
            Parser parser = Parser.createParser(loginPage, Charset.defaultCharset().toString());

            //缓冲层 parser解析一次之后，再次解析为空
            NodeList cacheNodeList = parser.parse(new NodeFilter() {
                public boolean accept(Node node) {
                    if(node instanceof ImageTag
                            && ((ImageTag) node).getAttribute("src")!=null
                            && ((ImageTag) node).getAttribute("src").contains("data:image")){
                        return true;
                    }else return false;

                }
            });
            Node[] nodes = cacheNodeList.toNodeArray();
            if(nodes != null && nodes.length ==0){
               return ((ImageTag)nodes[0]).getAttribute("src");
            }

        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

    public void doLogin(){

        if(username != null && password != null
                && encodePassword != null
                && nonce != null
                && cnonce != null){
            String url = this.baseUrl + "/user/login?username="+this.username+"&password="+this.encodePassword+"&nonce="+this.nonce+"&cnonce="+this.cnonce+"";

            ResultDTO resultDTO = HttpClientUtil.postResByUrl(url , null , null);
            System.out.println(resultDTO);
            this.xsid = JsonObjectUtil.getAttrValue(resultDTO.getResponse() , "xsid");
        }else{
            throw new IllegalArgumentException("用户名 "+this.username+", 密码  "+this.password+", 加密后的密码 "+this.encodePassword+", nonce "+this.nonce+"， cnonce "+this.cnonce+"参数缺失，无法登陆。");
        }


    }
    /**
     *
     * @param
     * @return
     */
    public void setCapchaInfo(){
        String url = "http://www.cqooc.net/captcha/c?ts="+System.currentTimeMillis();
        String mapJson = HttpClientUtil.getPageByURL(url);
        JSONObject mapObj = JSONObject.parseObject(mapJson);
        String key = mapObj.getString("key");
        String img = mapObj.getString("img");
        this.captchaInfo = CaptchaInfo.builder().img(img).key(key).build();
    }
    public String getLoginPage(){
        String url = "http://www.cqooc.net/login";
        String page = HttpClientUtil.getPageByURL(url);
        return page;
    }

    public String getUserId(String xsid) {
        //http://www.cqooc.net/user/session?xsid=60E448256BC78399&ts=1537523392140
        String url = "http://www.cqooc.net/user/session?xsid="+xsid+"&ts="+System.currentTimeMillis();
        return null;
    }
}
