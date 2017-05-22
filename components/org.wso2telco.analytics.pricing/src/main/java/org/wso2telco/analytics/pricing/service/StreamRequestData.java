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
import java.sql.Date;

/**
 *
 */
public class StreamRequestData {

    private String api;
    private String userId;
    private Integer applicationid;
    private Integer response_count;
    private String requestId;
    private String operatorId;
    private String operatorRef;
    private BigDecimal chargeAmount;    
    private Date reqtime;
    private String category;
    private String subcategory;
    private String merchant;
    
    private BigDecimal price; 
    private BigDecimal adscom; 
    private BigDecimal opcom; 
    private BigDecimal spcom; 
    private BigDecimal tax;
    private Integer count;
    
    
    public StreamRequestData(String api, String userId, Integer applicationid, Integer response_count, String requestId, String operatorId,
            String operatorRef, BigDecimal chargeAmount, Date reqtime, String category, String subcategory, String merchant) {
        
        this.api = api;
        this.userId = userId;
        this.applicationid = applicationid;
        this.response_count = response_count;
        this.requestId = requestId;
        this.operatorId = operatorId;
        this.operatorRef = operatorRef;
        this.chargeAmount = chargeAmount;
        this.reqtime = reqtime;
        this.category = category;
        this.subcategory = subcategory;
        this.merchant = merchant;
    }

    public String getApi() {
        return api;
    }

    public void setApi(String api) {
        this.api = api;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getApplicationid() {
        return applicationid;
    }

    public void setApplicationid(Integer applicationid) {
        this.applicationid = applicationid;
    }

    public Integer getResponse_count() {
        return response_count;
    }

    public void setResponse_count(Integer response_count) {
        this.response_count = response_count;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    public String getOperatorRef() {
        return operatorRef;
    }

    public void setOperatorRef(String operatorRef) {
        this.operatorRef = operatorRef;
    }

    public BigDecimal getChargeAmount() {
        return chargeAmount;
    }

    public void setChargeAmount(BigDecimal chargeAmount) {
        this.chargeAmount = chargeAmount;
    }

    public Date getReqtime() {
        return reqtime;
    }

    public void setReqtime(Date reqtime) {
        this.reqtime = reqtime;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSubcategory() {
        return subcategory;
    }

    public void setSubcategory(String subcategory) {
        this.subcategory = subcategory;
    }

    public String getMerchant() {
        return merchant;
    }

    public void setMerchant(String merchant) {
        this.merchant = merchant;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
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

    public BigDecimal getSpcom() {
        return spcom;
    }

    public void setSpcom(BigDecimal spcom) {
        this.spcom = spcom;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
    
    

    

}
