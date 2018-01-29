package test.java.task_SLE_LangChuang;

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
 * Date:2017/11/12
 * Time:下午1:41
 */
public class ExportPIDSampleList {

    private static int RowNum=1;
    public static Map<String,String>  mapLeiJiFenZu = new HashMap<String, String>();
    public static Map<String,String>  mapLeiJiSubFenZu = new HashMap<String, String>();
    public static Map<String,String>  mapDuiBiaoFenZu = new HashMap<String, String>();
    public static void main(String[] args) throws Exception
    {

        MongoDBHelper mongoDBHelper= new MongoDBHelper("HDP-live");
        MongoDatabase mdb=mongoDBHelper.getDb();
        SaveExcelTool saveExcelTool = new SaveExcelTool();
        SXSSFSheet sheet = saveExcelTool.getSheet("");
        saveExcelTool.fillExcelTitle(BaseInfo_Title_ListValue_DBCondition.tiltlePIDChouYangB);
        getSubAndItemMap();
        writeToEXcel(mdb,sheet);
        saveExcelTool.saveExcel("交付/PID抽样实体表.xlsx");
        mongoDBHelper.closeMongoDb();
    }

    private static void writeToEXcel(MongoDatabase mdb,SXSSFSheet sheet) throws Exception
    {
        Map<String,String> mapPID = new HashMap<String, String>();
        ReadFromExcelToMap.readFromExcelToMap(mapPID, LocalHostInfo.getPath()+"PID抽样列表.xlsx","患者（PID）");
        Map<String, Document> mapADO=getADO(mdb);
        Map<String,String> mapBiaoZhu=getBiaoZhuInfo();
        for(Map.Entry<String,String> mappid:mapPID.entrySet())
        {
           ArrayList<Document> entityList=null;
           String strPID=mappid.getKey();
           Document ducumentAdo =mapADO.get(strPID);

            entityList=getFirstZD(mdb,strPID);
            fillExcell(sheet,entityList,ducumentAdo,mapBiaoZhu,strPID);

           entityList=getFirstZZ(mdb,strPID);
           fillExcell(sheet,entityList,ducumentAdo,mapBiaoZhu,strPID);

            entityList=getFirstTZ(mdb,strPID);
            fillExcell(sheet,entityList,ducumentAdo,mapBiaoZhu,strPID);

            entityList=getFirstHY(mdb,strPID);
            fillExcell(sheet,entityList,ducumentAdo,mapBiaoZhu,strPID);

            entityList=getFirstYY(mdb,strPID);
            fillExcell(sheet,entityList,ducumentAdo,mapBiaoZhu,strPID);
        }


    }
    private static void fillExcell(SXSSFSheet sheet,ArrayList<Document> entityList,Document ducumentAdo, Map<String,String> mapBiaoZhu,String strPid)
    {

        String strAge="",strSex="",strAgeRid="";
        if(ducumentAdo !=null)
        {
            strAge=ducumentAdo.getString("出生年");
            strSex=ducumentAdo.getString("性别");
            strAgeRid=ducumentAdo.getString("出生年取值RID");
        }
        for (int i = 0; i <entityList.size() ; i++) {
            Row row= sheet.createRow(RowNum++);
            System.out.println("Process:"+RowNum);
            Document dd= entityList.get(i);
            row.createCell(0).setCellValue(strPid);
            row.createCell(1).setCellValue(strSex);
            row.createCell(2).setCellValue(strAgeRid);
            row.createCell(3).setCellValue(strAge);
            row.createCell(4).setCellValue(dd.getString("type"));
            row.createCell(5).setCellValue(dd.getString("RID"));
            row.createCell(6).setCellValue(dd.getString("锚点"));
            row.createCell(7).setCellValue(dd.getString("实体名称"));
            row.createCell(8).setCellValue(dd.getString("实体标准名"));
            row.createCell(9).setCellValue(dd.getString("状态1"));
            row.createCell(10).setCellValue(dd.getString("状态2"));
            row.createCell(11).setCellValue(dd.getString("时间"));
            row.createCell(12).setCellValue(dd.getString("时间天"));
            try {
                Integer entityAge=Integer.valueOf(dd.getString("时间天").substring(0,4))-Integer.valueOf(strAge);
                row.createCell(13).setCellValue(String.valueOf(entityAge));
            }catch(Exception e){e.printStackTrace();}
            String strBiaoZ=mapBiaoZhu.get(dd.getString("实体标准名"))==null?"":mapBiaoZhu.get(dd.getString("实体标准名"));
            row.createCell(14).setCellValue(strBiaoZ);
            if(mapDuiBiaoFenZu.containsKey(dd.getString("实体标准名")))
             row.createCell(15).setCellValue(mapDuiBiaoFenZu.get(dd.getString("实体标准名")));
            if(mapLeiJiSubFenZu.containsKey(dd.getString("实体标准名")))
               row.createCell(16).setCellValue(mapLeiJiSubFenZu.get(dd.getString("实体标准名")));
            if(mapLeiJiFenZu.containsKey(dd.getString("实体标准名")))
                row.createCell(17).setCellValue(mapLeiJiFenZu.get(dd.getString("实体标准名")));
        }
    }

