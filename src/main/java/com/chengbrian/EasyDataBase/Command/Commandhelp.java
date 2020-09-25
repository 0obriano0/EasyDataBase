package com.chengbrian.EasyDataBase.Command;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.chengbrian.EasyDataBase.main;
import com.chengbrian.EasyDataBase.DataBase.DataBase;

public class Commandhelp extends mainCommandSystem{

	public Commandhelp() {
		super(  "help",
				"/easydatabase help 取得指令說明",
				new ArrayList<String>(Arrays.asList("easydatabase.user.help")));
	}
	
	@Override
	public void run(CommandSender sender, String commandLabel, Command command, String[] args) throws Exception {
		sender.sendMessage(" ");
		sender.sendMessage("=============== EasyDataBase 資料庫系統 ===============");
		sender.sendMessage(" ");
		for(String command_value :DataBase.getCommands(main.plugin)) {
			ImainCommandSystem cmd = main.getCommandClass(command_value);
			if(cmd.hasPermission(sender))
				sender.sendMessage(cmd.getHelp());
		}
		sender.sendMessage(" ");
		sender.sendMessage("===========================================");
	}
	
	@Override
	public void run(Player player, String commandLabel, Command command, String[] args) throws Exception {
		run((CommandSender)player, commandLabel, command,args);
	}
}
