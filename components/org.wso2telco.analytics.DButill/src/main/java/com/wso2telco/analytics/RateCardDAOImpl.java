package com.wso2telco.analytics;

import java.sql.*;

import javax.persistence.PersistenceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wso2telco.analytics.dao.RateCardDAO;
import com.wso2telco.analytics.model.Commission;

public class RateCardDAOImpl implements RateCardDAO {

    /** The log. */
    private static Log log = LogFactory.getLog(RateCardDAOImpl.class);

    @Override
    public Commission getSBCommission(String api, String operatorId, String applicationId, String operation, String purchaseCategoryCode) {

        Connection con = null;
        ResultSet rs = null;
        PreparedStatement statement = null;
        Double spCommission = 0.0;
        Double opCoCommission = 0.0;
        Double hubCommission = 0.0;
        try {
            con = DBUtill.getDBConnection();
            if (con == null) {
                throw new Exception("Connection not found");
            }

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
            queryString.append("a.code = ? ");
            queryString.append("AND op.code = ? ");
            queryString.append("AND s.code= ? ");
            queryString.append("AND sb.applicationDid= ? ");
            queryString.append("AND c.code = ? ");

            statement = con.prepareStatement(queryString.toString());

            statement.setString(1, api);
            statement.setString(2, operatorId);
            statement.setString(3, operation);
            statement.setString(4, applicationId);
            statement.setString(5, purchaseCategoryCode);

            rs = statement.executeQuery();

            if (rs.next()) {
                spCommission = Double.parseDouble(rs.getString("sp"));
                opCoCommission = Double.parseDouble(rs.getString("opco"));
                hubCommission = Double.parseDouble(rs.getString("hub"));

            }

        } catch (PersistenceException e) {
            log.error("database operation error in subscription entry", e);
        } catch (Exception e) {
            log.error("database operation error in subscription entry", e);
        } finally {
            DBUtill.closeAllConnections(statement, con, rs);
        }

        Commission commission = new Commission();
        commission.setSp(spCommission);
        commission.setOpco(opCoCommission);
        commission.setAds(hubCommission);

        return commission;
    }

    @Override
    public Commission getSBCommissionDefaultCategory(String api, String operatorId, String applicationId, String operation, String purchaseCategoryCode) {

        Connection con = null;
        ResultSet rs = null;
        PreparedStatement statement = null;
        Double spCommission = 0.0;
        Double opCoCommission = 0.0;
        Double hubCommission = 0.0;
        try {
            con = DBUtill.getDBConnection();
            if (con == null) {
                throw new Exception("Connection not found");
            }

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
            queryString.append("a.code = ? ");
            queryString.append("AND op.code = ? ");
            queryString.append("AND s.code= ? ");
            queryString.append("AND sb.applicationDid= ? ");

            statement = con.prepareStatement(queryString.toString());

            statement.setString(1, api);
            statement.setString(2, operatorId);
            statement.setString(3, operation);
            statement.setString(4, applicationId);

            rs = statement.executeQuery();

            if (rs.next()) {
                spCommission = Double.parseDouble(rs.getString("sp"));
                opCoCommission = Double.parseDouble(rs.getString("opco"));
                hubCommission = Double.parseDouble(rs.getString("hub"));

            }

        } catch (PersistenceException e) {
            log.error("database operation error in subscription entry", e);
        } catch (Exception e) {
            log.error("database operation error in subscription entry", e);
        } finally {
            DBUtill.closeAllConnections(statement, con, rs);
        }

        Commission commission = new Commission();
        commission.setSp(spCommission);
        commission.setOpco(opCoCommission);
        commission.setAds(hubCommission);

        return commission;
    }

    @Override
    public Commission getNBCommission(String api,  String applicationId, String operation, String purchaseCategoryCode) {

        Connection con = null;
        ResultSet rs = null;
        PreparedStatement statement = null;
        Double spCommission = 0.0;
        Double opCoCommission = 0.0;
        Double hubCommission = 0.0;
        try {
            con = DBUtill.getDBConnection();
            if (con == null) {
                throw new Exception("Connection not found");
            }

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
            queryString.append("a.code = ? ");
            queryString.append("AND s.code= ? ");
            queryString.append("AND nb.applicationDid= ? ");
            queryString.append("AND c.code = ? ");

            statement = con.prepareStatement(queryString.toString());

            statement.setString(1, api);
            statement.setString(2, operation);
            statement.setString(3, applicationId);
            statement.setString(4, purchaseCategoryCode);

            rs = statement.executeQuery();

            if (rs.next()) {
                spCommission = Double.parseDouble(rs.getString("sp"));
                opCoCommission = Double.parseDouble(rs.getString("opco"));
                hubCommission = Double.parseDouble(rs.getString("hub"));

            }

        } catch (PersistenceException e) {
            log.error("database operation error in subscription entry", e);
        } catch (Exception e) {
            log.error("database operation error in subscription entry", e);
        } finally {
            DBUtill.closeAllConnections(statement, con, rs);
        }

        Commission commission = new Commission();
        commission.setSp(spCommission);
        commission.setOpco(opCoCommission);
        commission.setAds(hubCommission);

        return commission;
    }

