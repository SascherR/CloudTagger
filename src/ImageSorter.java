package cloudtagger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author Sascha Rasler
 */
public class ImageSorter {

    private final HashMap<String, FxImagePanel> newPanels;
    private final ArrayList<String> selectedTags;

    public ImageSorter() {

        newPanels = new HashMap<>();
        selectedTags = new ArrayList<>();
    }

    public void sortPanels() {

    }

    public void displayImagesForTags(boolean exclusivAnd, String... tags) {

        newPanels.clear();
        HashMap<String, FxImagePanel> toTraverseMap = (HashMap<String, FxImagePanel>) CloudUI.loadedPanels.clone();
        for (String tag : tags) {
            Iterator it = toTraverseMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                FxImagePanel panel = (FxImagePanel) pair.getValue();
                if (panel.containsTag(tag)) {
                    //System.out.println("adding panel: " + panel);
                    newPanels.put((String) pair.getKey(), panel);
                    if (!selectedTags.contains(tag)) {
                        selectedTags.add(tag);
                    }
                }
                it.remove(); // avoids a ConcurrentModificationException
            }
        }
        CloudUI.displayedPanels.clear();
        CloudUI.displayedPanels = (HashMap<String, FxImagePanel>) newPanels.clone();
        CloudUI.refreshPanels();
        //System.out.println("new image panels: " + newPanels);
        //System.out.println("loaded panels: " + CloudUI.loadedPanels);
    }

    public void removeSelectedTag(String tag) {
        if (selectedTags.contains(tag)) {
            selectedTags.remove(tag);
        }
        updateTags();
    }

    private void updateTags() {
        if (selectedTags.isEmpty()) {
            CloudUI.displayedPanels = (HashMap<String, FxImagePanel>) CloudUI.loadedPanels.clone();
            CloudUI.refreshPanels();
        } else {
            String[] transferArray = new String[selectedTags.size()];
            transferArray = selectedTags.toArray(transferArray);
            displayImagesForTags(true, transferArray);
        }
    }
}
