package test.java.task_SLE_QueZzhengLeiJi;

import com.RuiShiKeYan.Common.Method.LocalHostInfo;
import com.alibaba.fastjson.JSONObject;
import com.yiyihealth.data.DaX.reader.DSExcelReader2;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA
 * User:huangming
 * Date:2017/10/14
 * Time:上午10:57
 */
public class ExportBiaoJiTable {
    static   Map<String,JSONObject> mapTZ = new HashMap<String, JSONObject>();
    static   Map<String,JSONObject> mapZZ = new HashMap<String, JSONObject>();
    static   Map<String,JSONObject> mapHY = new HashMap<String, JSONObject>();
    static String strVersion="";
    static   Map<String,JSONObject> mapTZNew = new HashMap<String, JSONObject>();
    static   Map<String,JSONObject> mapZZNew = new HashMap<String, JSONObject>();
    static   Map<String,JSONObject> mapHYNew = new HashMap<String, JSONObject>();

    public static void SaveBiaoZhuTable(String stri) throws Exception
     {
         strVersion=stri;
         String fileQZBXB = LocalHostInfo.getPath()+"交付/确诊表现表.xlsx";
         String fileTZ = LocalHostInfo.getPath()+"交付/初发表现标注表-体征.xlsx";
         String fileZZ = LocalHostInfo.getPath()+"交付/初发表现标注表-症状.xlsx";
         String fileHY = LocalHostInfo.getPath()+"交付/初发表现标注表-化验.xlsx";
         getOldBiaoZhuInfo();
         getBiaoJiFromSFTable(fileQZBXB);
         writeToExcel(fileTZ,fileZZ,fileHY);
     }
    private static void getOldBiaoZhuInfo() throws Exception {
        String fileTZ = LocalHostInfo.getPath()+"初发表现标注表-体征.xlsx";
        String fileZZ = LocalHostInfo.getPath()+"初发表现标注表-症状.xlsx";
        String fileHY = LocalHostInfo.getPath()+"初发表现标注表-化验.xlsx";
        JSONObject document = null;

        JSONObject config = new JSONObject();
        config.put("filename", fileTZ);
        config.put("source_type", "excel");
        try {
            DSExcelReader2 excelReader = new DSExcelReader2(config);
            while ((document = excelReader.nextDocument()) != null) {
                if (!document.get("体征组合").equals(""))
                    mapTZ.put(document.get("体征组合").toString(), document);
            }

            config.put("filename", fileZZ);
            excelReader = new DSExcelReader2(config);
            while ((document = excelReader.nextDocument()) != null) {
                if (!document.get("症状组合").equals(""))
                    mapZZ.put(document.get("症状组合").toString(), document);
            }
            config.put("filename", fileHY);
            excelReader = new DSExcelReader2(config);
            while ((document = excelReader.nextDocument()) != null) {
                String tempStr = "";
                if (document.get("标准化验名") != null)
                    tempStr = document.get("标准化验名").toString();
                if (document.get("标准标本") != null)
                    tempStr += document.get("标准标本").toString();
                if (!tempStr.equals("")) {
                    mapHY.put(tempStr, document);
                }
            }
        }catch (Exception e){e.printStackTrace();}
    }

    private static void getBiaoJiFromSFTable(String fileQZBXB ) throws Exception
    {

        JSONObject document = null;

        JSONObject config = new JSONObject();
        config.put("filename", fileQZBXB);
        config.put("source_type", "excel");

        DSExcelReader2 excelReader = new DSExcelReader2(config);
        while ((document = excelReader.nextDocument()) != null) {
            String strSF=document.getString("初发表型");

            if(strSF !=null &&strSF.equals("症状")) {
                if (document.containsKey("症状组合") && !document.get("症状组合").toString().equals("") && mapZZ.get(document.get("症状组合").toString()) == null)
                    mapZZNew.put(document.get("症状组合").toString(), document);
            }
            if(strSF !=null &&strSF.equals("体征")) {
                if (document.containsKey("体征组合") && !document.get("体征组合").toString().equals("") && mapTZ.get(document.get("体征组合").toString()) == null)
                    mapTZNew.put(document.get("体征组合").toString(), document);
            }
            if(strSF !=null &&strSF.equals("化验")) {
                String tempStr="";
                if(document.get("标准化验名") !=null)
                    tempStr=document.get("标准化验名").toString();
                if(document.get("标准标本") !=null)
                    tempStr+=document.get("标准标本").toString();
                if (!tempStr.equals("") && mapHY.get(tempStr) == null) {
                    mapHYNew.put(tempStr, document);
                }
            }
        }

    }
    private static void writeToExcel(String fileTZ,String fileZZ,String fileHY) throws Exception
    {

        writeMapToExcel(fileTZ,"TZ","版本,体征组合,SLE表现系统分组");
        writeMapToExcel(fileZZ,"ZZ","版本,症状组合,SLE表现系统分组");
        writeMapToExcel(fileHY,"HY","版本,标准化验名,标准标本,SLE表现系统分组");
    }

