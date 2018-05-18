package pt.ulisboa.ciencias.di.aw1718.group06.ws;

import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.DiseaseCatalog;

import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.dto.MentionedDiseasesDAO;

@CrossOrigin
@RestController
@RequestMapping("/pubmed")
public class PubmedService {
    
	private static final Logger LOG = LoggerFactory.getLogger(FeedbackService.class);

    private DiseaseCatalog diseaseCatalog;

    @Autowired
    public PubmedService(DiseaseCatalog diseaseCatalog) {
        this.diseaseCatalog = diseaseCatalog;
    }
    
    @RequestMapping(value="/get_mentioned_diseases/{pubmedId}", method=RequestMethod.GET, produces="application/json")
    public List<MentionedDiseasesDAO> getMentionedDiseases(@PathVariable int pubmedId) throws SQLException{
    	return diseaseCatalog.getMentionedDiseases(pubmedId);
    }
}