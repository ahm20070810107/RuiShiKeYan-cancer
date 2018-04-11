package test.java.task_SLE_QueZzhengLeiJi;

import com.RuiShiKeYan.Common.Method.LocalHostInfo;
import com.RuiShiKeYan.dao.SSHLocalForward;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import test.java.task_SLE_LangChuang.BaseInfo_Title_ListValue_DBCondition;


/**
 * Created with IntelliJ IDEA
 * User:huangming
 * Date:2017/10/13
 * Time:下午8:39
 */


public class ExportShouZhenTable {

    public static  void main(String[] args) throws Exception
    {

        MongoClientURI uri = new MongoClientURI(LocalHostInfo.getUrl());
        MongoClient client = new MongoClient(uri);
        //本地通过跳板机跳转

        SSHLocalForward sshLocalForward = new SSHLocalForward(LocalHostInfo.getHosturl());
        if(LocalHostInfo.isLocation()) {
            sshLocalForward.connectSSH();
        }
        MongoDatabase db = client.getDatabase("HDP-live");

        String strALAConditon="{"+BaseInfo_Title_ListValue_DBCondition.HY13SLE+",'化验时间':{$exists:true,$regex:/^.{10,}$/},'化验结果定性（新）':'阳性'}";
        String strZZCondition="{"+BaseInfo_Title_ListValue_DBCondition.ZZTZ13SLE+",'否定词':'','症状1':{$ne:null,$exists:true,$ne:''},'症状&体征时间':{$exists:true,$regex:/^.{10,}$/}}}";
        String strTZConditon="{"+BaseInfo_Title_ListValue_DBCondition.ZZTZ13SLE+",'$or':[{'体征':{$ne:''}},{'体征定性描述':{$ne:''}}],'症状&体征时间':{$exists:true,$regex:/^.{10,}$/}}}";
        String strZDCondition=BaseInfo_Title_ListValue_DBCondition.strSLEZDLCCondition;

        ExportTables_SLE.getZDPID(db,strZDCondition);
        ExportTables_SLE.ExportADI(db, strZDCondition);
        ExportTables_SLE.getExcludeInfo();
        ExportTables_SLE.ExportALA(db,strALAConditon);
        ExportTables_SLE.ExportASYTZ(db,strTZConditon);
        ExportTables_SLE.ExportASYZZ(db,strZZCondition);
     //   ExportQueZhenTable.main(args,);

        client.close();
        if(LocalHostInfo.isLocation()) {
            sshLocalForward.closeSSH();
        }
    }


}
