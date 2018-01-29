package test.java.task_SLE_QueZzhengLeiJi;

import com.RuiShiKeYan.Common.Method.DateFormat;
import com.RuiShiKeYan.Common.Method.LocalHostInfo;
import com.RuiShiKeYan.Common.Method.SaveExcelTool;
import com.alibaba.fastjson.JSONObject;
import com.RuiShiKeYan.dao.SSHLocalForward;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
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
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA
 * User:huangming
 * Date:2017/10/12
 * Time:下午10:26
 */
public class ExportQueZhenTable {

    static   Map<String,JSONObject> mapExceptPID ;
    static  Map<String,String> mapExceptPIDQueZh = new HashMap<String, String>();
    static  String strBiaoZhuFlag="";
    public  static  void main(String[] args) throws Exception
    {
        System.out.println("Start:");
        strBiaoZhuFlag=args[1];
        Map<String,Map<String,JSONObject>> mapResult = new HashMap<String,Map<String,JSONObject>>();
        fillExceptPID();
        GetResult(mapResult);
        Map<String,String > mapSleBxianXiTongFenzu=ExportTables_SLE.getExcludeInfo();
        writeShouFaTableToExcel(mapResult, "{"+BaseInfo_Title_ListValue_DBCondition.ADO13+"}",mapSleBxianXiTongFenzu);
        ExportBiaoJiTable.SaveBiaoZhuTable(args[0]);
        saveExceptPID();  //保存移除的PID
        System.out.println("OK");
    }

    private static void saveExceptPID()
    {
        SaveExcelTool saveExcelTool= new SaveExcelTool();
        SXSSFSheet sheet = saveExcelTool.getSheet("");
        saveExcelTool.fillExcelTitle("移出步骤,PID");
        int RowNum=1;
        for(Map.Entry<String,JSONObject> map:mapExceptPID.entrySet())
        {
          Row row = sheet.createRow(RowNum++);
          row.createCell(0).setCellValue(map.getValue().getString("移出步骤"));
          row.createCell(1).setCellValue(map.getKey());
        }

        for(Map.Entry<String,String> map:mapExceptPIDQueZh.entrySet())
        {
            Row row = sheet.createRow(RowNum++);
            row.createCell(0).setCellValue(map.getValue());
            row.createCell(1).setCellValue(map.getKey());
        }
        saveExcelTool.saveExcel("交付/移除组PID列表.xlsx");
    }
    public static  Map<String,JSONObject>   fillExceptPID() throws Exception
    {
        mapExceptPID= new HashMap<String, JSONObject>();

        String fileYC1= LocalHostInfo.getPath()+"交付/移除组PID列表.xlsx";
        JSONObject document = null;
        JSONObject config = new JSONObject();
        config.put("filename", fileYC1);
        config.put("source_type", "excel");

        DSExcelReader2 excelReader = new DSExcelReader2(config);
        while((document=excelReader.nextDocument()) != null) {
            mapExceptPID.put(document.getString("PID"),document);
        }
//        config.put("filename", fileYC2);
//        excelReader = new DSExcelReader2(config);
//        while((document=excelReader.nextDocument()) != null) {
//            mapExceptPID.put(document.getString("PID"),document);
//        }
        return mapExceptPID;
    }


