package com.RuiShiKeYan.RAExportTable;

import com.RuiShiKeYan.Common.Interface.IruiShiKeYan;
import com.RuiShiKeYan.Common.Method.*;
import com.RuiShiKeYan.SubMethod.getHDPInfo;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.yiyihealth.data.DaX.reader.DSExcelReader2;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.bson.Document;
import test.java.task_SLE_LangChuang.BaseInfo_Title_ListValue_DBCondition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA
 * User:huangming
 * Date:2018/1/17
 * Time:下午6:10
 * //合并诊断表   用药情况表  用药组合表
 */

public class ExportRALeiJiTable extends RuiShiKeYan  implements IruiShiKeYan{

    Map<String,JSONObject> mapPId=new HashMap<String, JSONObject>();
    private    Map<String,Document> mapYY = new HashMap<String, Document>();
    private    Map<String,ArrayList<Document>> mapYYAllTime = new HashMap<String, ArrayList<Document>>();
    private    Map<String,Document> mapYYMD = new HashMap<String, Document>();
    private    Map<String,Document> mapZD = new HashMap<String, Document>();

    Map<String,ArrayList<String>> mapADISubItem= new HashMap<String, ArrayList<String>>();
    Map<String,ArrayList<String>> mapADRSubItem= new HashMap<String, ArrayList<String>>();
    Map<String,String> mapISubAndSysMapping=new HashMap<String, String>();
    Map<String,String> mapRSubAndSysMapping=new HashMap<String, String>();
    final String publicTitle="PID,医院,性别,生产状况分组,地域,首诊时间天,首诊年龄";
    public static void main(String[] args) throws Exception
    {
        MongoDBHelper mongoDBHelper = new MongoDBHelper("HDP-live");
        MongoDatabase db= mongoDBHelper.getDb();
        IruiShiKeYan iruiShiKeYan= new ExportRALeiJiTable();
        iruiShiKeYan.run(db);
        mongoDBHelper.closeMongoDb();
    }


