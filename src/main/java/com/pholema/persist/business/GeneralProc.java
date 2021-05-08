package com.pholema.persist.business;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.cache.Cache;

import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.SqlQuery;
import org.apache.log4j.Logger;

import com.pholema.persist.IgniteInstance;
import com.pholema.persist.models.AuthorizedRecipient;
import com.pholema.tool.utils.management.LdapUserQuery;

public class GeneralProc {
	private final static Logger logger = Logger.getLogger(GeneralProc.class);

	private IgniteCache<String, AuthorizedRecipient> cache = IgniteInstance.get().getCache(AuthorizedRecipient.cacheName());

	public String getUsernames(Map<String, LdapUserQuery> userMap) {
		String usernames = "";
		for (String key : userMap.keySet()) {
			if (usernames.length() > 0) {
				usernames += ",";
			}
			String userName = userMap.get(key).getUserName();
			if (userName.contains("'")) {
				userName = userName.replace("'", "''");
			}
			usernames += "'" + userName + "'";
		}
		logger.info("usernames " + usernames);
		return usernames;
	}

	public Map<String, String> getRoleNames(String usernames, String jobName) {
		Map<String, String> roleNameMap = new HashMap<>();
		StringBuilder sql = new StringBuilder();
		sql.append(" userName in (" + usernames + ")");
		sql.append(" and jobName = '" + jobName + "'");
		sql.append(" order by roleName,userName");

		SqlQuery<String, AuthorizedRecipient> query_instance = new SqlQuery<>(AuthorizedRecipient.class, sql.toString());
		QueryCursor query = cache.query(query_instance);
		Iterator iter = query.iterator();
		while (iter.hasNext()) {
			Cache.Entry entry = (Cache.Entry) iter.next();
			AuthorizedRecipient authorizedRecipient = (AuthorizedRecipient) entry.getValue();
			roleNameMap.put(authorizedRecipient.getUserName(), authorizedRecipient.getRoleName());
		}
		query.close();
		return roleNameMap;
	}
}
