package pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.dto;

import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.models.Image;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class FullImage extends Image {

    private int implicitFeedback;
    private int explicitFeedback;

    public FullImage(int id, String url, int implicitFeedback, int explicitFeedback) {
        super(id, url);
        this.implicitFeedback = implicitFeedback;
        this.explicitFeedback = explicitFeedback;
    }


    public int getImplicitFeedback() {
        return implicitFeedback;
    }

    public void incrementImplicitFeedback() {
        implicitFeedback++;
    }

    public void decrementImplicitFeedback() {
        implicitFeedback--;
    }

    public int getExplicitFeedback() {
        return explicitFeedback;
    }

    public void incrementExplicitFeedback() {
        explicitFeedback++;
    }

    public void decrementExplicitFeedback() {
        explicitFeedback--;
    }

}
