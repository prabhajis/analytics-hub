package org.wso2telco.analytics.sparkUdf.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.LocalDate;
import org.killbill.billing.client.KillBillClient;
import org.killbill.billing.client.KillBillClientException;
import org.killbill.billing.client.KillBillHttpClient;
import org.killbill.billing.client.RequestOptions;
import org.killbill.billing.client.model.*;
import org.killbill.billing.invoice.api.InvoiceStatus;
import org.wso2telco.analytics.sparkUdf.configProviders.ConfigurationDataProvider;

/**
 * @author dilan
 */
public class BillUpdaterService {

    private static ConfigurationDataProvider dataProvider = null;
    private static KillBillHttpClient killBillHttpClient;
    private static KillBillClient killBillClient;
    private static final Log log = LogFactory.getLog(BillUpdaterService.class);

    @SuppressWarnings("deprecation")
    public String updateBill(String accountId, Integer year, Integer month, String description, Double amount) {
        UUID invoiceItemId = null;
        try {
            dataProvider = ConfigurationDataProvider.getInstance();

            killBillHttpClient = new KillBillHttpClient(dataProvider.getUrl(),
                    dataProvider.getUname(),
                    dataProvider.getPassword(),
                    dataProvider.getApiKey(),
                    dataProvider.getApiSecret());

            killBillClient = new KillBillClient(killBillHttpClient);

            Invoice invoiceForThisMonth = getInvoiceForCurrentMonth(accountId);

            if (invoiceForThisMonth == null) {

                Invoice invoice = getInvoiceForLastMonth(accountId);


                if (invoice != null) {
                    double lastMonthAmount = invoice.getBalance().doubleValue();
                    if (lastMonthAmount == 0.0d) {
                        InvoiceService invoiceService = new InvoiceService();
                        lastMonthAmount = invoiceService.getCreditValue(accountId).doubleValue();
                    }
                    if (lastMonthAmount != 0.0d) {

                        setInvoiceBalanceToZero(invoice);

                        UUID currentInvoiceId = transferAmount(accountId, lastMonthAmount);
                        Invoice currentInvoice = killBillClient.getInvoice(currentInvoiceId, true);
                        invoiceItemId = updateInvoice(currentInvoice, description, amount);
                    } else {
                        invoiceItemId = updateInvoice(accountId, description, amount);
                    }

                } else {
                    invoiceItemId = updateInvoice(accountId, description, amount);
                }
            } else {
                invoiceItemId = updateInvoice(invoiceForThisMonth, description, amount);
            }

        } catch (Exception e) {
            log.error("error in updateBill", e);
            return "Bill was not updated";
        } finally {
            if (killBillClient != null) {
                killBillClient.close();
            }
            if (killBillHttpClient != null) {
                killBillHttpClient.close();
            }
        }
        return invoiceItemId.toString();

    }

    private UUID transferAmount(String accountId, double lastMonthAmount) throws KillBillClientException {

        dataProvider = ConfigurationDataProvider.getInstance();

        killBillHttpClient = new KillBillHttpClient(dataProvider.getUrl(),
                dataProvider.getUname(),
                dataProvider.getPassword(),
                dataProvider.getApiKey(),
                dataProvider.getApiSecret());

        killBillClient = new KillBillClient(killBillHttpClient);

        RequestOptions requestOptionsForBillUpdate = RequestOptions.builder().withCreatedBy("admin").withReason("usage amount").withComment("usage amount").build();
        UUID invoice = null;
        if (lastMonthAmount > 0.0d) {
            invoice = updateInvoice(accountId, "last month balance", lastMonthAmount);

        } else if (lastMonthAmount < 0.0d) {
            Credit credit = new Credit();
            credit.setDescription("last month balance");
            credit.setAccountId(UUID.fromString(accountId));
            credit.setCurrency(killBillClient.getAccount(UUID.fromString(accountId)).getCurrency());
            credit.setCreditAmount(BigDecimal.valueOf(new Double(Math.abs(lastMonthAmount)).doubleValue()));
            Credit creditJson = killBillClient.createCredit(credit, Boolean.valueOf(false), requestOptionsForBillUpdate);
            InvoiceItem invoiceItem = new InvoiceItem();
            invoiceItem.setDescription("ADJ fot last month balance");
            invoiceItem.setCurrency(killBillClient.getAccount(UUID.fromString(accountId)).getCurrency());
            invoiceItem.setAmount(BigDecimal.valueOf(new Double(Math.abs(lastMonthAmount)).doubleValue()));
            invoiceItem.setAccountId(UUID.fromString(accountId));
            invoiceItem.setInvoiceId(creditJson.getInvoiceId());
            invoiceItem = killBillClient.createExternalCharge(invoiceItem, new LocalDate(System.currentTimeMillis()), Boolean.valueOf(false), Boolean.valueOf(false), "admin", "usage amount", "usage amount");
            invoice = invoiceItem.getInvoiceId();

        }
        return invoice;
    }

