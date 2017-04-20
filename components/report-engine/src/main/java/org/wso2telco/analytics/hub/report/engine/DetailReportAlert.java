package org.wso2telco.analytics.hub.report.engine;


public class DetailReportAlert {


    private String api;
    private String applicationName;
    private String eventType;
    private String operatorName;
    private String purchaseCategoryCode;
    private Double sum_totalAmount;
    private Double spcommission;
    private Double revShare_sp;

    public String getApi() {
        return api;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getEventType() {
        return eventType;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public String getPurchaseCategoryCode() {
        return purchaseCategoryCode;
    }

    public Double getSum_totalAmount() {
        return sum_totalAmount;
    }

    public void setApi(String api) {
        this.api = api;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public void setPurchaseCategoryCode(String purchaseCategoryCode) {
        this.purchaseCategoryCode = purchaseCategoryCode;
    }

    public void setSum_totalAmount(Double sum_totalAmount) {
        this.sum_totalAmount = sum_totalAmount;
    }

    public Double getSpcommission() {
        return spcommission;
    }

    public void setSpcommission(Double spcommission) {
        this.spcommission = spcommission;
    }

    public Double getRevShare_sp() {
        return revShare_sp;
    }

    public void setRevShare_sp(Double revShare_sp) {
        this.revShare_sp = revShare_sp;
    }

    public DetailReportAlert () {}


}
