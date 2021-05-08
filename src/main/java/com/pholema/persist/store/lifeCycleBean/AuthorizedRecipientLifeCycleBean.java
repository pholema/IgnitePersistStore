package com.pholema.persist.store.lifeCycleBean;

import org.apache.ignite.IgniteException;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.lifecycle.LifecycleBean;
import org.apache.ignite.lifecycle.LifecycleEventType;
import org.apache.log4j.Logger;

import com.pholema.persist.IgniteInstance;
import com.pholema.persist.models.AuthorizedRecipient;
import com.pholema.persist.store.AuthorizedRecipientStore;

/**
 *
 * store all portal page authentication user
 *
 */
public class AuthorizedRecipientLifeCycleBean extends Thread implements LifecycleBean {

	private final static Logger logger = Logger.getLogger(AuthorizedRecipientLifeCycleBean.class);

	private void ignite() {
		while (IgniteInstance.get().getIgnite() == null) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		IgniteInstance.get().getCacheWithEntity(AuthorizedRecipient.cacheName(), String.class, AuthorizedRecipient.class,
				CacheMode.REPLICATED,
				AuthorizedRecipientStore.class.getName(),
				AuthorizedRecipient.getIndexes(), AuthorizedRecipient.getFields(), 0);
		IgniteInstance.get().getCache(AuthorizedRecipient.cacheName()).loadCache(null, 100000);
		logger.info("sl-loaded");
	}

	@Override
	public void onLifecycleEvent(LifecycleEventType evt) throws IgniteException {
		if (evt == LifecycleEventType.AFTER_NODE_START) {
			Thread load = new AuthorizedRecipientLifeCycleBean();
			load.start();
		}
	}

	@Override
	public void run() {
		ignite();
	}
}
