package org.wso2telco.analytics.pricing.service;

import com.wso2telco.analytics.DBUtill;
import com.wso2telco.analytics.exception.DBUtilException;
import com.wso2telco.analytics.util.DataSourceNames;
import org.apache.commons.collections.map.HashedMap;
import org.wso2telco.analytics.pricing.AnalyticsPricingException;
import org.wso2telco.analytics.pricing.service.dao.RateCardDAO;

import java.math.BigDecimal;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.sql.Date;
import org.wso2telco.analytics.pricing.Tax;

import javax.persistence.criteria.CriteriaBuilder;

import static java.sql.Types.NULL;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
****
TODO:use constant insetad of coloum names
 */
public class RateCardDAOImpl implements RateCardDAO {

    private static final String CAT_DEFAULT = "__default__";

    @Override
    public Object getNBRateCard(String operationId, String applicationId, String api, String category, String subCategory) throws AnalyticsPricingException, DBUtilException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        ChargeRate rate = null;
        Integer rateDefID = null;

        try {
            connection = DBUtill.getDBConnection();

            if (connection == null) {
                throw new AnalyticsPricingException("Database Connection Cannot Be Established");
            }

            //get rate def id from sub_rate_nb table
            String nbQuery = "SELECT rate_defid "
                    + "FROM api_operation ao, "
                    + "     sub_rate_nb rnb, "
                    + "     api a "
                    + "WHERE ao.api_operationid = rnb.api_operationid "
                    + "  AND a.apiid = ao.apiid "
                    + "  AND rnb.applicationid =? "
                    + "  AND a.apiname = ? "
                    + "  AND ao.api_operation = ?";

            preparedStatement = connection.prepareStatement(nbQuery);
            preparedStatement.setString(1, applicationId);
            preparedStatement.setString(2, api);
            preparedStatement.setString(3, operationId);

            resultSet = preparedStatement.executeQuery();
            //connection.commit();

            if (resultSet.next()) {
                rateDefID = resultSet.getInt("rate_defid");
            }

            if (rateDefID == null) {
                throw new AnalyticsPricingException("Rate Assignment is Faulty " + " :" + operationId + " :" + applicationId + " :" + api + " :" + category + " :" + subCategory);
            }

            // execute query
            rate = executeQuery(rateDefID, category, subCategory);

        } catch (SQLException e) {
            DBUtill.handleException("Error occured getNBRateCard: ", e);
        } catch (AnalyticsPricingException e) {
            DBUtill.handleException(e.getMessage(), e);
        } finally {
            DBUtill.closeAllConnections(preparedStatement, connection, resultSet);
        }
        return rate;
    }

    @Override
    public Object getSBRateCard(String operatorId, String operationId, String applicationId, String api, String category, String subCategory) throws AnalyticsPricingException, DBUtilException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        ChargeRate rate = null;
        int rateDefID = 0;

        try {

            connection = DBUtill.getDBConnection();
            if (connection == null) {
                throw new AnalyticsPricingException("Database Connection Cannot Be Established");
            }

            String sbQuery = "SELECT rate_defid "
                    + "FROM api_operation ao, "
                    + "     sub_rate_sb srb, "
                    + "     api a "
                    + "WHERE ao.api_operationid = srb.api_operationid "
                    + "  AND srb.applicationid =? "
                    + "  AND srb.operatorid =? "
                    + "  AND a.apiname = ? "
                    + "  AND ao.api_operation = ?";

            preparedStatement = connection.prepareStatement(sbQuery);
            preparedStatement.setInt(1, Integer.parseInt(applicationId));
            preparedStatement.setInt(2, Integer.parseInt(operatorId));
            preparedStatement.setString(3, api);
            preparedStatement.setString(4, operationId);

            resultSet = preparedStatement.executeQuery();
            //connection.commit();

            if (resultSet.next()) {
                rateDefID = resultSet.getInt("rate_defid");
            }

            // execute query
            rate = executeQuery(rateDefID, category, subCategory);

        } catch (SQLException e) {
            DBUtill.handleException("Error occured getSBRateCard: ", e);
        } finally {
            DBUtill.closeAllConnections(preparedStatement, connection, resultSet);
        }
        return rate;
    }

    private ArrayList<String> getRateTaxes(String rateName) throws AnalyticsPricingException, DBUtilException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String taxCode = null;
        ArrayList<String> taxes = new ArrayList<String>();

        try {

            connection = DBUtill.getDBConnection();
            if (connection == null) {
                throw new AnalyticsPricingException("Database Connection Cannot Be Established");
            }

            StringBuilder query = new StringBuilder();
            query.append("SELECT tax.taxcode ");
            query.append("FROM (tax ");
            query.append("      INNER JOIN rate_taxes ON tax.taxid=rate_taxes.taxid) ");
            query.append("INNER JOIN rate_def ON rate_def.rate_defid=rate_taxes.rate_defid ");
            query.append("WHERE rate_def.rate_defname= ?");

            preparedStatement = connection.prepareStatement(query.toString());
            preparedStatement.setString(1, rateName);

            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                taxCode = resultSet.getString("taxcode");
                taxes.add(taxCode);
            }

        } catch (SQLException e) {
            DBUtill.handleException("Error occured getRateTaxes: ", e);
        } finally {
            DBUtill.closeAllConnections(preparedStatement, connection, resultSet);
        }
        return taxes;
    }

    private boolean setBooleanVal(int value) {
        if (value > 0) {
            return true;
        } else {
            return false;
        }
    }

    private ChargeRate executeQuery(int rateDefID, String category, String subCategory) throws AnalyticsPricingException, DBUtilException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        ChargeRate rate = null;

        try {

            boolean isCategory = false;
            boolean isSubcategory = false;
            boolean RateCategoryBased = false;

            if (category != null && category.length() > 0) {
                isCategory = true;
            }

            if (subCategory != null && subCategory.length() > 0) {
                isSubcategory = true;
            }

            connection = DBUtill.getDBConnection();

            if (connection == null) {
                throw new AnalyticsPricingException("database connection cannot be established");
            }

            StringBuilder query = new StringBuilder();
            query.append("SELECT rate_defid,rate_typeid,rate_defcategorybase ");
            query.append("FROM rate_def ");
            query.append("WHERE rate_defid =?");

            preparedStatement = connection.prepareStatement(query.toString());
            preparedStatement.setInt(1, rateDefID);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                RateCategoryBased = setBooleanVal(resultSet.getInt("rate_defcategorybase"));
            } else {
                throw new AnalyticsPricingException("ReateCard Not found For the RateId: " + rateDefID);
            }

            query = new StringBuilder();
            query.append("SELECT rate_defname,rt.rate_typecode,rd.rate_defdefault,c.currencycode,rd.rate_defcategorybase, ");
            query.append("cat,sub,tariffname,tariffdesc,tariffdefaultval,tariffmaxcount,tariffexcessrate,tariffdefrate,tariffspcommission, ");
            query.append("tariffadscommission,tariffopcocommission,tariffsurchargeval,tariffsurchargeAds,tariffsurchargeOpco ");
            query.append("FROM rate_def rd, ");
            query.append("     currency c, ");
            query.append("     tariff tr, ");
            query.append("     rate_type rt, ");
            query.append("  (SELECT rate_defid, ");
            query.append("          NULL AS cat, ");
            query.append("          NULL AS sub, ");
            query.append("          tariffid ");
            query.append("   FROM rate_def ");
            query.append("   WHERE rate_defid =? ");
            query.append("   UNION ALL ");
            query.append("     (SELECT rate_defid, ");
            query.append("        (SELECT categorycode ");
            query.append("         FROM category ");
            query.append("         WHERE rc.parentcategoryid = category.categoryid) AS cat, ");
            query.append("        (SELECT categorycode ");
            query.append("         FROM category ");
            query.append("         WHERE rc.childcategoryid = category.categoryid) AS sub,tariffid ");
            query.append("      FROM rate_category rc ");
            query.append("      WHERE rc.rate_defid = ?) ) ct ");
            query.append("WHERE ct.rate_defid = rd.rate_defid ");
            query.append("  AND rd.currencyid = c.currencyid ");
            query.append("  AND ct.tariffid = tr.tariffid ");
            query.append("  AND rd.rate_typeid = rt.rate_typeid ");

            if (RateCategoryBased) {
                if (isCategory && isSubcategory) {
                    query.append("AND ct.cat = ? AND ct.sub = ?");
                } else if (isCategory) {
                    query.append("AND ct.cat = ? AND ct.sub is null");
                } else {
                    query.append("AND ct.cat is null AND ct.sub is null");
                }
            } else {
                query.append("AND ct.cat is null AND ct.sub is null");
            }

            preparedStatement = connection.prepareStatement(query.toString());
            preparedStatement.setInt(1, rateDefID);
            preparedStatement.setInt(2, rateDefID);

            if (RateCategoryBased) {
                if (isCategory && isSubcategory) {
                    preparedStatement.setString(3, category);
                    preparedStatement.setString(4, subCategory);
                } else if (isCategory) {
                    preparedStatement.setString(3, category);
                }
            }
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String rateCardName = resultSet.getString("rate_defname");
                String row_category = resultSet.getString("cat");
                String row_subCategory = resultSet.getString("sub");
                String currency = resultSet.getString("currencycode");
                String type = resultSet.getString("rate_typecode");
                Double row_maxCount = nullCheck(resultSet, "tariffmaxcount");
                Double row_excessRate = nullCheck(resultSet, "tariffexcessrate");
                Double row_attrDefRate = nullCheck(resultSet, "tariffdefrate");
                Double row_spCommission = nullCheck(resultSet, "tariffspcommission");
                Double row_adsCommission = nullCheck(resultSet, "tariffadscommission");
                Double row_opcoCommission = nullCheck(resultSet, "tariffopcocommission");
                Double row_tariffDefaultVal = nullCheck(resultSet, "tariffdefaultval");
                Double row_surchargeVal = nullCheck(resultSet, "tariffsurchargeval");
                Double row_surchargeAds = nullCheck(resultSet, "tariffsurchargeAds");
                Double row_surchargeOpco = nullCheck(resultSet, "tariffsurchargeOpco");

                int defval = resultSet.getInt("rate_defdefault");
                int categorybase = resultSet.getInt("rate_defcategorybase");

                rate = new ChargeRate(rateCardName);
                rate.setCurrency(currency);
                rate.setType(RateType.getEnum(type));
                //setting isdefault and categorybased values.
                rate.setDefault(setBooleanVal(defval));
                rate.setCategoryBasedVal(setBooleanVal(categorybase));

                Tariff tariff = new Tariff();

                //value element in every rate element
                if (row_tariffDefaultVal != null) {
                    tariff.setValue(BigDecimal.valueOf(row_tariffDefaultVal));
                }
                //if rate type is QUOTA
                if (row_maxCount != null || row_excessRate != null || row_attrDefRate != null) {
                    tariff.setTariffmaxcount(row_maxCount.intValue());
                    tariff.setExcessRate(new BigDecimal(row_excessRate));
                    tariff.setDefaultRate(new BigDecimal(row_attrDefRate));
                }
                if (row_spCommission != null || row_adsCommission != null || row_opcoCommission != null) {
                    tariff.setSpCommission(new BigDecimal(row_spCommission));
                    tariff.setAdsCommission(new BigDecimal(row_adsCommission));
                    tariff.setOpcoCommission(new BigDecimal(row_opcoCommission));
                }

                //surcharge values
                if (row_surchargeVal != null || row_surchargeAds != null || row_surchargeOpco != null) {
                    tariff.setSurchargeElementValue(new BigDecimal(row_surchargeVal));
                    tariff.setSurchargeElementAds(new BigDecimal(row_surchargeVal));
                    tariff.setSurchargeElementOpco(new BigDecimal(row_surchargeOpco));
                }

                //Sets Default Tariff 
                rate.setTarrif(tariff);

                if ((RateCategoryBased) && (isCategory)) {

                    Map<String, Object> categoryEntityMap = new HashMap<String, Object>();
                    Map<String, Object> subCategoryEntityMap = new HashMap<String, Object>();

                    if (!isSubcategory) {

                        subCategoryEntityMap.put(CAT_DEFAULT, tariff);
                        categoryEntityMap.put(row_category, subCategoryEntityMap);
                    } else {
                        //List<SubCategory> subCategoriesMapList = new ArrayList<SubCategory>();
                        subCategoryEntityMap.put(row_subCategory, tariff);
                        categoryEntityMap.put(row_category, subCategoryEntityMap);
                    }
                    rate.setCategories(categoryEntityMap);
                }
                //set tax values
                rate.setTaxList(getRateTaxes(rateCardName));
            }
        } catch (SQLException e) {
            DBUtill.handleException("Error Occured due to Invalid Rate Configuration: ", e);
        } finally {
            DBUtill.closeAllConnections(preparedStatement, connection, resultSet);
        }
        return rate;
    }

    private Double nullCheck(ResultSet resultSet, String col) throws SQLException {
        Double nValue = resultSet.getDouble(col);
        return resultSet.wasNull() ? null : new Double(nValue);
    }

    @Override
    public List<Tax> getValidTaxRate(List<String> taxList, /*Date taxDate*/ java.sql.Date taxDate) throws DBUtilException {

        //String date = new SimpleDateFormat("yyyy-MM-dd").format(taxDate);
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        List<Tax> taxes = new ArrayList<Tax>();

        if (taxList != null && taxDate != null) {
            try {

                // CSV format surrounded by single quote
                String taxListStr = taxList.toString().replace("[", "'").replace("]", "'").replace(", ", "','");

                connection = DBUtill.getDBConnection();
                if (connection == null) {
                    throw new AnalyticsPricingException("Database Connection Cannot Be Established");
                }

                StringBuilder query = new StringBuilder();
                query.append("SELECT taxcode, ");
                query.append("       tax_validityval ");
                query.append("FROM tax ");
                query.append("INNER JOIN tax_validity ON tax.taxid=tax_validity.taxid ");
                query.append("WHERE tax.taxcode IN ($taxlist) ");
                query.append("  AND (tax_validity.tax_validityactdate <=? ");
                query.append("       AND tax_validity.tax_validitydisdate >=?)");

                preparedStatement = connection.prepareStatement(
                        query.toString().replace("$taxlist", taxListStr));

                preparedStatement.setDate(1, /*date*/ new java.sql.Date(taxDate.getTime()));
                preparedStatement.setDate(2, /*date*/ new java.sql.Date(taxDate.getTime()));

                resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    Tax tax = new Tax();
                    tax.setType(resultSet.getString("taxcode"));
                    tax.setValue(resultSet.getBigDecimal("tax_validityval"));
                    taxes.add(tax);

                }

            } catch (SQLException e) {
                DBUtill.handleException("Error occured getRateTaxes: ", e);
            } catch (AnalyticsPricingException ex) {
                Logger.getLogger(RateCardDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                DBUtill.closeAllConnections(preparedStatement, connection, resultSet);
            }
        }
        return taxes;
    }

    @Override
    public void insertRateCard(ChargeRate chargeRate) throws AnalyticsPricingException, DBUtilException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        Integer currencyid = null;
        Integer rateTypeid = null;
        Integer defaultTariffId = null;
        BigDecimal defaultVal = null;
        String chargeRateType = null;
        List<Integer> taxListIds = null;

        try {

            connection = DBUtill.getDBConnection();
            if (connection == null) {
                throw new AnalyticsPricingException("database connection cannot be established");
            }

            isRateAvailable(connection, chargeRate.getName());

            currencyid = getCurrencyId(connection, chargeRate.getCurrency());

            rateTypeid = getRateTypeId(connection, chargeRateType);

            defaultTariffId = setDefaultTariff(connection, chargeRate);

            if (defaultTariffId == null) {
                throw new AnalyticsPricingException("Tariff defined is not valid");
            }

            //insert into rate_def and get newly added rate card id.
            Integer newRateDefId = inserttoRateDef(connection, chargeRate, currencyid, rateTypeid, defaultTariffId);

            //category subcategory mapping
            categoryLevelMapping(connection, chargeRate, newRateDefId);

            //get tax list
            taxListIds = getTaxRates(connection, chargeRate);
            inserttoTax(connection, taxListIds, newRateDefId);

        } catch (SQLException e) {
            DBUtill.handleException("error occoured while getting ratecard :", e);
        } finally {
            DBUtill.closeAllConnections(preparedStatement, connection, resultSet);
        }
    }

    /*this should be implemented.
     user need to call this api if they need to
     add new tariff schema to their ratecard*/
    public void insertTariff() {
    }

    private Object getCategoryid(Connection connection, String categoryName) throws AnalyticsPricingException, DBUtilException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        Integer categoryId = null;

        try {
            String query_catids = "SELECT categoryid "
                    + "FROM category "
                    + "WHERE categorycode = ?";
            preparedStatement = connection.prepareStatement(query_catids);
            preparedStatement.setString(1, categoryName);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                categoryId = new Integer(resultSet.getInt("categoryid"));
            } else {
                throw new AnalyticsPricingException("No Such Category Found");
            }
        } catch (SQLException e) {
            DBUtill.handleException("Error Occoured While Retreving Data ", e);
        } finally {
            DBUtill.closeResutl_statment(preparedStatement, resultSet);
        }
        return categoryId;
    }

    private Integer getCategoryTariff(Connection connection, Object subCategoryVal) throws AnalyticsPricingException, DBUtilException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        Integer tariffid = null;

        try {
            if (subCategoryVal instanceof String) {
                String qurey_default = "SELECT tariffid "
                        + "FROM tariff "
                        + "WHERE tariffdefaultval = ? "
                        + "  AND tariffmaxcount IS NULL "
                        + "  AND tariffexcessrate IS NULL "
                        + "  AND tariffdefrate IS NULL "
                        + "  AND tariffspcommission IS NULL "
                        + "  AND tariffopcocommission IS NULL "
                        + "  AND tariffadscommission IS NULL "
                        + "  AND tariffsurchargeval IS NULL "
                        + "  AND tariffsurchargeAds IS NULL "
                        + "  AND tariffsurchargeOpco IS NULL ";
                preparedStatement = connection.prepareStatement(qurey_default);
                preparedStatement.setString(1, (String) subCategoryVal);

            } else if (subCategoryVal instanceof HashMap) {
                HashMap<String, String> attrMap = (HashMap<String, String>) subCategoryVal;
                String maxCount = attrMap.get("MaxCount");
                String excessRate = attrMap.get("ExcessRate");
                String defaultRate = attrMap.get("DefaultRate");

                String query_attr = "SELECT tariffid "
                        + "FROM tariff "
                        + "WHERE "
                        + "  AND tariffmaxcount = ? "
                        + "  AND tariffexcessrate = ? "
                        + "  AND tariffdefrate = ? "
                        + "  AND tariffspcommission IS NULL "
                        + "  AND tariffopcocommission IS NULL "
                        + "  AND tariffadscommission IS NULL "
                        + "  AND tariffsurchargeval IS NULL "
                        + "  AND tariffsurchargeAds IS NULL "
                        + "  AND tariffsurchargeOpco IS NULL ";

                preparedStatement = connection.prepareStatement(query_attr);
                preparedStatement.setString(1, maxCount);
                preparedStatement.setString(2, excessRate);
                preparedStatement.setString(3, defaultRate);

                resultSet = preparedStatement.executeQuery();
                if (resultSet != null) {

                } else {
                    throw new AnalyticsPricingException("Tariff is not defined");
                }

            } else if (subCategoryVal instanceof RateCommission) {
                RateCommission rateCommission = (RateCommission) subCategoryVal;
                BigDecimal spcCmmission = rateCommission.getSpCommission();
                BigDecimal opcoCommission = rateCommission.getOpcoCommission();
                BigDecimal adsCommission = rateCommission.getAdsCommission();

                String query_commission = "SELECT tariffid "
                        + "FROM tariff "
                        + "WHERE tariffspcommission = ? "
                        + "  AND tariffopcocommission = ? "
                        + "  AND tariffadscommission = ? "
                        + "  AND tariffdefaultval IS NULL "
                        + "  AND tariffmaxcount IS NULL "
                        + "  AND tariffexcessrate IS NULL "
                        + "  AND tariffdefrate IS NULL "
                        + "  AND tariffsurchargeval IS NULL "
                        + "  AND tariffsurchargeAds IS NULL "
                        + "  AND tariffsurchargeOpco IS NULL ";

                preparedStatement = connection.prepareStatement(query_commission);
                preparedStatement.setDouble(1, spcCmmission.doubleValue());
                preparedStatement.setDouble(2, opcoCommission.doubleValue());
                preparedStatement.setDouble(3, adsCommission.doubleValue());
            }

            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                tariffid = new Integer(resultSet.getInt("tariffid"));
            } else {
                throw new AnalyticsPricingException("No such Tariff");
            }
        } catch (SQLException e) {
            DBUtill.handleException("Error While reterving data", e);
        } finally {
            DBUtill.closeResutl_statment(preparedStatement, resultSet);
        }

        return tariffid;
    }

    private Integer inserttoRateDef(Connection connection, ChargeRate chargeRate, Integer currencyid, Integer rateTypeid, Integer defaultTariffid) throws AnalyticsPricingException, DBUtilException {
        PreparedStatement ps_insertdef = null;
        PreparedStatement ps_selectdef = null;
        ResultSet rs_selectdef = null;
        Integer newRateDefId = null;

        try {
            String insQueryRateDef = "INSERT INTO rate_def (rate_defname, rate_defdefault, currencyid, rate_typeid, rate_defcategorybase, tariffid) "
                    + "VALUES(?,?,?,?,?,?)";

            connection.setAutoCommit(false);
            ps_insertdef = connection.prepareStatement(insQueryRateDef);
            ps_insertdef.setString(1, chargeRate.getName());
            ps_insertdef.setBoolean(2, chargeRate.isDefault()); // check this correct or not
            if (currencyid == null) {
                ps_insertdef.setNull(3, Types.NULL);
            } else {
                ps_insertdef.setInt(3, currencyid);
            }
            ps_insertdef.setInt(4, rateTypeid.intValue());
            ps_insertdef.setBoolean(5, chargeRate.getCategoryBasedVal());
            ps_insertdef.setInt(6, defaultTariffid.intValue());

            ps_insertdef.executeUpdate();
            connection.commit();

            //get last inseerted rate_def id  TODO:read the manual for this
            String new_rateDefid = "SELECT last_insert_id() as new_id;";
            ps_selectdef = connection.prepareStatement(new_rateDefid);
            rs_selectdef = ps_selectdef.executeQuery();
            if (rs_selectdef.next()) {
                newRateDefId = new Integer(rs_selectdef.getInt("new_id"));
            }
        } catch (SQLException e) {

            DBUtill.handleException("rollebacked :", e);

        } finally {
            DBUtill.closeResutl_statment(ps_insertdef, rs_selectdef);
            DBUtill.closeResutl_statment(ps_selectdef, rs_selectdef);
        }
        return newRateDefId;
    }

    private void inserttoRateCategory(Connection connection, Integer rateDefId, Integer categoryId, Integer subCategoryId, Integer tariffId) throws AnalyticsPricingException, DBUtilException {
        PreparedStatement preparedStatement = null;

        try {
            String query_insertCatMapping = "INSERT INTO rate_category (rate_defid, parentcategoryid, childcategoryid, tariffid)"
                    + " VALUES (?,?,?,?)";
            preparedStatement = connection.prepareStatement(query_insertCatMapping);
            preparedStatement.setInt(1, rateDefId.intValue());
            preparedStatement.setInt(2, categoryId.intValue());

            //if category subcategory same means this is default tariff
            if (categoryId.intValue() == subCategoryId.intValue()) {
                preparedStatement.setNull(3, Types.NULL); // record 2n ekai wadinne me null haduwata passe
            } else {
                preparedStatement.setInt(3, subCategoryId);
            }

            preparedStatement.setInt(4, tariffId.intValue());

            preparedStatement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {

            DBUtill.handleException("rollebacked :", e);

        } finally {
            DBUtill.closeStatement(preparedStatement);
        }

    }

    private void isRateAvailable(Connection connection, String rateName) throws AnalyticsPricingException, DBUtilException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            //check ratecard is available
            String query_defAva = "SELECT rate_defid "
                    + "FROM rate_def "
                    + "WHERE rate_defname = ?";
            preparedStatement = connection.prepareStatement(query_defAva);
            preparedStatement.setString(1, rateName);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                throw new SQLException("rate card alerady avaliable");
            }
        } catch (SQLException e) {
            DBUtill.handleException("Error Occoured While Retreving Data", e);
        } finally {
            DBUtill.closeResutl_statment(preparedStatement, resultSet);
        }
    }

    private Integer getCurrencyId(Connection connection, String currency) throws AnalyticsPricingException, DBUtilException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        Integer currencyid = null;

        try {
            //get rate_currency
            String query_cur = "SELECT currencyid "
                    + "FROM currency "
                    + "WHERE currencycode = ? ";
            preparedStatement = connection.prepareStatement(query_cur);
            preparedStatement.setString(1, currency);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                currencyid = resultSet.getInt("currencyid");
            } else {
                throw new SQLException("Currency Type defined is not valid");
            }
        } catch (SQLException e) {
            DBUtill.handleException("Error Occoured While Retreving Data", e);
        } finally {
            DBUtill.closeResutl_statment(preparedStatement, resultSet);
        }
        return currencyid;
    }

    private Integer getRateTypeId(Connection connection, String chargeRateType) throws AnalyticsPricingException, DBUtilException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        Integer rateTypeid = null;

        try {
            //get rate_type
            String query_rt = "SELECT rate_typeid "
                    + "FROM rate_type "
                    + "WHERE  rate_typecode = ? ";
            preparedStatement = connection.prepareStatement(query_rt);
            preparedStatement.setString(1, chargeRateType);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                rateTypeid = new Integer(resultSet.getInt("rate_typeid"));
            } else {
                throw new SQLException("Rate Type is not valid");
            }
        } catch (SQLException e) {
            DBUtill.handleException("Error While Retreving Data ", e);
        } finally {
            DBUtill.closeResutl_statment(preparedStatement, resultSet);
        }
        return rateTypeid;
    }

    private Integer setDefaultTariff(Connection connection, ChargeRate chargeRate) throws AnalyticsPricingException, DBUtilException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        Integer defaultTariffId = null;

        try {

            RateCommission def_commission = chargeRate.getCommission();
            Map<String, String> def_rateAttr = chargeRate.getRateAttributes();

            //set default tariff id in reate_def table
            if (def_commission != null) {
                BigDecimal spCommission = def_commission.getSpCommission();
                BigDecimal opcoCommission = def_commission.getOpcoCommission();
                BigDecimal hubCommission = def_commission.getAdsCommission();

                String query = "SELECT tariffid "
                        + "FROM tariff "
                        + "WHERE tariffdefaultval = ? "
                        + "  AND tariffspcommission = ? "
                        + "  AND tariffopcocommission = ? "
                        + "  AND tariffadscommission = ?";

                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, chargeRate.getValue().toString());
                preparedStatement.setString(2, spCommission.toString());
                preparedStatement.setString(3, opcoCommission.toString());
                preparedStatement.setString(4, hubCommission.toString());

                resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    defaultTariffId = new Integer(resultSet.getInt("tariffid"));
                }

            } else if (def_rateAttr != null) {
                String maxCount = def_rateAttr.get("MaxCount");
                String excessRate = def_rateAttr.get("ExcessRate");
                String defaultRate = def_rateAttr.get("DefaultRate");

                String query = "SELECT tariffid "
                        + "FROM tariff "
                        + "WHERE tariffdefaultval = ? "
                        + "  AND tariffmaxcount = ? "
                        + "  AND tariffexcessrate = ? "
                        + "  AND tariffdefrate = ?";

                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, chargeRate.getValue().toString());
                preparedStatement.setString(2, maxCount);
                preparedStatement.setString(3, excessRate);
                preparedStatement.setString(4, defaultRate);

                resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    defaultTariffId = new Integer(resultSet.getInt("tariffid"));
                } else {
                    throw new AnalyticsPricingException("Tariff Id Cannot Be found in Database");
                }
            } else {
                String query = "SELECT tariffid "
                        + "FROM tariff "
                        + "WHERE tariffdefaultval = ? "
                        + "  AND tariffmaxcount IS NULL "
                        + "  AND tariffexcessrate IS NULL "
                        + "  AND tariffdefrate IS NULL "
                        + "  AND tariffspcommission IS NULL "
                        + "  AND tariffopcocommission IS NULL "
                        + "  AND tariffadscommission IS NULL "
                        + "  AND tariffsurchargeval IS NULL "
                        + "  AND tariffsurchargeAds IS NULL "
                        + "  AND tariffsurchargeOpco IS NULL";

                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, chargeRate.getValue().toString());
                resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    defaultTariffId = new Integer(resultSet.getInt("tariffid"));
                } else {
                    throw new AnalyticsPricingException("Tariff Id Cannot Be found in Database");
                }
            }

            SurchargeEntity surchargeEntity = chargeRate.getSurchargeEntity();
            //surchage only comes for per_request. it do not contain or commissions
            if (surchargeEntity != null) {
                String surchage_opco = surchargeEntity.getSurchargeElementOpco();
                String surcharge_hub = surchargeEntity.getSurchargeElementAds();
                String surcharge_val = surchargeEntity.getSurchargeElementValue();

                String query_surchage = "SELECT tariffid "
                        + "FROM tariff "
                        + "WHERE tariffdefaultval = ? "
                        + "  AND tariffsurchargeAds = ? "
                        + "  AND tariffsurchargeval = ? "
                        + "  AND tariffsurchargeOpco = ?";

                preparedStatement = connection.prepareStatement(query_surchage);
                preparedStatement.setString(1, chargeRate.getValue().toString());
                preparedStatement.setString(2, surcharge_hub);
                preparedStatement.setString(3, surcharge_val);
                preparedStatement.setString(4, surchage_opco);
                resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    defaultTariffId = new Integer(resultSet.getInt("tariffid"));
                } else {
                    throw new AnalyticsPricingException("Tariff Id Cannot Be found in Database");
                }
            }
        } catch (SQLException e) {
            DBUtill.handleException("Error While Retreving Data ", e);
        } finally {
            DBUtill.closeResutl_statment(preparedStatement, resultSet);
        }
        return defaultTariffId;
    }

    private List<Integer> getTaxRates(Connection connection, ChargeRate chargeRate) throws AnalyticsPricingException, DBUtilException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        //get tax list
        List<String> taxes = chargeRate.getTaxList();
        List<Integer> taxListIds = new ArrayList<Integer>();

        try {
            for (String tax : taxes) {
                String query_tax = "SELECT taxid "
                        + "FROM tax "
                        + "WHERE taxcode=?";
                preparedStatement = connection.prepareStatement(query_tax);
                preparedStatement.setString(1, tax);
                resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    taxListIds.add(new Integer(resultSet.getInt("taxid")));
                } else {
                    throw new AnalyticsPricingException("Tax Id Cannot Be found in Database");
                }
            }
        } catch (SQLException e) {
            DBUtill.handleException("Error While Retreving Data ", e);
        } finally {
            DBUtill.closeResutl_statment(preparedStatement, resultSet);
        }

        return taxListIds;
    }

    private void categoryLevelMapping(Connection connection, ChargeRate chargeRate, Integer newRateDefId) throws AnalyticsPricingException, DBUtilException {
        Map<String, Object> categoryMap = chargeRate.getCategories();

        if (categoryMap != null) {
            for (Map.Entry<String, Object> catrgoryEntity : categoryMap.entrySet()) {
                String categoryName = catrgoryEntity.getKey();
                HashMap<String, Object> category = (HashMap<String, Object>) catrgoryEntity.getValue();
                Object categoryid = getCategoryid(connection, categoryName);

                if (categoryid != null) {
                    for (Map.Entry<String, Object> subCategoryEntity : category.entrySet()) {
                        //get subcategory
                        //get value of tariff. category sub category id ganna
                        //pick sub category and its value
                        String subCategoryName = subCategoryEntity.getKey();
                        Object subCategoryVal = subCategoryEntity.getValue(); // sub cat value
                        if (subCategoryName.equalsIgnoreCase("_default_")) {
                            subCategoryName = categoryName;
                        }
                        //get subcategory ids
                        Object subcategoryId = getCategoryid(connection, subCategoryName);

                        if (subcategoryId != null) {
                            Integer categoryTariff = getCategoryTariff(connection, subCategoryVal);
                            //categoryid. subcategoryid,tariff
                            inserttoRateCategory(connection, newRateDefId, (Integer) categoryid, (Integer) subcategoryId, categoryTariff);
                        } else {
                            throw new AnalyticsPricingException("No Such Category");
                        }
                    }

                } else {
                    throw new AnalyticsPricingException("No Such Category");
                }
            }
        }
    }

    private void inserttoTax(Connection connection, List<Integer> taxIds, Integer newRateDefId) throws AnalyticsPricingException, DBUtilException {
        PreparedStatement preparedStatement = null;

        String query_insertTax = "INSERT INTO rate_taxes (rate_defid, taxid)"
                + " VALUES (?,?)";
        Iterator<Integer> taxIterator = taxIds.iterator();

        try {

            preparedStatement = connection.prepareStatement(query_insertTax);

            while (taxIterator.hasNext()) {
                Integer taxId = taxIterator.next();

                preparedStatement.setInt(1, newRateDefId.intValue());
                preparedStatement.setInt(2, taxId.intValue());

                preparedStatement.executeUpdate();

            }
        } catch (SQLException e) {
            DBUtill.handleException("ROllebacked", e);
        } finally {
            DBUtill.closeStatement(preparedStatement);
        }

    }

}
