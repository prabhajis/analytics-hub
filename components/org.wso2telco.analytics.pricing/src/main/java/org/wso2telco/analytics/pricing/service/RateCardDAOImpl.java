package org.wso2telco.analytics.pricing.service;

import com.wso2telco.analytics.DBUtill;
import com.wso2telco.analytics.exception.DBUtilException;
import org.wso2telco.analytics.pricing.service.dao.RateCardDAO;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;


//TODO:use constant insetad of coloum names
public class RateCardDAOImpl implements RateCardDAO {

    @Override
    public Object getNBRateCard(String operation, String applicationId, String category, String subCategory) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = DBUtill.getDBConnection();
            if (connection == null) {
                throw new Exception("Database Connection Cannot Be Established");
            }

            StringBuilder query = new StringBuilder("SELECT A.rate_defdefault, A.rate_defname, A.currency, A.rtype, A.rate_defcategorybase, A.cat, A.sub,");
            query.append("B.tariffdefaultval, B.tariffmaxcount,B.tariffexcessrate,B.tariffdefrate,B.tariffspcommission,B.tariffadscommission,B.tariffopcocommission,B.tariffsurcharge");
            query.append("FROM TARIFF B, currency c, rate_type t,");
            query.append("(select rd.rate_defdefault,rd.rate_defname, (select cur.currencycode from currency cur where cur.currencyid=rd.currencyid) as currency,");
            query.append("(select rty.rate_typecode from rate_type rty where rty.rate_typeid=rd.rate_typeid) as rtype , rd.rate_defcategorybase,'' as cat,");
            query.append("'' as sub,rd.tariffid tariffid FROM rate_def rd where rd.rate_defname =");
            query.append("(SELECT rate_def.rate_defname from rate_def where rate_defid=");
            query.append("(SELECT srn.rate_defid from sub_rate_nb srn where srn.applicationid= ? AND srn.api_operationid= ?))");
            query.append("UNION ALL");
            query.append("(SELECT rate_def.rate_defdefault ,rate_def.rate_defname, '' as currency,'' as rtype, rate_def.rate_defcategorybase,");
            query.append("(select categorycode from category where rt.parentcategoryid = category.categoryid) as cat,");
            query.append("(select categorycode from category where rt.childcategoryid = category.categoryid) as sub,rt.tariffid tariffid");
            query.append("FROM rate_def, rate_category rt where rt.rate_defid=rate_def.rate_defid and rate_def.rate_defname=");
            query.append("(SELECT rate_def.rate_defname from rate_def where rate_defid=");
            query.append("(SELECT srn.rate_defid from sub_rate_nb srn where srn.applicationid= ? AND srn.api_operationid= ?)))");
            query.append(") A");
            query.append("WHERE A.tariffid = B.tariffid AND A.cat= ? AND A.sub= ?");
            query.append("ORDER BY A.cat, A.sub; ");

            preparedStatement = connection.prepareStatement(query.toString());
            preparedStatement.setString(1, applicationId);
            preparedStatement.setString(2, operation);
            preparedStatement.setString(3, applicationId);
            preparedStatement.setString(4, operation);
            preparedStatement.setString(5, category);
            preparedStatement.setString(6, subCategory);

            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String row_category = resultSet.getString("cat");
                String row_subCategory = resultSet.getString("sub");
                int row_maxCount  = resultSet.getInt("tariffmaxcount");
                double row_excessRate = resultSet.getDouble("tariffexcessrate");
                double row_attrDefRate = resultSet.getDouble("tariffdefrate");
                double row_spCommission = resultSet.getDouble("tariffspcommission");
                double row_adsCommission = resultSet.getDouble("tariffadscommission");
                double row_opcoCommission = resultSet.getDouble("tariffopcocommission");
                double row_tariffDefaultVal = resultSet.getDouble("tariffdefaultval");

                ChargeRate rate = new ChargeRate(resultSet.getString("rate_defname"));

