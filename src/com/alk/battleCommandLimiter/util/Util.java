package com.alk.battleCommandLimiter.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;

public class Util {
	public static String colorChat(String msg) {return msg.replaceAll("&", Character.toString((char) 167));}
	public static String deColorChat(String msg) {return msg.replaceAll("\\&[0-9a-zA-Z]", "");}    
	public static boolean isInt(String i) {try {Integer.parseInt(i);return true;} catch (Exception e) {return false;}}
	public static boolean isFloat(String i){try{Float.parseFloat(i);return true;} catch (Exception e){return false;}}

	public static File load(InputStream inputStream, String config_file) {
		File file = new File(config_file);
		if (!file.exists()){ /// Create a new config file from our default
			try{
				OutputStream out=new FileOutputStream(config_file);
				byte buf[]=new byte[1024];
				int len;
				while((len=inputStream.read(buf))>0){
					out.write(buf,0,len);}
				out.close();
				inputStream.close();
			} catch (Exception e){
			}
		}
		return file;
	}

	public static String convertSecondsToString(long t){
		long s = t % 60;
		t /= 60;
		long m = t %60;
		t /=60;
		long h = t % 24;
		t /=24;
		long d = t;
		StringBuilder sb = new StringBuilder();
		if (d > 0) {
			sb.append("&6"+d + "&e " + dayOrDays(d) +" ");}
		if (h > 0) {
			sb.append("&6"+h + "&e " + hourOrHours(h)+" ");}
		if (m > 0) {
			sb.append("&6"+m + "&e " + minOrMins(m)+" ");}
		if (s > 0) {
			sb.append("&6"+s + "&e " + secOrSecs(s)+" ");}
		return sb.toString();
	}

	public static String convertToString(long t){
		t = t / 1000;  
		return convertSecondsToString(t);
	}

	private static String dayOrDays(long t){
		return t > 1 || t == 0? "days" : "day";
	}

	private static String hourOrHours(long t){
		return t > 1 || t ==0 ? "hours" : "hour";
	}

	private static String minOrMins(long t){
		return t > 1 || t == 0? "minutes" : "minute";
	}
	private static String secOrSecs(long t){
		return t > 1 || t == 0? "sec" : "secs";
	}

	public static String convertLongToDate(long time) {
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd hh:mm");
		return sdf.format(time);
	}

	public static String PorP(int size) {
		return size == 1 ? "person" : "people";
	}
}
