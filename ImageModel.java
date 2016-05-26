package cloudtagger;

import java.net.URL;
import java.util.ArrayList;
import javax.swing.ImageIcon;

/**
 * simple container class for tagged images
 *
 * @author Sascha Rasler
 */
public class ImageModel  {

    private long id = 0;
    private int tagCount = 0;
    private String title = "";
    private final String imageScource;
    private String[] imageTags;
    private URL imageUrl = null;
    private ImageIcon nodeicon;
    private final ArrayList<String> tags;
   
    public ImageModel(String source) {
        imageScource = source;
        tags = new ArrayList<>();
        id++;
    }

    public long getId() {
        return id;
    }

    public void addTag(String tag) {
        tagCount++;
        tags.add(tag);
    }

    public int getTagCount() {
        return tagCount;
    }

    public ArrayList<String> getTags() {
        return tags;
    }

    public void setTitle(String newTitle) {
        this.title = newTitle;
    }

    public String getTitle() {
        return title;
    }

    public URL getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(URL imageUrl) {
        this.imageUrl = imageUrl;
    }

    public ImageIcon getNodeicon() {
        return nodeicon;
    }

    public void setNodeicon(ImageIcon nodeicon) {
        this.nodeicon = nodeicon;
    }
}
