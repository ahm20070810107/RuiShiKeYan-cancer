package com.RuiShiKeYan.Common.Method;

import com.alibaba.fastjson.JSONObject;
import com.yiyihealth.data.DaxHandler.check.Data;

import java.io.File;

/**
 * Created with IntelliJ IDEA
 * User:huangming
 * Date:2017/10/17
 * Time:下午7:09
 */
public class LocalHostInfo {

   static String Pathlocal="../../Desktop/tempExcel-changhai/";
   static String PathHost="/data/dax_backup/current_dax_home/home/睿识科研/huangming/tempExcel-changhai/";
  // static String PathHost="/data/hitales/huangming/Result/";

   static boolean isLocal=false;
   static {
       File  file = new File(PathHost);
       if(file.exists())
       {
           isLocal=false;
       }
       file = new File(Pathlocal);
       if(file.exists())
       {
           isLocal= true;
       }
   }
    public static String getPath() throws Exception
    {
        if(isLocation())
        {
            return Pathlocal;
        }else
        {
            return PathHost;
        }
    }

    public static boolean isLocation() throws Exception
    {
        return isLocal;
    }

    public static String getUrl() throws Exception
    {
        if(isLocation())
        {
            return "mongodb://hm:eql-LmnZ8xc9pxbg@localhost:3717/HDP-live?authSource=HDP-live&authMechanism=SCRAM-SHA-1";
        }
        else  {
            return "mongodb://hm:eql-LmnZ8xc9pxbg@"+getHosturl()+":3717/HDP-live?authSource=HDP-live&authMechanism=SCRAM-SHA-1";
        }
    }

    public static String getUrl(String dbName) throws Exception
    {
        if(isLocation())
        {
            return "mongodb://hm:eql-LmnZ8xc9pxbg@localhost:3717/"+dbName+"?authSource="+dbName+"&authMechanism=SCRAM-SHA-1";
        }
        else
        {
            return "mongodb://hm:eql-LmnZ8xc9pxbg@"+getHosturl()+":3717/"+dbName+"?authSource="+dbName+"&authMechanism=SCRAM-SHA-1";
        }
    }
    public static String getUrl(String dbName,String hostUrl) throws Exception
    {
        if(isLocation())
        {
            return "mongodb://hm:eql-LmnZ8xc9pxbg@localhost:3717/"+dbName+"?authSource="+dbName+"&authMechanism=SCRAM-SHA-1";
        }
        else
        {
            return "mongodb://hm:eql-LmnZ8xc9pxbg@"+hostUrl+":3717/"+dbName+"?authSource="+dbName+"&authMechanism=SCRAM-SHA-1";
        }
    }
    public static String getHosturl()
    {
        return "dds-bp1baff8ad4002a41.mongodb.rds.aliyuncs.com";
    }
    public static String getHosturl(String keyValue)
    {
        JSONObject standerdVersion = (JSONObject) Data.getJSON("/config/StandardVersion.json");
        return "";

    }
}
