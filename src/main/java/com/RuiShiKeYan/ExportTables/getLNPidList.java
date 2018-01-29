package com.RuiShiKeYan.ExportTables;

import com.RuiShiKeYan.Common.Interface.IruiShiKeYan;
import com.RuiShiKeYan.Common.Method.*;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
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
 * Date:2017/12/27
 * Time:下午5:14
 */
public class getLNPidList  extends RuiShiKeYan implements IruiShiKeYan{

    String strZDList="",strTZList="",strZZList="",strHYList="";
    Map<String,Document> mapLCShengyanHYInfo= new HashMap<String, Document>();
    Map<String,Document> mapLCShengyanZZInfo=new HashMap<String, Document>();
    Map<String,Document> mapLCShengyanTZInfo=new HashMap<String, Document>();
    Map<String,Document> mapLCShengyanZDInfo=new HashMap<String, Document>();
    double[] kGroup={0.5,1.0,2.0,3.0,4.0};

    public static void main(String[] args) throws Exception
    {
        MongoDBHelper mongoDBHelper = new MongoDBHelper("HDP-live");
        MongoDatabase db= mongoDBHelper.getDb();
        IruiShiKeYan  iruiShiKeYan=new getLNPidList();
        iruiShiKeYan.run(db);
        mongoDBHelper.closeMongoDb();
    }

    public  void run(MongoDatabase mdb, Object[] args) {
       try {
           String fileName = LocalHostInfo.getPath() + "交付/首诊时间表.xlsx";
           getEntityList();
           getLCShengyanHYInfo(mdb,mapLCShengyanHYInfo,strHYList,",'RPG科研结果转换':'阳性'");
           getZZLangCShengyan(mdb);
           getTZLangCShengyan(mdb);
           if(strZDList.length()>0)
               getZDSLEPerson(mdb,BaseInfo_Title_ListValue_DBCondition.strZDLCShengyanCondition+",'标准诊断名':{$in:["+strZDList+"]}}",mapLCShengyanZDInfo);
           fillresult(mdb,fileName);

       }catch (Exception e){e.printStackTrace();}

    }

