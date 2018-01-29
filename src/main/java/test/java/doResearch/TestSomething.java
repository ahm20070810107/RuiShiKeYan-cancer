package test.java.doResearch;



import com.RuiShiKeYan.Common.Method.FileHelper;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;


/**
 * Created with IntelliJ IDEA
 * User:huangming
 * Date:2017/9/27
 * Time:下午12:32
 */
public class TestSomething {

    public static void main(String[] str1)
    {
       String filestr= FileHelper.ReadStringFromPath("/config/config.json",true);
       JSONObject obj= JSON.parseObject(filestr);
       System.out.print(obj.getString("hello"));



       int aa=100;

       aa=(int)(0.05*aa);
       System.out.print(aa);
    }


}
