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
public class ChuFaLeiJiBiao extends LeijiPublicMethod{
    static   Map<String,Document> mapZD = new HashMap<String, Document>();
    static   Map<String,Document> mapZZ = new HashMap<String, Document>();
    static   Map<String,Document> mapTZ = new HashMap<String, Document>();
    static   Map<String,Document> mapHY = new HashMap<String, Document>();
    static   Map<String,Document> mapHYRPG = new HashMap<String, Document>();


    public static void getXiTongLeiJiBiao(MongoDatabase dbHDP,SXSSFSheet sheet,Map<String,String> mapHospital,Map<String,JSONObject>  mapPID,
                                          Map<String,JSONObject>  mapQZBXTable,Map<String,ArrayList> mapLeiJiFenZu,Map<String,String> mapNiGuanCXitLeiJGroup)
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
              System.out.println(strPID+":"+RowNum);
              JSONObject jsonObject = mapQZBXTable.get(strPID);
              String strChuFaTime=jsonObject.getString("初发时间天");
              Boolean flag=true;
              for(Map.Entry<String,ArrayList> mapLeiJi :mapLeiJiFenZu.entrySet())
              {
                  ArrayList<String> leiJiFenZuList=mapLeiJi.getValue();

                  JSONObject jsonLeiJiBx=fill6To10(strPID,leiJiFenZuList,mapLeiJi.getKey(),strChuFaTime);

                  if(jsonLeiJiBx==null )
                      continue;
                  String strZiXiangTime=jsonLeiJiBx.getString("子项时间天");
                  Row row = sheet.createRow(RowNum++);
                  flag=false;
                  row.createCell(0).setCellValue(document.getString("医院"));
                  row.createCell(1).setCellValue(strPID);
                  row.createCell(2).setCellValue(document.getString("出生年"));
                  row.createCell(3).setCellValue(document.getString("性别"));
                  row.createCell(4).setCellValue(document.getString("地域"));
                  row.createCell(5).setCellValue(strLastDay);
                  row.createCell(6).setCellValue(mapLeiJi.getKey());
                  row.createCell(7).setCellValue(mapNiGuanCXitLeiJGroup.get(mapLeiJi.getKey()));
                  row.createCell(8).setCellValue(jsonLeiJiBx.getString("子项表现"));
                  row.createCell(9).setCellValue(jsonLeiJiBx.getString("子项rid"));
                  row.createCell(10).setCellValue(jsonLeiJiBx.getString("表型"));
                  row.createCell(11).setCellValue(jsonLeiJiBx.getString("段落标题"));
                  row.createCell(12).setCellValue(jsonLeiJiBx.getString("上下文"));
                  row.createCell(13).setCellValue(jsonLeiJiBx.getString("子项时间天"));
                  try {
                      Integer intYear=Integer.valueOf(jsonLeiJiBx.getString("子项时间天").substring(0,4)) -Integer.valueOf(document.getString("出生年"));
                      row.createCell(14).setCellValue(String.valueOf(intYear));
                  }catch (Exception e)
                  {
                      e.printStackTrace();
                  }
                row.createCell(15).setCellValue(strChuFaTime);
                row.createCell(16).setCellValue(jsonObject.getString("初发时间年减去出生年"));
                row.createCell(17).setCellValue(getAgeGroup(jsonObject.getString("初发时间年减去出生年")));
                row.createCell(18).setCellValue(DateFormat.getDays(strZiXiangTime,jsonObject.getString("初发时间天")));
                row.createCell(19).setCellValue(DateFormat.getDays(strLastDay,jsonObject.getString("初发时间天")));
                row.createCell(20).setCellValue(jsonObject.getString("诊断时间天"));
                row.createCell(21).setCellValue(jsonObject.getString("诊断时间年减去出生年"));
                row.createCell(22).setCellValue(getAgeGroup(jsonObject.getString("诊断时间年减去出生年")));
                row.createCell(23).setCellValue(DateFormat.getDays(strZiXiangTime,jsonObject.getString("诊断时间天")));
                row.createCell(24).setCellValue(jsonObject.getString("诊断时间天减去初发时间天"));
                row.createCell(25).setCellValue(document.getString("生产状况分组"));
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
                  for (int i = 6; i < 14; i++) {  //将为空的天空而不是null
                      row.createCell(i).setCellValue("");
                  }
                  if(mapQZBXTable.containsKey(strPID))
                  {
                      row.createCell(15).setCellValue(strChuFaTime);
                      row.createCell(16).setCellValue(jsonObject.getString("初发时间年减去出生年"));
                      row.createCell(17).setCellValue(getAgeGroup(jsonObject.getString("初发时间年减去出生年")));
                    //  row.createCell(17).setCellValue(DateFormat.getDays(strZiXiangTime,jsonObject.getString("初发时间天")));
                      row.createCell(18).setCellValue("");
                      row.createCell(19).setCellValue(DateFormat.getDays(strLastDay,jsonObject.getString("初发时间天")));
                      row.createCell(20).setCellValue(jsonObject.getString("诊断时间天"));
                      row.createCell(21).setCellValue(jsonObject.getString("诊断时间年减去出生年"));
                      row.createCell(22).setCellValue(getAgeGroup(jsonObject.getString("诊断时间年减去出生年")));
                  //    row.createCell(22).setCellValue(DateFormat.getDays(strZiXiangTime,jsonObject.getString("诊断时间天")));
                      row.createCell(23).setCellValue("");
                      row.createCell(24).setCellValue(jsonObject.getString("诊断时间天减去初发时间天"));
                      row.createCell(25).setCellValue(document.getString("生产状况分组"));
                  }
              }
          }
    }

    public static JSONObject fill6To10(String strPid,ArrayList<String> arrayList,String strDuiBiaoItem,String strChuFaTime)
    {
       String tempLeiJiTime="w";
       String str30ChuFaTime= DateFormat.getNextDay(strChuFaTime,30);
       JSONObject jsonObject = new JSONObject();

        for (int i = 0; i < arrayList.size(); i++) {

            String strSrouce=strPid+arrayList.get(i);
            if(strDuiBiaoItem.equals("肾炎"))
            {
                if (mapHYRPG.containsKey(strSrouce)) {
                    if (tempLeiJiTime.compareTo(mapHYRPG.get(strSrouce).getString("化验时间")) > 0) {
                        tempLeiJiTime = mapHYRPG.get(strSrouce).getString("化验时间");
                        jsonObject.put("表型", "化验");
                        jsonObject.put("子项表现", mapHYRPG.get(strSrouce).getString("化验名称_原"));
                        jsonObject.put("子项时间天", tempLeiJiTime.substring(0, 10));
                        jsonObject.put("子项rid", mapHYRPG.get(strSrouce).getString("RID"));
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
                        jsonObject.put("子项表现", mapHY.get(strSrouce).getString("化验名称_原"));
                        jsonObject.put("子项时间天", tempLeiJiTime.substring(0, 10));
                        jsonObject.put("子项rid", mapHY.get(strSrouce).getString("RID"));
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
                    jsonObject.put("子项表现",arrayList.get(i));
                    jsonObject.put("子项时间天",tempLeiJiTime.substring(0,10));
                    jsonObject.put("子项rid",mapZZ.get(strSrouce).getString("RID"));
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
                    jsonObject.put("子项表现",arrayList.get(i));
                    jsonObject.put("子项时间天",tempLeiJiTime.substring(0,10));
                    jsonObject.put("子项rid",mapTZ.get(strSrouce).getString("RID"));
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
                    jsonObject.put("子项表现",mapZD.get(strSrouce).getString("标准诊断名_原"));
                    jsonObject.put("子项时间天",tempLeiJiTime.substring(0,10));
                    jsonObject.put("子项rid",mapZD.get(strSrouce).getString("RID"));
                    jsonObject.put("上下文",mapZD.get(strSrouce).getString("上下文"));
                    jsonObject.put("段落标题",mapZD.get(strSrouce).getString("段落标题"));
                }
            }
        }
        if(tempLeiJiTime.equals("w"))
            return null;
        String firstTime=jsonObject.getString("子项时间天");
        if(firstTime.compareTo(strChuFaTime) <0 ||firstTime.compareTo(str30ChuFaTime)>0)
            return null;

        return jsonObject;
    }


  //  private static void fill

}
