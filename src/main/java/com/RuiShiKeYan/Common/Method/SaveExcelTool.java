package com.RuiShiKeYan.Common.Method;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

/**
 * Created with IntelliJ IDEA
 * User:huangming
 * Date:2017/10/18
 * Time:下午8:30
 */
public class SaveExcelTool {
    SXSSFWorkbook sxssfWorkbook;
    SXSSFSheet sheet ;
    public SaveExcelTool()
    {
        sxssfWorkbook = new SXSSFWorkbook(2000);
    }
    public SaveExcelTool(int countCache)
    {
        sxssfWorkbook = new SXSSFWorkbook(countCache);
    }
    public SXSSFSheet getSheet(String sheetName)
    {
        if(sheetName.equals(""))
            sheet = sxssfWorkbook.createSheet();
        else {
            sheet = sxssfWorkbook.createSheet(sheetName);
        }
         return sheet;
    }
    public  int fillExcelTitle(String title)
    {
        String[] titles = title.split(",");
        Row row = sheet.createRow(0);
        for (int i = 0; i <titles.length ; i++) {
            row.createCell(i).setCellValue(titles[i]);
        }
        return titles.length;
    }
    public  int fillExcelTitle(HashSet<String> setKeys,int startCell)
    {
        Row row = sheet.createRow(0);
        for (String key:setKeys) {
            row.createCell(startCell++).setCellValue(key);
        }
        return startCell;
    }
    public  int fillExcelTitle(Map<String,ArrayList<String>> mapItem, int startCell,String preffix)
    {
        Row row=sheet.getRow(0);
        for (Map.Entry<String,ArrayList<String>> map:mapItem.entrySet()) {
            row.createCell(startCell++).setCellValue(preffix+map.getKey());
        }
        return startCell;
    }
    public  int fillExcelTitle(Map<String,ArrayList<String>> mapItem, int startCell,String preffix,String strExcept)
    {
        Row row=sheet.getRow(0);
        for (Map.Entry<String,ArrayList<String>> map:mapItem.entrySet()) {
            if(strExcept !=null && !strExcept.equals(""))
             if(strExcept.equals(map.getKey()))
                continue;
            row.createCell(startCell++).setCellValue(preffix+map.getKey());
        }
        return startCell;
    }
     public void saveExcel(String fileName)
     {
         try {
             FileOutputStream fileOutputStream = new FileOutputStream(LocalHostInfo.getPath() + fileName);
             sxssfWorkbook.write(fileOutputStream);
             sxssfWorkbook.close();
             fileOutputStream.close();
         }catch (Exception e)
         {
             e.printStackTrace();
         }

     }



}
