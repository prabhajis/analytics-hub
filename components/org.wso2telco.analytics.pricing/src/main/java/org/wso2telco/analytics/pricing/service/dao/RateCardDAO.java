package org.wso2telco.analytics.pricing.service.dao;

import com.wso2telco.analytics.exception.DBUtilException;
        import java.sql.Date;
        import java.util.List;
        import org.wso2telco.analytics.pricing.AnalyticsPricingException;
        import org.wso2telco.analytics.pricing.Tax;
        import org.wso2telco.analytics.pricing.service.ChargeRate;

public interface RateCardDAO {

    Object getNBRateCard (String operationId, String applicationId, String api, String category, String subCategory) throws AnalyticsPricingException,DBUtilException ;

    Object getSBRateCard (String operator, String operation, String applicationId,String api, String category, String subCategory) throws AnalyticsPricingException,DBUtilException;

    List<Tax> getValidTaxRate (List<String> taxCode, java.sql.Date taxDate) throws AnalyticsPricingException,DBUtilException;

    void insertRateCard (ChargeRate chargeRate) throws AnalyticsPricingException,DBUtilException;
}