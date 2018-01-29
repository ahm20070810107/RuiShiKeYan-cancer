package com.RuiShiKeYan.ExportTables;


import com.RuiShiKeYan.Common.Interface.IruiShiKeYan;
import com.RuiShiKeYan.Common.Method.LocalHostInfo;
import com.RuiShiKeYan.Common.Method.RuiShiKeYan;
import com.RuiShiKeYan.Common.Method.SaveExcelTool;
import com.alibaba.fastjson.JSONArray;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.bson.Document;
import com.alibaba.fastjson.JSONObject;
import com.RuiShiKeYan.SubMethod.*;
import com.RuiShiKeYan.Common.Method.ReadExcelToMap;
//import com.RuiShiKeYan.Common.Method.ChiSquare;
import com.RuiShiKeYan.Common.Method.DateFormat;
import test.java.task_SLE_LangChuang.BaseInfo_Title_ListValue_DBCondition;
import test.java.task_SLE_QueZzhengLeiJi.ExportTables_SLE;

import java.util.*;


/**
 * Created with IntelliJ IDEA
 * User:huangming
 * Date:2017/12/18
 * Time:下午7:54
 */
public class ExportACR1997andPhenomicMap extends RuiShiKeYan implements IruiShiKeYan {
    LangCShengYanYinShuPublicInfo langCPublicInfo;
    int[] kGroup={0,1,2,3,4,5,6,7,8,9,10};
  //   int[] kGroup={0};
    int kValue=0;
    private String strVersion="V0.002";
    private Map<String,Document> mapHYACR24DingXing = new HashMap<String, Document>();
    private Map<String, JSONObject> mapPIDBasicInfo=new HashMap<String, JSONObject>();
    private Map<String,String> mapException= new HashMap<String, String>();
    private Map<String,ArrayList<String>> map1997ACRList=new HashMap<String, ArrayList<String>>();
    private Map<String,ArrayList<String>> mapSubItemList=new HashMap<String, ArrayList<String>>();
    private MongoCollection<Document> collectionDest;
    private MongoClient mongoDest;
    private boolean TaFlag=false;
    private String[] sexGroup={"Male","Female"};
    private String[] ageGroup={"Child","Adult","Late"};
    public  void run(MongoDatabase mdb,Object[] args)
    {
        try {
        String flag=(String) args[0];
        String strZDCondition = "{'诊断时间':{$exists:true,$regex:/^.{9,}$/},'标准诊断名':'类风湿性关节炎','诊断状态':'是'," + BaseInfo_Title_ListValue_DBCondition.ZD13SLE + "}";

        String fileName=LocalHostInfo.getPath()+ "交付/首诊时间表.xlsx";
        InitMongoDb();
        getBasicInfo(mdb);
        if(flag.equals("true")) {
            writeACR1997(); //首诊时间表-ACR1997
            ExportTables_SLE.getZDPID(mdb,strZDCondition);
            ExportTables_SLE.ExportADI(mdb, strZDCondition);  //首诊时间表
        }
        map1997ACRList=null; //释放内存
        mapPIDBasicInfo=null;//释放内存
        langCPublicInfo.getFirstYYDay(mdb);

        getSLEPhenomicMap(kGroup,fileName);

        if(mongoDest !=null)
            mongoDest.close();
        }
       catch (Exception e){e.printStackTrace();}
    }
    private void InitMongoDb()
    {
        mongoDest = new MongoClient(new MongoClientURI("mongodb://rzt:Nytr28xbr-zlrYdc@dds-bp1f7b56b50093041.mongodb.rds.aliyuncs.com:3717/saga-test2"));
        MongoDatabase dbDest=mongoDest.getDatabase("saga-test2");
        collectionDest = dbDest.getCollection("SLEPhenomicMap-Single");
    }
    private void getSLEPhenomicMap(int[] kGroup,String fileName) throws Exception
    {
        Map<String,JSONObject> mapPidInfo= new HashMap<String, JSONObject>(); //首诊时间表信息
        ReadExcelToMap.readFromExcelToMap(mapPidInfo,fileName,"患者（PID）",true);

        for (int i = 0; i < kGroup.length; i++) {
            kValue=kGroup[i];
            if(kValue == 0)
                TaFlag=true;
            else{ TaFlag=false;}
            Map<String,HashSet<String>> mapPxInclude= new HashMap<String, HashSet<String>>(); //p发生的人数列表
            Map<String,HashSet<String>> mapPxExclude= new HashMap<String, HashSet<String>>();//p不发生列表
            Map<String,Map<String,String>> mapPIDPxFlag= new HashMap<String, Map<String, String>>();
            getPxResult(kValue,mapPxInclude,mapPxExclude,mapPIDPxFlag,mapPidInfo);//获得概率分布
            System.out.println("total:"+mapPIDPxFlag.size());
            SaveExcelTool saveExcelTool = new SaveExcelTool();
            SXSSFSheet sheet = saveExcelTool.getSheet("");
            fillSLEPhenomicMapRow(sheet,mapPxInclude,mapPxExclude,mapPIDPxFlag);
            saveExcelTool.saveExcel("交付/SLEPhenomicMap-分析1-单因素-"+kValue+"-"+mapPIDPxFlag.size()+".xlsx");
        }
    }

