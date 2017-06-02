package org.wso2telco.analytics.pricing.service;

import com.wso2telco.analytics.DBUtill;
import com.wso2telco.analytics.exception.DBUtilException;
import com.wso2telco.analytics.util.DataSourceNames;
import org.wso2telco.analytics.pricing.AnalyticsPricingException;
import org.wso2telco.analytics.pricing.service.dao.RateCardDAO;

import java.math.BigDecimal;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.sql.Date;
import org.wso2telco.analytics.pricing.Tax;

/*
****
TODO:use constant insetad of coloum names
 */
public class RateCardDAOImpl implements RateCardDAO {

    //TO-DO--------------- delete later ------------
    public Connection getcon(Connection con) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/rate_db", "root", "red7Top");
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
            //connection = getcon(connection);
            connection = DBUtill.getDBConnection();
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

            //connection = getcon(connection);
            connection = DBUtill.getDBConnection();

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
                        attributesMap.put("MaxCount", row_maxCount.toString());
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
                            attributesMap.put("MaxCount", row_maxCount.toString());
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
                            subCategoriesMap.put("MaxCount", row_maxCount.toString());
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
}
