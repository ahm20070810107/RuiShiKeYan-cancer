package test.java.task_SLE_LangChuang;

import com.RuiShiKeYan.Common.Method.LocalHostInfo;
import com.RuiShiKeYan.Common.Method.MongoDBHelper;
import com.RuiShiKeYan.Common.Method.SaveExcelTool;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.client.MongoDatabase;
import com.yiyihealth.data.DaX.reader.DSExcelReader2;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import com.RuiShiKeYan.SubMethod.LangCShengYanYinShuPublicInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA
 * User:huangming
 * Date:2017/10/25
 * Time:下午3:09
 */
public class ExportSlePingFenTable {
    static JSONObject document;
    static LangCShengYanYinShuPublicInfo langCShengYanYinShuPublicInfo;
    static Map<String,JSONObject> mapPid= new HashMap<String, JSONObject>();
    static Map<String,ArrayList> mapFirstGradeItem= new HashMap<String, ArrayList>();
    static Map<String,ArrayList> mapOneTwoMapping= new HashMap<String, ArrayList>();
    static Map<String,Integer> mapOneMark= new HashMap<String, Integer>();
    static Map<String,ArrayList> mapSlePinFenMark= new HashMap<String, ArrayList>();
    public static void main(String[] args) throws Exception
    {
        MongoDBHelper mongoDBHelper= new MongoDBHelper("HDP-live");
        MongoDatabase mdb=mongoDBHelper.getDb();
        langCShengYanYinShuPublicInfo= new LangCShengYanYinShuPublicInfo();
        langCShengYanYinShuPublicInfo.getBaseInfo(mdb);
        langCShengYanYinShuPublicInfo.getHYZhenDuanPinFen(mdb);
        getBaseInfo();
        SaveExcelTool saveExcelTool = new SaveExcelTool();
        SXSSFSheet sheet = saveExcelTool.getSheet("");
        langCShengYanYinShuPublicInfo.fillSlePingFenExcelTitle(sheet,BaseInfo_Title_ListValue_DBCondition.tiltlePingFenTable,mapOneTwoMapping,mapOneMark,true);
        writeToExcel(sheet,false,false);
        saveExcelTool.saveExcel("交付/SLE诊断评分表.xlsx");

        SaveExcelTool saveExcelTool1 = new SaveExcelTool();
        SXSSFSheet sheet1 = saveExcelTool1.getSheet("");
        langCShengYanYinShuPublicInfo.fillSlePingFenExcelTitle(sheet1,BaseInfo_Title_ListValue_DBCondition.tiltlePingFenTable,mapOneTwoMapping,mapOneMark,true);
        writeToExcel(sheet1,true,false);
        saveExcelTool1.saveExcel("交付/SLE诊断评分表-无sle时间限制.xlsx");

        SaveExcelTool saveExcelTool2 = new SaveExcelTool();
        SXSSFSheet sheet2 = saveExcelTool2.getSheet("");
        langCShengYanYinShuPublicInfo.fillSlePingFenExcelTitle(sheet2,BaseInfo_Title_ListValue_DBCondition.tiltlePingFenTable,mapOneTwoMapping,mapOneMark,true);
        writeToExcel(sheet2,true,true);
        saveExcelTool2.saveExcel("交付/SLE诊断评分表-无sle时间限制-ANA条件放宽.xlsx");

        mongoDBHelper.closeMongoDb();
    }

