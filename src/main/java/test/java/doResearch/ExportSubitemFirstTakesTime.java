package test.java.doResearch;

import com.RuiShiKeYan.Common.Interface.IruiShiKeYan;
import com.RuiShiKeYan.Common.Method.*;
import com.RuiShiKeYan.SubMethod.LangCShengYanYinShuPublicInfo;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA
 * User:huangming
 * Date:2017/12/21
 * Time:上午10:20
 */

//导出子项发生的时间

public class ExportSubitemFirstTakesTime extends RuiShiKeYan implements IruiShiKeYan{

    String strVersion="V0.002";
     Map<String,JSONObject> mapPidInfo= new HashMap<String, JSONObject>(); //验证时间信息
     Map<String,String> mapExcept=new HashMap<String, String>();
     Map<String,ArrayList<String>> mapSubItemList=new HashMap<String, ArrayList<String>>();
     LangCShengYanYinShuPublicInfo langCPublicInfo;
    private String[] sexGroup={"Male","Female"};
    private String[] ageGroup={"Child","Adult","Late"};


    public static void main(String[] args)
    {
        try {

            MongoDBHelper mongoDBHelper = new MongoDBHelper("HDP-live");
            MongoDatabase dbhost = mongoDBHelper.getDb();
            IruiShiKeYan ruiShiKeYan = new ExportSubitemFirstTakesTime();
            ruiShiKeYan.run(dbhost);
            mongoDBHelper.closeMongoDb();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run(MongoDatabase dbhost,Object ... args)
    {
   try {
       MongoClient mongoDest = new MongoClient(new MongoClientURI("mongodb://hm:eql-LmnZ8xc9pxbg@dds-bp1baad40c630d241.mongodb.rds.aliyuncs.com:3717/MDP-live"));
       MongoDatabase dbDest=mongoDest.getDatabase("MDP-live");
       MongoCollection<Document> collectionDest = dbDest.getCollection("EntityFirstTime");

       getBasicInfo(dbhost);

       getAndWriteResult(collectionDest);
       mongoDest.close();
   }catch (Exception e){e.printStackTrace();}


    }

    private  void getAndWriteResult(MongoCollection<Document> collectionDest)
    {
        Map<String,Map<String,String>> mapPIDPxFlag= new HashMap<String, Map<String, String>>();
        getPxResult(mapPIDPxFlag,mapPidInfo);//获得概率分布
        writeToMongoDB(mapPIDPxFlag,collectionDest);

    }
    private void writeToMongoDB(Map<String,Map<String,String>> mapPIDPxFlag,MongoCollection<Document> collectionDest)
    {   int rowNum=1;
        for (Map.Entry<String,Map<String,String>> mapPid:mapPIDPxFlag.entrySet())
        {
           Document document=new Document();
           document.put("Version",strVersion);
           document.put("PID",mapPid.getKey());
           Map<String,String> mapdetail=mapPid.getValue();
            for (int i = 0; i < sexGroup.length; i++) {
               if(mapdetail.get(sexGroup[i]) !=null)
               {
                   document.put("Sex",sexGroup[i]);
               }
            }
            for (int i = 0; i < ageGroup.length; i++) {
                if(mapdetail.get(ageGroup[i]) !=null)
                {
                    document.put("ageGroup",ageGroup[i]);
                }
            }

            for(Map.Entry<String,ArrayList<String>> maparr:mapSubItemList.entrySet())
            {
                if(mapdetail.containsKey(maparr.getKey()) ) {
                    document.put(maparr.getKey(), mapdetail.get(maparr.getKey()));
                }else
                {
                    document.put(maparr.getKey(),"");
                }
            }
            System.out.println("Insert:"+rowNum++);
            collectionDest.insertOne(document);
        }
    }
    private void getPxResult(Map<String,Map<String,String>> mapPIDPxFlag, Map<String,JSONObject> mapPidInfo)
    {

        for(Map.Entry<String,JSONObject> mapPid:mapPidInfo.entrySet()) {

            JSONObject jsonPid = mapPid.getValue();
            String sex = getSexMapping(getJSonValue(jsonPid, "性别"));
       //     String strBCTime=getJSonValue(jsonPid,"病程天");
        //    String strsleTime= DateFormat.getNextDay(getJSonValue(jsonPid,"诊断时间天"),k*360);
            String ageGroup = getAgeGroup(getJSonValue(jsonPid, "诊断时间年减去出生年"));

            for (Map.Entry<String, ArrayList<String>> mapSub : mapSubItemList.entrySet()) {

                boolean flag=false;
                String firstTime="N";
                for (int i = 0; i < mapSub.getValue().size(); i++) {

                    String entityName=mapPid.getKey()+mapSub.getValue().get(i);

                    if (langCPublicInfo.mapFirstHY.containsKey(entityName)) {
                        String temptime=get10JSonValue(langCPublicInfo.mapFirstHY.get(entityName), "化验时间");
                            if(firstTime.compareTo(temptime) >0)
                                firstTime=temptime;
                            flag=true;
                    }
                    if (langCPublicInfo.mapFirstZD.containsKey(entityName)) {
                        String temptime=get10JSonValue(langCPublicInfo.mapFirstZD.get(entityName), "诊断时间");
                            if(firstTime.compareTo(temptime) >0)
                                firstTime=temptime;
                            flag=true;
                    }
                    if (langCPublicInfo.mapFirstZZ.containsKey(entityName)) {
                        String temptime=get10JSonValue(langCPublicInfo.mapFirstZZ.get(entityName), "症状&体征时间");
                            if(firstTime.compareTo(temptime) >0)
                                firstTime=temptime;
                            flag=true;
                    }
                    if (langCPublicInfo.mapFirstTZ.containsKey(entityName)) {
                        String temptime=get10JSonValue(langCPublicInfo.mapFirstTZ.get(entityName), "症状&体征时间");
                   //    if (strsleTime.compareTo(temptime) >= 0) {
                            if(firstTime.compareTo(temptime) >0)
                                firstTime=temptime;
                            flag=true;
                     //   }
                    }
//                    if (langCPublicInfo.mapFirstYY.containsKey(entityName)) {
//                        String temptime=get10JSonValue(langCPublicInfo.mapFirstYY.get(entityName), "用药时间");
//                            if(firstTime.compareTo(temptime) >0)
//                                firstTime=temptime;
//                            flag=true;
//                    }
                }
                if(flag)  //发现子项有值
                {
                    fillStringMapMap(mapPIDPxFlag,mapPid.getKey(),mapSub.getKey(),firstTime);
                    //     flagPx=true;
                }
            }
             fillStringMapMap(mapPIDPxFlag, mapPid.getKey(), sex, "");
             fillStringMapMap(mapPIDPxFlag, mapPid.getKey(), ageGroup, "");
        }
    }
    private  void getBasicInfo(MongoDatabase mdb) throws Exception
    {
        String fileNameInfo= LocalHostInfo.getPath()+ "交付/首诊时间表.xlsx";
        String fileExcept=LocalHostInfo.getPath()+ "交付/移除组PID列表.xlsx";
        ReadExcelToMap.readFromExcelToMap(mapExcept,fileExcept,"患者（PID）");
        ReadExcelToMap.readFromExcelToMap(mapPidInfo,fileNameInfo,"患者（PID）",mapExcept);
        langCPublicInfo = new LangCShengYanYinShuPublicInfo();

        langCPublicInfo.getFirstHYDay(mdb, langCPublicInfo.mapFirstHY, "'化验结果定性（新）':'阳性',");
        langCPublicInfo.getFirstADIDay(mdb);
        langCPublicInfo.getFirstTZDay(mdb);
        langCPublicInfo.getFirstZZDay(mdb);
        //   langCPublicInfo.getFirstYYDay(mdb);  2017-12-26去除用药
        ReadExcelToMap.getLeiJiFenZu("条件概率组合子项",mapSubItemList);
    }
}
