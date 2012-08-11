package com.alk.battleCommandLimiter.controllers;

import java.util.Collection;
import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.alk.battleCommandLimiter.Defaults;
import com.alk.battleCommandLimiter.objects.LimitedCommand;
import com.alk.battleCommandLimiter.objects.Rule;
import com.alk.battleCommandLimiter.objects.ShouldLimitObject;
import com.alk.battleCommandLimiter.objects.ShouldLimitObject.ShouldLimit;
import com.alk.battleCommandLimiter.serializers.BCLMySQLSerializer;
import com.alk.battleCommandLimiter.util.InventoryUtil;

public class CommandController {
	BCLMySQLSerializer sql;

	public HashMap<String, LimitedCommand> limitedCommands = new HashMap<String, LimitedCommand>();

	public CommandController(BCLMySQLSerializer sql){
		this.sql = sql;
	}
//	public ArrayList<HashMap<String,LimitedCommand>> limitedCommands = new ArrayList<HashMap<String,LimitedCommand>>(); 
//	public HashMap<String, ArrayList<LimitedCommand>> commands = new HashMap<String, ArrayList<LimitedCommand>>(); 

//	public boolean hasCommand(BCLCommand omsg) {
//		final int size = Math.min(limitedCommands.size(), omsg.size());
//		for (int i=0;i<size;i++){
//			if (!limitedCommands.get(i).containsKey(omsg.get(i)))
//				return false;
//		}
//		return true;
//	}
//
//	public boolean shouldLimit(Player p, BCLCommand omsg) {
//		final int size = Math.min(limitedCommands.size(), omsg.size());
//		for (int i=0;i<size;i++){
//			
//		}
//		return false;
//	}

	public static String[] convert(String omsg){
	//	omsg = omsg.replaceAll("/", "");
		final int index = omsg.indexOf(' ');
//		String argsstr = null;
		String ret[] = null;
//		String msg = null;
		try{
			if (index != -1){
				ret = omsg.split(" ");
				if (ret != null){
					ret[0] = ret[0].substring(1,ret[0].length());
//					for (int i = 0;i<ret.length;i++){
//						ret[i] = ret[i].replaceAll(" ", "");
//						System.out.println(" args[" + i + "] = " + args[i]);
//					}
				}
				return ret;
			} else {
				ret = new String[1];
				ret[0] = omsg.substring(1,omsg.length());
				return ret;
//				msg = omsg;
			}
		} catch(Exception e){
			return null;
		}
//		
//		if (argsstr != null){
//			args = argsstr.split(" ");
//		}
	}

	public boolean hasCommand(String cmd) {
//		System.out.println("hasCommand cmd="+cmd +"  contains="+limitedCommands.containsKey(cmd));
		return limitedCommands.containsKey(cmd) || limitedCommands.containsKey(cmd.replaceAll("^"+Defaults.PAY_PREFIX, ""));
	}

	public ShouldLimitObject shouldLimit(Player p, String[] cmds, boolean pay) {
		if (cmds==null)
			return null;
		if (pay){
			cmds[0] = cmds[0].replaceAll("^"+Defaults.PAY_PREFIX, "");}
		if(!hasCommand(cmds[0]))
			return null;
		LimitedCommand lc = limitedCommands.get(cmds[0]);
//		System.out.println("lc = " + lc +"    dontlimit=" + lc.getDontLimit());
		if (lc.dontLimit(cmds))
			return null;
		return shouldLimit(lc, p,pay);
	}

	public ShouldLimitObject shouldLimit(LimitedCommand lc, Player p, boolean pay) {
		ShouldLimitObject slo = new ShouldLimitObject();
		slo.lc = lc;
		if (lc.getPermissionNode() != null && !p.hasPermission(lc.getPermissionNode())){
			slo.limit = ShouldLimit.YES;
			slo.setLimitMsg("&cYou don't have permission to use /" +lc.getCommand());
			return slo;
		}
			
		for (Rule r: lc.getRules()){
//			System.out.println("Checking rule " + r);
			slo.r = r;
			
			/// Check Money
			Double money = r.getMoney();
			if (money != null && !MoneyController.hasEnough(p.getName(), money)){
					continue;}
			/// Check Items
			Collection<ItemStack> items = r.getItems();
//			System.out.println("checking items = " + items);

			if (items != null && !InventoryUtil.hasItems(p,items)){
//				System.out.println("missed items");
				continue;}
			/// Check ntimes
			/// Do this last as it requires a sql check
			if (r.isTimeLimited()){
				final int nTimes = sql.getTimesUsedWithin(p.getName(), lc.getCommand(),r.getTimespan(),slo) ;
//				System.out.println("ntimes="+nTimes + " " + r.getNtimes() + " slo.time=" + slo.getFirstTimeUsed());
				if (nTimes >= r.getNtimes()){
					continue;}
			}
			/// They satisfied this rule, now do we need to warn them or not
			if (!lc.warnRule(r) || pay){
				/// We made it, they have passed the gauntlet,now make them pay it
//				payRule(r, lc, p,sql);
				slo.limit = ShouldLimit.NO;
				slo.isPay = pay;
				return slo;
			} else {
				slo.limit = ShouldLimit.WARN;
				slo.setLimitMsg(lc.getWarnMessage(slo.getFirstTimeUsed(), pay));
				return slo;
			}
		}
		/// No rules were satistfied
		slo.limit = ShouldLimit.YES;
		slo.r = null;
		slo.setLimitMsg(lc.getWarnMessage(slo.getFirstTimeUsed(), pay));
		return slo;
	}

	public void payRule(ShouldLimitObject slo, Player p){
		payRule(slo.r, slo.lc, p, p.getName());
	}

	public void payRule(ShouldLimitObject slo, Player p, String name){
		payRule(slo.r, slo.lc, p, name);
	}

	public void payRule(Rule r, LimitedCommand lc, Player p, String name) {
		/// Pay Money
		Double money = r.getMoney();
		if (money != null){
			MoneyController.subtract(name,money);}
		/// Pay Items
		Collection<ItemStack> items = r.getItems();
//		System.out.println("paying items = " + items);
		if (items != null && p!=null){
			InventoryUtil.removeItems(p,items);}
		if (r.isTimeLimited()){
			sql.addUsage(name, lc.getCommand());
		}		
	}

	public void addCommand(LimitedCommand lc) {
		limitedCommands.put(lc.getCommand(), lc);
	}
}
