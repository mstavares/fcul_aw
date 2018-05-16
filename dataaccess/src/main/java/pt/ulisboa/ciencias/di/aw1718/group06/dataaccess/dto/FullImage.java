package pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.dto;

import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.models.Image;

public class FullImage extends Image {

    private boolean blackListed;

    public FullImage(int id, String url, int implicitFeedback, int explicitFeedback, boolean blackListed) {
        super(id, url, implicitFeedback, explicitFeedback);
        this.blackListed = blackListed;
    }

    public boolean isBlackListed() {
        return blackListed;
    }

    public void setBlackListed(boolean blackListed) {
        this.blackListed = blackListed;
    }

}
