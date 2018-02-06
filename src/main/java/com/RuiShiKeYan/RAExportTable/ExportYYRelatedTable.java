package com.RuiShiKeYan.RAExportTable;

import com.RuiShiKeYan.Common.Interface.IruiShiKeYan;
import com.RuiShiKeYan.Common.Method.*;
import com.RuiShiKeYan.RAExportTable.entity.YYRelatedTable;
import com.RuiShiKeYan.SubMethod.getHDPInfo;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.client.MongoDatabase;
import com.yiyihealth.data.DaX.reader.DSExcelReader2;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.bson.Document;
import test.java.task_SLE_LangChuang.BaseInfo_Title_ListValue_DBCondition;

import java.util.*;

/**
 * Created with IntelliJ IDEA
 * User:huangming
 * Date:2018/1/2
 * Time:下午4:45
 */

//用药相关性分析表-d-Ta
public class ExportYYRelatedTable extends RuiShiKeYan implements IruiShiKeYan {

    public static void main(String[] args) throws Exception
    {
        MongoDBHelper mongoDBHelper = new MongoDBHelper("HDP-live");
        MongoDatabase db= mongoDBHelper.getDb();
        IruiShiKeYan iruiShiKeYan= new ExportYYRelatedTable();
        iruiShiKeYan.run(db);
        mongoDBHelper.closeMongoDb();
    }

    public void run(MongoDatabase mdb, Object[] args) {
        try {

            YYRelatedTable yyRelatedTable= new YYRelatedTable();
            yyRelatedTable.getBasicInfo(mdb);
//用药相关性分析表-d-Ta
            IruiShiKeYan iruiShiKeYan;
//            iruiShiKeYan= new ExportYYRelatedTable_analysis1();
//            iruiShiKeYan.run(mdb,yyRelatedTable);
////用药相关性分析表-分析2-d-Ta
//            iruiShiKeYan= new ExportYYRelatedTable_analysis2();
//            iruiShiKeYan.run(mdb,yyRelatedTable);
//用药相关性分析表-分析3-d
            iruiShiKeYan= new ExportYYRelatedTable_analysis3();
            iruiShiKeYan.run(mdb,yyRelatedTable);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
