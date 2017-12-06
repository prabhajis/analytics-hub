
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

    var operatorName = "all", serviceProviderId = 0;
    var loggedInUser;
    var selectedOperator;
    var operatorSelected = false;

    var mytable;
    var reportType = '';
    var selectedFiles = [];

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
        mytable.buttons().disable();

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
        var endpoint;
        if (reportType == 'csv') {
            endpoint = 'availableCSV';
        } else if (reportType == 'error-csv') {
            endpoint = 'availableErrorCSV';
        } else if (reportType == 'pdf') {
            endpoint = 'availablePDF';
        }
        mytable.ajax.url(gadgetLocation + '/gadget-controller.jag?action=' + endpoint);

        mytable.ajax.reload(function(rowdata) {
            //disable button when datatable is reloading.enable only if specified extension found.

            $('#select-all').get(0).indeterminate = false;
            $('#select-all').prop('checked', false);

            //disable buttons if nodata exists
            var data = rowdata.data;
            var checkednum = 0;

            $('input[type="checkbox"]').on('click', function () {
                if(this.checked) {
                    checkednum++;
                    data.forEach(function (row) {
                        var ext = row.filename.split(".").pop();
                        if (ext == "csv" || ext == "pdf") {
                            mytable.buttons().enable();
                        }
                    });
                } else {
                    checkednum--;
                    if (checkednum == 0) {
                        mytable.buttons().disable();
                    }
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
            scrollY: 1000,
            autoWidth: false,
            scrollCollapse: true,
            "paging": false,

            "ajax": {
                "url": gadgetLocation + '/gadget-controller.jag?action=csv',
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
                            status = "In Progress";
                        } else if (ext == 'csv') {
                            status = "Downloadable";
                        } else if (ext == 'pdf') {
                            status = "Downloadable";
                        }
                        return status;
                    }
                }
            ],
            dom: 'frtipB',
            "buttons": [
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
                            data: JSON.stringify({'files':selectedFiles, 'type':reportType}),
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
                },
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
                            data: JSON.stringify({"files":selectedFiles, 'type':reportType}),
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
                } else if (ext == 'pdf') {
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
                    $("#yearContainer").removeClass("col-top-pad");
                    $("#monthContainer").removeClass("col-top-pad");

                    //conf.operatorName = operatorName;
                } else if (!(loggedInUser.isAdmin) && loggedInUser.isServiceProvider) {
                    $("#yearContainer").removeClass("col-top-pad");
                    $("#monthContainer").removeClass("col-top-pad");
                }


                if(!(loggedInUser.isAdmin)) {
                    $("#directiondd").hide();
                    $("#repriceContainer").hide();
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

        return providerData;
    };

    var initiateReprice = function (){

        if (confirm('Are you sure you want to Initiate Repricing?')) {
            console.log('Inintiating repricing');
            $.ajax({
                url: gadgetLocation + '/gadget-controller.jag?action=initiateReprice',
                method: METHOD.POST,
                data: JSON.stringify(conf),
                contentType: CONTENT_TYPE,
                async: false,
                success: function (data) {
                    providerData = data;
                }
            });

            return providerData;
        }
    };

    function getFilterdResult() {
        $("#showCSV").hide();
        getGadgetLocation(function (gadget_Location) {
            gadgetLocation = gadget_Location;
            init();
            getProviderData();
        });
    };

    $("#button-search").click(function() {
        getGadgetLocation(function (gadget_Location) {
            getLoggedInUser();
            gadgetLocation = gadget_Location;
            init();
            getProviderData();
        });
    });

    function setConfDara() {
    	conf.applicationf=$("#button-app").text();
		conf.spf= $("#button-sp").text();
		conf.apif=$("#button-api").text();
        conf.directionf = $("#button-dir").text();
        conf.operatorf = $("#button-operator").text();
    };

    $("#button-initiate-reprice").click(function () {
        initiateReprice();
    });

    $("#button-generate-bill-csv").click(function () {
        getLoggedInUser();
        getGadgetLocation(function (gadget_Location) {
            gadgetLocation = gadget_Location;
            $("#output").html("");
            if(operatorSelected) {
                conf.operatorName =  selectedOperator;
            } else {
                conf.operatorName =  operatorName;
            }
            setConfDara();
            conf.serviceProvider = serviceProviderId;

            var year = $("#button-year").val();
            var month = $("#button-month").val();
            var isDirectionSet = true;

            if(loggedInUser.isAdmin) {
                var direction = $("#button-dir").val();
                if (direction === "") {
                    isDirectionSet = false;
                    $("#popupcontent p").html('Please select direction');
                    $('#notifyModal').modal('show');
                    return;
                } else {
                    conf.direction = direction;
                }
            }

            if(year === "") {
                $("#popupcontent p").html('Please select year');
                $('#notifyModal').modal('show');
            }
            else if(month === "") {
                $("#popupcontent p").html('Please select month');
                $('#notifyModal').modal('show');
            } else if (isDirectionSet) {

                $("#list-summery-report").removeClass("hidden");
                conf.year = year;
                conf.month = month;

                var btn = $("#button-generate-bill-csv");
                btn.prop('disabled', true);
                setTimeout(function () {
                    btn.prop('disabled', false);
                }, 2000);

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
                        $("#output").html('<div id="success-message" class="alert alert-success"><strong>Report is generating</strong> '
                            + "Please refresh the billing report"
                            + '</div>' + $("#output").html());
                        $('#success-message').fadeIn().delay(2000).fadeOut();
                    }
                });
            }
        });
    });

    $("#button-generate-error-csv").click(function () {
        getLoggedInUser();
        getGadgetLocation(function (gadget_Location) {
            gadgetLocation = gadget_Location;
            $("#output").html("");
            if(operatorSelected) {
                conf.operatorName =  selectedOperator;
            } else {
                conf.operatorName =  operatorName;
            }
            conf.serviceProvider = serviceProviderId;
            setConfDara();
            var year = $("#button-year").val();
            var month = $("#button-month").val();
            var isDirectionSet = true;

            if(loggedInUser.isAdmin) {
                var direction = $("#button-dir").val();
                if (direction === "") {
                    isDirectionSet = false;
                    $("#popupcontent p").html('Please select direction');
                    $('#notifyModal').modal('show');
                    return;
                } else {
                    conf.direction = direction;
                }
            }

            if(year === "") {
                $("#popupcontent p").html('Please select year');
                $('#notifyModal').modal('show');
            }
            else if(month === "") {
                $("#popupcontent p").html('Please select month');
                $('#notifyModal').modal('show');
            } else if (isDirectionSet) {

                $("#list-error-report").removeClass("hidden");
                conf.year = year;
                conf.month = month;

                var btn = $("#button-generate-bill-csv");
                btn.prop('disabled', true);
                setTimeout(function () {
                    btn.prop('disabled', false);
                }, 2000);

                $.ajax({
                    url: gadgetLocation + '/gadget-controller.jag?action=generateErrorCSV',
                    method: METHOD.POST,
                    data: JSON.stringify(conf),
                    contentType: CONTENT_TYPE,
                    async: false,

                    success: function (data) {
                        $("#output").show();
                        $("#showCSV").hide();
                        $("#showMsg").show();
                        $("#list-error-report").show();
                        $("#output").html('<div id="success-message" class="alert alert-success"><strong>Report is generating</strong> '
                            + "Please refresh the billing error report"
                            + '</div>' + $("#output").html());
                        $('#success-message').fadeIn().delay(2000).fadeOut();
                    }
                });
            }
        });
    });

    $("#button-generate-bill-pdf").click(function () {
        getLoggedInUser();
        $("#output").html("");
        getGadgetLocation(function (gadget_Location) {
            var serviceProviderName = $("#button-sp").val();

            gadgetLocation = gadget_Location;
            if(operatorSelected) {
                conf.operatorName =  selectedOperator;
            } else {
                conf.operatorName =  operatorName;
            }
            setConfDara();
            conf.serviceProvider = serviceProviderId;
            var year = $("#button-year").val();
            var month = $("#button-month").val();
            var isDirectionSet = true;

            if(loggedInUser.isAdmin) {
                var direction = $("#button-dir").val();
                if (direction === "") {
                    isDirectionSet = false;
                    $("#popupcontent p").html('Please select direction');
                    $('#notifyModal').modal('show');
                    return;
                } else {
                    conf.direction = direction;
                }
            }

            if(year === "") {
                $("#popupcontent p").html('Please select year');
                $('#notifyModal').modal('show');
            } else if(month === "") {
                $("#popupcontent p").html('Please select month');
                $('#notifyModal').modal('show');
            } else if (isDirectionSet) {
                conf.year = year;
                conf.month = month;
                $("#list-the-bill").removeClass("hidden");


                var btn = $("#button-generate-bill-pdf");
                btn.prop('disabled', true);
                setTimeout(function () {
                    btn.prop('disabled', false);
                }, 2000);

                setTimeout(function () {
                    $.ajax({
                        url: gadgetLocation + '/gadget-controller.jag?action=generateBill',
                        method: "POST",
                        data: JSON.stringify(conf),
                        contentType: "application/json",
                        async: true,
                        success: function (data) {
                            $("#output").show();
                            $("#showCSV").hide();
                            $("#showMsg").show();
                            $("#output").html('<div id="success-message" class="alert alert-success"><strong>Report is generating</strong> '
                                + "Please refresh the billing report"
                                + '</div>' + $("#output").html());
                            $('#success-message').fadeIn().delay(2000).fadeOut();
                        }
                    });
                }, 100);
            }
        });
    });

    $("#list-summery-report").click(function () {
        getLoggedInUser();
        reportType = 'csv';
        reloadTable();
        $("#output").html("");
        $("#showMsg").hide();
        $("#showCSV").show();
        $("#showCSV").attr('style','');
        $(".dt-buttons").attr('style','float:right');

    });

    $("#list-error-report").click(function () {
        getLoggedInUser();
        reportType = 'error-csv';
        reloadTable();
        $("#output").html("");
        $("#showMsg").hide();
        $("#showCSV").show();
        $("#showCSV").attr('style','');
        $(".dt-buttons").attr('style','float:right');
    });

    $("#list-the-bill").click(function () {
        getLoggedInUser();
        reportType = 'pdf';
        reloadTable();
        $("#output").html("");
        $("#showMsg").hide();
        $("#showCSV").show();
        $("#showCSV").attr('style','');
        $(".dt-buttons").attr('style','float:right');
    });

    var createYearSelectBox = function () {
        var date = new Date();
        var fixYear = date.getFullYear();
        var currentMonth = date.getMonth();
        var currentYear = fixYear;
        var allMonths = false;

        $("#button-year").text(currentYear);
        $("#button-year").append('&nbsp;<span class="caret"></span>');
        $("#button-month").text(moment(currentMonth+1, 'MM').format('MMMM'));
        $("#button-month").append('&nbsp;<span class="caret"></span>');
        $("#button-year").val(currentYear);
        $("#button-month").val(moment(currentMonth+1, 'MM').format('MMMM'));

        for (var i = 1; i <= 3; i++) {
            $("#dropdown-year").append(
                $('<li><a data-val='+currentYear+' href="#">'+currentYear+'</a></li>')
            );
            currentYear--;
        }
        $("#dropdown-year li a").click(function(){
            $("#dropdown-month").html("");
            $("#button-year").text($(this).text());
            $("#button-year").append('&nbsp;<span class="caret"></span>');
            $("#button-year").val($(this).text());
            var selectedYear = $("#button-year").val();
            if (selectedYear != fixYear) {
                allMonths = true;
                var monthList = getmonthList(currentMonth, allMonths);
                prepareMonthDropDown(monthList)
                registerMonthEvent();

            } else {
                allMonths = false;
                var monthList = getmonthList(currentMonth, allMonths);
                prepareMonthDropDown(monthList);
                registerMonthEvent();
            }
        });

        //set upto current month
        var monthList = getmonthList(currentMonth, allMonths);
        prepareMonthDropDown(monthList);
        registerMonthEvent();
    };

    $("#button-dir").click(function () {
            var direction = $("#button-dir").val();
            if (direction == 'nb') {
                $("#operatordd").hide();
                $("#button-operator").text("All Operator");
            }
    });

    getGadgetLocation(function (gadget_Location) {
        gadgetLocation = gadget_Location;
        init();
        initdatatable();
        getLoggedInUser();
        createYearSelectBox();
        loadOperator();
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

                        loadSP(operatorNames);
                        $("#dropdown-operator li a").click(function () {
                            $("#button-operator").text($(this).text());
                            $("#button-operator").append('&nbsp;<span class="caret"></span>');
                            $("#button-operator").val($(this).text());
                            //operatorNames = $(this).data('val');
                            if ($(this).data('val') == 'all'){
                                loadSP(operatorNames);
                            } else {
                                loadSP($(this).data('val'));
                            }
                            operatorSelected = true;
                        });
                    }
                });
            }
        }

        function loadSP (clickedOperator) {
            getLoggedInUser();
            conf[PROVIDER_CONF][TABLE_NAME] = STREAMS.API_SUMMERY;
            conf[PROVIDER_CONF][PROVIDER_NAME] = TYPE.OPERATOR;

            conf.operatorName = clickedOperator;
            selectedOperator = conf.operatorName;
            serviceProviderId =0;

            if (!loggedInUser.isServiceProvider) {

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

                        $("#dropdown-sp").html(spItems);

                        $("#button-sp").val('<li><a data-val="0" href="#">All Service provider</a></li>');

                        $("#dropdown-sp li a").click(function(){
                            getLoggedInUser();
                            $("#button-sp").text($(this).text());
                            $("#button-sp").append('&nbsp;<span class="caret"></span>');
                            $("#button-sp").val($(this).text());
                            serviceProviderId = $(this).data('val');

                            if(serviceProviderId != "0"){
                                conf.serviceProvider = serviceProviderId;
                            } else {
                                conf.serviceProvider = spIds;
                            }
                        });
                    }
                });
            }
        }

        $("#button-type").val("Billing");

    });
    $("#dropdown-direction li a").click(function () {
        if ($(this).data('val') == 'nb') {
            $("#operatordd").hide();
        } else {
            $("#operatordd").show();
        }
        $("#button-dir").text($(this).text());
        $("#button-dir").append('&nbsp;<span class="caret"></span>');
        $("#button-dir").val($(this).data('val'));
    });

    function registerMonthEvent () {
        $("#dropdown-month li a").click(function () {
            $("#button-month").text($(this).text());
            $("#button-month").append('&nbsp;<span class="caret"></span>');
            $("#button-month").val($(this).data('val'));
        });
    }

    function prepareMonthDropDown (monthList) {
        monthList.forEach(function (row) {
            $("#dropdown-month").append(
                $('<li><a data-val='+row+' href="#">'+row+'</a></li>')
            );
        });
    }
});


function downloadFile(index, type) {
    getGadgetLocation(function (gadget_Location) {
        gadgetLocation = gadget_Location;
        location.href = gadgetLocation + '/gadget-controller.jag?action=get&index=' + index +'&type='+ type;

    });
}

function removeFile(index, type) {
    getGadgetLocation(function(gadget_Location) {
        gadgetLocation = gadget_Location;
        $.ajax({
            url: gadgetLocation + '/gadget-controller.jag?action=remove&index=' + index+'&type='+ type,
            method: METHOD.POST,
            contentType: CONTENT_TYPE,
            async: false,
            success: function(data) {
                if(type == 'csv') {
                    $("#list-summery-report").click();
                }
                else if (type == 'csv-error'){
                    $("#list-error-report").click();
                } else {
                    $("#list-the-bill").click();
                }
            }
        });
    });
}

function getmonthList (month, allList) {
    var monthList = ['January','February','March','April','May','June','July','August','September','October','November','December'];
    if (allList) {
        return monthList;
    } else {
        var monthloadlist = monthList.slice(0, month+1);
        return monthloadlist;
    }
}