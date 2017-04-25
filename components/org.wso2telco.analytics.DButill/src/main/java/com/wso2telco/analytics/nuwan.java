package com.wso2telco.analytics;

import com.wso2telco.analytics.model.Commission;
import sun.awt.shell.ShellFolder;

import javax.persistence.PersistenceException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Created by nuwans on 4/24/17.
 */
public class nuwan {

    public static final String API = "payment";
    public static final String OPERATION = "Charge";
    public static final String APPLICATION_ID = "11";
    public static final String PURCHASE_CATOGEORY_CODE = "Game";
    public static final String OPERATOR = "OPERATOR2";

    public static void main(String args []){
        System.out.println("                                       ");

        nuwan_rate nr =new nuwan_rate();

        //nr.getCategoryBasedValueNB(API, OPERATION, APPLICATION_ID, PURCHASE_CATOGEORY_CODE);
        nr.getCategoryBasedValueSB(API,OPERATOR, OPERATION, APPLICATION_ID);
        System.out.println("                                       ");

        nr.getSBCommission(API,OPERATOR,APPLICATION_ID,OPERATION,PURCHASE_CATOGEORY_CODE);
        System.out.println("                                       ");

        nr.getSBCommissionDefaultCategory(API,OPERATOR,APPLICATION_ID,OPERATION,PURCHASE_CATOGEORY_CODE);

    //   RateCardService rateCardService =new RateCardService();
      // rateCardService.getSBCommissionHub(API, "OPERATOR2", "11","Charge","Game"  );
    }








}
