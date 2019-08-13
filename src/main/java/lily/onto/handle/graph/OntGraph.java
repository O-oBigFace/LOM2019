/************************************************
 * Source code information
 * -----------------------
 * Original author	 Peng Wang, School of Computer Science & Eng., Southeast University
 * Author email      pwangseu@gmail.com
 * Web               http://ontomapping.googlepages.com
 * Created			 2007-4-26
 * Filename          OntGraph.java
 * Version           2.0
 *
 * Last modified on  2007-4-26
 *               by  Peng Wang
 * -----------------------
 * Functions describe:
 * 与本体图相关的预处理模块
 * 包括：（1）本体转换为图的预处理过程；
 *      （2）几种目前子图匹配方法的实验；
 *      （3）语义子图的抽取方法
 * 技术难点：图的预处理需要考虑很多细节；
 *         各种图匹配方法的实现；
 *         语义子图抽取是一个繁琐的过程；
 *         图处理的效率；
 ***********************************************/
package lily.onto.handle.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.io.PrintWriter;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.ontology.*;

import lily.tool.parameters.ParamStore;
import lily.tool.parallelcompute.parfor;
import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.alg.*;

import simpack.measure.graph.*;
import simpack.api.IGraphAccessor;
import simpack.accessor.graph.SimpleGraphAccessor;
import simpack.util.graph.GraphNode;

import lily.tool.datastructure.*;
import lily.onto.parse.OWLOntParse;
import lily.tool.linearequation.*;
import lily.tool.graphsimilarity.*;
import lily.tool.filter.*;

/*******************************************************************************
 * Class information -------------------
 *
 * @author Peng Wang
 * @date 2007-4-26
 *
 * describe: 图方法的主要类
 ******************************************************************************/
@SuppressWarnings("unchecked")
public class OntGraph {
    // 基本本体模型
    public OntModel m;

    // 概念数目
    public int conceptNum;

    // 属性数目
    public int propNum;

    public int dataPropNum;

    public int objPropNum;

    // 实例数目
    public int insNum;

    // 概念名
    public String[] conceptName;

    // 属性名
    public String[] propName;

    public String[] dataPropName;

    public String[] objPropName;

    // 实例名
    public String[] insName;

    // base URI
    public String baseURI;

    // 不局限于baseURI下的本体元素
    public int fullConceptNum;

    public int fullPropNum;

    public int fullDataPropNum;

    public int fullObjPropNum;

    public int fullInsNum;

    public OntClass[] fullConceptName;

    public OntProperty[] fullPropName;

    public DatatypeProperty[] fullDataPropName;

    public ObjectProperty[] fullObjPropName;

    public Individual[] fullInsName;

    // 匿名资源
    ArrayList anonCnpt;

    ArrayList anonProp;

    ArrayList anonIns;

    // 三元组
    public int trpNum;

    public Statement[] graphTrp;

    // 标记多重边的List
    public ArrayList multiEdgeList;

    public ArrayList graphME;

    // 混合模型本体图
    public int graphVNum;

    public String[] graphVName;

    public String[] graphVFullName;

    public DirectedGraph orgnGraph;

    private ArrayList typeVertexName;
    private ArrayList typeVertexFullName;

    // 权重
    public ConceptWt[] cWeight;

    public PropertyWt[] pWeight;

    public InstanceWt[] iWeight;

    public OntLngMetaWt[] metaWeight;

    // 本体元语
    public Set ontLngURI;

    public Set ontLngMetaSet;

    public int ontLngMetaNum;

    public String[] ontLngMetaName;

    // 电路计算
    public double[][] rawCurrent;

    public double[] currentOut;

    public double[][] conductanceMatrix;

    public double[] voltageMatrix;

    //public DeliveryTable[][] Dvk;

    // 子图
    public ConceptSubGraph[] cnptSubG;

    public PropertySubGraph[] propSubG;

    // parse入口
    public OWLOntParse ontParse;

    // 常量
    int LARGE_GRAPH;

    /* 资源权重常数 */
    // 概念加权权重
    double cfw = 0.3;

    double csw = 0.5;

    double ciw = 0.2;

    // 属性加权权重
    double pfw = 0.3;

    double psw = 0.4;

    double piw = 0.3;

    // 实例加权权重
    double idpw = 0.25;

    double iopw = 0.25;

    double icw = 0.5;

    public OntModel m_source;

    public OntModel m_target;

    public DirectedGraph source_Graph;

    public DirectedGraph target_Graph;

    public int source_ConceptNum;

    public int target_ConceptNum;

    public int sourceVertexNum;

    public int targetVertexNum;

    public String[] source_ConceptName;

    public String[] target_ConceptName;

    public int source_PropertyNum;

    public int target_PropertyNum;

    public String[] source_PropertyName;

    public String[] target_PropertyName;

    public int source_InstanceNum;

    public int target_InstanceNum;

    public String[] source_InstanceName;

    public String[] target_InstanceName;

    public String[] sourceVertexName = new String[5000];

    public String[] targetVertexName = new String[5000];

    public ConceptSubGraph[] targetSubGraph = new ConceptSubGraph[1000];

    public double[][] SimMatrixGraph;

    public Set sourceExset = new HashSet();

    public Set sourceOtset = new HashSet();

    public Set sourceStset = new HashSet();

    public Set targetExset = new HashSet();

    public Set targetOtset = new HashSet();

    public Set targetStset = new HashSet();

    public Set newOntStm = new HashSet();

    public int[] pbarValue; //进度条控制
    public double pbarCurrent = 0;
    public double pbarTotal = 0;

    public boolean usedEnrich = true;

    // constant varibles
    public int MAX_DISPSUBGRAPH_SIZE = 0;

    public static final int MAX_DISPSUBGRAPH_ITER_TIMES = 6;

    /***************************************************************************
     * 设置Concept参数
     **************************************************************************/
    public void SetConceptPara(int s_CNum, int t_CNum, String[] s_CName,
                               String[] t_CName) {
        source_ConceptNum = s_CNum;
        target_ConceptNum = t_CNum;

        SimMatrixGraph = new double[source_ConceptNum][target_ConceptNum];
        source_ConceptName = new String[source_ConceptNum];
        target_ConceptName = new String[target_ConceptNum];

        source_ConceptName = s_CName;
        target_ConceptName = t_CName;
    }

    /***************************************************************************
     * 设置Property参数
     **************************************************************************/
    public void SetPropertyPara(int s_PNum, int t_PNum, String[] s_PName,
                                String[] t_PName) {
        source_PropertyNum = s_PNum;
        target_PropertyNum = t_PNum;

        source_PropertyName = new String[source_PropertyNum];
        target_PropertyName = new String[target_PropertyNum];

        source_PropertyName = s_PName;
        target_PropertyName = t_PName;
    }

    /***************************************************************************
     * 设置Instance参数
     **************************************************************************/
    public void SetInstancePara(int s_INum, int t_INum, String[] s_IName,
                                String[] t_IName) {
        source_InstanceNum = s_INum;
        target_InstanceNum = t_INum;

        source_InstanceName = new String[source_InstanceNum];
        target_InstanceName = new String[target_InstanceNum];

        source_InstanceName = s_IName;
        target_InstanceName = t_IName;
    }

    /***************************************************************************
     * 将处理过的本体模型转化为图; 为了处理MultiGraph的本体图，这里采用一种混合 的表示模型
     **************************************************************************/
    @SuppressWarnings("unchecked")
    public DirectedGraph ont2Graph() {
        int mECount;
        ArrayList vList = new ArrayList();
        ArrayList vListWithURI = new ArrayList();
        DirectedGraph g = new DefaultDirectedGraph(DefaultEdge.class);

        // System.out.println("原始本体的大小:" +m.getGraph().size());
        mECount = 0;

		/* 构造本体图*/
        /*1.现将单独的type vertex加入到图中*/
        for (Iterator it = typeVertexName.iterator(); it.hasNext(); ) {
            String vName = (String) it.next();
            g.addVertex(vName);
            vList.add(vName);
        }
        for (Iterator it = typeVertexFullName.iterator(); it.hasNext(); ) {
            String vFullName = (String) it.next();
            vListWithURI.add(vFullName);
        }
		/*2.遍历三元组，依次将三元组加入图*/
        for (int i = 0; i < trpNum; i++) {
            Statement st = graphTrp[i];
            Resource r = st.getSubject();
            Property p = st.getPredicate();
            RDFNode o = st.getObject();

            // 获得S,P,O的名称
            ArrayList list = ontParse.getStLocalName(st);
            String sStr = (String) list.get(0);
            String pStr = (String) list.get(1);
            String oStr = (String) list.get(2);
            String stStr;

            // 处理多重图的边
            if (multiEdgeList.contains(st)) {
                // 构造Statement节点
                stStr = "Statement_" + mECount;
                g.addVertex(stStr);
                vList.add(stStr);
                vListWithURI.add(stStr);
                mECount++;

                // 加入点S
                if (!g.containsVertex(sStr)) {
                    g.addVertex(sStr);
                    addNewVertex(vList, vListWithURI, sStr, r.toString(), r.getNameSpace(), baseURI);
                }
                // 加入点O
                if (!g.containsVertex(oStr)) {
                    g.addVertex(oStr);
                    addNewVertex(vList, vListWithURI, oStr, o.toString(), getResourceBaseURI(o.toString()), baseURI);
                }
                // 加入点P
                if (!g.containsVertex(pStr)) {
                    g.addVertex(pStr);
                    addNewVertex(vList, vListWithURI, pStr, p.toString(), getResourceBaseURI(p.toString()), baseURI);
                }

                // 加入边S-->St
                if (!g.containsEdge(sStr, stStr)) {
                    g.addEdge(sStr, stStr);
                    graphME.add(g.getEdge(sStr, stStr));
                }
                // 加入边St-->P
                if (!g.containsEdge(stStr, pStr)) {
                    g.addEdge(stStr, pStr);
                    graphME.add(g.getEdge(stStr, pStr));
                }
                // 加入边St-->O
                if (!g.containsEdge(stStr, oStr)) {
                    g.addEdge(stStr, oStr);
                    graphME.add(g.getEdge(stStr, oStr));
                }
            } else// 处理普通边
            {
                /****************************
                 * 这里添加点的方法在不同namespace下localname重复时会出现bug
                 * 处理有点麻烦，故忽略之.
                 * 一种处理方法是事先记录下所有要添加的点和边，当出现这种情况时，更正原来的记录
                 * 最后再将最后的处理结果一并加入图中.
                 * ***************************/
                // 加入点S
                if (!g.containsVertex(sStr)) {
                    g.addVertex(sStr);
                    addNewVertex(vList, vListWithURI, sStr, r.toString(), r.getNameSpace(), baseURI);
                }
                // 加入点O
                if (!g.containsVertex(oStr)) {
                    g.addVertex(oStr);
                    addNewVertex(vList, vListWithURI, oStr, o.toString(), getResourceBaseURI(o.toString()), baseURI);
                }
                // 加入边
                if (!g.containsEdge(sStr, oStr)) {
                    g.addEdge(sStr, oStr);
                }
            }
        }

        // 最后增加一个全局节点
        // Add a global vertex, named "PengWang2007"
        g.addVertex("pwang2007");
        vList.add("pwang2007");
        vListWithURI.add("pwang2007");

        // 保存图节点的信息
        graphVNum = vList.size();
        graphVName = (String[]) vList.toArray(new String[0]);
        graphVFullName = (String[]) vListWithURI.toArray(new String[0]);

//		 System.out.println("转化为混合模型图后的大小:" +g.edgeSet().size());

        // Output the graph for show
        // WriteGraphDataForShow(g,VertexName);
        return g;
    }

    /***************************************************************************
     * 将普通的本体图转化为二部图
     **************************************************************************/
    public DirectedGraph Onto2BiptGraph(OntModel m_Outer, boolean Flag_s_OR_t) {
        int i, count;
        OntModel m = m_Outer;
        String[] VertexName = new String[5000];
        int Vertex_Num;
        String baseURI, owlURI, rdfURI, rdfsURI;
        Set Exset = new HashSet();
        Set Otset = new HashSet();
        Set Stset = new HashSet();

        System.out.println("The size of Graph before Process: "
                + m.getGraph().size());

        // 初始化基本URI
        baseURI = m.getNsPrefixURI("");
        owlURI = "http://www.w3.org/2002/07/owl#";
        rdfURI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
        rdfsURI = "http://www.w3.org/2000/01/rdf-schema#";

        if (Flag_s_OR_t) {
            m_source = m;
        } else {
            m_target = m;
        }
        preProcessOnt();

        System.out.println("The size of Graph after Process: " + trpNum);
        // create a graph based on URL objects

        DirectedGraph g = new DefaultDirectedGraph(DefaultEdge.class);

        count = 0;

        for (i = 0; i < trpNum; i++) {
            String vs, vo, vp, stm;
            String stemp;
            boolean isURI;

            isURI = false;
            // 取S
            if (graphTrp[i].getSubject().isURIResource()) {
                vs = graphTrp[i].getSubject().getLocalName();
                isURI = true;
            } else {
                vs = graphTrp[i].getSubject().toString();
            }

            // 记录S到Set
            if (isURI) {
                stemp = getResourceBaseURI(graphTrp[i].getSubject().toString());

                if (stemp.equals(owlURI) || stemp.equals(rdfURI)
                        || stemp.equals(rdfsURI)) {
                    // 是Metadata Set
                    Exset.add(vs);
                } else {
                    // 是Ontology Set
                    Otset.add(vs);
                }
            } else {
                // 是Ontology Set
                Otset.add(vs);
            }

            isURI = false;
            // 取O
            if (graphTrp[i].getObject().isURIResource()) {
                vo = graphTrp[i].getObject().asNode().getLocalName();
                isURI = true;
            } else {
                vo = graphTrp[i].getObject().toString();
            }

            // 记录O到Set
            if (isURI) {
                stemp = getResourceBaseURI(graphTrp[i].getObject().asNode()
                        .getURI());
                if (stemp.equals(owlURI) || stemp.equals(rdfURI)
                        || stemp.equals(rdfsURI)) {
                    // 是Metadata Set
                    Exset.add(vo);
                } else {
                    // 是Ontology Set
                    Otset.add(vo);
                }
            } else {
                // 是Ontology Set
                Otset.add(vo);
            }

            isURI = false;
            // 取P
            if (graphTrp[i].getPredicate().isURIResource()) {
                vp = graphTrp[i].getPredicate().getLocalName();
                isURI = true;
            } else {
                vp = graphTrp[i].getPredicate().toString();
            }

            // 记录P到Set
            if (isURI) {
                stemp = getResourceBaseURI(graphTrp[i].getPredicate().getURI());
                if (stemp.equals(owlURI) || stemp.equals(rdfURI)
                        || stemp.equals(rdfsURI)) {
                    // 是Metadata Set
                    Exset.add(vp);
                } else {
                    // 是Ontology Set
                    Otset.add(vp);
                }
            } else {
                // 是Ontology Set
                Otset.add(vp);
            }

            // 构造Statement
            stm = "Statement_" + i;

            // 记录Statement到Set
            Stset.add(stm);

            // 加入S
            if (g.containsVertex(vs) == false) {
                g.addVertex(vs);
                VertexName[count] = vs;
                count++;
            }

            // 加入O
            if (g.containsVertex(vo) == false) {
                g.addVertex(vo);
                VertexName[count] = vo;
                count++;

            }

            // 加入P
            if (g.containsVertex(vp) == false) {
                g.addVertex(vp);
                VertexName[count] = vp;
                count++;

            }

            // 加入Statement
            g.addVertex(stm);
            VertexName[count] = stm;
            count++;

            // 加入边S-->St
            if (g.containsEdge(vs, stm) == false) {
                g.addEdge(vs, stm);
            } else {
                System.out.println(graphTrp[i].toString());
            }

            // 加入边St-->O
            if (g.containsEdge(stm, vo) == false) {
                g.addEdge(stm, vo);
            } else {
                System.out.println(graphTrp[i].toString());
            }

            // 加入边St-->P
            if (g.containsEdge(stm, vp) == false) {
                g.addEdge(stm, vp);
            } else {
                System.out.println(graphTrp[i].toString());
            }
        }

        // note directed edges are printed as: (<v1>,<v2>)

        // Record the numbers of vertex in the graph
        Vertex_Num = count;

        // Record the numbers of edges in the graph

        trpNum = g.edgeSet().size();

        System.out.println("转换为JGraphT格式后的图大小为：点：" + Vertex_Num + "\t边："
                + trpNum);

        // Output the graph for show
        writeGraphDataForShow(g, VertexName);

        if (Flag_s_OR_t) {
            sourceVertexName = VertexName;
            sourceVertexNum = Vertex_Num;
            // 保存Set
            sourceExset = Exset;
            sourceOtset = Otset;
            sourceStset = Stset;
        } else {
            targetVertexName = VertexName;
            targetVertexNum = Vertex_Num;
            // 保存Set
            targetExset = Exset;
            targetOtset = Otset;
            targetStset = Stset;
        }
        return g;
    }

    /***************************************************************************
     * 获取资源的baseURI
     **************************************************************************/
    public String getResourceBaseURI(String s) {
        String resStr = new String(s);
        int pos = 0;
        pos = resStr.indexOf((int) '#');
        if (pos == 0) {
            return null;
        }
        resStr = resStr.substring(0, pos + 1);
        return resStr;
    }

    /***************************************************************************
     * 获取资源的LocalName
     **************************************************************************/
    public String getResourceLocalName(String s) {
        String resStr = new String(s);
        int pos = resStr.lastIndexOf('#');
        if (pos < 0) {
            return resStr;
        }
        resStr = resStr.substring(pos + 1, resStr.length());
        return resStr;
    }

    public String getResourceLocalName(Resource r) {
        String resStr = new String();

        if (!ontParse.isBlankNode(r.toString())) {
            resStr = r.getLocalName();
        } else {
            resStr = r.toString();
        }
        return resStr;
    }

    /***************************************************************************
     * 将图处理的中间结果写入文件，其它程序读取文件来可视化图
     **************************************************************************/
    public void writeGraphDataForShow(DirectedGraph g, String[] VertexName) {
        int i, count1;
        // ------------------writting Graph File--------------------------
        PrintWriter outputStreamA = null;
        PrintWriter outputStreamB = null;
        PrintWriter outputStreamC = null;
        try {
            outputStreamA = new PrintWriter(new FileOutputStream(
                    "E:/Temp/NetGraph.net"));
        } catch (FileNotFoundException e) {
            System.out.println("Can't open NetGraph.net");
            System.exit(0);
        }
        System.out.println("Writing to file...");

        try {
            outputStreamB = new PrintWriter(new FileOutputStream(
                    "E:/Temp/VertexName.dat"));
        } catch (FileNotFoundException e) {
            System.out.println("Can't open VertexName.dat");
            System.exit(0);
        }

        try {
            outputStreamC = new PrintWriter(new FileOutputStream(
                    "E:/Temp/EdgeName.dat"));
        } catch (FileNotFoundException e) {
            System.out.println("Can't open EdgeName.dat");
            System.exit(0);
        }

        Iterator itr = g.vertexSet().iterator();
        outputStreamA.println("*Vertices " + g.vertexSet().size());
        outputStreamB.println(g.vertexSet().size());
        count1 = 0;
        while (itr.hasNext()) {
            // vertex vt = (vertex) itr.next();
            VertexName[count1] = itr.next().toString();
            outputStreamA.println(count1 + 1 + " \"" + VertexName[count1]
                    + "\"");
            outputStreamB.println(VertexName[count1]);
            count1++;
        }

        outputStreamB.close();

        outputStreamA.println("*Edgeslist");
        outputStreamC.println(g.edgeSet().size());
        itr = g.edgeSet().iterator();
        String strRaw = new String();
        String strV1 = new String();
        String strV2 = new String();

        while (itr.hasNext()) {
            strRaw = itr.next().toString();
            outputStreamC.println(strRaw);

            strV1 = strRaw.substring(1, strRaw.indexOf((int) ' '));
            for (i = 0; i < count1; i++) {
                if (strV1.equals(VertexName[i])) {
                    outputStreamA.print(i + 1);
                    break;
                }
            }
            strV2 = strRaw.substring(strRaw.lastIndexOf((int) ' ') + 1, strRaw
                    .length() - 1);
            for (i = 0; i < count1; i++) {
                if (strV2.equals(VertexName[i])) {
                    outputStreamA.println(" " + (i + 1));
                    break;
                }
            }

            // outputStreamA.println("edge: { sourcename: \"" + strV1 + "\"
            // targetname: \"" + strV2 + "\" }");

        }
        outputStreamC.close();
        outputStreamA.close();
        System.out.println("Writing complete!");

    }

