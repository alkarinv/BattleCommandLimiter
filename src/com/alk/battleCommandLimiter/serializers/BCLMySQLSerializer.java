package com.alk.battleCommandLimiter.serializers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Formatter;
import java.util.MissingFormatArgumentException;

import com.alk.battleCommandLimiter.objects.ShouldLimitObject;

/**
 * 
 * @author Alkarin
 *
 */
public class BCLMySQLSerializer {
	static final boolean DEBUG = false;

	static public int MAX_NAME_LENGTH = 32;
	static public int MAX_COMMAND_LENGTH = 64;
	static public String URL = "localhost";
	static public String PORT = "3306";
	static public String USERNAME = "root";
	static public String PASSWORD = "";

	static public String DB = "minecraft";
	static public String COMMAND_TABLE = "Commands";	

	static final public String ID = "ID";
	static final public String PLAYER = "Player";
	static final public String MONEY_NAME = "Money";
	static final public String COMMAND = "Command";
	static final public String DATE = "Date";
	static final public String PLAYTIME = "PlayTime";
	static final public String FIRSTJOINDATE = "FirstJoin";
	static final public String LASTJOINDATE = "LastJoin";
	
	String create_database; 

	String command_table_exists = "desc " + COMMAND_TABLE;

	String create_command_table = "CREATE TABLE IF NOT EXISTS " + COMMAND_TABLE +" ("+
			ID + " INTEGER AUTO_INCREMENT," +
			PLAYER + " VARCHAR(" + MAX_NAME_LENGTH +") NOT NULL ,"+
			COMMAND + " VARCHAR(" + MAX_COMMAND_LENGTH +") NOT NULL ,"+
			DATE + " DATETIME," +
			"PRIMARY KEY ("+ID+","+PLAYER+","+COMMAND+","+DATE+"), INDEX USING BTREE ("+DATE+
			"),INDEX USING BTREE ("+COMMAND+"),INDEX USING HASH ("+ PLAYER+")) "+
			"DEFAULT CHARACTER SET = utf8 "+
			"COLLATE = utf8_general_ci";
	final String get_times_used_within = "SELECT COUNT(*) FROM "+COMMAND_TABLE+" WHERE "+PLAYER+"='%s' AND "+COMMAND+"='%s' AND "+
			DATE+">now() - INTERVAL %s SECOND"; 
	final String get_used_within = "SELECT COUNT(*),"+DATE+" FROM "+COMMAND_TABLE+" WHERE "+PLAYER+"='%s' AND "+COMMAND+"='%s' AND "+
			DATE+">now() - INTERVAL %s SECOND ORDER BY "+DATE; 
	final String add_usage ="INSERT INTO "+COMMAND_TABLE+" ("+PLAYER+","+COMMAND+","+DATE+") VALUES ('%s','%s',now())";

	public BCLMySQLSerializer(){
	}
	private void initStatements(){
		create_database = "CREATE DATABASE IF NOT EXISTS " + DB;
	}
	class RSCon{
		public ResultSet rs;
		public Connection con;
	}

	public Connection getConnection(){
		Connection con;  /// Our database connection
		try {
			con = DriverManager.getConnection("jdbc:mysql://"+URL+":" + PORT +"/" + DB, USERNAME,PASSWORD);
			return con;
		} catch (SQLException e1) {
			e1.printStackTrace();
			return null;
		}	
	}

	public boolean init(){
		initStatements();
		Connection con;  /// Our database connection
		try {
			Class.forName("com.mysql.jdbc.Driver");
			if (DEBUG) System.out.println("Got Driver");
		} catch (ClassNotFoundException e1) {
			System.err.println("Failed getting driver");
			e1.printStackTrace();
			return false;
		}

		String strStmt = create_database;
		try {
			con = DriverManager.getConnection("jdbc:mysql://"+URL+":" + PORT, USERNAME,PASSWORD);
			Statement st = con.createStatement();
			st.executeUpdate(strStmt);
			if (DEBUG) System.out.println("Creating db");
		} catch (SQLException e) {
			System.err.println("Failed creating db: "  + strStmt);
			e.printStackTrace();
			return false;
		}
		try {
			con = DriverManager.getConnection("jdbc:mysql://"+URL+":" + PORT +"/" + DB, USERNAME,PASSWORD);
		} catch (SQLException e1) {
			e1.printStackTrace();
			return false;
		}

		
		createTable(con, command_table_exists, create_command_table,null);

		try {con.close();} catch (SQLException e) {e.printStackTrace();}

		return true;
	}

