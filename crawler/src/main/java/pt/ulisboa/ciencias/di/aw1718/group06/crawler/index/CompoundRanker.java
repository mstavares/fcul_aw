package pt.ulisboa.ciencias.di.aw1718.group06.crawler.index;

import com.google.common.collect.ImmutableMap;
import javafx.util.Pair;
import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.models.PubMed;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CompoundRanker implements ArticleRanker {

    private final Map<ArticleRanker, Double> rankers;

    public CompoundRanker(Map<ArticleRanker, Double> weightedRankers) {
        this.rankers = ImmutableMap.copyOf(weightedRankers);
    }

    @Override
    public List<Pair<Integer, IndexRank>> rank(int diseaseId, List<Pair<PubMed, List<Integer>>> pubMedsAnnotated) {
        Map<Integer, IndexRank> combinedResults = new HashMap<>();
        for (Map.Entry<ArticleRanker, Double> entry : rankers.entrySet()) {
            entry.getKey().rank(diseaseId, pubMedsAnnotated)
                .forEach(idToRank -> {
                    IndexRank current = combinedResults.getOrDefault(idToRank.getKey(), new NumericalRank(0));
                    double newRank = current.get() + idToRank.getValue().get() * entry.getValue();
                    combinedResults.put(idToRank.getKey(), new NumericalRank(newRank));
                });
        }
        return combinedResults.entrySet().stream()
            .map(entry -> new Pair<>(entry.getKey(), entry.getValue()))
            .sorted(Comparator.comparingDouble(o -> o.getValue().get()))
            .collect(Collectors.toList());
    }
}