    private void getPxResult(int k,Map<String,HashSet<String>> mapPxInclude,Map<String,HashSet<String>> mapPxExclude,
                             Map<String,Map<String,String>> mapPIDPxFlag,Map<String,JSONObject> mapPidInfo)
    {

       // System.out.println("mapPidInfo:"+mapPidInfo.size());
         for(Map.Entry<String,JSONObject> mapPid:mapPidInfo.entrySet()) {

             JSONObject jsonPid = mapPid.getValue();
             String sex = getSexMapping(getJSonValue(jsonPid, "性别"));
             String strBCTime = getJSonValue(jsonPid, "病程天");
             String strsleTime;
             if (TaFlag) {
                 strsleTime = "N";  //赋值为N使所有日期都小于其时间
             } else {
                 strsleTime = DateFormat.getNextDay(getJSonValue(jsonPid, "诊断时间天"), k * 360);
                 if (strBCTime.equals("") || Integer.valueOf(strBCTime).intValue() < k * 360) {
                     continue;
                 }
             }
             String ageGroup = getAgeGroup(getJSonValue(jsonPid, "诊断时间年减去出生年"));
             if (sex.equals("") || ageGroup.equals("异常") || strsleTime.equals(""))
                 continue;

             //  boolean flagPx=true;
             for (Map.Entry<String, ArrayList<String>> mapSub : mapSubItemList.entrySet()) {

                 boolean flag = false;
                 String firstTime = "N";
                 for (int i = 0; i < mapSub.getValue().size(); i++) {

                     String entityName = mapPid.getKey() + mapSub.getValue().get(i);

                     if (langCPublicInfo.mapFirstHY.containsKey(entityName)) {
                         String temptime = get10JSonValue(langCPublicInfo.mapFirstHY.get(entityName), "化验时间");
                         if (strsleTime.compareTo(temptime) >= 0) {
                             if (firstTime.compareTo(temptime) > 0)
                                 firstTime = temptime;
                             flag = true;
                         }
                     }
                     if (langCPublicInfo.mapFirstZD.containsKey(entityName)) {
                         String temptime = get10JSonValue(langCPublicInfo.mapFirstZD.get(entityName), "诊断时间");

                         if (strsleTime.compareTo(temptime) >= 0) {
                             if (firstTime.compareTo(temptime) > 0)
                                 firstTime = temptime;
                             flag = true;
                         }
                     }
                     if (langCPublicInfo.mapFirstZZ.containsKey(entityName)) {
                         String temptime = get10JSonValue(langCPublicInfo.mapFirstZZ.get(entityName), "症状&体征时间");
                         if (strsleTime.compareTo(temptime) >= 0) {
                             if (firstTime.compareTo(temptime) > 0)
                                 firstTime = temptime;
                             flag = true;
                         }
                     }
                     if (langCPublicInfo.mapFirstTZ.containsKey(entityName)) {
                         String temptime = get10JSonValue(langCPublicInfo.mapFirstTZ.get(entityName), "症状&体征时间");
                         if (strsleTime.compareTo(temptime) >= 0) {
                             if (firstTime.compareTo(temptime) > 0)
                                 firstTime = temptime;
                             flag = true;
                         }
                     }
                     if (langCPublicInfo.mapFirstYY.containsKey(entityName)) {
                         String temptime = get10JSonValue(langCPublicInfo.mapFirstYY.get(entityName), "用药时间");
                         if (strsleTime.compareTo(temptime) >= 0) {
                             if (firstTime.compareTo(temptime) > 0)
                                 firstTime = temptime;
                             flag = true;
                         }
                     }
                 }
                 if (flag)  //发现子项有值
                 {
                     fillStringArrayListMap(mapPxInclude, mapSub.getKey(), mapPid.getKey());
                     fillStringMapMap(mapPIDPxFlag, mapPid.getKey(), mapSub.getKey(), firstTime);
                     //     flagPx=true;
                 } else {
                     fillStringArrayListMap(mapPxExclude, mapSub.getKey(), mapPid.getKey());
                 }

             }
             fillStringArrayListMap(mapPxInclude, sex, mapPid.getKey());
             fillStringMapMap(mapPIDPxFlag, mapPid.getKey(), sex, "");
             fillStringArrayListMap(mapPxInclude, ageGroup, mapPid.getKey());
             fillStringMapMap(mapPIDPxFlag, mapPid.getKey(), ageGroup, "");
             if (!sex.equals("Male")) {
                 fillStringArrayListMap(mapPxExclude, "Male", mapPid.getKey());
             }
             //女
             if (!sex.equals("Female")) {
                 fillStringArrayListMap(mapPxExclude, "Female", mapPid.getKey());
             }
             //child
             if (!ageGroup.equals("Child")) {
                 fillStringArrayListMap(mapPxExclude, "Child", mapPid.getKey());
             }
             //Adult
             if (!ageGroup.equals("Adult")) {
                 fillStringArrayListMap(mapPxExclude, "Adult", mapPid.getKey());
             }
             //Late
             if (ageGroup.equals("Late")) {
                 fillStringArrayListMap(mapPxExclude, "Late", mapPid.getKey());
             }
         }
    }

