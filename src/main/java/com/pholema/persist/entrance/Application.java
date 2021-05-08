package com.pholema.persist.entrance;

import org.apache.log4j.Logger;

import com.pholema.persist.IgniteInstance;
import com.pholema.persist.JettyServer;
import com.pholema.tool.utils.dynamic.ParameterException;
import com.pholema.tool.utils.dynamic.PropertiesUtils;

public class Application {

    private static Logger logger = Logger.getLogger(Application.class);

    public static void main( String[] args ) throws ParameterException {

        PropertiesUtils.init();
        IgniteInstance.init();
        //
        logger.info("run jetty server...");
        JettyServer server = new JettyServer();
        try {
            server.start(PropertiesUtils.properties.getProperty("jetty.config.file"), PropertiesUtils.properties.getProperty("web.app.home"));
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }
}
