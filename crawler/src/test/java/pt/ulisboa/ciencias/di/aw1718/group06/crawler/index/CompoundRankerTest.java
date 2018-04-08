package pt.ulisboa.ciencias.di.aw1718.group06.crawler.index;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import javafx.util.Pair;
import org.junit.Test;
import org.mockito.Matchers;

import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CompoundRankerTest {

    @Test
    public void testCompoundRanking() {
        final int DIS_1_ID = 1;
        final double RANKER_1_WEIGHT = 0.3;
        final double RANKER_2_WEIGHT = 0.7;

        ArticleRanker rankerMock1 = mock(ArticleRanker.class);
        ArticleRanker rankerMock2 = mock(ArticleRanker.class);
        ImmutableList<Pair<Integer, IndexRank>> ranker1results = ImmutableList.of(
            new Pair<>(1, new NumericalRank(0.1)),
            new Pair<>(2, new NumericalRank(0.2)),
            new Pair<>(3, new NumericalRank(0.3))
        );
        ImmutableList<Pair<Integer, IndexRank>> ranker2results = ImmutableList.of(
            new Pair<>(1, new NumericalRank(0.5)),
            new Pair<>(2, new NumericalRank(0.7)),
            new Pair<>(3, new NumericalRank(0.1))
        );
        when(rankerMock1.rank(Matchers.eq(DIS_1_ID), Matchers.any())).thenReturn(ranker1results);
        when(rankerMock2.rank(Matchers.eq(DIS_1_ID), Matchers.any())).thenReturn(ranker2results);

        CompoundRanker ranker = new CompoundRanker(ImmutableMap.of(rankerMock1, RANKER_1_WEIGHT, rankerMock2, RANKER_2_WEIGHT));
        List<Pair<Integer, IndexRank>> ranking = ranker.rank(DIS_1_ID, ImmutableList.of());

        assertThat(ranking.size()).isEqualTo(3);
        assertThat(ranking).contains(new Pair<>(1, new NumericalRank(RANKER_1_WEIGHT * ranker1results.get(0).getValue().get() + RANKER_2_WEIGHT * ranker2results.get(0).getValue().get())));
        assertThat(ranking).contains(new Pair<>(2, new NumericalRank(RANKER_1_WEIGHT * ranker1results.get(1).getValue().get() + RANKER_2_WEIGHT * ranker2results.get(1).getValue().get())));
        assertThat(ranking).contains(new Pair<>(3, new NumericalRank(RANKER_1_WEIGHT * ranker1results.get(2).getValue().get() + RANKER_2_WEIGHT * ranker2results.get(2).getValue().get())));
    }
}
