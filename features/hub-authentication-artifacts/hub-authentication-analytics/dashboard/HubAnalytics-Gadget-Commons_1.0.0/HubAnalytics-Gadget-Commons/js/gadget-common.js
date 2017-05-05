/*
 * constants
 * */
var CHART_CONF = 'chart-conf';
var PROVIDER_CONF = 'provider-conf';
var REFRESH_INTERVAL = 'refreshInterval';
var TABLE_NAME = "tableName";
var PROVIDER_NAME = "provider-name";
var CONTENT_TYPE = "application/json";
var TYPE = { OPERATOR:"operator", SP:"sp", APP:"app" }; //TODO:CHECK THIS
var STREAMS = { RESPONSE_TIME_SUMMERY_DAY:"ORG_WSO2TELCO_ANALYTICS_HUB_STREAM_REPONSETIME_SUMMARY_PER_DAY",
    RESPONSE_TIME_SUMMERY:"ORG_WSO2TELCO_ANALYTICS_HUB_STREAM_REPONSETIME_SUMMARY_PER_",
    OPERATOR_SUMMERY:"ORG_WSO2TELCO_ANALYTICS_HUB_STREAM_OPERATOR_SUMMARY",
    API_SUMMERY:"ORG_WSO2TELCO_ANALYTICS_HUB_STREAM_API_SUMMARY"
};
var METHOD = {GET:"GET", POST:"POST"};

/*
* common functionality for all gadgets
* */


//TODO:check the usage of conf.operatorName
var hideDropDown = function (loggedInUser) {
    if (!(loggedInUser.isAdmin) && loggedInUser.isOperatorAdmin) {
        $("#operatordd").hide();

        //conf.operatorName = operatorName;
    } else if (!(loggedInUser.isAdmin) && loggedInUser.isServiceProvider) {
        $("#serviceProviderdd").hide();
    }
}

var dateStart = function () {
    return moment(moment($("#reportrange").text().split("-")[0]).format("MMMM D, YYYY hh:mm A")).valueOf();
}

var dateEnd = function () {
    return moment(moment($("#reportrange").text().split("-")[1]).format("MMMM D, YYYY hh:mm A")).valueOf();
}