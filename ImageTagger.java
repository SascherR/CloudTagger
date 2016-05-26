package cloudtagger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Sascha Rasler
 */
public class ImageTagger {

    private static final String IMAGES = "images.txt";
    private final ArrayList<String> loadedImages;
    private static HashMap<String, ArrayList<String>> allInfo;
    private final JSONParser jp;
    private JSONBuilder jb;

    public ImageTagger() throws IOException {
        long start = System.currentTimeMillis();
        jp = new JSONParser(IMAGES);
        loadedImages = new ArrayList<>();
        allInfo = new HashMap<>();
        init();
        System.out.println("----Image Tag parsing took: " + (System.currentTimeMillis() - start) + "ms");
    }

    private void init() throws IOException {
        allInfo = jp.getAllImageTags();
    }

    public ArrayList<String> getTagsForImage(String imageFileName) {
        return allInfo.get(imageFileName);
    }

    public void addImage(String image) {
        loadedImages.add(image);
        ArrayList<String> imageTags;
        imageTags = jp.getImageTagsForImage(image);
        if (!imageTags.isEmpty()) {
            allInfo.put(image, imageTags);
        } else {
            allInfo.put(image, new ArrayList<>());
        }
    }

    public void addTagToImage(String image, String tag) {
        String fileName = (String) image.subSequence(image.lastIndexOf("/") + 1, image.length());
        allInfo.get(fileName).add(tag);
        System.out.println("adding tag: " + tag + " to image: " + fileName);
        System.out.println("---has these tags now: " + allInfo.get(fileName));
    }

    public void removeTagFromImage(String image, String tag) {
        String fileName = (String) image.subSequence(image.lastIndexOf("/") + 1, image.length());
        allInfo.get(fileName).remove(tag);
        System.out.println("removing tag: " + tag + " from image: " + fileName);
        System.out.println("---has these tags now: " + allInfo.get(fileName));

    }

    public void writeAndClose() throws IOException {

        System.out.println("writing new JSON with image tags");
        long start = System.currentTimeMillis();
        //TODO: append to existing images!
        jb = new JSONBuilder(IMAGES);
        for (String key : allInfo.keySet()) {
            jb.addImage(key);
            ArrayList<String> imageTags = allInfo.get(key);
            jb.addTagsToImage(imageTags);
        }
        jb.finalizeJson();
        System.out.println("JSON-writing took: " + (System.currentTimeMillis() - start) + "ms");
    }

}
