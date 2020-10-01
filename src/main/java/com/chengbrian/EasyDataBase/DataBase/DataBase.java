package com.chengbrian.EasyDataBase.DataBase;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.bukkit.plugin.Plugin;

import com.chengbrian.EasyDataBase.AnsiColor;
import com.chengbrian.EasyDataBase.main;
import com.chengbrian.EasyDataBase.FileIO.FileMessage;
import com.chengbrian.EasyDataBase.Timer.checkMySQLConnect;


/**
 * 基本資料暫存區
 * @author brian
 *
 */
public class DataBase {
	
	/**
	 * 插件目錄 插件附屬檔案的存放路徑
	 */
	public static String pluginMainDir = "./plugins/EasyDataBase/";
	
	/**
	 * 此插件名稱
	 */
	public static String pluginName = "EasyDataBase";
	
	/**
	 * 指令列表
	 */
	private static List<String> Commands = null;
	
	/**
	 * message 設定
	 */
	public static FileMessage fileMessage = new FileMessage();
	
	/**
	 * 顯示訊息 在cmd 裡顯示 "[EasyDataBase] " + msg
	 * @param msg 要顯示的文字
	 */
	public static void Print(String msg){
		main.plugin.getLogger().info(msg + AnsiColor.RESET);
		//System.out.print("[MobDrop2] " + msg);
	}
	
	/**
	 * 使用一個多執行緒來跑檢查MySQL連線狀態
	 */
	public static checkMySQLConnect CMySQLC = new checkMySQLConnect();
	/**
	 * 抓取指令列表(/EasyDataBase 列表資料)
	 * @param plugin 系統資料
	 * @return 列表資料
	 */
	public static List<String> getCommands(Plugin plugin){
		if(Commands == null) {
			Commands = new ArrayList<String>();
			URL jarURL = plugin.getClass().getResource("/com/chengbrian/" + pluginName + "/Command");
	    	URI uri;
			try {
				FileSystem fileSystem = null;
				uri = jarURL.toURI();
				Path myPath;
		        if (uri.getScheme().equals("jar")) {
		            fileSystem = FileSystems.newFileSystem(uri, Collections.<String, Object>emptyMap());
		            myPath = fileSystem.getPath("/com/chengbrian/"+ pluginName +"/Command");
		            
		        } else {
		            myPath = Paths.get(uri);
		        }
		        for (Iterator<Path> it = Files.walk(myPath, 1).iterator(); it.hasNext();){
		        	String[] path = it.next().toString().split("/");
		        	
		        	String file = path[path.length - 1];
		        	if(file.matches("(.*)class$")) {
		        		file = file.split("\\.")[0];
		        		if(file.matches("^Command.*")) {
			        		String filename = file.split("Command")[1];
			        		Commands.add(filename);
			        	}
		        	}
		            //System.out.println(it.next());
		        	Collections.sort(Commands);
		        }
		        fileSystem.close();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return Commands;
        
    }
}