    private Invoice setInvoiceBalanceToZero(Invoice invoice) throws KillBillClientException {

        if (invoice.getBalance().doubleValue() > 0.0) {
            RequestOptions requestOptionsForBillUpdate = RequestOptions.builder()
                    .withCreatedBy("admin")
                    .withReason("payment")
                    .withComment("payment")
                    .build();
            InvoicePayment invoicePayment = new InvoicePayment();
            invoicePayment.setPurchasedAmount(invoice.getBalance());
            invoicePayment.setAccountId(invoice.getAccountId());
            invoicePayment.setTargetInvoiceId(invoice.getInvoiceId());
            InvoicePayment objFromJson = killBillClient.createInvoicePayment(invoicePayment, true, requestOptionsForBillUpdate);

        }
        return invoice;


    }

    private Invoice getInvoiceForMonthFromList(List<Invoice> invoices, int year, int month) throws KillBillClientException {
        Tags tag = null;
        dataProvider = ConfigurationDataProvider.getInstance();

        killBillHttpClient = new KillBillHttpClient(dataProvider.getUrl(),
                dataProvider.getUname(),
                dataProvider.getPassword(),
                dataProvider.getApiKey(),
                dataProvider.getApiSecret());

        killBillClient = new KillBillClient(killBillHttpClient);
        RequestOptions requestOptionsForBillUpdate = RequestOptions.builder()
                .withCreatedBy("admin")
                .withReason("payment")
                .withComment("payment")
                .build();

        for (Invoice invoice : invoices) {
          tag =  killBillClient.getInvoiceTags(invoice.getInvoiceId(),requestOptionsForBillUpdate);

          if(tag.getNext().toString().contains("WRITTEN_OFF"))
          {
              continue;
          }
          else
          {
              LocalDate targetDate = invoice.getTargetDate();
              if (targetDate.getMonthOfYear() == (month + 1) && targetDate.getYear() == year) {
                  return invoice;
              }
          }

        }
        return null;
    }

    private Invoice getInvoiceForLastMonth(String accountId) throws KillBillClientException {
        Tags tag = null;
        dataProvider = ConfigurationDataProvider.getInstance();

        killBillHttpClient = new KillBillHttpClient(dataProvider.getUrl(),
                dataProvider.getUname(),
                dataProvider.getPassword(),
                dataProvider.getApiKey(),
                dataProvider.getApiSecret());

        killBillClient = new KillBillClient(killBillHttpClient);
        RequestOptions requestOptionsForBillUpdate = RequestOptions.builder()
                .withCreatedBy("admin")
                .withReason("payment")
                .withComment("payment")
                .build();
        List<Invoice> invoices = killBillClient.getInvoicesForAccount(UUID.fromString(accountId), true, true);

        Date date = new Date();
        int year = date.getYear() + 1900;
        int month = date.getMonth();
        if (month == 0) {
            year = year - 1;
            month = 12;
        }
        if (invoices != null && invoices.size() != 0) {
            for (Invoice invoice : invoices) {
                tag = killBillClient.getInvoiceTags(invoice.getInvoiceId(), requestOptionsForBillUpdate);
                LocalDate targetDate = invoice.getTargetDate();
                if (targetDate.getMonthOfYear() == (month) && targetDate.getYear() == year) {
                    List<InvoiceItem> invoiceItems = invoice.getItems();
                    for (InvoiceItem invoiceItem : invoiceItems) {
                        if (invoiceItem.getDescription().equals("last month balance") || (invoiceItem.getDescription().split("\\|")).length > 2) {
                            if (tag.getNext().toString().contains("WRITTEN_OFF")) {
                                continue;
                            }
                            else
                            {
                                return invoice;
                            }
                        }
                    }

                }

            }
        }

        return null;
    }

