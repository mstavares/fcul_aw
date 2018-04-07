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
        return pubMedsAnnotated.stream()
            .filter(pma -> pma.getValue().contains(diseaseId))
            .map(pma -> {
                PubMed pm = pma.getKey();
                double tf = computeTermFrequency(pm.getDescription(), name);
                return new Pair<>(pm.getId(), (IndexRank) () -> tf);
            }).collect(Collectors.toList());
        // TODO: Implement IDF and combine.
    }

    private static double computeTermFrequency(String description, String name) {
        // for relevant articles, count:
        // - number of occurrences of the term
        // - number of words in the text
        int foundIdx = 0;
        int occurrences = 0;
        while (foundIdx != -1) {
            foundIdx = description.indexOf(name, foundIdx);
            if (foundIdx != -1) {
                occurrences++;
            }
        }
        int numOfTokens = new StringTokenizer(description).countTokens();
        // TODO: take care of multi-word names!
        return (double) occurrences / numOfTokens;
    }
}
