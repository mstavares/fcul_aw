package pt.ulisboa.ciencias.di.aw1718.group06.crawler.index;

public class RankingData {


    private final double tfidf;
    private final double date;
    private final double implicitFeedback;
    private final double explicitFeedback;

    public RankingData(double tfidf, double date) {
        this.tfidf = tfidf;
        this.date = date;
        this.implicitFeedback = 0;
        this.explicitFeedback = 0;
    }

    public RankingData(double tfidf, double date, double implicitFeedback, double explicitFeedback) {
        this.tfidf = tfidf;
        this.date = date;
        this.implicitFeedback = implicitFeedback;
        this.explicitFeedback = explicitFeedback;
    }

    public double getTfidf() {
        return tfidf;
    }

    public double getNormalizedDate() {
        return date;
    }

    public double getImplicitFeedback() {
        return implicitFeedback;
    }

    public double getExplicitFeedback() {
        return explicitFeedback;
    }
}