    private void fillSLEPhenomicMapRow(SXSSFSheet sheet,Map<String,HashSet<String>> mapPxInclude,Map<String,HashSet<String>> mapPxExclude,
                                       Map<String,Map<String,String>> mapPIDPxFlag)
    {
        fillPhenomicMapTitle(sheet);
        int rowNum=1;
        for (int i = 0; i < sexGroup.length; i++) {
            rowNum=dealTheRowValue(sheet,sexGroup[i],mapPxInclude,mapPxExclude,mapPIDPxFlag,rowNum);
        }
        for (int i = 0; i < ageGroup.length; i++) {
            rowNum=dealTheRowValue(sheet,ageGroup[i],mapPxInclude,mapPxExclude,mapPIDPxFlag,rowNum);
        }
        for(Map.Entry<String,ArrayList<String>> map:mapSubItemList.entrySet())
        {
            rowNum=dealTheRowValue(sheet,map.getKey(),mapPxInclude,mapPxExclude,mapPIDPxFlag,rowNum);
        }
    }
    private int dealTheRowValue(SXSSFSheet sheet,String pxRow,Map<String,HashSet<String>> mapPxInclude,Map<String,HashSet<String>> mapPxExclude,
                                 Map<String,Map<String,String>> mapPIDPxFlag,int rowNum)
    {
        System.out.println("Row:"+rowNum);
        Row row = sheet.createRow(rowNum++);
        JSONArray jsonArrayP= new JSONArray();//用数组传值请求卡方
        ArrayList<Document> jsonArrayS= new ArrayList<Document>();//用于存入数据库
        fillPhenomicCell(row,pxRow,mapPxInclude,mapPxExclude,mapPIDPxFlag,jsonArrayP,jsonArrayS);
      //  jsonArrayP=ChiSquare.calculate(jsonArrayP);
        fillPKaFangValue(row,jsonArrayP,jsonArrayS);
        collectionDest.insertMany(jsonArrayS);   //插入数据库
        return rowNum;
    }
    private  void fillPKaFangValue(Row row,JSONArray jsonArrayP,ArrayList<Document> jsonArrayS)
    {
       int length=sexGroup.length+ageGroup.length+mapSubItemList.size();
        for (int i = 0; i < length ; i++) {
            Double value=jsonArrayP.getDoubleValue(i);
            row.createCell((i+1)*5).setCellValue(value);

            Document jsonObject = jsonArrayS.get(i);
            jsonObject.put("P",value);
        }
    }
    private void fillPhenomicCell(Row row,String pxRow,Map<String,HashSet<String>> mapPxInclude,Map<String,HashSet<String>> mapPxExclude,
                                  Map<String,Map<String,String>> mapPIDPxFlag,JSONArray jsonArrayP,ArrayList<Document> jsonArrayS)
    {
        int cellNum=1;
        row.createCell(0).setCellValue(pxRow);
        for (int i = 0; i < sexGroup.length; i++) {
            cellNum=fillCellValue(row,pxRow,sexGroup[i],mapPxInclude,mapPxExclude,mapPIDPxFlag,cellNum,jsonArrayP,jsonArrayS);
        }
        for (int i = 0; i < ageGroup.length; i++) {
            cellNum=fillCellValue(row,pxRow,ageGroup[i],mapPxInclude,mapPxExclude,mapPIDPxFlag,cellNum,jsonArrayP,jsonArrayS);
        }
        for(Map.Entry<String,ArrayList<String>> map:mapSubItemList.entrySet())
        {
            cellNum=fillCellValue(row,pxRow,map.getKey(),mapPxInclude,mapPxExclude,mapPIDPxFlag,cellNum,jsonArrayP,jsonArrayS);
        }
    }

