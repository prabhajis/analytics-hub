package org.wso2telco.analytics.killbill.internal;


import java.math.BigDecimal;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.killbill.billing.client.KillBillClient;
import org.killbill.billing.client.KillBillHttpClient;
import org.killbill.billing.client.RequestOptions;
import org.killbill.billing.client.model.Account;
import org.killbill.billing.client.model.Invoice;
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
import org.wso2telco.analytics.killbill.internal.exceptions.KillBillError;
import org.wso2telco.analytics.sparkUdf.exception.KillBillException;
import org.wso2telco.analytics.sparkUdf.service.InvoiceService;



public class PaymentHandler implements PaymentHandlingService{



	private static KillBillHttpClient killBillHttpClient;
	private static KillBillClient killBillClient;


	@Override
	public String genaratePayment(String loggedInUser, String amount) {

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
				RequestOptions requestOptionsForBillUpdate = RequestOptions.builder()
						.withCreatedBy("admin")
						.withReason("payment")
						.withComment("payment")
						.build();

				killBillClient.payAllInvoices(account.getAccountId(), false, new BigDecimal(amount),requestOptionsForBillUpdate);

			}else{
				throw new KillBillError();
			}

		} catch (Exception e) {
			return "Error";
		}finally{
			if (killBillClient!=null) {
				killBillClient.close();
			}
			if (killBillHttpClient!=null) {
				killBillHttpClient.close();
			}
		}
		return "success";

	}


	@Override
	public String addPaymentMethod(String loggedInUser,String token) {

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
			return "Error";
		}finally{
			if (killBillClient!=null) {
				killBillClient.close();
			}
			if (killBillHttpClient!=null) {
				killBillHttpClient.close();
			}
		}
		return "success";

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


	@Override
	public Boolean validateUser(String user, String pwd) {

		ConfigurationDataProvider configurationDataProvider=ConfigurationDataProvider.getInstance();

		if(configurationDataProvider.getHubUser().equals(user) && configurationDataProvider.getHubPassword().equals(pwd)){
			return true;
		}else{
			return false;
		}
	}	
	
	private double getCurrentMonthAmount(String sp) throws AnalyticsException{
		Calendar c= Calendar.getInstance();
		int cyear = c.get(Calendar.YEAR);
		int cmonth = c.get(Calendar.MONTH);
		int year=cyear;
		int month=1+cmonth;
		String query= "serviceProvider:\"" + sp + "\""+" AND year:"+year+ " AND month:"+month+" AND direction:nb";

		int dataCount = AccountAdderServiceHolder.getAnalyticsDataService()
				.searchCount(-1234, "WSO2TELCO_PRICING_ACCUMULATED_SUMMARY", query);
		List<Record> records = new ArrayList<>();
		List<String> ids = new ArrayList<>();
		if (dataCount > 0) {
			List<SearchResultEntry> resultEntries = AccountAdderServiceHolder.getAnalyticsDataService()
					.search(-1234, "WSO2TELCO_PRICING_ACCUMULATED_SUMMARY", query, 0, 1);

			for (SearchResultEntry entry : resultEntries) {
				ids.add(entry.getId());
			}
			AnalyticsDataResponse resp = AccountAdderServiceHolder.getAnalyticsDataService()
					.get(-1234, "WSO2TELCO_PRICING_ACCUMULATED_SUMMARY", 1, null, ids);

			records = AnalyticsDataServiceUtils
					.listRecords(AccountAdderServiceHolder.getAnalyticsDataService(), resp);
			Collections.sort(records, new Comparator<Record> (){
				@Override
				public int compare(Record o1, Record o2) {
					return Long.compare(o1.getTimestamp(), o2.getTimestamp());
				}
			});
		}

		double total=0;
		for (Record r:records) {
			String totalAmountS = r.getValue("totalAmount").toString();
			double totalAmountD = Double.parseDouble(totalAmountS);
			total += totalAmountD;
		}
		return total;
	}


	private double getCurrentMonthAmountFromInvoice(String accountId) throws AnalyticsException {
		Calendar c= Calendar.getInstance();
		int cyear = c.get(Calendar.YEAR);
		int cmonth = c.get(Calendar.MONTH);
		int year=cyear;
		int month=1+cmonth;
		Invoice  invoice=getInvoice(year, month, accountId);
		
		return invoice.getBalance().doubleValue();
		
	}
	
	
	
	 private Invoice getInvoice(int year,int month, String accountId) throws AnalyticsException {
		    InvoiceService invoiceService = new InvoiceService();
	        Invoice invoiceForMonth = null;

	        try {
	            List<Invoice> invoicesForAccount = invoiceService.getInvoicesForAccount(accountId);

	            
	            for (Invoice invoice : invoicesForAccount) {
	                LocalDate invoiceDate = invoice.getTargetDate();
	                int invoiceMonth = invoiceDate.getMonthOfYear();
	                int yearb=invoiceDate.getYear();
	               if(invoiceMonth == month && year==yearb)
	               {
	                   invoiceForMonth = invoice;
	                   break;	               
	                }
	            }
	        } catch (KillBillException e) {
	            throw new AnalyticsException("Error occurred while getting invoice from killbill", e);
	        }
	        return invoiceForMonth;
	    }


	@Override
	public Double getCurrentAmount(String sp) {
		String accountId;
		try {
			accountId = getKillBillAccount(-1234, sp);
			Double billamount=getCurrentMonthAmountFromInvoice(accountId);
			Double accuAmount=getCurrentMonthAmount(sp);
			return (billamount+accuAmount);
		} catch (Exception e) {
			return -1.0;
		}
		
	}


}
