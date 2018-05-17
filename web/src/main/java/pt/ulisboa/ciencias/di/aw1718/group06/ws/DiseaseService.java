package pt.ulisboa.ciencias.di.aw1718.group06.ws;

import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.Api;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ulisboa.ciencias.di.aw1718.group06.crawler.index.CompoundRanker;
import pt.ulisboa.ciencias.di.aw1718.group06.crawler.index.Index;
import pt.ulisboa.ciencias.di.aw1718.group06.crawler.index.IndexRank;
import pt.ulisboa.ciencias.di.aw1718.group06.crawler.index.RankType;
import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.DiseaseCatalog;
import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.dto.*;
import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.models.Disease;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.sql.SQLException;
import java.util.List;


@Path("/disease")
@Api(value = "disease", description = "This WS provides information related with diseases")
public class DiseaseService {

    private static final Logger LOG = LoggerFactory.getLogger(DiseaseService.class);
    private static final String CONFIG_FILE_NAME = "config.properties";
    private DiseaseCatalog diseaseCatalog;


    public DiseaseService() {
        try {
            diseaseCatalog = new DiseaseCatalog(CONFIG_FILE_NAME);
        } catch (SQLException e) {
            LOG.error("Error while connecting to database: " + e.getErrorCode(), e);
        }
    }

    @GET
    @Path("/get_statistics")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Statistic getStatistics() throws SQLException {
        return diseaseCatalog.getStatistic();
    }

    @GET
    @Path("/get_all/{limit}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<Disease> getAllDiseases(@PathParam("limit") int limit) throws SQLException {
        return diseaseCatalog.getDiseases(limit);
    }

    @GET
    @Path("/get_by_name_fragment/{fragment}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<Disease> getDiseasesByNameFragment(@PathParam("fragment") String fragment) throws SQLException {
        return diseaseCatalog.getFragmentDiseases(fragment);
    }

    @GET
    @Path("/get_full_disease/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public FullDisease getFullDisease(@PathParam("id") int diseaseId) throws SQLException {
        return null;//buildFullDisease(diseaseId);
    }


    private FullDisease buildFullDisease(int diseaseId, int start, int limit) throws SQLException {
        Disease disease = diseaseCatalog.getDisease(String.valueOf(diseaseId));
        List<Pair<Integer, IndexRank>> rankedPubMeds = getRankedPubMeds(diseaseId);

        // get top n PubMeds ranked by the index
        // this.index.getTopPubMeds(n, diseaseId) : List<Integer> pubMedIds // sorted
        // select * from pubmeds where pubmedid in [pubMedIds]  // check if returns in the requested order

        // same for tweets and images

        /*
        //List<FullPubMed> pubMeds = diseaseCatalog.getFullPubmedsByDiseaseId(rankedPubMeds, start, limit);
        //List<FullTweet> tweets = diseaseCatalog.getFullTweetsByDiseaseId(diseaseId, start, limit);
        //List<FullImage> images = diseaseCatalog.getFullImagesByDiseaseId((diseaseId, start, limit);


        return new FullDisease(disease.getId(), disease.getName(), disease.getDescription(), disease.getDerivedFrom(),
                pubMeds, images, tweets);
        */
        return null;
    }

    public List<Pair<Integer, IndexRank>> getRankedPubMeds(int diseaseId) throws SQLException {
        CompoundRanker ranker = new CompoundRanker(ImmutableMap.of(
                RankType.TF_IDF_RANK, 0.3,
                RankType.DATE_RANK, 0.1,
                RankType.EXPLICIT_FEEDBACK_RANK, 0.4,
                RankType.IMPLICIT_FEEDBACK_RANK, 0.2
        ));

        Index index = new Index(ranker, diseaseCatalog);
        index.build();
        return index.getArticlesFor(diseaseId);
    }

}
