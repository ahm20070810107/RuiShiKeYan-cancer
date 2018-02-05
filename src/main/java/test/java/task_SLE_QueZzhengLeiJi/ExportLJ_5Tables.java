package test.java.task_SLE_QueZzhengLeiJi;

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
import test.java.task_SLE_LangChuang.BaseInfo_Title_ListValue_DBCondition;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA
 * User:huangming
 * Date:2017/10/18
 * Time:下午5:37
 */
public class ExportLJ_5Tables {

    static Map<String,JSONObject> mapExceptPID ;
    static Document dd;
    static Map<String,Integer> mapYY= new HashMap<String, Integer>();
    static Map<String,Integer> mapZZ= new HashMap<String, Integer>();
    static Map<String,Integer> mapTZ= new HashMap<String, Integer>();
    static Map<String,Integer> mapZD= new HashMap<String, Integer>();
    static Map<String,Integer> mapHY= new HashMap<String, Integer>();
    static Map<String,JSONObject> mapOldValue=new HashMap<String, JSONObject>();
    public static void main(String[] args)throws Exception
    {
      mapExceptPID= ExportQueZhenTable.fillExceptPID();
      exportLJ_5Tables();
    }

    public static void exportLJ_5Tables() throws Exception
    {
        MongoDBHelper mongoDBHelper= new MongoDBHelper();
        mongoDBHelper.getClient();
        MongoDatabase db = mongoDBHelper.getDb("HDP-live");
        String strZZCondition="{"+BaseInfo_Title_ListValue_DBCondition.ZZTZ13SLE+",'否定词':'','症状1':{$exists:true,$ne:''}}";
        String strZDCondition="{'诊断状态':'是','标准诊断名':{$exists:true,$ne:''},"+BaseInfo_Title_ListValue_DBCondition.ZD13SLE+"}";
        String strTZCondition="{"+BaseInfo_Title_ListValue_DBCondition.ZZTZ13SLE+",'$or':[{'体征':{$ne:''}},{'体征定性描述':{$ne:''}}]  }";
        String strALAConditon="{"+BaseInfo_Title_ListValue_DBCondition.HY13SLE+",'化验结果定性（新）':'阳性'}";//'化验结果定性（新）':'阳性'}";
        String strADRConditon="{'是否使用':'使用','通用名':{$exists:true,$ne:''},"+BaseInfo_Title_ListValue_DBCondition.ADR13+"}";//'用药时间':{$exists:true,$regex:/^.{10,}$/},
        exportZD(db,strZDCondition);
        exportZZ(db,strZZCondition);
        exportTZ(db,strTZCondition);
        exportHY(db,strALAConditon);
        exportYY(db,strADRConditon);
        readExcelValue();
        writeToExcel(BaseInfo_Title_ListValue_DBCondition.tiltleLeiJiFenZu);

        mongoDBHelper.closeMongoDb();
    }

    private static void readExcelValue() throws Exception
    {
        String fileName= LocalHostInfo.getPath()+BaseInfo_Title_ListValue_DBCondition.strCLeiJiFenZuFileName;
        String tempZuHe;
        JSONObject document;
        JSONObject config = new JSONObject();
        config.put("filename", fileName);
        config.put("source_type", "excel");
        DSExcelReader2 excelReader = new DSExcelReader2(config);
        while ((document = excelReader.nextDocument()) != null) {
            if(getJSonValue(document,"频次").equals(""))
                continue;
            if(getJSonValue(document,"标准标本").equals(""))
                tempZuHe=getJSonValue(document,"表型")+"|"+getJSonValue(document,"表型名称");
            else {
                tempZuHe = getJSonValue(document, "表型") + "|" + getJSonValue(document, "表型名称") + "|" + getJSonValue(document, "标准标本");
            }
            mapOldValue.put(tempZuHe,document);
        }
    }

    public static String getJSonValue(JSONObject jsonObject,String key)
    {
        if(jsonObject == null || key == null)return "";
        if(jsonObject.getString(key) ==null)
            return "";
        return jsonObject.getString(key);
    }
    private static void writeToExcel(String title)
    {
        SaveExcelTool excelTool = new SaveExcelTool();
        SXSSFSheet sheet = excelTool.getSheet("");
        excelTool.fillExcelTitle(title);
        int rowNum=1;
        rowNum=writeToExcelFromMap(rowNum,sheet,mapYY,"用药通用名");
        rowNum=writeToExcelFromMap(rowNum,sheet,mapZZ,"症状组合");
        rowNum=writeToExcelFromMap(rowNum,sheet,mapTZ,"体征组合");
        rowNum=writeToExcelFromMap(rowNum,sheet,mapZD,"标准诊断名");
        writeToExcelFromMap(rowNum,sheet,mapHY,"化验组合");

        excelTool.saveExcel("交付/系统累及分组标注表.xlsx");
    }

