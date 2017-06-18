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

/*
****
TODO:use constant insetad of coloum names
 */
public class RateCardDAOImpl implements RateCardDAO {

    //TO-DO--------------- delete later ------------
    public Connection getcon(Connection con) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/rate_db", "root", "root");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return con;
    }
    //----------------------------------

    @Override
    public Object getNBRateCard(String operationId, String applicationId, String api, String category, String subCategory) throws Exception {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        ChargeRate rate = null;
        Integer rateDefID = null;

        try {
            connection = DBUtill.getDBConnection();
            // connection = getcon(connection);
            if (connection == null) {
                throw new Exception("Database Connection Cannot Be Established");
            }

            //get rate def id from sub_rate_nb table
            String nbQuery = "select rate_defid from api_operation ao, sub_rate_nb rnb, api a "
                    + "where ao.api_operationid = rnb.api_operationid "
                    + "and a.apiid = ao.apiid "
                    + "and rnb.applicationid =? "
                    + "and a.apiname = ? "
                    + "and ao.api_operation = ? ";

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
    public Object getSBRateCard(String operatorId, String operationId, String applicationId, String api, String category, String subCategory) throws Exception {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        ChargeRate rate = null;
        int rateDefID = 0;

        try {
            //connection = getcon(connection);
            connection = DBUtill.getDBConnection();
            if (connection == null) {
                throw new Exception("Database Connection Cannot Be Established");
            }

            String sbQuery = "SELECT rate_defid FROM api_operation ao, sub_rate_sb srb, api a "
                    + "WHERE ao.api_operationid = srb.api_operationid "
                    + "AND srb.applicationid =? "
                    + "AND srb.operatorid =? "
                    + "AND a.apiname = ? "
                    + "AND ao.api_operation = ? ";

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

        } catch (Exception e) {
            DBUtill.handleException("Error occured getSBRateCard: ", e);
        } finally {
            DBUtill.closeAllConnections(preparedStatement, connection, resultSet);
        }
        return rate;
    }

    private ArrayList<String> getRateTaxes(String rateName) throws Exception {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String taxCode = null;
        ArrayList<String> taxes = new ArrayList<String>();

        try {
            connection = getcon(connection);
            //connection = DBUtill.getDBConnection();
            if (connection == null) {
                throw new Exception("Database Connection Cannot Be Established");
            }

            StringBuilder query = new StringBuilder("SELECT tax.taxcode ");
            query.append("FROM (tax ");
            query.append("INNER JOIN rate_taxes on tax.taxid=rate_taxes.taxid) ");
            query.append("INNER JOIN rate_def on rate_def.rate_defid=rate_taxes.rate_defid ");
            query.append("where rate_def.rate_defname= ?");

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

    private ChargeRate executeQuery(int rateDefID, String category, String subCategory) throws Exception {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        ChargeRate rate = null;

        try {

            boolean isCategory = false;
            boolean isSubcategory = false;

            if (category != null && category.length() > 0) {
                isCategory = true;
            }

            if (subCategory != null && subCategory.length() > 0) {
                isSubcategory = true;
            }

            //connection = DBUtill.getDBConnection();
            if (connection == null) {
                throw new Exception("database connection cannot be established");
            }

            StringBuilder query = new StringBuilder("select rate_defname, rt.rate_typecode, rd.rate_defdefault, c.currencycode,rd.rate_defcategorybase,cat,sub,tariffname,");
            query.append("tariffdesc, tariffdefaultval, tariffmaxcount, tariffexcessrate, tariffdefrate, tariffspcommission, tariffadscommission,");
            query.append("tariffopcocommission, tariffsurchargeval, tariffsurchargeAds, tariffsurchargeOpco ");
            query.append("from rate_def rd,currency c,tariff tr,rate_type rt,");
            query.append("(SELECT rate_defid, Null as cat, Null as sub,tariffid from rate_def where rate_defid =? ");
            query.append("union all");
            query.append("(select rate_defid, (select categorycode from category where rc.parentcategoryid = category.categoryid) as cat,");
            query.append("(select categorycode from category where rc.childcategoryid = category.categoryid) as sub,");
            query.append("tariffid from rate_category rc where rc.rate_defid = ?)");
            query.append(") ct ");
            query.append("where ct.rate_defid = rd.rate_defid ");
            query.append("and rd.currencyid = c.currencyid ");
            query.append("and ct.tariffid = tr.tariffid ");
            query.append("and rd.rate_typeid = rt.rate_typeid ");

            if (isCategory && isSubcategory) {
                query.append("and ct.cat = ? and ct.sub = ?");
            } else if (isCategory) {
                query.append("and ct.cat = ? and ct.sub is null");
            } else {
                query.append("and ct.cat is null and ct.sub is null");
            }

            preparedStatement = connection.prepareStatement(query.toString());
            preparedStatement.setInt(1, rateDefID);
            preparedStatement.setInt(2, rateDefID);

            if (isCategory && isSubcategory) {
                preparedStatement.setString(3, category);
                preparedStatement.setString(4, subCategory);
            } else if (isCategory) {
                preparedStatement.setString(3, category);
            }

            resultSet = preparedStatement.executeQuery();
            //connection.commit();

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

                if (!isCategory && !isSubcategory) {

                    //value element in every rate element
                    rate.setValue(BigDecimal.valueOf(row_tariffDefaultVal));

                    //if rate type is QUOTA
                    if (row_maxCount != null || row_excessRate != null || row_attrDefRate != null) {
                        Map<String, String> attributesMap = new HashMap<String, String>();
                        attributesMap.put("MaxCount", String.valueOf(row_maxCount.intValue()));
                        attributesMap.put("ExcessRate", row_excessRate.toString());
                        attributesMap.put("DefaultRate", row_attrDefRate.toString());

                        rate.setRateAttributes(attributesMap);

                    } else if (row_spCommission != null || row_adsCommission != null || row_opcoCommission != null) {
                        RateCommission rateCommission = new RateCommission();
                        rateCommission.setSpCommission(new BigDecimal(row_spCommission));
                        rateCommission.setAdsCommission(new BigDecimal(row_adsCommission));
                        rateCommission.setOpcoCommission(new BigDecimal(row_opcoCommission));

                        rate.setCommission(rateCommission);
                    }

                    //surcharge values
                    if (row_surchargeVal != null || row_surchargeAds != null || row_surchargeOpco != null) {
                        SurchargeEntity surchargeEntity = new SurchargeEntity();
                        surchargeEntity.setSurchargeElementValue(row_surchargeVal.toString());
                        surchargeEntity.setSurchargeElementAds(row_surchargeAds.toString());
                        surchargeEntity.setSurchargeElementOpco(row_surchargeOpco.toString());
                        rate.setSurchargeEntity(surchargeEntity);
                    }

                } else if (isCategory) {

                    Map<String, Object> categoryEntityMap = new HashMap<String, Object>();
                    Map<String, Object> subCategoryEntityMap = new HashMap<String, Object>();

                    if (!isSubcategory) {
                        if (row_tariffDefaultVal != null) {
                            subCategoryEntityMap.put("__default__", row_tariffDefaultVal.toString());
                            categoryEntityMap.put(row_category, subCategoryEntityMap);

                        } else if (row_maxCount != null || row_excessRate != null || row_attrDefRate != null) {
                            Map<String, String> attributesMap = new HashMap<String, String>();
                            attributesMap.put("MaxCount", String.valueOf(row_maxCount.intValue()));
                            attributesMap.put("ExcessRate", row_excessRate.toString());
                            attributesMap.put("DefaultRate", row_attrDefRate.toString());

                            subCategoryEntityMap.put("__default__", attributesMap);
                            categoryEntityMap.put(row_category, attributesMap);

                        } else if (row_spCommission != null || row_adsCommission != null || row_opcoCommission != null) {
                            RateCommission rateCommission = new RateCommission();
                            rateCommission.setSpCommission(new BigDecimal(row_spCommission));
                            rateCommission.setAdsCommission(new BigDecimal(row_adsCommission));
                            rateCommission.setOpcoCommission(new BigDecimal(row_opcoCommission));

                            subCategoryEntityMap.put("__default__", rateCommission);
                            categoryEntityMap.put(row_category, subCategoryEntityMap);
                        }
                        //surcharge values
                        if (row_surchargeVal != null || row_surchargeAds != null || row_surchargeOpco != null) {
                            SurchargeEntity surchargeEntity = new SurchargeEntity();
                            surchargeEntity.setSurchargeElementValue(row_surchargeVal.toString());
                            surchargeEntity.setSurchargeElementAds(row_surchargeAds.toString());
                            surchargeEntity.setSurchargeElementOpco(row_surchargeOpco.toString());
                            rate.setSurchargeEntity(surchargeEntity);
                        }

                    } else {
                        List<SubCategory> subCategoriesMapList = new ArrayList<SubCategory>();

                        if (row_tariffDefaultVal != null) {
                            subCategoryEntityMap.put(row_subCategory, row_tariffDefaultVal.toString());
                            categoryEntityMap.put(row_category, subCategoryEntityMap);

                        } else if (row_maxCount != null || row_excessRate != null || row_attrDefRate != null) {
                            Map<String, String> subCategoriesMap = new HashMap<String, String>();
                            subCategoriesMap.put("MaxCount", String.valueOf(row_maxCount.intValue()));
                            subCategoriesMap.put("ExcessRate", row_excessRate.toString());
                            subCategoriesMap.put("DefaultRate", row_attrDefRate.toString());

                            subCategoryEntityMap.put(row_subCategory, subCategoriesMap);
                            categoryEntityMap.put(row_category, subCategoriesMap);

                        } else if (row_spCommission != null || row_adsCommission != null || row_opcoCommission != null) {
                            RateCommission subRateCommission = new RateCommission();
                            subRateCommission.setSpCommission(new BigDecimal(row_spCommission));
                            subRateCommission.setAdsCommission(new BigDecimal(row_adsCommission));
                            subRateCommission.setOpcoCommission(new BigDecimal(row_opcoCommission));

                            subCategoryEntityMap.put(row_subCategory, subRateCommission);
                            categoryEntityMap.put(row_category, subCategoryEntityMap);
                        }

                        //surcharge values
                        if (row_surchargeVal != null || row_surchargeAds != null || row_surchargeOpco != null) {
                            SurchargeEntity surchargeEntity = new SurchargeEntity();
                            surchargeEntity.setSurchargeElementValue(row_surchargeVal.toString());
                            surchargeEntity.setSurchargeElementAds(row_surchargeAds.toString());
                            surchargeEntity.setSurchargeElementOpco(row_surchargeOpco.toString());
                            rate.setSurchargeEntity(surchargeEntity);
                        }

                    }
                    rate.setCategories(categoryEntityMap);
                }
                //set tax values
                rate.setTaxList(getRateTaxes(rateCardName));
            }
        } catch (Exception e) {
            DBUtill.handleException("Error occured getRateTaxes: ", e);
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
    public List<Tax> getValidTaxRate(List<String> taxList, /*Date taxDate*/ java.sql.Date taxDate) throws Exception {

        //String date = new SimpleDateFormat("yyyy-MM-dd").format(taxDate);
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        List<Tax> taxes = new ArrayList<Tax>();

        if (taxList != null && taxDate != null) {
            try {

                // CSV format surrounded by single quote
                String taxListStr = taxList.toString().replace("[", "'").replace("]", "'").replace(", ", "','");

                //  connection = getcon(connection);
                connection = DBUtill.getDBConnection();
                if (connection == null) {
                    throw new Exception("Database Connection Cannot Be Established");
                }

                StringBuilder query = new StringBuilder("select taxcode,tax_validityval ");
                query.append("from tax ");
                query.append("inner join tax_validity on tax.taxid=tax_validity.taxid ");
                query.append("Where tax.taxcode IN (" + taxListStr + ") ");
                query.append("AND ");
                query.append("(tax_validity.tax_validityactdate <=? AND tax_validity.tax_validitydisdate >=? );");

                preparedStatement = connection.prepareStatement(query.toString());
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
            } finally {
                DBUtill.closeAllConnections(preparedStatement, connection, resultSet);
            }
        }
        return taxes;
    }

    //get Rate card by given name.use for rest API
    @Override
    public Object getRateByName(String rateName) throws Exception {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        Integer rate_defid = null;
        Integer rate_default = null;
        String currency = null;
        String rateType = null;
        Integer defaultCategoryBase = null;
        Integer defaulttariff = null;

        Map<String, Object> categoryEntityMap = new HashMap<String, Object>();
        Map<String, Object> subCategoryEntityMap = new HashMap<String, Object>();

        ChargeRate rate = null;
        String category = null;

        try {
            connection = getcon(connection);
            //connection = DBUtill.getDBConnection();
            if (connection == null) {
                throw new Exception("database connection cannot be established");
            }

            StringBuilder query = new StringBuilder();
            query.append("SELECT rd.rate_defid,rd.rate_defdefault,(select currencycode from rate_db.currency where currencyid = rd.currencyid) as currency,");
            query.append("(SELECT rate_typecode from rate_type where rate_typeid = rd.rate_typeid) as ratetype,");
            query.append("rd.rate_defcategorybase,");
            query.append("rd.tariffid,");
            query.append("(SELECT category.categorycode from category where categoryid = rc.parentcategoryid) as parentcat,");
            query.append("(SELECT category.categorycode from category where categoryid = rc.childcategoryid) as subcat,");
            query.append("rc.tariffid,");
            query.append("t.tariffdefaultval,t.tariffmaxcount, t.tariffexcessrate, t.tariffdefrate, t.tariffspcommission, t.tariffopcocommission, t.tariffadscommission,");
            query.append("t.tariffsurchargeval, t.tariffsurchargeOpco, t.tariffsurchargeAds ");
            query.append("from ((rate_def rd ");
            query.append("inner join ");
            query.append("rate_category rc ");
            query.append("ON ");
            query.append("rd.rate_defid = rc.rate_defid) ");
            query.append("inner join tariff t on rc.tariffid = t.tariffid)");
            query.append("where rate_defname = ?;");

            preparedStatement = connection.prepareStatement(query.toString());
            preparedStatement.setString(1, rateName);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                rate = new ChargeRate(rateName);
                rate_defid = new Integer(resultSet.getInt("rate_defid"));
                rate_default = new Integer(resultSet.getInt("rate_defdefault"));
                currency = resultSet.getString("currency");
                rateType = resultSet.getString("ratetype");
                defaultCategoryBase = new Integer(resultSet.getInt("rate_defcategorybase"));
                defaulttariff = new Integer(resultSet.getInt("tariffid"));

                category = resultSet.getString("parentcat");
                String subCategory = resultSet.getString("subcat");
                Double row_tariffDefVal = nullCheck(resultSet, "tariffdefaultval");
                Double row_maxCount = nullCheck(resultSet,"tariffmaxcount");
                Double row_excessRate = nullCheck(resultSet, "tariffexcessrate");
                Double row_attrDefRate = nullCheck(resultSet, "tariffdefrate");
                Double row_spCommission = nullCheck(resultSet, "tariffspcommission");
                Double row_adsCommission = nullCheck(resultSet, "tariffadscommission");
                Double row_opcoCommission = nullCheck(resultSet, "tariffopcocommission");
                Double row_surchargeVal = nullCheck(resultSet, "tariffsurchargeval");
                Double row_surchargeAds = nullCheck(resultSet, "tariffsurchargeAds");
                Double row_surchargeOpco = nullCheck(resultSet, "tariffsurchargeOpco");

                if (category == null && subCategory == null) {
                    //TODO:can it be null values in sp commissions and attributes
                    //default rate val
                } else if ((category != null ) && (subCategory == null)) {
                    if (row_spCommission != null && row_opcoCommission != null && row_adsCommission != null) {
                        RateCommission rateCommission = new RateCommission();
                        rateCommission.setSpCommission(new BigDecimal(row_spCommission));
                        rateCommission.setOpcoCommission(new BigDecimal(row_opcoCommission));
                        rateCommission.setAdsCommission(new BigDecimal(row_adsCommission));

                        subCategoryEntityMap.put("_default_", rateCommission);
                    } else if (row_maxCount != null && row_excessRate != null && row_attrDefRate != null) {
                        Map<String, String> attributeMap = new HashMap<String,String>();
                        attributeMap.put("MaxCount", String.valueOf(row_maxCount.intValue()));
                        attributeMap.put("ExcessRate", row_excessRate.toString());
                        attributeMap.put("DefaultRate", row_attrDefRate.toString());

                        subCategoryEntityMap.put("_default_",attributeMap);
                    } else {
                        subCategoryEntityMap.put("_default_", row_tariffDefVal);
                    }


                } else if ((category != null) && (subCategory != null)) {
                    if (row_spCommission != null && row_opcoCommission != null && row_adsCommission != null) {
                        RateCommission rateCommission = new RateCommission();
                        rateCommission.setSpCommission(new BigDecimal(row_spCommission));
                        rateCommission.setOpcoCommission(new BigDecimal(row_opcoCommission));
                        rateCommission.setAdsCommission(new BigDecimal(row_adsCommission));

                        subCategoryEntityMap.put(subCategory, rateCommission);
                    } else if (row_maxCount != null && row_excessRate != null && row_attrDefRate != null) {
                        Map<String, String> attributeMap = new HashMap<String,String>();
                        attributeMap.put("MaxCount", String.valueOf(row_maxCount.intValue()));
                        attributeMap.put("ExcessRate", row_excessRate.toString());
                        attributeMap.put("DefaultRate", row_attrDefRate.toString());

                        subCategoryEntityMap.put(subCategory, attributeMap);
                    } else {
                        subCategoryEntityMap.put(subCategory, row_tariffDefVal);
                    }
                }

                if (row_surchargeAds != null && row_surchargeOpco != null && row_surchargeVal != null) {
                    SurchargeEntity surchargeEntity = new SurchargeEntity();
                    surchargeEntity.setSurchargeElementOpco(row_surchargeOpco.toString());
                    surchargeEntity.setSurchargeElementAds(row_surchargeAds.toString());
                    surchargeEntity.setSurchargeElementValue(row_surchargeVal.toString());

                    rate.setSurchargeEntity(surchargeEntity);
                }

                if (category != null && !category.isEmpty()) {
                    categoryEntityMap.put(category, subCategoryEntityMap);
                }
            }

            if (rate_defid != null) {
                double def_defVal = 0;
                int def_maxCount = 0;
                Double def_excessrate = null;
                Double def_rate = null;
                Double def_spcommission = null;
                Double def_opcocommission = null;
                Double def_hubcomission = null;

                String def_query = "SELECT tariffdefaultval, tariffmaxcount, tariffexcessrate, tariffdefrate,\n" +
                        "tariffspcommission, tariffopcocommission, tariffadscommission from tariff\n" +
                        "where tariffid=?";

                preparedStatement = connection.prepareStatement(def_query);
                preparedStatement.setInt(1,defaulttariff.intValue());
                resultSet = preparedStatement.executeQuery();

                //default values
                if (resultSet.next()) {
                    def_defVal = nullCheck(resultSet,"tariffdefaultval");
                    def_maxCount = resultSet.getInt(("tariffmaxcount"));
                    def_excessrate = nullCheck(resultSet, "tariffexcessrate");
                    def_rate = nullCheck(resultSet, "tariffdefrate");
                    def_spcommission = nullCheck(resultSet, "tariffspcommission");
                    def_opcocommission = nullCheck(resultSet, "tariffopcocommission");
                    def_hubcomission = nullCheck(resultSet, "tariffadscommission");
                }

                if (def_spcommission != null && def_opcocommission != null && def_hubcomission != null) {
                    RateCommission rateCommission = new RateCommission();
                    rateCommission.setSpCommission(new BigDecimal(def_spcommission));
                    rateCommission.setOpcoCommission(new BigDecimal(def_opcocommission));
                    rateCommission.setAdsCommission(new BigDecimal(def_hubcomission));

                    rate.setCommission(rateCommission);

                } else if (def_maxCount != 0 && def_excessrate != null && def_rate != null) {
                    Map<String, String> attributeMap = new HashMap<String, String>();
                    attributeMap.put("MaxCount", String.valueOf(def_maxCount));
                    attributeMap.put("ExcessRate", def_excessrate.toString());
                    attributeMap.put("DefaultRate", def_rate.toString());

                    rate.setRateAttributes(attributeMap);
                }

                rate.setDefault(setBooleanVal(rate_default.intValue()));
                rate.setCurrency(currency);
                rate.setType(RateType.getEnum(rateType));
                rate.setCategoryBasedVal(setBooleanVal(defaultCategoryBase.intValue()));
                rate.setValue(new BigDecimal(def_defVal));
                rate.setTaxList(getRateTaxes(rateName));
                rate.setCategories(categoryEntityMap);
            } else {
                return null;
            }


        } catch (SQLException e) {
            DBUtill.handleException("error occoured while getting ratecard :", e);
        } finally {
            DBUtill.closeAllConnections(preparedStatement, connection, resultSet);
        }
        return rate;
    }

    @Override
    public void insertRateCard(ChargeRate chargeRate) throws Exception {
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
            connection = getcon(connection);
            //connection = DBUtill.getDBConnection();
            if (connection == null) {
                throw new Exception("database connection cannot be established");
            }

            isRateAvailable(connection, chargeRate.getName());

            currencyid = getCurrencyId(connection, chargeRate.getCurrency());

            rateTypeid = getRateTypeId(connection, chargeRateType);

            defaultTariffId = setDefaultTariff(connection, chargeRate);

            if (defaultTariffId == null) {
                throw new SQLException("Tariff defined is not valid");
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
    public void insertTariff() {}

    private Object getCategoryid (Connection connection, String categoryName) throws Exception {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        Integer categoryId = null;

        try {
            String query_catids = "SELECT categoryid from category WHERE categorycode = ?";
            preparedStatement = connection.prepareStatement(query_catids);
            preparedStatement.setString(1, categoryName);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                categoryId = new Integer(resultSet.getInt("categoryid"));
            } else {
                throw new Exception("No Such Category Found");
            }
        } catch (SQLException e) {
            DBUtill.handleException("Error Occoured While Retreving Data ", e);
        } finally {
            DBUtill.closeResutl_statment(preparedStatement, resultSet);
        }
        return categoryId;
    }

    private Integer getCategoryTariff (Connection connection, Object subCategoryVal) throws Exception {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        Integer tariffid = null;

        try {
            if (subCategoryVal instanceof String) {
                String qurey_default = "SELECT tariffid FROM tariff WHERE\n" +
                        "\"tariffdefaultval = ? AND tariffmaxcount is null and tariffexcessrate is null and tariffdefrate is null\\n\" +\n" +
                        "\" and tariffspcommission is null and tariffopcocommission is null and tariffadscommission is null and\\n\" +\n" +
                        "\" tariffsurchargeval is null and tariffsurchargeAds is null and tariffsurchargeOpco is null;";
                preparedStatement = connection.prepareStatement(qurey_default);
                preparedStatement.setString(1, (String) subCategoryVal);

            } else if (subCategoryVal instanceof HashMap) {
                HashMap<String, String> attrMap = (HashMap<String, String>) subCategoryVal;
                String maxCount = attrMap.get("MaxCount");
                String excessRate = attrMap.get("ExcessRate");
                String defaultRate = attrMap.get("DefaultRate");

                String query_attr= "SELECT tariffid FROM tariff WHERE\n" +
                        "AND tariffmaxcount = ? AND tariffexcessrate = ? AND tariffdefrate = ?\n" +
                        "and tariffspcommission is null and tariffopcocommission is null and tariffadscommission is null and\n" +
                        "tariffsurchargeval is null and tariffsurchargeAds is null and tariffsurchargeOpco is null;";
                preparedStatement = connection.prepareStatement(query_attr);
                preparedStatement.setString(1, maxCount);
                preparedStatement.setString(2, excessRate);
                preparedStatement.setString(3, defaultRate);

                resultSet = preparedStatement.executeQuery();
                if (resultSet != null) {

                } else {
                    throw new Exception("Tariff is not defined");
                }

            } else if (subCategoryVal instanceof RateCommission) {
                RateCommission rateCommission = (RateCommission) subCategoryVal;
                BigDecimal spcCmmission = rateCommission.getSpCommission();
                BigDecimal opcoCommission = rateCommission.getOpcoCommission();
                BigDecimal adsCommission = rateCommission.getAdsCommission();

                String query_commission = "\n" +
                        "SELECT tariffid FROM tariff WHERE \n" +
                        "tariffspcommission = ? AND tariffopcocommission = ? AND tariffadscommission = ? AND\n" +
                        "tariffdefaultval is null AND tariffmaxcount is null AND tariffexcessrate is null AND tariffdefrate is null\n" +
                        "AND tariffsurchargeval is null AND tariffsurchargeAds is null AND tariffsurchargeOpco is null;\n";
                preparedStatement = connection.prepareStatement(query_commission);
                preparedStatement.setDouble(1, spcCmmission.doubleValue());
                preparedStatement.setDouble(2, opcoCommission.doubleValue());
                preparedStatement.setDouble(3, adsCommission.doubleValue());
            }

            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                tariffid = new Integer(resultSet.getInt("tariffid"));
            } else {
                throw new Exception("No such Tariff");
            }
        } catch (SQLException e) {
            DBUtill.handleException("Error While reterving data", e);
        } finally {
            DBUtill.closeResutl_statment(preparedStatement, resultSet);
        }

        return tariffid;
    }

    private Integer inserttoRateDef (Connection connection, ChargeRate chargeRate, Integer currencyid, Integer rateTypeid, Integer defaultTariffid) throws Exception {
        PreparedStatement ps_insertdef = null;
        PreparedStatement ps_selectdef = null;
        ResultSet rs_selectdef= null;
        Integer newRateDefId = null;

        try {
            String insQueryRateDef = "INSERT INTO rate_def (rate_defname, rate_defdefault, currencyid, rate_typeid, rate_defcategorybase, tariffid) \n" +
                    "VALUES(?,?,?,?,?,?)";

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
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    DBUtill.handleException("rollebacked :", ex);
                }
            }
        } finally {
            connection.setAutoCommit(true);
            DBUtill.closeResutl_statment(ps_insertdef, rs_selectdef);
            DBUtill.closeResutl_statment(ps_selectdef, rs_selectdef);
        }
        return newRateDefId;
    }

    private void inserttoRateCategory (Connection connection, Integer rateDefId, Integer categoryId, Integer subCategoryId, Integer tariffId) throws Exception {
        PreparedStatement preparedStatement = null;

        try {
            String query_insertCatMapping = "INSERT INTO rate_category (rate_defid, parentcategoryid, childcategoryid, tariffid) VALUES (?,?,?,?)";
            preparedStatement = connection.prepareStatement(query_insertCatMapping);
            preparedStatement.setInt(1, rateDefId.intValue());
            preparedStatement.setInt(2, categoryId.intValue());

            //if category subcategory same means this is default tariff
            if (categoryId.intValue() == subCategoryId.intValue()) {
                preparedStatement.setNull(3, Types.NULL); // record 2n ekai wadinne me null haduwata passe
            } else {
                preparedStatement.setInt(3,subCategoryId);
            }

            preparedStatement.setInt(4, tariffId.intValue());

            connection.setAutoCommit(false);
            preparedStatement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    DBUtill.handleException("rollebacked :", e);
                }
            }
        } finally {
            connection.setAutoCommit(true);
            DBUtill.closeStatement(preparedStatement);
        }

    }

    private void isRateAvailable (Connection connection, String rateName) throws Exception {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            //check ratecard is available
            String query_defAva = "SELECT rate_defid FROM rate_def where rate_defname = ?;";
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

    private Integer getCurrencyId (Connection connection, String currency) throws Exception {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        Integer currencyid = null;

        try {
            //get rate_currency
            String query_cur = "SELECT currencyid FROM currency WHERE currencycode = ? ";
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

    private Integer getRateTypeId (Connection connection, String chargeRateType) throws Exception {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        Integer rateTypeid = null;

        try {
            //get rate_type
            String query_rt = "SELECT rate_typeid FROM rate_type WHERE  rate_typecode = ? ";
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

    private Integer setDefaultTariff (Connection connection, ChargeRate chargeRate) throws Exception {
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

                String query = "SELECT tariffid from tariff where\n" +
                        "tariffdefaultval = ? and tariffspcommission = ? and tariffopcocommission = ? and tariffadscommission = ?";
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

                String query = "SELECT tariffid from tariff where\n" +
                        "tariffdefaultval = ? and tariffmaxcount = ? and tariffexcessrate = ? and tariffdefrate = ?";
                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, chargeRate.getValue().toString());
                preparedStatement.setString(2, maxCount);
                preparedStatement.setString(3, excessRate);
                preparedStatement.setString(4, defaultRate);

                resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    defaultTariffId = new Integer(resultSet.getInt("tariffid"));
                } else {
                    throw new Exception("Tariff Id Cannot Be found in Database");
                }
            } else {
                String query = "SELECT tariffid from tariff WHERE tariffdefaultval = ? and \n" +
                        "tariffmaxcount is null and tariffexcessrate is null and tariffdefrate is null and\n" +
                        "tariffspcommission is null and tariffopcocommission is null and tariffadscommission is null\n" +
                        "and tariffsurchargeval is null and tariffsurchargeAds is null and tariffsurchargeOpco is null;";
                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, chargeRate.getValue().toString());
                resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    defaultTariffId = new Integer(resultSet.getInt("tariffid"));
                } else {
                    throw new Exception("Tariff Id Cannot Be found in Database");
                }
            }

            SurchargeEntity surchargeEntity = chargeRate.getSurchargeEntity();
            //surchage only comes for per_request. it do not contain or commissions
            if (surchargeEntity != null) {
                String surchage_opco = surchargeEntity.getSurchargeElementOpco();
                String surcharge_hub = surchargeEntity.getSurchargeElementAds();
                String surcharge_val = surchargeEntity.getSurchargeElementValue();

                String query_surchage = "\n" +
                        "SELECT tariffid from tariff WHERE \n" +
                        "tariffdefaultval = ? and \n" +
                        "tariffsurchargeAds = ? and tariffsurchargeval = ? and tariffsurchargeOpco = ? ";
                preparedStatement = connection.prepareStatement(query_surchage);
                preparedStatement.setString(1, chargeRate.getValue().toString());
                preparedStatement.setString(2, surcharge_hub);
                preparedStatement.setString(3, surcharge_val);
                preparedStatement.setString(4, surchage_opco);
                resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    defaultTariffId = new Integer(resultSet.getInt("tariffid"));
                } else {
                    throw new Exception("Tariff Id Cannot Be found in Database");
                }
            }
        } catch (SQLException e) {
            DBUtill.handleException("Error While Retreving Data ",e);
        } finally {
            DBUtill.closeResutl_statment(preparedStatement, resultSet);
        }
        return defaultTariffId;
    }

    private List<Integer> getTaxRates (Connection connection, ChargeRate chargeRate) throws Exception {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        //get tax list
        List<String> taxes = chargeRate.getTaxList();
        List<Integer> taxListIds = new ArrayList<Integer>();

        try {
            for (String tax : taxes) {
                String query_tax = "SELECT taxid FROM tax WHERE taxcode=?";
                preparedStatement = connection.prepareStatement(query_tax);
                preparedStatement.setString(1, tax);
                resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    taxListIds.add(new Integer(resultSet.getInt("taxid")));
                } else {
                    throw new Exception("Tax Id Cannot Be found in Database");
                }
            }
        } catch (SQLException e) {
            DBUtill.handleException("Error While Retreving Data ", e);
        } finally {
            DBUtill.closeResutl_statment(preparedStatement, resultSet);
        }

        return taxListIds;
    }

    private void categoryLevelMapping (Connection connection, ChargeRate chargeRate, Integer newRateDefId) throws Exception {
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
                            throw new Exception("No Such Category");
                        }
                    }

                } else {
                    throw new Exception("No Such Category");
                }
            }
        }
    }

    private void inserttoTax (Connection connection, List<Integer> taxIds, Integer newRateDefId) throws Exception {
        PreparedStatement preparedStatement = null;

        String query_insertTax = "INSERT INTO rate_taxes (rate_defid, taxid) VALUES (?,?)";
        Iterator<Integer> taxIterator = taxIds.iterator();

        try {
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(query_insertTax);

            while (taxIterator.hasNext()) {
                Integer taxId = taxIterator.next();

                preparedStatement.setInt(1, newRateDefId.intValue());
                preparedStatement.setInt(2, taxId.intValue());

                preparedStatement.executeUpdate();
                connection.commit();
            }
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException e1) {
                    DBUtill.handleException("ROllebacked", e);
                }
            }
        } finally {
           connection.setAutoCommit(true);
           DBUtill.closeStatement(preparedStatement);
        }

    }
}