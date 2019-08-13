package lily.tool.strsimilarity;

import edu.mit.jwi.IRAMDictionary;
import edu.mit.jwi.RAMDictionary;
import edu.mit.jwi.data.ILoadPolicy;
import edu.mit.jwi.item.*;

import java.io.File;
import java.util.List;

/**
 * Created by Fred on 9/24/15.
 */
public class WordnetSim {

    private static IRAMDictionary dict = null;

    public static void init() {
        try {
            // construct the URL to the Wordnet dictionary directory
            File f = new File("dict");
            // construct the dictionary object and open it
            dict = new RAMDictionary(f, ILoadPolicy.NO_LOAD);
            dict.open();
            // now load into memory
            System.out.print("\nLoading Wordnet into memory...");
            long t = System.currentTimeMillis();
            dict.load(true);
            System.out.printf("done (%1d msec)\n", System.currentTimeMillis() - t);
        } catch (Exception ee) {
            ee.printStackTrace();
        }
    }

    public static boolean isSynonymNoun(String wd1, String wd2) {
        if (dict == null)
            init();
        wd1 = wd1.toLowerCase();
        wd2 = wd2.toLowerCase();
        IIndexWord idxWord = dict.getIndexWord(wd1, POS.NOUN);
        if (idxWord == null)
            return false;
        List<IWordID> wds = idxWord.getWordIDs();
        for (IWordID wordID : wds) {
            IWord word = dict.getWord(wordID);
            ISynset synset = word.getSynset();
            // iterate over words associated with the synset
            for (IWord w : synset.getWords())
                if (w.getLemma().toLowerCase().equals(wd2))
                    return true;
        }
        return false;
    }

}
