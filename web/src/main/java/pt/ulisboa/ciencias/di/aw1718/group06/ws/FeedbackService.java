package pt.ulisboa.ciencias.di.aw1718.group06.ws;


import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ResponseBody;
import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.DiseaseCatalog;
import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.dto.FullFeedback;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import java.sql.SQLException;

@Path("/feedback")
@Api(value = "feedback", description = "This receives user's feedback")
public class FeedbackService {

    private static final Logger LOG = LoggerFactory.getLogger(FeedbackService.class);
    private static final String CONFIG_FILE_NAME = "config.properties";
    private DiseaseCatalog diseaseCatalog;

    public FeedbackService() {
        try {
            diseaseCatalog = new DiseaseCatalog(CONFIG_FILE_NAME);
        } catch (SQLException e) {
            LOG.error("Error while connecting to database: " + e.getErrorCode(), e);
        }
    }

    @POST
    @Path("/pubmed")
    @ResponseBody
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Boolean updatePubMedFeedback(@FormParam("diseaseId") int diseaseId, @FormParam("pubMedId") int pubMedId, @FormParam("operation") int operation) throws SQLException {
        return diseaseCatalog.updatePubMedFeedback(diseaseId, pubMedId, FullFeedback.Operations.values()[operation]);
    }

    @POST
    @Path("/tweet")
    @ResponseBody
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Boolean updateTweetFeedback(@FormParam("diseaseId") int diseaseId, @FormParam("tweetId") int tweetId, @FormParam("operation") int operation) throws SQLException {
        return diseaseCatalog.updateTweetFeedback(diseaseId, tweetId, FullFeedback.Operations.values()[operation]);
    }

    @POST
    @Path("/image")
    @ResponseBody
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Boolean updateImageFeedback(@FormParam("diseaseId") int diseaseId, @FormParam("imageId") int imageId, @FormParam("operation") int operation) throws SQLException {
        return diseaseCatalog.updateImageFeedback(diseaseId, imageId, FullFeedback.Operations.values()[operation]);
    }

}
