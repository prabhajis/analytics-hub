package org.wso2telco.analytics.pricing.service.dao;

import java.util.Date;
import org.wso2telco.analytics.pricing.AnalyticsPricingException;

public interface RateCardDAO {

    Object getNBRateCard (String operationId, String applicationId, String api, String category, String subCategory) throws Exception ;

    Object getSBRateCard (String operator, String operation, String applicationId, String category, String subCategory) throws Exception;

    double getValidTaxRate (String taxCode, String taxDate) throws Exception;
}
