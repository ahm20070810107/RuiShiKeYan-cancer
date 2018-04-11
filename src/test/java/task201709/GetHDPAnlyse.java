package task201709;

import com.alibaba.fastjson.JSONObject;
import com.yiyihealth.data.DaX.reader.DSExcelReader2;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import  java.util.HashMap;
/**
 * Created with IntelliJ IDEA
 * User:huangming
 * Date:2017/9/28
 * Time:下午2:21
 */
public class GetHDPAnlyse {

    public static  void main(String[] args)throws Exception
    {
        SXSSFWorkbook sxssfWorkbook = new SXSSFWorkbook(2000);
        String table="ASY";
      //  SSHLocalForward sshLocalForward = new SSHLocalForward("dds-bp1baff8ad4002a42.mongodb.rds.aliyuncs.com");
       try {
          anlyseASY(sxssfWorkbook,table);
          //  anlyseADI(sxssfWorkbook,table);


       }catch (Exception e)
       {
           e.printStackTrace();
       }
finally {
        FileOutputStream fileOutputStream = new FileOutputStream("../../Desktop/"+ table+"二分表.xlsx");
        sxssfWorkbook.write(fileOutputStream);
        sxssfWorkbook.close();
        fileOutputStream.close();
       }
     //   sshLocalForward.closeSSH();

    }