    private static void getBaseInfo()throws Exception
    {
        ReadFromExcelToMap.getPidInfo(langCShengYanYinShuPublicInfo.mapExcludePID,mapPid);
        getPingFenMap();
        getSubAndItemMap();
    }
/*

  flag为true表示不判断sletime
  ANAflag为true表示ana使用RPG科研结果转换 flase表示用科研诊断评分定性
 */
   private static void writeToExcel(SXSSFSheet sheet,boolean flag,boolean ANAflag) throws Exception
   {
       int RowNum = 1;
       for (Map.Entry<String, JSONObject> map : mapPid.entrySet()) {
           System.out.println("SLE诊断评分表"+RowNum);
           String strPID = map.getKey();
           JSONObject jsonPID = map.getValue();
           String sleTime=jsonPID.getString("诊断时间天");
           Row row = sheet.createRow(RowNum++);
           row.createCell(0).setCellValue(strPID); //第二列和第一列放后面

           fillANAPingFenMark(strPID,3,mapSlePinFenMark,row,sleTime,flag,ANAflag);
           row.createCell(7).setCellValue(jsonPID.getString("医院"));
           row.createCell(8).setCellValue(jsonPID.getString("出生年"));
           row.createCell(9).setCellValue(jsonPID.getString("性别"));
           row.createCell(10).setCellValue(jsonPID.getString("地域"));

           row.createCell(11).setCellValue(sleTime);
           row.createCell(12).setCellValue(jsonPID.getString("SLE年龄"));
           try {
               row.createCell(13).setCellValue(langCShengYanYinShuPublicInfo.getAgeGroup(Integer.valueOf(jsonPID.getString("SLE年龄"))));
           }catch (Exception e){e.printStackTrace();}

           fillLeftColumForExcel(row, 14, strPID, sleTime,flag);
       }
   }

  private static void fillANAPingFenMark(String strPID,int column,Map<String,ArrayList> map,Row row,String sleTime,boolean flag,boolean ANAflag)
  {

      JSONObject jsonObject = langCShengYanYinShuPublicInfo.getANAPingFenMark(strPID, map,sleTime,flag,ANAflag);
      if(jsonObject ==  null)
      {
          row.createCell(column++).setCellValue("0");
          row.createCell(column++).setCellValue("");
          row.createCell(column++).setCellValue("");
          row.createCell(column++).setCellValue("");
      }else
      {
          row.createCell(column++).setCellValue("1");
          row.createCell(column++).setCellValue(jsonObject.getString("时间天"));
          row.createCell(column++).setCellValue(jsonObject.getString("名称"));
          row.createCell(column++).setCellValue(jsonObject.getString("RID"));
      }

  }
   private static void fillLeftColumForExcel(Row row,int columNum,String strPid,String sleTime,boolean flag)
   {
       Map<String ,Integer> mapItem = new HashMap<String, Integer>();
       Map<String ,JSONObject> mapSubItem = new HashMap<String, JSONObject>();
        for (Map.Entry<String,ArrayList> map:mapFirstGradeItem.entrySet())
        {
            JSONObject dd= langCShengYanYinShuPublicInfo.getYiJiMark(strPid,map.getValue(),sleTime,flag);
            if(dd !=null)
            {
                dd.put("mark",mapOneMark.get(map.getKey()));
            }
            mapSubItem.put(map.getKey(),dd);
        }

       for (Map.Entry<String,ArrayList> map:mapOneTwoMapping.entrySet())
       {
           ArrayList arry= map.getValue();
           Integer number=-1;
           if(map.getKey().equals("补体"))
           {
              if(mapSubItem.get("补体C3")!=null && mapSubItem.get("补体C4")!=null)
              {
                 mapItem.put(map.getKey(),mapOneMark.get("补体C3和补体C4"));
              }else if(mapSubItem.get("补体C3")==null && mapSubItem.get("补体C4")==null)
              {
                  mapItem.put(map.getKey(),-1);
              }else if(mapSubItem.get("补体C3")!=null)
              {
                  mapItem.put(map.getKey(),mapOneMark.get("补体C3"));
              }else if(mapSubItem.get("补体C4")!=null)
              {
                  mapItem.put(map.getKey(),mapOneMark.get("补体C4"));
              }
           }
           else{
             for (int i = 0; i < arry.size(); i++) {
                  String strYiJi=arry.get(i).toString();
                  if(mapSubItem.get(strYiJi) !=null)
                  {
                      Integer mark=mapSubItem.get(strYiJi).getInteger("mark");
                      if(mark > number)
                          number=mark;
                  }
              }
              mapItem.put(map.getKey(),number);
           }
       }
       //为了保证和表头一致使用mapOneTwoMapping循环
       Integer totalMark=0;
      for(Map.Entry<String,ArrayList> map:mapOneTwoMapping.entrySet())
      {
          if(mapItem.get(map.getKey()) ==-1)
            row.createCell(columNum++).setCellValue("");
          else {
              row.createCell(columNum++).setCellValue(mapItem.get(map.getKey()));
              totalMark +=mapItem.get(map.getKey());
          }
      }
      row.createCell(1).setCellValue(totalMark.toString());
      row.createCell(2).setCellValue(totalMark >=10?"Y":"N");
       for(Map.Entry<String,Integer> map:mapOneMark.entrySet())
       {
             if(mapSubItem.get(map.getKey())==null)
                 row.createCell(columNum++).setCellValue("");
             else
                 row.createCell(columNum++).setCellValue(mapSubItem.get(map.getKey()).getInteger("mark"));
       }
       for(Map.Entry<String,Integer> map:mapOneMark.entrySet())
       {
           if(mapSubItem.get(map.getKey())==null){
               row.createCell(columNum++).setCellValue("");
               row.createCell(columNum++).setCellValue("");
               row.createCell(columNum++).setCellValue("");
               row.createCell(columNum++).setCellValue("");
               row.createCell(columNum++).setCellValue("");
           }
           else {
               JSONObject obj=mapSubItem.get(map.getKey());
               row.createCell(columNum++).setCellValue(obj.getString("实体"));
               row.createCell(columNum++).setCellValue(obj.getString("状态"));
               row.createCell(columNum++).setCellValue(obj.getString("时间天"));
               row.createCell(columNum++).setCellValue(obj.getString("名称"));
               row.createCell(columNum++).setCellValue(obj.getString("RID"));
           }
       }
   }

