package pt.ulisboa.ciencias.di.aw1718.group06.crawler.index;

import java.util.Objects;

public class NumericalRank implements IndexRank {

    private final double rank;

    public NumericalRank(double rank) {
        this.rank = rank;
    }

    @Override
    public double get() {
        return rank;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NumericalRank that = (NumericalRank) o;
        return Double.compare(that.rank, rank) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rank);
    }
}
