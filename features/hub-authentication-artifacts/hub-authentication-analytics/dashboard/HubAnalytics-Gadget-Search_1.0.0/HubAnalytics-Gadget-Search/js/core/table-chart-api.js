/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
        var chartConf = require(CHART_LOCATION + '/table-chart/config.json').config;
        /*
         dynamic logic goes here
         */

        var columns = [];

        columns.push("None");
        columns.push("All");
        for(var i=0; i < schema.length; i++) {
            columns.push(schema[i]["fieldName"]);
        }

        for(var i=0; i < chartConf.length; i++) {
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

    };


    /**
     * return the gadget content
     * @param chartConfig
     * @param schema
     * @param data
     */
    var no;
    var table;
    draw = function(placeholder, chartConfig, _schema, data) {

        _schema.push({fieldName:"jsonContent", fieldType:"string"});
        _schema.push({fieldName:"no", fieldType:"string"});

        var schema = toVizGrammarSchema(_schema);
        var grid = chartConfig.grid;
        var columns = [];

        for(var i = 0; i < data.length; i++ ) {
            no = i + 1;
            data[i].no = no;

            try {
            var json =  data[i].jsonBody.replace(/\\n/g, "")
                                      .replace(/\\'/g, "\\'")
                                      .replace(/\\"/g, '\\"')
                                      .replace(/\\&/g, "\\&")
                                      .replace(/\\r/g, "\\r")
                                      .replace(/\\t/g, "\\t")
                                      .replace(/\\b/g, "\\b")
                                       .replace("%", "")
                                      .replace(/\\f/g, "\\f");

            data[i].jsonContent = JsonHuman.format(JSON.parse(json)).outerHTML;
        }
        catch (e) {
            data[i].jsonContent = data[i].jsonBody;
        }
        }

        for(var i=0; i < _schema.length; i++) {
            columns.push(_schema[i]["fieldName"]);
        }

        chartConfig.columns = columns;

        var view = {
            id: "chart-0",
            schema: schema,
            chartConfig: buildChartConfig(chartConfig),
            data: function() {
                if(data) {
                    var result = [];
                    data.forEach(function(item) {
                        var row = [];
                        schema[0].metadata.names.forEach(function(name) {
                            row.push(item[name]);
                        });
                        result.push(row);
                    });
                    wso2gadgets.onDataReady(result);
                }
            }

        };

        try {
            wso2gadgets.init(placeholder, view);
            var view = wso2gadgets.load("chart-0");

            if (grid) {
                $(document).ready(function() {
                    table = $('#table').DataTable({
                        "filter": true,
                        "paging":true,
                        "pagingType": "full_numbers",
                        "pageLength": 5,
                        scrollCollapse: true,
                        scrollY:'70vh',
                        "dom": '<"dataTablesTop"' +
                        'f' +
                        '<"dataTables_toolbar">' +
                        '>' +
                        'rt' +
                        '<"dataTablesBottom"' +
                        'lip' +
                        '>',
                        "info":true
                    });
                    $('#table').on('page.dt', function (e, settings) {
                        update(settings);
                    });
                });
            }

        } catch (e) {
            console.error(e);
        }

    };

    update = function(data) {
        var displayStart = data._iDisplayStart;
        var displayLength = data._iDisplayLength;
        var records = data.aiDisplay.length;
        if(displayStart != 0) {
            var data = getProviderData(displayStart, displayLength, records, true);
            for(var i = 0; i < data.length; i++ ) {
                no = no + 1;
                data[i].no = no;
                try {
                    var json =  data[i].jsonBody.replace(/\\n/g, "")
                        .replace(/\\'/g, "\\'")
                        .replace(/\\"/g, '\\"')
                        .replace(/\\&/g, "\\&")
                        .replace(/\\r/g, "\\r")
                        .replace(/\\t/g, "\\t")
                        .replace(/\\b/g, "\\b")
                        .replace("%", "")
                        .replace(/\\f/g, "\\f");

                    data[i].jsonContent = JsonHuman.format(JSON.parse(json)).outerHTML;
                }
                catch (e) {
                    data[i].jsonContent = data[i].jsonBody;
                }
            }

            var recordsArray = [];
            for(var j = 0; j < data.length; j++) {
                var temp = [];
                temp.push(data[j].no);
                temp.push(data[j].responseTime);
                temp.push(data[j].api);
                temp.push(data[j].jsonContent);
                recordsArray.push(temp);
            }
            table.rows.add(recordsArray).draw(false);
        }

        return;
    };

    buildChartConfig = function (_chartConfig) {
        var conf = {};
        conf.charts = [];
        conf.charts[0] = {
            type : "table",
            key : "no",
            grid: "off"
        };
        conf.maxLength = _chartConfig.maxLength;

        if (_chartConfig.color == "All") {
            conf.charts[0].color = "*";
        } else if (_chartConfig.color != "None") {
            conf.charts[0].color = _chartConfig.color;
        }
        conf.charts[0].columns = ["no", "responseTime", "api", "jsonContent"];
        return conf;
    };


}());
