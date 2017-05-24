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

import java.util.Locale;


/**
 * <TO-DO> <code>CategoryCharge</code>
 * @version $Id: CategoryCharge.java,v 1.00.000
 */
public class CategoryCharge {

    private String operationId;
    private String category;
    private String subcategory;
    //private BilledCharge billedcharge;
        
    public CategoryCharge(String operationId, String category, String subcategory) {
        this.category = category;
        this.subcategory = subcategory;
        this.operationId = operationId;
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

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }
    
   
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CategoryCharge categoryKey = (CategoryCharge) o;

        if (category != null ? !category.equalsIgnoreCase(categoryKey.category) : categoryKey.category != null) return false;
        if (subcategory != null ? !subcategory.equalsIgnoreCase(categoryKey.subcategory) : categoryKey.subcategory != null) return false;
        if (operationId != null ? operationId != categoryKey.operationId : categoryKey.operationId != null) return false;        
        return true;
    }

    @Override
    public int hashCode() {
        int result = category != null ? category.toLowerCase(Locale.ENGLISH).hashCode() : 0;
        result = 31 * result + (subcategory != null ? subcategory.toLowerCase(Locale.ENGLISH).hashCode() : 0);
        result = 31 * result + (operationId != null ? operationId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "categoryKey{" +
                "operator='" + category + '\'' +
                ", apiName='" + subcategory + '\'' +                
                ", operationId='" + operationId + '\'' +                
                '}';
    }
    
}
