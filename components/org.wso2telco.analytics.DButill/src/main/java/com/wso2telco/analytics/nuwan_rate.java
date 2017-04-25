package com.wso2telco.analytics;

/**
 * Created by nuwans on 4/24/17.
 */
public class nuwan_rate {


    public void getSBCommission(String api, String operatorId, String applicationId, String operation, String purchaseCategoryCode) {



            StringBuilder queryString = new StringBuilder("select sp,hub,opco ");
            queryString.append("FROM inmdsbsubscriptions sb ");
            queryString.append("INNER JOIN inmdoperatorrate opr ON opr.operatorRateDid = sb.operationRateDid ");
            queryString.append("INNER JOIN inmdpercentagerate p ON p.rateDid = opr.rateDid ");
            queryString.append("INNER JOIN inmdservices s ON s.servicesDid = opr.servicesDid ");
            queryString.append("INNER JOIN inmdapi a ON a.apiDid = s.apiDid ");
            queryString.append("INNER JOIN inmdRateCatagory rc ON p.percentageRateDid = rc.percentageRateDid ");
            queryString.append("INNER JOIN inmdcatagory c ON c.catagoryDid = rc.catagoryDid ");
            queryString.append("INNER JOIN inmdcommission com ON com.CommissionDid = rc.CommissionDid ");
            queryString.append("INNER JOIN inmdrate r ON r.rateDid = p.rateDid ");
            queryString.append("INNER JOIN inmdcurrency cur ON cur.currencyDid = r.currencyDid ");
            queryString.append("INNER JOIN inmdoperator op ON op.operatorDid = opr.operatorDid ");
            queryString.append("WHERE ");
            queryString.append("a.code = ").append("'"+api+"'");
            queryString.append("AND op.code = ").append("'"+operatorId+"'");
            queryString.append("AND s.code= ").append("'"+operation+"'");
            queryString.append("AND sb.applicationDid= ").append("'"+applicationId+"'");
            queryString.append("AND c.code = ").append("'"+purchaseCategoryCode+"'");
        queryString.append(" ;");
            System.out.println(queryString);

    }


    public void getSBCommissionDefaultCategory(String api, String operatorId, String applicationId, String operation, String purchaseCategoryCode) {

            StringBuilder queryString = new StringBuilder("select sp,hub,opco ");
            queryString.append("FROM inmdsbsubscriptions sb ");
            queryString.append("INNER JOIN inmdoperatorrate opr ON opr.operatorRateDid = sb.operationRateDid ");
            queryString.append("INNER JOIN inmdpercentagerate p ON p.rateDid = opr.rateDid ");
            queryString.append("INNER JOIN inmdservices s ON s.servicesDid = opr.servicesDid ");
            queryString.append("INNER JOIN inmdapi a ON a.apiDid = s.apiDid ");
            queryString.append("INNER JOIN inmdRateCatagory rc ON p.percentageRateDid = rc.percentageRateDid ");
            queryString.append("INNER JOIN inmdcommission com ON com.CommissionDid = p.defaultCommision ");
            queryString.append("INNER JOIN inmdrate r ON r.rateDid = p.rateDid ");
            queryString.append("INNER JOIN inmdcurrency cur ON cur.currencyDid = r.currencyDid ");
            queryString.append("INNER JOIN inmdoperator op ON op.operatorDid = opr.operatorDid ");
            queryString.append("WHERE ");
            queryString.append("a.code = ").append("'"+api+"'");
            queryString.append(" AND op.code = ").append("'"+operatorId+"'");
            queryString.append(" AND s.code= ").append("'"+operation+"'");
            queryString.append(" AND sb.applicationDid= ").append("'"+applicationId+"'");
        queryString.append(" ;");
        System.out.println(queryString);
    }


    public void getNBCommission(String api, String operation, String applicationId, String purchaseCategoryCode) {



            StringBuilder queryString = new StringBuilder("select sp,hub,opco ");
            queryString.append("FROM inmdnbsubscriptionrate nb ");
            queryString.append("INNER JOIN inmdoperationrate opr ON opr.servicesRateDid = nb.servicesRateDid ");
            queryString.append("INNER JOIN inmdservices s ON s.servicesDid = opr.servicesDid ");
            queryString.append("INNER JOIN inmdapi a ON a.apiDid = s.apiDid ");
            queryString.append("INNER JOIN inmdrate r ON r.rateDid = opr.rateDid ");
            queryString.append("INNER JOIN inmdpercentagerate p ON p.rateDid = r.rateDid ");
            queryString.append("INNER JOIN inmdratecatagory rc ON rc.percentageRateDid = p.percentageRateDid ");
            queryString.append("INNER JOIN inmdcatagory c ON c.catagoryDid = rc.catagoryDid ");
            queryString.append("INNER JOIN inmdcommission com ON com.CommissionDid = rc.CommissionDid ");
            queryString.append("INNER JOIN inmdcurrency cur ON cur.currencyDid = r.currencyDid ");
            queryString.append("WHERE ");
            queryString.append("a.code = ").append("'"+api+"'");
            queryString.append(" AND s.code= ").append("'"+operation+"'");
            queryString.append(" AND nb.applicationDid= ").append("'"+applicationId+"'");
            queryString.append(" AND c.code = ").append("'"+purchaseCategoryCode+"'");
        queryString.append(" ;");
        System.out.println(queryString);
        queryString.append(" ");
    }

