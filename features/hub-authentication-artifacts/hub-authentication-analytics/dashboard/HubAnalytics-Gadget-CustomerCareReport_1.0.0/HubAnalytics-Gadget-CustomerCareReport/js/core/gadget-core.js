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

var conf, datatableConf;

$(function () {
    var gadgetLocation;
    var operatorName = "all",
        serviceProviderId = 0,
        applicationId = 0;
    var role;
    var selectedOperator;
    var operatorSelected = false;
    var operatorNames = [];

    var mytable;

    var init = function () {
       
        $.ajax({
            url: gadgetLocation + '/conf.json',
            method: "GET",
            contentType: "application/json",
            async: false,
            success: function (data) {
                conf = JSON.parse(data);
                if (operatorSelected) {
                    conf.operatorName = selectedOperator;
                } else {
                    conf.operatorName = operatorName;
                }
                conf.serviceProvider = serviceProviderId;      
                conf.applicationName = applicationId;
                conf.dateStart = moment(moment($("#reportrange").text().split("-")[0]).format("MMMM D, YYYY hh:mm A")).valueOf();
                conf.dateEnd = moment(moment($("#reportrange").text().split("-")[1]).format("MMMM D, YYYY hh:mm A")).valueOf();
                loadOperator();
                addDatatable();
            }
        });        
    };
    // Add event listener for opening and closing details
    $('#example').on('click', 'td.details-control', function () {
        var tr = $(this).closest('tr');
        var row = mytable.row(tr);
        if (row.child.isShown()) {
            // This row is already open - close it
            row.child.hide();
            tr.removeClass('shown');
        } else {
            // Open this row
            row.child(format(row.data())).show();
            tr.addClass('shown');
        }
    });

    function getDatatableConf() {
        datatableConf = {};
        datatableConf.operator = $("#button-operator").text();
        datatableConf.operatorList = operatorNames;
        datatableConf.serviceProvider = serviceProviderId;
        datatableConf.application = applicationId;
        datatableConf.dateStart = moment(moment($("#reportrange").text().split("-")[0]).format("MMMM D, YYYY hh:mm A")).valueOf();
        datatableConf.dateEnd = moment(moment($("#reportrange").text().split("-")[1]).format("MMMM D, YYYY hh:mm A")).valueOf();
        return datatableConf;
    };


    
    function addDatatable(){
        

        mytable = $('#example').DataTable({
            "processing": true,
            "serverSide": true,
            scrollY: 800,
            autoWidth: false,
            scrollCollapse: true,
            "ajax": {
                "url": gadgetLocation + '/gadget-controller.jag?action=getData2',
                "data": function (d) {
                    d.a = getDatatableConf();
                }
            },
            "columns": [{
                    "className": 'details-control',
                    "orderable": false,
                    "data": null,
                    "defaultContent": ''
                },
                {
                    "data": "api"
                },
                {
                    "data": "serviceProvider"
                },
                {
                    "data": "responseTime",
                    "render": function (data) {
                        var responseTime_unix_sec = parseInt(data / 1000);
                        return moment.unix(responseTime_unix_sec).format("MM-DD-YYYY HH:mm:ss");
                    }
                },
                {
                    "data": "operatorName"
                },
                {
                    "data": "msisdn"
                },
                {
                    "data": "applicationName"
                },
                {
                    "data": "requestId"
                },
                {
                    "data": "isSuccess"
                },
                {
                    "data": "apiPublisher"
                }
            ]
        });
    };

    $('#button-search, button[role="date-update"], #btnCustomRange ').click(function () {
        reloadDatatable();
    });

    function reloadDatatable() {
        getDatatableConf();
        mytable.ajax.reload();
    };

    function loadOperator() {
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
                operatorNames = [];
                var loadedOperator = [];
                operatorNames.push(operatorName);
                operatorsItems += '<li><a data-val="all" href="#">All</a></li>';
                for (var i = 0; i < data.length; i++) {
                    var operator = data[i];
                    if ($.inArray(operator.operatorName, loadedOperator) < 0) {
                        operatorsItems += '<li><a data-val=' + operator.operatorName + ' href="#">' + operator.operatorName + '</a></li>';
                        operatorNames.push(" " + operator.operatorName);
                        loadedOperator.push(operator.operatorName);
                    }
                }
                $("#dropdown-operator").html($("#dropdown-operator").html() + operatorsItems);
                $("#button-operator").val('<li><a data-val="all" href="#">All</a></li>');
                if ("operatoradmin" == role || "customercare" == role) {
                    loadSP(operatorName);
                } else {
                    loadSP(operatorNames);
                }
                $("#dropdown-operator li a").click(function () {
                    //        alert('op dw');
                    $("#button-operator").text($(this).text());
                    $("#button-operator").append('<span class="caret"></span>');
                    $("#button-operator").val($(this).text());
                    operatorNames = $(this).data('val');
                    loadSP(operatorNames);
                    operatorSelected = true;
                });
            }
        });
    };

    function loadSP(clickedOperator) {
        conf["provider-conf"]["tableName"] = "ORG_WSO2TELCO_ANALYTICS_HUB_STREAM_API_SUMMARY";
        conf["provider-conf"]["provider-name"] = "operator";
        conf.operatorName = "(" + clickedOperator + ")";
        selectedOperator = conf.operatorName;
        serviceProviderId = 0;
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
                for (var i = 0; i < data.length; i++) {
                    var sp = data[i];
                    if ($.inArray(sp.serviceProviderId, loadedSps) < 0) {
                        spItems += '<li><a data-val=' + sp.serviceProviderId + ' href="#">' + sp.serviceProvider.replace("@carbon.super", "") + '</a></li>'
                        spIds.push(" " + sp.serviceProviderId);
                        loadedSps.push(sp.serviceProviderId);
                    }
                }
                $("#dropdown-sp").html(spItems);
                $("#button-sp").text('All');
                $("#button-sp").val('<li><a data-val="0" href="#">All</a></li>');
                loadApp(spIds, selectedOperator);
                $("#dropdown-sp li a").click(function () {
                    alert('sp dw');
                    $("#button-sp").text($(this).text());
                    $("#button-sp").append('<span class="caret"></span>');
                    $("#button-sp").val($(this).text());
                    spIds = $(this).data('val');
                    serviceProviderId = spIds;
                    loadApp(spIds, selectedOperator);
                });
            }
        });
    };

    function loadApp(sps, clickedOperator) {
        conf["provider-conf"]["tableName"] = "ORG_WSO2TELCO_ANALYTICS_HUB_STREAM_API_SUMMARY";
        conf["provider-conf"]["provider-name"] = "sp";
        conf.operatorName = "(" + clickedOperator + ")";
        applicationId = 0;
        conf.serviceProvider = "(" + sps + ")";
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
                for (var i = 0; i < data.length; i++) {
                    var app = data[i];
                    if ($.inArray(app.applicationId, loadedApps) < 0) {
                        appItems += '<li><a data-val=' + app.applicationId + ' href="#">' + app.applicationName + '</a></li>'
                        apps.push(" " + app.applicationId);
                        loadedApps.push(app.applicationId);
                    }
                }
                $("#dropdown-app").html($("#dropdown-app").html() + appItems);
                $("#button-app").val('<li><a data-val="0" href="#">All</a></li>');
                $("#button-app").text('All');
                $("#dropdown-app li a").click(function () {
                    $("#button-app").text($(this).text());
                    $("#button-app").append('<span class="caret"></span>');
                    $("#button-app").val($(this).text());
                    apps = $(this).data('val');
                    applicationId = apps;
                });
            }
        });
    };

    //to hide error messages visible to user. Remove following line for development.
    $.fn.dataTable.ext.errMode = 'none';

    getGadgetLocation(function (gadget_Location) {        
        gadgetLocation = gadget_Location;
          
        init();
        
            
        $('input[name="daterange"]').daterangepicker({
            timePicker: true,
            timePickerIncrement: 30,
            locale: {
                format: 'MM/DD/YYYY h:mm A'
            }
        });
    });

    function format(d) {
        // `d` is the original data object for the row
        return '<table cellpadding="5" cellspacing="0" border="0" style="padding-left:50px;">' +
            '<tr>' +
            '<td>Json Content:</td>' +
            '<td><pre style="width:1000px !important ">' + formatJsonBody(d.jsonBody) + '</pre></td>' +
            '</tr>' +
            '<tr style="overflow: hidden;">' +
            '<td>Message:</td>' +
            '<td ><pre style="width:1000px !important ">' + formatJsonBody(JSON.stringify(d)) + '</pre></td>' +
            '</tr>' +
            '</table>';
    };

    function formatJsonBody(json) {
        var jsonFormatted = json;
        try {
            jsonFormatted = JSON.stringify(JSON.parse(json), null, '\t');
        } catch (err) {
            console.log('invalid json');
        }
        return jsonFormatted;
    };
});