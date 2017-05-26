package org.wso2telco.analytics.pricing.service.dao;

import java.util.Date;
import java.util.List;

import org.wso2telco.analytics.pricing.AnalyticsPricingException;
import org.wso2telco.analytics.pricing.Tax;

public interface RateCardDAO {

    Object getNBRateCard (String operationId, String applicationId, String api, String category, String subCategory) throws Exception ;

    Object getSBRateCard (String operatorId, String operationId, String applicationId, String api, String category, String subCategory) throws Exception;

    List<Tax> getValidTaxRate (List<Tax> taxes, String taxDate) throws Exception;
}
