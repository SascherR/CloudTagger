package cloudtagger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 * @author Sascha Rasler
 */
public class CloudTaggerMain {

    /**
     * @param args the command line arguments
     */
    private static ArrayList<ImageModel> loadedImages;

    public static void main(String[] args) throws IOException {

        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            Logger.getLogger(CloudUI.class.getName()).log(Level.SEVERE, null, ex);
        }
        File tagFile = new File("res/tags.txt");
        TagParser tp = new TagParser(tagFile);
        ArrayList<String> importTags = tp.getTagList();
        CloudUI cloudUI = new CloudUI();
        //cloudUI.injectTags(importTags);
        cloudUI.init();
        loadedImages = cloudUI.getLoadedImages();
        if (!loadedImages.isEmpty()) {
            System.out.println(loadedImages.get(0).getImageUrl());
        }
    }
}
