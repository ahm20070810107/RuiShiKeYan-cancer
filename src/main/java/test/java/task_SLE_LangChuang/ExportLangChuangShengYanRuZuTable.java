package test.java.task_SLE_LangChuang;

import com.RuiShiKeYan.Common.Method.DateFormat;
import com.RuiShiKeYan.Common.Method.LocalHostInfo;
import com.RuiShiKeYan.Common.Method.MongoDBHelper;
import com.RuiShiKeYan.Common.Method.SaveExcelTool;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.yiyihealth.data.DaX.reader.DSExcelReader2;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.bson.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
/**
 * Created with IntelliJ IDEA
 * User:huangming
 * Date:2017/10/23
 * Time:下午5:27
 */
public class ExportLangChuangShengYanRuZuTable {
  //  static int[] kGroup= {2,1,3,4,5,10};
    static int[] kGroup= {2};
    static int[] mGroup={15};//14,

    static Map<String,Document> mapSLELCPerSon=new HashMap<String,Document>();
    static Map<String,Document> mapTangNiaoB=new HashMap<String,Document>();
    static Map<String,JSONObject> mapBasicInfo=new HashMap<String,JSONObject>();
    static Map<String,String> mapExcludePID=new HashMap<String,String>();
    static Map<String,Document> mapShengyanZDInfo= new HashMap<String, Document>();
    static Map<String,Document> mapShengyanHYInfo= new HashMap<String, Document>();
    static String strZDList="",strTZList="",strZZList="",strHYList="";

    static Map<String,Document> mapLCShengyanHYInfo= new HashMap<String, Document>();
    static Map<String,Document> mapLCShengyanZZInfo=new HashMap<String, Document>();
    static Map<String,Document> mapLCShengyanTZInfo=new HashMap<String, Document>();
    static Map<String,Document> mapLCShengyanZDInfo=new HashMap<String, Document>();
    public static  void main(String[] args) throws Exception
    {
        MongoDBHelper mongoDBHelper= new MongoDBHelper("HDP-live");
        MongoDatabase mdb=mongoDBHelper.getDb();
        getLCShengyanHYInfo(mdb,mapLCShengyanHYInfo,strHYList,",'RPG科研结果转换':'阳性'");
        getBasicInfo(mdb);
        getShengyanZDInfo(mdb);
        getLCShengyanHYInfo(mdb,mapShengyanHYInfo,BaseInfo_Title_ListValue_DBCondition.condHYShengShunHai,",'化验结果定性（新）':'阳性'");

        writeToSheet(mdb);
        mongoDBHelper.closeMongoDb();
    }

