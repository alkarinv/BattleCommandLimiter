package com.alk.battleCommandLimiter.objects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.inventory.ItemStack;

import com.alk.battleCommandLimiter.Defaults;
import com.alk.battleCommandLimiter.util.Util;

public class Rule {
	Integer priority = null;
	Double money = null;
	Long timespan = null;
	Integer ntimes = null;
	List<ItemStack> items = null;
	boolean hasItems = false;

	public Rule() {
	}

	public void setMoney(double d) {if (d > 0) this.money = d;}
	public void setTimespan(long d) {if (d > 0) this.timespan = d;}
	public void setNTimes(int ntimes) {if (ntimes >0) this.ntimes = ntimes;}
	public void setPriority(Integer priority) {this.priority = priority;}
	
	public Double getMoney() {return money;}
	public Long getTimespan() {return timespan;}
	public Integer getNtimes() {return ntimes;}
	public Integer getPriority() {return priority;}

	public String toString(){
		StringBuilder sb = new StringBuilder("[Rule " + priority);
		if (ntimes != null) sb.append(","+ntimes+"x");
		if (timespan != null) sb.append(" every "+timespan);
		if (money!= null) sb.append(",money="+money);
		if (items!= null) sb.append(",items="+getItemStr());
		sb.append("]");
		return sb.toString();
	}

	public boolean isTimeLimited() {
		return timespan != null;
	}

	public Collection<ItemStack> getItems() {
		return hasItems ? items : null;
	}

	public void addItems(ArrayList<ItemStack> items) {
		this.items = items;
	}

	public void setHasItems(boolean b) {
		this.hasItems= b;
	}

	public String limitMessage(Long time) {
		StringBuilder sb = new StringBuilder();
		boolean has = false;
		if (ntimes != null && time !=  null) {
//			if (time != null)
//				System.out.println(" timespan="+timespan + "  " + (System.currentTimeMillis() - time)/1000);
			long t = time == null ? 0 : timespan - (System.currentTimeMillis() - time)/1000;
			if (t > 0)
				sb.append("&ewait &6"+ Util.convertSecondsToString(t)); has = true;
		}
		if (money!= null) {
			sb.append((has ? "&e AND ": "")+"&ehave &6"+getMoneyStr()); has = true;
		}
		if (getItems() != null) {sb.append((has ? "&e AND ": "")+"items=&6"+getItemStr());has = true;}
		return sb.toString();
	}

	private String getItemStr() {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (ItemStack is: items){
			if (!first) sb.append(", ");
			sb.append(is.getType()+":" + is.getAmount());
			first = false;
		}
		return sb.toString();
	}

	private String getMoneyStr() {
		if (money.intValue() - money == 0){
//			System.out.println(money.intValue());
			return money.intValue() + Defaults.MONEY_STR;
		}
//		
//		try{
//			Integer.valueOf(money+"");
//			return money.intValue() +Defaults.MONEY_STR;
//		} catch(Exception e){}
		return money + Defaults.MONEY_STR;
	}

}
