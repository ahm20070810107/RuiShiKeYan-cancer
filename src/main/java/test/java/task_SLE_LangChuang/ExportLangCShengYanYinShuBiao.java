package test.java.task_SLE_LangChuang;

import com.RuiShiKeYan.Common.Method.MongoDBHelper;
import com.mongodb.client.MongoDatabase;
import com.RuiShiKeYan.SubMethod.LangCShengYanYinShuPublicInfo;
/**
 * Created with IntelliJ IDEA
 * User:huangming
 * Date:2017/11/6
 * Time:下午4:03
 */
public class ExportLangCShengYanYinShuBiao {
    static int[] kGroup= {2};
    static int[] mGroup={15};
     public static void main(String[] args) throws Exception {
         MongoDBHelper mongoDBHelper = new MongoDBHelper("HDP-live");
         MongoDatabase mdb = mongoDBHelper.getDb();
         LangCShengYanYinShuPublicInfo langCShengYanYinShuPublicInfo = new LangCShengYanYinShuPublicInfo();
         langCShengYanYinShuPublicInfo.getBaseInfo(mdb);
         langCShengYanYinShuPublicInfo.getHYYinShuInfo(mdb, true);
         for (int i : kGroup) {
             for (int m : mGroup) {
                 langCShengYanYinShuPublicInfo.getLCShengYanPIDmap(i,m);
//                 ExportLangCShengYanYinShuBiao_0.mainFunction(i,m, langCShengYanYinShuPublicInfo);
//                 ExportLangCShengYanYinShuBiao_1.mainFunction(i,m, langCShengYanYinShuPublicInfo);
//                 ExportLangCShengYanYinShuBiao_2.mainFunction(i,m, langCShengYanYinShuPublicInfo);
//                 ExportLangCShengYanYinShuBiao_3.mainFunction(i,m, langCShengYanYinShuPublicInfo);
                 ExportLangCShengYanYinShuBiao_4.mainFunction(i,m, langCShengYanYinShuPublicInfo);
                 ExportLangCShengYanYinShuBiao_4_Include_allColumn.mainFunction(i,m, langCShengYanYinShuPublicInfo);
//                 ExportLangCShengYanYinShuBiao_5.mainFunction(i, m,-90, langCShengYanYinShuPublicInfo);
//                 ExportLangCShengYanYinShuBiao_5.mainFunction(i, m,-180, langCShengYanYinShuPublicInfo);
             }
         }

         mongoDBHelper.closeMongoDb();
     }
}