                if ((row_category.isEmpty() && row_category == null) && (row_subCategory.isEmpty() && row_subCategory == null)) {

                    //TODO:tax is missing
                    //TODO: add new value for attribute default value to tariff col *******************
                    rate.setCurrency(resultSet.getString("currency"));
                    //value element in every rate element
                    rate.setValue(BigDecimal.valueOf(row_tariffDefaultVal)); //TODO:attribute wage set unoth meka maru karanna -- check here if right
                    //type value can be null
                    rate.setType(RateType.getEnum(resultSet.getString("r_type")));

                    int defval = resultSet.getInt("rate_defdefault");
                    if (defval > 1) {
                        rate.setDefault(true);
                    } else {
                        rate.setDefault(false);
                    }

                    int categorybase = resultSet.getInt("rate_defcategorybase");
                    if (categorybase > 0) {
                        rate.setCategoryBasedVal(true);
                    } else {
                        rate.setCategoryBasedVal(false);
                    }

                    //if rate type is QUOTA
                    //TODO:check if row_maxCount != 0 correct. because database send null if there is no value assigned. -- correctd to not use raterange class
                    if (row_maxCount != 0 || row_excessRate != 0.0 || row_attrDefRate != 0.0) {
                        Map<String,String> attributesMap = new HashMap<String,String>();
                        attributesMap.put("MaxCount", Integer.toString(resultSet.getInt("tariffmaxcount")));
                        attributesMap.put("ExcessRate", Double.toString(resultSet.getDouble("tariffexcessrate")));
                        attributesMap.put("DefaultRate", Double.toString(resultSet.getDouble("tariffdefrate")));

                        rate.setRateAttributes(attributesMap);

                    } else if (row_spCommission != 0.0 || row_adsCommission != 0.0 || row_opcoCommission != 0.0) {
                        RateCommission rateCommission = new RateCommission();
                        rateCommission.setSpCommission(new BigDecimal(row_spCommission));
                        rateCommission.setAdsCommission(new BigDecimal(row_adsCommission));
                        rateCommission.setOpcoCommission(new BigDecimal(row_opcoCommission));

                        rate.setCommission(rateCommission);
                    }
                    //TODO: do remeber to use _defult_ -- this may no need to us. because in else we igonr that -- check this again
                } else if ((!row_category.isEmpty() && row_category != null)) { //TODO:this should be else or else if - becasue we are going to fill subcategory in here
                    Map<String, Object> categoryEntityMap = new HashMap<String, Object>();
                    Map<String, Object> subCategoryEntityMap = new HashMap<String, Object>();

                    if (row_subCategory.isEmpty() && row_subCategory == null) {
                        if (row_tariffDefaultVal != 0.0) { //TODO:check this value if condition is correct
                            subCategoryEntityMap.put("__default__", row_tariffDefaultVal);
                            categoryEntityMap.put(row_category, subCategoryEntityMap);

                        } else if (row_maxCount != 0 || row_excessRate != 0.0 || row_attrDefRate != 0.0) {
                            Map<String,String> attributesMap = new HashMap<String,String>();
                            attributesMap.put("MaxCount", Integer.toString(resultSet.getInt("tariffmaxcount")));
                            attributesMap.put("ExcessRate", Double.toString(resultSet.getDouble("tariffexcessrate")));
                            attributesMap.put("DefaultRate", Double.toString(resultSet.getDouble("tariffdefrate")));

                            subCategoryEntityMap.put("__default__", attributesMap);
                            categoryEntityMap.put(row_category, attributesMap);

                        } else if (row_spCommission != 0.0 || row_adsCommission != 0.0 || row_opcoCommission != 0.0) {
                            RateCommission rateCommission = new RateCommission();
                            rateCommission.setSpCommission(new BigDecimal(row_spCommission));
                            rateCommission.setAdsCommission(new BigDecimal(row_adsCommission));
                            rateCommission.setOpcoCommission(new BigDecimal(row_opcoCommission));

                            subCategoryEntityMap.put("__default__", rateCommission);
                            categoryEntityMap.put(row_category, subCategoryEntityMap);
                        }

                    } else if (!row_subCategory.isEmpty() && row_subCategory != null) {
                        List<SubCategory> subCategoriesMapList = new ArrayList<SubCategory>();

                        if (row_tariffDefaultVal != 0.0) {
                            subCategoryEntityMap.put(row_subCategory, row_tariffDefaultVal);
                            categoryEntityMap.put(row_category, subCategoryEntityMap);

                        } else if (row_maxCount != 0 || row_excessRate != 0.0 || row_attrDefRate != 0.0) {
                            Map<String,String> subCategoriesMap  = new HashMap<String,String>();
                            subCategoriesMap.put("MaxCount", Integer.toString(resultSet.getInt("tariffmaxcount")));
                            subCategoriesMap.put("ExcessRate", Double.toString(resultSet.getDouble("tariffexcessrate")));
                            subCategoriesMap.put("DefaultRate", Double.toString(resultSet.getDouble("tariffdefrate")));

                            subCategoryEntityMap.put(row_subCategory,subCategoriesMap);
                            categoryEntityMap.put(row_category, subCategoriesMap);

                        } else if (row_spCommission != 0.0 || row_adsCommission != 0.0 || row_opcoCommission != 0.0) {
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
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (DBUtilException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtill.closeAllConnections(connection, preparedStatement, resultSet);
        }

        return null;
    }

    @Override
    public Object getSBRateCard(String operator, String operation, String applicationId, String category, String subCategory) {
        return null;
    }
}
