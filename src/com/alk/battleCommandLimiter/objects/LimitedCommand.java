package com.alk.battleCommandLimiter.objects;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import com.alk.battleCommandLimiter.Defaults;

public class LimitedCommand {
	String command;
	TreeMap<Integer, Rule> rules = new TreeMap<Integer, Rule>();
	Integer warnFrom = null;
	Set<String> dontLimit = null;
	boolean dontLimitNoArgs = false;
	String permissionNode = null;

	public String getPermissionNode() {return permissionNode;}
	public void setPermissionNode(String permissionNode) {this.permissionNode = permissionNode;}
	public void setCommand(String cmd) {this.command = cmd;}
	public String getCommand(){return this.command;}

	public String toString(){
		StringBuilder sb = new StringBuilder("[LimitedCommand ("+command +") ");
		sb.append("payFrom=" + warnFrom);
		for (Rule r: rules.values()){
			sb.append(r +",");
		}
		sb.append("]");
		return sb.toString();
	}

	public void addRule(Rule rule) {
		rules.put(rule.getPriority(), rule);
	}
	public Collection<Rule> getRules() {
		return rules.values();
	}
	public Rule getRule(int priority) {
		return rules.get(priority);
	}
	public void setWarnFrom(int priority) {
		warnFrom = priority;
	}
	public boolean warnRule(Rule r) {
		return warnFrom == null ? false : r.getPriority() >= warnFrom;
	}
//	public String getLimitMessage(Long time,boolean pay) {
//		StringBuilder sb = new StringBuilder("&eTo use this command you must");
//		for (Rule r: rules.values()){
//			sb.append(r.limitMessage(time) +",\n");
//		}
//	
//		return sb.toString();
//	}
	public String getWarnMessage(Long time,boolean pay) {
		StringBuilder sb = new StringBuilder("&eTo use this command you must ");
		boolean first = true;
		for (Rule r: rules.values()){
			if (!first) sb.append("&5 OR ");
			if (warnRule(r)) sb.append("&4pay");
			sb.append("&5 - &e" +r.limitMessage(time) +"");
			first = false;
		}
		if (warnFrom != null){
			sb.append("\n&6/"+Defaults.PAY_PREFIX+command+"&e to &4pay&e for this command.");
		}
		return sb.toString();
	}
	public void setDontLimit(List<String> dontLimit) {
		this.dontLimit = new HashSet<String>(dontLimit);
		if (this.dontLimit.contains("noArgs"))
			dontLimitNoArgs = true;
	}
	public boolean dontLimit(String string) {
//		System.out.println("dont limit contains = " + dontLimit.contains(string));
		return dontLimit == null ? false : dontLimit.contains(string);
	}
	public Set<String> getDontLimit() {
		return dontLimit;
	}
	public boolean dontLimit(String[] cmds) {
		if (dontLimitNoArgs && cmds.length==1)
			return true;
		return cmds.length > 1 && dontLimit(cmds[1]);
	}
	public String convert(String cmds[] , boolean pay) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (int i=0;i<cmds.length;i++){
			if (!first) sb.append(" ");
			if (i==0)
				sb.append(cmds[i].replaceAll("^"+Defaults.PAY_PREFIX, ""));
			else 
				sb.append(cmds[i]);
			first = false;
		}
		return sb.toString();
	}
}
