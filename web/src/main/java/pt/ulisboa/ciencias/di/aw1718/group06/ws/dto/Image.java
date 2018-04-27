package pt.ulisboa.ciencias.di.aw1718.group06.ws.dto;

public class Image {

    private int id;
    private String url;
    private boolean blackListed;

    public Image() {}

    public Image(int id, String url, boolean blackListed) {
        this.id = id;
        this.url = url;
        this.blackListed = blackListed;
    }
    
}
