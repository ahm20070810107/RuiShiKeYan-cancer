package test.java.task_SLE_QueZzhengLeiJi;

import com.RuiShiKeYan.Common.Method.LocalHostInfo;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.yiyihealth.data.DaX.reader.DSExcelReader2;
import org.bson.Document;
import test.java.task_SLE_LangChuang.BaseInfo_Title_ListValue_DBCondition;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created with IntelliJ IDEA
 * User:huangming
 * Date:2017/12/5
 * Time:下午7:55
 */
public class LeijiPublicMethod {
    static JSONObject document = null;

    public static void getPIDInfo(Map<String,JSONObject> mapPID,Map<String,String> mapExceptPID )throws Exception
    {
        String fileName= LocalHostInfo.getPath()+"交付/首诊时间表.xlsx";
        fillExceptPID(mapExceptPID);
        JSONObject config = new JSONObject();
        config.put("filename", fileName);
        config.put("source_type", "excel");

        DSExcelReader2 excelReader = new DSExcelReader2(config);
        while ((document = excelReader.nextDocument()) != null) {
            if (!document.get("患者（PID）").toString().equals(""))
                if(!mapExceptPID.containsKey(document.getString("患者（PID）")))
                    mapPID.put(document.getString("患者（PID）"), document);
        }
    }

    private static  void fillExceptPID(Map<String,String> mapExceptPID ) throws Exception
    {
        String fileYC1= LocalHostInfo.getPath()+"交付/移除组PID列表.xlsx";
        //   String fileYC2= LocalHostInfo.getPath()+"移除组PID列表-抽检.xlsx";
        JSONObject config = new JSONObject();
        config.put("filename", fileYC1);
        config.put("source_type", "excel");

        DSExcelReader2 excelReader = new DSExcelReader2(config);
        while((document=excelReader.nextDocument()) != null) {
            mapExceptPID.put(document.getString("PID"),document.getString("移出步骤"));
        }
//        config.put("filename", fileYC2);
//        excelReader = new DSExcelReader2(config);
//        while((document=excelReader.nextDocument()) != null) {
//            mapExceptPID.put(document.getString("PID"),"0");
//        }
    }

    public static void getQZSJTable(Map<String,JSONObject> mapQZBXTable) throws Exception
    {
        String fileNameQZBX= LocalHostInfo.getPath()+"交付/确诊表现表.xlsx";
        JSONObject config = new JSONObject();
        config.put("filename", fileNameQZBX);
        config.put("source_type", "excel");
        DSExcelReader2 excelReader = new DSExcelReader2(config);
        while ((document = excelReader.nextDocument()) != null) {
            mapQZBXTable.put(document.getString("患者（PID）"),document);
        }
    }

    public static void getHospitalInfo(Map<String,String> mapHospital) throws Exception
    {
        String fileName= LocalHostInfo.getPath()+"医院所属省.xlsx";
        JSONObject config = new JSONObject();
        config.put("filename", fileName);
        config.put("source_type", "excel");

        DSExcelReader2 excelReader = new DSExcelReader2(config);
        while ((document = excelReader.nextDocument()) != null) {
            if (!document.get("医院").equals(""))
                mapHospital.put(document.getString("医院"), document.getString("医院所属省"));
        }

    }


    public static String getAgeGroup(String age)
    {
        Integer ageNum;
        if(age==null||age.equals(""))
            return "异常";
        try
        {
            if(age.indexOf(".") >0)
                age=age.substring(0,age.indexOf("."));
            if(age.equals(""))
                return "异常";
            ageNum=Integer.valueOf(age);
        }catch (Exception e)
        {
            return "异常";
        }
        if(ageNum.intValue()>=0 &&ageNum.intValue()<=18)
            return "青少年";
        else if(ageNum.intValue()>=19 &&ageNum.intValue()<=49)
            return "成人";
        else if(ageNum.intValue()>=50 &&ageNum.intValue()<=100)
            return "晚发";
        else
        {
            return "异常";
        }
    }

