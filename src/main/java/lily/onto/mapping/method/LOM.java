/************************************************
 * Source code information
 * -----------------------
 * Original author	 Peng Wang, School of Computer Science & Eng., Southeast University
 * Author email      pwangseu@gmail.com
 * Web               http://ontomapping.googlepages.com
 * Created			 2007-10-25
 * Filename          Rclm.java
 * Version           2.0
 * <p/>
 * Last modified on  2008-4-26
 * by  Peng Wang
 * -----------------------
 * Functions describe:
 * 基于元素局部上下文快照的大规模本体映射解决方法
 ***********************************************/
package lily.onto.mapping.method;

import cern.colt.list.DoubleArrayList;
import cern.colt.list.IntArrayList;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import lily.onto.handle.describe.OntDes;
import lily.onto.handle.graph.OntGraph;
import lily.onto.handle.propagation.LargeOntSimPropagation;
import lily.onto.parse.OWLOntParse;
import lily.tool.datastructure.*;
import lily.tool.filter.SimpleFilter;
import lily.tool.mappingfile.MappingFile;
import lily.tool.parameters.ParamStore;
import lily.tool.textsimilarity.TextDocSim;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.util.*;

/********结构体定义*********/
class nodeDegree implements Comparable {
    String name;
    int degree;

    public int compareTo(Object p) {
        if (this.degree >= ((nodeDegree) p).degree) {
            return 0;
        } else {
            return 1;
        }
    }
}

/**
 * *********************
 */
//@SuppressWarnings("unchecked")
public class LOM {

    /***************类变量*******************/
    /**********本体模型和文件操作参数**********/
    /**
     * 源和目标本体的model
     **/
    public OntModel m_source;
    public OntModel m_target;

    /**
     * base URI
     **/
    private String sourceBaseURI;
    private String targetBaseURI;

    /**********本体解析相关参数**********/
    /**
     * 本体解析实例
     **/
    public OWLOntParse ontParse = new OWLOntParse();

    /**
     * 概念数目
     **/
    private int sourceConceptNum;
    private int targetConceptNum;

    /**
     * 属性数目
     **/
    private int sourcePropNum;
    private int targetPropNum;

    /**
     * 实例数目
     **/
    private int sourceInsNum;
    private int targetInsNum;

    /**
     * 概念名
     **/
    private String[] sourceConceptName;
    private String[] targetConceptName;
    private HashMap sCnptName2Pos;
    private HashMap tCnptName2Pos;

    /**
     * 属性名
     **/
    private String[] sourcePropName;
    private String[] targetPropName;

    /**
     * 实例名
     **/
    private String[] sourceInsName;
    private String[] targetInsName;
    private HashMap sInsName2Pos;
    private HashMap tInsName2Pos;

    /**
     * 不局限于baseURI下的本体元素
     **/
    private int sourceFullConceptNum;
    private int sourceFullPropNum;
    private int sourceFullInsNum;

    private OntClass[] sourceFullConceptName;
    private OntProperty[] sourceFullPropName;
    private Individual[] sourceFullInsName;

    private int targetFullConceptNum;
    private int targetFullPropNum;
    private int targetFullInsNum;

    private OntClass[] targetFullConceptName;
    private OntProperty[] targetFullPropName;
    private Individual[] targetFullInsName;

    /**
     * 匿名资源
     **/
    private ArrayList sourceAnonCnpt;
    private ArrayList sourceAnonProp;
    private ArrayList sourceAnonIns;
    private ArrayList targetAnonCnpt;
    private ArrayList targetAnonProp;
    private ArrayList targetAnonIns;

    /********语义子图参数********/
    private ConceptSubGraph[] sourceCnptSubG;
    private PropertySubGraph[] sourcePropSubG;
    private ConceptSubGraph[] targetCnptSubG;
    private PropertySubGraph[] targetPropSubG;
    private ArrayList s_cnptStm;// 合并的概念子图
    private ArrayList s_propStm;// 合并的属性子图
    private ArrayList t_cnptStm;// 合并的概念子图
    private ArrayList t_propStm;// 合并的属性子图

    /********文本描述信息********/
    private TextDes[] sourceCnptTextDes;
    private TextDes[] sourcePropTextDes;
    private TextDes[] sourceInsTextDes;

    private TextDes[] targetCnptTextDes;
    private TextDes[] targetPropTextDes;
    private TextDes[] targetInsTextDes;

    /**********本体文件和映射结果文件相关参数**********/
    private String sourceOntFile;// 源和目标本体的文件
    private String targetOntFile;
    private String lilyFileName = "";// 映射结果的文件名
    private String refalignFile;// 基准映射结果的文件

    private int mappingNum;// 映射结果数
    private int cMappingNum;
    private int pMappingNum;// 分别记录几种映射的数目

    private MapRecord[] mappingResult;// 映射结果

    /*******压缩的稀疏相似矩阵**********/
    private SparseDoubleMatrix2D cnptSimMx;
    private SparseDoubleMatrix2D propSimMx;
    private SparseDoubleMatrix2D insSimMx;
    private SparseDoubleMatrix2D pgSimMxCnpt;
    private SparseDoubleMatrix2D pgSimMxProp;

    /**
     * 层次路径
     **/
    private ArrayList pathRec;
    private ArrayList allPath;

    /*快照中的新三元组*/
    private ArrayList newGenStm;

    /**
     * 已经做映射处理的点集合
     **/
    private HashSet mappedNode;

    /**
     * 元素快照参数
     **/
    private int snapSize = 1;
    private boolean isCnptSnap;
    private boolean isPropSnap;
    private int topk = 1;// topk参数
    private int nbScale = 3;//邻居的范围参数
    private double ptValue = 0.5;
    private double ntValue = 0.001;
    private int desCapValue = 5;
    private HashMap ntTopKNode;//Topk negative node集合
    private HashMap ntNbNode; //Neighbor negative node集合
    private int ntNum = 0;
    private int localNtNum = 0;
    private int localPtNum = 0;
    private double OKSIM = 0.5;
    private double BADSIM = 0.001;

    /*Anatomy专用的part-of Model*/
    private OntModel m_sourcePart;
    private OntModel m_targetPart;

    /**
     * 相似度阀值
     **/
    private double cnptSimThreshold = 0.25;// 概念相似阀值
    private double propSimThreshold = 0.25;// Property相似阀值

    //进度条通信变量
    private int[] pbarValue;
    //层次树结构
    private CnptTree sTree;
    private CnptTree tTree;

    /**
     * 统计得到的语义描述文本量
     **/
    private int[] sCnptDesCapacity;
    private int[] tCnptDesCapacity;

    /**
     * 源本体在语义子图中的相关概念
     **/
    private HashMap sCnptFriends;

    /**
     * 结果评价
     **/
    private double precision;
    private double recall;
    private double f1Measure;

    /**********相似度传播相关参数**********/

    /***************类变量*******************/

    public static void main(String[] args) {
        new LOM().run();
//		new LOM().bestThresholdEvaluation();
    }

    /***************************
     * 大本体映射处理入口
     ***************************/

    public ArrayList run() {
        ArrayList finalList = new ArrayList();
        /**1.读本体、解析本体、初始化参数**/
        loadLargeOnt();
        /**2.大本体映射处理**/
        largeOntMapping();
//        if (ParamStore.doEvaluation) {
//            /**3.映射结果评估**/
//            evaluate();
//            /**返回映射结果**/
//            finalList.add(0, mappingNum);
//            finalList.add(1, mappingResult);
//            finalList.add(2, f1Measure);
//        }
//		System.out.println("ntNum:"+ntNum);
//		System.out.println("ptNum:"+localPtNum+"ntNum:"+localNtNum);

        return finalList;
    }

    public void runAPI() {
        cnptSimMx = new SparseDoubleMatrix2D(sourceConceptNum, targetConceptNum);
        propSimMx = new SparseDoubleMatrix2D(sourcePropNum, targetPropNum);
        mappedNode = new HashSet();
        /*
        if ((sourceBaseURI+targetBaseURI).contains("http://mouse.owl")){
			cnptSimThreshold = 0.35;
			propSimThreshold = 0.4;
			snapSize = 15;
		}
		*/
        createEntitySnap();

		/*1C.合并的算法*/
        hybridAlgorithm();
				
		/*2.计算属性相似矩阵*/
        propSimCompute();/*属性相似按照原始方法计算*/
		
		/*3.相似度传播算法*/
        if (isNeedProg()) {
            simPropagation();			
		/*3.5 相似矩阵合并*/
            combineSimMx();
        }
				
		/*4.匹配结果后处理*/
		/*相似矩阵过滤*/
        cnptSimMx = new SimpleFilter().maxValueFilter(sourceConceptNum, targetConceptNum, cnptSimMx, cnptSimThreshold);
        propSimMx = new SimpleFilter().maxValueFilter(sourcePropNum, targetPropNum, propSimMx, propSimThreshold);

    }

    /***************************
     * 读入大本体，并进行预处理
     ***************************/
    private void loadLargeOnt() {
        /**1.设置本体文件**/
        loadConfigFile();
        setOntFile();
        /**2.解析本体**/
        System.out.println("Parsing Ontologies...");
        parseOnt();
        /**3.初始化**/
        init();
    }

    /***************************
     * 大本体映射处理过程
     ***************************/
    public void largeOntMapping() {
        /**1.构造元素上下文快照**/
        /**2.计算语义子图**/
        /**3.计算语义文档**/
        long start = System.currentTimeMillis();
        pbarValue[1] = 0;
        System.out.println("Preprocess...");

        if ((sourceBaseURI + targetBaseURI).contains("http://mouse.owl")) {  //当成是本体匹配参数调谐？
            cnptSimThreshold = 0.29;
            propSimThreshold = 0.4;
            snapSize = 15;
            topk = 10;
            nbScale = 0;
            ptValue = 0.5;
            ntValue = 0.185;
            OKSIM = 0.57;
            BADSIM = 0.1;
            OntDes.isSkipCnptName = true;
        }

        createEntitySnap();
        pbarValue[1] = 100;
        long T1_time = System.currentTimeMillis() - start;
        /**4.相似度计算**/
        start = System.currentTimeMillis();
        pbarValue[2] = 0; //进度条数值
        System.out.println("Matching...");
        computeSimMatrix();
        pbarValue[2] = 100; //进度条数值
        long T2_time = System.currentTimeMillis() - start;
//        if (ParamStore.doEvaluation) {
//            System.out.println("快照和预处理:" + (double) T1_time / 1000.0 + "秒");
//            System.out.println("相似度计算:" + (double) T2_time / 1000.0 + "秒");
//        }
    }

    /***************************************************************************
     * 设置本体文件
     **************************************************************************/
    private void setOntFile() {
//		sourceOntFile = new String("./dataset/OAEI2007/bench/benchmarks/101/onto.rdf");
//		sourceOntFile = "./dataset/OAEI2009/anatomy/mouse_anatomy_2008.xml";//mouse_anatomy.owl
//		sourceOntFile = "./dataset/foamData/russiaA.owl";
//		sourceOntFile = "./dataset/OAEI2007/food/agrovoc_oaei2007.owl";
//		sourceOntFile = "./dataset/OAEI2008/library/Brinkman_OAEI.owl";
//		sourceOntFile = "./dataset/OAEI2008/fao/AGROVOC/ag_oaei2008.owl";
//		sourceOntFile = "./dataset/OAEI2011/anatomy/mouse_anatomy_2010.owl";

//		targetOntFile = new String("./dataset/OAEI2007/bench/benchmarks/238/onto.rdf");
//		targetOntFile = "./dataset/OAEI2009/anatomy/nci_anatomy_2008.xml";
//		targetOntFile = "./dataset/foamData/russiaA.owl";
//		targetOntFile = "./dataset/OAEI2007/food/nalt_oaei2007.owl";
//		targetOntFile = "./dataset/OAEI2008/library/GTT_OAEI.owl";
//		targetOntFile = "./dataset/OAEI2008/fao/asfa/asfa-d.owl";
//		targetOntFile = "./dataset/OAEI2011/anatomy/nci_anatomy_2010.owl";

//		refalignFile = new String("./dataset/OAEI2007/bench/benchmarks/238/refalign.rdf");
//		refalignFile = "./dataset/OAEI2008/anatomy/AOAS.rdf";//AOAS.rdf reference_partial.rdf
//		refalignFile = "./dataset/OAEI2007/food/falcon.xml";
//		refalignFile = "./dataset/OAEI2008/library/falcon.xml";
//		refalignFile = "./dataset/OAEI2009/anatomy/SAMBO.rdf";
//		refalignFile = "./dataset/OAEI2011/anatomy/mouse_anatomy_reference_2010.rdf";

//		lilyFileName = "Lily-best";
        System.out.println("Source Large Ontology:" + sourceOntFile);
        System.out.println("Target Large Ontology:" + targetOntFile);
    }

