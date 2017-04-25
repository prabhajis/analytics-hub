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
    var conf;
    var schema;
    var pref = new gadgets.Prefs();

    var refreshInterval;
    var providerData;

    var CHART_CONF = 'chart-conf';
    var PROVIDER_CONF = 'provider-conf';
    var REFRESH_INTERVAL = 'refreshInterval';
    var operatorName = "all", serviceProviderId = 0, apiId = 0, applicationId = 0;
    var role;
    var selectedOperator;
    var operatorSelected = false;


    var init = function () {


        $.ajax({
            url: gadgetLocation + '/conf.json',
            method: "GET",
            contentType: "application/json",
            async: false,
            success: function (data) {
                conf = JSON.parse(data);

                if(operatorSelected) {
                    conf.operatorName =  selectedOperator;
                } else {
                    conf.operatorName =  operatorName;
                }
                conf.serviceProvider = serviceProviderId;
                conf.api = apiId;
                conf.applicationName = applicationId;
                conf.dateStart = moment(moment($("#reportrange").text().split("-")[0]).format("MMMM D, YYYY hh:mm A")).valueOf();
                conf.dateEnd = moment(moment($("#reportrange").text().split("-")[1]).format("MMMM D, YYYY hh:mm A")).valueOf();

                if($("#button-type").val().toLowerCase().trim() == "error traffic") {
                    conf["provider-conf"].tableName = "ORG_WSO2TELCO_ANALYTICS_HUB_STREAM_FAILURE_SUMMARY_PER_DAY";
                } else {
                    conf["provider-conf"].tableName = "ORG_WSO2TELCO_ANALYTICS_HUB_STREAM_TRAFFIC_SUMMARY_PER_DAY";
                }

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


    var getRole = function () {
        console.log("************************"+ $('input[name="gender"]:checked').val());
        conf.operator = "test123";
        conf["provider-conf"]["tableName"] = "test";
        $.ajax({
            url: gadgetLocation + '/gadget-controller.jag?action=getRole',
            method: "POST",
            data: JSON.stringify(conf),
            contentType: "application/json",
            async: false,
            success: function (data) {
                role = data.role;
                if("operatoradmin" == role) {
                    $("#operatordd").hide();
                } else {
                    $("#operatordd").show();
                }
            }
        });
    };


    var getOperatorNameInProfile = function () {
        conf.operator = "test123";
        conf["provider-conf"]["tableName"] = "test";
        $.ajax({
            url: gadgetLocation + '/gadget-controller.jag?action=getProfileOperator',
            method: "POST",
            data: JSON.stringify(conf),
            contentType: "application/json",
            async: false,
            success: function (data) {
                operatorName = data.operatorName;
            }
        });
    };

    var getRole = function () {
        conf.operator = "test123";
        conf["provider-conf"]["tableName"] = "test";
        $.ajax({
            url: gadgetLocation + '/gadget-controller.jag?action=getRole',
            method: "POST",
            data: JSON.stringify(conf),
            contentType: "application/json",
            async: false,
            success: function (data) {
                role = data.role;
                if("operatoradmin" == role) {
                    $("#operatordd").hide();
                    conf.operatorName = operatorName;
                } else {
                    $("#operatordd").show();
                }
            }
        });
    };

    var getProviderData = function (){

        if($("#button-type").val().toLowerCase().trim() == "error traffic") {
            conf["provider-conf"].tableName = "ORG_WSO2TELCO_ANALYTICS_HUB_STREAM_FAILURE_SUMMARY_PER_";
        } else {
            conf["provider-conf"].tableName = "ORG_WSO2TELCO_ANALYTICS_HUB_STREAM_TRAFFIC_SUMMARY_PER_";
        }

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
        if(providerData != '') {
            $("#generateCSV").show();
            if(role == "admin"){
                $("#tableSelect").show();
            }

        } else {
            $("#generateCSV").hide();
            $("#tableSelect").hide();

        }
        return providerData;
    };


    var drawGadget = function (){
        draw('#canvas', conf[CHART_CONF], schema, providerData);
        setInterval(function() {
            draw('#canvas', conf[CHART_CONF], schema, getProviderData());
        },pref.getInt(REFRESH_INTERVAL));
    };


    $("#button-search").click(function() {
        $("#canvas").html("");
        $("#canvas2").html("");
        $("#showCSV").hide();
        getGadgetLocation(function (gadget_Location) {
            gadgetLocation = gadget_Location;
            init();
            getProviderData();
            drawGadget();
        });
    });

    $("#button-generate-tr").click(function () {
        getGadgetLocation(function (gadget_Location) {
            gadgetLocation = gadget_Location;
            $("#output").html("");
            if(operatorSelected) {
                conf.operatorName =  selectedOperator;
            } else {
                conf.operatorName =  operatorName;
            }
            conf.serviceProvider = serviceProviderId;
            conf.api = apiId;
            conf.applicationName = applicationId;

            conf.dateStart = moment(moment($("#reportrange").text().split("-")[0]).format("MMMM D, YYYY hh:mm A")).valueOf();
            conf.dateEnd = moment(moment($("#reportrange").text().split("-")[1]).format("MMMM D, YYYY hh:mm A")).valueOf();

            if($("#button-type").val().toLowerCase().trim() == "error traffic") {

                conf["provider-conf"].tableName = "ORG_WSO2TELCO_ANALYTICS_HUB_STREAM_FAILURE_SUMMARY_PER_";
            } else {
                getRole();

                if(role == "admin"){

                    if($('input[name="gender"]:checked').val()=="sb") {
                        conf["provider-conf"].tableName = "ORG_WSO2TELCO_ANALYTICS_HUB_STREAM_SOUTHBOUND_REPORT_SUMMARY_PER_DAY";
                    }else if($('input[name="gender"]:checked').val()=="nb"){
                        conf["provider-conf"].tableName = "ORG_WSO2TELCO_ANALYTICS_HUB_STREAM_NORTHBOUND_REPORT_SUMMARY_PER_DAY";
                    }
                } else if(role == "serviceProvider") {
                    conf["provider-conf"].tableName = "ORG_WSO2TELCO_ANALYTICS_HUB_STREAM_NORTHBOUND_REPORT_SUMMARY_PER_DAY";
                } else if(role == "operatoradmin") {
                    conf["provider-conf"].tableName = "ORG_WSO2TELCO_ANALYTICS_HUB_STREAM_SOUTHBOUND_REPORT_SUMMARY_PER_DAY";
                }

            }

            var btn =  $("#button-generate-tr");
            btn.prop('disabled', true);
            setTimeout(function(){
                btn.prop('disabled', false);
            }, 3000);

            $.ajax({
                url: gadgetLocation + '/gadget-controller.jag?action=generateCSV',
                method: "POST",
                data: JSON.stringify(conf),
                contentType: "application/json",
                async: false,
                success: function (data) {
                    $("#showCSV").show();
                    $("#list-available-report").show();
                    $("#output").html('<div id="success-message" class="alert alert-success"><strong>Report is generating</strong> '
                        + "Please refresh the traffic report"
                        + '</div>' + $("#output").html());
                    $('#success-message').fadeIn().delay(2000).fadeOut();
                }
            });
        });
    });


    $("#button-generate-bill").click(function () {
        getGadgetLocation(function (gadget_Location) {
            $("#output").html("");
            var serviceProviderName = $("#button-sp").val();

            gadgetLocation = gadget_Location;
            if(operatorSelected) {
                conf.operatorName =  selectedOperator;
            } else {
                conf.operatorName =  operatorName;
            }
            conf.serviceProvider = serviceProviderId;
            conf.api = apiId;
            conf.applicationName = applicationId;
            conf.serviceProviderName = serviceProviderName;

            conf.dateStart = moment(moment($("#reportrange").text().split("-")[0]).format("MMMM D, YYYY hh:mm A")).valueOf();
            conf.dateEnd = moment(moment($("#reportrange").text().split("-")[1]).format("MMMM D, YYYY hh:mm A")).valueOf();

            getRole();
            if(role == "admin"){

                if($('input[name="gender"]:checked').val()=="sb"){
                    conf["provider-conf"].tableName = "ORG_WSO2TELCO_ANALYTICS_HUB_STREAM_SOUTHBOUND_REPORT_SUMMARY_PER_DAY";
                }else if($('input[name="gender"]:checked').val()=="nb"){
                    conf["provider-conf"].tableName = "ORG_WSO2TELCO_ANALYTICS_HUB_STREAM_NORTHBOUND_REPORT_SUMMARY_PER_DAY";
                }
            } else if(role == "serviceProvider") {
                conf["provider-conf"].tableName = "ORG_WSO2TELCO_ANALYTICS_HUB_STREAM_NORTHBOUND_REPORT_SUMMARY_PER_DAY";
            } else if(role == "operatoradmin") {
                conf["provider-conf"].tableName = "ORG_WSO2TELCO_ANALYTICS_HUB_STREAM_SOUTHBOUND_REPORT_SUMMARY_PER_DAY";
            }

            var btn =  $("#button-generate-bill");
            btn.prop('disabled', true);
            setTimeout(function(){
                btn.prop('disabled', false);
            }, 3000);

            $.ajax({
                url: gadgetLocation + '/gadget-controller.jag?action=generateBill',
                method: "POST",
                data: JSON.stringify(conf),
                contentType: "application/json",
                async: false,
                success: function (data) {
                    $("#list-available-report").show();

                    $("#output").html('<div id="success-message" class="alert alert-success"><strong>Report is generating</strong> '
                        + '</div>' + $("#output").html());
                    $('#success-message').fadeIn().delay(2000).fadeOut();
                    $("#showCSV").show();

                }
            });
        });
    });


    $("#list-available-report").click(function () {
        $("#output").html("");
        getGadgetLocation(function(gadget_Location) {
            gadgetLocation = gadget_Location;
            $.ajax({
                url: gadgetLocation + '/gadget-controller.jag?action=available',
                method: "POST",
                data: JSON.stringify(conf),
                contentType: "application/json",
                async: false,
                success: function(data) {
                    $("#output").html("<ul class = 'list-group'>")
                    for (var i = 0; i < data.length; i++) {
                        $("#output").html($("#output").html() + "<li class = 'list-group-item'>" +
                            " <span class='btn-label'>" + data[i].name + "</span>" +
                            " <div class='btn-toolbar'>" +
                            "<a class='btn btn-primary btn-xs' onclick='downloadFile(" + data[i].index + ")'>Download</a>" +
                            "</div>" +
                            "</li>");
                    }
                    $("#output").html($("#output").html() + "<ul/>")

                }
            });

        });
    });




    getGadgetLocation(function (gadget_Location) {
        gadgetLocation = gadget_Location;
        init();
        getRole();
        loadOperator();
        $("#generateCSV").hide();
        $("#tableSelect").hide();
        $("#showCSV").hide();

        function loadOperator (){
            conf["provider-conf"]["tableName"] = "ORG_WSO2TELCO_ANALYTICS_HUB_STREAM_OPERATOR_SUMMARY";
            conf["provider-conf"]["provider-name"] = "operator";
            conf.operatorName = "all";
            operatorName = "all";
            $.ajax({
                url: gadgetLocation + '/gadget-controller.jag?action=getData',
                method: "POST",
                data: JSON.stringify(conf),
                contentType: "application/json",
                async: false,
                success: function (data) {
                    $("#dropdown-operator").empty();
                    var operatorsItems = "";
                    var operatorNames = [];
                    var loadedOperator = [];
                    operatorNames.push(operatorName);
                    operatorsItems += '<li><a data-val="all" href="#">All</a></li>';
                    for (var i =0 ; i < data.length; i++) {
                        var operator = data[i];
                        if($.inArray(operator.operatorName, loadedOperator)<0){
                            operatorsItems += '<li><a data-val='+ operator.operatorName +' href="#">' + operator.operatorName +'</a></li>';
                            operatorNames.push(" "+operator.operatorName);
                            loadedOperator.push(operator.operatorName);
                        }
                    }
                    $("#dropdown-operator").html( $("#dropdown-operator").html() + operatorsItems);
                    $("#button-operator").val('<li><a data-val="all" href="#">All</a></li>');
                    if("operatoradmin" == role) {
                        getOperatorNameInProfile();
                        loadSP(operatorName);
                    } else {
                        loadSP(operatorNames);
                    }

                    $("#dropdown-operator li a").click(function(){
                        $("#button-operator").text($(this).text());
                        $("#button-operator").append('<span class="caret"></span>');
                        $("#button-operator").val($(this).text());
                        operatorNames = $(this).data('val');
                        loadSP(operatorNames);
                        operatorSelected = true;

                    });
                }
            });
        }

        function loadSP (clickedOperator){

            conf["provider-conf"]["tableName"] = "ORG_WSO2TELCO_ANALYTICS_HUB_STREAM_API_SUMMARY";
            conf["provider-conf"]["provider-name"] = "operator";

            conf.operatorName = "("+clickedOperator+")";
            selectedOperator = conf.operatorName;
            serviceProviderId =0;

            $.ajax({
                url: gadgetLocation + '/gadget-controller.jag?action=getData',
                method: "POST",
                data: JSON.stringify(conf),
                contentType: "application/json",
                async: false,
                success: function (data) {
                    $("#dropdown-sp").empty();
                    var spItems = '';
                    var spIds = [];
                    var loadedSps = [];
                    spIds.push(serviceProviderId);
                    spItems += '<li><a data-val="0" href="#">All</a></li>';
                    for ( var i =0 ; i < data.length; i++) {
                        var sp = data[i];
                        if($.inArray(sp.serviceProviderId, loadedSps)<0){
                            spItems += '<li><a data-val='+ sp.serviceProviderId +' href="#">' + sp.serviceProvider.replace("@carbon.super","") +'</a></li>'
                            spIds.push(" "+sp.serviceProviderId);
                            loadedSps.push(sp.serviceProviderId);
                        }
                    }

                    $("#dropdown-sp").html(spItems);

                    $("#button-sp").text('All');
                    $("#button-sp").val('All');

                    loadApp(spIds,selectedOperator);
                    $("#dropdown-sp li a").click(function(){

                        $("#button-sp").text($(this).text());
                        $("#button-sp").append('<span class="caret"></span>');
                        $("#button-sp").val($(this).text());
                        // var clickedSP = [];
                        // clickedSP.push($(this).data('val'));
                        spIds = $(this).data('val');
                        serviceProviderId = spIds;

                        loadApp(spIds,selectedOperator);
                    });


                }
            });
        }

        function loadApp (sps,clickedOperator){
            // alert(sps);
            // if(sps)
            conf["provider-conf"]["tableName"] = "ORG_WSO2TELCO_ANALYTICS_HUB_STREAM_API_SUMMARY";
            conf["provider-conf"]["provider-name"] = "sp";
            applicationId = 0;
            conf.serviceProvider = "("+sps+")";
            conf.operatorName = "("+clickedOperator+")";
            $.ajax({
                url: gadgetLocation + '/gadget-controller.jag?action=getData',
                method: "POST",
                data: JSON.stringify(conf),
                contentType: "application/json",
                async: false,
                success: function (data) {

                    $("#dropdown-app").empty();
                    var apps = [];
                    var loadedApps = [];
                    var appItems = '<li><a data-val="0" href="#">All</a></li>';

                    for ( var i =0 ; i < data.length; i++) {
                        var app = data[i];
                        if($.inArray(app.applicationId, loadedApps)<0){
                            appItems += '<li><a data-val='+ app.applicationId +' href="#">' + app.applicationName +'</a></li>'
                            apps.push(" "+app.applicationId);
                            loadedApps.push(app.applicationId);
                        }
                    }

                    $("#dropdown-app").html( $("#dropdown-app").html() + appItems);
                    $("#button-app").val('<li><a data-val="0" href="#">All</a></li>');
                    $("#button-app").text('All');
                    // loadApp(sps[i]);

                    loadApi(apps);

                    $("#dropdown-app li a").click(function(){

                        $("#button-app").text($(this).text());
                        $("#button-app").append('<span class="caret"></span>');
                        $("#button-app").val($(this).text());
                        // var clickedSP = [];
                        // clickedSP.push($(this).data('val'));
                        apps = $(this).data('val');
                        applicationId = apps;
                        loadApi(apps);
                    });

                }
            });
        }

        function loadApi (apps){
            conf["provider-conf"]["tableName"] = "ORG_WSO2TELCO_ANALYTICS_HUB_STREAM_API_SUMMARY";
            conf["provider-conf"]["provider-name"] = "app";
            conf.applicationId = "("+apps+")";
            apiId = 0;
            $.ajax({
                url: gadgetLocation + '/gadget-controller.jag?action=getData',
                method: "POST",
                data: JSON.stringify(conf),
                contentType: "application/json",
                async: false,
                success: function (data) {


                    $("#dropdown-api").empty();
                    var apis = [];
                    var loadedApis = [];
                    var apiItems = '<li><a data-val="0" href="#">All</a></li>';
                    for ( var i =0 ; i < data.length; i++) {
                        var api = data[i];
                        if($.inArray(api.apiID, loadedApis)<0){
                            apiItems += '<li><a data-val='+ api.apiID +' href="#">' + api.api +'</a></li>';
                            loadedApis.push(api.apiID);
                        }
                    }

                    $("#dropdown-api").html( $("#dropdown-api").html() + apiItems);
                    $("#button-api").val('<li><a data-val="0" href="#">All</a></li>');
                    $("#button-api").text('All');
                    // loadApp(sps[i]);
                    $("#dropdown-api li a").click(function(){
                        $("#button-api").text($(this).text());
                        $("#button-api").append('<span class="caret"></span>');
                        $("#button-api").val($(this).text());
                        apiId = $(this).data('val');
                    });

                }
            });
        }

        $("#button-app").val("All");
        $("#button-api").val("All");
        $("#button-type").val("Api Traffic");

        $('input[name="daterange"]').daterangepicker({
            timePicker: true,
            timePickerIncrement: 30,
            locale: {
                format: 'MM/DD/YYYY h:mm A'
            }
        });
    });

    $("#dropdown-type li a").click(function(){
        $("#button-type").text($(this).text());
        $("#button-type").append('<span class="caret"></span>');
        $("#button-type").val($(this).text());
    });
});


function downloadFile(index) {
    getGadgetLocation(function (gadget_Location) {
        gadgetLocation = gadget_Location;
        location.href = gadgetLocation + '/gadget-controller.jag?action=get&index=' + index;

    });
}