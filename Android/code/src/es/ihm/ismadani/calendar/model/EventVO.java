package es.ihm.ismadani.calendar.model;


/**
 * Created by Filosente on 16/10/13.
 */

import java.util.List;
import org.ektorp.support.Entity;
import org.ektorp.support.*;

@TypeDiscriminator("doc.type == 'Event'")
public class EventVO extends Entity {


    private String type = "Event";
    private String date;
    private String creator;
    private String description;
    private List<String> tags;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    @Override
    public String toString() {
        String string = super.getId() + "@" + super.getRevision() + " | " + this.description + " | " + this.creator + " | " +
                this.date +  "\t| [";
        for (String tag:tags) {
            string += tag + ";";
        }
        string += "]";
        return string;
    }



}


