package cloudtagger;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Sascha Rasler
 */
public class JSONParser {

    private final File source;
    private final JsonNode rootNode;
    private final ObjectMapper m;
    private final JsonFactory f;

    public JSONParser(String fileName) throws IOException {

        m = new ObjectMapper();
        source = new File(fileName);
        rootNode = m.readTree(source);
        f = new JsonFactory();
    }

    public double getSimilarity(String fromTag, String toTag) throws IOException {

        JsonNode fromNode = rootNode.path(fromTag);
        return fromNode.path(toTag).asDouble();
    }

    public ArrayList<String> getImageTagsForImage(String image) {

        JsonNode imageNode = rootNode.path(image);
        ArrayList<String> imageTags = new ArrayList<>();
        for (int i = 0; i < imageNode.size(); i++) {
            imageTags.add(imageNode.get(i).asText());
        }
        return imageTags;
    }

    public ArrayList<String> getAllTagsNames() throws IOException {

        ArrayList<String> tags = new ArrayList<>();
        try (JsonParser jp = f.createParser(source)) {
            jp.nextToken();
            while (jp.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = jp.getCurrentName();
                tags.add(fieldName);
                jp.nextToken();
            }
        }
        return tags;
    }

    public HashMap<String, ArrayList<String>> getAllImageTags() throws IOException {

        HashMap<String, ArrayList<String>> allImageTags = new HashMap<>();
        String imageName = "";
        ArrayList<String> tags = new ArrayList<>();

        try (JsonParser jp = f.createParser(source)) {
            jp.nextToken();
            while (jp.nextToken() != JsonToken.END_OBJECT) {
                if (jp.getCurrentToken() == JsonToken.FIELD_NAME) {
                    if (!imageName.isEmpty()) {
                        allImageTags.put(imageName, tags);
                    }
                    //System.out.println("Image Name: " + jp.getCurrentName());
                    imageName = jp.getCurrentName();
                    tags = new ArrayList<>();
                }
                if (jp.getCurrentToken() == JsonToken.VALUE_STRING) {
                    //System.out.println("tag Name: " + jp.getValueAsString());
                    tags.add(jp.getValueAsString());
                }
            }
            allImageTags.put(imageName, tags);
        }
        System.out.println("************************parsed from JSON: " + allImageTags);
        return allImageTags;
    }

    public Map<String, Double> getAllDistances(String tag) throws IOException {

        HashMap<String, Double> allDistances;
        try (JsonParser jp = f.createParser(source)) {
            allDistances = new HashMap<>();
            jp.nextToken();
            while (jp.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = jp.getCurrentName();
                //make sure Token is a parent:
                if (tag.equals(fieldName) && (jp.nextToken() == JsonToken.START_OBJECT)) {
                    while (jp.nextToken() != JsonToken.END_OBJECT) {
                        String toTag = jp.getCurrentName();
                        if (toTag.equals(tag)) {
                            //looped back into parent - so all children have been read
                            break;
                        }
                        jp.nextValue();
                        allDistances.put(toTag, (Double) jp.getNumberValue());
                    }
                }
                jp.nextToken();
            }
        }
        return LinkedHashMapSorter.sortByValue(allDistances);
    }

}
