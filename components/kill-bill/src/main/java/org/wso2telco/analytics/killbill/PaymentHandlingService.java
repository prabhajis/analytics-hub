package org.wso2telco.analytics.killbill;


public interface PaymentHandlingService {

	String genaratePayment(String loggedInUser, String amount);
	String addPaymentMethod(String loggedInUser,String token);
	Boolean validateUser(String user,String pwd);
	Double getCurrentAmount(String sp);
}
