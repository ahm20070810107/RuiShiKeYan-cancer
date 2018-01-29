package com.RuiShiKeYan.Common.Interface;

import com.mongodb.client.MongoDatabase;

/**
 * Created with IntelliJ IDEA
 * User:huangming
 * Date:2017/12/12
 * Time:上午11:14
 */
public interface IruiShiKeYan {

    void run(MongoDatabase mdb,Object ... args);
}
