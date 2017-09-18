package org.wso2telco.analytics.sparkUdf;

import org.killbill.billing.client.KillBillClient;
import org.killbill.billing.client.KillBillHttpClient;
import org.killbill.billing.client.model.Account;
import org.wso2telco.analytics.sparkUdf.configProviders.ConfigurationDataProvider;
import java.util.UUID;

public class AccountAdder {

	private static ConfigurationDataProvider dataProvider=null;
	private static KillBillHttpClient killBillHttpClient;
	private static KillBillClient killBillClient;
	

	

	@SuppressWarnings("deprecation")
	public String addAccount(String user,String reason,String comment,String name,String currency,String externalKey,int nameL){
		Account account=null;
		try {
			ConfigurationDataProvider dataProvider=ConfigurationDataProvider.getInstance();

			killBillHttpClient= new KillBillHttpClient(dataProvider.getUrl(),
					dataProvider.getUname(),
					dataProvider.getPassword(),
					dataProvider.getApiKey(),
					dataProvider.getApiSecret());




			killBillClient = new KillBillClient(killBillHttpClient);

			// Create an account
			account = new Account();
			account.setName(name);
			account.setFirstNameLength(nameL);
			account.setExternalKey(externalKey);
			account.setCurrency(currency);
			account = killBillClient.createAccount(account, user, reason, comment);
		}catch (Exception e) {
			return "Did not created";
		}finally{
			if (killBillClient!=null) {
				killBillClient.close();
			}
			if (killBillHttpClient!=null) {
				killBillHttpClient.close();
			}
		}


		return account.getAccountId().toString();
	}
	
	
	
	@SuppressWarnings("deprecation")
	public String addSubAccount(String perentAcountId, String user,String reason,String comment,String name,String currency,String externalKey,int nameL){
		Account account=null;
		try {
			dataProvider=ConfigurationDataProvider.getInstance();

			killBillHttpClient= new KillBillHttpClient(dataProvider.getUrl(),
					dataProvider.getUname(),
					dataProvider.getPassword(),
					dataProvider.getApiKey(),
					dataProvider.getApiSecret());

			killBillClient = new KillBillClient(killBillHttpClient);
			// Create an account
			account = new Account();
			account.setName(name);
			account.setFirstNameLength(nameL);
			account.setExternalKey(externalKey);
			account.setCurrency(currency);
			//account.setIsPaymentDelegatedToParent(true);
			account.setParentAccountId(UUID.fromString(perentAcountId));
			account = killBillClient.createAccount(account, user, reason, comment);
		}catch (Exception e) {
			return "Child did not created";
		}finally{
			if (killBillClient!=null) {
				killBillClient.close();
			}
			if (killBillHttpClient!=null) {
				killBillHttpClient.close();
			}
		}

		return account.getAccountId().toString();
	}



}
