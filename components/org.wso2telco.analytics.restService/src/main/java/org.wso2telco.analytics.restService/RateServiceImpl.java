package org.wso2telco.analytics.restService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/rate")
@Produces(MediaType.APPLICATION_JSON)
public class RateServiceImpl implements RateService{

    @Override
    @GET
    @Path("/{ratecard}")
    public Response getRateCard(@PathParam("ratecard") String rateName) {
        return null;
    }

    @Override
    @POST
    public Response insertRateCard() {
        return Response.status(201).entity("Rate Card Insertred to System").build();
    }
}