    private void  fillresult(MongoDatabase mdb,String fileName)
    {
        try {
            Map<String, JSONObject> mapPidInfo = new HashMap<String, JSONObject>();
            ReadExcelToMap.readFromExcelToMap(mapPidInfo, fileName, "患者（PID）", true);
           for(int i=0;i<kGroup.length;i++) {
               double kvalue=kGroup[i];
               int count=0;
               for (Map.Entry<String, JSONObject> map : mapPidInfo.entrySet()) {
                   String strPid = map.getKey();
                   String strFirstTime = getFirstLastRIDDay(mdb, strPid, false).substring(0, 10);
                   strFirstTime = DateFormat.getNextDay(strFirstTime, (int)kvalue * 360);
                   JSONObject jsonObject = getLangCShengYanInfo(strPid);
                   if (jsonObject != null) {
                       String lnTime = jsonObject.getString("shunhaiTime");
                       if(lnTime.compareTo(strFirstTime)>=0 )
                           count++;
                   }
               }
               System.out.println("Tprediction:"+kvalue +",Count:"+count);
           }
        }catch (Exception e){e.printStackTrace();}
    }
    private  void  getZDSLEPerson(MongoDatabase mdb,String Condition, Map<String,Document> mapResult)
    {
        System.out.println("GetZDSLEPerson");
        MongoCollection<Document> mc = mdb.getCollection("ADI");
        ArrayList<Document> aggregates = new ArrayList<Document>();
        aggregates.add(new Document("$match",Document.parse(Condition)));
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
    private  void  getZZLangCShengyan(MongoDatabase mdb)
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
    private  void  getTZLangCShengyan(MongoDatabase mdb)
    {
        System.out.println("getTZLangCShengyan");
        if(strTZList.equals(""))
            return;
        MongoCollection<Document> mc = mdb.getCollection("ASY");
        ArrayList<Document> aggregates = new ArrayList<Document>();
        aggregates.add(new Document("$match",Document.parse(BaseInfo_Title_ListValue_DBCondition.strTZConditon)));
        aggregates.add(new Document("$project",Document.parse("{'体征组合':{'$concat':['$部位1','$体征','$体征定性描述','$体征定量描述','$体征定量单位']},'PID':'$PID','症状&体征时间':'$症状&体征时间','RID':'$RID','否定词':'$否定词'}")));
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
    private  JSONObject getLangCShengYanInfo(String strPID)
    {
        String strNewTime="first";
        JSONObject obj = new JSONObject();
        if(mapLCShengyanZZInfo.containsKey(strPID))
        {
            Document dd=mapLCShengyanZZInfo.get(strPID);
            if(strNewTime.compareTo(dd.getString("症状&体征时间"))>0 )
            {
                strNewTime=dd.getString("症状&体征时间");
                //     obj.put("shunhaiTime",dd.getString("症状&体征时间"));
                obj.put("RID",dd.getString("RID"));
                obj.put("实体","症状");
                obj.put("状态",dd.getString("否定词"));
                obj.put("ShunHaiName",dd.getString("症状组合"));
            }
        }
        if(mapLCShengyanTZInfo.containsKey(strPID))
        {
            Document dd=mapLCShengyanTZInfo.get(strPID);
            if(strNewTime.compareTo(dd.getString("症状&体征时间"))>0 )
            {
                strNewTime=dd.getString("症状&体征时间");
                obj.put("RID",dd.getString("RID"));
                obj.put("实体","体征");
                obj.put("状态",dd.getString("否定词"));
                obj.put("ShunHaiName",dd.getString("体征组合"));
            }
        }
        if(mapLCShengyanZDInfo.containsKey(strPID))
        {
            Document dd=mapLCShengyanZDInfo.get(strPID);
            if(strNewTime.compareTo(dd.getString("诊断时间"))>0 )
            {
                strNewTime=dd.getString("诊断时间");
                //     obj.put("shunhaiTime",dd.getString("诊断时间"));
                obj.put("RID",dd.getString("RID"));
                obj.put("实体","诊断");
                obj.put("状态",dd.getString("诊断状态"));
                obj.put("ShunHaiName",dd.getString("标准诊断名_原"));
            }
        }
        if(mapLCShengyanHYInfo.containsKey(strPID))
        {
            Document dd=mapLCShengyanHYInfo.get(strPID);
            if(strNewTime.compareTo(dd.getString("化验时间"))>0 )
            {
                strNewTime=dd.getString("化验时间");
                //      obj.put("shunhaiTime",dd.getString("化验时间"));
                obj.put("实体","化验");
                obj.put("状态",dd.getString("RPG科研结果转换"));
                obj.put("RID",dd.getString("RID"));
                obj.put("ShunHaiName",dd.getString("化验名称_原"));
            }
        }

        if(strNewTime.equals("first"))
            return null;
        obj.put("shunhaiTime", DateFormat.getDateFormatDay(strNewTime));
        return obj;
    }



    private  void getLCShengyanHYInfo(MongoDatabase mdb,Map<String,Document> mapResult,String ShenYanList,String strHyJieType)
    {
        if(ShenYanList.equals(""))
            return;
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
            mapResult.put(strPid,document);
        }
    }

    private  void getEntityList() throws Exception
    {
        JSONObject document=null;
        String fileName= LocalHostInfo.getPath()+ BaseInfo_Title_ListValue_DBCondition.strCLeiJiFenZuFileName;;
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
        if(strZDList.length()>0)
            strZDList=strZDList.substring(0,strZDList.length()-1);
        if(strHYList.length()>0)
            strHYList=strHYList.substring(0,strHYList.length()-1);
        if(strZZList.length()>0)
            strZZList=strZZList.substring(0,strZZList.length()-1);
        if(strTZList.length()>0)
            strTZList=strTZList.substring(0,strTZList.length()-1);
    }

    private  String getFirstLastRIDDay(MongoDatabase dbHDP,String PID,boolean flag)
    {
        MongoCollection<Document> mc = dbHDP.getCollection("ARB");
        ArrayList<Document> aggregates = new ArrayList<Document>();
        String result="";
        String strARBCondition="{'PID': '"+PID+"','记录时间戳':{$exists:true,$regex:/^.{10,}$/}"+BaseInfo_Title_ListValue_DBCondition.ADO13+"}";
        aggregates.add(new Document("$match",Document.parse(strARBCondition)));
        aggregates.add(new Document("$group",Document.parse("{'_id':{'记录时间戳':'$记录时间戳'}}")));
        if(flag)
            aggregates.add(new Document("$sort",Document.parse("{'_id.记录时间戳':-1}")));
        else
            aggregates.add(new Document("$sort",Document.parse("{'_id.记录时间戳':1}")));
        aggregates.add(new Document("$limit",1));
        MongoCursor<Document> cursor =mc.aggregate(aggregates).allowDiskUse(true).iterator();
        while (cursor.hasNext())
        {
            Document dd= (Document)cursor.next().get("_id");
            result=dd.getString("记录时间戳");
        }
        return result;
    }
}