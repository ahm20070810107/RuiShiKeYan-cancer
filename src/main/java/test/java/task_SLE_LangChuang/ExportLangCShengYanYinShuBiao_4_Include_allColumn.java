package test.java.task_SLE_LangChuang;

import com.RuiShiKeYan.Common.Method.DateFormat;
import com.RuiShiKeYan.Common.Method.SaveExcelTool;
import com.alibaba.fastjson.JSONObject;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import com.RuiShiKeYan.SubMethod.LangCShengYanYinShuPublicInfo;
/**
 * Created with IntelliJ IDEA
 * User:huangming
 * Date:2017/10/25
 * Time:下午3:09
 */
public class ExportLangCShengYanYinShuBiao_4_Include_allColumn {
    static int k= 2;
    static int m=14;
    static JSONObject document=null;
    static LangCShengYanYinShuPublicInfo langCShengYanYinShuPublicInfo;
    public static void mainFunction(int kValue,int mValue,LangCShengYanYinShuPublicInfo obj) throws Exception
    {
        k=kValue;
        m=mValue;
        langCShengYanYinShuPublicInfo=obj;
        SaveExcelTool saveExcelTool = new SaveExcelTool();
        SXSSFSheet sheet = saveExcelTool.getSheet("");
        langCShengYanYinShuPublicInfo.fillExcelTitle(sheet,BaseInfo_Title_ListValue_DBCondition.titleLCShengYanYinShuB4,true);
        writeToExcel(sheet);
        saveExcelTool.saveExcel("交付/狼疮肾炎因素表-分析4用-含表型-"+k+"-"+m+".xlsx");
     //   saveNotInYinSuBiao(k);  //获取分析2表未在2表但在pid验证的pid
    }

    private static void writeToExcel(SXSSFSheet sheet)
   {
       int RowNum=1;
       for(Map.Entry<String,JSONObject> map :langCShengYanYinShuPublicInfo.mapLCShengYanPID.entrySet())
       {
           System.out.println(RowNum);
           String strPID=map.getKey();
           JSONObject jsonPID=map.getValue();
        //   String strLCShengYanTime = jsonPID.getString("狼疮性肾炎时间天");
         //  String strGuanCQZhongDian=jsonPID.getString("观察期终点");
           String strsleTIme = DateFormat.getNextDay(jsonPID.getString("SLE时间天"), m);
           String strFinalFenZu="";
           if(jsonPID.getString("狼疮性肾炎分组")!=null &&(jsonPID.getString("狼疮性肾炎分组").equals("1")))
           {
               strFinalFenZu="1";
             //  strGuanCQZhongDian=strLCShengYanTime;
           }else if((jsonPID.getString("狼疮性肾炎分组")==null ||jsonPID.getString("狼疮性肾炎分组").equals(""))&&jsonPID.getString("确诊SLE后病程分组").equals("1"))
           {
               strFinalFenZu="2";
           }else {
               continue;
           }
           Row row = sheet.createRow(RowNum++);
           row.createCell(0).setCellValue(strPID);
           row.createCell(1).setCellValue(strFinalFenZu);
           row.createCell(2).setCellValue(jsonPID.getString("医院"));
           row.createCell(3).setCellValue(jsonPID.getString("出生年"));
           if(langCShengYanYinShuPublicInfo.mapBasicInfo.containsKey(strPID))
           {
               JSONObject jsonBasicInfo=langCShengYanYinShuPublicInfo.mapBasicInfo.get(strPID);
               row.createCell(4).setCellValue(jsonBasicInfo.getString("性别"));
               row.createCell(5).setCellValue(jsonBasicInfo.getString("地域"));
           }

          // row.createCell(5).setCellValue(strGuanCQZhongDian);
           Integer age=-1;

           if(langCShengYanYinShuPublicInfo.mapQZShiJianBiao.containsKey(strPID)) {
               row.createCell(6).setCellValue(langCShengYanYinShuPublicInfo.mapQZShiJianBiao.get(strPID).getString("诊断时间天").substring(0,4));
               row.createCell(7).setCellValue(langCShengYanYinShuPublicInfo.mapQZShiJianBiao.get(strPID).getString("诊断时间年减去出生年"));
               row.createCell(8).setCellValue(getAgeGroup(langCShengYanYinShuPublicInfo.mapQZShiJianBiao.get(strPID).getString("诊断时间年减去出生年")));

               age=Integer.valueOf(langCShengYanYinShuPublicInfo.mapQZShiJianBiao.get(strPID).getString("初发时间天").substring(0,4))-
                       Integer.valueOf(jsonPID.getString("出生年"));
               row.createCell(9).setCellValue(langCShengYanYinShuPublicInfo.mapQZShiJianBiao.get(strPID).getString("初发时间天").substring(0,4));
               row.createCell(10).setCellValue(age);
               row.createCell(11).setCellValue(getAgeGroup(age));
               row.createCell(12).setCellValue(langCShengYanYinShuPublicInfo.mapQZShiJianBiao.get(strPID).getString("诊断时间天减去初发时间天"));
           }

           fillLeftColumForExcel(row,13,strPID,strsleTIme);
       }

   }
    private static String getAgeGroup(Integer age)
    {
        if(age==null ||age==-1)
            return "异常";
        if(age.intValue()>=0 &&age.intValue()<=18)
            return "青少年";
        else if(age.intValue()>=19 &&age.intValue()<=49)
            return "成人";
        else if(age.intValue()>=50 &&age.intValue()<=100)
            return "晚发";
        else
        {
            return "异常";
        }

    }
    private static String getAgeGroup(String strage)
    {

        if(strage==null ||strage.equals(""))
            return "异常";
        Integer age=-1;
        try
        {  if(strage.indexOf(".")>0)
            age=Integer.valueOf(strage.substring(0,strage.indexOf(".")));
        else age=Integer.valueOf(strage);
        }catch (Exception e){e.printStackTrace();}
        if(age.intValue()>=0 &&age.intValue()<=18)
            return "青少年";
        else if(age.intValue()>=19 &&age.intValue()<=49)
            return "成人";
        else if(age.intValue()>=50 &&age.intValue()<=100)
            return "晚发";
        else
        {
            return "异常";
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
