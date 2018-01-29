package test.java.task_SLE_QueZzhengLeiJi;

import com.RuiShiKeYan.Common.Method.DateFormat;
import com.RuiShiKeYan.Common.Method.LocalHostInfo;
import com.RuiShiKeYan.Common.Method.SaveExcelTool;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.yiyihealth.data.DaX.reader.DSExcelReader2;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.bson.Document;
import test.java.task_SLE_LangChuang.BaseInfo_Title_ListValue_DBCondition;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA
 * User:huangming
 * Date:2017/10/13
 * Time:下午10:01
 */
public class ExportTables_SLE {

    static   Map<String,String> mapTZ ;
    static   Map<String,String> mapZZ ;
    static   Map<String,String> mapHY ;
    static   Map<String,String> mapZDPID = new HashMap<String, String>();
    static   Map<String,JSONObject> mapAge=new HashMap<String, JSONObject>();
    static   Map<String,String > mapSleBxianXiTongFenzu;
    public static void  getZDPID(MongoDatabase db,String strZDCondition)throws Exception
    {
        if(mapAge.size()<1)
           getBasicPidInfo(mapAge);
        MongoCollection<Document> mc = db.getCollection("ADI");


        ArrayList<Document> aggregates = new ArrayList<Document>();
        aggregates.add(new Document("$match",Document.parse(strZDCondition)));
    //    aggregates.add(new Document("$sort",Document.parse("{'PID':1,'诊断时间':1}")));
        aggregates.add(new Document("$group",Document.parse("{'_id':{'PID':'$PID'}, '首发诊断':{'$first':'$$ROOT'}}")));
        MongoCursor<Document> cursor =mc.aggregate(aggregates).allowDiskUse(true).iterator();
      //  Map<String,JSONObject> mapExceptPID = ExportQueZhenTable.fillExceptPID();
        while(cursor.hasNext()) {

            Document dd=(Document) cursor.next().get("首发诊断");
            mapZDPID.put(dd.getString("PID"),"0");
        }
    }

