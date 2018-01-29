package com.RuiShiKeYan.Common.Method;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yiyihealth.data.DaX.utils.FileHelper;
import com.yiyihealth.data.DaxHandler.check.CheckData;

import java.io.InputStream;


public class Data {
	
	
	private static JSONObject mData = new JSONObject();

	static {
		InputStream is = CheckData.class.getResourceAsStream("/datas/constants");
		String str = FileHelper.readFile(is);
		mData = JSONObject.parseObject(str);
	}
	
	public static JSONArray getArray(String name){
		JSONArray array = mData.getJSONArray(name);
		if(array == null)array = new JSONArray();
		return array;
	}
	
	public static JSON getJSON(String path){
		InputStream is = CheckData.class.getResourceAsStream(path);
		String str = FileHelper.readFile(is);
		JSON json = (JSON) JSON.parse(str);
		return json;
	}
	
}