   private  static void writeShouFaTableToExcel(Map<String,Map<String,JSONObject>> mapResult,String strADOCondition,Map<String,String > mapSleBxianXiTongFenzu) throws Exception
   {
       int RowNum=1;
       SXSSFWorkbook sxssfWorkbook = new SXSSFWorkbook(2000);
       SXSSFSheet sheet = sxssfWorkbook.createSheet();

       fillExcelTitle(sheet,"医院,科室,患者（PID）,症状病历（RID）,症状锚点,症状组合,症状时间,症状时间天,体征病历（RID）,体征锚点,体征组合,体征时间,体征时间天"+
               ",化验病历（RID）,化验上下文,化验组,化验组样本,化验名称,化验名称样本,标准化验名,标准标本,化验结果（定量）,化验结果（定性）,化验结果定性（新）,化验锚点,化验时间,化验时间天,初发表型,初发时间天,初发时间年,出生年取值RID,出生年,初发时间年减去出生年,诊断病历（RID）,诊断锚点,标准诊断名_原,诊断时间天,诊断时间年,诊断时间年减去出生年,诊断时间天减去初发时间天,SLE表现系统分组,性别,地域");
       Map<String,String> mapADO =ExportADO(strADOCondition);
        for(Map.Entry<String,Map<String,JSONObject>> tempMap: mapResult.entrySet()) {
            String strPID = tempMap.getKey();
            if(mapExceptPID.get(strPID) !=null)
                continue;
            String[] tempage={"",""};
            if(mapADO.get(strPID) !=null) {
                tempage = mapADO.get(strPID).split("\\|");
            }

            Map<String, JSONObject> map = tempMap.getValue();

            JSONObject josonJudge=JudgeQueZheng(map,mapSleBxianXiTongFenzu);

            if(josonJudge ==null) {
                mapExceptPIDQueZh.put(strPID, "无初发表现");
                continue;
            }
            String firstTime=josonJudge.getString("firstTime");
            String strSleXiTongFenZ=josonJudge.getString("strSleXiTongFenZ");
            String tableType=josonJudge.getString("tableType");
            Integer age=1;
            try {
                age =Integer.valueOf(firstTime.substring(0, 4)) - Integer.valueOf(tempage[0]);
                if(age<0 ||age>100) {
                    mapExceptPIDQueZh.put(strPID, "初发时间异常");
                    continue;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (map.get("ZDResult") != null) {
                JSONObject jo = map.get("ZDResult");
                 String intDays= DateFormat.getDays(jo.getString("诊断时间天"), firstTime);
                 if(Integer.valueOf(intDays) <0)
                 {
                     mapExceptPIDQueZh.put(strPID, "确诊时间小于0");
                     continue;
                 }
            }
            if(strBiaoZhuFlag.equals("true")) {
                if (strSleXiTongFenZ == null || "".equals(strSleXiTongFenZ)) {
                    mapExceptPIDQueZh.put(strPID, "初发表现未标注");
                    continue;
                }
            }
            Row row = sheet.createRow(RowNum++);
            row.createCell(2).setCellValue(strPID);
            if (map.get("ZZResult") != null) {
                JSONObject jo = map.get("ZZResult");
                row.createCell(0).setCellValue(jo.getString("医院"));
                row.createCell(1).setCellValue(jo.getString("科室"));

                row.createCell(3).setCellValue(jo.getString("病历（RID）"));
                row.createCell(4).setCellValue(jo.getString("症状锚点"));
                row.createCell(5).setCellValue(jo.getString("症状组合"));
                row.createCell(6).setCellValue(jo.getString("症状&体征时间"));
                row.createCell(7).setCellValue(jo.getString("症状时间天"));
            }
            if (map.get("TZResult") != null) {
                JSONObject jo = map.get("TZResult");
                row.createCell(0).setCellValue(jo.getString("医院"));
                row.createCell(1).setCellValue(jo.getString("科室"));

                row.createCell(8).setCellValue(jo.getString("病历（RID）"));
                row.createCell(9).setCellValue(jo.getString("体征锚点"));
                row.createCell(10).setCellValue(jo.getString("体征组合"));
                row.createCell(11).setCellValue(jo.getString("症状&体征时间"));
                row.createCell(12).setCellValue(jo.getString("体征时间天"));

            }
            if (map.get("HYResult") != null) {
                JSONObject jo = map.get("HYResult");
                row.createCell(0).setCellValue(jo.getString("医院"));
                row.createCell(1).setCellValue(jo.getString("科室"));

                row.createCell(13).setCellValue(jo.getString("病历（RID）"));
                row.createCell(14).setCellValue(jo.getString("上下文"));

                row.createCell(15).setCellValue(jo.getString("化验组"));
                row.createCell(16).setCellValue(jo.getString("化验组样本"));
                row.createCell(17).setCellValue(jo.getString("化验名称"));
                row.createCell(18).setCellValue(jo.getString("化验名称样本"));
                row.createCell(19).setCellValue(jo.getString("标准化验名"));
                row.createCell(20).setCellValue(jo.getString("标准标本"));
                row.createCell(21).setCellValue(jo.getString("化验结果（定量）"));
                row.createCell(22).setCellValue(jo.getString("化验结果（定性）"));
                row.createCell(23).setCellValue(jo.getString("化验结果定性（新）"));
                row.createCell(24).setCellValue(jo.getString("化验锚点"));
                row.createCell(25).setCellValue(jo.getString("化验时间"));
                row.createCell(26).setCellValue(jo.getString("化验时间天"));

            }else
            {
                row.createCell(13).setCellValue("");
                row.createCell(14).setCellValue("");
                row.createCell(15).setCellValue("");
                row.createCell(16).setCellValue("");
                row.createCell(17).setCellValue("");
                row.createCell(18).setCellValue("");
                row.createCell(19).setCellValue("");
                row.createCell(20).setCellValue("");
                row.createCell(21).setCellValue("");
                row.createCell(22).setCellValue("");
                row.createCell(23).setCellValue("");
                row.createCell(24).setCellValue("");
                row.createCell(25).setCellValue("");
                row.createCell(26).setCellValue("");
            }

            row.createCell(27).setCellValue(tableType);
            row.createCell(28).setCellValue(firstTime);
            row.createCell(29).setCellValue(firstTime.substring(0, 4));

            //取出生年月
            if(mapADO.get(strPID) !=null) {
                row.createCell(30).setCellValue(tempage[1]);
                row.createCell(31).setCellValue(tempage[0]);
                try {
                    row.createCell(32).setCellValue(String.valueOf(age));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (map.get("ZDResult") != null) {
                JSONObject jo = map.get("ZDResult");
                row.createCell(0).setCellValue(jo.getString("医院"));
                row.createCell(1).setCellValue(jo.getString("科室"));

                row.createCell(33).setCellValue(jo.getString("病历（RID）"));
                row.createCell(34).setCellValue(jo.getString("诊断锚点"));
                row.createCell(35).setCellValue(jo.getString("标准诊断名_原"));
                row.createCell(36).setCellValue(jo.getString("诊断时间天"));
                row.createCell(37).setCellValue(jo.getString("诊断时间年"));
                row.createCell(38).setCellValue(jo.getString("诊断时间年减去出生年"));
                if (jo.getString("诊断时间天").length()>6) {
                    row.createCell(39).setCellValue(String.valueOf(DateFormat.getDays(jo.getString("诊断时间天"),firstTime)));
                }
                row.createCell(40).setCellValue(strSleXiTongFenZ);
                row.createCell(41).setCellValue(jo.getString("性别"));
                row.createCell(42).setCellValue(jo.getString("地域"));

            }
        }
       FileOutputStream fileOutputStream = new FileOutputStream(LocalHostInfo.getPath()+"交付/确诊表现表.xlsx");
       sxssfWorkbook.write(fileOutputStream);
       sxssfWorkbook.close();
       fileOutputStream.close();
   }

   private static JSONObject JudgeQueZheng(Map<String, JSONObject> map,Map<String,String > mapSleBxianXiTongFenzu)
   {
       JSONObject obj = new JSONObject();
       String HYtime = "", TZtime = "", ZZtime = "";
       String HyZh="",TzZh="",ZzZh="";
       if (map.get("ZZResult") != null) {
           JSONObject jo = map.get("ZZResult");
           ZZtime = jo.getString("症状时间天");
           ZzZh = jo.getString("症状组合");
       }
       if (map.get("TZResult") != null) {
           JSONObject jo = map.get("TZResult");
           TZtime = jo.getString("体征时间天");
           TzZh = jo.getString("体征组合");
       }
       if (map.get("HYResult") != null) {
           JSONObject jo = map.get("HYResult");
           HYtime = jo.getString("化验时间天") == null ? "" : jo.getString("化验时间天");
           HyZh = jo.getString("标准化验名") + jo.getString("标准标本");
       }
       //判断表型
       String tableType = "";
       String firstTime = "",strSleXiTongFenZ="";
       if (!ZZtime.equals("") &&ZZtime.length()>9) {
           tableType = "症状";
           firstTime = ZZtime;
           strSleXiTongFenZ=mapSleBxianXiTongFenzu.get(ZzZh);
       }
       if (!TZtime.equals("") &&TZtime.length()>9)
           if (firstTime.equals("") || TZtime.compareTo(firstTime) < 0) {
               tableType = "体征";
               firstTime = TZtime;
               strSleXiTongFenZ=mapSleBxianXiTongFenzu.get(TzZh);
           }
       if (!HYtime.equals("") &&HYtime.length()>9)
           if (firstTime.equals("") || HYtime.compareTo(firstTime) < 0) {
               tableType = "化验";
               firstTime = HYtime;
               strSleXiTongFenZ=mapSleBxianXiTongFenzu.get(HyZh);
           }
       if("".equals(firstTime))
           return null;

       obj.put("tableType",tableType);
       if(firstTime.length() >10)
         obj.put("firstTime",firstTime.substring(0,10));
       else
         obj.put("firstTime",firstTime);
       obj.put("strSleXiTongFenZ",strSleXiTongFenZ);
       return obj;
   }

    private static Map<String,String> ExportADO(String strADOCondition) throws Exception
    {
        Document dd=null;
        Map<String,String> map = new HashMap<String, String>();

        MongoClientURI uri = new MongoClientURI(LocalHostInfo.getUrl());
        MongoClient client = new MongoClient(uri);
        //本地通过跳板机跳转
        SSHLocalForward sshLocalForward = new SSHLocalForward("dds-bp1baff8ad4002a41.mongodb.rds.aliyuncs.com");
        if(LocalHostInfo.isLocation()) {
            sshLocalForward.connectSSH();
        }
        MongoDatabase db = client.getDatabase("HDP-live");

        MongoCollection<Document> mc = db.getCollection("ADO");
        MongoCursor<Document> cursor = mc.find(Document.parse(strADOCondition)).iterator();
        while(cursor.hasNext())
        {
            dd= cursor.next();
            if(dd.containsKey("PID") &&dd.containsKey("出生年")) {
                map.put(dd.getString("PID"),dd.getString("出生年")+"|"+dd.getString("出生年RID"));
            }
        }
        client.close();
        if(LocalHostInfo.isLocation()) {
            sshLocalForward.closeSSH();
        }
        return map;
    }
    private static void GetResult(Map<String,Map<String,JSONObject>> mapResult) throws Exception
    {
        String fileHY= LocalHostInfo.getPath()+"化验首发表.xlsx";
        String fileTZ= LocalHostInfo.getPath()+"体征首发表.xlsx";
        String fileZZ= LocalHostInfo.getPath()+"症状首发表.xlsx";
        String fileZD= LocalHostInfo.getPath()+"交付/首诊时间表.xlsx";
        JSONObject document = null;
        JSONObject config = new JSONObject();
        config.put("filename", fileHY);
        config.put("source_type", "excel");

        DSExcelReader2 excelReader = new DSExcelReader2(config);
        while((document=excelReader.nextDocument()) != null)
        {

            if(document.getString("患者（PID）") ==null)
                continue;
            Map<String,JSONObject>  map = new HashMap<String, JSONObject>();
            map.put("HYResult",document);
            mapResult.put(document.getString("患者（PID）"),map);
        }

        config.put("filename", fileTZ);
        excelReader = new DSExcelReader2(config);
        while((document=excelReader.nextDocument()) != null)
        {
            if(document.getString("患者（PID）") ==null)
                continue;
            String PID=document.getString("患者（PID）");

            if(mapResult.get(PID) ==null)
            {
                Map<String,JSONObject>  map = new HashMap<String, JSONObject>();
                map.put("TZResult",document);
                mapResult.put(PID,map);
            }else
            {
                Map<String,JSONObject>  tempmap=mapResult.get(PID);
                tempmap.put("TZResult",document);
            }
        }
        config.put("filename", fileZZ);
        excelReader = new DSExcelReader2(config);
        while((document=excelReader.nextDocument()) != null)
        {
            if(document.getString("患者（PID）") ==null)
                continue;
          //  if(mapZZ.get(document.getString("症状组合"))  ==null)
          //      continue;
            String PID=document.getString("患者（PID）");

            if(mapResult.get(PID) ==null)
            {
                Map<String,JSONObject>  map = new HashMap<String, JSONObject>();
                map.put("ZZResult",document);
                mapResult.put(PID,map);
            }else
            {
                Map<String,JSONObject>  tempmap=mapResult.get(PID);
                tempmap.put("ZZResult",document);
            }
        }

        config.put("filename", fileZD);
        excelReader = new DSExcelReader2(config);
        while((document=excelReader.nextDocument()) != null)
        {
            if(document.getString("患者（PID）") ==null)
                continue;
            String PID=document.getString("患者（PID）");

            if(mapResult.get(PID) ==null)
            {
                Map<String,JSONObject>  map = new HashMap<String, JSONObject>();
                map.put("ZDResult",document);
                mapResult.put(PID,map);
            }else
            {
                Map<String,JSONObject>  tempmap=mapResult.get(PID);
                tempmap.put("ZDResult",document);
            }
        }
    }


    public static void fillExcelTitle(SXSSFSheet sheet,String title)
    {
        String[] titles = title.split(",");
        Row row = sheet.createRow(0);
        for (int i = 0; i <titles.length ; i++) {
            row.createCell(i).setCellValue(titles[i]);
        }
    }
}