    public static  void ExportALA(MongoDatabase db,  String strALAConditon) throws Exception
    {
        int RowNum=1;
        Document dd=null;
        SXSSFWorkbook sxssfWorkbook = new SXSSFWorkbook(2000);
        SXSSFSheet sheet = sxssfWorkbook.createSheet();
        fillExcelTitle(sheet,"医院,科室,患者（PID）,病历（RID）,上下文,标准标本,化验组样本,化验名称,化验名称样本,化验结果（定量）,化验单位,化验结果（定性）,化验时间,标准化验名,化验结果定性（新）,化验锚点,化验时间天");

        MongoCollection<Document> mc = db.getCollection("ALA");

        ArrayList<Document> aggregates = new ArrayList<Document>();
        aggregates.add(new Document("$match",Document.parse(strALAConditon)));
        aggregates.add(new Document("$sort",Document.parse("{'PID':1,'化验时间':1}")));
        aggregates.add(new Document("$group",Document.parse("{'_id':{'PID':'$PID'}, '排序结果':{'$push':'$$ROOT'}}")));
        MongoCursor<Document> cursor =mc.aggregate(aggregates).allowDiskUse(true).iterator();

        while(cursor.hasNext()) {
            dd = cursor.next();
            Document aPid = (Document) dd.get("_id");
            if (mapZDPID.get(aPid.getString("PID")) == null)
                continue;
            ArrayList<Document> arResult = (ArrayList<Document>) dd.get("排序结果");
            int startRecord = -1;
            for (int i = 0; i < arResult.size(); i++) {
                dd=arResult.get(i);
                String tempHYZH =dd.getString("标准化验名")+dd.getString("标准标本");

                if (mapHY.get(tempHYZH) == null) {
                    startRecord = i;
                    break;
                }
            }
            if(startRecord == -1)
                continue;
            dd = arResult.get(startRecord);
            for(int i=startRecord;i<arResult.size();i++)  //获取已标注的第一条
            {
                Document document=arResult.get(i);
                String tempHYZH =document.getString("标准化验名")+document.getString("标准标本");
                if(mapSleBxianXiTongFenzu.containsKey(tempHYZH) && document.getString("化验时间").equals(dd.getString("化验时间")))
                {startRecord = i;break;}
                if(!document.getString("化验时间").equals(dd.getString("化验时间")))
                    break;
            }
            dd = arResult.get(startRecord);
            Row row = sheet.createRow(RowNum++);
            System.out.println("HY首发表:" + RowNum);
            row.createCell(0).setCellValue(mapAge.get(dd.getString("PID")).getString("医院"));
            if (dd.containsKey("科室"))
                row.createCell(1).setCellValue(dd.get("科室").toString());
            if (dd.containsKey("PID"))
                row.createCell(2).setCellValue(dd.get("PID").toString());
            if (dd.containsKey("RID"))
                row.createCell(3).setCellValue(dd.get("RID").toString());
            if (dd.containsKey("上下文"))
                row.createCell(4).setCellValue(dd.get("上下文").toString());
            if (dd.containsKey("标准标本"))
                row.createCell(5).setCellValue(dd.get("标准标本").toString());
            if (dd.containsKey("化验组样本"))
                row.createCell(6).setCellValue(dd.get("化验组样本").toString());
            if (dd.containsKey("化验名称"))
                row.createCell(7).setCellValue(dd.get("化验名称").toString());
            if (dd.containsKey("化验名称样本"))
                row.createCell(8).setCellValue(dd.get("化验名称样本").toString());
            if (dd.containsKey("化验结果（定量）"))
                row.createCell(9).setCellValue(dd.get("化验结果（定量）").toString());
            if (dd.containsKey("化验单位"))
                row.createCell(10).setCellValue(dd.get("化验单位").toString());
            if (dd.containsKey("化验结果（定性）"))
                row.createCell(11).setCellValue(dd.get("化验结果（定性）").toString());
            if (dd.containsKey("化验时间"))
                row.createCell(12).setCellValue(dd.get("化验时间").toString());
            if (dd.containsKey("标准化验名"))
                row.createCell(13).setCellValue(dd.get("标准化验名").toString());
            if (dd.containsKey("化验结果定性（新）"))
                row.createCell(14).setCellValue(dd.get("化验结果定性（新）").toString());
            if (dd.containsKey("段落标题"))
                row.createCell(15).setCellValue(dd.get("段落标题").toString());
            if (dd.containsKey("化验时间"))
                row.createCell(16).setCellValue(DateFormat.getDateFormatDay(dd.get("化验时间").toString()));
        }
        FileOutputStream fileOutputStream = new FileOutputStream(LocalHostInfo.getPath()+"化验首发表.xlsx");
        sxssfWorkbook.write(fileOutputStream);
        sxssfWorkbook.close();
        fileOutputStream.close();
    }
    //体征首发表
    public static  void ExportASYTZ(MongoDatabase db, String strTZCondition) throws Exception {
        int RowNum = 1;
        Document dd = null;
        SXSSFWorkbook sxssfWorkbook = new SXSSFWorkbook(2000);
        SXSSFSheet sheet = sxssfWorkbook.createSheet();
        fillExcelTitle(sheet,"医院,科室,患者（PID）,病历（RID）,症状&体征时间,体征锚点,体征时间天,体征组合");

        MongoCollection<Document> mc = db.getCollection("ASY");
        ArrayList<Document> aggregates = new ArrayList<Document>();
        aggregates.add(new Document("$match", Document.parse(strTZCondition)));
        aggregates.add(new Document("$sort", Document.parse("{'PID':1,'症状&体征时间':1}")));
        aggregates.add(new Document("$group", Document.parse("{\"_id\":{\"PID\":\"$PID\"}, \"排序结果\":{\"$push\":'$$ROOT'}}")));
        MongoCursor<Document> cursor = mc.aggregate(aggregates).allowDiskUse(true).iterator();


        while (cursor.hasNext()) {
            dd = cursor.next();
            //判断是否属于诊断PID
            Document aPid = (Document) dd.get("_id");
            if (mapZDPID.get(aPid.getString("PID")) == null)
                continue;
            ArrayList<Document> arResult = (ArrayList<Document>) dd.get("排序结果");

            int startRecord = -1;

            for (int i = 0; i < arResult.size(); i++) {
                dd=arResult.get(i);
                String tempTZZH = dd.get("部位1").toString() +dd.getString("否定词") +dd.get("体征").toString() + dd.get("体征定性描述").toString()+
                        dd.getString("体征定量描述")+dd.getString("体征定量单位");
                if (mapTZ.get(tempTZZH) == null) {
                    startRecord = i;
                    break;
                }
            }
            if(startRecord == -1)
                continue;
            dd = arResult.get(startRecord);

            for(int i=startRecord;i<arResult.size();i++)  //获取已标注的第一条
            {
                Document document=arResult.get(i);
                String tempTZZH = document.get("部位1").toString()+dd.getString("否定词") + document.get("体征").toString() + document.get("体征定性描述").toString()+
                                  document.getString("体征定量描述")+document.getString("体征定量单位");
                if(mapSleBxianXiTongFenzu.containsKey(tempTZZH) && document.getString("症状&体征时间").equals(dd.getString("症状&体征时间")))
                {startRecord = i;break;}
                if(!document.getString("症状&体征时间").equals(dd.getString("症状&体征时间")))
                    break;
            }
            dd = arResult.get(startRecord);

            Row row = sheet.createRow(RowNum++);
            System.out.println("ProcessTZ:"+RowNum);

            String tempStr = "";
            row.createCell(0).setCellValue(mapAge.get(dd.getString("PID")).getString("医院"));
            if (dd.containsKey("科室"))
                row.createCell(1).setCellValue(dd.get("科室").toString());
            if (dd.containsKey("PID"))
                row.createCell(2).setCellValue(dd.get("PID").toString());
            if (dd.containsKey("RID"))
                row.createCell(3).setCellValue(dd.get("RID").toString());
            if (dd.containsKey("部位1")) {
                tempStr += dd.get("部位1").toString();
            }
            if (dd.containsKey("否定词")) {
                tempStr += dd.get("否定词").toString();
            }
            if (dd.containsKey("体征")) {
                tempStr += dd.get("体征").toString();
            }
            if (dd.containsKey("体征定性描述")) {
                tempStr += dd.get("体征定性描述").toString();
            }
            if (dd.containsKey("症状&体征时间"))
                row.createCell(4).setCellValue(dd.get("症状&体征时间").toString());
            if (dd.containsKey("段落标题"))
                row.createCell(5).setCellValue(dd.get("段落标题").toString());
            if(dd.containsKey("体征定量描述"))
                tempStr+=dd.getString("体征定量描述");
            if(dd.containsKey("体征定量单位"))
                tempStr+=dd.getString("体征定量单位");
            row.createCell(6).setCellValue(DateFormat.getDateFormatDay(dd.get("症状&体征时间").toString()));
            row.createCell(7).setCellValue(tempStr);
        }
        FileOutputStream fileOutputStream = new FileOutputStream(LocalHostInfo.getPath()+"体征首发表.xlsx");
        sxssfWorkbook.write(fileOutputStream);
        sxssfWorkbook.close();
        fileOutputStream.close();
    }



