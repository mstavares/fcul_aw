package pt.ulisboa.ciencias.di.aw1718.group06.ws.services;


import org.springframework.web.bind.annotation.ResponseBody;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/feedback")
public class FeedbackService {


    @POST
    @Path("/implicit/increment")
    //@ResponseBody
    @Produces(MediaType.APPLICATION_XML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public String incrementImplicit(@FormParam("disease") String disease, @FormParam("article") String article) {
        return null;
    }

    @POST
    @Path("/explicit/decrement")
    //@ResponseBody
    @Produces(MediaType.APPLICATION_XML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public String decrementExplicit(@FormParam("disease") String disease, @FormParam("article") String article) {
        return null;
    }

}
