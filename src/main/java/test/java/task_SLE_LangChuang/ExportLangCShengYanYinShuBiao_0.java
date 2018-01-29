package test.java.task_SLE_LangChuang;

import com.RuiShiKeYan.Common.Method.SaveExcelTool;
import com.alibaba.fastjson.JSONObject;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import com.RuiShiKeYan.SubMethod.LangCShengYanYinShuPublicInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA
 * User:huangming
 * Date:2017/10/25
 * Time:下午3:09
 */

//狼疮肾炎因素表
public class ExportLangCShengYanYinShuBiao_0  {
    static int k= 2;
    static int m=15;
    static LangCShengYanYinShuPublicInfo langCShengYanYinShuPublicInfo;
    public static void mainFunction(int kValue,int mValue,LangCShengYanYinShuPublicInfo obj) throws Exception
    {
        k=kValue;
        m=mValue;//暂时未加入计算
        langCShengYanYinShuPublicInfo=obj;
        SaveExcelTool saveExcelTool = new SaveExcelTool();
        SXSSFSheet sheet = saveExcelTool.getSheet("");
        langCShengYanYinShuPublicInfo.fillExcelTitle(sheet,BaseInfo_Title_ListValue_DBCondition.titleLCShengYanYinShuB,true);
        writeToExcel(sheet);
        saveExcelTool.saveExcel("交付/狼疮肾炎因素表-" + k +"-"+m+ ".xlsx");
    }
    public static String getJSonValue(JSONObject jsonObject, String key)
    {
        if(jsonObject == null || key == null)return "";
        if(jsonObject.getString(key) ==null)
            return "";
        return jsonObject.getString(key);
    }
   private static void writeToExcel(SXSSFSheet sheet) throws Exception
   {
       int RowNum = 1;
       for (Map.Entry<String, JSONObject> map : langCShengYanYinShuPublicInfo.mapLCShengYanPID.entrySet()) {
           System.out.println(RowNum);
           String strPID = map.getKey();
           JSONObject jsonPID = map.getValue();
           if (getJSonValue(jsonPID,"狼疮性肾炎分组").equals("") && jsonPID.getString("确诊SLE后病程分组").equals("2"))
                continue;
           Row row = sheet.createRow(RowNum++);
           row.createCell(0).setCellValue(jsonPID.getString("医院"));
           row.createCell(1).setCellValue(strPID);
           row.createCell(2).setCellValue(jsonPID.getString("出生年"));
           if (langCShengYanYinShuPublicInfo.mapBasicInfo.containsKey(strPID)) {
               JSONObject jsonBasicInfo = langCShengYanYinShuPublicInfo.mapBasicInfo.get(strPID);
               row.createCell(3).setCellValue(jsonBasicInfo.getString("性别"));
               row.createCell(4).setCellValue(jsonBasicInfo.getString("地域"));
           }
           String strGuanCQZhongDian = jsonPID.getString("观察期终点");
           row.createCell(5).setCellValue(strGuanCQZhongDian);
           row.createCell(6).setCellValue(jsonPID.getString("SLE时间天"));
           Integer age = -1;
           try {
               age = Integer.valueOf(jsonPID.getString("SLE时间天").substring(0, 4)) - Integer.valueOf(jsonPID.getString("出生年"));
               row.createCell(7).setCellValue(age);
               row.createCell(8).setCellValue(langCShengYanYinShuPublicInfo.getAgeGroup(age));
           } catch (Exception e) {
               e.printStackTrace();
           }

           age = -1;
           if (langCShengYanYinShuPublicInfo.mapQZShiJianBiao.containsKey(strPID)) {
               row.createCell(9).setCellValue(langCShengYanYinShuPublicInfo.mapQZShiJianBiao.get(strPID).getString("初发时间天"));
               age = Integer.valueOf(langCShengYanYinShuPublicInfo.mapQZShiJianBiao.get(strPID).getString("初发时间天").substring(0, 4)) -
                       Integer.valueOf(jsonPID.getString("出生年"));
               row.createCell(10).setCellValue(age);
               row.createCell(11).setCellValue(langCShengYanYinShuPublicInfo.getAgeGroup(age));
               row.createCell(12).setCellValue(langCShengYanYinShuPublicInfo.mapQZShiJianBiao.get(strPID).getString("诊断时间天减去初发时间天"));
           }
           String strLCShengYanTime = jsonPID.getString("狼疮性肾炎时间天");
           row.createCell(13).setCellValue(strLCShengYanTime);
           row.createCell(14).setCellValue(jsonPID.getString("狼疮性肾炎分组"));
           String strFinalFenZu = "";
           if (jsonPID.getString("狼疮性肾炎分组") != null && (jsonPID.getString("狼疮性肾炎分组").equals("1") || jsonPID.getString("狼疮性肾炎分组").equals("2"))) {
               strFinalFenZu = "1";
           } else if ((jsonPID.getString("狼疮性肾炎分组") == null || jsonPID.getString("狼疮性肾炎分组").equals("")) && jsonPID.getString("确诊SLE后病程分组").equals("1")) {
               strFinalFenZu = "2";
               strLCShengYanTime = strGuanCQZhongDian;
           }
           row.createCell(15).setCellValue(strFinalFenZu);
           fillLeftColumForExcel(row, 16, strPID, strLCShengYanTime);
       }

   }