    /***************************************************************************
     * In the PreProcessGraph() Function, the ontology will be processed before
     * matching.
     *这里不修改Model，只是控制构成图的三元组，通过去除某些三元组用来保证图的核心内容更突出
     **************************************************************************/
    public void preProcessOnt() {
        OWLOntParse owlParse = ontParse;
        ArrayList trpList = new ArrayList();
        String baseURI = owlParse.getOntBaseURI(m);

//		System.out.println("未处理前本体图大小："+m.size());

		/*处理Container和List*/
        owlParse.reParseContainer_List(m);

//		System.out.println("处理容器和集合后本体图大小："+m.size());

        /**记录本体丰富前的Statement**/
        Set ontStmV1 = new HashSet();
		/*Enrich the ontology*/
        if (usedEnrich) {
//			for (StmtIterator it=m.listStatements();it.hasNext();){
//				ontStmV1.add(((Statement)(it.next())).toString());
//			}
            //		owlParse.enrichOnt(m, conceptNum, propNum, propName, baseURI);
            /**记录新增加的三远组**/
            for (StmtIterator it = m.listStatements(); it.hasNext(); ) {
                Statement st = it.next();
                String str = st.toString();
                if (!ontStmV1.contains(str)) {
                    newOntStm.add(st);
                }
            }
        }

//		System.out.println("丰富本体后本体图大小："+m.size());

        // List all the statements in Ontology
        StmtIterator iStm = m.listStatements();

        // Traverse the statements to construct the triples needed in Graph
        String ontHeadName = owlParse.getOntHead(m);//取本体头信息
        while (iStm.hasNext()) {
            boolean WITH_ANNOTATION = true;//标注信息标记
            boolean WITH_ONTO_HEADER = true;//本体头信息标记
            boolean WITH_VERSION = true;//版本控制标记
            boolean WITH_RDFTYPE = true;//指向非元语的rdf:type标记
            boolean WITH_THING_NOTHING = true;//thing和nothing标记
            String subName, preName, objName, objNameSpace;

            Statement st = iStm.nextStatement();
            subName = st.getSubject().getLocalName();
            preName = st.getPredicate().getLocalName();
            objName = st.getObject().toString();
            objNameSpace = "";
            if (st.getObject().isURIResource()) {
                objNameSpace = st.getObject().asNode().getNameSpace();
            }

			/* Remove the annotation*/
            if (preName.equals("comment") || preName.equals("label")
                    || preName.equals("seeAlso") || preName.equals("isDefinedBy")
                    || preName.equals("AnnotationProperty")) {
                WITH_ANNOTATION = false;
            }

			/*去除本体头信息*/
            if (st.getSubject().toString().equals(ontHeadName) || preName.equals("imports") ||
                    objName.equals("http://www.w3.org/2002/07/owl#Ontology") ||
                    st.getSubject().toString().equals("http://www.w3.org/2002/07/owl")) {
                WITH_ONTO_HEADER = false;
            }

			/*去除版本控制信息*/
            if (preName.equals("versionInfo") || preName.equals("priorVersion")
                    || preName.equals("backwardCompatibleWith")
                    || preName.equals("incompatibleWith")
                    || (subName != null && subName.equals("DeprecatedProperty"))
                    || objName.equals("http://www.w3.org/2002/07/owl#DeprecatedProperty")
                    || (subName != null && subName.equals("DeprecatedClass"))
                    || objName.equals("http://www.w3.org/2002/07/owl#DeprecatedClass")) {
                WITH_VERSION = false;
            }

			/*去除指向元语的rdf:type，同时将该节点保留下来*/
            if (preName.equals("type") && ontLngURI.contains(objNameSpace)) {
                WITH_RDFTYPE = false;
                addNewVertex(typeVertexName, typeVertexFullName, subName, st.getSubject().toString(), st.getSubject().getNameSpace(), baseURI);
            }

			/*不要Thing和Nothing*/
            if ((subName != null && subName.equals("Thing")) ||
                    (subName != null && subName.equals("Nothing")) ||
                    objName.equals("http://www.w3.org/2002/07/owl#Thing") ||
                    objName.equals("http://www.w3.org/2002/07/owl#Nothing")) {
                WITH_THING_NOTHING = false;
            }

			/*加入未被过滤的三元组*/
            if (WITH_ANNOTATION && WITH_ONTO_HEADER && WITH_VERSION
                    && WITH_RDFTYPE && WITH_THING_NOTHING) {
                // Keeping the triples with instances
                trpList.add(st);
                //System.out.println(st.toString());//显示加入的三元组
            }
        }
        // System.out.println("经过简单过滤的本体三元组数目: "+trpList.size());
        // 判断三元组中MutiGraph的边，并进行处理，为混合的图模型表示做准备
        trp4MultiGraph(trpList);
        // 转换为数组
        trpNum = trpList.size();
        graphTrp = (Statement[]) trpList.toArray(new Statement[0]);
        // 三元组排序，这点很重要，排序才能保证输出结果一致
        sortGraphTriples(graphTrp);
        for (int i = 0; i < trpNum; i++) {
//			System.out.println(graphTrp[i].toString());
        }
//		System.out.println("精炼本体后本体图大小："+trpNum);
    }

    /***************************************************************************
     * Construct the sub ontlogy graph by the neighbors method
     **************************************************************************/
    public void ConsSGNeighbor(Graph source, Graph target, int k_distance) {
        int i, j;
        Set curTotal_set = new HashSet();
        Set star_set = new HashSet();
        Set new_set = new HashSet();
        Set temp_set = new HashSet();

        // For each concept, decide its position in the graph,
        // then construct its subgraph using its neighbors.
        for (i = 0; i < source_ConceptNum; i++) {
            System.out.println("Star Vertex:" + source_ConceptName[i]);
            // BreadthFirstIterator ir = new
            // BreadthFirstIterator(source,source_ConceptName[i]);
            NeighborIndex ni = new NeighborIndex(source);
            if (!star_set.isEmpty())
                star_set.clear();
            if (!curTotal_set.isEmpty())
                curTotal_set.clear();
            if (source_ConceptName[i] == null)
                continue;

            // recorde the subgraph information
            cnptSubG[i] = new ConceptSubGraph();
            cnptSubG[i].conceptName = new String(source_ConceptName[i]);

            star_set.add(source_ConceptName[i]);
            curTotal_set.add(source_ConceptName[i]);

            cnptSubG[i].subGraph = new DefaultDirectedGraph(DefaultEdge.class);
            cnptSubG[i].subGraph.addVertex(source_ConceptName[i]);

            for (j = 0; j < k_distance; j++) {
                if (!new_set.isEmpty())
                    new_set.clear();
                for (Iterator it = star_set.iterator(); it.hasNext(); ) {
                    // Get a Vertex
                    String cur_vertex = (String) it.next();
                    // find the neighbors about the Vetex
                    temp_set = ni.neighborsOf(cur_vertex);

                    // decide the right edges to be added to subgraph
                    for (Iterator itn = temp_set.iterator(); itn.hasNext(); ) {
                        String neighbor_vetex = (String) itn.next();

                        // prepare for the new star_set
                        if (!curTotal_set.contains(neighbor_vetex)) {
                            // add new vertex to the new subgraph
                            cnptSubG[i].subGraph.addVertex(neighbor_vetex);

                            // add the new edges to the new subgraph
                            if (source_Graph.containsEdge(cur_vertex,
                                    neighbor_vetex)) {
                                cnptSubG[i].subGraph.addEdge(cur_vertex,
                                        neighbor_vetex);
                            } else {
                                cnptSubG[i].subGraph.addEdge(neighbor_vetex,
                                        cur_vertex);
                            }

                            new_set.add(neighbor_vetex);
                            curTotal_set.add(neighbor_vetex);
                        }

                    }

                }

                // construct new star set
                System.out.println(new_set);
                // clear star_set
                star_set.clear();
                // add new neighbors
                star_set.addAll(new_set);

                if (new_set.isEmpty())
                    continue;
            }
            System.out.println("The SubGraph is:\n"
                    + cnptSubG[i].subGraph.toString());
        }

        // clear all the sets
        if (!curTotal_set.isEmpty())
            curTotal_set.clear();
        if (!star_set.isEmpty())
            star_set.clear();
        if (!new_set.isEmpty())
            new_set.clear();

        System.out.println("-----------------------------------------------");

        // For each concept, decide its position in the graph,
        // then construct its subgraph using its neighbors.
        for (i = 0; i < target_ConceptNum; i++) {
            System.out.println("Star Vertex:" + target_ConceptName[i]);
            // BreadthFirstIterator ir = new
            // BreadthFirstIterator(source,source_ConceptName[i]);
            NeighborIndex ni = new NeighborIndex(target);
            if (!star_set.isEmpty())
                star_set.clear();
            if (!curTotal_set.isEmpty())
                curTotal_set.clear();
            if (target_ConceptName[i] == null)
                continue;

            // recorde the subgraph information
            targetSubGraph[i] = new ConceptSubGraph();
            targetSubGraph[i].conceptName = new String(target_ConceptName[i]);

            star_set.add(target_ConceptName[i]);
            curTotal_set.add(target_ConceptName[i]);

            targetSubGraph[i].subGraph = new DefaultDirectedGraph(
                    DefaultEdge.class);
            targetSubGraph[i].subGraph.addVertex(target_ConceptName[i]);

            for (j = 0; j < k_distance; j++) {
                if (!new_set.isEmpty())
                    new_set.clear();
                for (Iterator it = star_set.iterator(); it.hasNext(); ) {
                    // Get a Vertex
                    String cur_vertex = (String) it.next();
                    // find the neighbors about the Vetex
                    temp_set = ni.neighborsOf(cur_vertex);

                    // decide the right edges to be added to subgraph
                    for (Iterator itn = temp_set.iterator(); itn.hasNext(); ) {
                        String neighbor_vetex = (String) itn.next();

                        // prepare for the new star_set
                        if (!curTotal_set.contains(neighbor_vetex)) {
                            // add new vertex to the new subgraph
                            targetSubGraph[i].subGraph
                                    .addVertex(neighbor_vetex);

                            // add the new edges to the new subgraph
                            if (target_Graph.containsEdge(cur_vertex,
                                    neighbor_vetex)) {
                                targetSubGraph[i].subGraph.addEdge(cur_vertex,
                                        neighbor_vetex);
                            } else {
                                targetSubGraph[i].subGraph.addEdge(
                                        neighbor_vetex, cur_vertex);
                            }

                            new_set.add(neighbor_vetex);
                            curTotal_set.add(neighbor_vetex);
                        }

                    }

                }

                // construct new star set
                System.out.println(new_set);
                // clear star_set
                star_set.clear();
                // add new neighbors
                star_set.addAll(new_set);

                if (new_set.isEmpty())
                    continue;
            }
            System.out.println("The SubGraph is:\n"
                    + targetSubGraph[i].subGraph.toString());
        }

    }

    /***************************************************************************
     * 子图匹配 方法：子图同构方法（利用了SimPack库）
     **************************************************************************/
    public void ComputeSGMapping_Neighbor(ConceptSubGraph SG_source[],
                                          ConceptSubGraph SG_target[]) {
        // MaxGraphIsoCoveringValiente match = new
        // MaxGraphIsoCoveringValiente(null, null);
        int i, j;
        IGraphAccessor graph1 = new SimpleGraphAccessor();
        IGraphAccessor graph2 = new SimpleGraphAccessor();
        int sourceSize, targetSize, commonSize;

        SubgraphIsomorphism calc;

        for (i = 0; i < source_ConceptNum; i++) {
            sourceSize = 0;
            targetSize = 0;
            if (cnptSubG[i] != null) {
                graph1 = JGraph2IGraph(cnptSubG[i].subGraph);
                sourceSize = cnptSubG[i].subGraph.vertexSet().size();
                for (j = 0; j < target_ConceptNum; j++) {
                    if (targetSubGraph[j] != null) {
                        graph2 = JGraph2IGraph(targetSubGraph[j].subGraph);
                        targetSize = targetSubGraph[j].subGraph.vertexSet()
                                .size();
                        // calc = new MaxCommonSubgraphIsoValiente(
                        // graph1,
                        // graph2,
                        // MaxCommonSubgraphIsoValiente.DEFAULT_MIN_CLIQUE_SIZE,
                        // MaxCommonSubgraphIsoValiente.DEFAULT_STRUCTURE_WEIGHT,
                        // MaxCommonSubgraphIsoValiente.DEFAULT_LABEL_WEIGHT,
                        // MaxCommonSubgraphIsoValiente.DEFAULT_DENOMINATOR);
                        calc = new SubgraphIsomorphism(graph1, graph2);
                        System.out.println("source:"
                                + cnptSubG[i].subGraph.toString());
                        System.out.println("target:"
                                + targetSubGraph[j].subGraph.toString());
                        System.out.println("sourceSubGraph[" + i + "]"
                                + "targetSubGraph[" + j + "]");

                        long start = System.currentTimeMillis();// 开始计时
                        calc.calculate();
                        System.out.println("此次结构匹配算法时间："
                                + (double) (System.currentTimeMillis() - start)
                                / 1000. + "秒");
                        TreeSet<String> covering = calc.getCliqueList();
                        Iterator it = covering.iterator();
                        commonSize = 0;
                        if (it.hasNext()) {
                            String t1 = (String) it.next();
                            System.out.println(t1);
                            commonSize = GetCommonSubGraphSize(t1);
                            System.out.println(commonSize);
                        }
                        SimMatrixGraph[i][j] = (double) commonSize
                                / (double) (sourceSize + targetSize - commonSize);
                        System.out.println("SimMatrixGraph[" + i + "][" + j
                                + "]" + SimMatrixGraph[i][j] + "\n");
                    }
                }
            }
        }

        // System.out.println("new MaxCommonSubgraphIsoValiente");
        // calc1.calculate();
        // System.out.println("calc1.calculate();");
        // calc1.isCalculated();
        // System.out.println("calc1.isCalculated();");

        // TreeSet<String> covering = calc1.getCliqueList();
        // // covering.toArray()
        // for (TreeSet<String> c : covering) {
        // System.out.println("Covering " + c.getClique().toString() + " "
        // + c.getSimilarity());
        // }

        // System.out.println(calc1.getSimilarity());

        System.out.println("done");

    }

    /***************************************************************************
     * JGraph图结构转化为IGraph图结构
     **************************************************************************/
    public IGraphAccessor JGraph2IGraph(DirectedGraph g) {
        IGraphAccessor igraph = new SimpleGraphAccessor();
        GraphNode[] node = new GraphNode[1500];
        int i, count, edgeStar, edgeEnd;
        int edgeCount;

        Iterator itr = g.vertexSet().iterator();

        count = 0;
        while (itr.hasNext()) {
            node[count] = new GraphNode(itr.next().toString());
            count++;
        }

        itr = g.edgeSet().iterator();
        String strRaw = new String();
        String strV1 = new String();
        String strV2 = new String();

        edgeCount = 0;
        while (itr.hasNext() && edgeCount <= 7) {
            strRaw = itr.next().toString();
            edgeStar = 0;
            edgeEnd = 0;
            strV1 = strRaw.substring(1, strRaw.indexOf((int) ' '));
            for (i = 0; i < count; i++) {
                if (strV1.equals(node[i].toString())) {
                    edgeStar = i;
                    break;
                }
            }
            strV2 = strRaw.substring(strRaw.lastIndexOf((int) ' ') + 1, strRaw
                    .length() - 1);
            for (i = 0; i < count; i++) {
                if (strV2.equals(node[i].toString())) {
                    edgeEnd = i;
                    break;
                }
            }
            igraph.setEdge(node[edgeStar], node[edgeEnd]);
            edgeCount++;
            System.out.println(edgeCount);
        }
        return igraph;
    }

    /***************************************************************************
     * 得到公共子图的size
     **************************************************************************/
    public int GetCommonSubGraphSize(String str) {
        int count = 1;
        int k = 0;
        if (str == null)
            return 0;
        while (k != -1) {
            k = str.indexOf((int) ',', k + 1);
            if (k != -1)
                count++;
        }
        return count;
    }

    /***************************************************************************
     * 抽取语义子图方法 新实现中不再使用该函数
     **************************************************************************/
    public void ConsSGInformative(DirectedGraph g, boolean SOURCE_TARGET) {
        int i, j;
        int VertexNum, ConceptNum;
        int rn;
        int star;
        LinearEquation Equation = new LinearEquation();
        ConceptSubGraph[] SubGraph = new ConceptSubGraph[1000];
        String[] VertexName = new String[5000];
        String[] ConceptName = new String[1000];

        // set the vertex number in the graph
        if (SOURCE_TARGET) {
            VertexNum = sourceVertexNum;
            VertexName = sourceVertexName;
            ConceptName = source_ConceptName;
            ConceptNum = source_ConceptNum;
        } else {
            VertexNum = targetVertexNum;
            VertexName = targetVertexName;
            ConceptName = target_ConceptName;
            ConceptNum = target_ConceptNum;
        }

        // initinalize the weight of resources
        getResWeight();

        // Get the conduct matrix
        conductanceMatrix = getConductMatrix(VertexNum + 1, g, VertexName);

        // for each vertex, compute its informative graph

        for (i = 0; i < ConceptNum; i++) {
            if (ConceptName[i] == null)
                continue;

            System.out.println("概念:" + i + "-->" + ConceptName[i]);

            // find the position of the concept
            star = -1;
            for (j = 0; j < VertexNum; j++) {
                if (VertexName[j].equals(ConceptName[i])) {
                    star = j + 1;
                    break;
                }
            }
            if (star == -1)
                continue;

            // recorde the subgraph information
            SubGraph[i] = new ConceptSubGraph();
            SubGraph[i].conceptName = new String(ConceptName[i]);

            SubGraph[i].subGraph = new DefaultDirectedGraph(DefaultEdge.class);

            // ------------------compute subgraph-------------------------

            // the last node is sink node, it is "sourceVertexNum+1"
            Equation.InitlizePara(VertexNum + 1, star, VertexNum + 1);

            // solve the linear system
            Equation.SetConductMatrix(conductanceMatrix);

            Equation.PrepareMatrixA_b(10.0, 0.0);

            Equation.Solve();

            // get row number of result
            rn = Equation.GetResultMaxtrixRowNum();

            // get result matrix
            voltageMatrix = Equation.GetResultMaxtrix();

            // raw current
            ComputeRawCurrent(VertexNum + 1);
            // out current at each vertex
            ComputeCurrentOut(VertexNum + 1);

            // run the displaygeneration algorithm
            // and saving the subgraph
            SubGraph[i].subGraph = displayGraphGeneration(VertexNum,
                    VertexName, MAX_DISPSUBGRAPH_SIZE);

            System.out.println("Star Vertex:" + VertexName[star - 1] + "\n");
            // -------------------compute subgraph done-----------------------

            // for show sub graph
            String[] tempName = new String[SubGraph[i].subGraph.vertexSet()
                    .size()];
            int ti = 0;
            int tcount = 0;

            Iterator itr = SubGraph[i].subGraph.vertexSet().iterator();
            tcount = 0;
            while (itr.hasNext()) {
                tempName[tcount] = itr.next().toString();
                tcount++;
            }

            this.writeGraphDataForShow(SubGraph[i].subGraph, tempName);
        }

        // return the subgraph
        if (SOURCE_TARGET) {
            cnptSubG = SubGraph;
        } else {
            targetSubGraph = SubGraph;
        }

    }

    /***************************************************************************
     * 根据Subject获得Resource
     **************************************************************************/
    public Resource GetResourceBySubject(OntModel m, Resource sub) {
        Resource r = null;
        Selector selector = new SimpleSelector(sub, null, (RDFNode) null);
        for (StmtIterator Iter = m.listStatements(); Iter.hasNext(); ) {
            Statement ts = Iter.next();
            r = ts.getSubject();
            if (r != null && r.toString().equals(sub.toString()))
                break;
        }
        return r;
    }

