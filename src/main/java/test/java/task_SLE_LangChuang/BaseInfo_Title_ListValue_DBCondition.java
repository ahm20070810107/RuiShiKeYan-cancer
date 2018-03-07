package test.java.task_SLE_LangChuang;

/**
 * Created with IntelliJ IDEA
 * User:huangming
 * Date:2017/10/23
 * Time:下午5:05
 */
public class BaseInfo_Title_ListValue_DBCondition {
     //Excel表头
    public static  String titleLCShengYanRuZB="医院,患者（PID）,出生年,SLE时间天,SLE病历（RID）,类风湿性关节炎诊断名称,SLE年龄,最早记录时间天,入院前病程,最晚记录时间天,观察期终点,确诊SLE后病程分组,糖尿病时间天,糖尿病病历（RID）,糖尿病诊断名称,糖尿病年龄,糖尿病分组,肾损害时间天,肾损害病历（RID）,肾损害名称,肾损害年龄,肾损害分组,狼疮性肾炎时间天,狼疮性肾炎实体,狼疮性肾炎状态,狼疮性肾炎病历（RID）,狼疮性肾炎名称,狼疮性肾炎年龄,狼疮性肾炎分组,入组判定" ;
    public static String titleLCShengYanYinShuB="医院,患者（PID）,出生年,性别,地域,观察期终点,SLE时间天,SLE年龄,SLE年龄分组,初发时间天,初发年龄,初发年龄分组,确诊时间天,狼疮性肾炎时间天,狼疮性肾炎分组,最终分组";
    public static String titleLCShengYanYinShuB1="患者（PID）,最终分组,医院,出生年,性别,地域,SLE诊断年,SLE年龄,SLE年龄分组,初发年,初发年龄,初发年龄分组,确诊时间天";
    public static String titleLCShengYanYinShuB2="患者（PID）,最终分组,医院,出生年,性别,地域,SLE诊断年,SLE年龄,SLE年龄分组,初发年,初发年龄,初发年龄分组,确诊时间天";
    public static String titleLCShengYanYinShuB3="患者（PID）,最终分组,医院,出生年,性别,地域,SLE诊断年,SLE年龄,SLE年龄分组,初发年,初发年龄,初发年龄分组,确诊时间天";
    public static String titleLCShengYanYinShuB4="患者（PID）,最终分组,医院,出生年,性别,地域,SLE诊断年,SLE年龄,SLE年龄分组,初发年,初发年龄,初发年龄分组,确诊时间天";
    public static String titleLCShengYanYinShuB5="患者（PID）,最终分组,医院,出生年,性别,地域,SLE诊断年,SLE年龄,SLE年龄分组,初发年,初发年龄,初发年龄分组,确诊时间天";
    public static String tiltlePIDChouYangB="患者（PID）,性别,出生年rid,出生年,实体类型,RID,锚点,实体名称,实体标准名,状态1,状态2,时间,时间天,实体年龄,初发表现标注,对标观察项目,子项,拟观察系统累及分组";
    public static String tiltleXitongleiji="医院,患者（PID）,出生年,性别,地域,最晚记录时间天,对标观察项目,累及表现,累及rid,表型,锚点,上下文,累及时间天,累及时间年减去出生年,初发时间天,初发年龄,初发年龄分组,累及时间天减去初发时间天,最晚记录时间天减去初发时间天,首诊时间天,首诊年龄,首诊年龄分组,累及时间天减去首诊时间天,确诊时间,病程天,病程年";
    public static String tiltleChuFaleiji="医院,患者（PID）,出生年,性别,地域,最晚记录时间天,子项,拟观察系统累及分组,子项表现,子项rid,表型,锚点,上下文,子项时间天,子项时间年减去出生年,初发时间天,初发年龄,初发年龄分组,子项时间天减去初发时间天,最晚记录时间天减去初发时间天,首诊时间天,首诊年龄,首诊年龄分组,子项时间天减去首诊时间天,确诊时间,生产状况分组";
    public static String tiltlePingFenTable="患者（PID）,最终得分,得分分组,ANA评分标记,ANA评分标记时间天,ANA评分标记名称,ANA评分标记RID,医院,出生年,性别,地域,SLE首诊时间天,SLE年龄,SLE年龄分组";
    public static String titleWPSEntityStatistic="医院,患者数,男性患者数,女性患者数,病历数,入院病历数,出院病历数,最早病历时间,最晚病历时间,基本信息表型数,诊断表型数,用药表型数,化验表型数,症状体征表型数";
    public static String tiltleLeiJiFenZu="频次,表型,表型名称,标准标本,对标观察项目,子项,拟观察系统累及分组,2017诊断评分一级,2017诊断评分二级,2017诊断评分标记";
    public static String titleSleBiaoXianB="患者（PID）,医院,出生年,性别,地域,首诊时间天,首诊年龄,首诊年龄分组";
    public static String titleSleLeiJiB="患者（PID）,医院,出生年,性别,地域,首诊时间天,首诊年龄,首诊年龄分组,最晚记录时间天,病程天,病程年";
    //查询列表