	private boolean createTable(Connection con, String sql_table_exists, String sql_create_table,String sql_create_index) {
		String strStmt;
		strStmt = sql_table_exists;
		/// Check to see if our table exists;
		boolean table_exists = false;
		try {
			Statement st = con.createStatement();
			st.executeUpdate(strStmt);
			if (DEBUG) System.out.println("table exists");
			table_exists = true;
		} catch (SQLException e) {
			if (DEBUG) System.out.println("table does not exist");
		}
		/// If the table exists nothing left to do
		if (table_exists)
			return true;
		/// Create our table and index
		strStmt = sql_create_table;
		Statement st = null;
		int result =0;
		try {
			st = con.createStatement();
			result = st.executeUpdate(strStmt);
			if (DEBUG) System.out.println("Created Table with stmt=" + strStmt);

			if (sql_create_index != null){
				try{
					st = con.createStatement();
					st.executeUpdate(sql_create_index);
					if (DEBUG) System.out.println("Created Index");				
				} catch (Exception e){
					if (DEBUG) System.err.println("Failed in creating Index");
					return false;
				}					
			}
		} catch (SQLException e) {
			if (DEBUG) System.err.println("Failed in creating Table " +
					strStmt + "   result=" + result);
			e.printStackTrace();
			return false;
		}

		return true;
	}

	private RSCon executeQuery(String strRawStmt, Object... varArgs){
		StringBuilder buf = new StringBuilder();
		Formatter form = new Formatter(buf);
		try{
			form.format(strRawStmt, varArgs);
		} catch (MissingFormatArgumentException e){
			System.err.println("Failed with stmt= " + strRawStmt + "   varArgs=" + varArgs);
			e.printStackTrace();
		}
		RSCon rscon = null;

		ResultSet rs = null;
		Connection con = getConnection();  /// Our database connection
		try {
			Statement st = null;
			st = con.createStatement();
			if (DEBUG)System.out.println("Executing   =" + buf.toString());
			st.executeQuery(buf.toString());

			rs = st.getResultSet();
			rscon = new RSCon();
			rscon.con = con;
			rscon.rs = rs;
		} catch (SQLException e) {
			System.err.println("Couldnt execute query "  + buf.toString());
			e.printStackTrace();
			rscon = null;
		} finally{
			//			try {con.close();} catch (Exception e) {e.printStackTrace();}
		}
		return rscon;
	}

	private int executeUpdate(String strRawStmt, Object... varArgs){
		StringBuilder buf = new StringBuilder();
		Formatter form = new Formatter(buf);
		try{
			form.format(strRawStmt, varArgs);
		} catch (MissingFormatArgumentException e){
			System.err.println("Failed with stmt= " + strRawStmt + "   varArgs=" + varArgs);
			e.printStackTrace();
		}
		int result= -1;
		Connection con = getConnection();  /// Our database connection
		try {
			Statement st = con.createStatement();
			if (DEBUG) System.out.println("Executing   =" + buf.toString());
			result = st.executeUpdate(buf.toString());
		} catch (SQLException e) {
			System.err.println("Couldnt execute update "  + buf.toString());
			e.printStackTrace();
		} finally {
			try {con.close();} catch (Exception e) {e.printStackTrace();}
		}
		return result;
	}

	public int getTimesUsedWithin(String name, String cmd, Long time, ShouldLimitObject slo) {
		RSCon rscon = executeQuery(get_used_within, name, cmd, time);
		try {
			ResultSet rs = rscon.rs;
			while (rs.next()){
				int count = rs.getInt(1);
				Timestamp ts = rs.getTimestamp(2);
				if (ts != null)
					slo.setFirstUsed(ts.getTime());
				try {rscon.con.close();} catch (Exception e) {e.printStackTrace();}
				return count;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {rscon.con.close();} catch (Exception e) {e.printStackTrace();}
		return -1;
	}
	
	public boolean addUsage(String name, String cmd) {
		try{
			executeUpdate(add_usage, name, cmd);				
		}catch (Exception e){
			return false;
		}
		return true;		
	}

}
