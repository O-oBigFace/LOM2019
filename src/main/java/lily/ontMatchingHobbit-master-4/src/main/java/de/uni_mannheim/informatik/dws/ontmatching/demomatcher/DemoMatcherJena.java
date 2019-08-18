package de.uni_mannheim.informatik.dws.ontmatching.demomatcher;

import de.uni_mannheim.informatik.dws.ontmatching.matchingbase.OaeiOptions;
import de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi.Mapping;
import de.uni_mannheim.informatik.dws.ontmatching.matchingjena.MatcherJena;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.base.Sys;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DemoMatcherJena extends MatcherJena{
    private static final Logger logger = LoggerFactory.getLogger(DemoMatcherJena.class);
    /*
    Accessing the resources:
    - all files in "src/main/oaei-resources" folder are stored in the current working directory and can be accessed with 
      Files.readAllLines(Paths.get("configuration_oaei.txt"));    
    - all files in "src/main/resources" folder are compiled to the resulting jat and can be accessed with
    getClass().getClassLoader().getResourceAsStream("configuration_jar.txt");
    Accessing :
    */

    private HashMap<String, Integer> map = new HashMap<>();

    @Override
    public Mapping match(OntModel ont1, OntModel ont2, Mapping mapping, Properties p) {

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
                o2mp[targetID].add(tp);
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

        return mapping;
    }
    
    public void matchResources(ExtendedIterator<? extends OntResource> resourceIterOnt1,ExtendedIterator<? extends OntResource> resourceIterOnt2, Mapping mapping) {
        HashMap<String, String> label2URI = new HashMap<>();
        while (resourceIterOnt1.hasNext()) {
            OntResource r = resourceIterOnt1.next();
            label2URI.put(r.getLabel(null), r.getURI());

            System.out.println("bug" + r.getURI());
        }
        while (resourceIterOnt2.hasNext()) {
            OntResource resourceOnto2 = resourceIterOnt2.next();
            String uriOnto1 = label2URI.get(resourceOnto2.getLabel(null));
            if(uriOnto1 != null){
                mapping.add(uriOnto1, resourceOnto2.getURI());
            }
        }
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
}
