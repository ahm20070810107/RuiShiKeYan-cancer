package com.RuiShiKeYan.ExportTables;


import com.RuiShiKeYan.Common.Interface.IruiShiKeYan;
import com.RuiShiKeYan.Common.Method.*;
import com.RuiShiKeYan.SubMethod.LangCShengYanYinShuPublicInfo;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.yiyihealth.beliefbase.chisquare.ChiSquare;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.bson.Document;
import test.java.task_SLE_LangChuang.BaseInfo_Title_ListValue_DBCondition;
import test.java.task_SLE_QueZzhengLeiJi.ExportTables_SLE;

import java.util.*;


/**
 * Created with IntelliJ IDEA
 * User:huangming
 * Date:2017/12/18
 * Time:下午7:54
 */
public class PhenomicMap_mutiFactor extends RuiShiKeYan implements IruiShiKeYan {
    LangCShengYanYinShuPublicInfo langCPublicInfo;
     //  int[] kGroup={0,1,2,3,4,5,6,7,8,9,10};
    int[] kGroup = {0,1};
    int kValue = 0;
    private String strVersion = "V0.006";
 //   private String pPort = "8201";
    private Map<String, Document> mapHYACR24DingXing = new HashMap<String, Document>();
    private Map<String, String> mapException = new HashMap<String, String>();
    private Map<String, ArrayList<String>> mapSubItemList = new HashMap<String, ArrayList<String>>();
    private Map<String, HashSet<String>> mapPxInclude; //p发生的人数列表
    private Map<String, HashSet<String>> mapPxExclude;//p不发生列表
    private Map<String, Map<String, String>> mapPIDPxFlag; //Pid发生的P列表
    private Map<String, Map<String, String>> mapCPXEntity = new HashMap<String, Map<String, String>>();
    private Map<String, JSONObject> mapPidInfo = new HashMap<String, JSONObject>(); //首诊时间表信息
    private MongoCollection<Document> collectionDest;
    private int icacCount=0;
    MongoDatabase dbDest;
    private MongoClient mongoDest;
    private boolean TaFlag = false;
//        private String[] sexGroup={""};
//    private String[] ageGroup={""};
    private String[] sexGroup = {"Male", "Female", ""};
    private String[] ageGroup = {"Child", "Adult", "Late", ""};