    /***************************************************************************
     * 根据object获得RDF Node
     **************************************************************************/
    public RDFNode GetRDFNodeByObject(OntModel m, Resource obj) {
        RDFNode r = null;
        Selector selector = new SimpleSelector(null, null, obj);
        for (StmtIterator Iter = m.listStatements(); Iter.hasNext(); ) {
            Statement ts = Iter.next();
            r = ts.getObject();
            if (r != null && r.toString().equals(obj.toString()))
                break;
        }
        return r;
    }

    /***************************************************************************
     * 计算电流图中的边的电导率 语义子图抽取过程的关键步骤之一
     **************************************************************************/
    @SuppressWarnings("unchecked")
    public double[][] getConductMatrix(int Num, DirectedGraph g, String[] VName) {
        double[][] matrix = new double[Num][Num];
        double sum_Cuw;
        Resource r, rS, rO;
        OntResource ontr;
        Property rP;
        double wtS, wtP, wtO, wtPai;
        Statement stmt;
        int CWNum = fullConceptNum;
        int PWNum = fullPropNum;
        int IWNum = fullInsNum;
        int MWNum = ontLngMetaNum;
        int Sdegree;
        int Odegree;

        // 遍历图的邻接矩阵，计算对应边的权值
        for (int i = 0; i < Num - 1; i++) {
            sum_Cuw = 0;
            boolean sFlag = false;
            rS = null;
            wtS = 0;
            for (int j = 0; j < Num - 1; j++) {
                // 判断图中的两点是否存在边,也就是是否有一个triple
                if (g.containsEdge(VName[i], VName[j])) {
//					if (VName[i].equals("sqdsq")||VName[j].equals("sqdsq")){
//						System.out.println("debug lost weight");
//					}
                    // 求出S和O的Degree
                    Sdegree = g.inDegreeOf(VName[i]) + g.outDegreeOf(VName[i]);

                    Odegree = g.inDegreeOf(VName[j]) + g.outDegreeOf(VName[j]);

                    // ---------计算这条边权重-----------

                    // 首先要提取出Triple的S,P,O,并判断它们的具体成分

                    if (!sFlag) {// S的权重还没有计算过
                        sFlag = true;
                        /** *********计算S的权重************** */
                        // 提取S
                        // 判断是不是Statement标记
                        if (graphVName[i].length() >= 9 && (graphVName[i].substring(0, 9)).equals("Statement")) {
                            wtS = 1.0;
                        } else {
                            // S肯定是Resource
                            ontr = m.getOntResource(graphVFullName[i]);
                            if (ontr == null) {// 非URL节点
								/* 从三元组中找到S */
                                ontr = m.getOntResource(findSFromGraphTriple(graphVFullName[i]));
                            }

                            if (ontr != null)// 是OntResource
                            {
                                // 记录
                                rS = ontr;

                                // 判断S的成分
                                boolean isMetaData = ontLngURI.contains(ontr.getNameSpace());
                                if (ontr.isClass() && !isMetaData) {
                                    // 是class
                                    wtS = FindConceptWeight(graphVName[i], CWNum);

                                } else if (ontr.isProperty() && !isMetaData) {
                                    // 是property
                                    wtS = this.FindPropertyWeight(graphVName[i], PWNum);
                                } else if (ontr.isIndividual() && !isMetaData) {
                                    // 是Instance
                                    wtS = this.FindInstanceyWeight(graphVName[i], IWNum);
                                } else if (isMetaData) {
                                    // 是元语
                                    wtS = this.FindMetaWeight(graphVName[i], MWNum);
                                } else {
                                    // 对于剩下的部分,包括没有处理的匿名节点
                                    // 赋予缺省值
                                    wtS = 0.2;
                                }
                            } else {
                                // 如果是Resource
                                r = m.getResource(graphVFullName[i]);
                                r = GetResourceBySubject(m, r);

                                // 记录
                                rS = r;
                                if (r != null) {
                                    if (r.isAnon()) {
                                        // 仅仅是匿名节点
                                        // 赋予缺省值
                                        wtS = 0.2;
                                    } else if (r.isLiteral()) {
                                        // 对Literal赋值
                                        wtS = 0.2;
                                    } else {
                                        wtS = 0.2;
                                    }
                                }
                            }
                        }

                    }

                    // **********计算O的权重***************
                    // 提取O
                    rO = null;
                    wtO = 0;
                    // 判断是不是Statement标记
                    if (graphVName[j].length() >= 9 && (graphVName[j].substring(0, 9)).equals("Statement")) {
                        wtO = 1.0;
                    } else {
                        // O是RDFNode
                        ontr = m.getOntResource(graphVFullName[j]);
                        RDFNode node = null;
                        if (ontr == null) {// 非URL节点
							/* 从三元组中找到O */
                            node = findOFromGraphTriple(graphVFullName[j]);

                            if (node.isAnon()) {// 匿名节点
                                ArrayList lx = ontParse.getAnonResource(
                                        graphVFullName[j], m);
                                if (((Integer) lx.get(1)).intValue() == 0) {
                                    ontr = m.getOntResource(m.getResource(node.toString()));
                                } else {
                                    ontr = (OntResource) lx.get(0);
                                }
                            } else {
                                ontr = m.getOntResource(m.getResource(node.toString()));
                            }
                        }
                        // 记录
                        rO = ontr;
                        if (ontr != null)// 是OntResource
                        {
                            // 判断O的成分
                            boolean isMetaData = ontLngURI.contains(ontr.getNameSpace());
                            if (ontr.isClass() && !isMetaData) {
                                // 是class
                                wtO = FindConceptWeight(graphVName[j], CWNum);
                            } else if (ontr.isProperty() && !isMetaData) {
                                // 是property
                                wtO = this.FindPropertyWeight(graphVName[j], PWNum);
                            } else if (ontr.isIndividual() && !isMetaData) {
                                // 是Instance
                                wtO = this.FindInstanceyWeight(graphVName[j], IWNum);
                            } else if (isMetaData) {
                                // 是元语
                                wtO = this.FindMetaWeight(graphVName[j], MWNum);
                            } else if (ontr.isAnon()) {
                                // 仅仅是匿名节点
                                // 赋予缺省值
                                wtO = 0.2;
                            } else if (ontr.isLiteral()) {
                                // 是Literal
                                wtO = 0.2;
                            } else {
                                // 如果都不是这些,赋予比较低的权重
                                wtO = 0.2;
                            }
                        } else {
                            // 是Resource,而不是OntResource
                            RDFNode tr = node;
                            // 记录
                            rO = m.getResource(tr.toString());

                            if (tr.isAnon()) {
                                // 仅仅是匿名节点
                                // 赋予缺省值
                                wtO = 0.2;
                            } else if (tr.isLiteral()) {
                                // 对Literal赋值
                                wtO = 0.2;
                            } else {
                                wtO = 0.2;
                            }
                        }
                    }

                    // *********计算P的权重*************
                    // 提取P,提取的过程和S,O不同
                    rP = null;
                    wtP = 0;
                    stmt = null;
                    String pStr = null;
                    // 如果是二部图的边，则不用寻找P
                    if (rS != null && rO != null) {
                        ArrayList ls = new ArrayList();
                        ls = findPFromGraphTriple(rS.toString(), rO.toString());
                        rP = (Property) ls.get(0);
                        stmt = (Statement) ls.get(1);
                    }

                    // 如果rP为空,则说明是多重边
                    if (rP == null) {
                        // 给一个缺省的权重
                        wtP = 1.0;
                    } else {
                        // 分OntProperty,元语和其它三种情况来处理
                        if ((m.getOntProperty(rP.toString()) != null)
                                && !ontLngURI.contains(getResourceBaseURI(rP.toString()))) {
                            // 是OntProperty
                            wtP = this
                                    .FindPropertyWeight(
                                            this.getResourceLocalName(rP.toString()), PWNum);
                        } else if (ontLngURI.contains(getResourceBaseURI(rP.toString()))) {
                            // 是元语
                            wtP = this
                                    .FindMetaWeight(
                                            this.getResourceLocalName(rP.toString()), MWNum);
                        } else {
                            // 对其它的情况
                            wtP = 0.2;
                        }
                    }

                    // **********计算Statement的Pai权重***************
                    if (stmt != null) {
                        wtPai = this.GetStatementPaiWeight(m, stmt, rP, rS);
                    } else {
                        wtPai = 0;
                    }

                    // --------------结束计算边的权重----------------------
                    if (wtPai > 0) {
                        matrix[i][j] = (wtP
                                + (wtS / (double) Sdegree + wtO
                                / (double) Odegree) / 2.0 + wtPai) / 3.0;
                    } else {
                        matrix[i][j] = (wtP + (wtS / (double) Sdegree + wtO
                                / (double) Odegree) / 2.0) / 2.0;
                    }
                    // 也许，对于逆向边的权重，不能简单地这样计算
                    // if (wtP>0.7)
                    // {
                    // matrix[j][i] = 1.0*matrix[i][j];
                    // }
                    // else
                    // {
                    // matrix[j][i] = 0.2*matrix[i][j];
                    // }
                    // 逆向边的权重
                    //matrix[j][i] = 0.8 * matrix[i][j];
                    /************************
                     * 早期对逆向边的考虑是多余的，实际上区别对待逆向边不会太多第影响结果
                     * 所以做了修改；
                     * 2012-05-04
                     ************************/
                    matrix[j][i] = 1.0 * matrix[i][j];
                }
                sum_Cuw += matrix[i][j];
            }

            // At the sink node Num, all nodes should link to it
            // The conductance of sink z is special, it is compted by follows:
            // C(u,z)=a Sigma C(u,w), where w!=z
            // Here, we set a=1.0
            // I will impose the result graph in my test
            // 这里的权重设置也觉得不合理，很容易让路径迅速落到sinknode
            // 为了避免惩罚太强，减少前面的系数。
			/*处理图中孤立的点，分配一个很小的边权重，保证图联通*/
            if (Math.abs(sum_Cuw - 0.0) < 0.00001) {
                sum_Cuw = 0.0000001;
                //System.out.println(i);
            }
            matrix[i][Num - 1] = 0.05 * sum_Cuw;
            matrix[Num - 1][i] = matrix[i][Num - 1];
        }
//		for (int i=0;i<Num;i++){
//			System.out.println(i+":"+matrix[i][6]);
//		}
        return matrix;
    }

    /***************************************************************************
     * compute the resource weight
     **************************************************************************/
    public void getResWeight() {
        // 基本数据
        int CNum = fullConceptNum;

        OntClass[] CName = fullConceptName;
        int PNum = fullPropNum;
        OntProperty[] PName = fullPropName;
        int INum = fullInsNum;
        Individual[] IName = fullInsName;
        int MNum = ontLngMetaNum;
        String[] MName = ontLngMetaName;

        ArrayList list = new ArrayList();

        // 计算概念的权重
        double[] cnptFW = new double[CNum];
        double[] cnptSW = new double[CNum];
        double[] cnptIW = new double[CNum];
        // （1）得到使用频率度
        list = getCnptFrequentW();
        cnptFW = (double[]) list.get(0);
        // (2)计算类结构Specificity和实例使用权重
        list = getCnptSpcfct();
        cnptSW = (double[]) list.get(0);
        // (3)计算对应实例构成的权重
        list = getCnptInsW();
        cnptIW = (double[]) list.get(0);

        for (int i = 0; i < CNum; i++) {
            String cStr = null;
            cStr = getResourceLocalName(CName[i]);
            cWeight[i] = new ConceptWt();
            cWeight[i].CName = new String(cStr);
            cWeight[i].weight = cfw * cnptFW[i] + csw * cnptSW[i] + ciw
                    * cnptIW[i];
        }

        // 计算属性的权重
        double[] propFW = new double[PNum];
        double[] propSW = new double[PNum];
        double[] propIW = new double[PNum];

        // （1）得到使用频率度
        list = getPropFrequentW();
        propFW = (double[]) list.get(0);
        // (2)计算层次Specificity权重
        list = getPropSpcfct();
        propSW = (double[]) list.get(0);
        // (3)计算在实例中构成的权重
        list = getPropInsW();
        propIW = (double[]) list.get(0);

        for (int i = 0; i < PNum; i++) {
            OntProperty p = PName[i];
            String pStr = null;
            pStr = getResourceLocalName(p);
            pWeight[i] = new PropertyWt();
            pWeight[i].PName = new String(pStr);
            pWeight[i].weight = pfw * propFW[i] + psw * propSW[i] + piw
                    * propIW[i];
        }

        // 计算实例权重
        double[] insDPW = new double[INum];
        double[] insOPW = new double[INum];
        double[] insCIW = new double[INum];

        // （1）得到使用datatypeproperty的频率权重
        list = getInsDPW();
        insDPW = (double[]) list.get(0);
        // (2)计算objectproperty的频率权重
        list = getInsOPW();
        insOPW = (double[]) list.get(0);
        // (3)对应概念的实例总数比权重
        list = getInsCIW();
        insCIW = (double[]) list.get(0);
        for (int i = 0; i < INum; i++) {
            Individual idv = IName[i];
            String iStr = null;
            iStr = getResourceLocalName(idv);
            iWeight[i] = new InstanceWt();
            iWeight[i].IName = new String(iStr);
            iWeight[i].weight = idpw * insDPW[i] + iopw * insOPW[i] + icw
                    * insCIW[i];
        }

        // 本体元语的权重
        double[] metaFW = new double[MNum];
        // 得到频率权重
        list = getMetaFW();
        metaFW = (double[]) list.get(0);
        for (int i = 0; i < MNum; i++) {
            metaWeight[i] = new OntLngMetaWt();
            metaWeight[i].MName = new String(MName[i]);
            metaWeight[i].weight = metaFW[i];
        }
    }

    /***************************************************************************
     * 计算元语的权重 这里的方法是遍历三元组，但也可以用图的度来计算
     **************************************************************************/
    public ArrayList getMetaFW() {
        int MNum = ontLngMetaNum;
        String[] MName = ontLngMetaName;
        double[] mw = new double[MNum];
        int max = 0;
        ArrayList result = new ArrayList();
        ArrayList mList = new ArrayList();
        // 遍历Statements，判断出现的元语次数
        for (StmtIterator it = m.listStatements(); it.hasNext(); ) {
            Statement st = it.next();
            Resource rs = st.getSubject();
            Property rp = st.getPredicate();
            RDFNode ro = st.getObject();
            // 通过URI判断元语
            String uriStr;
            if (rs.isURIResource()) {
                uriStr = rs.getNameSpace();
                if (ontLngURI.contains(uriStr)) {
                    mList.add(rs.getLocalName());
                }
            }
            if (rp.isURIResource()) {
                uriStr = rp.getNameSpace();
                if (ontLngURI.contains(uriStr)) {
                    mList.add(rp.getLocalName());
                }
            }
            if (ro.isURIResource()) {
                uriStr = ro.asNode().getNameSpace();
                if (ontLngURI.contains(uriStr)) {
                    mList.add(ro.asNode().getLocalName());
                }
            }
        }
        for (int i = 0; i < MNum; i++) {
            for (Iterator j = mList.iterator(); j.hasNext(); ) {
                String str = (String) j.next();
                if (MName[i].equals(str)) {
                    mw[i] += 1.0;
                }
            }
            max = Math.max(max, (int) mw[i]);
        }
        // 计算权重
        for (int i = 0; i < MNum; i++) {
			/*对主要元语给出缺省值*/
            if (MName[i].equals("subClassOf")) {
                mw[i] = 0.5;
                continue;
            }
            if (MName[i].equals("subProperty")) {
                mw[i] = 0.2;
                continue;
            }

            if (max == 0 || (int) mw[i] == 0) {
                mw[i] = 0.0;
            } else {
                if (max == 1) {
                    if ((int) mw[i] == 1) {
                        mw[i] = 1.0;
                    }
                } else {
                    mw[i] = 0.5 * (1.0 / mw[i]) + 0.5 * (1.0 - Math.log(mw[i]) / Math.log((double) max));
                }
            }
        }
        result.add(0, mw);
        result.add(1, max);
        return result;
    }

    /***************************************************************************
     * 计算实例涉及的同类实例的权重
     **************************************************************************/
    public ArrayList getInsCIW() {
        int INum = fullInsNum;
        Individual[] IName = fullInsName;
        double[] icw = new double[INum];
        int max = 0;
        ArrayList result = new ArrayList();
        for (int i = 0; i < INum; i++) {
            Individual idl = IName[i];
            ArrayList tl = new ArrayList();
            tl = ontParse.listSiblingOfInstance(m, idl);
            int n = ((Integer) tl.get(0)).intValue();
            max = Math.max(max, n);
            icw[i] = (double) n;
        }
        // 计算权重
        for (int i = 0; i < INum; i++) {
            if (max == 0 || icw[i] == 0) {
                icw[i] = 0.0;
            } else {
                if (max == 1 && icw[i] == 1) {
                    icw[i] = 1;
                } else {
                    icw[i] = 0.5 * (1.0 / icw[i]) + 0.5
                            * (1 - icw[i] / (double) max);
                }
            }
        }
        result.add(0, icw);
        result.add(1, max);
        return result;
    }

    /***************************************************************************
     * 计算实例涉及的DatatypeProperty的权重
     **************************************************************************/
    public ArrayList getInsDPW() {
        int INum = fullInsNum;
        Individual[] IName = fullInsName;

        double[] dpw = new double[INum];
        int max = 0;
        ArrayList result = new ArrayList();
        // 计算实例对应的DatatypeProperty
        for (int i = 0; i < INum; i++) {
            Individual idl = IName[i];
            ArrayList tl = new ArrayList();
            tl = ontParse.listDatatypePropertiesOfInstance(m, idl);
            int n = ((Integer) tl.get(0)).intValue();
            max = Math.max(max, n);
            dpw[i] = (double) n;
        }
        // 计算权重
        for (int i = 0; i < INum; i++) {
            if (max == 0) {
                dpw[i] = 0.0;
            } else {
                dpw[i] = dpw[i] / (double) max;
            }
        }
        result.add(0, dpw);
        result.add(1, max);
        return result;
    }

    /***************************************************************************
     * 计算实例涉及的ObjectProperty的权重
     **************************************************************************/
    public ArrayList getInsOPW() {
        int INum = fullInsNum;
        Individual[] IName = fullInsName;

        double[] opw = new double[INum];
        int max = 0;
        ArrayList result = new ArrayList();
        // 计算实例对应的ObjectProperty
        for (int i = 0; i < INum; i++) {
            Individual idl = IName[i];
            ArrayList tl = new ArrayList();
            tl = ontParse.listObjectPropertiesOfInstance(m, idl);
            int n = ((Integer) tl.get(0)).intValue();
            max = Math.max(max, n);
            opw[i] = (double) n;
        }
        // 计算权重
        for (int i = 0; i < INum; i++) {
            if (max == 0) {
                opw[i] = 0.0;
            } else {
                opw[i] = opw[i] / (double) max;
            }
        }
        result.add(0, opw);
        result.add(1, max);
        return result;
    }

    /***************************************************************************
     * 计算concept涉及的instance的权重
     **************************************************************************/
    public ArrayList getCnptInsW() {
        int CNum = fullConceptNum;
        OntClass[] CName = fullConceptName;

        double[] iw = new double[CNum];
        int max = 0;
        ArrayList result = new ArrayList();

        // 计算概念对应的实例
        for (int i = 0; i < CNum; i++) {
            OntClass c = CName[i];
            ArrayList tl = new ArrayList();
            tl = ontParse.listInstanceOfConcept(c);
            int n = ((Integer) tl.get(0)).intValue();
            max = Math.max(max, n);
            iw[i] = (double) n;
        }
        // 计算实例造成的权重
        for (int i = 0; i < CNum; i++) {
            if (max == 0 || (int) iw[i] == 0) {
                iw[i] = 0.0;
            } else {
                if (max == 1) {
                    if ((int) iw[i] == 1) {
                        iw[i] = 1.0;
                    }
                } else {
                    iw[i] = 0.5
                            * (1.0 / iw[i])
                            + 0.5
                            * (1.0 - Math.log(iw[i])
                            / Math.log((double) max));
                }
            }
        }
        result.add(0, iw);
        result.add(1, max);
        return result;
    }