    public static  String condHYLangChuangShengYan="'24小时尿蛋白定量试验','尿蛋白定性试验','尿蛋白/肌酐比值','管型'";
    public static  String condHYShengShunHai="'24小时尿蛋白定量试验','尿蛋白定性试验','尿蛋白/肌酐比值','肌酐','管型'";
    public static  String condZDTangNiaoB="'糖尿病','2型糖尿病','类固醇性糖尿病','特指糖尿病','继发性糖尿病','糖尿病性肾病','DM','妊娠期糖尿病','糖尿病酮症','糖尿病性白内障','1型糖尿病','医源性糖尿病','糖尿病性酮症酸中毒','dm','结节性糖尿病肾小球硬化症','糖尿病家族史','糖尿病性心肌炎','糖尿病性足病'";
    public static  String condZDShengYan="'狼疮性肾炎','肾病综合征','肾功能不全','肾病','慢性肾功能不全','慢性肾脏病5期','肾炎(肾小球肾炎) NOS','慢性肾衰竭','慢性肾脏病3期','蛋白尿','急性肾衰竭','腹膜透析','慢性肾脏病1期','急性肾功能不全','慢性肾炎','血液透析','肾透析','膜性肾病','慢性肾脏病4期','肾衰竭','慢性肾病-II期','尿毒症','慢性肾小球肾炎','慢性肾功能衰竭','慢性肾炎综合征','慢性肾脏病2期','急性肾炎','系膜增生性肾小球肾炎','慢性肾脏病','IgA肾病','系统性红斑狼疮肾','急进性肾炎','急性肾小球肾炎','右颈内静脉留置带涤纶套透析导管','系膜增生性肾炎','维持性血透','肾功能异常','慢性肾功能衰竭,尿毒症期','急性肾功能损害','狼疮性肾炎血液系统受累','过敏性紫癜性肾炎','腹膜透析导管留置状态','弥漫增生性合并膜性狼疮性肾炎','蛋白尿，局灶性节段性肾小球硬化','右颈内静脉留置长期透析导管','膜增殖性肾小球肾炎','新月体性肾小球肾炎','腹膜透析后腹膜炎','右颈内静脉留置临时透析导管','新月体肾炎','持续性非卧床腹膜透析','肾炎综合症','狼疮肾炎IV（A/G）型','狼疮累及肾脏','急性间质性肾炎','右股静脉留置临时透析导管','肾功能失代偿期','急性肾功能损伤','快速进展型肾衰竭','系膜增殖性肾小球肾炎','狼疮性肾炎（Ⅳ型）改变伴节段新月体形成','间质性肾炎','增殖性肾小球肾炎','膜性肾小球肾炎','腹膜透析管移位','腹膜透析状态','狼疮性肾炎V+IV型（伴新月体形成）','狼疮性肾炎IV型（伴新月体形成）','狼疮性肾损害','急进型肾小球肾炎','ANCA相关性肾炎','狼疮间质性肾炎','腹膜透析导管出口感染','急进型肾炎综合征','移植肾功能不全','毛细血管增生性肾小球肾炎','心肾衰竭','肾透析状态','维持性血液透析状态','肾功能失代偿CKD3','狼疮性肾炎（IV-G（A/C）+Ⅴ）伴较多新月体形成','慢性肾功能不全，氮质血症期','慢性肾炎慢性肾病－V期','毛内增生性肾小球肾炎','血管炎性肾小球肾炎','膜性肾炎','慢性肾盂肾炎','肾透析的动静脉造瘘','系膜增生性肾炎伴硬化','新月体型肾小球肾炎','3型狼疮肾炎改变','慢性肾脏病5期贫血','慢性间质性肾炎','肾病综合征，膜性增生性肾小球肾炎','慢性系膜增生性肾小球肾炎','急性肾炎综合征','硬化性肾炎','急性肾功能不全（RPGN）','急性肾炎综合症','肾性贫血性肾功能不全','维持性血压透析','狼苍肾炎','透析导管相关菌血症','CRF（尿毒症）','狼疮肾炎（IV型A/G）','血液透析后','膜性增生性肾小球肾炎','腹膜透析管置入术后','狼疮性肾炎V+IV型（（伴新月体形成））'";

