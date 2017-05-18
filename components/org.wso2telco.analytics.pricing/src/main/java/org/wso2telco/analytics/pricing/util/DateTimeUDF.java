/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2telco.analytics.pricing.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTimeUDF {


	/**
     * Returns the string daterime for the long to which the timestamp belongs to.
     * Ex: for a long timstamp value for 3/4/2016 12:33:22 it would return the string 2016-04-03 12:33:22 am
     * @param timestamp timestamp in milliseconds.
     * @return string value of the date.
     * @throws ParseException
     */
    public String getFormattedTimeString(Long timestamp) throws ParseException {
		if(timestamp!=null){
	        	DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss aa");
	        	return dateFormat.format(new Date(timestamp));
		}else{
			return null;		
		}
    }
	

    /**
     * Returns the string date for the month to which the timestamp belongs to.
     * Ex: for a long timstamp value for 3/4/2016 12:33:22 it would return the string 2016-4
     * @param timestamp timestamp in milliseconds.
     * @return string value of the date.
     * @throws ParseException
     */
    public String getMonthString(Long timestamp) throws ParseException {
		if(timestamp!=null){
	        	DateFormat dateFormat = new SimpleDateFormat("yyyy-MM");
	        	return dateFormat.format(new Date(timestamp));
		}else{
			return null;		
		}
    }


    /**
     * Returns the long value for the date to which the timestamp belongs to.
     * Ex: for a long timstamp value for 3/4/2016 12:33:22 it would return the long timstamp value for the date
     * 3/4/2016 00:00:00.
     *
     * @param timestamp timestamp in milliseconds.
     * @return long timestamp value for the date it belongs to.
     * @throws ParseException
     */
    public Long getDateTimestamp(Long timestamp) throws ParseException {
		if(timestamp!=null){
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			String dateString = dateFormat.format(new Date(timestamp));
			Date processedDate = dateFormat.parse(dateString);
			return processedDate.getTime();
		}else{
			return null;		
		}    
	}

    /**
     * Returns the string date for the day to which the timestamp belongs to.
     * Ex: for a long timstamp value for 3/4/2016 12:33:22 it would return the string 2016-4-3
     * @param timestamp timestamp in milliseconds.
     * @return string value of the date.
     * @throws ParseException
     */
    public String getDateString(Long timestamp) throws ParseException {
		if(timestamp!=null){
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			return dateFormat.format(new Date(timestamp));
		}else{
			return null;		
		}    
	}

    /**
     * Returns the long timestamp for the given date
     * @param dateString date of the format yyyy-mm-dd
     * @return timestamp in milliseconds
     * @throws ParseException
     */
    public Long getTimestampForDate(String dateString) throws ParseException {
		if(dateString!=null){
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Date processedDate = dateFormat.parse(dateString);
			return processedDate.getTime();
		}else{
			return null;		
		}    
	}

}
