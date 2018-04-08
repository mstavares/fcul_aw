package pt.ulisboa.ciencias.di.aw1718.group06.crawler.index;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import javafx.util.Pair;
import org.junit.Test;
import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.Disease;
import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.PubMed;

import java.util.List;

import static com.google.common.truth.Truth.assertThat;

public class TfIdfRankerTest {

    @Test
    public void testTfIdfRank() {

        Disease d1 = new Disease(1, "dis1", "description 1", "derived from 1");
        Disease d2 = new Disease(2, "dis2", "description 2", "derived from 2");

        PubMed pm1 = new PubMed(1, 1, "title 1", "one two three DIS1 five six DIS1 DIS2 DIS1 ten");
        PubMed pm2 = new PubMed(2, 2, "title 2", "one DIS2 DIS1 four DIS2 six seven DIS2 DIS2 ten");
        PubMed pm3 = new PubMed(3, 3, "title 3", "abstract 3 DIS2");

        ImmutableMap<Integer, Disease> diseases = ImmutableMap.of(d1.getId(), d1, d2.getId(), d2);
        Pair<PubMed, List<Integer>> pma1 = new Pair<>(pm1, ImmutableList.of(d1.getId(), d2.getId()));
        Pair<PubMed, List<Integer>> pma2 = new Pair<>(pm2, ImmutableList.of(d1.getId(), d2.getId()));
        Pair<PubMed, List<Integer>> pma3 = new Pair<>(pm3, ImmutableList.of(d2.getId()));

        TfIdfRanker ranker = new TfIdfRanker(diseases);

        List<Pair<Integer, IndexRank>> d1Rank = ranker.rank(d1.getId(), ImmutableList.of(pma1, pma2, pma3));
        // TF(d1, pm1) = 3/10 = 0.3
        // TF(d1, pm2) = 1/10 = 0.1
        // IDF(d1) = log(3/2)
        IndexRank pm1d1rank = new NumericalRank(0.3 * Math.log(1.5));
        IndexRank pm2d1rank = new NumericalRank(0.1 * Math.log(1.5));

        assertThat(d1Rank.size()).isEqualTo(2);
        assertThat(d1Rank.get(0).getKey()).isEqualTo(pm1.getId());
        assertThat(d1Rank.get(0).getValue().get()).isEqualTo(pm1d1rank.get());
        assertThat(d1Rank.get(1).getKey()).isEqualTo(pm2.getId());
        assertThat(d1Rank.get(1).getValue().get()).isEqualTo(pm2d1rank.get());


        List<Pair<Integer, IndexRank>> d2Rank = ranker.rank(d2.getId(), ImmutableList.of(pma1, pma2, pma3));
        // TF(d2, pm1) = 1/10 = 0.1
        // TF(d2, pm2) = 4/10 = 0.4
        // IDF(d2) = log(3/3) = 0   => all ranks are 0.0!
        assertThat(d2Rank.size()).isEqualTo(3);
        assertThat(d2Rank.get(0).getKey()).isEqualTo(pm1.getId());
        assertThat(d2Rank.get(0).getValue().get()).isEqualTo(0.0);
        assertThat(d2Rank.get(1).getKey()).isEqualTo(pm2.getId());
        assertThat(d2Rank.get(1).getValue().get()).isEqualTo(0.0);
        assertThat(d2Rank.get(2).getKey()).isEqualTo(pm3.getId());
        assertThat(d2Rank.get(2).getValue().get()).isEqualTo(0.0);
    }
}