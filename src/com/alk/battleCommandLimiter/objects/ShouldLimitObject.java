package com.alk.battleCommandLimiter.objects;



public class ShouldLimitObject {
	Long time = null;
	public ShouldLimitObject() {
	}
	public enum ShouldLimit {NO,YES,WARN};
	public LimitedCommand lc;
	public Rule r;
	public ShouldLimit limit;
	public boolean isPay = false;
	String limitMsg = null;
	
	public String getLimitMsg() {
		return limitMsg;
	}
	public void setLimitMsg(String limitMsg) {
		this.limitMsg = limitMsg;
	}
	public void setFirstUsed(long time) {
		this.time = time;
	}
	public Long getFirstTimeUsed() {
		return time;
	}

}