    //症状首发
    public static  void ExportASYZZ( MongoDatabase db, String strZZCondition) throws Exception {
        int RowNum = 1;
        Document dd = null;
        SXSSFWorkbook sxssfWorkbook = new SXSSFWorkbook(2000);
        SXSSFSheet sheet = sxssfWorkbook.createSheet();
        fillExcelTitle(sheet,"医院,科室,患者（PID）,病历（RID）,症状&体征时间,症状锚点,症状时间天,症状组合");
        MongoCollection<Document> mc = db.getCollection("ASY");
        ArrayList<Document> aggregates = new ArrayList<Document>();
        aggregates.add(new Document("$match", Document.parse(strZZCondition)));
        aggregates.add(new Document("$sort", Document.parse("{'PID':1,'症状&体征时间':1}")));
        aggregates.add(new Document("$group", Document.parse("{\"_id\":{\"PID\":\"$PID\"}, \"排序结果\":{\"$push\":'$$ROOT'}}")));
        MongoCursor<Document> cursor = mc.aggregate(aggregates).allowDiskUse(true).iterator();

        String tempStr;
        while (cursor.hasNext()) {
            dd = cursor.next();
            //判断是否属于诊断PID
            Document aPid = (Document) dd.get("_id");
            if (mapZDPID.get(aPid.getString("PID")) == null)
                continue;
            ArrayList<Document> arResult = (ArrayList<Document>) dd.get("排序结果");

            int startRecord = -1;

            for (int i = 0; i < arResult.size(); i++) {
                dd=arResult.get(i);
                String tempZZZH = dd.get("部位1").toString() + dd.get("症状1").toString();

                if (mapZZ.get(tempZZZH) == null) {
                    startRecord = i;
                    break;
                }
            }
            if(startRecord == -1)
                continue;
            System.out.println("症状首诊：" + RowNum);
            dd = arResult.get(startRecord);

            for(int i=startRecord;i<arResult.size();i++)  //获取已标注的第一条
            {
                Document document=arResult.get(i);
                String tempZZZH = document.get("部位1").toString() + document.get("症状1").toString();
                if(mapSleBxianXiTongFenzu.containsKey(tempZZZH) && document.getString("症状&体征时间").equals(dd.getString("症状&体征时间")))
                {startRecord = i;break;}
                if(!document.getString("症状&体征时间").equals(dd.getString("症状&体征时间")))
                    break;
            }
            dd = arResult.get(startRecord);

            Row row = sheet.createRow(RowNum++);
            tempStr = "";
            row.createCell(0).setCellValue(mapAge.get(dd.getString("PID")).getString("医院"));
            if (dd.containsKey("科室"))
                row.createCell(1).setCellValue(dd.get("科室").toString());
            if (dd.containsKey("PID"))
                row.createCell(2).setCellValue(dd.get("PID").toString());
            if (dd.containsKey("RID"))
                row.createCell(3).setCellValue(dd.get("RID").toString());
            if (dd.containsKey("部位1")) {
                tempStr = dd.get("部位1").toString();
            }
            if (dd.containsKey("症状1")) {
                tempStr += dd.get("症状1").toString();
            }
            if (dd.containsKey("症状&体征时间"))
                row.createCell(4).setCellValue(dd.get("症状&体征时间").toString());

            if (dd.containsKey("段落标题"))
                row.createCell(5).setCellValue(dd.get("段落标题").toString());
            row.createCell(6).setCellValue(DateFormat.getDateFormatDay(dd.get("症状&体征时间").toString()));
            row.createCell(7).setCellValue(tempStr);
        }
        FileOutputStream fileOutputStream = new FileOutputStream(LocalHostInfo.getPath()+"症状首发表.xlsx");
        sxssfWorkbook.write(fileOutputStream);
        sxssfWorkbook.close();
        fileOutputStream.close();
    }