    private int fillCellValue(Row row,String pxRow,String pxCloumn,Map<String,HashSet<String>> mapPxInclude,Map<String,HashSet<String>> mapPxExclude,
                               Map<String,Map<String,String>> mapPIDPxFlag,int cellNum,JSONArray jsonArrayP,ArrayList<Document> jsonArrayS)
    {
        int pxC_y=0,pxCR_y=0,pxC_n=0,pxCR_n=0;

        if(mapPxInclude.get(pxCloumn) !=null) {
            pxC_y =mapPxInclude.get(pxCloumn).size();
            pxCR_y = getPxRowNum(pxRow, mapPxInclude.get(pxCloumn), mapPIDPxFlag,pxCloumn);
        }
        if(mapPxExclude.get(pxCloumn) !=null) {
            int[] tempCount=getPxRowNum_n(pxRow, mapPxExclude.get(pxCloumn), mapPIDPxFlag);
            pxC_n = tempCount[0]+tempCount[1];
            pxCR_n =tempCount[0];
        }

        row.createCell(cellNum++).setCellValue(pxCR_y);
        row.createCell(cellNum++).setCellValue(getPercent(pxCR_y,pxC_y));
        row.createCell(cellNum++).setCellValue(pxCR_n);
        row.createCell(cellNum++).setCellValue(getPercent(pxCR_n,pxC_n));
        cellNum++;// 给p值留位置

        Document jsonS= new Document();
        jsonS.put("Y",pxCR_y);
        jsonS.put("Y-Percent",getPercent(pxCR_y,pxC_y));
        jsonS.put("N",pxCR_n);
        jsonS.put("N-Percent",getPercent(pxCR_n,pxC_n));
        jsonS.put("SubS",pxCloumn);
        jsonS.put("SubS-Rel",pxRow);
        jsonS.put("Version",strVersion);
        jsonS.put("Ta",kValue);
        jsonArrayS.add(jsonS);  //用于存入数据库

        JSONObject params= new JSONObject();
        params.put("p1", (double)pxCR_y);
        params.put("p2", (double)pxCR_n);
        params.put("p3", (double)(pxC_y-pxCR_y));
        params.put("p4", (double) (pxC_n-pxCR_n));
        jsonArrayP.add(params);
    //   Double pvalue=ChiSquare.calculate((double)pxCR_y,(double)pxCR_n,(double)(pxC_y-pxCR_y),(double) (pxC_n-pxCR_n));
  //采用上面数组传值请求p的卡方
  //     System.out.println("pxRow:"+pxRow+" pxCloumn:"+pxCloumn+" "+(double)pxCR_y+","+(double)pxCR_n+","+(double)(pxC_y-pxCR_y)+","+(double) (pxC_n-pxCR_n) +" pvalue："+pvalue);
   //    row.createCell(cellNum++).setCellValue(pvalue);
   //     row.createCell(cellNum++).setCellValue("");

        return cellNum;
    }
    private  int[] getPxRowNum_n(String pxRow,HashSet<String> setPid,Map<String,Map<String,String>> mapPIDPxFlag)
    {
        int[] tempCount= new int[2];
        int count1=0,count2=0;
        for (String strPid: setPid) {
          if(mapPIDPxFlag.get(strPid) !=null) {
              if (mapPIDPxFlag.get(strPid).get(pxRow) != null)
                  count1++;
              else
                  count2++;
          }
        }
        tempCount[0]=count1;
        tempCount[1]=count2;
        return tempCount;
    }

