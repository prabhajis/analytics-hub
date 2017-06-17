package org.wso2telco.analytics.pricing.service.dao;

import com.google.gson.Gson;
import org.wso2telco.analytics.pricing.service.ChargeRate;
import org.wso2telco.analytics.pricing.service.RateCardService;

public class Main {

    public static void main(String[] args) {
        RateCardService rateCardService = new RateCardService();
        try {
            ChargeRate chargeRate = (ChargeRate) rateCardService.getRateByName("p1");
            Gson gson = new Gson();
            String g = gson.toJson(chargeRate);
            System.out.print(g);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
