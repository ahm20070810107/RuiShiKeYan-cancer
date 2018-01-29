package com.RuiShiKeYan.EntityStatistic

import com.RuiShiKeYan.Common.Method.LocalHostInfo
import com.RuiShiKeYan.Common.Method.SaveExcelTool
import com.alibaba.fastjson.JSONObject
import com.yiyihealth.data.DaX.ds.mongo.MDBConnection
import com.yiyihealth.data.DaX.reader.DSExcelReader2
import org.apache.poi.ss.usermodel.Row
import org.bson.Document
import test.java.task_SLE_LangChuang.BaseInfo_Title_ListValue_DBCondition
import java.util.*


var ADR = "{"+BaseInfo_Title_ListValue_DBCondition.ADR13 +"}"
var ADO = "{"+BaseInfo_Title_ListValue_DBCondition.ADO13 +"}"
var ASY ="{"+BaseInfo_Title_ListValue_DBCondition.ZZTZ13SLE +"}"
var ATZ = BaseInfo_Title_ListValue_DBCondition.strTZConditon1
var ADI = "{"+BaseInfo_Title_ListValue_DBCondition.ZD13SLE +"}"
var ALA = "{"+BaseInfo_Title_ListValue_DBCondition.HY13SLE +"}"
var BH13SLE ="{"+BaseInfo_Title_ListValue_DBCondition.BH13SLE +"}"
var entityList:Array<String> = arrayOf("症状","体征","诊断","化验","用药")

inline fun getMongoConnection(debug: Boolean):MDBConnection{
    var mongoConnection =  MDBConnection()
    if(debug){
        mongoConnection.connectToDBServer("114.55.149.219:3718", "HDP-live", "ASY", "hm", "eql-LmnZ8xc9pxbg")
    }else{
        mongoConnection.connectToDBServer("dds-bp1baff8ad4002a42.mongodb.rds.aliyuncs.com:3717", "HDP-live", "ASY", "hm", "eql-LmnZ8xc9pxbg")
    }
    return  mongoConnection
}

inline fun getRemovePIDS(): ArrayList<String> {
    var config = JSONObject()
    config.put("filename",LocalHostInfo.getPath()+"交付/移除组PID列表.xlsx")
    var reader = DSExcelReader2(config)
    var row:JSONObject? = reader.nextDocument()
    val removePIDS = ArrayList<String>()
    while (row != null){
        removePIDS.add(row!!.getString("PID"))
        row = reader.nextDocument()
    }
    return removePIDS
}

internal inline fun getInitQueryWidthType(type:String,removePIDS:ArrayList<String>):Document{
    var query = ""
    when(type){
        "用药"-> query = ADR
        "症状"-> query = ASY
        "体征"-> query = ASY
        "诊断"-> query = ADI
        "化验"-> query = ALA

        else  -> throw  RuntimeException("未定义的类型")
    }
    return  Document.parse(query).append("PID", Document("\$nin", removePIDS))
}

fun main(args: Array<String>) {
    var excelTool = SaveExcelTool()
    var sheet = excelTool.getSheet("")
    excelTool.fillExcelTitle(" ,症状,体征,诊断,化验,用药,PID数")
    var row = sheet.createRow(1)

    val removePIDS = getRemovePIDS()
    var dbCollection = getMongoConnection(true)

    row.createCell(0).setCellValue("实体数量")
    row.createCell(1).setCellValue(printCount(dbCollection,"ASY",ASY,removePIDS,"症状1",Document("\$ne","")))
    row.createCell(2).setCellValue(printCount(dbCollection,"ASY",ATZ,removePIDS,"",Document("\$ne","")))
    row.createCell(3).setCellValue(printCount(dbCollection,"ADI",ADI,removePIDS,"标准诊断名_原",Document("\$ne","")))

    val huayanOr = ArrayList<Document>()
    huayanOr.add(Document("化验名称",Document("\$ne","")))
    huayanOr.add(Document("化验组_原",Document("\$ne","")))
    row.createCell(4).setCellValue(printCount(dbCollection,"ALA",ALA,removePIDS,"\$or",huayanOr))
    row.createCell(5).setCellValue(printCount(dbCollection,"ADR",ADR,removePIDS,"通用名_原",Document("\$ne","")))
    val initQuery = getInitQuery(ADO,removePIDS)
    var pidCount =printPIDCount(dbCollection,"ADO",initQuery)
    row.createCell(6).setCellValue(pidCount)

    row=sheet.createRow(2)
    row.createCell(6).setCellValue(pidCount)
    fillYangXingValue(row)

    excelTool.saveExcel("交付/入组实体数统计.xlsx")
}

internal inline fun fillYangXingValue(row :Row)
{
    var cell=0
    row.createCell(cell++).setCellValue("阳性实体数量")
    var jsObj=getExcelNum()
    row.createCell(cell++).setCellValue(getJSonValue(jsObj,"症状"))
    row.createCell(cell++).setCellValue(getJSonValue(jsObj,"体征"))
    row.createCell(cell++).setCellValue(getJSonValue(jsObj,"诊断"))
    row.createCell(cell++).setCellValue(getJSonValue(jsObj,"化验"))
    row.createCell(cell++).setCellValue(getJSonValue(jsObj,"用药"))

    fillEntityResult(jsObj)
}

