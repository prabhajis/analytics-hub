/**
 * *****************************************************************************
 * Copyright  (c) 2015-2016, WSO2.Telco Inc. (http://www.wso2telco.com) All Rights Reserved.
 *
 * WSO2.Telco Inc. licences this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *****************************************************************************
 */
package org.wso2telco.analytics.pricing.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.wso2telco.analytics.pricing.AnalyticsPricingException;
import org.wso2telco.analytics.pricing.Tax;
import org.wso2telco.analytics.pricing.service.dao.RateCardDAO;

/**
 *
 */
public class PriceServiceImpl implements IPriceService {

    @Override
    public void priceNorthBoundRequest(StreamRequestData reqdata, Map.Entry<CategoryCharge, BilledCharge> categoryEntry) {

         //Sample stream data
        reqdata = new StreamRequestData("payment", "admin", 12, 1, "1448128113683PA8602", "01", "DLG2-1448128113683",
                new BigDecimal(200), new java.sql.Date(2017, 5, 19), "", "", "25");
        
      
        RateCardService rateCardservice = new RateCardService();
        ChargeRate chargeRate = (ChargeRate)rateCardservice.getNBRateCard(reqdata.getOperatorId(), String.valueOf(reqdata.getApplicationid()),
                reqdata.getApi(), reqdata.getCategory(), reqdata.getSubcategory());

        /*ChargeRate chargeRate = new ChargeRate("SM1");
        chargeRate.setCurrency("LKR");
        chargeRate.setValue(new BigDecimal(100));
        chargeRate.setType(RateType.PERCENTAGE);
        chargeRate.setDefault(true);
        chargeRate.setCategoryBasedVal(true);//<CategorBase>        
        RateCommission cc = new RateCommission();
        Double spPercentage = 20.00;
        cc.setSpCommission(new BigDecimal(spPercentage));
        Double adsPercentage = 30.00;
        cc.setAdsCommission(new BigDecimal(adsPercentage));
        Double opcoPercentage = 50.00;
        cc.setOpcoCommission(new BigDecimal(opcoPercentage));
        chargeRate.setCommission(cc);//<Commission>     
*/
  
        BilledCharge billcharge = new BilledCharge(0);
        billcharge.setAdscom(BigDecimal.ZERO);
        billcharge.setOpcom(BigDecimal.ZERO);
        billcharge.setSpcom(BigDecimal.ZERO);
        billcharge.setCount(100);
        billcharge.addTax(BigDecimal.TEN);

        Map<CategoryCharge, BilledCharge> apiCount = new HashMap<CategoryCharge, BilledCharge>();
        CategoryCharge categoryCharge = null;
        categoryCharge = new CategoryCharge(200, "", "");
        apiCount.put(categoryCharge, billcharge);

        //Tax list
        List<Tax> taxList = new ArrayList<Tax>();

        try {

            ComponentPricing.priceComponent(chargeRate, apiCount.entrySet().iterator().next(), taxList, reqdata);
            BilledCharge billed = (BilledCharge) apiCount.entrySet().iterator().next().getValue();
            System.out.println(billed);

        } catch (AnalyticsPricingException ex) {
            Logger.getLogger(PriceServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public void priceSouthBoundRequest(StreamRequestData reqdata, Map.Entry<CategoryCharge, BilledCharge> categoryEntry) {

    }

}
