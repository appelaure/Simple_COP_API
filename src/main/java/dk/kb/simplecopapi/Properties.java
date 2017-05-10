package dk.kb.simplecopapi;

/**
 * Created by laap on 04-05-2017.
 */
public class Properties {

    private String name;
    private String thumbnail;
    private String src;
    private String genre;
    private String subjectCreationDate;
    private String recordCreationDate;
    private String recordChangeDate;
    private String geographic;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getSubjectCreationDate() {
        return subjectCreationDate;
    }

    public void setSubjectCreationDate(String subjectCreationDate) {
        this.subjectCreationDate = subjectCreationDate;
    }

    public String getRecordCreationDate() {
        return recordCreationDate;
    }

    public void setRecordCreationDate(String recordCreationDate) {
        this.recordCreationDate = recordCreationDate;
    }

    public String getGeographic() {
        return geographic;
    }

    public void setGeographic(String geographic) {
        this.geographic = geographic;
    }

    public String getRecordChangeDate() {
        return recordChangeDate;
    }

    public void setRecordChangeDate(String recordChangeDate) {
        this.recordChangeDate = recordChangeDate;
    }
}
