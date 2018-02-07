package com.RuiShiKeYan.SubMethod;

import com.alibaba.fastjson.JSONObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import test.java.task_SLE_LangChuang.BaseInfo_Title_ListValue_DBCondition;

import javax.print.Doc;
import java.util.ArrayList;
import java.util.Map;

public class getHDPInfo {

    /**
     *
     * @param dbHDP
     * HDP库
     * @param PID
     *
     * @param flag
     *  ---fasle返回最早时间，true返回最晚时间
     * @return 10位时间格式
     */

    public static  String getFirstLastRIDDay(MongoDatabase dbHDP, String PID, boolean flag)
    {
        String  sortFlag=flag?"-1":"1";
        MongoCollection<Document> mc = dbHDP.getCollection("ARB");
        ArrayList<Document> aggregates = new ArrayList<Document>();
        String result;
        String strARBCondition="{'PID': '"+PID+"','记录时间戳':{$exists:true,$regex:/^.{10,}$/}"+ BaseInfo_Title_ListValue_DBCondition.ADO13+"}";
        aggregates.add(new Document("$match",Document.parse(strARBCondition)));
        aggregates.add(new Document("$group",Document.parse("{'_id':{'记录时间戳':'$记录时间戳'}}")));
        aggregates.add(new Document("$sort",Document.parse("{'_id.记录时间戳':"+sortFlag+"}")));
        aggregates.add(new Document("$limit",1));
        MongoCursor<Document> cursor =mc.aggregate(aggregates).allowDiskUse(true).iterator();
        Document dd= (Document)cursor.next().get("_id");
        result=dd.getString("记录时间戳");
        if(result.length()>10)
            return result.substring(0,10);
        return result;
    }

    /**
     *
     * @param dbHDP
     * @param mapYY
     * @param strConditon
     *
     */
    public static void getYYDay(MongoDatabase dbHDP,Map<String,Document> mapYY,String strConditon)
    {
        System.out.println("getYYDay");
        MongoCollection<Document> mc = dbHDP.getCollection("ADR");
        ArrayList<Document> aggregates = new ArrayList<Document>();
        String strZDCondition="{"+BaseInfo_Title_ListValue_DBCondition.ADR13+strConditon+",'用药时间':{$exists:true,$regex:/^.{10,}$/},'是否使用':'使用','通用名':{$ne:'',$exists:true}}";
        aggregates.add(new Document("$match",Document.parse(strZDCondition)));
        aggregates.add(new Document("$sort",Document.parse("{'用药时间':1}")));
        aggregates.add(new Document("$group",Document.parse("{'_id':{'PID':'$PID','通用名':'$通用名'}, 'result':{'$first':'$$ROOT'},'lastTime':{'$last':'$用药时间'}}")));
        MongoCursor<Document> cursor =mc.aggregate(aggregates).allowDiskUse(true).iterator();
        while (cursor.hasNext())
        {
            Document  mcursor =cursor.next();
            Document groupInfo=(Document)mcursor.get("_id");

            Document dd= (Document)mcursor.get("result");
            Document obj = new Document();
            obj.put("firstTime",dd.getString("用药时间"));
            obj.put("lastTime",mcursor.getString("lastTime"));
            obj.put("RID",dd.getString("RID"));
            obj.put("段落标题",dd.getString("段落标题"));
//            obj.put("通用名_原",dd.getString("通用名_原"));
//            obj.put("是否使用",dd.getString("是否使用"));
            mapYY.put(groupInfo.getString("PID")+groupInfo.getString("通用名"),obj);
        }
    }