   private static void fillLeftColumForExcel(Row row,int columNum,String strPid,String strLCShengYanTime)
   {
       Map<String ,JSONObject> mapItem = new HashMap<String, JSONObject>();
       Map<String ,JSONObject> mapSubItem = new HashMap<String, JSONObject>();
        for (Map.Entry<String,ArrayList> map:langCShengYanYinShuPublicInfo.mapLeiJiFenZu.entrySet())
        {
            JSONObject dd= langCShengYanYinShuPublicInfo.fill6To10(strPid,map.getValue(),strLCShengYanTime,map.getKey());
            if(dd==null) {
                mapItem.put(map.getKey(),null);
                row.createCell(columNum++).setCellValue(0);
            }
            else
            {
                mapItem.put(map.getKey(),dd);
                row.createCell(columNum++).setCellValue(1);
            }
        }

       for (Map.Entry<String,ArrayList> map:langCShengYanYinShuPublicInfo.mapLeiJiSubFenZu.entrySet())
       {
           JSONObject dd= langCShengYanYinShuPublicInfo.fill6To10(strPid,map.getValue(),strLCShengYanTime,map.getKey());
           if(dd==null) {
               mapSubItem.put(map.getKey(),null);
               row.createCell(columNum++).setCellValue(0);
           }
           else
           {
               mapSubItem.put(map.getKey(),dd);
               row.createCell(columNum++).setCellValue(1);
           }
       }
      for(Map.Entry<String,JSONObject> map:mapItem.entrySet())
      {
          if(map.getValue() ==null) {
              row.createCell(columNum++).setCellValue("");
              row.createCell(columNum++).setCellValue("");
              row.createCell(columNum++).setCellValue("");
              row.createCell(columNum++).setCellValue("");
              row.createCell(columNum++).setCellValue("");
          }else
          {
              row.createCell(columNum++).setCellValue(map.getValue().getString("实体"));
              row.createCell(columNum++).setCellValue(map.getValue().getString("状态"));
              row.createCell(columNum++).setCellValue(map.getValue().getString("时间天"));
              row.createCell(columNum++).setCellValue(map.getValue().getString("名称"));
              row.createCell(columNum++).setCellValue(map.getValue().getString("RID"));
          }
      }
       for(Map.Entry<String,JSONObject> map:mapSubItem.entrySet())
       {
           if(map.getValue() ==null) {
               row.createCell(columNum++).setCellValue("");
               row.createCell(columNum++).setCellValue("");
               row.createCell(columNum++).setCellValue("");
               row.createCell(columNum++).setCellValue("");
               row.createCell(columNum++).setCellValue("");
           }else
           {
               row.createCell(columNum++).setCellValue(map.getValue().getString("实体"));
               row.createCell(columNum++).setCellValue(map.getValue().getString("状态"));
               row.createCell(columNum++).setCellValue(map.getValue().getString("时间天"));
               row.createCell(columNum++).setCellValue(map.getValue().getString("名称"));
               row.createCell(columNum++).setCellValue(map.getValue().getString("RID"));
           }
       }
   }

}
