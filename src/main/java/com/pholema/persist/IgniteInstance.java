package com.pholema.persist;

import java.util.ArrayList;
import java.util.List;

import org.apache.ignite.lifecycle.LifecycleBean;
import org.reflections.Reflections;

import com.pholema.tool.starter.ignite.IgniteStore;

public class IgniteInstance {

    private static IgniteStore igniteStore = new IgniteStore();

    public static IgniteStore get() {
        return igniteStore;
    }

    public static void init() {

        Reflections reflections = new Reflections("com.pholema.persist.store.lifeCycleBean");
        List<Class<? extends LifecycleBean>> allClasses = new ArrayList<>(reflections.getSubTypesOf(LifecycleBean.class));

        LifecycleBean[] beans = new LifecycleBean[allClasses.size()];
        for (int i = 0; i < allClasses.size(); i++) {
            System.out.println("lifeCycleBean " + allClasses.get(i).toString());
            try {
                beans[i] = allClasses.get(i).newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        igniteStore.init(beans);
    }

}
