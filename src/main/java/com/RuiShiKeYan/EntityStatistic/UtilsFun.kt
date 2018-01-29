package com.RuiShiKeYan.EntityStatistic

import java.util.*

/**
 * add filter function on ArrayList like js
 */
inline fun <T,E> ArrayList<T>.filter(block:(v:T)->E):ArrayList<E>{
    var res = ArrayList<E>()
    this.forEach {
        res.add(block(it))
    }
    return res
}

/**
 * add map function on ArrayList like js
 */
inline fun <T,E> ArrayList<T>.map(block:(v:T)->E):ArrayList<E>{
    var res = ArrayList<E>()
    this.forEach {
        res.add(block(it))
    }
    return res
}

/**
 * add reduce function on ArrayList like js
 */
inline fun <T,E> ArrayList<T>.reduce(initValue:E?=null,block:(v:T,lastResult:E?)->E?):E?{
    var res = initValue
    this.forEach {
        res = block(it,res)
    }
    return res
}

fun main(args: Array<String>) {
    var d = ArrayList<String>()
    d.add("1")
    d.add("5")
    d.add("2")
    println(d.map { it.toInt() })
    var q = {v:String, lastResult:Any? ->  lastResult}
    println(d.reduce(0,{v, lastResult ->  v.toInt()+(lastResult?:0)}))
}