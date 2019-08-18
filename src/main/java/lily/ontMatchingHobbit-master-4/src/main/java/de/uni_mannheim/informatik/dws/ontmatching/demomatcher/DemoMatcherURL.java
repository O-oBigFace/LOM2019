package de.uni_mannheim.informatik.dws.ontmatching.demomatcher;

import de.uni_mannheim.informatik.dws.ontmatching.matchingbase.MatcherURL;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.math.*;
import de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi.Mapping;
import eu.sealsproject.platform.res.domain.omt.IOntologyMatchingToolBridge;
import eu.sealsproject.platform.res.tool.api.ToolType;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;

import java.util.*;

public class DemoMatcherURL extends MatcherURL {

    private static final Logger logger = LoggerFactory.getLogger(DemoMatcherURL.class);
    private static String DIR = System.getProperty("user.dir");
//    private static String RES_FILE = DIR + "/temp/result.xml";
    private static String RES_FILE = "result.rdf";
    private static double EPS = 0.80;
    private HashMap<String, Integer> map = new HashMap<>();


    protected OntModel readOntology(URL url){
        // I would like to use only jena core, but then when sometimes jena arq is on the classpath 
        // it uses their parser which throws errors (it is more strict)
        // thus I decided to directly use arq to load the ontologies
        // check https://stackoverflow.com/questions/3466568/check-if-class-exists-in-java-classpath-without-running-its-static-initializer
        // for checking if riot (jena arq) is on the classpath and change behaviour
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);    
        model.read(url.toString());
        //RDFParser.create()
        //    .source(url.toString())
        //    //.errorHandler(ErrorHandlerFactory.errorHandlerWarn)
        //    .errorHandler((new ErrorHandlerCarryOn()))
        //    .parse(model.getGraph());
        return model;
    }

    public URL match(URL source, URL target, URL inputAlignment) throws Exception {
        //TODO: read the source and target URL and produce an alignment in alignment format ( http://alignapi.gforge.inria.fr/format.html )
        // private static final Logger logger = LoggerFactory.getLogger(DemoMatcherJena.class);
        /*
        Accessing the resources:
        - all files in "src/main/oaei-resources" folder are stored in the current working directory and can be accessed with 
          Files.readAllLines(Paths.get("configuration_oaei.txt"));    
        - all files in "src/main/resources" folder are compiled to the resulting jat and can be accessed with
        getClass().getClassLoader().getResourceAsStream("configuration_jar.txt");
        Accessing :
        */
    

        OntModel ont1 = readOntology(source);
        OntModel ont2 = readOntology(target);

        MappingURL mapping = new MappingURL(ont1.getNsPrefixURI("").substring(0, ont1.getNsPrefixURI("").lastIndexOf("#")), ont2.getNsPrefixURI("").substring(0, ont2.getNsPrefixURI("").lastIndexOf("#")));

        BioMatcher();

        logger.info("Start matching");

        System.out.println("inflate o1...... ");

        ArrayList<String> o1URIs = getAllClassesURIs(ont1);

        System.out.println("inflate o2...... ");
        ArrayList<String> o2URIs = getAllClassesURIs(ont2);

        int m = o1URIs.size();
        int n = o2URIs.size();
        System.out.println("m is " + m + ";n is " + n);

//        MappingFile mapFile = new MappingFile();
        String sourceBaseURI = getOntBaseURI(ont1);
        String targetBaseURI = getOntBaseURI(ont2);

        String sourceCategory = getCategory(sourceBaseURI);
        String targetCategory = getCategory(targetBaseURI);


        int MAX_SIZE = 600000;
//        MapRecord[] mapRecords = new MapRecord[30000];// num of mappings
        ArrayList[] o1mp = new ArrayList[MAX_SIZE + 5];
        ArrayList[] o2mp = new ArrayList[MAX_SIZE + 5];


        for (int i = 0; i < MAX_SIZE; i++) {
            o1mp[i] = new ArrayList();
            o2mp[i] = new ArrayList();
        }

        System.out.println("initial process is done... ");

        for (int i = 0; i < m; i++) {
            String URI1 = o1URIs.get(i);
            String tp = ont1.getOntClass(URI1).getURI();
            String a = ont1.getOntClass(URI1).getLocalName();
            String a1 = a.toLowerCase().replaceAll("-", "_").replaceAll("_", " ");

            Integer sourceID = map.get(a1 + "__" + sourceCategory);
            if (sourceID != null) {
                o1mp[sourceID.intValue()].add(tp);
                //System.out.println(sourceID.intValue() + " : [ " + a + " ]");
            }
        }
        System.out.println("First Big HashMap(ArrayList) is constructed done...");

        for (int j = 0; j < n; j++) {
            String URI2 = o2URIs.get(j);
            String tp = ont2.getOntClass(URI2).getURI();
            String b = ont2.getOntClass(URI2).getLocalName();
            String b1 = b.toLowerCase().replaceAll("-", "_").replaceAll("_", " ");

            Integer targetID = map.get(b1 + "__" + targetCategory);
            if (targetID != null) {
                o2mp[targetID.intValue()].add(tp);
                //System.out.println(targetID.intValue() + " : [ " + b + " ]");
            }
        }

        System.out.println("Second Big HashMap(ArrayList) is constructed done...");

        System.out.println("Now start matching alg with outer resource...");

        int mappingNum = 0;
        for (int i = 0; i < MAX_SIZE; i++) {
            // 常量级的时间复杂度，所以不大
            for (int x1 = 0; x1 < o1mp[i].size(); x1++) {
                String a = (String) o1mp[i].get(x1);
                for (int x2 = 0; x2 < o2mp[i].size(); x2++) {
                    String b = (String) o2mp[i].get(x2);
//                    mapRecords[mappingNum] = new MapRecord();
//                    mapRecords[mappingNum].sourceLabel = new String(a);
//                    mapRecords[mappingNum].targetLabel = new String(b);
//                    mapRecords[mappingNum].similarity = 1.0;
//                    mapRecords[mappingNum].relationType = 0;
//                    mappingNum++;
                    mapping.add(a ,b);
//                     System.out.println("found a mapping : " + mappingNum + " : " + a + " : " + b);
                        logger.info("found a mapping : " + mappingNum + " : " + a + " : " + b);
                }
            }
        }

//        mapFile.setBaseURI(sourceBaseURI, targetBaseURI);
//        mapFile.save2rdfByTyz(mapRecords, lilyFilePath, mappingNum);

        System.out.println("matching process by tyz in largebio is done !");
        logger.info("Finished matching");

        // mapping.set

        mapping.ended();

        return mapping.toURL();
    }
        


    public static String getOntBaseURI(OntModel m) {
        String uri = null;
        // 如果有明确定义的base URI，可以这样获得
        uri = m.getNsPrefixURI("");
        if (uri == null) {
            // 如果上面的方法无法得到base URI,就需要用其它的办法来得到
            // 这里用的方法是利用concept来判断
            uri = getPrimaryBaseURI(m);
        }
        return uri;
    }

    public static String getPrimaryBaseURI(OntModel m) {
        ArrayList uriList = new ArrayList();
        ArrayList timeList = new ArrayList();
        int pos = 0;
        int time = 0;
        int total = 0;

        Iterator i = m.listClasses();
        int num = 0;
        while (i.hasNext()) {
            OntClass c = (OntClass) i.next();
            String s;
            // If the class is not anonymous, output it.
            if (!c.isAnon()) {
                total++;
                s = c.getNameSpace();
                if (!uriList.contains(s)) {
                    uriList.add(num, s);
                    num++;
                }
                time = 1;
                pos = uriList.indexOf(s);
                if (!timeList.isEmpty() && timeList.size() > pos) {
                    time = ((Integer) timeList.get(pos)).intValue();
                    time++;
                    timeList.set(pos, time);
                } else {
                    timeList.add(pos, time);
                }

                if (total >= 20) {
                    break;
                }
            }
        }

        // 得到隐含的baseURI
        pos = 0;
        int max = 0;
        for (Iterator j = timeList.iterator(); j.hasNext(); ) {
            int value = ((Integer) j.next()).intValue();
            if (max <= value) {
                pos = timeList.indexOf(value);
                max = value;
            }
        }
        String baseURI = null;
        baseURI = (String) uriList.get(pos);
        return baseURI;
    }
    
    private static int getConceptMaxNum(String uri) {
        if (uri.contains("snomed")) {
            return 122464;
        } else if (uri.contains("nci")) {
            return 66724;
        }
        return Integer.MAX_VALUE - 10;
    }

    public static ArrayList<String> getAllClassesURIs(OntModel m) {
        ArrayList<String> result = new ArrayList<>();
        ExtendedIterator<OntClass> i = m.listClasses();
        int count = 0;
        while (i.hasNext()) {
            OntClass c_temp = i.next();
            if (!c_temp.isAnon() && c_temp.isClass()) {
                count++;
                String uri = c_temp.getURI();

                // System.out.println(count + " -> " + uri);
                result.add(uri);

                if(count == getConceptMaxNum(uri)){
                    return result;
                }
            }
        }
        return result;
    }

    public void BioMatcher() {
        try {
            String DIR =  System.getProperty("user.dir");
            List<String> tmp = Files.readAllLines(Paths.get(DIR + "/src/main/oaei-resources/UMLS.lexicon"), Charset.forName("GBK"));
            // List<String> tmp = Files.readAllLines(Paths.get("UMLS.lexicon"), Charset.forName("GBK"));
//            String FILE_BUFF = "UMLS.lexicon";
            System.out.println("read the lexicon successfully......");
//            BufferedReader inStream = new BufferedReader(new FileReader(FILE_BUFF));
            for (String line:tmp){
                String[] words = line.split("\t");
                int id = Integer.parseInt(words[0]);
                this.map.put(words[2] + "__" + words[1], id);
            }
//            inStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("The Big HashMap is constructed done... ");
    }


    private String getCategory(String uri) {
        if (uri.contains("fma")) {
            return "FMA";
        } else if (uri.contains("snomed")) {
            return "SNOMEDCT";
        } else if (uri.contains("nci")) {
            return "NCI";
        }
        return "null";
    }

    class MappingURL{
        private String ans = "";
        MappingURL(String a, String b){
            ans = "<?xml version='1.0' encoding='utf-8'?>\n" +
                    "<rdf:RDF xmlns='http://knowledgeweb.semanticweb.org/heterogeneity/alignment'\n" +
                    "\t xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#' \n" +
                    "\t xmlns:xsd='http://www.w3.org/2001/XMLSchema#' \n" +
                    "\t alignmentSource='AgreementMakerLight'>\n" +
                    "\n" +
                    "<Alignment>\n" +
                    "\t<xml>yes</xml>\n" +
                    "\t<level>0</level>\n" +
                    "\t<type>??</type>\n" +
                    "\t<onto1>" + a + "</onto1>\n" +
                    "\t<onto2>" + b + "</onto2>\n" +
                    "\t<uri1>" + a +"</uri1>\n" +
                    "\t<uri2>" + b + "</uri2>\n";
        }
        public void add(String a, String b){
                String tmp = "\t<map>\n" +
                        "\t\t<Cell>\n" +
                        "\t\t\t<entity1 rdf:resource=\"" + a + "\"/>\n" +
                        "\t\t\t<entity2 rdf:resource=\"" + b + "\"/>\n" +
                        "\t\t\t<measure rdf:datatype=\"http://www.w3.org/2001/XMLSchema#float\">1.0</measure>\n" +
                        "\t\t\t<relation>=</relation>\n" +
                        "\t\t</Cell>\n" +
                        "\t</map>\n";
                ans += tmp;
        }
        public void ended(){
            ans += "</Alignment>\n" +
                    "</rdf:RDF>";
        }
        public URL toURL(){
//            URL res = new URL(ans);
//            return res;
//            File alignmentFile = File.createTempFile("alignment", ".rdf");
//            try (BufferedWriter out = new BufferedWriter(new FileWriter(alignmentFile))) {
//                out.write(ans);
//            }
//            return alignmentFile.toURI().toURL();

            // try {
            //     File tat = File.createTempFile("result", "rdf");
            // }
            // catch (Exception e){
            //     e.printStackTrace();
            // }
            try {
                File path = new File(DIR,RES_FILE);
                WriteStringToFile(RES_FILE, ans);
//                File path = new File(System.getProperty("user.dir") + "/temp/refalign.rdf");
//                URL path = new URL("file:///"+RES_FILE);
                return path.toURI().toURL();
            }
            catch (Exception e){
                e.printStackTrace();
                return null;
            }
        }
    }

    public void WriteStringToFile(String filePath, String tmp) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                try {
                    file.createNewFile();
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
            PrintStream ps = new PrintStream(new FileOutputStream(file));
            ps.print(tmp);
//            ps.append(tmp);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