    private static void writeToSheet(MongoDatabase mdb)
    {
        for(int k :kGroup) {
            for (int m : mGroup) {
                SaveExcelTool saveExcelTool = new SaveExcelTool();
                SXSSFSheet sheet = saveExcelTool.getSheet("");
                saveExcelTool.fillExcelTitle(BaseInfo_Title_ListValue_DBCondition.titleLCShengYanRuZB);
                Document dd = null;
                int RowNum = 1;
                for (Map.Entry<String, Document> mapPID : mapSLELCPerSon.entrySet()) {
                    String strPID = mapPID.getKey();
                    dd = mapPID.getValue();
                    Row row = sheet.createRow(RowNum++);
                    JSONObject jsonBaseInfo = mapBasicInfo.get(strPID);

                    row.createCell(0).setCellValue(jsonBaseInfo.getString("医院"));
                    row.createCell(1).setCellValue(strPID);
                    row.createCell(2).setCellValue(jsonBaseInfo.getString("出生年"));
                    String sleTime = "";
                    try {
                        sleTime = dd.getString("诊断时间").substring(0, 10);
                        row.createCell(3).setCellValue(sleTime);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    String sleMtime = DateFormat.getNextDay(sleTime, m);
                    row.createCell(4).setCellValue(dd.getString("RID"));
                    row.createCell(5).setCellValue(dd.getString("标准诊断名_原"));
                    try {
                        Integer sLEAge = Integer.valueOf(sleTime.substring(0, 4)) - Integer.valueOf(jsonBaseInfo.getString("出生年"));
                        row.createCell(6).setCellValue(sLEAge);
                    } catch (Exception e) {
                        System.out.println(strPID + "SLE年龄");
                        e.printStackTrace();
                    }
                    String sleBcZhongDian=(k==0)?"N": DateFormat.getNextDay(sleTime, k * 360+m);//k为0时不判断终点
                    String sleBCFenZu = "";
                    try {
                        String firstTime = getFirstLastRIDDay(mdb, strPID, false).substring(0, 10);
                        String lastTime = getFirstLastRIDDay(mdb, strPID, true).substring(0, 10);


                        row.createCell(7).setCellValue(firstTime);
                        row.createCell(8).setCellValue(DateFormat.getDays(dd.getString("诊断时间").substring(0, 10), firstTime));
                        row.createCell(9).setCellValue(lastTime);
                        row.createCell(10).setCellValue(sleBcZhongDian);
                        if (lastTime.compareTo(sleBcZhongDian) >= 0) {
                            sleBCFenZu = "1";
                        } else {
                            sleBCFenZu = "2";
                        }
                        row.createCell(11).setCellValue(sleBCFenZu);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //糖尿病分组
                    String strTangNiaoBTime = "";
                    if (mapTangNiaoB.containsKey(strPID)) {
                        Document dTangNiaoB = mapTangNiaoB.get(strPID);
                        strTangNiaoBTime = dTangNiaoB.getString("诊断时间").substring(0, 10);

                        row.createCell(12).setCellValue(strTangNiaoBTime);
                        row.createCell(13).setCellValue(dTangNiaoB.getString("RID"));
                        row.createCell(14).setCellValue(dTangNiaoB.getString("标准诊断名_原"));
                        try {
                            Integer tangNiaoAge = Integer.valueOf(strTangNiaoBTime.substring(0, 4)) - Integer.valueOf(jsonBaseInfo.getString("出生年"));
                            row.createCell(15).setCellValue(tangNiaoAge);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    String strTangNiaoBFenZu = "";
                    if (!strTangNiaoBTime.equals("")) {
                        if (strTangNiaoBTime.compareTo(sleMtime) > 0)
                            strTangNiaoBFenZu = "1";
                        else strTangNiaoBFenZu = "2";
                    }
                    row.createCell(16).setCellValue(strTangNiaoBFenZu);
                    //肾损害分组

                    JSONObject docuShengSunHai = getShengShunHaiInfo(strPID, sleBcZhongDian + " 00:00:00", mapShengyanHYInfo);
                    String strShengShunHaiFenZu = "";
                    if (docuShengSunHai != null) {
                        row.createCell(17).setCellValue(docuShengSunHai.getString("shunhaiTime"));
                        row.createCell(18).setCellValue(docuShengSunHai.getString("RID"));
                        row.createCell(19).setCellValue(docuShengSunHai.getString("ShunHaiName"));
                        try {
                            Integer strShengShunHaiAge = Integer.valueOf(docuShengSunHai.getString("shunhaiTime").substring(0, 4)) - Integer.valueOf(jsonBaseInfo.getString("出生年"));
                            row.createCell(20).setCellValue(strShengShunHaiAge);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (docuShengSunHai.getString("shunhaiTime").compareTo(sleMtime) > 0)
                            strShengShunHaiFenZu = "1";
                        else strShengShunHaiFenZu = "2";
                        row.createCell(21).setCellValue(strShengShunHaiFenZu);
                    }
                    //狼疮性肾炎
                    JSONObject docuLangCShengYan = getLangCShengYanInfo(strPID, sleBcZhongDian + " 00:00:00");
                    String strLangCShengYanFenZu = "";

                    if (docuLangCShengYan != null) {
                        row.createCell(22).setCellValue(docuLangCShengYan.getString("shunhaiTime"));
                        row.createCell(23).setCellValue(docuLangCShengYan.getString("实体"));
                        row.createCell(24).setCellValue(docuLangCShengYan.getString("状态"));
                        row.createCell(25).setCellValue(docuLangCShengYan.getString("RID"));
                        row.createCell(26).setCellValue(docuLangCShengYan.getString("ShunHaiName"));
                        try {
                            Integer strShengShunHaiAge = Integer.valueOf(docuLangCShengYan.getString("shunhaiTime").substring(0, 4)) - Integer.valueOf(jsonBaseInfo.getString("出生年"));
                            row.createCell(27).setCellValue(strShengShunHaiAge);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (docuLangCShengYan.getString("shunhaiTime").compareTo(sleMtime) > 0)
                            strLangCShengYanFenZu = "1";
                        else strLangCShengYanFenZu = "2";
                        row.createCell(28).setCellValue(strLangCShengYanFenZu);
                    }
                    if (strLangCShengYanFenZu.equals("2") || strShengShunHaiFenZu.equals("2") || strTangNiaoBFenZu.equals("2") || sleBCFenZu.equals("2"))
                        row.createCell(29).setCellValue(0);
                    else row.createCell(29).setCellValue(1);

                    System.out.println("process:" + RowNum);
                }
                saveExcelTool.saveExcel("交付/狼疮肾炎入组表-" + k + "-" + m + ".xlsx");
            }
        }
    }
    private static JSONObject getLangCShengYanInfo(String strPID,String strGuanCQiZhongDian)
    {
        String strNewTime="first";
        JSONObject obj = new JSONObject();
        if(mapLCShengyanZZInfo.containsKey(strPID))
        {
            Document dd=mapLCShengyanZZInfo.get(strPID);
            if(strNewTime.compareTo(dd.getString("症状&体征时间"))>0 &&dd.getString("症状&体征时间").compareTo(strGuanCQiZhongDian)<=0)
            {
                strNewTime=dd.getString("症状&体征时间");
           //     obj.put("shunhaiTime",dd.getString("症状&体征时间"));
                obj.put("RID",dd.getString("RID"));
                obj.put("实体","症状");
                obj.put("状态",dd.getString("否定词"));
                obj.put("ShunHaiName",dd.getString("症状组合"));
            }
        }
        if(mapLCShengyanTZInfo.containsKey(strPID))
        {
            Document dd=mapLCShengyanTZInfo.get(strPID);
            if(strNewTime.compareTo(dd.getString("症状&体征时间"))>0 &&dd.getString("症状&体征时间").compareTo(strGuanCQiZhongDian)<=0)
            {
                strNewTime=dd.getString("症状&体征时间");
           //     obj.put("shunhaiTime",dd.getString("症状&体征时间"));
                obj.put("RID",dd.getString("RID"));
                obj.put("实体","体征");
                obj.put("状态",dd.getString("否定词"));
                obj.put("ShunHaiName",dd.getString("体征组合"));
            }
        }
        if(mapLCShengyanZDInfo.containsKey(strPID))
        {
            Document dd=mapLCShengyanZDInfo.get(strPID);
            if(strNewTime.compareTo(dd.getString("诊断时间"))>0 &&dd.getString("诊断时间").compareTo(strGuanCQiZhongDian)<=0)
            {
                strNewTime=dd.getString("诊断时间");
           //     obj.put("shunhaiTime",dd.getString("诊断时间"));
                obj.put("RID",dd.getString("RID"));
                obj.put("实体","诊断");
                obj.put("状态",dd.getString("诊断状态"));
                obj.put("ShunHaiName",dd.getString("标准诊断名_原"));
            }
        }
        if(mapLCShengyanHYInfo.containsKey(strPID))
        {
            Document dd=mapLCShengyanHYInfo.get(strPID);
            if(strNewTime.compareTo(dd.getString("化验时间"))>0 &&dd.getString("化验时间").compareTo(strGuanCQiZhongDian)<=0)
            {
                strNewTime=dd.getString("化验时间");
          //      obj.put("shunhaiTime",dd.getString("化验时间"));
                obj.put("实体","化验");
                obj.put("状态",dd.getString("RPG科研结果转换"));
                obj.put("RID",dd.getString("RID"));
                obj.put("ShunHaiName",dd.getString("化验名称_原"));
            }
        }

        if(strNewTime.equals("first"))
            return null;
        obj.put("shunhaiTime", DateFormat.getDateFormatDay(strNewTime));
        return obj;
    }

    private static void getShengyanZDInfo(MongoDatabase mdb)
    {
        MongoCollection<Document> mci = mdb.getCollection("ADI");
        ArrayList<Document> aggregates = new ArrayList<Document>();
        aggregates.add(new Document("$match",Document.parse(BaseInfo_Title_ListValue_DBCondition.strShengYanZDCondition)));
        aggregates.add(new Document("$sort",Document.parse("{'诊断时间':1}")));
        aggregates.add(new Document("$group",Document.parse("{'_id':{'PID':'$PID'}, 'result':{'$first':'$$ROOT'}}")));
        MongoCursor<Document> cursor =mci.aggregate(aggregates).allowDiskUse(true).iterator();
        while (cursor.hasNext())
        {
            Document dd=cursor.next();
            String strPid=((Document)dd.get("_id")).getString("PID");
            Document document=(Document) dd.get("result");
            mapShengyanZDInfo.put(strPid,document);
        }
    }
    private static void getLCShengyanHYInfo(MongoDatabase mdb,Map<String,Document> mapResult,String ShenYanList,String strHyJieType)
    {
        if(ShenYanList.equals(""))
            return;
        MongoCollection<Document> mci = mdb.getCollection("ALA");
        ArrayList<Document> aggregates = new ArrayList<Document>();
        aggregates.add(new Document("$match",Document.parse(BaseInfo_Title_ListValue_DBCondition.strHYCondition+strHyJieType+"}")));
        aggregates.add(new Document("$project",Document.parse("{'化验组合':{'$concat':['$标准化验名','$标准标本']},'PID':'$PID','化验时间':'$化验时间','RID':'$RID','化验名称_原':'$化验名称_原','化验结果定性（新）':'$化验结果定性（新）','RPG科研结果转换':'$RPG科研结果转换'}")));
        aggregates.add(new Document("$match",Document.parse("{'化验组合':{$in:["+ShenYanList+"]}}")));
        aggregates.add(new Document("$sort",Document.parse("{'化验时间':1}")));
        aggregates.add(new Document("$group",Document.parse("{'_id':{'PID':'$PID'}, 'result':{'$first':'$$ROOT'}}")));
        MongoCursor<Document> cursor =mci.aggregate(aggregates).allowDiskUse(true).iterator();
        while (cursor.hasNext())
        {
            Document dd=cursor.next();
            String strPid=((Document)dd.get("_id")).getString("PID");
            Document document=(Document) dd.get("result");
            mapResult.put(strPid,document);
        }
    }

    private static JSONObject getShengShunHaiInfo(String strPID,String strGuanCQiZhongDian,Map<String,Document> mapHyShenyan)
    {
      //  System.out.println("getShengShunHaiOrLangCShengYanInfo");
        String firstTime="first";
        JSONObject objResult = new JSONObject();

        if(mapShengyanZDInfo.containsKey(strPID))
        {
            Document dd=mapShengyanZDInfo.get(strPID);
            if(dd.getString("诊断时间") !=null  && dd.getString("诊断时间").compareTo(strGuanCQiZhongDian) <=0) {
                firstTime = dd.getString("诊断时间");
                objResult.put("shunhaiTime", DateFormat.getDateFormatDay(firstTime));
                objResult.put("RID", dd.getString("RID"));
                objResult.put("ShunHaiName", dd.getString("标准诊断名_原"));
            }
        }
        //化验取最早时间

        if(mapHyShenyan.containsKey(strPID))
        {
            Document dd =mapHyShenyan.get(strPID);
            if(firstTime.compareTo(dd.getString("化验时间"))>0 &&dd.getString("化验时间").compareTo(strGuanCQiZhongDian) <=0)
            {
               if(dd.getString("化验时间") !=null) {
                   firstTime = dd.getString("化验时间");
                   objResult.put("shunhaiTime", DateFormat.getDateFormatDay(firstTime));
                   objResult.put("RID", dd.getString("RID"));
                   objResult.put("ShunHaiName", dd.getString("化验名称_原"));
               }
            }
        }

        if(!firstTime.equals("first"))
            return objResult;

        return  null;

    }

    private static void getBasicInfo(MongoDatabase mdb) throws Exception
    {
        getEntityList();
        getZZLangCShengyan(mdb);
        getTZLangCShengyan(mdb);
        ReadFromExcelToMap.readFromExcelToMap(mapExcludePID, LocalHostInfo.getPath()+"交付/移除组PID列表.xlsx","PID");
   //     ReadFromExcelToMap.readFromExcelToMap(mapExcludePID,LocalHostInfo.getPath()+"/移除组PID列表-抽检.xlsx","PID");
        ReadFromExcelToMap.readFromExcelToMap(mapBasicInfo, LocalHostInfo.getPath()+"交付/PID验证列表.xlsx","患者（PID）",true);
        getZDSLEPerson(mdb,BaseInfo_Title_ListValue_DBCondition.strSLEZDLCCondition,mapSLELCPerSon);
        getZDSLEPerson(mdb,BaseInfo_Title_ListValue_DBCondition.strSLETangNiaoBCondition,mapTangNiaoB);
        if(strZDList.length()>0)
          getZDSLEPerson(mdb,BaseInfo_Title_ListValue_DBCondition.strZDLCShengyanCondition+",'标准诊断名':{$in:["+strZDList+"]}}",mapLCShengyanZDInfo);

    }


    private static void  getZZLangCShengyan(MongoDatabase mdb)
    {
        System.out.println("getZZLangCShengyan");
        if(strZZList.equals(""))
            return;
        MongoCollection<Document> mc = mdb.getCollection("ASY");
        ArrayList<Document> aggregates = new ArrayList<Document>();
        aggregates.add(new Document("$match",Document.parse(BaseInfo_Title_ListValue_DBCondition.strZZConditon)));
        aggregates.add(new Document("$project",Document.parse("{'症状组合':{'$concat':['$部位1','$症状1']},'PID':'$PID','症状&体征时间':'$症状&体征时间','RID':'$RID','否定词':'$否定词'}")));
        aggregates.add(new Document("$match",Document.parse("{'症状组合':{$in:["+strZZList+"]}}")));
        aggregates.add(new Document("$sort",Document.parse("{'症状&体征时间':1}")));
        aggregates.add(new Document("$group",Document.parse("{'_id':{'PID':'$PID'}, 'result':{'$first':'$$ROOT'}}")));
        MongoCursor<Document> cursor =mc.aggregate(aggregates).allowDiskUse(true).iterator();
        Document dd=null;
        while (cursor.hasNext())
        {
            dd=(Document) cursor.next().get("result");
            if(mapExcludePID.containsKey(dd.getString("PID")))
                continue;
            mapLCShengyanZZInfo.put(dd.getString("PID"),dd);
        }
    }
    private static void  getTZLangCShengyan(MongoDatabase mdb)
    {
        System.out.println("getTZLangCShengyan");
        if(strTZList.equals(""))
            return;
        MongoCollection<Document> mc = mdb.getCollection("ASY");
        ArrayList<Document> aggregates = new ArrayList<Document>();
        aggregates.add(new Document("$match",Document.parse(BaseInfo_Title_ListValue_DBCondition.strTZConditon)));
        aggregates.add(new Document("$project",Document.parse("{'体征组合':{'$concat':['$部位1','$体征','$体征定性描述','$体征定量描述','$体征定量单位']},'PID':'$PID','症状&体征时间':'$症状&体征时间','RID':'$RID','否定词':'$否定词'}")));
        aggregates.add(new Document("$match",Document.parse("{'体征组合':{$in:["+strTZList+"]}}")));
        aggregates.add(new Document("$sort",Document.parse("{'症状&体征时间':1}")));
        aggregates.add(new Document("$group",Document.parse("{'_id':{'PID':'$PID'}, 'result':{'$first':'$$ROOT'}}")));
        MongoCursor<Document> cursor =mc.aggregate(aggregates).allowDiskUse(true).iterator();
        Document dd;
        while (cursor.hasNext())
        {
            dd=(Document) cursor.next().get("result");
            if(mapExcludePID.containsKey(dd.getString("PID")))
                continue;
            mapLCShengyanTZInfo.put(dd.getString("PID"),dd);
        }
    }
    public static String getJSonValue(JSONObject jsonObject,String key)
    {
        if(jsonObject == null || key == null)return "";
        if(jsonObject.getString(key) ==null)
            return "";
        return jsonObject.getString(key);
    }
    private static void getEntityList() throws Exception
    {
        JSONObject document=null;
        String fileName= LocalHostInfo.getPath()+BaseInfo_Title_ListValue_DBCondition.strCLeiJiFenZuFileName;;
        JSONObject config = new JSONObject();
        String tempFenZu,tempZuHe,tempEntityName;
        config.put("filename", fileName);
        config.put("source_type", "excel");
        DSExcelReader2 excelReader = new DSExcelReader2(config);
        while ((document = excelReader.nextDocument()) != null) {
            tempFenZu =getJSonValue(document,"对标观察项目");
            tempZuHe=getJSonValue(document,"表型名称")+getJSonValue(document,"标准标本");
            tempEntityName=getJSonValue(document,"表型");
            if(tempFenZu.equals("肾炎")&&!tempZuHe.equals("") &&tempEntityName.equals("标准诊断名"))
            {
              strZDList +="'"+tempZuHe+"',";
            }
            if(tempFenZu.equals("肾炎")&&!tempZuHe.equals("") &&tempEntityName.equals("化验组合"))
            {
                strHYList +="'"+tempZuHe+"',";
            }
            if(tempFenZu.equals("肾炎")&&!tempZuHe.equals("") &&tempEntityName.equals("症状组合"))
            {
                strZZList +="'"+tempZuHe+"',";
            }
            if(tempFenZu.equals("肾炎")&&!tempZuHe.equals("") &&tempEntityName.equals("体征组合"))
            {
                strTZList +="'"+tempZuHe+"',";
            }
        }
        if(strZDList.length()>0)
            strZDList=strZDList.substring(0,strZDList.length()-1);
        if(strHYList.length()>0)
            strHYList=strHYList.substring(0,strHYList.length()-1);
        if(strZZList.length()>0)
            strZZList=strZZList.substring(0,strZZList.length()-1);
        if(strTZList.length()>0)
          strTZList=strTZList.substring(0,strTZList.length()-1);

    }
    private static void  getZDSLEPerson(MongoDatabase mdb,String Condition, Map<String,Document> mapResult)
    {
        System.out.println("GetZDSLEPerson");
        MongoCollection<Document> mc = mdb.getCollection("ADI");
        ArrayList<Document> aggregates = new ArrayList<Document>();
        aggregates.add(new Document("$match",Document.parse(Condition)));
        aggregates.add(new Document("$sort",Document.parse("{'诊断时间':1}")));
        aggregates.add(new Document("$group",Document.parse("{'_id':{'PID':'$PID'}, 'result':{'$first':'$$ROOT'}}")));
        MongoCursor<Document> cursor =mc.aggregate(aggregates).allowDiskUse(true).iterator();
        Document dd;
        while (cursor.hasNext())
        {
            dd=(Document) cursor.next().get("result");
            if(mapExcludePID.containsKey(dd.getString("PID")))
                continue;
            JSONObject obj= new JSONObject();
            obj.put("标准诊断名",dd.getString("标准诊断名"));
            obj.put("RID",dd.getString("RID"));
            obj.put("诊断时间",dd.getString("诊断时间"));
            obj.put("诊断状态",dd.getString("诊断状态"));
            obj.put("标准诊断名_原",dd.getString("标准诊断名_原"));
            mapResult.put(dd.getString("PID"),dd);
        }
    }

    private static String getFirstLastRIDDay(MongoDatabase dbHDP,String PID,boolean flag)
    {
        MongoCollection<Document> mc = dbHDP.getCollection("ARB");
        ArrayList<Document> aggregates = new ArrayList<Document>();
        String result="";
        String strARBCondition="{'PID': '"+PID+"','记录时间戳':{$exists:true,$regex:/^.{10,}$/}"+BaseInfo_Title_ListValue_DBCondition.ADO13+"}";
        aggregates.add(new Document("$match",Document.parse(strARBCondition)));
        aggregates.add(new Document("$group",Document.parse("{'_id':{'记录时间戳':'$记录时间戳'}}")));
        if(flag)
            aggregates.add(new Document("$sort",Document.parse("{'_id.记录时间戳':-1}")));
        else
            aggregates.add(new Document("$sort",Document.parse("{'_id.记录时间戳':1}")));
        aggregates.add(new Document("$limit",1));
        MongoCursor<Document> cursor =mc.aggregate(aggregates).allowDiskUse(true).iterator();
        while (cursor.hasNext())
        {
            Document dd= (Document)cursor.next().get("_id");
            result=dd.getString("记录时间戳");
        }
        return result;
    }

}
