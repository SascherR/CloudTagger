package cloudtagger;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author Sascha Rasler
 *
 */
public class JSONBuilder {

    private final JsonFactory jf;
    private final JsonGenerator jg;
    private final File target;

    public JSONBuilder(String fileName) throws IOException {

        jf = new JsonFactory();
        target = new File(fileName);

        jg = jf.createGenerator(target, JsonEncoding.UTF8);
        jg.writeStartObject();
    }

    public void addTag(String tagName) throws IOException {
        jg.writeObjectFieldStart(tagName);
    }

    public void addDistance(String toTag, double distance) throws IOException {
        jg.writeNumberField(toTag, distance);
    }

    public void addImage(String imageUrl) throws IOException {
        jg.writeArrayFieldStart(imageUrl);
    }

    public void addTagsToImage(ArrayList<String> tags) throws IOException {
        for (String tag : tags) {
            jg.writeString(tag);
        }
        jg.writeEndArray();
    }

    public void finalizeTag() throws IOException {
        jg.writeEndObject();
    }

    public void finalizeJson() throws IOException {
        jg.close();
    }

    /*
    public static void main(String[] args) throws IOException {

        JSONBuilder test = new JSONBuilder("images.txt");
        ArrayList<String> strings = new ArrayList();

        strings.add("frog");
        strings.add("green");
        test.addImage("Green Frog.jpg");
        test.addTagsToImage(strings);

        strings.clear();
        strings.add("dog");
        test.addImage("358497_8375.jpg");
        test.addTagsToImage(strings);
        test.finalizeJson();
    }
    */
}
