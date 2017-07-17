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
var getGadgetLocation = function (callback) {
    var gadgetLocation = "/portal/store/carbon.super/fs/gadget/HubAnalytics-Gadget-BillingReport";
	var PATH_SEPERATOR = "/";
    if (gadgetLocation.search("store") != -1) {
        wso2.gadgets.identity.getTenantDomain(function (tenantDomain) {
            var gadgetPath = gadgetLocation.split(PATH_SEPERATOR);
            var modifiedPath = '';
            for (var i = 1; i < gadgetPath.length; i++) {
                if (i === 3) {
                    modifiedPath = modifiedPath.concat(PATH_SEPERATOR, tenantDomain);
                } else {
                    modifiedPath = modifiedPath.concat(PATH_SEPERATOR, gadgetPath[i])
                }
            }
            callback(modifiedPath);
        });
    } else {
        callback(gadgetLocation);
    }
}


function mediaScreenSize(){
    var windowWidth = $(window).width();
    if(windowWidth < 767){
        $('body').attr('media-screen', 'xs');
    }
    if((windowWidth > 768) && (windowWidth < 991)){
        $('body').attr('media-screen', 'sm');
    }
    if((windowWidth > 992) && (windowWidth < 1199)){
        $('body').attr('media-screen', 'md');
    }
    if(windowWidth > 1200){
        $('body').attr('media-screen', 'lg');
    }
}

// Light/Dark Theme Switcher
$(document).ready(function() {

    mediaScreenSize();

});

$(window).resize(function(){
    mediaScreenSize();
});