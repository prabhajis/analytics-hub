package org.wso2telco.analytics.restService;

import org.wso2telco.analytics.pricing.service.RateCardService;

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
        RateCardService rateCardService = new RateCardService();
        try {
            rateCardService.getRateByName(rateName)
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response.status(200).entity(obj).build();
    }

    @Override
    @POST
    public Response insertRateCard() {
        return Response.status(201).entity("Rate Card Insertred to System").build();
    }
}