    /***************************************************************************
     * 解析本体
     **************************************************************************/
    public void parseOnt() {
        ArrayList list = new ArrayList();

        if (pbarValue == null) pbarValue = new int[4];
        pbarValue[0] = 0; //进度条控制

        // 源本体----------------------------------------------
        m_source = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
        // The ontology file information
        m_source.getDocumentManager().addAltEntry("http://pengwang/", sourceOntFile);
        // Read the reference ontology file
        ontParse.readOntFile(m_source, sourceOntFile);

        pbarValue[0] = 0; //进度条控制

        // 源本体的base URI
        sourceBaseURI = ontParse.getOntBaseURI(m_source);

        // 类型修补
        ontParse.repairOntType(m_source);

        // Get all Classes of Ontology
        list = ontParse.listAllConceptsFilterBaseURI(m_source, sourceBaseURI);
        sourceConceptNum = (Integer) list.get(0);
        sourceConceptName = new String[sourceConceptNum];
        sourceConceptName = (String[]) ((ArrayList) list.get(1)).toArray(new String[0]);

        // Get all datatype properties
        list = ontParse.listAllDatatypeRelationsURI(m_source, sourceBaseURI);
        int sourceDataPropNum = (Integer) list.get(0);
        String[] sourceDataPropName = new String[sourceDataPropNum];
        sourceDataPropName = (String[]) ((ArrayList) list.get(1)).toArray(new String[0]);

        // Get all object properties
        list = ontParse.listAllObjectRelationsURI(m_source, sourceBaseURI);
        int sourceObjPropNum = (Integer) list.get(0);
        String[] sourceObjPropName = new String[sourceObjPropNum];
        sourceObjPropName = (String[]) ((ArrayList) list.get(1)).toArray(new String[0]);

        // Get all properties
        sourcePropNum = sourceDataPropNum + sourceObjPropNum;
        sourcePropName = new String[sourcePropNum];
        System.arraycopy(sourceDataPropName, 0, sourcePropName, 0, sourceDataPropNum);
        System.arraycopy(sourceObjPropName, 0, sourcePropName, 0 + sourceDataPropNum, sourceObjPropNum);

        // get all instances
        list = ontParse.listAllInstances(m_source);
        sourceInsNum = (Integer) list.get(0);
        sourceInsName = new String[sourceInsNum];
        sourceInsName = (String[]) ((ArrayList) list.get(1)).toArray(new String[0]);

        pbarValue[0] = 30; //进度条控制
		
		/* 不局限于baseURI的本体信息 */
        ArrayList fullOntlist = ontParse.getFullOntInfo(m_source);
        // 概念信息
        list = (ArrayList) fullOntlist.get(0);
        sourceFullConceptNum = (Integer) list.get(0);
//		sourceFullConceptName = new OntClass[sourceFullConceptNum];
//		sourceFullConceptName = (OntClass[]) ((ArrayList) list.get(1)).toArray(new OntClass[0]);
        // 属性信息
        list = (ArrayList) fullOntlist.get(1);
        sourceFullPropNum = (Integer) list.get(0);
//		sourceFullPropName = new OntProperty[sourceFullPropNum];
//		sourceFullPropName = (OntProperty[]) ((ArrayList) list.get(1)).toArray(new OntProperty[0]);
        // DatatypeProperty
        list = (ArrayList) fullOntlist.get(2);
        int sourceFullDataPropNum = (Integer) list.get(0);
        DatatypeProperty[] sourceFullDataPropName = new DatatypeProperty[sourceFullDataPropNum];
        sourceFullDataPropName = (DatatypeProperty[]) ((ArrayList) list.get(1)).toArray(new DatatypeProperty[0]);
        // ObjectProperty
        // DatatypeProperty
        list = (ArrayList) fullOntlist.get(3);
        int sourceFullObjPropNum = (Integer) list.get(0);
        ObjectProperty[] sourceFullObjPropName = new ObjectProperty[sourceFullObjPropNum];
        sourceFullObjPropName = (ObjectProperty[]) ((ArrayList) list.get(1)).toArray(new ObjectProperty[0]);
        // 实例信息
        list = (ArrayList) fullOntlist.get(4);
        sourceFullInsNum = (Integer) list.get(0);
//		sourceFullInsName = new Individual[sourceFullInsNum];
//		sourceFullInsName = (Individual[]) ((ArrayList) list.get(1)).toArray(new Individual[0]);

        // 匿名资源
        sourceAnonCnpt = new ArrayList();
        sourceAnonProp = new ArrayList();
        sourceAnonIns = new ArrayList();
        list = ontParse.getOntAnonInfo(m_source);
        sourceAnonCnpt = (ArrayList) list.get(0);
        sourceAnonProp = (ArrayList) list.get(1);
        sourceAnonIns = (ArrayList) list.get(2);

        pbarValue[0] = 50; //进度条控制

        // 目标本体---------------------------------------------
        m_target = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
        // The ontology file information
        m_target.getDocumentManager().addAltEntry("http://LiSun/", targetOntFile);

        // Read the target ontology file
        ontParse.readOntFile(m_target, targetOntFile);

        pbarValue[0] = 60; //进度条控制

        // 目标本体的base URI
        targetBaseURI = ontParse.getOntBaseURI(m_target);

        // 类型修补
        ontParse.repairOntType(m_target);

        // Get all Classes of Ontology
        list = ontParse.listAllConceptsFilterBaseURI(m_target, targetBaseURI);
        targetConceptNum = (Integer) list.get(0);
        targetConceptName = new String[targetConceptNum];
        targetConceptName = (String[]) ((ArrayList) list.get(1)).toArray(new String[0]);

        // Get all datatype properties
        list = ontParse.listAllDatatypeRelationsURI(m_target, targetBaseURI);
        int targetDataPropNum = (Integer) list.get(0);
        String[] targetDataPropName = new String[targetDataPropNum];
        targetDataPropName = (String[]) ((ArrayList) list.get(1)).toArray(new String[0]);

        // Get all object properties
        list = ontParse.listAllObjectRelationsURI(m_target, targetBaseURI);
        int targetObjPropNum = (Integer) list.get(0);
        String[] targetObjPropName = new String[targetObjPropNum];
        targetObjPropName = (String[]) ((ArrayList) list.get(1)).toArray(new String[0]);

        // Get all properties
        targetPropNum = targetDataPropNum + targetObjPropNum;
        targetPropName = new String[targetPropNum];
        for (int i = 0; i < targetDataPropNum; i++) {
            targetPropName[i] = targetDataPropName[i];
        }
        for (int i = 0; i < targetObjPropNum; i++) {
            targetPropName[i + targetDataPropNum] = targetObjPropName[i];
        }

        // get all instances
        list = ontParse.listAllInstances(m_target);
        targetInsNum = (Integer) list.get(0);
        targetInsName = new String[targetInsNum];
        targetInsName = (String[]) ((ArrayList) list.get(1)).toArray(new String[0]);

        pbarValue[0] = 80; //进度条控制
		
		/* 不局限于baseURI的本体信息 */
        fullOntlist = ontParse.getFullOntInfo(m_target);
        // 概念信息
        list = (ArrayList) fullOntlist.get(0);
        targetFullConceptNum = (Integer) list.get(0);
//		targetFullConceptName = new OntClass[targetFullConceptNum];
//		targetFullConceptName = (OntClass[]) ((ArrayList) list.get(1)).toArray(new OntClass[0]);
        // 属性信息
        list = (ArrayList) fullOntlist.get(1);
        targetFullPropNum = (Integer) list.get(0);
//		targetFullPropName = new OntProperty[targetFullPropNum];
//		targetFullPropName = (OntProperty[]) ((ArrayList) list.get(1)).toArray(new OntProperty[0]);
        // DatatypeProperty
        list = (ArrayList) fullOntlist.get(2);
        int targetFullDataPropNum = (Integer) list.get(0);
        DatatypeProperty[] targetFullDataPropName = new DatatypeProperty[targetFullDataPropNum];
        targetFullDataPropName = (DatatypeProperty[]) ((ArrayList) list.get(1)).toArray(new DatatypeProperty[0]);
        // ObjectProperty
        // DatatypeProperty
        list = (ArrayList) fullOntlist.get(3);
        int targetFullObjPropNum = (Integer) list.get(0);
        ObjectProperty[] targetFullObjPropName = new ObjectProperty[targetFullObjPropNum];
        targetFullObjPropName = (ObjectProperty[]) ((ArrayList) list.get(1)).toArray(new ObjectProperty[0]);
        // 实例信息
        list = (ArrayList) fullOntlist.get(4);
        targetFullInsNum = (Integer) list.get(0);
//		targetFullInsName = new Individual[targetFullInsNum];
//		targetFullInsName = (Individual[]) ((ArrayList) list.get(1)).toArray(new Individual[0]);

        // 匿名资源
        targetAnonCnpt = new ArrayList();
        targetAnonProp = new ArrayList();
        targetAnonIns = new ArrayList();
        list = ontParse.getOntAnonInfo(m_target);
        targetAnonCnpt = (ArrayList) list.get(0);
        targetAnonProp = (ArrayList) list.get(1);
        targetAnonIns = (ArrayList) list.get(2);
        list = null;

        pbarValue[0] = 100; //进度条控制
    }

    /***************************************************************************
     * 初始化，主要是基本的参数设置
     **************************************************************************/
    private void init() {
        /**调整目标本体的概念次序，以减少negative set的空间**/
        turnTargetCnptPos();

        /**概念名到位置的映射**/
        sCnptName2Pos = new HashMap();
        for (int i = 0; i < sourceConceptNum; i++) {
            sCnptName2Pos.put(sourceConceptName[i], i);
        }
        tCnptName2Pos = new HashMap();
        for (int i = 0; i < targetConceptNum; i++) {
            tCnptName2Pos.put(targetConceptName[i], i);
        }

        /**实例名到位置的映射**/
        sInsName2Pos = new HashMap();
        for (int i = 0; i < sourceInsNum; i++) {
            sInsName2Pos.put(sourceInsName[i], i);
        }
        tInsName2Pos = new HashMap();
        for (int i = 0; i < targetInsNum; i++) {
            tInsName2Pos.put(targetInsName[i], i);
        }

        ntTopKNode = new HashMap();
        ntNbNode = new HashMap();

        sCnptFriends = new HashMap();

        m_sourcePart = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
        m_targetPart = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);

		/*预处理Anatomy数据*/
        if ((sourceBaseURI + targetBaseURI).contains("http://mouse.owl") &&
                (sourceBaseURI + targetBaseURI).contains("http://human.owl")) {
            preProcessAnatomy(m_source, sourceBaseURI, sourceConceptName, sourceConceptNum);
            preProcessAnatomy(m_target, targetBaseURI, targetConceptName, targetConceptNum);
        }

        /**构造概念层次树**/
        sTree = new CnptTree();
        buildCnptTree(sTree, true);
        tTree = new CnptTree();
        buildCnptTree(tTree, false);

    }

    /*******************
     * 构造概念层次树
     *********************/
    private void buildCnptTree(CnptTree tree, boolean flag) {
        tree.concept = null;
        tree.child = new ArrayList();
        if (flag) {
            for (int i = 0; i < sourceConceptNum; i++) {
                OntClass sc = m_source.getOntClass(sourceBaseURI + sourceConceptName[i]);
				/*判断当前概念是否是根节点*/
                if (sc != null && ontParse.isCnptHRoot(m_source, sc)) {
                    CnptTree newChild = new CnptTree();
                    newChild.concept = sc;
                    newChild.child = new ArrayList();
                    tree.child.add(newChild);
                }
            }
        } else {
            for (int i = 0; i < targetConceptNum; i++) {
                OntClass sc = m_target.getOntClass(targetBaseURI + targetConceptName[i]);
				/*判断当前概念是否是根节点*/
                if (sc != null && ontParse.isCnptHRoot(m_target, sc)) {
                    CnptTree newChild = new CnptTree();
                    newChild.concept = sc;
                    newChild.child = new ArrayList();
                    tree.child.add(newChild);
                }
            }
        }

        for (Iterator it = tree.child.iterator(); it.hasNext(); ) {
            CnptTree node = (CnptTree) it.next();
            getOneSubTree(node);
        }

    }


    private void getOneSubTree(CnptTree node) {
        OntClass c = node.concept;
        ArrayList childSet = ontParse.listDirectSubClassOfConcept(c);
        if (childSet.isEmpty()) {
            return;
        } else {
            for (Iterator it = childSet.iterator(); it.hasNext(); ) {
                OntClass tc = (OntClass) it.next();
                CnptTree newNode = new CnptTree();
                newNode.concept = tc;
                newNode.child = new ArrayList();
                node.child.add(newNode);
                getOneSubTree(newNode);
            }
        }
    }

    /***************************
     * 映射结果评估
     ***************************/
