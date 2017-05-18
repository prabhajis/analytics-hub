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
 * The type of rate.
 */
public enum RateType {
    /**
     * A constant charge for the charging period.
     */
    CONSTANT("CONSTANT"),
    /**
     * A percentage of the amount applied as the charge. Used for Payment API.
     */
    PERCENTAGE("PERCENTAGE"),
    /**
     * Rate applied per request.
     */
    PER_REQUEST("PER_REQUEST"),
    /**
     * A constant charge upto the quota value and a different rate for the excess count.
     */
    QUOTA("QUOTA"),
    /**
     * Charged based on the no. of unique subscribers for the API. eg: LBS
     */
    SUBSCRIPTION("SUBSCRIPTION"),
    /**
     * Charged based on the range. eg: 5000-600, 6001-7000
     */
    RANGE("RANGE"),
    /**
     * Charged based on the min-max. eg: 5000-600, 6001-7000
     */
    MULTITIER("MULTITIER");

    private String name;

    RateType(String name) { this.name = name; }

    public String getName() { return name; }

    public static RateType getEnum(String name) {
        for (RateType r : RateType.values()) {
            if ((r.name).equalsIgnoreCase(name)) {
                return r;
            }
        }
        return null;
    }
}
