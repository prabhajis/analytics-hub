package org.wso2telco.analytics.pricing.service;

import com.wso2telco.analytics.DBUtill;
import com.wso2telco.analytics.exception.DBUtilException;
import com.wso2telco.analytics.util.DataSourceNames;
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
    public Object getNBRateCard(String operationId, String applicationId, String category, String subCategory) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        ChargeRate rate = null;

        try {

            connection = DBUtill.getDBConnection();
            if (connection == null) {
                throw new Exception("Database Connection Cannot Be Established");
            }

            StringBuilder query = new StringBuilder("SELECT A.rate_defdefault, A.rate_defname, A.currency, A.rtype, A.rate_defcategorybase, A.cat, A.sub,");
            query.append("B.tariffdefaultval, B.tariffmaxcount,B.tariffexcessrate,B.tariffdefrate,B.tariffspcommission,B.tariffadscommission,B.tariffopcocommission,");
            query.append("B.tariffsurchargeval,B.tariffsurchargeAds,B.tariffsurchargeOpco ");
            query.append("FROM TARIFF B, currency c, rate_type t,");
            query.append("(select rd.rate_defdefault,rd.rate_defname, (select cur.currencycode from currency cur where cur.currencyid=rd.currencyid) as currency,");
            query.append("(select rty.rate_typecode from rate_type rty where rty.rate_typeid=rd.rate_typeid) as rtype , rd.rate_defcategorybase,Null as cat,");
            query.append("Null as sub,rd.tariffid tariffid FROM rate_def rd where rd.rate_defname =");
            query.append("(SELECT rate_def.rate_defname from rate_def where rate_defid=");
            query.append("(SELECT srn.rate_defid from sub_rate_nb srn where srn.applicationid= ? AND srn.api_operationid= ?))");
            query.append("UNION ALL");
            query.append("(SELECT rate_def.rate_defdefault ,rate_def.rate_defname, Null as currency,Null as rtype, rate_def.rate_defcategorybase,");
            query.append("(select categorycode from category where rt.parentcategoryid = category.categoryid) as cat,");
            query.append("(select categorycode from category where rt.childcategoryid = category.categoryid) as sub,rt.tariffid tariffid ");
            query.append("FROM rate_def, rate_category rt where rt.rate_defid=rate_def.rate_defid and rate_def.rate_defname=");
            query.append("(SELECT rate_def.rate_defname from rate_def where rate_defid=");
            query.append("(SELECT srn.rate_defid from sub_rate_nb srn where srn.applicationid= ? AND srn.api_operationid= ?)))");
            query.append(") A ");


            if ((category.isEmpty() || category == "") && (subCategory.isEmpty() || subCategory == "")) {
                query.append("WHERE A.tariffid = B.tariffid AND A.cat is null AND A.sub is null ");
            } else if (category.isEmpty() || category == "") {
                query.append("WHERE A.tariffid = B.tariffid AND A.cat is null AND A.sub= ? ");
            } else if (subCategory.isEmpty() || subCategory == "") {
                query.append("WHERE A.tariffid = B.tariffid AND A.cat= ? AND A.sub is null ");
            } else {
                query.append("WHERE A.tariffid = B.tariffid AND A.cat= ? AND A.sub = ? ");
            }

            query.append("ORDER BY A.cat, A.sub; ");

            connection.setAutoCommit(false);

            preparedStatement = connection.prepareStatement(query.toString());
            preparedStatement.setString(1, applicationId);
            preparedStatement.setString(2, operationId);
            preparedStatement.setString(3, applicationId);
            preparedStatement.setString(4, operationId);

            //if category = ? and subcategory = ?
            if ((!category.isEmpty() || category != "") && (!subCategory.isEmpty() || subCategory != "")) {
                preparedStatement.setString(5, category);
                preparedStatement.setString(6, subCategory);
                //if category is null and subcategory = ?
            } else if ((category.isEmpty() || category == "") && (!subCategory.isEmpty() || subCategory != "")) {
                preparedStatement.setString(5, subCategory);
                //if category=? and subcategory is null
            } else if ((subCategory.isEmpty() || subCategory == "") && (!category.isEmpty() || category != "")) {
                preparedStatement.setString(5, category);
            }

            resultSet = preparedStatement.executeQuery();
            connection.commit();

            if (resultSet.next()) {
                String rateCardName = resultSet.getString("rate_defname");
                String row_category = resultSet.getString("cat");
                String row_subCategory = resultSet.getString("sub");
                int row_maxCount  = resultSet.getInt("tariffmaxcount");
                double row_excessRate = resultSet.getDouble("tariffexcessrate");
                double row_attrDefRate = resultSet.getDouble("tariffdefrate");
                double row_spCommission = resultSet.getDouble("tariffspcommission");
                double row_adsCommission = resultSet.getDouble("tariffadscommission");
                double row_opcoCommission = resultSet.getDouble("tariffopcocommission");
                double row_tariffDefaultVal = resultSet.getDouble("tariffdefaultval");
                double row_surchargeVal = resultSet.getDouble("tariffsurchargeval");
                double row_surchargeAds = resultSet.getDouble("tariffsurchargeAds");
                double row_surchargeOpco = resultSet.getDouble("tariffsurchargeOpco");
                int defval = resultSet.getInt("rate_defdefault");
                int categorybase = resultSet.getInt("rate_defcategorybase");

                rate = new ChargeRate(rateCardName);

                if (row_category == null && row_subCategory == null) {

                    rate.setCurrency(resultSet.getString("currency"));
                    //value element in every rate element
                    rate.setValue(BigDecimal.valueOf(row_tariffDefaultVal));
                    //type value can be null
                    rate.setType(RateType.getEnum(resultSet.getString("rtype")));
                    rate.setDefault(setBooleanVal(defval));
                    rate.setCategoryBasedVal(setBooleanVal(categorybase));

                    //if rate type is QUOTA
                    if (row_maxCount != 0 || row_excessRate != 0 || row_attrDefRate != 0) {
                        Map<String,String> attributesMap = new HashMap<String,String>();
                        attributesMap.put("MaxCount", Integer.toString(row_maxCount));
                        attributesMap.put("ExcessRate", Double.toString(row_excessRate));
                        attributesMap.put("DefaultRate", Double.toString(row_attrDefRate));

                        rate.setRateAttributes(attributesMap);

                    } else if (row_spCommission != 0 || row_adsCommission != 0 || row_opcoCommission != 0) {
                        RateCommission rateCommission = new RateCommission();
                        rateCommission.setSpCommission(new BigDecimal(row_spCommission));
                        rateCommission.setAdsCommission(new BigDecimal(row_adsCommission));
                        rateCommission.setOpcoCommission(new BigDecimal(row_opcoCommission));

                        rate.setCommission(rateCommission);
                    }

                    //surcharge values
                    if (row_surchargeVal != 0 || row_surchargeAds != 0 || row_surchargeOpco != 0) {
                        SurchargeEntity surchargeEntity = new SurchargeEntity();
                        surchargeEntity.setSurchargeElementValue(Double.toString(row_surchargeVal));
                        surchargeEntity.setSurchargeElementAds(Double.toString(row_surchargeAds));
                        surchargeEntity.setSurchargeElementOpco(Double.toString(row_surchargeOpco));
                        rate.setSurchargeEntity(surchargeEntity);
                    }

                } else if (row_category != null) {

                    //setting isdefault and categorybased values.
                    rate.setDefault(setBooleanVal(defval));
                    rate.setCategoryBasedVal(setBooleanVal(categorybase));

                    Map<String, Object> categoryEntityMap = new HashMap<String, Object>();
                    Map<String, Object> subCategoryEntityMap = new HashMap<String, Object>();

                    if (row_subCategory == null) {
                        if (row_tariffDefaultVal != 0) {
                            subCategoryEntityMap.put("__default__", row_tariffDefaultVal);
                            categoryEntityMap.put(row_category, subCategoryEntityMap);

                        } else if (row_maxCount != 0 || row_excessRate != 0 || row_attrDefRate != 0) {
                            Map<String,String> attributesMap = new HashMap<String,String>();
                            attributesMap.put("MaxCount", Integer.toString(row_maxCount));
                            attributesMap.put("ExcessRate", Double.toString(row_excessRate));
                            attributesMap.put("DefaultRate", Double.toString(row_attrDefRate));

                            subCategoryEntityMap.put("__default__", attributesMap);
                            categoryEntityMap.put(row_category, attributesMap);

                        } else if (row_spCommission != 0 || row_adsCommission != 0 || row_opcoCommission != 0) {
                            RateCommission rateCommission = new RateCommission();
                            rateCommission.setSpCommission(new BigDecimal(row_spCommission));
                            rateCommission.setAdsCommission(new BigDecimal(row_adsCommission));
                            rateCommission.setOpcoCommission(new BigDecimal(row_opcoCommission));

                            subCategoryEntityMap.put("__default__", rateCommission);
                            categoryEntityMap.put(row_category, subCategoryEntityMap);
                        }

                    } else if (row_subCategory != null) {
                        List<SubCategory> subCategoriesMapList = new ArrayList<SubCategory>();

                        if (row_tariffDefaultVal != 0) {
                            subCategoryEntityMap.put(row_subCategory, row_tariffDefaultVal);
                            categoryEntityMap.put(row_category, subCategoryEntityMap);

                        } else if (row_maxCount != 0 || row_excessRate != 0 || row_attrDefRate != 0) {
                            Map<String,String> subCategoriesMap  = new HashMap<String,String>();
                            subCategoriesMap.put("MaxCount", Integer.toString(row_maxCount));
                            subCategoriesMap.put("ExcessRate", Double.toString(row_excessRate));
                            subCategoriesMap.put("DefaultRate", Double.toString(row_attrDefRate));

                            subCategoryEntityMap.put(row_subCategory,subCategoriesMap);
                            categoryEntityMap.put(row_category, subCategoriesMap);

                        } else if (row_spCommission != 0 || row_adsCommission != 0 || row_opcoCommission != 0) {
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
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
        } catch (DBUtilException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                connection.setAutoCommit(true);
                DBUtill.closeAllConnections(preparedStatement, connection, resultSet);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return rate;
    }

    @Override
    public Object getSBRateCard(String operator, String operation, String applicationId, String category, String subCategory) {
        //TODO:impl this. this is same as above
        return null;
    }


    private ArrayList<String> getRateTaxes (String rateName) {
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

            connection.setAutoCommit(false);

            preparedStatement = connection.prepareStatement(query.toString());
            preparedStatement.setString(1, rateName);

            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                taxCode = resultSet.getString("taxcode");
                taxes.add(taxCode);
            }

        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                connection.setAutoCommit(true);
                DBUtill.closeAllConnections(preparedStatement,connection, resultSet);
            } catch (SQLException e) {
                e.printStackTrace();
            }
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

    @Override
    public double getValidTaxRate (String taxCode, /*Date taxDate*/ String taxDate) {

        //String date = new SimpleDateFormat("yyyy-MM-dd").format(taxDate);

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        double validTaxVal = 0.0;

        if (taxCode != null && taxDate != null) {
            try {

                DBUtill.getDBConnection();
                if (connection == null) {
                    throw new Exception("Database Connection Cannot Be Established");
                }

                StringBuilder query = new StringBuilder("select tax.taxcode,tax_validity.tax_validityval ");
                query.append("from tax ");
                query.append("inner join tax_validity on tax.taxid=tax_validity.taxid ");
                query.append("Where tax.taxcode=? ");
                query.append("AND ");
                query.append("(tax_validity.tax_validityactdate <=? AND tax_validity.tax_validitydisdate >=? );");

                connection.setAutoCommit(false);
                preparedStatement = connection.prepareStatement(query.toString());
                preparedStatement.setString(1, taxCode);
                preparedStatement.setString(2, /*date*/ taxDate);
                preparedStatement.setString(3, /*date*/taxDate);

                resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    validTaxVal = resultSet.getDouble("tax_validityval");
                }

            } catch (SQLException e) {
                if (connection != null) {
                    try {
                        connection.rollback();
                    } catch (SQLException e1) {
                        e1.printStackTrace();
                    }
                }
            } catch (DBUtilException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    connection.setAutoCommit(true);
                    DBUtill.closeAllConnections(preparedStatement,connection, resultSet);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return validTaxVal;
    }
}