    /***************************************************************************
     * 计算属性参与到instance的情况
     **************************************************************************/
    public ArrayList getPropInsW() {
        int PNum = fullPropNum;
        OntProperty[] PName = fullPropName;
        double[] iw = new double[PNum];
        int max = 0;
        ArrayList result = new ArrayList();

        // 计算属性对应的实例
        for (int i = 0; i < PNum; i++) {
            OntProperty p = PName[i];
            ArrayList tl = new ArrayList();
            tl = ontParse.listInstanceOfProperty(m, p);
            int n = ((Integer) tl.get(0)).intValue();
            max = Math.max(max, n);
            iw[i] = (double) n;
        }
        // 计算实例造成的权重
        for (int i = 0; i < PNum; i++) {
            if (max == 0 || (int) iw[i] == 0) {
                iw[i] = 0.0;
            } else {
                if (max == 1) {
                    if ((int) iw[i] == 1) {
                        iw[i] = 1.0;
                    }
                } else {
                    iw[i] = 0.5
                            * (1 / iw[i])
                            + 0.5
                            * (1 - Math.log(iw[i])
                            / Math.log((double) max));
                }
            }
        }
        result.add(0, iw);
        result.add(1, max);
        return result;
    }

    /***************************************************************************
     * 计算给定概念的specificity
     **************************************************************************/
    public ArrayList getCnptSpcfct() {
        int CNum = fullConceptNum;
        OntClass[] CName = fullConceptName;
        double[] sw = new double[CNum];
        ArrayList result = new ArrayList();
        int uplength, downlength;
        // 计算概念的Specificity
        for (int i = 0; i < CNum; i++) {
            OntClass c = CName[i];
            // Node node=null;
            // node=m.getResource(CName[i]).asNode();
            // Resource ce=m.getResource(node.getURI());
            // OntClass c = m.getOntClass(node.getURI());

            uplength = 0;
            downlength = 0;
            Set cset = new HashSet();
            cset.add(c);
            uplength = ontParse.getMaxUpperClassPathCount(System.out, c, cset);
            cset.clear();
            cset.add(c);
            downlength = ontParse
                    .getMaxLowerClassPathCount(System.out, c, cset);
            if (uplength == 0) {
                uplength = 1;
            }
            if (downlength == 0) {
                downlength = 1;
            }
            sw[i] = (double) uplength / (double) (uplength + downlength - 1);
        }
        result.add(0, sw);
        return result;
    }

    /***************************************************************************
     * 计算给定属性的specificity
     **************************************************************************/
    public ArrayList getPropSpcfct() {
        int PNum = fullPropNum;
        OntProperty[] PName = fullPropName;
        double[] sw = new double[PNum];
        ArrayList result = new ArrayList();
        int uplength, downlength;
        // 计算概念的Specificity
        for (int i = 0; i < PNum; i++) {
            OntProperty p = PName[i];
            uplength = 0;
            downlength = 0;
            Set pset = new HashSet();
            pset.add(p);
            uplength = ontParse.getMaxUpperPropertyPathCount(System.out, p,
                    pset);
            pset.clear();
            pset.add(p);
            downlength = ontParse.getMaxLowerPropertyPathCount(System.out, p,
                    pset);
            if (uplength == 0)
                uplength = 1;
            if (downlength == 0)
                downlength = 1;
            sw[i] = (double) uplength / (double) (uplength + downlength - 1);
        }
        result.add(0, sw);
        return result;
    }

    /***************************************************************************
     * 计算给定概念的被使用情况的频率度 通过图的度来求
     **************************************************************************/
    public ArrayList getCnptFrequentW() {
        // 基本数据
        int CNum = fullConceptNum;
        OntClass[] CName = fullConceptName;
        int maxDegree = 0;
        double[] fw = new double[CNum];
        ArrayList result = new ArrayList();

        // 计算概念的频率
        for (int i = 0; i < CNum; i++) {
            // 得到LocalName
            String localName = ontParse.getResourceLocalName(CName[i]);
            // 求度
            fw[i] = orgnGraph.inDegreeOf(localName) + orgnGraph.outDegreeOf(localName);
            // 判断最大度
            maxDegree = Math.max(maxDegree, (int) fw[i]);
        }
        // 计算权重
        for (int i = 0; i < CNum; i++) {// 隐含条件fw[i]<=maxDegree
            if (maxDegree == 0 || (int) fw[i] == 0) {
                fw[i] = 0.0;
            } else {
                if (maxDegree == 1) {
                    if ((int) fw[i] == 1) {
                        fw[i] = 1.0;
                    }
                } else {
                    fw[i] = 0.5
                            * (1.0 / fw[i])
                            + 0.5
                            * (1.0 - Math.log(fw[i])
                            / Math.log((double) maxDegree));
                }
            }
        }
        // 返回结果
        result.add(0, fw);
        result.add(1, maxDegree);
        return result;
    }

    /***************************************************************************
     * 计算给定属性的被使用情况的频率度 这里只考虑属性作为节点的情况，把属性节电的度作为对它的描述来看待， 描述越多的属性，信息越大 通过图的度来求
     **************************************************************************/
    public ArrayList getPropFrequentW() {
        // 基本数据
        int PNum = fullPropNum;
        Property[] PName = fullPropName;
        int maxDegree = 0;
        double[] fw = new double[PNum];
        ArrayList result = new ArrayList();

        // 计算属性的频率
        for (int i = 0; i < PNum; i++) {
            // 得到LocalName
            String localName = getResourceLocalName(PName[i]);
            // 求度
            fw[i] = orgnGraph.inDegreeOf(localName)
                    + orgnGraph.outDegreeOf(localName);
            // 判断最大度
            maxDegree = Math.max(maxDegree, (int) fw[i]);
        }
        // 计算权重
        for (int i = 0; i < PNum; i++) {
            if (maxDegree == 0 || (int) fw[i] == 0) {
                fw[i] = 0.0;
            } else {
                if (maxDegree == 1) {
                    if ((int) fw[i] == 1) {
                        fw[i] = 1.0;
                    }
                } else {
                    fw[i] = 0.5
                            * (1.0 / fw[i])
                            + 0.5
                            * (1 - Math.log(fw[i])
                            / Math.log((double) maxDegree));
                }
            }
        }
        // 返回结果
        result.add(0, fw);
        result.add(1, maxDegree);
        return result;
    }

    /***************************************************************************
     * 由于要遍历对应的class,所以需要到底是那个本体
     **************************************************************************/
    public double GetInstanceWeight(Individual idv, boolean Flag_s_OR_t,
                                    String baseURI) {
        int uplength = 0, downlength = 0;
        int i, j;
        double weight = 0;
        String[] CName = new String[100];
        int CNum = 0;
        int CWTNum = 0;
        double dtemp = 0;

        if (Flag_s_OR_t) {
            CWTNum = source_ConceptNum;
        } else {
            CWTNum = target_ConceptNum;
        }
        CNum = ontParse.listConceptsOfInstance(CName, idv, true);

        // 对instance对应的所有class,遍历,寻找最大的权重
        for (i = 0; i < CNum; i++) {
            boolean HasClass = false;
            dtemp = 0;
            // 有没有这个class
            for (j = 0; j < CWTNum; j++) {
                if ((baseURI + CName[i]).equals(cWeight[j].CName)) {
                    HasClass = true;
                    dtemp = cWeight[j].weight;
                    break;
                }
            }

            // 如果有这个class的权,则和现有的权重取最大值
            if (HasClass) {
                weight = Math.max(weight, dtemp);
            } else
            // 如果没有这个class,则赋予缺省的权重
            {
                weight = Math.max(weight, 0.2);
            }
        }

        System.out.println(idv.toString() + "-->" + weight);

        return weight;
    }

    /***************************************************************************
     * 求三元组的Pai权重
     **************************************************************************/
    public double GetStatementPaiWeight(OntModel m, Statement stmt,
                                        Property rP, Resource rS) {
        double wtPai = 0;
        boolean flag;
        flag = false;
        int count = 0;
        String[] CName = new String[10];
        int CNum = 0;
        String CaName, CbName;
        boolean SameNameFlag;
        int i;
        Individual ix, iy;

        // 判断是否需要计算Pai
        OntResource tempS = m.getOntResource(rS);
        RDFNode tempONode = stmt.getObject();
        OntResource temprO = m.getOntResource(m.getResource(tempONode
                .toString()));
        if ((rP != null) && (m.getOntProperty(rP.toString()) != null)) {
            // 是OntProperty才需要计算Pai
            // 还需要满足(1):S是Instance,且(2)O是Instance或Literal

            if (tempS != null && tempS.isIndividual()) {
                // S是Instance
                // 进一步判断O
                if (temprO != null && temprO.isIndividual()) {
                    // System.out.println(tempS.toString());
                    flag = true;
                }
                if (tempONode != null && tempONode.isLiteral()) {
                    // System.out.println(tempS.toString());
                    flag = true;
                }

            }
        }

        if (flag) {
            // 符合条件,需要计算Pai
            count = 0;
            ix = null;
            iy = null;
            if (tempS.isAnon()) {
                ix = ontParse.getAnonIndividual(tempS.toString(), m);
            } else {
                ix = m.getIndividual(tempS.toString());
            }
            if (temprO != null && temprO.isIndividual()) {
                // 计算individual---individual情况
                if (temprO.isAnon()) {
                    iy = ontParse.getAnonIndividual(temprO.toString(), m);
                } else {
                    iy = m.getIndividual(temprO.toString());
                }

                // (0)预先找到Ca和Cb
                ontParse.listConceptsOfInstance(CName, ix, true);
                CaName = CName[0];// 只考虑第一个类
                ontParse.listConceptsOfInstance(CName, iy, true);
                CbName = CName[0];// 只考虑第一个类

                // (1)列出所有<?,P,?>
                Selector selector = new SimpleSelector(null, rP, (RDFNode) null);
                for (StmtIterator Iter = m.listStatements(selector); Iter
                        .hasNext(); ) {
                    Statement temps = Iter.next();
                    // System.out.println(temps.toString());

                    // (2)过滤,保证?都是individual
                    Resource temprsub = temps.getSubject();
                    OntResource tempOrsub = m
                            .getOntResource(temprsub);

                    RDFNode tempro = temps.getObject();
                    OntResource tempOro = m.getOntResource(m.getResource(tempro
                            .toString()));
                    if (tempOrsub != null && tempOrsub.isIndividual()
                            && tempOro != null && tempOro.isIndividual()) {
                        // 保证是individual

                        // (3)找到ix和iy对应的Cx和Cy
                        if (tempOrsub.isAnon()) {
                            ix = this
                                    .obtainAnonIndividual(tempOrsub.toString());
                        } else {
                            ix = m.getIndividual(tempOrsub.toString());
                        }
                        if (tempOro.isAnon()) {
                            iy = ontParse.getAnonIndividual(tempOro.toString(),
                                    m);
                        } else {
                            iy = m.getIndividual(tempOro.toString());
                        }

                        // (4)比较看是否和给定的ix和iy的Cx和Cy相同

                        SameNameFlag = false;

                        // 先找到Cx
                        CNum = ontParse.listConceptsOfInstance(CName, ix, true);

                        if (CNum > 0)// 比较Cx和Ca
                        {
                            // 如果概念存在
                            for (i = 0; i < CNum; i++) {
                                if (CName[i].equals(CaName)) {
                                    SameNameFlag = true;
                                    break;
                                }
                            }

                        } else {
                            SameNameFlag = false;
                        }

                        if (SameNameFlag) {
                            // Cx==Ca,则继续比较Cy和Cb
                            SameNameFlag = false;

                            // 先找到Cy
                            CNum = ontParse.listConceptsOfInstance(CName, iy,
                                    true);

                            if (CNum > 0)// 比较Cy和Cb
                            {
                                // 如果概念存在
                                for (i = 0; i < CNum; i++) {
                                    if (CName[i].equals(CbName)) {
                                        SameNameFlag = true;
                                        break;
                                    }
                                }

                            } else {
                                SameNameFlag = false;
                            }

                            if (SameNameFlag) {
                                // 如果两个对应的概念都相等
                                count++;
                            }
                        }
                    }
                }
            } else {
                // 计算individual---Literal情况

                // (1)找到Ca
                if (tempS.isAnon()) {
                    ix = ontParse.getAnonIndividual(tempS.toString(), m);
                } else {
                    ix = m.getIndividual(tempS.toString());
                }

                ontParse.listConceptsOfInstance(CName, ix, true);
                CaName = CName[0];

                // (2)列出所有<?,P,?>
                Selector selector = new SimpleSelector(null, rP, (RDFNode) null);
                for (StmtIterator Iter = m.listStatements(selector); Iter
                        .hasNext(); ) {
                    Statement temps = Iter.next();
                    // System.out.println(temps.toString());

                    // (3)过滤,保证?是individual和Literal
                    Resource temprsub = temps.getSubject();
                    OntResource tempOrsub = m
                            .getOntResource(temprsub);

                    RDFNode tempro = temps.getObject();
                    OntResource tempOro = m.getOntResource(tempro.toString());
                    // 注意这里的Literal是否能正确判断
                    if (tempOrsub != null && tempOrsub.isIndividual()
                            && tempro != null && tempro.isLiteral()) {
                        // 保证是individual--Literal

                        // (4)找到ix对应的Cx,比较看是否和给定的Ca相同
                        if (tempOrsub.isAnon()) {
                            ix = ontParse.getAnonIndividual(tempOrsub
                                    .toString(), m);
                        } else {
                            ix = m.getIndividual(tempOrsub.toString());
                        }

                        SameNameFlag = false;

                        // 找到Cx
                        CNum = ontParse.listConceptsOfInstance(CName, ix, true);

                        if (CNum > 0)// 比较Cx和Ca
                        {
                            // 如果概念存在
                            for (i = 0; i < CNum; i++) {
                                if (CName[i].equals(CaName)) {
                                    SameNameFlag = true;
                                    break;
                                }
                            }

                        } else {
                            SameNameFlag = false;
                        }

                        if (SameNameFlag) {
                            // Cx==Ca,则计数
                            count++;
                        }
                    }
                }
            }
            // 计算Pai
            if (count == 0) {
                wtPai = 0;
            } else {
                wtPai = 1.0 / (double) count;
            }
        } else {
            // 不用计算Pai
            wtPai = 0;
        }
        ix = null;
        iy = null;
        return wtPai;
    }

    /***************************************************************************
     * 查询Concept权重
     **************************************************************************/
    public double FindConceptWeight(String Name, int Num) {
        int i;
        double weight = 0;
        for (i = 0; i < Num; i++) {
            if (cWeight[i].CName.equals(Name)) {
                weight = cWeight[i].weight;
                break;
            }
        }
        return weight;
    }

    /***************************************************************************
     * 查询Property权重
     **************************************************************************/
    public double FindPropertyWeight(String Name, int Num) {
        int i;
        double weight = 0;
        for (i = 0; i < Num; i++) {
            if (pWeight[i].PName.equals(Name)) {
                weight = pWeight[i].weight;
                break;
            }
        }
        return weight;
    }

    /***************************************************************************
     * 查询Instance权重
     **************************************************************************/
    public double FindInstanceyWeight(String Name, int Num) {
        int i;
        double weight = 0;
        for (i = 0; i < Num; i++) {
            if (iWeight[i].IName.equals(Name)) {
                weight = iWeight[i].weight;
                break;
            }
        }
        return weight;
    }

    /***************************************************************************
     * 查询Meta data权重
     **************************************************************************/
    public double FindMetaWeight(String Name, int Num) {
        int i;
        double weight = 0;
        for (i = 0; i < Num; i++) {
            if (metaWeight[i].MName.equals(Name)) {
                weight = metaWeight[i].weight;
                break;
            }
        }
        return weight;
    }

    /***************************************************************************
     * 计算原始电流
     **************************************************************************/
    public void ComputeRawCurrent(int Num) {
        rawCurrent = ComputeRawCurrent(Num, voltageMatrix);
    }

    public double[][] ComputeRawCurrent(int Num, double[] voltageMatrix) {
        int i, j;
        double rawCurrent[][] = new double[Num][Num];
        for (i = 0; i < Num; i++) {
            for (j = 0; j < Num; j++) {
                rawCurrent[i][j] = conductanceMatrix[i][j] * (voltageMatrix[i] - voltageMatrix[j]);
            }
        }
        return rawCurrent;
    }

    /***************************************************************************
     * 计算out电流
     **************************************************************************/
    public void ComputeCurrentOut(int Num) {
        currentOut = ComputeCurrentOut(Num, rawCurrent);
    }

    public double[] ComputeCurrentOut(int Num, double rawCurrent[][]) {
        int i, j;
        double currentOut[] = new double[Num];
        for (i = 0; i < Num; i++) {
            currentOut[i] = 0;
            for (j = 0; j < Num; j++) {
                if (rawCurrent[i][j] > 0)
                    currentOut[i] += rawCurrent[i][j];
            }
            // System.out.println("currentOut["+i+"]"+currentOut[i]);
        }
        return currentOut;
    }

    /***************************************************************************
     * displayGeneration算法 是子图抽取的一个核心问题，本质上是一个搜索空间问题 该方法为贪心算法
     * 此算法的效率并不高，但如何改进是个挑战
     * 贪心算法不能保证结果最优
     **************************************************************************/
    public DirectedGraph displayGraphGeneration(int VertexNum, String[] VertexName, int gSize) {
        return displayGraphGeneration(VertexNum, VertexName, gSize, voltageMatrix, rawCurrent, currentOut);
    }

