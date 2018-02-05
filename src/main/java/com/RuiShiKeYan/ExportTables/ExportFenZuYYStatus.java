package com.RuiShiKeYan.ExportTables;

import com.RuiShiKeYan.Common.Interface.IruiShiKeYan;
import com.RuiShiKeYan.Common.Method.*;
import com.RuiShiKeYan.ExportTables.entity.RaYYAgeGroup;
import com.RuiShiKeYan.SubMethod.getHDPInfo;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.client.MongoDatabase;
import com.yiyihealth.data.DaX.reader.DSExcelReader2;
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
 * Date:2018/2/5
 * Time:上午10:24
 */
//分组用药情况表
public class ExportFenZuYYStatus extends RuiShiKeYan implements IruiShiKeYan{

    Map<String,ArrayList<String>> mapADRSubItem= new HashMap<String, ArrayList<String>>();
    Map<String,String> mapSubAndSysMapping=new HashMap<String, String>();

    public static void main(String[] args) throws Exception
    {
        MongoDBHelper mongoDBHelper = new MongoDBHelper("HDP-live");
        MongoDatabase db= mongoDBHelper.getDb();

        IruiShiKeYan iruiShiKeYan=new ExportFenZuYYStatus();
        iruiShiKeYan.run(db);

        mongoDBHelper.closeMongoDb();
    }

    public void run(MongoDatabase db, Object[] args) {
        try {
            String fileName= LocalHostInfo.getPath()+"交付/首诊时间表.xlsx";
            String fileColName=LocalHostInfo.getPath()+"交付/确诊表现表.xlsx";
            Map<String,JSONObject> mapShouZhen= new HashMap<String, JSONObject>();
            Map<String,JSONObject> mapQueZhen= new HashMap<String, JSONObject>();
            Map<String,Document> mapYY = new HashMap<String, Document>();
            ReadExcelToMap.readFromExcelToMap(mapShouZhen,fileName,"患者（PID）","生产状况分组","地域","医院","性别");
            ReadExcelToMap.readFromExcelToMap(mapQueZhen,fileColName,"患者（PID）","初发时间天","初发时间年减去出生年");
            getHDPInfo.getYYDay(db,mapYY,"");
            writeToExcel(mapQueZhen,mapShouZhen,mapYY);
        }catch (Exception e)
        {e.printStackTrace();}
    }

