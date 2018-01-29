package test.java.task_SLE_QueZzhengLeiJi;

import com.RuiShiKeYan.Common.Method.MongoDBHelper;
import com.RuiShiKeYan.Common.Method.SaveExcelTool;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
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
 * Date:2017/12/7
 * Time:下午3:54
 */
public class ExportWPSEntityStatistic {

    static Map<String,String> mapHospitalIdName= new HashMap<String, String>();
    static String strTotalFirstDay,strTotalLastDay;
    static String[] strBasicInfo={"性别","籍贯","出生地","现住址","婚姻状况","记录时间戳","出生年","现住址记录时间戳","婚姻状况记录时间戳"};
    public static void main(String[] args)
    {
        try {
            MongoDBHelper mongoDBHelper = new MongoDBHelper("HDP-live");
            MongoDatabase mdb = mongoDBHelper.getDb();

            SaveExcelTool saveExcelTool = new SaveExcelTool();
            SXSSFSheet sheet = saveExcelTool.getSheet("");
            int cellNum=saveExcelTool.fillExcelTitle(BaseInfo_Title_ListValue_DBCondition.titleWPSEntityStatistic);

            getBasicInfo(mdb);
            writeToExcel(sheet,mdb,cellNum);
            saveExcelTool.saveExcel("交付/WPS表型统计.xlsx");
            mongoDBHelper.closeMongoDb();
        }
        catch (Exception e)
        {e.printStackTrace();
        }
    }
    private static void writeToExcel(SXSSFSheet sheet,MongoDatabase db,int firstCellNum)
    {
        String[] hospitalList=BaseInfo_Title_ListValue_DBCondition.YiYuanList.split(",");
        strTotalFirstDay= getFirstLastRIDDay(db,"",false);
        strTotalLastDay=getFirstLastRIDDay(db,"",true);
        fillFirstRowTitle(sheet,firstCellNum,strTotalFirstDay.substring(0,4),strTotalLastDay.substring(0,4));
        for (int i = 0; i < hospitalList.length; i++) {
            Row row=sheet.createRow(i+1);
            String hospitalCondition=",'hospitalId':'"+hospitalList[i]+"'";
            fillTheRow(row,hospitalCondition,hospitalList[i],db);
        }
        //汇总
        Row row = sheet.createRow(hospitalList.length+1);
        fillTheRow(row,"","",db);
    }
    private static void fillTheRow(Row row,String hospitalCondition,String hospitalId,MongoDatabase db)
    {
        int cellNum=0;
        Long pidCount=getPIDCount(db,hospitalCondition,"");
        if(hospitalId.equals("")) {
            row.createCell(cellNum++).setCellValue("汇总");
        }
        else {
            row.createCell(cellNum++).setCellValue(mapHospitalIdName.get(hospitalId));
        }
        row.createCell(cellNum++).setCellValue(pidCount);
        row.createCell(cellNum++).setCellValue(getPIDCount(db,hospitalCondition,",'性别':'男'"));
        row.createCell(cellNum++).setCellValue(getPIDCount(db,hospitalCondition,",'性别':'女'"));
        Long totalCount=getEntityCount(db,"ARB",BaseInfo_Title_ListValue_DBCondition.ADO13+hospitalCondition);
        row.createCell(cellNum++).setCellValue(totalCount);
        row.createCell(cellNum++).setCellValue(getEntityCount(db,"ARB","'recordType':'入院记录',"+BaseInfo_Title_ListValue_DBCondition.ADO13+hospitalCondition));//入院病历数
        row.createCell(cellNum++).setCellValue(getEntityCount(db,"ARB","'recordType':'出院记录',"+BaseInfo_Title_ListValue_DBCondition.ADO13+hospitalCondition));//出院病历数
        row.createCell(cellNum++).setCellValue(getFirstLastRIDDay(db,hospitalCondition,false));
        row.createCell(cellNum++).setCellValue(getFirstLastRIDDay(db,hospitalCondition,true));
        row.createCell(cellNum++).setCellValue(pidCount);
        row.createCell(cellNum++).setCellValue(getEntityCount(db,"ADI",BaseInfo_Title_ListValue_DBCondition.ZD13SLE+hospitalCondition));
        row.createCell(cellNum++).setCellValue(getEntityCount(db,"ADR",BaseInfo_Title_ListValue_DBCondition.ADR13+hospitalCondition));
        row.createCell(cellNum++).setCellValue(getEntityCount(db,"ALA",BaseInfo_Title_ListValue_DBCondition.HY13SLE+hospitalCondition));
        row.createCell(cellNum++).setCellValue(getEntityCount(db,"ASY",BaseInfo_Title_ListValue_DBCondition.ZZTZ13SLE+hospitalCondition));
        cellNum=fillYearStatistic(strTotalFirstDay.substring(0,4),strTotalLastDay.substring(0,4),row,cellNum,hospitalCondition,db,totalCount);
        for (int i = 0; i < strBasicInfo.length; i++) {
                row.createCell(cellNum++).setCellValue(getPIDCount(db,hospitalCondition,",'"+strBasicInfo[i]+"':{'$ne':'未提及'}"));
        }
    }
    private static int fillYearStatistic(String firstYear,String lastYear,Row row,int cellNum,String hospitalCondition,MongoDatabase db,Long totalRID)
    {
        System.out.println(firstYear+":"+lastYear);
        Long totalCount=0L;
        Integer startYear,endYear;
        try {
            startYear = Integer.valueOf(firstYear);
            endYear = Integer.valueOf(lastYear);
          }catch (Exception e){e.printStackTrace();return -1;}
        //汇总
        for (Integer i = startYear; i <= endYear; i++) {
            Long count=getEntityCount(db,"ARB","'记录时间戳':{$exists:true,$regex:/^"+i.toString()+"/}"+BaseInfo_Title_ListValue_DBCondition.ADO13+hospitalCondition);
            row.createCell(cellNum++).setCellValue(count);
            totalCount += count;
        }
        row.createCell(cellNum++).setCellValue(totalRID-totalCount);
        //入院病历数
        for (Integer i = startYear; i <= endYear; i++) {
            row.createCell(cellNum++).setCellValue(getEntityCount(db,"ARB","'记录时间戳':{$exists:true,$regex:/^"+i.toString()+"/},'recordType':'入院记录',"+BaseInfo_Title_ListValue_DBCondition.ADO13+hospitalCondition));
          }
        //出院病历数
        for (Integer i = startYear; i <= endYear; i++) {
            row.createCell(cellNum++).setCellValue(getEntityCount(db,"ARB","'记录时间戳':{$exists:true,$regex:/^"+i.toString()+"/},'recordType':'出院记录',"+BaseInfo_Title_ListValue_DBCondition.ADO13+hospitalCondition));
        }
        return  cellNum;
    }

