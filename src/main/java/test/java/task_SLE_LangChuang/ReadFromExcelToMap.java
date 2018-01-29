package test.java.task_SLE_LangChuang;

import com.RuiShiKeYan.Common.Method.LocalHostInfo;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.yiyihealth.data.DaX.reader.DSExcelReader2;
import org.apache.poi.ss.usermodel.Row;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created with IntelliJ IDEA
 * User:huangming
 * Date:2017/10/23
 * Time:下午5:21
 */
public class ReadFromExcelToMap {

    static JSONObject document;
    public static  void readFromExcelToMap(Map<String,String> mapResult,String fileName,String KeyName) throws Exception
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
    public static void getPidInfo(Map<String,String> mapExcludePID,Map<String,JSONObject> mapPid) {
        try {
            JSONObject config = new JSONObject();
            config.put("filename", LocalHostInfo.getPath() + "交付/首诊时间表.xlsx");
            config.put("source_type", "excel");

            DSExcelReader2 excelReader = new DSExcelReader2(config);
            while ((document = excelReader.nextDocument()) != null) {
                if (mapExcludePID.get(document.getString("患者（PID）")) == null) {
                    JSONObject obj = new JSONObject();
                    obj.put("诊断时间天",document.getString("诊断时间天"));
                    obj.put("出生年",document.getString("出生年"));
                    obj.put("SLE年龄",document.getString("诊断时间年减去出生年"));
                    obj.put("医院",document.getString("医院"));
                    obj.put("性别",document.getString("性别"));
                    obj.put("地域",document.getString("地域"));
                    mapPid.put(document.getString("患者（PID）"), obj);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getJSonValue(JSONObject jsonObject,String key)
    {
        if(jsonObject == null || key == null)return "";
        if(jsonObject.getString(key) ==null)
            return "";
        return jsonObject.getString(key);
    }

    public static int SaveEntityDetail(int columNum, Map.Entry<String,JSONObject> map, Row row)
    {
        if(map.getValue() ==null) {
            row.createCell(columNum++).setCellValue("");
            row.createCell(columNum++).setCellValue("");
            row.createCell(columNum++).setCellValue("");
            row.createCell(columNum++).setCellValue("");
            row.createCell(columNum++).setCellValue("");
        }else
        {
            row.createCell(columNum++).setCellValue(map.getValue().getString("实体"));
            row.createCell(columNum++).setCellValue(map.getValue().getString("状态"));
            row.createCell(columNum++).setCellValue(map.getValue().getString("时间天"));
            row.createCell(columNum++).setCellValue(map.getValue().getString("名称"));
            row.createCell(columNum++).setCellValue(map.getValue().getString("RID"));
        }
        return columNum;
    }
    public static String getFirstLastRIDDay(MongoDatabase db, String strCondition, boolean flag)
    {
        MongoCollection<Document> mc = db.getCollection("ARB");
        ArrayList<Document> aggregates = new ArrayList<Document>();
        String result="";
        aggregates.add(new Document("$match",Document.parse("{"+strCondition+"}")));
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
        if(result.length() >10)
            return result.substring(0,10);
        return result;
    }
}
