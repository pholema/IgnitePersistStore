package com.pholema.persist.store.lifeCycleBean;

import org.apache.ignite.IgniteException;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.lifecycle.LifecycleBean;
import org.apache.ignite.lifecycle.LifecycleEventType;
import org.apache.log4j.Logger;

import com.pholema.persist.IgniteInstance;
import com.pholema.persist.models.SalesPersonManager;
import com.pholema.persist.store.SalesPersonManagerStore;
public class SalesPersonManagerLifeCycleBean extends Thread implements LifecycleBean {

    private final static Logger logger = Logger.getLogger(SalesPersonManagerLifeCycleBean.class);

    private void ignite() {
        while (IgniteInstance.get().getIgnite() == null) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        IgniteInstance.get().getCacheWithEntity(SalesPersonManager.cacheName(), String.class, SalesPersonManager.class,
                CacheMode.REPLICATED,
                SalesPersonManagerStore.class.getName(),
                SalesPersonManager.getIndexes(), SalesPersonManager.getFields(), 0);
        IgniteInstance.get().getCache(SalesPersonManager.cacheName()).loadCache(null, 100000);
        logger.info("sl-loaded");
    }

    public void onLifecycleEvent(LifecycleEventType evt) throws IgniteException {
        if (evt == LifecycleEventType.AFTER_NODE_START) {
            Thread load = new SalesPersonManagerLifeCycleBean();
            load.start();
        }
    }

    public void run() {
        ignite();
    }
}
