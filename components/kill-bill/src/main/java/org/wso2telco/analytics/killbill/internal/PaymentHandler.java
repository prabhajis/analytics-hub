package org.wso2telco.analytics.killbill.internal;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.killbill.billing.client.KillBillClient;
import org.killbill.billing.client.KillBillHttpClient;
import org.killbill.billing.client.RequestOptions;
import org.killbill.billing.client.model.Account;
import org.killbill.billing.client.model.Payment;
import org.killbill.billing.client.model.PaymentMethod;
import org.killbill.billing.client.model.PaymentMethodPluginDetail;
import org.killbill.billing.client.model.PaymentTransaction;
import org.killbill.billing.client.model.PluginProperty;
import org.killbill.billing.payment.api.TransactionStatus;
import org.killbill.billing.payment.api.TransactionType;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDataResponse;
import org.wso2.carbon.analytics.dataservice.commons.SearchResultEntry;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataServiceUtils;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2telco.analytics.killbill.PaymentHandlingService;
import org.wso2telco.analytics.killbill.internal.ds.AccountAdderServiceHolder;




public class PaymentHandler implements PaymentHandlingService{



	private static KillBillHttpClient killBillHttpClient;
	private static KillBillClient killBillClient;


	@Override
	public void genaratePayment(String loggedInUser, String amount) {

		try {
			ConfigurationDataProvider dataProvider=ConfigurationDataProvider.getInstance();

			killBillHttpClient= new KillBillHttpClient(dataProvider.getUrl(),
					dataProvider.getUname(),
					dataProvider.getPassword(),
					dataProvider.getApiKey(),
					dataProvider.getApiSecret());
			killBillClient = new KillBillClient(killBillHttpClient);

			final PaymentTransaction authTransaction = new PaymentTransaction();
			authTransaction.setAmount(new BigDecimal(amount));
			String username[]=loggedInUser.split("@");
			String killbillAccount=getKillBillAccount(-1234,username[0]);
			Account account=killBillClient.getAccount(UUID.fromString(killbillAccount));
			
			authTransaction.setCurrency(account.getCurrency());
			authTransaction.setTransactionType(TransactionType.AUTHORIZE.name());
			RequestOptions requestOptions = RequestOptions.builder()
					.withCreatedBy("admin")
					.withReason("payment")
					.withComment("payment")
					.build();

			final Payment payment = killBillClient.createPayment(account.getAccountId(), account.getPaymentMethodId(), authTransaction, requestOptions);
			final PaymentTransaction paymentTransaction = payment.getTransactions().get(0);
			
			if(paymentTransaction.getStatus().equals(TransactionStatus.SUCCESS.toString())){
				
				killBillClient.payAllInvoices(account.getAccountId(), true, new BigDecimal(amount), "admin", "payment", "payment");
				
			}else{
				throw new Exception();
			}
			
		} catch (Exception e) {
			// TODO: handle exception
		}

	}


	@Override
	public void addPaymentMethod(String loggedInUser,String token) {

		try {
			ConfigurationDataProvider dataProvider=ConfigurationDataProvider.getInstance();

			killBillHttpClient= new KillBillHttpClient(dataProvider.getUrl(),
					dataProvider.getUname(),
					dataProvider.getPassword(),
					dataProvider.getApiKey(),
					dataProvider.getApiSecret());
			killBillClient = new KillBillClient(killBillHttpClient);
			String username[]=loggedInUser.split("@");
			String killbillAccount=getKillBillAccount(-1234,username[0]);


			final PaymentMethodPluginDetail info = new PaymentMethodPluginDetail();
			List<PluginProperty> list=new ArrayList<PluginProperty>();
			list.add(new PluginProperty("token",token,true));
			info.setProperties(list);
			info.setIsDefaultPaymentMethod(true);

			final PaymentMethod paymentMethod = new PaymentMethod(null, UUID.randomUUID().toString(), UUID.fromString(killbillAccount), true, "killbill-stripe", info);

			RequestOptions requestOptions = RequestOptions.builder()
					.withCreatedBy("admin")
					.withReason("payment")
					.withComment("payment")
					.build();
			killBillClient.createPaymentMethod(paymentMethod, requestOptions);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}


	private String getKillBillAccount(int tenantId, String username) throws AnalyticsException {
		String killBillAccountQuery = "accountName:\"" + username + "\"";
		List<SearchResultEntry> killbillAccountsSearchResult = AccountAdderServiceHolder.getAnalyticsDataService()
				.search(tenantId, "ORG_WSO2TELCO_ANALYTICS_HUB_STREAM_KILLBILL_SP_ACCOUNT", killBillAccountQuery, 0, 1);

		if (killbillAccountsSearchResult.isEmpty()) {
			throw new AnalyticsException("Could not find a kill bill account for " + username);
		}
		List<String> killBillSearchIds = killbillAccountsSearchResult.stream().map(SearchResultEntry::getId).collect(Collectors.toList());

		AnalyticsDataResponse killBillAccountResponse = AccountAdderServiceHolder.getAnalyticsDataService().get(tenantId,
				"ORG_WSO2TELCO_ANALYTICS_HUB_STREAM_KILLBILL_SP_ACCOUNT", 1, null, killBillSearchIds);

		List<Record> killBillRecords = AnalyticsDataServiceUtils
				.listRecords(AccountAdderServiceHolder.getAnalyticsDataService(), killBillAccountResponse);

		return (String) killBillRecords.get(0).getValue("killBillAID");
	}	

}
