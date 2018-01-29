package test.java.doResearch;

import com.RuiShiKeYan.Common.Method.MongoDBHelper;
import com.RuiShiKeYan.Common.Method.SaveExcelTool;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.bson.Document;
import org.json.JSONObject;
import test.java.task_SLE_LangChuang.BaseInfo_Title_ListValue_DBCondition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA
 * User:huangming
 * Date:2017/12/15
 * Time:上午10:42
 */
public class getDictStructVersion {

    static Map<String ,JSONObject> mapInfoTable= new HashMap<String, JSONObject>();
    static Map<String,String> mapHospitalIdName = new HashMap<String, String>();
     public static void main(String[] args) throws Exception
     {
         initParam();
         MongoDBHelper mongoDBHelper = new MongoDBHelper("HDP-live");
         MongoDatabase db=mongoDBHelper.getDb();

         getHospital(db,mapHospitalIdName);
         writeToExcel(db);
     }

     private static void writeToExcel(MongoDatabase db)
     {
         Map<String,Document> mapInfo;
         SaveExcelTool saveExcelTool= new SaveExcelTool();
         for(Map.Entry<String,JSONObject> map:mapInfoTable.entrySet())
         {
             SXSSFSheet sheet= saveExcelTool.getSheet(map.getKey());
             saveExcelTool.fillExcelTitle("医院,PPID,词典版本,分词版本,结构化版本");
             mapInfo=new HashMap<String, Document>();
             getDBInfo(db,map.getValue(),mapInfo);
             saveToExcel(sheet,mapInfo);
         }
         saveExcelTool.saveExcel("交付/词典结构化分词版本.xlsx");
     }
     private static void saveToExcel(SXSSFSheet sheet,Map<String,Document> mapInfo)
     {
           int rowNum=1;
           for (Map.Entry<String ,Document> map:mapInfo.entrySet())
           {
               int cellNum=0;
               Document dd = map.getValue();
               Row row= sheet.createRow(rowNum++);
               row.createCell(cellNum++).setCellValue(mapHospitalIdName.get(getJSonValue(dd,"hospitalId")));
               row.createCell(cellNum++).setCellValue(dd.getInteger("projectProcessId"));
               String[] values=getJSonValue(dd,"SDS_Version").split("_");
               row.createCell(cellNum++).setCellValue(values[1]);
               row.createCell(cellNum++).setCellValue(values[2]);
               row.createCell(cellNum++).setCellValue(values[3]);
           }
     }
    public static String getJSonValue(com.alibaba.fastjson.JSONObject jsonObject, String key)
    {
        if(jsonObject == null || key == null)return "";
        if(jsonObject.getString(key) ==null)
            return "";
        return jsonObject.getString(key);
    }
    public static String getJSonValue(Document jsonObject, String key)
    {
        if(jsonObject == null || key == null)return "";
        if(jsonObject.getString(key) ==null)
            return "";
        return jsonObject.getString(key);
    }
     private static void getDBInfo(MongoDatabase db,JSONObject jsonObject,Map<String,Document> mapInfo)
     {
         MongoCollection<Document> mc= db.getCollection(jsonObject.getString("table"));
         ArrayList<Document> aggregates = new ArrayList<Document>();
         aggregates.add(new Document("$match",Document.parse("{"+jsonObject.getString("condition")+"}")));
         aggregates.add(new Document("$group",Document.parse("{'_id':{'projectProcessId':'$projectProcessId','SDS_Version':'$SDS_Version','hospitalId':'$hospitalId'}}")));
         aggregates.add(new Document("$project",Document.parse("{'projectProcessId':'$_id.projectProcessId','SDS_Version':'$_id.SDS_Version','hospitalId':'$_id.hospitalId'}")));
         MongoCursor<Document> cursor =mc.aggregate(aggregates).allowDiskUse(true).iterator();
         Integer i=12;
         while (cursor.hasNext())
         {
             Document dd= cursor.next();
             mapInfo.put((i++).toString(),dd);
         }
     }
    public static void getHospital(MongoDatabase db, Map<String,String> mapHospitalIdName) {
        MongoCollection<Document> mc = db.getCollection("Hospital");
        MongoCursor<Document> cursor = mc.find(Document.parse("{'_id':" + BaseInfo_Title_ListValue_DBCondition.YiYuan13 + "}")).iterator();
        while (cursor.hasNext()) {
            Document dd = cursor.next();
            mapHospitalIdName.put(dd.getString("_id"), dd.getString("name"));
        }
    }

    private static void initParam()
    {
        JSONObject obj=new JSONObject();
        obj.put("table","ADO");
        obj.put("condition",BaseInfo_Title_ListValue_DBCondition.ADO13);
        mapInfoTable.put("基本信息",obj);

        obj=new JSONObject();
        obj.put("table","ADR");
        obj.put("condition",BaseInfo_Title_ListValue_DBCondition.ADR13);
        mapInfoTable.put("用药",obj);

        obj=new JSONObject();
        obj.put("table","ASY");
        obj.put("condition",BaseInfo_Title_ListValue_DBCondition.ZZTZ13SLE);
        mapInfoTable.put("体征症状",obj);

        obj=new JSONObject();
        obj.put("table","ALA");
        obj.put("condition",BaseInfo_Title_ListValue_DBCondition.HY13SLE);
        mapInfoTable.put("化验",obj);

        obj=new JSONObject();
        obj.put("table","ADI");
        obj.put("condition",BaseInfo_Title_ListValue_DBCondition.ZD13SLE);
        mapInfoTable.put("诊断",obj);
    }

}
