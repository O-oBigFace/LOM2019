///************************************************
// * Source code information
// * -----------------------
// * Original author	 Peng Wang, School of Computer Science & Eng., Southeast University
// * Author email      pwangseu@gmail.com
// * Web               http://ontomapping.googlepages.com
// * Created			 2013-09-03
// * Filename          IOM.java
// * Version           0.1
// *
// * Last modified on  2013-09-03
// *               by  Peng Wang
// * -----------------------
// * Functions describe:
// * 针对OAEI2013的instance matching task的解决方法
// ***********************************************/
//
//package lily.onto.mapping.method;
//
//import com.hp.hpl.jena.ontology.Individual;
//import lily.onto.mapping.evaluation.EvaluateMapping;
//import lily.tool.datastructure.MapRecord;
//import lily.tool.datastructure.TextDes;
//import lily.tool.datastructure.Word;
//import lily.tool.filter.SimpleFilter;
//import lily.tool.mappingfile.MappingFile;
//import lily.tool.parallelcompute.parfor;
//import lily.tool.strsimilarity.StrEDSim;
//import lily.tool.textprocess.DelStopWords;
//import lily.tool.textprocess.SplitWords;
//import lily.tool.textsimilarity.TextDocSim;
//import org.w3c.dom.Document;
//import org.w3c.dom.Element;
//import org.w3c.dom.NodeList;
//
//import javax.xml.parsers.DocumentBuilder;
//import javax.xml.parsers.DocumentBuilderFactory;
//import java.io.*;
//import java.util.*;
//
////import com.hp.hpl.jena.tdb.store.Hash;
//
//@SuppressWarnings("unchecked")
//public class IOM {
//
//    /**base URI**/
//    public String sourceBaseURI;
//    public String targetBaseURI;
//    public String RDFBaseURI;
//    public String dbpediaBaseURI;
//
//    /**********本体文件和映射结果文件相关参数**********/
//    public String sourceOntFile;// 源和目标本体的文件
//    public String targetOntFile;
//    public String outputOntFile;
//    public String refalignFile;// 基准映射结果的文件
//    public String resourceTxtFile;
//
//    /**实例数目**/
//    public int sourceInsNum;
//    public int targetInsNum;
//
//    /**实例名**/
//    public ArrayList sourceInsName;
//    public ArrayList targetInsName;
//    public HashMap sInsName2Pos;
//    public HashMap tInsName2Pos;
//    public ArrayList sourceLabelInsName;
//    public ArrayList targetLabelInsName;
//    public ArrayList resourceRange;
//
//    public Individual[] sourceFullInsName;
//    public Individual[] targetFullInsName;
//
//    /**属性名与顺序**/
//    HashMap<String, Integer> propOrder;
//    HashMap<String, Double> propWeight;
//
//    /**三元组**/
//    public ArrayList sourceTriples;
//    public ArrayList targetTriples;
//
//    /**实例相关三元组**/
//    public ArrayList sourceInsTpl;
//    public ArrayList targetInsTpl;
//
//    /**实例的父子**/
//    public HashMap sourceName2Fathers;
//    public HashMap sourceName2Sons;
//    public HashMap targetName2Fathers;
//    public HashMap targetName2Sons;
//
//    /**描述文档**/
//    public TextDes[] sourceInsBasicTextDes;
//    public TextDes[] targetInsBasicTextDes;
//    public TextDes[] sourceInsFatherTextDes;
//    public TextDes[] targetInsFatherTextDes;
//    public TextDes[] sourceInsSonTextDes;
//    public TextDes[] targetInsSonTextDes;
//    public TextDes[] sourceInsObjTextDes;
//    public TextDes[] targetInsObjTextDes;
//    public TextDes[] sourceInsPreTextDes;
//    public TextDes[] targetInsPreTextDes;
//
//    /**谓词频率**/
//    public HashMap <String, Integer> sourcePreFrequence;
//    public HashMap <String, Integer> targetPreFrequence;
//
//    /**日期词典**/
//    public HashMap <String, String> dateDict;
//    public ArrayList <String> monthList;
//
//    /**相似矩阵**/
//    public double[][] basicSimMatrix;
//    public double[][] hierarchySimMatrix;
//    public double[][] normalSimMatrix;
//    public double[][] sigmaSimMatrix;
//
//    /**相似矩阵权重**/
//    double bmWeight;
//    double hmWeight;
//    double nmWeight;
//
//    /**实例消解结果**/
//    ArrayList <MapRecord> sourceSameIns;
//    ArrayList <MapRecord> targetSameIns;
//
//    /**映射结果**/
//    public MapRecord[] matchResult;
//    public int matchNum;
//
//    /**参数**/
//    public double wLocalName = 1.0; //基本描述文档local name初始权重
//    public double wLabel = 1.0; //基本描述文档label初始权重
//    public double wComment = 0.8; //基本描述文档comments初始权重
//    public double lowboundThreshold = 0.0; //相似阀值下限 default=0.105
//
//    /**当前本体标记**/
//    public boolean sourceFlag;
//
//    /**结果评价**/
//    public double precision;
//    public double recall;
//    public double f1Measure;
//
//
//    public static void main(String[] args) {
//        new IOM().run();
////		new IOM().tuningRun();
//        System.out.println("end");
//
//    }
//
//    public void run() {
//        /** 1.读本体、解析本体、初始化参数 **/
//        init();
//        parseOnt();
//        /** 2.相似度计算 **/
//        calBaseSimilarity_combined();
//        sigmaSimMatrix = basicSimMatrix;
//        //calculateSimilarity();
//        /**进行实例消解**/
//        //ontDisambiguation();
//        /**3.抽取匹配**/
//        extractMatching();
//        /**5.保存结果 **/
//        outputMatch();
//        /**4.映射结果评估 **/
//        //evaluate();
//    }
//
//    public void run2() {
//        init();
//        HashMap source, target;
//        source = parseAuthorRecSource();
//        target = parseAuthorRecTarget();
//        instStat[] sourceInstStats = new instStat[sourceInsNum];
//        instStat[] targetInstStats = new instStat[targetInsNum];
//        for (int i = 0; i < sourceInsNum; ++i)
//            sourceInstStats[i] = (instStat)source.get(sourceInsName.get(i));
//        for (int i = 0; i < targetInsNum; ++i)
//            targetInstStats[i] = (instStat)target.get(targetInsName.get(i));
//        /*instStat testA = (instStat)source.get("be97a523-c3fa-42b7-acbb-806008eec91f");
//        instStat testB = (instStat)target.get("40e5f2b6-160f-44fa-b60d-0c1e1c27235d");
//        double tmp = 1.0;
//        tmp *= strArrSimAllowIncluding(testA.strProp, testB.strProp);
//        tmp *= numArrSim(testA.numProp, testB.numProp);*/
//        source = null; //trigger GC
//        target = null;
//        calcAuthorRecSilimarity(sourceInstStats, targetInstStats);
//        extractMatching();
//        outputMatch();
//        //evaluateFromRDF();
//        //evaluate();
//    }
//
//    public void tuningRun(){
//        /** 1.读本体、解析本体、初始化参数 **/
//        init();
//        parseOnt();
//        /** 2.相似度计算 **/
//        calculateSimilarity();
//        /**3.参数Turning**/
//        paraTuning();
//    }
//
//    /**参数调谐**/
//    private void paraTuning() {
//        /**
//         * 需要调节的主要参数：
//         * 1. lowboundThreshold:匹配选择阀值，范围0.005-0.3,步长0.005
//         * 2. bmWeight:基本相似矩阵，范围0.1-1.0，步长0.005
//         * 3. hmWeight：层次相似矩阵，范围0.1-1.0，步长0.005
//         * 4. nmWeight：非层次相似矩阵，范围0.1-1.0，步长0.005
//         */
//        double maxP=0;
//        double maxR=0;
//        double maxF=0;
//        for (double a2 = 0.1; a2<=1.0; a2+=0.1)
//            for (double a3 = 0.1; a3<=1.0; a3+=0.1)
//                for (double a4 = 0.1; a4<=1.0; a4+=0.1)
//                    for (double a1 = 0.005; a1<=0.3; a1+=0.05){
//                        lowboundThreshold = a1;
//                        bmWeight = a2;
//                        hmWeight = a3;
//                        nmWeight = a4;
//
//                        //重新合并相似度
//                        sigmaSimMatrix = new double[sourceInsNum][targetInsNum];
//                        double totalWeight = bmWeight+hmWeight+nmWeight;
//                        for (int i=0;i<sourceInsNum;i++){
//                            for (int j=0;j<targetInsNum;j++){
//                                sigmaSimMatrix[i][j] = (bmWeight*basicSimMatrix[i][j]+hmWeight*hierarchySimMatrix[i][j]+nmWeight*normalSimMatrix[i][j])/totalWeight;
//                            }
//                        }
//                        /*引入resource限制*/
//                        for (int i=0; i<sourceInsNum; i++) {
//                            for (int j=0;j<targetInsNum;j++) {
//                                if (sourceLabelInsName.contains(sourceInsName.get(i)) || targetLabelInsName.contains(targetInsName.get(j))){ //!resourceRange.contains(sourceInsName.get(i)) ||
//                                    sigmaSimMatrix[i][j]=0.0;
//                                }
//                            }
//                        }
//                        //重新提取
//                        extractMatching();
//                        //重新评估
//                        evaluate();
//
//                        //记录当前最大值
//                        if (f1Measure>maxF){
//                            maxP=precision;
//                            maxR=recall;
//                            maxF=f1Measure;
//
//                            System.out.println("best: P="+maxP+" R="+maxR+" F="+maxF+" Parameters: threshold="+a1+"bmWeight="+a2+"hmWeight="+a3+"nmWeight="+a4);
//                        }
//
//                    }
//
//    }
//
//    /**参数初始化**/
//    private void init() {
//        sourceBaseURI = "http://islab.di.unimi.it/imoaei2015#"; //http://dbpedia.org/resource/
//        targetBaseURI = "http://islab.di.unimi.it/imoaei2015#"; //http://www.instancematching.org/
//        RDFBaseURI = "http://www.w3.org/2000/01/rdf-schema#";
//        dbpediaBaseURI = "http://dbpedia.org/ontology";
//
//        sourceOntFile = "dataset/OAEI2015/IM/author_dis_sandbox/ontoA_scomb.tri";  //./dataset/OAEI2013/original.rdf
//        targetOntFile = "dataset/OAEI2015/IM/author_dis_sandbox/ontoB_scomb.tri"; //./dataset/OAEI2013/contest/testcase05/training/training_transNew.rdf
//        //refalignFile = "dataset/OAEI2015/IM/author_dis_sandbox/refalign.tsv"; //./dataset/OAEI2013/contest/testcase05/training/refined_mappings.tsv
//        //resourceTxtFile = "dataset/OAEI2015/IM/author_dis_sandbox/resources.txt"; //./dataset/OAEI2013/resources.txt
//        outputOntFile = "dataset/OAEI2015/IM/author_dis_mainbox/out.tsv";
//
//        /*
//        sourceOntFile = "dataset/OAEI2015/IM/author_rec_mainbox/ontoA.owl";
//        targetOntFile = "dataset/OAEI2015/IM/author_rec_mainbox/ontoB.owl";
//        refalignFile = "dataset/OAEI2015/IM/author_rec_sandbox/refalign.rdf";
//        //resourceTxtFile = "dataset/OAEI2015/IM/author_rec_mainbox/resources.txt";
//        outputOntFile = "dataset/OAEI2015/IM/author_rec_mainbox/out.tsv";
//        */
//        propOrder = new HashMap();
//        propOrder.put("title", 0);
//        propOrder.put("venue", 1);
//        propOrder.put("publisher", 2);
//        propOrder.put("year", 3);
//        propOrder.put("citations", 4);
//        //propOrder.put("name", 5);
//        propWeight = new HashMap();
//        propWeight.put("title", 1.0);
//        propWeight.put("venue", 0.0);
//        propWeight.put("publisher", 0.0);
//        propWeight.put("year", 0.0);
//        propWeight.put("citations", 0.0);
//
//        sourceTriples = new ArrayList();
//        targetTriples = new ArrayList();
//
//        sourceInsName = new ArrayList();
//        targetInsName = new ArrayList();
//
//        sourceLabelInsName = new ArrayList();
//        targetLabelInsName = new ArrayList();
//
//        sourceInsTpl = new ArrayList();
//        targetInsTpl = new ArrayList();
//
//        sourceName2Fathers = new HashMap();
//        sourceName2Sons = new HashMap();
//        targetName2Fathers = new HashMap();
//        targetName2Sons = new HashMap();
//
//        sourcePreFrequence = new HashMap();
//        targetPreFrequence = new HashMap();
//
//        resourceRange = new ArrayList();
//
//        sourceSameIns = new ArrayList();
//        targetSameIns = new ArrayList();
//
//        setDateDict();
//    }
//
//    /**读取和解析本体控制函数**/
//    private void parseOnt () {
//        System.out.print("Parsing ontologies ....");
//        sourceInsNum = parseAnOntology(sourceOntFile, sourceTriples, sourceBaseURI, sourceInsName, sourceLabelInsName, sourceInsTpl, sourceName2Fathers, sourceName2Sons, sourcePreFrequence);
//        targetInsNum = parseAnOntology(targetOntFile, targetTriples, targetBaseURI, targetInsName, targetLabelInsName, targetInsTpl, targetName2Fathers, targetName2Sons, targetPreFrequence);
//        //for (int i = 0; i != sourceInsNum; ++i)
//        //System.out.println(sourceInsName.get(i));
//        //readResourceTxtFile(resourceTxtFile, resourceRange);
//        System.out.println("done!");
//    }
//
//    private class instStat {
//        public String[] strProp;
//        public double[] numProp;
//    }
//
//    HashMap parseAuthorRecSource() {
//        String tmp, tmp2, tmp3;
//        String[] sarr;
//        HashMap arr;
//        HashMap father = new HashMap<String, String>();
//        HashMap store = new HashMap<String, HashMap<String, String[]>>();
//        HashMap store2 = new HashMap<String, instStat>();
//        try {
//            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//            DocumentBuilder db = dbf.newDocumentBuilder();
//            Document document = db.parse(new File(sourceOntFile));
//            NodeList list = document.getElementsByTagName("Ontology").item(0).getChildNodes();
//            NodeList list2;
//            Element element;
//            for (int i = 0; i < list.getLength(); ++i) {
//                switch (list.item(i).getNodeName()) {
//                    case "ObjectPropertyAssertion":
//                        element = (Element) list.item(i);
//                        if (element.getElementsByTagName("ObjectProperty").item(0).getAttributes().getNamedItem("IRI").getNodeValue().contains("author_of")) {
//                            father.put(
//                                    element.getElementsByTagName("NamedIndividual").item(1).getAttributes().getNamedItem("IRI").getNodeValue(),
//                                    element.getElementsByTagName("NamedIndividual").item(0).getAttributes().getNamedItem("IRI").getNodeValue()
//                            );
//                        }
//                        break;
//                    case "DataPropertyAssertion":
//                        element = (Element) list.item(i);
//                        tmp2 = element.getElementsByTagName("NamedIndividual").item(0).getAttributes().getNamedItem("IRI").getNodeValue();
//                        tmp = (String) father.get(tmp2);
//                        if (tmp == null) {
//                            tmp = tmp2;
//                            //break;
//                        }
//                        arr = (HashMap) store.get(tmp);
//                        if (arr == null)
//                            arr = new HashMap();
//                        sarr = (String[]) arr.get(tmp2);
//                        if (sarr == null)
//                            sarr = new String[propOrder.size()];
//                        tmp3 = element.getElementsByTagName("DataProperty").item(0).getAttributes().getNamedItem("IRI").getNodeValue();
//                        tmp3 = getLocalName(tmp3, sourceBaseURI);
//                        if (propOrder.get(tmp3) == null)
//                            continue;
//                        sarr[(int) propOrder.get(tmp3)] = element.getElementsByTagName("Literal").item(0).getTextContent();
//                        arr.put(tmp2, sarr);
//                        store.put(tmp, arr);
//                        break;
//                    case "ClassAssertion":
//                        element = (Element) list.item(i);
//                        if (element.getElementsByTagName("Class").item(0).getAttributes().getNamedItem("IRI").getNodeValue().contains("#Person")) {
//                            sourceInsName.add(element.getElementsByTagName("NamedIndividual").item(0).getAttributes().getNamedItem("IRI").getNodeValue());
//                        }
//                        break;
//                }
//            }
//            sourceInsNum = sourceInsName.size();
//            //开始统计
//            //0 name
//            //0 active_from
//            //1 active_to
//            //2 publication_count
//            //3 sum_of_citations
//            //4 h_index (ign)
//            //HashMap tmper = (HashMap)store.get("c253de29-ef8a-486e-8078-0d13e080f68e");
//            for (Iterator iter = store.entrySet().iterator(); iter.hasNext();) {
//                Map.Entry entry = (Map.Entry)iter.next();
//                instStat iInst = new instStat();
//                iInst.strProp = new String[1];
//                iInst.numProp = new double[4];
//                iInst.numProp[0] = Integer.MAX_VALUE;
//                ArrayList citationList = new ArrayList();
//                for (Iterator iter2 = ((HashMap)entry.getValue()).entrySet().iterator(); iter2.hasNext();) {
//                    Map.Entry entry2 = (Map.Entry)iter2.next();
//                    if (((String)entry2.getKey()).equals((String)entry.getKey())) {
//                        iInst.strProp[0] = ((String[])entry2.getValue())[propOrder.get("name")];
//                    } else {
//                        iInst.numProp[2] += 1;
//                        tmp = ((String[])entry2.getValue())[propOrder.get("citations")];
//                        if (tmp != null) {
//                            tmp = tmp.replaceAll("\\D", "");
//                            iInst.numProp[3] += Integer.valueOf(tmp);
//                            citationList.add(Integer.valueOf(tmp));
//                        }
//                        tmp = ((String[])entry2.getValue())[propOrder.get("year")];
//                        if (tmp != null) {
//                            tmp = tmp.replaceAll("\\D", "");
//                            iInst.numProp[0] = Math.min(iInst.numProp[0], Integer.valueOf(tmp));
//                            iInst.numProp[1] = Math.max(iInst.numProp[1], Integer.valueOf(tmp));
//                        }
//                    }
//                }
//                if (iInst.numProp[0] == Integer.MAX_VALUE)
//                    iInst.numProp[0] = 0;
//                //iInst.numProp[4] = getHindex(citationList);
//                store2.put(entry.getKey(), iInst);
//                //if (entry.getKey().equals("c253de29-ef8a-486e-8078-0d13e080f68e"))
//                //continue;
//            }
//            return store2;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    HashMap parseAuthorRecTarget() {
//        HashMap<String, Integer> propOrder2 = new HashMap();
//        propOrder2.put("name", 0);
//        propOrder2.put("active_from", 1);
//        propOrder2.put("active_to", 2);
//        //propOrder2.put("h_index", 5);
//        propOrder2.put("publication_count", 3);
//        propOrder2.put("sum_of_citations", 4);
//        String tmp, tmp2, tmp3;
//        String[] sarr;
//        HashMap arr;
//        HashMap father = new HashMap<String, String>();
//        HashMap store = new HashMap<String, HashMap<String, String[]>>();
//        HashMap store2 = new HashMap<String, instStat>();
//        try {
//            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//            DocumentBuilder db = dbf.newDocumentBuilder();
//            Document document = db.parse(new File(targetOntFile));
//            NodeList list = document.getElementsByTagName("Ontology").item(0).getChildNodes();
//            NodeList list2;
//            Element element;
//            for (int i = 0; i < list.getLength(); ++i) {
//                switch (list.item(i).getNodeName()) {
//                    case "ObjectPropertyAssertion":
//                        element = (Element) list.item(i);
//                        if (element.getElementsByTagName("ObjectProperty").item(0).getAttributes().getNamedItem("IRI").getNodeValue().contains("author_of")) {
//                            father.put(
//                                    element.getElementsByTagName("NamedIndividual").item(1).getAttributes().getNamedItem("IRI").getNodeValue(),
//                                    element.getElementsByTagName("NamedIndividual").item(0).getAttributes().getNamedItem("IRI").getNodeValue()
//                            );
//                        }
//                        break;
//                    case "DataPropertyAssertion":
//                        element = (Element) list.item(i);
//                        tmp2 = element.getElementsByTagName("NamedIndividual").item(0).getAttributes().getNamedItem("IRI").getNodeValue();
//                        tmp = (String) father.get(tmp2);
//                        if (tmp == null) {
//                            tmp = tmp2;
//                            //break;
//                        }
//                        arr = (HashMap) store.get(tmp);
//                        if (arr == null)
//                            arr = new HashMap();
//                        sarr = (String[]) arr.get(tmp2);
//                        if (sarr == null)
//                            sarr = new String[propOrder2.size()];
//                        tmp3 = element.getElementsByTagName("DataProperty").item(0).getAttributes().getNamedItem("IRI").getNodeValue();
//                        tmp3 = getLocalName(tmp3, targetBaseURI);
//                        if (propOrder2.get(tmp3) == null)
//                            continue;
//                        sarr[(int) propOrder2.get(tmp3)] = element.getElementsByTagName("Literal").item(0).getTextContent();
//                        arr.put(tmp2, sarr);
//                        store.put(tmp, arr);
//                        break;
//                    case "ClassAssertion":
//                        element = (Element) list.item(i);
//                        if (element.getElementsByTagName("Class").item(0).getAttributes().getNamedItem("IRI").getNodeValue().contains("#Person")) {
//                            targetInsName.add(element.getElementsByTagName("NamedIndividual").item(0).getAttributes().getNamedItem("IRI").getNodeValue());
//                        }
//                        break;
//                }
//            }
//            targetInsNum = targetInsName.size();
//            //开始统计
//            //0 name
//            //0 active_from
//            //1 active_to
//            //2 publication_count
//            //3 sum_of_citations
//            //4 h_index (ign)
//            for (Iterator iter = store.entrySet().iterator(); iter.hasNext();) {
//                Map.Entry entry = (Map.Entry)iter.next();
//                instStat iInst = new instStat();
//                iInst.strProp = new String[1];
//                iInst.numProp = new double[4];
//                for (Iterator iter2 = ((HashMap)entry.getValue()).entrySet().iterator(); iter2.hasNext();) {
//                    Map.Entry entry2 = (Map.Entry)iter2.next();
//                    if (((String)entry2.getKey()).equals((String)entry.getKey())) {
//                        iInst.strProp[0] = ((String[])entry2.getValue())[propOrder2.get("name")];
//                    } else {
//                        tmp = ((String[])entry2.getValue())[propOrder2.get("active_from")];
//                        if (tmp != null)
//                            iInst.numProp[0] = Integer.valueOf(tmp.replaceAll("\\D", ""));
//                        tmp = ((String[])entry2.getValue())[propOrder2.get("active_to")];
//                        if (tmp != null)
//                            iInst.numProp[1] = Integer.valueOf(tmp.replaceAll("\\D", ""));
//                        /*tmp = ((String[])entry2.getValue())[propOrder2.get("h_index")];
//                        if (tmp != null)
//                            iInst.numProp[4] = Integer.valueOf(tmp.replaceAll("\\D", ""));*/
//                        tmp = ((String[])entry2.getValue())[propOrder2.get("publication_count")];
//                        if (tmp != null)
//                            iInst.numProp[2] = Integer.valueOf(tmp.replaceAll("\\D", ""));
//                        tmp = ((String[])entry2.getValue())[propOrder2.get("sum_of_citations")];
//                        if (tmp != null)
//                            iInst.numProp[3] = Integer.valueOf(tmp.replaceAll("\\D", ""));
//                    }
//                }
//                store2.put(entry.getKey(), iInst);
//            }
//            return store2;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    private int getHindex(ArrayList citationList) {
//        Collections.sort(citationList);
//        for (int i = 0 ; i < citationList.size(); ++i)
//            if ((int)citationList.get(citationList.size() - i - 1) < i + 1)
//                return i;
//        return citationList.size();
//    }
//
//    /**读取和解析本体**/
//    private int parseAnOntology(String ontFile, ArrayList triples, String baseURI, ArrayList insName, ArrayList labelInsName,
//                                ArrayList insTpl, HashMap name2Fathers, HashMap name2Sons, HashMap preFrequence) {
//
//        Map names = new HashMap<String, HashSet<Integer>>();
//        Set labelNames = new HashSet();
//        Set numOfTriples;
//        int ct = 0;
//
//        /*读取和解析源本体*/
//        try {
//            FileReader in = new FileReader(ontFile);
//            BufferedReader br = new BufferedReader(in);
//            if (br != null) {
//                while (br.ready()) {
//                    //读入一个三元组
//                    String strLine = br.readLine().trim();
//                    if (strLine.length() == 0) //跳过空白行
//                        continue;
//                    //保存该三元组
//                    triples.add(strLine);
//                    //分离主谓宾
//                    ArrayList lt = parseStatement(strLine);
//                    String strSub = (String) lt.get(0);
//                    String strPre = (String) lt.get(1);
//                    String strObj = (String) lt.get(2);
//                    //解析实例；实例只出现在主语和宾语位置
//                    String localName = getLocalName(strSub, baseURI);
//                    if (names.containsKey(localName)) {
//                        numOfTriples = (HashSet<Integer>) names.get(localName);
//                        numOfTriples.add(ct);
//                        //names.put(localName, numOfTriples);
//                    } else {
//                        numOfTriples = new HashSet<Integer>();
//                        numOfTriples.add(ct);
//                        names.put(localName, numOfTriples);
//                    }
//                    //判断是否存在父子关系
//                    /*if (strPre.equals("<http://dbpedia.org/ontology/isPartOf>")) {
//                        String sub = getLocalName(strSub, baseURI);
//                        String obj = getLocalName(strObj, baseURI);
//                        Set t = new HashSet();
//                        //添加父亲
//                        if (name2Fathers.containsKey(sub)){
//                            t = (Set)name2Fathers.get(sub);
//                        }
//                        t.add(obj);
//                        name2Fathers.put(sub, t);
//                        //添加孩子
//                        t = new HashSet();
//                        if (name2Sons.containsKey(obj)){
//                            t = (Set)name2Sons.get(obj);
//                        }
//                        t.add(sub);
//                        name2Sons.put(obj, t);
//                    }*/
//
//                    //统计非层次描述使用的谓词及其频率
//                    /*
//                    if (strPre.indexOf(RDFBaseURI)<0 && strPre.indexOf("label")<0 && strPre.indexOf("isPartOf")<0){
//                        String pLocalName = getLocalName(strPre, baseURI);
//                        int fre=0;
//                        //if (strPre.indexOf("http://dbpedia.org/ontology/")>=0) pLocalName = getLocalName(strPre, "http://dbpedia.org/ontology/");
//                        //if (strPre.indexOf("http://www.instancematching.org/")>=0) pLocalName = getLocalName(strPre, "http://www.instancematching.org/");
//                        if (preFrequence.containsKey(pLocalName)){
//                            fre=(Integer) preFrequence.get(pLocalName);
//                        }
//                        preFrequence.put(pLocalName, fre+1);
//                    }
//                    */
//                    ++ct;
//                }
//            }
//            br.close();
//        } catch (IOException e) {
//            System.err.println("Can't find file:\n" + e.toString());
//            System.exit(1);
//        }
//
//        int insNum = names.size();
//        //insName.addAll(names);
//        for (Iterator it = names.entrySet().iterator(); it.hasNext(); ) {
//            Map.Entry entry = (Map.Entry)it.next();
//            insName.add((String)entry.getKey());
//            insTpl.add((HashSet)entry.getValue());
//        }
//        labelInsName.addAll(labelNames);
//        return insNum;
//    }
//
//    /**读取匹配的范围**/
//    public void readResourceTxtFile(String file, ArrayList range) {
//
//        try {
//            FileReader in = new FileReader(file);
//            BufferedReader br = new BufferedReader(in);
//            if (br != null) {
//                while (br.ready()) {
//                    //读入一个三元组
//                    String strLine = br.readLine().trim();
//                    if (strLine.length() == 0)
//                        continue;
//                    range.add(strLine);
//                }
//            }
//        }catch (IOException e) {
//            System.err.println("Can't find file:\n" + e.toString());
//            System.exit(1);
//        }
//    }
//
//    /**抽取匹配**/
//    private void extractMatching() {
////		System.out.print("Extracting matching ....");
////		basicSimMatrix = new StableMarriageFilter().run(sigmaSimMatrix,sourceInsNum,targetInsNum);
//        sigmaSimMatrix = new SimpleFilter().maxValueFilter(sourceInsNum,targetInsNum, sigmaSimMatrix, lowboundThreshold);
//        matchResult = new MapRecord[Math.max(sourceInsNum,targetInsNum)+targetSameIns.size()];
//        matchNum =0;
//
//        for (int i = 0; i < sourceInsNum; i++) {
//            /*把结果限定在resource范围内*/
//			/*String sourceName = new String((String)sourceInsName.get(i));
//			if (!resourceRange.contains(sourceName)){
//				continue;
//			}*/
//
//            for (int j = 0; j < targetInsNum; j++) {
//                if (sigmaSimMatrix[i][j] > lowboundThreshold) {
//                    matchResult[matchNum] = new MapRecord();
//                    matchResult[matchNum].sourceLabel = "http://islab.di.unimi.it/imoaei2015/ontoA.owl#" + sourceInsName.get(i);
//                    matchResult[matchNum].targetLabel = "http://islab.di.unimi.it/imoaei2015/ontoB.owl#" + targetInsName.get(j);
//                    matchResult[matchNum].similarity = sigmaSimMatrix[i][j];
////					System.out.println("DEBUG"+matchNum+": "+matchResult[matchNum].sourceLabel+" "+matchResult[matchNum].targetLabel+" "+matchResult[matchNum].similarity);
//                    matchNum++;
//
//                    //添加存在消解实例的情况
//                    for (Iterator it = targetSameIns.iterator();it.hasNext();){
//                        MapRecord m = (MapRecord) it.next();
//                        if (m.sourceLabel.equals(targetInsName.get(j))){
//                            matchResult[matchNum] = new MapRecord();
//                            matchResult[matchNum].sourceLabel = sourceBaseURI + new String((String)sourceInsName.get(i));
//                            matchResult[matchNum].targetLabel = targetBaseURI + m.targetLabel;
//                            matchResult[matchNum].similarity = sigmaSimMatrix[i][j];
//                            matchNum++;
//                            System.out.println("DEBUG: GET New SameINs:"+m.sourceLabel+"--"+m.targetLabel);
////							break;//只添加一个,仅仅是考虑评测结果的特点
//                        }
//                    }
//                }
//            }
//        }
////		System.out.println("done!");
//    }
//    /**对本体中的实例进行消解处理**/
//    private void ontDisambiguation() {
//        sourceOntDisambiguation();//OAEI2013不考虑对原本体的消解
//        targetOntDisambiguation();
//    }
//
//    /**对源本体中的实例进行消解处理**/
//    private void sourceOntDisambiguation() {
//        //源本体消解
//        double[][] sourceBSMatrix = new double[sourceInsNum][sourceInsNum];
//        sourceBSMatrix = getTextSim(sourceInsNum, sourceInsNum, sourceInsBasicTextDes, sourceInsBasicTextDes);
//
//        double[][] sourceHSMatrix = new double [sourceInsNum][sourceInsNum];
//        double[][] sourceFSMatrix = getTextSim(sourceInsNum, sourceInsNum, sourceInsFatherTextDes, sourceInsFatherTextDes);
//        double[][] sourceSSMatrix = getTextSim(sourceInsNum, sourceInsNum, sourceInsSonTextDes, sourceInsSonTextDes);
//        for (int i=0;i<sourceInsNum;i++)
//            for (int j=0;j<sourceInsNum;j++){
//                sourceHSMatrix[i][j]=0.6*sourceFSMatrix[i][j]+0.6*sourceSSMatrix[i][j];
//                if (sourceHSMatrix[i][j]>1.0) sourceHSMatrix[i][j]=1.0;
//            }
//
//        double[][] sourceNSMatrix = new double [sourceInsNum][sourceInsNum];
//        double[][] sourcePSMatrix = getTextSim(sourceInsNum, sourceInsNum, sourceInsPreTextDes, sourceInsPreTextDes);
//        double[][] sourceOSMatrix = getTextSim(sourceInsNum, sourceInsNum, sourceInsObjTextDes, sourceInsObjTextDes);
//        for (int i=0;i<sourceInsNum;i++)
//            for (int j=0;j<sourceInsNum;j++){
//                sourceNSMatrix[i][j]=sourcePSMatrix[i][j]*sourceOSMatrix[i][j];
//            }
//
//        double totalWeight = 0.4+0.2+0.4;
//        double[][] sourceSMatrix = new double [sourceInsNum][sourceInsNum];
//        for (int i=0;i<sourceInsNum;i++){
//            for (int j=0;j<sourceInsNum;j++){
//                sourceSMatrix[i][j] = (0.4*sourceBSMatrix[i][j]+0.2*sourceHSMatrix[i][j]+0.4*sourceNSMatrix[i][j])/totalWeight;
//                if (i==j){
//                    sourceSMatrix[i][j] = 0.0; //消除自身的相似度
//                }
//            }
//        }
//
//        /*相似实例抽取*/
//        double upboundThreshold = 0.4;
//        ArrayList <MapRecord> sameIns = new ArrayList();
//        boolean newSameFlag;
//        do {
//            newSameFlag = false;
//            double[][] tMatrix = new SimpleFilter().maxValueFilter(sourceInsNum, sourceInsNum, sourceSMatrix, upboundThreshold);
//            for (int i = 0; i < sourceInsNum; i++) {
//                for (int j = 0; j <= i; j++) {
//                    if (tMatrix[i][j] > upboundThreshold) {
//                        MapRecord m = new MapRecord();
//                        m.sourceLabel = (String) sourceInsName.get(i);
//                        m.targetLabel = (String) sourceInsName.get(j);
//                        m.similarity = tMatrix[i][j];
//                        sameIns.add(m);
//                        newSameFlag = true;
//                        System.out.println("DEBUG:" + m.sourceLabel + " "+ m.targetLabel + " " + m.similarity);
//                        sourceSMatrix[i][j] = 0.0; // 清空当前匹配位置，供下一个消解用
//                        sourceSMatrix[j][i] = 0.0; // 清空当前匹配位置，供下一个消解用
//                    }
//                }
//            }
//            System.out.println("DEBUG:------------------");
//        } while (newSameFlag);// 直到没有新的结果出现为止
//
//        System.out.println("DEBUG!");
//        sourceSameIns = sameIns;
//    }
//
//    /**对源本体中的实例进行消解处理**/
//    private void targetOntDisambiguation() {
//        //源本体消解
//        double[][] targetBSMatrix = new double[targetInsNum][targetInsNum];
//        targetBSMatrix = getTextSim(targetInsNum, targetInsNum, targetInsBasicTextDes, targetInsBasicTextDes);
//
//        double[][] targetHSMatrix = new double [targetInsNum][targetInsNum];
//        double[][] targetFSMatrix = getTextSim(targetInsNum, targetInsNum, targetInsFatherTextDes, targetInsFatherTextDes);
//        double[][] targetSSMatrix = getTextSim(targetInsNum, targetInsNum, targetInsSonTextDes, targetInsSonTextDes);
//        for (int i=0;i<targetInsNum;i++)
//            for (int j=0;j<targetInsNum;j++){
//                targetHSMatrix[i][j]=0.6*targetFSMatrix[i][j]+0.6*targetSSMatrix[i][j];
//                if (targetHSMatrix[i][j]>1.0) targetHSMatrix[i][j]=1.0;
//            }
//
//        double[][] targetNSMatrix = new double [targetInsNum][targetInsNum];
//        double[][] targetPSMatrix = getTextSim(targetInsNum, targetInsNum, targetInsPreTextDes, targetInsPreTextDes);
//        double[][] targetOSMatrix = getTextSim(targetInsNum, targetInsNum, targetInsObjTextDes, targetInsObjTextDes);
//        for (int i=0;i<targetInsNum;i++)
//            for (int j=0;j<targetInsNum;j++){
//                targetNSMatrix[i][j]=targetPSMatrix[i][j]*targetOSMatrix[i][j];
//            }
//
//        double totalWeight = 0.4+0.2+0.4;
//        double[][] targetSMatrix = new double [targetInsNum][targetInsNum];
//        for (int i=0;i<targetInsNum;i++){
//            for (int j=0;j<targetInsNum;j++){
//                targetSMatrix[i][j] = (0.4*targetBSMatrix[i][j]+0.2*targetHSMatrix[i][j]+0.4*targetNSMatrix[i][j])/totalWeight;
//                if (i==j){
//                    targetSMatrix[i][j] = 0.0; //消除自身的相似度
//                }
//            }
//        }
//
//        /*相似实例抽取*/
//        double upboundThreshold = 0.25;
//        ArrayList <MapRecord> sameIns = new ArrayList();
//        boolean newSameFlag;
//        do {
//            newSameFlag = false;
//            double[][] tMatrix = new SimpleFilter().maxValueFilter(targetInsNum, targetInsNum, targetSMatrix, upboundThreshold);
//            for (int i = 0; i < targetInsNum; i++) {
//                for (int j = 0; j <= i; j++) {
//                    if (tMatrix[i][j] > upboundThreshold) {
//                        //找到一个相似实例后记录对称结果两次，便于后面选择遍历方便
//                        MapRecord m = new MapRecord();
//                        m.sourceLabel = (String) targetInsName.get(i);
//                        m.targetLabel = (String) targetInsName.get(j);
//                        m.similarity = tMatrix[i][j];
//                        sameIns.add(m);
//                        m = new MapRecord();
//                        m.sourceLabel = (String) targetInsName.get(j);
//                        m.targetLabel = (String) targetInsName.get(i);
//                        m.similarity = tMatrix[i][j];
//                        sameIns.add(m);
//                        newSameFlag = true;
//                        System.out.println("DEBUG:" + m.sourceLabel + " "+ m.targetLabel + " " + m.similarity);
//                        targetSMatrix[i][j] = 0.0; // 清空当前匹配位置，供下一个消解用
//                        targetSMatrix[j][i] = 0.0; // 清空当前匹配位置，供下一个消解用
//                    }
//                }
//            }
//            System.out.println("DEBUG:------------------");
//        } while (newSameFlag);// 直到没有新的结果出现为止
//
//        System.out.println("DEBUG!");
//        targetSameIns = sameIns;
//    }
//
//
//    /**相似度计算**/
//    private void calculateSimilarity() {
//        /*基本描述文档相似度计算*/
//        System.out.print("Calculating basic similarity ....");
//        //calBaseSimilarity();
//        calBaseSimilarity_combined();
//        System.out.println("done!");
//        /*层次相似度计算*/
//        System.out.print("Calculating hierarchy similarity ....");
//        //calHierarchySimilarity();
//        System.out.println("done!");
//        /*非层次相似度计算*/
//        System.out.print("Calculating normal similarity ....");
//        //calNomalSimilarity();
//        System.out.println("done!");
//        /*相似度传播计算*/
//
//        /*合并相似度*/
//        System.out.print("Combining similarity matrix....");
//        combineSimlarityMatrix();
//        System.out.println("done!");
//        basicSimMatrix = null;
//        hierarchySimMatrix = null;
//        normalSimMatrix = null;
//    }
//
//    private double NaNInf2Zero(final double d)
//    {
//        if (Double.isNaN(d) || Double.isInfinite(d))
//            return 0;
//        else
//            return d;
//    }
//
//    /*相似度合并函数*/
//    private void combineSimlarityMatrix() {
//        sigmaSimMatrix = new double[sourceInsNum][targetInsNum];
//        int harmony = 0;
//        /*计算每个匹配器的初始权重*/
//        //基本相似度权重
//        harmony = getMatrixHarmony(basicSimMatrix);
//        bmWeight = (harmony*1.0)/(Math.min(sourceInsNum, targetInsNum)*1.0);
//        bmWeight = NaNInf2Zero(bmWeight);
//        //层次相似度权重
//        /*
//		    harmony = getMatrixHarmony(hierarchySimMatrix);
//		    double t=Math.min(sourceName2Fathers.size()+sourceName2Sons.size(), targetName2Fathers.size()+targetName2Sons.size())*1.0;
//		    hmWeight = (harmony*1.0)/t;
//            hmWeight = NaNInf2Zero(hmWeight);
//        */
//        hierarchySimMatrix = new double[sourceInsNum][targetInsNum];
//        hmWeight = 0;
//        //非层次相似度权重
//        /*
//		    harmony = getMatrixHarmony(normalSimMatrix);
//		    nmWeight = (harmony*1.0)/(Math.min(sourceInsNum, targetInsNum)*1.0);
//            nmWeight = NaNInf2Zero(nmWeight);
//        */
//        normalSimMatrix = new double[sourceInsNum][targetInsNum];
//        nmWeight = 0;
//
//        System.out.println("DEBUG: bmweight:" + bmWeight + "--hmWeight:" + hmWeight + "--nmWeight:" + nmWeight);
//
//        /*合并相似度*/
//        double totalWeight = bmWeight+hmWeight+nmWeight;
//        for (int i=0;i<sourceInsNum;i++){
//            for (int j=0;j<targetInsNum;j++){
//                sigmaSimMatrix[i][j] = (bmWeight*basicSimMatrix[i][j]+hmWeight*hierarchySimMatrix[i][j]+nmWeight*normalSimMatrix[i][j])/totalWeight;
//            }
//        }
//
//        /*引入resource限制应该放在这里*/
//        /*对labelInsName部分进行处理, 对resource范围进行限制*/
//        for (int i=0; i<sourceInsNum; i++) {
//            for (int j=0;j<targetInsNum;j++) {
//                if (sourceLabelInsName.contains(sourceInsName.get(i)) || targetLabelInsName.contains(targetInsName.get(j))){ //!resourceRange.contains(sourceInsName.get(i)) ||
//                    sigmaSimMatrix[i][j]=0.0;
//                }
//            }
//        }
//
//    }
//
//    /*计算非层次相似度*/
//    private void calNomalSimilarity() {
//        /*获取非层次描述文档*/
//        sourceInsPreTextDes = new TextDes[sourceInsNum];
//        sourceInsObjTextDes = new TextDes[sourceInsNum];
//        targetInsPreTextDes = new TextDes[targetInsNum];
//        targetInsObjTextDes = new TextDes[targetInsNum];
//        sourceFlag = true;
//        getNormalTextDes(sourceInsNum, sourceInsName, sourceInsTpl, sourceTriples, sourceInsBasicTextDes, sourceInsPreTextDes, sourceInsObjTextDes);
//        sourceFlag = false;
//        getNormalTextDes(targetInsNum, targetInsName, targetInsTpl, targetTriples, targetInsBasicTextDes, targetInsPreTextDes, targetInsObjTextDes);
//
//        /*计算相似矩阵*/
//        normalSimMatrix = new double [sourceInsNum][targetInsNum];
//        double[][] preSimMatrix = getTextSim(sourceInsNum, targetInsNum, sourceInsPreTextDes, targetInsPreTextDes);
//        double[][] objSimMatrix = getTextSim(sourceInsNum, targetInsNum, sourceInsObjTextDes, targetInsObjTextDes);
//        for (int i=0;i<sourceInsNum;i++)
//            for (int j=0;j<targetInsNum;j++){
//                normalSimMatrix[i][j]=preSimMatrix[i][j]*objSimMatrix[i][j];
//            }
//    }
//
//    /*计算层次描述文档相似度*/
//    private void calHierarchySimilarity() {
//        /*获取层次描述文档*/
//        sourceInsFatherTextDes = new TextDes[sourceInsNum];
//        sourceInsSonTextDes = new TextDes[sourceInsNum];
//        targetInsFatherTextDes = new TextDes[targetInsNum];
//        targetInsSonTextDes = new TextDes[targetInsNum];
//        sourceFlag = true;
//        getHierarchyTextDes(sourceInsNum, sourceInsName, sourceName2Fathers, sourceName2Sons, sourceInsBasicTextDes, sourceInsFatherTextDes, sourceInsSonTextDes);
//        sourceFlag = false;
//        getHierarchyTextDes(targetInsNum, targetInsName, targetName2Fathers, targetName2Sons, targetInsBasicTextDes, targetInsFatherTextDes, targetInsSonTextDes);
//
//        /*计算相似矩阵*/
//        hierarchySimMatrix = new double [sourceInsNum][targetInsNum];
//        double[][] fatherSimMatrix = getTextSim(sourceInsNum, targetInsNum, sourceInsFatherTextDes, targetInsFatherTextDes);
//        double[][] sonSimMatrix = getTextSim(sourceInsNum, targetInsNum, sourceInsSonTextDes, targetInsSonTextDes);
//        for (int i=0;i<sourceInsNum;i++)
//            for (int j=0;j<targetInsNum;j++){
//                hierarchySimMatrix[i][j]=0.6*fatherSimMatrix[i][j]+0.6*sonSimMatrix[i][j];
//                if (hierarchySimMatrix[i][j]>1.0) hierarchySimMatrix[i][j]=1.0;
//            }
//
//    }
//
//    /*计算基本描述文档相似度*/
//    private void calBaseSimilarity() {
//        /*获取基本描述文档*/
//        sourceInsBasicTextDes = new TextDes[sourceInsNum];
//        targetInsBasicTextDes = new TextDes[targetInsNum];
//        sourceFlag = true;
//        getBasicTextDes(sourceInsNum, sourceInsName, sourceLabelInsName, sourceBaseURI, sourceInsTpl, sourceTriples, sourceInsBasicTextDes);
//        sourceFlag = false;
//        getBasicTextDes(targetInsNum, targetInsName, targetLabelInsName, targetBaseURI, targetInsTpl, targetTriples, targetInsBasicTextDes);
//
//        /*计算相似矩阵*/
//        basicSimMatrix = new double[sourceInsNum][targetInsNum];
//        basicSimMatrix = getTextSim(sourceInsNum, targetInsNum, sourceInsBasicTextDes, targetInsBasicTextDes);
//    }
//
//    private void calBaseSimilarity_combined() {
//        /*获取基本描述文档*/
//        sourceInsBasicTextDes = new TextDes[sourceInsNum];
//        targetInsBasicTextDes = new TextDes[targetInsNum];
//        sourceFlag = true;
//        getBasicTextDes_combined(sourceInsNum, sourceInsName, sourceLabelInsName, sourceBaseURI, sourceInsTpl, sourceTriples, sourceInsBasicTextDes);
//        sourceFlag = false;
//        getBasicTextDes_combined(targetInsNum, targetInsName, targetLabelInsName, targetBaseURI, targetInsTpl, targetTriples, targetInsBasicTextDes);
//
//        /*计算相似矩阵*/
//        basicSimMatrix = new double[sourceInsNum][targetInsNum];
//        basicSimMatrix = getTextSim_combined(sourceInsNum, targetInsNum, sourceInsBasicTextDes, targetInsBasicTextDes, propOrder);
//    }
//
//    private void calcAuthorRecSilimarity(final instStat[] source, final instStat[] target) {
//        sigmaSimMatrix = new double[sourceInsNum][targetInsNum];
//        new parfor() {
//            public void iter(int thread_idx, int i) {
//                for (int j = 0; j < targetInsNum; j++) {
//                    double tmp = 1.0;
//                    tmp *= strArrSimAllowIncluding(source[i].strProp, target[j].strProp);
//                    tmp *= numArrSim(source[i].numProp, target[j].numProp);
//                    sigmaSimMatrix[i][j] = tmp;
//                    //if (Double.isNaN(tmp))
//                    //continue;
//                    //if (i == 0 && targetInsName.get(j).equals("4bbdf4c0-73ff-48ea-b0ca-b75dfa0d76a5"))
//                    //continue;
//                }
//            }
//        }.execute(0, sourceInsNum);
//    }
//
//    private double strArrSimAllowIncluding(final String[] strA, final String[] strB)
//    {
//        if (strA.length != strB.length)
//            return 1.0;
//        final StrEDSim edsim = new StrEDSim();
//        double score = 1.0;
//        for (int i = 0; i < strA.length; ++i)
//            if (strA[i].length() > 0 && strB[i].length() > 0 && !strA[i].contains(strB[i]) && !strB[i].contains(strA[i])) //跳过空串与串包含的情况
//                score *= edsim.getNormEDSim(strA[i], strB[i]);
//        return score;
//    }
//
//    private double numArrSim(final double[] numA, final double[] numB)
//    {
//        if (numA.length != numB.length)
//            return 1.0;
//        double score = 1.0;
//        for (int i = 0; i < numA.length; ++i) {
//            if (numA[i] >= 0 && numB[i] >= 0 && !(numA[i] == 0 && numB[i] == 0))
//                score *= 1 - (Math.abs(numA[i] - numB[i]) / Math.max(numA[i], numB[i]));
//        }
//        //if (score < 0)
//        //score += 0;
//        return score;
//    }
//
//    private double[][] getTextSim(int sourceNum, int targetNum, TextDes[] sDes, TextDes[] tDes){
//        double[][] simMatrix;
//        new TextDocSim().tuneDesDocbyED(sDes,sourceNum,tDes,targetNum);
//        simMatrix=new TextDocSim().computeTFIDFSim(sDes, sourceNum, tDes, targetNum);
//        return simMatrix;
//    }
//
//    private double[][] getTextSim_combined(int sourceNum, int targetNum, TextDes[] sDes, TextDes[] tDes, HashMap propOrder){
//        double[][] simMatrix;
//        simMatrix = new TextDocSim().simpleSim(sDes, sourceNum, tDes, targetNum, propOrder);
//        return simMatrix;
//    }
//
//    /*获取非层次描述文档*/
//    private void getNormalTextDes(int insNum, ArrayList insName, ArrayList insTpl, ArrayList triples, TextDes[] insBasicTextDes, TextDes[] insPreTextDes, TextDes[] insObjTextDes) {
//        SplitWords spWord = new SplitWords();
//        DelStopWords delSWrod = new DelStopWords();
//        delSWrod.loadStopWords();
//
//        for (int i=0;i<insNum;i++) {
//            String curInsName = (String)insName.get(i);
//            insPreTextDes[i] = new TextDes();
//            insPreTextDes[i].name = curInsName;
//            insPreTextDes[i].text = new ArrayList();
//            insObjTextDes[i] = new TextDes();
//            insObjTextDes[i].name = curInsName;
//            insObjTextDes[i].text = new ArrayList();
//
//            //遍历每个实例对应的三元组
//            Set tp = (Set) insTpl.get(i);
//            for (Iterator it = tp.iterator(); it.hasNext();) {
//                int pos = (Integer) it.next();
//                String strLine = (String) triples.get(pos);
//                ArrayList lt = parseStatement(strLine);
//                String strSub = (String) lt.get(0);
//                String strPre = (String) lt.get(1);
//                String strObj = (String) lt.get(2);
//
//                //跳过不是当前实例开始的三元组
//                if (strSub.indexOf(curInsName)<0) {
//                    continue;
//                }
//
//                //跳过基本描述和层次描述三元组
//                if (!(strPre.indexOf(RDFBaseURI)<0 && strPre.indexOf("author_of")<0)){
//                    continue;
//                }
//
//                //构造谓词描述文档
//                String pLocalName= new String();
//                pLocalName = getLocalName(strPre, sourceFlag ? sourceBaseURI : targetBaseURI);
//                //if (strPre.indexOf("http://dbpedia.org/ontology/")>=0) pLocalName = getLocalName(strPre, "http://dbpedia.org/ontology/");
//                //if (strPre.indexOf("http://www.instancematching.org/")>=0) pLocalName = getLocalName(strPre, "http://www.instancematching.org/");
//
//                ArrayList listP = new ArrayList();
//                Word w = new Word();
//                listP = spWord.split(pLocalName);
//                listP = delSWrod.removeStopWords(listP);
//                for (Iterator it1 = listP.iterator(); it1.hasNext();) {
//                    String stemp = (String) it1.next();
//                    w = new Word();
//                    w.content = stemp;
//                    w.weight = wLabel;
//                    insPreTextDes[i].text.add(w);
//                }
//
//                //构造宾语描述文档
//                String bLocalName = strObj;
//                bLocalName = getLocalName(strObj, sourceFlag ? sourceBaseURI : targetBaseURI);
//                //if (strObj.indexOf("http://dbpedia.org/ontology/")>=0) bLocalName = getLocalName(strObj, "http://dbpedia.org/ontology/");
//                //if (strObj.indexOf("http://www.instancematching.org/")>=0) bLocalName = getLocalName(strObj, "http://www.instancematching.org/");
//                //if (strObj.indexOf("http://dbpedia.org/resource/")>=0) bLocalName = getLocalName(strObj, "http://dbpedia.org/resource/");
//                //判断宾语是否是实例
//                double bweight = 0;
//                if (sourceFlag) bweight = 1.0/(Math.log(sourcePreFrequence.get(pLocalName))+1.0);
//                if (!sourceFlag) bweight = 1.0/(Math.log(targetPreFrequence.get(pLocalName))+1.0);
//                int bPos = insName.indexOf(bLocalName);
//                //添加到当前实例的描述文档中
//                if (bPos>=0){
//                    ArrayList des = insBasicTextDes[bPos].text;
//                    for (Iterator it1 = des.iterator(); it1.hasNext();){
//                        w = (Word) it1.next();
//                        w.weight = w.weight*bweight;
//                        insObjTextDes[i].text.add(w);
//                    }
//                }
//                else{
//                    //宾语不是实例的情况
//                    ArrayList list = new ArrayList();
//                    //处理日期情况
//                    bLocalName = handleDate(bLocalName);
//                    bLocalName = delSWrod.removeStopWords(bLocalName);
//                    list = spWord.split(bLocalName);
//                    list = delSWrod.removeStopWords(list);
//                    for (Iterator it1 = list.iterator(); it1.hasNext();) {
//                        String stemp = (String) it1.next();
//                        w = new Word();
//                        w.content = stemp;
//                        w.weight = bweight;
//                        insObjTextDes[i].text.add(w);
//                    }
//
//                }
//
//            }
//
//        }
//    }
//
//    /*获取层次描述文档*/
//    private void getHierarchyTextDes(int insNum, ArrayList insName, HashMap name2Fathers, HashMap name2Sons, TextDes[] insBasicTextDes, TextDes[] insFatherTextDes, TextDes[] insSonTextDes) {
//        SplitWords spWord = new SplitWords();
//        DelStopWords delSWrod = new DelStopWords();
//        delSWrod.loadStopWords();
//
//        for (int i=0; i<insNum; i++){
//            insFatherTextDes[i] = new TextDes();
//            insFatherTextDes[i].name = (String)insName.get(i);
//            insFatherTextDes[i].text = new ArrayList();
//            insSonTextDes[i] = new TextDes();
//            insSonTextDes[i].name = (String)insName.get(i);
//            insSonTextDes[i].text = new ArrayList();
//
//            //获得实例第一层父亲集合
//            Set fathersLevel1 = new HashSet();
//            fathersLevel1 = (Set) name2Fathers.get(insName.get(i));
//            Set fathersLevel2 = new HashSet();
//            //以父亲的基本描述文档构造层次描述文档
//            if (fathersLevel1!=null){
//                for (Iterator it = fathersLevel1.iterator(); it.hasNext();){
//                    String str = (String) it.next();
//                    if (name2Fathers.get(str)!=null) fathersLevel2.addAll((Set)name2Fathers.get(str));
//                    int pos = insName.indexOf(str);
//                    //如果pos=-1，说明当前实例不是baseURI的实例
//                    if (pos<0){
//                        String sName = getLocalName(str, "http://dbpedia.org/resource/");
//                        ArrayList list = new ArrayList();
//                        list = spWord.split(sName);
//                        list = delSWrod.removeStopWords(list);
//                        for (Iterator it1 = list.iterator(); it1.hasNext();) {
//                            String stemp = (String) it1.next();
//                            Word w = new Word();
//                            w.content = stemp;
//                            w.weight = wLocalName;
//                            insFatherTextDes[i].text.add(w);
//                        }
//                    }
//                    else {
//                        ArrayList words = insBasicTextDes[pos].text;
//                        for (Iterator it1 = words.iterator(); it1.hasNext();) {
//                            Word wFather = (Word) it1.next();
//                            Word w = new Word();
//                            w.content = wFather.content;
//                            w.weight = wFather.weight*1.0;
//                            insFatherTextDes[i].text.add(w);
//                        }
//                    }
//                }
//            }
//
//            //获得实例第二层父亲集合
//            Set fathersLevel3 = new HashSet();
//            if (fathersLevel2!=null){
//                for (Iterator it = fathersLevel2.iterator(); it.hasNext();){
//                    String str = (String) it.next();
//                    if (name2Fathers.get(str)!=null) fathersLevel3.addAll((Set)name2Fathers.get(str));
//                    int pos = insName.indexOf(str);
//                    //如果pos=-1，说明当前实例不是baseURI的实例
//                    if (pos<0){
//                        String sName = getLocalName(str, "http://dbpedia.org/resource/");
//                        ArrayList list = new ArrayList();
//                        list = spWord.split(sName);
//                        list = delSWrod.removeStopWords(list);
//                        for (Iterator it1 = list.iterator(); it1.hasNext();) {
//                            String stemp = (String) it1.next();
//                            Word w = new Word();
//                            w.content = stemp;
//                            w.weight = wLocalName/2.0;
//                            insFatherTextDes[i].text.add(w);
//                        }
//                    }
//                    else {
//                        ArrayList words = insBasicTextDes[pos].text;
//
//                        for (Iterator it1 = words.iterator(); it1.hasNext();) {
//                            Word wFather = (Word) it1.next();
//                            Word w = new Word();
//                            w.content = wFather.content;
//                            w.weight = wFather.weight*1.0/2.0;
//                            insFatherTextDes[i].text.add(w);
//                        }
//                    }
//                }
//            }
//
//            //获得实例的第三层父亲集合
//            if (fathersLevel3!=null){
//                for (Iterator it = fathersLevel3.iterator(); it.hasNext();){
//                    String str = (String) it.next();
//                    int pos = insName.indexOf(str);
//                    //如果pos=-1，说明当前实例不是baseURI的实例
//                    if (pos<0){
//                        String sName = getLocalName(str, "http://dbpedia.org/resource/");
//                        ArrayList list = new ArrayList();
//                        list = spWord.split(sName);
//                        list = delSWrod.removeStopWords(list);
//                        for (Iterator it1 = list.iterator(); it1.hasNext();) {
//                            String stemp = (String) it1.next();
//                            Word w = new Word();
//                            w.content = stemp;
//                            w.weight = wLocalName/4.0;
//                            insFatherTextDes[i].text.add(w);
//                        }
//                    }
//                    else {
//                        ArrayList words = insBasicTextDes[pos].text;
//
//                        for (Iterator it1 = words.iterator(); it1.hasNext();) {
//                            Word wFather = (Word) it1.next();
//                            Word w = new Word();
//                            w.content = wFather.content;
//                            w.weight = wFather.weight*1.0/4.0;
//                            insFatherTextDes[i].text.add(w);
//                        }
//                    }
//                }
//            }
//
//            //获得实例第一层孩子集合
//            Set sonsLevel1 = new HashSet();
//            sonsLevel1 = (Set) name2Sons.get(insName.get(i));
//            Set sonsLevel2 = new HashSet();
//            //以实例的基本描述文档构造层次描述文档
//            if (sonsLevel1!=null){
//                for (Iterator it = sonsLevel1.iterator(); it.hasNext();){
//                    String str = (String) it.next();
//                    if (name2Sons.get(str)!=null) sonsLevel2.addAll((Set)name2Sons.get(str));
//                    int pos = insName.indexOf(str);
//                    //如果pos=-1，说明当前实例不是baseURI的实例
//                    if (pos<0){
//                        String sName = getLocalName(str, "http://dbpedia.org/resource/");
//                        ArrayList list = new ArrayList();
//                        list = spWord.split(sName);
//                        list = delSWrod.removeStopWords(list);
//                        for (Iterator it1 = list.iterator(); it1.hasNext();) {
//                            String stemp = (String) it1.next();
//                            Word w = new Word();
//                            w.content = stemp;
//                            w.weight = wLocalName;
//                            insSonTextDes[i].text.add(w);
//                        }
//                    }
//                    else {
//                        ArrayList words = insBasicTextDes[pos].text;
//                        for (Iterator it1 = words.iterator(); it1.hasNext();) {
//                            Word wSon = (Word) it1.next();
//                            Word w = new Word();
//                            w.content = wSon.content;
//                            w.weight = wSon.weight*1.0;
//                            insSonTextDes[i].text.add(w);
//                        }
//                    }
//                }
//            }
//
//            //获得实例第二层孩子集合
//            Set sonsLevel3 = new HashSet();
//            if (sonsLevel2!=null){
//                for (Iterator it = sonsLevel2.iterator(); it.hasNext();){
//                    String str = (String) it.next();
//                    if (name2Sons.get(str)!=null)  sonsLevel3.addAll((Set)name2Sons.get(str));
//                    int pos = insName.indexOf(str);
//                    //如果pos=-1，说明当前实例不是baseURI的实例
//                    if (pos<0){
//                        String sName = getLocalName(str, "http://dbpedia.org/resource/");
//                        ArrayList list = new ArrayList();
//                        list = spWord.split(sName);
//                        list = delSWrod.removeStopWords(list);
//                        for (Iterator it1 = list.iterator(); it1.hasNext();) {
//                            String stemp = (String) it1.next();
//                            Word w = new Word();
//                            w.content = stemp;
//                            w.weight = wLocalName/2.0;
//                            insSonTextDes[i].text.add(w);
//                        }
//                    }
//                    else {
//                        ArrayList words = insBasicTextDes[pos].text;
//                        for (Iterator it1 = words.iterator(); it1.hasNext();) {
//                            Word wSon = (Word) it1.next();
//                            Word w = new Word();
//                            w.content = wSon.content;
//                            w.weight = wSon.weight*1.0/2.0;
//                            insSonTextDes[i].text.add(w);
//                        }
//                    }
//                }
//            }
//
//            //获得实例的第三层孩子集合
//            if (sonsLevel3!=null){
//                for (Iterator it = sonsLevel3.iterator(); it.hasNext();){
//                    String str = (String) it.next();
//                    int pos = insName.indexOf(str);
//                    //如果pos=-1，说明当前实例不是baseURI的实例
//                    if (pos<0){
//                        String sName = getLocalName(str, "http://dbpedia.org/resource/");
//                        ArrayList list = new ArrayList();
//                        list = spWord.split(sName);
//                        list = delSWrod.removeStopWords(list);
//                        for (Iterator it1 = list.iterator(); it1.hasNext();) {
//                            String stemp = (String) it1.next();
//                            Word w = new Word();
//                            w.content = stemp;
//                            w.weight = wLocalName/4.0;
//                            insSonTextDes[i].text.add(w);
//                        }
//                    }
//                    else {
//                        ArrayList words = insBasicTextDes[pos].text;
//
//                        for (Iterator it1 = words.iterator(); it1.hasNext();) {
//                            Word wSon = (Word) it1.next();
//                            Word w = new Word();
//                            w.content = wSon.content;
//                            w.weight = wSon.weight*1.0/4.0;
//                            insSonTextDes[i].text.add(w);
//                        }
//                    }
//                }
//            }
//
//        }
//    }
//
//    /*获取基本描述文档*/
//    private void getBasicTextDes(int insNum, ArrayList insName, ArrayList labelInsName, String baseURI, ArrayList insTpl, ArrayList triples, TextDes[] insBasicTextDes){
//        SplitWords spWord = new SplitWords();
//        DelStopWords delSWrod = new DelStopWords();
//        delSWrod.loadStopWords();
//
//        for (int i=0; i<insNum; i++){
//            //获取localname,因为并非所有实例都有label
//            String sName = new String();
//            String sLabel = new String();
//            String sComment = new String();
////			System.out.println(i);
//            sName = (String) insName.get(i);
//            Set tp = (Set) insTpl.get(i);
//            ArrayList list = new ArrayList();
//            insBasicTextDes[i] = new TextDes();
//            insBasicTextDes[i].name = sName;
//            insBasicTextDes[i].text = new ArrayList();
//            for (Iterator it = tp.iterator(); it.hasNext();) {
//                int pos = (Integer) it.next();
//                String strLine = (String) triples.get(pos);
//                ArrayList lt = parseStatement(strLine);
//                String strSub = (String) lt.get(0);
//                String strPre = (String) lt.get(1);
//                String strObj = (String) lt.get(2);
//
//                //跳过不是当前实例开始的三元组
//                if (strSub.indexOf(sName)<0) {
//                    continue;
//                }
//
//                strPre = getLocalName(strPre, baseURI);
//                Double weight = propWeight.get(strPre);
//                if (weight == null)
//                    continue; //未知的谓词，忽略
//                if (weight <= 0.0)
//                    continue; //无权重的谓词，忽略
//				/*
//				// 获取label
//				if (strPre.indexOf("http://www.w3.org/2000/01/rdf-schema#label") >= 0) {
//					sLabel = strObj.replace("@en", "");
//				}
//				// 获取comment
//				if (strPre.indexOf("http://www.w3.org/2000/01/rdf-schema#comment") >= 0) {
//					sComment = strObj.replace("@en", "");
//				}
//				*/
//
//                sLabel = strObj;
//
//                //sLabel = delSWrod.removeStopWords(sLabel);
//                //list = spWord.split(sLabel);
//                //list = delSWrod.removeStopWords(list);
//                //for (Iterator iter = list.iterator(); iter.hasNext();) {
//                //String stemp = (String) iter.next();
//                Word w = new Word();
//                w.content = sLabel; //stemp;
//                w.weight = weight;
//                insBasicTextDes[i].text.add(w); //TODO:check this out
//                //}
//            }
//
//            /*描述文档处理*/
//			/*
//			ArrayList list = new ArrayList();
//			insBasicTextDes[i] = new TextDes();
//			insBasicTextDes[i].name = sName;
//			insBasicTextDes[i].text = new ArrayList();
//
//			//处理localName,这里要排除目标本体的处理
//			sName = delSWrod.removeStopWords(sName);
//			if (!sourceFlag){
//				sName="";
//			}
//			list = spWord.split(sName);
//			list = delSWrod.removeStopWords(list);
//			for (Iterator it = list.iterator(); it.hasNext();) {
//				String stemp = (String) it.next();
//				Word w = new Word();
//				w.content = stemp;
//				w.weight = wLocalName;
//				insBasicTextDes[i].text.add(w);
//			}
//			//处理label
//			sLabel = delSWrod.removeStopWords(sLabel);
//			list = spWord.split(sLabel);
//			list = delSWrod.removeStopWords(list);
//			for (Iterator it = list.iterator(); it.hasNext();) {
//				String stemp = (String) it.next();
//				Word w = new Word();
//				w.content = stemp;
//				w.weight = wLabel;
//				insBasicTextDes[i].text.add(w);
//			}
//			//处理comment
//			sComment = delSWrod.removeStopWords(sComment);
//			list = spWord.split(sComment);
//			list = delSWrod.removeStopWords(list);
//			for (Iterator it = list.iterator(); it.hasNext();) {
//				String stemp = (String) it.next();
//				Word w = new Word();
//				w.content = stemp;
//				w.weight = wComment;
//				insBasicTextDes[i].text.add(w);
//			}
//			*/
//        }
//
//        /**处理LableInsName问题**/
//        /*
//		for (int i=0; i<insNum; i++){
//			//获取localname,因为并非所有实例都有label
//			String sName = new String();
//			String sLabel = new String();
//			String sComment = new String();
////			System.out.println(i);
//			sName = (String) insName.get(i);
//			Set tp = (Set) insTpl.get(i);
//			for (Iterator it = tp.iterator(); it.hasNext();) {
//				int pos = (Integer) it.next();
//				String strLine = (String) triples.get(pos);
//				ArrayList lt = parseStatement(strLine);
//				String strSub = (String) lt.get(0);
//				String strPre = (String) lt.get(1);
//				String strObj = (String) lt.get(2);
//
//				//跳过不是当前实例开始的三元组
//				if (strSub.indexOf(sName)<0) {
//					continue;
//				}
//
//				// 如果宾语是labelInsName，把其文本描述作为当前实例的文本描述
//				String objLocalName = getLocalName(strObj, baseURI);
//				if (strPre.indexOf("<http://www.instancematching.org/label>") >= 0 && labelInsName.contains(objLocalName)) {
//					//找到labelInsName对应的描述文档
//					int posLabel = insName.indexOf(objLocalName);
//					//添加到当前实例的描述文档中
//					insBasicTextDes[i].text.addAll(insBasicTextDes[posLabel].text);
//				}
//
//
//			}
//		}
//		*/
//    }
//
//    private void getBasicTextDes_combined(int insNum, ArrayList insName, ArrayList labelInsName, String baseURI, ArrayList insTpl, ArrayList triples, TextDes[] insBasicTextDes){
//        for (int i=0; i<insNum; i++) {
//            String sName = new String();
//            String sLabel = new String();
//            String[] props;
//            sName = (String) insName.get(i);
//            Set tp = (Set) insTpl.get(i);
//            ArrayList list = new ArrayList();
//            insBasicTextDes[i] = new TextDes();
//            insBasicTextDes[i].name = sName;
//            insBasicTextDes[i].text = new ArrayList();
//            for (Iterator it = tp.iterator(); it.hasNext();) {
//                int pos = (Integer) it.next();
//                String strLine = (String) triples.get(pos);
//                ArrayList lt = parseStatement(strLine);
//                String strSub = (String) lt.get(0);
//                String strPre = (String) lt.get(1);
//                String strObj = (String) lt.get(2);
//
//                //跳过不是当前实例开始的三元组
//                if (strSub.indexOf(sName)<0) {
//                    continue;
//                }
//
//                if (strPre.equals("pub")) {
//                    sLabel = strObj;
//                    Word w = new Word();
//                    props = sLabel.split("!#!");
//                    props[1] = props[1].replace(" ", "").replace("-", "");
//                    props[2] = props[2].replace(" ", "").replace("-", "");
//                    if (!props[3].equals("null"))
//                        props[3] = props[3].replaceAll("\\D", ""); //仅留下数字
//                    if (!props[4].equals("null"))
//                        props[4] = props[4].replaceAll("\\D", ""); //仅留下数字
//                    w.content = joinString(props, "\n").toLowerCase(Locale.US); //信息被包含在一个字符串内，此处不分离之
//                    w.weight = 1.0;
//                    insBasicTextDes[i].text.add(w);
//                } else if (strPre.equals("name")) {
//                    //if (strSub.equals("fd1bfe94-6239-4355-a2ba-f65f63545d8f"))
//                    //continue;
//                    //if (strSub.equals("3a6d7701-1a74-4dd5-88f7-cfe47618aa96"))
//                    //continue;
//                    Word w = new Word();
//                    w.content = strObj;
//                    w.weight = 1.0;
//                    insBasicTextDes[i].text.add(w);
//                }
//            }
//        }
//    }
//
//    private static String joinString(final String str[], final String det)
//    {
//        int i;
//        String ret = new String();
//        for (i = 0; i < str.length - 1; ++i)
//            ret += str[i] + det;
//        ret += str[i];
//        return ret;
//    }
//
//    /**结果评估**/
//    private void evaluate() {
////		System.out.print("Evaluating matching results ....");
//        ArrayList list = new ArrayList();
//        MapRecord[] refMapResult = null;
//        int refMapNum = 0;
//        boolean hasRefFile=false;
//        if (refalignFile.length() == 0) {
//            return;
//        }
//        // 读出标准结果
//        list = new MappingFile().read4tsv(refalignFile);
//        refMapNum = ((Integer) list.get(0)).intValue();
//        refMapResult = new MapRecord[refMapNum];
//        refMapResult = (MapRecord[]) ((ArrayList) list.get(1)).toArray(new MapRecord[0]);
//        hasRefFile = true;
//        // 输入给评估类
//        if (hasRefFile){
//            list = new EvaluateMapping().getEvaluation(refMapNum, refMapResult,	matchNum, matchResult);
//            precision = ((Double) list.get(0)).doubleValue();
//            recall = ((Double) list.get(1)).doubleValue();
//            f1Measure = ((Double) list.get(2)).doubleValue();
//            if (Math.abs(precision)<0.0001 && recall>0.9999) {recall = 0.0;}
//        }
//        else {
//            precision = -1.0;
//            recall = -1.0;
//            f1Measure = -1.0;
//        }
////		System.out.println("done!");
//    }
//
//    private void evaluateFromRDF() {
//        int count = 0;
//        boolean[] flag, flag2;
//        //String sarr[];
//        //ArrayList list;
//        MapRecord[] refMapResult;
//        ArrayList<MapRecord> tmp = new ArrayList<>();
//        try {
//            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//            DocumentBuilder db = dbf.newDocumentBuilder();
//            Document document = db.parse(new File(refalignFile));
//            NodeList list = ((Element)document.getElementsByTagName("rdf:RDF").item(0)).getElementsByTagName("Alignment").item(0).getChildNodes();
//            NodeList list2;
//            Element element;
//            for(int i = 0; i < list.getLength(); ++i) {
//                switch(list.item(i).getNodeName()) {
//                    case "map":
//                        element = (Element)list.item(i);
//                        element = (Element)element.getElementsByTagName("Cell").item(0);
//                        MapRecord tmpMap = new MapRecord();
//                        tmpMap.sourceLabel = element.getElementsByTagName("entity1").item(0).getAttributes().getNamedItem("rdf:resource").getNodeValue();
//                        tmpMap.targetLabel = element.getElementsByTagName("entity2").item(0).getAttributes().getNamedItem("rdf:resource").getNodeValue();
//                        tmpMap.similarity = Double.valueOf(element.getElementsByTagName("measure").item(0).getTextContent());
//                        tmp.add(tmpMap);
//                        break;
//                }
//            }
//        } catch (Exception e)
//        {
//            e.printStackTrace();
//        }
//        refMapResult = tmp.toArray(new MapRecord[0]);
//        tmp.clear();
//        flag = new boolean[matchResult.length];
//        flag2 = new boolean[refMapResult.length];
//        for (int i = 0; i < matchResult.length; ++i) {
//            if (matchResult[i] == null)
//                continue;
//            for (int j = 0; j < refMapResult.length; ++j)
//                if (matchResult[i].sourceLabel.equals(refMapResult[j].sourceLabel) && matchResult[i].targetLabel.equals(refMapResult[j].targetLabel)) {
//                    ++count;
//                    flag[i] = true;
//                    flag2[j] = true;
//                    break;
//                }
//        }
//        for (int i = 0; i < matchResult.length; ++i)
//            if (!flag[i] && matchResult[i] != null)
//            {
//                System.out.println(matchResult[i].sourceLabel + "\t" + matchResult[i].targetLabel + "\t" + matchResult[i].similarity + "\t(X)");
//            }
//        for (int j = 0; j < refMapResult.length; ++j)
//            if (!flag2[j])
//            {
//                System.out.println(refMapResult[j].sourceLabel + "\t" + refMapResult[j].targetLabel + "\t(?)");
//            }
//        double p, r, f;
//        p = (double)count/(double)matchResult.length;
//        System.out.println("P: " + p);
//        r = (double)count/(double)refMapResult.length;
//        System.out.println("R: " + r);
//        f = (2.0 * p * r) / (p + r);
//        System.out.println("F: " + f);
//    }
//
//
//    /**处理翻译后的本体中的不规范问题**/
//    private void transProcess() {
//        ArrayList raw = new ArrayList();
//
//        /**读取翻译后本体文件**/
//        try {
//            FileReader in = new FileReader("./dataset/OAEI2013/contest/testcase03/contest/contest_trans0.rdf");
//            BufferedReader br = new BufferedReader(in);
//            if (br != null) {
//                while (br.ready()) {
//                    String strLine = br.readLine().trim();
//                    String strSub = new String();
//                    String strPre = new String();
//                    String strObj = new String();
//                    //分离主谓宾
//                    ArrayList lt = parseStatement(strLine);
//                    strSub = (String) lt.get(0);
//                    strPre = (String) lt.get(1);
//                    strObj = (String) lt.get(2);
//                    //处理宾语中的空格问题
//                    if (strObj.indexOf("http://")>0) {
//                        strObj=strObj.replaceAll(" ", "");
//                    }
////					System.out.println(strSub+" "+strPre+" "+strObj);
//                    raw.add(strSub+" "+strPre+" "+strObj);
//                }
//            }
//
//        } catch (IOException e) {
//            System.err.println("Can't find file:\n" + e.toString());
//            System.exit(1);
//        }
//
//        /**修改后的本体写入文件**/
//        try {
//            FileWriter out = new FileWriter("test.rdf");
//            BufferedWriter bw = new BufferedWriter(out);
//
//            for (Iterator it = raw.iterator(); it.hasNext();) {
//                String str = (String)it.next();
//                bw.write(str);
//                bw.newLine();
//            }
//
//            //关闭文件
//            bw.close();
//            out.close();
//        } catch (IOException e) {
//            System.err.println("Can't open file:\n" + e.toString());
//            System.exit(1);
//        }
//    }
//
//    /*对三元组进行解析，返回主谓宾的链表*/
//    private ArrayList parseStatement(String sta) {
//        ArrayList lt = new ArrayList();
//        int s = sta.indexOf("<");
//        int t = sta.indexOf(">", s);
//        //if (s<0 || t<0) {System.out.println(sta);}
//        String strSub = sta.substring(s, t + 1);
//        sta = sta.replace(strSub, "");
//        s = sta.indexOf("<");
//        t = sta.indexOf(">", s);
//        String strPre = sta.substring(s, t + 1);
//        String strObj = sta.replace(strPre, "");
//
//        lt.add(strSub.substring(1, strSub.length()-1));
//        lt.add(strPre.substring(1, strPre.length()-1));
//        lt.add(strObj.substring(1, strObj.length()-1));
//
//        return lt;
//    }
//
//    private String getLocalName(String str, String baseURI) {
//        int sPos = str.indexOf(baseURI);
//        int tPos = str.length(); //str.lastIndexOf(">");
//        String localName = new String(str);
//        if (sPos>=0 && tPos>0) {
//            sPos+=baseURI.length();
//            localName = str.substring(sPos, tPos);
//        }
//        return localName;
//    }
//
//    /*计算相似矩阵Harmony*/
//    private int getMatrixHarmony(double[][] matrix){
//        double harmony=0.0;
//        int hCount = 0;
//        //求每行最大值
//        double[] rowMax = new double[sourceInsNum];
//        for (int i=0;i<sourceInsNum;i++){
//            double a[] = matrix[i].clone();
//            Arrays.sort(a);
//            rowMax[i]=a[targetInsNum-1];
//        }
//        //求每列最大值
//        double[] colMax = new double[targetInsNum];
//        for (int i=0;i<targetInsNum;i++){
//            double a[] = new double[sourceInsNum];
//            for (int j=0;j<sourceInsNum;j++){
//                a[j] = matrix[j][i];
//            }
//            Arrays.sort(a);
//            colMax[i]=a[sourceInsNum-1];
//        }
//        //遍历相似矩阵
//        for (int i=0;i<sourceInsNum;i++){
//            for (int j=0;j<targetInsNum;j++){
//                //判断当前是否一个harmony
//                if (Math.abs(matrix[i][j]-rowMax[i])<0.001 && Math.abs(matrix[i][j]-colMax[j])<0.001 && matrix[i][j]>lowboundThreshold) {
//                    hCount++;
//                }
//            }
//        }
//        return hCount;
//    }
//
//    /**输出匹配该结果**/
//    private void outputMatch() {
//        System.out.print("Saving matching results ....");
//        new MappingFile().save2tsv(outputOntFile, matchNum, matchResult);
//        System.out.println("done!");
//        //System.out.println("Precision:" + precision);
//        //System.out.println("Recall:" + recall);
//        //System.out.println("F1Measure:" + f1Measure);
//    }
//
//    /**日期转换词典**/
//    private void setDateDict(){
//        dateDict = new HashMap();
//        dateDict.put("January", "01");
//        dateDict.put("February", "02");
//        dateDict.put("March", "03");
//        dateDict.put("April", "04");
//        dateDict.put("May", "05");
//        dateDict.put("June", "06");
//        dateDict.put("July", "07");
//        dateDict.put("August", "08");
//        dateDict.put("September", "09");
//        dateDict.put("October", "10");
//        dateDict.put("November", "11");
//        dateDict.put("December", "12");
//
//        dateDict.put("Jan", "01");
//        dateDict.put("Feb", "02");
//        dateDict.put("Mar", "03");
//        dateDict.put("Apr", "04");
//        dateDict.put("Jun", "06");
//        dateDict.put("Jul", "07");
//        dateDict.put("Aug", "08");
//        dateDict.put("Sept", "09");
//        dateDict.put("Sep", "09");
//        dateDict.put("Oct", "10");
//        dateDict.put("Nov", "11");
//        dateDict.put("Dec", "12");
//
//        monthList = new ArrayList(Arrays.asList("January", "February", "March","April", "May","June", "July","August", "September","October", "November","December",
//                "Jan","Feb", "Mar","Apr", "Jun","Jul", "Aug","Sept", "Sep","Oct", "Nov","Dec"));
//    }
//
//    private String handleDate(String dStr){
//        for (Iterator it=monthList.iterator();it.hasNext();){
//            String month=(String)it.next();
//            dStr = dStr.replaceFirst(month, dateDict.get(month));
//        }
//        return dStr;
//    }
//}
