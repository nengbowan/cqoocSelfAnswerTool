//package net.cqooc.tool;
//
//import net.cqooc.tool.domain.enums.BrowserElementType;
//import org.openqa.selenium.By;
//import org.openqa.selenium.Keys;
//import org.openqa.selenium.WebElement;
//import org.openqa.selenium.firefox.FirefoxDriver;
//import org.openqa.selenium.support.ui.ExpectedConditions;
//import org.openqa.selenium.support.ui.WebDriverWait;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
///**
// * selenium自动化支持程度不高  , 故不采用
// */
//public class SeleniumUtil {
//    private String username;
//
//    private String password;
//    private SeleniumUtil(String username , String password ){
//        this.username = username;
//        this.password = password;
//    }
//    public void login(){
//        // TODO Auto-generated method stub
//        //如果测试的浏览器没有安装在默认目录，那么必须在程序中设置
//        //bug1:System.setProperty("webdriver.chrome.driver", "C://Program Files (x86)//Google//Chrome//Application//chrome.exe");
//        //bug2:System.setProperty("webdriver.chrome.driver", "C://Users//Yoga//Downloads//chromedriver_win32//chromedriver.exe");
//        //System.setProperty("webdriver.chrome.driver", "D://tanzhenTest//chromedriver_win32//chromedriver.exe");
//        FirefoxDriver firefoxDriver = new FirefoxDriver();
//
//        String loginUrl = "http://www.cqooc.net/login";
//        firefoxDriver.get(loginUrl);
//        // 获取 网页的 title
//        //System.out.println("The testing page title is: " + firefoxDriver.getTitle());
//
//        WebDriverWait webDriverWait=new WebDriverWait(firefoxDriver,5);
//
//        //ensure that all condition can be done
//        //username editbale  password  editable login clickable
//        ensureClickPreCondition( webDriverWait );
//
//        login(firefoxDriver);
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//
////        navigate2UserInfo();
//
//        firefoxDriver.get("http://www.cqooc.net/learn");
//
//        //<a class="btn2" href="/learn/mooc?id=334564673&amp;cid=11173712">进入学习</a>
//
//
//        webDriverWait.until(ExpectedConditions.elementToBeClickable(By.className("btn2")));
//
//        WebElement studyButton = firefoxDriver.findElementByClassName("btn2");
//
//        studyButton.click();
//
//        WebElement studyContent = firefoxDriver.findElementByXPath("/html/body/div[1]/div[2]/div/div[2]/ul/li[3]/a");
//        webDriverWait.until(ExpectedConditions.elementToBeClickable(studyContent));
//        studyContent.click();
//
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//
//        //firefox低版本核心可能弹窗提示 TODO
//        List<WebElement> allLinks = firefoxDriver.findElementsByTagName("a");
//        List<WebElement> listenLinks = allLinks.stream().filter((WebElement ele)->{
//            return ele.getAttribute("onclick") != null;
//        }).collect(Collectors.toList());
//
//        List<String> listenIds = new ArrayList<>();
//
//
//        String xsid = getXsid(firefoxDriver);
//
//
//        for(WebElement listenLink : listenLinks){
//            String onclickValue = listenLink.getAttribute("onclick");
//            String tempValue = onclickValue.replace("X.pub('getLesson','","");
//            int endIndex = tempValue.indexOf("'");
//            listenIds.add(tempValue.substring(0,endIndex));
//        }
//
//
//
//
//        String userId = new APIController().getUserId(xsid);
//
//
//
//        return;
//
//
//    }
//
//
//
//    private String getXsid(FirefoxDriver firefoxDriver) {
//        return firefoxDriver.manage().getCookieNamed("xsid").getValue();
//    }
//
//    private void login(FirefoxDriver firefoxDriver) {
//        WebElement usernameText = firefoxDriver.findElementByName("username");
//        usernameText.sendKeys(this.username);
//        WebElement passwordText = firefoxDriver.findElementByName("password");
//        passwordText.sendKeys(this.password);
//        WebElement loginEle = firefoxDriver.findElement(By.id("loginBtn"));
//        loginEle.click();
//
//
//        return;
//    }
//
//    private void ensureClickPreCondition(WebDriverWait webDriverWait) {
//        webDriverWait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
//        webDriverWait.until(ExpectedConditions.elementToBeClickable(By.name("password")));
//        //loginBtn
//        webDriverWait.until(ExpectedConditions.elementToBeClickable(By.id("loginBtn")));
//    }
//    private void ensureClickable(WebDriverWait webDriverWait , BrowserElementType elementType , String id){
//        if(elementType == BrowserElementType.NAME){
//            webDriverWait.until(ExpectedConditions.elementToBeClickable(By.name(id)));
//        }else if(elementType == BrowserElementType.ID){
//            webDriverWait.until(ExpectedConditions.elementToBeClickable(By.id(id)));
//        }else ;
//    }
//
//    public static void main(String[] args) {
//        String username = "106372016051413207";
//
//        String password = "123456ABc";
//        new SeleniumUtil(username , password).login();
//    }
//
//}
