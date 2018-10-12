package net.cqooc.tool;

import org.junit.Test;

import java.math.BigDecimal;

public class APIControllerTests {

    @Test
    public void getLoginPageTests(){

//        String username = "106372016051413207";
//
//        String password = "123456ABc";

        String username1 = "137351611150549";
        String password1 = "Dch1997921";
        new APIController(username1 , password1 ,false);
//        new APIController(username , password);
    }

    //@Test
    public void getTestPage(){
//        new APIController().getTestPage("8366");
    }

//    //@Test
//    public void toHEXTests(){
//        Integer.MAX_VALUE;
//        long a = BigDecimal.valueOf(4061789743);
//        new APIController().toHEX(a);
//    }

    //@Test
    public void buildLessionTests(){
        String lessonRes = "{\"meta\":{\"total\":\"4\",\"start\":\"1\",\"size\":\"4\"},\"data\":[{\"id\":\"68139\",\"parentId\":\"42550\",\"chapter\":{\"id\":\"42550\",\"title\":\"调酒师与调酒比赛\",\"status\":\"1\"},\"courseId\":\"334564673\",\"category\":\"2\",\"title\":null,\"resId\":null,\"resource\":{\"id\":\"\",\"title\":null,\"authorName\":null,\"resSort\":null,\"resMediaType\":null,\"resSize\":null,\"viewer\":null,\"oid\":\"\",\"username\":null,\"resMediaType_lk_display\":null,\"pages\":null,\"duration\":null,\"dimension\":null,\"resourceType_lk_display\":null},\"testId\":\"8361\",\"test\":{\"id\":8361,\"title\":\"任务5测试\",\"body\":[{\"desc\":\"单选题\",\"type\":\"0\",\"total\":\"2\",\"questions\":[{\"points\":\"2\",\"question\":\"<p><span style=\\\"font-size:14px;font-family:宋体\\\">随着酒吧数量的大大增加，作为酒吧“灵魂”的（ &nbsp; &nbsp;）的薪酬会水涨船高<\\/span><\\/p>\",\"body\":{\"answer\":[\"0\"],\"choices\":[\"调酒师\",\"酒吧主管\",\"酒吧保安\",\"酒吧管理层\"]},\"type\":\"0\",\"id\":\"12521\"},{\"points\":\"2\",\"question\":\"<p>调酒师不同于一些幕后行业，他们经常要和顾客面对面交流，良好的（ &nbsp; &nbsp;）是打开与顾客对话的一扇窗口。<br\\/><\\/p>\",\"body\":{\"answer\":[\"0\"],\"choices\":[\"外在形象\",\"沟通能力\",\"英语能力\",\"其他\"]},\"type\":\"0\",\"id\":\"12522\"}]},{\"desc\":\"多选题\",\"type\":\"1\",\"total\":\"2\",\"questions\":[{\"points\":\"2\",\"question\":\"<p><span style=\\\"font-size:16px;font-family:黑体;color:#333333\\\">调酒师的类型<\\/span><span style=\\\"font-family: 宋体;\\\">按调酒风格可以分为（ &nbsp; &nbsp;）<\\/span><\\/p>\",\"body\":{\"answer\":[\"0\",\"1\"],\"choices\":[\"英式调酒师\",\"花式调酒师\",\"国内调酒师\",\"国际调酒师\"]},\"type\":\"1\",\"id\":\"12523\"},{\"points\":\"2\",\"question\":\"<p>调酒师的专业素质包括一下哪些内容（ &nbsp; &nbsp;）。<\\/p>\",\"body\":{\"answer\":[\"0\",\"1\",\"2\"],\"choices\":[\"服务意识\",\"专业知识\",\"专业技能\",\"英语沟通能力\"]},\"type\":\"1\",\"id\":\"12524\"}]},{\"desc\":\"\",\"type\":\"2\",\"total\":\"0\",\"questions\":[]},{\"desc\":\"\",\"type\":\"3\",\"total\":\"0\",\"questions\":[]},{\"desc\":\"判断题\",\"type\":\"4\",\"total\":\"3\",\"questions\":[{\"points\":\"2\",\"question\":\"<p><span style=\\\"font-size:14px;font-family:宋体\\\">调酒师是指在酒吧或餐厅专门从事配制酒水、销售酒水，并让客人领略酒的文化各风情的人员，调酒师英语称为<span>bartender<\\/span>或<span>barman<\\/span>。<\\/span><\\/p>\",\"body\":{\"answer\":\"1\"},\"type\":\"4\",\"id\":\"12518\"},{\"points\":\"2\",\"question\":\"<p>调酒师的规模已经可以满足酒吧行业的人才需求。（ &nbsp; &nbsp;）<br\\/><\\/p>\",\"body\":{\"answer\":\"2\"},\"type\":\"4\",\"id\":\"12519\"},{\"points\":\"2\",\"question\":\"<p><span style=\\\"font-size:14px;font-family:宋体\\\">调酒师每日工作前必须对自己的形象进行整理。（ &nbsp; &nbsp;）<\\/span><\\/p>\",\"body\":{\"answer\":\"1\"},\"type\":\"4\",\"id\":\"12520\"}]}],\"status\":1,\"submitEnd\":1551024000000,\"number\":2,\"content\":\"1、调酒师的含义；2、调酒师的职业素质；3、调酒师工作内容。\"},\"forumId\":null,\"forum\":{\"id\":\"\",\"title\":\"8.4 小节讨论\",\"status\":\"2\",\"content\":\"<p><span style=\\\"font-size:16px;font-family:宋体\\\">中国传统文化中，每年农历七月初七是乞巧节，也叫七夕节。<\\/span><\\/p><p><span style=\\\"font-size:16px;font-family:宋体\\\">讨论这个节日的传统活动有哪些？当前又有什么演变？演变原因是什么？<\\/span><\\/p>\"},\"ownerId\":\"547690\",\"created\":1531315580257,\"lastUpdated\":1531315580257,\"owner\":\"12758030204\",\"chapterId\":\"42545\",\"selfId\":\"1\",\"isLeader\":\"1\"},{\"id\":\"68140\",\"parentId\":\"42550\",\"chapter\":{\"id\":\"42550\",\"title\":\"调酒师与调酒比赛\",\"status\":\"1\"},\"courseId\":\"334564673\",\"category\":\"3\",\"title\":null,\"resId\":null,\"resource\":{\"id\":\"\",\"title\":null,\"authorName\":null,\"resSort\":null,\"resMediaType\":null,\"resSize\":null,\"viewer\":null,\"oid\":\"\",\"username\":null,\"resMediaType_lk_display\":null,\"pages\":null,\"duration\":null,\"dimension\":null,\"resourceType_lk_display\":null},\"testId\":null,\"test\":\"Not a number:\",\"forumId\":\"3483225\",\"forum\":{\"id\":\"3483225\",\"title\":\"调酒师职业资格考证是不是取消了？\",\"status\":\"1\",\"content\":\"<div class=\\\"f-title\\\" style=\\\"color: rgb(51, 51, 51); font-size: 15px; font-family: &#39;Microsoft YaHei&#39;, 微软雅黑, 宋体, Helvetica, &#39;STHeiti STXihei&#39;, &#39;Microsoft JhengHei&#39;, Arial; line-height: 22px; white-space: normal;\\\"><span style=\\\"color: rgb(68, 68, 68); font-size: 13px;\\\">&nbsp; &nbsp; &nbsp; 是的。国内的调酒师职业资格考证已经取消了。但是国外的调酒师职业资格考证还有。另外。考证的取消并不影响该职业的发展。调酒师岗位的需求不受影响。目前行业对调酒师岗位的需求非常大。<\\/span><\\/div>\"},\"ownerId\":\"547690\",\"created\":1531315580260,\"lastUpdated\":1531315580260,\"owner\":\"12758030204\",\"chapterId\":\"42545\",\"selfId\":\"2\",\"isLeader\":\"1\"},{\"id\":\"68141\",\"parentId\":\"42550\",\"chapter\":{\"id\":\"42550\",\"title\":\"调酒师与调酒比赛\",\"status\":\"1\"},\"courseId\":\"334564673\",\"category\":\"1\",\"title\":\"调酒师与调酒比赛\",\"resId\":\"60142\",\"resource\":{\"id\":\"60142\",\"title\":\"项目一：认识鸡尾酒与调酒师（5）\",\"authorName\":\"殷开明\",\"resSort\":\"1\",\"resMediaType\":\"doc\",\"resSize\":\"353 KB\",\"viewer\":\"1\",\"oid\":\"60142\",\"username\":\"12758030204\",\"resMediaType_lk_display\":\"Word文档\",\"pages\":\"12\",\"duration\":null,\"dimension\":null,\"resourceType_lk_display\":\"教学课件\"},\"testId\":null,\"test\":\"Not a number:\",\"forumId\":null,\"forum\":{\"id\":\"\",\"title\":\"8.4 小节讨论\",\"status\":\"2\",\"content\":\"<p><span style=\\\"font-size:16px;font-family:宋体\\\">中国传统文化中，每年农历七月初七是乞巧节，也叫七夕节。<\\/span><\\/p><p><span style=\\\"font-size:16px;font-family:宋体\\\">讨论这个节日的传统活动有哪些？当前又有什么演变？演变原因是什么？<\\/span><\\/p>\"},\"ownerId\":\"547690\",\"created\":1531315580261,\"lastUpdated\":1531315580261,\"owner\":\"12758030204\",\"chapterId\":\"42545\",\"selfId\":\"3\",\"isLeader\":\"1\"},{\"id\":\"70710\",\"parentId\":\"42550\",\"chapter\":{\"id\":\"42550\",\"title\":\"调酒师与调酒比赛\",\"status\":\"1\"},\"courseId\":\"334564673\",\"category\":\"1\",\"title\":\"调酒师和调酒比赛\",\"resId\":\"98225\",\"resource\":{\"id\":\"98225\",\"title\":\"1.5调酒师和调酒比赛\",\"authorName\":\"殷开明\",\"resSort\":\"45\",\"resMediaType\":\"video\",\"resSize\":\"399473 KB\",\"viewer\":\"2\",\"oid\":\"98225\",\"username\":\"12758030204\",\"resMediaType_lk_display\":\"视频\",\"pages\":null,\"duration\":\"00:05:28.520\",\"dimension\":\"1920x1080\",\"resourceType_lk_display\":\"微视频\"},\"testId\":null,\"test\":\"Not a number:\",\"forumId\":null,\"forum\":{\"id\":\"\",\"title\":\"8.4 小节讨论\",\"status\":\"2\",\"content\":\"<p><span style=\\\"font-size:16px;font-family:宋体\\\">中国传统文化中，每年农历七月初七是乞巧节，也叫七夕节。<\\/span><\\/p><p><span style=\\\"font-size:16px;font-family:宋体\\\">讨论这个节日的传统活动有哪些？当前又有什么演变？演变原因是什么？<\\/span><\\/p>\"},\"ownerId\":\"547690\",\"created\":1535271960654,\"lastUpdated\":1535271960654,\"owner\":\"12758030204\",\"chapterId\":\"42545\",\"selfId\":\"4\",\"isLeader\":\"1\"}]}";
        new APIController().buildLessions(lessonRes);
        return;

    }
}

