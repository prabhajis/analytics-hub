var count = 1;
var draw;

$(document).ready(function() {
    removeField = function(keyval) {
        if(count > 1) {
            $("#keydiv"+keyval).remove();
            count = count - 1;
        }
    };

    $("#addbtn").click(function() {
        if (count > 4) {
            $("#popupcontent p").html('Only 5 keywords are allowed.');
            $('#notifyModal').modal('show');
            return;
        }

        var countStr = count.toString();
        var newSearchField =   '<div id="keydiv'+count+'" style="height:2.5em;" class="form-group">' +
            '<div class="col-11">' +
            '<input type="text" class="form-control" id="keyval'+countStr+'" placeholder="Enter keyword '+(count+1) +'">' +
            '<div class="col-1 remove-field" onClick="removeField('+countStr+')" >' +
            '<a><span class="glyphicon glyphicon-minus minus-button"></span></a>' +
            '</div>' +
            '</div>' +
            '</div>';
        var objNewDiv = document.createElement('div');
        objNewDiv.setAttribute('id', 'key' + count);
		objNewDiv.setAttribute('class', 'col-2');
        objNewDiv.innerHTML = newSearchField;
        document.getElementById('searchFieldList').appendChild(objNewDiv);
        count = count + 1;
    });

    draw = function(placeholder, chartConfig, _schema, data) {
            _schema.push({fieldName:"jsonContent", fieldType:"string"});
            _schema.push({fieldName:"no", fieldType:"string"});

            var schema = toVizGrammarSchema(_schema);
            var grid = chartConfig.grid;
            var columns = [];
            var no = 0;

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
                var table;
                if (grid) {
                        table = $("#table").DataTable({
                            "filter": true,
                            "paging":true,
                            "pagingType": "simple_numbers",
                            "pageLength": 10,
                            "lengthChange": true,
                            "dom": '<"dataTablesTop"' +
                            '<"dataTables_toolbar">' +
                            '>' +
                            'rt' +
                            '<"dataTablesBottom"' +
                            'ipf' +
                            '>',
                            "info":true,
                            "columnDefs": [
                                { "type": "num-html", targets: 0 }
                            ],
							language: {
                                search: "_INPUT_",
                                searchPlaceholder: "Filter result"
                            }
                        });
                        table.clear();
                        //server side pagination has been disabled for the moment. Use this event to capture the click on next page button.
                        // $('#table').on('page.dt', function (e, settings) {
                        //      alert(JSON.stringify(settings));
                        //     update(settings);
                        // });

                        for(var i = 0; i < data.length; i++ ) {
                            data[i].no = i+1;
                            var responseTime_unix = data[i].responseTime;
                            var responseTime_unix_sec = parseInt(responseTime_unix/1000);
                            var responseTime_dateTime =  moment.unix(responseTime_unix_sec).format("MM-DD-YYYY HH:mm:ss");
                            data[i].responseTime = responseTime_dateTime;
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
                        for (var j = 0; j < data.length; j++) {
                            var temp = [];
                            temp.push(parseInt(data[j].no));
                            temp.push(data[j].responseTime);
                            temp.push(data[j].api);
                            temp.push(data[j].jsonContent);
                            recordsArray.push(temp);
                        }
                        table.rows.add(recordsArray).draw();
                }
            } catch (e) {
                console.error(e);
            }
        };

        // update = function (data) {
        //         for (var i = 0; i < data.length; i++) {
        //             no = no + 1;
        //             data[i].no = no;
        //             try {
        //                 var json = data[i].jsonBody.replace(/\\n/g, "")
        //                     .replace(/\\'/g, "\\'")
        //                     .replace(/\\"/g, '\\"')
        //                     .replace(/\\&/g, "\\&")
        //                     .replace(/\\r/g, "\\r")
        //                     .replace(/\\t/g, "\\t")
        //                     .replace(/\\b/g, "\\b")
        //                     .replace("%", "")
        //                     .replace(/\\f/g, "\\f");
        //
        //                 data[i].jsonContent = JsonHuman.format(JSON.parse(json)).outerHTML;
        //             }
        //             catch (e) {
        //                 data[i].jsonContent = data[i].jsonBody;
        //             }
        //         }
        //
        //         var recordsArray = [];
        //         for (var j = 0; j < data.length; j++) {
        //             var temp = [];
        //             temp.push(data[j].no);
        //             temp.push(data[j].responseTime);
        //             temp.push(data[j].api);
        //             temp.push(data[j].jsonContent);
        //             recordsArray.push(temp);
        //         }
        //         table.rows.add(recordsArray).draw();
        // };


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
});