//    public void evaluate() {
//        ArrayList list = new ArrayList();
//        MapRecord[] refMapResult = null;
//        int refMapNum = 0;
//        if (refalignFile == null || refalignFile.length() == 0) {
//            return;
//        }
//        // 读出标准结果
//        try {
//            list = new MappingFile().read4xml(refalignFile);
//            refMapNum = ((Integer) list.get(0)).intValue();
//            refMapResult = new MapRecord[refMapNum];
//            refMapResult = (MapRecord[]) ((ArrayList) list.get(1)).toArray(new MapRecord[0]);
//        } catch (MalformedURLException e) {
//            System.out.println("Can't open refalign result file!" + e.toString());
//        } catch (DocumentException e) {
//            System.out.println("Can't open refalign result file!" + e.toString());
//        }
//        // 输入给评估类
//        list = new EvaluateMapping().getEvaluation(refMapNum, refMapResult, mappingNum, mappingResult);
//        precision = ((Double) list.get(0)).doubleValue();
//        recall = ((Double) list.get(1)).doubleValue();
//        f1Measure = ((Double) list.get(2)).doubleValue();
//    }
    public String handleResult() {
        // 根据相似矩阵，生成映射结果
        mappingResult = new MapRecord[Math.max(sourceConceptNum, targetConceptNum)
                + Math.max(sourcePropNum, targetPropNum)];
        produceMapResult(cnptSimMx, propSimMx);


        // 保存映射结果到一个String
        MappingFile mapFile = new MappingFile();
        mapFile.setBaseURI(sourceBaseURI, targetBaseURI);
        return mapFile.save2rdfstring(sourceOntFile, targetOntFile, mappingNum, mappingResult);
    }

    /***************************
     * 构造元素上下文快照
     ***************************/
    private void createEntitySnap() {
        int pbarCount = 0;
        int pbarTotal = sourceConceptNum + sourcePropNum + targetConceptNum + targetPropNum;

        /**1.1构造源本体概念上下文快照**/
        isCnptSnap = true;
        isPropSnap = false;
        newGenStm = new ArrayList();

        sourceCnptTextDes = new TextDes[sourceConceptNum];
        for (int i = 0; i < sourceConceptNum; i++) {
            sourceCnptTextDes[i] = new TextDes();
        }
		
		/*Statement HashMap*/
        ArrayList sourceStm = (ArrayList) m_source.listStatements().toList();
        HashMap sourceStmHash = buildStmHash(sourceStm);

        s_cnptStm = new ArrayList();
        for (int i = 0; i < sourceConceptNum; i++) {
            pbarCount++;
            pbarValue[1] = (int) (100.0 * (double) pbarCount / (double) pbarTotal);

            OntClass c = m_source.getOntClass(sourceBaseURI + sourceConceptName[i]);
//			System.out.println(i+":"+sourceConceptName[i]);
			/*提取concept子本体片段*/
            OntModel s_cnptSubModel = getResSnapshot(c, m_source, snapSize);
//			OntModel s_cnptSubModel = getResSnapshotQuick(c, m_source);
			/*计算语义子图和语义文本*/
            Rclm ontM = new Rclm();
            ontM.sourceBaseURI = sourceBaseURI;
            sourceCnptTextDes[i] = ontM.getOneCnptDes(s_cnptSubModel, sourceConceptName[i]);
            ArrayList lt = ontM.sourceCnptSubG[0].stmList;
			/*统计语义子图中出现的其它概念*/
            Set tset = new HashSet();
            for (Iterator it = lt.iterator(); it.hasNext(); ) {
                Statement st = (Statement) it.next();
                Resource sub = st.getSubject();
                RDFNode obj = st.getObject();
                String subName = sub.getLocalName();
                String objName = "";
                if (s_cnptSubModel.getOntClass(obj.toString()) != null) {
                    objName = s_cnptSubModel.getOntClass(obj.toString()).getLocalName();
                }
                if (sCnptName2Pos.containsKey(subName)) {
                    tset.add(sCnptName2Pos.get(subName));
                }
                if (sCnptName2Pos.containsKey(objName)) {
                    tset.add(sCnptName2Pos.get(objName));
                }
            }
			/*合并语义子图*/
//			combineSubGraph(s_cnptStm,lt,sourceStmHash);
//			System.out.println("cnptStm:"+s_cnptStm.size()+"--lt:"+lt.size()+"--total:"+m_source.size());
			
			/*友元*/
            sCnptFriends.put(i, tset);

            ontM = null;
            s_cnptSubModel = null;
            tset = null;
            lt = null;
        }
		/*恢复s_cnptStm*/
        reverseStmHash(s_cnptStm, sourceStm);
        s_cnptStm.addAll(newGenStm);
        newGenStm.clear();
        System.gc();

        /**1.2构造源本体属性上下文快照**/
        isCnptSnap = false;
        isPropSnap = true;

        sourcePropTextDes = new TextDes[sourcePropNum];
        for (int i = 0; i < sourcePropNum; i++) {
            sourcePropTextDes[i] = new TextDes();
        }//描述文档初始化

        s_propStm = new ArrayList();
        for (int i = 0; i < sourcePropNum; i++) {
            pbarCount++;
            pbarValue[1] = (int) (100.0 * (double) pbarCount / (double) pbarTotal);

            OntProperty p = m_source.getOntProperty(sourceBaseURI + sourcePropName[i]);
//			System.out.println(i+":"+sourcePropName[i]);
	
			/*提取property子本体片段*/
            OntModel s_propSubModel = getResSnapshot(p, m_source, snapSize);
            s_propSubModel.setNsPrefix("", sourceBaseURI);
			/*计算语义子图和语义文本*/
            Rclm ontM = new Rclm();
            ontM.sourceBaseURI = sourceBaseURI;
            sourcePropTextDes[i] = ontM.getOnePropDes(s_propSubModel, sourcePropName[i]);
            ArrayList lt = ontM.sourcePropSubG[0].stmList;
			/*合并语义子图*/
            combineSubGraph(s_propStm, lt, sourceStmHash);

            ontM = null;
            s_propSubModel = null;
            lt = null;
        }
        reverseStmHash(s_propStm, sourceStm);
        s_propStm.addAll(newGenStm);
        newGenStm.clear();
        sourceStm.clear();
        sourceStm = null;
        sourceStmHash.clear();
        sourceStmHash = null;
        System.gc();

        /**2.1构造目标本体概念上下文快照**/
        isCnptSnap = true;
        isPropSnap = false;

        targetCnptTextDes = new TextDes[targetConceptNum];
        for (int i = 0; i < targetConceptNum; i++) {
            targetCnptTextDes[i] = new TextDes();
        }
		
		/*Statement HashMap*/
        ArrayList targetStm = (ArrayList) m_target.listStatements().toList();
        HashMap targetStmHash = buildStmHash(targetStm);

        t_cnptStm = new ArrayList();
        for (int i = 0; i < targetConceptNum; i++) {
            pbarCount++;
            pbarValue[1] = (int) (100.0 * (double) pbarCount / (double) pbarTotal);

            OntClass c = m_target.getOntClass(targetBaseURI + targetConceptName[i]);
//			if (c==null) {System.out.println("debug error:"+targetConceptName[i]);}
//			System.out.println(i+":"+targetConceptName[i]);
			/*提取concept子本体片段*/
            OntModel s_cnptSubModel = getResSnapshot(c, m_target, snapSize);
//			OntModel s_cnptSubModel = getResSnapshotQuick(c, m_target);
			/*计算语义子图和语义文本*/
            Rclm ontM = new Rclm();
            ontM.sourceBaseURI = targetBaseURI;
            targetCnptTextDes[i] = ontM.getOneCnptDes(s_cnptSubModel, targetConceptName[i]);
            ArrayList lt = ontM.sourceCnptSubG[0].stmList;
			/*合并语义子图*/
//			combineSubGraph(t_cnptStm,lt,targetStmHash);	

            ontM = null;
            s_cnptSubModel = null;
            lt = null;
        }
        reverseStmHash(t_cnptStm, targetStm);
        t_cnptStm.addAll(newGenStm);
        newGenStm.clear();
        System.gc();

        /**2.2构造目标本体属性上下文快照**/
        isCnptSnap = false;
        isPropSnap = true;

        targetPropTextDes = new TextDes[targetPropNum];
        for (int i = 0; i < targetPropNum; i++) {
            targetPropTextDes[i] = new TextDes();
        }

        t_propStm = new ArrayList();
        for (int i = 0; i < targetPropNum; i++) {
            pbarCount++;
            pbarValue[1] = (int) (100.0 * (double) pbarCount / (double) pbarTotal);

            OntProperty p = m_target.getOntProperty(targetBaseURI + targetPropName[i]);
//			System.out.println(i+":"+targetPropName[i]);
			/*提取property子本体片段*/
            OntModel s_propSubModel = getResSnapshot(p, m_target, snapSize);
            s_propSubModel.setNsPrefix("", targetBaseURI);
			/*计算语义子图和语义文本*/
            Rclm ontM = new Rclm();
            ontM.sourceBaseURI = targetBaseURI;
            targetPropTextDes[i] = ontM.getOnePropDes(s_propSubModel, targetPropName[i]);

            ArrayList lt = ontM.sourcePropSubG[0].stmList;
			/*合并语义子图*/
            combineSubGraph(t_propStm, lt, targetStmHash);

            ontM = null;
            s_propSubModel = null;
            lt = null;
        }
        reverseStmHash(t_propStm, targetStm);
        t_propStm.addAll(newGenStm);
        newGenStm.clear();
        targetStm.clear();
        targetStm = null;
        targetStmHash.clear();
        targetStmHash = null;
        System.gc();
    }

    private OntClass findCnptInFullName(String s, OntClass[] fullConceptName, int fullConceptNum) {
        OntClass c = null;
        for (int i = 0; i < fullConceptNum; i++) {
            if (fullConceptName[i].toString().equals(s)) {
                c = fullConceptName[i];
                break;
            }
        }
        return c;
    }

    private OntProperty findPropInFullName(String s, OntProperty[] fullPropName, int fullPropNum) {
        OntProperty c = null;
        for (int i = 0; i < fullPropNum; i++) {
            if (fullPropName[i].toString().equals(s)) {
                c = fullPropName[i];
                break;
            }
        }
        return c;
    }


    /***************************
     * 遍历资源的快照算法
     ***************************/
    private OntModel getResSnapshot(Resource r, OntModel orgModel, int size) {
        OntModel outModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        OWLOntParse ontParse = new OWLOntParse();
        Set Sg = new HashSet();//快照的三元组集合
        Set Sc = new HashSet();//候选的三元组集合
        HashMap node2Drg = new HashMap();//记录候选三元组集合中的点的度 string-int
        HashMap node2Triple = new HashMap();//记录点所对应的三元组 string-Set
        HashMap node2InverDrg = new HashMap();//记录候选三元组集合中的点的入度和 string-int
        Set inNode = new HashSet();//已经在快照中的点
        HashMap inNode2Dw = new HashMap();//快照中点的依赖度
        HashMap triple2Dw = new HashMap();//候选三元组的依赖度
        HashMap anno2Res = new HashMap();//维护匿名节点
        ArrayList axiom = new ArrayList();//元素的公理集合
        Set tempSet = new HashSet();

        /**1.初始快照**/
        inNode.add(r.toString());
        inNode2Dw.put(r.toString(), 1.0);
        /**1.5 如果是属性，为了避免初始种子为空，将属性的实例化三元组放入种子中**/
        if (isPropSnap) {
            handlePropSeed(r, r, orgModel, Sc, Sg, anno2Res, node2Triple, inNode, inNode2Dw);
            Selector selector = new SimpleSelector(r, null, (RDFNode) null);
            for (StmtIterator Iter = orgModel.listStatements(selector); Iter.hasNext(); ) {
                Statement st = Iter.next();
                outModel.add(st);
            }
            return outModel;
        }
        /**初始种子构造**/
        genSnapCandidate(r, r, orgModel, Sc, Sg, anno2Res, node2Triple,
                node2Drg, inNode, node2InverDrg, triple2Dw, inNode2Dw);
        ArrayList lt = new ArrayList();
        lt.addAll(Sc);
        axiom = (ArrayList) lt.clone();
        /** 2.迭代计算元素快照**/
        while (Sg.size() < size && !Sc.isEmpty()) {
            /**2.1 从候选三元组中选出最大依赖度的三元组，放入快照中**/
            Statement curSt = null;
            double maxw = 0;
            for (Iterator itx = triple2Dw.keySet().iterator(); itx.hasNext(); ) {
                Statement st = (Statement) itx.next();
                double w = (Double) triple2Dw.get(st);
                if (w > maxw) {
                    maxw = w;
                    curSt = st;
                }
            }
            Sg.add(curSt);

            /**2.2 更新相关数据结构**/
    		/*从候选集中删除*/
            Sc.remove(curSt);
    		/*从点所对应的三元组集中删除*/
//    		for (Iterator itx=node2Triple.values().iterator();itx.hasNext();){
//    			Set tset=(Set)itx.next();
//    			if (tset.contains(curSt)){
//    				tset.remove(curSt);
//    			}
//    		}
//    		tempSet=new HashSet();
//    		for (Iterator itx=node2Triple.keySet().iterator();itx.hasNext();){
//    			String nodeName=(String)itx.next();
//    			if (((Set)node2Triple.get(nodeName)).isEmpty()){
//    				tempSet.add(nodeName);
//    			}
//    		}
//    		for (Iterator itx=tempSet.iterator();itx.hasNext();){
//    			String name=(String)itx.next();
//    			node2Triple.remove(name);
//    		}
    		/*更新已在快照中的点*/
            String nodeA = curSt.getSubject().toString();
            String nodeB = curSt.getObject().toString();
            String inName = nodeA;
            String outName = nodeB;
            RDFNode newRDFNode = curSt.getObject();
            Resource newRes = orgModel.getResource(outName);
            if (ontParse.isBlankNode(outName)) {
                newRes = (Resource) anno2Res.get(outName);
            }
            if (!inNode.contains(nodeA)) {
                inName = nodeB;
                outName = nodeA;
                newRDFNode = curSt.getSubject();
                newRes = curSt.getSubject();
            }
            inNode.add(outName);    		
    		/*更新快照点的依赖度*/
            double tpw = (Double) triple2Dw.get(curSt);
            if (!inNode2Dw.containsKey(outName)) {
                inNode2Dw.put(outName, tpw);
            } else {
                double tpw2 = (Double) inNode2Dw.get(outName);
                inNode2Dw.put(outName, Math.max(tpw, tpw2));
            }
    		/*从候选三元组到依赖度的映射中删除*/
            triple2Dw.remove(curSt);

            /**2.3 更新候选三元组集合**/
            genSnapCandidate(newRes, newRDFNode, orgModel, Sc, Sg, anno2Res, node2Triple,
                    node2Drg, inNode, node2InverDrg, triple2Dw, inNode2Dw);
        }

        /** 3.加入遗漏公理**/
    	/*简单处理为快照必须包含初始的候选集合*/
        int maxAxiomCount = 0;
        for (Iterator itx = axiom.iterator(); itx.hasNext(); ) {
            Statement st = (Statement) itx.next();
            if (!Sg.contains(st)) {
                maxAxiomCount++;
                Sg.add(st);
            }
            if (maxAxiomCount > 800) {
                break;
            }
        }

        /**3.5 处理Sg为空的特殊情况**/
        if (Sg.isEmpty()) {
            Selector selector = new SimpleSelector(r, null, (RDFNode) null);
            for (StmtIterator Iter = orgModel.listStatements(selector); Iter.hasNext(); ) {
                Statement st = Iter.next();
                Sg.add(st);
            }
        }

//    	/**3.6 在m_partof中把part-of的信息都添加进来**/
//       	if (r.getURI().contains(sourceBaseURI)){
//    		Selector selector = new SimpleSelector(r,null,(RDFNode)null);
//        	for (StmtIterator Iter = m_sourcePart.listStatements( selector);Iter.hasNext();)
//        	{
//        		Statement st = (Statement) Iter.next();
//        		Sg.add(st);
//        		System.out.println("添加："+st.toString());
//        	}
//        	selector = new SimpleSelector(null,null,(RDFNode)r);
//        	for (StmtIterator Iter = m_sourcePart.listStatements(selector);Iter.hasNext();)
//        	{
//        		Statement st = (Statement) Iter.next();
//        		Sg.add(st);
//        		System.out.println("添加："+st.toString());
//        	}
//    	}
//    	
//    	if (r.getURI().contains(targetBaseURI)){
//    		Selector selector = new SimpleSelector(r,null,(RDFNode)null);
//        	for (StmtIterator Iter = m_targetPart.listStatements( selector);Iter.hasNext();)
//        	{
//        		Statement st = (Statement) Iter.next();
//        		Sg.add(st);
//        		System.out.println("添加："+st.toString());
//        	}
//        	selector = new SimpleSelector(null,null,(RDFNode)r);
//        	for (StmtIterator Iter = m_targetPart.listStatements(selector);Iter.hasNext();)
//        	{
//        		Statement st = (Statement) Iter.next();
//        		Sg.add(st);
//        		System.out.println("添加："+st.toString());
//        	}
//    	}
    	
		/*1.寻找直接相关的三元组*/
		/*1.1以r开头的三元组*/
        Selector selector = new SimpleSelector(r, null, (RDFNode) null);
        for (StmtIterator Iter = orgModel.listStatements(selector); Iter.hasNext(); ) {
            Statement st = Iter.next();
            Sg.add(st);
//    		System.out.println("添加："+st.toString());
        }
		/*1.2以r结尾的三元组*/
        selector = new SimpleSelector(null, null, r);
        for (StmtIterator Iter = orgModel.listStatements(selector); Iter.hasNext(); ) {
            Statement st = Iter.next();
            Sg.add(st);
//    		System.out.println("添加："+st.toString());
        }
		
    	/*2.寻找概念层次结构*/
    	/*2.1父概念*/
        OntClass c = orgModel.getOntClass(r.toString());
        Selector slx = new SimpleSelector(c, orgModel.getProperty("http://www.w3.org/2000/01/rdf-schema#subClassOf"), (RDFNode) null);
//		for (StmtIterator itx=orgModel.listStatements(slx);itx.hasNext();){
//			Statement stx=(Statement)itx.next();
//			Sg.add(stx);
//			
//	    	OntClass c2 = orgModel.getOntClass(stx.getObject().toString());
//	    	Selector sly=new SimpleSelector(c2,orgModel.getProperty("http://www.w3.org/2000/01/rdf-schema#subClassOf"),(RDFNode)null);
//			for (StmtIterator ity=orgModel.listStatements(sly);ity.hasNext();){
//				Statement sty=(Statement)ity.next();
//				Sg.add(sty);
//			}			
//		}
//    	
//    	/*2.2子概念*/
//		slx=new SimpleSelector(null,orgModel.getProperty("http://www.w3.org/2000/01/rdf-schema#subClassOf"),(RDFNode)c);
//		for (StmtIterator itx=orgModel.listStatements(slx);itx.hasNext();){
//			Statement stx=(Statement)itx.next();
//			Sg.add(stx);
//			
//			OntClass c2 = orgModel.getOntClass(stx.getSubject().toString());
//			Selector sly=new SimpleSelector(null,orgModel.getProperty("http://www.w3.org/2000/01/rdf-schema#subClassOf"),(RDFNode)c2);
//			for (StmtIterator ity=orgModel.listStatements(sly);ity.hasNext();){
//				Statement sty=(Statement)ity.next();
//				Sg.add(sty);
//			}			
//		}
    	
		
    	/*3.part-of结构*/
        if (r.getURI().contains(sourceBaseURI)) {
            selector = new SimpleSelector(r, null, (RDFNode) null);
            for (StmtIterator Iter = m_sourcePart.listStatements(selector); Iter.hasNext(); ) {
                Statement st = Iter.next();
                Sg.add(st);
//        		System.out.println("添加："+st.toString());
            }
            selector = new SimpleSelector(null, null, r);
            for (StmtIterator Iter = m_sourcePart.listStatements(selector); Iter.hasNext(); ) {
                Statement st = Iter.next();
                Sg.add(st);
//        		System.out.println("添加："+st.toString());
            }
        }

        if (r.getURI().contains(targetBaseURI)) {
            selector = new SimpleSelector(r, null, (RDFNode) null);
            for (StmtIterator Iter = m_targetPart.listStatements(selector); Iter.hasNext(); ) {
                Statement st = Iter.next();
                Sg.add(st);
//        		System.out.println("添加："+st.toString());
            }
            selector = new SimpleSelector(null, null, r);
            for (StmtIterator Iter = m_targetPart.listStatements(selector); Iter.hasNext(); ) {
                Statement st = Iter.next();
                Sg.add(st);
//        		System.out.println("添加："+st.toString());
            }
        }

    	
    	
    	/*4.同义词处理*/
    	/*Class在Part Model中的声明*/
//		selector = new SimpleSelector(c,orgModel.getProperty("http://www.geneontology.org/formats/oboInOwl#hasRelatedSynonym"),(RDFNode)null);
//		ArrayList labelLt=new ArrayList();
//    	String newComment="";
//		/*寻找同义词入口*/
//    	for (StmtIterator itx = orgModel.listStatements(selector);itx.hasNext();)
//    	{
//    		Statement st = (Statement) itx.next();
//    		
//    		String rStr = st.getObject().toString();
//    		String curStr = rStr.substring(rStr.indexOf("#")+1);
//    		newComment = newComment+curStr+" ";
//    		slx=new SimpleSelector((Resource)st.getObject(),orgModel.getProperty("http://www.w3.org/2000/01/rdf-schema#label"),(RDFNode)null);
//    		/*取出label*/
//    		for (StmtIterator ity=orgModel.listStatements(slx);ity.hasNext();)
//    		{
//    			Statement stemp=(Statement)ity.next();
//    			labelLt.add(stemp.getObject());
//    		}        		
//    	}
//		/*将同义词作为comment加入*/
//
//    	for (Iterator itx=labelLt.iterator();itx.hasNext();){
//    		String rStr=((RDFNode)itx.next()).toString();
//    		String curStr=rStr.substring(0,rStr.indexOf("^^"));
//    		newComment=newComment+curStr+" ";
//    	}
//    	if (newComment.length()>1){
//    		Literal liT=orgModel.createLiteral(newComment);
//    		if (c!=null){
//    			c.addComment(liT);
//    		}    		
//    	}		


        /** 4.完善声明、定义和注释**/
        Set remainSt = new HashSet();
        for (Iterator itx = Sg.iterator(); itx.hasNext(); ) {
            Statement st = (Statement) itx.next();
            Resource sub = st.getSubject();
            Property prop = st.getPredicate();
            RDFNode obj = st.getObject();
            String suri = null, puri = null, ouri = null;
            if (sub.isURIResource()) {
                suri = sub.getNameSpace();
            }
            if (prop.isURIResource()) {
                puri = prop.getNameSpace();
            }
            if (obj.isURIResource()) {
                ouri = obj.asNode().getNameSpace();
            }

            if (sub.toString().contains("isPartOf") || obj.toString().contains("isPartOf")) {
//				System.out.println("debug");
            }

            if (!ontParse.metaURISet.contains(suri)) {
				/*完善定义和声明，添加rdf:type*/
                lt = ontParse.getAllStatement(orgModel, sub,
                        orgModel.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), null);

                if (lt != null && lt.size() < 1000) {/*跳过少数畸形变态的点*/
                    for (Iterator ity = lt.iterator(); ity.hasNext(); ) {
                        Statement sx = (Statement) ity.next();
                        if (!remainSt.contains(sx)) {
                            remainSt.add(sx);
                        }
                    }
                }
				/*完善注释，添加label和comment等*/
                lt = ontParse.getAllStatement(orgModel, sub,
                        orgModel.getProperty("http://www.w3.org/2000/01/rdf-schema#label"), null);
                if (lt != null && lt.size() < 1000) {
                    for (Iterator ity = lt.iterator(); ity.hasNext(); ) {
                        Statement sx = (Statement) ity.next();
                        if (!remainSt.contains(sx)) {
                            remainSt.add(sx);
                        }
                    }
                }
                lt = ontParse.getAllStatement(orgModel, sub,
                        orgModel.getProperty("http://www.w3.org/2000/01/rdf-schema#comment"), null);
                if (lt != null && lt.size() < 1000) {
                    for (Iterator ity = lt.iterator(); ity.hasNext(); ) {
                        Statement sx = (Statement) ity.next();
                        if (!remainSt.contains(sx)) {
                            remainSt.add(sx);
                        }
                    }
                }
            }
            if (!ontParse.metaURISet.contains(puri)) {
				/*完善定义和声明，添加rdf:type*/
                lt = ontParse.getAllStatement(orgModel, orgModel.getResource(prop.toString()),
                        orgModel.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), null);
                if (lt != null && lt.size() < 1000) {
                    for (Iterator ity = lt.iterator(); ity.hasNext(); ) {
                        Statement sx = (Statement) ity.next();
                        if (!remainSt.contains(sx)) {
                            remainSt.add(sx);
                        }
                    }
                }
				/*完善注释，添加label和comment等*/
                lt = ontParse.getAllStatement(orgModel, orgModel.getResource(prop.toString()),
                        orgModel.getProperty("http://www.w3.org/2000/01/rdf-schema#label"), null);
                if (lt != null && lt.size() < 1000) {
                    for (Iterator ity = lt.iterator(); ity.hasNext(); ) {
                        Statement sx = (Statement) ity.next();
                        if (!remainSt.contains(sx)) {
                            remainSt.add(sx);
                        }
                    }
                }

                lt = ontParse.getAllStatement(orgModel, orgModel.getResource(prop.toString()),
                        orgModel.getProperty("http://www.w3.org/2000/01/rdf-schema#comment"), null);
                if (lt != null && lt.size() < 1000) {
                    for (Iterator ity = lt.iterator(); ity.hasNext(); ) {
                        Statement sx = (Statement) ity.next();
                        if (!remainSt.contains(sx)) {
                            remainSt.add(sx);
                        }
                    }
                }
            }
            if (!ontParse.metaURISet.contains(ouri)) {
				/*完善定义和声明，添加rdf:type*/
                Resource objRes = orgModel.getResource(obj.toString());
                if (ontParse.isBlankNode(obj.toString())) {
                    objRes = (Resource) anno2Res.get(obj.toString());
                }
                lt = ontParse.getAllStatement(orgModel, objRes,
                        orgModel.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), null);
                if (lt != null && lt.size() < 1000) {
                    for (Iterator ity = lt.iterator(); ity.hasNext(); ) {
                        Statement sx = (Statement) ity.next();
                        if (!remainSt.contains(sx)) {
                            remainSt.add(sx);
                        }
                    }
                }
				/*完善注释，添加label和comment等*/
                lt = ontParse.getAllStatement(orgModel, objRes,
                        orgModel.getProperty("http://www.w3.org/2000/01/rdf-schema#label"), null);
                if (lt != null && lt.size() < 1000) {
                    for (Iterator ity = lt.iterator(); ity.hasNext(); ) {
                        Statement sx = (Statement) ity.next();
                        if (!remainSt.contains(sx)) {
                            remainSt.add(sx);
                        }
                    }
                }
                lt = ontParse.getAllStatement(orgModel, objRes,
                        orgModel.getProperty("http://www.w3.org/2000/01/rdf-schema#comment"), null);
                if (lt != null && lt.size() < 1000) {
                    for (Iterator ity = lt.iterator(); ity.hasNext(); ) {
                        Statement sx = (Statement) ity.next();
                        if (!remainSt.contains(sx)) {
                            remainSt.add(sx);
                        }
                    }
                }
            }
        }
        Sg.addAll(remainSt);

        /**构造OntModel**/
