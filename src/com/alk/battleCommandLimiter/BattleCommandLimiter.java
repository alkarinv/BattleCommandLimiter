package com.alk.battleCommandLimiter;

import java.io.File;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import com.alk.battleCommandLimiter.controllers.CommandController;
import com.alk.battleCommandLimiter.controllers.ConfigController;
import com.alk.battleCommandLimiter.controllers.MoneyController;
import com.alk.battleCommandLimiter.executors.BCLCommandExecutor;
import com.alk.battleCommandLimiter.listeners.BCLPlayerListener;
import com.alk.battleCommandLimiter.serializers.BCLMySQLSerializer;

public class BattleCommandLimiter extends JavaPlugin {

	static private String pluginname; 
	static private String version;
	static private BattleCommandLimiter plugin;
	
	private BCLCommandExecutor commandExecutor = new BCLCommandExecutor();
	private static BCLMySQLSerializer sql = new BCLMySQLSerializer();
	private static CommandController commandController = new CommandController(sql);
	private BCLPlayerListener playerListener = null;

	@Override
	public void onEnable() {
		plugin = this;	
		PluginDescriptionFile pdfFile = plugin.getDescription();
		pluginname = pdfFile.getName();
		version = pdfFile.getVersion();
		if (!loadAllConfig()){
			Log.err(getVersion()  + " could not enable!");
			return;
		}
		playerListener = new BCLPlayerListener(commandController);

		getServer().getPluginManager().registerEvents(playerListener, this);
		getCommand("commandlimiter").setExecutor(commandExecutor);
		MoneyController.setup();
		Log.info(getVersion() + " enabled!");
	}

	@Override
	public void onDisable() {
		
	}

	public boolean loadAllConfig(){
		File dir = this.getDataFolder();
        if (!dir.exists()){
        	dir.mkdirs();}
        boolean success = ConfigController.setConfig(Util.load(getClass().getResourceAsStream("/default_files/config.yml"),dir.getPath() +"/config.yml"));
        if (!success)
        	return false;
		sql.init();
		return true;
	}
	
	public static BattleCommandLimiter getSelf() {return plugin;}
	public static String getVersion() {return "[" + pluginname + " v" + version +"]";}

	public static CommandController getCommandController() {
		return commandController;
	}

}