    public void getNBCommissionDefaultCategory(String api, String operation, String applicationId, String purchaseCategoryCode) {



            StringBuilder queryString = new StringBuilder("select sp,hub,opco ");
            queryString.append("FROM inmdnbsubscriptionrate nb ");
            queryString.append("INNER JOIN inmdoperationrate opr ON opr.servicesRateDid = nb.servicesRateDid ");
            queryString.append("INNER JOIN inmdservices s ON s.servicesDid = opr.servicesDid ");
            queryString.append("INNER JOIN inmdapi a ON a.apiDid = s.apiDid ");
            queryString.append("INNER JOIN inmdrate r ON r.rateDid = opr.rateDid ");
            queryString.append("INNER JOIN inmdpercentagerate p ON p.rateDid = r.rateDid ");
            queryString.append("INNER JOIN inmdcommission com ON com.CommissionDid = p.defaultCommision ");
            queryString.append("INNER JOIN inmdcurrency cur ON cur.currencyDid = r.currencyDid ");
            queryString.append("WHERE ");
            queryString.append("a.code = ").append("'"+api+"'");
            queryString.append(" AND s.code= ").append("'"+operation+"'");
            queryString.append(" AND nb.applicationDid= ").append("'"+applicationId+"'");
        queryString.append(" ;");
        System.out.println(queryString);
        queryString.append(" ");
    }


    public void getCategoryBasedValueNB(String api, String operation, String applicationId, String purchaseCategoryCode) {


            StringBuilder queryString = new StringBuilder("select isCatagoryBase ");
            queryString.append("FROM inmdCommission c ");
            queryString.append("INNER JOIN inmdpercentagerate p ON p.defaultCommision = c.CommissionDid ");
            queryString.append("INNER JOIN inmdRate r on r.rateDid = p.rateDid ");
            queryString.append("INNER JOIN inmdOperationRate opr ON opr.rateDid = r.rateDid ");
            queryString.append("INNER JOIN inmdServices s ON s.servicesDid = opr.servicesDid ");
            queryString.append("INNER JOIN inmdApi a ON a.apiDid = s.apiDid ");
            queryString.append("INNER JOIN inmdNBSubscriptionRate nb ON nb.servicesRateDid = opr.servicesRateDid ");
            queryString.append("WHERE ");
            queryString.append("a.code = ").append("'"+api+"'");
            queryString.append(" AND s.code= ").append("'"+operation+"'");
            queryString.append(" AND nb.applicationDid= ").append("'"+applicationId+"'");
        queryString.append(" ;");
        System.out.println(queryString);
        queryString.append(" ");

    }


    public void getCategoryBasedValueSB(String api, String operatorId, String operation, String applicationId){

            StringBuilder queryString = new StringBuilder("select isCatagoryBase ");
            queryString.append("FROM inmdsbsubscriptions sb ");
            queryString.append("INNER JOIN inmdoperatorrate opr ON opr.operatorRateDid = sb.operationRateDid ");
            queryString.append("INNER JOIN inmdpercentagerate p ON p.rateDid = opr.rateDid ");
            queryString.append("INNER JOIN inmdservices s ON s.servicesDid = opr.servicesDid ");
            queryString.append("INNER JOIN inmdapi a ON a.apiDid = s.apiDid ");
            queryString.append("INNER JOIN inmdoperator op ON op.operatorDid = opr.operatorDid ");
            queryString.append("WHERE ");
            queryString.append("a.code = ").append("'"+api+"'");
            queryString.append(" AND op.code = ").append("'"+operatorId+"'");
            queryString.append(" AND s.code= ").append("'"+operation+"'");
            queryString.append(" AND sb.applicationDid= ").append("'"+applicationId+"'");
        queryString.append(" ;");
            System.out.println(queryString);
        queryString.append(" ");
    }
}
