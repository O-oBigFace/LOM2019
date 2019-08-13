/************************************************
 * Source code information
 * -----------------------
 * Original author	 Peng Wang, School of Computer Science & Eng., Southeast University
 * Author email      pwangseu@gmail.com
 * Web               http://ontomapping.googlepages.com
 * Created			 2007-4-25
 * Filename          Rclm.java
 * Version           2.0
 *
 * Last modified on  2007-4-26
 *               by  Peng Wang
 * -----------------------
 * Functions describe:
 * 基于子图抽取方法的主类
 * 控制整个Semantic Subgraph-based方法的运行过程
 * 包含也不同的实验的入口函数
 ***********************************************/
package lily.onto.mapping.method;

import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import lily.onto.handle.describe.OntDes;
import lily.onto.handle.graph.OntGraph;
import lily.onto.handle.propagation.*;
import lily.onto.parse.OWLOntParse;
import lily.tool.datastructure.*;
import lily.tool.filter.SimpleFilter;
import lily.tool.filter.StableMarriageFilter;
import lily.tool.mappingfile.MappingFile;
import lily.tool.parameters.ParamStore;
import lily.tool.strsimilarity.StrEDSim;
import lily.tool.textsimilarity.TextDocSim;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/*******************************************************************************
 * Class information -------------------
 *
 * @author Peng Wang
 * @date 2007-4-25
 *
 * describe: 
 * 主要类
 * 语义子图方法的起点
 ******************************************************************************/
public class Rclm {
    // 源和目标本体的文件
    public String sourceOntFile;

    public String targetOntFile;

    // 映射结果的文件名
    public String lilyFileName = "";

    // 基准映射文件
    public String refalignFile;

    // 源和目标本体的model
    public OntModel m_source;
    public OntModel m_target;

    public OntModel m_sourceCopy;
    public OntModel m_targetCopy;

    // 概念数目
    public int sourceConceptNum;

    public int targetConceptNum;

    // 属性数目
    public int sourcePropNum;

    public int sourceDataPropNum;

    public int sourceObjPropNum;

    public int targetPropNum;

    public int targetDataPropNum;

    public int targetObjPropNum;

    // 实例数目
    public int sourceInsNum;

    public int targetInsNum;

    // 概念名
    public String[] sourceConceptName;

    public String[] targetConceptName;

    // 属性名
    public String[] sourcePropName;

    public String[] sourceDataPropName;

    public String[] sourceObjPropName;

    public String[] targetPropName;

    public String[] targetDataPropName;

    public String[] targetObjPropName;

    // 实例名
    public String[] sourceInsName;

    public String[] targetInsName;

    // 匿名资源
    ArrayList sourceAnonCnpt;

    ArrayList sourceAnonProp;

    ArrayList sourceAnonIns;

    ArrayList targetAnonCnpt;

    ArrayList targetAnonProp;

    ArrayList targetAnonIns;

    // 不局限于baseURI下的本体元素
    public int sourceFullConceptNum;

    public int sourceFullPropNum;

    public int sourceFullDataPropNum;

    public int sourceFullObjPropNum;

    public int sourceFullInsNum;

    public OntClass[] sourceFullConceptName;

    public OntProperty[] sourceFullPropName;

    public DatatypeProperty[] sourceFullDataPropName;

    public ObjectProperty[] sourceFullObjPropName;

    public Individual[] sourceFullInsName;

    public int targetFullConceptNum;

    public int targetFullPropNum;

    public int targetFullDataPropNum;

    public int targetFullObjPropNum;

    public int targetFullInsNum;

    public OntClass[] targetFullConceptName;

    public OntProperty[] targetFullPropName;

    public DatatypeProperty[] targetFullDataPropName;

    public ObjectProperty[] targetFullObjPropName;

    public Individual[] targetFullInsName;

    // 子图信息
    public ConceptSubGraph[] sourceCnptSubG;

    public PropertySubGraph[] sourcePropSubG;

    public ConceptSubGraph[] targetCnptSubG;

    public PropertySubGraph[] targetPropSubG;

    // 全图信息,三元组形式
    public ArrayList sourceStmList;

    public ArrayList targetStmList;

    // 文本描述信息
    public TextDes[] sourceCnptTextDes;

    public TextDes[] sourcePropTextDes;

    public TextDes[] sourceInsTextDes;

    public ArrayList[] sourceCnptOtTextDes;

    public ArrayList[] sourcePropOtTextDes;

    public ArrayList sourceFullOtTextDes;

    public TextDes[] targetCnptTextDes;

    public TextDes[] targetPropTextDes;

    public TextDes[] targetInsTextDes;

    public ArrayList[] targetCnptOtTextDes;

    public ArrayList[] targetPropOtTextDes;

    public ArrayList targetFullOtTextDes;

    // 相似矩阵
    public double[][] simMxConcept;

    public double[][] simMxProp;

    public double[][] simMxDataProp;

    public double[][] simMxObjProp;

    public double[][] simMxIns;

    //压缩的稀疏矩阵，处理大规模本体必须用
    public ArrayList simLargeMxConcept;
    public ArrayList simLargeMxProp;
    public ArrayList simLargeMxIns;

    // 相似度传播后的相似矩阵
    public double[][] pgSimMxConcept;

    public double[][] pgSimMxProp;

    // 传播模式
    public boolean isSubProg;

    // 映射结果数
    public int mappingNum;
    public int cMappingNum;
    public int pMappingNum;// 分别记录几种映射的数目

    // 映射结果
    public MapRecord[] mappingResult;

    // 结果评价
    public double precision;

    public double recall;

    public double f1Measure;

    // 文本相似和子图相似的权重
    public double graphWeight;

    public double literalWeight;

    // base URI
    public String sourceBaseURI;

    public String targetBaseURI;

    // 相似度阀值
    public double cnptSimThreshold;// 概念相似阀值

    public double dpSimThreshold;// Datatype Property相似阀值

    public double opSimThreshold;// Object Property相似阀值

    public double propSimThreshold;// Property相似阀值

    // TextMatch可信结果位置
    public Set sourceCnptOkSimPos;

    public Set sourcePropOkSimPos;

    public Set targetCnptOkSimPos;

    public Set targetPropOkSimPos;

    //丰富后的新增三元组
    public Set sourceNewStm;
    public Set targetNewStm;

    //层次树结构
    public CnptTree sTree;
    public CnptTree tTree;

    //进度条通信变量
    public int[] pbarValue;

    // 是否需要相似度传播
    public boolean isNeedSimProg;

    // 相似度传播策略
    public int simProgType;

    //大本体元素局部上下文快照尺寸
    public int snapSize = 25;

    //本体处理对象
    public OWLOntParse ontParse = new OWLOntParse();

    // constants
    public int EQUALITY = 0;

    public int GENERAL = 1;

    public int SPECIFIC = 2;

    public boolean DISTINCT_DP_OP = false;

    public double OKSIM = 0.5;

    public double BADSIM = 0.001;

    public int LARGEONTBASE = 2000;