    private static void fillFirstRowTitle(SXSSFSheet sheet,int firstCellNum,String firstYear,String lastYear)
    {
        Integer startYear,endYear;
        try {
            startYear = Integer.valueOf(firstYear);
            endYear = Integer.valueOf(lastYear);
        }catch (Exception e){e.printStackTrace();return;}
        Row row =sheet.getRow(0);
        //汇总
        for (Integer i = startYear; i <= endYear; i++) {

            row.createCell(firstCellNum++).setCellValue(i.toString()+"年病历数_总");
        }
        row.createCell(firstCellNum++).setCellValue("其他年病历数_总");
        //入院病历数
        for (Integer i = startYear; i <= endYear; i++) {
            row.createCell(firstCellNum++).setCellValue(i.toString()+"年病历数_入院");
        }
        //出院病历数
        for (Integer i = startYear; i <= endYear; i++) {
            row.createCell(firstCellNum++).setCellValue(i.toString()+"年病历数_出院");
        }
        for (int i = 0; i <strBasicInfo.length ; i++) {
            row.createCell(firstCellNum++).setCellValue(strBasicInfo[i]);
        }
    }

    private static void getBasicInfo(MongoDatabase mdb)
     {
         getHospital(mdb);
     }

     private static Long getEntityCount(MongoDatabase db,String tableName,String strCondition)
     {
         MongoCollection<Document> mc = db.getCollection(tableName);
         return mc.count(Document.parse("{"+strCondition+ "}"));
     }
     private static Long getPIDCount(MongoDatabase db,String hospitalId,String Sex)
     {
         MongoCollection<Document> mc = db.getCollection("ADO");
      //   System.out.println("ADO:"+"{"+BaseInfo_Title_ListValue_DBCondition.ADO13 +hospitalId + Sex+ "}");
         return mc.count(Document.parse("{"+BaseInfo_Title_ListValue_DBCondition.ADO13 +hospitalId + Sex+ "}"));
     }
    public static void getHospital(MongoDatabase db) {
        MongoCollection<Document> mc = db.getCollection("Hospital");
        MongoCursor<Document> cursor = mc.find(Document.parse("{'_id':" + BaseInfo_Title_ListValue_DBCondition.YiYuan13 + "}")).iterator();
        while (cursor.hasNext()) {
            Document dd = cursor.next();
            mapHospitalIdName.put(dd.getString("_id"), dd.getString("name"));
        }
    }

    public static String getFirstLastRIDDay(MongoDatabase db, String strHospital, boolean flag)
    {
        MongoCollection<Document> mc = db.getCollection("ARB");
        ArrayList<Document> aggregates = new ArrayList<Document>();
        String result="";
        aggregates.add(new Document("$match",Document.parse("{"+BaseInfo_Title_ListValue_DBCondition.ADO13+strHospital+",'记录时间戳':{$exists:true,$regex:/^[0-9]{4}.*/}}")));
        aggregates.add(new Document("$project",Document.parse("{'year':{$substr:['$记录时间戳',0,4]},'记录时间戳':'$记录时间戳'}")));
        aggregates.add(new Document("$match",Document.parse("{'year':{$gte:'2000',$lte:'2017'}}")));
        if(flag)
            aggregates.add(new Document("$sort",Document.parse("{'记录时间戳':-1}")));
        else
            aggregates.add(new Document("$sort",Document.parse("{'记录时间戳':1}")));
        aggregates.add(new Document("$limit",1));
        MongoCursor<Document> cursor =mc.aggregate(aggregates).allowDiskUse(true).iterator();
        while (cursor.hasNext())
        {
            result=cursor.next().getString("记录时间戳");
        }
        if(result.length() >10)
            return result.substring(0,10);
        return result;
    }
}
