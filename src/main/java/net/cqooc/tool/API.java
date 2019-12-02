package net.cqooc.tool;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 2019.11.27　去除自动识别登录验证码功能
 * 2019.11.27　更换HttpClient为原生java自带库 HttpURLConnection ,否则HttpClient报nginx 403 ,官方做了大量检测
 */
@SuppressWarnings(value = "unchecked")
public class API {
    private static Logger logger = LoggerFactory.getLogger(API.class);
    private String username;
    private String password;
    private String baseUrl = "http://www.cqooc.net";

    private String xsid; //登陆之后的授权cookie
    private String sessionId;
    private List<Course0> allCourse; //全部的课程
    private List<Course0> courseIng; //进行中的课程
    /*真实姓名*/
    private String realName;
    private List<Chapter1> task;
    //key 项目作业所在的标题 value 答案
    public static Map<String, String> answersCache = new HashMap<>();//项目作业 简答题答案
    private CaptchaInfo captchaInfo;

    private CookieStore cookieStore = new BasicCookieStore();
    private HttpClient client = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
    private static ObjectNode answerMap;

    /*是否开启载入测试答案*/
    private static boolean loadTestAnswerTrue = true;


    private boolean loginTrue = false;

    static {
        if (loadTestAnswerTrue) {
            try {
                logger.info("准备载入测试答案...");
                loadTestAnswer();
                logger.info("载入测试答案完成...");
            } catch (IOException io) {
                logger.error("测试答案载入失败,系统退出");
                logger.error(io.getMessage());
                io.printStackTrace();
                System.exit(0);
            }
        }
    }

    public API(String username, String password) {
        if (StringUtils.isEmpty(username)) {
            logger.error("用户名不得为空");
            return;
        }
        if (StringUtils.isEmpty(password)) {
            logger.error("用户密码不得为空");
            return;
        }
        this.username = username;
        this.password = password;
    }

    /**
     * 登录以及获取xsid(cookie)
     */
    private void login() {
        logger.info("准备用户登入...");
        //登录
        doLogin();
        if (!loginTrue) {
            return;
        }

        //获取提交试卷时候的唯一识别id码
        String requestUrl = "http://www.cqooc.net/user/session?xsid=" + this.xsid + "&ts=" + System.currentTimeMillis() + "";
        String resp = HttpUrlConnectionUtil.get(requestUrl, null);
        if (!resp.contains("id")) {
            requestLogger("登录发生错误", requestUrl, "", resp);
            return;
        }
        this.sessionId = JSONObject.parseObject(resp).getString("id");
    }

