package test.java.doResearch;

import com.RuiShiKeYan.Common.Method.LocalHostInfo;
import com.RuiShiKeYan.Common.Method.MongoDBHelper;
import com.RuiShiKeYan.Common.Method.ReadExcelToMap;
import com.RuiShiKeYan.Common.Method.SaveExcelTool;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.bson.Document;
import test.java.task_SLE_LangChuang.BaseInfo_Title_ListValue_DBCondition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA
 * User:huangming
 * Date:2018/1/10
 * Time:下午3:16
 */
public class GetAllPIDRecordTime {

    public static void main(String[] args)throws Exception
    {
        String fileName = LocalHostInfo.getPath() + "交付/首诊时间表.xlsx";
        Map<String,String> mapPIDInfo= new HashMap<String, String>();
        ReadExcelToMap.readFromExcelToMap(mapPIDInfo,fileName,"患者（PID）");
        MongoDBHelper mongoDBHelper = new MongoDBHelper("HDP-live");
        MongoDatabase mdb= mongoDBHelper.getDb();

        Map<String,ArrayList<String>> mapArray=getFirstLastRIDDay(mdb);
        writeToExcel(mapPIDInfo,mapArray);
      mongoDBHelper.closeMongoDb();
    }
   private static void writeToExcel(Map<String,String> mapPIDInfo,Map<String,ArrayList<String>> mapArray)
   {
       SaveExcelTool saveExcelTool=new SaveExcelTool();
       SXSSFSheet sheet =saveExcelTool.getSheet("");
       int RowNum=0;
       for(Map.Entry<String,String> map:mapPIDInfo.entrySet())
       {
           Row row =sheet.createRow(RowNum++);
           int cell=0;
           ArrayList<String> arrayList=mapArray.get(map.getKey());
           row.createCell(cell++).setCellValue(map.getKey());
           for (int i = 0; i < arrayList.size(); i++) {
              if(arrayList.get(i).length()>10)
                  row.createCell(cell++).setCellValue(arrayList.get(i).substring(0,10));
              else
                  row.createCell(cell++).setCellValue(arrayList.get(i));
           }

       }
       saveExcelTool.saveExcel("交付/PID所有病历时间.xlsx");
   }
    public static Map<String,ArrayList<String>> getFirstLastRIDDay(MongoDatabase db)
    {
        Map<String,ArrayList<String>> mapResult=new HashMap<String, ArrayList<String>>();
        MongoCollection<Document> mc = db.getCollection("ARB");
        ArrayList<Document> aggregates = new ArrayList<Document>();
        aggregates.add(new Document("$match",Document.parse("{"+ BaseInfo_Title_ListValue_DBCondition.ADO13+",'记录时间戳':{$exists:true,$regex:/^[0-9]{4}.*/}}")));
        aggregates.add(new Document("$sort",Document.parse("{'PID':1,'记录时间戳':1}")));
        aggregates.add(new Document("$group",Document.parse("{'_id':{'PID':'$PID'}, 'result':{'$push':'$记录时间戳'}}")));
        MongoCursor<Document> cursor =mc.aggregate(aggregates).allowDiskUse(true).iterator();
        while (cursor.hasNext())
        {
            Document dd=cursor.next();
            Document dPid=(Document)dd.get("_id");

            ArrayList<String> arrayList=(ArrayList<String>)dd.get("result");
            mapResult.put(dPid.getString("PID"),arrayList);
        }

        return mapResult;
    }
}
