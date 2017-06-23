package org.wso2telco.analytics.pricing.service.dao;

import com.google.gson.Gson;
import org.wso2telco.analytics.pricing.service.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        RateCardService rateCardService = new RateCardService();
        /*try {
            ChargeRate chargeRate = (ChargeRate) rateCardService.getRateByName("p1");
            Gson gson = new Gson();
            String g = gson.toJson(chargeRate);
            System.out.print(g);
        } catch (Exception e) {
            e.printStackTrace();
        }*/
//TODO: validate required fields from json when receive them
        //categories
        RateCommission rateCommission = new RateCommission();
        rateCommission.setSpCommission(new BigDecimal(80));
        rateCommission.setOpcoCommission(new BigDecimal(5));
        rateCommission.setAdsCommission(new BigDecimal(15));

        HashMap<String,Object> categoryMap = new HashMap<>();
        HashMap<String,Object> subcategoryMap = new HashMap<>();

        RateCommission rateCommission1 = new RateCommission();
        rateCommission1.setSpCommission(new BigDecimal(81));
        rateCommission1.setAdsCommission(new BigDecimal(16));
        rateCommission1.setOpcoCommission(new BigDecimal(6));
        subcategoryMap.put("_default_", rateCommission1);

        RateCommission rateCommission2 = new RateCommission();
        rateCommission2.setSpCommission(new BigDecimal(82));
        rateCommission2.setAdsCommission(new BigDecimal(12));
        rateCommission2.setOpcoCommission(new BigDecimal(2));
        subcategoryMap.put("121",rateCommission2);

        categoryMap.put("111",subcategoryMap);

        ChargeRate chargeRate = new ChargeRate("bar");
        chargeRate.setCurrency("Rs");
        chargeRate.setValue(new BigDecimal(20));
        chargeRate.setType(RateType.CONSTANT);
        chargeRate.setCategoryBasedVal(true);
        List<String> list = new ArrayList<String>();
        list.add("VAT");
        list.add("ABC");
        chargeRate.setTaxList(list);
        chargeRate.setCategories(categoryMap);
        chargeRate.setCommission(rateCommission);


        RateCardDAOImpl rd = new RateCardDAOImpl();
        try {
            rd.insertRateCard(chargeRate);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}