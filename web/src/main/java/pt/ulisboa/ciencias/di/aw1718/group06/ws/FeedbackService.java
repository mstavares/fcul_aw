package pt.ulisboa.ciencias.di.aw1718.group06.ws;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pt.ulisboa.ciencias.di.aw1718.group06.crawler.index.Index;
import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.DiseaseCatalog;
import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.dto.FullFeedback;

import java.sql.SQLException;

@RestController
@RequestMapping("/feedback")
public class FeedbackService {

    private static final Logger LOG = LoggerFactory.getLogger(FeedbackService.class);

    private DiseaseCatalog diseaseCatalog;
    private Index index;

    @Autowired
    public FeedbackService(DiseaseCatalog diseaseCatalog, Index index) {
        this.diseaseCatalog = diseaseCatalog;
        this.index = index;
    }

    @RequestMapping(value="/pubmed", method=RequestMethod.POST, produces="application/json")
    public boolean updatePubMedFeedback(@RequestParam int diseaseId, @RequestParam int pubmedId, @RequestParam int operation) throws SQLException {
        boolean updated = diseaseCatalog.updatePubMedFeedback(diseaseId, pubmedId, FullFeedback.Operations.values()[operation]);
        if (updated) {
            this.index.updateRankOfPubMedsForDisease(diseaseId);
        }
        return updated;
    }

    @RequestMapping(value="/tweet", method=RequestMethod.POST, produces="application/json")
    public Boolean updateTweetFeedback(@RequestParam int diseaseId, @RequestParam int tweetId, @RequestParam int operation) throws SQLException {
        return diseaseCatalog.updateTweetFeedback(diseaseId, tweetId, FullFeedback.Operations.values()[operation]);
    }

    @RequestMapping(value="/image", method=RequestMethod.POST, produces="application/json")
    public Boolean updateImageFeedback(@RequestParam int diseaseId, @RequestParam int imageId, @RequestParam int operation) throws SQLException {
        return diseaseCatalog.updateImageFeedback(diseaseId, imageId, FullFeedback.Operations.values()[operation]);
    }

}
