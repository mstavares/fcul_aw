package pt.ulisboa.ciencias.di.aw1718.group06.crawler.index;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import javafx.util.Pair;
import org.junit.Test;
import org.mockito.Matchers;
import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.models.Disease;
import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.models.PubMed;

import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class IndexTest {

    @Test
    public void testBuildIndex() {
        Disease d1 = new Disease(1, "d1", "description 1", "derived from 1");
        Disease d2 = new Disease(2, "d2", "description 2", "derived from 2");

        PubMed pm1 = new PubMed(1, 1, "title 1", "abstract 1");
        PubMed pm2 = new PubMed(2, 2, "title 2", "abstract 2");
        PubMed pm3 = new PubMed(3, 3, "title 3", "abstract 3");

        ImmutableMap<Integer, Disease> diseases = ImmutableMap.of(d1.getId(), d1, d2.getId(), d2);
        Pair<PubMed, List<Integer>> pma1 = new Pair<>(pm1, ImmutableList.of(d1.getId(), d2.getId()));
        Pair<PubMed, List<Integer>> pma2 = new Pair<>(pm2, ImmutableList.of(d1.getId()));
        Pair<PubMed, List<Integer>> pma3 = new Pair<>(pm3, ImmutableList.of(d2.getId()));

        PubMedRanked ranked1 = new PubMedRanked(pm1, new NumericalRank(0.5));
        PubMedRanked ranked2 = new PubMedRanked(pm2, new NumericalRank(0.2));
        ArticleRanker rankerMock = mock(ArticleRanker.class);
        when(rankerMock.rank(Matchers.eq(d1.getId()), Matchers.any()))
            .thenReturn(ImmutableList.of(
                new Pair<>(ranked1.getPubMed().getId(), ranked1.getRank()),
                new Pair<>(ranked2.getPubMed().getId(), ranked2.getRank())));

        Index index = new Index(diseases, ImmutableList.of(pma1, pma2, pma3), rankerMock);

        assertThat(index.getArticlesFor(d1.getId())).containsExactlyElementsIn(ImmutableList.of(ranked1, ranked2));
    }
}
