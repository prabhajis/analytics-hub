package com.wso2telco.analytics;

import com.wso2telco.analytics.dao.RateCardDAO;
import com.wso2telco.analytics.model.Commission;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RateCardService {

    public static Boolean isCategoryBasedNB(String api, String operation, String applicationId, String purchaseCategoryCode){
        RateCardDAO rateCardDAO = new RateCardDAOImpl();
        Boolean isCatagoryBased = rateCardDAO.getCategoryBasedValueNB(api,  applicationId,operation, purchaseCategoryCode);

        return isCatagoryBased;
    }


    public static Boolean isCategoryBasedSB(String api, String operatorId, String operation, String applicationId){
        RateCardDAO rateCardDAO = new RateCardDAOImpl();
        Boolean isCatagoryBased = rateCardDAO.getCategoryBasedValueSB(api, operatorId, applicationId, operation);

        return isCatagoryBased;
    }

    public static Double getNBCommissionSP(String api, String operation, String applicationId, String purchaseCategoryCode){
        RateCardDAO rateCardDAO = new RateCardDAOImpl();
        Commission comm;

        if(isCategoryBasedNB(api,operation,applicationId,purchaseCategoryCode)) {
            comm = rateCardDAO.getNBCommission(api, applicationId,operation,  purchaseCategoryCode);
        }else{
            comm = rateCardDAO.getNBCommissionDefaultCategory(api, applicationId, operation,  purchaseCategoryCode);
        }

        return comm.getSp();
    }

    public static Double getNBCommissionOpco(String api, String operation, String applicationId, String purchaseCategoryCode){
        RateCardDAO rateCardDAO = new RateCardDAOImpl();
        Commission comm;

        if(isCategoryBasedNB(api,operation,applicationId,purchaseCategoryCode)) {
            comm = rateCardDAO.getNBCommission(api,  applicationId,operation, purchaseCategoryCode);
        }else{
            comm = rateCardDAO.getNBCommissionDefaultCategory(api, applicationId,operation,  purchaseCategoryCode);
        }

        return comm.getOpco();
    }

    public static Double getNBCommissionHub(String api, String operation, String applicationId, String purchaseCategoryCode){
        RateCardDAO rateCardDAO = new RateCardDAOImpl();
        Commission comm;

        if(isCategoryBasedNB(api,operation,applicationId,purchaseCategoryCode)) {
            comm = rateCardDAO.getNBCommission(api, applicationId,operation,  purchaseCategoryCode);
        }else{
            comm = rateCardDAO.getNBCommissionDefaultCategory(api,  applicationId,operation, purchaseCategoryCode);
        }

        return comm.getAds();
    }


    public Double getSBCommissionSP(String api, String operatorId, String applicationId, String operation, String purchaseCategoryCode){
        RateCardDAO rateCardDAO = new RateCardDAOImpl();
        Commission comm;

        if (isCategoryBasedSB(api, operatorId, operation, applicationId)) {
            comm = rateCardDAO.getSBCommission(api,operatorId,applicationId,operation,purchaseCategoryCode);
        } else {
            comm = rateCardDAO.getSBCommissionDefaultCategory(api, operatorId,  applicationId, operation,  purchaseCategoryCode);
        }

        return comm.getSp();
    }

    public Double getSBCommissionOpco(String api, String operatorId, String applicationId, String operation, String purchaseCategoryCode){
        RateCardDAO rateCardDAO = new RateCardDAOImpl();
        Commission comm;

        if (isCategoryBasedSB(api, operatorId, operation, applicationId)) {
            comm = rateCardDAO.getSBCommission(api,operatorId,applicationId,operation,purchaseCategoryCode);
        } else {
            comm = rateCardDAO.getSBCommissionDefaultCategory(api, operatorId,  applicationId,operation, purchaseCategoryCode);
        }

        return comm.getOpco();
    }

    public Double getSBCommissionHub(String api, String operatorId, String applicationId, String operation, String purchaseCategoryCode){
        RateCardDAO rateCardDAO = new RateCardDAOImpl();
        Commission comm;

        if (isCategoryBasedSB(api, operatorId, operation, applicationId)) {
            comm = rateCardDAO.getSBCommission(api,operatorId,  applicationId,operation, purchaseCategoryCode);
        } else {
            comm = rateCardDAO.getSBCommissionDefaultCategory(api, operatorId,  applicationId, operation,  purchaseCategoryCode);
        }

        return comm.getAds();
    }

}
