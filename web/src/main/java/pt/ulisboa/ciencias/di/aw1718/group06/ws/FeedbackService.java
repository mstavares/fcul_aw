package pt.ulisboa.ciencias.di.aw1718.group06.ws;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.DiseaseCatalog;
import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.dto.FullFeedback;

import java.sql.SQLException;

@RestController
@RequestMapping("/feedback")
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

    @RequestMapping(value="/pubmed", method=RequestMethod.POST, produces= {"application/json", "application/xml"})
    public Boolean updatePubMedFeedback(@RequestParam int diseaseId, @RequestParam int pubmedId, @RequestParam int operation) throws SQLException {
        return diseaseCatalog.updatePubMedFeedback(diseaseId, pubmedId, FullFeedback.Operations.values()[operation]);
    }

    @RequestMapping(value="/tweet", method=RequestMethod.POST, produces= {"application/json", "application/xml"})
    public Boolean updateTweetFeedback(@RequestParam int diseaseId, @RequestParam int tweetId, @RequestParam int operation) throws SQLException {
        return diseaseCatalog.updateTweetFeedback(diseaseId, tweetId, FullFeedback.Operations.values()[operation]);
    }

    @RequestMapping(value="/image", method=RequestMethod.POST, produces= {"application/json", "application/xml"})
    public Boolean updateImageFeedback(@RequestParam int diseaseId, @RequestParam int imageId, @RequestParam int operation) throws SQLException {
        return diseaseCatalog.updateImageFeedback(diseaseId, imageId, FullFeedback.Operations.values()[operation]);
    }

}
