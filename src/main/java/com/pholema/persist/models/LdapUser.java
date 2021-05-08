package com.pholema.persist.models;

import java.util.HashMap;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.pholema.tool.utils.timer.TimerTracker;

import jersey.repackaged.com.google.common.cache.Cache;
import jersey.repackaged.com.google.common.cache.CacheBuilder;
public class LdapUser extends TimerTask {

    private final static Logger logger = Logger.getLogger(LdapUser.class);

    public static Cache<String, String> MAP = CacheBuilder
            .newBuilder()
            .expireAfterWrite(1, TimeUnit.DAYS)
            .build();

    static {
        b2b_init();

        TimerTracker timerTracker = new TimerTracker();
        timerTracker.runCrossDay(0, new LdapUser());
    }

    public static void b2b_init() {
        logger.info("b2b_init");
        MAP.asMap().putAll(new HashMap<String, String>() {{
            put("others", "Others");
            put("egg", "NE Business Online");
            put("sci", "Sci Quest");
            put("sub", "Subscription Order");
            put("mkpl", "Offline Marketplace");
        }});
    }

    @Override
    public void run() {
        b2b_init();

        TimerTracker.reSchedule(new LdapUser());
    }
}
