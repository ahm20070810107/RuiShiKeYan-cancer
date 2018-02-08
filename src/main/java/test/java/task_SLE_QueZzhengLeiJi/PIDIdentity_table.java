package test.java.task_SLE_QueZzhengLeiJi;

import com.RuiShiKeYan.Common.Method.LocalHostInfo;
import com.RuiShiKeYan.Common.Method.MongoDBHelper;
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA
 * User:huangming
 * Date:2017/10/16
 * Time:下午6:34
 */
public class PIDIdentity_table {

    static Map<String, String> mapHospital= new HashMap<String, String>();
    static Map<String,String> mapHospitalPro=new HashMap<String, String>();
    public static void main(String[] args) throws Exception {
        MongoDBHelper mongoDBHelper= new MongoDBHelper("HDP-live");
        MongoDatabase dbp=mongoDBHelper.getDb();

        MongoDBHelper mongoDBHelper1= new MongoDBHelper("HRS-live");
        MongoDatabase dbh=mongoDBHelper1.getDb();
        getHospitalInfo(mapHospitalPro);
        getExcel(dbp,dbh);
        mongoDBHelper.closeMongoDb();
        mongoDBHelper1.closeMongoDb();
    }

    private static void getExcel(MongoDatabase dbp,MongoDatabase dbh) throws Exception
    {
        Map<String, Document> mapADO=getADO(dbp);
        Map<String, String>  mapZhuYuan=getZhuYuanXin(dbh);
        Map<String, String> mapADI=getADI(dbp);
        Map<String, String> mapADR=getADR(dbp);
        Map<String, String> mapALA=getALA(dbp);
        Map<String, String> mapASY=getASY(dbp);
        mapHospital=getHospital(dbp);
        Map<String,Map<String,Object>> mapTotal= new HashMap<String, Map<String,Object>>();
        SXSSFWorkbook sxssfWorkbook1 = new SXSSFWorkbook(2000);
        SXSSFSheet sheet1 = sxssfWorkbook1.createSheet();

        SXSSFWorkbook sxssfWorkbook2 = new SXSSFWorkbook(2000);
        SXSSFSheet sheet2 = sxssfWorkbook2.createSheet();
        getTotalMap(mapTotal,mapADO,mapZhuYuan,mapADI,mapADR,mapALA,mapASY);
        writeToExcel(dbp,mapTotal,sheet1,sheet2);


        FileOutputStream fileOutputStream1 = new FileOutputStream(LocalHostInfo.getPath()+"交付/PID验证列表.xlsx");
        sxssfWorkbook1.write(fileOutputStream1);
        sxssfWorkbook1.close();
        fileOutputStream1.close();

        FileOutputStream fileOutputStream2 = new FileOutputStream(LocalHostInfo.getPath()+"交付/移除组PID列表.xlsx");
        sxssfWorkbook2.write(fileOutputStream2);
        sxssfWorkbook2.close();
        fileOutputStream2.close();
    }
    private static void writeToExcel(MongoDatabase dbp, Map<String,Map<String,Object>> mapTotal ,SXSSFSheet sheet1,SXSSFSheet sheet2)
    {
        fillExcelTitle(sheet1,"医院,科室,患者（PID）,住院信息表,ADO,ADO出生年,ADO性别,ADI,ADR,ALA,ASY,吸烟,出生年,婚姻状况,籍贯,性别,饮酒,现住址,出生年RID,地域,记录时间");
        fillExcelTitle(sheet2,"移出步骤,PID");
        int RowNum1=1,RowNum2=1;

        for(Map.Entry<String,Map<String,Object>> map :mapTotal.entrySet())
        {
            Boolean flag =true;
            Row row1= sheet1.createRow(RowNum1 ++);
         //
            row1.createCell(2).setCellValue(map.getKey());

            Map<String,Object> mapInfo = map.getValue();

            if(mapInfo.get("zhuyuan") ==null)
            {
                 row1.createCell(3).setCellValue("N");
                 flag=false;
            }

            if(mapInfo.get("ADO") !=null)
            {
                Document dd=(Document) mapInfo.get("ADO");
                row1.createCell(0).setCellValue(mapHospital.get(dd.getString("hospitalId")));
                if(dd.getString("出生年").equals("未提及") ||dd.getString("出生年").equals(""))
                {
                    row1.createCell(5).setCellValue("N");
                    flag=false;
                }
                if(dd.getString("性别").equals("未提及")||dd.getString("性别").equals(""))
                {
                    row1.createCell(6).setCellValue("N");
                    flag=false;
                }
                if(dd.containsKey("吸烟"))
                    row1.createCell(11).setCellValue(dd.get("吸烟").toString());
                if(dd.containsKey("出生年"))
                    row1.createCell(12).setCellValue(dd.get("出生年").toString());
                if(dd.containsKey("婚姻状况"))
                    row1.createCell(13).setCellValue(dd.get("婚姻状况").toString());
                if(dd.containsKey("籍贯"))
                    row1.createCell(14).setCellValue(dd.get("籍贯").toString());
                if(dd.containsKey("性别"))
                    row1.createCell(15).setCellValue(dd.get("性别").toString());
                if(dd.containsKey("饮酒"))
                    row1.createCell(16).setCellValue(dd.get("饮酒").toString());
                if(dd.containsKey("现住址"))
                    row1.createCell(17).setCellValue(dd.get("现住址").toString());
                if(dd.containsKey("出生年RID"))
                    row1.createCell(18).setCellValue(dd.get("出生年RID").toString());
                String strAddress=dd.getString("现住址");
                if(strAddress.equals("")||strAddress.equals("未提及"))
                {
                    if(!dd.getString("籍贯").equals("") &&!dd.getString("籍贯").equals("未提及"))
                    {
                        strAddress=dd.getString("籍贯");
                    }
                }
                if(strAddress.equals("未提及"))
                {
                     strAddress=mapHospitalPro.get(mapHospital.get(dd.getString("hospitalId")));
                }
                row1.createCell(19).setCellValue(strAddress);


            } else
            {
                row1.createCell(4).setCellValue("N");
                flag=false;
            }
            if(mapInfo.get("ADI") ==null)
            {
                row1.createCell(7).setCellValue("N");
                flag=false;
            }
            if(mapInfo.get("ADR") ==null)
            {
                row1.createCell(8).setCellValue("N");
                flag=false;
            }
            if(mapInfo.get("ALA") ==null)
            {
                row1.createCell(9).setCellValue("N");
                flag=false;
            }
            if(mapInfo.get("ASY") ==null)
            {
                row1.createCell(10).setCellValue("N");
                flag=false;
            }
            if(getPIDRecordTime(dbp,BaseInfo_Title_ListValue_DBCondition.ADO13,map.getKey()).equals("N"))
            {
                row1.createCell(20).setCellValue("N");
                Row row2 =sheet2.createRow(RowNum2++);
                row2.createCell(0).setCellValue("记录时间异常");
                row2.createCell(1).setCellValue(map.getKey());
                flag=true;
            }
            if(!flag)
            {
                Row row2 =sheet2.createRow(RowNum2++);
                row2.createCell(0).setCellValue("PID验证");
                row2.createCell(1).setCellValue(map.getKey());
            }
        }
        System.out.println(RowNum1+":"+RowNum2);

    }

