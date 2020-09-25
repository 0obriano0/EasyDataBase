package com.chengbrian.EasyDataBase.MySQL;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.chengbrian.EasyDataBase.DataBase.DataBase;

public class MySQL {
	// JDBC driver name and database URL
	protected final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
	protected transient String DB_URL;
	
	//  Database credentials
	protected transient String USER;
	protected transient String PASS;
	protected transient String db;
   
	protected Connection conn = null;
	
	protected Timer timerCheckConnect = new Timer();
	protected boolean isconnect = false;
	
	protected boolean showLog = true;
	
	/**
	 * 設定要不要顯示測試文字
	 * @param showlog
	 */
	public void setshowLog(boolean showlog) {
		this.showLog = showlog;
	}
	
	/**
	 * 分析DB_URL中的主連結
	 * @return
	 */
	protected String getMainDB_URL() {
		String reg = "jdbc:mysql://[^/].+/";

		//將規則封裝成物件
		Pattern p = Pattern.compile(reg);
		
		//讓正則物件與要作用的字串相關聯
		Matcher m = p.matcher(DB_URL);
		  
		//將規則作用到字串上, 並進行符合規則的子串查找
		m.find();
		//log("找到得連結是" + m.group());
		return m.group();
	}
	
	/**
	 * MySQL 基本資料設定
	 * @param USER 帳號
	 * @param PASS 密碼
	 * @param DB_URL 連結網址
	 * @param db 數據庫
	 */
	public MySQL(String USER,String PASS,String DB_URL,String db) {
		this.USER = USER;
		this.PASS = PASS;
		this.DB_URL = DB_URL;
		
		if(this.DB_URL.contains("autoReconnect")) {
			print("已經有加入了 autoReconnect");
		}else {
			print("自動加入autoReconnect");
			if(this.DB_URL.contains("?")) {
				String[] data_URL = this.DB_URL.split("\\?");
				this.DB_URL = data_URL[0] + "?autoReconnect=true&" + data_URL[1];
				print("DB_URL = " + this.DB_URL);
			}else {
				this.DB_URL = this.DB_URL + "?autoReconnect=true";
				print("DB_URL = " + this.DB_URL);
			}
		}
		
		this.db = db;
		
		if (this.open()) this.SelectDataBase();
	}
	
	/**
	 * MySQL 基本資料設定
	 * 含設定 字元編碼
	 * @param USER 帳號
	 * @param PASS 密碼
	 * @param DB_URL 連結網址
	 * @param db 數據庫
	 * @param useSSL 要不要使用SSL
	 * @param useUnicode 是否使用 Unicode
	 * @param characterEncoding 字元編碼
	 */
	public MySQL(String USER,String PASS,String DB_URL,String db,boolean useSSL,boolean useUnicode,String characterEncoding) {
		this.USER = USER;
		this.PASS = PASS;
		
		String Encodingsetting = "";
		if(useUnicode && !characterEncoding.equals("")) 
			Encodingsetting = "useUnicode=true&characterEncoding=" + characterEncoding;
		
		if(useSSL) this.DB_URL = DB_URL + "?useSSL=true" + (Encodingsetting.equals("") ? "" : "&" + Encodingsetting);
		else this.DB_URL = DB_URL + (Encodingsetting.equals("") ? "" : "?" + Encodingsetting);
		
		if(this.DB_URL.contains("autoReconnect")) {
			print("已經有加入了 autoReconnect");
		}else {
			print("自動加入autoReconnect");
			if(this.DB_URL.contains("?")) {
				String[] data_URL = this.DB_URL.split("?");
				this.DB_URL = data_URL[0] + "?autoReconnect=true" + data_URL[1];
				print("DB_URL = " + this.DB_URL);
			}else {
				this.DB_URL = this.DB_URL + "?autoReconnect=true";
				print("DB_URL = " + this.DB_URL);
			}
		}
		
		this.db = db;
		
		if (this.open()) this.SelectDataBase();
	}
	
	
	/**
	 * 檢查跟mysql的連線狀態
	 * 如果有斷線的話會先暫停所有的動作
	 * 直到重新連線為止
	 * @author brian
	 *
	 */
	private class checkMySQLConnect extends TimerTask {
	    public void run() {
	    	Statement stmt = null;
			if(conn==null) open();
		    
			try{
				stmt = conn.createStatement();
				String sql = "use " + db;
				stmt.executeUpdate(sql);
				if(!isconnect) print("DataBase is connect");
				isconnect = true;
			}catch(SQLException se){
				if(se.getClass().getSimpleName().equals("CommunicationsException") || se.getClass().getSimpleName().equals("MySQLNonTransientConnectionException")) {
					print("Reconnecting the DataBase...");
					isconnect = false;
				}else
					se.printStackTrace();
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				try{
					if(stmt!=null)
					stmt.close();
				}catch(SQLException se2){
					se2.printStackTrace();
				}
			}
	    }
	}
	