    public void run(MongoDatabase mdb, Object[] args) {
        try {
            String fileName = LocalHostInfo.getPath() + "交付/首诊时间表.xlsx";
            InitReadWriteMongoDb();
            kValue = Integer.valueOf(args[0].toString());
         //   pPort = args[1].toString();
            //    InitReadMongoDb();
            getBasicInfo(mdb);
            getSLEPhenomicMap(kGroup, fileName);

            if (mongoDest != null)
                mongoDest.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void InitReadMongoDb()
     {
//        mongoDest_srouce = new MongoClient(new MongoClientURI("mongodb://hm:eql-LmnZ8xc9pxbg@dds-bp1baad40c630d241.mongodb.rds.aliyuncs.com:3717/MDP-live"));
//     //   mongoDest_srouce = new MongoClient(new MongoClientURI("mongodb://webstats:lfweb7xff@121.196.244.147:3717/stats"));
//        dbDest_srouce=mongoDest_srouce.getDatabase("MDP-live");
//      //  collectionDest_srouce = dbDest.getCollection("CP");
     }
    private void InitReadWriteMongoDb()
    {
        mongoDest = new MongoClient(new MongoClientURI("mongodb://hm:eql-LmnZ8xc9pxbg@dds-bp1baad40c630d241.mongodb.rds.aliyuncs.com:3717/MDP-live"));
        dbDest=mongoDest.getDatabase("MDP-live");
    //    collectionDest = dbDest.getCollection("SLEPhenomicMap-Muti");
    }
    private void getSLEPhenomicMap(int[] kGroup,String fileName) throws Exception {

        ReadExcelToMap.readFromExcelToMap(mapPidInfo, fileName, "患者（PID）", true);

      //     for (int i = 0; i < kGroup.length; i++) {
               for (String strSex : sexGroup) {
                   for (String strAge : ageGroup) {
             //             kValue = kGroup[i];
                       TaFlag = kValue == 0 ? true : false;
                       collectionDest = dbDest.getCollection(getCollectionName(strSex, strAge, kValue));
                       getPxResult(kValue, strSex, strAge, mapPidInfo);//获得概率分布

                       icacCount=(int)(0.01*mapPIDPxFlag.size());  //计算
                       if(mapPIDPxFlag.size() < 100)
                           icacCount=(int)(0.05*mapPIDPxFlag.size());
                       System.out.println("icacCount:"+icacCount);
                       getTotalPIDCount(strSex,strAge,mapPIDPxFlag.size());
                       SaveExcelTool saveExcelTool = new SaveExcelTool();
                       SXSSFSheet sheet = saveExcelTool.getSheet("");
                       fillSLEPhenomicMapRow(sheet);
                       saveExcelTool.saveExcel("交付/组合因素及单因素卡方计算-" + kValue + "-" + strSex + "-" + strAge + "-" + mapPIDPxFlag.size() + ".xlsx");
                   }
               }
          // }
    }
    private void getTotalPIDCount(String strSex,String strAge,int count)
    {
        MongoCollection<Document>  collection = dbDest.getCollection("MDP_Statistic");
        Document document= new Document();
        document.put("Key","Muti_slePhenomicMap");
        document.put("Version",strVersion);
        document.put("Ta",kValue);
        document.put("Sex",strSex);
        document.put("ageGroup",strAge);
        document.put("PIDCount",count);
        collection.insertOne(document);
    }
    private String getCollectionName(String sex,String age,int kValue)
    {
        String strDbName="Muti_slePhenomicMap_"+kValue;
        if(!sex.equals(""))
            strDbName +="_"+sex;
        if(!age.equals(""))
            strDbName +="_"+age;
        return strDbName;
    }
 //   }
    private void getPxResult(int k,String strSex,String strAge,Map<String,JSONObject> mapPidInfo)
    {
        mapPxInclude= new HashMap<String, HashSet<String>>(); //p发生的人数列表
        mapPxExclude= new HashMap<String, HashSet<String>>();//p不发生列表
        mapPIDPxFlag= new HashMap<String, Map<String, String>>();

   //     fillMutiFactor(mapPidInfo,strSex,strAge,k);

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
             String agegroup = getAgeGroup(getJSonValue(jsonPid, "诊断时间年减去出生年"));
             if (sex.equals("") || agegroup.equals("异常") || strsleTime.equals(""))
                 continue;
             if(!strSex.equals("")&&!strSex.equals(sex))
                 continue;
             if(!strAge.equals("")&&!strAge.equals(agegroup))
                 continue;
             fillSingleFactor(strsleTime,mapPid.getKey());
         }
    }

    private void fillMutiFactor(Map<String,JSONObject> mapPidInfo,String strSex,String strAge,int k)
    {
         for(Map.Entry<String,Map<String,String>> mapEntity:mapCPXEntity.entrySet())
         {
             String strCxName=mapEntity.getKey();
             Map<String,String> mapPIdList= mapEntity.getValue();
             HashSet<String> setIncludePid= new HashSet<String>();
             HashSet<String> setExCludePID= new HashSet<String>();

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
                 String agegroup = getAgeGroup(getJSonValue(jsonPid, "诊断时间年减去出生年"));
                 if (sex.equals("") || agegroup.equals("异常") || strsleTime.equals(""))
                     continue;
                 if(!strSex.equals("")&&!strSex.equals(sex))
                     continue;
                 if(!strAge.equals("")&&!strAge.equals(agegroup))
                     continue;
                 if(mapPIdList.get(mapPid.getKey()) ==null)
                 {
                     setExCludePID.add(mapPid.getKey());
                 }else
                 {
                     String endTime=mapPIdList.get(mapPid.getKey());
                     if (strsleTime.compareTo(endTime) >= 0)
                     {
                         Map<String, String> tempMap = mapPIDPxFlag.get(mapPid.getKey());
                         if(tempMap == null)
                         {
                             tempMap = new HashMap<String, String>();
                             mapPIDPxFlag.put(mapPid.getKey(), tempMap);
                         }
                         tempMap.put(strCxName, endTime);

                         setIncludePid.add(mapPid.getKey());
                     }else
                     {
                         setExCludePID.add(mapPid.getKey());
                     }
                 }
             }
             mapPxInclude.put(strCxName,setIncludePid);
             mapPxExclude.put(strCxName,setExCludePID);
         }
    }
    private void fillSingleFactor(String strsleTime,String strPid)
    {
        for (Map.Entry<String, ArrayList<String>> mapSub : mapSubItemList.entrySet()) {
            boolean flag = false;
            String firstTime = "N";
            for (int i = 0; i < mapSub.getValue().size(); i++) {

                String entityName =strPid + mapSub.getValue().get(i);

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
//                if (langCPublicInfo.mapFirstYY.containsKey(entityName)) {
//                    String temptime = get10JSonValue(langCPublicInfo.mapFirstYY.get(entityName), "用药时间");
//                    if (strsleTime.compareTo(temptime) >= 0) {
//                        if (firstTime.compareTo(temptime) > 0)
//                            firstTime = temptime;
//                        flag = true;
//                    }
 //               }
            }
            if (flag)  //发现子项有值
            {
                fillStringArrayListMap(mapPxInclude,  mapSub.getKey(), strPid);
                fillStringMapMap(mapPIDPxFlag, strPid, mapSub.getKey(), firstTime);
                //     flagPx=true;
            } else {
                fillStringArrayListMap(mapPxExclude,  mapSub.getKey(), strPid);
            }
        }
    }
    private void fillSLEPhenomicMapRow(SXSSFSheet sheet)
    {
        fillPhenomicMapTitle(sheet);
        int rowNum=1;
        for(Map.Entry<String,ArrayList<String>> map:mapSubItemList.entrySet())
        {
            rowNum=fillTheRowValue(sheet,map.getKey(),rowNum);
        }
//        for(Map.Entry<String,Map<String,String>> map:mapCPXEntity.entrySet())
//        {
//            rowNum=fillTheRowValue(sheet,map.getKey(),rowNum);
//        }
    }
    private int fillTheRowValue(SXSSFSheet sheet,String pxRow,int rowNum)
    {
        System.out.println("Row:"+rowNum);
        Row row = sheet.createRow(rowNum++);
        JSONArray jsonArrayP= new JSONArray();//用数组传值请求卡方
        ArrayList<Document> jsonArrayS= new ArrayList<Document>();//用于存入数据库

        fillPhenomicCell(row,pxRow,jsonArrayP,jsonArrayS);

        //计算卡方
//        jsonArrayP= ChiSquare2.calculate(jsonArrayP);
//        fillPKaFangValue(row,jsonArrayP,jsonArrayS);
        collectionDest.insertMany(jsonArrayS);   //插入数据库
        return rowNum;
    }
    private  void fillPKaFangValue(Row row,JSONArray jsonArrayP,ArrayList<Document> jsonArrayS)
    {
       int length=mapSubItemList.size();//+mapCPXEntity.size();
        for (int i = 0; i < length ; i++) {
            Double value=jsonArrayP.getDoubleValue(i);
          //  row.createCell((i+1)*5).setCellValue(value);

            Document jsonObject = jsonArrayS.get(i);  //插入数据库的卡方
            jsonObject.put("P",value);
        }
    }
    private void fillPhenomicCell(Row row,String pxRow,JSONArray jsonArrayP,ArrayList<Document> jsonArrayS)
    {
        int cellNum=1;
        row.createCell(0).setCellValue(pxRow);
//        for(Map.Entry<String,Map<String,String>> map:mapCPXEntity.entrySet())
//        {
//            cellNum=fillCellValue(row,pxRow,map.getKey(),cellNum,jsonArrayP,jsonArrayS);
//        }
        for(Map.Entry<String,ArrayList<String>> map:mapSubItemList.entrySet())
        {
            cellNum=fillCellValue(row,pxRow,map.getKey(),cellNum,jsonArrayP,jsonArrayS);
        }
    }

