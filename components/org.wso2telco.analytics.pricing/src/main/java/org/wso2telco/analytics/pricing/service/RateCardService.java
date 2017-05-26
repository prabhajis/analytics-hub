package org.wso2telco.analytics.pricing.service;

import org.wso2telco.analytics.pricing.Tax;
import org.wso2telco.analytics.pricing.service.dao.RateCardDAO;

import java.util.List;

public class RateCardService {

    public Object getNBRateCard(String operationId, String applicationId, String api,String category, String subCategory) throws Exception {
        RateCardDAO rateCardDAO = new RateCardDAOImpl();
        ChargeRate chargeRate = (ChargeRate) rateCardDAO.getNBRateCard(operationId, applicationId, api, category, subCategory);

        return chargeRate;
    }

    public Object getSBRateCard(String operatorId, String operationId, String applicationId, String api, String category, String subCategory) throws Exception {
        RateCardDAO rateCardDAO = new RateCardDAOImpl();
        ChargeRate chargeRate = (ChargeRate) rateCardDAO.getSBRateCard(operatorId, operationId, applicationId, api, category, subCategory);

        return chargeRate;
    }

    public List<Tax> getValidTaxRate(List<Tax> taxes, String taxDate) throws Exception {
        RateCardDAO rateCardDAO = new RateCardDAOImpl();
        List<Tax> validTaxVal = rateCardDAO.getValidTaxRate(taxes, taxDate);

        return validTaxVal;
    }
}
