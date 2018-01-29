package com.RuiShiKeYan.ExportTables;

import com.RuiShiKeYan.Common.Interface.IruiShiKeYan;
import com.RuiShiKeYan.Common.Method.*;
import com.RuiShiKeYan.SubMethod.getHDPInfo;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.client.MongoDatabase;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.bson.Document;

import java.util.*;

/**
 * Created with IntelliJ IDEA
 * User:huangming
 * Date:2018/1/2
 * Time:下午4:45
 */
public class ExportWholePheno_LnPrediction extends RuiShiKeYan implements IruiShiKeYan {

    private int[] xGroup={12,6,24,36};
    private  Map<String,ArrayList<String>>  mapLeiJiFenZu = new HashMap<String, ArrayList<String>>();
    private  Map<String,ArrayList<String>>  mapLeiJiSubFenZu = new HashMap<String, ArrayList<String>>();
    private Map<String,Document> mapLCShengyanHYInfo= new HashMap<String, Document>();
    private Map<String,Document> mapLCShengyanZZInfo=new HashMap<String, Document>();
    private Map<String,Document> mapLCShengyanTZInfo=new HashMap<String, Document>();
    private Map<String,Document> mapLCShengyanZDInfo=new HashMap<String, Document>();
    private Set<String> setDelist=new HashSet<String>();
    private    Map<String,Document> mapYY = new HashMap<String, Document>();
    private    Map<String,Document> mapZD = new HashMap<String, Document>();
    private    Map<String,Document> mapZZ = new HashMap<String, Document>();
    private    Map<String,Document> mapTZ = new HashMap<String, Document>();
    private    Map<String,Document> mapHY = new HashMap<String, Document>();

    public static void main(String[] args) throws Exception
    {
        MongoDBHelper mongoDBHelper = new MongoDBHelper("HDP-live");
        MongoDatabase db= mongoDBHelper.getDb();
        IruiShiKeYan iruiShiKeYan= new ExportWholePheno_LnPrediction();
        iruiShiKeYan.run(db);
        mongoDBHelper.closeMongoDb();
    }