    public static void getHYDay(MongoDatabase dbHDP, Map<String,Document> mapHY, String strConditon)
    {
        System.out.println("getHYDay");
        MongoCollection<Document> mc = dbHDP.getCollection("ALA");
        ArrayList<Document> aggregates = new ArrayList<Document>();
        String strHYConditon="{"+BaseInfo_Title_ListValue_DBCondition.HY13SLE+strConditon+",'化验时间':{$exists:true,$regex:/^.{10,}$/}}";
     //   System.out.println(strHYConditon);
        aggregates.add(new Document("$match",Document.parse(strHYConditon)));
        aggregates.add(new Document("$sort",Document.parse("{'化验时间':1}")));
        aggregates.add(new Document("$group",Document.parse("{'_id':{'PID':'$PID','标准化验名':'$标准化验名','标准标本':'$标准标本'}, 'result':{'$first':'$$ROOT'},'lastTime':{'$last':'$化验时间'}}")));
        MongoCursor<Document> cursor =mc.aggregate(aggregates).allowDiskUse(true).iterator();
        while (cursor.hasNext())
        {
            Document  mcursor =cursor.next();
            Document groupInfo=(Document)mcursor.get("_id");
            Document dd= (Document)mcursor.get("result");
            Document obj = new Document();
            obj.put("firstTime",dd.getString("化验时间"));
            obj.put("lastTime",mcursor.getString("lastTime"));
            obj.put("RID",dd.getString("RID"));
//            obj.put("化验结果定性（新）",dd.getString("化验结果定性（新）"));
//            obj.put("化验名称_原",dd.getString("化验名称"));
            mapHY.put(groupInfo.getString("PID")+groupInfo.getString("标准化验名")+groupInfo.getString("标准标本"),obj);
        }
    }

    public static void getTZDay(MongoDatabase dbHDP, Map<String,Document> mapTZ, String strConditon)
    {
        System.out.println("getTZDay");
        MongoCollection<Document> mc = dbHDP.getCollection("ASY");
        ArrayList<Document> aggregates = new ArrayList<Document>();
        String strZZCondition="{"+BaseInfo_Title_ListValue_DBCondition.ZZTZ13SLE+strConditon+",'$or':[{'体征':{$ne:''}},{'体征定性描述':{$ne:''}}],'症状&体征时间':{$exists:true,$regex:/^.{10,}$/}}}";
        aggregates.add(new Document("$match",Document.parse(strZZCondition)));
        aggregates.add(new Document("$sort",Document.parse("{'症状&体征时间':1}")));
        aggregates.add(new Document("$group",Document.parse("{'_id':{'PID':'$PID','部位1':'$部位1','否定词':'$否定词','体征':'$体征','体征定性描述':'$体征定性描述','体征定量描述':'$体征定量描述','体征定量单位':'$体征定量单位'}, 'result':{'$first':'$$ROOT'},'lastTime':{'$last':'$症状&体征时间'}}")));
        MongoCursor<Document> cursor =mc.aggregate(aggregates).allowDiskUse(true).iterator();
        while (cursor.hasNext())
        {
            Document  mcursor =cursor.next();
            Document groupInfo=(Document)mcursor.get("_id");

            Document dd= (Document)mcursor.get("result");

            Document obj = new Document();
            obj.put("firstTime",dd.getString("症状&体征时间"));
            obj.put("lastTime",mcursor.getString("lastTime"));
//            obj.put("RID",dd.getString("RID"));
//            obj.put("否定词",dd.getString("否定词"));
            mapTZ.put(groupInfo.getString("PID")+groupInfo.getString("部位1")+groupInfo.getString("否定词")+groupInfo.getString("体征")+groupInfo.getString("体征定性描述")
                    +groupInfo.getString("体征定量描述")+groupInfo.getString("体征定量单位"),obj);
        }
    }

