package com.RuiShiKeYan.Common.Method;

import com.alibaba.fastjson.JSONObject;
import com.yiyihealth.data.DaX.reader.DSExcelReader2;
import test.java.task_SLE_LangChuang.BaseInfo_Title_ListValue_DBCondition;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA
 * User:huangming
 * Date:2017/12/13
 * Time:下午4:40
 */
public class ReadExcelToMap  {

    static JSONObject document;
    public static  void readFromExcelToMap(Map<String,String> mapResult, String fileName, String KeyName) throws Exception
    {
        JSONObject config = new JSONObject();
        config.put("filename", fileName);
        config.put("source_type", "excel");

        DSExcelReader2 excelReader = new DSExcelReader2(config);
        while((document=excelReader.nextDocument()) != null) {
            if(document.getString(KeyName)!=null && !document.getString(KeyName).equals(""))
                mapResult.put(document.getString(KeyName),"0");
        }
    }
    public static  void readFromExcelToMap(Map<String,JSONObject> mapResult, String fileName, String KeyName,String storeKey1, String ... storeKeyn) throws Exception
    {
        if(storeKey1.equals(""))
            return;
        JSONObject config = new JSONObject();
        config.put("filename", fileName);
        config.put("source_type", "excel");

        Set<String> setKeys= new HashSet<String>();
        setKeys.add(storeKey1);
        for(String str : storeKeyn)
            setKeys.add(str);

        DSExcelReader2 excelReader = new DSExcelReader2(config);
        while((document=excelReader.nextDocument()) != null) {

            if (document.getString(KeyName) != null && !document.getString(KeyName).equals("")) {
                JSONObject jsonObject = new JSONObject();
                for(String skeys: setKeys) {
                    jsonObject.put(skeys,document.getString(skeys));
                }
                mapResult.put(document.getString(KeyName), jsonObject);
            }
        }
    }
    public static  void readFromExcelToMap(Set<String> mapResult, String fileName, String KeyName) throws Exception
    {
        JSONObject config = new JSONObject();
        config.put("filename", fileName);
        config.put("source_type", "excel");

        DSExcelReader2 excelReader = new DSExcelReader2(config);
        while((document=excelReader.nextDocument()) != null) {
            if(document.getString(KeyName)!=null && !document.getString(KeyName).equals(""))
                mapResult.add(document.getString(KeyName));
        }
    }
    public static  void readFromExcelToMap(Map<String,JSONObject> mapResult,String fileName,String KeyName,Boolean flag) throws Exception
    {
        JSONObject config = new JSONObject();
        config.put("filename", fileName);
        config.put("source_type", "excel");

        DSExcelReader2 excelReader = new DSExcelReader2(config);
        while((document=excelReader.nextDocument()) != null) {
            mapResult.put(document.getString(KeyName),document);
        }
    }

    public static  void readFromExcelToMap(Map<String,JSONObject> mapResult,String fileName,String KeyName,Map<String,String> mapExcept) throws Exception
    {
        JSONObject config = new JSONObject();
        config.put("filename", fileName);
        config.put("source_type", "excel");

        DSExcelReader2 excelReader = new DSExcelReader2(config);
        while((document=excelReader.nextDocument()) != null) {
            if(mapExcept.containsKey(getJSonValue(document,KeyName)))
                continue;
            mapResult.put(document.getString(KeyName),document);
        }
    }
    public static void getLeiJiFenZu(String keyName,Map<String,ArrayList<String>> map) throws Exception
    {
        String fileName= LocalHostInfo.getPath()+ BaseInfo_Title_ListValue_DBCondition.strCLeiJiFenZuFileName;
        String tempFenZu,tempZuHe;
        JSONObject config = new JSONObject();
        config.put("filename", fileName);
        config.put("source_type", "excel");

        DSExcelReader2 excelReader = new DSExcelReader2(config);
        while ((document = excelReader.nextDocument()) != null) {
            tempFenZu=getJSonValue(document,keyName);
            tempZuHe=getJSonValue(document,"表型名称")+getJSonValue(document,"标准标本");
            if(tempFenZu.equals("")||tempFenZu.toLowerCase().equals("n"))
                continue;
            if(map.containsKey(tempFenZu)) {
                ArrayList arrayList=map.get(tempFenZu);
                arrayList.add(tempZuHe);
            }
            else
            {
                ArrayList arrayList=new ArrayList();
                arrayList.add(tempZuHe);
                map.put(tempFenZu,arrayList);
            }
        }
    }
    public static String getJSonValue(JSONObject jsonObject,String key)
    {
        if(jsonObject == null || key == null)return "";
        if(jsonObject.getString(key) ==null)
            return "";
        return jsonObject.getString(key);
    }

