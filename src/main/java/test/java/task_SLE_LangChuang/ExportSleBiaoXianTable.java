package test.java.task_SLE_LangChuang;

import com.RuiShiKeYan.Common.Method.MongoDBHelper;
import com.RuiShiKeYan.Common.Method.SaveExcelTool;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.client.MongoDatabase;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import com.RuiShiKeYan.SubMethod.LangCShengYanYinShuPublicInfo;
/**
 * Created with IntelliJ IDEA
 * User:huangming
 * Date:2017/10/25
 * Time:下午3:09
 */

//SLE表现表
public class ExportSleBiaoXianTable {
    static LangCShengYanYinShuPublicInfo langCShengYanYinShuPublicInfo;
    static Map<String,JSONObject> mapPid= new HashMap<String, JSONObject>();
    public static void main(String[] args) throws Exception
    {
        MongoDBHelper mongoDBHelper= new MongoDBHelper("HDP-live");
        MongoDatabase mdb=mongoDBHelper.getDb();
        getBaseInfo(mdb);
        SaveExcelTool saveExcelTool = new SaveExcelTool();
        SXSSFSheet sheet = saveExcelTool.getSheet("");
        langCShengYanYinShuPublicInfo.fillExcelTitle(sheet,BaseInfo_Title_ListValue_DBCondition.titleSleBiaoXianB,true);
        writeToExcel(sheet);
        saveExcelTool.saveExcel("交付/SLE表现表.xlsx");
        mongoDBHelper.closeMongoDb();
    }

    private static void getBaseInfo(MongoDatabase mdb)throws Exception
    {
        langCShengYanYinShuPublicInfo=new LangCShengYanYinShuPublicInfo();
        langCShengYanYinShuPublicInfo.getBaseInfo(mdb);
        langCShengYanYinShuPublicInfo.getHYYinShuInfo(mdb,false);
        ReadFromExcelToMap.getPidInfo(langCShengYanYinShuPublicInfo.mapExcludePID,mapPid);
    }
   private static void writeToExcel(SXSSFSheet sheet) throws Exception
   {
       int RowNum = 1;
       for (Map.Entry<String, JSONObject> map : mapPid.entrySet()) {
           System.out.println("SLE表现表:"+RowNum);
           String strPID = map.getKey();
           JSONObject jsonPID = map.getValue();
           String sleTime=jsonPID.getString("诊断时间天");
           Row row = sheet.createRow(RowNum++);
           row.createCell(0).setCellValue(strPID); //第二列和第一列放后面
           row.createCell(1).setCellValue(jsonPID.getString("医院"));
           row.createCell(2).setCellValue(jsonPID.getString("出生年"));
           row.createCell(3).setCellValue(jsonPID.getString("性别"));
           row.createCell(4).setCellValue(jsonPID.getString("地域"));

           row.createCell(5).setCellValue(sleTime);
           row.createCell(6).setCellValue(jsonPID.getString("SLE年龄"));
           try {
               row.createCell(7).setCellValue(langCShengYanYinShuPublicInfo.getAgeGroup(Integer.valueOf(jsonPID.getString("SLE年龄"))));
           }catch (Exception e){e.printStackTrace();}

           fillLeftColumForExcel(row, 8, strPID);
       }

   }

   private static void fillLeftColumForExcel(Row row,int columNum,String strPid)
   {
       Map<String ,JSONObject> mapItem = new HashMap<String, JSONObject>();
       Map<String ,JSONObject> mapSubItem = new HashMap<String, JSONObject>();
        for (Map.Entry<String,ArrayList> map:langCShengYanYinShuPublicInfo.mapLeiJiFenZu.entrySet())
        {
            JSONObject dd= langCShengYanYinShuPublicInfo.fill6To10(strPid,map.getValue(),"empty",map.getKey());  //empty表示不比较时间
            if(dd==null) {
                mapItem.put(map.getKey(),null);
                row.createCell(columNum++).setCellValue(0);
            }
            else
            {
                mapItem.put(map.getKey(),dd);
                row.createCell(columNum++).setCellValue(1);
            }
        }

       for (Map.Entry<String,ArrayList> map:langCShengYanYinShuPublicInfo.mapLeiJiSubFenZu.entrySet())
       {
           JSONObject dd= langCShengYanYinShuPublicInfo.fill6To10(strPid,map.getValue(),"empty",map.getKey());
           if(dd==null) {
               mapSubItem.put(map.getKey(),null);
               row.createCell(columNum++).setCellValue(0);
           }
           else
           {
               mapSubItem.put(map.getKey(),dd);
               row.createCell(columNum++).setCellValue(1);
           }
       }
      for(Map.Entry<String,JSONObject> map:mapItem.entrySet())
      {
          columNum=ReadFromExcelToMap.SaveEntityDetail(columNum,map,row);
      }
       for(Map.Entry<String,JSONObject> map:mapSubItem.entrySet())
       {
           columNum=ReadFromExcelToMap.SaveEntityDetail(columNum,map,row);
       }
   }

}
