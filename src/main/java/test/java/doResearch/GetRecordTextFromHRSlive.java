package test.java.doResearch;

import com.RuiShiKeYan.Common.Method.LocalHostInfo;
import com.RuiShiKeYan.Common.Method.MongoDBHelper;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.yiyihealth.data.DaX.reader.DSExcelReader2;
import org.bson.Document;
import java.io.*;

/**
 * Created with IntelliJ IDEA
 * User:huangming
 * Date:2017/12/15
 * Time:下午3:05
 */
public class GetRecordTextFromHRSlive {
    static  JSONObject document;
    public static void main(String[] args) throws Exception
    {
        MongoDBHelper mongoDBHelper1 = new MongoDBHelper("HDP-live");
        MongoDBHelper mongoDBHelper2 = new MongoDBHelper("HRS-live");
        MongoDatabase dbp=mongoDBHelper1.getDb();
        MongoDatabase dbr=mongoDBHelper2.getDb();

        writeToFile(dbp,dbr);
        mongoDBHelper1.closeMongoDb();
        mongoDBHelper2.closeMongoDb();
    }
    public static String getJSonValue(JSONObject jsonObject, String key)
    {
        if(jsonObject == null || key == null)return "";
        if(jsonObject.getString(key) ==null)
            return "";
        return jsonObject.getString(key);
    }
    public static String getJSonValue(Document jsonObject, String key)
    {
        if(jsonObject == null || key == null)return "";
        if(jsonObject.getString(key) ==null)
            return "";
        return jsonObject.getString(key);
    }
    private static void writeToFile(MongoDatabase dbp,MongoDatabase dbr) throws Exception
    {
        String fileName= LocalHostInfo.getPath()+"PIDRID信息表-100个pid.xlsx";
        JSONObject config = new JSONObject();
        config.put("filename", fileName);
        config.put("source_type", "excel");

        DSExcelReader2 excelReader = new DSExcelReader2(config);
        while ((document = excelReader.nextDocument()) != null) {
            String strPid=getJSonValue(document,"PID");
            String strRid=getJSonValue(document,"RID");
            String strPPID=getJSonValue(document,"projectProcessId");
            String strRecordTime=getRecoedTime(dbp,strPid,strRid,strPPID);
            String strRecordInfo=getRecordInfo(dbr,strRid);
            saveFile(strPid,strRecordTime,strRid,strRecordInfo);
        }
    }

    private static void saveFile(String pid,String recordTime,String rid,String recordInfo) throws Exception
    {
        String dir= LocalHostInfo.getPath()+"交付/Record";
        File file=new File(dir, pid+"-"+recordTime+"-"+rid+".txt");
        if(!file.exists()){
           file.getParentFile().mkdirs();
            try {
                file.createNewFile();
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
        }
        FileOutputStream fOutputStream=new FileOutputStream(file);
        OutputStreamWriter writer=new OutputStreamWriter(fOutputStream);
        writer.append(recordInfo);
        writer.close();
        fOutputStream.close();
    }

    private static String getRecordInfo(MongoDatabase dbr,String RID)
    {
        MongoCollection<Document> mc = dbr.getCollection("Record");
        MongoCursor<Document> cursor= mc.find(Document.parse("{'_id':'"+RID+"'}")).limit(1).iterator();
       if(cursor.hasNext())
       {
           Document dd=(Document)cursor.next().get("info");
           if(getJSonValue(dd,"textARS").equals(""))
             return   getJSonValue(dd,"text");
           return getJSonValue(dd,"textARS");
       }
       return "";
    }
    private static String getRecoedTime(MongoDatabase dbp,String pid,String rid,String PPID)
    {
        MongoCollection<Document> mc = dbp.getCollection("ARB");
        MongoCursor<Document> cursor= mc.find(Document.parse("{'projectProcessId':"+PPID+",'PID':'"+pid+
                "','RID':'"+rid+"'}")).limit(1).iterator();
        if(cursor.hasNext())
        {
            String reslt= getJSonValue(cursor.next(),"记录时间戳");
            if(reslt.length() >10)
                return reslt.substring(0,10);
            return reslt;
        }
     return "";

    }
}