    public DirectedGraph displayGraphGeneration(int VertexNum, String[] VertexName, int gSize, double[] voltageMatrix, double[][] rawCurrent, double[] currentOut) {
        int i, k;
        int k1;
        int v;
        int MAX_TABLE_ROW = VertexNum + 10;
        int MAX_TABLE_COLADD = gSize * 3;//估计列的大小
        int[] order = new int[MAX_TABLE_ROW];
        DirectedGraph gDisp;
        double maxpath = 0;
        int max_v = 0, max_k = 0;
        int iter_v = 0, iter_k = 0;
        int[][] updateMatrix;
        int count = 0;
        int k_temp = 0;
        int temp_v = 0;
        String[] pathList;
        boolean AllVertex = false;
        int trueNode = 0;
        int trueEdge = 0;//不含匿名节点的边的数目
        Set mtEdge = new HashSet();//多重边计数
        int mtEdgeNum = 0;


        // P=P+1;//sink node不算，所以总数要加1

        // sort the vertex
        order = SortVertexByVoltage(VertexNum, voltageMatrix);

        // Initialize output graph
        gDisp = new DefaultDirectedGraph(DefaultEdge.class);

        pathList = new String[MAX_TABLE_COLADD + 1];

        // Initialize Dvk
        DeliveryTable[][] Dvk = new DeliveryTable[MAX_TABLE_ROW][MAX_TABLE_COLADD + 1];

        updateMatrix = new int[MAX_TABLE_ROW][MAX_TABLE_COLADD + 1];

        // While output graph is not big enough

        // Initinalize the table
        for (i = 0; i < VertexNum; i++) {
            for (k = 0; k <= MAX_TABLE_COLADD; k++) {
                Dvk[i][k] = new DeliveryTable();
            }
        }

        for (i = 0; i < VertexNum; i++) {
            // Let v=ui
            v = i;
            for (k = 1; k <= MAX_TABLE_COLADD; k++) {
                if (gDisp.containsVertex(VertexName[order[v]])) {
                    k1 = k;
                } else {
                    k1 = k - 1;
                }

                if (v == 0) {
                    Dvk[v][k].deliveryCurrent = -1.0;
                    continue;
                }

                // compute the Dvk
                // deal with v=s and dvk(s,1)

                int[] myint = new int[1];
                myint[0] = -1;
                Dvk[v][k].deliveryCurrent = GetMaxDvk(v, k1, order, myint, Dvk, rawCurrent, currentOut);
                Dvk[v][k].predV = myint[0];
                Dvk[v][k].predK = k1;

                // System.out.println("Dvk["+v+"]["+k+"]:"+Dvk[v][k].deliveryCurrent);
            }

        }

        //设置算法退出条件：图最大为25, 当前加入最大电流为0.15
        while (((trueEdge + mtEdgeNum) < 25) && (AllVertex == false)) {

            AllVertex = true;

            // When a iterate is complete, update the D(v,k)
            // compute the inserted vertexs along the path
            for (i = 0; i < VertexNum; i++)
                for (k = 0; k <= MAX_TABLE_COLADD; k++) {
                    if (Dvk[i][k].deliveryCurrent > 0) {
                        updateMatrix[i][k] = k;
                    } else {
                        updateMatrix[i][k] = 0;
                    }
                }

            // compute new k num for each Dvk
            for (i = 0; i < VertexNum; i++) {

                for (k = 0; k <= MAX_TABLE_COLADD; k++) {
                    count = 0;
                    k_temp = k;
                    v = i;
                    while (Dvk[v][k_temp].deliveryCurrent > 0) {
                        if (gDisp.containsVertex(VertexName[order[v]])) {
                            count++;
                        }
                        temp_v = v;
                        v = Dvk[v][k_temp].predV;
                        k_temp = Dvk[temp_v][k_temp].predK;
                        if (v == 0
                                && gDisp.containsVertex(VertexName[order[v]])) {
                            count++;
                            break;
                        }
                    }
                    if (updateMatrix[i][k] > 0) {

                        updateMatrix[i][k] = k - count;
                    }
                }
            }

            // find the max Dvk/k
            maxpath = 0;
            for (i = 0; i < VertexNum; i++)
                for (k = 0; k <= MAX_TABLE_COLADD; k++) {
                    if (updateMatrix[i][k] > 0) {
                        AllVertex = false;
                        if (Dvk[i][k].deliveryCurrent / (double) updateMatrix[i][k] > maxpath) {
                            maxpath = Dvk[i][k].deliveryCurrent / (double) updateMatrix[i][k];
                            max_v = i;
                            max_k = k;
                        }
                    }
                }

            // Add the path maximizing D(t,k)/k
            iter_v = max_v;
            iter_k = max_k;
            count = 0;
            //System.out.println("maxpath delivery current:"+maxpath);

            while ((Dvk[iter_v][iter_k].deliveryCurrent > 0.0)) {
                // System.out.print(sourceVertexName[order[iter_v]]+"-->");
                if ((!ontParse.isBlankNode(VertexName[order[iter_v]]))
                        && (!gDisp.containsVertex(VertexName[order[iter_v]]))
                        && (VertexName[order[iter_v]].indexOf("Statement_") == -1)) {
                    trueNode++;
                }
                if (VertexName[order[iter_v]].startsWith("Statement_")) {
                    mtEdge.add(VertexName[order[iter_v]]);
                }
                gDisp.addVertex(VertexName[order[iter_v]]);

                pathList[count] = VertexName[order[iter_v]];
                count++;
                iter_v = Dvk[iter_v][iter_k].predV;
                iter_k = Dvk[iter_v][iter_k].predK;
                if (iter_v == 0) {
                    // System.out.println(sourceVertexName[order[iter_v]]);
                    if (!ontParse.isBlankNode(VertexName[order[iter_v]])
                            && !gDisp.containsVertex(VertexName[order[iter_v]])) {
                        trueNode++;
                    }
                    if (VertexName[order[iter_v]].startsWith("Statement_")) {
                        mtEdge.add(VertexName[order[iter_v]]);
                    }
                    gDisp.addVertex(VertexName[order[iter_v]]);

                    pathList[count] = VertexName[order[iter_v]];
                    count++;
                    break;
                }
            }
            // System.out.println();

            for (i = count - 1; i > 0; i--) {
				/* 加入边前要判断边的方向是否正确 */
				/* 不加入辅助的sink node */
                boolean isTrueEdge = true;
//				if (ontParse.isBlankNode(pathList[i]) || pathList[i].startsWith("Statement_")
//					|| ontParse.isBlankNode(pathList[i-1]) || pathList[i-1].startsWith("Statement_")){
//					isTrueEdge = false;
//				}
                if (pathList[i].startsWith("Statement_")
                        || ontParse.isBlankNode(pathList[i - 1]) || pathList[i - 1].startsWith("Statement_")) {
                    isTrueEdge = false;
                }
                if (!pathList[i].equals("pwang2007")
                        && !pathList[i - 1].equals("pwang2007")) {
                    if (orgnGraph.containsEdge(pathList[i], pathList[i - 1])) {
                        gDisp.addEdge(pathList[i], pathList[i - 1]);
						/*判断是否为需要的边*/
//						System.out.println("edge:"+"("+pathList[i]+"-->"+pathList[i - 1]+")");
                        if (isTrueEdge) {
                            trueEdge++;
//							System.out.println("edge:"+trueEdge+"("+pathList[i]+"-->"+pathList[i - 1]+")");
                        }
                    } else if (orgnGraph.containsEdge(pathList[i - 1],
                            pathList[i])) {
                        gDisp.addEdge(pathList[i - 1], pathList[i]);
//						System.out.println("edge:"+"("+pathList[i-1]+"-->"+pathList[i]+")");
                        if (isTrueEdge) {
                            trueEdge++;
//							System.out.println("edge:"+trueEdge+"("+pathList[i-1]+"-->"+pathList[i]+")");
                        }
                    }
                }
            }
            //多重边数目
            mtEdgeNum = mtEdge.size();
            //System.out.println("mtEdge:"+mtEdge.toString());

            if (maxpath < 0.1) {
                break;
            }
        }
		/* 去除sink node */
        if (gDisp.containsVertex("pwang2007")) {
            gDisp.removeVertex("pwang2007");
        }

        //System.out.println("完成一次抽取");
        return gDisp;
    }

    /***************************************************************************
     * sort the vertex by voltage the order[] records the result
     **************************************************************************/
    public int[] SortVertexByVoltage(int Num) {
        return SortVertexByVoltage(Num, voltageMatrix);
    }

    public int[] SortVertexByVoltage(int Num, double[] voltageMatrix) {
        int id = 0;
        int[] order = new int[Num];
        double[] mirror = new double[Num];
        boolean[] flag = new boolean[Num];

        for (int i = 0; i < Num; i++) {
            order[i] = i;
            mirror[i] = voltageMatrix[i];
            flag[i] = false;
        }

        Arrays.sort(mirror);

        for (int i = 0; i < Num; i++) {
            id = 0;
            for (int j = 0; j < Num; j++) {
                if (!flag[j] && (voltageMatrix[j] == mirror[Num - i - 1])) {
                    id = j;
                    break;
                }
            }
            order[i] = id;
            flag[id] = true;
        }
        return order;
    }

    /***************************************************************************
     * 尽量保证读入的本体,每次处理的triple都相同
     **************************************************************************/
    public void sortGraphTriples(Statement[] trp) {
        StmComparator comp = new StmComparator();
        Arrays.sort(trp, comp);
    }

    /***************************************************************************
     * 计算最大分配电流与新节点之比
     **************************************************************************/
    public double GetMaxDvk(int v, int k1, int[] order, int[] get_u, DeliveryTable[][] Dvk) {
        return GetMaxDvk(v, k1, order, get_u, Dvk, rawCurrent, currentOut);
    }

    public double GetMaxDvk(int v, int k1, int[] order, int[] get_u, DeliveryTable[][] Dvk, double[][] rawCurrent, double[] currentOut) {
        int i;
        int u = 0;
        double maxdc = -1.0;
        double dxv = 0;

        get_u[0] = -1;
        for (i = 0; i < v; i++) {
            u = i;
            if (rawCurrent[order[u]][order[v]] > 0) {
                if (u == 0 && v > 0 && k1 == 1) {
                    dxv = rawCurrent[order[u]][order[v]];
                } else {
                    if (Dvk[u][k1].deliveryCurrent > 0.0) {
                        dxv = Dvk[u][k1].deliveryCurrent * rawCurrent[order[u]][order[v]] / currentOut[order[u]];
                    } else {
                        dxv = -1.0;
                    }
                }
            }
            if (dxv > maxdc) {
                maxdc = dxv;
                get_u[0] = u;
            }
        }
        return maxdc;
    }

    /***************************************************************************
     * 语义子图匹配 新实现不调用此函数
     **************************************************************************/
    public void ComputeSGMapping_Informative(ConceptSubGraph SG_source[],
                                             ConceptSubGraph SG_target[]) {
        // MaxGraphIsoCoveringValiente match = new
        // MaxGraphIsoCoveringValiente(null, null);
        int i, j;

        int sourceSize, targetSize, commonSize;

        for (i = 0; i < source_ConceptNum; i++) {
            sourceSize = 0;
            targetSize = 0;
            if (cnptSubG[i] != null) {

                sourceSize = cnptSubG[i].subGraph.vertexSet().size();
                for (j = 0; j < target_ConceptNum; j++) {
                    SimMatrixGraph[i][j] = 0;
                    if (targetSubGraph[j] != null) {

                        targetSize = targetSubGraph[j].subGraph.vertexSet()
                                .size();

                        System.out.println("source:" + source_ConceptName[i]
                                + cnptSubG[i].subGraph.toString());
                        System.out.println("target:" + target_ConceptName[j]
                                + targetSubGraph[j].subGraph.toString());
                        System.out.println("sourceSubGraph[" + i + "]"
                                + "targetSubGraph[" + j + "]");

                        long start = System.currentTimeMillis();// 开始计时

                        System.out.println("此次结构匹配算法时间："
                                + (double) (System.currentTimeMillis() - start)
                                / 1000. + "秒");

                        SimMatrixGraph[i][j] = GetBlondeSimilarity(
                                source_ConceptName[i], target_ConceptName[j],
                                cnptSubG[i].subGraph,
                                targetSubGraph[j].subGraph);
                        // SimMatrixGraph[i][j] =
                        // (float)commonSize/(float)(sourceSize+targetSize-commonSize);
                        System.out.println("SimMatrixGraph[" + i + "][" + j
                                + "]" + SimMatrixGraph[i][j] + "\n");
                    }
                }
            }
        }

        // SimMatrixGraph = this.RemoveSmallSim(
        // SimMatrixGraph,source_ConceptNum,target_ConceptNum);
        // try mutual information method
        // SimMatrixGraph =
        // this.GetMutualInformation(SimMatrixGraph,source_ConceptNum,target_ConceptNum);

        // BlondelMatrixSimAlgorithm BMSim = new BlondelMatrixSimAlgorithm();
        // SimMatrixGraph =
        // BMSim.GetSelectMatching(SimMatrixGraph,source_ConceptNum,
        // target_ConceptNum);
        (new StableMarriageFilter()).run(SimMatrixGraph, source_ConceptNum,
                target_ConceptNum);

        System.out.println("done");
    }

    /***************************************************************************
     * 子图匹配方法： 经典的Blonde图中点相似算法入口
     **************************************************************************/
    public double GetBlondeSimilarity(String iName, String jName,
                                      DirectedGraph soureG, DirectedGraph targetG) {
        int i, j, count;
        Iterator itr;
        int nA, nB;
        int sourcePos, targetPos;
        double sim;
        String[] SName = new String[MAX_DISPSUBGRAPH_SIZE + 120];
        String[] TName = new String[MAX_DISPSUBGRAPH_SIZE + 120];

        BlondelMatrixSimAlgorithm BMSim = new BlondelMatrixSimAlgorithm();

        // initinalize the basic params
        nA = soureG.vertexSet().size();
        nB = targetG.vertexSet().size();

        // get source subgraph vertex names
        itr = soureG.vertexSet().iterator();
        count = 0;
        sourcePos = 0;
        while (itr.hasNext()) {
            SName[count] = itr.next().toString();
            // get iPos
            if (SName[count].equals(iName)) {
                sourcePos = count;
            }
            count++;
        }

        // get target subgraph vertex names
        itr = targetG.vertexSet().iterator();
        count = 0;
        targetPos = 0;
        while (itr.hasNext()) {
            TName[count] = itr.next().toString();
            // get jPos
            if (TName[count].equals(jName)) {
                targetPos = count;
            }
            count++;
        }

        // initinalize arrayA
        double[][] arrayA = new double[nA][nA];
        for (i = 0; i < nA; i++)
            for (j = 0; j < nA; j++) {
                if (soureG.containsEdge(SName[i], SName[j])) {
                    arrayA[i][j] = 1.0;
                } else {
                    arrayA[i][j] = 0.0;
                }
            }

        // initinalize arrayB
        double[][] arrayB = new double[nB][nB];
        for (i = 0; i < nB; i++)
            for (j = 0; j < nB; j++) {
                if (targetG.containsEdge(TName[i], TName[j])) {
                    arrayB[i][j] = 1.0;
                } else {
                    arrayB[i][j] = 0.0;
                }
            }

        // initinalize similarity matrix
        double[][] SimMatrix = new double[nB][nA];

        SimMatrix = BMSim.solve(nA, nB, arrayA, arrayB,
                MAX_DISPSUBGRAPH_ITER_TIMES);
        // SimMatrix = TurnSim(SimMatrix,nB,nA);

        // SimMatrix = this.RemoveSmallSim( SimMatrix,nB,nA);
        // SimMatrix = this.RefineSim( SimMatrix,nB,nA);

        // try mutual information method
        // SimMatrix = this.GetMutualInformation(SimMatrix,nB,nA);
        // SimMatrix = BMSim.GetSelectMatchingRaw(SimMatrix,nB,nA);
        // SimMatrix = BMSim.GetSelectMatching(SimMatrix,nB,nA);

        // SimMatrix = this.RemoveSmallSim(SimMatrix,nB,nA);
        // SimMatrix = this.TurnSim(SimMatrix,nB,nA);
        // (new SMSelector()).run(SimMatrix,nB,nA);

        sim = SimMatrix[targetPos][sourcePos];

        // sim = 1.0/(1.0+Math.exp(-10.0*sim));

        return sim;
    }

    /***************************************************************************
     * 全图上的相似度匹配 采用Blonde算法
     **************************************************************************/
    public void ComputeSGMapping_WholeGraph() {
        int i, j, k;
        double[][] sim = new double[1000][1000];
        int iPos = 0, jPos = 0;
        double max = 0;

        sim = GetBlondeSimilarity_WholeGraph(source_Graph, target_Graph);

        for (i = 0; i < source_ConceptNum; i++) {
            for (k = 0; k < sourceVertexNum; k++) {
                if (sourceVertexName[k].equals(source_ConceptName[i])) {
                    iPos = k;
                    break;
                }
            }

            for (j = 0; j < target_ConceptNum; j++) {
                for (k = 0; k < targetVertexNum; k++) {
                    if (targetVertexName[k].equals(target_ConceptName[j])) {
                        jPos = k;
                        break;
                    }
                }

                SimMatrixGraph[i][j] = sim[jPos][iPos];
                // SimMatrixGraph[i][j]=1.0/(1.0+Math.exp(-1.0*sim[jPos][iPos]
                // ));
            }
        }

        // remove too small similarity values
        // SimMatrixGraph = this.RemoveSmallSim(
        // SimMatrixGraph,source_ConceptNum,target_ConceptNum);
        // try mutual information method
        // SimMatrixGraph =
        // this.GetMuturalInformation(SimMatrixGraph,source_ConceptNum,target_ConceptNum);

        System.out.println("Begin selecting");
        long start = System.currentTimeMillis();// 开始计时
        BlondelMatrixSimAlgorithm BMSim = new BlondelMatrixSimAlgorithm();
        // SimMatrixGraph =
        // BMSim.GetSelectMatching(SimMatrixGraph,source_ConceptNum,target_ConceptNum);
        // BMSim.GetSelectMatching2(SimMatrixGraph,source_ConceptNum,target_ConceptNum);
        (new StableMarriageFilter()).run(SimMatrixGraph, source_ConceptNum,
                target_ConceptNum);
        long end = System.currentTimeMillis();// 结束计时
        long costtime = end - start;// 统计算法时间
        System.out.println("选择结果时间：" + (double) costtime / 1000. + "秒");
        System.out.println("End selecting");

    }

    /***************************************************************************
     * 转化为二部图，然后再做Blonde算法匹配
     **************************************************************************/
    public void ComputeSGMapping_BiptGraph() {
        int i, j, k;
        double[][] sim = new double[1000][1000];
        int iPos = 0, jPos = 0;
        double max = 0;

        // sim =
        // GetBlondeSimilarity_WholeGraph_Block(source_Graph,target_Graph);
        // sim = GetBlondeSimilarity_WholeGraph(source_Graph,target_Graph);
        SimMatrixGraph = this.GetBlondeSimilarity_WholeGraph_GMOBlock(
                source_Graph, target_Graph);

        System.out.println("Begin selecting");
        long start = System.currentTimeMillis();// 开始计时
        // 老的选择方法
        // BlondelMatrixSimAlgorithm BMSim = new BlondelMatrixSimAlgorithm();
        // SimMatrixGraph =
        // BMSim.GetSelectMatching(SimMatrixGraph,source_ConceptNum,target_ConceptNum);

        // 新的选择方法：Stable Marriage方法
        (new StableMarriageFilter()).run(SimMatrixGraph, source_ConceptNum,
                target_ConceptNum);

        long end = System.currentTimeMillis();// 结束计时
        long costtime = end - start;// 统计算法时间
        System.out.println("选择结果时间：" + (double) costtime / 1000. + "秒");
        System.out.println("End selecting");
    }

    /***************************************************************************
     * 带分块的Blonde匹配算法
     **************************************************************************/
    public void ComputeSGMapping_WholeGraph_Block() {
        int i, j, k;
        double[][] sim = new double[1000][1000];
        int iPos = 0, jPos = 0;

        sim = GetBlondeSimilarity_WholeGraph_Block(source_Graph, target_Graph);

        for (i = 0; i < source_ConceptNum; i++) {
            for (k = 0; k < sourceVertexNum; k++) {
                if (sourceVertexName[k].equals(source_ConceptName[i])) {
                    iPos = k;
                    break;
                }
            }

            for (j = 0; j < target_ConceptNum; j++) {
                for (k = 0; k < targetVertexNum; k++) {
                    if (targetVertexName[k].equals(target_ConceptName[j])) {
                        jPos = k;
                        break;
                    }
                }

                SimMatrixGraph[i][j] = sim[jPos][iPos];

                // if (SimMatrixGraph[i][j]>max) {max=SimMatrixGraph[i][j];}

                // System.out.println("source:"+source_ConceptName[i]+"---"+sourceVertexName[iPos]);
                // System.out.println("target:"+target_ConceptName[j]+"---"+targetVertexName[jPos]);

                // long start = System.currentTimeMillis();//开始计时
                //
                // System.out.println("此次结构匹配算法时间："+(double)(System.currentTimeMillis()-start)/1000.+"秒");

                // SimMatrixGraph[i][j] =
                // (float)commonSize/(float)(sourceSize+targetSize-commonSize);
                // System.out.println("SimMatrixGraph["+i+"]["+j+"]"+SimMatrixGraph[i][j]+"\n");
            }
        }

        // try mutual information method
        // SimMatrixGraph =
        // this.GetMuturalInformation(SimMatrixGraph,source_ConceptNum,target_ConceptNum);

        System.out.println("Begin selecting");
        long start = System.currentTimeMillis();// 开始计时
        BlondelMatrixSimAlgorithm BMSim = new BlondelMatrixSimAlgorithm();
        // SimMatrixGraph =
        // BMSim.GetSelectMatching(SimMatrixGraph,source_ConceptNum,target_ConceptNum);
        // BMSim.GetSelectMatching2(SimMatrixGraph,source_ConceptNum,target_ConceptNum);
        (new StableMarriageFilter()).run(SimMatrixGraph, source_ConceptNum,
                target_ConceptNum);
        long end = System.currentTimeMillis();// 结束计时
        long costtime = end - start;// 统计算法时间
        System.out.println("选择结果时间：" + (double) costtime / 1000. + "秒");
        System.out.println("End selecting");

    }

