package com.xxl.job.executor.nextpage;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.WebDriver;

import com.ustcinfo.ptp.yunting.support.JsonKeys;
import com.xxl.job.executor.util.StringHas;
/**
 * Created by lisen on 2017/12/6.
 * 翻页：

 RULE-1、根据“下一页”元素进行翻页：要求--》元素位置无论在哪个页面都固定
 RULE-2、根据“下一页”文字进行翻页：要求--》规则定位在下一页按钮的父级，而且翻页按钮的文字必须是“下一页”，要求翻页的上层标签是a标签
 RULE-3、分阶段根据“下一页”进行翻页：要求--》在每个区间段中，翻页按钮位置固定
 RULE-4、根据页面数字进行翻页：要求--》要求数字的上层标签是span标签

 扩展：
 RULE-5、根据页面规则进行翻页，例如；

y-0-0-${pageNo}.html

        在input字段中输入：
        形式一：[{provice:1,city:1,pageNum:20},{
        形式二：（1）1-20,（2）300-200（*******）
        形式三：30

        RULE-5 进化版：
        1、根据扩展功能，进行其他信息的扩展，例如：http://xxxxx/y-${province}-${city}-${pageNo}.html

        优先级：ac>rule

        if(ac==1){
        调用RULE-2//还可以优化
        }else if(ac==2){
        调用RULE-4
        }else{
        if(rule==点击)
        if(规则列表==1）
        调用RULE-1
        else
        调用RULE-3
        else 按规则翻页
        调用RULE-5
        }
 */
public class TestMain {


    public static void main(String[] args){
//        String xpath = "http://******/y-${province}-${city}-${pageNo}.html" ;
//        String xpath = "http://******/y-0-${pageNo}.html" ;
//        String input = "200" ;
//
//        TestMain tm = new TestMain();
//        System.out.println(tm.getNextPageUrl(xpath,input,200));
    	
<<<<<<< HEAD
    	String template = "1|中移-${ext1}|${ext3}|zy_f(md5Tourl)|zy_f(now,HHmmSS)|${ext2}|zy_f(date,2017年12月23日)|ext3|zy_f(url)|id|#\n\r ";
=======
    	String template = "1|中移-${ext1}|${ext3}|zy_f(md5Tourl)|zy_f(now,HHmmSS)|${ext2}|ext3|zy_f(url)|id|#\n\r ";
>>>>>>> 5d68a508ba5119927fb3da65abcb3ee27ad9168a
    	List<String> keys =  new ArrayList<String>() ;
    	keys.add("ext1") ;
    	keys.add("ext2") ;
//    	keys.add("ext3") ;
    	
<<<<<<< HEAD
    	System.out.println(">>>>>"+StringHas.encapTemplate(template, keys.iterator(), "http://19216..asdfasdf"));
=======
    //	System.out.println(">>>>>"+StringHas.encapTemplate(template, keys.iterator(), "http://19216..asdfasdf"));
>>>>>>> 5d68a508ba5119927fb3da65abcb3ee27ad9168a

    }

    public void acLeZero(String oper, WebDriver webDriver, int currentPageNo,JSONArray jsonFlipArray){
        if("点击".equals(oper)){

        }else{
            JSONObject nextPageRule = jsonFlipArray.getJSONObject(0) ;
            String nextPageUrl= getNextPageUrl(nextPageRule.getString(JsonKeys.ZCD_PAGE_NEXTXPATH),nextPageRule.getString("fieldInput"),currentPageNo) ;
            if(null!=nextPageUrl)
                webDriver.get(nextPageUrl) ;
        }
    }

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
<<<<<<< HEAD
    
   
=======
>>>>>>> 5d68a508ba5119927fb3da65abcb3ee27ad9168a


}
