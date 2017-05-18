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

$(function () {
    var gadgetLocation;
    var schema;
    var providerData;
    var conf;
    var CHART_CONF = 'chart-conf';
    var PROVIDER_CONF = 'provider-conf';
    var TABLE = 'tableName';
    var QUERY = 'query';
    var TABLE_NAME = 'ORG_WSO2TELCO_ANALYTICS_HUB_STREAM_PROCESSEDSTATISTICS';
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
                conf.operator = operatorId;
                conf.serviceProvider = serviceProviderId;
                conf.applicationName = applicationId;
                conf.dateStart = moment(moment($("#reportrange").text().split("-")[0]).format("MMMM D, YYYY hh:mm A")).valueOf();
                conf.dateEnd = moment(moment($("#reportrange").text().split("-")[1]).format("MMMM D, YYYY hh:mm A")).valueOf();
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
    $("#searchbtn").click(function () {
        keywords = [];
        $("#canvas").html("");
        getGadgetLocation(function (gadget_Location) {
            gadgetLocation = gadget_Location;
            init();
            for (var i = 0; i < count; i++) {
                var key_i = $("#keyval" + i).val().trim();
                if (key_i) {
                    keywords.push(key_i);
                }
            }
            if (keywords.length == 0) {
                $("#popupcontent p").html('Please enter atleast one keyword to search');
                $('#notifyModal').modal('show');
            }
            else {
                loadResults();
                if (providerData.length > 0) {
                    draw("#canvas", conf[CHART_CONF], schema, providerData);
                }
                else {
                    $("#popupcontent p").html('Your query did not return any results');
                    $('#notifyModal').modal('show');
                }
            }
        });
    });
    function loadResults() {
        var query = "";
        query = "jsonBody:" + "\"" + keywords[0] + "\"";
        for (var i = 1; i < keywords.length; i++) {
            query += " AND jsonBody:" + "\"" + keywords[i] + "\"";
        }
        conf[PROVIDER_CONF][TABLE] = TABLE_NAME;
        conf[PROVIDER_CONF][QUERY] = query;

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
