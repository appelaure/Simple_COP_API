package dk.kb.simplecopapi;

/**
 * Created by laap on 03-05-2017.
 */
public class Edition {
    private String title;
    private String link;
    private String description;
    private String Identifier;
    private String thumbnailURI;
    private String imageURI;

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIdentifier() {
        return Identifier;
    }

    public void setIdentifier(String identifier) {
        Identifier = identifier;
    }

    public String getThumbnailURI() {
        return thumbnailURI;
    }

    public void setThumbnailURI(String thumbnailURI) {
        this.thumbnailURI = thumbnailURI;
    }

    public String getImageURI() {
        return imageURI;
    }

    public void setImageURI(String imageURI) {
        this.imageURI = imageURI;
    }
}
