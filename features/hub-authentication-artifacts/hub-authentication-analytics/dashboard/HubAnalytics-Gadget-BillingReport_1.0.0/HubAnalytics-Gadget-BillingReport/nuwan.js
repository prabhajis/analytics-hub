var getUserIdList;
(function() {


    getUserIdList = function(providerConfig, propertyName) {



        var killBillAIDList=[];
        for (var i in providerConfig){

//        var vf="killbillAID";
            log.info(providerConfig[i][propertyName] );

            killBillAIDList.push(providerConfig[i][propertyName] );

        }


        return killBillAIDList;
    };

}());