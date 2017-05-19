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

    private String RequestRawID;
    private String api;
    private String api_version;
    private String version;

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
    private String apiPublisher;
    private String consumerKey;
    private String userId;
    private Integer response_count;
    private String requestId;
    private String operatorId;
    private String msisdn;
    private String operatorRef;
    private BigDecimal chargeAmount;
    private String purchaseCategoryCode;
    private Integer year;
    private Integer month;
    private Integer day;
    private Date reqtime;
    private String category;
    private String subcategory;
    private String merchant;

    public StreamRequestData(String RequestRawID, String api, String api_version, String version, String apiPublisher, String consumerKey, String userId, Integer response_count, String requestId, String operatorId, String msisdn, String operatorRef, BigDecimal chargeAmount, String purchaseCategoryCode, Integer year, Integer month, Integer day, Date reqtime) {
        this.RequestRawID = RequestRawID;
        this.api = api;
        this.api_version = api_version;
        this.version = version;
        this.apiPublisher = apiPublisher;
        this.consumerKey = consumerKey;
        this.userId = userId;
        this.response_count = response_count;
        this.requestId = requestId;
        this.operatorId = operatorId;
        this.msisdn = msisdn;
        this.operatorRef = operatorRef;
        this.chargeAmount = chargeAmount;
        this.purchaseCategoryCode = purchaseCategoryCode;
        this.year = year;
        this.month = month;
        this.day = day;
        this.reqtime = reqtime;
    }

    public String getRequestRawID() {
        return RequestRawID;
    }

    public void setRequestRawID(String RequestRawID) {
        this.RequestRawID = RequestRawID;
    }

    public String getApi() {
        return api;
    }

    public void setApi(String api) {
        this.api = api;
    }

    public String getApi_version() {
        return api_version;
    }

    public void setApi_version(String api_version) {
        this.api_version = api_version;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getApiPublisher() {
        return apiPublisher;
    }

    public void setApiPublisher(String apiPublisher) {
        this.apiPublisher = apiPublisher;
    }

    public String getConsumerKey() {
        return consumerKey;
    }

    public void setConsumerKey(String consumerKey) {
        this.consumerKey = consumerKey;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
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

    public String getPurchaseCategoryCode() {
        return purchaseCategoryCode;
    }

    public void setPurchaseCategoryCode(String purchaseCategoryCode) {
        this.purchaseCategoryCode = purchaseCategoryCode;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }

    public Date getReqtime() {
        return reqtime;
    }

    public void setReqtime(Date reqtime) {
        this.reqtime = reqtime;
    }
    
    

}
