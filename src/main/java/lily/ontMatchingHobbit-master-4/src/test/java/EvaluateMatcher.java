import de.uni_mannheim.informatik.dws.ontmatching.demomatcher.DemoMatcherString;
import de.uni_mannheim.informatik.dws.ontmatching.demomatcher.DemoMatcherURL;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.Evaluation;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.benchmarks.Benchmark;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.benchmarks.BenchmarkRepository;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.resultswriter.OnlyCsvWriter;
import de.uni_mannheim.informatik.dws.ontmatching.demomatcher.DemoMatcherJena;
import org.apache.jena.sparql.engine.ref.Eval;

import java.net.MalformedURLException;


public class EvaluateMatcher {
    
    public static void main(String[] args) throws MalformedURLException{
        Evaluation.setResultsWriter(new OnlyCsvWriter());
       Evaluation.run(SPIM_EVA.SPIMBENCH.Default, new DemoMatcherURL());
//        Evaluation.run(BenchmarkRepository.LinkDiscovery.Default, new DemoMatcherURL());
//        Evaluation.run(BenchmarkRepository.Anatomy.Default, new DemoMatcherURL());
//        Evaluation.run(BenchmarkRepository.Phenotype.V2017.DOID_ORDO, new DemoMatcherURL());
//        Evaluation.run(BenchmarkRepository.Conference.V1, new DemoMatcherJena());
        // Evaluation.run(BenchmarkRepository.Largebio.V2016.FMA_NCI_WHOLE, new DemoMatcherURL());
        // Evaluation.run(BenchmarkRepository.LinkDiscovery.Default, new DemoMatcherJena());
    }
    
    
}