    public void run(MongoDatabase mdb, Object[] args) {
        try {
            getBasicInfo(mdb);
            writeADIToExcel();
            writeADRToExcel();
            writeADRZHToExcel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeADIToExcel()
    {
        String title=publicTitle+",合并诊断子项,合并诊断系统项,合并诊断时间天,合并诊断RID";
        SaveExcelTool saveExcelTool=new SaveExcelTool();
        SXSSFSheet sheet= saveExcelTool.getSheet("");
        saveExcelTool.fillExcelTitle(title);
        int Rownum=1;
        for(Map.Entry<String,JSONObject> mappid:mapPId.entrySet())
        {
            JSONObject jsonBasic=mappid.getValue();
            for(Map.Entry<String,ArrayList<String>> mapTable:mapADISubItem.entrySet()) {
                JSONObject jsonObject = getEntityFirstTime(mappid.getKey(), mapZD, mapTable.getValue(),"");
                if (jsonObject == null)  //为空的话就创建空cell
                {
                    continue;
                }
                Row row = sheet.createRow(Rownum++);
                int cell = 0;
                cell = fillPublicCell(mappid.getKey(), row, jsonBasic, cell);
                row.createCell(cell++).setCellValue(mapTable.getKey());
                row.createCell(cell++).setCellValue(mapISubAndSysMapping.get(mapTable.getKey()));
                row.createCell(cell++).setCellValue(getJSonValue(jsonObject, "firstTime"));
                row.createCell(cell++).setCellValue(getJSonValue(jsonObject, "RID"));
            }
        }
        saveExcelTool.saveExcel("交付/合并诊断表.xlsx");
    }

    private void writeADRToExcel()
    {
        String title=publicTitle+",生产状况RID记录时间天,用药子项,用药系统项,用药时间天,用药RID,用药年份,出生年,用药年龄";
        SaveExcelTool saveExcelTool=new SaveExcelTool();
        SXSSFSheet sheet= saveExcelTool.getSheet("");
        saveExcelTool.fillExcelTitle(title);
        int Rownum=1;
        for(Map.Entry<String,JSONObject> mappid:mapPId.entrySet())
        {
            JSONObject jsonBasic=mappid.getValue();

            // 3/7去掉时间限制
//            String strShouZhenTime=jsonBasic.getString("诊断时间天");
         //   String strSYRIDTime="";//getJSonValue(jsonBasic,"生产状况RID记录时间天");
//            if(strSYRIDTime.equals("") ||strSYRIDTime.compareTo(strShouZhenTime)<= 0 )
//            {
//                strSYRIDTime="";
//            }
            for(Map.Entry<String,ArrayList<String>> mapTable:mapADRSubItem.entrySet()) {
          //3/9 添加 系统项为"其他药物"的子项不要
                if(mapRSubAndSysMapping.get(mapTable.getKey())!=null &&mapRSubAndSysMapping.get(mapTable.getKey()).equals("其它药物"))
                {
                    continue;
                }
                List<Document> listDocuments = getEntityFirstTime(mappid.getKey(), mapYYAllTime, mapTable.getValue());
                if (listDocuments.size() <1)  //为空的话就创建空cell
                {
                   continue;
                }
                for(Document jsonObject:listDocuments) {

                    Row row = sheet.createRow(Rownum++);
                    int cell = 0;
                    cell = fillPublicCell(mappid.getKey(), row, jsonBasic, cell);
                    row.createCell(cell++).setCellValue(getJSonValue(jsonBasic, "生产状况RID记录时间天"));
                    row.createCell(cell++).setCellValue(mapTable.getKey());
                    row.createCell(cell++).setCellValue(mapRSubAndSysMapping.get(mapTable.getKey()));
                    row.createCell(cell++).setCellValue(get10JSonValue(jsonObject, "用药时间"));
                    row.createCell(cell++).setCellValue(getJSonValue(jsonObject, "RID"));
                    try {
                        String yyYear = getJSonValue(jsonObject, "用药时间").substring(0, 4);
                        String birthYear = getJSonValue(jsonBasic, "出生年");
                        Integer yyAge = Integer.valueOf(yyYear) - Integer.valueOf(birthYear);
                        row.createCell(cell++).setCellValue(yyYear);
                        row.createCell(cell++).setCellValue(birthYear);
                        row.createCell(cell++).setCellValue(yyAge);


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        saveExcelTool.saveExcel("交付/用药情况表.xlsx");
    }

    private void writeADRZHToExcel()
    {
        String title=publicTitle+",用药子项,用药系统项,用药时间天,用药RID,段落标题";
        SaveExcelTool saveExcelTool=new SaveExcelTool();
        SXSSFSheet sheet= saveExcelTool.getSheet("");
        saveExcelTool.fillExcelTitle(title);
        int Rownum=1;
        for(Map.Entry<String,JSONObject> mappid:mapPId.entrySet())
        {
            JSONObject jsonBasic=mappid.getValue();

            for(Map.Entry<String,ArrayList<String>> mapTable:mapADRSubItem.entrySet()) {

                JSONObject jsonObject = getEntityFirstTime(mappid.getKey(), mapYYMD, mapTable.getValue(),"");
                if (jsonObject == null)  //为空的话就创建空cell
                {
                    continue;
                }
                Row row = sheet.createRow(Rownum++);
                int cell = 0;
                cell = fillPublicCell(mappid.getKey(), row, jsonBasic, cell);
                row.createCell(cell++).setCellValue(mapTable.getKey());
                row.createCell(cell++).setCellValue(mapRSubAndSysMapping.get(mapTable.getKey()));
                row.createCell(cell++).setCellValue(getJSonValue(jsonObject, "firstTime"));
                row.createCell(cell++).setCellValue(getJSonValue(jsonObject, "RID"));
                row.createCell(cell++).setCellValue(getJSonValue(jsonObject,"段落标题"));
            }
        }

        saveExcelTool.saveExcel("交付/用药组合表.xlsx");
    }

    private List<Document> getEntityFirstTime(String strPId,Map<String,ArrayList<Document>> mapEntity,ArrayList<String> arrayList)
    {
        List<Document> arrList= new ArrayList<Document>();

        for (String strTableName:arrayList) {
            ArrayList<Document> arrDocument=mapEntity.get(strPId+strTableName);
            if(arrDocument !=null)
            {
                for(Document document : arrDocument)
                {
                    arrList.add(document);
                }
            }
        }

        return arrList;
    }



    private JSONObject getEntityFirstTime(String strPId,Map<String,Document> mapEntity,ArrayList<String> arrayList,String strSYRIDTime)
    {
        JSONObject jsonObject= new JSONObject();
        String strFirstTime="N";


        for (int i = 0; i <arrayList.size() ; i++) {
           String strTableName=arrayList.get(i);
           Document document=mapEntity.get(strPId+strTableName);
           if(document !=null)
           {
               String strtempTime=get10JSonValue(document,"firstTime");
               if(strFirstTime.compareTo(strtempTime) >0 &&(strSYRIDTime.equals("") || strtempTime.compareTo(strSYRIDTime) >=0))
               {
                   strFirstTime=strtempTime;
                   jsonObject.put("firstTime",strFirstTime);
                   jsonObject.put("RID",getJSonValue(document,"RID"));
                   jsonObject.put("段落标题",getJSonValue(document,"段落标题"));
               }
           }
        }

        if(strFirstTime.equals("N"))
            return null;
       return jsonObject;
    }

    private int fillPublicCell(String strPID,Row row,JSONObject jsonBasic,int cell)
    {
        row.createCell(cell++).setCellValue(strPID);
        row.createCell(cell++).setCellValue(jsonBasic.getString("医院"));
        row.createCell(cell++).setCellValue(jsonBasic.getString("性别"));
        row.createCell(cell++).setCellValue(jsonBasic.getString("生产状况分组"));
        row.createCell(cell++).setCellValue(jsonBasic.getString("地域"));
        row.createCell(cell++).setCellValue(jsonBasic.getString("诊断时间天"));
        row.createCell(cell++).setCellValue(jsonBasic.getString("诊断时间年减去出生年"));
      //  row.createCell(cell++).setCellValue(jsonBasic.getString("出生年"));

        return cell;
    }
    private void getBasicInfo(MongoDatabase mdb) throws Exception
    {
        String fileName = LocalHostInfo.getPath() + "交付/首诊时间表.xlsx";
        ReadExcelToMap.readFromExcelToMap(mapPId,fileName,"患者（PID）",true);
        getSubItemMap();
        getHDPInfo.getYYDay(mdb,mapYY,"");
        getYYAllDay(mdb,mapYYAllTime,"");
        getHDPInfo.getYYDay(mdb,mapYYMD,",'段落标题':{$in:['出院医嘱','出院带药','出院后用药及建议']}");
        getHDPInfo.getZDDay(mdb,mapZD,"");
    }
    public  void getYYAllDay(MongoDatabase dbHDP,Map<String,ArrayList<Document>> mapYYAll,String strConditon)
    {
        System.out.println("getAllYYDay");
        MongoCollection<Document> mc = dbHDP.getCollection("ADR");
        ArrayList<Document> aggregates = new ArrayList<Document>();
        String strZDCondition="{"+BaseInfo_Title_ListValue_DBCondition.ADR13+strConditon+",'用药时间':{$exists:true,$regex:/^.{10,}$/},'是否使用':'使用','通用名':{$ne:'',$exists:true}}";
        aggregates.add(new Document("$match",Document.parse(strZDCondition)));
        aggregates.add(new Document("$project",Document.parse("{'用药时间':'$用药时间','通用名':'$通用名','PID':'$PID','RID','$RID'")));//,'段落标题':'$段落标题'
      //  aggregates.add(new Document("$sort",Document.parse("{'用药时间':1}")));
        aggregates.add(new Document("$group",Document.parse("{'_id':{'PID':'$PID','通用名':'$通用名'}, 'result':{'$push':'$$ROOT'}}")));
        MongoCursor<Document> cursor =mc.aggregate(aggregates).allowDiskUse(true).iterator();
        while (cursor.hasNext())
        {
            Document  mcursor =cursor.next();
            Document groupInfo=(Document)mcursor.get("_id");

            ArrayList arrDocument= (ArrayList)mcursor.get("result");
            mapYYAll.put(groupInfo.getString("PID")+groupInfo.getString("通用名"),arrDocument);
        }
    }

    private  void getSubItemMap() throws Exception
    {
        JSONObject document;
        String fileName= LocalHostInfo.getPath()+ BaseInfo_Title_ListValue_DBCondition.strCLeiJiFenZuFileName;
        String tempZiXiang,tempSystem;
        JSONObject config = new JSONObject();
        config.put("filename", fileName);
        config.put("source_type", "excel");

        DSExcelReader2 excelReader = new DSExcelReader2(config);
        while ((document = excelReader.nextDocument()) != null) {
            tempZiXiang=getJSonValue(document,"子项");
            tempSystem=getJSonValue(document,"拟观察系统累及分组");
            if(tempZiXiang.equals("")||tempZiXiang.toUpperCase().equals("N"))
                continue;

            if(getJSonValue(document,"表型").equals("标准诊断名") &&!tempZiXiang.equals("类风湿性关节炎"))
            {
                ArrayList<String> arrayList=mapADISubItem.get(tempZiXiang);
                if(arrayList ==null) {
                    arrayList = new ArrayList<String>();
                    mapADISubItem.put(tempZiXiang,arrayList);
                    mapISubAndSysMapping.put(tempZiXiang,tempSystem);
                }
                arrayList.add(getJSonValue(document,"表型名称") +getJSonValue(document,"标准标本"));
            }else if(getJSonValue(document,"表型").equals("用药通用名"))
            {
                ArrayList<String> arrayList=mapADRSubItem.get(tempZiXiang);
                if(arrayList ==null) {
                    arrayList = new ArrayList<String>();
                    mapADRSubItem.put(tempZiXiang,arrayList);
                    mapRSubAndSysMapping.put(tempZiXiang,tempSystem);
                }
                arrayList.add(getJSonValue(document,"表型名称") +getJSonValue(document,"标准标本"));

            }


        }
    }


//
//    private void getBirthDetail(MongoDatabase mdb,Map<String,JSONObject> mapBHInfo)
//    {
//        MongoCollection<Document> dc=mdb.getCollection("ABH");
//        MongoCursor<Document> mongoCursor=dc.find(Document.parse("{"+ BaseInfo_Title_ListValue_DBCondition.BH13SLE+"}")).iterator();
//        while (mongoCursor.hasNext())
//        {
//             Document dd=mongoCursor.next();
//             JSONObject jsonObject=new JSONObject();
//             jsonObject.put("生产状况",getJSonValue(dd,"生产状况"));
//              mapBHInfo.put(getJSonValue(dd,"PID"),jsonObject);
//        }
//
//    }

}
