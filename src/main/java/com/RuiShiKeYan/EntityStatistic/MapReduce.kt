package com.RuiShiKeYan.EntityStatistic

import com.alibaba.fastjson.JSONObject
import com.RuiShiKeYan.Common.Method.FileHelper
import com.yiyihealth.data.DaX.ds.mongo.MDBConnection
import com.yiyihealth.data.DaX.reader.DSMongoMapReduce2
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.bson.Document
import java.io.File
import java.io.FileOutputStream
import java.util.*

inline fun getMDSMongoCollection(debug: Boolean): MDBConnection {

    var mongoConnection =  MDBConnection()
    if(debug){
        mongoConnection.connectToDBServer("114.55.149.219:3718", "SDS-live", "nlp", "lm", "6Swib%3addgzuaIzc5");
    }else{
        mongoConnection.connectToDBServer("dds-bp1baff8ad4002a42.mongodb.rds.aliyuncs.com:3717", "SDS-live", "nlp", "lm", "6Swib%3addgzuaIzc5");
    }
    return  mongoConnection
}

//java -cp nlpwebservers.jar com.yiyihealth.nlp.nlpwebservers.rs.MapReduceKt
fun main(args: Array<String>) {
    var queryFlie = File("./query.json")
    var mapFile = File("./map.js")
    var reduceFile = File("./reduce.js")
    if(queryFlie.exists() && mapFile.exists() && reduceFile.exists()){
        var dbc = getMDSMongoCollection(args.isEmpty())
        var queryJSON = Document.parse(FileHelper.ReadStringFromPath(queryFlie.absolutePath))
        var dbName = "msdata"
        if(queryJSON.containsKey("dbName")) dbName = queryJSON.getString("dbName")
        var dbCollection = dbc.mongoDatabase.getCollection(dbName)
        var query = if(queryJSON.containsKey("query"))(queryJSON["query"] as Document) else Document()
        val  mapReduce = DSMongoMapReduce2(FileHelper.ReadStringFromPath(mapFile.absolutePath),FileHelper.ReadStringFromPath(reduceFile.absolutePath))
        var limit:Int = if(queryJSON.containsKey("limit")) queryJSON.getInteger("limit") else -1
        var iter = dbCollection.find(query)
        if(limit > 0) iter = iter.limit(limit)
        mapReduce.doMapReduce(iter)
        var obj:JSONObject? = mapReduce.nextDocument()
        var fileName = if(queryJSON.containsKey("output")) query.getString("output") else "output.xlsx"
        val sxssfWorkbook = SXSSFWorkbook(100)
        val sxssfSheet = sxssfWorkbook.createSheet()
        var rowNum = 0
        val row = sxssfSheet.createRow(rowNum++)
        var isAddHeader = false
        val addcoloumns= { coloumns: ArrayList<String>, json:JSONObject ->
            json.keys.forEach {
                coloumns.add(json.get(it).toString())
            }
        }
        while (obj != null){
            if(!isAddHeader){
                val headers = ArrayList<String>()
                val addHeader = { json:JSONObject ->
                    json.keys.forEach {
                        headers.add(it)
                    }
                }
                addHeader(obj.getJSONObject("_id"))
//                addHeader(obj.getJSONObject("value"))
                headers.add("value")
                for (i in headers.indices) {
                    val cell = row.createCell(i)
                    cell.setCellValue(headers[i])
                }
                isAddHeader = true
            }

            val row = sxssfSheet.createRow(rowNum++)
            val coloumns = ArrayList<String>()
            addcoloumns(coloumns,obj.getJSONObject("_id"))
//            addcoloumns(coloumns,obj.getJSONObject("value"))
            coloumns.add(obj.getJSONObject("value").toJSONString())
            coloumns.forEachIndexed { index, s ->
                row.createCell(index).setCellValue(coloumns.get(index))
            }
            obj = mapReduce.nextDocument()
        }

        try {
            sxssfWorkbook.write(FileOutputStream(File( fileName)))
            sxssfWorkbook.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }else{
        println("文件不全")
    }
}