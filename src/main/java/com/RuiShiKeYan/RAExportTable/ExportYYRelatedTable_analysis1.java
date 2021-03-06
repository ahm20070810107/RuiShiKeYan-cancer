package com.RuiShiKeYan.RAExportTable;

import com.RuiShiKeYan.Common.Interface.IruiShiKeYan;
import com.RuiShiKeYan.Common.Method.*;
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

//用药相关性分析表-d-Ta
public class ExportYYRelatedTable_analysis1 extends RuiShiKeYan implements IruiShiKeYan {

    private int Ta=1;
    private String[] strYYGroup={"英夫利西单抗","益赛普","甲氨蝶呤","来氟米特"};
    private  Map<String,ArrayList<String>> mapLeiJiSubFenZu;
    private    Map<String,Document> mapYY ;
    private    Map<String,Document> mapYYOne;
    private    Map<String,Document> mapZD;
    private    Map<String,Document> mapZZ ;
    private    Map<String,Document> mapTZ;
    private    Map<String,Document> mapHY ;
    private    Map<String,Document> mapHYRPG;
    private    Map<String,JSONObject> mapPIDInfo;
    public void run(MongoDatabase mdb, Object[] args) {
        try {
            YYRelatedTable yyRelatedTable =(YYRelatedTable) args[0];
            getBasicInfo(yyRelatedTable);
            for (String yyName:strYYGroup) {
                mapYYOne=new HashMap<String, Document>();
                getHDPInfo.getYYDay(mdb,mapYYOne,",'通用名':'"+yyName+"'");
                writeToExcel(mdb, mapPIDInfo, yyName,Ta);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void getSubAndItemMap(Map<String,ArrayList<String>> mapLeiJiSubFenZu) throws Exception
    {
        String fileName= LocalHostInfo.getPath()+ BaseInfo_Title_ListValue_DBCondition.strCLeiJiFenZuFileName;
        String tempFenZu,tempZuHe;
        JSONObject config = new JSONObject();
        config.put("filename", fileName);
        config.put("source_type", "excel");
        JSONObject document;
        DSExcelReader2 excelReader = new DSExcelReader2(config);
        while ((document = excelReader.nextDocument()) != null) {
            tempZuHe=getJSonValue(document,"表型名称")+getJSonValue(document,"标准标本");
            tempFenZu=getJSonValue(document,"子项");
            if(!tempFenZu.toUpperCase().equals("N")&&!tempFenZu.equals(""))
                if(mapLeiJiSubFenZu.containsKey(tempFenZu)) {
                    ArrayList arrayList=mapLeiJiSubFenZu.get(tempFenZu);
                    arrayList.add(tempZuHe);
                }
                else
                {
                    ArrayList arrayList=new ArrayList();
                    arrayList.add(tempZuHe);
                    mapLeiJiSubFenZu.put(tempFenZu,arrayList);
                }
        }
    }
    private void getBasicInfo(YYRelatedTable yyRelatedTable)
    {
        this.mapHY=yyRelatedTable.mapHY;
        this.mapHYRPG=yyRelatedTable.mapHYRPG;
        this.mapLeiJiSubFenZu=yyRelatedTable.mapLeiJiSubFenZu;
        this.mapPIDInfo=yyRelatedTable.mapPIDInfo;
        this.mapYY=yyRelatedTable.mapYY;
        this.mapTZ =yyRelatedTable.mapTZ;
        this.mapZZ=yyRelatedTable.mapZZ;
        this.mapZD=yyRelatedTable.mapZD;
    }
    private void  writeToExcel(MongoDatabase mdb,Map<String,JSONObject> mapPIDInfo,String yyName,int ta)
    {
        String title="患者（PID）,医院,性别,生产状况分组,地域,首诊时间天,首诊年龄,目标药物是否使用";

        SaveExcelTool saveExcelTool= new SaveExcelTool();
        SXSSFSheet sheet=saveExcelTool.getSheet("");
        int cellNum=saveExcelTool.fillExcelTitle(title);
        saveExcelTool.fillExcelTitle(mapLeiJiSubFenZu,cellNum,"",yyName);
        int rowNum=1;
        for(Map.Entry<String,JSONObject> mapPid:mapPIDInfo.entrySet())
         {
             JSONObject jsPidinfo= mapPid.getValue();
             if(ta !=0)
             {
                 int bcDay=Integer.valueOf(getJSonValue(jsPidinfo,"病程天")).intValue();
                 if(bcDay<ta*360)
                     continue;
             }
             System.out.println("用药相关性分析表:"+yyName+":"+rowNum);
             String strGCTime=DateFormat.getNextDay(getJSonValue(jsPidinfo,"诊断时间天"),ta*360);
             cellNum=0;
             Row row= sheet.createRow(rowNum++);
             row.createCell(cellNum++).setCellValue(mapPid.getKey());
             row.createCell(cellNum++).setCellValue(getJSonValue(jsPidinfo,"医院"));
             row.createCell(cellNum++).setCellValue(getJSonValue(jsPidinfo,"性别"));
             row.createCell(cellNum++).setCellValue(getJSonValue(jsPidinfo,"生产状况分组"));
             row.createCell(cellNum++).setCellValue(getJSonValue(jsPidinfo,"地域"));
             row.createCell(cellNum++).setCellValue(getJSonValue(jsPidinfo,"诊断时间天"));
             row.createCell(cellNum++).setCellValue(getJSonValue(jsPidinfo,"诊断时间年减去出生年"));
             row.createCell(cellNum++).setCellValue(getYYStatus(mapPid.getKey(),yyName,strGCTime));
             fillLeftCellValue(row,cellNum,mapPid.getKey(),strGCTime,yyName);
         }
         saveExcelTool.saveExcel("/交付/用药相关性分析表-"+yyName+"-"+ta+".xlsx");
    }

    private int getYYStatus(String strPid,String yyName,String gcTime)
    {
        ArrayList<String> arrayList=mapLeiJiSubFenZu.get(yyName);
        if(arrayList ==null)
            return 0;
        for (int i = 0; i <arrayList.size() ; i++) {
            String key=strPid+arrayList.get(i);
            Document document=mapYYOne.get(key);
            if(document== null)
                continue;
            if(get10JSonValue(document,"firstTime").compareTo(gcTime) <=0)
                return 1;
        }
        return 0;
    }
    private void fillLeftCellValue(Row row,int cellNum,String strPid,String strgcTime,String yyName)
    {
        for (Map.Entry<String,ArrayList<String>> map:mapLeiJiSubFenZu.entrySet())
        {
            if(yyName.equals(map.getKey()))
                continue;
            JSONObject dd= getSystemAndSubDay(strPid,map.getValue(),strgcTime,map.getKey());
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
                if (mapHYRPG.containsKey(strSrouce)) {
                    strTempTime=get10JSonValue(mapHYRPG.get(strSrouce),"化验时间");
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


}
