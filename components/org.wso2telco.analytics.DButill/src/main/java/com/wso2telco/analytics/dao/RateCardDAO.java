package com.wso2telco.analytics.dao;


import com.wso2telco.analytics.model.Commission;

public interface RateCardDAO {
    Commission getSBCommission(String api, String operatorId, String applicationId, String operation, String purchaseCategoryCode);

    Commission getSBCommissionDefaultCategory(String api, String operatorId, String applicationId, String operation, String purchaseCategoryCode);

    public Commission getNBCommission(String api, String operation, String applicationId, String purchaseCategoryCode);

    public Commission getNBCommissionDefaultCategory(String api, String operation, String applicationId, String purchaseCategoryCode);

    public Boolean getCategoryBasedValueNB(String api, String operation, String applicationId, String purchaseCategoryCode);

    public Boolean getCategoryBasedValueSB(String api, String operatorId, String operation, String applicationId);
}
