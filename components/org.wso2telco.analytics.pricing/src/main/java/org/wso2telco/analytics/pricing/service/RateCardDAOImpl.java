package org.wso2telco.analytics.pricing.service;

import com.wso2telco.analytics.DBUtill;
import com.wso2telco.analytics.exception.DBUtilException;
import com.wso2telco.analytics.util.DataSourceNames;
import org.wso2telco.analytics.pricing.AnalyticsPricingException;
import org.wso2telco.analytics.pricing.Tax;
import org.wso2telco.analytics.pricing.service.dao.RateCardDAO;

import java.math.BigDecimal;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
/*
****
TODO:use constant insetad of coloum names
*/
public class RateCardDAOImpl implements RateCardDAO {

    @Override
    public Object getNBRateCard(String operationId, String applicationId, String api, String category, String subCategory) throws Exception {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        ChargeRate rate = null;
        Integer rateDefID = null;

        try {

            connection = DBUtill.getDBConnection();
            if (connection == null) {
                throw new Exception("Database Connection Cannot Be Established");
            }

            //get rate def id from sub_rate_nb table
            String nbQuery = "select rate_defid from api_operation ao, sub_rate_nb rnb, api a " +
                        "where ao.api_operationid = rnb.api_operationid " +
                        "and a.apiid = ao.apiid " +
                        "and rnb.applicationid =? " +
                        "and a.apiname = ? " +
                        "and ao.api_operation = ? ";
            
        

            preparedStatement = connection.prepareStatement(nbQuery);
            preparedStatement.setString(1,applicationId);
            preparedStatement.setString(2,api);
            preparedStatement.setString(3,operationId);

            resultSet = preparedStatement.executeQuery();
            //connection.commit();

            if (resultSet.next()) {
                rateDefID = resultSet.getInt("rate_defid");
            }

            if (rateDefID == null ) {
                throw new AnalyticsPricingException("Rate Assignment is Faulty " + " :" + operationId + " :" + applicationId + " :" + api + " :" + category + " :" + subCategory);
            }

            // execute query
            rate = executeQuery(rate, rateDefID, category, subCategory);


        } catch (SQLException e) {
            DBUtill.handleException("Error occured getNBRateCard: " , e);
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
            connection = DBUtill.getDBConnection();
            if (connection == null) {
                throw new Exception("Database Connection Cannot Be Established");
            }

            String sbQuery = "select rate_defid from operator o,api_operation ao, sub_rate_sb rsb, api a " +
                    "where o.operatorId = rsb.operatorid " +
                    "and ao.api_operationid = rsb.api_operationid " +
                    "and a.apiid = ao.apiid " +
                    "and rsb.operatorid=? " +
                    "and rsb.applicationid =? " +
                    "and a.apiname = ?" +
                    "and ao.api_operation = ? ";
            
            preparedStatement = connection.prepareStatement(sbQuery);
            preparedStatement.setString(1,operationId);
            preparedStatement.setString(2, applicationId);
            preparedStatement.setString(3, api);
            preparedStatement.setString(4, operationId);

            resultSet = preparedStatement.executeQuery();
            //connection.commit();

            if (resultSet.next()) {
                rateDefID = resultSet.getInt("rate_defid");
            }

            executeQuery(rate, rateDefID, category, subCategory);

        } catch (Exception e) {
            DBUtill.handleException("Error occured getSBRateCard: " , e);
        } finally {
            DBUtill.closeAllConnections(preparedStatement, connection, resultSet);
        }
        return rate;
    }


    private ArrayList<String> getRateTaxes (String rateName) throws Exception {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String taxCode = null;
        ArrayList<String> taxes = new ArrayList<String>();

        try {
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
            DBUtill.handleException("Error occured getRateTaxes: " , e);
        } finally {
            DBUtill.closeAllConnections(preparedStatement, connection, resultSet);
        }
        return taxes;
    }

    private boolean setBooleanVal (int value) {
        if (value > 0) {
            return true;
        } else {
            return false;
        }
    }

