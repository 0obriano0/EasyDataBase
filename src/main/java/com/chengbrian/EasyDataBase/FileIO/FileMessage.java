package com.chengbrian.EasyDataBase.FileIO;

import com.chengbrian.EasyDataBase.main;
import com.chengbrian.EasyDataBase.FileIO.FileIO;

public class FileMessage extends FileIO{
	public FileMessage() {
		super("message", main.plugin.getConfig().getString("lang") + ".yml");
	}
	
	@Override
	public boolean reloadcmd() {
		return true;
	}
}