    public static void getFirstHYDay(MongoDatabase dbHDP,Map<String,Document> mapHY)
    {
        System.out.println("getFirstHYDay");
        MongoCollection<Document> mc = dbHDP.getCollection("ALA");
        ArrayList<Document> aggregates = new ArrayList<Document>();
        String strHYConditon="{'化验结果定性（新）':'阳性',"+ BaseInfo_Title_ListValue_DBCondition.HY13SLE+",'化验时间':{$exists:true,$regex:/^.{10,}$/}}";
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
            obj.put("RID",dd.getString("RID"));
            obj.put("化验时间",dd.getString("化验时间"));
            obj.put("化验名称_原",dd.getString("化验名称_原"));
            obj.put("上下文",dd.getString("上下文"));
            obj.put("段落标题",dd.getString("段落标题"));
            mapHY.put(groupInfo.getString("PID")+groupInfo.getString("标准化验名")+groupInfo.getString("标准标本"),obj);
        }
    }
    public static void getFirstHYRPGDay(MongoDatabase dbHDP,Map<String,Document> mapHYRPG)
    {
        System.out.println("getFirstHYRPGDay");
        MongoCollection<Document> mc = dbHDP.getCollection("ALA");
        ArrayList<Document> aggregates = new ArrayList<Document>();
        String strHYConditon="{'RPG科研结果转换':'阳性',"+ BaseInfo_Title_ListValue_DBCondition.HY13SLE+",'化验时间':{$exists:true,$regex:/^.{10,}$/}}";
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
            obj.put("RID",dd.getString("RID"));
            obj.put("化验时间",dd.getString("化验时间"));
            obj.put("化验名称_原",dd.getString("化验名称_原"));
            obj.put("上下文",dd.getString("上下文"));
            obj.put("段落标题",dd.getString("段落标题"));
            mapHYRPG.put(groupInfo.getString("PID")+groupInfo.getString("标准化验名")+groupInfo.getString("标准标本"),obj);
        }
    }

    public static void getFirstTZDay(MongoDatabase dbHDP,Map<String,Document> mapTZ)
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
            Document dd= (Document)mcursor.get("result");
            Document obj = new Document();
            obj.put("症状&体征时间",dd.getString("症状&体征时间"));
            obj.put("RID",dd.getString("RID"));
            obj.put("上下文",dd.getString("上下文"));
            obj.put("段落标题",dd.getString("段落标题"));
            mapTZ.put(dd.getString("PID")+dd.getString("部位1")+dd.getString("体征")+dd.getString("体征定性描述")+dd.getString("体征定量描述")+dd.getString("体征定量单位"),obj);
        }
    }

    public static void getFirstZZDay(MongoDatabase dbHDP,Map<String,Document> mapZZ)
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
            Document dd= (Document)mcursor.get("result");
            Document obj = new Document();
            obj.put("症状&体征时间",dd.getString("症状&体征时间"));
            obj.put("RID",dd.getString("RID"));
            obj.put("上下文",dd.getString("上下文"));
            obj.put("段落标题",dd.getString("段落标题"));
            mapZZ.put(dd.getString("PID")+dd.getString("部位1")+dd.getString("症状1"),obj);
        }
    }
    public static void getFirstADIDay(MongoDatabase dbHDP,Map<String,Document> mapZD)
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
            obj.put("RID",dd.getString("RID"));
            obj.put("上下文",dd.getString("上下文"));
            obj.put("段落标题",dd.getString("段落标题"));
            obj.put("诊断时间",dd.getString("诊断时间"));
            obj.put("标准诊断名_原",dd.getString("标准诊断名_原"));
            mapZD.put(groupInfo.getString("PID")+groupInfo.getString("标准诊断名"),obj);
        }
    }


    public static String getLastRIDDay(MongoDatabase dbHDP,String PID)
    {
        //    System.out.println("getLastRIDDay");
        MongoCollection<Document> mc = dbHDP.getCollection("ARB");
        ArrayList<Document> aggregates = new ArrayList<Document>();
        String result="";
        String strARBCondition="{'PID': '"+PID+"','记录时间戳':{$exists:true,$regex:/^.{10,}$/}"+BaseInfo_Title_ListValue_DBCondition.ADO13+"}";
        aggregates.add(new Document("$match",Document.parse(strARBCondition)));
        aggregates.add(new Document("$sort",Document.parse("{'记录时间戳':-1}")));
        aggregates.add(new Document("$group",Document.parse("{'_id':{'PID':'$PID'}, '记录时间戳':{'$first':'$$ROOT'}}")));
        MongoCursor<Document> cursor =mc.aggregate(aggregates).allowDiskUse(true).iterator();
        while (cursor.hasNext())
        {
            Document dd= (Document)cursor.next().get("记录时间戳");
            result=dd.getString("记录时间戳");
        }
        return result;
    }

    public static String getJSonValue(JSONObject jsonObject,String key)
    {
        if(jsonObject == null || key == null)return "";
        if(jsonObject.getString(key) ==null)
            return "";
        return jsonObject.getString(key);
    }
}
