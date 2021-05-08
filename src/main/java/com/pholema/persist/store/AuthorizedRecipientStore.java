package com.pholema.persist.store;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.cache.Cache.Entry;
import javax.cache.integration.CacheLoaderException;
import javax.cache.integration.CacheWriterException;

import org.apache.ignite.cache.store.CacheStoreAdapter;
import org.apache.ignite.lang.IgniteBiInClosure;
import org.apache.log4j.Logger;

import com.pholema.persist.models.AuthorizedRecipient;
import com.pholema.tool.utils.dynamic.PropertiesUtils;

public class AuthorizedRecipientStore extends CacheStoreAdapter<String, AuthorizedRecipient> {

	private final static Logger logger = Logger.getLogger(AuthorizedRecipientStore.class);

	private static String connect_sqlServer = PropertiesUtils.properties.getProperty("db.jdbc.url");
	private static String dbSchema = "s7bidb09.RealTM.dbo.D_RTM_Authorized_Recipient";
	
	@Override
	public AuthorizedRecipient load(String key) throws CacheLoaderException {
		AuthorizedRecipient authorizedRecipient = null;
		String script = "SELECT ID, userName, roleName, jobName "
				+ "FROM " + dbSchema
				+ " WHERE ID = ? ";
	    try {
	    	logger.info("Get data start...");
	    	Connection conn = DBConnection.generateConnection(connect_sqlServer);
	    	ResultSet res;
	        try (PreparedStatement st = conn.prepareStatement(script)) {
	          st.setString(1, key);
	          res = st.executeQuery();
	          while (res.next()) {
				  authorizedRecipient = new AuthorizedRecipient(res.getString(1), res.getString(2), res.getString(3), res.getString(4));
	        	  logger.info("Get data complete-->" + res.getString(2));
	          }
	        }
	        DBConnection.closeResultSet(res);
	        DBConnection.closeConnection(conn);
	   } catch (SQLException e) {
	        throw new CacheLoaderException("Failed to load: " + key, e);
	   }
	   return authorizedRecipient;
	}

	@Override
	public void delete(Object key) throws CacheWriterException {
		String script = "DELETE " + dbSchema
				+ " WHERE ID = ? ";
		try {
			Connection conn = DBConnection.generateConnection(connect_sqlServer);
			try (PreparedStatement st = conn.prepareStatement(script)) {
				st.setString(1, (String)key);
				st.executeUpdate();
			}
			DBConnection.closeConnection(conn);
			System.out.println("Delete data complete");
		} catch (SQLException e) {
			throw new CacheWriterException("Failed to delete: " + key, e);
		}
	}

	@Override
	public void write(Entry<? extends String, ? extends AuthorizedRecipient> entry) throws CacheWriterException {
		try {
			Connection conn = DBConnection.generateConnection(connect_sqlServer);
			// Syntax of MERGE statement is database specific and should be adopted for your database.
			// If your database does not support MERGE statement then use sequentially update, insert statements.
			StringBuilder sb = new StringBuilder();
			sb.append("declare @ID varchar(100) \r\n");
			sb.append("declare @userName varchar(50) \r\n");
			sb.append("declare @roleName varchar(50) \r\n");
			sb.append("declare @jobName varchar(50) \r\n");
			sb.append("select @ID=?, @userName=?, @roleName=?, @jobName=? \r\n");
			sb.append("if Not exists(select top 1 1 from " + dbSchema + " where ID=@ID) \r\n");
			sb.append("   begin \r\n");
			sb.append("      insert into " + dbSchema + "(ID,userName,roleName,jobName) values(@ID,@userName,@roleName,@jobName) \r\n");
			sb.append("   end \r\n");
			sb.append("else \r\n");
			sb.append("   begin \r\n");
			sb.append("      update " + dbSchema + " set userName=@userName, roleName=@roleName, jobName=@jobName where ID=@ID \r\n");
			sb.append("   end \r\n");

			try (PreparedStatement st = conn.prepareStatement(sb.toString())) {
				AuthorizedRecipient v = entry.getValue();
				st.setString(1, v.getID());
				st.setString(2, v.getUserName());
				st.setString(3, v.getRoleName());
				st.setString(4, v.getJobName());
				st.executeUpdate();
			}
			DBConnection.closeConnection(conn);
			System.out.println("insert/update finished");
		} catch (SQLException e) {
			logger.error("fail " , e);
			throw new CacheWriterException("Failed to write");
		}
	}
	
	// This method is called whenever "loadCache()" and "localLoadCache()"
	// methods are called on IgniteCache. It is used for bulk-loading the cache.
	// If you don't need to bulk-load the cache, skip this method.
	@Override 
	public void loadCache(IgniteBiInClosure<String, AuthorizedRecipient> clo, Object... args) {
		String script = "SELECT ID,userName,roleName,jobName "
				+ "FROM " + dbSchema;

		Integer defined_entry_count;
		if (args == null || args.length == 0 || args[0] == null) {
			defined_entry_count = 2000000;
		} else {
			defined_entry_count = (Integer)args[0];
		}
		final int entryCnt = defined_entry_count;
		logger.info("entryCnt-->" + entryCnt);
		try {
			Connection conn = DBConnection.generateConnection(connect_sqlServer);
			try (PreparedStatement st = conn.prepareStatement(script)) {
				try (ResultSet res = st.executeQuery()) {
					int cnt = 0;
					AuthorizedRecipient authorizedRecipient;
					while (cnt < entryCnt && res.next()) {
						authorizedRecipient = new AuthorizedRecipient(res.getString(1), res.getString(2), res.getString(3), res.getString(4));
						clo.apply(authorizedRecipient.getID(), authorizedRecipient);
						cnt++;
					}
				}
			}
			DBConnection.closeConnection(conn);
		} catch (SQLException e) {
			throw new CacheLoaderException("Failed to load values from cache store.", e);
		}
		logger.info("warm up finished");
	  }
}
