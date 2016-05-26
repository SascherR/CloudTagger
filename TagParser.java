package cloudtagger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author Sascha Rasler
 */
public class TagParser {

    private final ArrayList<String> tagList;

    public TagParser(File tagFile) throws FileNotFoundException, IOException {

        //TODO: read from JSON
        tagList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(tagFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                tagList.add(line);
            }
        }
    }

        
    public ArrayList<String> getTagList(){
        return tagList;
    }
}