    /***************************************************************************
     * 不带分块的Blonde匹配算法
     **************************************************************************/
    public double[][] GetBlondeSimilarity_WholeGraph(DirectedGraph soureG,
                                                     DirectedGraph targetG) {
        int i, j, count;
        Iterator itr;
        int nA, nB;
        String[] SName = new String[5000];
        String[] TName = new String[5000];

        BlondelMatrixSimAlgorithm BMSim = new BlondelMatrixSimAlgorithm();

        // initinalize the basic params
        nA = soureG.vertexSet().size();
        nB = targetG.vertexSet().size();

        // get source subgraph vertex names
        itr = soureG.vertexSet().iterator();
        count = 0;
        while (itr.hasNext()) {
            SName[count] = itr.next().toString();
            count++;
        }

        // get target subgraph vertex names
        itr = targetG.vertexSet().iterator();
        count = 0;
        while (itr.hasNext()) {
            TName[count] = itr.next().toString();
            count++;
        }

        // initinalize arrayA
        double[][] arrayA = new double[nA][nA];
        for (i = 0; i < nA; i++)
            for (j = 0; j < nA; j++) {
                if (soureG.containsEdge(SName[i], SName[j])) {
                    arrayA[i][j] = 1.0;
                } else {
                    arrayA[i][j] = 0.0;
                }
            }

        // initinalize arrayB
        double[][] arrayB = new double[nB][nB];
        for (i = 0; i < nB; i++)
            for (j = 0; j < nB; j++) {
                if (targetG.containsEdge(TName[i], TName[j])) {
                    arrayB[i][j] = 1.0;
                } else {
                    arrayB[i][j] = 0.0;
                }
            }

        // initinalize similarity matrix
        double[][] SimMatrix = new double[nB][nA];

        SimMatrix = BMSim.solve(nA, nB, arrayA, arrayB,
                MAX_DISPSUBGRAPH_ITER_TIMES);

        // mutural information
        // SimMatrix = this.GetMuturalInformation( SimMatrix,nB,nA);

        // SimMatrix = TurnSim(SimMatrix,nB,nA);
        // SimMatrix = BMSim.GetSelectMatching(SimMatrix,nB,nA);

        return SimMatrix;
    }

    /***************************************************************************
     * 图分块
     **************************************************************************/
    public double[][] GetBlondeSimilarity_WholeGraph_Block(
            DirectedGraph soureG, DirectedGraph targetG) {
        int i, j, k, count;
        Iterator itr;
        int nA, nB;
        String[] SName = new String[5000];
        String[] TName = new String[5000];
        Set sourceCPset = new HashSet();
        Set targetCPset = new HashSet();

        BlondelMatrixSimAlgorithm BMSim = new BlondelMatrixSimAlgorithm();

        // initinalize the basic params
        nA = soureG.vertexSet().size();
        nB = targetG.vertexSet().size();

        // get source subgraph vertex names
        itr = soureG.vertexSet().iterator();
        count = 0;
        while (itr.hasNext()) {
            SName[count] = itr.next().toString();
            count++;
        }

        // get target subgraph vertex names
        itr = targetG.vertexSet().iterator();
        count = 0;
        while (itr.hasNext()) {
            TName[count] = itr.next().toString();
            count++;
        }

        // construct the set position of concepts
        for (i = 0; i < source_ConceptNum; i++) {
            for (k = 0; k < nA; k++) {
                if (SName[k].equals(source_ConceptName[i])) {
                    sourceCPset.add(k);
                    break;
                }
            }
        }

        for (i = 0; i < target_ConceptNum; i++) {
            for (k = 0; k < nB; k++) {
                if (TName[k].equals(target_ConceptName[i])) {
                    targetCPset.add(k);
                    break;
                }
            }
        }

        // initinalize arrayA
        double[][] arrayA = new double[nA][nA];
        for (i = 0; i < nA; i++)
            for (j = 0; j < nA; j++) {
                if (soureG.containsEdge(SName[i], SName[j])) {
                    arrayA[i][j] = 1.0;
                } else {
                    arrayA[i][j] = 0.0;
                }
            }

        // initinalize arrayB
        double[][] arrayB = new double[nB][nB];
        for (i = 0; i < nB; i++)
            for (j = 0; j < nB; j++) {
                if (targetG.containsEdge(TName[i], TName[j])) {
                    arrayB[i][j] = 1.0;
                } else {
                    arrayB[i][j] = 0.0;
                }
            }

        // initinalize similarity matrix
        double[][] SimMatrix = new double[nB][nA];

        SimMatrix = BMSim.solve_block(nA, nB, arrayA, arrayB, sourceCPset,
                targetCPset, MAX_DISPSUBGRAPH_ITER_TIMES);
        // SimMatrix = TurnSim(SimMatrix,nB,nA);

        return SimMatrix;
    }

    /***************************************************************************
     * GMO分块，然后用Blondel算法
     **************************************************************************/
    public double[][] GetBlondeSimilarity_WholeGraph_GMOBlock(
            DirectedGraph sourceG, DirectedGraph targetG) {
        int i, j, k, count;
        String str1, str2;
        int nA, nB;// 两个图中的Vertex总数
        int nExA, nOtA, nStA;// A中三类Vertex数目
        int nExB, nOtB, nStB;// B中三类Vertex数目
        BlondelMatrixSimAlgorithm BMSim = new BlondelMatrixSimAlgorithm();

        // ***************对Source Graph,构造各个分块矩阵*********************

        // 获得三类的点数
        nExA = sourceExset.size();
        nOtA = sourceOtset.size();
        nStA = sourceStset.size();

        // ********1.构造Ex--St-->AEs
        // 初始化矩阵AEs
        double[][] AEs = new double[nExA][nStA];
        i = 0;
        for (Iterator Iti = sourceExset.iterator(); Iti.hasNext(); ) {
            str1 = (String) Iti.next();
            j = 0;
            for (Iterator Itj = sourceStset.iterator(); Itj.hasNext(); ) {
                str2 = (String) Itj.next();
                if (sourceG.containsEdge(str1, str2)) {
                    AEs[i][j] = 1.0;
                } else {
                    AEs[i][j] = 0.0;
                }
                j++;
            }
            i++;
        }

        // ********2.构造Ot--St-->As
        // 初始化矩阵As
        double[][] As = new double[nOtA][nStA];
        i = 0;
        for (Iterator Iti = sourceOtset.iterator(); Iti.hasNext(); ) {
            str1 = (String) Iti.next();
            j = 0;
            for (Iterator Itj = sourceStset.iterator(); Itj.hasNext(); ) {
                str2 = (String) Itj.next();
                if (sourceG.containsEdge(str1, str2)) {
                    As[i][j] = 1.0;
                } else {
                    As[i][j] = 0.0;
                }
                j++;
            }
            i++;
        }

        // ********3.构造St--Ex-->AE
        // 初始化矩阵AE
        double[][] AE = new double[nStA][nExA];
        i = 0;
        for (Iterator Iti = sourceStset.iterator(); Iti.hasNext(); ) {
            str1 = (String) Iti.next();
            j = 0;
            for (Iterator Itj = sourceExset.iterator(); Itj.hasNext(); ) {
                str2 = (String) Itj.next();
                if (sourceG.containsEdge(str1, str2)) {
                    AE[i][j] = 1.0;
                } else {
                    AE[i][j] = 0.0;
                }
                j++;
            }
            i++;
        }

        // ********4.构造St--Ot-->Aop
        // 初始化矩阵Aop
        double[][] Aop = new double[nStA][nOtA];
        i = 0;
        for (Iterator Iti = sourceStset.iterator(); Iti.hasNext(); ) {
            str1 = (String) Iti.next();
            j = 0;
            for (Iterator Itj = sourceOtset.iterator(); Itj.hasNext(); ) {
                str2 = (String) Itj.next();
                if (sourceG.containsEdge(str1, str2)) {
                    Aop[i][j] = 1.0;
                } else {
                    Aop[i][j] = 0.0;
                }
                j++;
            }
            i++;
        }

        System.out.println("A点集构造完成");

        // ***************对Target Graph,构造各个分块矩阵*********************

        // 获得三类的点数
        nExB = targetExset.size();
        nOtB = targetOtset.size();
        nStB = targetStset.size();

        // ********1.构造Ex--St-->BEs
        // 初始化矩阵BEs
        double[][] BEs = new double[nExB][nStB];
        i = 0;
        for (Iterator Iti = targetExset.iterator(); Iti.hasNext(); ) {
            str1 = (String) Iti.next();
            j = 0;
            for (Iterator Itj = targetStset.iterator(); Itj.hasNext(); ) {
                str2 = (String) Itj.next();
                if (targetG.containsEdge(str1, str2)) {
                    BEs[i][j] = 1.0;
                } else {
                    BEs[i][j] = 0.0;
                }
                j++;
            }
            i++;
        }

        // ********2.构造Ot--St-->Bs
        // 初始化矩阵Bs
        double[][] Bs = new double[nOtB][nStB];
        i = 0;
        for (Iterator Iti = targetOtset.iterator(); Iti.hasNext(); ) {
            str1 = (String) Iti.next();
            j = 0;
            for (Iterator Itj = targetStset.iterator(); Itj.hasNext(); ) {
                str2 = (String) Itj.next();
                if (targetG.containsEdge(str1, str2)) {
                    Bs[i][j] = 1.0;
                } else {
                    Bs[i][j] = 0.0;
                }
                j++;
            }
            i++;
        }

        // ********3.构造St--Ex-->BE
        // 初始化矩阵BE
        double[][] BE = new double[nStB][nExB];
        i = 0;
        for (Iterator Iti = targetStset.iterator(); Iti.hasNext(); ) {
            str1 = (String) Iti.next();
            j = 0;
            for (Iterator Itj = targetExset.iterator(); Itj.hasNext(); ) {
                str2 = (String) Itj.next();
                if (targetG.containsEdge(str1, str2)) {
                    BE[i][j] = 1.0;
                } else {
                    BE[i][j] = 0.0;
                }
                j++;
            }
            i++;
        }

        // ********4.构造St--Ot-->Bop
        // 初始化矩阵Bop
        double[][] Bop = new double[nStB][nOtB];
        i = 0;
        for (Iterator Iti = targetStset.iterator(); Iti.hasNext(); ) {
            str1 = (String) Iti.next();
            j = 0;
            for (Iterator Itj = targetOtset.iterator(); Itj.hasNext(); ) {
                str2 = (String) Itj.next();
                if (targetG.containsEdge(str1, str2)) {
                    Bop[i][j] = 1.0;
                } else {
                    Bop[i][j] = 0.0;
                }
                j++;
            }
            i++;
        }

        System.out.println("B点集构造完成");

        // *******构造矩阵EBA*************
        double[][] EBA = new double[nExB][nExA];
        i = 0;
        for (Iterator Iti = targetExset.iterator(); Iti.hasNext(); ) {
            str1 = (String) Iti.next();
            j = 0;
            for (Iterator Itj = targetExset.iterator(); Itj.hasNext(); ) {
                str2 = (String) Itj.next();
                if (str1.equals(str2)) {
                    EBA[i][j] = 1.0;
                } else {
                    EBA[i][j] = 0.0;
                }
                j++;
            }
            i++;
        }

        // initinalize similarity matrix
        double[][] sim = new double[nStB][nStA];

        sim = BMSim.solve_GMO(nExA, nOtA, nStA, AEs, As, AE, Aop, nExB, nOtB,
                nStB, BEs, Bs, BE, Bop, EBA, MAX_DISPSUBGRAPH_ITER_TIMES);

        // 转换为concept--concept的相似矩阵
        double[][] simMatrix = new double[source_ConceptNum][target_ConceptNum];
        int iPos = 0, jPos = 0;
        for (i = 0; i < source_ConceptNum; i++) {
            k = 0;
            iPos = 0;
            for (Iterator Itj = sourceOtset.iterator(); Itj.hasNext(); ) {
                str1 = (String) Itj.next();
                if (str1.equals(source_ConceptName[i])) {
                    iPos = k;
                    break;
                }

                k++;
            }

            for (j = 0; j < target_ConceptNum; j++) {

                k = 0;
                jPos = 0;
                for (Iterator Itj = targetOtset.iterator(); Itj.hasNext(); ) {
                    str1 = (String) Itj.next();
                    if (str1.equals(target_ConceptName[j])) {
                        jPos = k;
                        break;
                    }

                    k++;
                }
                simMatrix[i][j] = sim[jPos][iPos];
            }
        }
        return simMatrix;
    }

    /***************************************************************************
     * 相似矩阵值调整
     **************************************************************************/
    public double[][] TurnSim(double[][] sim, int nA, int nB) {
        int i, j;
        int num = 0;
        double sum = 0;
        double avg = 0;
        double max = 0;

        for (i = 0; i < nA; i++) {
            for (j = 0; j < nB; j++) {
                max = Math.max(max, sim[i][j]);
                if (sim[i][j] > 0) {
                    num++;
                    sum += sim[i][j];
                }
            }
        }

        if (num > 0) {
            avg = sum / num;
        }

        for (i = 0; i < nA; i++) {
            for (j = 0; j < nB; j++) {
                if (sim[i][j] > 0) {
                    sim[i][j] = sim[i][j] * (avg / max)
                            * ((double) num / (double) (nA + nB - num));
                    // sim[i][j]=sim[i][j]*((double)num/(double)(nA+nB-num));
                }
            }
        }

        return sim;
    }

    /***************************************************************************
     * remove the too small value in the similarity matrix
     **************************************************************************/
    public double[][] RemoveSmallSim(double[][] sim, int nA, int nB) {
        int i, j;
        double max = 0;
        double pera = 1 / (double) (Math.max(nA, nB) * Math.max(nA, nB));
        double threshold;

        for (i = 0; i < nA; i++) {
            for (j = 0; j < nB; j++) {
                if (sim[i][j] > max) {
                    max = sim[i][j];
                }
            }
        }

        threshold = max * pera;

        for (i = 0; i < nA; i++) {
            for (j = 0; j < nB; j++) {
                if (sim[i][j] < threshold) {
                    sim[i][j] = 0;
                }
            }
        }
        return sim;
    }

    /***************************************************************************
     * 相似矩阵的MutualInformation
     **************************************************************************/
    public double[][] GetMutualInformation(double[][] sim, int nA, int nB) {
        int i, j, k;
        double px, py;
        double[][] tempsim = new double[nA][nB];

        for (i = 0; i < nA; i++) {
            px = 0;
            for (k = 0; k < nB; k++)
                px += sim[i][k];
            for (j = 0; j < nB; j++) {
                py = 0;
                for (k = 0; k < nA; k++)
                    py += sim[k][j];
                if (px * py * sim[i][j] == 0) {
                    tempsim[i][j] = 0;
                } else {
                    tempsim[i][j] = sim[i][j]
                            * Math.log((sim[i][j]) / (px * py));

                    tempsim[i][j] = 1 / (1 + Math.exp(-5.0 * tempsim[i][j]));
                }
            }
        }
        return tempsim;
    }

    /***************************************************************************
     * 相似矩阵Refine
     **************************************************************************/
    public double[][] RefineSim(double[][] sim, int nA, int nB) {
        int i, j, k;
        double[] px = new double[nA];
        double[] py = new double[nB];
        int[] nx = new int[nA];
        int[] ny = new int[nB];
        double[][] tempsim = new double[nA][nB];

        for (i = 0; i < nA; i++) {
            for (j = 0; j < nB; j++) {
                px[i] = px[i] + sim[i][j];
                if (sim[i][j] > 0) {
                    nx[i] = nx[i] + 1;
                }
            }
        }

        for (j = 0; j < nB; j++) {
            for (i = 0; i < nA; i++) {
                py[j] = py[j] + sim[i][j];
                if (sim[i][j] > 0) {
                    ny[j] = ny[j] + 1;
                }
            }
        }

        for (i = 0; i < nA; i++) {
            for (j = 0; j < nB; j++) {
                if (sim[i][j] > 0) {
                    tempsim[i][j] = sim[i][j] + (sim[i][j] - px[i] / nB)
                            + (sim[i][j] - py[j] / nA);
                    tempsim[i][j] = 0.5 * (tempsim[i][j]
                            * ((nB - nx[i] + 1.0) / (double) nB) + tempsim[i][j]
                            * ((double) (nA - ny[j] + 1) / (double) nA));
                } else {
                    tempsim[i][j] = 0;
                }
            }
        }

        return tempsim;
    }

    /***************************************************************************
     * 重构本体Informative graph的主入口
     **************************************************************************/
    public ArrayList consInfSubOnt(ArrayList paraList) {
        ArrayList result = new ArrayList();

        // 1.解析参数
        unPackOntPara(paraList);

        loadConfigFile();

        // 2.本体预处理
//		System.out.println("    本体预处理");
        preProcessOnt();
        // 3.重构本体成分的informativ graph
        // (1)图模型的建立
//		System.out.println("    图模型的建立");
        orgnGraph = ont2Graph();

        // 计算资源权重
        getResWeight();
        // 计算电导矩阵
        conductanceMatrix = new double[graphVNum][graphVNum];
        conductanceMatrix = getConductMatrix(graphVNum, orgnGraph, graphVName);
        // (2)概念语义子图的抽取
//		System.out.println("    概念语义子图的抽取");
        reConsCnptSubOnt();
        // (3)属性语义子图的抽取
//		System.out.println("    属性语义子图的抽取");
        reConsPropSubOnt();

        result.add(0, cnptSubG);
        result.add(1, propSubG);
        result.add(2, m);
        result.add(3, new ArrayList(Arrays.asList(graphTrp)));// 整个图的三元组表示

        return result;
    }

    /***************************************************************************
     * 接收本体参数 初始化OntGraph
     **************************************************************************/
    public void unPackOntPara(ArrayList paraList) {
        m = (OntModel) paraList.get(0);
        conceptNum = ((Integer) paraList.get(1)).intValue();
        propNum = ((Integer) paraList.get(2)).intValue();
        dataPropNum = ((Integer) paraList.get(3)).intValue();
        objPropNum = ((Integer) paraList.get(4)).intValue();
        insNum = ((Integer) paraList.get(5)).intValue();

        fullConceptNum = ((Integer) paraList.get(12)).intValue();
        fullPropNum = ((Integer) paraList.get(13)).intValue();
        fullDataPropNum = ((Integer) paraList.get(14)).intValue();
        fullObjPropNum = ((Integer) paraList.get(15)).intValue();
        fullInsNum = ((Integer) paraList.get(16)).intValue();

        // 根据得到的number初始化各种数组
        initPara();
        conceptName = (String[]) (paraList.get(6));
        propName = (String[]) (paraList.get(7));
        dataPropName = (String[]) (paraList.get(8));
        objPropName = (String[]) (paraList.get(9));
        insName = (String[]) (paraList.get(10));
        baseURI = (String) paraList.get(11);

        fullConceptName = (OntClass[]) (paraList.get(17));
        fullPropName = (OntProperty[]) (paraList.get(18));
        fullDataPropName = (DatatypeProperty[]) (paraList.get(19));
        fullObjPropName = (ObjectProperty[]) (paraList.get(20));
        fullInsName = (Individual[]) (paraList.get(21));

        anonCnpt = (ArrayList) (paraList.get(22));
        anonProp = (ArrayList) (paraList.get(23));
        anonIns = (ArrayList) (paraList.get(24));
    }

    /***************************************************************************
     * 初始化本体的一些数据结构
     **************************************************************************/
    public void initPara() {
        conceptName = new String[conceptNum];
        propName = new String[propNum];
        dataPropName = new String[dataPropNum];
        objPropName = new String[objPropNum];
        insName = new String[insNum];
        fullConceptName = new OntClass[fullConceptNum];
        fullPropName = new OntProperty[fullPropNum];
        fullDataPropName = new DatatypeProperty[fullDataPropNum];
        fullObjPropName = new ObjectProperty[fullObjPropNum];
        fullInsName = new Individual[fullInsNum];

        trpNum = 0;
        multiEdgeList = new ArrayList();
        graphME = new ArrayList();
        LARGE_GRAPH = 4000;

        ontParse = new OWLOntParse();

        // 获得本体元语信息
        getOntMetaInfo();
        cWeight = new ConceptWt[fullConceptNum];
        pWeight = new PropertyWt[fullPropNum];
        iWeight = new InstanceWt[fullInsNum];
        ontLngMetaNum = ontLngMetaSet.size();
        metaWeight = new OntLngMetaWt[ontLngMetaNum];
        ontLngMetaName = new String[ontLngMetaNum];
        ontLngMetaName = (String[]) ontLngMetaSet.toArray(new String[0]);

        // 本体子图
        cnptSubG = new ConceptSubGraph[conceptNum];
        propSubG = new PropertySubGraph[propNum];
        typeVertexName = new ArrayList();
        typeVertexFullName = new ArrayList();

    }

