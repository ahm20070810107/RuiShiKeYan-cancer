package test.java.doResearch;

import com.RuiShiKeYan.Common.Method.SaveExcelTool;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.yiyihealth.data.DaX.reader.DSExcelReader2;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.bson.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA
 * User:huangming
 * Date:2017/11/15
 * Time:上午9:24
 */
public class ExportPIDFromName {


    public static void main(String[] args)  throws Exception
    {
        String ExportTable = "ADO";
        MongoClientURI uri = new MongoClientURI("mongodb://webstats:lfweb7xff@121.196.244.147:3717/stats?authSource=stats&authMechanism=SCRAM-SHA-1");
        MongoClient client = new MongoClient(uri);
        MongoDatabase db = client.getDatabase("stats");



        getResult(db);


        client.close();
    }


    private static void getResult(MongoDatabase db)  throws Exception
    {
        Map<String,Map> mapList=new HashMap<String,Map>();
        MongoCollection<Document> mc = db.getCollection("患者姓名全表");
        ArrayList<Document> aggregates = new ArrayList<Document>();
        aggregates.add(new Document("$match",Document.parse("{}")));
        aggregates.add(new Document("$group",Document.parse("{'_id':{'hospitalId':'$hospitalId','name':'$姓名'}, 'result':{'$push':'$PID'}}")));
        MongoCursor<Document> cursor =mc.aggregate(aggregates).allowDiskUse(true).iterator();
        while (cursor.hasNext())
        {
            Document dd= cursor.next();
            Document docuId=(Document) dd.get("_id");
            ArrayList<String> docuResult=(ArrayList<String>)  dd.get("result");

            String keyValue=docuId.getString("hospitalId")+docuId.getString("name");

            Map<String,String>  arrResult= new HashMap<String, String>();

            for (int i = 0; i < docuResult.size(); i++) {

                arrResult.put(docuResult.get(i).trim(),"");
            }
            mapList.put(keyValue,arrResult);

        }
        SaveToExcel(mapList);
    }
    private static void  SaveToExcel(Map<String,Map> mapList) throws Exception
    {
        JSONObject document=null;
        int RowNum=1;
        SaveExcelTool saveExcelTool = new SaveExcelTool();
        SXSSFSheet sheet = saveExcelTool.getSheet("");
        saveExcelTool.fillExcelTitle("编号,姓名,来源,2次编号,hospitalId,PID");


        JSONObject config = new JSONObject();
        config.put("filename", "/Users/huangming/Desktop/AS患者汇总表.xlsx");
        config.put("source_type", "excel");
        DSExcelReader2 excelReader = new DSExcelReader2(config);
        while ((document = excelReader.nextDocument()) != null) {
            Row row = sheet.createRow(RowNum++);
            row.createCell(0).setCellValue(getDocument(document,"编号"));
            row.createCell(1).setCellValue(getDocument(document,"姓名"));
            row.createCell(2).setCellValue(getDocument(document,"来源"));
            row.createCell(3).setCellValue(getDocument(document,"2次编号"));
            row.createCell(4).setCellValue(getDocument(document,"hospitalId"));

            Map<String,String> arrlist=mapList.get(getDocument(document,"hospitalId").trim()+getDocument(document,"姓名").trim());
            if(arrlist == null)
                row.createCell(5).setCellValue("");
            else
            {
                String strPID="";

                for(Map.Entry<String,String> map:arrlist.entrySet() )
                 {
                    strPID+=map.getKey()+"/";
                 }
                row.createCell(5).setCellValue(strPID.substring(0,strPID.length()-1));
            }
        }
        saveExcelTool.saveExcel("AS患者汇总表.xlsx");
    }

    private static String getDocument(JSONObject obj,String key)
    {
        String result=obj.getString(key);
        if(result == null)
            return "";
        return result;
    }
}
