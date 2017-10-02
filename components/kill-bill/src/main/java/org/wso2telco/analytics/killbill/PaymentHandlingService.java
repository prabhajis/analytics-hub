package org.wso2telco.analytics.killbill;


public interface PaymentHandlingService {

	void genaratePayment(String loggedInUser, String amount);
	void addPaymentMethod(String loggedInUser,String token);
}
