package com.chengbrian.EasyDataBase.Command;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.chengbrian.EasyDataBase.main;

public class Commandreload extends mainCommandSystem{
	public Commandreload() {
		super(  "reload",
				"/easydatabase reload 重新讀取資料",
				new ArrayList<String>(Arrays.asList("easydatabase.admin.reload")));
	}
	
	@Override
	public void run(CommandSender sender, String commandLabel, Command command, String[] args) throws Exception {
		sender.sendMessage("重新讀取資料...");
		main.reload();
		sender.sendMessage("資料讀取完成");
	}
	
	@Override
	public void run(Player player, String commandLabel, Command command, String[] args) throws Exception {
		run((CommandSender)player, commandLabel, command,args);
	}
}