internal inline fun fillEntityResult(jsObj:JSONObject)
{
    var excelTool = SaveExcelTool()
    var sheet = excelTool.getSheet("")
    excelTool.fillExcelTitle("实体类别,实体数量,表型化数量,未表型化数量,表型化占比")

    var rowNum=1
    var eCountT=0
    var tCountT=0
    for(str in entityList)
    {
        var row = sheet.createRow(rowNum++)
        var cell=0

        var eCount=jsObj.getInteger(str+"E")
        var tCount=jsObj.getInteger(str+"T")
        eCountT += eCount
        tCountT += tCount
        var percent = tCount.toDouble()/eCount.toDouble()

        row.createCell(cell++).setCellValue(str)
        row.createCell(cell++).setCellValue(eCount.toString())
        row.createCell(cell++).setCellValue(tCount.toString())
        row.createCell(cell++).setCellValue((eCount-tCount).toString())
        row.createCell(cell++).setCellValue(percent.toString())
    }
    var row = sheet.createRow(rowNum++)
    var cell=0
    var percent = tCountT.toDouble()/eCountT.toDouble()
    row.createCell(cell++).setCellValue("整体")
    row.createCell(cell++).setCellValue(eCountT.toString())
    row.createCell(cell++).setCellValue(tCountT.toString())
    row.createCell(cell++).setCellValue((eCountT-tCountT).toString())
    row.createCell(cell++).setCellValue(percent.toString())

    excelTool.saveExcel("交付/实体表型数统计结果.xlsx")
}

internal inline fun getExcelNum() :JSONObject
{
    var jsonObj= JSONObject()
    var fileName=LocalHostInfo.getPath()+"交付/实体表型数统计.xlsx"
    val config = JSONObject()
    config.put("filename", fileName)
    config.put("source_type", "excel")

    val excelReader = DSExcelReader2(config)
    var document = excelReader.nextDocument()
    while (document != null) {

        var tempFenZu = getJSonValue(document, "实体类别")
        var countY=jsonObj.getInteger(tempFenZu)?:0
        var yCount=document.getInteger("阳性数")
        jsonObj.put(tempFenZu,countY+yCount)

        countY=jsonObj.getInteger(tempFenZu +"E")?:0
        yCount=document.getInteger("数量")
        jsonObj.put(tempFenZu+"E",countY+yCount)

        countY=jsonObj.getInteger(tempFenZu +"T")?:0
        var sysvalue=getJSonValue(document,"拟观察系统累及分组")
        if(sysvalue.equals(""))
            yCount=0
        jsonObj.put(tempFenZu+"T",countY+yCount)

        document = excelReader.nextDocument()
    }
    return jsonObj
}
internal inline fun getInitQuery(query:String,removePIDS:ArrayList<String>):Document{
    return  Document.parse(query).append("PID", Document("\$nin", removePIDS))
}





internal inline fun getCount(dbCollection: MDBConnection,collectionName:String,query:String,removePIDS:ArrayList<String>,key:String,value:Any):Long {
    val query = getInitQuery(query,removePIDS)
    query.append(key, value)
    return  dbCollection.mongoDatabase.getCollection(collectionName).count(query)
}

internal inline fun printCount(dbCollection: MDBConnection,collectionName:String,query:String,removePIDS:ArrayList<String>,key:String,value:Any):String {
    val query = getInitQuery(query,removePIDS)
    if(key !="") {
        query.append(key, value)
}
    val builder = StringBuilder()
    val total = dbCollection.mongoDatabase.getCollection(collectionName).count(query).toInt()
//    val projectProcessId  = query.get("projectProcessId") as Document?
//    var sum = 0L
//    projectProcessId?.let {
//        val inA = it.get("\$in") as ArrayList<Long>?
//        inA?.forEach {
//            query.put("projectProcessId",it)
//            val sub = dbCollection.mongoDatabase.getCollection(collectionName).count(query)
//            sum += sub
//            builder.append("$it:${sub}  ")
//        }
//    }
//    if (sum != total) throw  RuntimeException("不等")
    return  total.toString()
}


internal inline fun printPIDCount(dbCollection: MDBConnection,collectionName:String,query:Document):String {
    val builder = StringBuilder()
    val total = dbCollection.mongoDatabase.getCollection("ADO").distinct("PID",query,String::class.java).count()
//    builder.append(total)
//    builder.append("  ")
//    val projectProcessId  = query.get("projectProcessId") as Document?
//    var sum = 0
//    projectProcessId?.let {
//        val inA = it.get("\$in") as ArrayList<Long>?
//        inA?.forEach {
//            query.put("projectProcessId",it)
//            val sub = dbCollection.mongoDatabase.getCollection("ADO").distinct("PID",query,String::class.java).count()
//            sum += sub
//            builder.append("$it:${sub}  ")
//        }
//    }
//    if (sum != total) throw  RuntimeException("不等")
    return   total.toString()
}

fun getJSonValue(jsonObject: JSONObject?, key: String?): String {
    if (jsonObject == null || key == null) return ""
    return if (jsonObject.getString(key) == null) "" else jsonObject.getString(key)
}