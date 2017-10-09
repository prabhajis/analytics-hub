package org.wso2telco.analytics.sparkUdf.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.joda.time.LocalDate;
import org.killbill.billing.client.KillBillClient;
import org.killbill.billing.client.KillBillClientException;
import org.killbill.billing.client.KillBillHttpClient;
import org.killbill.billing.client.RequestOptions;
import org.killbill.billing.client.RequestOptions.RequestOptionsBuilder;
import org.killbill.billing.client.model.Account;
import org.killbill.billing.client.model.Invoice;
import org.killbill.billing.client.model.InvoiceItem;
import org.wso2telco.analytics.sparkUdf.configProviders.ConfigurationDataProvider;
import org.wso2telco.analytics.sparkUdf.exception.KillBillException;

/**
 * @author dilan
 */
public class BillUpdaterService {


	private static ConfigurationDataProvider dataProvider=null;
	private static KillBillHttpClient killBillHttpClient;
	private static KillBillClient killBillClient;
	private static UUID invoiceItemId=null;

	@SuppressWarnings("deprecation")
	public String updateBill(String accountId,Integer year,Integer month,String description,Double amount){
		Account account=null;
		try {
			dataProvider=ConfigurationDataProvider.getInstance();

			killBillHttpClient= new KillBillHttpClient(dataProvider.getUrl(),
					dataProvider.getUname(),
					dataProvider.getPassword(),
					dataProvider.getApiKey(),
					dataProvider.getApiSecret());

			killBillClient = new KillBillClient(killBillHttpClient);

			List<Invoice> invoices=killBillClient.getInvoicesForAccount(UUID.fromString(accountId));
			Invoice invoice=getInvoiceForMonthFromList(invoices, year,month);
			
			if (invoice!=null) {
				invoiceItemId=updateInvoice(invoice, description, amount);	
			}

		}catch (Exception e) {
			return "Bill was not updated";
		}finally{
			if (killBillClient!=null) {
				killBillClient.close();
			}
			if (killBillHttpClient!=null) {
				killBillHttpClient.close();
			}
		}
		return invoiceItemId.toString();

	}

	private Invoice getInvoiceForMonthFromList(List<Invoice> invoices,int year,int month) {
		// TODO Auto-generated method stub
		for(Invoice invoice:invoices){
			LocalDate targetDate=invoice.getTargetDate();
			if (targetDate.getMonthOfYear()==month && targetDate.getYear()== year ) {
				return invoice;
			}
		}
		return null; 
	}

	/*
	 * 
	 * To use this method need to use correct killbill client version compatibility metric:http://killbill.io/downloads/
	 */

	private UUID updateInvoice(Invoice invoice,String description,Double amount) throws KillBillClientException {
		// TODO Auto-generated method stub
		if (invoice != null) {
			
			InvoiceItem invoiceItem=new InvoiceItem();
			invoiceItem.setInvoiceId(invoice.getInvoiceId());
			invoiceItem.setDescription(description);
			invoiceItem.setCurrency(killBillClient.getAccount(invoice.getAccountId()).getCurrency());
			invoiceItem.setAmount(BigDecimal.valueOf(new Double(amount)));
			invoiceItem.setAccountId(invoice.getAccountId());
			invoiceItem=killBillClient.createExternalCharge(invoiceItem, new LocalDate(System.currentTimeMillis()),false, false, "admin", "usage amount", "usage amount");
			/*if(killBillClient.getAccount(invoice.getAccountId()).getParentAccountId()!=null){
				RequestOptions.RequestOptionsBuilder builder= new RequestOptions.RequestOptionsBuilder();
				builder.withCreatedBy("ADMIN");
				builder.withComment("");
				builder.withUser("admin");
				killBillClient.transferChildCreditToParent(invoice.getAccountId(),builder.build());
			}*/
			return invoiceItem.getInvoiceItemId();
			
		}else {
			throw new KillBillClientException(new NullPointerException());
		}
			

	}

}