    private static void anlyseADI(SXSSFWorkbook workBook,String table) throws Exception
    {
        String inputFile="../../Desktop/Material/HDP表/ADI_v0.3.xlsx";
        Map<String,Integer> mapTitle= new HashMap<String, Integer>();
        Map<String,Map<Integer,String>> mapResult = new HashMap<String, Map<Integer,String>>();
        String title="PID,医院,性别,第一次入院时间,当前年龄,胰腺恶性肿瘤,狼疮性肾炎,糖尿病,高血压,结核,肝炎,肺炎,风湿病,干燥综合症,发热,冠脉病变,胰腺恶性肿瘤,血吸虫病,狼疮性脑病,肺动脉高压,结缔组织病,紫癜和出血点,哮喘,血小板减少,肺结核,甲状腺机能减退症,心脏病,神经精神狼疮,肿瘤,肾病综合症,肾功能衰竭,胸腔积液,间质性肺炎,脂肪肝,腹腔妊娠,乙型病毒性肝炎,骨质疏松,流产,低蛋白血症,单纯性肾囊肿,血管瘤,脑梗死,缺血性坏死,胃肠道血管炎,心功能不全,间质性肺病,自然流产,上呼吸道感染,强直性脊柱炎,终末期肾病,泌尿道感染,抗心磷脂抗体综合征,高脂血症,心包积液,皮疹,肾性贫血,胆囊炎,病毒性肝炎,全血细胞减少,肝功能异常,心律失常,脱发,性传播疾病,白细胞减少,胆囊结石,呕吐,伤寒,自身免疫性溶血性贫血,腹腔积液,子宫平滑肌瘤,癫痫,左室舒张功能减退,贫血,甲状腺机能亢进症,溶血性贫血,杵状指,继发性干燥综合征,带状疱疹,脾肿大,多浆膜腔积液,胃炎,肝血管病,肾结石,腹痛,慢性淋巴细胞性甲状腺炎,甲状腺结节,关节痛,肝囊肿,高尿酸血症,狼疮性肝炎,低钾血症,肾小球肾炎,干眼症,乳腺增生,腔隙性脑梗死,胆囊息肉,慢性阻塞性肺病,瓣膜病变,关节炎,狼疮性肺炎,混合性结缔组织病,呼吸衰竭,胸痛,雷诺,肌炎,呼吸道感染,肺纤维化,电解质代谢紊乱,肝硬化,血管炎,炎性肠病,脑卒中,支气管炎,白内障,进行性系统性硬化症,狼疮性胃肠道损害,缺铁性贫血,头痛,腰椎间盘突出,结节性甲状腺肿,硬皮病,无菌性坏死,妊娠,亚临床甲状腺功能减退症,屈光不正,未分化结缔组织病,狼疮性血液系统损害,肝内胆管结石,陈旧性肺结核,骨量减少,卵巢囊肿,先天性结核病,急腹症,颈椎间盘疾患,先天性心脏病,败血症,结核性脑膜炎,隐球菌性脑膜炎,肾积水,心房颤动,淋巴瘤,高钾血症,肝病,胰腺炎,副脾,腺瘤,慢性胆囊炎,肝占位性病变,过敏性皮炎,骨折,抑郁,癌症,骨质增生,结核性胸膜炎,脊柱关节病,无尿,胸膜炎,多形性红斑,EB病毒感染,腹膜炎,肾炎,风湿性心脏病,银屑病样皮损,免疫性血小板减少性紫癜,血尿,浮肿,消化道出血,心脏扩大,肝脏结节,晶体沉积性关节病变,血栓性微血管病,胆汁返流,低T3综合征,胆囊胆固醇沉着症,粒细胞缺乏,先天性卵圆孔未闭,骨坏死,子宫腺肌症,周围神经病,原发性胆汁性肝硬变,皮肤血管炎,自身免疫性甲状腺炎,湿疹,自身免疫病,药物性皮炎,荨麻疹,盆腔积液,重症肌无力,食道运动障碍,肺结节,血栓性血小板减少性紫癜,脂质代谢紊乱,急性全鼻窦炎,死亡,肺栓塞,阑尾炎,呼吸困难,颅内感染,脐带绕颈,非免疫性贫血,急性上呼吸道感染,感染性病变,动脉粥样硬化,门静脉高压,心肌梗死,继发性甲状旁腺功能亢进症,药物性肝损害,肺不张,代谢性酸中毒,肝硬化失代偿期,骨髓异常,脊髓炎,中耳炎,食管炎,支气管扩张,淋巴结肿大,肾小管酸中毒,脱髓鞘综合症,脑萎缩,青光眼,肉芽肿,肺出血,特指肠道感染,巨细胞病毒感染,感冒,肺气肿,心肌病,骶髂关节改变,噬血细胞综合征,脑出血,肾错构瘤,梅毒,缺血性肠病,肺大泡,腮腺炎,感染性休克,脾脏病变,异位妊娠,结膜炎,高血压性心脏病,亚急性皮肤红斑狼疮,宫颈腺囊肿,肾周积液,滑膜炎,胎死宫内,高胆固醇血症,静脉血栓,多发性肝囊肿,下肢溃疡,高甘油三酯血症,上消化道出血,功能障碍性子宫出血,房间隔缺损,低钠血症,鼻窦炎,病毒性肝炎病原携带者,晕厥,低钙血症,心绞痛,消化道溃疡,提前自然临产伴足月产,膀胱炎,气胸,颈椎间盘突出,异常阴道出血,心肌炎,肌痛,丙型病毒性肝炎,乳突炎,附件囊肿,下肢动脉粥样硬化,糖耐量异常,特指甲状腺功能减退症,输尿管结石,大动脉炎,营养不良,冻疮样皮肤病变,胸腺病变,重度子痫前期,再生障碍性贫血,动脉瘤,皮肤和皮下组织局部感染,良性肿瘤,结节性红斑,视网膜病变,胎膜早破,精神障碍,心包炎,自身免疫性多腺体衰竭,视野缺损,虹膜睫状体炎,关节肿胀,风湿性关节炎,白血病,肺泡出血,上皮瘤和皮肤纤维瘤,焦虑,妊娠合并高血压,黄疸,视网膜血管炎,脂膜炎,偏头痛,盘状红斑,皮下结节,意识障碍,脑血管病,脾梗死,脊髓病,慢性病贫血,视神经病变,视神经脊髓炎,眼底病变,心内膜炎,狼疮性脊髓病,风湿热,化脓性关节炎,结核性心包炎,类风湿性关节呀,肌无力,病毒性心肌炎,脾动脉栓塞,关节积液,纯红细胞发育不良,狼疮性心包炎,妊娠合并糖尿病,结节病,淋巴结核,肝脏增大,静脉炎,CIDP,情感障碍,动脉栓塞,胸膜粘连,颅神经病,视盘水肿,药物性系统性红斑狼疮,浆膜炎,淋巴结钙化,假性肠梗阻,风湿性多肌痛,蛋白丢失性肠病,吞咽困难,腕管综合症,多发性关节炎,生殖器溃疡,血栓性静脉炎,妊娠合并血小板减少,眶周水肿,大疱性皮肤损害,血管性水肿,器质性脑病,隐匿性系统性红斑狼疮,肌肉萎缩,肌腱炎,新生儿红斑狼疮,关节脱位,口腔溃疡,色素沉着,结节性多动脉炎,狼疮性膀胱炎,认知障碍,无菌性脑膜炎,深部狼疮,指端硬化,妊娠合并肾病综合症,病态妊娠,结核性心包积液,角膜溃疡,特指局限性红斑狼疮,关节僵,糖尿病性心肌炎,单神经病,风湿性二尖瓣狭窄,风湿性二尖瓣狭窄伴关闭不全,毛细血管扩张,肠系膜动脉炎,蛋白尿,胆囊手术,多发性单神经炎,感染性心包炎,红斑肢痛,皮肤溃疡,幼年型类风湿关节炎";
        SXSSFSheet sheet = workBook.createSheet(table);
        JSONObject config = new JSONObject();
        config.put("filename",inputFile);
        config.put("source_type","excel");
        DSExcelReader2 excelReader = new DSExcelReader2(config);

        fillExcelTitle(sheet,title,mapTitle);
        getAnlyseresult(excelReader,mapTitle,mapResult,"患者（PID）","诊断状态","狼疮知识","诊断时间","是／不详");
        System.out.println(mapResult);
        saveExcel(  mapResult,sheet,"../../Desktop/Material/表库/ADO.xlsx");
        excelReader.close();

    }
    private static void anlyseASY(SXSSFWorkbook workBook,String table) throws Exception
    {
        String inputFile="../../Desktop/Material/HDP表/ASY_v0.5.xlsx";
        Map<String,Integer> mapTitle= new HashMap<String, Integer>();
        Map<String,Map<Integer,String>> mapResult = new HashMap<String, Map<Integer,String>>();
        String title="PID,医院,性别,第一次入院时间,当前年龄,背部疼痛,鼻黏膜溃疡,鼻窦压痛,鼻塞,鼻翼扇动,鼻中隔偏曲,扁桃体肿大,扁桃体渗出,扁桃体大,扁桃体充血,扁桃体脓点,假性肠梗阻,肠系膜功能不全,肠系膜血管炎,齿龈肿胀,齿龈溢脓,口唇发绀,唇紫绀,大血管枪击音,瓣膜病变,肺动脉高压,肺纤维化,肺梗死,肺出血,肺炎,肺部感染,肺鼓音,肺过清音,肺实音,肺湿啰音,罗音,肺部杂音,慢性间质性肺炎,肺泡出血,腹痛,腹腔积液,腹部包块,腹肌紧张,腹膨隆,肠鸣音亢进,肠鸣音减弱,腹部气过水音,腹膜炎,肝脏增大,肝功能异常,肝血管病变,肝区叩痛,急腹症,肝脏结节,肛周溃疡,骨质疏松,骨髓异常,关节积液,关节肿胀,关节晨僵,关节脱位,关节强直,关节畸形,关节痛,关节活动受限,咯血,肌痛,肌炎,肌肉萎缩,肌无力,肌腱挛缩,肌腱断裂,棘突压痛,脊柱畸形,甲襞血管炎,甲周红斑,甲周毛细血管扩张,甲状腺肿大,甲状腺压痛,甲状腺震颤,角膜混浊,炎性肠病,结膜苍白,结膜充血,颈抵抗,颈静脉怒张,颈静脉充盈,颈部强直,静脉曲张,腹壁静脉曲张,胸壁静脉曲张,胸壁静脉充盈,口干,口唇紫绀,口腔脓点,肝包块,淋巴结肿大,淋巴结病变,丘疹鳞屑型,麦氏点压痛,毛细血管扩张,面部瘙痒,脑血管病,脑血管炎,皮肤松弛,皮肤苍白,皮肤潮红,皮肤感染,皮肤绀红,皮肤脱水,皮肤溃疡,皮下捻发感,脾脏病变,脾肿大,脐突出,癫痫,疲乏,浮肿,发热,蝶形红斑,乳突压痛,软组织钙化,肾脏病变,肾区移动性浊音,肾区叩痛,肾小球肾炎,食道运动异常,视盘水肿,视神经病变,视网膜病变,视网膜血管炎,雷诺,手指血管炎,四肢畸形,四肢叩痛,剑突压痛,头痛,脱发,头颅畸形,头颅凹陷,生殖器溃疡,腕管综合征,慢性胃炎,消化道溃疡,手指缺如,肢体缺如,下肢溃疡,上消化道狭窄,心功能不全,心界大,心绞痛或冠状动脉搭桥术后,心力衰竭,心包粘连,心包渗出,心包增厚,心包炎,心包积液,心包摩擦音,心包震颤,心尖搏动弥散,心尖震颤,心前区震颤,心前区隆起,心前区搏动异常,心脏杂音,心脏震颤,水冲脉,心脏抬举感,心脏枪击音,心脏增大,轻度或间歇性胸痛,胸痛,胸廓隆起,胸膜增厚,胸膜炎,胸膜摩擦音,胸膜粘连,胸膜渗出,胸腔积液,腹部血管杂音,甲状腺血管杂音,牙齿脱落,牙龈肿胀,咽充血,咽部充血,咽部红肿,咽喉充血,眼干,眼部溃疡,眼底病变,眼肌损害,眼皮水肿,眶周水肿,突眼,眼球凹陷,腰痛,胰腺炎,阴道流血,粘膜DLE,粘膜瘢痕形成,支气管炎,肢体瘫痪,植物神经病,手指肿胀,指端硬化,自主神经功能紊乱,手足掌面红斑,口腔溃疡,血栓栓塞,瘢痕型脱发,不规则皮疹,厌食,大疱性皮肤损害,冻疮样皮肤病变,多形性红斑,非瘢痕型脱发,性格改变,情感障碍,其它红斑,环状红斑,体重下降,结节性红斑,盘状红斑,脱髓鞘综合征,网状青斑,萎缩性白斑样,无菌性坏死,缺血性坏死,认知障碍,记忆力减退,精神行为异常,银屑病样皮损,意识障碍,紫癜和出血点,皮下结节,昏迷,疲劳,呕吐,恶心,斑丘疹,钙质沉着,光过敏,类风湿结节,色素沉着,黄疸,骨折,呼吸困难,心律失常,跛行,荨麻疹,蛋白尿,吞咽困难,泡沫尿,其它皮疹,咳嗽,胸闷,咳痰,腹泻,畏寒,头晕,呼吸音粗,气促,尿频,尿急,尿痛,慢性病容,寒战,震颤,蜘蛛痣,肝掌,心悸,贫血貌,咽痛,心慌,呼吸运动减弱,呼吸运动增强,排尿困难,血尿,多尿,多饮,多食,多汗,剧吐,黑便,呕血,反酸,耳鸣,瘙痒,声音嘶哑,睡眠欠佳,腹胀,满月脸,肉眼血尿,饮食异常,盗汗,睡眠差,心累,龋齿,发育畸形,皮肤出汗,视物模糊,流涕,脱屑,头昏,贫血,干咳,咯痰,扑翼样震颤,感冒,晕厥,夜尿增多,少尿,平卧不能,怀孕,经量少,鼻衄,P2亢进,畏光,呼吸音低,带状疱疹,急性病容,便血,便秘,S2亢进,S1亢进";
        SXSSFSheet sheet = workBook.createSheet(table);
        JSONObject config = new JSONObject();
        config.put("filename",inputFile);
        config.put("source_type","excel");
        DSExcelReader2 excelReader = new DSExcelReader2(config);

        fillExcelTitle(sheet,title,mapTitle);
        getAnlyseresult(excelReader,mapTitle,mapResult,"患者（PID）","否定词","概念","症状&体征时间","");
       // System.out.println(mapResult);
        saveExcel(  mapResult,sheet,"../../Desktop/Material/表库/ADO.xlsx");
        excelReader.close();

    }


