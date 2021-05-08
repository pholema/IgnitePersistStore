package com.pholema.persist.models;

public class AuthorizedRecipientWithManager extends AuthorizedRecipient {

    private String salesPerson;
    private String manager;
    private String salesPersonName;
    private String managerName;

    public AuthorizedRecipientWithManager() {
    }

    public AuthorizedRecipientWithManager(AuthorizedRecipient authorizedRecipient) {
        super(authorizedRecipient);
        // for order
        this.salesPerson = "";
        this.manager = "";
        this.salesPersonName = "";
        this.managerName = "";
    }

    public void setSalesPersonManager(SalesPersonManager salesPersonManager) {
        this.salesPerson = salesPersonManager.getSalesPerson();
        this.manager = salesPersonManager.getManager();
        this.salesPersonName = salesPersonManager.getSalesPersonName();
        this.managerName = salesPersonManager.getManagerName();
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
}
