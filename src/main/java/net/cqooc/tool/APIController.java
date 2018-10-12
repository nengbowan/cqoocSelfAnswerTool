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

    private CaptchaInfo captchaInfo;

    private String username;

    private String password;

    private String nonce;

    private String cnonce;

    private String baseUrl = "http://www.cqooc.net";

    private String encodePassword;

    private String captchaToken;

    private String xsid; //登陆之后的授权cookie

    private List<Lesson> lessons = new ArrayList<>();

    private String chapterId ;

    private String realName;

    private String sessionId;


    private boolean auth = false;//是否登录需要授权

    private String moocCid ; //选课的ID 应该每个学生 都不一样 等于预约的时候分配的选课ID 因为一个课程 可以被多个学生所共享选课

    private List<Chapter1> task;

    //完成了之后 要点击一次 测试 否则学习进度是 75%
    private HashMap<String,String> _obj;

    List<Course0> allCourse ; //完全的课程

    List<Course0> courseIng; //进行中的课程


    private boolean doExamFlag;
    public APIController(){

    }

    //默认只刷任务
    public APIController(String username , String password, boolean isAuth ){
        this.username = username;
        this.password = password;
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
        setCourse();
        doCourse();

    }

    private void doCourse() {
        int index = -1;
        if(this.courseIng !=null && this.courseIng.size()>0){
            for(Course0 course0 : this.courseIng){
                index++;
                if(index == 0 ){
                    this.realName = course0.getName();
                }
                String courseId = course0.getCourseId();
                this.moocCid = course0.getId();
                List<Chapter> chapters = getAllChapter(courseId);
                //做测试
                if(chapters != null){
                    int count = 0;
                    for(Chapter c : chapters){
                        System.out.println(c.getTitle());
                        getLessonFun(courseId , c.getId() );
                        count++;
                        String testId = _obj.get("testId");
                        String currentLessionId = _obj.get("currentLessonId");
                        String parentLessionId = _obj.get("parentLessonId");
                        //tid 是 testid structure是学科id sid是lession编号
                        doTest(testId, courseId , currentLessionId ,  parentLessionId , course0.getId());
                        System.out.println("最后一次点击测试刷新");

//                        new Thread(new Runnable() {
//                            @Override
//                            public void run() {
////                                try {
////                                    Thread.sleep(30000);
////                                } catch (InterruptedException e) {
////                                    e.printStackTrace();
////                                }
//                                String testId = _obj.get("testId");
//                                String currentLessionId = _obj.get("currentLessonId");
//                                String parentLessionId = _obj.get("parentLessonId");
//                                //tid 是 testid structure是学科id sid是lession编号
//                                doTest(testId, courseId , currentLessionId ,  parentLessionId , course0.getId());
//                                System.out.println("最后一次点击测试刷新");
//                            }
//                        }).start();
                    }
                    System.out.println(realName +course0.getTitle() +"的测试 讨论 视频 资源 共"+count+"个,已看完 。。。" );
                }

                //做项目作业
                getAllTask(courseId);
                doTask(courseId);
                //做大试卷
                doExam(courseId);
            }
        }
    }

    private void doExam(String courseId) {
        //开启线程三分钟后提交试卷
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("等待三分钟 完成试卷 开启多线程");
                try {
                    Thread.sleep(3*60*1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    String getPaperUrl = baseUrl + "/json/exams?select=id,title&status=1&courseId=" + courseId + "&limit=99&sortby=id&reverse=true&ts=" + System.currentTimeMillis() + "";
                    String resp = HttpClientUtil.getPageByURLAndCookie(getPaperUrl, cookieMap, null);
                    if(!resp.equals("{\"meta\":{\"total\":\"0\",\"start\":\"1\",\"size\":\"0\"},\"data\":[]}")){
                        String examId = JsonObjectUtil.getArray(resp, "data", 0, "id");
                        if (examId != null && !examId.equals("")) {
                            System.out.println("正在做大试卷");
                            //获取大试卷答案
                            String getAnswerUrl = baseUrl + "/json/eps?ownerId=" +sessionId + "&examId=" + examId + "&ts=" + System.currentTimeMillis() + "";
                            String answerResp = HttpClientUtil.getPageByURLAndCookie(getAnswerUrl, cookieMap, null);
                            //fastjson真好用啊 给个赞!
                            ExamDTO examDTO = JSONObject.parseObject(answerResp, ExamDTO.class);


//                        String realName = realName;
                            StringBuffer postParam = new StringBuffer();
                            //获取ownerId 代表session用户

                            String moocId = JsonObjectUtil.getArray(answerResp, "data", 0, "id");
                            postParam.append("{\n" +
                                    "\t\"id\": " + moocId + ",\n" +
                                    "\t\"ownerId\": " + sessionId + ",\n" +
                                    "\t\"username\": \"" + username + "\",\n" +
                                    "\t\"name\": \"" + realName + "\",\n" +
                                    "\t\"examId\": \"" + examId + "\",\n" +
                                    "\t\"courseId\": \"" + courseId + "\",\n" +
                                    "\t\"answers\": {\n");
                            //遍历paper
                            Paper paper = examDTO.getData().get(0);
                            if (paper != null) {
                                if (paper.getBody() != null) {
                                    List<PaperQuestion> paperQuestions = paper.getBody().stream().filter(
                                            (PaperQuestion paperQuestion) -> {
                                                return paperQuestion.getDesc() != null && !paperQuestion.getDesc().equals("");
                                            }
                                    ).collect(Collectors.toList());
                                    int index = 0;
                                    for (PaperQuestion paperQuestion : paperQuestions) {
                                        //单选题 0
                                        if ("0".equals(paperQuestion.getType())) {
                                            List<Question> questions = paperQuestion.getQuestions();
                                            if (questions != null && questions.size() > 0) {
                                                for (Question question : questions) {
                                                    //处理下单选题 返回答案为数组类型 ["0"]
                                                    postParam.append("\"q" + question.getId() + "\":\"" + question.getBody().getAnswer().replaceAll("\\[\"", "").replaceAll("\"]", "") + "\",");
                                                }
                                            }
                                            if (index == paperQuestions.size() - 1) {
                                                postParam.replace(postParam.length() - 1, postParam.length(), "");
                                            }
                                            index++;
                                            //多选题 1
                                        } else if ("1".equals(paperQuestion.getType())) {
                                            List<Question> questions = paperQuestion.getQuestions();
                                            if (questions != null && questions.size() > 0) {
                                                for (Question question : questions) {
                                                    postParam.append("\"q" + question.getId() + "\":" + question.getBody().getAnswer() + ",");
                                                }
                                            }
                                            if (index == paperQuestions.size() - 1) {
                                                postParam.replace(postParam.length() - 1, postParam.length(), "");
                                            }
                                            index++;
                                            //判断题 4
                                        } else if ("4".equals(paperQuestion.getType())) {
                                            List<Question> questions = paperQuestion.getQuestions();
                                            if (questions != null && questions.size() > 0) {
                                                for (Question question : questions) {
                                                    if (question.getBody().getAnswer() == null || question.getBody().getAnswer().equals("")) {
                                                        //没有答案 采用默认的答案
                                                        postParam.append("\"q" + question.getId() + "\":\"" + "1" + "\",");
                                                    } else
                                                        postParam.append("\"q" + question.getId() + "\":\"" + question.getBody().getAnswer().replaceAll("\\[\"", "").replaceAll("\"]", "") + "\",");
                                                }
                                            }
                                            if (index == paperQuestions.size() - 1) {
                                                postParam.replace(postParam.length() - 1, postParam.length(), "");
                                            }
                                            index++;
                                        }
                                    }

                                }

                            }
                            postParam.append("\t}\n" +
                                    "}")
                            ;

                            String doExamUrl = baseUrl + "/exam/api/student/do";
                            ResultDTO submitResp = HttpClientUtil.postByURLJSON(doExamUrl, cookieMap, postParam.toString());
                            System.out.println("大试卷 返回" + submitResp.getResponse());
                            return;
                        } else {
                            System.out.println("大试卷不存在，不做了");
                            return;
                        }
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();



    }

    private void doTask(String courseId) {
        try {
            if (this.task != null && this.task.size() > 0) {
                for (Chapter1 chapter1 : this.task) {
                    String title = chapter1.getChapter().getTitle().trim();
                    String taskId = chapter1.getId();
                    String answer = answersCache.get(title);
//                String answer = "调酒工具、计量与方法";
                    if (answer != null && !answer.equals("")) {
                        String doTaskUrl = "http://www.cqooc.net/json/task/results";

                        String postParam = new HashMapUtil().put("attachment", "")
                                .put("content", "<p>" + answer + "</p>")
                                .put("courseId", courseId)
                                .put("name", this.realName)
                                .put("ownerId", this.sessionId)
                                .put("status", "2")
                                .put("taskId", taskId)
                                .put("username", this.username)
                                .toJsonStr();
                        ResultDTO resultDTO = HttpClientUtil.postByURLJSON(doTaskUrl, this.cookieMap, postParam);
                        System.out.println(title + resultDTO);
                    } else {
                        System.out.println("题库不包含这种简答题,所以跳过..." + title);
                        continue;
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void getAllTask(String courseId) {

        String url = "http://www.cqooc.net/json/tasks?limit=100&start=1&status=1&courseId="+courseId+"&sortby=id&reverse=false&select=id,title,unitId,submitEnd&ts="+System.currentTimeMillis()+"";
        String resp = HttpClientUtil.getPageByURLAndCookie( url , this.cookieMap , null);
        ResultDTO0 result = JSONObject.parseObject(resp , ResultDTO0.class);
        this.task = result.getData();
    }

    private void setCourse() {
        String url = "http://www.cqooc.net/json/mcs?sortby=id&reverse=true&del=2&courseType=2&ownerId="+sessionId+"&limit=20&ts="+System.currentTimeMillis()+"";
        String resp = HttpClientUtil.getPageByURLAndCookie(url , this.cookieMap , null);


        this.allCourse = JSONObject.parseObject(resp).getJSONArray("data").toJavaList(Course0.class);
        //进行中的课程定义为分数为0 且课程结束时间大于当前时间
        this.courseIng = this.allCourse.stream().filter((Course0 course0)->{
            return course0.getScore().equals("0") && course0.getCourse().getEndDate().compareTo(""+System.currentTimeMillis()) > 0 ;
        }).collect(Collectors.toList());

    }



    private List<Chapter> getAllChapter(String courseId) {
        List<Chapter> res = new ArrayList<>();
        String getAllChapterUrl = "http://www.cqooc.net/json/chapters?status=1&select=id,title,level&courseId="+courseId+"&sortby=selfId&reverse=false&limit=200&start=0&ts="+System.currentTimeMillis()+"";
        String chapters = HttpClientUtil.getPageByURLAndCookie(getAllChapterUrl , this.cookieMap , null );
        JSONArray chapterArr = JSONObject.parseObject(chapters).getJSONArray("data");
        if(chapterArr != null && chapterArr.size()>0){
            for(int index = 0 ; index < chapterArr.size() ; index++){
                res.add(  chapterArr.getObject(index , Chapter.class));
                res = res.stream().filter(
                        (Chapter chapter)->{return chapter.getLevel().equalsIgnoreCase("2");}
                ).collect(Collectors.toList());
            }
        }
        return res;
    }

    private void getLessonFun(String courseId , String lessionId){
        String url = this.baseUrl + "/json/mooc/lessons?parentId=" + lessionId + "&limit=100&sortby=selfId&reverse=false";
        String json = HttpClientUtil.getPageByURLAndCookie(url , this.cookieMap , null );
        if(!(JsonObjectUtil.getAttrValue(json , "meta") != null && JsonObjectUtil.getAttrValue(json , "meta" , "total").equals("0"))){
            Integer sizeI = Integer.valueOf(JsonObjectUtil.getAttrValue(json , "meta" , "size"));
            buildLessions(json);
            Lesson[]  lessonArr = new Lesson[this.lessons.size()];
            if(this.lessons != null && this.lessons.size()> 0 ){
                int index = 0 ;
                for(Lesson lesson : this.lessons){
                    lessonArr[index] = lesson;
                    index++;
                }
            }
            boolean testFlag = false;
            for (int i = 0; i < sizeI; i++) {
                //获取该chapter下面所有的 1，测试 2， 所有资源
                if (lessonArr[i].getCategory() == 1) {
                        /*资源*/
                    String resourceId = lessonArr[i].getResource().getId();
                    String title = lessonArr[i].getTitle();
                    HashMap<String,String> obj = new HashMapUtil<String,String>()
                            .put("resourceId",resourceId)
                            .put("parentLessonId" , lessonArr[i].getParentId() )
                            .put("cat" , lessonArr[i].getCategory()+"")
                            .put("title" , title)
                            .put("currentLessonId",lessonArr[i].getId())
                            .map();
                    getResFun(courseId , obj);
                } else if (lessonArr[i].getCategory() == 2) {
                    testFlag = true;
                        /*测试*/
                        //TODO
                    String title = "";
                    String testId = lessonArr[i].getTestId();
                    HashMap<String,String> obj = new HashMapUtil<String,String>()
                            .put("testId",testId)
                            .put("parentLessonId" , lessonArr[i].getParentId() )
                            .put("cat" , lessonArr[i].getCategory()+"")
                            .put("title" , title)
                            .put("currentLessonId",lessonArr[i].getId())
                            .map();
                    //测试id 父id
                    getTestFun(courseId , obj);
                } else if (lessonArr[i].getCategory() == 3) {
                    String title = lessonArr[i].getForum().getTitle();
                    HashMap<String,String> obj = new HashMapUtil()
                            .put("forumId",lessonArr[i].getForumId())
                            .put("currentLessonId" , lessonArr[i].getId() )
                            .put("parentLessonId" , lessonArr[i].getParentId())
                            .put("title" , title)
                            .map();
                    getForumFun(courseId , obj);
                }
            }
            if(testFlag == Boolean.TRUE){
                String testId = _obj.get("testId");
                String currentLessionId = _obj.get("currentLessonId");
                String parentLessionId = _obj.get("parentLessonId");
                //tid 是 testid structure是学科id sid是lession编号
                doTest(testId, courseId , currentLessionId ,  parentLessionId , this.moocCid);

                //使学习进度的测试能够完成
                String url00 = this.baseUrl + "/json/exam/papers?id="+testId+"&ts="+System.currentTimeMillis();
                String resp = HttpClientUtil.getPageByURLAndCookie(url00 , this.cookieMap , null);
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
    }

    /*获取测试*/
    private void getTestFun(String courseId,  HashMap<String,String> _obj){
        this._obj = _obj;
        String testId = _obj.get("testId");
        String currentLessionId = _obj.get("currentLessonId");
        String parentLessionId = _obj.get("parentLessonId");
        String url = this.baseUrl + "/json/exam/papers?id="+testId+"&ts="+System.currentTimeMillis();
        String resp = HttpClientUtil.getPageByURLAndCookie(url , this.cookieMap , null);
        if(!("0").equals(JsonObjectUtil.getAttrValue(resp, "total"))){
            this._obj = _obj;
            getTestPage(courseId , testId);
        }
    }

    public void getTestPage(String courseId , String testId){
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
                "\t\"courseId\": \""+courseId+"\",\n" +
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
    }

    private void doTest(String itemId ,String xqsId, String sid,String cid ,String mcItemId ){

        //&id=' + X.qs.id + '&sid=' + sid + '&cid=' + cid + '&mid=' + mcItem.id + '

            String url = this.baseUrl + "/learn/mooc/testing/do?tid=" + itemId + "&id=" +xqsId  + "&sid=" +sid + "&cid="+cid + "&mid=" + mcItemId;

            String resp = HttpClientUtil.getPageByURLAndCookie(url , this.cookieMap , null);
            return;
            ///learn/mooc/testing/do?tid=8218&id=334564643&sid=67372&cid=42306&mid=11158259
    }



    public void onLogin(){
        String url = baseUrl + "/user/login";
        String onGotNonce = HttpClientUtil.getPageByURL(url);
        String nonce = JsonObjectUtil.getAttrValue(onGotNonce , "nonce");
        String pw = nonce;
        pw+=ScriptEngineUtil.encode(password);
        String cn = cnonce();
        pw+= cn ;
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
    private void getResFun(String courseId , Map<String,String>_obj) {
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
                "\t\"courseId\": \""+courseId+"\",\n" +
                "\t\"sectionId\": \""+_obj.get("currentLessonId")+"\",\n" +
                "\t\"chapterId\": \""+ _obj.get("parentLessonId")+"\",\n" +
                "\t\"category\": 2,\n" +
                "\t\"time\": \""+System.currentTimeMillis()+"\"\n" +
                "}";

        String url0 = this.baseUrl + "/json/learnLogs";
        ResultDTO resultDTO = HttpClientUtil.postByURLJSON(url0 , this.cookieMap , param);
    }

    private void getForumFun(String courseId , Map<String, String> _obj) {
        //做好所有看视频等等
        String url = this.baseUrl + "/json/forum?id="+_obj.get("forumId")+"&ts="+System.currentTimeMillis()+"";
        String resp = HttpClientUtil.getPageByURLAndCookie(url , this.cookieMap , null);
        String param = "{\n" +
                "\t\"username\": \""+this.username+"\",\n" +
                "\t\"ownerId\": "+sessionId+",\n" +
                "\t\"parentId\": \""+this.moocCid+"\",\n" +
                "\t\"action\": 0,\n" +
                "\t\"courseId\": \""+courseId+"\",\n" +
                "\t\"sectionId\": \""+_obj.get("currentLessonId")+"\",\n" +
                "\t\"chapterId\": \""+ _obj.get("parentLessonId")+"\",\n" +
                "\t\"category\": 2,\n" +
                "\t\"time\": \""+System.currentTimeMillis()+"\"\n" +
                "}";

        String url0 = this.baseUrl + "/json/learnLogs";
        ResultDTO resultDTO = HttpClientUtil.postByURLJSON(url0 , this.cookieMap , param);
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
}