    public void run(MongoDatabase mdb, Object[] args) {
        try {
            String fileName = LocalHostInfo.getPath() + "交付/首诊时间表.xlsx";
            String fileYCName=LocalHostInfo.getPath() + "交付/移除组PID列表.xlsx";
            String fileDeName=LocalHostInfo.getPath() + "WholePheno_LnPrediction_Delist.xlsx";
            Map<String,String> mapException= new HashMap<String,String>();
            Map<String,JSONObject> mapPIDInfo= new HashMap<String, JSONObject>();
            ReadExcelToMap.readFromExcelToMap(mapException,fileYCName,"PID");
            ReadExcelToMap.readFromExcelToMap(mapPIDInfo,fileName,"患者（PID）",mapException);
            ReadExcelToMap.readFromExcelToMap(setDelist,fileDeName,"Delist");

            getBasicInfo(mdb);
            mapException=null;
            for (int x:xGroup) {
                writeToExcel(mdb, mapPIDInfo, x);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getBasicInfo(MongoDatabase mdb)
    {
         try
         {

             ReadExcelToMap.getSubAndItemMap(mapLeiJiFenZu,mapLeiJiSubFenZu);
             JSONObject jsShenYanList=ReadExcelToMap.getShenYanList();  //加载狼疮肾炎
             if(!getJSonValue(jsShenYanList,"ZDList").equals(""))
                 getHDPInfo.getLCShengyanZDInfo(mdb,",'标准诊断名':{$in:["+getJSonValue(jsShenYanList,"ZDList")+"]}",mapLCShengyanZDInfo);
             if(!getJSonValue(jsShenYanList,"HYList").equals(""))
                 getHDPInfo.getLCShengyanHYInfo(mdb,mapLCShengyanHYInfo,getJSonValue(jsShenYanList,"HYList"),",'RPG科研结果转换':'阳性'");
             if(!getJSonValue(jsShenYanList,"TZList").equals(""))
                 getHDPInfo.getTZLangCShengyan(mdb,mapLCShengyanTZInfo,getJSonValue(jsShenYanList,"TZList"));
             if(!getJSonValue(jsShenYanList,"ZZList").equals(""))
                 getHDPInfo.getZZLangCShengyan(mdb,mapLCShengyanZZInfo,getJSonValue(jsShenYanList,"ZZList"));

    //加载每PID所有表型第一次及最后一次发生时间
             getHDPInfo.getYYDay(mdb,mapYY,"");
             getHDPInfo.getZDDay(mdb,mapZD,"");
             getHDPInfo.getHYDay(mdb,mapHY,",'化验结果定性（新）':'阳性'");
             getHDPInfo.getTZDay(mdb,mapTZ,"");
             getHDPInfo.getZZDay(mdb,mapZZ,"");

         }
         catch (Exception e) {
             e.printStackTrace();
         }
    }
    private void  writeToExcel(MongoDatabase mdb,Map<String,JSONObject> mapPIDInfo,int x)
    {
        String title="患者（PID）,医院,性别,出生年,SLE时间天,SLE首诊分组,首次表型时间天,最后表型时间天,全表型量,是否入组,LN时间天,最终分组,观测窗截止时间天,观测窗内表型量";

        SaveExcelTool saveExcelTool= new SaveExcelTool();
        SXSSFSheet sheet=saveExcelTool.getSheet("");
        int cellNum=saveExcelTool.fillExcelTitle(title);
        cellNum=saveExcelTool.fillExcelTitle(mapLeiJiFenZu,cellNum,"系统_");
        saveExcelTool.fillExcelTitle(mapLeiJiSubFenZu,cellNum,"");

        SaveExcelTool saveExcelTool1= new SaveExcelTool();
        SXSSFSheet sheet1=saveExcelTool1.getSheet("");
        fillExcelTitle(sheet1);

        int rowNum=1,rowNum2=1;
        for(Map.Entry<String,JSONObject> mapPid:mapPIDInfo.entrySet())
         {
             System.out.println("WholePheno_LnPrediction:"+rowNum);
             cellNum=0;
             Row row= sheet.createRow(rowNum++);

             JSONObject jsPidinfo= mapPid.getValue();
             row.createCell(cellNum++).setCellValue(mapPid.getKey());
             row.createCell(cellNum++).setCellValue(getJSonValue(jsPidinfo,"医院"));
             row.createCell(cellNum++).setCellValue(getJSonValue(jsPidinfo,"性别"));
             row.createCell(cellNum++).setCellValue(getJSonValue(jsPidinfo,"出生年"));
             row.createCell(cellNum++).setCellValue(getJSonValue(jsPidinfo,"诊断时间天"));
             row.createCell(cellNum++).setCellValue(getAgeGroup(getJSonValue(jsPidinfo,"诊断时间年减去出生年")));
             rowNum2=fillLeftCellValue(row,cellNum,mapPid.getKey(),x,rowNum2,sheet1);
         }

         saveExcelTool.saveExcel("/交付/WholePheno_LnPrediction-"+x+".xlsx");
         saveExcelTool1.saveExcel("/交付/WholePheno_LnPrediction-"+x+"-Analysis.xlsx");
    }
    private int fillLeftCellValue(Row row,int cellNum,String strPid,int x, int rowNum2,SXSSFSheet sheet)
    {
        String strFirstTime,str30FirstTime="",strLastTime,strGroupFlag;
        JSONObject jsonFirstLastDay=getFristLastDay(strPid);
        strFirstTime=getJSonValue(jsonFirstLastDay,"firstTime");

        strLastTime=getJSonValue(jsonFirstLastDay,"lastTime");
        if(strFirstTime.equals("")||strLastTime.equals(""))
        {strGroupFlag="N";}
        else {
            str30FirstTime=DateFormat.getNextDay(strFirstTime,x * 30);
            if (strLastTime.compareTo(str30FirstTime) < 0) {
                strGroupFlag = "N";
            } else {
                strGroupFlag = "Y";
            }
        }
        row.createCell(cellNum++).setCellValue(strFirstTime);
        row.createCell(cellNum++).setCellValue(strLastTime);
        row.createCell(cellNum++).setCellValue("");//全表型量
        row.createCell(cellNum++).setCellValue(strGroupFlag);
        if(strGroupFlag.equals("Y"))
        {
           String strLNTime=getLCShenYanTime(strPid);
           int strLastGroup; //最终分组
           String strEndTime;
           if(!strLNTime.equals("")&&str30FirstTime.compareTo(strLNTime) <=0)
           {
               strLastGroup=1;
           }else { strLastGroup=0;}

           if(strLastGroup==1)
               strEndTime=DateFormat.getNextDay(strLNTime,-30*x);
           else
               strEndTime=strLastTime;

            int cellNum2=0;
            Row row2=sheet.createRow(rowNum2++);

            row2.createCell(cellNum2++).setCellValue(strPid);
            row2.createCell(cellNum2++).setCellValue(strLastGroup);

            row.createCell(cellNum++).setCellValue(strLNTime);
            row.createCell(cellNum++).setCellValue(strLastGroup);
            row.createCell(cellNum++).setCellValue(strEndTime);
            row.createCell(cellNum++).setCellValue("");//观测窗内表型量
            for (Map.Entry<String,ArrayList<String>> map:mapLeiJiFenZu.entrySet())
            {
                JSONObject dd= getSystemAndSubDay(strPid,map.getValue(),strEndTime,map.getKey());
                int result=1;
                if(getJSonValue(dd,"firstTime").equals("N")) {
                    result=0;
                }
                row.createCell(cellNum++).setCellValue(result);
                if(!setDelist.contains("系统_"+map.getKey()))
                     row2.createCell(cellNum2++).setCellValue(result);
            }
            for (Map.Entry<String,ArrayList<String>> map:mapLeiJiSubFenZu.entrySet())
            {
                JSONObject dd= getSystemAndSubDay(strPid,map.getValue(),strEndTime,map.getKey());
                int result=1;
                if(getJSonValue(dd,"firstTime").equals("N")) {
                    result=0;
                }
                row.createCell(cellNum++).setCellValue(result);
                if(!setDelist.contains(map.getKey()))
                    row2.createCell(cellNum2++).setCellValue(result);
            }
        }
        return rowNum2;
    }




    private JSONObject getFristLastDay(String strPid)
    {
        JSONObject jsonObject = new JSONObject();
        String strFirstTime="N";
        String strLastTime="0";
        for (Map.Entry<String,ArrayList<String>> map:mapLeiJiFenZu.entrySet()) {
            JSONObject dd = getSystemAndSubDay(strPid, map.getValue(), "N", map.getKey());
            if(strFirstTime.compareTo(getJSonValue(dd,"firstTime")) >0)
                strFirstTime=getJSonValue(dd,"firstTime");
            if(strLastTime.compareTo(getJSonValue(dd,"lastTime"))<0)
                strLastTime=getJSonValue(dd,"lastTime");
        }
        for (Map.Entry<String,ArrayList<String>> map:mapLeiJiSubFenZu.entrySet()) {
            JSONObject dd = getSystemAndSubDay(strPid, map.getValue(), "N", map.getKey());
            if(strFirstTime.compareTo(getJSonValue(dd,"firstTime")) >0)
                strFirstTime=getJSonValue(dd,"firstTime");
            if(strLastTime.compareTo(getJSonValue(dd,"lastTime"))<0)
                strLastTime=getJSonValue(dd,"lastTime");
        }
        if(strFirstTime.equals("N"))
            strFirstTime="";
        if(strLastTime.equals("0"))
            strLastTime="";
        jsonObject.put("firstTime",strFirstTime);
        jsonObject.put("lastTime",strLastTime);
        return jsonObject;
    }


    /**
     *@return 返回Jsonobject的结果
     *@param strPid   pid的值
     *@param arrayList arrayList
     *@param strLCShengYanTime
     *@param strItem  子项的值
     */
    public  JSONObject getSystemAndSubDay(String strPid,ArrayList<String> arrayList,String strLCShengYanTime,String strItem)
    {
        String strFirstTime="N";
        String strLastTime="0";
        String strTempTime;
        JSONObject jsonObject = new JSONObject();

        for (int i = 0; i < arrayList.size(); i++) {
            String strSrouce=strPid+arrayList.get(i);
            if(strItem.equals("肾炎")) {
                if (mapLCShengyanHYInfo.containsKey(strSrouce)) {
                    strTempTime=get10JSonValue(mapLCShengyanHYInfo.get(strSrouce),"化验时间");
                    if (strFirstTime.compareTo(strTempTime) > 0 && strTempTime.compareTo(strLCShengYanTime) <= 0) {
                        strFirstTime=strTempTime;
                        strLastTime=strTempTime;
                    }
                }
            }
            else
            {
                if (mapHY.containsKey(strSrouce)) {
                    strTempTime=get10JSonValue(mapHY.get(strSrouce),"firstTime");
                    if(get10JSonValue(mapHY.get(strSrouce),"lastTime").compareTo(strLastTime) >0)
                        strLastTime=get10JSonValue(mapHY.get(strSrouce),"lastTime");
                    if (strFirstTime.compareTo(strTempTime) > 0 && strTempTime.compareTo(strLCShengYanTime) <= 0) {
                        strFirstTime = strTempTime;
                    }
                }
            }

            if (mapZZ.containsKey(strSrouce)) {
                strTempTime=get10JSonValue(mapZZ.get(strSrouce),"firstTime");
                if(get10JSonValue(mapZZ.get(strSrouce),"lastTime").compareTo(strLastTime) >0)
                    strLastTime=get10JSonValue(mapZZ.get(strSrouce),"lastTime");
                if (strFirstTime.compareTo(strTempTime) > 0 && strTempTime.compareTo(strLCShengYanTime) <= 0) {
                    strFirstTime = strTempTime;
                }
            }

            if (mapTZ.containsKey(strSrouce)) {
                strTempTime=get10JSonValue(mapTZ.get(strSrouce),"firstTime");
                if(get10JSonValue(mapTZ.get(strSrouce),"lastTime").compareTo(strLastTime) >0)
                    strLastTime=get10JSonValue(mapTZ.get(strSrouce),"lastTime");
                if (strFirstTime.compareTo(strTempTime) > 0 && strTempTime.compareTo(strLCShengYanTime) <= 0) {
                    strFirstTime = strTempTime;
                }
            }
            if (mapZD.containsKey(strSrouce)) {
                strTempTime=get10JSonValue(mapZD.get(strSrouce),"firstTime");
                if(get10JSonValue(mapZD.get(strSrouce),"lastTime").compareTo(strLastTime) >0)
                    strLastTime=get10JSonValue(mapZD.get(strSrouce),"lastTime");
                if (strFirstTime.compareTo(strTempTime) > 0 && strTempTime.compareTo(strLCShengYanTime) <= 0) {
                    strFirstTime = strTempTime;
                }
            }
            if (mapYY.containsKey(strSrouce)) {
                strTempTime=get10JSonValue(mapYY.get(strSrouce),"firstTime");
                if(get10JSonValue(mapYY.get(strSrouce),"lastTime").compareTo(strLastTime) >0)
                    strLastTime=get10JSonValue(mapYY.get(strSrouce),"lastTime");
                if (strFirstTime.compareTo(strTempTime) > 0 && strTempTime.compareTo(strLCShengYanTime) <= 0) {
                    strFirstTime = strTempTime;
                }
            }

        }
        jsonObject.put("firstTime",strFirstTime);
        jsonObject.put("lastTime",strLastTime);
        return jsonObject;
    }

    private  String getLCShenYanTime(String strPID)
    {
        String strNewTime="first";
        if(mapLCShengyanZZInfo.containsKey(strPID))
        {
            Document dd=mapLCShengyanZZInfo.get(strPID);
            if(strNewTime.compareTo(dd.getString("症状&体征时间"))>0 )
            {
                strNewTime=dd.getString("症状&体征时间");
            }
        }
        if(mapLCShengyanTZInfo.containsKey(strPID))
        {
            Document dd=mapLCShengyanTZInfo.get(strPID);
            if(strNewTime.compareTo(dd.getString("症状&体征时间"))>0 )
            {
                strNewTime=dd.getString("症状&体征时间");
            }
        }
        if(mapLCShengyanZDInfo.containsKey(strPID))
        {
            Document dd=mapLCShengyanZDInfo.get(strPID);
            if(strNewTime.compareTo(dd.getString("诊断时间"))>0 )
            {
                strNewTime=dd.getString("诊断时间");
            }
        }
        if(mapLCShengyanHYInfo.containsKey(strPID))
        {
            Document dd=mapLCShengyanHYInfo.get(strPID);
            if(strNewTime.compareTo(dd.getString("化验时间"))>0 )
            {
                strNewTime=dd.getString("化验时间");
            }
        }

        if(strNewTime.equals("first"))
            return "";
        return DateFormat.getDateFormatDay(strNewTime);
    }

    private void fillExcelTitle(SXSSFSheet sheet)
    {
       Row row = sheet.createRow(0);
       int cellNum=0;
       row.createCell(cellNum++).setCellValue("患者（PID）");
        row.createCell(cellNum++).setCellValue("最终分组");

        for (Map.Entry<String,ArrayList<String>> map:mapLeiJiFenZu.entrySet())
        {
            if(setDelist.contains("系统_"+map.getKey()))
                continue;
            row.createCell(cellNum++).setCellValue("系统_"+map.getKey());
        }
        for (Map.Entry<String,ArrayList<String>> map:mapLeiJiSubFenZu.entrySet())
        {
            if(setDelist.contains(map.getKey()))
                continue;
            row.createCell(cellNum++).setCellValue(map.getKey());
        }

    }
}