    private Invoice getInvoiceForCurrentMonth(String accountId) throws KillBillClientException {
        Tags tag = null;
        dataProvider = ConfigurationDataProvider.getInstance();

        killBillHttpClient = new KillBillHttpClient(dataProvider.getUrl(),
                dataProvider.getUname(),
                dataProvider.getPassword(),
                dataProvider.getApiKey(),
                dataProvider.getApiSecret());

        killBillClient = new KillBillClient(killBillHttpClient);
        RequestOptions requestOptionsForBillUpdate = RequestOptions.builder()
                .withCreatedBy("admin")
                .withReason("payment")
                .withComment("payment")
                .build();
        List<Invoice> invoices = killBillClient.getInvoicesForAccount(UUID.fromString(accountId), true, true);
        Date date = new Date();
        int year = date.getYear() + 1900;
        int month = date.getMonth() + 1;

        if (invoices != null && invoices.size() != 0) {
            for (Invoice invoice : invoices) {
                tag = killBillClient.getInvoiceTags(invoice.getInvoiceId(), requestOptionsForBillUpdate);
                LocalDate targetDate = invoice.getTargetDate();
                if (targetDate.getMonthOfYear() == (month) && targetDate.getYear() == year) {

                    List<InvoiceItem> invoiceItems = invoice.getItems();
                    for (InvoiceItem invoiceItem : invoiceItems) {
                        if (invoiceItem.getDescription().equals("last month balance") || (invoiceItem.getDescription().split("\\|")).length > 2) {
                            if (tag.getNext().toString().contains("WRITTEN_OFF")) {
                                continue;
                            }
                            else
                            {
                                return invoice;
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

    private boolean commitInvoice(Invoice invoice) throws KillBillClientException {

        RequestOptions requestOptionsForBillUpdate = RequestOptions.builder()
                .withCreatedBy("admin")
                .withReason("commit")
                .withComment("commit")
                .build();
        killBillClient.commitInvoice(invoice.getInvoiceId(), requestOptionsForBillUpdate);


        return true;
    }

    private boolean commitAllInvoice(String accountID) throws KillBillClientException {

        RequestOptions requestOptionsForBillUpdate = RequestOptions.builder()
                .withCreatedBy("admin")
                .withReason("commit")
                .withComment("commit")
                .build();
        List<Invoice> invoices = killBillClient.getInvoicesForAccount(UUID.fromString(accountID), true, true);
        for (Invoice invoice : invoices) {


            if (!(invoice.getStatus().equalsIgnoreCase(InvoiceStatus.COMMITTED.toString()))) {
                killBillClient.commitInvoice(invoice.getInvoiceId(), requestOptionsForBillUpdate);
            }
        }
        return true;
    }

	/*
     *
	 * To use this method need to use correct killbill client version compatibility metric:http://killbill.io/downloads/
	 */

    private UUID updateInvoice(Invoice invoice, String description, Double amount) throws KillBillClientException {
        // TODO Auto-generated method stub

        if (invoice != null) {
            RequestOptions requestOptionsForBillUpdate = RequestOptions.builder()
                    .withCreatedBy("admin")
                    .withReason("usage amount")
                    .withComment("usage amount")
                    .build();

            if (amount > 0.0d) {
                InvoiceItem invoiceItem = new InvoiceItem();
                invoiceItem.setInvoiceId(invoice.getInvoiceId());
                invoiceItem.setDescription(description);
                invoiceItem.setCurrency(killBillClient.getAccount(invoice.getAccountId()).getCurrency());
                invoiceItem.setAmount(BigDecimal.valueOf(new Double(amount)));
                invoiceItem.setAccountId(invoice.getAccountId());
                invoiceItem = killBillClient.createExternalCharge(invoiceItem, new LocalDate(System.currentTimeMillis()), false, false, "admin", "usage amount", "usage amount");
                return invoiceItem.getInvoiceItemId();
            } else if (amount < 0.0d) {
                Credit credit = new Credit();
                credit.setAccountId(invoice.getAccountId());
                credit.setCurrency(killBillClient.getAccount(invoice.getAccountId()).getCurrency());
                credit.setDescription(description);
                credit.setInvoiceId(invoice.getInvoiceId());
                credit.setCreditAmount(BigDecimal.valueOf(new Double(Math.abs(amount))));
                Credit creditJson = killBillClient.createCredit(credit, false, requestOptionsForBillUpdate);
                return creditJson.getInvoiceId();
            } else {
                return UUID.fromString("Bill was not updated");
            }

        } else {
            throw new KillBillClientException(new NullPointerException());
        }

    }


    private UUID updateInvoice(String accountId, String description, Double amount) throws KillBillClientException {
        // TODO Auto-generated method stub
        RequestOptions requestOptionsForBillUpdate = RequestOptions.builder()
                .withCreatedBy("admin")
                .withReason("usage amount")
                .withComment("usage amount")
                .build();
        if (amount > 0.0d) {
            InvoiceItem invoiceItem = new InvoiceItem();
            invoiceItem.setDescription(description);
            invoiceItem.setCurrency(killBillClient.getAccount(UUID.fromString(accountId)).getCurrency());
            invoiceItem.setAmount(BigDecimal.valueOf(new Double(amount)));
            invoiceItem.setAccountId(UUID.fromString(accountId));
            invoiceItem = killBillClient.createExternalCharge(invoiceItem, new LocalDate(System.currentTimeMillis()), false, false, "admin", "usage amount", "usage amount");
            return invoiceItem.getInvoiceId();
        } else if (amount < 0.0d) {
            Credit credit = new Credit();
            credit.setDescription(description);
            credit.setAccountId(UUID.fromString(accountId));
            credit.setCurrency(killBillClient.getAccount(UUID.fromString(accountId)).getCurrency());
            credit.setCreditAmount(BigDecimal.valueOf(new Double(Math.abs(amount))));
            Credit creditJson = killBillClient.createCredit(credit, false, requestOptionsForBillUpdate);
            return creditJson.getInvoiceId();
        } else {
            return UUID.fromString("Bill was not updated");
        }

    }

}