    public static Map<String,String > getExcludeInfo() throws Exception {

        String fileTZ = LocalHostInfo.getPath()+"初发表现标注表-体征.xlsx";
        String fileZZ = LocalHostInfo.getPath()+"初发表现标注表-症状.xlsx";
        String fileHY = LocalHostInfo.getPath()+"初发表现标注表-化验.xlsx";
        JSONObject document = null;
        mapSleBxianXiTongFenzu= new HashMap<String, String>();
        mapTZ = new HashMap<String, String>();
        mapZZ = new HashMap<String, String>();
        mapHY = new HashMap<String, String>();

        JSONObject config = new JSONObject();
        config.put("filename", fileTZ);
        config.put("source_type", "excel");
       try {
           DSExcelReader2 excelReader = new DSExcelReader2(config);
           while ((document = excelReader.nextDocument()) != null) {

               if (document.getString("SLE表现系统分组").toUpperCase().equals("N") || document.getString("SLE表现系统分组").equals(""))
                   mapTZ.put(document.get("体征组合").toString(), "0");
               else if (!document.get("体征组合").toString().equals(""))
                   mapSleBxianXiTongFenzu.put(document.get("体征组合").toString(), document.get("SLE表现系统分组").toString());
           }

           config.put("filename", fileZZ);
           excelReader = new DSExcelReader2(config);
           while ((document = excelReader.nextDocument()) != null) {
               if (document.getString("SLE表现系统分组").toUpperCase().equals("N") || document.getString("SLE表现系统分组").equals(""))
                   mapZZ.put(document.get("症状组合").toString(), "0");
               else if (!document.get("症状组合").toString().equals(""))
                   mapSleBxianXiTongFenzu.put(document.get("症状组合").toString(), document.get("SLE表现系统分组").toString());
           }
           config.put("filename", fileHY);
           excelReader = new DSExcelReader2(config);
           while ((document = excelReader.nextDocument()) != null) {
               String tempStr = "";
               if (document.get("标准化验名") != null)
                   tempStr = document.get("标准化验名").toString();
               if (document.get("标准标本") != null)
                   tempStr += document.get("标准标本").toString();
               if (document.getString("SLE表现系统分组").toUpperCase().equals("N") || document.getString("SLE表现系统分组").equals("")) {
                   if (!tempStr.equals(""))
                       mapHY.put(tempStr, "0");
               } else if (!tempStr.equals(""))
                   mapSleBxianXiTongFenzu.put(tempStr, document.get("SLE表现系统分组").toString());

           }
           System.out.println("Process Exclude PID OK.");
       }catch (Exception e){e.printStackTrace();}
        return  mapSleBxianXiTongFenzu;
    }


