package test.java.task_SLE_QueZzhengLeiJi;

import com.RuiShiKeYan.Common.Method.DateFormat;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.client.MongoDatabase;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.bson.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA
 * User:huangming
 * Date:2017/10/19····································
 *
 * 、
 * Time:下午6:51
 */
public class XiTongLeiJiBiao extends LeijiPublicMethod{
    static   Map<String,Document> mapZD = new HashMap<String, Document>();
    static   Map<String,Document> mapZZ = new HashMap<String, Document>();
    static   Map<String,Document> mapTZ = new HashMap<String, Document>();
    static   Map<String,Document> mapHY = new HashMap<String, Document>();
    static   Map<String,Document> mapHYRPG = new HashMap<String, Document>();

    public static void getXiTongLeiJiBiao(MongoDatabase dbHDP,SXSSFSheet sheet,Map<String,String> mapHospital,Map<String,JSONObject>  mapPID,
                                          Map<String,JSONObject>  mapQZBXTable,Map<String,ArrayList> mapLeiJiFenZu)
    {
        int RowNum=1;
        getFirstADIDay(dbHDP,mapZD);  //获取诊断
        getFirstTZDay(dbHDP,mapTZ);
        getFirstZZDay(dbHDP,mapZZ);
        getFirstHYDay(dbHDP,mapHY);
        getFirstHYRPGDay(dbHDP,mapHYRPG);
          for (Map.Entry<String,JSONObject> mapPid: mapPID.entrySet())
          {

              JSONObject document=mapPid.getValue();
              String strPID=mapPid.getKey();
              String strLastDay= DateFormat.getDateFormatDay(getLastRIDDay(dbHDP, strPID));
              String strSleTime;
              System.out.println(strPID+":"+RowNum);
              Boolean flag=true;
              for(Map.Entry<String,ArrayList> mapLeiJi :mapLeiJiFenZu.entrySet())
              {
                  ArrayList<String> leiJiFenZuList=mapLeiJi.getValue();

                  JSONObject jsonLeiJiBx=fill6To10(strPID,leiJiFenZuList,mapLeiJi.getKey());

                  if(jsonLeiJiBx==null )
                      continue;
                  String strLjiTime=jsonLeiJiBx.getString("累及时间天");
                  Row row = sheet.createRow(RowNum++);
                  flag=false;
                  row.createCell(0).setCellValue(document.getString("医院"));
                  row.createCell(1).setCellValue(strPID);
                  row.createCell(2).setCellValue(document.getString("出生年"));
                  row.createCell(3).setCellValue(document.getString("性别"));
                  row.createCell(4).setCellValue(document.getString("地域"));
                  row.createCell(5).setCellValue(strLastDay);
                  row.createCell(6).setCellValue(mapLeiJi.getKey());
                  row.createCell(7).setCellValue(jsonLeiJiBx.getString("累及表现"));
                  row.createCell(8).setCellValue(jsonLeiJiBx.getString("累及rid"));
                  row.createCell(9).setCellValue(jsonLeiJiBx.getString("表型"));
                  row.createCell(10).setCellValue(jsonLeiJiBx.getString("段落标题"));
                  row.createCell(11).setCellValue(jsonLeiJiBx.getString("上下文"));
                  row.createCell(12).setCellValue(jsonLeiJiBx.getString("累及时间天"));
                  try {
                      Integer intYear=Integer.valueOf(jsonLeiJiBx.getString("累及时间天").substring(0,4)) -Integer.valueOf(document.getString("出生年"));
                      row.createCell(13).setCellValue(String.valueOf(intYear));
                  }catch (Exception e)
                  {
                      e.printStackTrace();
                  }

                if(mapQZBXTable.containsKey(strPID))
                 {
                    JSONObject jsonObject = mapQZBXTable.get(strPID);
                     strSleTime=jsonObject.getString("诊断时间天");
                    row.createCell(14).setCellValue(jsonObject.getString("初发时间天"));
                    row.createCell(15).setCellValue(jsonObject.getString("初发时间年减去出生年"));
                    row.createCell(16).setCellValue(getAgeGroup(jsonObject.getString("初发时间年减去出生年")));
                    row.createCell(17).setCellValue(DateFormat.getDays(strLjiTime,jsonObject.getString("初发时间天")));
                    row.createCell(18).setCellValue(DateFormat.getDays(strLastDay,jsonObject.getString("初发时间天")));
                    row.createCell(19).setCellValue(strSleTime);
                    row.createCell(20).setCellValue(jsonObject.getString("诊断时间年减去出生年"));
                    row.createCell(21).setCellValue(getAgeGroup(jsonObject.getString("诊断时间年减去出生年")));
                    row.createCell(22).setCellValue(DateFormat.getDays(strLjiTime,jsonObject.getString("诊断时间天")));
                    row.createCell(23).setCellValue(jsonObject.getString("诊断时间天减去初发时间天"));
                    fillBingChengColumn(row,strLastDay,strSleTime,24);
                 }

              }
              if(flag)//若未找到累计项只填入基本信息
              {
                  Row row = sheet.createRow(RowNum++);
                  row.createCell(0).setCellValue(document.getString("医院"));
                  row.createCell(1).setCellValue(strPID);
                  row.createCell(2).setCellValue(document.getString("出生年"));
                  row.createCell(3).setCellValue(document.getString("性别"));
                  if(document.containsKey("现住址")) {
                      if (document.getString("现住址").equals("") || document.getString("现住址").equals("未提及"))
                          row.createCell(4).setCellValue(mapHospital.get(document.getString("医院")));
                      else {
                          row.createCell(4).setCellValue(document.getString("现住址"));
                      }
                  }else
                  {
                      row.createCell(4).setCellValue(mapHospital.get(document.getString("医院")));
                  }
                  row.createCell(5).setCellValue(strLastDay);
                  for (int i = 6; i < 13; i++) {  //将为空的天空而不是null
                      row.createCell(i).setCellValue("");
                  }
                  if(mapQZBXTable.containsKey(strPID))
                  {
                      JSONObject jsonObject = mapQZBXTable.get(strPID);
                      strSleTime=jsonObject.getString("诊断时间天");
                      row.createCell(14).setCellValue(jsonObject.getString("初发时间天"));
                      row.createCell(15).setCellValue(jsonObject.getString("初发时间年减去出生年"));
                      row.createCell(16).setCellValue(getAgeGroup(jsonObject.getString("初发时间年减去出生年")));
                    //  row.createCell(17).setCellValue(DateFormat.getDays(strLjiTime,jsonObject.getString("初发时间天")));
                      row.createCell(17).setCellValue("");
                      row.createCell(18).setCellValue(DateFormat.getDays(strLastDay,jsonObject.getString("初发时间天")));
                      row.createCell(19).setCellValue(strSleTime);
                      row.createCell(20).setCellValue(jsonObject.getString("诊断时间年减去出生年"));
                      row.createCell(21).setCellValue(getAgeGroup(jsonObject.getString("诊断时间年减去出生年")));
                  //    row.createCell(22).setCellValue(DateFormat.getDays(strLjiTime,jsonObject.getString("诊断时间天")));
                      row.createCell(22).setCellValue("");
                      row.createCell(23).setCellValue(jsonObject.getString("诊断时间天减去初发时间天"));
                      fillBingChengColumn(row,strLastDay,strSleTime,24);
                  }
              }

          }
    }
    private static int fillBingChengColumn(Row row,String strLastDay,String sleDay,int columNum)
    {
        try {
            String days= DateFormat.getTwoDay(strLastDay,sleDay);
            row.createCell(columNum++).setCellValue(days);
            Double dValue=Double.valueOf(days)/360;
            java.text.DecimalFormat df=new java.text.DecimalFormat("#0.00");
            row.createCell(columNum++).setCellValue(df.format(dValue));
        }catch (Exception e)
        {e.printStackTrace();}

        return columNum;
    }
    public static JSONObject fill6To10(String strPid,ArrayList<String> arrayList,String strDuiBiaoItem)
    {
       String tempLeiJiTime="w";
        JSONObject jsonObject = new JSONObject();

        for (int i = 0; i < arrayList.size(); i++) {

            String strSrouce=strPid+arrayList.get(i);
            if(strDuiBiaoItem.equals("肾炎"))
            {
                if (mapHYRPG.containsKey(strSrouce)) {
                    if (tempLeiJiTime.compareTo(mapHYRPG.get(strSrouce).getString("化验时间")) > 0) {
                        tempLeiJiTime = mapHYRPG.get(strSrouce).getString("化验时间");
                        jsonObject.put("表型", "化验");
                        jsonObject.put("累及表现", mapHYRPG.get(strSrouce).getString("化验名称_原"));
                        jsonObject.put("累及时间天", tempLeiJiTime.substring(0, 10));
                        jsonObject.put("累及rid", mapHYRPG.get(strSrouce).getString("RID"));
                        jsonObject.put("上下文", mapHYRPG.get(strSrouce).getString("上下文"));
                        jsonObject.put("段落标题", mapHYRPG.get(strSrouce).getString("段落标题"));
                    }
                }
            }
            else {
                if (mapHY.containsKey(strSrouce)) {
                    if (tempLeiJiTime.compareTo(mapHY.get(strSrouce).getString("化验时间")) > 0) {
                        tempLeiJiTime = mapHY.get(strSrouce).getString("化验时间");
                        jsonObject.put("表型", "化验");
                        jsonObject.put("累及表现", mapHY.get(strSrouce).getString("化验名称_原"));
                        jsonObject.put("累及时间天", tempLeiJiTime.substring(0, 10));
                        jsonObject.put("累及rid", mapHY.get(strSrouce).getString("RID"));
                        jsonObject.put("上下文", mapHY.get(strSrouce).getString("上下文"));
                        jsonObject.put("段落标题", mapHY.get(strSrouce).getString("段落标题"));
                    }
                }
            }

            if(mapZZ.containsKey(strSrouce))
            {
                if(tempLeiJiTime.compareTo(mapZZ.get(strSrouce).getString("症状&体征时间"))>0)
                {
                    tempLeiJiTime=mapZZ.get(strSrouce).getString("症状&体征时间");
                    jsonObject.put("表型","症状");
                    jsonObject.put("累及表现",arrayList.get(i));
                    jsonObject.put("累及时间天",tempLeiJiTime.substring(0,10));
                    jsonObject.put("累及rid",mapZZ.get(strSrouce).getString("RID"));
                    jsonObject.put("上下文",mapZZ.get(strSrouce).getString("上下文"));
                    jsonObject.put("段落标题",mapZZ.get(strSrouce).getString("段落标题"));
                }
            }
            if(mapTZ.containsKey(strSrouce))
            {
                if(tempLeiJiTime.compareTo(mapTZ.get(strSrouce).getString("症状&体征时间"))>0)
                {
                    tempLeiJiTime=mapTZ.get(strSrouce).getString("症状&体征时间");
                    jsonObject.put("表型","体征");
                    jsonObject.put("累及表现",arrayList.get(i));
                    jsonObject.put("累及时间天",tempLeiJiTime.substring(0,10));
                    jsonObject.put("累及rid",mapTZ.get(strSrouce).getString("RID"));
                    jsonObject.put("上下文",mapTZ.get(strSrouce).getString("上下文"));
                    jsonObject.put("段落标题",mapTZ.get(strSrouce).getString("段落标题"));
                }
            }
            if(mapZD.containsKey(strSrouce))
            {
                if(tempLeiJiTime.compareTo(mapZD.get(strSrouce).getString("诊断时间"))>0)
                {
                    tempLeiJiTime=mapZD.get(strSrouce).getString("诊断时间");
                    jsonObject.put("表型","诊断");
                    jsonObject.put("累及表现",mapZD.get(strSrouce).getString("标准诊断名_原"));
                    jsonObject.put("累及时间天",tempLeiJiTime.substring(0,10));
                    jsonObject.put("累及rid",mapZD.get(strSrouce).getString("RID"));
                    jsonObject.put("上下文",mapZD.get(strSrouce).getString("上下文"));
                    jsonObject.put("段落标题",mapZD.get(strSrouce).getString("段落标题"));
                }
            }
        }
        if(tempLeiJiTime.equals("w"))
            return null;

        return jsonObject;
    }


  //  private static void fill

}
