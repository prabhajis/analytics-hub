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

/**
 *
 */
public class Tariff {
    
    private BigDecimal value;
    private BigDecimal spCommission;
    private BigDecimal adsCommission;
    private BigDecimal opcoCommission;
    private BigDecimal surchargeElementValue;
    private BigDecimal surchargeElementAds;
    private BigDecimal surchargeElementOpco;
    private Integer tariffmaxcount;
    private BigDecimal excessRate;
    private BigDecimal defaultRate;

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public BigDecimal getSpCommission() {
        return spCommission;
    }

    public void setSpCommission(BigDecimal spCommission) {
        this.spCommission = spCommission;
    }

    public BigDecimal getAdsCommission() {
        return adsCommission;
    }

    public void setAdsCommission(BigDecimal adsCommission) {
        this.adsCommission = adsCommission;
    }

    public BigDecimal getOpcoCommission() {
        return opcoCommission;
    }

    public void setOpcoCommission(BigDecimal opcoCommission) {
        this.opcoCommission = opcoCommission;
    }

    public BigDecimal getSurchargeElementValue() {
        return surchargeElementValue;
    }

    public void setSurchargeElementValue(BigDecimal surchargeElementValue) {
        this.surchargeElementValue = surchargeElementValue;
    }

    public BigDecimal getSurchargeElementAds() {
        return surchargeElementAds;
    }

    public void setSurchargeElementAds(BigDecimal surchargeElementAds) {
        this.surchargeElementAds = surchargeElementAds;
    }

    public BigDecimal getSurchargeElementOpco() {
        return surchargeElementOpco;
    }

    public void setSurchargeElementOpco(BigDecimal surchargeElementOpco) {
        this.surchargeElementOpco = surchargeElementOpco;
    }

    public Integer getTariffmaxcount() {
        return tariffmaxcount;
    }

    public void setTariffmaxcount(Integer tariffmaxcount) {
        this.tariffmaxcount = tariffmaxcount;
    }

    public BigDecimal getExcessRate() {
        return excessRate;
    }

    public void setExcessRate(BigDecimal excessRate) {
        this.excessRate = excessRate;
    }

    public BigDecimal getDefaultRate() {
        return defaultRate;
    }

    public void setDefaultRate(BigDecimal defaultRate) {
        this.defaultRate = defaultRate;
    }
  
}