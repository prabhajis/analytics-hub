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
var getConfig, validate, isProviderRequired, draw, update;

(function() {

    var CHART_LOCATION = '/extensions/chart-templates/';

    /**
     * return the config to be populated in the chart configuration UI
     * @param schema
     */
    getConfig = function(schema) {
        var chartConf = require(CHART_LOCATION + '/line-chart/config.json').config;
        /*
         dynamic logic goes here
         */

        var columns = [];

        columns.push("None");
        for (var i = 0; i < schema.length; i++) {
            columns.push(schema[i]["fieldName"]);
        }

        for (var i = 0; i < chartConf.length; i++) {
            if (chartConf[i]["fieldName"] == "color") {
                chartConf[i]["valueSet"] = columns;
                break;
            }
        }

        return chartConf;
    };

    /**
     * validate the user inout for the chart configuration
     * @param chartConfig
     */
    validate = function(chartConfig) {
        return true;
    };

    /**
     * TO be used when provider configuration steps need to be skipped
     */
    isProviderRequired = function() {

    }


    /**
     * return the gadget content
     * @param chartConfig
     * @param schema
     * @param data
     */

    //TODO: data tika awahama mehtanin view tika dala pie chart 3 ma load karanna
    draw = function(placeholder, chartConfig, _schema, data) {
 /*data = [
     {
         "year":"2017",
         "month":"6",
         "direction":"sb",
         "api":"payment",
         "apiID":"payment_vv1",
         "applicationName":"demo-sp1-app1",
         "applicationId":"36",
         "serviceProvider":"demo-sp1@carbon.super",
         "serviceProviderId":"demo-sp1@carbon.super",
         "operatorName":"OPERATOR1",
         "operatorId":"1",
         "operation":"Charge",
         "category":"",
         "subcategory":"",
         "totalCount":"18",
         "totalAmount":"40.0",
         "totalOpCommision":"37.5",
         "totalSpCommision":"600.0",
         "totalHbCommision":"112.5",
         "totalTaxAmount":"600.0",
         "_timestamp":"2017-06-05 20:40:45 IST"
     },
 ]*/
        console.log("***** " +JSON.stringify(chartConfig));
        console.log("^^^^^^^^^^^^^^^^^^^^  " + JSON.stringify(data));
        _schema = updateUserPrefXYTypes(_schema, chartConfig);
        var schema = toVizGrammarSchema(_schema);

     /*   var type = $("#button-type").val().toLowerCase().trim();

        if (type == "api traffic") {
            chartConfig.color = "api";
            chartConfig.count = "totalCount";
            /!* } else if (type == "operator traffic") {
             chartConfig.color = "operatorName";
             chartConfig.count = "totalCount";
             *!/
        } else if (type == "error traffic") {
            chartConfig.color = "errorMessageId";
            chartConfig.count = "totalFailureCount";
        }*/

        /*var view = {
            id: "chart-0",
            schema: schema,
            chartConfig: buildChartConfig(chartConfig),
            data: function() {
                if (lineChartGroupData) {
                    var result = [];
                    lineChartGroupData.forEach(function(item) {
                        var row = [];
                        schema[0].metadata.names.forEach(function(name) {
                            row.push(item[name]);
                        });
                        result.push(row);
                    });
                    wso2gadgets.onDataReady(result.sort(compare));
                }
            }
        };*/

        var groupDataAPI = [];
        var groupDataSP = [];
        var groupDataMNO = [];
        //var lineChartGroupData = [];
        var arcConfig = buildChart2Config(chartConfig);
        console.log(">>>>>>>>>>>>>>>> " + JSON.stringify(arcConfig));
        data.forEach(function(row) {
            var notAvailable = true;
            var groupRowAPI = JSON.parse(JSON.stringify(row));
            var groupRowSP = JSON.parse(JSON.stringify(row));
            var groupRowMNO = JSON.parse(JSON.stringify(row));

            var notAvailableForLineChart = true;
            //var lineCharGroupRow = JSON.parse(JSON.stringify(row));

         /*   groupData.forEach(function(row2) {
                if (groupRow[arcConfig.color] == row2[arcConfig.color]) {
                    notAvailable = false;
                    if (lineCharGroupRow['eventTimeStamp'] == row2['eventTimeStamp']) {
                        notAvailableForLineChart = false;
                    }
                }

            });*/

            if (notAvailable) {

                groupRowAPI[arcConfig.x] = 0;
                groupRowSP[arcConfig.x] = 0;
                groupRowMNO[arcConfig.x] = 0;

                data.forEach(function(row2) {
                    if (groupRowAPI[arcConfig.colorapi] == row2[arcConfig.colorapi]) {
                        groupRowAPI[arcConfig.x] += row2[arcConfig.x];
                        console.log("...............................111 ++++++  " + groupRowAPI[arcConfig.x]);
                    }

                    if (groupRowSP[arcConfig.colorsp] == row2[arcConfig.colorsp]) {
                        groupRowSP[arcConfig.x] += row2[arcConfig.x];
                        console.log("...............................222 ++++++  " + groupRowSP[arcConfig.x]);
                    }

                    if (groupRowMNO[arcConfig.colormno] == row2[arcConfig.colormno]) {
                        groupRowMNO[arcConfig.x] += row2[arcConfig.x];
                        console.log("...............................333 ++++++  " + groupRowMNO[arcConfig.x]);
                    }
                });

                groupDataAPI.push(groupRowAPI);
                groupDataSP.push(groupRowSP);
                groupDataMNO.push(groupRowMNO);
            }

            /*if (notAvailableForLineChart) {
                lineCharGroupRow[arcConfig.x] = 0;
                data.forEach(function(row3) {
                    if ((lineCharGroupRow[arcConfig.colorapi] == row3[arcConfig.colorapi]) && (lineCharGroupRow['eventTimeStamp'] == row3['eventTimeStamp'])) {
                        lineCharGroupRow[arcConfig.x] += row3[arcConfig.x];
                    }
                });
                lineChartGroupData.push(lineCharGroupRow);
            } */
        });

        var viewSP = {
            id: "chart-0",
            schema: schema,
            chartConfig: arcConfig,
            data: function() {
                if (groupDataSP) {
                    var result = [];
                    groupDataSP.forEach(function(item) {
                        var row = [];
                        schema[0].metadata.names.forEach(function(name) {
                            row.push(item[name]);
                        });
                        result.push(row);
                    });
                    wso2gadgets.onDataReady(result.sort(compare));
                }
            }

        };

        var viewAPI = {
            id: "chart-1",
            schema: schema,
            chartConfig: arcConfig,
            data: function() {
                if (groupDataAPI) {
                    var result = [];
                    groupDataAPI.forEach(function(item) {
                        var row = [];
                        schema[0].metadata.names.forEach(function(name) {
                            row.push(item[name]);
                        });
                        result.push(row);
                    });
                    wso2gadgets.onDataReady(result.sort(compare));
                }
            }

        };

        var viewMNO = {
            id: "chart-2",
            schema: schema,
            chartConfig: arcConfig,
            data: function() {
                if (groupDataMNO) {
                    var result = [];
                    groupDataMNO.forEach(function(item) {
                        var row = [];
                        schema[0].metadata.names.forEach(function(name) {
                            row.push(item[name]);
                        });
                        result.push(row);
                    });
                    wso2gadgets.onDataReady(result.sort(compare));
                }
            }

        };

        try {
            /*wso2gadgets.init(placeholder, view);
            var view = wso2gadgets.load("chart-0");*/

            wso2gadgets.init(placeholder, viewSP);
             var view = wso2gadgets.load("chart-0");


            wso2gadgets.init("#canvas2", viewAPI);
            var viewAPI = wso2gadgets.load("chart-1");

            wso2gadgets.init("#canvas3", viewMNO);
            var viewAPI = wso2gadgets.load("chart-2");

        } catch (e) {
            console.error(e);
        }

    };

    compare = function(a, b) {
        return a[9] - b[9];

    };

    /**
     *
     * @param data
     */
    update = function(data) {
        wso2gadgets.onDataReady(data, "append");
    };

    /*buildChartConfig = function(_chartConfig) {
        var conf = {};
        conf.x = "eventTimeStamp";
        conf.height = 400;
        conf.color = _chartConfig.color;
        conf.width = 600;
        conf.xType = _chartConfig.xType;
        conf.padding = { "top": 5, "left": 70, "bottom": 40, "right": 20 };
        conf.yType = "linear";
        conf.maxLength = _chartConfig.maxLength;
        conf.charts = [];
        conf.charts[0] = {
            type: "line",
            y: _chartConfig.count,
            legend: false,
            zero: true
        };
        return conf;
    };
*/

    buildChart2Config = function(_chartConfig) {
        var conf = {};
        conf.x = _chartConfig.amount;
        conf.colorapi = _chartConfig.colorAPI;  //add additonals
        conf.colorsp = _chartConfig.colorSP;
        conf.colormno = _chartConfig.colorMNO;
        conf.height = 400;
        conf.width = 450;
        conf.xType = _chartConfig.xType;
        //conf.yType = _chartConfig.yType;
        conf.padding = { "top": 0, "left": 0, "bottom": 40, "right": 50 };
        conf.maxLength = _chartConfig.maxLength;
        conf.charts = [];
        conf.charts[0] = {
            type: "arc",
            mode: "pie"
        };

        return conf;
    };


}());