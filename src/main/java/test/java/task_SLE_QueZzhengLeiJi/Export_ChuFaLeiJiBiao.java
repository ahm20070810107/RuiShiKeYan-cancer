package test.java.task_SLE_QueZzhengLeiJi;

import com.RuiShiKeYan.Common.Method.LocalHostInfo;
import com.RuiShiKeYan.Common.Method.MongoDBHelper;
import com.RuiShiKeYan.Common.Method.SaveExcelTool;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.client.MongoDatabase;
import com.yiyihealth.data.DaX.reader.DSExcelReader2;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import test.java.task_SLE_LangChuang.BaseInfo_Title_ListValue_DBCondition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA
 * User:huangming
 * Date:2017/10/19
 * Time:下午2:02
 */
public class Export_ChuFaLeiJiBiao extends  LeijiPublicMethod{
     static  Map<String,String>  mapHospital = new HashMap<String, String>();
     static  Map<String,JSONObject>  mapPID = new HashMap<String, JSONObject>();
    static  Map<String,JSONObject>  mapQZBXTable = new HashMap<String, JSONObject>();
     static  Map<String,ArrayList> mapLeiJiFenZu = new HashMap<String, ArrayList>();
     static  Map<String,String> mapNiGuanCXitLeiJGroup= new HashMap<String, String>();
    static Map<String,String> mapExceptPID =new HashMap<String, String>();
     static JSONObject document = null;

     public static void main(String[] args) throws Exception
     {
         SaveExcelTool saveExcelTool= new SaveExcelTool();
         SXSSFSheet sheet = saveExcelTool.getSheet("");
         saveExcelTool.fillExcelTitle(BaseInfo_Title_ListValue_DBCondition.tiltleChuFaleiji);
         MongoDBHelper mongoDBHelperHD= new MongoDBHelper("HDP-live");
         MongoDatabase dbHDP=mongoDBHelperHD.getDb();

         getHospitalInfo(mapHospital);//获取医院省市mapHospital
         getPIDInfo(mapPID,mapExceptPID); //获取所有满足条件的PID,mapPID
         getLeiJiFenZu();//获取累计分组的所有分组字段mapLeiJiFenZu
         getQZSJTable(mapQZBXTable);//获取确诊表现表数据mapQZBXTable
         ChuFaLeiJiBiao.getXiTongLeiJiBiao(dbHDP,sheet,mapHospital,mapPID,mapQZBXTable,mapLeiJiFenZu,mapNiGuanCXitLeiJGroup);

         saveExcelTool.saveExcel("交付/初发累及表.xlsx");
         mongoDBHelperHD.closeMongoDb();
     }

    public static void getLeiJiFenZu() throws Exception
    {
        String fileName= LocalHostInfo.getPath()+BaseInfo_Title_ListValue_DBCondition.strCLeiJiFenZuFileName;
        String tempFenZu,tempZuHe;
        JSONObject config = new JSONObject();
        config.put("filename", fileName);
        config.put("source_type", "excel");

        DSExcelReader2 excelReader = new DSExcelReader2(config);
        while ((document = excelReader.nextDocument()) != null) {
            tempFenZu=getJSonValue(document,"子项");
            tempZuHe=getJSonValue(document,"表型名称")+getJSonValue(document,"标准标本");
            if( tempFenZu.equals("")||tempFenZu.toLowerCase().equals("n"))
                continue;
            mapNiGuanCXitLeiJGroup.put(tempFenZu,getJSonValue(document,"拟观察系统累及分组"));
            if(mapLeiJiFenZu.containsKey(tempFenZu)) {
                ArrayList arrayList=mapLeiJiFenZu.get(tempFenZu);
                arrayList.add(tempZuHe);
            }
            else
            {
                ArrayList arrayList=new ArrayList();
                arrayList.add(tempZuHe);
                mapLeiJiFenZu.put(tempFenZu,arrayList);
            }
        }
    }
}
