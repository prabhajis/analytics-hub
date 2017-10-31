package org.wso2telco.analytics.killbill.internal;

import java.math.BigDecimal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.killbill.billing.client.KillBillClient;
import org.killbill.billing.client.KillBillClientException;
import org.killbill.billing.client.KillBillHttpClient;
import org.killbill.billing.client.RequestOptions;
import org.killbill.billing.client.model.Account;
import org.killbill.billing.client.model.Credit;
import org.killbill.billing.client.model.Invoice;
import org.killbill.billing.client.model.InvoiceItem;
import org.killbill.billing.client.model.InvoicePayment;
import org.killbill.billing.client.model.Payment;
import org.killbill.billing.client.model.PaymentMethod;
import org.killbill.billing.client.model.PaymentMethodPluginDetail;
import org.killbill.billing.client.model.PaymentTransaction;
import org.killbill.billing.client.model.Payments;
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
	private static final Log log = LogFactory.getLog(PaymentHandler.class);

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
			String killbillAccount=getKillBillAccount(-1234,loggedInUser);
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
			log.info("Transaction payment success");

			if(paymentTransaction.getStatus().equals(TransactionStatus.SUCCESS.toString())){

				double amountPaid=paymentTransaction.getAmount().doubleValue();

				RequestOptions requestOptionsForBillUpdate = RequestOptions.builder()
						.withCreatedBy("admin")
						.withReason("payment")
						.withComment("payment")
						.build();
				Invoice currentInvoice=getCurrentInvoice(killbillAccount);
				if(currentInvoice!=null){
					BigDecimal balance=currentInvoice.getBalance();
					if(amountPaid<=currentInvoice.getBalance().doubleValue()){
						// CREATE PAYMENT
						InvoicePayment invoicePayment = new InvoicePayment();
						invoicePayment.setPurchasedAmount(new BigDecimal(amountPaid));
						invoicePayment.setAccountId(currentInvoice.getAccountId());
						invoicePayment.setTargetInvoiceId(currentInvoice.getInvoiceId());
						InvoicePayment objFromJson = killBillClient.createInvoicePayment(invoicePayment, true, "admin", "payments", "payments");
       
					}else{
						if(balance.doubleValue()>0.0){
							InvoicePayment invoicePayment = new InvoicePayment();
							invoicePayment.setPurchasedAmount(balance);
							invoicePayment.setAccountId(currentInvoice.getAccountId());
							invoicePayment.setTargetInvoiceId(currentInvoice.getInvoiceId());
							InvoicePayment objFromJson = killBillClient.createInvoicePayment(invoicePayment, true, "admin", "payments", "payments");	
						}

						final Credit remCredit = new Credit();
						remCredit.setAccountId(account.getAccountId());
						BigDecimal remainingCredit=new BigDecimal((amountPaid-balance.doubleValue()));
						remCredit.setCreditAmount(remainingCredit);
						remCredit.setDescription("payment");
						killBillClient.createCredit(remCredit, true, "admin", "payment", "payment");


					}

				}else{
					final Credit remCredit = new Credit();
					remCredit.setAccountId(account.getAccountId());
					BigDecimal paidAmount=new BigDecimal(amountPaid);
					remCredit.setCreditAmount(paidAmount);
					remCredit.setDescription("payment");
					killBillClient.createCredit(remCredit, true, "admin", "payment", "payment");
					
					
				}
			}else{

				throw new KillBillError();
			}

		} catch (Exception e) {
			log.error("error in genarating payment:  ",e);
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

			String killbillAccount=getKillBillAccount(-1234,loggedInUser);


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
			log.error("error in payment method adding:  ",e);
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


	private double getCurrentMonthAmountFromInvoice(String accountId) throws AnalyticsException, KillBillClientException {
		
		double amount=killBillClient.getAccount(UUID.fromString(accountId),true,true).getAccountBalance().doubleValue();
		return amount;

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

					List<InvoiceItem> invoiceItems=invoice.getItems();
					for(InvoiceItem invoiceItem:invoiceItems){
						if(invoiceItem.getDescription().contains("last month balance") || (invoiceItem.getDescription().split("\\|")).length>2){

							invoiceForMonth = invoice;
							break;
						}
					}	

					if(invoiceForMonth!=null){
						break;
					}	               
				}
			}
		} catch (KillBillException e) {
			log.error("error in getInvoice :  ",e);
			throw new AnalyticsException("Error occurred while getting invoice from killbill", e);
		}
		return invoiceForMonth;
	}


	private Invoice getCurrentInvoice(String accountId) throws AnalyticsException {
		InvoiceService invoiceService = new InvoiceService();
		Invoice invoiceForMonth = null;

		try {
			List<Invoice> invoicesForAccount = invoiceService.getInvoicesForAccount(accountId);
			Date day=new Date();
			int month=day.getMonth()+1;
			int year=day.getYear()+1900;
			for (Invoice invoice : invoicesForAccount) {
				LocalDate invoiceDate = invoice.getTargetDate();
				int invoiceMonth = invoiceDate.getMonthOfYear();
				int yearb=invoiceDate.getYear();
				if(invoiceMonth == month && year==yearb)
				{
					List<InvoiceItem> invoiceItems=invoice.getItems();
					for(InvoiceItem invoiceItem:invoiceItems){
						if(invoiceItem.getDescription().equals("last month balance") || (invoiceItem.getDescription().split("|")).length>2){

							invoiceForMonth = invoice;
							break;
						}
					}			               
				}
				if(invoiceForMonth!=null){
					break;
				}	 
			}
		} catch (KillBillException e) {
			log.error("error in getCurrentInvoice :  ",e);
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
			log.error("error in getCurrentAmount :  ",e);
			return -1.0;
		}

	}


	@Override
	public String getPayments(String username) {
		JSONObject paymentsJson=new JSONObject();
		try {
			ConfigurationDataProvider dataProvider=ConfigurationDataProvider.getInstance();

			killBillHttpClient= new KillBillHttpClient(dataProvider.getUrl(),
					dataProvider.getUname(),
					dataProvider.getPassword(),
					dataProvider.getApiKey(),
					dataProvider.getApiSecret());
			killBillClient = new KillBillClient(killBillHttpClient);

			String accountID=getKillBillAccount(-1234,username);
			Payments payments=killBillClient.getPaymentsForAccount(UUID.fromString(accountID));
			JSONArray attemptsArray= new JSONArray();
			paymentsJson.put("paymentAttempts",attemptsArray);
			for(Payment payment:payments){
				boolean isValid=false;
				List<PaymentTransaction> paymentTransactions=payment.getTransactions();
				for(PaymentTransaction paymentTransaction:paymentTransactions){
					if(paymentTransaction.getTransactionType().equals(TransactionType.AUTHORIZE.name())){
						isValid=true;
					}

				}

				if(isValid){

					List<PaymentTransaction> attempts=payment.getTransactions();
					for(PaymentTransaction attempt:attempts){

						DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
						String timeAsString = fmt.print(attempt.getEffectiveDate());


						JSONObject attemptsobject=new JSONObject();
						attemptsobject.put("date", timeAsString);
						attemptsobject.put("amount", attempt.getAmount());
						attemptsobject.put("state", attempt.getStatus());
						attemptsArray.put(attemptsobject);

					}
				}

			}

		} catch (Exception e) {
			log.error("error in getPayments :  ",e);
			return null;
		}finally{
			if (killBillClient!=null) {
				killBillClient.close();
			}
			if (killBillHttpClient!=null) {
				killBillHttpClient.close();
			}
		}



		return paymentsJson.toString();
	}


}
