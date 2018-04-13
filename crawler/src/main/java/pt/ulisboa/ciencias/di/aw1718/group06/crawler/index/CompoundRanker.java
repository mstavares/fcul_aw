package pt.ulisboa.ciencias.di.aw1718.group06.crawler.index;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class CompoundRanker {

    private final Map<RankType, Double> rankers;
    private final double WEIGHTS_SUM;

    public CompoundRanker(Map<RankType, Double> weightedRankers) {
        this.rankers = ImmutableMap.copyOf(weightedRankers);
        this.WEIGHTS_SUM = rankers.values().stream().mapToDouble(w -> w).sum();
    }

    public IndexRank computeRank(RankingData rd) {
        double rank = 0;
        rank += rd.getTfidf() * rankers.getOrDefault(RankType.TF_IDF_RANK, 0.0);
        rank += rd.getNormalizedDate() * rankers.getOrDefault(RankType.DATE_RANK, 0.0);
        rank += rd.getExplicitFeedback() * rankers.getOrDefault(RankType.EXPLICIT_FEEDBACK_RANK, 0.0);
        rank += rd.getImplicitFeedback() * rankers.getOrDefault(RankType.IMPLICIT_FEEDBACK_RANK, 0.0);
        return new NumericalRank(rank / WEIGHTS_SUM);
    }
}
