package cloudtagger;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.cortical.rest.model.Metric;
import io.cortical.rest.model.Retina;
import io.cortical.rest.model.Term;
import io.cortical.services.Compare;
import io.cortical.services.PosType;
import io.cortical.services.RetinaApis;
import static io.cortical.services.RetinaApis.getInfo;
import io.cortical.services.Retinas;
import io.cortical.services.Terms;
import io.cortical.services.api.client.ApiException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Sascha Rasler
 *
 * see: http://www.cortical.io/resources_apidocumentation.html
 *
 */
public class CorticalQuery {

    private final static String API_KEY = "4db4b860-fee6-11e4-a409-7159d0ac8188";
    private static final String RETINA_NAME = "en_associative";
    private static final String RETINA_IP = "api.cortical.io";
    private final RetinaApis retinaApisInstance;
    private final Compare compareApiInstance;

    public CorticalQuery() throws ApiException {

        //Retinas api = getInfo("api.cortical.io", API_KEY);
        //List<Retina> retinas = api.getAllRetinas();
        retinaApisInstance = new RetinaApis(RETINA_NAME, RETINA_IP, API_KEY);
        compareApiInstance = retinaApisInstance.compareApi();
        //Compare compareApiInstance = retinaApisInstance.compareApi();
    }

    private void compareWords(String exp1, String exp2) throws JsonProcessingException, ApiException {
        System.out.println("Comparing: " + exp1 + " with: " + exp2);
        Metric metric = compareApiInstance.compare(new Term(exp1), new Term(exp2));
        System.out.println(("Metric: Cosine Similarity: " + metric.getCosineSimilarity() + "  Euclidean Distance: "
                + metric.getEuclideanDistance() + "  Jaccard Distance: " + metric.getJaccardDistance()
                + "  Over lappingAll: " + metric.getOverlappingAll() + "  Over lapping Left Right: "
                + metric.getOverlappingLeftRight() + "  Over lapping Right Left: " + metric.getOverlappingRightLeft()
                + "  Size Left: " + metric.getSizeLeft() + "  Size Right: " + metric.getSizeRight()
                + "  Weighted Scoring: " + metric.getWeightedScoring()));
    }

    public double getWordSimilarity(String exp1, String exp2) throws JsonProcessingException, ApiException {
        Metric metric = compareApiInstance.compare(new Term(exp1), new Term(exp2));
        return metric.getWeightedScoring();
    }

    public ArrayList<String> findSimilarTerms(String exp) throws ApiException {
        ArrayList<String> similarTerms = new ArrayList();
        Terms api = retinaApisInstance.termsApi();
        List<Term> terms;
        terms = api.getSimilarTerms(exp, null, PosType.NOUN, true);
        Term first = terms.get(1);
        String test = first.getTerm();
        similarTerms.add(test);
        return similarTerms;
    }
    /*
     public static void main(String[] args) throws Exception {

     CorticalQuery query = new CorticalQuery();

        
     query.compareWords("still-life", "painting");
     //euclideanDistance":0.7701711491442542
     //"weightedScoring": 16.421795206818548
        
         
     ArrayList lala = query.findSimilarTerms("plum");
     System.out.println(lala.get(0));
        
     //System.out.println("" + query.getWordSimilarity("dog", "cat"));
     File tagFile = new File("res/tags.txt");
     TagParser tp = new TagParser(tagFile);
     ArrayList<String> toParseTags = tp.getTagList();
     ArrayList<String> allTags = new ArrayList<>();
     for (String tag : toParseTags) {
     allTags.add(tag);
     }
     JSONBuilder jsonBuilder = new JSONBuilder("distances.txt");
     for (String fromTag : allTags) {
     jsonBuilder.addTag(fromTag);
     for (String toTag : allTags) {
     if (!fromTag.contentEquals(toTag)) {
     jsonBuilder.addDistance(toTag, query.getWordSimilarity(fromTag, toTag));
     }
     }
     jsonBuilder.finalizeTag();
     }
     jsonBuilder.finalizeJson();
     }*/
}