    private int fillCellValue(Row row,String pxRow,String pxCloumn,
                              int cellNum,JSONArray jsonArrayP,ArrayList<Document> jsonArrayS)
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
//        row.createCell(cellNum++).setCellValue(pxCR_y);
//        row.createCell(cellNum++).setCellValue(getPercent(pxCR_y,pxC_y));
//        row.createCell(cellNum++).setCellValue(pxCR_n);
//        row.createCell(cellNum++).setCellValue(getPercent(pxCR_n,pxC_n));
//        cellNum++;// 给p值留位置


        Document jsonS= new Document();
        if(pxCR_y<icacCount||pxCR_n<icacCount||pxC_y-pxCR_y<icacCount || pxC_n-pxCR_n <icacCount ) {
            jsonS.put("P",null);
        }else
        {

            double pValue = ChiSquare.calculate((double) pxCR_y, (double) pxCR_n, (double) (pxC_y - pxCR_y), (double) (pxC_n - pxCR_n));
            jsonS.put("P",pValue);
        }

        jsonS.put("Y",pxCR_y);
        jsonS.put("Y-Total",pxC_y);
        jsonS.put("Y-Percent",getPercent(pxCR_y,pxC_y));
        jsonS.put("N",pxCR_n);
        jsonS.put("N-Total",pxC_n);
        jsonS.put("N-Percent",getPercent(pxCR_n,pxC_n));
        jsonS.put("SubS",getCPList(pxCloumn));
        jsonS.put("SubS-Rel",getCPList(pxRow));
        jsonS.put("Version",strVersion);
        jsonS.put("Ta",kValue);

