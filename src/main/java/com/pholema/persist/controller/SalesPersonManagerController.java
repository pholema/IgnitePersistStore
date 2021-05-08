package com.pholema.persist.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.cache.Cache;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.SqlQuery;
import org.apache.log4j.Logger;

import com.pholema.persist.IgniteInstance;
import com.pholema.persist.models.AuthorizedRecipient;
import com.pholema.persist.models.AuthorizedRecipientWithManager;
import com.pholema.persist.models.SalesPersonManager;
import com.pholema.tool.utils.SystemUtil;
import com.pholema.tool.utils.common.StringUtils;

@Singleton
@Path("/persist/salesPersonManager")
public class SalesPersonManagerController {

    private static Logger logger = Logger.getLogger(SalesPersonManagerController.class);

    @Context
    UriInfo uri;

    private IgniteCache<String, SalesPersonManager> cache = IgniteInstance.get().getCache(SalesPersonManager.cacheName());

    @GET
    @Path("/version")
    @Produces(MediaType.TEXT_PLAIN)
    public String version() {
        logger.info("version...");
        return "version :"+ SystemUtil.getVersion();
    }

    @POST
    @Path("/add")
    @Consumes({MediaType.APPLICATION_JSON})
    public Response add(List<SalesPersonManager> list) {
        if (list != null && list.size() > 0) {
            logger.info(uri.getBaseUri() + uri.getPath());
            logger.debug("list " + StringUtils.toGson(list));
            Iterator<SalesPersonManager> iterator = list.iterator();
            while (iterator.hasNext()) {
                SalesPersonManager salesPersonManager = iterator.next();
                if (!salesPersonManager.checkAndConfigField()) {
                    logger.info("salesPersonManager wrong format.");
                    iterator.remove();
                }
            }
            cache.putAll(list.stream().collect(Collectors.toMap(SalesPersonManager::key, x -> x)));
        }
        return Response.ok().build();
    }

    @POST
    @Path("/delete")
    @Consumes({MediaType.APPLICATION_JSON})
    public Response delete(List<SalesPersonManager> list) {
        if (list != null && list.size() > 0) {
            logger.info(uri.getBaseUri() + uri.getPath());
            Map<String, SalesPersonManager> map = list.stream().filter(x -> !StringUtils.isEmpty(x.getSalesPerson()))
                    .map(salesPersonManager -> {   // get original and replace
                        SalesPersonManager orig = cache.get(salesPersonManager.getSalesPerson());
                        orig.setUserLeadingAdding(salesPersonManager.getUserLeadingAdding());
                        return orig;}).collect(Collectors.toMap(SalesPersonManager::key, x -> x));
            cache.putAll(map); // adding lastEditInfo
            cache.removeAll(new HashSet<>(map.keySet()));
        }
        return Response.ok().build();
    }

    @Deprecated
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public SalesPersonManager get(@QueryParam("salesPerson") String salesPerson,
                                  @QueryParam("manager") String manager) {
        if (salesPerson == null && manager == null) {
            return null;
        }
        SalesPersonManager salesPersonManager = new SalesPersonManager();
        try {
            String sql = (salesPerson != null ? "salesPerson = '" + salesPerson + "'" : "") +
                    (salesPerson != null && manager != null ? " and " : "") +
                    (manager != null ? "manager = '" + manager + "'" : "");
            logger.info("sql " + sql);
            SqlQuery<String, SalesPersonManager> query_instance = new SqlQuery<>(SalesPersonManager.class, sql);
            QueryCursor query = cache.query(query_instance);
            Iterator iter = query.iterator();
            while (iter.hasNext()) {
                Cache.Entry entry = (Cache.Entry) iter.next();
                salesPersonManager = (SalesPersonManager) entry.getValue();
                return salesPersonManager;
            }
            query.close();
        } catch (Exception e) {
            logger.error("cache query fail", e);
        }
        salesPersonManager.setSalesPerson(salesPerson);
        salesPersonManager.setManager(manager);
        salesPersonManager.setSalesPersonName(salesPerson);
        salesPersonManager.setManagerName("Others");
        return salesPersonManager;
    }

