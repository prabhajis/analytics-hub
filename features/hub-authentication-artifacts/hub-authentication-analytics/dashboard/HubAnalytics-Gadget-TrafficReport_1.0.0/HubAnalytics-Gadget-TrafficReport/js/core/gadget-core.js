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

        var operatorName = "all", serviceProviderId = 0, apiId = 0, applicationId = 0,application="0";
        var loggedInUser;
        var selectedOperator;
        var operatorSelected = false;

        var mytable;
        var selectedFiles = [];

        $(document).ready(function(){
            getFilterdResult();
        });

        var init = function () {
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
                    conf.dateStart = moment(moment($("#reportrange").text().split("-")[0]).format("MMMM D, YYYY hh:mm A")).valueOf();
                    conf.dateEnd = moment(moment($("#reportrange").text().split("-")[1]).format("MMMM D, YYYY hh:mm A")).valueOf();

                    if($("#button-type").val().toLowerCase().trim() == ERROR_TRAFFIC) {
                        conf[PROVIDER_CONF][TABLE_NAME] = STREAMS.FAILURE_SUMMARY_PER_DAY;
                    } else {
                        conf[PROVIDER_CONF][TABLE_NAME] = STREAMS.TRAFFIC_SUMMARY_PER_DAY;
                    }

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

        function initdatatable () {
            adddataTable();

            //regestered after datatable added.
            $('#listReportTable tbody').on('change', 'input[type="checkbox"]', function(){
                if (!this.checked) {
                    var selectallstat = $('#select-all').get(0);
                    if(selectallstat && selectallstat.checked && ('indeterminate' in selectallstat)){
                        selectallstat.indeterminate = true;
                    }
                }
            });
        }

        function reloadTable () {
            mytable.ajax.reload(function(rowdata) {
                //disable button when datatable is reloading.enable only if specified extension found.
                mytable.buttons().disable();

                $('#select-all').get(0).indeterminate = false;
                $('#select-all').prop('checked', false);

                //disable buttons if nodata exists
                var data = rowdata.data;

                data.forEach(function (row) {
                    var ext = row.filename.split(".").pop();
                    if (ext == "csv") {
                        mytable.buttons().enable();
                    }
                });
            });
        }

        function adddataTable () {

            mytable = $('#listReportTable').DataTable({
                "processing": true,
                "searching": false,
                "serverSide": true,
                "select": true,
                scrollY: 120,
                autoWidth: false,
                scrollCollapse: true,
                "paging": false,

                "ajax": {
                    "url": gadgetLocation + '/gadget-controller.jag?action=available'
                },
                "rowId": 'myrowid',
                "columns": [{

                    "orderable": false,
                    "data": null,
                    "render":function (data) {
                        var checkbox;
                        var ext = data.filename.split(".").pop();
                        if (ext == 'wte') {
                            checkbox = '<input type="checkbox" name="id[]" disabled="disabled">';
                        } else {
                            checkbox = '<input type="checkbox" name="id[]" >';
                        }
                        return checkbox;
                    }
                },
                    {
                        "data": "filename"
                    },
                    {
                        "data": "filename",
                        "render": function (data) {
                            var status;
                            var ext = data.split(".").pop();
                            if (ext == 'wte') {
                                status = "In Progress"
                            } else if (ext == 'csv') {
                                status = "Downloadable"
                            }
                            return status;
                        }
                    }
                ],
                dom: 'frtipB',
                "buttons": [
                    {
                        "text": 'Delete',
                        "action": function ( e, dt, node, config ) {

                            $("input:checked", mytable.rows().nodes()).each(function(){

                                var fileid = (mytable.row( $(this).parents('tr')).id());
                                if (!selectedFiles.includes(fileid)) {
                                    selectedFiles.push(fileid);
                                }
                            });
                            $.ajax({
                                url: gadgetLocation + '/gadget-controller.jag?action=removefile',
                                method: METHOD.POST,
                                data: JSON.stringify({"files":selectedFiles}),
                                contentType: CONTENT_TYPE,
                                async: false,
                                success: function (data) {
                                    if (data.fileDeleted) {
                                        reloadTable();
                                    }
                                }
                            });
                            selectedFiles = [];
                        }
                    },
                    {
                        "text": 'Download',
                        "action": function (e, dt, node, config) {
                            $("input:checked", mytable.rows().nodes()).each(function(){
                                var fileid = (mytable.row( $(this).parents('tr')).id());
                                if (!selectedFiles.includes(fileid)) {
                                    selectedFiles.push(fileid);
                                }
                            });
                            $.ajax({
                                url: gadgetLocation + '/gadget-controller.jag?action=downlaodzip',
                                method: METHOD.POST,
                                data: JSON.stringify({"files":selectedFiles}),
                                contentType: CONTENT_TYPE,
                                async:false,
                                success:function (data) {
                                    if (data.zipStatus) {
                                        downloadFile(0)
                                    }
                                }
                            });
                            selectedFiles = [];
                        }
                    }
                ]
            });

        }

        $('#select-all').on('click', function () {
            var rows = mytable.rows().nodes();
            if (this.checked) {
                $('input[type="checkbox"]', rows).prop('checked', function (index,val) {
                    var fileid = (mytable.row( $(this).parents('tr')).id());
                    var ext = fileid.split('.').pop();
                    if (ext == 'wte') {
                        return false;
                    } else if (ext == 'csv') {
                        return true;
                    }
                });
            } else {
                $('input[type="checkbox"]', rows).prop('checked', false);
            }
        });

        //to hide error messages visible to user. Remove following line for development.
        $.fn.dataTable.ext.errMode = 'none';

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
                        $("#apiContainer").removeClass("col-top-pad");

                        //conf.operatorName = operatorName;
                    } else if (!(loggedInUser.isAdmin) && loggedInUser.isServiceProvider) {
                        $("#apiContainer").removeClass("col-top-pad");
                        $("#typeContainer").removeClass("col-top-pad");
                    }

                },
                complete : function (xhr, textStatus) {
                    if (xhr.status == "403") {
                        window.top.location.reload(false);
                    }
                }
            });
        };

        var getProviderData = function (){

            if($("#button-type").val().toLowerCase().trim() == ERROR_TRAFFIC) {
                conf[PROVIDER_CONF][TABLE_NAME] = STREAMS.HUB_STREAM_FAILURE_SUMMARY_PER_;
            } else {
                conf[PROVIDER_CONF][TABLE_NAME] = STREAMS.TRAFFIC_SUMMARY_PER_;
            }

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
            if(providerData != '') {
                $("#generateCSV").show();
                if(loggedInUser.isAdmin){
                    $("#tableSelect").show();
                }

            } else {
                $("#generateCSV").hide();
                $("#tableSelect").hide();

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

        function getFilterdResult() {
            $("#canvas").html("");
            $("#canvas2").html("");
            $("#showCSV").hide();
            getGadgetLocation(function (gadget_Location) {
                getLoggedInUser();
                gadgetLocation = gadget_Location;
                init();
                getProviderData();
                drawGadget();
            });
        };

        $("#button-search").click(function() {
            getFilterdResult();
        });

        $("#btnLastDay").click(function() {
            getFilterdResult();
        });

        $("#btnLastMonth").click(function() {
            getFilterdResult();
        });


        $("#btnLastYear").click(function() {
            getFilterdResult();
        });

        $('#btnCustomRange').on('apply.daterangepicker', function(ev, picker) {
            getFilterdResult();
        });


        $("#button-generate-tr").click(function () {
            getLoggedInUser();
            getGadgetLocation(function (gadget_Location) {
                gadgetLocation = gadget_Location;
                $("#output").html("");
                $("#nodata_info").html("");
                if(operatorSelected) {
                    conf.operatorName =  selectedOperator;
                } else {
                    conf.operatorName =  operatorName;
                }
                conf.serviceProvider = serviceProviderId;
                conf.api = apiId;
                conf.applicationName = applicationId;
                conf.applicationf=$("#button-app").text();
                conf.operatorf=$("#button-operator").text();
                conf.spf= $("#button-sp").text();
                conf.apif=$("#button-api").text();

                conf.dateStart = moment(moment($("#reportrange").text().split("-")[0]).format("MMMM D, YYYY hh:mm A")).valueOf();
                conf.dateEnd = moment(moment($("#reportrange").text().split("-")[1]).format("MMMM D, YYYY hh:mm A")).valueOf();

                if($("#button-type").val().toLowerCase().trim() == ERROR_TRAFFIC) {
                    conf[PROVIDER_CONF][TABLE_NAME] = STREAMS.HUB_STREAM_FAILURE_SUMMARY_PER_;
                } else {
                    conf[PROVIDER_CONF][TABLE_NAME] = STREAMS.TRAFFIC_SUMMARY_PER_;
                }

                var btn =  $("#button-generate-tr");
                btn.prop('disabled', true);
                setTimeout(function(){
                    btn.prop('disabled', false);
                }, 3000);

                $.ajax({
                    url: gadgetLocation + '/gadget-controller.jag?action=generateCSV',
                    method: METHOD.POST,
                    data: JSON.stringify(conf),
                    contentType: CONTENT_TYPE,
                    async: false,
                    success: function (data) {
                        $("#output").show();
                        $("#showCSV").hide();
                        $("#showMsg").show();
                        $("#list-available-report").show();
                        $("#output").html('<div id="success-message" class="alert alert-success"><strong>Report is generating</strong> '
                            + "Please refresh the traffic report"
                            + '</div>' + $("#output").html());
                        $('#success-message').fadeIn().delay(2000).fadeOut();
                    }
                });
            });
        });

        $("#list-available-report").click(function () {
            reloadTable();
            $("#output").hide();
            $("#showMsg").hide();
            $("#showCSV").show();
            $("#showCSV").attr('style','');
            $(".dt-buttons").attr('style','float:right');
        });

        getGadgetLocation(function (gadget_Location) {
            gadgetLocation = gadget_Location;
            init();
            initdatatable();
            getLoggedInUser();
            loadOperator();
            $("#generateCSV").hide();
            $("#tableSelect").hide();
            $("#showCSV").hide();

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
                            $("#button-operator").val('<li><a data-val="all" href="#">All Operator</a></li>');

                            operatorNames = "("+operatorNames+")";

                            loadSP(operatorNames);

                            $("#dropdown-operator li a").click(function () {
                                $("#button-operator").text($(this).text());
                                $("#button-operator").append('&nbsp;<span class="caret"></span>');
                                $("#button-operator").val($(this).text());
                                if ($(this).data('val').toString() != 'all'){
                                    loadSP($(this).data('val'));
                                }
                                else {
                                    loadSP(operatorNames);
                                }
                                operatorSelected = true;
                                getFilterdResult();
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
                serviceProviderId =0;

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
                            spItems += '<li><a data-val="0" href="#">All Service provider</a></li>';
                            for (var i = 0; i < data.length; i++) {
                                var sp = data[i];
                                if($.inArray(sp.serviceProviderId, loadedSps) < 0){
                                    spItems += '<li><a data-val='+ sp.serviceProviderId +' href="#">' + sp.serviceProvider.replace("@carbon.super","") +'</a></li>'
                                    spIds.push(" "+ "\"" + sp.serviceProviderId + "\"");
                                    loadedSps.push(sp.serviceProviderId);
                                }
                            }

                            spIds = "("+spIds+")";
                            $("#dropdown-sp").html(spItems);

                            $("#button-sp").val('<li><a data-val="0" href="#">All Service provider</a></li>');
                            loadApp(spIds,selectedOperator);
                            $("#dropdown-sp li a").click(function(){

                                $("#button-sp").text($(this).text());
                                $("#button-sp").append('&nbsp;<span class="caret"></span>');
                                $("#button-sp").val($(this).text());
                                serviceProviderId = $(this).data('val');
                                if(serviceProviderId != "0") {
                                    loadApp( "\"" + serviceProviderId +"\"", selectedOperator.toString());
                                } else {
                                    if(loggedInUser.isOperatorAdmin) {
                                        loadSP(loggedInUser.operatorNameInProfile);
                                    } else {
                                        loadApp(  spIds , selectedOperator.toString());
                                    }
                                }
                                getFilterdResult();
                            });
                        }
                    });
                }
            }

            function loadApp (sps,clickedOperator) {
                conf[PROVIDER_CONF][TABLE_NAME] = STREAMS.API_SUMMERY;
                conf[PROVIDER_CONF][PROVIDER_NAME] = TYPE.SP;
                applicationId = 0;
                application="0";
                if(sps != "0") {
                    conf.serviceProvider = sps;
                }
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

                        $("#dropdown-app").html( $("#dropdown-app").html() + appItems);
                        $("#button-app").val('<li><a data-val="0" href="#">All</a></li>');

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
                                getFilterdResult();
                            } else {
                                loadApi(selectedApp);
                                getFilterdResult();
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
                        $("#button-api").text('All Api');
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

                        $("#dropdown-api").html( $("#dropdown-api").html() + apiItems);
                        $("#button-api").val('<li><a data-val="0" href="#">All</a></li>');

                        $("#dropdown-api li a").click(function() {
                            $("#button-api").text($(this).text());
                            $("#button-api").append('&nbsp;<span class="caret"></span>');
                            $("#button-api").val($(this).text());
                            apiId = $(this).data('val');
                            getFilterdResult();
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
            $("#button-type").append('&nbsp;<span class="caret"></span>');
            $("#button-type").val($(this).text());
            getFilterdResult();
        });
    });


    function downloadFile(index) {
        getGadgetLocation(function (gadget_Location) {
            gadgetLocation = gadget_Location;
            location.href = gadgetLocation + '/gadget-controller.jag?action=get&index=' + index;
        });
    }