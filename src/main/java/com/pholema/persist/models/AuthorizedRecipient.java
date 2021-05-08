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
public class AuthorizedRecipient implements Serializable {

    private static final long serialVersionUID = -1774590528954882156L;
    private String ID;
    private String userName;
    private String roleName;
    private String jobName;

    public AuthorizedRecipient() {
    }

    public AuthorizedRecipient(AuthorizedRecipient authorizedRecipient) {
        this.ID = authorizedRecipient.ID;
        this.userName = authorizedRecipient.userName;
        this.roleName = authorizedRecipient.roleName;
        this.jobName = authorizedRecipient.jobName;
    }

    public AuthorizedRecipient(String id, String userName, String roleName, String jobName) {
        this.ID = id;
        this.userName = userName;
        this.roleName = roleName;
        this.jobName = jobName;
    }

    public AuthorizedRecipient(String userName, String jobName) {
        this.userName = userName;
        this.jobName = jobName;
        checkAndConfigField();
    }

    public static LinkedHashMap<String, String> getFields() {
        LinkedHashMap<String, String> fields = new LinkedHashMap<>();
        fields.put("ID", "java.lang.String");
        fields.put("userName", "java.lang.String");
        fields.put("roleName", "java.lang.String");
        fields.put("jobName", "java.lang.String");
        return fields;
    }

    public static List<QueryIndex> getIndexes() {
        List<QueryIndex> indexes = new ArrayList<>();
        indexes.add(new QueryIndex("userName"));
        indexes.add(new QueryIndex("jobName"));
        return indexes;
    }

    public static String cacheName() {
        return AuthorizedRecipient.class.getSimpleName();
    }

    public boolean checkAndConfigField() {
        if (StringUtils.isEmpty(userName)) {
            return false;
        }
        userName = CommonUtil.captureName(userName);
        //
        if (roleName != null) {
            if (!AuthorizedRecipientRoleType.contains(roleName.toLowerCase())) {
                return false;
            }
        } else {
            roleName = AuthorizedRecipientRoleType.user.name();
        }
        roleName = roleName.toLowerCase();
        return true;
    }

    public static String key(String userName, String jobName) {
        return userName + "_" + jobName;
    }

    public void setID() {
        this.ID = key(userName, jobName);
    }

    public String getID() {
        if (ID == null) {
            setID();
        }
        return ID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }
}