    @Override
    public Commission getNBCommissionDefaultCategory(String api,  String applicationId, String operation, String purchaseCategoryCode) {

        Connection con = null;
        ResultSet rs = null;
        PreparedStatement statement = null;
        Double spCommission = 0.0;
        Double opCoCommission = 0.0;
        Double hubCommission = 0.0;
        try {
            con = DBUtill.getDBConnection();
            if (con == null) {
                throw new Exception("Connection not found");
            }

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
            queryString.append("a.code = ? ");
            queryString.append("AND s.code= ? ");
            queryString.append("AND nb.applicationDid= ? ");

            statement = con.prepareStatement(queryString.toString());

            statement.setString(1, api);
            statement.setString(2, operation);
            statement.setString(3, applicationId);

            rs = statement.executeQuery();

            if (rs.next()) {
                spCommission = Double.parseDouble(rs.getString("sp"));
                opCoCommission = Double.parseDouble(rs.getString("opco"));
                hubCommission = Double.parseDouble(rs.getString("hub"));

            }

        } catch (PersistenceException e) {
            log.error("database operation error in subscription entry", e);
        } catch (Exception e) {
            log.error("database operation error in subscription entry", e);
        } finally {
            DBUtill.closeAllConnections(statement, con, rs);
        }

        Commission commission = new Commission();
        commission.setSp(spCommission);
        commission.setOpco(opCoCommission);
        commission.setAds(hubCommission);

        return commission;
    }

    @Override
    public Boolean getCategoryBasedValueNB(String api,  String applicationId, String operation, String purchaseCategoryCode) {
        Connection con = null;
        ResultSet rs = null;
        PreparedStatement statement = null;
        Boolean isCategoryBased = false;
        try {
            con = DBUtill.getDBConnection();
            if (con == null) {
                throw new Exception("Connection not found");
            }

            StringBuilder queryString = new StringBuilder("select isCatagoryBase ");
            queryString.append("FROM inmdCommission c ");
            queryString.append("INNER JOIN inmdpercentagerate p ON p.defaultCommision = c.CommissionDid ");
            queryString.append("INNER JOIN inmdRate r on r.rateDid = p.rateDid ");
            queryString.append("INNER JOIN inmdOperationRate opr ON opr.rateDid = r.rateDid ");
            queryString.append("INNER JOIN inmdServices s ON s.servicesDid = opr.servicesDid ");
            queryString.append("INNER JOIN inmdApi a ON a.apiDid = s.apiDid ");
            queryString.append("INNER JOIN inmdNBSubscriptionRate nb ON nb.servicesRateDid = opr.servicesRateDid ");
            queryString.append("WHERE ");
            queryString.append("a.code = ? ");
            queryString.append("AND s.code= ? ");
            queryString.append("AND nb.applicationDid= ? ");

            statement = con.prepareStatement(queryString.toString());

            statement.setString(1, api);
            statement.setString(2, operation);
            statement.setString(3, applicationId);

            rs = statement.executeQuery();

            if (rs.next()) {
                isCategoryBased = "1".equals(rs.getString("isCatagoryBase"));
            }

        } catch (PersistenceException e) {
            log.error("database operation error in subscription entry", e);
        } catch (Exception e) {
            log.error("database operation error in subscription entry", e);
        } finally {
            DBUtill.closeAllConnections(statement, con, rs);
        }

        return isCategoryBased;

    }

    @Override
    public Boolean getCategoryBasedValueSB(String api, String operatorId, String applicationId, String operation){
        Connection con = null;
        ResultSet rs = null;
        PreparedStatement statement = null;
        Boolean isCategoryBased = false;
        try {
            con = DBUtill.getDBConnection();
            if (con == null) {
                throw new Exception("Connection not found");
            }

            StringBuilder queryString = new StringBuilder("select isCatagoryBase ");
            queryString.append("FROM inmdsbsubscriptions sb ");
            queryString.append("INNER JOIN inmdoperatorrate opr ON opr.operatorRateDid = sb.operationRateDid ");
            queryString.append("INNER JOIN inmdpercentagerate p ON p.rateDid = opr.rateDid ");
            queryString.append("INNER JOIN inmdservices s ON s.servicesDid = opr.servicesDid ");
            queryString.append("INNER JOIN inmdapi a ON a.apiDid = s.apiDid ");
            queryString.append("INNER JOIN inmdoperator op ON op.operatorDid = opr.operatorDid ");
            queryString.append("WHERE ");
            queryString.append("a.code = ? ");
            queryString.append("AND op.code = ? ");
            queryString.append("AND s.code= ? ");
            queryString.append("AND sb.applicationDid= ? ");

            statement = con.prepareStatement(queryString.toString());

            statement.setString(1, api);
            statement.setString(2, operatorId);
            statement.setString(3, operation);
            statement.setString(4, applicationId);

            rs = statement.executeQuery();

            if (rs.next()) {
                isCategoryBased = "1".equals(rs.getString("isCatagoryBase"));
            }

        } catch (PersistenceException e) {
            log.error("database operation error in subscription entry", e);
        } catch (Exception e) {
            log.error("database operation error in subscription entry", e);
        } finally {
            DBUtill.closeAllConnections(statement, con, rs);
        }

        return isCategoryBased;

    }

}