    //查询条件
//   public static String ADR13="'Tab_Version':'3.0','projectProcessId':{$in:[26725874,26725981,26722459,26722968,26722977,26723071,26724501,26725038,26725103,26726621,26725655,26725739,26726762]}";
    public static String ADR13="'Tab_Version':'3.0','projectProcessId':{$in:[1518189131863,31649041,1514475453889,1514477396717,1514477401710,1514477417855,1514477421844,1514477433508,1514477436832,1514477441701,1514477446375,1514477451505,1514477454960,1514477459016]}";
    public static String ADO13="'projectProcessId':{$in:[1518189131863,31649041,1514128252661,1514128259831,1514128273815,1514128279314,1514128284357,1514128290550,1514128296051,1514128301173,1514128306936,1514128312064,1514128317372,1514128267906]},'Tab_Version':'VB1.0.1'";
    public static String YiYuan13="{$in:['57b1e21fd897cd373ec7a0e7','57b1e21fd897cd373ec7a117','57b1e21fd897cd373ec7a118','57b1e21fd897cd373ec7a14f','57b1e217d897cd373ec78454','57b1e21ad897cd373ec78e8f','57b1e211d897cd373ec76dc6'," +
            "'57b1e219d897cd373ec78a19','57b1e21fd897cd373ec7a215','57b1e21ad897cd373ec78f91','57b1e216d897cd373ec7815c','57b1e219d897cd373ec78a1a','57b1e222d897cd373ec7ad22','57b1e222d897cd373ec7ad23']}";
    public static String YiYuanList="57b1e21fd897cd373ec7a0e7,57b1e21fd897cd373ec7a117,57b1e21fd897cd373ec7a118,57b1e21fd897cd373ec7a14f,57b1e217d897cd373ec78454,57b1e21ad897cd373ec78e8f,57b1e211d897cd373ec76dc6,57b1e219d897cd373ec78a19,57b1e21fd897cd373ec7a215,57b1e21ad897cd373ec78f91,57b1e216d897cd373ec7815c,57b1e219d897cd373ec78a1a,57b1e222d897cd373ec7ad22,57b1e222d897cd373ec7ad23";
    public static String ZZTZ13SLE="'projectProcessId':{$in:[1518189131863,31649041,1514475453889,1514477396717,1514477401710,1514477417855,1514477421844,1514477433508,1514477436832,1514477441701,1514477446375,1514477451505,1514477454960,1514477459016]},'Tab_Version':'2.0'";
    public static String ZD13SLE="'Tab_Version':'2.0','projectProcessId':{$in:[1518189131863,31649041,1514128252661,1514128259831,1514128273815,1514128279314,1514128284357,1514128290550,1514128296051,1514128301173,1514128306936,1514128312064,1514128317372,1514128267906]}";
  //  public static String HY13SLE="'Tab_Version':'3.02','projectProcessId':{$in:[31645224,31647760,31647826,31647918,31648036,31648691,31649041,31649573,31650348,31650634,31651371,31651606,31652349]}";
    public static String HY13SLE="'Tab_Version':'3.02','projectProcessId':{$in:[1518189131863,1514475453889,1514477396717,1514477401710,1514477417855,1514477421844,1514477433508,1514477436832,1514477441701,1514477446375,1514477451505,1514477454960,1514477459016,33394229]}";
    public static String BH13SLE="'Tab_Version':'VB1.0.1','projectProcessId':{$in:[1515384977821,1515384983671,1515384990527,1515381762394,1515381769740,1515381774479,1515381780329,1515381786449,1515381790778,1515381795011,1515381798858,1515399684275,1515382100969,1517568038854]}";

//    static {
//         String strpid= "'PID' : {$in:['xiangya_E1980728','7f19b34d5d4acbf09b5782297a5da66f','c9761d247f97fdfdff963c5fc05d0402']}, ";
//        ADR13=strpid+ADR13;
//        ADO13=strpid+ADO13;
//        ZZTZ13SLE=strpid+ZZTZ13SLE;
//        HY13SLE=strpid+HY13SLE;
//        ZD13SLE=strpid+ZD13SLE;
//    }

    public static String strTZConditon="{"+ZZTZ13SLE+",'症状&体征时间':{$exists:true,$regex:/^.{10,}$/},'$or':[{'体征':{$ne:''}},{'体征定性描述':{$ne:''}}]}";
    public static String strTZConditon1="{"+ZZTZ13SLE+",'$or':[{'体征':{$ne:''}},{'体征定性描述':{$ne:''}}]}";
    public static String strZZConditon="{"+ZZTZ13SLE+",'症状&体征时间':{$exists:true,$regex:/^.{10,}$/},'症状1':{$exists:true,$ne:''},'否定词':''}";
    public static String strHYCondition="{"+HY13SLE+",'化验时间':{$exists:true,$regex:/^.{10,}$/}";
    public static String strShengYanZDCondition="{'诊断状态':'是','标准诊断名':{$in:["+condZDShengYan+"]},"+ZD13SLE+",'诊断时间':{$exists:true,$regex:/^.{10,}$/}}";
    public static  String strSLEZDLCCondition="{'诊断时间':{$exists:true,$regex:/^.{10,}$/},'标准诊断名':'类风湿性关节炎','诊断状态':'是',"+ZD13SLE+"}";
    public static  String strZDLCShengyanCondition="{'诊断时间':{$exists:true,$regex:/^.{10,}$/},'诊断状态':'是',"+ZD13SLE;
    public static String strSLETangNiaoBCondition="{'诊断时间':{$exists:true,$regex:/^.{10,}$/},'标准诊断名':{$in:["+condZDTangNiaoB+"]},'诊断状态':'是',"+ZD13SLE+"}";


    //配置参数
    public static String strCLeiJiFenZuFileName="95系统累及分组标注表.xlsx";
}
