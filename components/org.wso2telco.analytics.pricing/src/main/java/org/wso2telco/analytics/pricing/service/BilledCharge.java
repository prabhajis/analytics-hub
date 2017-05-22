
/*******************************************************************************
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
 ******************************************************************************/

package org.wso2telco.analytics.pricing.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * <TO-DO> <code>BilledCharge</code>
 * @version $Id: BilledCharge.java,v 1.00.000
 */
public class BilledCharge {

    private int count;
    
    private BigDecimal price = BigDecimal.ZERO;
    private BigDecimal spcom = BigDecimal.ZERO;
    private BigDecimal adscom = BigDecimal.ZERO;
    private BigDecimal opcom = BigDecimal.ZERO;
    private BigDecimal tax = BigDecimal.ZERO;
    
    //Map<String,BilledCharge> merchantCharges = new HashMap<String,BilledCharge>();
    Map<String,BilledCharge> tierCharges = new HashMap<String,BilledCharge>();

    public BilledCharge(int count) {
        this.count = count;
    }   
    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getSpcom() {
        return spcom;
    }

    public void setSpcom(BigDecimal spcom) {
        this.spcom = spcom;
    }

    public BigDecimal getAdscom() {
        return adscom;
    }

    public void setAdscom(BigDecimal adscom) {
        this.adscom = adscom;
    }

    public BigDecimal getOpcom() {
        return opcom;
    }

    public void setOpcom(BigDecimal opcom) {
        this.opcom = opcom;
    }  

    public BigDecimal getTax() {
        return tax;
    }

    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }
    
    public void addAdscom(BigDecimal adscom) {
        this.adscom = this.adscom.add(adscom);
    }
    
    public void addOpcom(BigDecimal opcom) {
        this.opcom = this.opcom.add(opcom);
    }
    
    public void addSpcom(BigDecimal spcom) {
        this.spcom = this.spcom.add(spcom);
    }
    
    public void addPrice(BigDecimal price) {
        this.price = this.price.add(price);
    }
    
    public void addTax(BigDecimal tax) {
        this.tax = this.tax.add(tax);
    }
    
    public void addCount(int count) {
        this.count = this.count + count;
    }
    
    public Map<String, BilledCharge> getTierCharges() {
            return tierCharges;
        }

    public void setTierCharges(Map<String, BilledCharge> tierCharges) {
            this.tierCharges = tierCharges;
    }

    @Override
    public String toString() {
        return "BilledCharge{" + "price=" + price + ", spcom=" + spcom + ", adscom=" + adscom + ", opcom=" + opcom + ", tax=" + tax + '}';
    }
    
    
}