    /***************************************************************************
     * 基本测试函数
     **************************************************************************/
    @SuppressWarnings("unchecked")
    public static void runStandard() {
        ArrayList finalList = new ArrayList();
        Rclm ontM = new Rclm();
        ontM.loadConfigFile();
        ontM.setOntFile();
        System.out.println("Parsing ontologies...");
        long start = System.currentTimeMillis();// 开始计时
        // 最新版OAEI中，第一次运行可能会出现问题，尝试两次。
        try {
            ontM.parseOnt();
        } catch (Exception e) {
            try {
                ontM.parseOnt();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        ontM.init();
        long end = System.currentTimeMillis();// 结束计时
        long costtime = end - start;// 统计算法时间
        System.out.println("Size: " + ontM.m_source.size() + "\t" + ontM.m_target.size());

//        if (ontM.m_source.size() > LARGEONTBASE || ontM.m_target.size() > LARGEONTBASE) { //大本体，调用LOM
//            ontM = null;
//            LOM matcher = new LOM();
//            matcher.run();
//        } else {
            ontM.run();
            ontM.mappingDebugging();//映射调试
            ontM.showResult(false); //显示映射结果
            ontM.saveResult(); //保存映射结果
//            if (ParamStore.doEvaluation) {
//                ontM.evaluate(); //评估
//                //finalList.add(0, ontM.mappingNum);
//                //finalList.add(1, ontM.mappingResult);
//                //finalList.add(2, ontM.f1Measure); //后来添加的，返回F值
//            }
            ontM = null;
            //System.out.println("Time：" + (double) costtime / 1000. + "s");
            //return finalList;
//        }

    }

    /***************************************************************************
     * 初始化，主要是基本的参数设置
     **************************************************************************/
    public void init() {
        graphWeight = 0.0;
        literalWeight = 1.0;
        isSubProg = false;// true:相似度按照子图传播

        sourceCnptSubG = new ConceptSubGraph[sourceConceptNum];
        sourcePropSubG = new PropertySubGraph[sourcePropNum];
        targetCnptSubG = new ConceptSubGraph[targetConceptNum];
        targetPropSubG = new PropertySubGraph[targetPropNum];

        /**构造概念层次树**/
        sTree = new CnptTree();
        buildCnptTree(sTree, true);
        tTree = new CnptTree();
        buildCnptTree(tTree, false);

        return;
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


    /***************************************************************************
     * 设置本体文件
     **************************************************************************/
    public void setOntFile() {
        System.out.println("Source Ontology:" + sourceOntFile);
        System.out.println("Target Ontology:" + targetOntFile);
        System.out.println("RefalignFile:" + refalignFile);
        System.out.println("OutputFileURL:" + lilyFileName);
    }

    private void setOntFile(String sourceFile, String targetFile) {
        sourceOntFile = sourceFile;
        targetOntFile = targetFile;
        refalignFile = "";
    }

    private void setOntFile(String sourceFile, String targetFile, String refFile) {
        sourceOntFile = sourceFile;
        targetOntFile = targetFile;
        refalignFile = refFile;
    }

    /***************************************************************************
     * 解析本体
     **************************************************************************/
    public void parseOnt() {
        ArrayList list = new ArrayList();

        if (pbarValue == null) pbarValue = new int[4];
        pbarValue[0] = 0; //进度条初始化

        // 源本体----------------------------------------------
        m_source = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
        // The ontology file information
        m_source.getDocumentManager().addAltEntry("http://pengwang/", sourceOntFile);
        // Read the reference ontology file
        ontParse.readOntFile(m_source, sourceOntFile);

        pbarValue[0] = 10; //进度条控制

        // 源本体的base URI
        sourceBaseURI = ontParse.getOntBaseURI(m_source);

        // 类型修补
        ontParse.repairOntType(m_source);

        // Get all Classes of Ontology
        list = ontParse.listAllConceptsFilterBaseURI(m_source, sourceBaseURI);
        sourceConceptNum = ((Integer) list.get(0)).intValue();
        sourceConceptName = new String[sourceConceptNum];
        sourceConceptName = (String[]) ((ArrayList) list.get(1)).toArray(new String[0]);

        // Get all datatype properties
        list = ontParse.listAllDatatypeRelationsURI(m_source, sourceBaseURI);
        sourceDataPropNum = ((Integer) list.get(0)).intValue();
        sourceDataPropName = new String[sourceDataPropNum];
        sourceDataPropName = (String[]) ((ArrayList) list.get(1)).toArray(new String[0]);

        // Get all object properties
        list = ontParse.listAllObjectRelationsURI(m_source, sourceBaseURI);
        sourceObjPropNum = ((Integer) list.get(0)).intValue();
        sourceObjPropName = new String[sourceObjPropNum];
        sourceObjPropName = (String[]) ((ArrayList) list.get(1)).toArray(new String[0]);

        // Get all properties
        sourcePropNum = sourceDataPropNum + sourceObjPropNum;
        sourcePropName = new String[sourcePropNum];
        for (int i = 0; i < sourceDataPropNum; i++) {
            sourcePropName[i] = sourceDataPropName[i];
        }
        for (int i = 0; i < sourceObjPropNum; i++) {
            sourcePropName[i + sourceDataPropNum] = sourceObjPropName[i];
        }

        // get all instances
        list = ontParse.listAllInstances(m_source);
        sourceInsNum = ((Integer) list.get(0)).intValue();
        sourceInsName = new String[sourceInsNum];
        sourceInsName = (String[]) ((ArrayList) list.get(1)).toArray(new String[0]);

        pbarValue[0] = 30; //进度条控制

		/* 不局限于baseURI的本体信息 */
        ArrayList fullOntlist = ontParse.getFullOntInfo(m_source);
        // 概念信息
        list = (ArrayList) fullOntlist.get(0);
        sourceFullConceptNum = ((Integer) list.get(0)).intValue();
        sourceFullConceptName = new OntClass[sourceFullConceptNum];
        sourceFullConceptName = (OntClass[]) ((ArrayList) list.get(1)).toArray(new OntClass[0]);
        // 属性信息
        list = (ArrayList) fullOntlist.get(1);
        sourceFullPropNum = ((Integer) list.get(0)).intValue();
        sourceFullPropName = new OntProperty[sourceFullPropNum];
        sourceFullPropName = (OntProperty[]) ((ArrayList) list.get(1)).toArray(new OntProperty[0]);
        // DatatypeProperty
        list = (ArrayList) fullOntlist.get(2);
        sourceFullDataPropNum = ((Integer) list.get(0)).intValue();
        sourceFullDataPropName = new DatatypeProperty[sourceFullDataPropNum];
        sourceFullDataPropName = (DatatypeProperty[]) ((ArrayList) list.get(1)).toArray(new DatatypeProperty[0]);
        // ObjectProperty
        // DatatypeProperty
        list = (ArrayList) fullOntlist.get(3);
        sourceFullObjPropNum = ((Integer) list.get(0)).intValue();
        sourceFullObjPropName = new ObjectProperty[sourceFullObjPropNum];
        sourceFullObjPropName = (ObjectProperty[]) ((ArrayList) list.get(1)).toArray(new ObjectProperty[0]);
        // 实例信息
        list = (ArrayList) fullOntlist.get(4);
        sourceFullInsNum = ((Integer) list.get(0)).intValue();
        sourceFullInsName = new Individual[sourceFullInsNum];
        sourceFullInsName = (Individual[]) ((ArrayList) list.get(1)).toArray(new Individual[0]);

        // 匿名资源
        sourceAnonCnpt = new ArrayList();
        sourceAnonProp = new ArrayList();
        sourceAnonIns = new ArrayList();
        list = ontParse.getOntAnonInfo(m_source);
        sourceAnonCnpt = (ArrayList) list.get(0);
        sourceAnonProp = (ArrayList) list.get(1);
        sourceAnonIns = (ArrayList) list.get(2);

        pbarValue[0] = 50; //进度条控制
        System.out.println("Source ontology loaded");

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
        targetConceptNum = ((Integer) list.get(0)).intValue();
        targetConceptName = new String[targetConceptNum];
        targetConceptName = (String[]) ((ArrayList) list.get(1)).toArray(new String[0]);

        // Get all datatype properties
        list = ontParse.listAllDatatypeRelationsURI(m_target, targetBaseURI);
        targetDataPropNum = ((Integer) list.get(0)).intValue();
        targetDataPropName = new String[targetDataPropNum];
        targetDataPropName = (String[]) ((ArrayList) list.get(1)).toArray(new String[0]);

        // Get all object properties
        list = ontParse.listAllObjectRelationsURI(m_target, targetBaseURI);
        targetObjPropNum = ((Integer) list.get(0)).intValue();
        targetObjPropName = new String[targetObjPropNum];
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
        targetInsNum = ((Integer) list.get(0)).intValue();
        targetInsName = new String[targetInsNum];
        targetInsName = (String[]) ((ArrayList) list.get(1)).toArray(new String[0]);

        pbarValue[0] = 80; //进度条控制

		/* 不局限于baseURI的本体信息 */
        fullOntlist = ontParse.getFullOntInfo(m_target);
        // 概念信息
        list = (ArrayList) fullOntlist.get(0);
        targetFullConceptNum = ((Integer) list.get(0)).intValue();
        targetFullConceptName = new OntClass[targetFullConceptNum];
        targetFullConceptName = (OntClass[]) ((ArrayList) list.get(1)).toArray(new OntClass[0]);
        // 属性信息
        list = (ArrayList) fullOntlist.get(1);
        targetFullPropNum = ((Integer) list.get(0)).intValue();
        targetFullPropName = new OntProperty[targetFullPropNum];
        targetFullPropName = (OntProperty[]) ((ArrayList) list.get(1)).toArray(new OntProperty[0]);
        // DatatypeProperty
        list = (ArrayList) fullOntlist.get(2);
        targetFullDataPropNum = ((Integer) list.get(0)).intValue();
        targetFullDataPropName = new DatatypeProperty[targetFullDataPropNum];
        targetFullDataPropName = (DatatypeProperty[]) ((ArrayList) list.get(1)).toArray(new DatatypeProperty[0]);
        // ObjectProperty
        // DatatypeProperty
        list = (ArrayList) fullOntlist.get(3);
        targetFullObjPropNum = ((Integer) list.get(0)).intValue();
        targetFullObjPropName = new ObjectProperty[targetFullObjPropNum];
        targetFullObjPropName = (ObjectProperty[]) ((ArrayList) list.get(1)).toArray(new ObjectProperty[0]);
        // 实例信息
        list = (ArrayList) fullOntlist.get(4);
        targetFullInsNum = ((Integer) list.get(0)).intValue();
        targetFullInsName = new Individual[targetFullInsNum];
        targetFullInsName = (Individual[]) ((ArrayList) list.get(1)).toArray(new Individual[0]);

        // 匿名资源
        targetAnonCnpt = new ArrayList();
        targetAnonProp = new ArrayList();
        targetAnonIns = new ArrayList();
        list = ontParse.getOntAnonInfo(m_target);
        targetAnonCnpt = (ArrayList) list.get(0);
        targetAnonProp = (ArrayList) list.get(1);
        targetAnonIns = (ArrayList) list.get(2);

        pbarValue[0] = 100; //进度条控制
        System.out.println("Target ontology loaded");
    }

    /***************************************************************************
     * 执行映射发现算法
     **************************************************************************/
    public void run() {
        // 匹配计算
        long start = System.currentTimeMillis();// 开始计时
        // 语义结构抽取
        System.out.println("Preprocessing...");
        // 在下面这个方法里面还有一部分参数的读入，之前的load()是其中的6个.
        reConsSemInf(true);
        long end1 = System.currentTimeMillis();// 结束计时
        long costtime1 = end1 - start;// 统计算法时间

        // 文本匹配
        System.out.println("Text Matching...");
        ontMatchText();
        long end2 = System.currentTimeMillis();// 结束计时
        long costtime2 = end2 - end1;// 统计算法时间

        // 结构匹配
        System.out.println("Structure Matching...");
        ontMatchStru();
        long star4 = System.currentTimeMillis();
        long end3 = 0;
        long costtime3 = 0;

        if (isNeedSimProg) {
            /**相似度传播**/
            simPropagation();
            end3 = System.currentTimeMillis();// 结束计时
            costtime3 = end3 - end2;// 统计算法时间
            star4 = System.currentTimeMillis();
            /**合并结果**/
//			System.out.println("合并结果");
            combineResult();
            pbarValue[2] = 100; //进度条数值
        }
        pbarValue[2] = 100; //进度条数值

        long end4 = System.currentTimeMillis();// 结束计时
        long costtime4 = end4 - star4;// 统计算法时间
        if (ParamStore.doEvaluation) {
            System.out.println("语义子图抽取时间：" + (double) costtime1 / 1000. + "秒");
            System.out.println("文本匹配算法时间：" + (double) costtime2 / 1000. + "秒");
            System.out.println("相似度传播算法时间：" + (double) costtime3 / 1000. + "秒");
            System.out.println("后处理时间：" + (double) costtime4 / 1000. + "秒");
            System.out.println("总时间：" + (double) (end4 - start) / 1000. + "秒");
        }
    }

    /***************************************************************************
     * 以子图为基础的相似度传播
     **************************************************************************/
    private void subSimPropagation() {
        pgSimMxConcept = new double[sourceConceptNum][targetConceptNum];
        pgSimMxProp = new double[sourcePropNum][targetPropNum];
        ArrayList paraList = new ArrayList();
        ArrayList lt = new ArrayList();

		/* 计算子图中其它元素的缺省相似度 */
//		System.out.println("概念子图中其它元素的缺省相似度");
        ArrayList[][] cnptOtSim = new ArrayList[sourceConceptNum][targetConceptNum];
        for (int i = 0; i < sourceConceptNum; i++) {
            for (int j = 0; j < targetConceptNum; j++) {
                if (sourceCnptOtTextDes[i].isEmpty()
                        || targetCnptOtTextDes[j].isEmpty()) {
                    cnptOtSim[i][j] = new ArrayList();
                } else {
                    cnptOtSim[i][j] = new TextDocSim().getOtTextSim(sourceCnptOtTextDes[i], targetCnptOtTextDes[j]);
                }
                // System.out.println(sourceConceptName[i]+"--"+targetConceptName[j]);
            }
        }
//		System.out.println("属性子图中其它元素的缺省相似度");
        ArrayList[][] propOtSim = new ArrayList[sourcePropNum][targetPropNum];
        for (int i = 0; i < sourcePropNum; i++) {
            for (int j = 0; j < targetPropNum; j++) {
                if (sourcePropOtTextDes[i].isEmpty()
                        || targetPropOtTextDes[j].isEmpty()) {
                    propOtSim[i][j] = new ArrayList();
                } else {
                    propOtSim[i][j] = new TextDocSim().getOtTextSim(sourcePropOtTextDes[i], targetPropOtTextDes[j]);
                }
                // System.out.println(sourcePropName[i]+"--"+targetPropName[j]);
            }
        }

		/* 源本体的语义文本描述 */
        // 构造输入参数链表
        packSubSimPgPara(paraList);
        paraList.add(29, cnptOtSim);
        paraList.add(30, propOtSim);

//		System.out.println("相似度传播");
        lt = new SepSubSimPropagation().ontSimPg(paraList);
        pgSimMxConcept = (double[][]) lt.get(0);
        pgSimMxProp = (double[][]) lt.get(1);
    }

    private void cbSubSimPropagation() {
        pgSimMxConcept = new double[sourceConceptNum][targetConceptNum];
        pgSimMxProp = new double[sourcePropNum][targetPropNum];
        ArrayList paraList = new ArrayList();
        ArrayList lt = new ArrayList();

		/* 计算子图中其它元素的缺省相似度 */
        System.out.println("概念子图中其它元素的缺省相似度");
        ArrayList[][] cnptOtSim = new ArrayList[sourceConceptNum][targetConceptNum];
        for (int i = 0; i < sourceConceptNum; i++) {
            for (int j = 0; j < targetConceptNum; j++) {
                if (sourceCnptOtTextDes[i].isEmpty()
                        || targetCnptOtTextDes[j].isEmpty()) {
                    cnptOtSim[i][j] = new ArrayList();
                } else {
                    cnptOtSim[i][j] = new TextDocSim().getOtTextSim(sourceCnptOtTextDes[i], targetCnptOtTextDes[j]);
                }
                // System.out.println(sourceConceptName[i]+"--"+targetConceptName[j]);
            }
        }
        System.out.println("属性子图中其它元素的缺省相似度");
        ArrayList[][] propOtSim = new ArrayList[sourcePropNum][targetPropNum];
        for (int i = 0; i < sourcePropNum; i++) {
            for (int j = 0; j < targetPropNum; j++) {
                if (sourcePropOtTextDes[i].isEmpty()
                        || targetPropOtTextDes[j].isEmpty()) {
                    propOtSim[i][j] = new ArrayList();
                } else {
                    propOtSim[i][j] = new TextDocSim().getOtTextSim(sourcePropOtTextDes[i], targetPropOtTextDes[j]);
                }
                // System.out.println(sourcePropName[i]+"--"+targetPropName[j]);
            }
        }

		/* 源本体的语义文本描述 */

        // 构造输入参数链表
        packSubSimPgPara(paraList);
        paraList.add(33, cnptOtSim);
        paraList.add(34, propOtSim);

//		System.out.println("相似度传播");
        lt = new CbSubSimPropagation().ontSimPg(paraList);
        pgSimMxConcept = (double[][]) lt.get(0);
        pgSimMxProp = (double[][]) lt.get(1);
    }

    /***************************************************************************
     * 以合并的语义子图为基础的相似度传播
     **************************************************************************/
    private void fullSimPropagation() {
        pgSimMxConcept = new double[sourceConceptNum][targetConceptNum];
        pgSimMxProp = new double[sourcePropNum][targetPropNum];
        ArrayList paraList = new ArrayList();
        ArrayList lt = new ArrayList();
        TextDocSim simHandle = new TextDocSim();

		/* 计算子图中其它元素的缺省相似度 */
        System.out.println("图中其它元素的缺省相似度");
        ArrayList OtSimList;
        if (sourceStmList.isEmpty() || targetStmList.isEmpty()) {
            OtSimList = new ArrayList();
        } else {
            OtSimList = simHandle.getOtTextSim(sourceFullOtTextDes, targetFullOtTextDes);
        }

		/* 源本体的语义文本描述 */
        // 构造输入参数链表
        packFullSimPgPara(paraList);
        paraList.add(27, OtSimList);

//		System.out.println("相似度传播");
        lt = new FullSimPropagation().ontSimPg(paraList);
        pgSimMxConcept = (double[][]) lt.get(0);
        pgSimMxProp = (double[][]) lt.get(1);

    }

    /***************************************************************************
     * 以合并的语义子图为基础的相似度传播
     **************************************************************************/
    private void inteSubSimPropagation() {
        pgSimMxConcept = new double[sourceConceptNum][targetConceptNum];
        pgSimMxProp = new double[sourcePropNum][targetPropNum];
        ArrayList paraList = new ArrayList();
        ArrayList lt = new ArrayList();
        TextDocSim simHandle = new TextDocSim();

		/* 计算子图中其它元素的缺省相似度 */
        System.out.println("图中其它元素的缺省相似度");
        ArrayList OtSimList;
        if (sourceStmList.isEmpty() || targetStmList.isEmpty()) {
            OtSimList = new ArrayList();
        } else {
            OtSimList = simHandle.getOtTextSim(sourceFullOtTextDes, targetFullOtTextDes);
        }

		/* 源本体的语义文本描述 */
        // 构造输入参数链表
        packInteSubSimPgPara(paraList);
        paraList.add(31, OtSimList);

//		System.out.println("相似度传播");
        lt = new InteSubSimPropagation().ontSimPg(paraList);
        pgSimMxConcept = (double[][]) lt.get(0);
        pgSimMxProp = (double[][]) lt.get(1);

    }

    /***************************************************************************
     * 以合并的概念和关系语义子图为基础的相似度传播
     **************************************************************************/
    private void inteTwoSubSimPropagation() {
        pgSimMxConcept = new double[sourceConceptNum][targetConceptNum];
        pgSimMxProp = new double[sourcePropNum][targetPropNum];
        ArrayList paraList = new ArrayList();
        ArrayList lt = new ArrayList();
        TextDocSim simHandle = new TextDocSim();

		/* 计算子图中其它元素的缺省相似度 */
        //System.out.println("图中其它元素的缺省相似度");
        ArrayList OtSimList;
        if (sourceStmList.isEmpty() || targetStmList.isEmpty()) {
            OtSimList = new ArrayList();
        } else {
            OtSimList = simHandle.getOtTextSim(sourceFullOtTextDes, targetFullOtTextDes);
        }

		/* 源本体的语义文本描述 */
        // 构造输入参数链表
        packInteTwoSubSimPgPara(paraList);
        paraList.add(31, OtSimList);

//		System.out.println("相似度传播");

        InteTwoSubSimPropagation pg = new InteTwoSubSimPropagation();
        pg.pbarValue = this.pbarValue;
        lt = pg.ontSimPg(paraList);

        pgSimMxConcept = (double[][]) lt.get(0);
        pgSimMxProp = (double[][]) lt.get(1);
    }

    /***************************************************************************
     * 以混合的概念和关系语义子图为基础的相似度传播
     **************************************************************************/
    private void hybridSubSimPropagation() {
        pgSimMxConcept = new double[sourceConceptNum][targetConceptNum];
        pgSimMxProp = new double[sourcePropNum][targetPropNum];
        ArrayList paraList = new ArrayList();
        ArrayList lt = new ArrayList();
        TextDocSim simHandle = new TextDocSim();

		/* 计算子图中其它元素的缺省相似度 */
//		System.out.println("图中其它元素的缺省相似度");
        ArrayList OtSimList;
        if (sourceStmList.isEmpty() || targetStmList.isEmpty()) {
            OtSimList = new ArrayList();
        } else {
            OtSimList = simHandle.getOtTextSim(sourceFullOtTextDes, targetFullOtTextDes);
        }

//		/*使用不丰富的子图，代价是需要重新计算*/
//		for (Iterator it=sourceNewStm.iterator();it.hasNext();){
//			Statement st=(Statement)it.next();
//			m_source.remove(st);
//		}
//		for (Iterator it=targetNewStm.iterator();it.hasNext();){
//			Statement st=(Statement)it.next();
//			m_target.remove(st);
//		}
//		reConsSemInf(false);
		
		/* 源本体的语义文本描述 */
        // 构造输入参数链表
        packInteTwoSubSimPgPara(paraList);
        paraList.add(31, OtSimList);
        // 已包含确信相似度的位置集合
        paraList.add(32, sourceCnptOkSimPos);
        paraList.add(33, targetCnptOkSimPos);
        paraList.add(34, sourcePropOkSimPos);
        paraList.add(35, targetPropOkSimPos);

//		System.out.println("相似度传播");
        HybridSubSimPropagation pg = new HybridSubSimPropagation();
        pg.pbarValue = this.pbarValue;
        lt = pg.ontSimPg(paraList);
        pgSimMxConcept = (double[][]) lt.get(0);
        pgSimMxProp = (double[][]) lt.get(1);
        return;
    }

    /***************************************************************************
     * 以子图为基础的相似度传播
     **************************************************************************/
    private void simPropagation() {

        //subSimPropagation();
        //cbSubSimPropagation();
        //InteSubSimPropagation();

        if (simProgType == 3) {
            hybridSubSimPropagation();
        } else {
            inteTwoSubSimPropagation();
        }

//		hybridSubSimPropagation();
//		fullSimPropagation();
    }

    /***************************************************************************
     * 重构源本体Informative graph
     * 重构目标本体Informative graph
     **************************************************************************/
    public void reConsSemInf(boolean enrich) {
        ArrayList paraList = new ArrayList();
        ArrayList subGList = new ArrayList();

        pbarValue[1] = 0; //进度条控制

		/* 首先重构源本体的语义子图 */
        // 构造输入参数链表
        packOntGraphPara(paraList, true);

        // 抽取源本体语义子图
//		System.out.println("  抽取源本体语义子图");
        OntGraph extract = new OntGraph();
        extract.usedEnrich = enrich;
        extract.pbarValue = pbarValue;
        extract.pbarCurrent = 0;
        extract.pbarTotal = sourceConceptNum + sourcePropNum + targetConceptNum + targetPropNum;
        subGList = extract.consInfSubOnt(paraList);
        sourceCnptSubG = (ConceptSubGraph[]) subGList.get(0);
        sourcePropSubG = (PropertySubGraph[]) subGList.get(1);
        m_source = (OntModel) subGList.get(2);
        sourceStmList = (ArrayList) subGList.get(3);
        sourceNewStm = extract.newOntStm;

        // 再重构源本体的语义子图

		/* 首先重构目标本体的语义子图 */
        // 构造输入参数链表
        packOntGraphPara(paraList, false);
        // 抽取目标本体语义子图
//		System.out.println("  抽取目标本体语义子图");
        extract = new OntGraph();
        extract.usedEnrich = enrich;
        extract.pbarValue = pbarValue;
        extract.pbarCurrent = sourceConceptNum + sourcePropNum;
        extract.pbarTotal = sourceConceptNum + sourcePropNum + targetConceptNum + targetPropNum;

        subGList = extract.consInfSubOnt(paraList);
        targetCnptSubG = (ConceptSubGraph[]) subGList.get(0);
        targetPropSubG = (PropertySubGraph[]) subGList.get(1);
        m_target = (OntModel) subGList.get(2);
        targetStmList = (ArrayList) subGList.get(3);
        targetNewStm = extract.newOntStm;

        pbarValue[1] = 100; //进度条控制
    }

    private double calcMeanValue(double[] numbers) {
        double sum = 0;
        for (int i = 0; i < numbers.length; ++i)
            sum += numbers[i];
        return sum / numbers.length;
    }

    private double calcStDev(double[] numbers) {
        double sum = 0, avg = calcMeanValue(numbers);
        for (int i = 0; i < numbers.length; ++i)
            sum += (numbers[i] - avg) * (numbers[i] - avg);
        sum /= numbers.length - 1;
        return Math.sqrt(sum);
    }

    private int countGreaterNum(double[] numbers, double num) {
        int sum = 0;
        for (int i = 0; i < numbers.length; ++i)
            if (numbers[i] >= num)
                ++sum;
        return sum;
    }

    private double findMax(double[] numbers) {
        double max = 0;
        for (int i = 0; i < numbers.length; ++i)
            if (numbers[i] > max)
                max = numbers[i];
        return max;
    }

    private double findMin(double[] numbers) {
        double min = 1;
        for (int i = 1; i < numbers.length; ++i)
            if (numbers[i] < min)
                min = numbers[i];
        return min;
    }

    /***************************************************************************
     * 基于文本的匹配
     * 1.构造Informative graph
     * 2.抽取文本
     * 3.计算文本相似度
     **************************************************************************/
    private void ontMatchText() {
        ArrayList paraList = new ArrayList();
        ArrayList lt = new ArrayList();

        pbarValue[2] = 0;

		/* 源本体的语义文本描述 */
        // 构造输入参数链表
        packOntDesPara(paraList, true);
//		System.out.println("  源本体的语义文本描述");
        lt = new OntDes().getOntDes(paraList);
        sourceCnptTextDes = (TextDes[]) lt.get(0);
        sourcePropTextDes = (TextDes[]) lt.get(1);
        sourceInsTextDes = (TextDes[]) lt.get(2);
        sourceCnptOtTextDes = (ArrayList[]) lt.get(3);
        sourcePropOtTextDes = (ArrayList[]) lt.get(4);
        sourceFullOtTextDes = (ArrayList) lt.get(5);

        pbarValue[2] = 10;

		/* 目标本体的语义文本描述 */
        // 构造输入参数链表
        packOntDesPara(paraList, false);
		/* 源本体的语义文本描述 */
//		System.out.println("  目标本体的语义文本描述");
        lt = new OntDes().getOntDes(paraList);
        targetCnptTextDes = (TextDes[]) lt.get(0);
        targetPropTextDes = (TextDes[]) lt.get(1);
        targetInsTextDes = (TextDes[]) lt.get(2);
        targetCnptOtTextDes = (ArrayList[]) lt.get(3);
        targetPropOtTextDes = (ArrayList[]) lt.get(4);
        targetFullOtTextDes = (ArrayList) lt.get(5);

        pbarValue[2] = 20;

        StrEDSim edsim = new StrEDSim();
        double[][] simCnptTxt = new double[sourceConceptNum][targetConceptNum];
        double[] maxSimCnptTxt_r = new double[sourceConceptNum];
        double[] maxSimCnptTxt_c = new double[targetConceptNum];
        for (int i = 0; i < sourceConceptNum; ++i)
            for (int j = 0; j < targetConceptNum; ++j) {
                simCnptTxt[i][j] = edsim.getNormEDSim(sourceCnptTextDes[i].name, targetCnptTextDes[j].name);
                maxSimCnptTxt_r[i] = Math.max(maxSimCnptTxt_r[i], simCnptTxt[i][j]);
                maxSimCnptTxt_c[j] = Math.max(maxSimCnptTxt_c[j], simCnptTxt[i][j]);
            }
        /*
        for (int i = 0; i < sourceConceptNum; ++i)
            System.out.print(maxSimCnptTxt_r[i] + "\t");
        System.out.println();
        for (int j = 0; j < targetConceptNum; ++j)
            System.out.print(maxSimCnptTxt_c[j] + "\t");
        System.out.println();
        */
        double avg, stDev;
        double[] chosenData;
        // 选数据量多的一组
        if (sourceConceptNum > targetConceptNum + 5) {
            avg = calcMeanValue(maxSimCnptTxt_r);
            stDev = calcStDev(maxSimCnptTxt_r);
            chosenData = maxSimCnptTxt_r;
        } else if (sourceConceptNum + 5 < targetConceptNum) {
            avg = calcMeanValue(maxSimCnptTxt_c);
            stDev = calcStDev(maxSimCnptTxt_c);
            chosenData = maxSimCnptTxt_c;
        } else {
            // 两组差不多一样多
            avg = (calcMeanValue(maxSimCnptTxt_r) * sourceConceptNum + calcMeanValue(maxSimCnptTxt_c) * targetConceptNum) / (sourceConceptNum + targetConceptNum);
            stDev = Math.max(calcStDev(maxSimCnptTxt_r), calcStDev(maxSimCnptTxt_c));
            chosenData = maxSimCnptTxt_r;
        }
        if (avg < 0.65 && avg > 0.45 && stDev < 0.25) {
            ParamStore.Concept_Similarity_Threshold = cnptSimThreshold = 0.22;
            ParamStore.Property_Similarity_Threshold = propSimThreshold = 0.55;
            //ParamStore.Concept_Similarity_Threshold = cnptSimThreshold = (double)countGreaterNum(chosenData, (findMax(chosenData) + findMax(chosenData)) / 2) / (double)chosenData.length; //0.22
            //ParamStore.Property_Similarity_Threshold = propSimThreshold = (double)countGreaterNum(chosenData, avg) / (double)chosenData.length; //0.55
            ParamStore.Propagation_Strategy = simProgType = 0;
            ParamStore.edThreshold = 0.9;
            //System.out.println("CONFERENCE dataset");
            //System.out.println("cnptSimThreshold = " + cnptSimThreshold);
            //System.out.println("propSimThreshold = " + propSimThreshold);
        } else {
            ParamStore.Concept_Similarity_Threshold = cnptSimThreshold = 0.05;
            ParamStore.Property_Similarity_Threshold = propSimThreshold = 0.05;
            ParamStore.Propagation_Strategy = simProgType = 3;
            ParamStore.edThreshold = 0.8;
        }
        /*
        conference: 选个数大的一组数据，平均值在0.45~0.65左右，标准差小于0.25，不要开相似度传播，两个阀值设高一点
        benchmark: 三种情况，可以开相似度传播
                            1）平均值很大(>0.95)，方差很小(<0.1)
                            2）平均值偏大，方差很大(>0.2)
                            3) 平均值很小(0.2~0.3)，方差很小(<0.1)
         */

		/* 语义匹配 */
        packTextDocSimPara(paraList);
        TextDocSim tdsim = new TextDocSim();
        tdsim.pbBarValue = this.pbarValue;
        lt = tdsim.getOntTextSim(paraList, DISTINCT_DP_OP);

        simMxConcept = new double[sourceConceptNum][targetConceptNum];
        simMxProp = new double[sourcePropNum][targetPropNum];
        simMxDataProp = new double[sourceDataPropNum][targetDataPropNum];
        simMxObjProp = new double[sourceObjPropNum][targetObjPropNum];
        simMxIns = new double[sourceInsNum][targetInsNum];

        simMxConcept = (double[][]) lt.get(0);
        simMxIns = (double[][]) lt.get(1);
        if (DISTINCT_DP_OP) {
            simMxDataProp = (double[][]) lt.get(2);
            simMxObjProp = (double[][]) lt.get(3);
        } else {
            simMxProp = (double[][]) lt.get(2);
        }

//		/* 相似矩阵的可视化 */
//		new SimDataVisual().visualize(simMxConcept, sourceConceptNum,targetConceptNum);
//		if (DISTINCT_DP_OP) {
//			new SimDataVisual().visualize(simMxDataProp, sourceDataPropNum,targetDataPropNum);
//			new SimDataVisual().visualize(simMxObjProp, sourceObjPropNum,targetObjPropNum);
//		} else {
//			new SimDataVisual().visualize(simMxProp, sourcePropNum,targetPropNum);
//		}
//		// /*强化相似矩阵实验*/
//		 Test ts=new Test();
//		 ts.enSimMatrix(simMxConcept,sourceConceptNum,targetConceptNum);
//		 ts.enSimMatrix(simMxProp,sourcePropNum,targetPropNum);
//		//		
//		// /*显示强化后的矩阵*/
//		 new
//		 SimDataVisual().visualize(simMxConcept,sourceConceptNum,targetConceptNum);
//		 if (DISTINCT_DP_OP){
//		 new
//		 SimDataVisual().visualize(simMxDataProp,sourceDataPropNum,targetDataPropNum);
//		 new
//		 SimDataVisual().visualize(simMxObjProp,sourceObjPropNum,targetObjPropNum);
//		 }
//		 else{
//		 new SimDataVisual().visualize(simMxProp,sourcePropNum,targetPropNum);
//		 }
        // 计算动态阀值
        //DynamicThreshold tdSelector = new DynamicThreshold();
		/* 简单估计方法 */
        // cnptSimThreshold=tdSelector.naiveThreshold(simMxConcept,sourceConceptNum,targetConceptNum);
        // if (DISTINCT_DP_OP){
        // dpSimThreshold=tdSelector.naiveThreshold(simMxDataProp,sourceDataPropNum,targetDataPropNum);
        // opSimThreshold=tdSelector.naiveThreshold(simMxObjProp,sourceObjPropNum,targetObjPropNum);
        // }
        // else{
        // propSimThreshold=tdSelector.naiveThreshold(simMxProp,sourcePropNum,targetPropNum);
        // }
		/* 最大熵方法 */
        // cnptSimThreshold=tdSelector.maxEntropyThreshold(simMxConcept,sourceConceptNum,targetConceptNum);
        // if (DISTINCT_DP_OP){
        // dpSimThreshold=tdSelector.maxEntropyThreshold(simMxDataProp,sourceDataPropNum,targetDataPropNum);
        // opSimThreshold=tdSelector.maxEntropyThreshold(simMxObjProp,sourceObjPropNum,targetObjPropNum);
        // }
        // else{
        // propSimThreshold=tdSelector.maxEntropyThreshold(simMxProp,sourcePropNum,targetPropNum);
        // }
		/* 利用最大熵方法获得阀值 */
//		cnptSimThreshold = tdSelector.maxEntropyThresholdA(simMxConcept,
//				sourceConceptNum, targetConceptNum);
//		if (DISTINCT_DP_OP) {
//			dpSimThreshold = tdSelector.maxEntropyThresholdA(simMxDataProp,
//					sourceDataPropNum, targetDataPropNum);
//			opSimThreshold = tdSelector.maxEntropyThresholdA(simMxObjProp,
//					sourceObjPropNum, targetObjPropNum);
//		} else {
//			propSimThreshold = tdSelector.maxEntropyThresholdA(simMxProp,
//					sourcePropNum, targetPropNum);
//		}

//		cnptSimThreshold=0.3;
//		propSimThreshold=0.3;
		
		/* 利用ostu方法获得阀值 */
        // cnptSimThreshold=tdSelector.ostuThreshold(simMxConcept,sourceConceptNum,targetConceptNum);
        // if (DISTINCT_DP_OP){
        // dpSimThreshold=tdSelector.ostuThreshold(simMxDataProp,sourceDataPropNum,targetDataPropNum);
        // opSimThreshold=tdSelector.ostuThreshold(simMxObjProp,sourceObjPropNum,targetObjPropNum);
        // }
        // else{
        // propSimThreshold=tdSelector.ostuThreshold(simMxProp,sourcePropNum,targetPropNum);
        // }
//		/* 利用mini error方法获得阀值 */
//		 cnptSimThreshold=tdSelector.miniErrorThreshold(simMxConcept,sourceConceptNum,targetConceptNum);
//		 if (DISTINCT_DP_OP){
//		 dpSimThreshold=tdSelector.miniErrorThreshold(simMxDataProp,sourceDataPropNum,targetDataPropNum);
//		 opSimThreshold=tdSelector.miniErrorThreshold(simMxObjProp,sourceObjPropNum,targetObjPropNum);
//		 }
//		 else{
//		 propSimThreshold=tdSelector.miniErrorThreshold(simMxProp,sourcePropNum,targetPropNum);
//		 }
		/* 利用max correlation方法获得阀值 */
        // cnptSimThreshold=tdSelector.maxCorrelationThreshold(simMxConcept,sourceConceptNum,targetConceptNum);
        // if (DISTINCT_DP_OP){
        // dpSimThreshold=tdSelector.maxCorrelationThreshold(simMxDataProp,sourceDataPropNum,targetDataPropNum);
        // opSimThreshold=tdSelector.maxCorrelationThreshold(simMxObjProp,sourceObjPropNum,targetObjPropNum);
        // }
        // else{
        // propSimThreshold=tdSelector.maxCorrelationThreshold(simMxProp,sourcePropNum,targetPropNum);
        // }
		/* 利用WP方法获得阀值 */
        // cnptSimThreshold=tdSelector.maxWPThreshold(simMxConcept,sourceConceptNum,targetConceptNum);
        // if (DISTINCT_DP_OP){
        // dpSimThreshold=tdSelector.maxWPThreshold(simMxDataProp,sourceDataPropNum,targetDataPropNum);
        // opSimThreshold=tdSelector.maxWPThreshold(simMxObjProp,sourceObjPropNum,targetObjPropNum);
        // }
        // else{
        // propSimThreshold=tdSelector.maxWPThreshold(simMxProp,sourcePropNum,targetPropNum);
        // }
//		System.out.println("cnptSimThreshold"+cnptSimThreshold);
//		System.out.println("propSimThreshold"+propSimThreshold);
        for (int i = 0; i < sourcePropNum; i++)
            for (int j = 0; j < targetPropNum; j++) {
                if (simMxProp[i][j] < propSimThreshold) simMxProp[i][j] = 0;
            }

        //new SimDataVisual().visualize(simMxProp, sourcePropNum,targetPropNum);


        // 结果过滤，用简单的方法
//		simMxConcept = new SimpleFilter().maxValueFilter(sourceConceptNum,
//				targetConceptNum, simMxConcept, cnptSimThreshold);
//		if (DISTINCT_DP_OP) {
//			simMxDataProp = new SimpleFilter().maxValueFilter(
//					sourceDataPropNum, targetDataPropNum, simMxDataProp,
//					dpSimThreshold);
//			simMxObjProp = new SimpleFilter().maxValueFilter(sourceObjPropNum,
//					targetObjPropNum, simMxObjProp, opSimThreshold);
//		} else {
//			simMxProp = new SimpleFilter().maxValueFilter(sourcePropNum,
//					targetPropNum, simMxProp, propSimThreshold);
//		}

		/* 处理实例相似矩阵 */
        simMxIns = new SimpleFilter().maxValueFilter(sourceInsNum, targetInsNum, simMxIns, 0.3);

        //结果过滤，用稳定婚姻的方法
        simMxConcept = new
                StableMarriageFilter().run(simMxConcept, sourceConceptNum, targetConceptNum);
        simMxIns = new
                StableMarriageFilter().run(simMxIns, sourceInsNum, targetInsNum);
        simMxProp = new
                StableMarriageFilter().run(simMxProp, sourcePropNum, targetPropNum);

        // simMxIns = new
        // StableMarriageFilter().run(simMxIns,sourceInsNum,targetInsNum);

        for (int i = 0; i < sourceInsNum; i++) {
            for (int j = 0; j < targetInsNum; j++) {
                if (simMxIns[i][j] > 0.0001) {
                    //System.out.println(sourceInsName[i]+"-->"+targetInsName[j]+"="+simMxIns[i][j]); //debug
                }
            }
        }

		/* 记录可信结果的位置 */
		/* 同时判断是否组要相似度传播 */
        int gotSim = 0;// 实际得到的映射数目
        int theorySim = Math.min(sourceConceptNum, targetConceptNum)
                + Math.min(sourcePropNum, targetPropNum);// 理论上的映射数目
        int gotSim_concept = 0, gotSim_prop = 0;
        int theorySim_concept = Math.min(sourceConceptNum, targetConceptNum);
        int theorySim_prop = Math.min(sourcePropNum, targetPropNum);
        sourceCnptOkSimPos = new HashSet();
        targetCnptOkSimPos = new HashSet();
        for (int i = 0; i < sourceConceptNum; i++) {
            for (int j = 0; j < targetConceptNum; j++) {
                if (simMxConcept[i][j] > cnptSimThreshold) {
                    gotSim++;
                    ++gotSim_concept;
                }
                if (simMxConcept[i][j] > OKSIM) {
                    sourceCnptOkSimPos.add(i);
                    targetCnptOkSimPos.add(j);
                }
            }
        }
        sourcePropOkSimPos = new HashSet();
        targetPropOkSimPos = new HashSet();
        for (int i = 0; i < sourcePropNum; i++) {
            for (int j = 0; j < targetPropNum; j++) {
                if (simMxProp[i][j] > propSimThreshold) {
                    gotSim++;
                    ++gotSim_prop;
                }
                if (simMxProp[i][j] > OKSIM) {
                    sourcePropOkSimPos.add(i);
                    targetPropOkSimPos.add(j);
                }
            }
        }
        //System.out.println(String.format("gotSim_concept / theorySim_concept = %f", (double) gotSim_concept / (double) theorySim_concept));
        //System.out.println(String.format("gotSim_prop / theorySim_prop = %f", (double) gotSim_prop / (double) theorySim_prop));
        //System.out.println(String.format("gotSim / theorySim = %f", (double) gotSim / (double) theorySim));
        if (simProgType == 1) {
            if ((double) gotSim / (double) theorySim < 0.7 && (double) gotSim / (double) theorySim > 0.3) {
                // 需要相似度传播
                isNeedSimProg = true;
            }
        } else if (simProgType == 0) {
            isNeedSimProg = false;
        } else {
            isNeedSimProg = true;
        }

        // /*相似矩阵的可视化代码*/
        // new
        // SimDataVisual().visualize(simMxConcept,sourceConceptNum,targetConceptNum);
        // if (DISTINCT_DP_OP){
        // new
        // SimDataVisual().visualize(simMxDataProp,sourceDataPropNum,targetDataPropNum);
        // new
        // SimDataVisual().visualize(simMxObjProp,sourceObjPropNum,targetObjPropNum);
        // }
        // else{
        // new SimDataVisual().visualize(simMxProp,sourcePropNum,targetPropNum);
        // }

		/*
		 * 以过虑后的相似矩阵为输入， 利用最大熵方法获得阀值
		 */
//		 cnptSimThreshold=tdSelector.maxEntropyThresholdA(simMxConcept,sourceConceptNum,targetConceptNum);
//		 if (DISTINCT_DP_OP){
//		 dpSimThreshold=tdSelector.maxEntropyThresholdA(simMxDataProp,sourceDataPropNum,targetDataPropNum);
//		 opSimThreshold=tdSelector.maxEntropyThresholdA(simMxObjProp,sourceObjPropNum,targetObjPropNum);
//		 }
//		 else{
//		 propSimThreshold=tdSelector.maxEntropyThresholdA(simMxProp,sourcePropNum,targetPropNum);
//		 }
		/* 利用ostu方法获得阀值 */
        // cnptSimThreshold=tdSelector.ostuThreshold(simMxConcept,sourceConceptNum,targetConceptNum);
        // if (DISTINCT_DP_OP){
        // dpSimThreshold=tdSelector.ostuThreshold(simMxDataProp,sourceDataPropNum,targetDataPropNum);
        // opSimThreshold=tdSelector.ostuThreshold(simMxObjProp,sourceObjPropNum,targetObjPropNum);
        // }
        // else{
        // propSimThreshold=tdSelector.ostuThreshold(simMxProp,sourcePropNum,targetPropNum);
        // }
		/* 利用mini error方法获得阀值 */
//		 cnptSimThreshold=tdSelector.miniErrorThreshold(simMxConcept,sourceConceptNum,targetConceptNum);
//		 if (DISTINCT_DP_OP){
//		 dpSimThreshold=tdSelector.miniErrorThreshold(simMxDataProp,sourceDataPropNum,targetDataPropNum);
//		 opSimThreshold=tdSelector.miniErrorThreshold(simMxObjProp,sourceObjPropNum,targetObjPropNum);
//		 }
//		 else{
//		 propSimThreshold=tdSelector.miniErrorThreshold(simMxProp,sourcePropNum,targetPropNum);
//		 }
//		 System.out.println("cnptSimThreshold" + cnptSimThreshold);
//		 System.out.println("propSimThreshold"+propSimThreshold);
//		 /* 利用max correlation方法获得阀值*/
//		 cnptSimThreshold=tdSelector.maxCorrelationThreshold(simMxConcept,sourceConceptNum,targetConceptNum);
//		 if (DISTINCT_DP_OP){
//		 dpSimThreshold=tdSelector.maxCorrelationThreshold(simMxDataProp,sourceDataPropNum,targetDataPropNum);
//		 opSimThreshold=tdSelector.maxCorrelationThreshold(simMxObjProp,sourceObjPropNum,targetObjPropNum);
//		 }
//		 else{
//		 propSimThreshold=tdSelector.maxCorrelationThreshold(simMxProp,sourcePropNum,targetPropNum);
//		 }
		/* 综合过滤前后的阀值 */
        // cnptSimThreshold=(cnptSimThreshold+tdSelector.maxEntropyThresholdA(simMxConcept,sourceConceptNum,targetConceptNum))/2.0;
        // if (DISTINCT_DP_OP){
        // dpSimThreshold=(dpSimThreshold+tdSelector.maxEntropyThresholdA(simMxDataProp,sourceDataPropNum,targetDataPropNum))/2.0;
        // opSimThreshold=(opSimThreshold+tdSelector.maxEntropyThresholdA(simMxObjProp,sourceObjPropNum,targetObjPropNum))/2.0;
        // }
        // else{
        // propSimThreshold=(propSimThreshold+tdSelector.maxEntropyThresholdA(simMxProp,sourcePropNum,targetPropNum))/2.0;
        // }
		/* 利用WP方法获得阀值 */
        // cnptSimThreshold=tdSelector.maxWPThreshold(simMxConcept,sourceConceptNum,targetConceptNum);
        // if (DISTINCT_DP_OP){
        // dpSimThreshold=tdSelector.maxWPThreshold(simMxDataProp,sourceDataPropNum,targetDataPropNum);
        // opSimThreshold=tdSelector.maxWPThreshold(simMxObjProp,sourceObjPropNum,targetObjPropNum);
        // }
        // else{
        // propSimThreshold=tdSelector.maxWPThreshold(simMxProp,sourcePropNum,targetPropNum);
        // }
//		 System.out.println("cnptSimThreshold" + cnptSimThreshold);
//		 System.out.println("propSimThreshold"+propSimThreshold);
        // cnptSimThreshold=0.32;
        // propSimThreshold=0.20;

    }

    /***************************************************************************
     * 基于结构的匹配
     * 1.构造Informative graph
     * 2.抽取结构
     * 3.计算结构相似度
     **************************************************************************/
    private void ontMatchStru() {

        // 文本映射计算
        // ComputeLiteralMapping();

        // 结构映射计算
        // ----------Structure Methods Testing-------------------
        // OntGraph OntG = new OntGraph();
        // OntG.SetConceptPara(sourceConceptNum, targetConceptNum,
        // sourceConceptName, targetConceptName);
        // OntG.SetPropertyPara(sourcePropNum, targetPropNum, sourcePropName,
        // targetPropName);
        // OntG.SetInstancePara(sourceInsNum, targetInsNum, sourceInsName,
        // targetInsName);
        // -------------------------------------------------------

        // 转换为Bipartite Graph的匹配算法
        // myOntoGraph.source_Graph = myOntoGraph.Onto2BiptGraph(m_source,
        // true);
        // myOntoGraph.target_Graph = myOntoGraph.Onto2BiptGraph(m_target,
        // false);
        // myOntoGraph.ComputeSGMapping_BiptGraph();

        // Informative Graph的匹配算法
        // OntG.source_Graph = OntG.Onto2Graph(m_source, true);
        // OntG.target_Graph = OntG.Onto2Graph(m_target, false);
        // OntG.ConsSGInformative(OntG.source_Graph,true);
        // OntG.ConsSGInformative(OntG.target_Graph,false);
        // OntG.ComputeSGMapping_Informative(OntG.sourceSubGraph,
        // OntG.targetSubGraph);

        // 未优化的全图匹配算法
        // myOntoGraph.source_Graph = myOntoGraph.Onto2Graph(m_source, true);
        // myOntoGraph.target_Graph = myOntoGraph.Onto2Graph(m_target, false);
        // myOntoGraph.ComputeSGMapping_WholeGraph();

        // 简单分块的全图匹配算法
        // myOntoGraph.source_Graph = myOntoGraph.Onto2Graph(m_source, true);
        // myOntoGraph.target_Graph = myOntoGraph.Onto2Graph(m_target, false);
        // myOntoGraph.ComputeSGMapping_WholeGraph_Block();

        // myOntoGraph.ConsSGNeighbor(myOntoGraph.source_Graph,myOntoGraph.target_Graph,2);
        // myOntoGraph.ComputeSGMapping_Neighbor(myOntoGraph.sourceSubGraph,
        // myOntoGraph.targetSubGraph);

        // --------------------------------------------------------

        // CombineMultiMappingResults();
    }

    /***************************************************************************
     * 文本相似和结构相似结果合并
     **************************************************************************/
    private void combineResult() {
		/* 合并子图描述方法的相似度和相似度传播后的相似度结果 */
		/* 方法1.最简单的合并:取均值 */
        // for (int i=0;i<sourceConceptNum;i++){
        // for (int j=0;j<targetConceptNum;j++){
        // simMxConcept[i][j]=(simMxConcept[i][j]+pgSimMxConcept[i][j])/2.0;
        // }
        // }
        // for (int i=0;i<sourcePropNum;i++){
        // for (int j=0;j<targetPropNum;j++){
        // simMxProp[i][j]=(simMxProp[i][j]+pgSimMxProp[i][j])/2.0;
        // }
        // }
		/* 方法2.只考虑增加的新匹配 */

		/* 方法3.综合考虑的合并方法 */
		/* 处理概念 */
        // 1.如果Aij>=t，确定Aij,同时修改对应的传播矩阵Bij
        double cget1 = 0;
        double cget2 = 0;
        double pget1 = 0;
        double pget2 = 0;
        for (int i = 0; i < sourceConceptNum; i++) {
            for (int j = 0; j < targetConceptNum; j++) {
                if (simMxConcept[i][j] >= OKSIM) {
                    // B的i行
                    for (int k = 0; k < targetConceptNum; k++) {
                        if (k != j) {
                            pgSimMxConcept[i][k] = 0;
                        }
                    }
                    // B的j列
                    for (int k = 0; k < sourceConceptNum; k++) {
                        if (k != i) {
                            pgSimMxConcept[k][j] = 0;
                        }
                    }
                }
            }
        }
        // 2.如果Aij<t，不能确定Aij,考察传播矩阵Bij
        // 2.1Bij<t，Bij肯定是噪声
        for (int i = 0; i < sourceConceptNum; i++) {
            for (int j = 0; j < targetConceptNum; j++) {
                if (pgSimMxConcept[i][j] > 0 && pgSimMxConcept[i][j] < BADSIM) {
                    pgSimMxConcept[i][j] = 0;
                }
                if (simMxConcept[i][j] > 0.0001) {
                    cget1 = cget1 + 1.0;
                }
                if (pgSimMxConcept[i][j] > 0.0001) {
                    cget2 = cget2 + 1.0;
                }
            }
        }

        // 合并处理结果
        Double fc1 = cget1 / (cget1 + cget2);
        Double fc2 = cget2 / (cget1 + cget2);
        if (!fc1.isInfinite() && !fc1.isNaN()) {
            for (int i = 0; i < sourceConceptNum; i++) {
                for (int j = 0; j < targetConceptNum; j++) {
                    if (simMxConcept[i][j] < OKSIM) {
//					simMxConcept[i][j] = (simMxConcept[i][j] + pgSimMxConcept[i][j]) / 2.0;
                        simMxConcept[i][j] = simMxConcept[i][j] * fc1 + pgSimMxConcept[i][j] * fc2;
                        //simMxConcept[i][j] =pgSimMxConcept[i][j];
                    }
                }
            }
            //System.out.println("fc1:"+fc1+",fc2:"+fc2);
        }

		/* 处理属性 */
        // 1.如果Aij>=t，确定Aij,同时修改对应的传播矩阵Bij
        for (int i = 0; i < sourcePropNum; i++) {
            for (int j = 0; j < targetPropNum; j++) {
                if (simMxProp[i][j] >= OKSIM) {
                    // B的i行
                    for (int k = 0; k < targetPropNum; k++) {
                        if (k != j) {
                            pgSimMxProp[i][k] = 0;
                        }
                    }
                    // B的j列
                    for (int k = 0; k < sourcePropNum; k++) {
                        if (k != i) {
                            pgSimMxProp[k][j] = 0;
                        }
                    }
                }
            }
        }
        // 2.如果Aij<t，不能确定Aij,考察传播矩阵Bij
        // 2.1Bij<t，Bij肯定是噪声
        for (int i = 0; i < sourcePropNum; i++) {
            for (int j = 0; j < targetPropNum; j++) {
                if (pgSimMxProp[i][j] > 0 && pgSimMxProp[i][j] < BADSIM) {
                    pgSimMxProp[i][j] = 0;
                }
                if (simMxProp[i][j] > 0.0001) {
                    pget1 = pget1 + 1.0;
                }
                if (pgSimMxProp[i][j] > 0.0001) {
                    pget2 = pget2 + 1.0;
                }
            }
        }

        // 合并处理结果
        Double fp1 = pget1 / (pget1 + pget2);
        Double fp2 = pget2 / (pget1 + pget2);
        if (!fp1.isNaN() && !fp1.isInfinite()) {
            for (int i = 0; i < sourcePropNum; i++) {
                for (int j = 0; j < targetPropNum; j++) {
                    if (simMxProp[i][j] < OKSIM) {
//					simMxProp[i][j] = (simMxProp[i][j] + pgSimMxProp[i][j]) / 2.0;
                        simMxProp[i][j] = simMxProp[i][j] * fp1 + pgSimMxProp[i][j] * fp2;
                        //simMxProp[i][j] = pgSimMxProp[i][j];
                    }
                }
            }
            //System.out.println("fp1:"+fp1+",fp2:"+fp2);
        }
        /**合并后的相似矩阵可视化**/
//		new SimDataVisual().visualize(simMxConcept, sourceConceptNum,targetConceptNum);
//		if (DISTINCT_DP_OP) {
//			new SimDataVisual().visualize(simMxDataProp, sourceDataPropNum,targetDataPropNum);
//			new SimDataVisual().visualize(simMxObjProp, sourceObjPropNum,targetObjPropNum);
//		} else {
//			new SimDataVisual().visualize(simMxProp, sourcePropNum,targetPropNum);
//		}

        // 结果过滤，用稳定婚姻的方法
        try {
            simMxConcept = new StableMarriageFilter().run(simMxConcept, sourceConceptNum, targetConceptNum);
            simMxProp = new StableMarriageFilter().run(simMxProp, sourcePropNum, targetPropNum);
        } catch (NoSuchElementException e) {
            simMxConcept = new SimpleFilter().maxValueFilter(sourceConceptNum, targetConceptNum, simMxConcept, cnptSimThreshold);
            simMxProp = new SimpleFilter().maxValueFilter(sourcePropNum, targetPropNum, simMxProp, propSimThreshold);
        }


        // 重新计算动态阀值
//		DynamicThreshold tdSelector = new DynamicThreshold();
//		/* 利用最大熵方法获得阀值 */
//		cnptSimThreshold = tdSelector.maxEntropyThresholdA(simMxConcept,
//				sourceConceptNum, targetConceptNum);
//		if (DISTINCT_DP_OP) {
//			dpSimThreshold = tdSelector.maxEntropyThresholdA(simMxDataProp,
//					sourceDataPropNum, targetDataPropNum);
//			opSimThreshold = tdSelector.maxEntropyThresholdA(simMxObjProp,
//					sourceObjPropNum, targetObjPropNum);
//		} else {
//			propSimThreshold = tdSelector.maxEntropyThresholdA(simMxProp,
//					sourcePropNum, targetPropNum);
//		}

//		System.out.println("合并结果后的新阀值：ct="+cnptSimThreshold+"pt="+propSimThreshold);

//		cnptSimThreshold = 0.008;
//		propSimThreshold = 0.008;

//		// 结果过滤，用简单的方法
//		simMxConcept = new SimpleFilter().maxValueFilter(sourceConceptNum,
//				targetConceptNum, simMxConcept, cnptSimThreshold);
//		if (DISTINCT_DP_OP) {
//			simMxDataProp = new SimpleFilter().maxValueFilter(
//					sourceDataPropNum, targetDataPropNum, simMxDataProp,
//					dpSimThreshold);
//			simMxObjProp = new SimpleFilter().maxValueFilter(sourceObjPropNum,
//					targetObjPropNum, simMxObjProp, opSimThreshold);
//		} else {
//			simMxProp = new SimpleFilter().maxValueFilter(sourcePropNum,
//					targetPropNum, simMxProp, propSimThreshold);
//		}

    }

    private void showResult(boolean flag) {
        // 根据相似矩阵，生成映射结果
        mappingResult = new MapRecord[Math.max(sourceConceptNum, targetConceptNum)
                + Math.max(sourceDataPropNum, targetDataPropNum)
                + Math.max(sourceObjPropNum, targetObjPropNum)];
        if (DISTINCT_DP_OP) {
            generateMapping(simMxConcept, simMxDataProp, simMxObjProp);
        } else {
            generateMapping(simMxConcept, simMxProp);
        }

        // 显示结果
        if (!flag)
            return;
        for (int i = 0; i < mappingNum; i++) {
            mappingResult[i].show();
        }
    }

    /***************************************************************************
     * 将结果以两种方式写入文件：
     * 1.普通文本文件
     * 2.xml文件
     * 目标文件夹是target本体的目录
     **************************************************************************/
    private void saveResult() {
        MappingFile mapFile = new MappingFile();
        if (lilyFileName.length() == 0) {
            lilyFileName = "lily";
        }
        // 普通文本文件的方式
        //mapFile.save2txt(sourceOntFile, targetOntFile, mappingNum, mappingResult, lilyFileName);
        // XML文件的方式
        mapFile.setBaseURI(sourceBaseURI, targetBaseURI);
        mapFile.save2rdf(sourceOntFile, targetOntFile, mappingNum,
                mappingResult, lilyFileName);
    }

    /***************************************************************************
     * 评估映射结果：
     * 输入映射结果给评估类
     * 通过和基准结果比较得到结果
     **************************************************************************/
//	public void evaluate() {
//		ArrayList list = new ArrayList();
//		MapRecord[] refMapResult = null;
//		int refMapNum = 0;
//		boolean hasRefFile=false;
//		if (refalignFile.length() == 0) {
//			return;
//		}
//		// 读出标准结果
//		try {
//			list = new MappingFile().read4xml(refalignFile);
//			refMapNum = ((Integer) list.get(0)).intValue();
//			refMapResult = new MapRecord[refMapNum];
//			refMapResult = (MapRecord[]) ((ArrayList) list.get(1)).toArray(new MapRecord[0]);
//			hasRefFile = true;
//		} catch (MalformedURLException e) {
//			System.out.println("Can't open refalign result file!"+ e.toString());
//		} catch (DocumentException e) {
//			System.out.println("Can't open refalign result file!"+ e.toString());
//		}
//		// 输入给评估类
//		if (hasRefFile){
//			list = new EvaluateMapping().getEvaluation(refMapNum, refMapResult,	mappingNum, mappingResult);
//			precision = ((Double) list.get(0)).doubleValue();
//			recall = ((Double) list.get(1)).doubleValue();
//			f1Measure = ((Double) list.get(2)).doubleValue();
//			if (Math.abs(precision)<0.0001 && recall>0.9999) {recall = 0.0;}
//		}
//		else {
//			precision = -1.0;
//			recall = -1.0;
//			f1Measure = -1.0;
//		}
//
//	}

    /***************************************************************************
     * 映射结果生成：
     * 由于这里并不一定只限制1-1的映射，所以只要相似矩阵中的元素不为0， 就认为是符合要求的映射
     **************************************************************************/
    public void generateMapping(double[][] simMxC, double[][] simMxDP,
                                double[][] simMxOp) {
        int cMappingNum, dpMappingNum, opMappingNum;// 分别记录几种映射的数目

        mappingNum = 0;
        // 概念映射结果
        cMappingNum = 0;
        for (int i = 0; i < sourceConceptNum; i++)
            for (int j = 0; j < targetConceptNum; j++) {
                if (simMxC[i][j] > cnptSimThreshold) {
                    mappingResult[mappingNum] = new MapRecord();
                    mappingResult[mappingNum].sourceLabel = new String(
                            sourceConceptName[i]);
                    mappingResult[mappingNum].targetLabel = new String(
                            targetConceptName[j]);
                    mappingResult[mappingNum].similarity = simMxC[i][j];
                    mappingResult[mappingNum].relationType = EQUALITY;
                    mappingNum++;
                    cMappingNum++;
                }
            }
//		System.out.println("cMappingNum:" + cMappingNum);
        // DatatypeProperty映射结果
        dpMappingNum = 0;
        for (int i = 0; i < sourceDataPropNum; i++)
            for (int j = 0; j < targetDataPropNum; j++) {
                if (simMxDP[i][j] > dpSimThreshold) {
                    mappingResult[mappingNum] = new MapRecord();
                    mappingResult[mappingNum].sourceLabel = new String(
                            sourceDataPropName[i]);
                    mappingResult[mappingNum].targetLabel = new String(
                            targetDataPropName[j]);
                    mappingResult[mappingNum].similarity = simMxDP[i][j];
                    mappingResult[mappingNum].relationType = EQUALITY;
                    mappingNum++;
                    dpMappingNum++;
                }
            }
//		System.out.println("dpMappingNum:" + dpMappingNum);
        // ObjectProperty映射结果
        opMappingNum = 0;
        for (int i = 0; i < sourceObjPropNum; i++)
            for (int j = 0; j < targetObjPropNum; j++) {
                if (simMxOp[i][j] > opSimThreshold) {
                    mappingResult[mappingNum] = new MapRecord();
                    mappingResult[mappingNum].sourceLabel = new String(
                            sourceObjPropName[i]);
                    mappingResult[mappingNum].targetLabel = new String(
                            targetObjPropName[j]);
                    mappingResult[mappingNum].similarity = simMxOp[i][j];
                    mappingResult[mappingNum].relationType = EQUALITY;
                    mappingNum++;
                    opMappingNum++;
                }
            }
//		System.out.println("opMappingNum:" + opMappingNum);
    }

    public void generateMapping(double[][] simMxC, double[][] simMxP) {
        mappingNum = 0;
        // 概念映射结果
        cMappingNum = 0;
        for (int i = 0; i < sourceConceptNum; i++)
            for (int j = 0; j < targetConceptNum; j++) {
                //DecimalFormat df = new DecimalFormat("0.0000");
                //simMxC[i][j] = Double.parseDouble(df.format(simMxC[i][j]));
                simMxC[i][j] = Math.round(simMxC[i][j] * 10000) / 10000.0;

                if (simMxC[i][j] > cnptSimThreshold) {
                    //System.out.println("value of mappingNum in C is:" + mappingNum); //debug
                    //System.out.println("length of mappingResult in C is:" + mappingResult.length); //debug
                    mappingResult[mappingNum] = new MapRecord();
                    mappingResult[mappingNum].sourceLabel = new String(
                            sourceConceptName[i]);
                    mappingResult[mappingNum].targetLabel = new String(
                            targetConceptName[j]);
                    mappingResult[mappingNum].similarity = simMxC[i][j];
                    mappingResult[mappingNum].relationType = EQUALITY;
                    mappingNum++;
                    cMappingNum++;
                }
            }
//		System.out.println("cMappingNum:" + cMappingNum);
        // Property映射结果
        pMappingNum = 0;
        for (int i = 0; i < sourcePropNum; i++)
            for (int j = 0; j < targetPropNum; j++) {
                //DecimalFormat df = new DecimalFormat("0.0000");
                //simMxP[i][j] = Double.parseDouble(df.format(simMxP[i][j]));
                simMxP[i][j] = Math.round(simMxP[i][j] * 10000) / 10000.0;

                if (simMxP[i][j] > propSimThreshold) {
                    //System.out.println("value of mappingNum in P is :" + mappingNum); //debug
                    //System.out.println("length of mappingResult in P is :" + mappingResult.length); //debug
                    mappingResult[mappingNum] = new MapRecord();
                    mappingResult[mappingNum].sourceLabel = new String(
                            sourcePropName[i]);
                    mappingResult[mappingNum].targetLabel = new String(
                            targetPropName[j]);
                    mappingResult[mappingNum].similarity = simMxP[i][j];
                    mappingResult[mappingNum].relationType = EQUALITY;
                    mappingNum++;
                    pMappingNum++;
                }
            }
//		System.out.println("pMappingNum:" + pMappingNum);
    }

    /***************************************************************************
     * 将本体参数打包
     **************************************************************************/
    @SuppressWarnings("unchecked")
    private void packOntGraphPara(ArrayList list, boolean flag) {
        if (flag) {
            // 源本体
            list.add(0, m_source);
            list.add(1, sourceConceptNum);
            list.add(2, sourcePropNum);
            list.add(3, sourceDataPropNum);
            list.add(4, sourceObjPropNum);
            list.add(5, sourceInsNum);
            list.add(6, sourceConceptName);
            list.add(7, sourcePropName);
            list.add(8, sourceDataPropName);
            list.add(9, sourceObjPropName);
            list.add(10, sourceInsName);
            list.add(11, sourceBaseURI);

            list.add(12, sourceFullConceptNum);
            list.add(13, sourceFullPropNum);
            list.add(14, sourceFullDataPropNum);
            list.add(15, sourceFullObjPropNum);
            list.add(16, sourceFullInsNum);
            list.add(17, sourceFullConceptName);
            list.add(18, sourceFullPropName);
            list.add(19, sourceFullDataPropName);
            list.add(20, sourceFullObjPropName);
            list.add(21, sourceFullInsName);

            list.add(22, sourceAnonCnpt);
            list.add(23, sourceAnonProp);
            list.add(24, sourceAnonIns);
        } else {
            // 目标本体
            list.add(0, m_target);
            list.add(1, targetConceptNum);
            list.add(2, targetPropNum);
            list.add(3, targetDataPropNum);
            list.add(4, targetObjPropNum);
            list.add(5, targetInsNum);
            list.add(6, targetConceptName);
            list.add(7, targetPropName);
            list.add(8, targetDataPropName);
            list.add(9, targetObjPropName);
            list.add(10, targetInsName);
            list.add(11, targetBaseURI);

            list.add(12, targetFullConceptNum);
            list.add(13, targetFullPropNum);
            list.add(14, targetFullDataPropNum);
            list.add(15, targetFullObjPropNum);
            list.add(16, targetFullInsNum);
            list.add(17, targetFullConceptName);
            list.add(18, targetFullPropName);
            list.add(19, targetFullDataPropName);
            list.add(20, targetFullObjPropName);
            list.add(21, targetFullInsName);

            list.add(22, targetAnonCnpt);
            list.add(23, targetAnonProp);
            list.add(24, targetAnonIns);
        }
    }

    /***************************************************************************
     * 将本体参数打包
     **************************************************************************/
    @SuppressWarnings("unchecked")
    private void packOntDesPara(ArrayList list, boolean flag) {
        if (flag) {
            // 源本体
            list.add(0, m_source);
            list.add(1, sourceConceptNum);
            list.add(2, sourcePropNum);
            list.add(3, sourceInsNum);

            list.add(4, sourceConceptName);
            list.add(5, sourcePropName);

            list.add(6, sourceBaseURI);

            list.add(7, sourceFullConceptNum);
            list.add(8, sourceFullPropNum);
            list.add(9, sourceFullInsNum);

            list.add(10, sourceFullConceptName);
            list.add(11, sourceFullPropName);
            list.add(12, sourceFullInsName);

            list.add(13, sourceCnptSubG);
            list.add(14, sourcePropSubG);

            list.add(15, sourceAnonCnpt);
            list.add(16, sourceAnonProp);
            list.add(17, sourceAnonIns);

            list.add(18, sourceInsName);

            list.add(19, sourceStmList);
            list.add(20, isSubProg);
        } else {
            // 目标本体
            list.add(0, m_target);
            list.add(1, targetConceptNum);
            list.add(2, targetPropNum);
            list.add(3, targetInsNum);

            list.add(4, targetConceptName);
            list.add(5, targetPropName);

            list.add(6, targetBaseURI);

            list.add(7, targetFullConceptNum);
            list.add(8, targetFullPropNum);
            list.add(9, targetFullInsNum);

            list.add(10, targetFullConceptName);
            list.add(11, targetFullPropName);
            list.add(12, targetFullInsName);

            list.add(13, targetCnptSubG);
            list.add(14, targetPropSubG);

            list.add(15, targetAnonCnpt);
            list.add(16, targetAnonProp);
            list.add(17, targetAnonIns);

            list.add(18, targetInsName);

            list.add(19, targetStmList);
            list.add(20, isSubProg);
        }
    }

    /***************************************************************************
     * 将本体参数打包
     **************************************************************************/
    @SuppressWarnings("unchecked")
    private void packTextDocSimPara(ArrayList list) {
        // 源本体
        list.add(0, sourceConceptNum);
        list.add(1, sourcePropNum);
        list.add(2, sourceDataPropNum);
        list.add(3, sourceObjPropNum);

        list.add(4, sourceConceptName);
        list.add(5, sourcePropName);
        list.add(6, sourceDataPropName);
        list.add(7, sourceObjPropName);

        list.add(8, sourceCnptTextDes);
        list.add(9, sourcePropTextDes);

        // 目标本体
        list.add(10, targetConceptNum);
        list.add(11, targetPropNum);
        list.add(12, targetDataPropNum);
        list.add(13, targetObjPropNum);

        list.add(14, targetConceptName);
        list.add(15, targetPropName);
        list.add(16, targetDataPropName);
        list.add(17, targetObjPropName);

        list.add(18, targetCnptTextDes);
        list.add(19, targetPropTextDes);

        // 实例信息
        list.add(20, sourceInsNum);
        list.add(21, sourceInsName);
        list.add(22, sourceInsTextDes);
        list.add(23, targetInsNum);
        list.add(24, targetInsName);
        list.add(25, targetInsTextDes);
    }

    /***************************************************************************
     * 将本体参数打包
     **************************************************************************/
    @SuppressWarnings("unchecked")
    private void packSubSimPgPara(ArrayList list) {
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
        list.add(8, sourceCnptSubG);
        list.add(9, sourcePropSubG);
        list.add(10, sourceBaseURI);

        // 目标本体
        list.add(11, targetConceptNum);
        list.add(12, targetPropNum);
        list.add(13, targetInsNum);
        list.add(14, targetConceptName);
        list.add(15, targetPropName);
        list.add(16, targetInsName);
        list.add(17, targetCnptSubG);
        list.add(18, targetPropSubG);
        list.add(19, targetBaseURI);

        // 相似度矩阵
        //相似度矩阵的拷贝
        double[][] simMxC = new double[sourceConceptNum][targetConceptNum];
        simMxC = simMxConcept.clone();
        for (int i = 0; i < sourceConceptNum; i++) {
            if (simMxC[i] != null) {
                simMxC[i] = simMxConcept[i].clone();
            }
        }
        list.add(20, simMxC);
        double[][] simMxP = new double[sourcePropNum][targetPropNum];
        simMxP = simMxProp.clone();
        for (int i = 0; i < sourcePropNum; i++) {
            if (simMxP[i] != null) {
                simMxP[i] = simMxProp[i].clone();
            }
        }
        list.add(21, simMxP);
        double[][] simMxI = new double[sourceInsNum][targetInsNum];
        simMxI = simMxIns.clone();
        for (int i = 0; i < sourceInsNum; i++) {
            if (simMxI[i] != null) {
                simMxI[i] = simMxIns[i].clone();
            }
        }
        list.add(22, simMxI);
        // 本体full基本信息
        list.add(23, sourceAnonCnpt);
        list.add(24, sourceAnonProp);
        list.add(25, sourceAnonIns);
        list.add(26, targetAnonCnpt);
        list.add(27, targetAnonProp);
        list.add(28, targetAnonIns);

        // 已包含确信相似度的位置集合
        list.add(29, sourceCnptOkSimPos);
        list.add(30, targetCnptOkSimPos);
        list.add(31, sourcePropOkSimPos);
        list.add(32, targetPropOkSimPos);
    }

    @SuppressWarnings("unchecked")
    private void packFullSimPgPara(ArrayList list) {
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
        list.add(8, sourceStmList);
        list.add(9, sourceBaseURI);

        // 目标本体
        list.add(10, targetConceptNum);
        list.add(11, targetPropNum);
        list.add(12, targetInsNum);
        list.add(13, targetConceptName);
        list.add(14, targetPropName);
        list.add(15, targetInsName);
        list.add(16, targetStmList);

        list.add(17, targetBaseURI);

        //相似度矩阵的拷贝
        double[][] simMxC = new double[sourceConceptNum][targetConceptNum];
        simMxC = simMxConcept.clone();
        for (int i = 0; i < sourceConceptNum; i++) {
            if (simMxC[i] != null) {
                simMxC[i] = simMxConcept[i].clone();
            }
        }
        list.add(18, simMxC);
        double[][] simMxP = new double[sourcePropNum][targetPropNum];
        simMxP = simMxProp.clone();
        for (int i = 0; i < sourcePropNum; i++) {
            if (simMxP[i] != null) {
                simMxP[i] = simMxProp[i].clone();
            }
        }
        list.add(19, simMxP);
        double[][] simMxI = new double[sourceInsNum][targetInsNum];
        simMxI = simMxIns.clone();
        for (int i = 0; i < sourceInsNum; i++) {
            if (simMxI[i] != null) {
                simMxI[i] = simMxIns[i].clone();
            }
        }
        list.add(20, simMxI);

        //本体full基本信息
        list.add(21, sourceAnonCnpt);
        list.add(22, sourceAnonProp);
        list.add(23, sourceAnonIns);
        list.add(24, targetAnonCnpt);
        list.add(25, targetAnonProp);
        list.add(26, targetAnonIns);
    }

    @SuppressWarnings({"unchecked"})
    private void packInteSubSimPgPara(ArrayList list) {
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
        list.add(8, sourceStmList);
        list.add(9, sourceBaseURI);

        // 目标本体
        list.add(10, targetConceptNum);
        list.add(11, targetPropNum);
        list.add(12, targetInsNum);
        list.add(13, targetConceptName);
        list.add(14, targetPropName);
        list.add(15, targetInsName);
        list.add(16, targetStmList);

        list.add(17, targetBaseURI);

        //相似度矩阵的拷贝
        double[][] simMxC = new double[sourceConceptNum][targetConceptNum];
        simMxC = simMxConcept.clone();
        for (int i = 0; i < sourceConceptNum; i++) {
            if (simMxC[i] != null) {
                simMxC[i] = simMxConcept[i].clone();
            }
        }
        list.add(18, simMxC);
        double[][] simMxP = new double[sourcePropNum][targetPropNum];
        simMxP = simMxProp.clone();
        for (int i = 0; i < sourcePropNum; i++) {
            if (simMxP[i] != null) {
                simMxP[i] = simMxProp[i].clone();
            }
        }
        list.add(19, simMxP);
        double[][] simMxI = new double[sourceInsNum][targetInsNum];
        simMxI = simMxIns.clone();
        for (int i = 0; i < sourceInsNum; i++) {
            if (simMxI[i] != null) {
                simMxI[i] = simMxIns[i].clone();
            }
        }
        list.add(20, simMxI);

        //本体full基本信息
        list.add(21, sourceAnonCnpt);
        list.add(22, sourceAnonProp);
        list.add(23, sourceAnonIns);
        list.add(24, targetAnonCnpt);
        list.add(25, targetAnonProp);
        list.add(26, targetAnonIns);

        //本体的语义子图
        list.add(27, sourceCnptSubG);
        list.add(28, sourcePropSubG);
        list.add(29, targetCnptSubG);
        list.add(30, targetPropSubG);
    }

    @SuppressWarnings("unchecked")
    private void packInteTwoSubSimPgPara(ArrayList list) {
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
        list.add(8, sourceStmList);
        list.add(9, sourceBaseURI);

        // 目标本体
        list.add(10, targetConceptNum);
        list.add(11, targetPropNum);
        list.add(12, targetInsNum);
        list.add(13, targetConceptName);
        list.add(14, targetPropName);
        list.add(15, targetInsName);
        list.add(16, targetStmList);

        list.add(17, targetBaseURI);

        //相似度矩阵的拷贝
        double[][] simMxC = new double[sourceConceptNum][targetConceptNum];
        simMxC = simMxConcept.clone();
        for (int i = 0; i < sourceConceptNum; i++) {
            if (simMxC[i] != null) {
                simMxC[i] = simMxConcept[i].clone();
            }
        }
        list.add(18, simMxC);
        double[][] simMxP = new double[sourcePropNum][targetPropNum];
        simMxP = simMxProp.clone();
        for (int i = 0; i < sourcePropNum; i++) {
            if (simMxP[i] != null) {
                simMxP[i] = simMxProp[i].clone();
            }
        }
        list.add(19, simMxP);
        double[][] simMxI = new double[sourceInsNum][targetInsNum];
        simMxI = simMxIns.clone();
        for (int i = 0; i < sourceInsNum; i++) {
            if (simMxI[i] != null) {
                simMxI[i] = simMxIns[i].clone();
            }
        }
        list.add(20, simMxI);

        //本体full基本信息
        list.add(21, sourceAnonCnpt);
        list.add(22, sourceAnonProp);
        list.add(23, sourceAnonIns);
        list.add(24, targetAnonCnpt);
        list.add(25, targetAnonProp);
        list.add(26, targetAnonIns);

        //本体的语义子图
        list.add(27, sourceCnptSubG);
        list.add(28, sourcePropSubG);
        list.add(29, targetCnptSubG);
        list.add(30, targetPropSubG);
    }

    /***************************************************************************
     * 大本体映射的每个子任务的执行函数
     **************************************************************************/
    public void largeOntMapTask() {
        //匹配计算
        //语义结构抽取
        System.out.println("语义结构抽取");
        reConsSemInf(true);
        //文本匹配
        System.out.println("文本匹配");
        ontMatchText();
        //结构匹配
        System.out.println("结构匹配");
        ontMatchStru();

//		if (isNeedSimProg){
//	        /*相似度传播*/
//			simPropagation();
//			//合并结果
//			System.out.println("合并结果");
//			combineResult();
//		}

    }

    public TextDes getOneCnptDes(OntModel inModel, String cnptName) {
        m_source = inModel;
        sourceOntFile = "";
        parseSingleOnt();
        init();
        ArrayList paraList = new ArrayList();
        ArrayList lt = new ArrayList();
        packOntGraphPara(paraList, true);
        lt = new OntGraph().consInfSubOnt4OneCnpt(paraList, cnptName);
        ConceptSubGraph tsubG = new ConceptSubGraph();
        tsubG = (ConceptSubGraph) lt.get(0);
        sourceCnptSubG[0] = tsubG;

        /**********************************
         * 针对OAEI2008 Library的添加实例处理
         *********************************/
//		Set used=new HashSet();
//		for (Iterator itx=tsubG.stmList.iterator();itx.hasNext();){
//			Statement st=(Statement)itx.next();
//			used.add(st.toString());
//		}
//		for (int i=0;i<sourceFullInsNum;i++){
//			/*遍历实例*/
//			Individual idv=sourceFullInsName[i];
//			if (i>100) {break;}
//			Selector selector = new SimpleSelector(idv,m_source.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),m_source.getOntClass(sourceBaseURI+cnptName));
//	    	for (StmtIterator itx = m_source.listStatements( selector);itx.hasNext();)
//	    	{
//	    		Statement st = (Statement) itx.next();
//	    		if (!used.contains(st.toString())){
//	    			used.add(st.toString());
//	    			sourceCnptSubG[0].stmList.add(st);
//	    		}
//	    	}			
//		}
//		used=null;


        packOntDesPara(paraList, true);
        lt = new OntDes().getCnptOntDes(paraList, cnptName);
        TextDes cnptDes = new TextDes();
        cnptDes = (TextDes) lt.get(0);
        return cnptDes;
    }

    public TextDes getOnePropDes(OntModel inModel, String propName) {
        m_source = inModel;
        sourceOntFile = "";
        parseSingleOnt();
        init();
        ArrayList paraList = new ArrayList();
        ArrayList lt = new ArrayList();
        packOntGraphPara(paraList, true);
        lt = new OntGraph().consInfSubOnt4OneProp(paraList, propName);
        PropertySubGraph tsubG = new PropertySubGraph();
        tsubG = (PropertySubGraph) lt.get(0);
        sourcePropSubG[0] = tsubG;

        packOntDesPara(paraList, true);
        lt = new OntDes().getPropOntDes(paraList, propName);
        TextDes propDes = new TextDes();
        propDes = (TextDes) lt.get(0);
        return propDes;
    }

    //解析本体
    public void parseSingleOnt() {
        OWLOntParse ontParse = new OWLOntParse();
        ArrayList list = new ArrayList();

        //源本体----------------------------------------------
        if (sourceOntFile.length() > 0) {
            m_source = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
            //The ontology file information
            m_source.getDocumentManager().addAltEntry("http://pengwang/", sourceOntFile);
            //Read the reference ontology file
            ontParse.readOntFile(m_source, sourceOntFile);
            //源本体的base URI
            sourceBaseURI = ontParse.getOntBaseURI(m_source);
        }

        //Get all Classes of Ontology
        list = ontParse.listAllConceptsFilterBaseURI(m_source, sourceBaseURI);
        sourceConceptNum = ((Integer) list.get(0)).intValue();
        sourceConceptName = new String[sourceConceptNum];
        sourceConceptName = (String[]) ((ArrayList) list.get(1)).toArray(new String[0]);

        //Get all datatype properties
        list = ontParse.listAllDatatypeRelationsURI(m_source, sourceBaseURI);
        sourceDataPropNum = ((Integer) list.get(0)).intValue();
        sourceDataPropName = new String[sourceDataPropNum];
        sourceDataPropName = (String[]) ((ArrayList) list.get(1)).toArray(new String[0]);

        //Get all object properties
        list = ontParse.listAllObjectRelationsURI(m_source, sourceBaseURI);
        sourceObjPropNum = ((Integer) list.get(0)).intValue();
        sourceObjPropName = new String[sourceObjPropNum];
        sourceObjPropName = (String[]) ((ArrayList) list.get(1)).toArray(new String[0]);

        //Get all properties
        sourcePropNum = sourceDataPropNum + sourceObjPropNum;
        sourcePropName = new String[sourcePropNum];
        for (int i = 0; i < sourceDataPropNum; i++) {
            sourcePropName[i] = sourceDataPropName[i];
        }
        for (int i = 0; i < sourceObjPropNum; i++) {
            sourcePropName[i + sourceDataPropNum] = sourceObjPropName[i];
        }

        //get all instances
        list = ontParse.listAllInstances(m_source);
        sourceInsNum = ((Integer) list.get(0)).intValue();
        sourceInsName = new String[sourceInsNum];
        sourceInsName = (String[]) ((ArrayList) list.get(1)).toArray(new String[0]);
       	
       	/*不局限于baseURI的本体信息*/
        ArrayList fullOntlist = ontParse.getFullOntInfo(m_source);
        //概念信息
        list = (ArrayList) fullOntlist.get(0);
        sourceFullConceptNum = ((Integer) list.get(0)).intValue();
        sourceFullConceptName = new OntClass[sourceFullConceptNum];
        sourceFullConceptName = (OntClass[]) ((ArrayList) list.get(1)).toArray(new OntClass[0]);
        //属性信息
        list = (ArrayList) fullOntlist.get(1);
        sourceFullPropNum = ((Integer) list.get(0)).intValue();
        sourceFullPropName = new OntProperty[sourceFullPropNum];
        sourceFullPropName = (OntProperty[]) ((ArrayList) list.get(1)).toArray(new OntProperty[0]);
        //DatatypeProperty
        list = (ArrayList) fullOntlist.get(2);
        sourceFullDataPropNum = ((Integer) list.get(0)).intValue();
        sourceFullDataPropName = new DatatypeProperty[sourceFullDataPropNum];
        sourceFullDataPropName = (DatatypeProperty[]) ((ArrayList) list.get(1)).toArray(new DatatypeProperty[0]);
        //ObjectProperty
        //DatatypeProperty
        list = (ArrayList) fullOntlist.get(3);
        sourceFullObjPropNum = ((Integer) list.get(0)).intValue();
        sourceFullObjPropName = new ObjectProperty[sourceFullObjPropNum];
        sourceFullObjPropName = (ObjectProperty[]) ((ArrayList) list.get(1)).toArray(new ObjectProperty[0]);
        //实例信息
        list = (ArrayList) fullOntlist.get(4);
        sourceFullInsNum = ((Integer) list.get(0)).intValue();
        sourceFullInsName = new Individual[sourceFullInsNum];
        sourceFullInsName = (Individual[]) ((ArrayList) list.get(1)).toArray(new Individual[0]);

        //匿名资源
        sourceAnonCnpt = new ArrayList();
        sourceAnonProp = new ArrayList();
        sourceAnonIns = new ArrayList();
        list = ontParse.getOntAnonInfo(m_source);
        sourceAnonCnpt = (ArrayList) list.get(0);
        sourceAnonProp = (ArrayList) list.get(1);
        sourceAnonIns = (ArrayList) list.get(2);
    }

    /*************************************
     * 将一些测试数据集的参考结果转换为标准RDF格式
     *************************************/
    private void transAlignResultFormat() {
		/*数据读取*/
        setOntFile();
        parseOnt();
        init();
		
		/*实例集合*/
        Set sIns = new HashSet();
        sIns.addAll(Arrays.asList(sourceInsName));
        Set tIns = new HashSet();
        tIns.addAll(Arrays.asList(targetInsName));

        mappingNum = 0;
        ArrayList lt = new ArrayList();
        Scanner input = null;
        try {
            input = new Scanner(new FileInputStream("./dataset/foamData/russiaCDMap.txt"));
        } catch (IOException e) {
            System.err.println("File not opened:\n" + e.toString());
            System.exit(1);
        }
        while (input.hasNext()) {
            MapRecord mr = new MapRecord();
            String labelA = input.next();
            labelA = labelA.substring(labelA.indexOf("#") + 1, labelA.indexOf(";"));
            String labelB = input.next();
            labelB = labelB.substring(labelB.indexOf("#") + 1, labelB.length());
//			String value=(String)input.next();
//			value=value.substring(0,value.length()-1);
//			double sim=Double.valueOf(value);
            double sim = 1.0;
            mr.sourceLabel = labelA;
            mr.targetLabel = labelB;
            mr.similarity = sim;
            mr.relationType = EQUALITY;
			/*过滤掉实例相似*/
            if (sIns.contains(labelA) || tIns.contains(labelB)) {
                continue;
            }
            lt.add(mr);
            mappingNum++;
        }
        input.close();

        mappingResult = new MapRecord[mappingNum];
        for (int i = 0; i < mappingNum; i++) {
            mappingResult[i] = (MapRecord) lt.get(i);
        }

        lilyFileName = "russiaCDMapNew";
        MappingFile mapFile = new MappingFile();
        mapFile.setBaseURI(sourceBaseURI, targetBaseURI);
        mapFile.save2rdf(sourceOntFile, targetOntFile, mappingNum,
                mappingResult, lilyFileName);

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
        simProgType = ParamStore.Propagation_Strategy;

        OKSIM = ParamStore.OKSIM;
        BADSIM = ParamStore.BADSIM;
        //System.out.println("OKSIM:" + OKSIM);
        //System.out.println("BADSIM" + BADSIM);
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
        for (int i = 0; i < sourceConceptNum; i++)
            for (int j = 0; j < targetConceptNum; j++) {
                if (simMxConcept[i][j] > OKSIM) {
                    int na = t.findCycles().size();
//					System.out.println("添加锚点前的回路数目："+na);
                    g.addEdge("OA_" + sourceConceptName[i], "OB_" + targetConceptName[j]);
                    g.addEdge("OB_" + targetConceptName[j], "OA_" + sourceConceptName[i]);
                    int nb = t.findCycles().size();
//					System.out.println("添加锚点后的回路数目：--"+nb);
                    if (nb - na > 2) {//当前的映射产生了回路，需要放弃
                        g.removeEdge("OA_" + sourceConceptName[i], "OB_" + targetConceptName[j]);
                        g.removeEdge("OB_" + targetConceptName[j], "OA_" + sourceConceptName[i]);
//						System.out.println("当前的映射产生了回路，需要放弃");

                    }
                }
            }
		
		/*判断每个映射加入后有无环路产生*/
        for (int i = 0; i < sourceConceptNum; i++)
            for (int j = 0; j < targetConceptNum; j++) {
                if (simMxConcept[i][j] > cnptSimThreshold && simMxConcept[i][j] < OKSIM) {
					/*错误的映射加入后会导致不一致！*/
                    int na = t.findCycles().size();
//					System.out.println("添加映射前的回路数目："+na);
                    g.addEdge("OA_" + sourceConceptName[i], "OB_" + targetConceptName[j]);
                    int nb = t.findCycles().size();
                    if (nb > na) {
//						System.out.println("发现一条错误映射！！");
                        simMxConcept[i][j] = 0;
                    }
                    g.removeEdge("OA_" + sourceConceptName[i], "OB_" + targetConceptName[j]);

                    g.addEdge("OB_" + targetConceptName[j], "OA_" + sourceConceptName[i]);
                    nb = t.findCycles().size();
                    if (nb > na) {
//						System.out.println("发现一条错误映射！！");
                        simMxConcept[i][j] = 0;
                    }
                    g.removeEdge("OB_" + targetConceptName[j], "OA_" + sourceConceptName[i]);

                }
            }

    }

}