//    	System.out.println(r.toString()+"快照：------");
        for (Iterator itx = Sg.iterator(); itx.hasNext(); ) {
            Statement st = (Statement) itx.next();
//    		System.out.println(st.toString());
            outModel.add(st);
        }
        return outModel;
    }

    private OntModel getResSnapshotQuick(Resource r, OntModel orgModel) {
        OntModel outModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        OWLOntParse ontParse = new OWLOntParse();
        Set Sg = new HashSet();//快照的三元组集合
        ArrayList lt = new ArrayList();
		
		/*1.寻找直接相关的三元组*/
		/*1.1以r开头的三元组*/
        Selector selector = new SimpleSelector(r, null, (RDFNode) null);
        for (StmtIterator Iter = orgModel.listStatements(selector); Iter.hasNext(); ) {
            Statement st = Iter.next();
            Sg.add(st);
//    		System.out.println("添加："+st.toString());
        }
		/*1.2以r结尾的三元组*/
        selector = new SimpleSelector(null, null, r);
        for (StmtIterator Iter = orgModel.listStatements(selector); Iter.hasNext(); ) {
            Statement st = Iter.next();
            Sg.add(st);
//    		System.out.println("添加："+st.toString());
        }
		
    	/*2.寻找概念层次结构*/
    	/*2.1父概念*/
        OntClass c = orgModel.getOntClass(r.toString());
        Selector slx = new SimpleSelector(c, orgModel.getProperty("http://www.w3.org/2000/01/rdf-schema#subClassOf"), (RDFNode) null);
        for (StmtIterator itx = orgModel.listStatements(slx); itx.hasNext(); ) {
            Statement stx = itx.next();
            Sg.add(stx);

            OntClass c2 = orgModel.getOntClass(stx.getObject().toString());
            Selector sly = new SimpleSelector(c2, orgModel.getProperty("http://www.w3.org/2000/01/rdf-schema#subClassOf"), (RDFNode) null);
            for (StmtIterator ity = orgModel.listStatements(sly); ity.hasNext(); ) {
                Statement sty = ity.next();
                Sg.add(sty);
            }

        }
    	
    	/*2.2子概念*/
        slx = new SimpleSelector(null, orgModel.getProperty("http://www.w3.org/2000/01/rdf-schema#subClassOf"), c);
        for (StmtIterator itx = orgModel.listStatements(slx); itx.hasNext(); ) {
            Statement stx = itx.next();
            Sg.add(stx);

            OntClass c2 = orgModel.getOntClass(stx.getSubject().toString());
            Selector sly = new SimpleSelector(null, orgModel.getProperty("http://www.w3.org/2000/01/rdf-schema#subClassOf"), c2);
            for (StmtIterator ity = orgModel.listStatements(sly); ity.hasNext(); ) {
                Statement sty = ity.next();
                Sg.add(sty);
            }

        }
    	
    	/*3.part-of结构*/
    	/*3.1父概念*/
        if (r.getURI().contains(sourceBaseURI)) {
            selector = new SimpleSelector(r, null, (RDFNode) null);
            for (StmtIterator Iter = m_sourcePart.listStatements(selector); Iter.hasNext(); ) {
                Statement st = Iter.next();
                Sg.add(st);
//        		System.out.println("添加："+st.toString());
            }
            selector = new SimpleSelector(null, null, r);
            for (StmtIterator Iter = m_sourcePart.listStatements(selector); Iter.hasNext(); ) {
                Statement st = Iter.next();
                Sg.add(st);
//        		System.out.println("添加："+st.toString());
            }
        }

        if (r.getURI().contains(targetBaseURI)) {
            selector = new SimpleSelector(r, null, (RDFNode) null);
            for (StmtIterator Iter = m_targetPart.listStatements(selector); Iter.hasNext(); ) {
                Statement st = Iter.next();
                Sg.add(st);
//        		System.out.println("添加："+st.toString());
            }
            selector = new SimpleSelector(null, null, r);
            for (StmtIterator Iter = m_targetPart.listStatements(selector); Iter.hasNext(); ) {
                Statement st = Iter.next();
                Sg.add(st);
//        		System.out.println("添加："+st.toString());
            }
        }
    	/*3.2子概念*/
    	
    	/*4.同义词处理*/
    	/*Class在Part Model中的声明*/
        selector = new SimpleSelector(c, orgModel.getProperty("http://www.geneontology.org/formats/oboInOwl#hasRelatedSynonym"), (RDFNode) null);
        ArrayList labelLt = new ArrayList();
        String newComment = "";
		/*寻找同义词入口*/
        for (StmtIterator itx = orgModel.listStatements(selector); itx.hasNext(); ) {
            Statement st = itx.next();

            String rStr = st.getObject().toString();
            String curStr = rStr.substring(rStr.indexOf("#") + 1);
            newComment = newComment + curStr + " ";
            slx = new SimpleSelector((Resource) st.getObject(), orgModel.getProperty("http://www.w3.org/2000/01/rdf-schema#label"), (RDFNode) null);
    		/*取出label*/
            for (StmtIterator ity = orgModel.listStatements(slx); ity.hasNext(); ) {
                Statement stemp = ity.next();
                labelLt.add(stemp.getObject());
            }
        }
		/*将同义词作为comment加入*/

        for (Iterator itx = labelLt.iterator(); itx.hasNext(); ) {
            String rStr = itx.next().toString();
            String curStr = rStr.substring(0, rStr.indexOf("^^"));
            newComment = newComment + curStr + " ";
        }
        if (newComment.length() > 1) {
            Literal liT = orgModel.createLiteral(newComment);
            c.addComment(liT);
        }			
    	
    	/*5.修补三元组*/
        Set remainSt = new HashSet();
        for (Iterator itx = Sg.iterator(); itx.hasNext(); ) {
            Statement st = (Statement) itx.next();
            Resource sub = st.getSubject();
            Property prop = st.getPredicate();
            RDFNode obj = st.getObject();
            String suri = null, puri = null, ouri = null;
            if (sub.isURIResource()) {
                suri = sub.getNameSpace();
            }
            if (prop.isURIResource()) {
                puri = prop.getNameSpace();
            }
            if (obj.isURIResource()) {
                ouri = obj.asNode().getNameSpace();
            }

            if (!ontParse.metaURISet.contains(suri)) {
				/*完善定义和声明，添加rdf:type*/
                lt = ontParse.getAllStatement(orgModel, sub,
                        orgModel.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), null);

                if (lt != null && lt.size() < 1000) {/*跳过少数畸形变态的点*/
                    for (Iterator ity = lt.iterator(); ity.hasNext(); ) {
                        Statement sx = (Statement) ity.next();
                        if (!remainSt.contains(sx)) {
                            remainSt.add(sx);
                        }
                    }
                }
				/*完善注释，添加label和comment等*/
                lt = ontParse.getAllStatement(orgModel, sub,
                        orgModel.getProperty("http://www.w3.org/2000/01/rdf-schema#label"), null);
                if (lt != null && lt.size() < 1000) {
                    for (Iterator ity = lt.iterator(); ity.hasNext(); ) {
                        Statement sx = (Statement) ity.next();
                        if (!remainSt.contains(sx)) {
                            remainSt.add(sx);
                        }
                    }
                }
                lt = ontParse.getAllStatement(orgModel, sub,
                        orgModel.getProperty("http://www.w3.org/2000/01/rdf-schema#comment"), null);
                if (lt != null && lt.size() < 1000) {
                    for (Iterator ity = lt.iterator(); ity.hasNext(); ) {
                        Statement sx = (Statement) ity.next();
                        if (!remainSt.contains(sx)) {
                            remainSt.add(sx);
                        }
                    }
                }
            }
            if (!ontParse.metaURISet.contains(puri)) {
				/*完善定义和声明，添加rdf:type*/
                lt = ontParse.getAllStatement(orgModel, orgModel.getResource(prop.toString()),
                        orgModel.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), null);
                if (lt != null && lt.size() < 1000) {
                    for (Iterator ity = lt.iterator(); ity.hasNext(); ) {
                        Statement sx = (Statement) ity.next();
                        if (!remainSt.contains(sx)) {
                            remainSt.add(sx);
                        }
                    }
                }
				/*完善注释，添加label和comment等*/
                lt = ontParse.getAllStatement(orgModel, orgModel.getResource(prop.toString()),
                        orgModel.getProperty("http://www.w3.org/2000/01/rdf-schema#label"), null);
                if (lt != null && lt.size() < 1000) {
                    for (Iterator ity = lt.iterator(); ity.hasNext(); ) {
                        Statement sx = (Statement) ity.next();
                        if (!remainSt.contains(sx)) {
                            remainSt.add(sx);
                        }
                    }
                }

                lt = ontParse.getAllStatement(orgModel, orgModel.getResource(prop.toString()),
                        orgModel.getProperty("http://www.w3.org/2000/01/rdf-schema#comment"), null);
                if (lt != null && lt.size() < 1000) {
                    for (Iterator ity = lt.iterator(); ity.hasNext(); ) {
                        Statement sx = (Statement) ity.next();
                        if (!remainSt.contains(sx)) {
                            remainSt.add(sx);
                        }
                    }
                }
            }
            if (!ontParse.metaURISet.contains(ouri)) {
				/*完善定义和声明，添加rdf:type*/
                Resource objRes = orgModel.getResource(obj.toString());
//				if (ontParse.isBlankNode(obj.toString())){
//					objRes=(Resource)anno2Res.get(obj.toString());
//				}
                lt = ontParse.getAllStatement(orgModel, objRes, orgModel.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), null);
                if (lt != null && lt.size() < 1000) {
                    for (Iterator ity = lt.iterator(); ity.hasNext(); ) {
                        Statement sx = (Statement) ity.next();
                        if (!remainSt.contains(sx)) {
                            remainSt.add(sx);
                        }
                    }
                }
				/*完善注释，添加label和comment等*/
                lt = ontParse.getAllStatement(orgModel, objRes,
                        orgModel.getProperty("http://www.w3.org/2000/01/rdf-schema#label"), null);
                if (lt != null && lt.size() < 1000) {
                    for (Iterator ity = lt.iterator(); ity.hasNext(); ) {
                        Statement sx = (Statement) ity.next();
                        if (!remainSt.contains(sx)) {
                            remainSt.add(sx);
                        }
                    }
                }
                lt = ontParse.getAllStatement(orgModel, objRes,
                        orgModel.getProperty("http://www.w3.org/2000/01/rdf-schema#comment"), null);
                if (lt != null && lt.size() < 1000) {
                    for (Iterator ity = lt.iterator(); ity.hasNext(); ) {
                        Statement sx = (Statement) ity.next();
                        if (!remainSt.contains(sx)) {
                            remainSt.add(sx);
                        }
                    }
                }
            }
        }
        Sg.addAll(remainSt);

        /**构造OntModel**/
