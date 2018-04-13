package pt.ulisboa.ciencias.di.aw1718.group06.crawler.index;

import com.google.common.collect.ImmutableList;
import javafx.util.Pair;
import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.Disease;
import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.DiseaseCatalog;
import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.Feedback;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class Index {

    private static final long BASE_TIME = System.currentTimeMillis();

    private final Map<Integer, List<Pair<Integer, RankingData>>> index = new HashMap<>();
    private final CompoundRanker ranker;
    private final DiseaseCatalog catalog;

    public Index(CompoundRanker ranker, DiseaseCatalog catalog) {
        this.ranker = ranker;
        this.catalog = catalog;
    }

    /**
     * Computes the ranking values for each relevant article for a given {@code diseaseId}.
     * Returns a sorted list of pairs of PubMedId of a relevant article and the computed {@link IndexRank}.
     *
     * @param diseaseId
     * @return
     * @throws SQLException
     */
    public List<Pair<Integer, IndexRank>> getArticlesFor(int diseaseId) throws SQLException {
        List<Pair<Integer, IndexRank>> finalRanking = new ArrayList<>();

        Feedback maxFb = catalog.getPubMedMaxFeedback(diseaseId);
        for (Pair<Integer, RankingData> pmToRD : index.get(diseaseId)) {
            Integer pmId = pmToRD.getKey();
            Feedback fb = catalog.getDiseasePubMedFeedback(diseaseId, pmId);

            RankingData newRd = new RankingData(
                pmToRD.getValue().getTfidf(),
                pmToRD.getValue().getNormalizedDate(),
                fb.getImplicitFeedback() / (double) maxFb.getImplicitFeedback(),
                fb.getExplicitFeedback() / (double) maxFb.getExplicitFeedback());

            finalRanking.add(new Pair<>(pmId, ranker.computeRank(newRd)));
        }
        // sort in descending order
        finalRanking.sort((o1, o2) -> -Double.compare(o1.getValue().get(), o2.getValue().get()));
        return ImmutableList.copyOf(finalRanking);
    }

    /**
     * Builds the Index, precomputes the values of TF-IDF and date metrics.
     */
    public void build() throws SQLException {
        List<Integer> pubmedIds = catalog.getAllPubMedIds();
        List<Integer> disIds = catalog.getDiseases(0).stream()
            .map(Disease::getId)
            .collect(Collectors.toList());

        // list of (PubMedId, List<RelatedDiseaseIds>)
        List<Pair<Integer, List<Integer>>> pubMedsAnnotated = getPubMedsAnnotated(pubmedIds);

        computeAndStoreTfIdf(disIds, pubMedsAnnotated);
        index.putAll(getPrecomputedRankingData(disIds, pubMedsAnnotated));

    }

    private List<Pair<Integer, List<Integer>>> getPubMedsAnnotated(List<Integer> pubmeds) throws SQLException {
        List<Pair<Integer, List<Integer>>> pubMedsAnnotated = new ArrayList<>();
        for (Integer pmId : pubmeds) {
            pubMedsAnnotated.add(new Pair<>(pmId, catalog.getRelatedDiseaseIds(pmId)));
        }
        return pubMedsAnnotated;
    }

    private void computeAndStoreTfIdf(List<Integer> diseases, List<Pair<Integer, List<Integer>>> pubMedsAnnotated) throws SQLException {
        // for normalization of idf for all diseases
        Map<Integer, Double> diseaseToIdf = new HashMap<>();
        double maxIdf = 0;

        for (Integer disId : diseases) {
            List<Integer> relevant = getRelevantPubMeds(disId, pubMedsAnnotated);

            double idf = Math.log(pubMedsAnnotated.size() / (double) relevant.size());
            diseaseToIdf.put(disId, idf);
            if (idf > maxIdf) {
                maxIdf = idf;
            }
            for (Integer pm : relevant) {
                int occ = catalog.getDiseaseOccurrences(disId, pm);
                double tf = occ / (double) catalog.getAllOccurrences(pm);
                catalog.updateDiseasePubmedTf(disId, pm, tf);
            }
        }
        // idf normalization
        for (Map.Entry<Integer, Double> dToIdf : diseaseToIdf.entrySet()) {
            catalog.updateDiseaseIdf(dToIdf.getKey(), dToIdf.getValue() / maxIdf);
        }
    }

    private Map<Integer, List<Pair<Integer, RankingData>>> getPrecomputedRankingData(
          List<Integer> disIds, List<Pair<Integer, List<Integer>>> pubMedsAnnotated) throws SQLException {

        Map<Integer, List<Pair<Integer, RankingData>>> ind = new HashMap<>();
        for (Integer disId : disIds) {
            double idf = catalog.getIdf(disId);
            List<Integer> relevant = getRelevantPubMeds(disId, pubMedsAnnotated);

            List<Pair<Integer, RankingData>> rankingData = new ArrayList<>();
            for (Integer pmId : relevant) {
                double tfidf = catalog.getTf(disId, pmId) * idf;
                double normalizedDate = catalog.getPubMedDate(pmId).toMillis() / (double) BASE_TIME;
                rankingData.add(new Pair<>(pmId, new RankingData(tfidf, normalizedDate)));
            }
            ind.put(disId, rankingData);
        }
        return ind;
    }

    private List<Integer> getRelevantPubMeds(int disId, List<Pair<Integer, List<Integer>>> pubMedsAnnotated) {
        return pubMedsAnnotated.stream()
                    .filter(pma -> pma.getValue().contains(disId))
                    .map(Pair::getKey)
                    .collect(Collectors.toList());
    }
}