   private static void getPingFenMap()throws Exception
   {
       String fileName= LocalHostInfo.getPath()+"SLE评分表.xlsx";
       JSONObject config = new JSONObject();
       config.put("filename", fileName);
       config.put("source_type", "excel");
       DSExcelReader2 excelReader = new DSExcelReader2(config);
       while ((document = excelReader.nextDocument()) != null) {
            String strErJi=ReadFromExcelToMap.getJSonValue(document,"2017诊断评分二级");
            String strYiJi=ReadFromExcelToMap.getJSonValue(document,"2017诊断评分一级");
            if(strErJi.equals("")||strYiJi.equals(""))
                continue;
           mapOneMark.put(strYiJi,document.getInteger("2017诊断评分一级分数"));
           if(mapOneTwoMapping.containsKey(strErJi)) {
               ArrayList arrayList=mapOneTwoMapping.get(strErJi);
               arrayList.add(strYiJi);
           }
           else
           {
               ArrayList arrayList=new ArrayList();
               arrayList.add(strYiJi);
               mapOneTwoMapping.put(strErJi,arrayList);
           }
       }
   }
    private static void getSubAndItemMap() throws Exception
    {
        String fileName= LocalHostInfo.getPath()+BaseInfo_Title_ListValue_DBCondition.strCLeiJiFenZuFileName;
        String tempFenZu,tempZuHe;
        JSONObject config = new JSONObject();
        config.put("filename", fileName);
        config.put("source_type", "excel");

        DSExcelReader2 excelReader = new DSExcelReader2(config);
        while ((document = excelReader.nextDocument()) != null) {
            tempFenZu=ReadFromExcelToMap.getJSonValue(document,"2017诊断评分一级");
            tempZuHe=ReadFromExcelToMap.getJSonValue(document,"表型名称")+ReadFromExcelToMap.getJSonValue(document,"标准标本");
            if(!tempFenZu.equals("")&&mapFirstGradeItem.containsKey(tempFenZu)) {
                ArrayList arrayList=mapFirstGradeItem.get(tempFenZu);
                arrayList.add(tempZuHe);
            }
            else if(!tempFenZu.equals(""))
            {
                ArrayList arrayList=new ArrayList();
                arrayList.add(tempZuHe);
                mapFirstGradeItem.put(tempFenZu,arrayList);
            }
            tempFenZu=ReadFromExcelToMap.getJSonValue(document,"2017诊断评分标记");
            if(!tempFenZu.equals(""))
            {
               if(mapSlePinFenMark.containsKey(tempFenZu))
               {
                   ArrayList arrayList=mapSlePinFenMark.get(tempFenZu);
                   arrayList.add(tempZuHe);
               }else
               {
                   ArrayList arrayList=new ArrayList();
                   arrayList.add(tempZuHe);
                   mapSlePinFenMark.put(tempFenZu,arrayList);
               }
            }
        }

    }


}
