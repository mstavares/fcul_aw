package pt.ulisboa.ciencias.di.aw1718.group06.ws;

import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pt.ulisboa.ciencias.di.aw1718.group06.crawler.index.Index;
import pt.ulisboa.ciencias.di.aw1718.group06.crawler.index.IndexRank;
import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.DiseaseCatalog;
import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.dto.*;
import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.models.Disease;

import javax.annotation.PostConstruct;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


@CrossOrigin
@RestController
@RequestMapping("/disease")
public class DiseaseService {

    private static final Logger LOG = LoggerFactory.getLogger(DiseaseService.class);

    private DiseaseCatalog diseaseCatalog;

    private Index index;

    @Autowired
    public DiseaseService(DiseaseCatalog diseaseCatalog, Index index) {
        this.diseaseCatalog = diseaseCatalog;
        this.index = index;
    }

    @PostConstruct
    public void init() throws SQLException {
        this.index.build();
    }

    @RequestMapping(value = "/get_statistics", method = RequestMethod.GET, produces = "application/json")
    public Statistic getStatistics() throws SQLException {
        return diseaseCatalog.getStatistic();
    }

    @RequestMapping(value="/get_all/{limit}", method=RequestMethod.GET, produces="application/json")
    public List<Disease> getAllDiseases(@PathVariable int limit) throws SQLException {
        return diseaseCatalog.getDiseases(limit);
    }

    @RequestMapping(value="/get_by_name_fragment/{fragment}", method=RequestMethod.GET, produces="application/json")
    public List<Disease> getDiseasesByNameFragment(@PathVariable String fragment) throws SQLException {
       return diseaseCatalog.getFragmentDiseases(fragment);
    }

    @RequestMapping(value="/get/{id}", method=RequestMethod.GET, produces="application/json")
    public FullDisease getFullDisease(@PathVariable int id) throws SQLException, IOException {
        return buildFullDisease(id);
    }


    private FullDisease buildFullDisease(int diseaseId) throws SQLException, IOException {
        Disease disease = diseaseCatalog.getDisease(String.valueOf(diseaseId));
        List<Pair<Integer, IndexRank>> rankedPubMeds = index.getArticlesFor(diseaseId);
        List<FullPubMed> pubmeds = getFullPubmeds(rankedPubMeds, diseaseId);
        List<FullTweet> tweets = diseaseCatalog.getOrderedTweets(diseaseId);
        List<FullImage> images = diseaseCatalog.getOrderedImages(diseaseId);
        List<Disease> diseases = diseaseCatalog.getTopRelatedDiseases(5, diseaseId);
        
        return new FullDisease(disease.getId(), disease.getDoid(), disease.getName(), disease.getDescription(), disease.getDerivedFrom(),
                disease.getField(), disease.getDead(), pubmeds, images, tweets, diseases);
    }
    
    @RequestMapping(value="/get_top_articles", method=RequestMethod.GET, produces="application/json")
    public List<FullPubMed> getTopNFullPubmeds(@RequestParam int lim, @RequestParam int diseaseId) throws SQLException{
    	List<FullPubMed> pubmeds = getFullPubmeds(index.getArticlesFor(diseaseId).subList(0, lim), diseaseId);   	
    	return pubmeds;
    }

	private List<FullPubMed> getFullPubmeds(List<Pair<Integer, IndexRank>> rankedPubMeds, int diseaseId) throws SQLException {
		List<FullPubMed> pubmeds = new ArrayList<>();
		for(Pair<Integer, IndexRank> p : rankedPubMeds) {
			int id = p.getKey();
			FullPubMed pub = diseaseCatalog.getFullPubmedByPubID(id, diseaseId);
			if(pub != null)
				pubmeds.add(pub);
		}	
		return pubmeds;
	}
	
	@RequestMapping(value="/get_top_related_diseases", method=RequestMethod.GET, produces="application/json")
	public List<Disease> getTopRelatedDiseases(@RequestParam int lim, @RequestParam int diseaseId) throws SQLException, IOException{
		return diseaseCatalog.getTopRelatedDiseases(lim, diseaseId);
	}

}