	private void noconnect(String command) {
		print("can not use this command '" + command + "', connect is fail");
	}
	
    /**
     * 開啟與MySQL連結
     * @return 有沒有成功
     */
	public boolean open() {
		try{
			//Register JDBC driver
			Class.forName(JDBC_DRIVER);

			//Open a connection
			print("Connecting to database...");
			this.conn = DriverManager.getConnection(DB_URL, USER, PASS);		
			timerCheckConnect.schedule(new checkMySQLConnect(), 1000, 1000);
			isconnect = true;
			//if(!this.SelectDataBase()) CreateDataBase(this.db);
		}catch(SQLException se){
			//Handle errors for JDBC	
			if(se.getClass().getSimpleName().equals("CommunicationsException")) {
				conn = null;
				print("Connecting fail");
			}else
				se.printStackTrace();
			return false;
		}catch(Exception e){
			//Handle errors for Class.forName
			print("exception error = " + e.getLocalizedMessage());
			e.printStackTrace();
			return false;
		}
		return true;
	}
   /**
    * 關閉與MySQL連結
    * @return 有沒有成功
    */
	public boolean close(){
		try{
			if(conn!=null)
				conn.close();
			timerCheckConnect.cancel();
			timerCheckConnect = new Timer();
			conn = null;
			return true;
		}catch(SQLException se){
			se.printStackTrace();
		}
		return false;
	}
   
	/**
     * 創建一個數據庫
     * @param DataBaseName 數據庫名稱
     */
	public void CreateDataBase(String DataBaseName) {
		if(!isconnect){
			noconnect("CreateDataBase");
			return;
		}
		
		Statement stmt = null;
		if(conn==null) open();
		
		try{
			//Execute a query
			print("Creating database...");
			stmt = conn.createStatement();
			String sql = "CREATE DATABASE " + DataBaseName;
			stmt.executeUpdate(sql);
			print("Database created successfully...");
			this.SelectDataBase();
		}catch(SQLException se){
			//Handle errors for JDBC
			se.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try{
				if(stmt!=null)
				stmt.close();
			}catch(SQLException se2){
				se2.printStackTrace();
			}
		}
	}
	
	/**
     * 刪除一個數據庫
     * @param DataBaseName 數據庫名稱
     */
	public void DelectDataBase(String DataBaseName) {
		if(!isconnect){
			noconnect("DelectDataBase");
			return;
		}
		
		Statement stmt = null;
		if(conn==null) open();
	   
		try{
			//Execute a query
			print("Deleting database...");
			stmt = conn.createStatement();
			String sql = "DROP DATABASE " + DataBaseName;
			stmt.executeUpdate(sql);
			print("Database deleted successfully...");
		}catch(SQLException se){
			//Handle errors for JDBC
			se.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try{
				if(stmt!=null)
				stmt.close();
			}catch(SQLException se2){
				se2.printStackTrace();
			}
		}
	}
	
