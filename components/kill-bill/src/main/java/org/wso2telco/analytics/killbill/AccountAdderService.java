package org.wso2telco.analytics.killbill;

import org.json.JSONException;



public interface AccountAdderService {
	void generatePDFReport(String tableName, String query, String reportName, int maxLength, String
            reportType, String direction, String year, String month, boolean isServiceProvider, String loggedInUser,
            String billingInfo) throws JSONException;

}