    private static void writeMapToExcel(String filePath,String key,String tile) throws Exception
    {
        SXSSFWorkbook sxssfWorkbook = new SXSSFWorkbook(2000);
        SXSSFSheet sheet = sxssfWorkbook.createSheet();
        fillExcelTitle(sheet,tile);

        if(key.equals("ZZ"))
        {
            saveZZ(sheet);
        }else if(key.equals("HY"))
        {
            saveHY(sheet);

        }else if(key.equals("TZ"))
        {
            saveTZ(sheet);
        }else
        {
            System.out.println("所传参数不正确！不能保存"+key+"标注文件！");
        }

        FileOutputStream fileOutputStream = new FileOutputStream(filePath);
        sxssfWorkbook.write(fileOutputStream);
        sxssfWorkbook.close();
        fileOutputStream.close();

    }
    private static void saveHY(SXSSFSheet sheet)
    {
        int RowNum=1;
        for(Map.Entry<String,JSONObject> map :mapHY.entrySet())
        {
            JSONObject document= map.getValue();
            Row row =sheet.createRow(RowNum++);

            row.createCell(0).setCellValue(document.getString("版本"));
            row.createCell(1).setCellValue(document.getString("标准化验名"));
            row.createCell(2).setCellValue(document.getString("标准标本"));
            row.createCell(3).setCellValue(document.getString("SLE表现系统分组"));
        }
        for(Map.Entry<String,JSONObject> map :mapHYNew.entrySet())
        {
            JSONObject document= map.getValue();
            Row row =sheet.createRow(RowNum++);
            row.createCell(0).setCellValue(strVersion);
            row.createCell(1).setCellValue(document.getString("标准化验名"));
            row.createCell(2).setCellValue(document.getString("标准标本"));
            row.createCell(3).setCellValue("");
        }

    }
  private static void saveZZ(SXSSFSheet sheet)
  {
      int RowNum=1;
      for(Map.Entry<String,JSONObject> map :mapZZ.entrySet())
      {
           JSONObject document= map.getValue();
           Row row =sheet.createRow(RowNum++);

          row.createCell(0).setCellValue(document.getString("版本"));
          row.createCell(1).setCellValue(document.getString("症状组合"));
          row.createCell(2).setCellValue(document.getString("SLE表现系统分组"));
      }
      for(Map.Entry<String,JSONObject> map :mapZZNew.entrySet())
      {
          JSONObject document= map.getValue();
          Row row =sheet.createRow(RowNum++);
          row.createCell(0).setCellValue(strVersion);
          row.createCell(1).setCellValue(document.getString("症状组合"));
          row.createCell(2).setCellValue("");
      }

  }
    private static void saveTZ(SXSSFSheet sheet)
    {
        int RowNum=1;
        for(Map.Entry<String,JSONObject> map :mapTZ.entrySet())
        {
            JSONObject document= map.getValue();
            Row row =sheet.createRow(RowNum++);

            row.createCell(0).setCellValue(document.getString("版本"));
            row.createCell(1).setCellValue(document.getString("体征组合"));
            row.createCell(2).setCellValue(document.getString("SLE表现系统分组"));
        }
        for(Map.Entry<String,JSONObject> map :mapTZNew.entrySet())
        {
            JSONObject document= map.getValue();
            Row row =sheet.createRow(RowNum++);
            row.createCell(0).setCellValue(strVersion);
            row.createCell(1).setCellValue(document.getString("体征组合"));
            row.createCell(2).setCellValue("");
        }

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
