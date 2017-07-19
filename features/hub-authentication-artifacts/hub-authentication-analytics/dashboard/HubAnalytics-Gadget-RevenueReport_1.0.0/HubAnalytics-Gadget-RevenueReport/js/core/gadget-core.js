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

    var providerData;

    var operatorName = "all", serviceProviderId = 0, apiId = 0, applicationId = 0, application="";
    var loggedInUser;
    var selectedOperator;
    var operatorSelected = false;
    var initloading = false;


    var init = function (clickedEvent) {

        if (clickedEvent) {
            var date = new Date();
            var currentYear = date.getFullYear();
            var currentMonth = moment(date.getMonth()+1, 'MM').format('MMMM');

            $("#button-year").text(currentYear);
            $("#button-year").append('&nbsp;<span class="caret"></span>');
            $("#button-month").text(currentMonth);
            $("#button-month").append('&nbsp;<span class="caret"></span>');
            $("#button-year").val(currentYear);
            $("#button-month").val(currentMonth);

            for (var i = 1; i <= 3; i++) {
                $("#dropdown-year").append(
                    $('<li><a data-val=' + currentYear + ' href="#">' + currentYear + '</a></li>')
                );
                currentYear--;
            }

            $("#dropdown-year li a").click(function () {
                $("#button-year").text($(this).text());
                $("#button-year").append('&nbsp;<span class="caret"></span>');
                $("#button-year").val($(this).text());

                getFilterdResult(initloading);
            });
        }

        $.ajax({
            url: gadgetLocation + '/conf.json',
            method: METHOD.GET,
            contentType: CONTENT_TYPE,
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
                conf.application=application;
                conf.year = $("#button-year").val();
                conf.month = $("#button-month").val();

                $.ajax({
                    url: gadgetLocation + '/gadget-controller.jag?action=getSchema',
                    method: METHOD.POST,
                    data: JSON.stringify(conf),
                    contentType: CONTENT_TYPE,
                    async: false,
                    success: function (data) {
                        schema = data;
                    }
                });
            }
        });
    };

    var getLoggedInUser = function () {

        $.ajax({
            url: gadgetLocation + '/gadget-controller.jag?action=getLoggedInUser',
            method: METHOD.POST,
            data: JSON.stringify(conf),
            contentType: CONTENT_TYPE,
            async: false,
            success: function (data) {
                loggedInUser = data.LoggedInUser;
                operatorName = loggedInUser.operatorNameInProfile;

                // hide the operator / serviceProvider drop-down according to logged in user
                hideDropDown(loggedInUser);

                if (!(loggedInUser.isAdmin) && (loggedInUser.isOperatorAdmin || loggedInUser.isCustomerCareUser)) {
                    $("#spContainer").removeClass("col-top-pad");
                    $("#spContainer").removeClass("col-md-top-pad");
            
                    //conf.operatorName = operatorName;
                } else if (!(loggedInUser.isAdmin) && loggedInUser.isServiceProvider) {
                    $("#appContainer").removeClass("col-top-pad");
                    $("#appContainer").removeClass("col-md-top-pad");
                   
                }
                
            }
        });
    };

    var getProviderData = function (){
        $.ajax({
            url: gadgetLocation + '/gadget-controller.jag?action=getData',
            method: METHOD.POST,
            data: JSON.stringify(conf),
            contentType: CONTENT_TYPE,
            async: false,
            success: function (data) {
                providerData = data;
            }
        });

        if(providerData == '') {
            //show No matching records found msg
            //$("#nodata_info").html("<div class='alert alert-info no-data' role='alert'>* No matching records found.</div>");
            $("#nodata_info").html('<div id="success-message" class="alert alert-info"><strong>* No matching records found.</strong> ' +
                '</div>');
            $('#success-message').fadeIn().delay(1000).fadeOut();
        }

        return providerData;
    };

    var drawGadget = function (){
        draw('#canvas', conf[CHART_CONF], schema, providerData);
        setInterval(function() {
            draw('#canvas', conf[CHART_CONF], schema, getProviderData());
        },pref.getInt(REFRESH_INTERVAL));
    };

    function getFilterdResult(clickedEvent) {
        $("#canvas").html("");
        $("#canvas2").html("");
        $("#canvas3").html("");
        // $("#showCSV").hide();
        getGadgetLocation(function (gadget_Location) {
            gadgetLocation = gadget_Location;
            init(clickedEvent);
            //if (checkTimeSelection()) {
            getProviderData();
            drawGadget();
            //}
        });
    };

    var loadTimelyData = function () {
        /*var date = new Date();
         var currentYear = date.getFullYear();
         var currentMonth = moment(date.getMonth(), 'MM').format('MMMM');

         $("#button-year").text(currentYear);
         $("#button-month").text(currentMonth);

         conf.year = currentYear;
         conf.month = currentMonth;


         for (var i = 1; i <= 3; i++) {
         $("#dropdown-year").append(
         $('<li><a data-val='+currentYear+' href="#">'+currentYear+'</a></li>')
         );
         currentYear--;
         }*/

        //draw pie chart for data, current year and month
        getFilterdResult(initloading);

        $("#dropdown-month li a").click(function () {

            $("#button-month").text($(this).text());
            $("#button-month").append('&nbsp;<span class="caret"></span>');
            $("#button-month").val($(this).data('val'));

            getFilterdResult(initloading);
        });
    }

    getGadgetLocation(function (gadget_Location) {
        gadgetLocation = gadget_Location;
        init(initloading);
        getLoggedInUser();
        initloading = true;
        loadTimelyData();
        initloading = false;
        loadOperator();

        $("#tableSelect").hide();

        function loadOperator () {
            if (loggedInUser.isOperatorAdmin) {
                loadSP(loggedInUser.operatorNameInProfile);
            } else {
                conf[PROVIDER_CONF][TABLE_NAME] = STREAMS.OPERATOR_SUMMERY;
                conf[PROVIDER_CONF][PROVIDER_NAME] = TYPE.OPERATOR;
                conf.operatorName = "all";
                operatorName = "all";
                $.ajax({
                    url: gadgetLocation + '/gadget-controller.jag?action=getData',
                    method: METHOD.POST,
                    data: JSON.stringify(conf),
                    contentType: CONTENT_TYPE,
                    async: false,
                    success: function (data) {
                        $("#dropdown-operator").empty();
                        var operatorsItems = "";
                        var operatorNames = [];
                        var loadedOperator = [];
                        operatorNames.push(operatorName);
                        operatorsItems += '<li><a data-val="all" href="#">All Operator</a></li>';
                        for (var i = 0; i < data.length; i++) {
                            var operator = data[i];
                            if ($.inArray(operator.operatorName, loadedOperator) < 0) {
                                operatorsItems += '<li><a data-val=' + operator.operatorName + ' href="#">' + operator.operatorName + '</a></li>';
                                if(operator.operatorName.toString() != "all") {
                                    operatorNames.push(" " + "\"" + operator.operatorName +"\"");
                                }
                                loadedOperator.push(operator.operatorName);
                            }
                        }
                        $("#dropdown-operator").html($("#dropdown-operator").html() + operatorsItems);
                        $("#button-operator").val('<li><a data-val="all" href="#">All</a></li>');
                        operatorNames = "("+ operatorNames+")";
                        loadSP(operatorNames);

                        $("#dropdown-operator li a").click(function () {
                            $("#button-operator").text($(this).text());
                            $("#button-operator").append('&nbsp;<span class="caret"></span>');
                            $("#button-operator").val($(this).text());
                            var opName = $(this).data('val');
                            if (opName.toString() != "all") {
                                loadSP(opName);
                            } else {
                                loadSP(operatorNames);
                            }


                            operatorSelected = true;
                            //                         getFilterdResult(initloading);
                        });
                    }
                });
            }
        }

        function loadSP (clickedOperator) {
            conf[PROVIDER_CONF][TABLE_NAME] = STREAMS.API_SUMMERY;
            conf[PROVIDER_CONF][PROVIDER_NAME] = TYPE.OPERATOR;

            conf.operatorName = clickedOperator;
            selectedOperator = conf.operatorName;
            serviceProviderId = 0;

            if (loggedInUser.isServiceProvider) {
                loadApp("\"" + loggedInUser.username + "\"", selectedOperator);
            } else {
                $.ajax({
                    url: gadgetLocation + '/gadget-controller.jag?action=getData',
                    method: METHOD.POST,
                    data: JSON.stringify(conf),
                    contentType: CONTENT_TYPE,
                    async: false,
                    success: function (data) {
                        $("#dropdown-sp").empty();
                        $("#button-sp").text('All Service provider');
                        $("#button-sp").append('&nbsp;<span class="caret"></span>');
                        var spItems = '';
                        var spIds = [];
                        var loadedSps = [];
                        spIds.push(serviceProviderId);
                        spItems += '<li><a data-val="0" href="#">All Service Provider</a></li>';
                        for ( var i =0 ; i < data.length; i++) {
                            var sp = data[i];
                            if($.inArray(sp.serviceProviderId, loadedSps) < 0){
                                spItems += '<li><a data-val='+ sp.serviceProviderId +' href="#">' + sp.serviceProvider.replace("@carbon.super","") +'</a></li>'
                                spIds.push(" "+ "\"" + sp.serviceProviderId + "\"");
                                loadedSps.push(sp.serviceProviderId);
                            }
                        }

                        $("#dropdown-sp").html(spItems);

                        $("#button-sp").val('<li><a data-val="0" href="#">All Service provider</a></li>');

                        loadApp(spIds,selectedOperator);
                        $("#dropdown-sp li a").click(function(){

                            $("#button-sp").text($(this).text());
                            $("#button-sp").append('&nbsp;<span class="caret"></span>');
                            $("#button-sp").val($(this).text());
                            serviceProviderId = $(this).data('val');

                            // serviceProviderId = spIds;
                            // if(selectedOperator.toString() == "all") {

                            if(serviceProviderId != "0") {
                                loadApp( "\"" + serviceProviderId +"\"", selectedOperator.toString());
                            } else {
                                // if(loggedInUser.isOperatorAdmin) {
                                //     loadSP(loggedInUser.operatorNameInProfile);
                                // } else {
                                loadApp(  spIds , selectedOperator.toString());
                                // }
                            }
                            // } else {
                            //     if(spIds != "0") {
                            //         loadApp( "\"" +spIds+"\"","\"" + selectedOperator+"\"");
                            //     } else {
                            //         if(loggedInUser.isOperatorAdmin) {
                            //             loadSP(loggedInUser.operatorNameInProfile);
                            //         } else {
                            //             loadApp(  spIds , selectedOperator.toString());
                            //         }
                            //     }
                            // }
                            //      getFilterdResult(initloading);
                        });
                    }
                });
            }
        }

        function loadApp (sps,clickedOperator) {
            conf[PROVIDER_CONF][TABLE_NAME] = STREAMS.API_SUMMERY;
            conf[PROVIDER_CONF][PROVIDER_NAME] = TYPE.SP;
            //applicationId = 0;
            //if(sps != "0") {
            conf.serviceProvider = sps;

            //}
            conf.operatorName = clickedOperator; //TODO: check this brackets.
            $.ajax({
                url: gadgetLocation + '/gadget-controller.jag?action=getData',
                method: METHOD.POST,
                data: JSON.stringify(conf),
                contentType: CONTENT_TYPE,
                async: false,
                success: function (data) {
                    $("#dropdown-app").empty();
                    $("#button-app").text('All Application');
                    $("#button-app").append('&nbsp;<span class="caret"></span>');
                    var apps = [];
                    var loadedApps = [];
                    var selectedApp = [];
                    var appItems = '<li><a data-val="0" href="#">All Application</a></li>';

                    for ( var i = 0 ; i < data.length; i++) {
                        var app = data[i];
                        if($.inArray(app.applicationId, loadedApps) < 0 ) {
                            appItems += '<li><a data-val='+ app.applicationId +' href="#">' + app.applicationName +'</a></li>'
                            apps.push(" "+ app.applicationId);
                            loadedApps.push(app.applicationId);
                        }
                    }

                    $("#dropdown-app").html($("#dropdown-app").html() + appItems);
                    $("#button-app").val('<li><a data-val="0" href="#">All Application</a></li>');
                    loadApi(apps);

                    $("#dropdown-app li a").click(function() {

                        $("#button-app").text($(this).text());
                        $("#button-app").append('&nbsp;<span class="caret"></span>');
                        $("#button-app").val($(this).text());

                        selectedApp = $(this).data('val');
                        applicationId = selectedApp;
                        application=$(this).text();
                        if(selectedApp == "0") {
                            loadApi(apps);
                            //     getFilterdResult(initloading);
                        } else {
                            loadApi(selectedApp);
                            //       getFilterdResult(initloading);
                        }
                    });
                }
            });
        }

        function loadApi (apps) {
            conf[PROVIDER_CONF][TABLE_NAME] = STREAMS.API_SUMMERY;
            conf[PROVIDER_CONF][PROVIDER_NAME] = TYPE.APP;
            conf.applicationId = "("+apps+")";
            apiId = 0;
            $.ajax({
                url: gadgetLocation + '/gadget-controller.jag?action=getData',
                method: METHOD.POST,
                data: JSON.stringify(conf),
                contentType: CONTENT_TYPE,
                async: false,
                success: function (data) {
                    $("#dropdown-api").empty();
                    $("#button-api").text('All API');
                    $("#button-api").append('&nbsp;<span class="caret"></span>');
                    var apis = [];
                    var loadedApis = [];
                    var apiItems = '<li><a data-val="0" href="#">All Api</a></li>';
                    for ( var i =0 ; i < data.length; i++) {
                        var api = data[i];
                        if($.inArray(api.apiID, loadedApis) < 0){
                            apiItems += '<li><a data-val='+ api.apiID +' href="#">' + api.api +'</a></li>';
                            loadedApis.push(api.apiID);
                        }
                    }

                    $("#dropdown-api").html($("#dropdown-api").html() + apiItems);
                    $("#button-api").val('<li><a data-val="0" href="#">All Api</a></li>');

                    getFilterdResult(initloading);
                    $("#dropdown-api li a").click(function() {
                        $("#button-api").text($(this).text());
                        $("#button-api").append('&nbsp;<span class="caret"></span>');
                        $("#button-api").val($(this).text());
                        apiId = $(this).data('val');
                        getFilterdResult(initloading);
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

    /*$("#dropdown-type li a").click(function(){
     $("#button-type").text($(this).text());
     $("#button-type").append('&nbsp;<span class="caret"></span>');
     $("#button-type").val($(this).text());
     getFilterdResult(false);
     });*/
});
