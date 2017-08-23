/*
 * Copyright (c) 2016, WSO2.Telco Inc. ((http://www.wso2telco.com)) All Rights Reserved.
 *
 * WSO2.Telco Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2telco.analytics.killbill.internal.ds;


import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataService;
import org.wso2telco.analytics.killbill.AccountAdderService;
import org.wso2telco.analytics.killbill.internal.AccountAdder;


/**
 * This class represents the analytics data service declarative services component.
 * @scr.component name="org.wso2telco.analytics.killbill.AccountAdderService" immediate="true"
 * @scr.reference name="AnalyticsDataService.service" interface="org.wso2.carbon.analytics.dataservice.core.AnalyticsDataService"
 * cardinality="1..1" policy="dynamic"  bind="setAnalyticsDataService" unbind="unsetAnalyticsDataService"
 */
public class AccountAdderServiceDS {



	protected void activate(ComponentContext ctx) {

		try {
			AccountAdderService accountAdderService = new AccountAdder();
			ctx.getBundleContext().registerService(AccountAdderService.class.getName(),
					accountAdderService, null);
			

		} catch (Exception e) {
			System.out.println("ddddd"+e.getMessage());
		}



	}

	protected void deactivate(ComponentContext ctx) {
		try {

		} catch (Exception e) {

		}
	}

	protected void setAnalyticsDataService(AnalyticsDataService analyticsDataService) {
		AccountAdderServiceHolder.setAnalyticsDataService(analyticsDataService);
	}

	protected void unsetAnalyticsDataService(AnalyticsDataService analyticsDataService) {
		AccountAdderServiceHolder.setAnalyticsDataService(null);
	}
}