    private static String getPIDRecordTime(MongoDatabase dbp,String condition,String strPid)
    {
        MongoCollection<Document> mongoCollection=dbp.getCollection("ARB");
        Document dConditon=Document.parse("{"+condition+",'PID':'"+strPid+"'}");
        MongoCursor<Document>  mc =mongoCollection.find(dConditon).projection(Document.parse("{'记录时间戳':1,'_id':0}")).iterator();
        while (mc.hasNext())
        {
            Document document = mc.next();
            String strTime= document.getString("记录时间戳");
            if(judgeTime(strTime).equals("N"))
                return "N";
        }
        return "";
    }
    private static String judgeTime(String strTime)
    {
        if(strTime == null || strTime.equals("") ||strTime.length()<10)
            return "N";
        strTime = strTime.substring(0,4);
        try {
            int recordYear= Integer.valueOf(strTime).intValue();
            int nowYear= Calendar.getInstance().get(Calendar.YEAR);
            if(recordYear < 1900 ||recordYear > nowYear)
                return "N";
        }catch (Exception e)
        {
            return "N";
        }
        return "";
    }
    private static void getTotalMap(Map<String,Map<String,Object>> mapTotal,Map<String, Document> mapADO,Map<String, String>  mapZhuYuan,Map<String, String> mapADI,
                               Map<String, String> mapADR,Map<String, String> mapALA,Map<String, String> mapASY)
    {
        for(Map.Entry<String, String> zhuyuan :mapZhuYuan.entrySet())
        {
                Map<String,Object> map =new HashMap<String,Object>();
                map.put("zhuyuan","0");
                mapTotal.put(zhuyuan.getKey(),map);
        }

        for(Map.Entry<String, String> adi :mapADI.entrySet())
        {
            if(mapTotal.get(adi.getKey()) ==null)
            {
                Map<String,Object> map =new HashMap<String,Object>();
                map.put("ADI","0");
                mapTotal.put(adi.getKey(),map);
            }
            else {
                Map<String,Object> map = mapTotal.get(adi.getKey());
                map.put("ADI", "0");
            }
        }
        for(Map.Entry<String, String> adr :mapADR.entrySet())
        {
            if(mapTotal.get(adr.getKey()) ==null)
            {
                Map<String,Object> map =new HashMap<String,Object>();
                map.put("ADR","0");
                mapTotal.put(adr.getKey(),map);
            }
            else {
                Map<String,Object> map = mapTotal.get(adr.getKey());
                map.put("ADR", "0");
            }
        }
        for(Map.Entry<String, String> ala :mapALA.entrySet())
        {
            if(mapTotal.get(ala.getKey()) ==null)
            {
                Map<String,Object> map =new HashMap<String,Object>();
                map.put("ALA","0");
                mapTotal.put(ala.getKey(),map);
            }
            else {
                Map<String,Object> map = mapTotal.get(ala.getKey());
                map.put("ALA", "0");
            }
        }

        for(Map.Entry<String, String> asy :mapASY.entrySet())
        {
            if(mapTotal.get(asy.getKey()) ==null)
            {
                Map<String,Object> map =new HashMap<String,Object>();
                map.put("ASY","0");
                mapTotal.put(asy.getKey(),map);
            }
            else {
                Map<String,Object> map = mapTotal.get(asy.getKey());
                map.put("ASY", "0");
            }
        }
        for(Map.Entry<String, Document> ado :mapADO.entrySet())
        {
            if(mapTotal.get(ado.getKey()) !=null)
            {
                Map<String,Object> map = mapTotal.get(ado.getKey());
                map.put("ADO",ado.getValue());
            }
        }
    }

