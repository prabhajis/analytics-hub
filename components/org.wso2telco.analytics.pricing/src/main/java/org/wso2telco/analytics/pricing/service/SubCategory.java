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
import java.util.List;
import java.util.Map;


public class SubCategory{
	private String name;
	private Double rate;
	private RateCommission subCategoryCommission;
	private Map<String, BigDecimal> subCategoryMap;//Sub category Name & Rate
	private List<UsageTiers> usageTiers;
	
	public List<UsageTiers> getUsageTiers() {
		return usageTiers;
	}
	public void setUsageTiers(List<UsageTiers> usageTiers) {
		this.usageTiers = usageTiers;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Double getRate() {
		return rate;
	}

	public void setRate(Double rate) {
		this.rate = rate;
	}

	public Map<String, BigDecimal> getSubCategoryMap() {
		return subCategoryMap;
	}

	public void setSubCategoryMap(Map<String, BigDecimal> subCategoryMap) {
		this.subCategoryMap = subCategoryMap;
	}

	public RateCommission getSubCategoryCommission() {
		return subCategoryCommission;
	}

	public void setSubCategoryCommission(RateCommission subCategoryCommission) {
		this.subCategoryCommission = subCategoryCommission;
	}
	
}