    /***************************************************************************
     * 处理初步过滤的三元组，得到能构成混合模型的三元组
     **************************************************************************/
    public void trp4MultiGraph(ArrayList oldList) {
        int stNum = 0;

        // 原List转换为数组，方便根据下标进行遍历和记录
        int oldStNum = oldList.size();
        Statement[] oldSt = (Statement[]) oldList.toArray(new Statement[0]);

        // 提取出两个端点，辅助判断
        String[] vName = new String[oldStNum];
        for (int i = 0; i < oldStNum; i++) {
            Resource s = oldSt[i].getSubject();
            Property p = oldSt[i].getPredicate();
            RDFNode o = oldSt[i].getObject();
            // 把端点信息放在辅助数组中
            vName[i] = s.toString() + o.toString();
        }

        // 辅助判断已经处理过的端点的集合
        Set typeSet = new HashSet();

        // 遍历辅助数组，判断多重边出现的位置
        for (int i = 0; i < oldStNum - 1; i++) {
            // 如果这种类型还没有被处理过
            if (!typeSet.contains(vName[i])) {
                // 记录该类型已经被处理
                typeSet.add(vName[i]);
                // 找到所有的这类类型，并进行标记
                boolean isM = false;
                for (int j = i + 1; j < oldStNum; j++) {
                    if (vName[i].equals(vName[j])) {
                        // 如果出现重边，则记录下来
                        // 放入multiEdgeList
                        multiEdgeList.add(oldSt[j]);
                        isM = true;
                    }
                }
				/* 如果是多重边，将i也加入 */
                if (isM) {
                    multiEdgeList.add(oldSt[i]);
                }
            }
        }
    }

    /***************************************************************************
     * 重构概念的子本体
     **************************************************************************/
    public void reConsCnptSubOnt() {
        boolean largeOnt;
        // 判断是否大本体
        largeOnt = isLargeOnt();
        // 处理大本体
        if (largeOnt) {
            dealLargeOnt();
        }
        // 解方程得到语义子图
        //cnptSubG = gainCnptSubGraph();
        cnptSubG = gainCnptSubGraphQuick();
    }

    /***************************************************************************
     * 重构属性的子本体
     **************************************************************************/
    public void reConsPropSubOnt() {
        boolean largeOnt;
        // 判断是否大本体
        largeOnt = isLargeOnt();
        // 计算资源权重
        // 当不处理大本体的时候，资源权重的计算在概念子本体抽取中已经计算
        // getResWeight();
        // 处理大本体
        if (largeOnt) {
            dealLargeOnt();
        }
        // 解方程得到语义子图
        //propSubG = gainPropSubGraph();
        propSubG = gainPropSubGraphQuick();
    }

    /***************************************************************************
     * 判断当前的图是否大本体
     **************************************************************************/
    public boolean isLargeOnt() {
        // 边和点的权重
        double a = 0.8;
        double b = 0.9;
        double scale = a * orgnGraph.edgeSet().size() + b
                * orgnGraph.vertexSet().size();
        return (scale > LARGE_GRAPH);
    }

    /***************************************************************************
     * 根据指定的概念，处理大本体，缩小本体规模
     **************************************************************************/
    public void dealLargeOnt() {

    }

    /***************************************************************************
     * 获得本体的元语信息
     **************************************************************************/
    public void getOntMetaInfo() {
        ontLngURI = ontParse.getOntLngURI();
        ontLngMetaSet = ontParse.getOntLngMeta();
    }

    /***************************************************************************
     * 从构成图的三元组中确定P 这个步骤会影响系统的效率
     **************************************************************************/
    public ArrayList findPFromGraphTriple(String sStr, String oStr) {
        ArrayList result = new ArrayList();
        Property pr = null;
        Statement stm = null;
        for (int i = 0; i < trpNum; i++) {
            Statement st = graphTrp[i];
            Resource r = st.getSubject();
            Property p = st.getPredicate();
            RDFNode o = st.getObject();
            if (r.toString().equals(sStr) && o.toString().equals(oStr)) {
                pr = p;
                stm = st;
                break;
            }
        }
        result.add(0, pr);
        result.add(1, stm);
        return result;
    }

    /***************************************************************************
     * 从构成图的三元组中确定S
     **************************************************************************/
    public Resource findSFromGraphTriple(String sStr) {
        Resource sub = null;
        for (int i = 0; i < trpNum; i++) {
            Statement st = graphTrp[i];
            Resource r = st.getSubject();
            // Property p=st.getPredicate();
            // RDFNode o=st.getObject();
            if (r.toString().equals(sStr)) {
                sub = r;
                break;
            }
        }
        return sub;
    }

    /***************************************************************************
     * 通过fullName查询Property
     **************************************************************************/
    public OntProperty findPropByFullName(String name) {
        OntProperty x = null;
        for (int i = 0; i < fullPropNum; i++) {
            String str = null;
            if (fullPropName[i].isAnon()) {
                str = fullPropName[i].toString();
            } else {
                str = fullPropName[i].toString();
            }
            if (str.equals(name)) {
                x = fullPropName[i];
                break;
            }
        }
        return x;
    }

    /***************************************************************************
     * 通过LocalName查询Property
     **************************************************************************/
    public OntProperty findPropByLocalName(String name) {
        OntProperty x = null;
        for (int i = 0; i < fullPropNum; i++) {
            String str = null;
            if (fullPropName[i].isAnon()) {
                str = fullPropName[i].toString();
            } else {
                str = fullPropName[i].getLocalName();
            }
            if (str.equals(name)) {
                x = fullPropName[i];
                break;
            }
        }
        return x;
    }

    /***************************************************************************
     * 通过localName查询Instance
     **************************************************************************/
    public Individual findInsByLocalName(String name) {
        Individual x = null;
        for (int i = 0; i < fullInsNum; i++) {
            String str = null;
            if (fullInsName[i].isAnon()) {
                str = fullInsName[i].toString();
            } else {
                str = fullInsName[i].getLocalName();
            }
            if (str.equals(name)) {
                x = fullInsName[i];
                break;
            }
        }
        return x;
    }

    /***************************************************************************
     * 通过localName查询Concept
     **************************************************************************/
    public OntClass findCnptByLocalName(String name) {
        OntClass x = null;
        for (int i = 0; i < fullConceptNum; i++) {
            String str = null;
            if (fullConceptName[i].isAnon()) {
                str = fullConceptName[i].toString();
            } else {
                str = fullConceptName[i].getLocalName();
            }
            if (str.equals(name)) {
                x = fullConceptName[i];
                break;
            }
        }
        return x;
    }

    /***************************************************************************
     * 从构成图的三元组中确定O
     **************************************************************************/
    public RDFNode findOFromGraphTriple(String oStr) {
        RDFNode obj = null;
        for (int i = 0; i < trpNum; i++) {
            Statement st = graphTrp[i];
            RDFNode o = st.getObject();
            if (o.toString().equals(oStr)) {
                obj = o;
                break;
            }
        }
        return obj;
    }

    /***************************************************************************
     * 通过计算电流，获得每个概念的子图
     **************************************************************************/
    public ConceptSubGraph[] gainCnptSubGraph() {
        // 初始化电路中的参数
        voltageMatrix = new double[graphVNum];
        rawCurrent = new double[graphVNum][graphVNum];
        currentOut = new double[graphVNum];

        ConceptSubGraph[] subG = new ConceptSubGraph[conceptNum];

        for (int i = 0; i < conceptNum; i++) {
            if (conceptName[i] == null)
                continue;

            //System.out.println("概念:"+i+"-->"+conceptName[i]);

            // find the position of the concept
            int star = this.findVetexPosInGraph(conceptName[i]);
            int end = graphVNum - 1;

            // recorde the subgraph information
            subG[i] = new ConceptSubGraph();
            subG[i].conceptName = new String(conceptName[i]);
            subG[i].subGraph = new DefaultDirectedGraph(DefaultEdge.class);

/*
            // 计算指定概念的基本子图
            LinearEquation Equation = new LinearEquation();
            // the last node is sink node, it is "sourceVertexNum+1"
            Equation.InitlizePara(graphVNum, star, end);
            // 设定电导矩阵
            Equation.SetConductMatrix(conductanceMatrix);
            // 指定初始电压
            Equation.PrepareMatrixA_b(10.0, 0.0);
            // 解线性方程组
            Equation.Solve();
            // 得到各个点上电压
            voltageMatrix = Equation.GetResultMaxtrix();
*/
            voltageMatrix = JBlasWrapper.JBlasSolver(graphVNum, star, end, conductanceMatrix, 10.0, 0.0);
//			CallLapackpp call = new CallLapackpp();
//			voltageMatrix = call.JBlasSolver( graphVNum,star,end,conductanceMatrix,10.0,0.0);
//			call=null;

            // 图中的电流
            ComputeRawCurrent(graphVNum);
            // 每个点的out current
            ComputeCurrentOut(graphVNum);

            // Displaygeneration启发式算法
            subG[i].subGraph = displayGraphGeneration(graphVNum, graphVName, MAX_DISPSUBGRAPH_SIZE);

            // 根据当前的计算环境来精化子图
            refineSubGraph(subG[i].subGraph);

            // 修补残缺的multiEdge
            mendSubGraphWithMultiEdge(subG[i].subGraph);

            // 抽取出subGraph对应的三元组List
            subG[i].stmList = extractSubGraphTriples(subG[i].subGraph);

            //System.out.println("Display Graph: " + subG[i].subGraph.toString());
            //System.out.println("Sub Graph Size:"+ subG[i].subGraph.vertexSet().size());
            // 显示子图的实际三元组
//			 for (Iterator itxx=subG[i].stmList.iterator();itxx.hasNext();){
//			 System.out.println((Statement)itxx.next());
//			 }

            // 显示和存储得到的子图
            // showAndSaveSubGraph(subG[i].subGraph,graphVName[star]);
        }
        return subG;
    }

    /***************************************************************************
     * 通过计算电流，获得每个概念的子图
     **************************************************************************/
    ConceptSubGraph[] con_subG;

    public ConceptSubGraph[] gainCnptSubGraphQuick() {
        con_subG = new ConceptSubGraph[conceptNum];
        new parfor() {
            public void iter(int thread_idx, int i) {
                if (conceptName[i] == null)
                    return;

                double[] voltageMatrix = new double[graphVNum];
                double[][] rawCurrent = new double[graphVNum][graphVNum];
                double[] currentOut = new double[graphVNum];

                // find the position of the concept
                int star = findVetexPosInGraph(conceptName[i]);
                int end = graphVNum - 1;

                // recorde the subgraph information
                con_subG[i] = new ConceptSubGraph();
                con_subG[i].conceptName = new String(conceptName[i]);
                con_subG[i].subGraph = new DefaultDirectedGraph(DefaultEdge.class);

                // 计算指定概念的基本子图
                // 解线性方程组
                voltageMatrix = JBlasWrapper.JBlasSolver(graphVNum, star, end, conductanceMatrix, 10.0, 0.0);
/*
                LinearEquation Equation = new LinearEquation();
                // the last node is sink node, it is "sourceVertexNum+1"
                Equation.InitlizePara(graphVNum, star, end);
                // 设定电导矩阵
                Equation.SetConductMatrix(conductanceMatrix);
                // 指定初始电压
                Equation.PrepareMatrixA_b(10.0, 0.0);
                // 解线性方程组
                Equation.Solve();
                // 得到各个点上电压
                voltageMatrix = Equation.GetResultMaxtrix();
*/

                // 图中的电流
                rawCurrent = ComputeRawCurrent(graphVNum, voltageMatrix);
                // 每个点的out current
                currentOut = ComputeCurrentOut(graphVNum, rawCurrent);

                // Displaygeneration启发式算法
                con_subG[i].subGraph = displayGraphGeneration(graphVNum, graphVName, MAX_DISPSUBGRAPH_SIZE, voltageMatrix, rawCurrent, currentOut);

                // 根据当前的计算环境来精化子图
                refineSubGraph(con_subG[i].subGraph);

                // 修补残缺的multiEdge
                mendSubGraphWithMultiEdge(con_subG[i].subGraph);

                // 抽取出subGraph对应的三元组List
                con_subG[i].stmList = extractSubGraphTriples(con_subG[i].subGraph);

                //System.out.println("Display Graph: " + subG[i].subGraph.toString());
                //System.out.println("Sub Graph Size:"+ subG[i].subGraph.vertexSet().size());
                // 显示子图的实际三元组
//			 for (Iterator itxx=subG[i].stmList.iterator();itxx.hasNext();){
//			 System.out.println((Statement)itxx.next());
//			 }

                // 显示和存储得到的子图
                // showAndSaveSubGraph(subG[i].subGraph,graphVName[star]);
            }
        }.execute(0, conceptNum);
        return con_subG;
    }

    public ConceptSubGraph gainCnptSubGraphQuick(String cnptName) {
        // 初始化电路中的参数
        voltageMatrix = new double[graphVNum];
        rawCurrent = new double[graphVNum][graphVNum];
        currentOut = new double[graphVNum];

        ConceptSubGraph subG = new ConceptSubGraph();

        // find the position of the concept
        int star = findVetexPosInGraph(cnptName);
        int end = graphVNum - 1;

        // recorde the subgraph information
        subG.conceptName = new String(cnptName);
        subG.subGraph = new DefaultDirectedGraph(DefaultEdge.class);

        // 计算指定概念的基本子图
        // 解线性方程组
//		CallLapackpp call = new CallLapackpp();
//		voltageMatrix = call.JBlasSolver(graphVNum, star, end, conductanceMatrix, 10.0, 0.0);
//		call=null;
        voltageMatrix = JBlasWrapper.JBlasSolver(graphVNum, star, end, conductanceMatrix, 10.0, 0.0);
/*
        LinearEquation Equation = new LinearEquation();
        // the last node is sink node, it is "sourceVertexNum+1"
        Equation.InitlizePara(graphVNum, star, end);
        // 设定电导矩阵
        Equation.SetConductMatrix(conductanceMatrix);
        // 指定初始电压
        Equation.PrepareMatrixA_b(10.0, 0.0);
        // 解线性方程组
        Equation.Solve();
        // 得到各个点上电压
        voltageMatrix = Equation.GetResultMaxtrix();
*/

        // 图中的电流
        ComputeRawCurrent(graphVNum);
        // 每个点的out current
        ComputeCurrentOut(graphVNum);

        // Displaygeneration启发式算法
        subG.subGraph = displayGraphGeneration(graphVNum, graphVName, MAX_DISPSUBGRAPH_SIZE);

        // 根据当前的计算环境来精化子图
        refineSubGraph(subG.subGraph);

        // 修补残缺的multiEdge
        mendSubGraphWithMultiEdge(subG.subGraph);

        // 抽取出subGraph对应的三元组List
        subG.stmList = extractSubGraphTriples(subG.subGraph);

//		System.out.println("Display Graph: " + subG.subGraph.toString());
//		System.out.println("Sub Graph Size:" + subG.subGraph.vertexSet().size());
        return subG;
    }

    /***************************************************************************
     * 修复图中的多重边
     **************************************************************************/
    private void mendSubGraphWithMultiEdge(DirectedGraph g) {
		/* 找到存在的Multi Edge */
        ArrayList subgEdgeList = new ArrayList();
        subgEdgeList.addAll(g.edgeSet());
        Set multEdgeNode = new HashSet();
        for (Iterator it = subgEdgeList.iterator(); it.hasNext(); ) {
            DefaultEdge eg = (DefaultEdge) it.next();
            String sV = g.getEdgeSource(eg).toString();
            String tV = g.getEdgeTarget(eg).toString();
			/* sV,tV是否包含Statement_x */
            if (sV.indexOf("Statement_") != -1
                    || tV.indexOf("Statement_") != -1) {// 包含
                String bgNode;
                if (sV.indexOf("Statement_") != -1) {
                    bgNode = sV;
                } else {
                    bgNode = tV;
                }
				/* 记录子图中出现的多重边标记 */
                multEdgeNode.add(bgNode);
            }
        }

		/* 修补出现的多重边 */
        for (Iterator it = multEdgeNode.iterator(); it.hasNext(); ) {
            String bgNode = (String) it.next();
            for (Iterator jt = graphME.iterator(); jt.hasNext(); ) {
                DefaultEdge eg = (DefaultEdge) jt.next();
                String sV = g.getEdgeSource(eg).toString();
                String tV = g.getEdgeTarget(eg).toString();
                if (sV.equals(bgNode) || tV.equals(bgNode)) {// 找到多重边
                    g.addVertex(sV);
                    g.addVertex(tV);
                    g.addEdge(sV, tV);
                }
            }
        }
    }

    /***************************************************************************
     * 寻找概念再图中节点的位置
     **************************************************************************/
    public int findVetexPosInGraph(String name) {
        int pos = -1;
        for (int i = 0; i < graphVNum; i++) {
            if (graphVName[i].equals(name)) {
                pos = i;
                break;
            }
        }
        return pos;
    }

    /***************************************************************************
     * 显示和存储子图
     **************************************************************************/
    public void showAndSaveSubGraph(DirectedGraph g, String vName) {
        // System.out.println("Star Vertex:" + vName + "\n");
        String[] tempName = new String[g.vertexSet().size()];
        Iterator itr = g.vertexSet().iterator();
        int tcount = 0;
        while (itr.hasNext()) {
            tempName[tcount] = itr.next().toString();
            tcount++;
        }
        this.writeGraphDataForShow(g, tempName);
    }

    /***************************************************************************
     * 子图的精化
     **************************************************************************/
    public void refineSubGraph(DirectedGraph g) {

    }

    /***************************************************************************
     * 通过计算电流，获得每个属性的子图
     **************************************************************************/
    public PropertySubGraph[] gainPropSubGraph() {
        // 初始化电路中的参数
        voltageMatrix = new double[graphVNum];
        rawCurrent = new double[graphVNum][graphVNum];
        currentOut = new double[graphVNum];
        PropertySubGraph[] subG = new PropertySubGraph[propNum];
        for (int i = 0; i < propNum; i++) {
            if (propName[i] == null)
                continue;
//			 System.out.println("属性:"+i+"-->"+propName[i]);
            // find the position of the property
            int star = this.findVetexPosInGraph(propName[i]);
            int end = graphVNum - 1;
            /* 增加边来描述属性 */
            ArrayList newEdge = new ArrayList();
            double[][] newMatrix = new double[graphVNum][graphVNum];
            newMatrix = conductanceMatrix.clone();// 复制原始权重矩阵
            newEdge = addNewPropUseEdge(star, graphVFullName[star],
                    newMatrix);
            // recorde the subgraph information
            subG[i] = new PropertySubGraph();
            subG[i].propName = new String(propName[i]);
            subG[i].subGraph = new DefaultDirectedGraph(DefaultEdge.class);
/*
            // 计算指定属性的基本子图
            LinearEquation Equation = new LinearEquation();
            // the last node is sink node, it is "sourceVertexNum+1"
            Equation.InitlizePara(graphVNum, star, end);
            // 设定电导矩阵
            Equation.SetConductMatrix(newMatrix);
            // 指定初始电压
            Equation.PrepareMatrixA_b(10.0, 0.0);
            // 解线性方程组
            Equation.Solve();
            // 得到各个点上电压
            voltageMatrix = Equation.GetResultMaxtrix();
*/
            voltageMatrix = JBlasWrapper.JBlasSolver(graphVNum, star, end, newMatrix, 10.0, 0.0);
//			CallLapackpp call = new CallLapackpp();
//			voltageMatrix = call.JBlasSolver( graphVNum,star,end,newMatrix,10.0,0.0);
//			call =null;
            // 图中的电流
            ComputeRawCurrent(graphVNum);
            // 每个点的out current
            ComputeCurrentOut(graphVNum);
            // Displaygeneration启发式算法
            subG[i].subGraph = displayGraphGeneration(graphVNum, graphVName,
                    MAX_DISPSUBGRAPH_SIZE);
            // 完善子图
            for (Iterator it = newEdge.iterator(); it.hasNext(); ) {
                DefaultEdge eg = (DefaultEdge) it.next();
                String sV = orgnGraph.getEdgeSource(eg).toString();
                String tV = orgnGraph.getEdgeTarget(eg).toString();
                if (subG[i].subGraph.containsVertex(sV)
                        && !subG[i].subGraph.containsEdge(sV, tV)) {
                    subG[i].subGraph.addVertex(tV);
                    subG[i].subGraph.addEdge(sV, tV);
                }
            }
            // 根据当前的计算环境来精化子图
            refineSubGraph(subG[i].subGraph);
            // 修补残缺的multiEdge
            mendSubGraphWithMultiEdge(subG[i].subGraph);
//			System.out.println("Display Graph: " + subG[i].subGraph.toString());
//			System.out.println("Sub Graph Size:"
//					+ subG[i].subGraph.vertexSet().size());
            // 抽取出subGraph对应的三元组List
            subG[i].stmList = extractSubGraphTriples(subG[i].subGraph);
            // 显示和存储得到的子图
            // showAndSaveSubGraph(subG[i].subGraph,graphVName[star]);
        }
        return subG;
    }