    private  int getPxRowNum(String pxRow,HashSet<String> setPid,Map<String,Map<String,String>> mapPIDPxFlag,String pxCloumn)
    {
        int count=0;
        for (String strPid: setPid) {
          if(mapPIDPxFlag.get(strPid) !=null)
            if(mapPIDPxFlag.get(strPid).get(pxRow)!=null &&(TaFlag||pxCloumn.equals("类风湿性关节炎")||mapPIDPxFlag.get(strPid).get(pxRow).equals("")||
              mapPIDPxFlag.get(strPid).get(pxCloumn).equals("")|| mapPIDPxFlag.get(strPid).get(pxRow).compareTo(mapPIDPxFlag.get(strPid).get(pxCloumn))<=0))
                count++;
        }

        return count;
    }
    private void fillPhenomicMapTitle(SXSSFSheet sheet)
    {
        Row row=sheet.createRow(0);
        int cellNum=1;
        for (int i = 0; i < sexGroup.length; i++) {
            cellNum=fillCellTitle(row,cellNum,sexGroup[i]);
        }
        for (int i = 0; i < ageGroup.length; i++) {
            cellNum=fillCellTitle(row,cellNum,ageGroup[i]);
        }
        for(Map.Entry<String,ArrayList<String>> map:mapSubItemList.entrySet())
        {
            cellNum=fillCellTitle(row,cellNum,map.getKey());
        }
    }
    private int fillCellTitle(Row row,int cellNum,String Value)
    {
        row.createCell(cellNum++).setCellValue(Value+"-Y");
        row.createCell(cellNum++).setCellValue(Value+"-Y%");
        row.createCell(cellNum++).setCellValue(Value+"-N");
        row.createCell(cellNum++).setCellValue(Value+"-N%");
        row.createCell(cellNum++).setCellValue(Value+"-P");
        return cellNum;
    }

     private void writeACR1997()
     {
         SaveExcelTool saveExcelTool = new SaveExcelTool();
         SXSSFSheet sheet= saveExcelTool.getSheet("");
         int cellNum=saveExcelTool.fillExcelTitle("医院,患者（PID）,出生年,性别,地域,诊断时间天,病历（RID）,诊断表型名称,诊断时间年减去出生年");
         fillLeftColumn(sheet,cellNum);
         fillACR1997Row(sheet);
         saveExcelTool.saveExcel("交付/首诊时间表-ACR1997.xlsx");
     }