    private  static  void saveExcel( Map<String,Map<Integer,String>>  mapResult,SXSSFSheet sheet,String baseInfoFile) throws Exception
    {
        List<JSONObject> list = new ArrayList<JSONObject>();
        JSONObject document;
        JSONObject config = new JSONObject();
        config.put("filename",baseInfoFile);
        config.put("source_type","excel");

        DSExcelReader2 baseInfoReader = new DSExcelReader2(config);

        while((document=baseInfoReader.nextDocument())!=null)
        {
            list.add(document);
        }
        baseInfoReader.close();
        int RowNum=1;

        for(Map.Entry<String,Map<Integer,String>> enty :mapResult.entrySet())
        {
            Row row =sheet.createRow(RowNum++);
            Map<Integer,String> tempMap = enty.getValue();

            row.createCell(0).setCellValue(enty.getKey());

            for (int i = 0; i < list.size(); i++) {
                if(list.get(i).getString("PID").equals(enty.getKey()))
                {

                    row.createCell(1).setCellValue(list.get(i).getString("医院"));
                    row.createCell(2).setCellValue(list.get(i).getString("性别"));
                    row.createCell(3).setCellValue("空");
                    try {
                        Integer age= Integer.valueOf(2017)-Integer.valueOf(list.get(i).getString("出生年"));
                        row.createCell(4).setCellValue(age.toString());
                    }catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                      //  flag=true;
                    break;
                }
            }
            for(Map.Entry<Integer,String> tempEntry:tempMap.entrySet())
            {
                row.createCell(tempEntry.getKey()).setCellValue(tempEntry.getValue());
            }
    }

    }


