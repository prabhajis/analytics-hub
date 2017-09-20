package org.wso2telco.analytics.killbill;

/**
 * Created by isuru on 6/2/17.
 */
public class LoggedInUser {

    private boolean isAdmin;
    private boolean isOperatorAdmin;
    private boolean isServiceProvider;
    private boolean isCustomerCareUser;
    private boolean isPublisher;
    private boolean hasNoRole;
    private String username;
    private String tenantId;
    private String domain;
    private String operatorNameInProfile;

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public boolean isOperatorAdmin() {
        return isOperatorAdmin;
    }

    public void setOperatorAdmin(boolean operatorAdmin) {
        isOperatorAdmin = operatorAdmin;
    }

    public boolean isServiceProvider() {
        return isServiceProvider;
    }

    public void setServiceProvider(boolean serviceProvider) {
        isServiceProvider = serviceProvider;
    }

    public boolean isCustomerCareUser() {
        return isCustomerCareUser;
    }

    public void setCustomerCareUser(boolean customerCareUser) {
        isCustomerCareUser = customerCareUser;
    }

    public boolean isPublisher() {
        return isPublisher;
    }

    public void setPublisher(boolean publisher) {
        isPublisher = publisher;
    }

    public boolean isHasNoRole() {
        return hasNoRole;
    }

    public void setHasNoRole(boolean hasNoRole) {
        this.hasNoRole = hasNoRole;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getOperatorNameInProfile() {
        return operatorNameInProfile;
    }

    public void setOperatorNameInProfile(String operatorNameInProfile) {
        this.operatorNameInProfile = operatorNameInProfile;
    }
}