        jsonArrayS.add(jsonS);  //用于存入数据库
//        JSONObject params= new JSONObject();
//        params.put("p1", (double)pxCR_y);
//        params.put("p2", (double)pxCR_n);
//        params.put("p3", (double)(pxC_y-pxCR_y));
//        params.put("p4", (double) (pxC_n-pxCR_n));
//        jsonArrayP.add(params);
        return cellNum;
    }
    private ArrayList<String> getCPList(String srouce)
    {
         ArrayList<String> arrayList= new ArrayList<String>();
     //    srouce=srouce.replace("--","");
         String[] values=srouce.split(",");
        for (String str:values) {
           if(!str.equals(""))
               arrayList.add(str);
        }
        return arrayList;
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
            if (mapPIDPxFlag.get(strPid) != null) {
                Map<String, String> mapPID = mapPIDPxFlag.get(strPid);
          //      if (mapPID.get(pxRow) != null && (TaFlag || mapPID.get(pxRow).equals("") ||
           //             mapPID.get(pxCloumn).equals("") || mapPID.get(pxRow).compareTo(mapPID.get(pxCloumn)) <= 0))
            if (mapPID.get(pxRow) != null )
                count++;
            }
        }
        return count;
    }
    private void fillPhenomicMapTitle(SXSSFSheet sheet)
    {
        Row row=sheet.createRow(0);
        int cellNum=1;
//        for(Map.Entry<String,Map<String,String>> map:mapCPXEntity.entrySet())
//        {
//            cellNum=fillCellTitle(row,cellNum,map.getKey());
//        }
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

    private void getBasicInfo(MongoDatabase mdb)
    {
        langCPublicInfo = new LangCShengYanYinShuPublicInfo();
        try {
            String fileYCFileName= LocalHostInfo.getPath()+"交付/移除组PID列表.xlsx";
            ReadExcelToMap.readFromExcelToMap(mapException, fileYCFileName, "PID");
            ReadExcelToMap.getLeiJiFenZu("条件概率组合子项",mapSubItemList);
       //     getCPXEntity();  //加载多因素表信息
            langCPublicInfo.getFirstADIDay(mdb);
            langCPublicInfo.getFirstHYDay(mdb, mapHYACR24DingXing, "'标准化验名':{$in:['24小时尿蛋白定量试验','尿蛋白定性试验']},'RPG科研结果转换':'阳性',");
            langCPublicInfo.getFirstHYDay(mdb, langCPublicInfo.mapFirstHY, "'化验结果定性（新）':'阳性',");
            langCPublicInfo.getFirstTZDay(mdb);
            langCPublicInfo.getFirstZZDay(mdb);
         //   langCPublicInfo.getFirstYYDay(mdb);  2017-12-26去除用药
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    private void getCPXEntity()
    {
       MongoCollection<Document> collectionDest_srouce = dbDest.getCollection("CP");
        MongoCursor<Document> cursor=collectionDest_srouce.find(Document.parse("{}")).iterator();

        while (cursor.hasNext()) {
            Document dd= cursor.next();
            ArrayList<String> CPlist=(ArrayList<String>)dd.get("CP");
            String keyValue="";
            for(String str:CPlist)
            {
              keyValue +=","+str;
            }
            keyValue=keyValue.substring(1,keyValue.length());

            ArrayList<Document> arrayList=(ArrayList<Document>) dd.get("patientInfo");
            Map<String,String> mapPidTime= new HashMap<String, String>();

            for(Document document:arrayList)
            {
                mapPidTime.put(getJSonValue(document,"PID"),getJSonValue(document,"endTime"));
            }

            mapCPXEntity.put(keyValue,mapPidTime);
        }
    }

}
