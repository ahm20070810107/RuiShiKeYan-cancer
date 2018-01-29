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
 * Date:2017/12/13
 * Time:下午5:09
 */
public class SLELeiJiProbabilityTable {
   static Set<String> setSubItem= new HashSet<String>();
   static Set<String> setSystemItem= new HashSet<String>();
   static Map<String,Double> mapSubItem= new HashMap<String, Double>();
   static Map<String,Double> mapSubAndSystem= new HashMap<String, Double>();
   static String[] sexGroup={"青少年","成人","晚发"};
   static String[] sexes={"男","女"};
    //SLE累及概率表
    public static void main(String[] args) throws Exception
    {
        String strReadFileName= LocalHostInfo.getPath()+"交付/"+SavedFileNameList.strSleLeiJiTable;
        getSubAndItemMap();
        getResult(strReadFileName);
        writeToExcel();
    }

    private static void writeToExcel()
    {
        SaveExcelTool saveExcelTool= new SaveExcelTool();
        SXSSFSheet sheet = saveExcelTool.getSheet("");
        fillExcelTitle(sheet);
        int rowNum=1;

        for(String strSubItem:setSubItem) {
            for (String strSystm : setSystemItem) {
                int cellNum=0;
               Row row = sheet.createRow(rowNum++);
                System.out.println(rowNum);
               row.createCell(cellNum++).setCellValue(strSubItem);
               row.createCell(cellNum++).setCellValue(strSystm);

                for (int i = -1; i < sexes.length; i++) {
                    String sex=getArryValue(sexes,i);
                    String strDown=sex+strSubItem;
                    String strUp=sex+strSubItem+strSystm;
                    row.createCell(cellNum ++).setCellValue(cacalateResult(mapSubAndSystem.get(strUp),mapSubItem.get(strDown)));
                }
                for (int i = 0; i < sexGroup.length; i++) {
                    String sexgroup=getArryValue(sexGroup,i);
                    String strDown=sexgroup+strSubItem;
                    String strUp=sexgroup+strSubItem+strSystm;
                    row.createCell(cellNum ++).setCellValue(cacalateResult(mapSubAndSystem.get(strUp),mapSubItem.get(strDown)));
                }
                for (int i = 0; i < sexes.length; i++) {
                    for (int j = 0; j < sexGroup.length; j++) {
                        String sex=getArryValue(sexes,i);
                        String sexgroup=getArryValue(sexGroup,j);
                        String strDown=sex+sexgroup+strSubItem;
                        String strUp=sex+sexgroup+strSubItem+strSystm;
                        row.createCell(cellNum ++).setCellValue(cacalateResult(mapSubAndSystem.get(strUp),mapSubItem.get(strDown)));
                    }
                }
            }
        }

        saveExcelTool.saveExcel("交付/"+SavedFileNameList.strSleProbabilityTtable);
    }
    private static double cacalateResult(Double value1,Double value2)
    {
        if(value1==null  |value2 ==null)
            return 0.00;
        try {
            Double dValue=value1/value2;
            java.text.DecimalFormat df=new java.text.DecimalFormat("#0.00");
            return  Double.valueOf(df.format(dValue));
        }catch (Exception e)
        {
            e.printStackTrace();
            return 0.00;
        }
    }
    private static void fillExcelTitle(SXSSFSheet sheet)
    {
        int cellNum=0;
        String titleName="累及概率";
        Row row= sheet.createRow(0);
        row.createCell(cellNum++).setCellValue("子项");
        row.createCell(cellNum++).setCellValue("拟观察系统累及分组");
        for (int i = -1; i < sexes.length; i++) {
          row.createCell(cellNum ++).setCellValue(getArryValue(sexes,i)+titleName);
        }
        for (int i = 0; i < sexGroup.length; i++) {
          row.createCell(cellNum++).setCellValue(sexGroup[i]+titleName);
        }
        for (int i = 0; i < sexes.length; i++) {
            for (int j = 0; j < sexGroup.length; j++) {
                row.createCell(cellNum++).setCellValue(sexes[i] +sexGroup[j]+ titleName);
            }
        }
    }
    public static String getJSonValue(JSONObject jsonObject, String key)
    {
        if(jsonObject == null || key == null)return "";
        if(jsonObject.getString(key) ==null)
            return "";
        return jsonObject.getString(key);
    }
    private static void getResult(String fileName)
    {
        JSONObject document;
        DSExcelReader2 excelReader;
        JSONObject config = new JSONObject();
        config.put("filename", fileName);
        config.put("source_type", "excel");
        try {
            excelReader = new DSExcelReader2(config);
            while ((document = excelReader.nextDocument()) != null) {
                if(getJSonValue(document,"患者（PID）").equals(""))
                    continue;
                String strSex=getJSonValue(document,"性别");
                String strGroup= getJSonValue(document,"首诊年龄分组");
                for(String strSubItem:setSubItem)
                {
                   String strSubValue=getJSonValue(document,strSubItem);
              //     System.out.println(strSubItem+"strSubValue:"+strSubValue);
                   if(!strSubValue.equals(""))
                   {
                       fillMapCount(mapSubItem,strSubItem);
                       fillMapCount(mapSubItem,strSex+strSubItem);
                       fillMapCount(mapSubItem,strGroup+strSubItem);
                       fillMapCount(mapSubItem,strSex+strGroup+strSubItem);
                      //分子
                      for (String strSystm : setSystemItem) {
                         String strSysValue=getJSonValue(document,strSystm);
                         if(!strSysValue.equals(""))
                         {
                             fillMapCount(mapSubAndSystem,strSubItem+strSystm);
                             fillMapCount(mapSubAndSystem,strSex+strSubItem+strSystm);
                             fillMapCount(mapSubAndSystem,strGroup+strSubItem+strSystm);
                             fillMapCount(mapSubAndSystem ,strSex+strGroup+strSubItem+strSystm);
                         }
                      }
                   }
                }

            }
            excelReader.close();
        }catch (Exception e){e.printStackTrace();}

        System.out.println("mapSubAndSystem:"+mapSubAndSystem.size()+"mapSubItem:"+mapSubItem.size());

    }

    private static void fillMapCount(Map<String,Double> map,String key)
    {
        if(map.containsKey(key))
        {
           Double intValue=map.get(key);
           intValue +=1;
           map.put(key,intValue);
        }else
        {
            map.put(key,1.0);
        }

    }
    private static String getArryValue(String[] arr,int i)
    {
        if(i == -1)
            return "";
        if(arr[i] ==null)
            return "";
        return arr[i];
    }

    private static void getSubAndItemMap() throws Exception
    {
        JSONObject document;
        String fileName= LocalHostInfo.getPath()+ BaseInfo_Title_ListValue_DBCondition.strCLeiJiFenZuFileName;
        String tempFenZu,tempZiXiang;
        JSONObject config = new JSONObject();
        config.put("filename", fileName);
        config.put("source_type", "excel");

        DSExcelReader2 excelReader = new DSExcelReader2(config);
        while ((document = excelReader.nextDocument()) != null) {
            tempFenZu=getJSonValue(document,"拟观察系统累及分组");
            tempZiXiang=getJSonValue(document,"子项");
            if(!tempFenZu.equals("")&&!tempFenZu.toUpperCase().equals("N")) {
               setSystemItem.add("系统_"+tempFenZu);
            }
            if(!tempZiXiang.equals("")&&!tempZiXiang.toUpperCase().equals("N")) {
                setSubItem.add(tempZiXiang);
            }
        }
    }
}