    /**
     * 载入测试题答案
     *
     * @throws IOException
     */
    private static void loadTestAnswer() throws IOException {

        File[] files = new File("test").listFiles();
        for (File f : files) {
            if (f.getName().contains("指定")) {
                continue;
            }
            logger.info("载入测试答案...:" + f.getAbsolutePath());
            if (answerMap == null) {
                answerMap = (ObjectNode) JacksonUtil.objectMapper.readTree(f);
            } else {
                ObjectNode objectToPut = (ObjectNode) JacksonUtil.objectMapper.readTree(f);
                answerMap.putAll(objectToPut);
            }

        }
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


    private LoginBeforeDto parseLoginBeforeStr(String loginBeforeStr) {
        return JSONObject.parseObject(loginBeforeStr, LoginBeforeDto.class);
    }

    public void doLogin() {
        String captchaToken = null;
        String nonce;
        String cnonce;
        String encodePassword;
        LoginBeforeDto loginBeforeDto = getNonce();
        if (loginBeforeDto == null) {
            return;
        }
        nonce = loginBeforeDto.getNonce();
        String pw = nonce;
        pw += ScriptEngineUtil.encode(password);
        String cn = cnonce();
        pw += cn;
        String hash = ScriptEngineUtil.encode(pw);
        String captcha = null;
        try {
            captcha = captchaToken != null ? "&captchaToken=" + URLEncoder.encode(captchaToken, "utf8") : "";
        } catch (UnsupportedEncodingException e) {
            logger.error("Token解析出错!");
            System.exit(0);
        }
        cnonce = cn + captcha;
        encodePassword = hash;
        if (encodePassword == null || nonce == null || cnonce == null) {
            logger.error("Password不能为空!");
            System.exit(0);
        }
        login2(encodePassword, nonce, cnonce);
    }


    private void login2(String encodePassword, String nonce, String cnonce) {
        String requestUrl = this.baseUrl + "/user/login?username=" + this.username.toLowerCase() + "&password=" + encodePassword + "&nonce=" + nonce + "&cnonce=" + cnonce + "";
        String loginStr = HttpUrlConnectionUtil.post(requestUrl, "{}", null, null);
        if (!loginStr.contains("ok")) {
            requestLogger("登录发生错误", requestUrl, "", loginStr);
            loginTrue = false;
        }
        LoginDto loginDto = parseLoginStr(loginStr);
        this.xsid = loginDto.getXsid();
        logger.info("用户登录完成...");
        loginTrue = true;
    }


    /**
     * 登陆之前的必要参数获
     *
     * @return {"nonce":"34108F654639A4A7"}
     */
    public LoginBeforeDto getNonce() {
        String requestUrl = "http://www.cqooc.net/user/login?ts=" + System.currentTimeMillis();
        String resp = HttpUrlConnectionUtil.get(requestUrl, null);
        if (!resp.contains("nonce")) {
            requestLogger("获取nonce失败", requestUrl, "", resp);
            return null;
        }
        LoginBeforeDto loginBeforeDto = parseLoginBeforeStr(resp);
        return loginBeforeDto;
    }

    /**
     * 获取用户所有课程列表
     */
    private List<Course0> getCourseList() {
        String requestUrl = "http://www.cqooc.net/json/mcs?sortby=id&reverse=true&del=2&courseType=2&ownerId=" + sessionId + "&limit=20&ts=" + System.currentTimeMillis() + "";
        String resp = HttpUrlConnectionUtil.get(requestUrl, null);
        if (!resp.contains("data")) {
            requestLogger("获取课程列表失败", requestUrl, "", resp);
            return null;
        }
        List<Course0> result = JSONObject.parseObject(resp).getJSONArray("data").toJavaList(Course0.class);
        return result;
    }


    private LoginDto parseLoginStr(String loginStr) {
        return JSONObject.parseObject(loginStr, LoginDto.class);
    }

    /**
     * 获取课程下的所有章节列表
     *
     * @param courseId
     * @return
     */
    private List<Chapter> getAllChapter(String courseId) {
        String getAllChapterUrl = "http://www.cqooc.net/json/chapters?status=1&select=id,title,level&courseId=" + courseId + "&sortby=selfId&reverse=false&limit=200&start=0&ts=" + System.currentTimeMillis() + "";
        String refererValue = "http://www.cqooc.net/learn/mooc/structure?id=" + courseId;
        String chaptersStr = HttpUrlConnectionUtil.get(getAllChapterUrl, refererValue);
        if (!chaptersStr.contains("id")) {
            requestLogger("获取课程章节失败", getAllChapterUrl, "", chaptersStr);
            return null;
        }
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

    public void getTestPage(String courseName, String courseId, String testId, String title, boolean firstFlag) {
        if (answerMap.get(courseId) == null) {
            logger.error(courseName + "未配置答案,程序退出,请配置答案,在运行.");
            return;
        }
        String getPaperUrl = this.baseUrl + "/test/api/paper/get?id=" + testId;
        String resp = HttpUrlConnectionUtil.get(getPaperUrl, null);
        if (!resp.contains("id")) {
            requestLogger("获取测试题" + courseName + "," + courseId + "," + testId + "页面失败", getPaperUrl, "", resp);
            return;
        }
        Paper paper = JSONObject.parseObject(resp, Paper.class);
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
                                if (answerMap.get(courseId).get(paper.getId()) == null) {
                                    logger.error(courseName + "答案已配置,但是名称为" + title + "测试答案未配置,忽略答此题,请配置测试答案...");
                                    return;
                                }

                                String answer = answerMap.get(courseId).get(paper.getId()).get("q" + question.getId()).asText();
                                answer = answer == null ? "2" : answer;
                                postParam.append("\"q" + question.getId() + "\":\"" + answer + "\",");
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
                                if (answerMap.get(courseId).get(paper.getId()) == null) {
                                    logger.error(courseName + "答案已配置,但是名称为" + title + "测试答案未配置,忽略答此题,请配置测试答案...");
                                    return;
                                }
                                String answer = answerMap.get(courseId).get(paper.getId()).get("q" + question.getId()).toString();
                                answer = answer == null ? "[\"2\"]" : answer;
                                postParam.append("\"q" + question.getId() + "\":" + answer + ",");
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
                                if (answerMap.get(courseId).get(paper.getId()) == null) {
                                    logger.error(courseName + "答案已配置,但是名称为" + title + "测试答案未配置,忽略答此题,请配置测试答案...");
                                    return;
                                }
                                String answer = answerMap.get(courseId).get(paper.getId()).get("q" + question.getId()).toString();
                                //没有答案 采用默认的答案
                                if (answer != null) {
                                    postParam.append("\"q" + question.getId() + "\":" + answer + ",");
                                } else {
                                    postParam.append("\"q" + question.getId() + "\":\"" + "1" + "\",");
                                }
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
        String submitTestUrl = this.baseUrl + "/test/api/result/add";
        String testResp = HttpUrlConnectionUtil.post(submitTestUrl, postParam.toString(), null, null);
        if (!testResp.contains("code")) {
            logger.error("提交测试题" + title + "出错", submitTestUrl, "", testResp);
        } else {
            JSONObject jsonObject = JSONObject.parseObject(testResp);
            if (1 == jsonObject.getInteger("code")) {
                logger.error("提交测试题" + title + "出错", submitTestUrl, "", testResp);

                sleep30s();
                getTestPage(courseName, courseId, testId, title, true);

            }
            if (firstFlag == true) {
                logger.info("提交测试题" + title + "重试成功");
            }
        }
    }

    /**
     * 获取某章节下的视频,测验,论坛并完成
     *
     * @param courseName
     * @param courseId
     * @param lessionId
     * @param moocCid
     */
    private void doChapter(String courseName, String courseId, String lessionId, String moocCid) {
        String url = this.baseUrl + "/json/mooc/lessons?parentId=" + lessionId + "&limit=100&sortby=selfId&reverse=false";
        String refererValue = "http://www.cqooc.net/learn/mooc/structure?id=" + courseId;
        String json = HttpUrlConnectionUtil.get(url, refererValue);
        if (!json.contains("total")) {
            requestLogger("获取章节下视频测试论坛列表失败", url, "", json);
            return;
        }
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
                    String resourceUrl = this.baseUrl + "/json/my/res?id=" + resourceId;
                    String resp = HttpUrlConnectionUtil.get(resourceUrl, null);
                    if (!resp.contains("ownerId") && !resp.equals("{}")) {
                        requestLogger("打开" + lessonArr[i].getTitle() + "看视频页面错误", resourceUrl, "", resp);
                        continue;
                    }
                    String currentLessonId = lessonArr[i].getId();
                    String parentLessionId = lessonArr[i].getParentId();
                    //看视频
                    doWatchVideo(moocCid, courseId, currentLessonId, parentLessionId, lessonArr[i].getTitle(), false);
                } else if (lessonArr[i].getCategory() == 2) {
                    /*测试*/
                    String testId = lessonArr[i].getTestId();
                    String currentLessionId = lessonArr[i].getId();
                    String parentLessionId = lessonArr[i].getParentId();
                    String paperUrl = this.baseUrl + "/json/exam/papers?id=" + testId + "&ts=" + System.currentTimeMillis();
                    String resp = HttpUrlConnectionUtil.get(paperUrl, null);
                    if (!("0").equals(JsonObjectUtil.getAttrValue(resp, "total"))) {
                        getTestPage(courseName, courseId, testId, lessonArr[i].getTitle(), false);
                    }
                    //做测试
                    doTest(moocCid, courseId, currentLessionId, parentLessionId, lessonArr[i].getChapter().getTitle(), false);
                } else if (lessonArr[i].getCategory() == 3) {
                    String forumId = lessonArr[i].getForumId();
                    String currentLessonId = lessonArr[i].getId();
                    String parentLessonId = lessonArr[i].getParentId();

                    //论坛
                    String forumUrl = this.baseUrl + "/json/forum?id=" + forumId + "&ts=" + System.currentTimeMillis() + "";

                    String resp2 = HttpUrlConnectionUtil.get(forumUrl, null);
                    if (!resp2.contains("id")) {
                        requestLogger("载入论坛页面" + lessonArr[i].getChapter().getTitle() + "出错", forumUrl, "", resp2);
                        continue;
                    }
                    //做论坛
                    doForum(moocCid, courseId, currentLessonId, parentLessonId, false);
                }
            }
        }
    }

    /**
     * 做某一章节下的论坛问题
     *
     * @param moocCid
     * @param courseId
     * @param currentLessonId
     * @param parentLessonId
     * @param firstLoop
     */
    private void doForum(String moocCid, String courseId, String currentLessonId, String parentLessonId, boolean firstLoop) {
        String param = "{\n" +
                "\t\"username\": \"" + this.username + "\",\n" +
                "\t\"ownerId\": " + sessionId + ",\n" +
                "\t\"parentId\": \"" + moocCid + "\",\n" +
                "\t\"action\": 0,\n" +
                "\t\"courseId\": \"" + courseId + "\",\n" +
                "\t\"sectionId\": \"" + currentLessonId + "\",\n" +
                "\t\"chapterId\": \"" + parentLessonId + "\",\n" +
                "\t\"category\": 2,\n" +
                "\t\"time\": \"" + System.currentTimeMillis() + "\"\n" +
                "}";

        String url0 = this.baseUrl + "/learnLog/api/add";
        String resp = HttpUrlConnectionUtil.post(url0, param, null, null);
        if (!resp.contains("code")) {
            System.out.println("论坛提交失败," + "不包含指定特征符号,返回:" + resp);
        } else {
            JSONObject jsonObject = JSONObject.parseObject(resp);
            if (jsonObject.getInteger("code") != 2
                    && jsonObject.getInteger("code") != 0) {
                requestLogger("论坛提交失败", url0, param, resp);

                sleep30s();
                doForum(moocCid, courseId, currentLessonId, parentLessonId, true);
            }
            if (firstLoop == true) {
                System.out.println("论坛提交失败," + "重试成功:");
            }
        }
    }

    /**
     * 休眠三十秒
     */
    public void sleep30s() {
        logger.info("准备重试...等候三十秒..");
        try {
            Thread.sleep(30 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * 测试题目时间打卡
     *
     * @param moocCid
     * @param courseId
     * @param currentLessionId
     * @param parentLessionId
     * @param firstLoop
     */
    private void doTest(String moocCid, String courseId, String currentLessionId, String parentLessionId, String title,
                        boolean firstLoop) {
        String testLearnLog = baseUrl + "/learnLog/api/add";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String yearDateTime = sdf.format(new Date());

        String postParam = "{\n" +
                "\t\"username\": \"" + this.username + "\",\n" +
                "\t\"ownerId\": " + sessionId + ",\n" +
                "\t\"parentId\": \"" + moocCid + "\",\n" +
                "\t\"action\": 0,\n" +
                "\t\"courseId\": \"" + courseId + "\",\n" +
                "\t\"sectionId\": \"" + currentLessionId + "\",\n" +
                "\t\"chapterId\": \"" + parentLessionId + "\",\n" +
                "\t\"category\": 2,\n" +
                "\t\"time\": \"" + yearDateTime + "\"\n" +
                "}";
        String resp = HttpUrlConnectionUtil.post(testLearnLog, postParam, null, null);
        if (!resp.contains("code")) {
            System.out.println("测试提交时间打卡失败," + title + "不包含指定特征符号,返回:" + resp);
        } else {
            JSONObject jsonObject = JSONObject.parseObject(resp);
            if (jsonObject.getInteger("code") != 2
                    && jsonObject.getInteger("code") != 0) {
                System.out.println("测试提交时间打卡失败," + title + "返回:" + resp);
                sleep30s();
                doTest(moocCid, courseId, currentLessionId, parentLessionId, title, true);
            }
            if (firstLoop == true) {
                System.out.println("测试提交时间打卡失败," + title + "重试成功:");
            }
        }

    }

    /**
     * 看视频
     *
     * @param moocCid
     * @param courseId
     * @param currentLessonId
     * @param parentLessionId
     * @param title
     * @param firstLoop       是否是第一次看视频
     */
    public void doWatchVideo(String moocCid, String courseId, String currentLessonId, String parentLessionId, String title, boolean firstLoop) {
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
        String url0 = this.baseUrl + "/learnLog/api/add";
        String referer = "http://www.cqooc.net/learn/mooc/structure?id=" + courseId;
        String userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.13; rv:70.0) Gecko/20100101 Firefox/70.0";
        String lookResp = HttpUrlConnectionUtil.post(url0, param, referer, userAgent);
        JSONObject jsonObject = JSONObject.parseObject(lookResp);
        if (
                jsonObject.getInteger("code") != 2  //已经添加记录
                        && jsonObject.getInteger("code") != 0) //成功
        {
            requestLogger("看视频错误:" + title, url0, param, lookResp);
            //尝试等10秒再请求　看看　是否非法操作
            if (jsonObject.getInteger("code") == 3) {
                sleep30s();
                doWatchVideo(moocCid, courseId, currentLessonId, parentLessionId, title, true);
            }
        }
        if (firstLoop == true) {
            System.out.println("看视频错误," + title + "重试成功:");
        }
    }

    private void getAllTask(String courseId) {
        String url = "http://www.cqooc.net/json/tasks?limit=100&start=1&status=1&courseId=" + courseId + "&sortby=id&reverse=false&select=id,title,unitId,submitEnd&ts=" + System.currentTimeMillis() + "";
        HttpGet get = new HttpGet(url);
        String resp = HttpClientUtil.getOrPost(get, client);
        ResultDTO0 result = JSONObject.parseObject(resp, ResultDTO0.class);
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
                        post.setEntity(new StringEntity(postParam, Charset.defaultCharset()));
                        HttpClientUtil.getOrPost(post, client);
                    } else {
                        System.out.println("题库不包含这种简答题,所以跳过..." + title);
                        continue;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doExam(String courseId) {
        String getPaperUrl = baseUrl + "/json/exams?select=id,title&status=1&courseId=" + courseId + "&limit=99&sortby=id&reverse=true&ts=" + System.currentTimeMillis() + "";
        String resp = HttpUrlConnectionUtil.get(getPaperUrl, null);
        if (!resp.equals("{\"meta\":{\"total\":\"0\",\"start\":\"1\",\"size\":\"0\"},\"data\":[]}")) {
            String examId = JsonObjectUtil.getArray(resp, "data", 0, "id");
            System.out.println(("获取到期末考试ID:" + examId));
            if (examId != null && !examId.equals("")) {
                System.out.println("正在做大试卷");

                //申请试卷
                //http://www.cqooc.net/exam/api/paper/gen
                String generatePaperUrl = baseUrl + "/exam/api/paper/gen";
                String generaetPaperPostData = new HashMapUtil().put("courseId", courseId)
                        .put("examId", examId)
                        .put("name", realName)
                        .put("ownerId", sessionId)
                        .put("username", username.toLowerCase())
                        .put("courseId", courseId)
                        .toJsonStr();
                //{"code":0,"id":537412,"msg":"No error"}
                String generatePaperResp = HttpUrlConnectionUtil.post(generatePaperUrl, generaetPaperPostData, null, null);
                if (!generatePaperResp.contains("No error") && !generatePaperResp.contains("已经生成过试卷，请联系授课教师")) {
                    System.out.println("生成试卷失败:" + generatePaperResp);
                    return;
                }

                //exam/api/paper/get?examId=2559&ts=1575085453963
                //获取大试卷答案
                String getAnswerUrl = baseUrl + "exam/api/paper/get?examId=" + examId + "&ts=" + System.currentTimeMillis() + "";

                String answerResp = HttpUrlConnectionUtil.get(getAnswerUrl, null);
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
                String examResp = HttpUrlConnectionUtil.post(doExamUrl, postParam.toString(), null, null);
                System.out.println("大试卷 返回" + examResp);
                return;
            } else {
                System.out.println("大试卷不存在，不做了");
                return;
            }
        } else {
            System.out.println("大试卷不存在，不做了");
        }


    }

    /**
     * 做用户所有课程
     */
    private void doCourse() {
        int index = -1;
        for (Course0 course0 : this.courseIng) {
            index++;
            //获取真实姓名
            if (index == 0) {
                this.realName = course0.getName();
                logger.info("欢迎您," + realName);
            }
            String courseId = course0.getCourseId();
            //获取课程下的所有章节
            List<Chapter> chapters = getAllChapter(courseId);
            if (CollectionUtils.isEmpty(chapters)) {
                logger.error("获取课程章节失败,跳过此课程" + course0.getTitle());
                continue;
            }
            //排序
            sortChapterById(chapters);
            doChapters(course0, chapters);
            System.out.println(realName + course0.getTitle() + "的测试,讨论,视频,资源已看完 。。。");
            System.out.println(course0.getTitle() + "已完成");

            //做项目作业　简单题
//                getAllTask(courseId);
//                doTask(courseId);
            //做大试卷
//                doExam(courseId);
        }
    }

    /**
     * 做所有的章节列表
     *
     * @param course0  指定课程
     * @param chapters 　指定课程下的所有章节列表
     */
    private void doChapters(Course0 course0, List<Chapter> chapters) {
        for (Chapter c : chapters) {
            doChapter(course0.getTitle(), course0.getCourseId(), c.getId(), course0.getId());
        }
    }

    /**
     * 按照章节ID进行排序
     *
     * @param chapters
     */
    private void sortChapterById(List<Chapter> chapters) {
        chapters.sort((Chapter one, Chapter two) -> {
            return Integer.valueOf(one.getId()) - Integer.valueOf(two.getId());
        });
    }

    /**
     * 运行api
     *
     * @param courseIds
     */
    public void run(String... courseIds) {
        login();
        if (!loginTrue) {
            return;
        }
        logger.info("准备获取课程列表...");
        this.allCourse = getCourseList();
        if (CollectionUtils.isEmpty(allCourse)) {
            return;
        }
        logger.info("获取课程列表完成...");

        //如果课程名不指定　默认刷全部有效课程
        if (ArrayUtil.isNotEmpty(courseIds)) {
            //进行中的课程定义为分数为0 且课程结束时间大于当前时间 course0.getScore().equals("0") &&
            this.courseIng = this.allCourse.stream().filter((Course0 course0) -> {

                return course0.getCourse().getEndDate().compareTo("" + System.currentTimeMillis()) > 0;
            }).filter(
                    //      //刷指定课程
                    (Course0 course0) -> {
                        return Arrays.asList(courseIds).contains(course0.getCourseId());
                    })
                    .collect(Collectors.toList());
        } else {
            this.courseIng = this.allCourse.stream().filter((Course0 course0) -> {
                return course0.getCourse().getEndDate().compareTo("" + System.currentTimeMillis()) > 0;
            }).collect(Collectors.toList());
        }
        if (CollectionUtils.isEmpty(courseIng)) {
            logger.error("课程可刷的课程为空..");
            return;
        } else {
            logger.info("获取到账号进行中的课程..." +
                    courseIng.stream().map(course0 -> {
                        return course0.getTitle();
                    }).collect(Collectors.joining(",")));
        }
        logger.info("准备做课程...");
        doCourse();
        logger.info(realName + "的课程已全部完成...");
    }


    /**
     * 日志打印　请求url , 请求实体 ,返回内容
     *
     * @param requestUrl
     * @param requestBody
     * @param resp
     */
    private void requestLogger(String title, String requestUrl, String requestBody, String resp) {
        int length = "--------------------------------------------------------------".length();
        logger.info("--------------------------------------------------------------");
        logger.info("----------------------" + title + "------------------------------");
        logger.info(String.format("------------------URL:-----------%s------------------------------", requestUrl));
        logger.info(String.format("-----------------BODY:-----------%s---------------------------", requestBody));
        logger.info(String.format("-----------------RESP:-----------%s----------------------------", resp));
        logger.info("--------------------------------------------------------------");
    }
}