    /***************************************************************************
     * 通过计算电流，获得每个属性的子图
     **************************************************************************/
    private PropertySubGraph[] subG;

    public PropertySubGraph[] gainPropSubGraphQuick() {
        // 初始化电路中的参数
        subG = new PropertySubGraph[propNum];

        new parfor() {
            public void iter(int thread_idx, int i) {
                if (propName[i] == null)
                    return;

                double[] voltageMatrix = new double[graphVNum];
                double[][] rawCurrent = new double[graphVNum][graphVNum];
                double[] currentOut = new double[graphVNum];

                // find the position of the property
                int start = findVetexPosInGraph(propName[i]);
                int end = graphVNum - 1;

                //增加边来描述属性
                double[][] newMatrix = conductanceMatrix.clone(); // 复制原始权重矩阵
                ArrayList newEdge = addNewPropUseEdge(start, graphVFullName[start], newMatrix);

                // recorde the subgraph information
                subG[i] = new PropertySubGraph();
                subG[i].propName = new String(propName[i]);
                subG[i].subGraph = new DefaultDirectedGraph(DefaultEdge.class);

                // 计算指定属性的基本子图

                // 解线性方程组
                voltageMatrix = JBlasWrapper.JBlasSolver(graphVNum, start, end, newMatrix, 10.0, 0.0);
/*
                LinearEquation Equation = new LinearEquation();
                // the last node is sink node, it is "sourceVertexNum+1"
                Equation.InitlizePara(graphVNum, start, end);
                // 设定电导矩阵
                Equation.SetConductMatrix(newMatrix);
                // 指定初始电压
                Equation.PrepareMatrixA_b(10.0, 0.0);
                // 解线性方程组
                Equation.Solve();
                // 得到各个点上电压
                voltageMatrix = Equation.GetResultMaxtrix();
*/

                // 图中的电流
                rawCurrent = ComputeRawCurrent(graphVNum, voltageMatrix);
                // 每个点的out current
                currentOut = ComputeCurrentOut(graphVNum, rawCurrent);

                // Displaygeneration启发式算法
                subG[i].subGraph = displayGraphGeneration(graphVNum, graphVName, MAX_DISPSUBGRAPH_SIZE, voltageMatrix, rawCurrent, currentOut);

                // 完善子图
                for (Iterator it = newEdge.iterator(); it.hasNext(); ) {
                    DefaultEdge eg = (DefaultEdge) it.next();
                    String sV = orgnGraph.getEdgeSource(eg).toString();
                    String tV = orgnGraph.getEdgeTarget(eg).toString();
                    if (subG[i].subGraph.containsVertex(sV) && !subG[i].subGraph.containsEdge(sV, tV)) {
                        subG[i].subGraph.addVertex(tV);
                        subG[i].subGraph.addEdge(sV, tV);
                    }
                }

                // 根据当前的计算环境来精化子图
                refineSubGraph(subG[i].subGraph);

                // 修补残缺的multiEdge
                mendSubGraphWithMultiEdge(subG[i].subGraph);

                // 抽取出subGraph对应的三元组List
                subG[i].stmList = extractSubGraphTriples(subG[i].subGraph);
            }
        }.execute(0, propNum);
        return subG;
    }

    public PropertySubGraph gainPropSubGraphQuick(String propName) {
        // 初始化电路中的参数
        voltageMatrix = new double[graphVNum];
        rawCurrent = new double[graphVNum][graphVNum];
        currentOut = new double[graphVNum];

        PropertySubGraph subG = new PropertySubGraph();

        // find the position of the property
        int star = findVetexPosInGraph(propName);
        int end = graphVNum - 1;

		/*对特殊的只有申明的属性的处理*/
        if (star < 0) return subG;

		/* 增加边来描述属性 */
        ArrayList newEdge = new ArrayList();
        double[][] newMatrix = new double[graphVNum][graphVNum];
        newMatrix = conductanceMatrix.clone();// 复制原始权重矩阵
        newEdge = addNewPropUseEdge(star, graphVFullName[star], newMatrix);

        // recorde the subgraph information
        subG = new PropertySubGraph();
        subG.propName = new String(propName);
        subG.subGraph = new DefaultDirectedGraph(DefaultEdge.class);

        // 计算指定属性的基本子图

        // 解线性方程组
        voltageMatrix = JBlasWrapper.JBlasSolver(graphVNum, star, end, newMatrix, 10.0, 0.0);
/*
        LinearEquation Equation = new LinearEquation();
        // the last node is sink node, it is "sourceVertexNum+1"
        Equation.InitlizePara(graphVNum, star, end);
        // 设定电导矩阵
        Equation.SetConductMatrix(newMatrix);
        // 指定初始电压
        Equation.PrepareMatrixA_b(10.0, 0.0);
        // 解线性方程组
        Equation.Solve();
        // 得到各个点上电压
        voltageMatrix = Equation.GetResultMaxtrix();
*/

        // 图中的电流
        ComputeRawCurrent(graphVNum);
        // 每个点的out current
        ComputeCurrentOut(graphVNum);

        // Displaygeneration启发式算法
        subG.subGraph = displayGraphGeneration(graphVNum, graphVName, MAX_DISPSUBGRAPH_SIZE);

        // 完善子图
        for (Iterator it = newEdge.iterator(); it.hasNext(); ) {
            DefaultEdge eg = (DefaultEdge) it.next();
            String sV = orgnGraph.getEdgeSource(eg).toString();
            String tV = orgnGraph.getEdgeTarget(eg).toString();
            if (subG.subGraph.containsVertex(sV) && !subG.subGraph.containsEdge(sV, tV)) {
                subG.subGraph.addVertex(tV);
                subG.subGraph.addEdge(sV, tV);
            }
        }

        // 根据当前的计算环境来精化子图
        refineSubGraph(subG.subGraph);

        // 修补残缺的multiEdge
        mendSubGraphWithMultiEdge(subG.subGraph);

//		 System.out.println("Display Graph: " + subG.subGraph.toString());
//		 System.out.println("Sub Graph Size:"+ subG.subGraph.vertexSet().size());

        // 抽取出subGraph对应的三元组List
        subG.stmList = extractSubGraphTriples(subG.subGraph);

        // 显示和存储得到的子图
        // showAndSaveSubGraph(subG[i].subGraph,graphVName[star]);
        return subG;
    }

    /***************************************************************************
     * 从构成大图的三元组中抽取构成当前子图的triples
     **************************************************************************/
    public ArrayList extractSubGraphTriples(DirectedGraph g) {
        ArrayList stmList = new ArrayList();

		/* 转换子图的边为<a,b>和<a,p,b>两种情形 */
        ArrayList gEdgeList = new ArrayList();
        gEdgeList.addAll(g.edgeSet());
        Set multEdge = new HashSet();
        for (Iterator it = g.edgeSet().iterator(); it.hasNext(); ) {
            DefaultEdge eg = (DefaultEdge) it.next();
            String sV = g.getEdgeSource(eg).toString();
            String tV = g.getEdgeTarget(eg).toString();
			/* sV,tV是否包含Statement_x */
            if (sV.indexOf("Statement_") != -1
                    || tV.indexOf("Statement_") != -1) {// 包含
                String bgNode;
                if (sV.indexOf("Statement_") != -1) {
                    bgNode = sV;
                } else {
                    bgNode = tV;
                }
				/* 记录多重边出现的位置 */
                multEdge.add(bgNode);
            } else {// 不包含
				/* 得到唯一确定的Statement */
                Statement st = getTripleFromGraph(sV, tV);
                if (!stmList.contains(st) && st != null) {
                    stmList.add(st);
                }
				/* 辅助List删除当前的边 */
                gEdgeList.remove(eg);
            }
        }

		/* 处理多重边 */
        for (Iterator it = multEdge.iterator(); it.hasNext(); ) {
            String bgNode = (String) it.next();
			/* 找到出现Statement_x的边 */
            String mSub = null;
            String mPre = null;
            String mObj = null;
            for (Iterator jt = gEdgeList.iterator(); jt.hasNext(); ) {
                DefaultEdge eg = (DefaultEdge) jt.next();
                String sV = g.getEdgeSource(eg).toString();
                String tV = g.getEdgeTarget(eg).toString();
                if (!sV.equals(bgNode) && tV.equals(bgNode)) {
                    // 找到S
                    mSub = sV;
                }
                if (sV.equals(bgNode)) {
                    // 找到P和O
                    if (mPre == null) {
                        mPre = tV;
                    } else {
                        mObj = tV;
                    }
                }
            }
            if (mSub != null && mPre != null && mObj != null) {
				/* 得到唯一确定的Statement */
                Statement st = getTripleFromGraph(mSub, mPre, mObj);
                if (st == null) {
                    st = getTripleFromGraph(mSub, mObj, mPre);
                }
                if (!stmList.contains(st) && st != null) {
                    stmList.add(st);
                }
            } else {// 三元组信息已经不完整
				/*
				 * 这种情况的处理不好判断，难以精确处理， 这里采用的近似方法是尽量返回包含<s,o>的三元组
				 */
                if (mSub != null && mPre != null && mObj == null) {
                    Statement st = getTripleFromGraph(mSub, mPre);
                    if (!stmList.contains(st) && st != null) {
                        stmList.add(st);
                    }
                }
                if (mSub != null && mPre == null && mObj != null) {
                    Statement st = getTripleFromGraph(mSub, mObj);
                    if (!stmList.contains(st) && st != null) {
                        stmList.add(st);
                    }
                }
            }
        }
        return stmList;
    }

    /***************************************************************************
     * 得到唯一的由s,o决定的三元组
     **************************************************************************/
    public Statement getTripleFromGraph(String s, String o) {
        Statement result = null;
        for (int i = 0; i < trpNum; i++) {
            Statement st = graphTrp[i];
            Resource sub = st.getSubject();
            Property pre = st.getPredicate();
            RDFNode obj = st.getObject();

            // 获得S,P,O的名称
            ArrayList list = ontParse.getStLocalName(st);
            String sStr = (String) list.get(0);
            String pStr = (String) list.get(1);
            String oStr = (String) list.get(2);

            if (sStr.equals(s) && oStr.equals(o)) {
                result = st;
                break;
            }
        }
        return result;
    }

    /***************************************************************************
     * 得到唯一的由s,p,o决定的三元组
     **************************************************************************/
    public Statement getTripleFromGraph(String s, String p, String o) {
        Statement result = null;
        for (int i = 0; i < trpNum; i++) {
            Statement st = graphTrp[i];
            Resource sub = st.getSubject();
            Property pre = st.getPredicate();
            RDFNode obj = st.getObject();

            // 获得S,P,O的名称
            ArrayList list = ontParse.getStLocalName(st);
            String sStr = (String) list.get(0);
            String pStr = (String) list.get(1);
            String oStr = (String) list.get(2);

            if (sStr.equals(s) && pStr.equals(p) && oStr.equals(o)) {
                result = st;
                break;
            }
        }
        return result;
    }

    /***************************************************************************
     * 获取匿名concept
     **************************************************************************/
    public OntClass obtainAnonOntClass(String name) {
        OntClass c = null;
        for (Iterator i = anonCnpt.iterator(); i.hasNext(); ) {
            OntClass c_temp = (OntClass) i.next();
            if (name.equals(c_temp.toString())) {
                c = c_temp;
                break;
            }
        }
        return c;
    }

    /***************************************************************************
     * 获取匿名Property
     **************************************************************************/
    public OntProperty obtainAnonProperty(String name) {
        OntProperty c = null;
        for (Iterator i = anonProp.iterator(); i.hasNext(); ) {
            OntProperty c_temp = (OntProperty) i.next();
            if (name.equals(c_temp.toString())) {
                c = c_temp;
                break;
            }
        }
        return c;
    }

    /***************************************************************************
     * 获取匿名Instance
     **************************************************************************/
    public Individual obtainAnonIndividual(String name) {
        Individual c = null;
        for (Iterator i = anonIns.iterator(); i.hasNext(); ) {
            Individual c_temp = (Individual) i.next();
            if (name.equals(c_temp.toString())) {
                c = c_temp;
                break;
            }
        }
        return c;
    }

    /***************************************************************************
     * 添加新边
     **************************************************************************/
    public ArrayList addNewPropUseEdge(int star, String pName,
                                       double[][] oldmatrix) {
        ArrayList newEdgeList = new ArrayList();
		/* (1)取出属性被使用的三元组 */
        ArrayList stmList = new ArrayList();
        OntProperty p = findPropByFullName(pName);
        Selector sl = new SimpleSelector(null, p, (RDFNode) null);
		/* (2)遍历三元组，确定DOMAIN和RANGE的实例是否在图中 */
        for (StmtIterator it = m.listStatements(sl); it.hasNext(); ) {
            Statement st = it.nextStatement();
            Resource sRes = st.getSubject();
            RDFNode oNode = st.getObject();
            ArrayList list = ontParse.getStLocalName(st);
            String sStr = (String) list.get(0);
            String oStr = (String) list.get(2);
            double wta = 0;
            double wtb = 0;
            double wtPai = 0;
            double wtA = 0;
            double wtB = 0;
            double wnew = 0;
			/* (3)添加边 */
            if (orgnGraph.containsEdge(sStr, oStr)) {// 当前三元组存在于图中
                // 添加边P-->ai
                int aPos = -1;
                aPos = this.findVetexPosInGraph(sStr);
                DefaultEdge eg = (DefaultEdge) orgnGraph.getEdge(sStr, oStr);
                newEdgeList.add(eg);
				/* (4)计算新加边的权重 */
                // ai的权重
                // ai只能是实例
                Individual ai = findInsByLocalName(sStr);
                if (ai != null) {
                    wta = FindInstanceyWeight(sStr, fullInsNum);
                }

                // bi的权重
                // bi可能是实例或值
                int bPos = -1;
                bPos = this.findVetexPosInGraph(oStr);
                Individual bi = findInsByLocalName(oStr);
                if (bi == null) {// 不是实例
                    if (oNode.isLiteral()) {
                        wtb = 0.5;
                    } else {
                        wtb = 0.2;
                    }
                } else {// 是实例
                    wtb = FindInstanceyWeight(oStr, fullInsNum);
                }
                // pai权重
                wtPai = GetStatementPaiWeight(m, st, p, sRes);
                // ai-->A的权重
                if (ai != null) {
                    for (ExtendedIterator itx = ai.listRDFTypes(true); itx
                            .hasNext(); ) {
                        Resource rx = (Resource) itx.next();
                        int APos = -1;
                        APos = this.findVetexPosInGraph(rx.getLocalName());
                        wtA = Math.max(wtA, oldmatrix[aPos][APos]);
                    }
                }

                // bi-->B的权重
                if (bi != null) {
                    for (ExtendedIterator itx = bi.listRDFTypes(true); itx.hasNext(); ) {
                        Resource rx = (Resource) itx.next();
                        int BPos = -1;
                        BPos = findVetexPosInGraph(rx.getLocalName());
                        if (BPos < 0) continue;
                        wtB = Math.max(wtB, oldmatrix[aPos][BPos]);
                    }
                } else {
                    wtB = 0.5;
                }
                // 总的权重
                wnew = (wtA + wtB + (wtPai + wta + wtb) / 3.0) / 3.0;
                oldmatrix[star][aPos] = wnew;
                oldmatrix[aPos][star] = 0.8 * wnew;
            }
        }
        return newEdgeList;
    }

    /***************************************************************************
     * 添加新节点到节点集合中，由于该功能被多处使用，因此独立为函数
     **************************************************************************/
    private void addNewVertex(ArrayList vList, ArrayList vFList, String lName, String fName, String URI, String baseURI) {
        if (lName != null && !(vList.contains(lName) && vFList.contains(fName))) {
				/*避免重名节点，保持点集同步*/
            if (vList.contains(lName) && !vFList.contains(fName)) {
					/*对于重复，优先baseURI的*/
                int pos = vList.indexOf(lName);//取重复元素位置
					/*如果当前是baseURI,则原来存在的必然不是*/
                if (URI.equals(baseURI)) {
                    vList.set(pos, vFList.get(pos));//修改原来的为fullName形式
                    vList.add(lName);//写入当前的
                } else {
                    vList.add(fName);//如果不是baseURI,直接写入全名
                }
            } else {
                vList.add(lName);//不存在重复
            }
            vFList.add(fName);//写入fullName
        }
    }

    public ArrayList consInfSubOnt4OneCnpt(ArrayList paraList, String cnptName) {
        ArrayList result = new ArrayList();

        //1.解析参数
        unPackOntPara(paraList);

        //2.本体预处理
        //usedEnrich=true;
        preProcessOnt();
        //3.重构本体成分的informativ graph
        //图模型的建立
        orgnGraph = ont2Graph();

        //计算资源权重
        //getResWeight();

        //计算电导矩阵
        //conductanceMatrix = new double[graphVNum][graphVNum];
        //conductanceMatrix = getConductMatrix(graphVNum, orgnGraph, graphVName);

        //解方程得到语义子图
        ConceptSubGraph tsubG = new ConceptSubGraph();
//		tsubG=gainCnptSubGraphQuick(cnptName); //在新方法中，不采用解语义子图的耗时处理

        tsubG.conceptName = cnptName;
        tsubG.subGraph = orgnGraph;
        tsubG.stmList = new ArrayList();


        //tsubG.stmList = extractSubGraphTriples(tsubG.subGraph);

        for (int i = 0; i < trpNum; i++) {
            Statement st = graphTrp[i];
            tsubG.stmList.add(st);
        }

        result.add(0, tsubG);

//		System.out.println(cnptName+"的Snap完成！  大小："+tsubG.subGraph.vertexSet().size());
        return result;
    }

    public ArrayList consInfSubOnt4OneProp(ArrayList paraList, String propName) {
        ArrayList result = new ArrayList();

        //1.解析参数
        unPackOntPara(paraList);

        //2.本体预处理
        //usedEnrich=false;
        preProcessOnt();
        //3.重构本体成分的informative graph
        //图模型的建立
        orgnGraph = ont2Graph();

        //计算资源权重
        getResWeight();

        //计算电导矩阵
        conductanceMatrix = new double[graphVNum][graphVNum];
        conductanceMatrix = getConductMatrix(graphVNum, orgnGraph, graphVName);

        //解方程得到语义子图
        PropertySubGraph tsubG = new PropertySubGraph();
        tsubG = gainPropSubGraphQuick(propName);
        result.add(0, tsubG);
        return result;
    }

    /**
     * 读取参数
     **/
    public void loadConfigFile() {
        MAX_DISPSUBGRAPH_SIZE = ParamStore.Semantic_SubGraph_Size;

        cfw = ParamStore.cfw;
        csw = ParamStore.csw;
        ciw = ParamStore.ciw;

        pfw = ParamStore.pfw;
        psw = ParamStore.psw;
        piw = ParamStore.piw;

        idpw = ParamStore.idpw;
        iopw = ParamStore.iopw;
        icw = ParamStore.icw;

		/*System.out.println("MAX_DISPSUBGRAPH_SIZE:" + MAX_DISPSUBGRAPH_SIZE);
		System.out.println("cfw:" + cfw );
		System.out.println("csw:" + csw );
		System.out.println("ciw:" + ciw );*/
    }
}