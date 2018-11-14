package net.cqooc.tool;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import net.cqooc.tool.domain.*;
import net.cqooc.tool.dto.Chapter1;
import net.cqooc.tool.dto.ExamDTO;
import net.cqooc.tool.dto.ResultDTO0;
import net.cqooc.tool.dto.v2.LoginBeforeDto;
import net.cqooc.tool.dto.v2.LoginDto;
import net.cqooc.tool.util.*;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClients;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class APIControllerV2 {
    private String username;
    private String password;
    private String baseUrl = "http://www.cqooc.net";
    private CookieStore cookieStore = new BasicCookieStore();
    private HttpClient client = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
    private String captchaToken;
    private String nonce;
    private String cnonce;
    private String encodePassword;
    private String xsid; //登陆之后的授权cookie
    private String sessionId;
    private List<Course0> allCourse; //全部的课程
    private List<Course0> courseIng; //进行中的课程
    private String realName;  //真实姓名
    private List<Chapter1> task;
    //key 项目作业所在的标题 value 答案
    public static Map<String, String> answersCache= new HashMap<>();//项目作业 简答题答案

    public APIControllerV2(String username, String password) {
        this.username = username;
        this.password = password;
    }


    public String toHEX(long v) {
        //4061789743
        String[] INT2HEX = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
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
        long cacheLeft = (long) Math.floor(Math.random() * (long) Math.pow(2, 32));
        long cacheRight = (long) Math.floor(Math.random() * (long) Math.pow(2, 32));
        return toHEX(cacheLeft) + toHEX(cacheRight);
    }

    public void loginBefore() {
        String url = baseUrl + "/user/login";
        HttpGet get = new HttpGet(url);
        String loginBeforeStr = HttpClientUtil.getOrPost( get, client);
        LoginBeforeDto loginBeforeDto = parseLoginBeforeStr(loginBeforeStr);
        String nonce = loginBeforeDto.getNonce();
        String pw = nonce;
        pw += ScriptEngineUtil.encode(password);
        String cn = cnonce();
        pw += cn;
        String hash = ScriptEngineUtil.encode(pw);
        String captcha = null;
        try {
            captcha = this.captchaToken != null ? "&captchaToken=" + URLEncoder.encode(this.captchaToken, "utf8") : "";
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String cnonce = cn + captcha;
        this.encodePassword = hash;
        this.nonce = nonce;
        this.cnonce = cnonce;
    }

    private LoginBeforeDto parseLoginBeforeStr(String loginBeforeStr) {
        return JSONObject.parseObject(loginBeforeStr, LoginBeforeDto.class);
    }

    public String doLogin() {
        if (username != null && password != null
                && encodePassword != null
                && nonce != null
                && cnonce != null) {
            String url = this.baseUrl + "/user/login?username=" + this.username.toLowerCase() + "&password=" + this.encodePassword + "&nonce=" + this.nonce + "&cnonce=" + this.cnonce + "";
            HttpPost post = new HttpPost(url);
            String loginStr = HttpClientUtil.getOrPost(post, client);

            return loginStr;
//            System.out.println(loginStr);

        } else {
            throw new IllegalArgumentException("用户名 " + this.username.toLowerCase() + ", 密码  " + this.password + ", 加密后的密码 " + this.encodePassword + ", nonce " + this.nonce + "， cnonce " + this.cnonce + " 参数缺失");
        }
    }

    private String getCourseListStr() {
        String url = "http://www.cqooc.net/json/mcs?sortby=id&reverse=true&del=2&courseType=2&ownerId=" + sessionId + "&limit=20&ts=" + System.currentTimeMillis() + "";
        HttpGet get = new HttpGet(url);
        String courseListStr = HttpClientUtil.getOrPost(get, client);
        return courseListStr;
    }

    private List<Course0> parseCourseList(String courseListStr) {
        return JSONObject.parseObject(courseListStr).getJSONArray("data").toJavaList(Course0.class);
    }

    private LoginDto parseLoginStr(String loginStr) {
        return JSONObject.parseObject(loginStr, LoginDto.class);
    }

    private List<Chapter> getAllChapter(String courseId) {
        String getAllChapterUrl = "http://www.cqooc.net/json/chapters?status=1&select=id,title,level&courseId=" + courseId + "&sortby=selfId&reverse=false&limit=200&start=0&ts=" + System.currentTimeMillis() + "";
        HttpGet get = new HttpGet(getAllChapterUrl);
        String chaptersStr = HttpClientUtil.getOrPost(get, client);
        return parseChapterList(chaptersStr);
    }

    private List<Chapter> parseChapterList(String chaptersStr) {
        List<Chapter> chapters = new ArrayList<>();
        JSONArray chapterArr = JSONObject.parseObject(chaptersStr).getJSONArray("data");
        if (chapterArr != null && chapterArr.size() > 0) {
            for (int index = 0; index < chapterArr.size(); index++) {
                chapters.add(chapterArr.getObject(index, Chapter.class));
                chapters = chapters.stream().filter(
                        (Chapter chapter) -> {
                            return chapter.getLevel().equalsIgnoreCase("2");
                        }
                ).collect(Collectors.toList());
            }
        }
        return chapters;
    }

    public void getTestPage(String courseId, String testId) {
        String getPaperUrl = this.baseUrl + "/json/exam/paper?id=" + testId;
        HttpGet get = new HttpGet(getPaperUrl);
        String resp = HttpClientUtil.getOrPost(get, client);

        Paper paper = JSONObject.parseObject(resp, Paper.class);
        String realName = this.realName;
        StringBuffer postParam = new StringBuffer();
        //获取ownerId 代表session用户
        postParam.append("{\n" +
                "\t\"ownerId\": " + this.sessionId + ",\n" +
                "\t\"username\": \"" + this.username + "\",\n" +
                "\t\"name\": \"" + realName + "\",\n" +
                "\t\"paperId\": \"" + paper.getId() + "\",\n" +
                "\t\"courseId\": \"" + courseId + "\",\n" +
                "\t\"answers\": {\n");
        //遍历paper
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
                                    postParam.append("\"q" + question.getId() + "\":\"" + question.getBody().getAnswer() + "\",");
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
                "}");
        String submitTestUrl = this.baseUrl + "/json/scoring";
        HttpPost post = new HttpPost(submitTestUrl);
        post.setEntity(new StringEntity(postParam.toString(), Charset.defaultCharset()));
       HttpClientUtil.getOrPost(post, client);
    }

    /*获取测试*/
    private void getTestFun(String courseId, HashMap<String, String> _obj) {
//        this._obj = _obj;
        String testId = _obj.get("testId");
        String currentLessionId = _obj.get("currentLessonId");
        String parentLessionId = _obj.get("parentLessonId");
        String url = this.baseUrl + "/json/exam/papers?id=" + testId + "&ts=" + System.currentTimeMillis();
        HttpGet get = new HttpGet(url);
        String resp = HttpClientUtil.getOrPost(get, client);
        if (!("0").equals(JsonObjectUtil.getAttrValue(resp, "total"))) {
            getTestPage(courseId, testId);
        }
    }

    private void getForumFun(String courseId , HashMap<String, String> _obj ,String moocCid) {
        //做好所有看视频等等
        String forumUrl = this.baseUrl + "/json/forum?id="+_obj.get("forumId")+"&ts="+System.currentTimeMillis()+"";
        HttpGet get = new HttpGet(forumUrl);
        String resp = HttpClientUtil.getOrPost(get , client);
        String param = "{\n" +
                "\t\"username\": \""+this.username+"\",\n" +
                "\t\"ownerId\": "+sessionId+",\n" +
                "\t\"parentId\": \""+moocCid+"\",\n" +
                "\t\"action\": 0,\n" +
                "\t\"courseId\": \""+courseId+"\",\n" +
                "\t\"sectionId\": \""+_obj.get("currentLessonId")+"\",\n" +
                "\t\"chapterId\": \""+ _obj.get("parentLessonId")+"\",\n" +
                "\t\"category\": 2,\n" +
                "\t\"time\": \""+System.currentTimeMillis()+"\"\n" +
                "}";

        String url0 = this.baseUrl + "/json/learnLogs";
        HttpPost post = new HttpPost(url0);
        HttpClientUtil.getOrPost(post , client);
        return;
    }


    private void getLessonFun(String courseId, String lessionId, String moocCid) {
        String url = this.baseUrl + "/json/mooc/lessons?parentId=" + lessionId + "&limit=100&sortby=selfId&reverse=false";
        HttpGet get = new HttpGet(url);
        String json = HttpClientUtil.getOrPost(get, client);
        if (!(JsonObjectUtil.getAttrValue(json, "meta") != null && JsonObjectUtil.getAttrValue(json, "meta", "total").equals("0"))) {
            Integer sizeI = Integer.valueOf(JsonObjectUtil.getAttrValue(json, "meta", "size"));
            List<Lesson> lessons = new ArrayList<>();

            //build lession
            JSONObject lessionObj = JSONObject.parseObject(json);
            JSONArray lessonArr1 = lessionObj.getJSONArray("data");
            if (lessonArr1 != null) {
                for (int index = 0; index < lessonArr1.size(); index++) {
                    Lesson lesson = lessonArr1.getObject(index, Lesson.class);
                    lessons.add(lesson);
                }
            }

            //convert to array
            Lesson[] lessonArr = new Lesson[lessons.size()];
            if (lessons != null && lessons.size() > 0) {
                int index = 0;
                for (Lesson lesson : lessons) {
                    lessonArr[index] = lesson;
                    index++;
                }
            }
            for (int i = 0; i < sizeI; i++) {
                //获取该chapter下面所有的 1，测试 2， 所有资源
                if (lessonArr[i].getCategory() == 1) {
                        /*资源*/
                    Resource resource = lessonArr[i].getResource();
                    String resourceId = null;
                    if (resource == null) {
                        resourceId = lessonArr[i].getResid();
                    } else {
                        resourceId = resource.getId();
                    }
                    String currentLessonId = lessonArr[i].getId();
                    String parentLessionId = lessonArr[i].getParentId();
                    String resourceUrl = this.baseUrl + "/json/my/res?id=" + resourceId;
                    HttpGet resourceGet = new HttpGet(resourceUrl);
                    HttpClientUtil.getOrPost( resourceGet, client);

                    //做好所有看视频等等
                    String param = "{\n" +
                            "\t\"username\": \"" + this.username + "\",\n" +
                            "\t\"ownerId\": " + sessionId + ",\n" +
                            "\t\"parentId\": \"" + moocCid + "\",\n" +
                            "\t\"action\": 0,\n" +
                            "\t\"courseId\": \"" + courseId + "\",\n" +
                            "\t\"sectionId\": \"" + currentLessonId + "\",\n" +
                            "\t\"chapterId\": \"" + parentLessionId + "\",\n" +
                            "\t\"category\": 2,\n" +
                            "\t\"time\": \"" + System.currentTimeMillis() + "\"\n" +
                            "}";
                    //刷资源进度
                    String url0 = this.baseUrl + "/json/learnLogs";
                    HttpPost post = new HttpPost(url0);
                    post.setEntity(new StringEntity(param ,Charset.defaultCharset()));
                    HttpClientUtil.getOrPost(post, client);
                } else if (lessonArr[i].getCategory() == 2) {
//                    testFlag = true;
                        /*测试*/
                    String testId = lessonArr[i].getTestId();
                    String currentLessionId = lessonArr[i].getId();
                    String parentLessionId = lessonArr[i].getParentId();
                    String paperUrl = this.baseUrl + "/json/exam/papers?id=" + testId + "&ts=" + System.currentTimeMillis();
                    HttpGet paperGet = new HttpGet(paperUrl);
                    String resp = HttpClientUtil.getOrPost(paperGet, client);
                    if (!("0").equals(JsonObjectUtil.getAttrValue(resp, "total"))) {
                        getTestPage(courseId, testId);
                    }
                    String testLearnLog = baseUrl + "/json/learnLogs";
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                    String yearDateTime = sdf.format(new Date());

                    String postParam = "{\n" +
                            "\t\"username\": \""+this.username+"\",\n" +
                            "\t\"ownerId\": "+sessionId+",\n" +
                            "\t\"parentId\": \""+moocCid+"\",\n" +
                            "\t\"action\": 0,\n" +
                            "\t\"courseId\": \""+courseId+"\",\n" +
                            "\t\"sectionId\": \""+currentLessionId+"\",\n" +
                            "\t\"chapterId\": \""+parentLessionId+"\",\n" +
                            "\t\"category\": 2,\n" +
                            "\t\"time\": \""+yearDateTime+"\"\n" +
                            "}";

                    HttpPost testLearnLogPost = new HttpPost(testLearnLog);
                    testLearnLogPost.setEntity(new StringEntity(postParam , Charset.defaultCharset()));
                    HttpClientUtil.getOrPost(testLearnLogPost , client);
                    return;
                } else if (lessonArr[i].getCategory() == 3) {
                    String forumId = lessonArr[i].getForumId();
                    String currentLessonId = lessonArr[i].getId();
                    String parentLessonId = lessonArr[i].getParentId();

                    //做好所有看视频等等
                    String forumUrl = this.baseUrl + "/json/forum?id="+forumId+"&ts="+System.currentTimeMillis()+"";
                    HttpGet forumUrlGet = new HttpGet(forumUrl);
                    String resp = HttpClientUtil.getOrPost(forumUrlGet , client);
                    String param = "{\n" +
                            "\t\"username\": \""+this.username+"\",\n" +
                            "\t\"ownerId\": "+sessionId+",\n" +
                            "\t\"parentId\": \""+moocCid+"\",\n" +
                            "\t\"action\": 0,\n" +
                            "\t\"courseId\": \""+courseId+"\",\n" +
                            "\t\"sectionId\": \""+currentLessonId+"\",\n" +
                            "\t\"chapterId\": \""+ parentLessonId+"\",\n" +
                            "\t\"category\": 2,\n" +
                            "\t\"time\": \""+System.currentTimeMillis()+"\"\n" +
                            "}";

                    String url0 = this.baseUrl + "/json/learnLogs";
                    HttpPost post = new HttpPost(url0);
                    post.setEntity(new StringEntity(param , Charset.defaultCharset()));
                    HttpClientUtil.getOrPost(post , client);
                }
            }
        }
    }

    private void getAllTask(String courseId) {

        String url = "http://www.cqooc.net/json/tasks?limit=100&start=1&status=1&courseId="+courseId+"&sortby=id&reverse=false&select=id,title,unitId,submitEnd&ts="+System.currentTimeMillis()+"";
        HttpGet get = new HttpGet(url);
        String resp = HttpClientUtil.getOrPost(get , client);
        ResultDTO0 result = JSONObject.parseObject(resp , ResultDTO0.class);
        this.task = result.getData();
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

                        HttpPost post = new HttpPost(doTaskUrl);
                        post.setEntity(new StringEntity(postParam , Charset.defaultCharset()));
                        HttpClientUtil.getOrPost(post ,  client);
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
    private void doExam(String courseId) {
        //开启线程三分钟后提交试卷
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String getPaperUrl = baseUrl + "/json/exams?select=id,title&status=1&courseId=" + courseId + "&limit=99&sortby=id&reverse=true&ts=" + System.currentTimeMillis() + "";
                    HttpGet getPaperUrlGet = new HttpGet(getPaperUrl);
                    String resp = HttpClientUtil.getOrPost(getPaperUrlGet , client);
                    if(!resp.equals("{\"meta\":{\"total\":\"0\",\"start\":\"1\",\"size\":\"0\"},\"data\":[]}")){
                        String examId = JsonObjectUtil.getArray(resp, "data", 0, "id");
                        if (examId != null && !examId.equals("")) {
                            System.out.println("正在做大试卷");

                            //申请试卷
                            //http://www.cqooc.net/exam/api/paper/gen
                            String generatePaperUrl=  baseUrl + "/exam/api/paper/gen";
                            String generaetPaperPostData =  new HashMapUtil().put("courseId", courseId)
                                    .put("examId", examId)
                                    .put("name", realName)
                                    .put("ownerId", sessionId)
                                    .put("username", username.toLowerCase())
                                    .toJsonStr();
                            HttpPost generatePaperUrlPost = new HttpPost(generatePaperUrl);
                            generatePaperUrlPost.setEntity(new StringEntity(generaetPaperPostData , Charset.defaultCharset()));
                            System.out.println("生成试卷"+HttpClientUtil.getOrPost(generatePaperUrlPost,client ));

                            //获取大试卷答案
                            String getAnswerUrl = baseUrl + "/json/eps?ownerId=" +sessionId + "&examId=" + examId + "&ts=" + System.currentTimeMillis() + "";
                            HttpGet getAnswerUrlGet = new HttpGet(getAnswerUrl);
                            String answerResp = HttpClientUtil.getOrPost(getAnswerUrlGet, client);
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
                            HttpPost doExamUrlPost = new HttpPost(doExamUrl);
                            System.out.println("大试卷 返回" + HttpClientUtil.getOrPost(doExamUrlPost , client));
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
    private void doCourse() {
        int index = -1;
        if (CollectionUtils.isNotEmpty(this.courseIng)) {
            for (Course0 course0 : this.courseIng) {
                index++;
                if (index == 0) {
                    this.realName = course0.getName();
                }
                String courseId = course0.getCourseId();
                String moocCid = course0.getId();
                List<Chapter> chapters = getAllChapter(courseId);
                //做测试
                if (CollectionUtils.isNotEmpty(chapters)) {
                    reSort(chapters);
                     for (Chapter c : chapters) {
                         System.out.println(c.getTitle());
                        getLessonFun(courseId, c.getId(), moocCid);
                    }
                    System.out.println(realName + course0.getTitle() + "的测试,讨论,视频,资源已看完 。。。");
                }

                //做项目作业
                getAllTask(courseId);
                doTask(courseId);
                //做大试卷
                doExam(courseId);
            }
        }

    }

    private void reSort(List<Chapter> chapters) {
        chapters.sort(new Comparator<Chapter>() {
            @Override
            public int compare(Chapter o1, Chapter o2) {
                return Integer.valueOf(o1.getId()) - Integer.valueOf(o2.getId());
            }
        });
    }

    public void run() {
        //登录之前的参数获取
        loginBefore();
        String loginStr = doLogin();
        System.out.println(loginStr);
        LoginDto loginDto = parseLoginStr(loginStr);
        this.xsid = loginDto.getXsid();
        //获取提交试卷时候的唯一识别id码
        String sessionIdUrl = "http://www.cqooc.net/user/session?xsid=" + this.xsid + "&ts=" + System.currentTimeMillis() + "";

        HttpGet get = new HttpGet(sessionIdUrl);
        String sessionIdDto = HttpClientUtil.getOrPost( get, client);
        if (sessionIdDto != null) {
            this.sessionId = JsonObjectUtil.getAttrValue(sessionIdDto, "id");
        }
        String courseListStr = getCourseListStr();

        this.allCourse = parseCourseList(courseListStr);

        //进行中的课程定义为分数为0 且课程结束时间大于当前时间
        this.courseIng = this.allCourse.stream().filter((Course0 course0) -> {
            return course0.getScore().equals("0") && course0.getCourse().getEndDate().compareTo("" + System.currentTimeMillis()) > 0;
        }).collect(Collectors.toList());

        doCourse();

    }


}