	/**
	 * 選擇數據庫
	 * @return 是否成
	 */
	public boolean SelectDataBase() {
		if(!isconnect){
			noconnect("SelectDataBase");
			return false;
		}
		
		Statement stmt = null;
		if(conn==null) open();
	   
		
		boolean success = false;
		try{
			//Execute a query
			print("Select database[ " + db + " ]...");
			stmt = conn.createStatement();
			String sql = "use " + db;
			print("SelectDataBase = " + stmt.executeUpdate(sql));
			print("Database[ " + db + " ]Select successfully...");
			success = true;
		}catch(SQLSyntaxErrorException mse) {
			print("can not get DataBase [ " + db + " ]");
		}catch(SQLException se){
			//Handle errors for JDBC
			se.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try{
				if(stmt!=null)
				stmt.close();
			}catch(SQLException se2){
				se2.printStackTrace();
			}
		}
		
		return success;
	}
	
	/**
	 * 創建一個資料表
	 * @param PRIMARY_KEY 關鍵資料
	 * @param PRIMARY_key_Type 關鍵資料型態
	 * @param tableName 資料表名稱
	 * @param table 其他資料
	 */
	public void CreateTable(String PRIMARY_KEY,String PRIMARY_key_Type,String tableName,List<String> table) {
		if(!isconnect){
			noconnect("CreateTable");
			return;
		}
		
		Statement stmt = null;
		if(conn==null) open();
	   
		try{
			//Execute a query
			print("Creating table in given database...");
			stmt = conn.createStatement();
			
			String sql = "use " + db;
			stmt.executeUpdate(sql);
			
			sql = "CREATE TABLE " + tableName + " (" + PRIMARY_KEY + " " + PRIMARY_key_Type + " not NULL, ";
			for(String value : table) sql = sql + value + ", ";
			sql = sql + "PRIMARY KEY ( " + PRIMARY_KEY +" ))";
			
			stmt.executeUpdate(sql);
			print("Created table in given database successfully...");
		}catch(SQLException se){
			//Handle errors for JDBC
			se.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try{
				if(stmt!=null)
				stmt.close();
			}catch(SQLException se2){
				se2.printStackTrace();
			}
		}
	}
	
	/**
	 * 插入資料
	 * db 使用內部設定好的
	 * 如果要更改請使用 setdb("database")
	 * @param tableName 資料表名稱
	 * @param insertdata 要輸入資料
	 * @return 是否成功
	 */
	public boolean Insert(String tableName,Map<String,String> insertdata) {
		if(!isconnect){
			noconnect("Insert");
			return false;
		}
		
		Statement stmt = null;
		if(conn==null) open();
	   
		boolean success = false;
		try{
			stmt = conn.createStatement();
			print("Insert DATA");
			String sql = "use " + db;
			stmt.executeUpdate(sql);
			
			String FieldName = "(";
			String InsertValue = "(";
			for(Entry<String, String> data : insertdata.entrySet()) {
				FieldName += "`" + data.getKey() + "`" + ",";
				InsertValue += "'" + data.getValue() + "'" + ",";
			}
			FieldName = FieldName.substring(0, FieldName.length()-1) + ")";
			InsertValue = InsertValue.substring(0, InsertValue.length()-1) + ")";
			print("Command = INSERT INTO " + tableName + " " + FieldName + " VALUES " + InsertValue);
			stmt.executeUpdate("INSERT INTO " + tableName + " " + FieldName + " VALUES " + InsertValue);
			print("Insert DATA successfully...");
			success = true;
		}catch(SQLException se){
			//Handle errors for JDBC
			se.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try{
				if(stmt!=null)
				stmt.close();
			}catch(SQLException se2){
				se2.printStackTrace();
			}
		}
		return success;
	}
	
	/**
	 * 檢查是否有連接資料庫
	 * @return true or false
	 */
	public boolean isopen() {
		if(conn==null) return false;
		return true;
	}
	
