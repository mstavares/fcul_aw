package pt.ulisboa.ciencias.di.aw1718.group06.crawler.index;

import javafx.util.Pair;
import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.PubMed;

import java.util.List;

public interface ArticleRanker {

    /**
     * Computes ranking for all articles with regard to the given disease.
     *
     * @param diseaseId        ID of the queried disease.
     * @param pubMedsAnnotated List of pairs containing the pubMed article and a list of IDs of diseases it was annotated with.
     * @return List of pairs containing the article ID and the corresponding ranking value. // TODO: should be sorted here?
     */
    List<Pair<Integer, IndexRank>> rank(int diseaseId, List<Pair<PubMed, List<Integer>>> pubMedsAnnotated);
}
