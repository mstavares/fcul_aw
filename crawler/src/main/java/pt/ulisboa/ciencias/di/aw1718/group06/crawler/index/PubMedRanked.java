package pt.ulisboa.ciencias.di.aw1718.group06.crawler.index;

import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.models.PubMed;

import java.util.Objects;

public class PubMedRanked {

    private final PubMed pubMed;
    private final IndexRank rank;

    public PubMedRanked(PubMed pubMed, IndexRank rank) {
        this.pubMed = pubMed;
        this.rank = rank;
    }

    public PubMed getPubMed() {
        return pubMed;
    }

    public IndexRank getRank() {
        return rank;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PubMedRanked that = (PubMedRanked) o;
        return Objects.equals(pubMed, that.pubMed) &&
            Objects.equals(rank, that.rank);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pubMed, rank);
    }
}
