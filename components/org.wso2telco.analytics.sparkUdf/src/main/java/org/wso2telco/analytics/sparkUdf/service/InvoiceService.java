package org.wso2telco.analytics.sparkUdf.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.killbill.billing.client.KillBillClient;
import org.killbill.billing.client.KillBillHttpClient;
import org.killbill.billing.client.RequestOptions;
import org.killbill.billing.client.model.Invoice;
import org.wso2telco.analytics.sparkUdf.configProviders.ConfigurationDataProvider;
import org.wso2telco.analytics.sparkUdf.exception.KillBillException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class InvoiceService {

	private static final Log log = LogFactory.getLog(InvoiceService.class);
    public List<Invoice> getInvoicesForAccount(String accountId) throws KillBillException {

        KillBillHttpClient killBillHttpClient = null;
        KillBillClient killBillClient = null;

        ConfigurationDataProvider dataProvider = ConfigurationDataProvider.getInstance();

        try {
            killBillHttpClient = new KillBillHttpClient(dataProvider.getUrl(), dataProvider.getUname(),
                    dataProvider.getPassword(), dataProvider.getApiKey(), dataProvider.getApiSecret());

            killBillClient = new KillBillClient(killBillHttpClient);
            
            
            return killBillClient.getInvoicesForAccount(UUID.fromString(accountId), RequestOptions.empty());
        } catch (Exception e) {
        	log.error("error in getInvoicesForAccount"+ e);
            throw new KillBillException("Error occurred while getting invoice for invoice id [" + accountId + "]", e);
        } finally {
            if (killBillClient != null) {
                killBillClient.close();
            }
            if (killBillHttpClient != null) {
                killBillHttpClient.close();
            }
        }
    }
    public BigDecimal getCreditValue(String killBillAccountId) throws KillBillException {
        KillBillHttpClient killBillHttpClient = null;
        KillBillClient killBillClient = null;
        ConfigurationDataProvider dataProvider = ConfigurationDataProvider.getInstance();
        try {
            killBillHttpClient = new KillBillHttpClient(dataProvider.getUrl(), dataProvider.getUname(),
                    dataProvider.getPassword(), dataProvider.getApiKey(), dataProvider.getApiSecret());

            killBillClient = new KillBillClient(killBillHttpClient);
            return killBillClient.getAccount(UUID.fromString(killBillAccountId),true,true).getAccountBalance();
        }
        catch (Exception e)
        {
        	log.error("error in getCreditValue"+ e);
            throw new KillBillException("Error occurred while getting invoice for invoice id [" + killBillAccountId + "]", e);
        }
        finally {
            if (killBillClient != null) {
                killBillClient.close();
            }
            if (killBillHttpClient != null) {
                killBillHttpClient.close();
            }
        }
    }
}
