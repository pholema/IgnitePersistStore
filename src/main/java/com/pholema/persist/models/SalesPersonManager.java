package com.pholema.persist.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.ignite.cache.QueryIndex;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pholema.persist.util.CommonUtil;
import com.pholema.tool.utils.common.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SalesPersonManager implements Serializable {

    private static final long serialVersionUID = -2618627300166784354L;
    private String salesPerson;
    private String manager;
    private String salesPersonName;
    private String managerName;
    private String userLeadingAdding;

    private transient String key;
    public String key() {
        if (key == null)
            key = salesPerson;
        return key;
    }

    public SalesPersonManager() {
    }

    public SalesPersonManager(String salesPerson, String manager) {
        this.salesPerson = salesPerson;
        this.manager = manager;
    }

    public static LinkedHashMap<String, String> getFields() {
        LinkedHashMap<String, String> fields = new LinkedHashMap<>();
        fields.put("salesPerson","java.lang.String");
        fields.put("manager","java.lang.String");
        fields.put("salesPersonName","java.lang.String");
        fields.put("managerName","java.lang.String");
        return fields;
    }

    public static List<QueryIndex> getIndexes() {
        List<QueryIndex> indexes = new ArrayList<>();
        indexes.add(new QueryIndex("salesPerson"));
        return indexes;
    }

    public static String cacheName() {
        return SalesPersonManager.class.getSimpleName();
    }

    public boolean checkAndConfigField() {
        if (StringUtils.isEmpty(salesPerson) || StringUtils.isEmpty(salesPersonName)) {
            return false;
        }
        salesPersonName = CommonUtil.captureName(salesPersonName);
        manager = StringUtils.isEmpty(manager) ? salesPerson : StringUtils.null2Empty(manager).toLowerCase();
        managerName = StringUtils.isEmpty(managerName) ? salesPersonName : StringUtils.null2Empty(CommonUtil.captureName(managerName));
        setUserLeadingAdding(userLeadingAdding);
        return true;
    }

    public String getSalesPerson() {
        return salesPerson;
    }

    public void setSalesPerson(String salesPerson) {
        this.salesPerson = salesPerson;
    }

    public String getManager() {
        return manager;
    }

    public void setManager(String manager) {
        this.manager = manager;
    }

    public String getSalesPersonName() {
        return salesPersonName;
    }

    public void setSalesPersonName(String salesPersonName) {
        this.salesPersonName = salesPersonName;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    public String getUserLeadingAdding() {
        return userLeadingAdding;
    }

    public void setUserLeadingAdding(String userLeadingAdding) {
        this.userLeadingAdding = StringUtils.isEmpty(userLeadingAdding) ? "BI" : userLeadingAdding;
    }
}