    private static int writeToExcelFromMap(int rowNum,SXSSFSheet sheet,Map<String,Integer> mapValue,String entityName)
    {
        for (Map.Entry<String,Integer> map :mapValue.entrySet())
        {
            Row row=sheet.createRow(rowNum++);
            System.out.println(entityName+":"+rowNum);

            row.createCell(0).setCellValue(map.getValue());
            row.createCell(1).setCellValue(entityName);
            if(entityName.equals("化验组合"))  //化验组合是两列
            {
                String[] values=getArray(map.getKey());
                row.createCell(2).setCellValue(values[0]);
                row.createCell(3).setCellValue(values[1]);
            }else {
                row.createCell(2).setCellValue(map.getKey());
                row.createCell(3).setCellValue("");
            }
            String tempZuHe=entityName+"|"+map.getKey();
            JSONObject jsonObject = mapOldValue.get(tempZuHe);
           if(jsonObject ==null) {
               row.createCell(4).setCellValue("");
               row.createCell(5).setCellValue("");
               row.createCell(6).setCellValue("");
               row.createCell(7).setCellValue("");
               row.createCell(8).setCellValue("");
               row.createCell(9).setCellValue("");
           }else
           {
               row.createCell(4).setCellValue(getJSonValue(jsonObject,"对标观察项目"));
               row.createCell(5).setCellValue(getJSonValue(jsonObject,"子项"));
               row.createCell(6).setCellValue(getJSonValue(jsonObject,"拟观察系统累及分组"));
               row.createCell(7).setCellValue(getJSonValue(jsonObject,"2017诊断评分一级"));
               row.createCell(8).setCellValue(getJSonValue(jsonObject,"2017诊断评分二级"));
               row.createCell(9).setCellValue(getJSonValue(jsonObject,"2017诊断评分标记"));
           }
        }
        return rowNum;
    }
    private  static void exportYY(MongoDatabase db,String strZZCondition)
    {
        MongoCollection<Document> mc = db.getCollection("ADR");
        MongoCursor<Document> cursor = mc.find(Document.parse(strZZCondition)).iterator();

        while (cursor.hasNext())
        {
            dd=cursor.next();
            if(mapExceptPID.containsKey(dd.getString("PID")))
                continue;

            if(mapYY.containsKey(dd.getString("通用名")))
            {
                Integer pc = mapYY.get(dd.getString("通用名"));
                mapYY.put(dd.getString("通用名"),pc+1);
            }else{
                mapYY.put(dd.getString("通用名"),1);
            }
        }
    }
    private static void exportZZ(MongoDatabase db,String strZZCondition) throws Exception
    {
        MongoCollection<Document> mc = db.getCollection("ASY");
        MongoCursor<Document> cursor = mc.find(Document.parse(strZZCondition)).iterator();

        while(cursor.hasNext())
        {
            dd=cursor.next();
            if(mapExceptPID.containsKey(dd.getString("PID")))
                continue;
            String strZZ=dd.getString("部位1")+dd.getString("症状1");
            if(!mapZZ.containsKey(strZZ))
            {
                mapZZ.put(strZZ,1);
            }
            else
            {
                Integer pc = mapZZ.get(strZZ);
                mapZZ.put(strZZ,pc+1);
            }
        }
    }
    private static void exportTZ(MongoDatabase db,String strZZCondition) throws Exception
    {
        MongoCollection<Document> mc = db.getCollection("ASY");
        MongoCursor<Document> cursor = mc.find(Document.parse(strZZCondition)).iterator();

        while(cursor.hasNext())
        {
            dd=cursor.next();
            if(mapExceptPID.containsKey(dd.getString("PID")))
                continue;
            String strTZ=dd.getString("部位1")+dd.getString("否定词")+dd.getString("体征")+dd.getString("体征定性描述")+dd.getString("体征定量描述")+dd.getString("体征定量单位");
            if(!mapTZ.containsKey(strTZ))
            {
                mapTZ.put(strTZ,1);
            }
            else
            {
                Integer pc = mapTZ.get(strTZ);
                mapTZ.put(strTZ,pc+1);
            }
        }
    }

    private static void exportZD(MongoDatabase db,String strZDCondition) throws Exception
    {
        MongoCollection<Document> mc = db.getCollection("ADI");
        MongoCursor<Document> cursor = mc.find(Document.parse(strZDCondition)).iterator();

        while(cursor.hasNext())
        {
            dd=cursor.next();
            if(mapExceptPID.containsKey(dd.getString("PID")))
                continue;

           if(!mapZD.containsKey(dd.getString("标准诊断名")))
           {
               mapZD.put(dd.getString("标准诊断名"),1);
           }
           else
           {
               Integer pc = mapZD.get(dd.getString("标准诊断名"));
               mapZD.put(dd.getString("标准诊断名"),pc+1);
           }
        }
    }

    private static void exportHY(MongoDatabase db,String strHYCondition)
    {
        MongoCollection<Document> mc = db.getCollection("ALA");
        MongoCursor<Document> cursor = mc.find(Document.parse(strHYCondition)).iterator();
        while(cursor.hasNext())
        {

            dd=cursor.next();
            if(mapExceptPID.containsKey(dd.getString("PID")))
                continue;

            String tempStr="";
            tempStr=dd.getString("标准化验名")+"|"+dd.getString("标准标本");
            if(tempStr.equals("|"))
                continue;
            if(!mapHY.containsKey(tempStr))
            {
                mapHY.put(tempStr,1);
            }
            else
            {
                Integer pc = mapHY.get(tempStr);
                mapHY.put(tempStr,pc+1);
            }
        }
    }

    private static String[] getArray(String source)
    {
        String[] strArr=new String[3];
        String tempStr="";
        for (int i = 0; i < source.length(); i++) {
            if(source.charAt(i) !='|')
                tempStr+=source.charAt(i);
            else
            {
                source=source.substring(i+1,source.length());
                break;
            }
        }
        strArr[0]=tempStr;
        tempStr="";
        for (int i = 0; i < source.length(); i++) {
            if(source.charAt(i) !='|')
                tempStr+=source.charAt(i);
            else
            {
                source=source.substring(i+1,source.length());
                break;
            }
        }
        strArr[1]=tempStr;
        tempStr="";
        for (int i = 0; i < source.length(); i++) {
            if(source.charAt(i) !='|')
                tempStr+=source.charAt(i);
        }
        strArr[2]=tempStr;

        return  strArr;
    }
}
