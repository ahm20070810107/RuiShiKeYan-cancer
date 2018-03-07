package com.RuiShiKeYan.RAExportTable;

import com.RuiShiKeYan.Common.Interface.IruiShiKeYan;
import com.RuiShiKeYan.Common.Method.DateFormat;
import com.RuiShiKeYan.Common.Method.LocalHostInfo;
import com.RuiShiKeYan.Common.Method.RuiShiKeYan;
import com.RuiShiKeYan.Common.Method.SaveExcelTool;
import com.RuiShiKeYan.RAExportTable.entity.YYRelatedTable;
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
 * Date:2018/1/2
 * Time:下午4:45
 */

//用药相关性分析表-分析3-d
public class ExportYYRelatedTable_analysis3 extends RuiShiKeYan implements IruiShiKeYan {

//    private String[] strYYGroup={"英夫利西单抗","益赛普","甲氨蝶呤","来氟米特"};
private String[] strYYGroup={"吗替麦考酚酯","艾拉莫德"};
    private  Map<String,ArrayList<String>> mapLeiJiSubFenZu;
    private    Map<String,Document> mapYY ;
    private    Map<String,Document> mapYYOne;
    private    Map<String,Document> mapZD;
    private    Map<String,Document> mapZZ ;
    private    Map<String,Document> mapTZ;
    private    Map<String,Document> mapHY ;
    private    Map<String,Document> mapHYRPG;
    private    Map<String,JSONObject> mapShouZhen;
    private    Map<String,JSONObject> mapPids = new HashMap<String, JSONObject>();

