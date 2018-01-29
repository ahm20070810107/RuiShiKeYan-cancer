package test.java.task_SLE_QueZzhengLeiJi;

import com.RuiShiKeYan.Common.Method.LocalHostInfo;
import com.RuiShiKeYan.Common.Method.SaveExcelTool;
import com.alibaba.fastjson.JSONObject;
import com.yiyihealth.data.DaX.reader.DSExcelReader2;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import test.java.task_SLE_LangChuang.BaseInfo_Title_ListValue_DBCondition;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA
 * User:huangming
 * Date:2017/12/10
 * Time:下午1:25
 */

//系统累及分组标注汇总
public class ReadLeijiTable {
    static Map<String,Set<String> > mapOldValue=new HashMap<String, Set<String> >();
    static Map<String,JSONObject> mapIncludeEnty= new HashMap<String, JSONObject>();
     public static void main(String[] args)throws Exception
     {
         readExcelValue();
         saveExcel_HuiZhong();
         saveExcel_Entity();
     }
    private static void saveExcel_Entity()
     {
         SaveExcelTool excelTool = new SaveExcelTool();
         SXSSFSheet sheet = excelTool.getSheet("");
         excelTool.fillExcelTitle("subsystem,system,description");
         int rowNum=1;
         for(Map.Entry<String,JSONObject> map:mapIncludeEnty.entrySet())
         {
            Row row = sheet.createRow(rowNum++);
            JSONObject jsonObject=map.getValue();
            row.createCell(0).setCellValue(getJSonValue(jsonObject,"subsystem"));
            row.createCell(1).setCellValue(getJSonValue(jsonObject,"system"));
            row.createCell(2).setCellValue(getJSonValue(jsonObject,"description"));
         }

         excelTool.saveExcel("交付/系统累及分组标注汇总-带表型.xlsx");
     }
    private static void saveExcel_HuiZhong()
    {
        SaveExcelTool excelTool = new SaveExcelTool();
        SXSSFSheet sheet = excelTool.getSheet("");
        excelTool.fillExcelTitle("System,Subystem");
        int rowNum=1;
        for(Map.Entry<String,Set<String> > map :mapOldValue.entrySet())
        {
            Row row = sheet.createRow(rowNum++);
            String tempStr="";
            Set<String>  hashSet=map.getValue();
            for(String str:hashSet)
            {
                if(!str.equals(""))
                    tempStr +=str+";";
            }

            row.createCell(0).setCellValue(map.getKey());
            row.createCell(1).setCellValue(tempStr);
        }
        excelTool.saveExcel("交付/系统累及分组标注汇总.xlsx");
    }
    private static void readExcelValue() throws Exception
    {
        String fileName= LocalHostInfo.getPath()+ BaseInfo_Title_ListValue_DBCondition.strCLeiJiFenZuFileName;
        String tempZuHe,tempZixiang,tempEntity;
        JSONObject document;
        JSONObject config = new JSONObject();
        config.put("filename", fileName);
        config.put("source_type", "excel");
        DSExcelReader2 excelReader = new DSExcelReader2(config);
        while ((document = excelReader.nextDocument()) != null) {
            tempZuHe=getJSonValue(document,"拟观察系统累及分组");
            tempZixiang=getJSonValue(document,"子项");
            tempEntity=getJSonValue(document,"表型");
            saveMapEntity(tempZuHe,tempZixiang,tempEntity,mapIncludeEnty);
            if(tempZuHe.equals("") ||tempZuHe.toLowerCase().equals("n"))
                continue;
            if(tempZixiang.toLowerCase().equals("n"))
                tempZixiang="";
            if(mapOldValue.containsKey(tempZuHe))
            {
                Set<String>  hashSet=mapOldValue.get(tempZuHe);
                hashSet.add(tempZixiang);
            }else
            {
                Set<String>  hashSet= new HashSet<String>();
                hashSet.add(tempZixiang);
                mapOldValue.put(tempZuHe,hashSet);
            }
        }
    }
    private static void saveMapEntity(String tempSystem,String tempZixiang,String tempEntity,Map<String,JSONObject> mapIncludeEnty)
    {
        String strZhuHe=tempSystem+tempZixiang+tempEntity;
        if(!mapIncludeEnty.containsKey(strZhuHe))
        {
            JSONObject obj=new JSONObject();
            obj.put("subsystem",tempZixiang);
            obj.put("system",tempSystem);
            obj.put("description",tempEntity);
            mapIncludeEnty.put(strZhuHe,obj);
        }
    }
    public static String getJSonValue(JSONObject jsonObject,String key)
    {
        if(jsonObject == null || key == null)return "";
        if(jsonObject.getString(key) ==null)
            return "";
        return jsonObject.getString(key);
    }
}