    public static void getZZDay(MongoDatabase dbHDP, Map<String,Document> mapZZ, String strConditon)
    {
        System.out.println("getZZDay");
        MongoCollection<Document> mc = dbHDP.getCollection("ASY");
        ArrayList<Document> aggregates = new ArrayList<Document>();
        String strZZCondition="{"+BaseInfo_Title_ListValue_DBCondition.ZZTZ13SLE+strConditon+",'否定词':'','症状1':{$exists:true,$ne:''},'症状&体征时间':{$exists:true,$regex:/^.{10,}$/}}}";
        aggregates.add(new Document("$match",Document.parse(strZZCondition)));
        aggregates.add(new Document("$sort",Document.parse("{'症状&体征时间':1}")));
        aggregates.add(new Document("$group",Document.parse("{'_id':{'PID':'$PID','部位1':'$部位1','症状1':'$症状1'}, 'result':{'$first':'$$ROOT'},'lastTime':{'$last':'$症状&体征时间'}}")));
        MongoCursor<Document> cursor =mc.aggregate(aggregates).allowDiskUse(true).iterator();
        while (cursor.hasNext())
        {
            Document  mcursor =cursor.next();
            Document groupInfo=(Document)mcursor.get("_id");

            Document dd= (Document)mcursor.get("result");
            Document obj = new Document();
            obj.put("firstTime",dd.getString("症状&体征时间"));
            obj.put("lastTime",mcursor.getString("lastTime"));
//            obj.put("RID",dd.getString("RID"));
//            obj.put("否定词",dd.getString("否定词"));
            mapZZ.put(groupInfo.getString("PID")+groupInfo.getString("部位1")+groupInfo.getString("症状1"),obj);
        }
    }
    public static void getZDDay(MongoDatabase dbHDP, Map<String,Document> mapZD, String strConditon)
    {
        System.out.println("getZDDay");
        MongoCollection<Document> mc = dbHDP.getCollection("ADI");
        ArrayList<Document> aggregates = new ArrayList<Document>();
        String strZDCondition="{"+BaseInfo_Title_ListValue_DBCondition.ZD13SLE+strConditon+"'诊断时间':{$exists:true,$regex:/^.{9,}$/},'诊断状态':'是','标准诊断名':{$ne:'',$exists:true}}";
        aggregates.add(new Document("$match",Document.parse(strZDCondition)));
        aggregates.add(new Document("$sort",Document.parse("{'诊断时间':1}")));
        aggregates.add(new Document("$group",Document.parse("{'_id':{'PID':'$PID','标准诊断名':'$标准诊断名'}, 'result':{'$first':'$$ROOT'},'lastTime':{'$last':'$诊断时间'}}")));
        MongoCursor<Document> cursor =mc.aggregate(aggregates).allowDiskUse(true).iterator();
        while (cursor.hasNext())
        {
            Document  mcursor =cursor.next();
            Document groupInfo=(Document)mcursor.get("_id");

            Document dd= (Document)mcursor.get("result");
            Document obj = new Document();
            obj.put("firstTime",dd.getString("诊断时间"));
            obj.put("lastTime",mcursor.getString("lastTime"));
            obj.put("RID",dd.getString("RID"));
//            obj.put("诊断状态",dd.getString("诊断状态"));
//            obj.put("标准诊断名_原",dd.getString("标准诊断名_原"));
            mapZD.put(groupInfo.getString("PID")+groupInfo.getString("标准诊断名"),obj);
        }
    }
    public static   void  getLCShengyanZDInfo(MongoDatabase mdb,String Condition, Map<String,Document> mapResult)
    {
        System.out.println("getLCShengyanZDInfo");
        MongoCollection<Document> mc = mdb.getCollection("ADI");
        ArrayList<Document> aggregates = new ArrayList<Document>();
        aggregates.add(new Document("$match",Document.parse(BaseInfo_Title_ListValue_DBCondition.strZDLCShengyanCondition+Condition+"}")));
        aggregates.add(new Document("$sort",Document.parse("{'诊断时间':1}")));
        aggregates.add(new Document("$group",Document.parse("{'_id':{'PID':'$PID'}, 'result':{'$first':'$$ROOT'}}")));
        MongoCursor<Document> cursor =mc.aggregate(aggregates).allowDiskUse(true).iterator();
        Document dd=null;
        while (cursor.hasNext())
        {
            dd=(Document) cursor.next().get("result");
            JSONObject obj= new JSONObject();
            obj.put("标准诊断名",dd.getString("标准诊断名"));
            obj.put("RID",dd.getString("RID"));
            obj.put("诊断时间",dd.getString("诊断时间"));
            obj.put("诊断状态",dd.getString("诊断状态"));
            obj.put("标准诊断名_原",dd.getString("标准诊断名_原"));
            mapResult.put(dd.getString("PID"),dd);
        }
    }
    public static   void getLCShengyanHYInfo(MongoDatabase mdb,Map<String,Document> mapLCShengyanHYInfo,String ShenYanList,String strHyJieType)
    {
        if(ShenYanList.equals(""))
            return;
        System.out.println("getLCShengyanHYInfo");
        MongoCollection<Document> mci = mdb.getCollection("ALA");
        ArrayList<Document> aggregates = new ArrayList<Document>();
        aggregates.add(new Document("$match",Document.parse(BaseInfo_Title_ListValue_DBCondition.strHYCondition+strHyJieType+"}")));
        aggregates.add(new Document("$project",Document.parse("{'化验组合':{'$concat':['$标准化验名','$标准标本']},'PID':'$PID','化验时间':'$化验时间','RID':'$RID','化验名称_原':'$化验名称_原','化验结果定性（新）':'$化验结果定性（新）','RPG科研结果转换':'$RPG科研结果转换'}")));
        aggregates.add(new Document("$match",Document.parse("{'化验组合':{$in:["+ShenYanList+"]}}")));
        aggregates.add(new Document("$sort",Document.parse("{'化验时间':1}")));
        aggregates.add(new Document("$group",Document.parse("{'_id':{'PID':'$PID'}, 'result':{'$first':'$$ROOT'}}")));
        MongoCursor<Document> cursor =mci.aggregate(aggregates).allowDiskUse(true).iterator();
        while (cursor.hasNext())
        {
            Document dd=cursor.next();
            String strPid=((Document)dd.get("_id")).getString("PID");
            Document document=(Document) dd.get("result");
            mapLCShengyanHYInfo.put(strPid,document);
        }
    }


