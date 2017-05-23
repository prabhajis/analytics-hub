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

/**
 *
 */
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


/**
 * This class represents the Rate used for charging
 */
public class ChargeRate {

    private String name;                        // filled
    private String currency;                   //filled
    private BigDecimal value;                   //filled
    private RateType type;                      //filled
    private Map<String, Object> categories;
    private List<String> taxList;
    private boolean isDefault = false;              //filled
    private Map<String, String> rateAttributes;
    private List<RateRange> rateRanges;
    private RateCommission commission;              // filled
    private Boolean categoryBasedVal = false;       // filled
    private List<UsageTiers> usageTiers;
    private RefundEntity RefundList;
    private SurchargeEntity surchargeEntity;
    private List<CategoryEntity> categoryEntityList;
    
    
    public SurchargeEntity getSurchargeEntity() {
		return surchargeEntity;
	}

	public void setSurchargeEntity(SurchargeEntity surchargeEntity) {
		this.surchargeEntity = surchargeEntity;
	}

	public Boolean getCategoryBasedVal() {
		return categoryBasedVal;
	}

	public void setCategoryBasedVal(Boolean categoryBasedVal) {
		this.categoryBasedVal = categoryBasedVal;
	}

	public List<UsageTiers> getUsageTiers() {
		return usageTiers;
	}

	public RateCommission getCommission() {
		return commission;
	}

	public void setCommission(RateCommission commission) {
		this.commission = commission;
	}



	public RefundEntity getRefundList() {
		return RefundList;
	}

	public void setRefundList(RefundEntity refundList) {
		RefundList = refundList;
	}

	public void setUsageTiers(List<UsageTiers> tiersEntities) {
		this.usageTiers = tiersEntities;
	}

	public void setName(String name) {
		this.name = name;
	}

	

    public ChargeRate(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public RateType getType() {
        return type;
    }

    public void setType(RateType type) {
        this.type = type;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public Map<String, String> getRateAttributes() {
        return rateAttributes;
    }

    public void setRateAttributes(Map<String, String> rateAttributes) {
        this.rateAttributes = rateAttributes;
    }

    public List<RateRange> getRateRanges() {
		return rateRanges;
	}

	public void setRateRanges(List<RateRange> rateRanges) {
		this.rateRanges = rateRanges;
	}

	public List<String> getTaxList() { return taxList; }

    public void setTaxList(List<String> taxList) { this.taxList = taxList; }

    public Map<String, Object> getCategories() {
        return categories;
    }

    public void setCategories(Map<String, Object> categoryEntityMap) {
        this.categories = categoryEntityMap;
    }
    
    @Override
    public String toString() {
        return "ChargeRate{" +
                "name='" + name + '\'' +
                ", currency='" + currency + '\'' +
                ", value=" + value +
                ", type=" + type +
                ", isDefault=" + isDefault +
                ", rateAttributes=" + rateAttributes +
                ", taxList=" + taxList +
                '}';
    }

	public List<CategoryEntity> getCategoryEntityList() {
		return categoryEntityList;
	}

	public void setCategoryEntityList(List<CategoryEntity> categoryEntityList) {
		this.categoryEntityList = categoryEntityList;
	}


}