    private  static void getAnlyseresult(DSExcelReader2 reader ,Map<String,Integer> map,Map<String,Map<Integer,String>> reultMap,String key,String notDic,String judgeValue,String time,String yesValue) throws Exception
    {
        JSONObject document =null;

         while((document=reader.nextDocument())!=null)
         {
             if(document.getString(judgeValue)==null||document.getString(judgeValue).equals(""))
                 continue;
            // String keyword= document.getString(key);
             if(reultMap.get(document.getString(key))==null) //当前病人未存入值
             {
                   Map<Integer,String> tempMap= new HashMap<Integer, String>();
                   if(notDic.equals("")||document.getString(notDic) == null ||(!yesValue.equals("")&&yesValue.indexOf(document.getString(notDic))>-1)||!document.getString(notDic).equals(""))
                      tempMap.put(map.get(document.getString(judgeValue)),document.getString(time));
                   else tempMap.put(map.get(document.getString(judgeValue)),"0");

                   reultMap.put(document.getString(key),tempMap);
             }else
             {
                 Map<Integer,String> tempMap =reultMap.get(document.getString(key));
               //  Integer location=map.get(document.getString(judgeValue));  //获取将存入值在excel的位置

                  if(tempMap.get(map.get(document.getString(judgeValue)))!=null)
                  {
                      if(notDic.equals("")||document.getString(notDic) == null ||yesValue.indexOf(document.getString(notDic))>-1)
                      {
                          tempMap.put(map.get(document.getString(judgeValue)), document.getString(time));
                      }
                  }
                  else
                  {
                      if(notDic.equals("")||document.getString(notDic) == null ||yesValue.indexOf(document.getString(notDic))>-1)
                          tempMap.put(map.get(document.getString(judgeValue)),document.getString(time));
                      else tempMap.put(map.get(document.getString(judgeValue)),"0");
                  }
             }
         }


    }

    /*
    * 以","分割的字符串填充excel表头，并返回表头名称及所在列位置的map
    * */
    private static void fillExcelTitle(SXSSFSheet sheet,String title,Map<String,Integer>  map)
    {
       String[] titles = title.split(",");
        Row row = sheet.createRow(0);
        for (int i = 0; i <titles.length ; i++) {
            row.createCell(i).setCellValue(titles[i]);
            map.put(titles[i],i);
        }

      //  System.out.println(map.get(""));
    }

}
