package com.alk.battleCommandLimiter.controllers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import com.alk.battleCommandLimiter.BattleCommandLimiter;
import com.alk.battleCommandLimiter.Defaults;
import com.alk.battleCommandLimiter.Log;
import com.alk.battleCommandLimiter.objects.LimitedCommand;
import com.alk.battleCommandLimiter.objects.Rule;
import com.alk.battleCommandLimiter.serializers.BCLMySQLSerializer;
import com.alk.battleCommandLimiter.util.InventoryUtil;
import com.alk.battleCommandLimiter.util.RuleParser;
/**
 * 
 * @author alkarin
 *
 */
public class ConfigController {
    public static FileConfiguration config;
	
    public static boolean getBoolean(String node) {return config.getBoolean(node, false);}
    public static  String getString(String node) {return config.getString(node,null);}
    public static  String getString(String node,String def) {return config.getString(node,def);}
    public static int getInt(String node,int i) {return config.getInt(node, i);}
    public static double getDouble(String node, double d) {return config.getDouble(node, d);}

    public static boolean setConfig(File f){
    	config = new YamlConfiguration();
    	try {config.load(f);} catch (Exception e) {e.printStackTrace();}
    	boolean success = false;
    	try {success = loadAll();}
    	catch(Exception e){
    		return false;
    	}
    	return success;
    }

    private static boolean loadAll(){
    	CommandController cc = BattleCommandLimiter.getCommandController();
    	String p = "mySQLOptions.";
    	BCLMySQLSerializer.DB = ConfigController.getString(p +"db");
    	BCLMySQLSerializer.URL = ConfigController.getString(p +"url");
    	BCLMySQLSerializer.PORT = ConfigController.getString(p +"port");
    	BCLMySQLSerializer.USERNAME = ConfigController.getString(p +"username");
    	BCLMySQLSerializer.PASSWORD = ConfigController.getString(p +"password");
    	ConfigurationSection cs = config.getConfigurationSection("limitedCommands");

    	String payprefix = config.getString("limitedCommands.payPrefix");
    	if (payprefix == null){
    		Log.err("need to specify a payprefix!!!");
    		return false;
    	}
    	Defaults.PAY_PREFIX = payprefix;
    	Defaults.TIME_BETWEEN_COMMANDS = config.getLong("timeBetweenCommands");
    	Set<String> lcs = cs.getKeys(false);
    	if (lcs != null){
        	for (String cmd : lcs){
        		if (cmd.contains("payPrefix"))
        			continue;
        		LimitedCommand lc = parseCommand(cs.getConfigurationSection(cmd), cmd);
        		Log.warn("[BattleCommandLimiter] limiting="+lc);
        		if (lc == null){
        			Log.err("Couldnt parse command " + cmd);
        			continue;
        		}
            	cc.addCommand(lc);
        	}    		
    	}
    	return true;
    }
    
	private static LimitedCommand parseCommand(ConfigurationSection cs, String cmd) {
		LimitedCommand lc = new LimitedCommand();
		lc.setCommand(cmd);
		Set<String> strrules = cs.getKeys(false);
		for (String rulekey : strrules){
			if (rulekey.contains("permission")){
				lc.setPermissionNode(cs.getString(rulekey));
				continue;
			}
			if (rulekey.contains("pay")){
				int priority = RuleParser.parsePriority(cs.getString(rulekey));
				lc.setWarnFrom(priority);
				continue;
			}
			if (rulekey.contains("dontLimit")){
				List<String> dontLimit = cs.getStringList(rulekey);
				lc.setDontLimit(dontLimit);
			}
			if (!rulekey.contains("rule") || rulekey.contains("item"))
				continue;
//			System.out.println("rulekey=" + rulekey);
			Rule rule = RuleParser.parseRule(rulekey,cs.getString(rulekey));
			lc.addRule(rule);
		}
		for (String rulekey : strrules){
			if (!rulekey.contains("item"))
				continue;
			int priority = RuleParser.parsePriority(rulekey);
//			System.out.println(cs.getCurrentPath());
//			System.out.println(cs.getConfigurationSection(rulekey).getCurrentPath());

			ArrayList<ItemStack> items = getItemList(cs, rulekey);
//			System.out.println("rulekey=" + rulekey + " priority=" + priority + "  items=" + items);
//			Rule rule = RuleParser.parseRule(rulekey,cs.getString(rulekey));
			lc.getRule(priority).addItems(items);
		}
		
//		System.out.println("LimitedCommand lc = " + lc);
		return lc;
	}

	private static ArrayList<ItemStack> getItemList(ConfigurationSection cs, String nodeString) {
		ArrayList<ItemStack> items = new ArrayList<ItemStack>();
		try {
			String str;
			for (Object o : cs.getList(nodeString)){
				str = o.toString();
				ItemStack is = parseItem(str);
				if (is != null)
					items.add(is);
			}
		} catch (Exception e){
			Log.warn(nodeString + " could not be parsed in config.yml");
		}
		return items;
	}

	public static ItemStack parseItem(String str) throws Exception{
		str = str.replaceAll("[}{]", "");
		if (Defaults.DEBUG) System.out.println("item=" + str);
		ItemStack is =null;
		try{
			String split[] = str.split("=");

			is = InventoryUtil.getItemStack(split[0]);
			is.setAmount(Integer.valueOf(split[1]));
		} catch(Exception e){
			Log.err("Couldnt parse item=" + str);
			throw new Exception("parse item was bad");
		}
		return is;
	}
    
}
