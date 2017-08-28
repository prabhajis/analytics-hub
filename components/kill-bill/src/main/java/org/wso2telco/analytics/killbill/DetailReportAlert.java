package org.wso2telco.analytics.killbill;


public class DetailReportAlert {

    private String subscriber;
    private String api;
    private String applicationName;
    private String eventType;
    private String operatorName;
    private Double totalamount;
    private Double spshare;
    private Double operatorshare;
    private Double hubshare;
    private Double tax;

    public String getSubscriber() {
        return subscriber;
    }

    public void setSubscriber(String subscriber) {
        this.subscriber = subscriber;
    }

    public String getApi() {
        return api;
    }

    public void setApi(String api) {
        this.api = api;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public Double getTotalamount() {
        return totalamount;
    }

    public void setTotalamount(Double totalamount) {
        this.totalamount = totalamount;
    }

    public Double getSpshare() {
        return spshare;
    }

    public void setSpshare(Double spshare) {
        this.spshare = spshare;
    }

    public Double getOperatorshare() {
        return operatorshare;
    }

    public void setOperatorshare(Double operatorshare) {
        this.operatorshare = operatorshare;
    }

    public Double getHubshare() {
        return hubshare;
    }

    public void setHubshare(Double hubshare) {
        this.hubshare = hubshare;
    }

    public Double getTax() {
        return tax;
    }

    public void setTax(Double tax) {
        this.tax = tax;
    }

    public DetailReportAlert () {}

}