//    	System.out.println(r.toString()+"快照：------");
        for (Iterator itx = Sg.iterator(); itx.hasNext(); ) {
            Statement st = (Statement) itx.next();
//    		System.out.println(st.toString());
            outModel.add(st);
        }

        return outModel;
    }

    private double getPreWeight(String preName, Resource r) {
        if (isCnptSnap) {
            if (preName.equals("http://www.w3.org/2000/01/rdf-schema#subClassOf")) {
                return 1.0;
            } else if (preName.equals("http://www.w3.org/2000/01/rdf-schema#range")
                    || preName.equals("http://www.w3.org/2000/01/rdf-schema#domain")) {
                return 0.8;
            } else {
                return 0.6;
            }
        } else {
            if (preName.equals("http://www.w3.org/2000/01/rdf-schema#subPropertyOf")
                    || preName.equals("http://www.w3.org/2000/01/rdf-schema#range")
                    || preName.equals("http://www.w3.org/2000/01/rdf-schema#domain")
                    || preName.equals(r.toString())) {
                return 1.0;
            } else {
                return 0.6;
            }
        }
    }

    /***************************
     * 判断三元组是否是定义或注释
     ***************************/
    private boolean isDCInfo(Statement st) {
        String pName = st.getPredicate().getLocalName();
        if (pName.equals("type") || pName.equals("comment") || pName.equals("label")
                || pName.equals("AnnotationProperty")) {
            return true;
        } else {
            return false;
        }
    }

    /***************************
     * 生成快照的候选三元组集合
     ***************************/
    private void genSnapCandidate(Resource newRes, RDFNode newNode, OntModel m, Set Sc, Set Sg, HashMap anno2Res,
                                  HashMap node2Triple, HashMap node2Drg, Set inNode,
                                  HashMap node2InverDrg, HashMap triple2Dw, HashMap inNode2Dw) {
        Set tempSet = new HashSet();
		/*以r开头的三元组*/
        Selector selector = new SimpleSelector(newRes, null, (RDFNode) null);
        for (StmtIterator Iter = m.listStatements(selector); Iter.hasNext(); ) {
            Statement st = Iter.next();
            /**除去定义、注释的影响**/
            if (!isDCInfo(st) && !Sc.contains(st) && !Sg.contains(st)) {
                Sc.add(st);
                String subName = st.getSubject().toString();
                String objName = st.getObject().toString();
    			/*如果是匿名节点，则记录*/
                if (ontParse.isBlankNode(subName) && !anno2Res.keySet().contains(subName)) {
                    anno2Res.put(subName, st.getSubject());
                }
                if (ontParse.isBlankNode(objName) && !anno2Res.keySet().contains(objName)) {
                    anno2Res.put(objName, st.getObject());
                }
    			/*记录点对应的三元组*/
                if (!node2Triple.keySet().contains(subName)) {
                    tempSet = new HashSet();
                } else {
                    tempSet = (Set) node2Triple.get(subName);
                }
                tempSet.add(st);
                node2Triple.put(subName, tempSet);
                if (!node2Triple.keySet().contains(objName)) {
                    tempSet = new HashSet();
                } else {
                    tempSet = (Set) node2Triple.get(objName);
                }
                tempSet.add(st);
                node2Triple.put(objName, tempSet);
            }
        }
    	/*不考虑r为predicate的三元组，只有属性的实例可能是这种情况*/
    	/*以r结尾的三元组*/
        selector = new SimpleSelector(null, null, newNode);
        for (StmtIterator Iter = m.listStatements(selector); Iter.hasNext(); ) {
            Statement st = Iter.next();
            /**不除去定义、注释的影响，目的是保留直接实例**/
            if (!Sc.contains(st) && !Sg.contains(st)) {
                Sc.add(st);
                String subName = st.getSubject().toString();
                String objName = st.getObject().toString();
    			/*如果是匿名节点，则记录*/
                if (ontParse.isBlankNode(subName) && !anno2Res.keySet().contains(subName)) {
                    anno2Res.put(subName, st.getSubject());
                }
                if (ontParse.isBlankNode(objName) && !anno2Res.keySet().contains(objName)) {
                    anno2Res.put(objName, st.getObject());
                }
    			/*记录点对应的三元组*/
                if (!node2Triple.keySet().contains(subName)) {
                    tempSet = new HashSet();
                } else {
                    tempSet = (Set) node2Triple.get(subName);
                }
                tempSet.add(st);
                node2Triple.put(subName, tempSet);
                if (!node2Triple.keySet().contains(objName)) {
                    tempSet = new HashSet();
                } else {
                    tempSet = (Set) node2Triple.get(objName);
                }
                tempSet.add(st);
                node2Triple.put(objName, tempSet);
            }
        }

        /**1.1 初始种子中点的度**/
        for (Iterator itx = node2Triple.keySet().iterator(); itx.hasNext(); ) {
            String nodeName = (String) itx.next();
            if (node2Drg.containsKey(nodeName)) {
                continue;
            }
            Resource nodeRes = (Resource) anno2Res.get(nodeName);
            if (nodeRes == null) {
                nodeRes = m.getResource(nodeName);
            }
            int count = 0;
    		/*作为sub得到的度*/
            selector = new SimpleSelector(nodeRes, null, (RDFNode) null);
            for (StmtIterator ity = m.listStatements(selector); ity.hasNext(); ) {
                Statement tmptst = ity.next();
                /**除去定义、注释的影响**/
                if (!isDCInfo(tmptst)) {
                    count++;
                }
            }
    		
    		/*作为obj得到的度*/
            selector = new SimpleSelector(null, null, nodeRes);
            if (m.listStatements(selector).toList().isEmpty()) {
                if (nodeName.contains("^^") && nodeName.contains("http://www.w3.org/") && !nodeName.contains("--")) {
                    String value = nodeName.substring(0, nodeName.indexOf("^^"));
                    selector = new SimpleSelector(null, null, value);
                    if (m.listStatements(selector).toList().isEmpty()) {
                        try {
                            selector = new SimpleSelector(null, null, Integer.valueOf(value));
                        } catch (Exception e1) {
                            // ignore
                        }
                        if (m.listStatements(selector).toList().isEmpty()) {
                            try {
                                selector = new SimpleSelector(null, null, Double.valueOf(value));
                            } catch (Exception e1) {
                                // ignore
                            }
                            if (m.listStatements(selector).toList().isEmpty()) {
                                try {
                                    selector = new SimpleSelector(null, null, Boolean.valueOf(value));
                                } catch (Exception e1) {
                                    // ignore
                                }
                            }
                        }
                    }
                }
            }

            for (StmtIterator ity = m.listStatements(selector); ity.hasNext(); ) {
                Statement tmptst = ity.next();
                /**除去定义、注释的影响**/
                if (!isDCInfo(tmptst)) {
                    count++;
                }
            }
	    	/*处理节点为未标注类型的文本的特殊情况*/
            if (count == 0) {
//	    		System.out.println("debug: node2Drg.put(nodeName,count)");
                selector = new SimpleSelector(null, null, nodeName);
                for (StmtIterator ity = m.listStatements(selector); ity.hasNext(); ) {
                    Statement tmptst = ity.next();
                    /**除去定义、注释的影响**/
                    if (!isDCInfo(tmptst)) {
                        count++;
                    }
                }
            }
	    	/*实在无法处理的情况，只好设为1了。
	    	 * NND，Jena就是一坨狗屎，以后再也不用这个怪物了*/
            if (count == 0) {
                count = 1;
            }
            node2Drg.put(nodeName, count);
        }

        /**1.2 快照中点的入度和**/
        for (Iterator itx = inNode.iterator(); itx.hasNext(); ) {
            String nodeName = (String) itx.next();
            double wt = 0;
    		/*点对应的三元组*/
            if (node2Triple.keySet().isEmpty()) {
                continue;
            }
            for (Iterator ity = ((Set) node2Triple.get(nodeName)).iterator(); ity.hasNext(); ) {
                Statement st = (Statement) ity.next();
                String subName = st.getSubject().toString();
                String objName = st.getObject().toString();
                if (subName.equals(nodeName)) {
                    wt = wt + 1.0 / (Integer) node2Drg.get(subName);
                } else {
                    wt = wt + 1.0 / (Integer) node2Drg.get(objName);
                }
            }
            node2InverDrg.put(nodeName, wt);
        }

        /**1.3 初始种子中三元组的依赖度**/
        for (Iterator itx = Sc.iterator(); itx.hasNext(); ) {
            Statement st = (Statement) itx.next();
            String subName = st.getSubject().toString();
            String preName = st.getPredicate().toString();
            String objName = st.getObject().toString();
			
			/*区分在快照中和快照外的点*/
            String inName = subName;
            String outName = objName;
            if (!inNode.contains(subName)) {
                inName = objName;
                outName = subName;
            }

            if (!inNode.contains(inName)) continue;
			/*求三元组谓词的权重*/
            double wp = getPreWeight(preName, newRes);
			/*求入度和Zib*/
            double zib;
            if (node2InverDrg.get(inName) == null) {
                zib = 1.0;
            } else {
                zib = (Double) node2InverDrg.get(inName);
            }
			/*求ziq和zqi*/
            double ziq = 1.0 / (Integer) node2Drg.get(inName);
            double zqi = 1.0 / (Integer) node2Drg.get(outName);
			/*求三元组的依赖度*/
            double dw = (ziq + zqi) / (1.0 + zib) * wp;
			/*求依赖度累积*/
            double sumDw = (Double) inNode2Dw.get(inName) * dw;
			
			/*记录三元组依赖度*/
            triple2Dw.put(st, sumDw);
        }
    }

    private void handlePropSeed(Resource newRes, RDFNode newNode, OntModel m, Set Sc, Set Sg, HashMap anno2Res
            , HashMap node2Triple, Set inNode, HashMap inNode2Dw) {
        Set tempSet = new HashSet();
		/*以r开头的三元组*/
        Selector selector = new SimpleSelector(newRes, null, (RDFNode) null);
        for (StmtIterator Iter = m.listStatements(selector); Iter.hasNext(); ) {
            Statement st = Iter.next();
            /**除去定义、注释的影响**/
            if (!isDCInfo(st) && !Sc.contains(st) && !Sg.contains(st)) {
                Sc.add(st);
                String subName = st.getSubject().toString();
                String objName = st.getObject().toString();
    			/*如果是匿名节点，则记录*/
                if (ontParse.isBlankNode(subName) && !anno2Res.keySet().contains(subName)) {
                    anno2Res.put(subName, st.getSubject());
                }
                if (ontParse.isBlankNode(objName) && !anno2Res.keySet().contains(objName)) {
                    anno2Res.put(objName, st.getObject());
                }
    			/*记录点对应的三元组*/
                if (!node2Triple.keySet().contains(subName)) {
                    tempSet = new HashSet();
                } else {
                    tempSet = (Set) node2Triple.get(subName);
                }
                tempSet.add(st);
                node2Triple.put(subName, tempSet);
                if (!node2Triple.keySet().contains(objName)) {
                    tempSet = new HashSet();
                } else {
                    tempSet = (Set) node2Triple.get(objName);
                }
                tempSet.add(st);
                node2Triple.put(objName, tempSet);
            }
        }
    	/*以r结尾的三元组*/
        selector = new SimpleSelector(null, null, newNode);
        for (StmtIterator Iter = m.listStatements(selector); Iter.hasNext(); ) {
            Statement st = Iter.next();
            /**除去定义、注释的影响**/
            if (!isDCInfo(st) && !Sc.contains(st) && !Sg.contains(st)) {
                Sc.add(st);
                String subName = st.getSubject().toString();
                String objName = st.getObject().toString();
    			/*如果是匿名节点，则记录*/
                if (ontParse.isBlankNode(subName) && !anno2Res.keySet().contains(subName)) {
                    anno2Res.put(subName, st.getSubject());
                }
                if (ontParse.isBlankNode(objName) && !anno2Res.keySet().contains(objName)) {
                    anno2Res.put(objName, st.getObject());
                }
    			/*记录点对应的三元组*/
                if (!node2Triple.keySet().contains(subName)) {
                    tempSet = new HashSet();
                } else {
                    tempSet = (Set) node2Triple.get(subName);
                }
                tempSet.add(st);
                node2Triple.put(subName, tempSet);
                if (!node2Triple.keySet().contains(objName)) {
                    tempSet = new HashSet();
                } else {
                    tempSet = (Set) node2Triple.get(objName);
                }
                tempSet.add(st);
                node2Triple.put(objName, tempSet);
            }
        }
    	
    	/*如果头尾都没有得到任何三元组*/
        if (Sc.isEmpty()) {
            inNode.clear();
            inNode2Dw.clear();
        }
        selector = new SimpleSelector(null, (Property) newRes, (RDFNode) null);
        for (StmtIterator Iter = m.listStatements(selector); Iter.hasNext(); ) {
            Statement st = Iter.next();
            /**除去定义、注释的影响**/
            if (!isDCInfo(st) && !Sc.contains(st) && !Sg.contains(st)) {
                Sc.add(st);
                String subName = st.getSubject().toString();
                String objName = st.getObject().toString();
                inNode.add(subName);
                inNode2Dw.put(subName, 0.5);
    			/*如果是匿名节点，则记录*/
                if (ontParse.isBlankNode(subName) && !anno2Res.keySet().contains(subName)) {
                    anno2Res.put(subName, st.getSubject());
                }
                if (ontParse.isBlankNode(objName) && !anno2Res.keySet().contains(objName)) {
                    anno2Res.put(objName, st.getObject());
                }
    			/*记录点对应的三元组*/
                if (!node2Triple.keySet().contains(subName)) {
                    tempSet = new HashSet();
                } else {
                    tempSet = (Set) node2Triple.get(subName);
                }
                tempSet.add(st);
                node2Triple.put(subName, tempSet);
                if (!node2Triple.keySet().contains(objName)) {
                    tempSet = new HashSet();
                } else {
                    tempSet = (Set) node2Triple.get(objName);
                }
                tempSet.add(st);
                node2Triple.put(objName, tempSet);
            }
        }
    }

    /********************
     * 大规模本体相似度计算
     ********************/
    private void computeSimMatrix() {
        /**1.相似矩阵和其它数据结构的初始化**/
        cnptSimMx = new SparseDoubleMatrix2D(sourceConceptNum, targetConceptNum);
        propSimMx = new SparseDoubleMatrix2D(sourcePropNum, targetPropNum);
        mappedNode = new HashSet();
		
		/*快速算法适用于概念*/
		
		/*1A.利用Positive的算法*/
//		utilizePositiveAlgorithm();
		
		/*1B.利用Negative的算法*/
//		utilizeNegativeAlgorithm();
		
		/*1C.合并的算法*/
        hybridAlgorithm();
		
		/*属性相似按照原始方法计算*/
		/*2.计算属性相似矩阵*/
        propSimCompute();
		
		/*3.相似度传播算法*/
        if (isNeedProg()) {
            simPropagation();			
		/*3.5 相似矩阵合并*/
            combineSimMx();
        }
				
		/*4.匹配结果后处理*/
		/*相似矩阵过滤*/
        cnptSimMx = new SimpleFilter().maxValueFilter(sourceConceptNum, targetConceptNum, cnptSimMx, cnptSimThreshold);
        propSimMx = new SimpleFilter().maxValueFilter(sourcePropNum, targetPropNum, propSimMx, propSimThreshold);

        mappingDebugging();
		
		/*显示映射结果*/
        showResult(false);
		/*保存映射结果*/
        saveResult();
    }

    /********************
     * 合并Positive和Negative两种算法
     ********************/
    private void hybridAlgorithm() {
        int ntcount = 0;
        /**1.根据概念在层次结构中的度，对概念排序**/
		/*计算每个概念在层次中的度*/
        nodeDegree[] cnptDgr = new nodeDegree[sourceConceptNum];
        for (int i = 0; i < sourceConceptNum; i++) {
            OntClass c = m_source.getOntClass(sourceBaseURI + sourceConceptName[i]);
            cnptDgr[i] = new nodeDegree();
			/*得到概念的度*/
            cnptDgr[i].name = sourceConceptName[i];
            cnptDgr[i].degree = ontParse.getCnptDegreeInHierarchy(c);
        }
		/*排序*/
        Arrays.sort(cnptDgr);

        /**1.5 统计每个概念拥有的文本量**/
        sCnptDesCapacity = new int[sourceConceptNum];
        for (int i = 0; i < sourceConceptNum; i++) {
            sCnptDesCapacity[i] = getCnptDesCapacity(sourceCnptTextDes[i]);
        }
        tCnptDesCapacity = new int[targetConceptNum];
        for (int i = 0; i < targetConceptNum; i++) {
            tCnptDesCapacity[i] = getCnptDesCapacity(targetCnptTextDes[i]);
        }

        /**2.遍历概念**/
        ArrayList visited = new ArrayList();
        for (int i = 0; i < sourceConceptNum; i++) {
            pbarValue[2] = (int) (100.0 * i / (double) sourceConceptNum); //进度条数值

            int sPos = (Integer) sCnptName2Pos.get(cnptDgr[i].name);
            visited.add(sPos);

//			System.out.println("No."+i+" ntSet:"+ntcount);

            /**3.从指定范围内的邻居概念得到相似度计算的NSet**/
            ArrayList ntSeq = new ArrayList();
            ArrayList ptSeq = new ArrayList();
            ArrayList newNtSeq = new ArrayList();
			/*得到邻居*/
            Set nbSet = getCnptNeighborsInScale(sourceConceptName[sPos]);
			/*根据邻居得到NSet*/
            for (Iterator itx = nbSet.iterator(); itx.hasNext(); ) {
                OntClass tc = (OntClass) itx.next();
                int nbPos = (Integer) sCnptName2Pos.get(tc.getLocalName());

                /**约束条件2：邻居的语义子图中并不包含当前点**/
                if (!((Set) sCnptFriends.get(nbPos)).contains(sPos)) {
                    continue;
                }

                if (ntNbNode.keySet().contains(nbPos)) {
                    ArrayList tempSeq = (ArrayList) ntNbNode.get(nbPos);
					/*和已有的合并*/
                    ntSeq = mergeSequence(ntSeq, tempSeq);
                }
            }

            /**3.5得到positive上的NSet**/
            ptSeq = (ArrayList) ntTopKNode.get(sPos);


            /**4.计算相似度**/
			/*记录已处理概念*/
            mappedNode.add(sourceConceptName[sPos]);
			/*计算相似度*/
            TextDocSim simer = new TextDocSim();
            Set newtSet = new HashSet();
            for (int j = 0; j < targetConceptNum; j++) {
				/* 跳过Positive位置 */
                if (isInSequence(j, j, ptSeq)) {
//					System.out.println("跳过");
                    localPtNum++;
                    continue;
                }
				
				/* 跳过Nagetive位置 */
                if (isInSequence(j, j, ntSeq)) {
//					System.out.println("跳过");
                    localNtNum++;
                    continue;
                }
				/* 否则正常计算 */
                double sim = simer.getSimpleCnptTextSim(sourceCnptTextDes[sPos], targetCnptTextDes[j]);
                if (sim > ntValue) {
                    if (sim > cnptSimThreshold) {
                        cnptSimMx.setQuick(sPos, j, sim);
                    }
                } else {
					/*记录新产生的negative pos*/
                    /**约束条件1: 两者必须具有一定量的文本信息**/
                    if (sCnptDesCapacity[sPos] > desCapValue && tCnptDesCapacity[j] > desCapValue) {
                        newtSet.add(j);
                    }
                }
//				System.out.println("No."+i+"--"+sPos + "--" + j + ":" + sim+" ntCount:"+ntcount);
            }

            /**5.构造概念自己产生的NSet**/
            newNtSeq = genSequence(newtSet);
            ntNbNode.put(sPos, newNtSeq);

            ntcount += newNtSeq.size();

            /**5.5构造概念层次产生的NSet**/
            maintainTopKNegative(sPos);
			
			/*将已经计算过的点从Negative Set中去除*/
            if (ntTopKNode.keySet().contains(sPos)) {
                ntTopKNode.remove(sPos);
            }

            sourceCnptTextDes[sPos] = null;
            newNtSeq = null;
            ntSeq = null;
            ptSeq = null;
			
			/*内存释放和垃圾回收*/
//			if (i%500==0){
//				/*遍历已计算过的概念*/
//				for (int k=0;k<=i;k++){
//					int mPos=(Integer)sCnptName2Pos.get(cnptDgr[k].name);
//					if (!ntNbNode.keySet().contains(mPos)){
//						continue;
//					}
//					/*得到友好概念*/
//					Set mNbCnpt=(Set)sCnptFriends.get(mPos);
//					/*判断友好概念是否都计算过*/
//					boolean release=true;
//					for (Iterator itx=mNbCnpt.iterator();itx.hasNext();){
//						int friend=(Integer)itx.next();
//						if (!visited.contains(friend)){
//							release=false;
//							break;
//						}
//					}
//					/*判断概念的negative set是否可以释放*/
//					if (release){
//						ntcount=ntcount-((ArrayList)ntNbNode.get(mPos)).size();
//						ntNbNode.remove(mPos);
//					}					
//				}
//				System.gc();
//			}
        }
		/*变量释放*/
        cnptDgr = null;
        sCnptDesCapacity = null;
        tCnptDesCapacity = null;
        sCnptName2Pos = null;
        ntNbNode = null;
        targetCnptTextDes = null;
    }

    /********************
     * 相似度传播算法
     * 从效率上考虑,采用C3B传播范围
     ********************/
    private void simPropagation() {
		/*数据结构*/
        pgSimMxCnpt = new SparseDoubleMatrix2D(sourceConceptNum, targetConceptNum);
        pgSimMxProp = new SparseDoubleMatrix2D(sourcePropNum, targetPropNum);

        insSimMx = new SparseDoubleMatrix2D(sourceInsNum, targetInsNum);

        TextDes[] sInsDes = new TextDes[sourceInsNum];
        TextDes[] tInsDes = new TextDes[targetInsNum];

        ArrayList paraList = new ArrayList();
        ArrayList lt = new ArrayList();

        /**1.构造实例的相似矩阵**/
		/*1.1实例基本描述*/
        for (int i = 0; i < sourceInsNum; i++) {
            sInsDes[i] = new OntDes().getOneInsOntTextDes(m_source,
                    m_source.getIndividual(sourceBaseURI + sourceInsName[i]));
        }
        for (int i = 0; i < targetInsNum; i++) {
            tInsDes[i] = new OntDes().getOneInsOntTextDes(m_target,
                    m_target.getIndividual(targetBaseURI + targetInsName[i]));
        }
		/*1.2计算相似度*/
        TextDocSim simer = new TextDocSim();
        for (int i = 0; i < sourceInsNum; i++) {
            for (int j = 0; j < targetInsNum; j++) {
                double sim = simer.getSimpleInsTextSim(sInsDes[i], tInsDes[j]);
                if (sim > 0.5) {
                    insSimMx.setQuick(i, j, sim);
                }
            }
        }

        cnptSimMx = new SimpleFilter().maxValueFilter(sourceConceptNum, targetConceptNum, cnptSimMx, cnptSimThreshold);
        propSimMx = new SimpleFilter().maxValueFilter(sourcePropNum, targetPropNum, propSimMx, propSimThreshold);
        insSimMx = new SimpleFilter().maxValueFilter(sourceInsNum, targetInsNum, insSimMx, 0.3);

        /**2.构造传播参数表**/
        packSimPgPara(paraList);

        /**3.相似度传播**/
//		System.out.println("相似度传播");
        lt = new LargeOntSimPropagation().ontSimPg(paraList);
        pgSimMxCnpt = (SparseDoubleMatrix2D) lt.get(0);
        pgSimMxProp = (SparseDoubleMatrix2D) lt.get(1);

    }

    private void utilizePositiveAlgorithm() {
        /**2.以深度优先遍历概念层次图**/
        /**2.2 找到概念层次的根节点**/
//		/*概念名集合*/
//		ArrayList sCnptNameList=new ArrayList(java.util.Arrays.asList(sourceConceptName));
		/*概念层次根节点集合*/
        ArrayList sCnptHRoot = new ArrayList();
        for (int i = 0; i < sourceConceptNum; i++) {
            OntClass sc = m_source.getOntClass(sourceBaseURI + sourceConceptName[i]);
			/*判断当前概念是否是根节点*/
            if (ontParse.isCnptHRoot(m_source, sc)) {
                sCnptHRoot.add(sc);
            }
        }

        /**2.4 从一个根节点出发，深度优先遍历图得到一条达到叶子节点的路径**/
        for (Iterator itx = sCnptHRoot.iterator(); itx.hasNext(); ) {
            OntClass rootC = (OntClass) itx.next();
            pathRec = new ArrayList();
            allPath = new ArrayList();
            pathRec.add(rootC);
            cnptDFTraversal(m_source, rootC);
			/*遍历以当前节点为根的全部路径*/
            for (Iterator ity = allPath.iterator(); ity.hasNext(); ) {
                ArrayList path = (ArrayList) ity.next();
                /**3. 二分法处理路径，计算概念相似度**/
                handlePath(path, 0, path.size() - 1);
            }
        }
    }

    private void handlePath(ArrayList path, int left, int right) {
        if (left <= right) {
			/*计算路径中间点位置的相似度*/
            int midPos = (right + left) / 2;
            OntClass sc = (OntClass) path.get(midPos);
            int scPos = (Integer) sCnptName2Pos.get(sc.getLocalName());
			
			/*文本相似度计算*/
            if (!mappedNode.contains(sourceConceptName[scPos])) {
                mappedNode.add(sourceConceptName[scPos]);
                TextDocSim simer = new TextDocSim();
                ArrayList ntSeq = (ArrayList) ntTopKNode.get(scPos);
                for (int i = 0; i < targetConceptNum; i++) {
					/*跳过Nagetive位置*/
                    if (isInSequence(i, i, ntSeq)) {
                        System.out.println("跳过");
                        ntNum++;
                        continue;
                    }
					/*否则正常计算*/
                    double sim = simer.getSimpleCnptTextSim(sourceCnptTextDes[scPos], targetCnptTextDes[i]);
                    if (sim > cnptSimThreshold) {
                        cnptSimMx.setQuick(scPos, i, sim);
                    }
                    // System.out.println(scPos + "--" + i + ":" + sim);
                }
				/*topk构造Negative Set*/
                maintainTopKNegative(scPos);
				/*将已经计算过的点从Negative Set中去除*/
                if (ntTopKNode.keySet().contains(scPos)) {
                    ntTopKNode.remove(scPos);
                }
            }			
			
			/*计算前半部分的相似度*/
            handlePath(path, left, midPos - 1);
			/*计算后半部分的相似度*/
            handlePath(path, midPos + 1, right);
        }
        return;
    }

    private void cnptDFTraversal(OntModel m, OntClass root) {
		/*得到子节点*/
        ArrayList subList = ontParse.listDirectSubClassOfConcept(root);
		/*如果子节点未遍历完，递归*/
        if (!subList.isEmpty()) {
            for (Iterator itx = subList.iterator(); itx.hasNext(); ) {
                OntClass sc = (OntClass) itx.next();
                pathRec.add(sc);
                cnptDFTraversal(m, sc);
            }
        } else {
			/*如果是叶子节点，得到一条路径*/
			/*记录当前路径*/
            allPath.add(pathRec.clone());
            pathRec.remove(root);
            return;
        }
        pathRec.remove(root);
		
		/*路径按照长度排序*/
        for (int i = 0; i < allPath.size(); i++) {
            ArrayList pi = (ArrayList) allPath.get(i);
            for (int j = i + 1; j < allPath.size(); j++) {
                ArrayList pj = (ArrayList) allPath.get(j);
                if (pi.size() < pj.size()) {
                    ArrayList pt = (ArrayList) pi.clone();
                    pi = pj;
                    allPath.set(i, pj);
                    pj = pt;
                    allPath.set(j, pt);
                }
            }
        }

//		System.out.println("path length of: "+root.toString());
//		for (Iterator it=allPath.iterator();it.hasNext();){
//			ArrayList p=(ArrayList)it.next();
//			System.out.println(p.size());
//		}		

        return;
    }

    private void showResult(boolean flag) {
        // 根据相似矩阵，生成映射结果
        mappingResult = new MapRecord[Math.max(sourceConceptNum, targetConceptNum)
                + Math.max(sourcePropNum, targetPropNum)];
        produceMapResult(cnptSimMx, propSimMx);

        // 显示结果
        if (!flag)
            return;
        for (int i = 0; i < mappingNum; i++) {
            mappingResult[i].show();
        }
    }

    private void produceMapResult(SparseDoubleMatrix2D simMxC, SparseDoubleMatrix2D simMxP) {
        IntArrayList rowList = new IntArrayList();
        IntArrayList colList = new IntArrayList();
        DoubleArrayList valueList = new DoubleArrayList();

        mappingNum = 0;
        // 概念映射结果
        cMappingNum = 0;
        simMxC.getNonZeros(rowList, colList, valueList);
        for (int k = 0; k < valueList.size(); k++) {
            int i = rowList.get(k);
            int j = colList.get(k);
            //DecimalFormat df = new DecimalFormat("0.0000");
            double tsim = valueList.get(k);
            //tsim = Double.parseDouble(df.format(tsim));
            tsim = Math.round(tsim * 10000) / 10000.0;

            // System.out.println(sourceConceptName[i] + "\t" + targetConceptName[j] + "\t" + tsim);

            if (tsim > cnptSimThreshold) {
                mappingResult[mappingNum] = new MapRecord();
                mappingResult[mappingNum].sourceLabel = new String(sourceConceptName[i]);
                mappingResult[mappingNum].targetLabel = new String(targetConceptName[j]);
                mappingResult[mappingNum].similarity = tsim;
                mappingResult[mappingNum].relationType = 0;
                mappingNum++;
                cMappingNum++;
            }
        }
//		System.out.println("cMappingNum:" + cMappingNum);

        // Property映射结果
        pMappingNum = 0;
        rowList.clear();
        colList.clear();
        valueList.clear();
        simMxP.getNonZeros(rowList, colList, valueList);
        for (int k = 0; k < valueList.size(); k++) {
            int i = rowList.get(k);
            int j = colList.get(k);
            //DecimalFormat df = new DecimalFormat("0.0000");
            double tsim = valueList.get(k);
            //tsim = Double.parseDouble(df.format(tsim));
            if (tsim > propSimThreshold) {
                mappingResult[mappingNum] = new MapRecord();
                mappingResult[mappingNum].sourceLabel = new String(sourcePropName[i]);
                mappingResult[mappingNum].targetLabel = new String(targetPropName[j]);
                mappingResult[mappingNum].similarity = tsim;
                mappingResult[mappingNum].relationType = 0;
                mappingNum++;
                pMappingNum++;
            }
        }
//		System.out.println("pMappingNum:" + pMappingNum);
    }

    private void saveResult() {
        MappingFile mapFile = new MappingFile();
        if (lilyFileName.length() == 0) {
            lilyFileName = "lily";
        }
        // 普通文本文件的方式
        //mapFile.save2txt(sourceOntFile, targetOntFile, mappingNum,mappingResult, lilyFileName);
        // XML文件的方式
        mapFile.setBaseURI(sourceBaseURI, targetBaseURI);
        mapFile.save2rdf(sourceOntFile, targetOntFile, mappingNum, mappingResult, lilyFileName);
    }

    /*************************
     *top-k算法 
     *************************/
    private void maintainTopKNegative(int sPos) {
        int[] topkPos = new int[topk];
        double[] tempSim = new double[targetConceptNum];
        int realk = 0;
        Set sSupSet = new HashSet();
        Set sSubSet = new HashSet();
        Set tSupSet = new HashSet();
        Set tSubSet = new HashSet();
        ArrayList lt = new ArrayList();
        int sgStar;
        int sgEnd;
        HashMap newNt = new HashMap();
        boolean isAnatomy;
		
		/*判断当前数据集是不是Anatomy*/
        isAnatomy = (sourceBaseURI + targetBaseURI).contains("http://mouse.owl") && (sourceBaseURI + targetBaseURI).contains("http://human.owl");
				
		/*1.判断有没有negative的情形存在*/
		/*寻找Top-k的位置*/
        for (int i = 0; i < targetConceptNum; i++) {
            tempSim[i] = cnptSimMx.getQuick(sPos, i);
        }
        Arrays.sort(tempSim);
        for (int i = 0; i < topk; i++) {
            if (tempSim[targetConceptNum - i - 1] >= ptValue) {
                realk++;
            }
        }
        for (int i = 0; i < realk; i++) {
            for (int j = 0; j < targetConceptNum; j++) {
                if (cnptSimMx.getQuick(sPos, j) == tempSim[targetConceptNum - i - 1]) {
                    topkPos[i] = j;
                    break;
                }
            }
        }
		/*2.根据topk的位置生成negative Set*/
		/*source的层次划分*/
        if (realk > 0) {
			/*super Class集合*/
            lt = ontParse.listAllSuperClassOfConcept(m_source.getOntClass(sourceBaseURI + sourceConceptName[sPos]));
            for (Iterator itx = lt.iterator(); itx.hasNext(); ) {
                OntClass tc = (OntClass) itx.next();
                if (!ontParse.isBlankNode(tc.toString()) && tc.getNameSpace().equals(sourceBaseURI)) {
                    sSupSet.add(sCnptName2Pos.get(tc.getLocalName()));
                }
            }
			
			/*super Part-of*/
            if (m_sourcePart != null) {
                lt = ontParse.listAllSuperClassOfConcept(m_sourcePart.getOntClass(sourceBaseURI + sourceConceptName[sPos]));
                for (Iterator itx = lt.iterator(); itx.hasNext(); ) {
                    OntClass tc = (OntClass) itx.next();
                    if (!ontParse.isBlankNode(tc.toString()) && tc.getNameSpace().equals(sourceBaseURI)) {
                        sSupSet.add(sCnptName2Pos.get(tc.getLocalName()));
                    }
                }
            }					
			
			/*sub Class集合*/
            lt = ontParse.listAllSubClassOfConcept(
                    m_source.getOntClass(sourceBaseURI + sourceConceptName[sPos]));
            for (Iterator itx = lt.iterator(); itx.hasNext(); ) {
                OntClass tc = (OntClass) itx.next();
                if (!ontParse.isBlankNode(tc.toString()) && tc.getNameSpace().equals(sourceBaseURI)) {
                    sSubSet.add(sCnptName2Pos.get(tc.getLocalName()));
                }
            }
			
			/*sub Part-of*/
            if (m_sourcePart != null) {
                lt = ontParse.listAllSubClassOfConcept(
                        m_sourcePart.getOntClass(sourceBaseURI + sourceConceptName[sPos]));
                for (Iterator itx = lt.iterator(); itx.hasNext(); ) {
                    OntClass tc = (OntClass) itx.next();
                    if (!ontParse.isBlankNode(tc.toString()) && tc.getNameSpace().equals(sourceBaseURI)) {
                        sSubSet.add(sCnptName2Pos.get(tc.getLocalName()));
                    }
                }
            }
        }

        for (int i = 0; i < realk; i++) {
            int tPos = topkPos[i];
            tSupSet = new HashSet();
            tSubSet = new HashSet();
			/*super概念集合*/
            lt = ontParse.listAllSuperClassOfConcept(
                    m_target.getOntClass(targetBaseURI + targetConceptName[tPos]));
            for (Iterator itx = lt.iterator(); itx.hasNext(); ) {
                OntClass tc = (OntClass) itx.next();
                if (!ontParse.isBlankNode(tc.toString()) && tc.getNameSpace().equals(targetBaseURI)) {
                    tSupSet.add(tCnptName2Pos.get(tc.getLocalName()));
                }
            }
			
			/*super Part-of*/
            if (m_targetPart != null) {
                lt = ontParse.listAllSuperClassOfConcept(
                        m_targetPart.getOntClass(targetBaseURI + targetConceptName[tPos]));
                for (Iterator itx = lt.iterator(); itx.hasNext(); ) {
                    OntClass tc = (OntClass) itx.next();
                    if (!ontParse.isBlankNode(tc.toString()) && tc.getNameSpace().equals(targetBaseURI)) {
                        tSupSet.add(tCnptName2Pos.get(tc.getLocalName()));
                    }
                }
            }			
						
			/*super概念集合序列*/
            ArrayList tSupSq = new ArrayList();
            tSupSq = genSequence(tSupSet);
			
			/*sub概念集合*/
            lt = ontParse.listAllSubClassOfConcept(
                    m_target.getOntClass(targetBaseURI + targetConceptName[tPos]));
            for (Iterator itx = lt.iterator(); itx.hasNext(); ) {
                OntClass tc = (OntClass) itx.next();
                if (!ontParse.isBlankNode(tc.toString()) && tc.getNameSpace().equals(targetBaseURI)) {
                    tSubSet.add(tCnptName2Pos.get(tc.getLocalName()));
                }
            }
			
			/*sub Part-of*/
            if (m_targetPart != null) {
                lt = ontParse.listAllSubClassOfConcept(
                        m_targetPart.getOntClass(targetBaseURI + targetConceptName[tPos]));
                for (Iterator itx = lt.iterator(); itx.hasNext(); ) {
                    OntClass tc = (OntClass) itx.next();
                    if (!ontParse.isBlankNode(tc.toString()) && tc.getNameSpace().equals(targetBaseURI)) {
                        tSubSet.add(tCnptName2Pos.get(tc.getLocalName()));
                    }
                }
            }			
			
			/*sub概念集合序列*/
            ArrayList tSubSq = new ArrayList();
            tSubSq = genSequence(tSubSet);
					
			/*构造映射对应的ntSet*/
            //源super--目标sub
            for (Iterator itx = sSupSet.iterator(); itx.hasNext(); ) {
                int s = (Integer) itx.next();
                if (!tSubSq.isEmpty()) {
                    newNt.put(s, tSubSq);
                }
            }
            //源sub--目标super
            for (Iterator itx = sSubSet.iterator(); itx.hasNext(); ) {
                int s = (Integer) itx.next();
                if (!tSupSq.isEmpty()) {
                    newNt.put(s, tSupSq);
                }
            }


        }

		/*3.维护现有的negative Set,处理新NSet和旧NSet的关系*/
        for (Iterator itx = newNt.keySet().iterator(); itx.hasNext(); ) {
            int newPos = (Integer) itx.next();
            ArrayList newsq = (ArrayList) newNt.get(newPos);
			
			/*如果没有包含当前序列，则加入*/
            if (!ntTopKNode.keySet().contains(newPos)) {
                ntTopKNode.put(newPos, newsq);
            } else {
			/*如果已包含当前序列，则合并*/
                ArrayList oldsq = (ArrayList) ntTopKNode.get(newPos);
                ArrayList msq = mergeSequence(newsq, oldsq);
                ntTopKNode.put(newPos, msq);
            }
        }
    }

    /*************************
     *neighbor算法 
     *************************/
    private void utilizeNegativeAlgorithm() {
        HashMap newNt = new HashMap();

        /**1.根据概念在层次结构中的度，对概念排序**/
		/*计算每个概念在层次中的度*/
        nodeDegree[] cnptDgr = new nodeDegree[sourceConceptNum];
        for (int i = 0; i < sourceConceptNum; i++) {
            OntClass c = m_source.getOntClass(sourceBaseURI + sourceConceptName[i]);
            cnptDgr[i] = new nodeDegree();
			/*得到概念的度*/
            cnptDgr[i].name = sourceConceptName[i];
            cnptDgr[i].degree = ontParse.getCnptDegreeInHierarchy(c);
        }
		/*排序*/
        Arrays.sort(cnptDgr);

        /**1.5 统计每个概念拥有的文本量**/
        sCnptDesCapacity = new int[sourceConceptNum];
        for (int i = 0; i < sourceConceptNum; i++) {
            sCnptDesCapacity[i] = getCnptDesCapacity(sourceCnptTextDes[i]);
        }
        tCnptDesCapacity = new int[targetConceptNum];
        for (int i = 0; i < targetConceptNum; i++) {
            tCnptDesCapacity[i] = getCnptDesCapacity(targetCnptTextDes[i]);
        }

        /**2.遍历概念**/
        for (int i = 0; i < sourceConceptNum; i++) {
            int sPos = (Integer) sCnptName2Pos.get(cnptDgr[i].name);

            /**3.从指定范围内的邻居概念得到相似度计算的NSet**/
            ArrayList ntSeq = new ArrayList();
            ArrayList newNtSeq = new ArrayList();
			/*得到邻居*/
            Set nbSet = getCnptNeighborsInScale(sourceConceptName[sPos]);
			/*根据邻居得到NSet*/
            for (Iterator itx = nbSet.iterator(); itx.hasNext(); ) {
                OntClass tc = (OntClass) itx.next();
                int nbPos = (Integer) sCnptName2Pos.get(tc.getLocalName());

                /**约束条件2：邻居的语义子图中并不包含当前点**/
//				if (!((Set)sCnptFriends.get(nbPos)).contains(sPos)){
//					continue;
//					}

                if (ntNbNode.keySet().contains(nbPos)) {
                    ArrayList tempSeq = (ArrayList) ntNbNode.get(nbPos);
					/*和已有的合并*/
                    ntSeq = mergeSequence(ntSeq, tempSeq);
                }
            }

            /**4.计算相似度**/
			/*记录已处理概念*/
            mappedNode.add(sourceConceptName[sPos]);
			/*计算相似度*/
            TextDocSim simer = new TextDocSim();
            Set newtSet = new HashSet();
            for (int j = 0; j < targetConceptNum; j++) {
				/* 跳过Nagetive位置 */
                if (isInSequence(j, j, ntSeq)) {
                    System.out.println("跳过");
                    ntNum++;
                    continue;
                }
				/* 否则正常计算 */
                double sim = simer.getSimpleCnptTextSim(sourceCnptTextDes[sPos], targetCnptTextDes[j]);
                if (sim > ntValue) {
                    if (sim > cnptSimThreshold) {
                        cnptSimMx.setQuick(sPos, j, sim);
                    }
                } else {
					/*记录新产生的negative pos*/
                    /**约束条件1: 两者必须具有一定量的文本信息**/
                    if (sCnptDesCapacity[sPos] > desCapValue && tCnptDesCapacity[j] > desCapValue) {
                        newtSet.add(j);
                    }
                }
                System.out.println(sPos + "--" + j + ":" + sim);
            }

            /**5.构造概念自己产生的NSet**/
            newNtSeq = genSequence(newtSet);
            ntNbNode.put(sPos, newNtSeq);
        }
    }

    private Set getCnptNeighborsInScale(String cName) {
        Set result = new HashSet();
        OntClass c = m_source.getOntClass(sourceBaseURI + cName);
        Set lst = ontParse.listSuperClassOfConceptInScale(c, nbScale);
        for (Iterator it = lst.iterator(); it.hasNext(); ) {
            OntClass tc = (OntClass) it.next();
            if (tc.toString().contains(sourceBaseURI)) {
                result.add(tc);
            }
        }
        lst = ontParse.listSubClassOfConceptInScale(c, nbScale);
        for (Iterator it = lst.iterator(); it.hasNext(); ) {
            OntClass tc = (OntClass) it.next();
            if (tc.toString().contains(sourceBaseURI)) {
                result.add(tc);
            }
        }
        return result;
    }

    /*************************
     *采用传统语义描述文档方法计算属性相似度
     *************************/
    private void propSimCompute() {
        TextDocSim simer = new TextDocSim();
        for (int i = 0; i < sourcePropNum; i++) {
            for (int j = 0; j < targetPropNum; j++) {
                double sim = simer.getSimplePropTextSim(sourcePropTextDes[i], targetPropTextDes[j]);
                if (sim > propSimThreshold) {
                    propSimMx.setQuick(i, j, sim);
                }
            }
            sourcePropTextDes[i] = null;
        }
        sourcePropTextDes = null;
        targetPropTextDes = null;
    }

    private ArrayList genSequence(Set unOrder) {
        ArrayList sgLt = new ArrayList();
        ArrayList lt = new ArrayList();
        lt.addAll(unOrder);
        Collections.sort(lt);
        int sgStar = -1;
        int sgEnd = -1;
        for (Iterator ity = lt.iterator(); ity.hasNext(); ) {
            int pos = (Integer) ity.next();
            if (sgStar == -1 && sgEnd == -1) {
                sgStar = pos;
                sgEnd = pos;
            }
            if ((pos - sgEnd) > 1) {
                int[] a = new int[2];
                a[0] = sgStar;
                a[1] = sgEnd;
                sgLt.add(a);
                sgStar = pos;
                sgEnd = pos;
            } else {
                sgEnd = pos;
            }
        }
        if (sgStar >= 0 && sgEnd >= sgStar) {
            int[] a = new int[2];
            a[0] = sgStar;
            a[1] = sgEnd;
            sgLt.add(a);
        }
        return sgLt;
    }

    private ArrayList mergeSequence(ArrayList sqA, ArrayList sqB) {
        ArrayList proj = new ArrayList();
        ArrayList lt = new ArrayList();
        int star = 0;
        int end = 0;
    	
    	/*1.构造投影*/
        for (int i = 0; i < sqA.size(); i++) {
            int[] a = (int[]) sqA.get(i);
            star = a[0];
            end = a[1];
            if (!proj.contains(star)) proj.add(star);
            if (!proj.contains(end)) proj.add(end);
        }
        for (int i = 0; i < sqB.size(); i++) {
            int[] a = (int[]) sqB.get(i);
            star = a[0];
            end = a[1];
            if (!proj.contains(star)) proj.add(star);
            if (!proj.contains(end)) proj.add(end);
        }
    	/*排序*/
        Collections.sort(proj);
    	
    	/*2.遍历投影区域*/
        for (int i = 0; i < proj.size() - 1; i++) {
            star = (Integer) proj.get(i);
            end = star;
    		/*判断当前区域是否在序列中*/
            if (isInSequence(star, end, sqA) || isInSequence(star, end, sqB)) {
                int[] a = new int[2];
                a[0] = star;
                a[1] = end;
                lt.add(a);
            }
            end = (Integer) proj.get(i + 1);
    		/*判断当前区域是否在序列中*/
            if (isInSequence(star, end, sqA) || isInSequence(star, end, sqB)) {
                int[] a = new int[2];
                a[0] = star;
                a[1] = end;
                lt.add(a);
            }
        }
        if (proj.size() > 0) {
            if (isInSequence(end, end, sqA) || isInSequence(end, end, sqB)) {
                int[] a = new int[2];
                a[0] = end;
                a[1] = end;
                lt.add(a);
            }
        }
    	
    	/*3.连接连续区域*/
        star = -1;
        end = -1;
        ArrayList result = new ArrayList();
        for (int i = 0; i < lt.size(); i++) {
            int[] b = (int[]) lt.get(i);
            if (star < 0 && end < 0) {
                star = b[0];
                end = b[1];
                continue;
            }
            if (end == b[0] || end == (b[0] - 1)) {
                end = b[1];
            } else {
                int[] a = new int[2];
                a[0] = star;
                a[1] = end;
                result.add(a);
                star = b[0];
                end = b[1];
            }
        }
        int[] a = new int[2];
        a[0] = star;
        a[1] = end;
        result.add(a);

        return result;
    }

    private boolean isInSequence(int star, int end, ArrayList sq) {
        boolean flag = false;
        if (sq == null) return false;
        for (int i = 0; i < sq.size(); i++) {
            int[] b = (int[]) sq.get(i);
            if (star >= b[0] && end <= b[1]) {
                flag = true;
                break;
            }
            if (end < b[0]) break;
        }
        return flag;
    }

    private void combineSimMx() {
		/* 处理概念 */
        // 1.如果Aij>=t，确定Aij,同时修改对应的传播矩阵Bij
        double cget1 = 0;
        double cget2 = 0;
        double pget1 = 0;
        double pget2 = 0;
        for (int i = 0; i < sourceConceptNum; i++) {
            for (int j = 0; j < targetConceptNum; j++) {
                if (cnptSimMx.get(i, j) >= OKSIM) {
                    // B的i行
                    for (int k = 0; k < targetConceptNum; k++) {
                        if (k != j) {
                            pgSimMxCnpt.setQuick(i, k, 0);
                        }
                    }
                    // B的j列
                    for (int k = 0; k < sourceConceptNum; k++) {
                        if (k != i) {
                            pgSimMxCnpt.setQuick(k, j, 0);
                        }
                    }
                }
            }
        }
        // 2.如果Aij<t，不能确定Aij,考察传播矩阵Bij
        // 2.1Bij<t，Bij肯定是噪声
        for (int i = 0; i < sourceConceptNum; i++) {
            for (int j = 0; j < targetConceptNum; j++) {
                if (pgSimMxCnpt.getQuick(i, j) > 0 && pgSimMxCnpt.getQuick(i, j) < BADSIM) {
                    pgSimMxCnpt.set(i, j, 0);
                }
                if (cnptSimMx.getQuick(i, j) > 0.0001) {
                    cget1 = cget1 + 1.0;
                }
                if (pgSimMxCnpt.getQuick(i, j) > 0.0001) {
                    cget2 = cget2 + 1.0;
                }
            }
        }

        // 合并处理结果
        double fc1 = cget1 / (cget1 + cget2);
        double fc2 = cget2 / (cget1 + cget2);
        for (int i = 0; i < sourceConceptNum; i++) {
            for (int j = 0; j < targetConceptNum; j++) {
                if (cnptSimMx.getQuick(i, j) < OKSIM) {
//					simMxConcept[i][j] = (simMxConcept[i][j] + pgSimMxConcept[i][j]) / 2.0;
                    cnptSimMx.setQuick(i, j, cnptSimMx.getQuick(i, j) * fc1 + pgSimMxCnpt.getQuick(i, j) * fc2);
                    //simMxConcept[i][j] =pgSimMxConcept[i][j];
                }
            }
        }

		/* 处理属性 */
        // 1.如果Aij>=t，确定Aij,同时修改对应的传播矩阵Bij
        for (int i = 0; i < sourcePropNum; i++) {
            for (int j = 0; j < targetPropNum; j++) {
                if (pgSimMxProp.getQuick(i, j) >= OKSIM) {
                    // B的i行
                    for (int k = 0; k < targetPropNum; k++) {
                        if (k != j) {
                            pgSimMxProp.setQuick(i, k, 0);
                        }
                    }
                    // B的j列
                    for (int k = 0; k < sourcePropNum; k++) {
                        if (k != i) {
                            pgSimMxProp.setQuick(k, j, 0);
                        }
                    }
                }
            }
        }
        // 2.如果Aij<t，不能确定Aij,考察传播矩阵Bij
        // 2.1Bij<t，Bij肯定是噪声
        for (int i = 0; i < sourcePropNum; i++) {
            for (int j = 0; j < targetPropNum; j++) {
                if (pgSimMxProp.getQuick(i, j) > 0 && pgSimMxProp.getQuick(i, j) < BADSIM) {
                    pgSimMxProp.setQuick(i, j, 0);
                }
                if (propSimMx.getQuick(i, j) > 0.0001) {
                    pget1 = pget1 + 1.0;
                }
                if (pgSimMxProp.getQuick(i, j) > 0.0001) {
                    pget2 = pget2 + 1.0;
                }
            }
        }

        // 合并处理结果
        double fp1 = pget1 / (pget1 + pget2);
        double fp2 = pget2 / (pget1 + pget2);
        for (int i = 0; i < sourcePropNum; i++) {
            for (int j = 0; j < targetPropNum; j++) {
                if (propSimMx.getQuick(i, j) < OKSIM) {
//					simMxProp[i][j] = (simMxProp[i][j] + pgSimMxProp[i][j]) / 2.0;
                    propSimMx.setQuick(i, j, propSimMx.getQuick(i, j) * fp1 + pgSimMxProp.getQuick(i, j) * fp2);
                    //simMxProp[i][j] = pgSimMxProp[i][j];
                }
            }
        }
    }

    private int getCnptDesCapacity(TextDes des) {
        int sum = 0;
        ArrayList lt = new ArrayList();
    	/*自身描述*/
        lt = (ArrayList) des.text.get(0);
        sum += lt.size();
    	/*层次描述*/
        lt = (ArrayList) des.text.get(1);
        sum += ((TextDes) lt.get(0)).text.size();
        sum += ((TextDes) lt.get(1)).text.size();
        sum += ((TextDes) lt.get(2)).text.size();
        sum += ((TextDes) lt.get(3)).text.size();
        sum += ((TextDes) lt.get(4)).text.size();
    	/*附加属性描述*/
        lt = (ArrayList) des.text.get(2);
        sum += ((TextDes) lt.get(0)).text.size();
        sum += ((TextDes) lt.get(1)).text.size();
    	/*实例描述*/
        lt = (ArrayList) des.text.get(3);
        sum += ((TextDes) lt.get(0)).text.size();
        return sum;
    }

    private void packSimPgPara(ArrayList list) {
        // 模型
        list.add(0, m_source);
        list.add(1, m_target);

        // 源本体
        list.add(2, sourceConceptNum);
        list.add(3, sourcePropNum);
        list.add(4, sourceInsNum);
        list.add(5, sourceConceptName);
        list.add(6, sourcePropName);
        list.add(7, sourceInsName);
        list.add(8, new ArrayList());
        list.add(9, sourceBaseURI);

        // 目标本体
        list.add(10, targetConceptNum);
        list.add(11, targetPropNum);
        list.add(12, targetInsNum);
        list.add(13, targetConceptName);
        list.add(14, targetPropName);
        list.add(15, targetInsName);
        list.add(16, new ArrayList());

        list.add(17, targetBaseURI);

        //相似度矩阵的拷贝
        SparseDoubleMatrix2D simMxC = (SparseDoubleMatrix2D) cnptSimMx.copy();
        list.add(18, simMxC);
        SparseDoubleMatrix2D simMxP = (SparseDoubleMatrix2D) propSimMx.copy();
        list.add(19, simMxP);

        SparseDoubleMatrix2D simMxI = (SparseDoubleMatrix2D) insSimMx.copy();
        list.add(20, simMxI);

        //本体full基本信息
        list.add(21, sourceAnonCnpt);
        list.add(22, sourceAnonProp);
        list.add(23, sourceAnonIns);
        list.add(24, targetAnonCnpt);
        list.add(25, targetAnonProp);
        list.add(26, targetAnonIns);

        //本体的语义子图
        list.add(27, s_cnptStm);
        list.add(28, s_propStm);
        list.add(29, t_cnptStm);
        list.add(30, t_propStm);
    }

    /*************************************
     * 难以确定阀值的情况，通过这个函数来辅助选择
     *************************************/
