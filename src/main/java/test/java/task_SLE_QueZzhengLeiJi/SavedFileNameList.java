package test.java.task_SLE_QueZzhengLeiJi;

import com.RuiShiKeYan.Common.Method.DateFormat;

/**
 * Created with IntelliJ IDEA
 * User:huangming
 * Date:2017/12/13
 * Time:下午5:41
 */
public class SavedFileNameList {
    private static String fileNameVersion= DateFormat.getStringDateShort();
    private static String strPreffix=".xlsx";

    public static  String strSleBiaoXianTable="SLE表现表"+strPreffix;
    public static  String strSleLeiJiTable="SLE累及表"+strPreffix;
    public static String strSleProbabilityTtable="SLE累及概率表"+strPreffix;
}
