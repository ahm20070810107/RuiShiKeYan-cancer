package com.RuiShiKeYan.dao;

import com.alibaba.fastjson.JSONObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientOptions.Builder;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.io.IOException;
import java.util.Set;

public class MDBConnection {
	
	private static class ConnectInfo {
		String ip;
		String dbName;
		String tableName;
		String username;
		String password;
		public ConnectInfo(String ip, String dbName, String tableName, String username, String password) {
			this.ip = ip;
			this.dbName = dbName;
			this.tableName = tableName;
			this.username = username;
			this.password = password;
		}
	}
	
	private ConnectInfo connectInfo;
	
	private MongodbClient dbClient = null;
	
	private MongoCollection<Document> dbCollection = null;
	
	private MongoDatabase mongoDatabase = null;

	public MDBConnection() {
	}
	
	public MongoCollection<Document> getDbCollection() {
		return dbCollection;
	}
	
	public MongoDatabase getMongoDatabase() {
		return mongoDatabase;
	}
	
	public void connectToDBServer(String ip, String dbName, String tableName, String username, String password) throws IOException {
		dbClient = new MongodbClient( dbName, username, tableName, password, ip);
		dbCollection = dbClient.getCollection(tableName);
		mongoDatabase = dbClient.getDatabase(dbName);
		connectInfo = new ConnectInfo(ip, dbName, tableName, username, password);
	}
	
	public void reconnect(){
		try {
			try {
				dbClient.closeClient();
			} catch (Exception e) {
				e.printStackTrace();
			}
			dbClient = new MongodbClient( connectInfo.dbName, connectInfo.username, connectInfo.tableName, connectInfo.password, connectInfo.ip);
			dbCollection = dbClient.getCollection(connectInfo.tableName);
			mongoDatabase = dbClient.getDatabase(connectInfo.dbName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void close(){
		try {
			dbClient.closeClient();
		} catch (Exception e) {
		}
	}
	
	public MongoDatabase getMongoDBClient() {
		try {
			return dbClient.getMongoClient();
		} catch (Exception e) {
		}
		return null;
	}
	

	/**
	 * 插入到结构化数据库服务器
	 * @param object
	 */
	public void putDocument(JSONObject object){
		Document mDocument = new Document();
		Set<String> keys = object.keySet();
		for (String key : keys) {
			mDocument.put(key, object.get(key));
		}
		final int retryCnt = 2;
		for (int i = 0; i < retryCnt; i++) {
			boolean needRetry = false;
			try {
				dbCollection.insertOne(mDocument);
			} catch (Exception e) {
				e.printStackTrace();
				try {
					dbClient.closeClient();
				} catch (Exception e2) {
				}
				try {
					connectToDBServer(connectInfo.ip, connectInfo.dbName, connectInfo.tableName, connectInfo.username, connectInfo.password);
					needRetry = true;
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
			if (!needRetry) {
				break;
			}
		}
	}
	
}

class MongodbClient {

	private MongoClient mongoClient = null;
	private MongoCollection<Document> collection = null;
	private String dbName;

	public MongodbClient(String dbName, String userName, String tableName, String passWord, String serverIp) {
		this.dbName = dbName;
		Builder builder = MongoClientOptions.builder().socketTimeout(6*60*60*1000);//部分mapreduce非常耗时，这里最长设置6小时
		builder.socketKeepAlive(true);
		builder.heartbeatSocketTimeout(30000);
		MongoClientURI mongoClientURI = new MongoClientURI(
				"mongodb://" + userName + ":" + passWord + "@" + serverIp + "/" + dbName, builder);
		mongoClient = new MongoClient(mongoClientURI);
	}

	public MongoCollection<Document> getCollection(String tableName) throws IOException {
		MongoDatabase database = mongoClient.getDatabase(dbName);
		collection = database.getCollection(tableName);
		return collection;
	}
	
	public MongoDatabase getDatabase(String tableName) throws IOException {
		MongoDatabase database = mongoClient.getDatabase(dbName);
		return database;
	}

	/**
	 * 关闭mongoclient
	 */
	public void closeClient() {
		mongoClient.close();
	}

	public MongoDatabase getMongoClient() {
		return  mongoClient.getDatabase(dbName);
	}
	
}
