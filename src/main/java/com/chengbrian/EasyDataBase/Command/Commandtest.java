package com.chengbrian.EasyDataBase.Command;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class Commandtest extends mainCommandSystem{
	public Commandtest() {
		super(  "test",
				"/easydatabase test 取得指令說明",
				new ArrayList<String>(Arrays.asList("easydatabase.admin.test")));
	}
	
	@Override
	public void run(CommandSender sender, String commandLabel, Command command, String[] args) throws Exception {
		
	}
	
}