    public static void getSubAndItemMap(Map<String,ArrayList<String>> mapLeiJiFenZu,
                                  Map<String,ArrayList<String>> mapLeiJiSubFenZu) throws Exception
    {
        String fileName= LocalHostInfo.getPath()+BaseInfo_Title_ListValue_DBCondition.strCLeiJiFenZuFileName;
        String tempFenZu,tempZuHe;
        JSONObject config = new JSONObject();
        config.put("filename", fileName);
        config.put("source_type", "excel");

        DSExcelReader2 excelReader = new DSExcelReader2(config);
        while ((document = excelReader.nextDocument()) != null) {
            tempFenZu=getJSonValue(document,"拟观察系统累及分组");
            tempZuHe=getJSonValue(document,"表型名称")+getJSonValue(document,"标准标本");
            if(!tempFenZu.equals("")&&!tempFenZu.toUpperCase().equals("N")) {
                if (mapLeiJiFenZu.containsKey(tempFenZu)) {
                    ArrayList arrayList = mapLeiJiFenZu.get(tempFenZu);
                    arrayList.add(tempZuHe);
                } else {
                    ArrayList arrayList = new ArrayList();
                    arrayList.add(tempZuHe);
                    mapLeiJiFenZu.put(tempFenZu, arrayList);
                }
            }
            tempFenZu=getJSonValue(document,"子项");
            if(!tempFenZu.toUpperCase().equals("N")&&!tempFenZu.equals(""))
                if(mapLeiJiSubFenZu.containsKey(tempFenZu)) {
                    ArrayList arrayList=mapLeiJiSubFenZu.get(tempFenZu);
                    arrayList.add(tempZuHe);
                }
                else
                {
                    ArrayList arrayList=new ArrayList();
                    arrayList.add(tempZuHe);
                    mapLeiJiSubFenZu.put(tempFenZu,arrayList);
                }
        }
    }
    public static JSONObject getShenYanList() throws Exception
    {
        String  strZDList="",strTZList="",strZZList="",strHYList="";

        JSONObject document;
        String fileName= LocalHostInfo.getPath()+BaseInfo_Title_ListValue_DBCondition.strCLeiJiFenZuFileName;;
        JSONObject config = new JSONObject();
        String tempFenZu,tempZuHe,tempEntityName;
        config.put("filename", fileName);
        config.put("source_type", "excel");
        DSExcelReader2 excelReader = new DSExcelReader2(config);
        while ((document = excelReader.nextDocument()) != null) {
            tempFenZu =getJSonValue(document,"对标观察项目");
            tempZuHe=getJSonValue(document,"表型名称")+getJSonValue(document,"标准标本");
            tempEntityName=getJSonValue(document,"表型");
            if(tempFenZu.equals("肾炎")&&!tempZuHe.equals("") &&tempEntityName.equals("标准诊断名"))
            {
                strZDList +="'"+tempZuHe+"',";
            }
            if(tempFenZu.equals("肾炎")&&!tempZuHe.equals("") &&tempEntityName.equals("化验组合"))
            {
                strHYList +="'"+tempZuHe+"',";
            }
            if(tempFenZu.equals("肾炎")&&!tempZuHe.equals("") &&tempEntityName.equals("症状组合"))
            {
                strZZList +="'"+tempZuHe+"',";
            }
            if(tempFenZu.equals("肾炎")&&!tempZuHe.equals("") &&tempEntityName.equals("体征组合"))
            {
                strTZList +="'"+tempZuHe+"',";
            }
        }
        JSONObject jsResult = new JSONObject();
        if(strZDList.length()>0)
            jsResult.put("ZDList",strZDList.substring(0,strZDList.length()-1));
        if(strHYList.length()>0)
            jsResult.put("HYList",strHYList.substring(0,strHYList.length()-1));
        if(strZZList.length()>0)
            jsResult.put("ZZList",strZZList.substring(0,strZZList.length()-1));
        if(strTZList.length()>0)
            jsResult.put("TZList",strTZList.substring(0,strTZList.length()-1));
        return jsResult;
    }
}
