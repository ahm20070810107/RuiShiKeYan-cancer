package com.RuiShiKeYan.Common.Method;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created with IntelliJ IDEA
 * User:huangming
 * Date:2017/10/19
 * Time:上午10:10
 */
public class Tools {


    public  ArrayList<String> getSplitArray(String source,char key)
    {
       StringBuilder sbSource=new StringBuilder(source);
        ArrayList<String> arrayResult = new ArrayList<String>();

        String tempStr="";
        for (int i = 0; i < sbSource.length(); ) {
            if(sbSource.charAt(i) != key)
                tempStr+=sbSource.charAt(i++);
            else
            {
                arrayResult.add(tempStr);
                tempStr="";
                sbSource.delete(0,i+1);
                i=0;
            }
            if(i==sbSource.length())
                arrayResult.add(tempStr);
        }

        return  arrayResult;
    }
}
