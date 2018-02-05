package com.RuiShiKeYan.RAExportTable.entity;

import com.RuiShiKeYan.Common.Method.LocalHostInfo;
import com.RuiShiKeYan.Common.Method.ReadExcelToMap;
import com.RuiShiKeYan.Common.Method.RuiShiKeYan;
import com.RuiShiKeYan.SubMethod.getHDPInfo;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.client.MongoDatabase;
import com.yiyihealth.data.DaX.reader.DSExcelReader2;
import org.bson.Document;
import test.java.task_SLE_LangChuang.BaseInfo_Title_ListValue_DBCondition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA
 * User:huangming
 * Date:2018/2/5
 * Time:下午3:34
 */
public class YYRelatedTable extends RuiShiKeYan{

    public Map<String,ArrayList<String>> mapLeiJiSubFenZu = new HashMap<String, ArrayList<String>>();
    public    Map<String,Document> mapYY = new HashMap<String, Document>();
    public    Map<String,Document> mapZD = new HashMap<String, Document>();
    public    Map<String,Document> mapZZ = new HashMap<String, Document>();
    public    Map<String,Document> mapTZ = new HashMap<String, Document>();
    public    Map<String,Document> mapHY = new HashMap<String, Document>();
    public    Map<String,Document> mapHYRPG = new HashMap<String, Document>();
    public    Map<String,JSONObject> mapPIDInfo= new HashMap<String, JSONObject>();

    public void getBasicInfo(MongoDatabase mdb)
    {
        try
        {
            String fileName = LocalHostInfo.getPath() + "交付/首诊时间表.xlsx";
            ReadExcelToMap.readFromExcelToMap(mapPIDInfo,fileName,"患者（PID）",true);
            getSubAndItemMap(mapLeiJiSubFenZu);
            //加载每PID所有表型第一次及最后一次发生时间
            getHDPInfo.getYYDay(mdb,mapYY,"");
            getHDPInfo.getZDDay(mdb,mapZD,"");
            getHDPInfo.getHYDay(mdb,mapHY,",'化验结果定性（新）':'阳性'");
            getHDPInfo.getHYDay(mdb,mapHYRPG,",'RPG科研结果转换':'阳性'");
            getHDPInfo.getTZDay(mdb,mapTZ,"");
            getHDPInfo.getZZDay(mdb,mapZZ,"");

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getSubAndItemMap(Map<String,ArrayList<String>> mapLeiJiSubFenZu) throws Exception
    {
        String fileName= LocalHostInfo.getPath()+ BaseInfo_Title_ListValue_DBCondition.strCLeiJiFenZuFileName;
        String tempFenZu,tempZuHe;
        JSONObject config = new JSONObject();
        config.put("filename", fileName);
        config.put("source_type", "excel");
        JSONObject document;
        DSExcelReader2 excelReader = new DSExcelReader2(config);
        while ((document = excelReader.nextDocument()) != null) {
            tempZuHe=getJSonValue(document,"表型名称")+getJSonValue(document,"标准标本");
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
}
