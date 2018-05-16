package com.xxl.job.executor.nextpage;

<<<<<<< HEAD
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
=======
import com.ustcinfo.ptp.yunting.support.JsonKeys;
import com.xxl.job.executor.util.CommonHelper;
import com.xxl.job.executor.util.StringHas;
import com.xxl.job.executor.util.WebDriverHelper;
>>>>>>> 5d68a508ba5119927fb3da65abcb3ee27ad9168a

import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.util.StringUtils;

<<<<<<< HEAD
import com.ustcinfo.ptp.yunting.support.JsonKeys;
import com.xxl.job.executor.util.CommonHelper;
import com.xxl.job.executor.util.WebDriverHelper;
=======
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
>>>>>>> 5d68a508ba5119927fb3da65abcb3ee27ad9168a

/**
 * Created by lisen on 2017/12/6.
 */
public class RulePageGenerator implements NextPageGenerator{
    @Override
    public WebElement getNextPageElement(WebDriver webDriver, int currentPageNo, JSONArray pageFlipArray, List<String> listPageUrls) throws Exception {
        if (pageFlipArray == null || pageFlipArray.length() < 1)
            throw new Exception("pageFlipArray is null.");
        else if (pageFlipArray.length() == 1) {
            if(pageFlipArray.getJSONObject(0).getString(JsonKeys.ZCD_PAGE_TYPE).equals("单击")){
                WebElement element = WebDriverHelper.findNextPageElement(webDriver, currentPageNo,pageFlipArray);
                findNextPageElement(webDriver,currentPageNo,element,listPageUrls) ;
            }else{
                JSONObject nextPageRule = pageFlipArray.getJSONObject(0) ;
<<<<<<< HEAD
                String nextPageUrl= getNextPageUrl(nextPageRule.getString(JsonKeys.ZCD_PAGE_NEXTXPATH),nextPageRule.getString(JsonKeys.ZCD_PAGE_INPUT),currentPageNo) ;
=======
                String nextPageUrl= StringHas.getNextPageUrl(nextPageRule.getString(JsonKeys.ZCD_PAGE_NEXTXPATH),nextPageRule.getString(JsonKeys.ZCD_PAGE_INPUT),currentPageNo) ;
>>>>>>> 5d68a508ba5119927fb3da65abcb3ee27ad9168a
                if(null!=nextPageUrl){
                    webDriver.get(nextPageUrl) ;
                }
            }
        } else {
            WebElement element = WebDriverHelper.findNextPageElement(webDriver, currentPageNo,pageFlipArray);
            findNextPageElement(webDriver,currentPageNo,element,listPageUrls) ;
        }
        return null;
    }

    public void findNextPageElement(WebDriver webDriver, int currentPageNo,WebElement element, List<String> listPageUrls) throws Exception {
        if (element != null) {
            // Thread.sleep(500);
            if (WebDriverHelper.hrefIsValid(element)) {
                webDriver.get(element.getAttribute("href"));
            } else {
                element.click();
                WebDriverHelper.switchToNewWindow(webDriver);
            }
        } else {
            String nextPageUrl = CommonHelper.findNextPageByUrlComparison(currentPageNo,
                    listPageUrls);
            if (!StringUtils.hasText(nextPageUrl))
                throw new Exception(
                        "#LAD# page processor [get next list page url], cannot find page flip element , current pageNo: "
                                + currentPageNo);
            webDriver.get(nextPageUrl);
        }
    }
<<<<<<< HEAD

    public String getNextPageUrl(String xpath ,String input, int currentPageNo){
        String pattern = "$\\{(.+?)}" ;
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(xpath);
        List<String> params = new ArrayList<String>() ;
        while(m.find()){
            params.add(m.group(1)) ;
        }

        String nextPageUrl = null ;
        if(input.indexOf("[{")>-1){
            //TODO:解析json
            //JSONArray jsonArray = JSONObject.parseArray(input) ;
            //太复杂，暂不实现
        }else if(input.contains("-")){
            //TODO:解析范围页码
            String[] ym = input.split("-") ;
            int begin = Integer.parseInt(ym[0]) ;
            int end = Integer.parseInt(ym[1]) ;
            if(begin<end){
                if(currentPageNo<end){
                    nextPageUrl = xpath.replace("${pageNo}",(currentPageNo+1)+"") ;
                }
            }else{
                if(currentPageNo>end){
                    nextPageUrl = xpath.replace("${pageNo}",(currentPageNo-1)+"") ;
                }
            }

        }else{
            int end = Integer.parseInt(input) ;
            if(currentPageNo<end){
                nextPageUrl = xpath.replace("${pageNo}",(currentPageNo+1)+"") ;
            }
        }

        return nextPageUrl ;
    }

=======
>>>>>>> 5d68a508ba5119927fb3da65abcb3ee27ad9168a
}