    private void fillACR1997Row(SXSSFSheet sheet)
    {
        int rowNum=1;
        for (Map.Entry<String,JSONObject> map:mapPIDBasicInfo.entrySet())
        {
            String strPID=map.getKey();
            JSONObject jsonObject=map.getValue();
            Map<String,JSONObject> mapJsonResult=new HashMap<String, JSONObject>();
            Map<String,JSONObject> mapFenZuResult=new HashMap<String, JSONObject>();
            ArrayList<String> arrayList=getResultACR1997(strPID,map1997ACRList,mapJsonResult,mapFenZuResult);
            if(arrayList.size()<4)
                continue;
            Collections.sort(arrayList);
            String strResultTime= arrayList.get(3);
            JSONObject jsonResult=mapJsonResult.get(strResultTime) ;//取第4位的时间对应的信息
            strResultTime=DateFormat.getDateFormatDay(strResultTime);  //取十位
            int cellNum=0;
            Row row =sheet.createRow(rowNum ++);
            System.out.println("首诊时间表-ACR1997:"+rowNum);
            row.createCell(cellNum++).setCellValue(getJSonValue(jsonObject,"医院"));
            row.createCell(cellNum++).setCellValue(strPID);
            row.createCell(cellNum++).setCellValue(getJSonValue(jsonObject,"出生年"));
            row.createCell(cellNum++).setCellValue(getJSonValue(jsonObject,"性别"));
            row.createCell(cellNum++).setCellValue(getJSonValue(jsonObject,"地域"));
            row.createCell(cellNum++).setCellValue(strResultTime);
            row.createCell(cellNum++).setCellValue(getJSonValue(jsonResult,"RID"));
            row.createCell(cellNum++).setCellValue(getJSonValue(jsonResult,"表型名称"));
            try {
                row.createCell(cellNum++).setCellValue(getAge(strResultTime.substring(0,4),getJSonValue(jsonObject,"出生年")));
            }catch (Exception e){e.printStackTrace();}
            for(Map.Entry<String,ArrayList<String>> mapCloumn:map1997ACRList.entrySet())
            {
                if(mapFenZuResult.containsKey(mapCloumn.getKey()))
                {
                    String strTime=mapFenZuResult.get(mapCloumn.getKey()).getString("时间天");
                   row.createCell(cellNum++).setCellValue(DateFormat.getDateFormatDay(strTime));
                }
                else
                {
                    row.createCell(cellNum++).setCellValue("");
                }
            }

        }
    }