    public static void getHospitalInfo(Map<String,String> mapHospital) throws Exception
    {
        JSONObject document;
        String fileName= LocalHostInfo.getPath()+"医院所属省.xlsx";
        JSONObject config = new JSONObject();
        config.put("filename", fileName);
        config.put("source_type", "excel");

        DSExcelReader2 excelReader = new DSExcelReader2(config);
        while ((document = excelReader.nextDocument()) != null) {
            if (!document.get("医院").equals(""))
                mapHospital.put(document.getString("医院"), document.getString("医院所属省"));
        }

    }
    public static Map<String, String> getZhuYuanXin(MongoDatabase db) {
        Map<String, String> map = new HashMap<String, String>();
        MongoCollection<Document> mc = db.getCollection("Record");
        ArrayList<Document> aggregates = new ArrayList<Document>();
        aggregates.add(new Document("$match", Document.parse("{'odCategories':{$in:['RA']},'deleted':false,'status':'AMD识别完成','hospitalId':"+ BaseInfo_Title_ListValue_DBCondition.YiYuan13+", 'recordType':{'$in': ['出院记录', '入院记录']}}")));
        aggregates.add(new Document("$group", Document.parse("{'_id':{'patientId':'$patientId'}}")));
        MongoCursor<Document> cursor = mc.aggregate(aggregates).allowDiskUse(true).iterator();

        while (cursor.hasNext()) {
            Document dd = (Document) cursor.next().get("_id");
            map.put(dd.getString("patientId"), "0");

        }
        return map;
    }
    public static Map<String, String> getHospital(MongoDatabase db) {
        Map<String, String> map = new HashMap<String, String>();
        MongoCollection<Document> mc = db.getCollection("Hospital");
        MongoCursor<Document> cursor = mc.find(Document.parse("{'_id':"+BaseInfo_Title_ListValue_DBCondition.YiYuan13+"}")).iterator();
        while (cursor.hasNext()) {
            Document dd = cursor.next();
            map.put(dd.getString("_id"), dd.getString("name"));

        }
        return map;
    }

    public static Map<String, Document> getADO(MongoDatabase db) {
        Map<String, Document> map = new HashMap<String, Document>();
        MongoCollection<Document> mc = db.getCollection("ADO");
       // String strADOCondition = "{'中心':{'$in':['四川大学华西医院','北京大学深圳医院','南京大学医学院附属鼓楼医院（南京鼓楼医院、南京市红十字中心医院）','上海长征医院','上海长海医院','江苏省人民医院（南京医科大学第一附属医院、江苏省红十字医院）','大连医科大学附属第二医院','浙江大学医学院附属第一医院(浙江省第一医院)','浙江大学医学院附属第二医院','中国医科大学附属第一医院','上海交通大学医学院附属仁济医院','武汉同济医院（华中科技大学同济医学院附属同济医院）','中南大学湘雅医院']}}";
        MongoCursor<Document> cursor = mc.find(Document.parse("{"+BaseInfo_Title_ListValue_DBCondition.ADO13+"}")).iterator();
        while (cursor.hasNext()) {
            Document dd = cursor.next();
            map.put(dd.getString("PID"), dd);

        }
        return map;
    }