    private ChargeRate executeQuery (ChargeRate rate, int rateDefID, String category, String subCategory) throws Exception {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = DBUtill.getDBConnection();
            if (connection == null) {
                throw new Exception("Database Connection Cannot Be Established");
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


            if ((category.isEmpty() || category == null) && (subCategory.isEmpty() || subCategory == null)) {
                query.append("and ct.cat is null and ct.sub is null");
            } else if (category.isEmpty() || category == null) {
                query.append("and ct.cat is null and ct.sub = ?");
            } else if (subCategory.isEmpty() || subCategory == null) {
                query.append("and ct.cat = ? and ct.sub is null");
            } else {
                query.append("and ct.cat = ? and ct.sub = ?");
            }

            
            preparedStatement = connection.prepareStatement(query.toString());
            preparedStatement.setInt(1, rateDefID);
            preparedStatement.setInt(2, rateDefID);


            //if category = ? and subcategory = ?
            if ((!category.isEmpty() || category != null) && (!subCategory.isEmpty() || subCategory != null)) {
                preparedStatement.setString(3,category);
                preparedStatement.setString(4,subCategory);
                //if category is null and subcategory = ?
            } else if ((category.isEmpty() || category == null) && (!subCategory.isEmpty() || subCategory != null)) {
                preparedStatement.setString(3, subCategory);
                //if category=? and subcategory is null
            } else if ((subCategory.isEmpty() || subCategory == null) && (!category.isEmpty() || category != null)) {
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
                Double row_maxCount  = nullCheck(resultSet, "tariffmaxcount");
                Double row_excessRate = nullCheck(resultSet, "tariffexcessrate");
                Double row_attrDefRate = nullCheck(resultSet, "tariffdefrate");
                Double row_spCommission = nullCheck(resultSet,"tariffspcommission");
                Double row_adsCommission = nullCheck(resultSet,"tariffadscommission");
                Double row_opcoCommission = nullCheck(resultSet,"tariffopcocommission");
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

                if (row_category == null && row_subCategory == null) {

                    //value element in every rate element
                    rate.setValue(BigDecimal.valueOf(row_tariffDefaultVal));

                    //if rate type is QUOTA
                    if (row_maxCount != null || row_excessRate != null || row_attrDefRate != null) {
                        Map<String,String> attributesMap = new HashMap<String,String>();
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

                } else if (row_category != null) {

                    Map<String, Object> categoryEntityMap = new HashMap<String, Object>();
                    Map<String, Object> subCategoryEntityMap = new HashMap<String, Object>();

                    if (row_subCategory == null) {
                        if (row_tariffDefaultVal != null) {
                            subCategoryEntityMap.put("__default__", row_tariffDefaultVal.toString());
                            categoryEntityMap.put(row_category, subCategoryEntityMap);

                        } else if (row_maxCount != null || row_excessRate != null || row_attrDefRate != null) {
                            Map<String,String> attributesMap = new HashMap<String,String>();
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

                    } else if (row_subCategory != null) {
                        List<SubCategory> subCategoriesMapList = new ArrayList<SubCategory>();

                        if (row_tariffDefaultVal != null) {
                            subCategoryEntityMap.put(row_subCategory, row_tariffDefaultVal.toString());
                            categoryEntityMap.put(row_category, subCategoryEntityMap);

                        } else if (row_maxCount != null || row_excessRate != null || row_attrDefRate != null) {
                            Map<String,String> subCategoriesMap  = new HashMap<String,String>();
                            subCategoriesMap.put("MaxCount", row_maxCount.toString());
                            subCategoriesMap.put("ExcessRate", row_excessRate.toString());
                            subCategoriesMap.put("DefaultRate", row_attrDefRate.toString());

                            subCategoryEntityMap.put(row_subCategory,subCategoriesMap);
                            categoryEntityMap.put(row_category, subCategoriesMap);

                        } else if (row_spCommission != null || row_adsCommission != null || row_opcoCommission != null) {
                            RateCommission subRateCommission = new RateCommission();
                            subRateCommission.setSpCommission(new BigDecimal(row_spCommission));
                            subRateCommission.setAdsCommission(new BigDecimal(row_adsCommission));
                            subRateCommission.setOpcoCommission(new BigDecimal(row_opcoCommission));

                            subCategoryEntityMap.put(row_subCategory, subRateCommission);
                            categoryEntityMap.put(row_category, subCategoryEntityMap);
                        }
                    }
                    rate.setCategories(categoryEntityMap);
                }
                //set tax values
                rate.setTaxList(getRateTaxes(rateCardName));
            }
         } catch (Exception e) {
            DBUtill.handleException("Error occured getRateTaxes: " , e);
        } finally {
            DBUtill.closeAllConnections(preparedStatement, connection, resultSet);
        }
        return rate;
    }

    private Double nullCheck (ResultSet resultSet, String col) throws SQLException {
        Double nValue = resultSet.getDouble(col);
        return resultSet.wasNull() ? null : new Double(nValue);
    }

    @Override
    public List<Tax> getValidTaxRate (List<Tax> taxes, /*Date taxDate*/ String taxDate) throws Exception {

        //String date = new SimpleDateFormat("yyyy-MM-dd").format(taxDate);
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String taxCode = null;
        double validTaxVal = 0.0;
        List<Tax> taxList = new ArrayList<Tax>();
        Tax tax = null;

        if (taxes != null && taxDate != null) {

            String taxVal = "";
            Iterator<Tax> taxIterator = taxes.iterator();

            while (taxIterator.hasNext()) {
                taxVal +=  "," + taxIterator.next().getType();
            }
            taxVal = taxVal.replaceFirst(",","");

            try {

               connection = DBUtill.getDBConnection();
                if (connection == null) {
                    throw new Exception("Database Connection Cannot Be Established");
                }

                StringBuilder query = new StringBuilder("select tax.taxcode,tax_validity.tax_validityval ");
                query.append("from tax ");
                query.append("inner join tax_validity on tax.taxid=tax_validity.taxid ");
                query.append("Where ");
                query.append("(tax_validity.tax_validityactdate <=? AND tax_validity.tax_validitydisdate >=? ) ");
                query.append("AND ");
                query.append("tax.taxcode in (?)");

                preparedStatement = connection.prepareStatement(query.toString());
                preparedStatement.setString(1, /*date*/ taxDate);
                preparedStatement.setString(2, /*date*/taxDate);
                preparedStatement.setString(3, taxVal);

                resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    taxCode = resultSet.getString("taxcode");
                    validTaxVal = resultSet.getDouble("tax_validityval");
                    tax = new Tax();
                    tax.setType(taxCode);
                    tax.setValue(new BigDecimal(validTaxVal));

                    taxList.add(tax);

                }

        } catch (SQLException e) {
            DBUtill.handleException("Error occured getRateTaxes: " , e);
        } finally {
            DBUtill.closeAllConnections(preparedStatement, connection, resultSet);
        }
        }
        return taxList;
    }
}