    /**
     * customized with AuthorizedRecipient cache
     */
    @GET
    @Path("/authorized")
    @Produces(MediaType.APPLICATION_JSON)
    public List<AuthorizedRecipientWithManager> get() {
        logger.info(uri.getBaseUri() + uri.getPath());
        Map<String,AuthorizedRecipientWithManager> map = new HashMap<>();
        try {
            AuthorizedRecipientController auth = new AuthorizedRecipientController();
            List<AuthorizedRecipient> authList = auth.get("b2b");
            map.putAll(authList.stream().map(AuthorizedRecipientWithManager::new).collect(Collectors.toMap(AuthorizedRecipientWithManager::getUserName, x -> x)));
            //
            String sql = "order by salesPerson"; // cant be empty
            SqlQuery<String, SalesPersonManager> query_instance = new SqlQuery<>(SalesPersonManager.class, sql);
            QueryCursor query = cache.query(query_instance);
            Iterator iter = query.iterator();
            while (iter.hasNext()) {
                Cache.Entry entry = (Cache.Entry) iter.next();
                SalesPersonManager salesPersonManager = (SalesPersonManager) entry.getValue();
                if (map.get(salesPersonManager.getSalesPersonName()) != null) {
                    map.get(salesPersonManager.getSalesPersonName()).setSalesPersonManager(salesPersonManager);
                }
            }
            query.close();
        } catch (Exception e) {
            logger.error("cache query fail", e);
        }
        return new ArrayList<>(map.values());
    }

    @GET
    @Path("/reload")
    @Produces(MediaType.TEXT_PLAIN)
    public Response reload(){
        logger.info("reload start...");
        cache.clear();
        logger.info("clear end...");
        cache.loadCache(null,null);
        logger.info("reload end.");
        return Response.ok().build();
    }

    @GET
    @Path("/mapToAuthorized")
    @Produces(MediaType.TEXT_PLAIN)
    public Response mapToAuthorized() {
        logger.info(uri.getBaseUri() + uri.getPath());
        List<SalesPersonManager> list = new ArrayList<>();
        try {
            String sql = "salesPerson != salesPersonName and manager != ''";
            SqlQuery<String, SalesPersonManager> query_instance = new SqlQuery<>(SalesPersonManager.class, sql);
            QueryCursor query = cache.query(query_instance);
            Iterator iter = query.iterator();
            while (iter.hasNext()) {
                Cache.Entry entry = (Cache.Entry) iter.next();
                SalesPersonManager salesPersonManager = (SalesPersonManager) entry.getValue();
                list.add(salesPersonManager);
            }
            query.close();
        } catch (Exception e) {
            logger.error("cache query fail", e);
        }
        List<AuthorizedRecipient> authList = list.stream().map(x -> {
                    AuthorizedRecipient au = new AuthorizedRecipient();
                    au.setUserName(x.getSalesPersonName());
                    return au;
                }).collect(Collectors.toList());
        AuthorizedRecipientController auth = new AuthorizedRecipientController();
        auth.add(authList, "b2b");
        logger.info("mapToAuthorized end.");
        return Response.ok().build();
    }
    
    /**
     * get user role : user/manager/admin
     */
    @GET
    @Path("/getRole")
    public String getRole(@QueryParam("userid") String userid) {
    	String role = "user";
        logger.info(uri.getBaseUri() + uri.getPath());
        Map<String,AuthorizedRecipientWithManager> map = new HashMap<>();
        try {
            AuthorizedRecipientController auth = new AuthorizedRecipientController();
            List<AuthorizedRecipient> authList = auth.get("b2b");
            map.putAll(authList.stream().map(AuthorizedRecipientWithManager::new).collect(Collectors.toMap(AuthorizedRecipientWithManager::getUserName, x -> x)));
            //
            String sql = "manager = ?"; // cant be empty
            SqlQuery<String, SalesPersonManager> query_instance = new SqlQuery<>(SalesPersonManager.class, sql);
            query_instance.setArgs(userid);
            QueryCursor query = cache.query(query_instance);
            
            Iterator iter = query.iterator();
            while (iter.hasNext()) {
                Cache.Entry entry = (Cache.Entry) iter.next();
                SalesPersonManager salesPersonManager = (SalesPersonManager) entry.getValue();
                role = "manager";
                AuthorizedRecipientWithManager ar = map.get(salesPersonManager.getManagerName());
                if (ar != null && !ar.getRoleName().equals("user")) {
                	role = map.get(salesPersonManager.getManagerName()).getRoleName();
                }
                break;
            }
            query.close();
        } catch (Exception e) {
            logger.error("cache query fail", e);
        }
        return role;
    }
    
}
