package com.pholema.persist.models;

public enum AuthorizedRecipientRoleType {
    user,
    admin;

    public static boolean contains(String value) {
        for (AuthorizedRecipientRoleType r : AuthorizedRecipientRoleType.values()) {
            if (r.name().equals(value)) {
                return true;
            }
        }
        return false;
    }
}
