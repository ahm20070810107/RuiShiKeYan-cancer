package test.java.doResearch;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import javax.swing.text.Document;

/**
 * Created with IntelliJ IDEA
 * User:huangming
 * Date:2017/12/27
 * Time:下午10:26
 */
public class dropcollection {
  static   private String[] sexGroup = {"Male", "Female", ""};
   static private String[] ageGroup = {"Child", "Adult", "Late", ""};

    public static void main(String[] args)
    {

         MongoClient mongoDest = new MongoClient(new MongoClientURI("mongodb://hm:eql-LmnZ8xc9pxbg@dds-bp1baad40c630d241.mongodb.rds.aliyuncs.com:3717/MDP-live"));
        MongoDatabase dbDest=mongoDest.getDatabase("MDP-live");

        for(int i =0;i<11;i++)
            for (String strSex : sexGroup) {
                for (String strAge : ageGroup) {
                  String strCollection= getCollectionName(strSex,strAge,i);
                   dbDest.getCollection(strCollection).drop();
                }
            }

    }
    private static String getCollectionName(String sex,String age,int kValue)
    {
        String strDbName="Muti_slePhenomicMap_"+kValue;
        if(!sex.equals(""))
            strDbName +="_"+sex;
        if(!age.equals(""))
            strDbName +="_"+age;
        return strDbName;
    }


}
