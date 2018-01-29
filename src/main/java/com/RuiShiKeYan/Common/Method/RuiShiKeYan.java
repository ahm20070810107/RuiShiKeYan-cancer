package com.RuiShiKeYan.Common.Method;

import com.RuiShiKeYan.Common.Interface.IruiShiKeYan;
import com.alibaba.fastjson.JSONObject;
import org.bson.Document;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created with IntelliJ IDEA
 * User:huangming
 * Date:2017/12/12
 * Time:上午11:16
 */
public abstract class RuiShiKeYan  {


    public  String getJSonValue(JSONObject jsonObject, String key)
    {
        if(jsonObject == null || key == null)return "";
        if(jsonObject.getString(key) ==null)
            return "";
        return jsonObject.getString(key);
    }
    public  String get10JSonValue(Document jsonObject, String key)
    {
        if(jsonObject == null || key == null)return "N";
        if(jsonObject.getString(key) ==null)
            return "N";
        if(jsonObject.getString(key).length()>10)
            return jsonObject.getString(key).substring(0,10);
        return jsonObject.getString(key);
    }
    public String getJSonValue(Document jsonObject, String key)
    {
        if(jsonObject == null || key == null)return "";
        if(jsonObject.getString(key) ==null)
            return "";
        return jsonObject.getString(key);
    }
    public String getAge(String source ,String dest)
    {
       try
       {
           Integer result=Integer.valueOf(source)-Integer.valueOf(dest);
           return result.toString();
       }catch (Exception e)
       {
           return "-1";
       }
    }
    public String getSexMapping(String sex)
    {
        if(sex.equals("男"))
            return "Male";
        if(sex.equals("女"))
            return "Female";
        return "";
    }
    public   String getAgeGroup(int age)
    {
        if(age < 0)
            return "异常";
        if(age >=0 &&age<=18)
            return "Child";
        else if(age>=19 &&age<=49)
            return "Adult";
        else if(age>=50 &&age<=100)
            return "Late";
        return "异常";
    }
    public   String getAgeGroup(String strage)
    {
        if(strage==null ||strage.equals(""))
            return "异常";
        Integer age=-1;
        try
        {  if(strage.indexOf(".")>0)
            age=Integer.valueOf(strage.substring(0,strage.indexOf(".")));
        else age=Integer.valueOf(strage);
        }catch (Exception e){e.printStackTrace();}
        if(age.intValue()>=0 &&age.intValue()<=18)
            return "Child";
        else if(age.intValue()>=19 &&age.intValue()<=49)
            return "Adult";
        else if(age.intValue()>=50 &&age.intValue()<=100)
            return "Late";
        return "异常";
    }

    public   String getAgeGroup(String strage,boolean flag)
    {
        if(strage==null ||strage.equals(""))
            return "异常";
        Integer age=-1;
        try
        {  if(strage.indexOf(".")>0)
            age=Integer.valueOf(strage.substring(0,strage.indexOf(".")));
        else age=Integer.valueOf(strage);
        }catch (Exception e){e.printStackTrace();}
        if(age.intValue()>=0 &&age.intValue()<=18)
            return "青少年";
        else if(age.intValue()>=19 &&age.intValue()<=49)
            return "成人";
        else if(age.intValue()>=50 &&age.intValue()<=100)
            return "晚发";
        return "异常";
    }

    public void fillStringArrayListMap(Map<String,HashSet<String>> mapArray, String keyValue, String arrValue)
    {
        if(mapArray==null)
            return;
        if(mapArray.containsKey(keyValue))
        {
            HashSet<String> arrList=mapArray.get(keyValue);
            arrList.add(arrValue);
        }else {
            HashSet<String> arrList= new HashSet<String>();
            arrList.add(arrValue);
            mapArray.put(keyValue,arrList);
        }
    }

    public void fillStringMapMap(Map<String,Map<String,String>> mapArray,String keyValue,String arrValue,String firstTime)
    {
        if(mapArray==null)
            return;
        if(mapArray.containsKey(keyValue))
        {
            Map<String,String> mapB=mapArray.get(keyValue);
            mapB.put(arrValue,firstTime);
        }else {
            Map<String,String> mapB= new HashMap<String, String>();
            mapB.put(arrValue,firstTime);
            mapArray.put(keyValue,mapB);
        }
    }
    public Double getPercent(int upNum,int downNum)
    {
        // 创建一个数值格式化对象
        if(downNum ==0)
            return 0.0;
        NumberFormat numberFormat = NumberFormat.getInstance();
        // 设置精确到小数点后2位
        numberFormat.setMaximumFractionDigits(2);
        String result = numberFormat.format((float)upNum/(float)downNum*100);
    //    System.out.println("diliverNum和queryMailNum的百分比为:" + result + "%");
        return Double.valueOf(result);
    }
}
