package pt.ulisboa.ciencias.di.aw1718.group06.crawler.index;

import com.google.common.collect.ImmutableList;
import javafx.util.Pair;
import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.models.Disease;
import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.DiseaseCatalog;
import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.models.Feedback;

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

        for (Pair<Integer, RankingData> pmToRD : index.get(diseaseId)) {
            int pmId = pmToRD.getKey();
            double rank = catalog.getDiseasePubMedRank(diseaseId, pmId);
            finalRanking.add(new Pair<>(pmId, new NumericalRank(rank)));
        }
        // sort in descending order
        finalRanking.sort((o1, o2) -> -Double.compare(o1.getValue().get(), o2.getValue().get()));
        return ImmutableList.copyOf(finalRanking);
    }

    /**
     * Computes and updates ranks stored in the catalog for all articles related to a given {@code diseaseId}.
     * Should be called if any feedback data for any of the articles related to the {@code diseaseId} has changed.
     *
     * @param diseaseId
     * @throws SQLException
     */
    public void updateRankOfPubMedsForDisease(int diseaseId) throws SQLException {
        List<Pair<Integer, RankingData>> rankedPubmeds = index.get(diseaseId);

        Feedback maxFb = catalog.getPubMedMaxFeedback(diseaseId);
        for (Pair<Integer, RankingData> pm : rankedPubmeds) {
            Feedback fb = catalog.getDiseasePubMedFeedback(diseaseId, pm.getKey());
            RankingData newRd = getDataWithNormalizedFeedback(fb, maxFb, pm.getValue());
            catalog.updateDiseasePubMedRank(diseaseId, pm.getKey(), ranker.computeRank(newRd).get());
        }
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
        computeRanks();
    }

    private void computeRanks() throws SQLException {
        for (int disId : index.keySet()) {
            updateRankOfPubMedsForDisease(disId);
        }
    }

    private RankingData getDataWithNormalizedFeedback(Feedback fb, Feedback maxFb, RankingData ranking) throws SQLException {
        double normImplicit = (maxFb.getImplicitFeedback() == 0
            ? fb.getImplicitFeedback()
            : fb.getImplicitFeedback() / (double) maxFb.getImplicitFeedback());
        double normExplicit = (maxFb.getExplicitFeedback() == 0
            ? fb.getExplicitFeedback()
            : fb.getExplicitFeedback() / (double) maxFb.getExplicitFeedback());
        return new RankingData(
            ranking.getTfidf(),
            ranking.getNormalizedDate(),
            normImplicit,
            normExplicit);
    }

    private List<Pair<Integer, List<Integer>>> getPubMedsAnnotated(List<Integer> pubmeds) throws SQLException {
        List<Pair<Integer, List<Integer>>> pubMedsAnnotated = new ArrayList<>();
        for (int pmId : pubmeds) {
            pubMedsAnnotated.add(new Pair<>(pmId, catalog.getRelatedDiseaseIds(pmId)));
        }
        return pubMedsAnnotated;
    }

    private List<Integer> getRelevantPubMeds(int disId, List<Pair<Integer, List<Integer>>> pubMedsAnnotated) {
        return pubMedsAnnotated.stream()
            .filter(pma -> pma.getValue().contains(disId))
            .map(Pair::getKey)
            .collect(Collectors.toList());
    }

    /**
     * Computes and updates {@code catalog}'s values of TF-IDF metrics.
     *
     * @param diseases
     * @param pubMedsAnnotated
     * @throws SQLException
     */
    private void computeAndStoreTfIdf(List<Integer> diseases, List<Pair<Integer, List<Integer>>> pubMedsAnnotated) throws SQLException {
        for (int disId : diseases) {
            List<Integer> relevant = getRelevantPubMeds(disId, pubMedsAnnotated);

            double idf = Math.log(pubMedsAnnotated.size() / (double) relevant.size());
            catalog.updateDiseaseIdf(disId, idf);
            for (int pmId : relevant) {
                double tf = catalog.getDiseaseOccurrences(disId, pmId) / (double) catalog.getAllOccurrences(pmId);
                catalog.updateDiseasePubmedTf(disId, pmId, tf);
            }
        }
    }

    private Map<Integer, List<Pair<Integer, RankingData>>> getPrecomputedRankingData(
          List<Integer> disIds, List<Pair<Integer, List<Integer>>> pubMedsAnnotated) throws SQLException {

        Map<Integer, Double> idfs = catalog.getAllIdfs();
        Map<Integer, List<Pair<Integer, RankingData>>> ind = new HashMap<>();
        for (int disId : disIds) {
            double idf = idfs.get(disId);
            List<Integer> relevant = getRelevantPubMeds(disId, pubMedsAnnotated);

            double maxTfIdf = 0;
            List<Pair<Integer, RankingData>> rankingData = new ArrayList<>();
            for (int pmId : relevant) {
                double tfidf = catalog.getTf(disId, pmId) * idf;
                if (tfidf > maxTfIdf) {
                    maxTfIdf = tfidf;
                }
                double normalizedDate = catalog.getPubMedDate(pmId).getTime() / (double) BASE_TIME;
                rankingData.add(new Pair<>(pmId, new RankingData(tfidf, normalizedDate)));
            }
            // Normalize tf-idf values by the maximum value among relevant documents.
            // Thanks to that, the document with the highest value of tf-idf will effectively get a max value of 1.
            for (Pair<Integer, RankingData> rd : rankingData) {
                rd.getValue().normalizeTfIdf(maxTfIdf);
            }

            ind.put(disId, rankingData);
        }
        return ind;
    }


}