    private static void getSubAndItemMap() throws Exception {
        JSONObject document ;
        String fileName= LocalHostInfo.getPath()+BaseInfo_Title_ListValue_DBCondition.strCLeiJiFenZuFileName;
        String tempFenZu;
        JSONObject config = new JSONObject();
        config.put("filename", fileName);
        config.put("source_type", "excel");

        DSExcelReader2 excelReader = new DSExcelReader2(config);
        while ((document = excelReader.nextDocument()) != null) {
            tempFenZu = getJSonValue(document,"表型名称")+getJSonValue(document,"标准标本");
            if (!"".equals(tempFenZu)) {
                if (!"".equals(document.getString("拟观察系统累及分组")))
                    mapLeiJiFenZu.put(tempFenZu, document.getString("拟观察系统累及分组"));
                if (!"".equals(document.getString("子项")))
                    mapLeiJiSubFenZu.put(tempFenZu, document.getString("子项"));
                if (!"".equals(document.getString("对标观察项目")))
                    mapDuiBiaoFenZu.put(tempFenZu, document.getString("对标观察项目"));
            }
        }
    }
    public static String getJSonValue(JSONObject jsonObject,String key)
    {
        if(jsonObject == null || key == null)return "";
        if(jsonObject.getString(key) ==null)
            return "";
        return jsonObject.getString(key);
    }
    public static Map<String,String > getBiaoZhuInfo() throws Exception {

        String fileTZ = LocalHostInfo.getPath()+"初发表现标注表-体征.xlsx";
        String fileZZ = LocalHostInfo.getPath()+"初发表现标注表-症状.xlsx";
        String fileHY = LocalHostInfo.getPath()+"初发表现标注表-化验.xlsx";
        JSONObject document;
        Map<String,String > mapSleBxianXiTongFenzu= new HashMap<String, String>();

        JSONObject config = new JSONObject();
        config.put("filename", fileTZ);
        config.put("source_type", "excel");

        DSExcelReader2 excelReader = new DSExcelReader2(config);
        while ((document = excelReader.nextDocument()) != null) {
            if(!document.get("体征组合").toString().equals(""))
                mapSleBxianXiTongFenzu.put(document.get("体征组合").toString(),document.get("SLE表现系统分组").toString());
        }

        config.put("filename", fileZZ);
        excelReader = new DSExcelReader2(config);
        while ((document = excelReader.nextDocument()) != null) {
            if(!document.get("症状组合").toString().equals(""))
                mapSleBxianXiTongFenzu.put(document.get("症状组合").toString(),document.get("SLE表现系统分组").toString());
        }
        config.put("filename", fileHY);
        excelReader = new DSExcelReader2(config);
        while ((document = excelReader.nextDocument()) != null) {
            String tempStr="";
            if(document.get("标准化验名") !=null)
                tempStr=document.get("标准化验名").toString();
            if(document.get("标准标本") !=null)
                tempStr+=document.get("标准标本").toString();
            if(!tempStr.equals(""))
                mapSleBxianXiTongFenzu.put(tempStr,document.get("SLE表现系统分组").toString());

        }
        return  mapSleBxianXiTongFenzu;
    }

