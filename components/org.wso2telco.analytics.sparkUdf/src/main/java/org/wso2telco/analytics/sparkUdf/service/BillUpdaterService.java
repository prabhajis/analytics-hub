package org.wso2telco.analytics.sparkUdf.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.joda.time.LocalDate;
import org.killbill.billing.client.KillBillClient;
import org.killbill.billing.client.KillBillClientException;
import org.killbill.billing.client.KillBillHttpClient;
import org.killbill.billing.client.RequestOptions;
import org.killbill.billing.client.model.Account;
import org.killbill.billing.client.model.Invoice;
import org.killbill.billing.client.model.InvoiceItem;
import org.wso2telco.analytics.sparkUdf.configProviders.ConfigurationDataProvider;

/**
 * @author dilan
 */
public class BillUpdaterService {


	private static ConfigurationDataProvider dataProvider=null;
	private static KillBillHttpClient killBillHttpClient;
	private static KillBillClient killBillClient;
	private static UUID invoiceItemId=null;

	@SuppressWarnings("deprecation")
	public String l(String accountId,int year,int month,String description,int amount){
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

	private UUID updateInvoice(Invoice invoice,String description,int amount) throws KillBillClientException {
		// TODO Auto-generated method stub
		InvoiceItem invoiceItem=new InvoiceItem();
		invoiceItem.setInvoiceId(invoice.getInvoiceId());
		invoiceItem.setDescription(description);
		invoiceItem.setPhaseName(description);
		invoiceItem.setAmount(BigDecimal.valueOf(new Long(amount)));
		invoiceItem.setAccountId(invoice.getAccountId());
		invoiceItem=killBillClient.createExternalCharge(invoiceItem, new LocalDate(System.currentTimeMillis()),true, true, "admin", "usage amount", "usage amount");
		return invoiceItem.getInvoiceItemId();
	}
	
}