    private ArrayList<String> getResultACR1997(String strPID,Map<String,ArrayList<String>> map1997ACR,Map<String,JSONObject> mapJsonResult ,Map<String,JSONObject> mapFenZuResult)
    {
        ArrayList<String> arrayList= new ArrayList<String>();
        for (Map.Entry<String,ArrayList<String>> map:map1997ACR.entrySet())
        {
            String firstTime="w";
            JSONObject jsonObject= new JSONObject();
            ArrayList<String> ar1997ACR= map.getValue();
            for (int i = 0; i <ar1997ACR.size() ; i++) {
                String entityName=strPID+ar1997ACR.get(i);
                if (ar1997ACR.get(i).equals("24小时尿蛋白定量试验") ||ar1997ACR.get(i).equals("尿蛋白定性试验")) //化验
                {
                    if(mapHYACR24DingXing.containsKey(entityName))
                    {
                       if(firstTime.compareTo(getJSonValue(mapHYACR24DingXing.get(entityName),"化验时间")) >0)
                       {
                           firstTime=getJSonValue(mapHYACR24DingXing.get(entityName),"化验时间");
                           jsonObject.put("RID",getJSonValue(mapHYACR24DingXing.get(entityName),"RID"));
                           jsonObject.put("表型名称",ar1997ACR.get(i));
                           jsonObject.put("时间天",firstTime);
                       }
                    }
                 //   continue;
                }else
                {
                    if(langCPublicInfo.mapFirstHY.containsKey(entityName))
                    {
                        if(firstTime.compareTo(getJSonValue(langCPublicInfo.mapFirstHY.get(entityName),"化验时间")) >0)
                        {
                            firstTime=getJSonValue(langCPublicInfo.mapFirstHY.get(entityName),"化验时间");
                            jsonObject.put("RID",getJSonValue(langCPublicInfo.mapFirstHY.get(entityName),"RID"));
                            jsonObject.put("表型名称",ar1997ACR.get(i));
                            jsonObject.put("时间天",firstTime);
                        }
                    }
                }
                if(langCPublicInfo.mapFirstZD.containsKey(entityName))
                {
                    if(firstTime.compareTo(getJSonValue(langCPublicInfo.mapFirstZD.get(entityName),"诊断时间")) >0)
                    {
                        firstTime=getJSonValue(langCPublicInfo.mapFirstZD.get(entityName),"诊断时间");
                        jsonObject.put("RID",getJSonValue(langCPublicInfo.mapFirstZD.get(entityName),"RID"));
                        jsonObject.put("表型名称",ar1997ACR.get(i));
                        jsonObject.put("时间天",firstTime);
                    }
                }
                if(langCPublicInfo.mapFirstZZ.containsKey(entityName))
                {
                    if(firstTime.compareTo(getJSonValue(langCPublicInfo.mapFirstZZ.get(entityName),"症状&体征时间")) >0)
                    {
                        firstTime=getJSonValue(langCPublicInfo.mapFirstZZ.get(entityName),"症状&体征时间");
                        jsonObject.put("RID",getJSonValue(langCPublicInfo.mapFirstZZ.get(entityName),"RID"));
                        jsonObject.put("表型名称",ar1997ACR.get(i));
                        jsonObject.put("时间天",firstTime);
                    }
                }
                if(langCPublicInfo.mapFirstTZ.containsKey(entityName))
                {
                    if(firstTime.compareTo(getJSonValue(langCPublicInfo.mapFirstTZ.get(entityName),"症状&体征时间")) >0)
                    {
                        firstTime=getJSonValue(langCPublicInfo.mapFirstTZ.get(entityName),"症状&体征时间");
                        jsonObject.put("RID",getJSonValue(langCPublicInfo.mapFirstTZ.get(entityName),"RID"));
                        jsonObject.put("表型名称",ar1997ACR.get(i));
                        jsonObject.put("时间天",firstTime);
                    }
                }

            }
            if(!firstTime.equals("w"))
            {
                arrayList.add(firstTime);
                mapJsonResult.put(firstTime,jsonObject);
                mapFenZuResult.put(map.getKey(),jsonObject);
            }
        }
        return arrayList;
    }

    private void fillLeftColumn(SXSSFSheet sheet,int cellNum)
    {
        Row row=sheet.getRow(0);
        for(Map.Entry<String,ArrayList<String>> map:map1997ACRList.entrySet())
        {
            row.createCell(cellNum++).setCellValue(map.getKey());
        }
    }
    private void getBasicInfo(MongoDatabase mdb)
    {
        langCPublicInfo = new LangCShengYanYinShuPublicInfo();
        try {
            String fileYCFileName= LocalHostInfo.getPath()+"交付/移除组PID列表.xlsx";
            String fileBasicInfo=LocalHostInfo.getPath()+"交付/PID验证列表.xlsx";
            langCPublicInfo.getFirstADIDay(mdb);
            langCPublicInfo.getFirstHYDay(mdb, mapHYACR24DingXing, "'标准化验名':{$in:['24小时尿蛋白定量试验','尿蛋白定性试验']},'RPG科研结果转换':'阳性',");
            langCPublicInfo.getFirstHYDay(mdb, langCPublicInfo.mapFirstHY, "'化验结果定性（新）':'阳性',");
            langCPublicInfo.getFirstTZDay(mdb);
            langCPublicInfo.getFirstZZDay(mdb);
            ReadExcelToMap.readFromExcelToMap(mapException, fileYCFileName, "PID");
            ReadExcelToMap.readFromExcelToMap(mapPIDBasicInfo,fileBasicInfo,"患者（PID）",mapException);
            ReadExcelToMap.getLeiJiFenZu("1997ACR",map1997ACRList);
            ReadExcelToMap.getLeiJiFenZu("条件概率子项",mapSubItemList);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}