     private void writeToExcel(Map<String,JSONObject> mapQueZhen,Map<String,JSONObject> mapShouZhen,Map<String,Document> mapYY)
     {
         try {
             getSubItemMap();
             SaveExcelTool saveExcelTool = new SaveExcelTool();
             SXSSFSheet sheet = saveExcelTool.getSheet("");
             saveExcelTool.fillExcelTitle("PID,医院,性别,生产状况分组,地域,初发时间天,初发年龄,初发年龄分5组,观察终点,用药子项,用药系统项,用药时间天,用药RID");
             int rowNum=1;
             for(Map.Entry<String,JSONObject> mapPid:mapQueZhen.entrySet())
             {
               JSONObject jsShouZhen= mapShouZhen.get(mapPid.getKey());
               JSONObject jsQueZhen= mapPid.getValue();
               if(jsShouZhen == null)
                   continue;
               String strAge =getJSonValue(jsQueZhen,"初发时间年减去出生年");
               String strStartTime=getJSonValue(jsQueZhen,"初发时间天");
               int ageIndex = RaYYAgeGroup.getAgeIndex(strAge);
               String strEndTime=RaYYAgeGroup.getIndexToMaxAge(ageIndex,strStartTime,strAge);
               String strAgeGroupName=RaYYAgeGroup.getRaAgeGroupName(getJSonValue(jsShouZhen,"性别"),ageIndex);

            //   boolean flag =true;
               for(Map.Entry<String, ArrayList<String>> mapItem:mapADRSubItem.entrySet()) {
                   JSONObject jsFirstTime=getEntityFirstTime(mapPid.getKey(),mapYY,mapItem.getValue(),strEndTime);
                   if(jsFirstTime == null)
                       continue;
               //    flag =false;
                   System.out.println("分组用药情况表:"+rowNum);
                   Row row = sheet.createRow(rowNum++);
                   int cell = 0;
                   cell = fillBasicInfo(row, cell, mapPid.getKey(), jsShouZhen, strStartTime, strAge, strAgeGroupName, strEndTime);
                   row.createCell(cell++).setCellValue(mapItem.getKey());
                   row.createCell(cell++).setCellValue(mapSubAndSysMapping.get(mapItem.getKey()));
                   row.createCell(cell++).setCellValue(getJSonValue(jsFirstTime,"firstTime"));
                   row.createCell(cell++).setCellValue(getJSonValue(jsFirstTime,"RID"));
               }
//               if(flag)
//               {
//                   Row row = sheet.createRow(rowNum++);
//                   int cell = 0;
//                   fillBasicInfo(row, cell, mapPid.getKey(), jsShouZhen, strStartTime, strAge, strAgeGroupName, strEndTime);
//               }
             }

             saveExcelTool.saveExcel("交付/分组用药情况表.xlsx");

         }catch (Exception e)
         {
             e.printStackTrace();
         }

     }
   private JSONObject getEntityFirstTime(String strPId,Map<String,Document> mapEntity,ArrayList<String> arrayList,String strSYRIDTime)
    {
        JSONObject jsonObject= new JSONObject();
        String strFirstTime="N";


        for (int i = 0; i <arrayList.size() ; i++) {
            String strTableName=arrayList.get(i);
            Document document=mapEntity.get(strPId+strTableName);
            if(document !=null)
            {
                String strtempTime=get10JSonValue(document,"firstTime");
                if(strFirstTime.compareTo(strtempTime) >0 &&(strSYRIDTime.equals("全病程") || strtempTime.compareTo(strSYRIDTime) <=0))
                {
                    strFirstTime=strtempTime;
                    jsonObject.put("firstTime",strFirstTime);
                    jsonObject.put("RID",getJSonValue(document,"RID"));
                //    jsonObject.put("段落标题",getJSonValue(document,"段落标题"));
                }
            }
        }

        if(strFirstTime.equals("N"))
            return null;
        return jsonObject;
    }
    private int fillBasicInfo(Row row,int cell,String strPid,JSONObject jsShouZhen,String strStartTime,String strAge,
                              String ageGroupName,String strEndTime)
    {
        row.createCell(cell++).setCellValue(strPid);
        row.createCell(cell++).setCellValue(getJSonValue(jsShouZhen,"医院"));
        row.createCell(cell++).setCellValue(getJSonValue(jsShouZhen,"性别"));
        row.createCell(cell++).setCellValue(getJSonValue(jsShouZhen,"生产状况分组"));
        row.createCell(cell++).setCellValue(getJSonValue(jsShouZhen,"地域"));
        row.createCell(cell++).setCellValue(strStartTime);
        row.createCell(cell++).setCellValue(strAge);
        row.createCell(cell++).setCellValue(ageGroupName);
        row.createCell(cell++).setCellValue(strEndTime);

        return cell;
    }
    private  void getSubItemMap() throws Exception
    {
        JSONObject document;
        String fileName= LocalHostInfo.getPath()+ BaseInfo_Title_ListValue_DBCondition.strCLeiJiFenZuFileName;
        String tempZiXiang,tempSystem;
        JSONObject config = new JSONObject();
        config.put("filename", fileName);
        config.put("source_type", "excel");

        DSExcelReader2 excelReader = new DSExcelReader2(config);
        while ((document = excelReader.nextDocument()) != null) {
            tempZiXiang=getJSonValue(document,"子项");
            tempSystem=getJSonValue(document,"拟观察系统累及分组");
            if(tempZiXiang.equals("")||tempZiXiang.toUpperCase().equals("N"))
                continue;
            if(getJSonValue(document,"表型").equals("用药通用名"))
            {
                ArrayList<String> arrayList=mapADRSubItem.get(tempZiXiang);
                if(arrayList ==null) {
                    arrayList = new ArrayList<String>();
                    mapADRSubItem.put(tempZiXiang,arrayList);
                    mapSubAndSysMapping.put(tempZiXiang,tempSystem);
                }
                arrayList.add(getJSonValue(document,"表型名称") +getJSonValue(document,"标准标本"));

            }


        }
    }
}
