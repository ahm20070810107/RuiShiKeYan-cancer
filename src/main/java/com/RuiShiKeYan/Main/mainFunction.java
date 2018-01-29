package com.RuiShiKeYan.Main;

import com.RuiShiKeYan.Common.Interface.IruiShiKeYan;
import com.RuiShiKeYan.Common.Method.MongoDBHelper;
import com.RuiShiKeYan.ExportTables.*;
import com.mongodb.client.MongoDatabase;

/**
 * Created with IntelliJ IDEA
 * User:huangming
 * Date:2017/12/19
 * Time:上午12:35
 */
public class mainFunction {


    public static void main(String[] args) throws Exception
    {
        MongoDBHelper mongoDBHelper = new MongoDBHelper("HDP-live");
        MongoDatabase db= mongoDBHelper.getDb();

       // IruiShiKeYan Acr1997andPhenomic = new ExportACR1997andPhenomicMap();
        IruiShiKeYan iruiShiKeYan= new PhenomicMap_mutiFactor();
        iruiShiKeYan.run(db,args[0]);
     //   iruiShiKeYan.run(db);
        mongoDBHelper.closeMongoDb();
    }

}
