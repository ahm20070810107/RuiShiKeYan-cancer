package com.RuiShiKeYan.Common.Method;

import com.RuiShiKeYan.dao.SSHLocalForward;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;

/**
 * Created with IntelliJ IDEA
 * User:huangming
 * Date:2017/10/18
 * Time:下午8:43
 */
public class MongoDBHelper {
    MongoClient client;
    SSHLocalForward sshLocalForward;
    String Dbname=null;
    public MongoDBHelper(String dbName)throws Exception
     {
         Dbname=dbName;
         MongoClientURI uri = new MongoClientURI(LocalHostInfo.getUrl(dbName));
         client = new MongoClient(uri);
         //本地通过跳板机跳转

         sshLocalForward = new SSHLocalForward(LocalHostInfo.getHosturl());
         if(LocalHostInfo.isLocation()) {
             sshLocalForward.connectSSH();
         }
     }
    public MongoDBHelper(String dbName,String HostUrl)throws Exception
    {
        Dbname=dbName;
        MongoClientURI uri = new MongoClientURI(LocalHostInfo.getUrl(dbName,HostUrl));
        client = new MongoClient(uri);
        //本地通过跳板机跳转

        sshLocalForward = new SSHLocalForward(HostUrl);
        if(LocalHostInfo.isLocation()) {
            sshLocalForward.connectSSH();
        }
    }
    public MongoDBHelper()throws Exception
    {
        MongoClientURI uri = new MongoClientURI(LocalHostInfo.getUrl());
        client = new MongoClient(uri);
        //本地通过跳板机跳转

        sshLocalForward = new SSHLocalForward(LocalHostInfo.getHosturl());
        if(LocalHostInfo.isLocation()) {
            sshLocalForward.connectSSH();
        }
    }
    public MongoClient getClient()
    {
       return client;
    }
    public MongoDatabase getDb()
    {
       return client.getDatabase(Dbname);
    }
    public MongoDatabase getDb(String dbname)
    {
        return client.getDatabase(dbname);
    }
    public void closeMongoDb()
    {
       try {
           client.close();
           if (LocalHostInfo.isLocation()) {
               sshLocalForward.closeSSH();
           }
       }catch (Exception e)
       {
           e.printStackTrace();
       }
    }
}
