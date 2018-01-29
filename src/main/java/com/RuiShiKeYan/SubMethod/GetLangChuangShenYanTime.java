package com.RuiShiKeYan.SubMethod;

import com.RuiShiKeYan.Common.Method.DateFormat;
import com.RuiShiKeYan.Common.Method.ReadExcelToMap;
import com.RuiShiKeYan.Common.Method.RuiShiKeYan;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA
 * User:huangming
 * Date:2018/1/7
 * Time:下午9:49
 */
public class GetLangChuangShenYanTime extends RuiShiKeYan {

    private Map<String,Document> mapLCShengyanHYInfo= new HashMap<String, Document>();
    private Map<String,Document> mapLCShengyanZZInfo=new HashMap<String, Document>();
    private Map<String,Document> mapLCShengyanTZInfo=new HashMap<String, Document>();
    private Map<String,Document> mapLCShengyanZDInfo=new HashMap<String, Document>();

    public void fillBasicInfo(MongoDatabase mdb)
    {
        try {
            JSONObject jsShenYanList = ReadExcelToMap.getShenYanList();  //加载狼疮肾炎
            if (!getJSonValue(jsShenYanList, "ZDList").equals(""))
                getHDPInfo.getLCShengyanZDInfo(mdb, ",'标准诊断名':{$in:[" + getJSonValue(jsShenYanList, "ZDList") + "]}", mapLCShengyanZDInfo);
            if (!getJSonValue(jsShenYanList, "HYList").equals(""))
                getHDPInfo.getLCShengyanHYInfo(mdb, mapLCShengyanHYInfo, getJSonValue(jsShenYanList, "HYList"), ",'RPG科研结果转换':'阳性'");
            if (!getJSonValue(jsShenYanList, "TZList").equals(""))
                getHDPInfo.getTZLangCShengyan(mdb, mapLCShengyanTZInfo, getJSonValue(jsShenYanList, "TZList"));
            if (!getJSonValue(jsShenYanList, "ZZList").equals(""))
                getHDPInfo.getZZLangCShengyan(mdb, mapLCShengyanZZInfo, getJSonValue(jsShenYanList, "ZZList"));
        }catch (Exception e){e.fillInStackTrace();}
    }

    public   String getLCShenYanTime(String strPID)
    {
        String strNewTime="first";
        if(mapLCShengyanZZInfo.containsKey(strPID))
        {
            Document dd=mapLCShengyanZZInfo.get(strPID);
            if(strNewTime.compareTo(dd.getString("症状&体征时间"))>0 )
            {
                strNewTime=dd.getString("症状&体征时间");
            }
        }
        if(mapLCShengyanTZInfo.containsKey(strPID))
        {
            Document dd=mapLCShengyanTZInfo.get(strPID);
            if(strNewTime.compareTo(dd.getString("症状&体征时间"))>0 )
            {
                strNewTime=dd.getString("症状&体征时间");
            }
        }
        if(mapLCShengyanZDInfo.containsKey(strPID))
        {
            Document dd=mapLCShengyanZDInfo.get(strPID);
            if(strNewTime.compareTo(dd.getString("诊断时间"))>0 )
            {
                strNewTime=dd.getString("诊断时间");
            }
        }
        if(mapLCShengyanHYInfo.containsKey(strPID))
        {
            Document dd=mapLCShengyanHYInfo.get(strPID);
            if(strNewTime.compareTo(dd.getString("化验时间"))>0 )
            {
                strNewTime=dd.getString("化验时间");
            }
        }

        if(strNewTime.equals("first"))
            return "";
        return DateFormat.getDateFormatDay(strNewTime);
    }
}
