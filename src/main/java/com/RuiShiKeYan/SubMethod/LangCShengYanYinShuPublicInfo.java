package com.RuiShiKeYan.SubMethod;

import com.RuiShiKeYan.Common.Method.LocalHostInfo;
import com.RuiShiKeYan.Common.Method.SaveExcelTool;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.yiyihealth.data.DaX.reader.DSExcelReader2;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.bson.Document;
import test.java.task_SLE_LangChuang.BaseInfo_Title_ListValue_DBCondition;
import test.java.task_SLE_LangChuang.ReadFromExcelToMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA
 * User:huangming
 * Date:2017/10/31
 * Time:上午10:56
 */
public class LangCShengYanYinShuPublicInfo {

    JSONObject document;
    public  Map<String,ArrayList>  mapLeiJiFenZu = new HashMap<String, ArrayList>();
    public  Map<String,ArrayList>  mapLeiJiSubFenZu = new HashMap<String, ArrayList>();
    public  Map<String,String>     mapExcludePID=new HashMap<String,String>();
    public  Map<String,JSONObject>     mapQZShiJianBiao= new HashMap<String, JSONObject>();
    public  Map<String,JSONObject> mapBasicInfo=new HashMap<String,JSONObject>();
    public Map<String,JSONObject> mapLCShengYanPID;
    //数据库中对应子项第一次时间
    public    Map<String,Document> mapFirstYY = new HashMap<String, Document>();
    public    Map<String,Document> mapFirstZD = new HashMap<String, Document>();
    public    Map<String,Document> mapFirstZZ = new HashMap<String, Document>();
    public    Map<String,Document> mapFirstTZ = new HashMap<String, Document>();
    public    Map<String,Document> mapFirstHY = new HashMap<String, Document>();
    public    Map<String,Document> mapFirstHYRPG = new HashMap<String, Document>();
    public    Map<String,Document> mapFirstHYPinfen = new HashMap<String, Document>();
    public  void getLCShengYanPIDmap(int k ,int m) throws Exception
    {
        mapLCShengYanPID=new HashMap<String,JSONObject>();
        String fileName= LocalHostInfo.getPath()+"交付/狼疮肾炎入组表-"+k+"-"+m+".xlsx";
        JSONObject config = new JSONObject();
        config.put("filename", fileName);
        config.put("source_type", "excel");

        DSExcelReader2 excelReader = new DSExcelReader2(config);
        while ((document = excelReader.nextDocument()) != null) {
            String strPid=document.getString("患者（PID）");
            if(!mapExcludePID.containsKey(strPid) &&!strPid.equals("")) {
                if((document.getString("狼疮性肾炎分组")==null ||document.getString("狼疮性肾炎分组").equals(""))&&document.getString("确诊SLE后病程分组") !=null&&document.getString("确诊SLE后病程分组").equals("2"))
                {}else {
                    mapLCShengYanPID.put(strPid,document);
                }
            }
        }
    }
/**
 *@return 返回Jsonobject的结果
 *@param strPid   pid的值
 *@param arrayList arrayList
 *@param strLCShengYanTime
 *@param strItem  子项的值
 */
    public  JSONObject fill6To10(String strPid,ArrayList<String> arrayList,String strLCShengYanTime,String strItem)
    {
        String tempLeiJiTime="w";
        JSONObject jsonObject = new JSONObject();

        for (int i = 0; i < arrayList.size(); i++) {
            String strSrouce=strPid+arrayList.get(i);
            if(strItem.equals("肾炎")) {
                if (mapFirstHYRPG.containsKey(strSrouce)) {
                    if (mapFirstHYRPG.get(strSrouce).getString("化验时间")!=null&&tempLeiJiTime.compareTo(mapFirstHYRPG.get(strSrouce).getString("化验时间")) > 0 &&
                            mapFirstHYRPG.get(strSrouce).getString("化验时间").substring(0, 10).compareTo(strLCShengYanTime) <= 0) {
                        tempLeiJiTime = mapFirstHYRPG.get(strSrouce).getString("化验时间");
                        jsonObject.put("名称", mapFirstHYRPG.get(strSrouce).getString("化验名称_原"));
                        jsonObject.put("实体", "化验");
                        jsonObject.put("状态", "RPG科研结果转换");
                        jsonObject.put("时间天", tempLeiJiTime.substring(0, 10));
                        jsonObject.put("RID", mapFirstHYRPG.get(strSrouce).getString("RID"));
                    }
                }
            }
            else
            {
                if (mapFirstHY.containsKey(strSrouce)) {
                    if (tempLeiJiTime.compareTo(mapFirstHY.get(strSrouce).getString("化验时间")) > 0 &&
                            mapFirstHY.get(strSrouce).getString("化验时间").substring(0, 10).compareTo(strLCShengYanTime) <= 0) {
                        tempLeiJiTime = mapFirstHY.get(strSrouce).getString("化验时间");
                        jsonObject.put("名称", mapFirstHY.get(strSrouce).getString("化验名称_原"));
                        jsonObject.put("实体", "化验");
                        jsonObject.put("状态", mapFirstHY.get(strSrouce).getString("化验结果定性（新）"));
                        jsonObject.put("时间天", tempLeiJiTime.substring(0, 10));
                        jsonObject.put("RID", mapFirstHY.get(strSrouce).getString("RID"));
                    }
                }
            }

            if(mapFirstZZ.containsKey(strSrouce))
            {
                if(tempLeiJiTime.compareTo(mapFirstZZ.get(strSrouce).getString("症状&体征时间"))>0 &&
                        mapFirstZZ.get(strSrouce).getString("症状&体征时间").substring(0,10).compareTo(strLCShengYanTime)<=0)
                {
                    tempLeiJiTime=mapFirstZZ.get(strSrouce).getString("症状&体征时间");
                    jsonObject.put("名称",arrayList.get(i));
                    jsonObject.put("实体","症状");
                    jsonObject.put("状态",mapFirstZZ.get(strSrouce).getString("否定词"));
                    jsonObject.put("时间天",tempLeiJiTime.substring(0,10));
                    jsonObject.put("RID",mapFirstZZ.get(strSrouce).getString("RID"));
                }
            }
            if(mapFirstTZ.containsKey(strSrouce))
            {
                if(tempLeiJiTime.compareTo(mapFirstTZ.get(strSrouce).getString("症状&体征时间"))>0&&
                        mapFirstTZ.get(strSrouce).getString("症状&体征时间").substring(0,10).compareTo(strLCShengYanTime)<=0)
                {
                    tempLeiJiTime=mapFirstTZ.get(strSrouce).getString("症状&体征时间");
                    jsonObject.put("名称",arrayList.get(i));
                    jsonObject.put("实体","体征");
                    jsonObject.put("状态",mapFirstTZ.get(strSrouce).getString("否定词"));
                    jsonObject.put("时间天",tempLeiJiTime.substring(0,10));
                    jsonObject.put("RID",mapFirstTZ.get(strSrouce).getString("RID"));
                }
            }
            if(mapFirstZD.containsKey(strSrouce))
            {
                if(tempLeiJiTime.compareTo(mapFirstZD.get(strSrouce).getString("诊断时间"))>0 &&
                        mapFirstZD.get(strSrouce).getString("诊断时间").substring(0,10).compareTo(strLCShengYanTime)<=0)
                {
                    tempLeiJiTime=mapFirstZD.get(strSrouce).getString("诊断时间");
                    jsonObject.put("名称",mapFirstZD.get(strSrouce).getString("标准诊断名_原"));
                    jsonObject.put("实体","诊断");
                    jsonObject.put("状态",mapFirstZD.get(strSrouce).getString("诊断状态"));
                    jsonObject.put("时间天",tempLeiJiTime.substring(0,10));
                    jsonObject.put("RID",mapFirstZD.get(strSrouce).getString("RID"));
                }
            }
            if(mapFirstYY.containsKey(strSrouce))
            {
                try {
                    if (tempLeiJiTime.compareTo(mapFirstYY.get(strSrouce).getString("用药时间")) > 0 &&
                            mapFirstYY.get(strSrouce).getString("用药时间").substring(0, 10).compareTo(strLCShengYanTime) <= 0) {
                        tempLeiJiTime = mapFirstYY.get(strSrouce).getString("用药时间");
                        jsonObject.put("名称", mapFirstYY.get(strSrouce).getString("通用名_原"));
                        jsonObject.put("实体", "用药");
                        jsonObject.put("状态", mapFirstYY.get(strSrouce).getString("是否使用"));
                        jsonObject.put("时间天", tempLeiJiTime.substring(0, 10));
                        jsonObject.put("RID", mapFirstYY.get(strSrouce).getString("RID"));
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    System.out.println(mapFirstYY.get(strSrouce).getString("用药时间"));
                }
            }
        }
        if(tempLeiJiTime.equals("w"))
            return null;


        return jsonObject;
    }

    public JSONObject getANAPingFenMark(String strPid,Map<String,ArrayList> mapList,String sleTime,boolean flag,boolean ANAflag)
    {
        Map<String,Document> mapHY;
        if(ANAflag)
            mapHY=mapFirstHYRPG;
        else
            mapHY=mapFirstHYPinfen;
        JSONObject jsonObject= new JSONObject();
        String tempLeiJiTime="w";
        for(Map.Entry<String,ArrayList> mapp:mapList.entrySet()) {
           ArrayList arrayList=mapp.getValue();
            for (int i = 0; i < arrayList.size(); i++) {
                String strSrouce = strPid + arrayList.get(i);
                if (mapHY.containsKey(strSrouce)) {
                    if (tempLeiJiTime.compareTo(mapHY.get(strSrouce).getString("化验时间")) > 0 &&
                            ( mapHY.get(strSrouce).getString("化验时间").substring(0, 10).compareTo(sleTime) <= 0||flag)) {
                        tempLeiJiTime = mapHY.get(strSrouce).getString("化验时间");
                        jsonObject.put("名称", mapHY.get(strSrouce).getString("化验名称_原"));
                        jsonObject.put("实体", "化验");
                        jsonObject.put("状态", mapHY.get(strSrouce).getString("科研诊断评分定性"));
                        jsonObject.put("时间天", tempLeiJiTime.substring(0, 10));
                        jsonObject.put("RID", mapHY.get(strSrouce).getString("RID"));
                    }
                }
            }
        }

        if(tempLeiJiTime.equals("w"))
            return  null;
        return jsonObject;
    }

    public  JSONObject getYiJiMark(String strPid,ArrayList<String> arrayList,String sleTime,boolean flag)
    {
        String tempLeiJiTime="w";
        JSONObject jsonObject = new JSONObject();

        for (int i = 0; i < arrayList.size(); i++) {
            String strSrouce=strPid+arrayList.get(i);
            if (mapFirstHYPinfen.containsKey(strSrouce)) {
                if (tempLeiJiTime.compareTo(mapFirstHYPinfen.get(strSrouce).getString("化验时间")) > 0 &&
                        ( mapFirstHYPinfen.get(strSrouce).getString("化验时间").substring(0, 10).compareTo(sleTime) <= 0||flag)) {
                    tempLeiJiTime = mapFirstHYPinfen.get(strSrouce).getString("化验时间");
                    jsonObject.put("名称", mapFirstHYPinfen.get(strSrouce).getString("化验名称_原"));
                    jsonObject.put("实体", "化验");
                    jsonObject.put("状态", mapFirstHYPinfen.get(strSrouce).getString("科研诊断评分定性"));
                    jsonObject.put("时间天", tempLeiJiTime.substring(0, 10));
                    jsonObject.put("RID", mapFirstHYPinfen.get(strSrouce).getString("RID"));
                }
            }

            if(mapFirstZZ.containsKey(strSrouce))
            {
                if(tempLeiJiTime.compareTo(mapFirstZZ.get(strSrouce).getString("症状&体征时间"))>0 &&
                        (mapFirstZZ.get(strSrouce).getString("症状&体征时间").substring(0,10).compareTo(sleTime)<=0||flag))
                {
                    tempLeiJiTime=mapFirstZZ.get(strSrouce).getString("症状&体征时间");
                    jsonObject.put("名称",arrayList.get(i));
                    jsonObject.put("实体","症状");
                    jsonObject.put("状态",mapFirstZZ.get(strSrouce).getString("否定词"));
                    jsonObject.put("时间天",tempLeiJiTime.substring(0,10));
                    jsonObject.put("RID",mapFirstZZ.get(strSrouce).getString("RID"));
                }
            }
            if(mapFirstTZ.containsKey(strSrouce))
            {
                if(tempLeiJiTime.compareTo(mapFirstTZ.get(strSrouce).getString("症状&体征时间"))>0&&
                        (mapFirstTZ.get(strSrouce).getString("症状&体征时间").substring(0,10).compareTo(sleTime)<=0||flag))
                {
                    tempLeiJiTime=mapFirstTZ.get(strSrouce).getString("症状&体征时间");
                    jsonObject.put("名称",arrayList.get(i));
                    jsonObject.put("实体","体征");
                    jsonObject.put("状态",mapFirstTZ.get(strSrouce).getString("否定词"));
                    jsonObject.put("时间天",tempLeiJiTime.substring(0,10));
                    jsonObject.put("RID",mapFirstTZ.get(strSrouce).getString("RID"));
                }
            }
            if(mapFirstZD.containsKey(strSrouce))
            {
                if(tempLeiJiTime.compareTo(mapFirstZD.get(strSrouce).getString("诊断时间"))>0 &&
                        ( mapFirstZD.get(strSrouce).getString("诊断时间").substring(0,10).compareTo(sleTime)<=0||flag))
                {
                    tempLeiJiTime=mapFirstZD.get(strSrouce).getString("诊断时间");
                    jsonObject.put("名称",mapFirstZD.get(strSrouce).getString("标准诊断名_原"));
                    jsonObject.put("实体","诊断");
                    jsonObject.put("状态",mapFirstZD.get(strSrouce).getString("诊断状态"));
                    jsonObject.put("时间天",tempLeiJiTime.substring(0,10));
                    jsonObject.put("RID",mapFirstZD.get(strSrouce).getString("RID"));
                }
            }
            if(mapFirstYY.containsKey(strSrouce))
            {
                try {
                    if (tempLeiJiTime.compareTo(mapFirstYY.get(strSrouce).getString("用药时间")) > 0 &&
                            (mapFirstYY.get(strSrouce).getString("用药时间").substring(0, 10).compareTo(sleTime) <= 0||flag)) {
                        tempLeiJiTime = mapFirstYY.get(strSrouce).getString("用药时间");
                        jsonObject.put("名称", mapFirstYY.get(strSrouce).getString("通用名_原"));
                        jsonObject.put("实体", "用药");
                        jsonObject.put("状态", mapFirstYY.get(strSrouce).getString("是否使用"));
                        jsonObject.put("时间天", tempLeiJiTime.substring(0, 10));
                        jsonObject.put("RID", mapFirstYY.get(strSrouce).getString("RID"));
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        if(tempLeiJiTime.equals("w"))
            return null;

        return jsonObject;
    }
    public  void getHYYinShuInfo(MongoDatabase mdb,boolean flag)throws Exception
    {
        getSubAndItemMap();
        if(flag) {
            getQZShiJianBiao();
            getBasicInfoFromLeiJiB();
        }
        getFirstHYDay(mdb,mapFirstHY,"'化验结果定性（新）':'阳性',");

    }
    public  void getHYZhenDuanPinFen(MongoDatabase mdb)
    {
        getFirstHYDay(mdb,mapFirstHYPinfen,"'科研诊断评分定性':'阳性',");
    }
    public  void getBaseInfo(MongoDatabase mdb)throws Exception
    {
        ReadFromExcelToMap.readFromExcelToMap(mapExcludePID, LocalHostInfo.getPath()+"交付/移除组PID列表.xlsx","PID");
        getFirstADIDay(mdb);  //获取诊断
        getFirstTZDay(mdb);
        getFirstZZDay(mdb);
        getFirstYYDay(mdb);
        getFirstHYDay(mdb,mapFirstHYRPG,"'RPG科研结果转换':'阳性',");

    }

    public  void getBasicInfoFromLeiJiB() throws Exception
    {
        String fileName= LocalHostInfo.getPath()+"交付/系统累及表.xlsx";
        JSONObject config = new JSONObject();
        config.put("filename", fileName);
        config.put("source_type", "excel");

        DSExcelReader2 excelReader = new DSExcelReader2(config);
        while ((document = excelReader.nextDocument()) != null) {
            String strPid=document.getString("患者（PID）");
            if(!mapExcludePID.containsKey(strPid)) {
                if(!mapBasicInfo.containsKey(strPid))
                    mapBasicInfo.put(strPid,document);
            }
        }
    }
    public  void getQZShiJianBiao() throws Exception
    {
        String fileName= LocalHostInfo.getPath()+"交付/确诊表现表.xlsx";
        JSONObject config = new JSONObject();
        config.put("filename", fileName);
        config.put("source_type", "excel");

        DSExcelReader2 excelReader = new DSExcelReader2(config);
        while ((document = excelReader.nextDocument()) != null) {
            String strPid=document.getString("患者（PID）");
            if(!mapExcludePID.containsKey(strPid)) {
                JSONObject obj= new JSONObject();
                obj.put("初发时间天",document.getString("初发时间天"));
                obj.put("诊断时间天减去初发时间天",document.getString("诊断时间天减去初发时间天"));
                obj.put("诊断时间天",document.getString("诊断时间天"));
                obj.put("诊断时间年减去出生年",document.getString("诊断时间年减去出生年"));

                mapQZShiJianBiao.put(strPid,obj);
            }
        }
    }

    public  void getFirstYYDay(MongoDatabase dbHDP)
    {
        System.out.println("getFirstYYDay");
        MongoCollection<Document> mc = dbHDP.getCollection("ADR");
        ArrayList<Document> aggregates = new ArrayList<Document>();
        String strZDCondition="{'用药时间':{$exists:true,$regex:/^.{10,}$/},'是否使用':'使用','通用名':{$ne:'',$exists:true},"+BaseInfo_Title_ListValue_DBCondition.ADR13+"}";
        aggregates.add(new Document("$match",Document.parse(strZDCondition)));
        aggregates.add(new Document("$sort",Document.parse("{'用药时间':1}")));
        aggregates.add(new Document("$group",Document.parse("{'_id':{'PID':'$PID','通用名':'$通用名'}, 'result':{'$first':'$$ROOT'}}")));
        MongoCursor<Document> cursor =mc.aggregate(aggregates).allowDiskUse(true).iterator();
        while (cursor.hasNext())
        {
            Document  mcursor =cursor.next();
            Document groupInfo=(Document)mcursor.get("_id");

            Document dd= (Document)mcursor.get("result");
            Document obj = new Document();
            obj.put("用药时间",dd.getString("用药时间"));
            obj.put("RID",dd.getString("RID"));
            obj.put("通用名_原",dd.getString("通用名_原"));
            obj.put("是否使用",dd.getString("是否使用"));
            mapFirstYY.put(groupInfo.getString("PID")+groupInfo.getString("通用名"),obj);
        }
    }

    public  void getFirstHYDay(MongoDatabase dbHDP,Map<String,Document> mapresult,String strConditon)
    {
        System.out.println("getFirstHYDay");
        MongoCollection<Document> mc = dbHDP.getCollection("ALA");
        ArrayList<Document> aggregates = new ArrayList<Document>();
        String strHYConditon="{"+strConditon+BaseInfo_Title_ListValue_DBCondition.HY13SLE+",'化验时间':{$exists:true,$regex:/^.{10,}$/}}";
       System.out.println(strHYConditon);
        aggregates.add(new Document("$match",Document.parse(strHYConditon)));
        aggregates.add(new Document("$sort",Document.parse("{'化验时间':1}")));
        aggregates.add(new Document("$group",Document.parse("{'_id':{'PID':'$PID','标准化验名':'$标准化验名','标准标本':'$标准标本'}, 'result':{'$first':'$$ROOT'}}")));
        MongoCursor<Document> cursor =mc.aggregate(aggregates).allowDiskUse(true).iterator();
        while (cursor.hasNext())
        {
            Document  mcursor =cursor.next();
            Document groupInfo=(Document)mcursor.get("_id");
            Document dd= (Document)mcursor.get("result");
            Document obj = new Document();
            obj.put("化验时间",dd.getString("化验时间"));
            obj.put("RID",dd.getString("RID"));
            obj.put("化验结果定性（新）",dd.getString("化验结果定性（新）"));
            obj.put("化验名称_原",dd.getString("化验名称"));
            mapresult.put(groupInfo.getString("PID")+groupInfo.getString("标准化验名")+groupInfo.getString("标准标本"),obj);
        }
    }

    public  void getFirstTZDay(MongoDatabase dbHDP)
    {
        System.out.println("getFirstTZDay");
        MongoCollection<Document> mc = dbHDP.getCollection("ASY");
        ArrayList<Document> aggregates = new ArrayList<Document>();
        String strZZCondition="{"+BaseInfo_Title_ListValue_DBCondition.ZZTZ13SLE+",'否定词':'','体征':{$exists:true,$ne:''},'症状&体征时间':{$exists:true,$regex:/^.{10,}$/}}}";
        aggregates.add(new Document("$match",Document.parse(strZZCondition)));
        aggregates.add(new Document("$sort",Document.parse("{'症状&体征时间':1}")));
        aggregates.add(new Document("$group",Document.parse("{'_id':{'PID':'$PID','部位1':'$部位1','体征':'$体征','体征定性描述':'$体征定性描述','体征定量描述':'$体征定量描述','体征定量单位':'$体征定量单位'}, 'result':{'$first':'$$ROOT'}}")));
        MongoCursor<Document> cursor =mc.aggregate(aggregates).allowDiskUse(true).iterator();
        while (cursor.hasNext())
        {
            Document  mcursor =cursor.next();
            Document groupInfo=(Document)mcursor.get("_id");

            Document dd= (Document)mcursor.get("result");

            Document obj = new Document();
            obj.put("症状&体征时间",dd.getString("症状&体征时间"));
            obj.put("RID",dd.getString("RID"));
            obj.put("否定词",dd.getString("否定词"));
            mapFirstTZ.put(groupInfo.getString("PID")+groupInfo.getString("部位1")+groupInfo.getString("体征")+groupInfo.getString("体征定性描述")
                    +groupInfo.getString("体征定量描述")+groupInfo.getString("体征定量单位"),obj);
        }
    }

    public  void getFirstZZDay(MongoDatabase dbHDP)
    {
        System.out.println("getFirstZZDay");
        MongoCollection<Document> mc = dbHDP.getCollection("ASY");
        ArrayList<Document> aggregates = new ArrayList<Document>();
        String strZZCondition="{"+BaseInfo_Title_ListValue_DBCondition.ZZTZ13SLE+",'否定词':'','症状1':{$exists:true,$ne:''},'症状&体征时间':{$exists:true,$regex:/^.{10,}$/}}}";
        aggregates.add(new Document("$match",Document.parse(strZZCondition)));
        aggregates.add(new Document("$sort",Document.parse("{'症状&体征时间':1}")));
        aggregates.add(new Document("$group",Document.parse("{'_id':{'PID':'$PID','部位1':'$部位1','症状1':'$症状1'}, 'result':{'$first':'$$ROOT'}}")));
        MongoCursor<Document> cursor =mc.aggregate(aggregates).allowDiskUse(true).iterator();
        while (cursor.hasNext())
        {
            Document  mcursor =cursor.next();
            Document groupInfo=(Document)mcursor.get("_id");

            Document dd= (Document)mcursor.get("result");
            Document obj = new Document();
            obj.put("症状&体征时间",dd.getString("症状&体征时间"));
            obj.put("RID",dd.getString("RID"));
            obj.put("否定词",dd.getString("否定词"));
            mapFirstZZ.put(groupInfo.getString("PID")+groupInfo.getString("部位1")+groupInfo.getString("症状1"),obj);
        }
    }
    public  void getFirstADIDay(MongoDatabase dbHDP)
    {
        System.out.println("getFirstADIDay");
        MongoCollection<Document> mc = dbHDP.getCollection("ADI");
        ArrayList<Document> aggregates = new ArrayList<Document>();
        String strZDCondition="{'诊断时间':{$exists:true,$regex:/^.{9,}$/},'诊断状态':'是','标准诊断名':{$ne:'',$exists:true},"+BaseInfo_Title_ListValue_DBCondition.ZD13SLE+"}";
        aggregates.add(new Document("$match",Document.parse(strZDCondition)));
        aggregates.add(new Document("$sort",Document.parse("{'诊断时间':1}")));
        aggregates.add(new Document("$group",Document.parse("{'_id':{'PID':'$PID','标准诊断名':'$标准诊断名'}, 'result':{'$first':'$$ROOT'}}")));
        MongoCursor<Document> cursor =mc.aggregate(aggregates).allowDiskUse(true).iterator();
        while (cursor.hasNext())
        {
            Document  mcursor =cursor.next();
            Document groupInfo=(Document)mcursor.get("_id");

            Document dd= (Document)mcursor.get("result");
            Document obj = new Document();
            obj.put("诊断时间",dd.getString("诊断时间"));
            obj.put("RID",dd.getString("RID"));
            obj.put("诊断状态",dd.getString("诊断状态"));
            obj.put("标准诊断名_原",dd.getString("标准诊断名_原"));
            mapFirstZD.put(groupInfo.getString("PID")+groupInfo.getString("标准诊断名"),obj);
        }
    }

    public  void getSubAndItemMap() throws Exception
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
            if(!tempFenZu.toUpperCase().equals("N"))
                if(!tempFenZu.equals("")&&mapLeiJiSubFenZu.containsKey(tempFenZu)) {
                    ArrayList arrayList=mapLeiJiSubFenZu.get(tempFenZu);
                    arrayList.add(tempZuHe);
                }
                else if(!tempFenZu.equals(""))
                {
                    ArrayList arrayList=new ArrayList();
                    arrayList.add(tempZuHe);
                    mapLeiJiSubFenZu.put(tempFenZu,arrayList);
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
    public void fillSlePingFenExcelTitle(SXSSFSheet sheet ,String title,Map<String,ArrayList> mapOneTwoMapping,Map<String,Integer> mapOneMark,boolean valueFlag)
    {
        String[] titles = title.split(",");
        int length=titles.length;
        Row row = sheet.createRow(0);
        for (int i = 0; i <titles.length ; i++) {
            row.createCell(i).setCellValue(titles[i]);
        }
        for(Map.Entry<String,ArrayList> map:mapOneTwoMapping.entrySet())
        {
            row.createCell(length++).setCellValue("二级_"+map.getKey());
        }
        for(Map.Entry<String,Integer> map:mapOneMark.entrySet())
        {
            row.createCell(length++).setCellValue(map.getKey());
        }
        if(valueFlag)
        {
            for(Map.Entry<String,Integer> map:mapOneMark.entrySet())
            {
                row.createCell(length++).setCellValue(map.getKey()+"实体");
                row.createCell(length++).setCellValue(map.getKey()+"状态");
                row.createCell(length++).setCellValue(map.getKey()+"时间天");
                row.createCell(length++).setCellValue(map.getKey()+"名称");
                row.createCell(length++).setCellValue(map.getKey()+"rid");
            }
        }
    }

    public  void fillExcelTitle(SXSSFSheet sheet ,String title,boolean valueFlag)
    {
        String[] titles = title.split(",");
        int length=titles.length;
        Row row = sheet.createRow(0);
        for (int i = 0; i <titles.length ; i++) {
            row.createCell(i).setCellValue(titles[i]);
        }

        for(Map.Entry<String,ArrayList> map:mapLeiJiFenZu.entrySet())
        {
            row.createCell(length++).setCellValue("系统_"+map.getKey());
        }
        for(Map.Entry<String,ArrayList> map:mapLeiJiSubFenZu.entrySet())
        {
            row.createCell(length++).setCellValue(map.getKey());
        }
      if(valueFlag) {
          for (Map.Entry<String, ArrayList> map : mapLeiJiFenZu.entrySet()) {
              row.createCell(length++).setCellValue("系统_" + map.getKey() + "实体");
              row.createCell(length++).setCellValue("系统_" + map.getKey() + "状态");
              row.createCell(length++).setCellValue("系统_" + map.getKey() + "时间天");
              row.createCell(length++).setCellValue("系统_" + map.getKey() + "名称");
              row.createCell(length++).setCellValue("系统_" + map.getKey() + "RID");
          }
          for (Map.Entry<String, ArrayList> map : mapLeiJiSubFenZu.entrySet()) {
              row.createCell(length++).setCellValue(map.getKey() + "实体");
              row.createCell(length++).setCellValue(map.getKey() + "状态");
              row.createCell(length++).setCellValue(map.getKey() + "时间天");
              row.createCell(length++).setCellValue(map.getKey() + "名称");
              row.createCell(length++).setCellValue(map.getKey() + "RID");
          }
      }
    }

    public  void fillExcelTitle(SXSSFSheet sheet ,String title,Map<String,String> mapExcludeColumn)
    {
        String[] titles = title.split(",");
        int length=titles.length;
        Row row = sheet.createRow(0);
        for (int i = 0; i <titles.length ; i++) {
            row.createCell(i).setCellValue(titles[i]);
        }

        for(Map.Entry<String,ArrayList> map:mapLeiJiFenZu.entrySet())
        {
            if(mapExcludeColumn.containsKey("系统_"+map.getKey()))
                continue;
            row.createCell(length++).setCellValue("系统_"+map.getKey());
        }
        for(Map.Entry<String,ArrayList> map:mapLeiJiSubFenZu.entrySet())
        {
            if(mapExcludeColumn.containsKey(map.getKey()))
                continue;
            row.createCell(length++).setCellValue(map.getKey());
        }
    }
    public   void fillExceptPID(Map<String,String> mapExceptPID ,Boolean includeChJian) throws Exception
    {
        String fileYC1= LocalHostInfo.getPath()+"交付/移除组PID列表.xlsx";
   //     String fileYC2= LocalHostInfo.getPath()+"移除组PID列表-抽检.xlsx";
        JSONObject config = new JSONObject();
        config.put("filename", fileYC1);
        config.put("source_type", "excel");

        DSExcelReader2 excelReader = new DSExcelReader2(config);
        while((document=excelReader.nextDocument()) != null) {
            mapExceptPID.put(document.getString("PID"),"0");
        }
//        if(includeChJian) {
//            config.put("filename", fileYC2);
//            excelReader = new DSExcelReader2(config);
//            while ((document = excelReader.nextDocument()) != null) {
//                mapExceptPID.put(document.getString("PID"), "0");
//            }
//        }
    }

      public   void getCacliteExcel(String deleteFileName,String srouceFileName,String title,String savedFile,String strXunLianFlag) throws Exception  //获取删除指定列的表
      {
        System.out.println("getCacliteExcel");
        Map<String,String> mapDeleteCloum = new HashMap<String, String>();
        ReadFromExcelToMap.readFromExcelToMap(mapDeleteCloum, LocalHostInfo.getPath()+deleteFileName,"删除列");
        String[] titles = title.split(",");


        SaveExcelTool saveExcelTool = new SaveExcelTool();
        SXSSFSheet sheet = saveExcelTool.getSheet("");
        fillExcelTitle(sheet,title,mapDeleteCloum);

        JSONObject config = new JSONObject();
        config.put("filename", LocalHostInfo.getPath()+srouceFileName);
        config.put("source_type", "excel");
        DSExcelReader2 excelReader = new DSExcelReader2(config);

        int RowNum=1;
        while ((document = excelReader.nextDocument()) != null) {
            int length=titles.length;

            if(strXunLianFlag.equals("one"))
            {
                if(!document.getString("医院").equals("武汉同济医院"))
                    continue;
            }else if(strXunLianFlag.equals("muti"))
            {
                if(document.getString("医院").equals("武汉同济医院"))
                    continue;
            }
            Row row = sheet.createRow(RowNum++);
            for (int i = 0; i < titles.length; i++) {
                if(document.getString(titles[i]) !=null)
                    row.createCell(i).setCellValue(getNumberValue(document.getString(titles[i])));
            }
            for(Map.Entry<String,ArrayList> map:mapLeiJiFenZu.entrySet())
            {
                if(mapDeleteCloum.containsKey("系统_"+map.getKey()))
                    continue;
                String value=getNumberValue(ReadFromExcelToMap.getJSonValue(document,"系统_"+map.getKey()));
                if(value.equals("0")||value.equals("1"))
                  row.createCell(length++).setCellValue(Integer.valueOf(value));
                else
                  row.createCell(length++).setCellValue(value);
            }
            for(Map.Entry<String,ArrayList> map:mapLeiJiSubFenZu.entrySet())
            {
                if(mapDeleteCloum.containsKey(map.getKey()))
                    continue;
                String value=getNumberValue(ReadFromExcelToMap.getJSonValue(document,map.getKey()));
                if(value.equals("0")||value.equals("1"))
                    row.createCell(length++).setCellValue(Integer.valueOf(value));
                else
                    row.createCell(length++).setCellValue(value);
            }

        }
        saveExcelTool.saveExcel(savedFile);
        System.out.println("OK");
    }

    public  String getNumberValue(String value)
    {
        if(value.indexOf(".") >-1)
        {
            return value.substring(0,value.indexOf("."));
        }
        return value;
    }
    public  String getAgeGroup(Integer age)
    {
        if(age==null ||age==-1)
            return "异常";
        if(age.intValue()>=0 &&age.intValue()<=18)
            return "青少年";
        else if(age.intValue()>=19 &&age.intValue()<=49)
            return "成人";
        else if(age.intValue()>=50 &&age.intValue()<=100)
            return "晚发";
        else
        {
            return "异常";
        }

    }
}
