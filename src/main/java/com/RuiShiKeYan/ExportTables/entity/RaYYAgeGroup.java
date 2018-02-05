package com.RuiShiKeYan.ExportTables.entity;

import com.RuiShiKeYan.Common.Method.DateFormat;

/**
 * Created with IntelliJ IDEA
 * User:huangming
 * Date:2018/2/5
 * Time:上午10:52
 */
public class RaYYAgeGroup {
    private static String[] femaleGroup={"未成年女性","生育高峰期","高龄产妇","围绝经期","老年女性"};
    private static String[] maleGroup={"未成年男性","18-35岁男性","36-45岁男性","46-60岁男性","60岁以上男性"};

    public static int getAgeIndex(String strAge)
    {
        try {
           Integer age=Integer.valueOf(strAge);
           if(age <=17)
               return 0;
           if(age >17 && age <=35)
               return 1;
           if(age >35 && age <=45)
               return 2;
           if(age >45 && age <=60)
               return 3;
           return 4;
        }catch (Exception e)
        {
            e.printStackTrace();
            return -1;
        }
    }
    public static String getRaAgeGroupName(String sex,int ageIndex)
    {
        try {
            if (sex.equals("男") || sex.contains("男"))
                return maleGroup[ageIndex];
            if (sex.equals("女") || sex.contains("女"))
                return femaleGroup[ageIndex];
            return "";
        }catch (Exception e)
        {
            e.printStackTrace();
            return "";
        }
    }

    public static String getIndexToMaxAge(int index,String startTime,String strAge)
    {
        try {
            int age = Integer.valueOf(strAge).intValue();
            int ageDiff=0;
            if(index == 0)
                ageDiff=17-age;
            if(index == 1)
                ageDiff=35-age;
            if(index == 2)
                ageDiff=45-age;
            if(index == 3)
                ageDiff=60-age;
            if(index == 4)
                return "全病程";
            return DateFormat.getNextDay(startTime,ageDiff*360);
        }catch (Exception e)
        {
            e.printStackTrace();
            return "";
        }
    }
}