    public static Map<String, String> getADI(MongoDatabase db) {
        Map<String, String> map = new HashMap<String, String>();
        String strZDCondition = "{"+BaseInfo_Title_ListValue_DBCondition.ZD13SLE+"}";
        MongoCollection<Document> mc = db.getCollection("ADI");
        ArrayList<Document> aggregates = new ArrayList<Document>();
        aggregates.add(new Document("$match", Document.parse(strZDCondition)));
        aggregates.add(new Document("$group", Document.parse("{'_id':{'PID':'$PID'}}")));
        MongoCursor<Document> cursor = mc.aggregate(aggregates).allowDiskUse(true).iterator();
        int Rownum= 1;
        while (cursor.hasNext()) {
            Document dd = (Document) cursor.next().get("_id");
            map.put(dd.getString("PID"), "0");
            Rownum ++;
        }
        System.out.println(Rownum);
        return map;
    }

    public static Map<String, String> getADR(MongoDatabase db) {
        Map<String, String> map = new HashMap<String, String>();
        String strZDCondition = "{"+BaseInfo_Title_ListValue_DBCondition.ADR13+"}";
        MongoCollection<Document> mc = db.getCollection("ADR");
        ArrayList<Document> aggregates = new ArrayList<Document>();
        aggregates.add(new Document("$match", Document.parse(strZDCondition)));
        aggregates.add(new Document("$group", Document.parse("{'_id':{'PID':'$PID'}}")));
        MongoCursor<Document> cursor = mc.aggregate(aggregates).allowDiskUse(true).iterator();
        int Rownum= 1;
        while (cursor.hasNext()) {
            Document dd = (Document) cursor.next().get("_id");
            map.put(dd.getString("PID"), "0");
            Rownum ++;
        }
        System.out.println("getADR");
        System.out.println(Rownum);
        return map;
    }

    public static Map<String, String> getALA(MongoDatabase db) {
        Map<String, String> map = new HashMap<String, String>();
        String strALAConditon = "{"+BaseInfo_Title_ListValue_DBCondition.HY13SLE+",'化验时间':{$exists:true,$regex:/^.{10,}$/}}";
        MongoCollection<Document> mc = db.getCollection("ALA");
        ArrayList<Document> aggregates = new ArrayList<Document>();
        aggregates.add(new Document("$match", Document.parse(strALAConditon)));
        aggregates.add(new Document("$group", Document.parse("{'_id':{'PID':'$PID'}}")));
        MongoCursor<Document> cursor = mc.aggregate(aggregates).allowDiskUse(true).iterator();
        int Rownum= 1;
        while (cursor.hasNext()) {
            Document dd = (Document) cursor.next().get("_id");
            map.put(dd.getString("PID"), "0");
            Rownum ++;
        }
        System.out.println("getALA");
        System.out.println(Rownum);
        return map;
    }

    public static Map<String, String> getASY(MongoDatabase db) {
        Map<String, String> map = new HashMap<String, String>();
        String strZZCondition = "{"+BaseInfo_Title_ListValue_DBCondition.ZZTZ13SLE+",'否定词':''}";
        MongoCollection<Document> mc = db.getCollection("ASY");
        ArrayList<Document> aggregates = new ArrayList<Document>();
        aggregates.add(new Document("$match", Document.parse(strZZCondition)));
        aggregates.add(new Document("$group", Document.parse("{'_id':{'PID':'$PID'}}")));
        MongoCursor<Document> cursor = mc.aggregate(aggregates).allowDiskUse(true).iterator();
        int Rownum= 1;
        while (cursor.hasNext()) {
            Document dd = (Document) cursor.next().get("_id");
            map.put(dd.getString("PID"), "0");
            Rownum ++;
        }
        System.out.println("getASY");
        System.out.println(Rownum);
        return map;
    }

    private static void fillExcelTitle(SXSSFSheet sheet,String title)
    {
        String[] titles = title.split(",");
        Row row = sheet.createRow(0);
        for (int i = 0; i <titles.length ; i++) {
            row.createCell(i).setCellValue(titles[i]);
        }
    }
}