    public static   void  getZZLangCShengyan(MongoDatabase mdb,Map<String,Document>mapLCShengyanZZInfo, String strZZList)
    {
        System.out.println("getZZLangCShengyan");
        if(strZZList.equals(""))
            return;
        MongoCollection<Document> mc = mdb.getCollection("ASY");
        ArrayList<Document> aggregates = new ArrayList<Document>();
        aggregates.add(new Document("$match",Document.parse(BaseInfo_Title_ListValue_DBCondition.strZZConditon)));
        aggregates.add(new Document("$project",Document.parse("{'症状组合':{'$concat':['$部位1','$症状1']},'PID':'$PID','症状&体征时间':'$症状&体征时间','RID':'$RID','否定词':'$否定词'}")));
        aggregates.add(new Document("$match",Document.parse("{'症状组合':{$in:["+strZZList+"]}}")));
        aggregates.add(new Document("$sort",Document.parse("{'症状&体征时间':1}")));
        aggregates.add(new Document("$group",Document.parse("{'_id':{'PID':'$PID'}, 'result':{'$first':'$$ROOT'}}")));
        MongoCursor<Document> cursor =mc.aggregate(aggregates).allowDiskUse(true).iterator();
        Document dd=null;
        while (cursor.hasNext())
        {
            dd=(Document) cursor.next().get("result");
            mapLCShengyanZZInfo.put(dd.getString("PID"),dd);
        }
    }
    public static   void  getTZLangCShengyan(MongoDatabase mdb,Map<String,Document> mapLCShengyanTZInfo,String strTZList)
    {
        System.out.println("getTZLangCShengyan");
        if(strTZList.equals(""))
            return;
        MongoCollection<Document> mc = mdb.getCollection("ASY");
        ArrayList<Document> aggregates = new ArrayList<Document>();
        aggregates.add(new Document("$match",Document.parse(BaseInfo_Title_ListValue_DBCondition.strTZConditon)));
        aggregates.add(new Document("$project",Document.parse("{'体征组合':{'$concat':['$部位1','$否定词','$体征','$体征定性描述','$体征定量描述','$体征定量单位']},'PID':'$PID','症状&体征时间':'$症状&体征时间','RID':'$RID'}")));
        aggregates.add(new Document("$match",Document.parse("{'体征组合':{$in:["+strTZList+"]}}")));
        aggregates.add(new Document("$sort",Document.parse("{'症状&体征时间':1}")));
        aggregates.add(new Document("$group",Document.parse("{'_id':{'PID':'$PID'}, 'result':{'$first':'$$ROOT'}}")));
        MongoCursor<Document> cursor =mc.aggregate(aggregates).allowDiskUse(true).iterator();
        Document dd;
        while (cursor.hasNext())
        {
            dd=(Document) cursor.next().get("result");
            mapLCShengyanTZInfo.put(dd.getString("PID"),dd);
        }
    }

}
