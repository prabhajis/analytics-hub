package org.wso2telco.analytics.restService;

import javax.ws.rs.core.Response;

public interface RateService {

    public Response getRateCard (String rateName);
    public Response insertRateCard ();
}