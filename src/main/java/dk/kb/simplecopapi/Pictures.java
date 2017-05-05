package dk.kb.simplecopapi;

import java.util.List;

/**
 * Created by laap on 04-05-2017.
 */
public class Pictures {
    private String type;
    private List<Picture> features;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Picture> getFeatures() {
        return features;
    }

    public void setFeatures(List<Picture> features) {
        this.features = features;
    }
}
