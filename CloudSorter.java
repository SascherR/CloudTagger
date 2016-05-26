/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cloudtagger;

import java.awt.Point;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 * @author Sascha Rasler
 */
public class CloudSorter {

    private static final String DISTANCES = "distances.txt";
    private static final int LEVELS = 6;
    private static final int NUMTAGS = 96;
    private final int tagsPerLevel;
    private final JSONParser jp;
    private final LinkedHashMap<String, Double> sortedDistances;
    private String mainTag;

    public CloudSorter(String mainTag) throws IOException {
        this.mainTag = mainTag;
        jp = new JSONParser(DISTANCES);
        
        /*Step-wise:*/
        //tagsPerLevel = (int) Math.ceil((double) NUMTAGS / (double) LEVELS);
        
        /* manually set for step-less zoom:*/
        tagsPerLevel = 16;
        
        sortedDistances = (LinkedHashMap<String, Double>) jp.getAllDistances(mainTag);
        //System.out.println(sortedDistances);
        //System.out.println(currentLevel);
    }

    public void setMainTag(String tag) {
        this.mainTag = tag;
    }

    public Point getTagCoordinates(String tag) {
        Point coordinates = new Point();
        return coordinates;
    }


    private Map<String, Double> setupLevel(Map distances, int level) {
        HashMap<String, Double> m = new HashMap<>();
        int startIndex = (level * tagsPerLevel);
        int endIndex = startIndex + tagsPerLevel;
        endIndex = endIndex > NUMTAGS ? NUMTAGS : endIndex;

        for (int j = startIndex; j < endIndex; j++) {
            m.put((String) distances.keySet().toArray()[j], (Double) distances.values().toArray()[j]);
        }
        return LinkedHashMapSorter.sortByValue(m);
    }
    
    private Map<String, Double> setupLevelFromIndex (Map distances, int index) {
        HashMap<String, Double> m = new HashMap<>();
        int startIndex = index;
        int endIndex = startIndex + tagsPerLevel;
        endIndex = endIndex > NUMTAGS ? NUMTAGS : endIndex;

        for (int j = startIndex; j < endIndex; j++) {
            m.put((String) distances.keySet().toArray()[j], (Double) distances.values().toArray()[j]);
        }
        return LinkedHashMapSorter.sortByValue(m);
    }
    

    public CopyOnWriteArrayList<String> getTagsForLevel(int level) {
        Map<String, Double> currentLevel = setupLevel(sortedDistances, level);
        Iterator itr = currentLevel.keySet().iterator();
        CopyOnWriteArrayList<String> tags = new CopyOnWriteArrayList<>();
        while (itr.hasNext()) {
            String key = (String) itr.next();
            tags.add(key);
        }
        return tags;
    }
    
    public CopyOnWriteArrayList<String> getTagsFromIndex(int index) {
        Map<String, Double> currentLevel = setupLevelFromIndex(sortedDistances, index);
        Iterator itr = currentLevel.keySet().iterator();
        CopyOnWriteArrayList<String> tags = new CopyOnWriteArrayList<>();
        while (itr.hasNext()) {
            String key = (String) itr.next();
            tags.add(key);
        }
        return tags;
    }
    
    public int getIndexOfTag(String tag){
        
        Iterator itr = sortedDistances.keySet().iterator();
        int index = 0;
        while (itr.hasNext()) {
            String key = (String) itr.next();
            if(key.equals(tag)){
                break;
            }
            index++;
        }
        return index;
    }

    public String getClosestTagFromSet(String fromTag, List<String> toTags) throws IOException {

        String closestTag = "";
        double highestSimiliarity = 0;
        for (String toTag : toTags) {
            double similarity = jp.getSimilarity(fromTag, toTag);
            if (similarity > highestSimiliarity) {
                highestSimiliarity = similarity;
                closestTag = toTag;
            }
        }
        return closestTag;
    }
    
    public String getFurthestTagFromSet(String fromTag, List<String> toTags) throws IOException {

        String closestTag = "";
        double lowestSimiliarity = Double.MAX_VALUE;
        for (String toTag : toTags) {
            double similarity = jp.getSimilarity(fromTag, toTag);
            if (similarity < lowestSimiliarity) {
                lowestSimiliarity = similarity;
                closestTag = toTag;
            }
        }
        return closestTag;
    }
}
