package pt.ulisboa.ciencias.di.aw1718.group06.ws;

import com.google.common.collect.ImmutableMap;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import pt.ulisboa.ciencias.di.aw1718.group06.crawler.index.CompoundRanker;
import pt.ulisboa.ciencias.di.aw1718.group06.crawler.index.Index;
import pt.ulisboa.ciencias.di.aw1718.group06.crawler.index.IndexRank;
import pt.ulisboa.ciencias.di.aw1718.group06.crawler.index.RankType;
import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.DiseaseCatalog;
import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.dto.*;
import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.models.Disease;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/disease")
public class DiseaseService {

    private static final Logger LOG = LoggerFactory.getLogger(DiseaseService.class);
    private static final String CONFIG_FILE_NAME = "config.properties";
    private DiseaseCatalog diseaseCatalog;
    private Index index;


    public DiseaseService() {
        try {
            diseaseCatalog = new DiseaseCatalog(CONFIG_FILE_NAME);

            CompoundRanker ranker = new CompoundRanker(ImmutableMap.of(
                RankType.TF_IDF_RANK, 0.3,
                RankType.DATE_RANK, 0.1,
                RankType.EXPLICIT_FEEDBACK_RANK, 0.4,
                RankType.IMPLICIT_FEEDBACK_RANK, 0.2
            ));
            this.index = new Index(ranker, diseaseCatalog);
            this.index.build();

        } catch (SQLException e) {
            LOG.error("Error while connecting to database: " + e.getErrorCode(), e);
        }
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
    public FullDisease getFullDisease(@PathVariable int id) throws SQLException {
        return buildFullDisease(id);
    }


    private FullDisease buildFullDisease(int diseaseId) throws SQLException {
        Disease disease = diseaseCatalog.getDisease(String.valueOf(diseaseId));
        List<Pair<Integer, IndexRank>> rankedPubMeds = index.getArticlesFor(diseaseId);
        List<FullPubMed> pubmeds = getFullPubmeds(rankedPubMeds, diseaseId);
        List<FullTweet> tweets = diseaseCatalog.getOrderedTweets(diseaseId);
        List<FullImage> images = diseaseCatalog.getOrderedImages(diseaseId);

        // get top n PubMeds ranked by the index
        // this.index.getTopPubMeds(n, diseaseId) : List<Integer> pubMedIds // sorted
        // select * from pubmeds where pubmedid in [pubMedIds]  // check if returns in the requested order

        // same for tweets and images

        return new FullDisease(disease.getId(), disease.getName(), disease.getDescription(), disease.getDerivedFrom(),
                disease.getField(), disease.getDead(), pubmeds, images, tweets);
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

}
