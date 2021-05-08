package com.pholema.persist.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;
import javax.naming.NamingException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.server.JSONP;

import com.pholema.persist.business.GeneralProc;
import com.pholema.persist.models.LdapUser;
import com.pholema.tool.utils.management.Ldap;
import com.pholema.tool.utils.management.LdapUserQuery;

@Singleton
@Path("/persist")
public class GeneralController {
	GeneralProc generalProc = new GeneralProc();

	/**
	 * 
	 * 
	 * @param users
	 * @return
	 */
	@GET
	@Path("/ldap")
	@Produces(MediaType.APPLICATION_JSON)
	public List<LdapUserQuery> getLdapUser(@QueryParam("user") List<String> users) {

		List<LdapUserQuery> list = new ArrayList<>();

		for (String user : users) {
			if (LdapUser.MAP.asMap().get(user) == null) {
				LdapUser.MAP.asMap().put(user, Ldap.getLdapUser(user, false));
			}

			LdapUserQuery ldapUserQuery = new LdapUserQuery();
			ldapUserQuery.setUserId(user);
			ldapUserQuery.setUserName(LdapUser.MAP.asMap().get(user));
			list.add(ldapUserQuery);
		}
		return list;
	}

	/**
	 * provide admin portal to search accounts 
	 * 
	 * @param queryId
	 * @param jobName
	 * @return
	 */
	@GET
	@JSONP
	@Path("/getLdapUsers")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public List<LdapUserQuery> getLdapUsers(@QueryParam("query") String queryId, @QueryParam("job") String jobName) {
		List<LdapUserQuery> list = new ArrayList<>();
		if (queryId == null || queryId.length() < 4) {
			return list;
		}

		try {
			Map<String, LdapUserQuery> userMap = Ldap.getFuzzyQuery(queryId, false);
			if (jobName != null) {
				// build usernames
				String usernames = generalProc.getUsernames(userMap);

				// query AuthorizedRecipient
				Map<String, String> roleNameMap = generalProc.getRoleNames(usernames, jobName);

				// mapping role name
				if (roleNameMap.size() > 0) {
					for (String key : userMap.keySet()) {
						userMap.get(key).setRoleName(roleNameMap.get(userMap.get(key).getUserName()));
					}
				}
			}
			return new ArrayList<>(userMap.values());
		} catch (NamingException e) {
			e.printStackTrace();
		}
		return list;
	}

}
