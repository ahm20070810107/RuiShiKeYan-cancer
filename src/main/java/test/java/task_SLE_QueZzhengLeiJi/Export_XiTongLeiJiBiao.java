package test.java.task_SLE_QueZzhengLeiJi;

import com.RuiShiKeYan.Common.Method.LocalHostInfo;
import com.RuiShiKeYan.Common.Method.MongoDBHelper;
import com.RuiShiKeYan.Common.Method.SaveExcelTool;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.client.MongoDatabase;
import com.yiyihealth.data.DaX.reader.DSExcelReader2;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import test.java.task_SLE_LangChuang.BaseInfo_Title_ListValue_DBCondition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA
 * User:huangming
 * Date:2017/10/19
 * Time:下午2:02
 */
public class Export_XiTongLeiJiBiao extends  LeijiPublicMethod{
     static  Map<String,String>  mapHospital = new HashMap<String, String>();
     static  Map<String,JSONObject>  mapPID = new HashMap<String, JSONObject>();
    static  Map<String,JSONObject>  mapQZBXTable = new HashMap<String, JSONObject>();
     static  Map<String,ArrayList> mapLeiJiFenZu = new HashMap<String, ArrayList>();
    static Map<String,String> mapExceptPID =new HashMap<String, String>();
    static JSONObject document = null;

     public static void main(String[] args) throws Exception
     {
         SaveExcelTool saveExcelTool= new SaveExcelTool();
         SXSSFSheet sheet = saveExcelTool.getSheet("");
         saveExcelTool.fillExcelTitle(BaseInfo_Title_ListValue_DBCondition.tiltleXitongleiji);
         MongoDBHelper mongoDBHelperHD= new MongoDBHelper("HDP-live");
         MongoDatabase dbHDP=mongoDBHelperHD.getDb();

         getHospitalInfo(mapHospital);//获取医院省市mapHospital
         getPIDInfo(mapPID,mapExceptPID); //获取所有满足条件的PID,mapPID
         getLeiJiFenZu();//获取累计分组的所有分组字段mapLeiJiFenZu
         getQZSJTable(mapQZBXTable);//获取确诊表现表数据mapQZBXTable
         XiTongLeiJiBiao.getXiTongLeiJiBiao(dbHDP,sheet,mapHospital,mapPID,mapQZBXTable,mapLeiJiFenZu);

         saveExcelTool.saveExcel("交付/系统累及表.xlsx");
         mongoDBHelperHD.closeMongoDb();
         saveNotInLeijiTable();

     }

     public static void saveNotInLeijiTable() throws Exception
     {
         String fileXTLeiJ= LocalHostInfo.getPath()+"交付/系统累及表.xlsx";
         JSONObject config = new JSONObject();
         config.put("filename", fileXTLeiJ);
         config.put("source_type", "excel");
         DSExcelReader2 excelReader = new DSExcelReader2(config);
         while((document=excelReader.nextDocument()) != null) {

             //判断最晚时间
             String strLastDay=document.getString("最晚记录时间天");
             if(strLastDay == null ||strLastDay.equals(""))
                 mapExceptPID.put(document.getString("患者（PID）"),"记录时间异常");
             else{
                 try{
                     Integer year=Integer.valueOf(strLastDay.substring(0,4));
                     if(year <= 1900 ||year >= 2018)
                         mapExceptPID.put(document.getString("患者（PID）"),"记录时间异常");
                 }catch (Exception e){e.printStackTrace();
                     mapExceptPID.put(document.getString("患者（PID）"),"记录时间异常");
                 }
             }

             //累及时间年减去出生年判断
             String strLeiJBirth=document.getString("累及时间年减去出生年");

             if(strLeiJBirth == null ||"".equals(strLeiJBirth))
             {
                 //  mapExceptPID.put(document.getString("患者（PID）"),"累及时间异常");
             }
             else
             {
                  try{
                      Integer leijiBirth=Integer.valueOf(strLeiJBirth);
                      if(leijiBirth <0 ||leijiBirth >100)
                          mapExceptPID.put(document.getString("患者（PID）"),"累及时间异常");
                  }catch (Exception e){e.printStackTrace();}
             }

         }
         DeleteExceptPIDForLeiJiTable(mapExceptPID);
        saveToExcel(mapExceptPID);
     }

     private static void DeleteExceptPIDForLeiJiTable(Map<String, String> mapExceptPID)
     {
        try
        {
            String fileXTLeiJ = LocalHostInfo.getPath() + "交付/系统累及表.xlsx";
            SaveExcelTool saveExcelTool= new SaveExcelTool();
            SXSSFSheet sheet =saveExcelTool.getSheet("");
            saveExcelTool.fillExcelTitle(BaseInfo_Title_ListValue_DBCondition.tiltleXitongleiji);
            String[] title = BaseInfo_Title_ListValue_DBCondition.tiltleXitongleiji.split(",");
            JSONObject document;
            JSONObject config = new JSONObject();
            config.put("filename", fileXTLeiJ);
            int RowNum=1;
            DSExcelReader2 excelReader = new DSExcelReader2(config);
            while ((document =excelReader.nextDocument()) !=null)
            {
                if(mapExceptPID.containsKey(document.getString("患者（PID）")))
                    continue;
                Row row = sheet.createRow(RowNum++);
                for (int i = 0; i < title.length; i++) {
                   row.createCell(i).setCellValue(document.getString(title[i]));
                }
            }
            saveExcelTool.saveExcel("交付/系统累及表.xlsx");

        }catch (Exception e)
        {
            e.printStackTrace();
        }
     }
     private  static void saveToExcel(Map<String, String> mapExceptPID)
     {
         SaveExcelTool saveExcelTool= new SaveExcelTool();
         SXSSFSheet sheet = saveExcelTool.getSheet("");
         saveExcelTool.fillExcelTitle("移出步骤,PID");
         int RowNum=1;
         for(Map.Entry<String,String> map :mapExceptPID.entrySet())
         {
           Row row= sheet.createRow(RowNum++);
           row.createCell(0).setCellValue(map.getValue());
           row.createCell(1).setCellValue(map.getKey());
         }
         saveExcelTool.saveExcel("交付/移除组PID列表.xlsx");
     }


    public static void getLeiJiFenZu() throws Exception
    {
        String fileName= LocalHostInfo.getPath()+BaseInfo_Title_ListValue_DBCondition.strCLeiJiFenZuFileName;
        String tempFenZu,tempZuHe;
        JSONObject config = new JSONObject();
        config.put("filename", fileName);
        config.put("source_type", "excel");

        DSExcelReader2 excelReader = new DSExcelReader2(config);
        while ((document = excelReader.nextDocument()) != null) {
            tempFenZu=getJSonValue(document,"对标观察项目");
            tempZuHe=getJSonValue(document,"表型名称")+getJSonValue(document,"标准标本");
            if(tempFenZu.equals("")||tempFenZu.toLowerCase().equals("n"))
                continue;
            if(mapLeiJiFenZu.containsKey(tempFenZu)) {
                ArrayList arrayList=mapLeiJiFenZu.get(tempFenZu);
                arrayList.add(tempZuHe);
            }
            else
            {
                ArrayList arrayList=new ArrayList();
                arrayList.add(tempZuHe);
                mapLeiJiFenZu.put(tempFenZu,arrayList);
            }
        }
    }
}
