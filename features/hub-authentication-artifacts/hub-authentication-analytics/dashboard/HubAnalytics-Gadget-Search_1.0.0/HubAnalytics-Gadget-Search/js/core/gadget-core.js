/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// var getProviderData, conf, schema;

$(function () {
    var gadgetLocation;
    var pref = new gadgets.Prefs();
    var schema;
    var refreshInterval;
    var providerData;

    var CHART_CONF = 'chart-conf';
    var PROVIDER_CONF = 'provider-conf';
    var REFRESH_INTERVAL = 'refreshInterval';
    var operatorId = 0, serviceProviderId = 0, applicationId = 0;

    $(".nano").nanoScroller();

    var init = function () {
        $.ajax({
            url: gadgetLocation + '/conf.json',
            method: "GET",
            contentType: "application/json",
            async: false,
            success: function (data) {
                conf = JSON.parse(data);
                conf.operator =  operatorId;
                conf.serviceProvider = serviceProviderId;
                conf.applicationName = applicationId;

                $.ajax({
                    url: gadgetLocation + '/gadget-controller.jag?action=getSchema',
                    method: "POST",
                    data: JSON.stringify(conf),
                    contentType: "application/json",
                    async: false,
                    success: function (data) {
                        schema = data;
                    }
                });
            }
        });
    };


    var keywords;
        $("#searchbtn").click(function() {
            keywords = [];
            $("#canvas").html("");
            getGadgetLocation(function (gadget_Location) {
                gadgetLocation = gadget_Location;
                init();
                for(var i=0;i<count;i++) {
                    var key_i = $("#keyval"+i).val().trim();
                    if(key_i) {
                        keywords.push(key_i);
                    }
                }
                if(keywords.length == 0) {
                    $("#popupcontent p").html('Please enter atleast one keyword to search');
                    $('#notifyModal').modal('show');
                }
                else {
                    loadOperators();
                    if(providerData.length > 0) {
                        draw("#canvas", conf[CHART_CONF], schema, providerData);
                    }
                    else {
                        $("#popupcontent p").html('Your query did not return any results');
                        $('#notifyModal').modal('show');
                    }
                }
            });
        });


    var operators = [];
    var sps = [];
    var applications = [];

    function loadOperators() {
        conf["provider-conf"]["tableName"] = "ORG_WSO2TELCO_ANALYTICS_HUB_STREAM_OPERATOR_SUMMARY";
        conf["provider-conf"]["provider-name"] = "operator";
        conf.operator = 0;
        operatorId = 0;
        var operatorIds = [];
        var loadedOperator = [];
        $.ajax({
            url: gadgetLocation + '/gadget-controller.jag?action=getData',
            method: "POST",
            data: JSON.stringify(conf),
            contentType: "application/json",
            async: false,
            success: function (data) {
                for (var i = 0; i < data.length; i++) {
                    var operator = data[i];
                    if(operator.operatorId) {
                        operatorIds.push(" "+operator.operatorId);
                    }
                }
                }
            });
            loadResults(operatorIds);
    }


     function loadResults (operator_ids){
         var query = "";
         query = "jsonBody:"+keywords[0];

         for(var i = 1;i<keywords.length;i++) {
             query += " AND jsonBody:"+keywords[i];
         }
         conf["provider-conf"]["tableName"] = "ORG_WSO2TELCO_ANALYTICS_HUB_STREAM_PROCESSEDSTATISTICS";
         conf["provider-conf"]["query"] = query;

         $.ajax({
             url: gadgetLocation + '/gadget-controller.jag?action=getData',
             method: "POST",
             data: JSON.stringify(conf),
             contentType: "application/json",
             async: false,
             success: function (data) {
                 providerData = data;
             }
         });
         return providerData;
     }
});
