package pt.ulisboa.ciencias.di.aw1718.group06.crawler.index;

import javafx.util.Pair;
import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.models.Disease;
import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.models.PubMed;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Index {

    private final HashMap<Integer, Disease> diseases = new HashMap<>();
    private final HashMap<Integer, PubMed> articles = new HashMap<>();
    private final HashMap<Integer, List<Pair<Integer, IndexRank>>> index = new HashMap<>();
    private final ArticleRanker ranker;

    public Index(Map<Integer, Disease> diseases, List<Pair<PubMed, List<Integer>>> pubMedsAnnotated, ArticleRanker ranker) {
        this.diseases.putAll(diseases);
        this.articles.putAll(pubMedsAnnotated.stream()
            .map(Pair::getKey)
            .collect(Collectors.toMap(PubMed::getId, pm -> pm)));
        this.ranker = ranker;
        build(pubMedsAnnotated);
    }

    public void build(List<Pair<PubMed, List<Integer>>> pubMedsAnnotated) {
        for (Disease disease : this.diseases.values()) {
            index.put(disease.getId(), this.ranker.rank(disease.getId(), pubMedsAnnotated));
        }
    }

    public List<PubMedRanked> getArticlesFor(int diseaseId) {
        return index.get(diseaseId).stream()
            .map(idToRank -> new PubMedRanked(articles.get(idToRank.getKey()), idToRank.getValue()))
            .collect(Collectors.toList());
    }
}
