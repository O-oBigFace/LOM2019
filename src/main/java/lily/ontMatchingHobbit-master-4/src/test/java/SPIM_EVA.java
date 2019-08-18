import de.uni_mannheim.informatik.dws.ontmatching.demomatcher.DemoMatcherURL;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.Evaluation;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.benchmarks.Benchmark;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.benchmarks.BenchmarkRepository;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.benchmarks.HobbitBenchmark;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.resultswriter.OnlyCsvWriter;
import de.uni_mannheim.informatik.dws.ontmatching.demomatcher.DemoMatcherJena;
import java.net.MalformedURLException;


public class SPIM_EVA extends BenchmarkRepository{

    /**
     * HOBBIT SPIMBENCH.
     * In this track two benchmark generators are proposed to deal with creative work matching.
     * This new track is based on the HOBBIT platform and it requires to follow different intructions from the SEALS-based tracks.
     */

    public static class SPIMBENCH {
        /** The default(small) HOBBIT SPIMBENCH Task*/
        public static Benchmark Default = new HobbitBenchmark("file:///E:\\Contest\\OAEI_SPIMBENCH\\ontMatchingHobbit-master-4\\SPIMBENCH_small\\SPIMBENCH_small.tar.gz", "SPIMBENCH", "Tbox1.nt", "Tbox2.nt", "refalign.rdf");
    }
}
