package com.chengbrian.EasyDataBase.Timer;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import com.chengbrian.EasyDataBase.DataBase.DataBase;
import com.chengbrian.EasyDataBase.MySQL.MySQL;

public class checkMySQLConnect {
	private Map<Integer,MySQL> connectList = new HashMap<Integer,MySQL>();
	
	private Timer timerCheckConnect = null;
	
	/**
	 * 開啟timer
	 * 來處理MySQL有沒有斷線
	 * @return 有沒有啟動成功
	 */
	public boolean openTimer() {
		if(!isopen()) {
			timerCheckConnect = new Timer();
			timerCheckConnect.schedule(new check(), 1000, 1000);
			DataBase.Print(DataBase.fileMessage.getString("SQL.Timer_open"));
			return true;
		}else {
			return false;
		}
	}
	
	/**
	 * 檢查timer有沒有開
	 * @return timer有沒有開
	 */
	public boolean isopen() {
		if(timerCheckConnect == null)
			return false;
		else
			return true;
	}
	
	/**
	 * 關閉timer
	 * @return 有沒有關成功
	 */
	public boolean closeTimer() {
		if(isopen()) {
			timerCheckConnect.cancel();
			timerCheckConnect = null;
			DataBase.Print(DataBase.fileMessage.getString("SQL.Timer_close"));
			return true;
		}else {
			return false;
		}
	}
	
	/**
	 * 增加要跑得MySQL運算
	 * @param data
	 * @return 回傳取得的key
	 */
	public int add(MySQL data) {
		if(connectList.size() == 0) {
			connectList.put(1, data);
			openTimer();
			return 1;
		}else {
			int index = connectList.size();
			while(connectList.containsKey(index)) {
				index++;
			}
			connectList.put(index, data);
			return index;
		}
	}
	
	/**
	 * 刪除檢查因素
	 * @param key
	 * @return 回傳刪除狀況
	 */
	public boolean remove(int key) {
		if(connectList.remove(key) != null) {
			if(connectList.size() == 0) closeTimer();
			return true;
		}else {
			return false;
		}
	}
	
	private class check extends TimerTask {
	    public void run() {
	    	//DataBase.Print("TimerTask 執行");
	    	for (Entry<Integer, MySQL> entry: connectList.entrySet()) {
	    		//DataBase.Print("TimerTask key = " + entry.getKey() + " dataname = " + entry.getValue().getdb());
	    		checkMySQLConnect(entry.getValue());
	    	}
	    }
	    
	    public void checkMySQLConnect(MySQL data) {
	    	//DataBase.Print("偵測~~~~");
	    	Statement stmt = null;
			if(data.getconn()==null) data.open();
		    
			try{
				stmt = data.getconn().createStatement();
				String sql = "use " + data.getdb();
				stmt.executeUpdate(sql);
				if(!data.isconnect) DataBase.Print(DataBase.fileMessage.getString("SQL.DataBase_is_connect"));
				data.isconnect = true;
			}catch(SQLException se){
				if(se.getClass().getSimpleName().equals("CommunicationsException") || se.getClass().getSimpleName().equals("MySQLNonTransientConnectionException")) {
					DataBase.Print(DataBase.fileMessage.getString("SQL.DataBase_Reconnecting"));
					data.isconnect = false;
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
}
