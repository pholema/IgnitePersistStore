package com.pholema.persist.store;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.pholema.tool.utils.common.EncryptUtils;
import com.pholema.tool.utils.dynamic.PropertiesUtils;

public class DBConnection {
	private static Logger logger = Logger.getLogger(DBConnection.class);
	private static String username = PropertiesUtils.properties.getProperty("db.username");
	private static String password = EncryptUtils.getInstance().decrypt(PropertiesUtils.properties.getProperty("db.password"));
	
	// Opens JDBC connection.
	public static Connection generateConnection(String connectServer) throws SQLException {
    	// Open connection to your RDBMS systems (Oracle, MySQL, Postgres, DB2, Microsoft SQL, etc.)
    	//In this example we use H2 Database for simplification.
    	Connection cn = DriverManager.getConnection(connectServer, username, password);
    	cn.setAutoCommit(true);
    	//logger.info("Connect to sql server --> ["+sqlserver_connect_string+"]");
    	return cn;
    }
	
	public static Connection generateConnection(String connectServer,String username,String password) throws SQLException {
    	// Open connection to your RDBMS systems (Oracle, MySQL, Postgres, DB2, Microsoft SQL, etc.)
    	//In this example we use H2 Database for simplification.
    	Connection cn = DriverManager.getConnection(connectServer, username, password);
    	cn.setAutoCommit(true);
    	//logger.info("Connect to sql server --> ["+sqlserver_connect_string+"]");
    	return cn;
    }

	public static void closeConnection(Connection conn) {
		if (conn != null) {
   			try {
   				conn.close();
			} catch (SQLException e) {
				StringWriter sw = new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				logger.error(sw.toString());
			}
   		 }
	}
	
	public static void closeResultSet(ResultSet res) {
		if (res != null) {
   			try {
				res.close();
			} catch (SQLException e) {
				StringWriter sw = new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				logger.error(sw.toString());
			}
   		 }
	}
	
}