	/**
	 * 傳送查詢相關指令 並轉換成 list map 模式
	 * db 使用內部設定好的
	 * 如果要更改請使用 setdb("database")
	 * @param command 指令
	 * @return 回傳查詢資料(null 代表取得失敗)
	 */
	public List<Map<String,String>> executeQuery_listMap(String command) {
		if(!isconnect){
			noconnect("Insert");
			return null;
		}
		
		print("run executeQuery_listMap"); 
		Statement stmt = null;
		ResultSet rs = null;
		if(conn==null) open();
		
		List<Map<String,String>> data_list = new ArrayList<Map<String,String>>();
		
		boolean success = false;
		try{
			//Execute a query
			print("run command: " + command);
			stmt = conn.createStatement();
			
			String sql = "use " + db;
			stmt.executeUpdate(sql);
			
			rs = stmt.executeQuery(command);
			
			ResultSetMetaData metadata = rs.getMetaData();
			int columnCount = metadata.getColumnCount();
			
			while(rs.next()){
				Map<String,String> data_map = new HashMap<String,String>();
				for(int loopnum1 = 1; loopnum1 <= columnCount;loopnum1++) {
					String ColumnName = metadata.getColumnName(loopnum1);
					data_map.put(ColumnName,rs.getString(ColumnName));
				}
				data_list.add(data_map);
		    }
			rs.close();
			print("command run successfully...");
			success = true;
		}catch(SQLException se){
			//Handle errors for JDBC
			se.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try{
				if(stmt!=null)
				stmt.close();
			}catch(SQLException se2){
				se2.printStackTrace();
			}
		}
		return success ? data_list : null;
	}
	
	/**
	 * 傳送查詢相關指令
	 * db 使用內部設定好的
	 * 如果要更改請使用 setdb("database")
	 * @param command 指令
	 * @return 回傳查詢資料(null 代表取得失敗)
	 */
	public ResultSet executeQuery(String command) {
		if(!isconnect){
			noconnect("executeQuery");
			return null;
		}
		
		Statement stmt = null;
		ResultSet rs = null;
		if(conn==null) open();
		
		boolean success = false;
		try{
			//Execute a query
			print("run command: " + command);
			stmt = conn.createStatement();
			
			String sql = "use " + db;
			stmt.executeUpdate(sql);
			
			rs = stmt.executeQuery(command);
			print("command run successfully...");
			success = true;
		}catch(SQLException se){
			//Handle errors for JDBC
			se.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try{
				if(stmt!=null)
				stmt.close();
			}catch(SQLException se2){
				se2.printStackTrace();
			}
		}
		return success ? rs : null;
	}
	
	/**
	 * 發送指令給 MySQL
	 * db 使用內部設定好的
	 * 如果要更改請使用 setdb("database")
	 * @param command 指令
	 * @return 回傳指令是否成功送出
	 */
	public boolean executeUpdate(String command) {
		if(!isconnect){
			noconnect("executeUpdate");
			return false;
		}
		
		Statement stmt = null;
		if(conn==null) open();
	   
		boolean success = false;
		try{
			//Execute a query
			print("run command: " + command);
			stmt = conn.createStatement();
			
			String sql = "use " + db;
			stmt.executeUpdate(sql);
			
			stmt.executeUpdate(command);
			print("command run successfully...");
			success = true;
		}catch(SQLException se){
			//Handle errors for JDBC
			se.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try{
				if(stmt!=null)
				stmt.close();
			}catch(SQLException se2){
				se2.printStackTrace();
			}
		}
		return success;
	}
	
	/**
	 * print顯示控制器
	 * @param message 訊息
	 */
	protected void print(String message) {
		if(showLog) DataBase.Print(message);
	}
	
	/**
	 * 更改目前的資料表
	 * @param db 資料表
	 * @return 檢查有沒有更改成功
	 */
	public boolean setdb(String db) {
		if(SelectDataBase()) {
			this.db = db;
			return true;
		}
		return false;
	}
	
	/**
	 * 查詢目前所在的資料表
	 * @return 目前所在的資料表
	 */
	public String getdb() {
		return this.db;
	}
	
	/**
	 * 以 Timestamp 資料型態取得 當前時間
	 * @return 當前時間
	 */
	public static java.sql.Timestamp getCurrentTimeStamp() {
		java.util.Date today = new java.util.Date();
		return new java.sql.Timestamp(today.getTime());
	}
}
