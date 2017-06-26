package org.wso2telco.analytics.restService;

import com.google.gson.Gson;
import org.wso2telco.analytics.pricing.service.ChargeRate;
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
        String jsonRespone = null;

        try {
            ChargeRate chargeRate = (ChargeRate) rateCardService.getRateByName(rateName);
            Gson gson = new Gson();
            jsonRespone = gson.toJson(chargeRate);

        } catch (Exception e) {
            //TODO:handle exception with better formant
            e.printStackTrace();
        }
        return Response.status(200).entity(jsonRespone).build();   //TODO:check othter options availble
    }

    @Override
    @POST
    public Response insertRateCard() {
        return Response.status(201).entity("Rate Card Insertred to System").build();
    }
}