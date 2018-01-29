//package com.yiyihealth.nlp.nlpwebservers.rs
package com.RuiShiKeYan.EntityStatistic

import com.alibaba.fastjson.JSONObject
import com.RuiShiKeYan.Common.Method.FileHelper
import com.RuiShiKeYan.Common.Method.LocalHostInfo
import com.RuiShiKeYan.EntityStatistic.getInitQueryWidthType
import com.RuiShiKeYan.EntityStatistic.getMongoConnection
import com.RuiShiKeYan.EntityStatistic.getRemovePIDS
import com.yiyihealth.data.DaX.reader.DSExcelReader2
import com.yiyihealth.data.DaX.reader.DSMongoMapReduce2
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.util.ArrayList
import kotlin.collections.HashMap



data class Item(val 子项:String?,val 拟观察系统累及分组:String?)

class Mapping(var excel:String){
    val map = object :HashMap<String,HashMap<String,Item>>(){
        override fun get(key: String): HashMap<String, Item>? {
            var cache = super.get(key)
            if(cache == null){
                cache = HashMap<String,Item>()
                super.put(key,cache)
            }
            return cache
        }
    }
    init {
        var config = JSONObject()
        config.put("filename",excel)
        var red = DSExcelReader2(config)
        var r :JSONObject? = red.nextDocument()
        while (r != null){
            val item = Item(r.getString("子项"),r.getString("拟观察系统累及分组"))
            map.get(r.getString("表型名称"))!!.put(r.getString("标准标本"),item)
            r = red.nextDocument()
        }
    }

    fun getItem(biaoxing:String,biaoben:String):Item?{
        return  map.get(biaoxing)!!.get(biaoben)
    }
}

//java -cp nlpwebservers.jar com.yiyihealth.nlp.nlpwebservers.rs.ExportExcel2Kt

fun main(args: Array<String>) {
    var debug = true
    if(args.size > 0) debug = false
    var mapping = Mapping(LocalHostInfo.getPath()+"95系统累及分组标注表.xlsx")
    val removePIDS = getRemovePIDS()
    val types = arrayOf("用药","诊断","化验","体征","症状")
//    val types = arrayOf("体征")
    val mongoConnection = getMongoConnection(debug)
    var fileName = LocalHostInfo.getPath()+"交付/实体表型数统计.xlsx"
    val sxssfWorkbook = SXSSFWorkbook(100)
    val sxssfSheet = sxssfWorkbook.createSheet()
    var rowNum = 0
    val row = sxssfSheet.createRow(rowNum++)
    val head = arrayOf("实体类别", "实体名称", "实体标本", "表型名称", "标准标本", "数量", "阳性数", "PID数", "子项", "拟观察系统累及分组")
    for (i in head.indices) {
        val cell = row.createCell(i)
        cell.setCellValue(head[i])
    }
    types.forEach {
        val collectionName = getTableName(it)
        val  mapReduce = DSMongoMapReduce2(FileHelper.ReadStringFromPath("/js/map3$it.js",true),FileHelper.ReadStringFromPath("/js/reduce3.js",true))
        val dbCollection = mongoConnection.mongoDatabase.getCollection(collectionName)
        val initQuery = getInitQueryWidthType(it,removePIDS)
        if(debug){
            mapReduce.doMapReduce(dbCollection.find(initQuery).limit(200))
        }else{
            mapReduce.doMapReduce(dbCollection.find(initQuery))
        }
        var obj:JSONObject? = mapReduce.nextDocument()
        while (obj != null){
         //   println(obj)
            var row = sxssfSheet.createRow(rowNum++)
            val coloumns = ArrayList<String>()
            coloumns.add(it)
            var _id = obj.getJSONObject("_id")
            var value = obj.getJSONObject("value")
            var biaoxing = _id.getString("表型名称")
            var biaoben = _id.getString("标准标本")
            var item = mapping.getItem(biaoxing, biaoben)
            coloumns.add(_id.getString("实体名称"))
            coloumns.add(_id.getString("实体标本"))
            coloumns.add(biaoxing)
            coloumns.add(biaoben)
            coloumns.add(value.get("count").toString())
            coloumns.add(value.get("使用Count").toString())
            coloumns.add(value.get("pidSize").toString())
            if(item == null){
                coloumns.add("")
                coloumns.add("")
            }else{
                coloumns.add(item?.子项?:"")
                coloumns.add(item?.拟观察系统累及分组?:"")
            }
            coloumns.forEachIndexed { index, s ->
                row.createCell(index).setCellValue(coloumns.get(index))
            }
            obj =  mapReduce.nextDocument()
        }

    }
    try {
        sxssfWorkbook.write(FileOutputStream(File( fileName)))
        sxssfWorkbook.close()
        mongoConnection.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
internal inline fun getTableName(type:String):String{
    when(type){
        "用药"->return "ADR"
        "症状"->return "ASY"
        "体征"->return "ASY"
        "诊断"->return "ADI"
        "化验"->return "ALA"
    }
    throw RuntimeException("未定义的类型")
}