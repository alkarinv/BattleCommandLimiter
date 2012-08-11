package com.alk.battleCommandLimiter.util;

import com.alk.battleCommandLimiter.objects.Rule;

public class RuleParser {

	public static Rule parseRule(String rulekey, String rulestr) {
//		System.out.println("ruleKey = " + rulekey + ", ruleStr =" + rulestr);
		Rule rule = new Rule();
		rulestr = rulestr.replaceAll("and", "AND");
		String[] rules = rulestr.split("AND");
		rule.setPriority(parsePriority(rulekey));
		for (String r : rules){
			parseIndividualRule(rule, r);
		}
//		System.out.println("Finished parsing rule=" + rule);
		return rule;
	}

	public static Integer parsePriority(String rulekey) {
		String intstr = rulekey.replaceAll("[a-zA-Z_]", "");
		return Integer.valueOf(intstr);		
	}

	private static void parseIndividualRule(Rule rule, String rulestr) {
		rulestr = rulestr.trim();
		if (rulestr.contains("every")){
			parseTimeLimit(rule,rulestr);
		} else if (rulestr.contains("money")){
			parseMoney(rule, rulestr);
		} else if (rulestr.contains("item")){
			parseItems(rule, rulestr);
		}
	}

	private static void parseItems(Rule rule, String rulestr) {
		rule.setHasItems(true);
	}

	private static void parseMoney(Rule rule, String rulestr) {
		rulestr = rulestr.replaceAll("[^0-9]", "").trim();
		rule.setMoney(Double.valueOf(rulestr));
	}

	private static void parseTimeLimit(Rule rule, String rulestr) {
		String[] split = rulestr.split("every");
		String ntimestr = split[0].replaceAll("x", "");
		rule.setNTimes(Integer.valueOf(ntimestr.trim()));
		rule.setTimespan(parseTimeSpan(split[1].trim()));
	}

	private static long parseTimeSpan(String str) {
		final int index = str.indexOf(' ');
		String timespanstr = str.substring(0,index);
		Integer timespan = Integer.valueOf(timespanstr);
		if (str.contains("sec")){ return timespan;}
		else if (str.contains("min")){return timespan * 60;}
		else if (str.contains("hour")){return timespan * 60*60;}
		else if (str.contains("day")){return timespan * 60*60*24;}
		else if (str.contains("week")){return timespan * 60*60*24*7;}
		return 0;
	}

}
