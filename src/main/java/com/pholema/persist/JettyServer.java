package com.pholema.persist;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.xml.XmlConfiguration;

import java.io.File;

public class JettyServer {

	public void start(String jettyConfigFile, String webAppHome) throws Exception {
		Resource resourceConfig = Resource.newResource(jettyConfigFile);
		XmlConfiguration config = new XmlConfiguration(resourceConfig.getInputStream());
		Server server=(Server)(config.configure());
		initServlet(server,webAppHome);
		server.start();
		server.join();
	}

	public void initServlet(Server server, String webAppHome) {
		WebAppContext context = new WebAppContext();
		context.setDescriptor(webAppHome + File.separator + "WEB-INF/web.xml");
		context.setResourceBase(webAppHome);
		context.setContextPath("/");
		context.setParentLoaderPriority(true);
		server.setHandler(context);
	}
}