    public static Map<String, Document> getADO(MongoDatabase db) {
        Map<String, Document> map = new HashMap<String, Document>();
        MongoCollection<Document> mc = db.getCollection("ADO");
        MongoCursor<Document> cursor = mc.find(Document.parse("{"+BaseInfo_Title_ListValue_DBCondition.ADO13+"}")).iterator();
        while (cursor.hasNext()) {
            Document dd = cursor.next();
            Document obj= new Document();
            obj.put("出生年",dd.getString("出生年"));
            obj.put("出生年RID",dd.getString("出生年取值RID"));
            obj.put("性别",dd.getString("性别"));
            map.put(dd.getString("PID"), dd);
        }
        return map;
    }

    private  static ArrayList<Document> getFirstZD(MongoDatabase dbHDP,String strPId)
    {
        ArrayList<Document> arrADI = new ArrayList<Document>();
        MongoCollection<Document> mc = dbHDP.getCollection("ADI");
        MongoCursor<Document> cursor =mc.find(Document.parse("{"+BaseInfo_Title_ListValue_DBCondition.ZD13SLE+",'PID':'"+strPId+"'}")).iterator();
        while (cursor.hasNext())
        {
            Document  dd =cursor.next();
            Document obj = new Document();
            obj.put("type","诊断");
            obj.put("时间",dd.getString("诊断时间"));
            if(dd.getString("诊断时间").length() >10)
              obj.put("时间天",dd.getString("诊断时间").substring(0,10));
            else
              obj.put("时间天",dd.getString("诊断时间"));
            obj.put("RID",dd.getString("RID"));
            obj.put("锚点",dd.getString("段落标题"));
            obj.put("状态1",dd.getString("诊断状态"));
            obj.put("实体名称",dd.getString("标准诊断名_原"));
            obj.put("实体标准名",dd.getString("标准诊断名"));
            obj.put("状态2","");
            arrADI.add(obj);
        }
        return arrADI;
    }
    private static ArrayList<Document> getFirstZZ(MongoDatabase dbHDP,String strPId)
    {
        ArrayList<Document> arrZZ = new ArrayList<Document>();
        MongoCollection<Document> mc = dbHDP.getCollection("ASY");
        MongoCursor<Document> cursor =mc.find(Document.parse("{"+BaseInfo_Title_ListValue_DBCondition.ZZTZ13SLE+",'PID':'"+strPId+"','症状1':{$exists:true,$ne:''}}")).iterator();
        while (cursor.hasNext())
        {
            Document  dd =cursor.next();
            Document obj = new Document();
            obj.put("type","症状");
            obj.put("时间",dd.getString("症状&体征时间"));
            if(dd.getString("症状&体征时间").length() >10)
                obj.put("时间天",dd.getString("症状&体征时间").substring(0,10));
            else
                obj.put("时间天",dd.getString("症状&体征时间"));
            obj.put("RID",dd.getString("RID"));
            obj.put("锚点",dd.getString("段落标题"));
            obj.put("状态1",dd.getString("否定词"));
            obj.put("实体名称",dd.getString("部位1")+dd.getString("症状1"));
            obj.put("实体标准名",dd.getString("部位1")+dd.getString("症状1"));
            obj.put("状态2","");
            arrZZ.add(obj);
        }
        return arrZZ;
    }
    private static ArrayList<Document> getFirstTZ(MongoDatabase dbHDP,String strPId)
    {
        ArrayList<Document> arrZZ = new ArrayList<Document>();
        MongoCollection<Document> mc = dbHDP.getCollection("ASY");
        MongoCursor<Document> cursor =mc.find(Document.parse("{"+BaseInfo_Title_ListValue_DBCondition.ZZTZ13SLE+",'PID':'"+strPId+"','体征':{$exists:true,$ne:''}}")).iterator();
        while (cursor.hasNext())
        {
            Document  dd =cursor.next();
            Document obj = new Document();
            obj.put("type","体征");
            obj.put("时间",dd.getString("症状&体征时间"));
            if(dd.getString("症状&体征时间").length() >10)
                obj.put("时间天",dd.getString("症状&体征时间").substring(0,10));
            else
                obj.put("时间天",dd.getString("症状&体征时间"));
            String tempTZZhe=dd.getString("部位1")+dd.getString("体征")+dd.getString("体征定性描述")+dd.getString("体征定量描述")+dd.getString("体征定量单位");
            obj.put("RID",dd.getString("RID"));
            obj.put("锚点",dd.getString("段落标题"));
            obj.put("状态1",dd.getString("否定词"));
            obj.put("实体名称",tempTZZhe);
            obj.put("实体标准名",tempTZZhe);
            obj.put("状态2","");
            arrZZ.add(obj);
        }
        return arrZZ;
    }
    private static ArrayList<Document> getFirstHY(MongoDatabase dbHDP,String strPId)
    {
        ArrayList<Document> arrHY = new ArrayList<Document>();
        MongoCollection<Document> mc = dbHDP.getCollection("ALA");
        MongoCursor<Document> cursor =mc.find(Document.parse("{"+BaseInfo_Title_ListValue_DBCondition.HY13SLE+",'PID':'"+strPId+"'}")).iterator();
        while (cursor.hasNext())
        {
            Document  dd =cursor.next();
            Document obj = new Document();
            obj.put("type","化验");
            obj.put("时间",dd.getString("化验时间"));
            if(dd.getString("化验时间").length() >10)
                obj.put("时间天",dd.getString("化验时间").substring(0,10));
            else
                obj.put("时间天",dd.getString("化验时间"));
            obj.put("RID",dd.getString("RID"));
            obj.put("锚点",dd.getString("段落标题"));
            obj.put("状态1",dd.getString("化验结果定性（新）"));
            obj.put("实体名称",dd.getString("化验名称_原")+dd.getString("标准标本_原"));
            obj.put("实体标准名",dd.getString("标准化验名")+dd.getString("标准标本"));
            obj.put("状态2",dd.getString("RPG科研结果转换"));
            arrHY.add(obj);
        }
        return arrHY;
    }
    private static ArrayList<Document> getFirstYY(MongoDatabase dbHDP,String strPId)
    {
        ArrayList<Document> arrYY = new ArrayList<Document>();
        MongoCollection<Document> mc = dbHDP.getCollection("ADR");
        MongoCursor<Document> cursor =mc.find(Document.parse("{"+BaseInfo_Title_ListValue_DBCondition.ADR13+",'PID':'"+strPId+"'}")).iterator();
        while (cursor.hasNext())
        {
            Document  dd =cursor.next();
            Document obj = new Document();
            obj.put("type","用药");
            obj.put("时间",dd.getString("用药时间"));
            if(dd.getString("用药时间").length() >10)
                obj.put("时间天",dd.getString("用药时间").substring(0,10));
            else
                obj.put("时间天",dd.getString("用药时间"));
            obj.put("RID",dd.getString("RID"));
            obj.put("锚点",dd.getString("段落标题"));
            obj.put("状态1",dd.getString("是否使用"));
            obj.put("实体名称",dd.getString("通用名_原"));
            obj.put("实体标准名",dd.getString("通用名"));
            obj.put("状态2","");
            arrYY.add(obj);
        }
        return arrYY;
    }
}