    public void run(MongoDatabase mdb, Object[] args) {
        try {
            String filePidName= LocalHostInfo.getPath()+"交付/分组用药情况表.xlsx";
            YYRelatedTable yyRelatedTable =(YYRelatedTable) args[0];
            getBasicInfo(yyRelatedTable,filePidName);
            for (String yyName:strYYGroup) {
                mapYYOne=new HashMap<String, Document>();
                getHDPInfo.getYYDay(mdb,mapYYOne,",'通用名':'"+yyName+"'");
                writeToExcel(mdb, mapPids, yyName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getBasicInfo(YYRelatedTable yyRelatedTable,String filePidName)
    {
        this.mapHY=yyRelatedTable.mapHY;
        this.mapHYRPG=yyRelatedTable.mapHYRPG;
        this.mapLeiJiSubFenZu=yyRelatedTable.mapLeiJiSubFenZu;
        this.mapShouZhen=yyRelatedTable.mapPIDInfo;
        this.mapYY=yyRelatedTable.mapYY;
        this.mapTZ =yyRelatedTable.mapTZ;
        this.mapZZ=yyRelatedTable.mapZZ;
        this.mapZD=yyRelatedTable.mapZD;

        getPids(filePidName);
    }
    private void getPids(String filePidName)
    {
        try {
            JSONObject config = new JSONObject();
            config.put("filename", filePidName);
            config.put("source_type", "excel");
            JSONObject document;
            DSExcelReader2 excelReader = new DSExcelReader2(config);
            while ((document = excelReader.nextDocument()) != null) {
                String strPid=getJSonValue(document,"PID");
                if (strPid.equals(""))
                    continue;
                if(mapPids.get(strPid) !=null)
                    continue;
                if(getJSonValue(document,"性别").equals("男") ||getJSonValue(document,"性别").contains("男"))
                    continue;
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("观察终点",getJSonValue(document,"观察终点"));
                jsonObject.put("初发年龄",getJSonValue(document,"初发年龄"));
                jsonObject.put("初发年龄分5组",getJSonValue(document,"初发年龄分5组"));
                mapPids.put(strPid,jsonObject);
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void  writeToExcel(MongoDatabase mdb,Map<String,JSONObject> mapPids,String yyName)
    {
        String title="患者（PID）,医院,性别,生产状况分组,初发年龄,初发年龄分5组,观察终点,最晚记录时间天,最晚记录时间天减去观察终点,目标药物是否使用";

        SaveExcelTool saveExcelTool= new SaveExcelTool();
        SXSSFSheet sheet=saveExcelTool.getSheet("");
        int cellNum=saveExcelTool.fillExcelTitle(title);
        saveExcelTool.fillExcelTitle(mapLeiJiSubFenZu,cellNum,"",yyName);
        int rowNum=1;
        for(Map.Entry<String,JSONObject> mapPid:mapPids.entrySet())
         {
             System.out.println(yyName+":" +rowNum);
             JSONObject jsPidinfo= mapPid.getValue();
             JSONObject jsShouZhen= mapShouZhen.get(mapPid.getKey());
             String strEndTime=getJSonValue(jsPidinfo,"观察终点");
             cellNum=0;
             Row row= sheet.createRow(rowNum++);
             row.createCell(cellNum++).setCellValue(mapPid.getKey());
             row.createCell(cellNum++).setCellValue(getJSonValue(jsShouZhen,"医院"));
             row.createCell(cellNum++).setCellValue(getJSonValue(jsShouZhen,"性别"));
             row.createCell(cellNum++).setCellValue(getJSonValue(jsShouZhen,"生产状况分组"));

             row.createCell(cellNum++).setCellValue(getJSonValue(jsPidinfo,"初发年龄"));
             row.createCell(cellNum++).setCellValue(getJSonValue(jsPidinfo,"初发年龄分5组"));
             row.createCell(cellNum++).setCellValue(strEndTime);
             row.createCell(cellNum++).setCellValue(getJSonValue(jsShouZhen,"最晚记录时间天"));
             if(strEndTime.equals("全病程"))
             {
                 row.createCell(cellNum++).setCellValue("全病程");
             }else{
                 row.createCell(cellNum++).setCellValue(DateFormat.getDays(getJSonValue(jsShouZhen,"最晚记录时间天"),strEndTime));
             }
             row.createCell(cellNum++).setCellValue(getYYStatus(mapPid.getKey(),yyName,strEndTime));
             fillLeftCellValue(row,cellNum,mapPid.getKey(),strEndTime,yyName);
         }
         saveExcelTool.saveExcel("/交付/用药相关性分析表-分析3-"+yyName+".xlsx");
    }

    private int getYYStatus(String strPid,String yyName,String strEndTime)
    {
        ArrayList<String> arrayList=mapLeiJiSubFenZu.get(yyName);
        if(arrayList ==null)
            return 0;
        for (int i = 0; i <arrayList.size() ; i++) {
            String key=strPid+arrayList.get(i);
            Document document=mapYYOne.get(key);
            if(document== null)
                continue;
            String strFirstTime=get10JSonValue(document,"firstTime");
            if(strEndTime.equals("全病程") ||strFirstTime.compareTo(strEndTime) <=0)
                return 1;
        }
        return 0;
    }
    private void fillLeftCellValue(Row row,int cellNum,String strPid,String strEndTime,String yyName)
    {
        for (Map.Entry<String,ArrayList<String>> map:mapLeiJiSubFenZu.entrySet())
        {
            if(yyName.equals(map.getKey()))
                continue;
            JSONObject dd= getSystemAndSubDay(strPid,map.getValue(),strEndTime,map.getKey());
            int result=1;
            if(getJSonValue(dd,"firstTime").equals("N")) {
                result=0;
            }
            row.createCell(cellNum++).setCellValue(result);
        }

    }


    /**
     *@return 返回Jsonobject的结果
     *@param strPid   pid的值
     *@param arrayList arrayList
     *@param strEndTime
     *@param strItem  子项的值
     */
    public  JSONObject getSystemAndSubDay(String strPid,ArrayList<String> arrayList,String strEndTime,String strItem)
    {
        String strFirstTime="N";
        String strTempTime;
        JSONObject jsonObject = new JSONObject();

        for (int i = 0; i < arrayList.size(); i++) {
            String strSrouce=strPid+arrayList.get(i);
            if(strItem.equals("肾炎")) {
                if (mapHYRPG.containsKey(strSrouce)) {
                    strTempTime=get10JSonValue(mapHYRPG.get(strSrouce),"化验时间");
                    if (getCompareResult(strTempTime,strFirstTime,strEndTime)) {
                        strFirstTime=strTempTime;
                    }
                }
            }
            else
            {
                if (mapHY.containsKey(strSrouce)) {
                    strTempTime=get10JSonValue(mapHY.get(strSrouce),"firstTime");
                    if (getCompareResult(strTempTime,strFirstTime,strEndTime)) {
                        strFirstTime = strTempTime;
                    }
                }
            }

            if (mapZZ.containsKey(strSrouce)) {
                strTempTime=get10JSonValue(mapZZ.get(strSrouce),"firstTime");
                if (getCompareResult(strTempTime,strFirstTime,strEndTime)) {
                    strFirstTime = strTempTime;
                }
            }

            if (mapTZ.containsKey(strSrouce)) {
                strTempTime=get10JSonValue(mapTZ.get(strSrouce),"firstTime");
                if (getCompareResult(strTempTime,strFirstTime,strEndTime)) {
                    strFirstTime = strTempTime;
                }
            }
            if (mapZD.containsKey(strSrouce)) {
                strTempTime=get10JSonValue(mapZD.get(strSrouce),"firstTime");
                if (getCompareResult(strTempTime,strFirstTime,strEndTime)) {
                    strFirstTime = strTempTime;
                }
            }
            if (mapYY.containsKey(strSrouce)) {
                strTempTime=get10JSonValue(mapYY.get(strSrouce),"firstTime");
//                if(get10JSonValue(mapYY.get(strSrouce),"lastTime").compareTo(strLastTime) >0)
//                    strLastTime=get10JSonValue(mapYY.get(strSrouce),"lastTime");
                if (getCompareResult(strTempTime,strFirstTime,strEndTime)) {
                    strFirstTime = strTempTime;
                }
            }

        }
        jsonObject.put("firstTime",strFirstTime);
//        jsonObject.put("lastTime",strLastTime);
        return jsonObject;
    }

    private boolean getCompareResult(String tempTime,String firstTime,String endTime)
    {
        return  firstTime.compareTo(tempTime) > 0 && (endTime.equals("全病程") ||tempTime.compareTo(endTime) <= 0);
    }
}