    private static Map<String,String> ExportADO( MongoDatabase db, String strADOCondition)
    {
        Document dd=null;
        Map<String,String> map = new HashMap<String, String>();
        MongoCollection<Document> mc = db.getCollection("ADO");
        MongoCursor<Document> cursor = mc.find(Document.parse(strADOCondition)).iterator();
        while(cursor.hasNext())
        {

            dd= cursor.next();
            if(dd.containsKey("PID") &&dd.containsKey("出生年") &&dd.containsKey("中心")) {
                map.put(dd.getString("PID"),dd.getString("出生年")+"|"+dd.getString("出生年取值RID")+"|"+dd.getString("中心"));
            }
        }
        return map;
    }
    public static void getBasicPidInfo(Map<String,JSONObject> mapAge) {
        try {
            JSONObject document;
            JSONObject config = new JSONObject();
            config.put("filename", LocalHostInfo.getPath() + "交付/PID验证列表.xlsx");
            config.put("source_type", "excel");

            DSExcelReader2 excelReader = new DSExcelReader2(config);
            while ((document = excelReader.nextDocument()) != null) {
                if (document.getString("患者（PID）") != null)
                    mapAge.put(document.getString("患者（PID）"), document);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void getBirthDetail(MongoDatabase mdb,Map<String,JSONObject> mapBHInfo)
    {
        MongoCollection<Document> dc=mdb.getCollection("ABH");
        MongoCursor<Document> mongoCursor=dc.find(Document.parse("{"+ BaseInfo_Title_ListValue_DBCondition.BH13SLE+"}")).iterator();
        while (mongoCursor.hasNext())
        {
            Document dd=mongoCursor.next();
            JSONObject jsonObject=new JSONObject();
            jsonObject.put("生产状况",dd.getString("生产状况"));
            jsonObject.put("RID",dd.getString("RID"));
            jsonObject.put("记录时间戳",dd.getString("记录时间戳"));
            mapBHInfo.put(dd.getString("PID"),jsonObject);
        }

    }

    public static  void ExportADI( MongoDatabase db,String strADICondition) throws Exception
    {
        int RowNum=1;
        Map<String,JSONObject> mapBHInfo=new HashMap<String, JSONObject>();
        getBasicPidInfo(mapAge);
        getBirthDetail(db,mapBHInfo);
        SXSSFWorkbook sxssfWorkbook = new SXSSFWorkbook(2000);
        SXSSFSheet sheet = sxssfWorkbook.createSheet();
        fillExcelTitle(sheet,"表头,医院,科室,患者（PID）,性别,地域,病历（RID）,诊断锚点,上下文,诊断状态,标准诊断名_原,词尾词,标准诊断名,ICD-10主码,CD-10副码,部位,部位描绘,诊断修饰,诊断程度,诊断时间,时间状态,备注,诊断时间天,诊断时间年," +
                "出生年取值RID,出生年,诊断时间年减去出生年,最晚记录时间天,病程天,生产状况RID,生产状况,生产状况分组,生产状况RID记录时间天");
        MongoCollection<Document> mc = db.getCollection("ADI");
        Map<String,JSONObject> mapExceptPID = ExportQueZhenTable.fillExceptPID();
        Map<String,String> mapExceptZD=new HashMap<String, String>(); //存需要移除的PID
        ArrayList<Document> aggregates = new ArrayList<Document>();

        aggregates.add(new Document("$match",Document.parse(strADICondition)));
        aggregates.add(new Document("$sort",Document.parse("{'PID':1,'诊断时间':1}")));
        aggregates.add(new Document("$group",Document.parse("{'_id':{'PID':'$PID'}, '首发诊断':{'$first':'$$ROOT'}}")));
        MongoCursor<Document> cursor =mc.aggregate(aggregates).allowDiskUse(true).iterator();
        while(cursor.hasNext())
        {
            System.out.println("processing ADI:"+RowNum);
            Document dd=(Document) cursor.next().get("首发诊断");
            if(mapExceptPID.get(dd.get("PID").toString()) !=null)
                continue;

            if(mapAge.get(dd.get("PID").toString())==null)  //取年龄失败直接移除
            {
                mapExceptZD.put(dd.get("PID").toString(), "首诊时间异常");
                continue;
            }
            JSONObject objectBasic=mapAge.get(dd.getString("PID"));
            String tempDay= DateFormat.getDateFormatStr(dd.get("诊断时间").toString());
            Integer age=1;
            try
            {
                age = Integer.valueOf(tempDay.substring(0,4))-Integer.valueOf(objectBasic.getString("出生年"));
            } catch (Exception e) { e.printStackTrace(); }
            if(age<0 ||age>100)
            {
                mapExceptZD.put(dd.get("PID").toString(),"首诊时间异常");
                continue;
            }
            Row row = sheet.createRow(RowNum++);
            if(dd.containsKey("_id"))
                row.createCell(0).setCellValue(dd.getObjectId("_id").toString());
            row.createCell(1).setCellValue(objectBasic.getString("医院"));  //医院
            if(dd.containsKey("科室"))
                row.createCell(2).setCellValue(dd.get("科室").toString());
            if(dd.containsKey("PID"))
                row.createCell(3).setCellValue(dd.getString("PID"));
            row.createCell(4).setCellValue(objectBasic.getString("性别"));
            row.createCell(5).setCellValue(objectBasic.getString("地域"));
            if(dd.containsKey("RID"))
                row.createCell(6).setCellValue(dd.get("RID").toString());
            if(dd.containsKey("段落标题"))
                row.createCell(7).setCellValue(dd.get("段落标题").toString());
            if(dd.containsKey("上下文"))
                row.createCell(8).setCellValue(dd.get("上下文").toString());
            if(dd.containsKey("诊断状态"))
                row.createCell(9).setCellValue(dd.get("诊断状态").toString());
            if(dd.containsKey("标准诊断名_原"))
                row.createCell(10).setCellValue(dd.get("标准诊断名_原").toString());
            if(dd.containsKey("词尾词"))
                row.createCell(11).setCellValue(dd.get("词尾词").toString());
            if(dd.containsKey("标准诊断名"))
                row.createCell(12).setCellValue(dd.get("标准诊断名").toString());
            if(dd.containsKey("ICD-10主码"))
                row.createCell(13).setCellValue(dd.get("ICD-10主码").toString());
            if(dd.containsKey("CD-10副码"))
                row.createCell(14).setCellValue(dd.get("CD-10副码").toString());
            if(dd.containsKey("部位"))
                row.createCell(15).setCellValue(dd.get("部位").toString());
            if(dd.containsKey("部位描绘"))
                row.createCell(16).setCellValue(dd.get("部位描绘").toString());
            if(dd.containsKey("诊断修饰"))
                row.createCell(17).setCellValue(dd.get("诊断修饰").toString());
            if(dd.containsKey("诊断程度"))
                row.createCell(18).setCellValue(dd.get("诊断程度").toString());
            if(dd.containsKey("诊断时间"))
                row.createCell(19).setCellValue(dd.get("诊断时间").toString());
            if(dd.containsKey("时间状态"))
                row.createCell(20).setCellValue(dd.get("时间状态").toString());

            //备注留一列
            row.createCell(22).setCellValue(tempDay.substring(0,10));
            row.createCell(23).setCellValue(tempDay.substring(0,4));
            row.createCell(24).setCellValue(objectBasic.getString("出生年RID"));
            row.createCell(25).setCellValue(objectBasic.getString("出生年"));
            row.createCell(26).setCellValue(String.valueOf(age));
            String strLastDay=getLastRIDDay(db,dd.getString("PID"));
            row.createCell(27).setCellValue(strLastDay);
            String days= DateFormat.getTwoDay(strLastDay,tempDay.substring(0,10));
            row.createCell(28).setCellValue(days);
            JSONObject jsBHinfo=mapBHInfo.get(dd.getString("PID")); //获取生育信息
            if(jsBHinfo ==null) {
                row.createCell(29).setCellValue("");
                row.createCell(30).setCellValue("");
                if(objectBasic.getString("性别").equals("男"))
                  row.createCell(31).setCellValue("男性");
                else
                  row.createCell(31).setCellValue("女性无生育实体");
                row.createCell(32).setCellValue("");
            }
            else
            {
                row.createCell(29).setCellValue(jsBHinfo.getString("RID"));
                String strBStatus=jsBHinfo.getString("生产状况");
                row.createCell(30).setCellValue(strBStatus);
                if(strBStatus.equals("0")||strBStatus.equals("0次"))
                    row.createCell(31).setCellValue("未生育组");
                else if(strBStatus.equals("未提及"))
                    row.createCell(31).setCellValue("未提及");
                else
                    row.createCell(31).setCellValue("生育组");
                row.createCell(32).setCellValue(get10JSonValue(jsBHinfo,"记录时间戳"));
            }


        }
        FileOutputStream fileOutputStream = new FileOutputStream(LocalHostInfo.getPath()+"交付/首诊时间表.xlsx");
        sxssfWorkbook.write(fileOutputStream);
        sxssfWorkbook.close();
        fileOutputStream.close();
        getADIPIDExcludeTable(mapExceptPID,mapExceptZD,db);
    }

    public  static String get10JSonValue(JSONObject jsonObject, String key)
    {
        if(jsonObject == null || key == null)return "N";
        if(jsonObject.getString(key) ==null)
            return "N";
        if(jsonObject.getString(key).length()>10)
            return jsonObject.getString(key).substring(0,10);
        return jsonObject.getString(key);
    }
    public static String getLastRIDDay(MongoDatabase dbHDP,String PID)
    {
        //    System.out.println("getLastRIDDay");
        MongoCollection<Document> mc = dbHDP.getCollection("ARB");
        ArrayList<Document> aggregates = new ArrayList<Document>();
        String result="";
        String strARBCondition="{'PID': '"+PID+"','记录时间戳':{$exists:true,$regex:/^.{10,}$/}"+BaseInfo_Title_ListValue_DBCondition.ADO13+"}";
        aggregates.add(new Document("$match",Document.parse(strARBCondition)));
        aggregates.add(new Document("$sort",Document.parse("{'记录时间戳':-1}")));
        aggregates.add(new Document("$group",Document.parse("{'_id':{'PID':'$PID'}, '记录时间戳':{'$first':'$$ROOT'}}")));
        MongoCursor<Document> cursor =mc.aggregate(aggregates).allowDiskUse(true).iterator();
        while (cursor.hasNext())
        {
            Document dd= (Document)cursor.next().get("记录时间戳");
            result=dd.getString("记录时间戳");
        }
        if(result.length() >10)
            return result.substring(0,10);
        return result;
    }
    private static void getADIPIDExcludeTable(Map<String,JSONObject> mapExceptPID,Map<String,String>mapExceptZD,
                                              MongoDatabase db) throws Exception
    {
        Map<String,String> mapTotalPID=getTotalADIPID(db);

        SaveExcelTool saveExcelTool = new SaveExcelTool();
        SXSSFSheet sheet = saveExcelTool.getSheet("");
        saveExcelTool.fillExcelTitle("移出步骤,PID");

        int RowNum=1;
        for(Map.Entry<String,String> map :mapTotalPID.entrySet())
        {
            if(mapZDPID.containsKey(map.getKey()))
                continue;
            if(mapExceptPID.containsKey(map.getKey()))
                continue;
            Row row = sheet.createRow(RowNum++);
            row.createCell(0).setCellValue("诊断未入组");
            row.createCell(1).setCellValue(map.getKey());
        }
        for(Map.Entry<String,String> map :mapExceptZD.entrySet())
        {
            Row row = sheet.createRow(RowNum++);
            row.createCell(0).setCellValue(map.getValue());
            row.createCell(1).setCellValue(map.getKey());
        }
        for(Map.Entry<String,JSONObject> map:mapExceptPID.entrySet())
        {
            String strYiChuBuzHou=map.getValue().getString("移出步骤");
            if(strYiChuBuzHou.equals("PID验证")||strYiChuBuzHou.equals("诊断未入组")||strYiChuBuzHou.equals("确诊时间小于0")||
                    strYiChuBuzHou.equals("首诊时间异常")||strYiChuBuzHou.equals("无初发表现")||strYiChuBuzHou.equals("初发时间异常")) {
                Row row = sheet.createRow(RowNum++);
                row.createCell(0).setCellValue(map.getValue().getString("移出步骤"));
                row.createCell(1).setCellValue(map.getKey());
            }
        }
        saveExcelTool.saveExcel("交付/移除组PID列表.xlsx");
        System.out.println("Export 移除组PID列表 OK");
    }

    private  static Map<String,String> getTotalADIPID(MongoDatabase db)
    {
        Map<String,String> map = new HashMap<String, String>();
        MongoCollection<Document> mc = db.getCollection("ADI");ArrayList<Document> aggregates = new ArrayList<Document>();
        aggregates.add(new Document("$match",Document.parse("{"+BaseInfo_Title_ListValue_DBCondition.ZD13SLE+"}")));
        aggregates.add(new Document("$group",Document.parse("{'_id':{'PID':'$PID'}}")));
        MongoCursor<Document> cursor =mc.aggregate(aggregates).allowDiskUse(true).iterator();

        while(cursor.hasNext())
        {
             String strPID=((Document)cursor.next().get("_id")).getString("PID");

             if(!strPID.equals(""))
             {
                 map.put(strPID,"0");
             }
        }
        return map;
    }
    /*
    * 以","分割的字符串填充excel表头，并返回表头名称及所在列位置的map
    * */
    private static void fillExcelTitle(SXSSFSheet sheet,String title)
    {
        String[] titles = title.split(",");
        Row row = sheet.createRow(0);
        for (int i = 0; i <titles.length ; i++) {
            row.createCell(i).setCellValue(titles[i]);
        }
    }

}
