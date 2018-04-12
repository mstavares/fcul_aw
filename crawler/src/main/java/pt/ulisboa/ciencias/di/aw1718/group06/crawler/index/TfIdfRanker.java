package pt.ulisboa.ciencias.di.aw1718.group06.crawler.index;

import javafx.util.Pair;
import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.Disease;
import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.PubMed;

import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

public class TfIdfRanker implements ArticleRanker {

    private Map<Integer, Disease> diseases;

    public TfIdfRanker(Map<Integer, Disease> diseases) {
        this.diseases = diseases;
    }

    @Override
    public List<Pair<Integer, IndexRank>> rank(int diseaseId, List<Pair<PubMed, List<Integer>>> pubMedsAnnotated) {

        String name = this.diseases.get(diseaseId).getName();
        List<PubMed> relevant = pubMedsAnnotated.stream()
            .filter(pma -> pma.getValue().contains(diseaseId))
            .map(Pair::getKey)
            .collect(Collectors.toList());
        double idf = Math.log(pubMedsAnnotated.size() / (double) relevant.size());
        return relevant.stream()
            .map(pm -> {
                double tf = computeTermFrequency(pm.getDescription(), name);
                return new Pair<>(pm.getId(), (IndexRank) new NumericalRank(tf * idf));
            }).collect(Collectors.toList());
    }

    private double computeTermFrequency(String description, String name) {
        // for relevant articles, count:
        // - number of occurrences of the term
        // - number of words in the text
        int foundIdx = 0;
        int occurrences = 0;
        name = name.toLowerCase();
        description = description.toLowerCase();
        while (foundIdx != -1) {
            foundIdx = description.indexOf(name, foundIdx + 1);
            if (foundIdx != -1) {
                occurrences++;
            }
        }
        int numOfTokens = new StringTokenizer(description).countTokens();
        int nameTokens = new StringTokenizer(name).countTokens();
        if (nameTokens > 1) {
            // We want to count the whole phrase as one token, so the excessive amount of tokens
            // counted in description is subtracted.
            numOfTokens -= occurrences * (nameTokens - 1);
        }
        return (double) occurrences / numOfTokens;
    }
}
