package test.java.doResearch;

import com.RuiShiKeYan.Common.Method.*;
import com.alibaba.fastjson.JSONObject;
import com.yiyihealth.beliefbase.chisquare.ChiSquare;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;

import java.text.NumberFormat;
import java.util.*;

/**
 * Created with IntelliJ IDEA
 * User:huangming
 * Date:2017/12/25
 * Time:下午4:44
 */
public class getJiaPangKangKaFang {

    static Map<String,JSONObject>  mapExcelInfo= new HashMap<String, JSONObject>();
    static Map<String,HashSet<String>> mapPxInclude= new HashMap<String, HashSet<String>>(); //p发生的人数列表
    static Map<String,HashSet<String>> mapPxExclude= new HashMap<String, HashSet<String>>();//p不发生列表
    public static void main(String[] args) throws Exception
    {
        String fileName= LocalHostInfo.getPath()+"甲旁减数据表20171225.xlsx";

        ReadExcelToMap.readFromExcelToMap(mapExcelInfo, fileName, "患者编号",true);
        HashSet<String> setKeys=getResult();
        writeToExcel(setKeys);
    }

   private static void writeToExcel(HashSet<String> setKeys)
   {
       SaveExcelTool saveExcelTool= new SaveExcelTool();
       SXSSFSheet sheet=saveExcelTool.getSheet("");
       fillExcelTitle(sheet,setKeys,1);
      int RowNum=1;
       for(String strRow:setKeys)
       {
         Row row=sheet.createRow(RowNum++);
         row.createCell(0).setCellValue(strRow);
         int cellNum=1;
           for(String strCol:setKeys)
           {
               cellNum=fillResultCell(row,strRow,strCol,cellNum);
           }
       }

       saveExcelTool.saveExcel("交付/甲旁减数据表-卡方数据.xlsx");
       System.out.print("OK");
   }
   private static int fillResultCell(Row row,String strRow,String strCol,int cellNum)
   {
       int pxC_y=0,pxCR_y=0,pxC_n=0,pxCR_n=0;

       if(mapPxInclude.get(strCol) !=null) {
           pxC_y =mapPxInclude.get(strCol).size();
           pxCR_y = getPxRowNum( mapPxInclude.get(strCol),  mapPxInclude.get(strRow));

       }
       if(mapPxExclude.get(strCol) !=null) {
           pxC_n = mapPxExclude.get(strCol).size();
           pxCR_n =getPxRowNum( mapPxExclude.get(strCol),  mapPxInclude.get(strRow));
       }
        row.createCell(cellNum++).setCellValue(pxCR_y);
        row.createCell(cellNum++).setCellValue(getPercent(pxCR_y,pxC_y));
        row.createCell(cellNum++).setCellValue(pxCR_n);
        row.createCell(cellNum++).setCellValue(getPercent(pxCR_n,pxC_n));
       Double pvalue= ChiSquare.calculate((double)pxCR_y,(double)pxCR_n,(double)(pxC_y-pxCR_y),(double) (pxC_n-pxCR_n));
       //采用上面数组传值请求p的卡方
       System.out.println("pxRow:"+strRow+" pxCloumn:"+strCol+" "+(double)pxCR_y+","+(double)pxCR_n+","+(double)(pxC_y-pxCR_y)+","+(double) (pxC_n-pxCR_n) +" pvalue："+pvalue);
           row.createCell(cellNum++).setCellValue(pvalue);
       return cellNum;
   }
    public static Double getPercent(int upNum,int downNum)
    {
        // 创建一个数值格式化对象
        if(downNum ==0)
            return 0.0;
        NumberFormat numberFormat = NumberFormat.getInstance();
        // 设置精确到小数点后2位
        numberFormat.setMaximumFractionDigits(2);
        String result = numberFormat.format((float)upNum/(float)downNum*100);
        //    System.out.println("diliverNum和queryMailNum的百分比为:" + result + "%");
        return Double.valueOf(result);
    }
   private static int getPxRowNum(HashSet<String> colPids,HashSet<String> rowPids)
   {
       int count=0;

       for(String strCol:colPids)
       {
           if(rowPids.contains(strCol))
               count++;
       }
       return count;
   }
    public static int fillExcelTitle(SXSSFSheet sheet,HashSet<String> setKeys,int cellNum)
    {
        Row row = sheet.createRow(0);
        for (String Value:setKeys) {
            row.createCell(cellNum++).setCellValue(Value+"-Y");
            row.createCell(cellNum++).setCellValue(Value+"-Y%");
            row.createCell(cellNum++).setCellValue(Value+"-N");
            row.createCell(cellNum++).setCellValue(Value+"-N%");
            row.createCell(cellNum++).setCellValue(Value+"-P");
        }
        return cellNum;
    }

    private static HashSet<String> getResult()
    {
        HashSet<String> setKeys= new HashSet<String>();
       for(Map.Entry<String,JSONObject> map:mapExcelInfo.entrySet())
       {
            String strPid=map.getKey();
            JSONObject jsonObject= map.getValue();
            Set<String> keys=jsonObject.keySet();
           for(String str:keys)
           {
               if(str.equals("患者编号") ||str.equals(""))
                   continue;
               setKeys.add(str);
               if(jsonObject.getIntValue(str)==1)
               {
                   HashSet<String> setInclude=mapPxInclude.get(str);
                   if(setInclude ==null)
                   {
                       setInclude= new HashSet<String>();
                       mapPxInclude.put(str,setInclude);
                   }
                   setInclude.add(strPid);

               }else
               {
                   HashSet<String> setExclude=mapPxExclude.get(str);
                   if(setExclude ==null)
                   {
                       setExclude= new HashSet<String>();
                       mapPxExclude.put(str,setExclude);
                   }
                   setExclude.add(strPid);
               }
           }
       }
       return  setKeys;
    }
}
