package org.wso2telco.analytics.pricing.service.dao;

public interface RateCardDAO {

    Object getNBRateCard (String operation, String applicationId, String category, String subCategory);

    Object getSBRateCard (String operator, String operation, String applicationId, String category, String subCategory);
}
