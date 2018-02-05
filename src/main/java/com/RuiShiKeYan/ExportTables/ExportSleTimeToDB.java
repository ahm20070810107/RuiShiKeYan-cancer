package com.RuiShiKeYan.ExportTables;

import com.RuiShiKeYan.Common.Interface.IruiShiKeYan;
import com.RuiShiKeYan.Common.Method.LocalHostInfo;
import com.RuiShiKeYan.Common.Method.MongoDBHelper;
import com.RuiShiKeYan.Common.Method.ReadExcelToMap;
import com.RuiShiKeYan.Common.Method.RuiShiKeYan;
import com.RuiShiKeYan.SubMethod.GetLangChuangShenYanTime;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import com.RuiShiKeYan.SubMethod.getHDPInfo;

import java.util.HashMap;
import java.util.Map;

public class ExportSleTimeToDB  extends RuiShiKeYan implements IruiShiKeYan{


    private MongoCollection<Document> collectionDest;
    private MongoDatabase dbDest;
    private MongoClient mongoDest;
    GetLangChuangShenYanTime getLangChuangShenYanTime;
    private String Version="0.001";

    public static void main(String[] args) throws Exception
    {
        MongoDBHelper mongoDBHelper = new MongoDBHelper("HDP-live");
        MongoDatabase db= mongoDBHelper.getDb();

        IruiShiKeYan  iruiShiKeYan=new ExportSleTimeToDB();
        iruiShiKeYan.run(db);

        mongoDBHelper.closeMongoDb();
    }


    public void run(MongoDatabase db,Object[] args)
    {

        try {

            String fileName = LocalHostInfo.getPath() + "交付/首诊时间表.xlsx";
            InitReadWriteMongoDb();
            getLangChuangShenYanTime= new GetLangChuangShenYanTime();
            getLangChuangShenYanTime.fillBasicInfo(db);
            Map<String,JSONObject> mapSlePid= new HashMap<String,JSONObject>();
            ReadExcelToMap.readFromExcelToMap(mapSlePid,fileName,"患者（PID）", true);


            for(Map.Entry<String,JSONObject> mappid:mapSlePid.entrySet())
            {
                JSONObject jsonObject=mappid.getValue();
                Document docuPid= new Document();
                docuPid.put("Version",Version);
                docuPid.put("PID",mappid.getKey());
                docuPid.put("Sex",getSexMapping(getJSonValue(jsonObject,"性别")));
                docuPid.put("RaYYAgeGroup",getAgeGroup(getJSonValue(jsonObject,"诊断时间年减去出生年")));
                docuPid.put("SleTime",getJSonValue(jsonObject,"诊断时间天"));
                docuPid.put("LnTime",getLangChuangShenYanTime.getLCShenYanTime(mappid.getKey()));
                docuPid.put("BirthYear",getJSonValue(jsonObject,"出生年"));
                docuPid.put("DurationDay",jsonObject.getIntValue("病程天"));
                docuPid.put("FirstRecordTime",getHDPInfo.getFirstLastRIDDay(db,mappid.getKey(),false));
                docuPid.put("LastRecordTime",getJSonValue(jsonObject,"最晚记录时间天"));
                docuPid.put("Location",getJSonValue(jsonObject,"地域"));
                collectionDest.insertOne(docuPid);
                System.out.println(mappid.getKey());
            }
           mongoDest.close();
        }catch (Exception e){
            e.fillInStackTrace();
        }
    }
    private void InitReadWriteMongoDb()
    {
        mongoDest = new MongoClient(new MongoClientURI("mongodb://hm:eql-LmnZ8xc9pxbg@dds-bp1baad40c630d241.mongodb.rds.aliyuncs.com:3717/MDP-live"));
        dbDest=mongoDest.getDatabase("MDP-live");
        collectionDest = dbDest.getCollection("SlePidInfo");
    }
}
