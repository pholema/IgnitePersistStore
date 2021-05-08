package com.pholema.persist.controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import javax.cache.Cache;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
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
import com.pholema.tool.utils.SystemUtil;
import com.pholema.tool.utils.common.StringUtils;

@Singleton
@Path("/persist/authorizedRecipient")
public class AuthorizedRecipientController {

    private static Logger logger = Logger.getLogger(AuthorizedRecipientController.class);

    @Context
    UriInfo uri;

    private IgniteCache<String, AuthorizedRecipient> cache = IgniteInstance.get().getCache(AuthorizedRecipient.cacheName());

    @GET
    @Path("/version")
    @Produces(MediaType.TEXT_PLAIN)
    public String version(){
        logger.info("version...");
        return "version :"+ SystemUtil.getVersion();
    }

    @POST
    @Path("/{jobName}/add")
    @Consumes({MediaType.APPLICATION_JSON})
    public Response add(List<AuthorizedRecipient> list, @PathParam("jobName") String jobName) {
        if (list != null && list.size() > 0) {
            if (uri != null) logger.info(uri.getBaseUri() + uri.getPath());
            System.out.println("list " + StringUtils.toGson(list));
            Iterator<AuthorizedRecipient> iterator = list.iterator();
            while (iterator.hasNext()) {
                AuthorizedRecipient authorizedRecipient = iterator.next();
                authorizedRecipient.setJobName(jobName);
                if (!authorizedRecipient.checkAndConfigField()) {
                    logger.info("authorizedRecipient wrong format.");
                    iterator.remove();
                }
            }
            cache.putAll(list.stream().collect(Collectors.toMap(AuthorizedRecipient::getID, x -> x)));
        }
        return Response.ok().build();
    }

    @POST
    @Path("/{jobName}/delete")
    @Consumes({MediaType.APPLICATION_JSON})
    public Response delete(List<String> list, @PathParam("jobName") String jobName) {
        if (list != null && list.size() > 0) {
            logger.info(uri.getBaseUri() + uri.getPath());
            cache.removeAll(new HashSet<>(list.stream().map(x -> AuthorizedRecipient.key(x, jobName)).collect(Collectors.toList())));
        }
        return Response.ok().build();
    }

    @GET
    @Path("/{jobName}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<AuthorizedRecipient> get(@PathParam("jobName") String jobName) {
        if (uri != null) logger.info(uri.getBaseUri() + uri.getPath());
        List<AuthorizedRecipient> list = new ArrayList<>();
        try {
            String sql = "jobName = '" + jobName + "' order by roleName,userName";
            SqlQuery<String, AuthorizedRecipient> query_instance = new SqlQuery<>(AuthorizedRecipient.class, sql);
            QueryCursor query = cache.query(query_instance);
            Iterator iter = query.iterator();
            while (iter.hasNext()) {
                Cache.Entry entry = (Cache.Entry) iter.next();
                AuthorizedRecipient authorizedRecipient = (AuthorizedRecipient) entry.getValue();
                list.add(authorizedRecipient);
            }
            query.close();
        } catch (Exception e) {
            logger.error("cache query fail", e);
        }
        return list;
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
}