//    private void bestThresholdEvaluation() {
//        ArrayList lt = new ArrayList();
//        int refMapNum = 0;
//        MapRecord[] refMapResult = null;
//        int curMapNum = 0;
//        MapRecord[] curMapResult = null;
//
//		/*参数*/
//        String curAlignFile = "./dataset/OAEI2011/anatomy/Lily-best.rdf";
//        double CTMIN = 0.20;//最小阈值
//        double CTMAX = 0.80;//最大阈值
//        double pt = 0.1;
//        double STEP = 0.01;//步长
//        boolean isSaveResult = true;//是否保存结果
//
//		/*数据读取*/
//        setOntFile();
//        parseOnt();
//        init();
//
//		/*概念和关系集合*/
//        Set cnptSet = new HashSet();
//        for (int i = 0; i < sourceConceptNum; i++) {
//            cnptSet.add(sourceConceptName[i]);
//        }
//        for (int i = 0; i < targetConceptNum; i++) {
//            cnptSet.add(targetConceptName[i]);
//        }
//        Set propSet = new HashSet();
//        for (int i = 0; i < sourcePropNum; i++) {
//            propSet.add(sourcePropName[i]);
//        }
//        for (int i = 0; i < targetPropNum; i++) {
//            propSet.add(targetPropName[i]);
//        }
//
//        /**判断当前结果的P-R变化曲线**/
//        double ct = CTMIN;
//        for (; ct >= CTMIN && ct <= CTMAX; ) {
//			/*读取当前结果*/
//            int orgMapNum = 0;
//            try {
//                lt = new MappingFile().read4xml(curAlignFile);
//                orgMapNum = ((Integer) lt.get(0)).intValue();
//                ArrayList tempLt = (ArrayList) lt.get(1);
//                ArrayList getLt = new ArrayList();
//                for (Iterator it = tempLt.iterator(); it.hasNext(); ) {
//                    MapRecord tr = (MapRecord) it.next();
//                    boolean flag = false;
//                    if (cnptSet.contains(tr.sourceLabel)) {
//                        if (tr.similarity >= ct) {
//                            flag = true;
//                        }
//                    } else if (propSet.contains(tr.sourceLabel)) {
//                        if (tr.similarity >= pt) {
//                            flag = true;
//                        }
//                    }
//                    if (flag) {
//                        getLt.add(tr);
//                    }
//                }
//                curMapNum = getLt.size();
//                curMapResult = new MapRecord[curMapNum];
//                curMapResult = (MapRecord[]) getLt.toArray(new MapRecord[0]);
//				/*过滤*/
//
//            } catch (MalformedURLException e) {
//                System.out.println("Can't open refalign result file!" + e.toString());
//            } catch (DocumentException e) {
//                System.out.println("Can't open refalign result file!" + e.toString());
//            }
//
//			/*读取标准结果*/
//            try {
//                lt = new MappingFile().read4xml(refalignFile);
//                refMapNum = ((Integer) lt.get(0)).intValue();
//                refMapResult = new MapRecord[refMapNum];
//                refMapResult = (MapRecord[]) ((ArrayList) lt.get(1)).toArray(new MapRecord[0]);
//            } catch (MalformedURLException e) {
//                System.out.println("Can't open refalign result file!" + e.toString());
//            } catch (DocumentException e) {
//                System.out.println("Can't open refalign result file!" + e.toString());
//            }
//
//			/*评估*/
//            System.out.println("ct=" + ct + ",pt=" + pt + "-------");
//            lt = new EvaluateMapping().getEvaluation(refMapNum, refMapResult, curMapNum, curMapResult);
//
//            if (isSaveResult) {
//				/*重新保存结果*/
//                MappingFile mapFile = new MappingFile();
//                if (lilyFileName.length() == 0) {
//                    lilyFileName = "lily";
//                }
//				/*XML文件的方式*/
//                mapFile.setBaseURI(sourceBaseURI, targetBaseURI);
//                mapFile.save2rdf(sourceOntFile, targetOntFile, curMapNum, curMapResult, lilyFileName + ".rdf");
//                System.out.println("Ref Align: Num:" + refMapNum);
//                System.out.println("My Align: Num:" + orgMapNum);
//                System.out.println("New Align: Num:" + curMapNum);
//            }
//
//			/*增加阀值步长*/
//            ct += STEP;
//            if (STEP == 0) {
//                break;
//            }
//        }
//
//
//    }
    private void combineSubGraph(ArrayList cbStm, ArrayList newStm, HashMap hash) {
        for (Iterator it = newStm.iterator(); it.hasNext(); ) {
            Statement st = (Statement) it.next();
            if (hash.keySet().contains(st)) {
                int key = (Integer) hash.get(st);
                if (!cbStm.contains(key) && metaElmInTriple(st) < 2) {
                    cbStm.add(key);
                }
            } else {
                newGenStm.add(st);
            }
        }
        newStm.clear();
        newStm = null;
    }

    private int metaElmInTriple(Statement stm) {
        int metaNum = 0;
        Resource sub = stm.getSubject();
        Property prop = stm.getPredicate();
        RDFNode obj = stm.getObject();
        String suri = null, puri = null, ouri = null;

        if (sub.isURIResource()) {
            suri = sub.getNameSpace();
        }
        if (prop.isURIResource()) {
            puri = prop.getNameSpace();
        }
        if (obj.isURIResource()) {
            ouri = obj.asNode().getNameSpace();
        }

        if (ontParse.metaURISet.contains(suri)) {
            metaNum++;
        }
        if (ontParse.metaURISet.contains(puri)) {
            metaNum++;
        }
        if (ontParse.metaURISet.contains(ouri)) {
            metaNum++;
        }
        return metaNum;
    }

    private HashMap buildStmHash(ArrayList stmLt) {
        int count = 0;
        HashMap hash = new HashMap();
        for (Iterator it = stmLt.iterator(); it.hasNext(); ) {
            Statement st = (Statement) it.next();
            hash.put(st, count);
            count++;
        }
        return hash;
    }

    private void reverseStmHash(ArrayList cur, ArrayList source) {
        ArrayList lt = new ArrayList();
        for (Iterator it = cur.iterator(); it.hasNext(); ) {
            int pos = (Integer) it.next();
            lt.add(source.get(pos));
        }
        cur.clear();
        cur.addAll(lt);
    }

    /*使用相似度传播的策略*/
    private boolean isNeedProg() {
        if (sourceConceptNum > 1000 || targetConceptNum > 1000 ||
                sourcePropNum > 1000 || targetPropNum > 1000) {
            return false;
        } else {
            return true;
        }
    }

    /**************************
     * 处理Anatomy数据集中的同义词
     **************************/
    private void preProcessAnatomy(OntModel m, String baseURI, String[] cnptName, int cnptNum) {
        OntModel partModel = null;
		/*判断当前是哪个本体*/
        if (baseURI.equals(sourceBaseURI)) {
            partModel = m_sourcePart;
        } else {
            partModel = m_targetPart;
        }
			
		/*遍历概念,处理同义词*/
        for (int i = 0; i < cnptNum; i++) {
            OntClass c = m.getOntClass(baseURI + cnptName[i]);
			
			/*Class在Part Model中的声明*/
            partModel.add(c, m.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), m.getResource("http://www.w3.org/2002/07/owl#Class"));

            Selector selector = new SimpleSelector(c, m.getProperty("http://www.geneontology.org/formats/oboInOwl#hasRelatedSynonym"), (RDFNode) null);
            ArrayList labelLt = new ArrayList();
			/*寻找同义词入口*/
            for (StmtIterator itx = m.listStatements(selector); itx.hasNext(); ) {
                Statement st = itx.next();
                Selector slx = new SimpleSelector((Resource) st.getObject(), m.getProperty("http://www.w3.org/2000/01/rdf-schema#label"), (RDFNode) null);
        		/*取出label*/
                for (StmtIterator ity = m.listStatements(slx); ity.hasNext(); ) {
                    Statement stemp = ity.next();
                    labelLt.add(stemp.getObject());
                }
            }
			/*将同义词作为comment加入*/
            String newComment = "";
            for (Iterator itx = labelLt.iterator(); itx.hasNext(); ) {
                String rStr = itx.next().toString();
                String curStr = rStr.substring(0, rStr.indexOf("^^"));
                newComment = newComment + curStr + " ";
            }
            if (newComment.length() > 1) {
                Literal liT = m.createLiteral(newComment);
                c.addComment(liT);
//        		System.out.println(c.toString()+" http://www.w3.org/2000/01/rdf-schema#comment "+liT.toString());
//        		System.out.println("验证："+c.getComment(null));
            }
        }
		
		/*处理Part-Of关系*/
        for (int i = 0; i < cnptNum; i++) {
            OntClass c = m.getOntClass(baseURI + cnptName[i]);
			/*1.找到a subClassOf blank*/
            Selector slx = new SimpleSelector(c, m.getProperty("http://www.w3.org/2000/01/rdf-schema#subClassOf"), (RDFNode) null);
            for (StmtIterator itx = m.listStatements(slx); itx.hasNext(); ) {
                Statement stx = itx.next();
                RDFNode obj = stx.getObject();
				/*2.找到blank someValuesFrom b*/
                if (ontParse.isBlankNode(obj.toString())) {
                    Selector sly = new SimpleSelector((Resource) obj, m.getProperty("http://www.w3.org/2002/07/owl#someValuesFrom"), (RDFNode) null);
					/*3.添加 a subClassOf b*/
                    for (StmtIterator ity = m.listStatements(sly); ity.hasNext(); ) {
                        Statement sty = ity.next();
                        partModel.add(c, m.getProperty("http://www.w3.org/2000/01/rdf-schema#subClassOf"), m.getOntClass(sty.getObject().toString()));
                    }
                }
            }
        }
    }

    private void turnTargetCnptPos() {
		/*概念层次根节点集合*/
        ArrayList sCnptHRoot = new ArrayList();
        for (int i = 0; i < targetConceptNum; i++) {
            OntClass sc = m_target.getOntClass(targetBaseURI + targetConceptName[i]);
			/*判断当前概念是否是根节点*/
            if (sc != null && ontParse.isCnptHRoot(m_target, sc)) {
                sCnptHRoot.add(sc);
            }
        }

        /**从一个根节点出发，深度优先遍历图得到一条达到叶子节点的路径**/
        int count = 0;
        ArrayList used = new ArrayList();
        for (Iterator itx = sCnptHRoot.iterator(); itx.hasNext(); ) {
            OntClass rootC = (OntClass) itx.next();
            pathRec = new ArrayList();
            allPath = new ArrayList();
            pathRec.add(rootC);
            cnptDFTraversal(m_target, rootC);
			/*遍历以当前节点为根的全部路径*/
            for (Iterator ity = allPath.iterator(); ity.hasNext(); ) {
                ArrayList path = (ArrayList) ity.next();
                /**遍历路径上的每个概念**/
                for (Iterator itz = path.iterator(); itz.hasNext(); ) {
                    OntClass getC = (OntClass) itz.next();
                    String cName = getC.getLocalName();
                    if (!used.contains(cName) && getC.getNameSpace().equals(targetBaseURI)) {
                        targetConceptName[count] = cName;
                        used.add(cName);
                        count++;
                    }
                }
            }
            pathRec = null;
            allPath = null;
        }
        used = null;
        sCnptHRoot = null;
    }

    /**
     * 读取参数文件
     **/
    public void loadConfigFile() {
        // ParamStore.loadConfig();

        sourceOntFile = ParamStore.SourceOnt;
        targetOntFile = ParamStore.TargetOnt;
        refalignFile = ParamStore.RefAlignFile;
        lilyFileName = ParamStore.ResultFile;

        cnptSimThreshold = ParamStore.Concept_Similarity_Threshold;
        propSimThreshold = ParamStore.Property_Similarity_Threshold;

        snapSize = ParamStore.Snap_Size;
        topk = ParamStore.Top_k;
        nbScale = ParamStore.Neighbor_Scale;
        ptValue = ParamStore.Positive_Anchor_Threshold;
        ntValue = ParamStore.Negative_Anchor_Threshold;
        desCapValue = ParamStore.SemanticDoc_Threshold;

        OKSIM = ParamStore.OKSIM;
        BADSIM = ParamStore.BADSIM;
    }

    /*映射调试函数*/
    public void mappingDebugging() {
        System.out.println("Debugging");
		
		/*1  确定两个本体本身没有回路*/
		/*1.1 确定两个本体概念层次对应的有向图*/
		/*遍历所有概念，根据层次结构加入到有向图中*/
        DirectedGraph g = new DefaultDirectedGraph(DefaultEdge.class);

        for (int i = 0; i < sourceConceptNum; i++) {//遍历源本体概念
            g.addVertex("OA_" + sourceConceptName[i]);

            OntClass c = m_source.getOntClass(sourceBaseURI + sourceConceptName[i]);

//			System.out.println(c.toString());
//			System.out.println("baseURI:"+sourceBaseURI);

            ArrayList list = new ArrayList();
            ExtendedIterator it = c.listSubClasses(true);//取得概念的直接子概念
            while (it.hasNext()) {//遍历所有子概念
//				System.out.println("有子概念：");
                OntClass c_temp = (OntClass) it.next();
                String c_name = c_temp.getLocalName();
//				System.out.println("子概念："+c_temp.toString());
//				System.out.println("子概念URI:"+c_temp.getURI().toString());
                if ((new OntGraph()).getResourceBaseURI(c_temp.toString()).equals(sourceBaseURI)) {//将正确的这条边加入有向图
                    if (!g.containsVertex("OA_" + c_name)) {
                        g.addVertex("OA_" + c_name);
                    }
                    g.addEdge("OA_" + sourceConceptName[i], "OA_" + c_name);
//					System.out.println(c_name+" is-a " + sourceConceptName[i]);
                }
            }
            c = null;
        }
		
		/*判断本身的概念层次是否有环路*/
        CycleDetector t = new CycleDetector(g);
        if (t.detectCycles()) {
            System.out.println("源本体概念层次有环路！");
            System.out.println(t.findCycles().toString());
            return;
        }

        for (int i = 0; i < targetConceptNum; i++) {//遍历目标本体概念
            g.addVertex("OB_" + targetConceptName[i]);

            OntClass c = m_target.getOntClass(targetBaseURI + targetConceptName[i]);

            ArrayList list = new ArrayList();
            ExtendedIterator it = c.listSubClasses(true);//取得概念的直接子概念
            while (it.hasNext()) {//遍历所有子概念
                OntClass c_temp = (OntClass) it.next();
                String c_name = c_temp.getLocalName();

                if ((new OntGraph()).getResourceBaseURI(c_temp.toString()).equals(targetBaseURI)) {//将正确的这条边加入有向图
                    if (!g.containsVertex("OB_" + c_name)) {
                        g.addVertex("OB_" + c_name);
                    }
                    g.addEdge("OB_" + targetConceptName[i], "OB_" + c_name);
                }
            }
            c = null;
        }
		
		/*判断本身的概念层次是否有环路*/
        if (t.detectCycles()) {
            System.out.println("目标本体概念层次有环路！");
            System.out.println(t.findCycles().toString());
            return;
        }
		
		/*2.加入映射锚点*/
        IntArrayList rowList = new IntArrayList();
        IntArrayList colList = new IntArrayList();
        DoubleArrayList valueList = new DoubleArrayList();
        cnptSimMx.getNonZeros(rowList, colList, valueList);

        for (int k = 0; k < valueList.size(); k++) {
            int i = rowList.get(k);
            int j = colList.get(k);
            double tsim = valueList.get(k);

            if (tsim > OKSIM) {
                int na = t.findCycles().size();
//				System.out.println("添加锚点前的回路数目："+na);
                g.addEdge("OA_" + sourceConceptName[i], "OB_" + targetConceptName[j]);
                g.addEdge("OB_" + targetConceptName[j], "OA_" + sourceConceptName[i]);
                int nb = t.findCycles().size();
//				System.out.println("添加锚点后的回路数目：--"+nb);
                if (nb - na > 2) {//当前的映射产生了回路，需要放弃
                    g.removeEdge("OA_" + sourceConceptName[i], "OB_" + targetConceptName[j]);
                    g.removeEdge("OB_" + targetConceptName[j], "OA_" + sourceConceptName[i]);
//					System.out.println("当前的映射产生了回路，需要放弃");

                }
            }


        }
		
		/*判断每个映射加入后有无环路产生*/
        for (int k = 0; k < valueList.size(); k++) {
            int i = rowList.get(k);
            int j = colList.get(k);
            double tsim = valueList.get(k);

            if (tsim > cnptSimThreshold && tsim < OKSIM) {
				/*错误的映射加入后会导致不一致！*/
                int na = t.findCycles().size();
//				System.out.println("添加映射前的回路数目："+na);
                g.addEdge("OA_" + sourceConceptName[i], "OB_" + targetConceptName[j]);
                int nb = t.findCycles().size();
                if (nb > na) {
//					System.out.println("发现一条错误映射！！");
                    cnptSimMx.setQuick(i, j, 0);
                }
                g.removeEdge("OA_" + sourceConceptName[i], "OB_" + targetConceptName[j]);

                g.addEdge("OB_" + targetConceptName[j], "OA_" + sourceConceptName[i]);
                nb = t.findCycles().size();
                if (nb > na) {
//					System.out.println("发现一条错误映射！！");
                    cnptSimMx.setQuick(i, j, 0);
                }
                g.removeEdge("OB_" + targetConceptName[j], "OA_" + sourceConceptName[i]);

            }

        }

    }

/*-----End Line-----*/
}
