/************************************************
 * Source code information
 * -----------------------
 * Original author	 Peng Wang, School of Computer Science & Eng., Southeast University
 * Author email      pwangseu@gmail.com
 * Web               http://ontomapping.googlepages.com
 * Created			 2007-6-20
 * Filename          FullSimPropagation.java
 * Version           2.0
 * 
 * Last modified on  2007-6-20
 *               by  Peng Wang
 * -----------------------
 * Functions describe:
 * 正对全图的相似度传播算法；
 * 传播条件仍然是强相似条件；
 * 迭代收敛不快，效率不高。
 ***********************************************/
package lily.onto.handle.propagation;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import lily.onto.parse.OWLOntParse;
import lily.tool.datastructure.*;
import lily.tool.filter.SimpleFilter;
import lily.tool.mappingfile.MappingFile;
import lily.tool.textsimilarity.TfIdfSim;
import org.dom4j.DocumentException;

import java.net.MalformedURLException;
import java.util.*;

/*******************************************************************************
 * Class information -------------------
 * 
 * @author Peng Wang
 * @date 2007-6-20
 * 
 * describe: 该算法的特点是将传播范围限制在语义子图内，不考虑语义子图外的元素
 ******************************************************************************/
public class HybridSubSimPropagation {
	/* 数据成员 */
	public OntModel m_source;

	public OntModel m_target;

	/** ***源本体***** */
	public int s_cnptNum;// 概念数目

	public int s_propNum;// 属性数目

	public int s_insNum;// 实例数目

	public String[] s_cnptName;// 概念名

	public String[] s_propName;// 属性名

	public String[] s_insName;// 实例名

	public ArrayList s_stmList;// 图
	public ArrayList s_cnptStm;// 合并的概念子图
	public ArrayList s_propStm;// 合并的概念子图
	public ConceptSubGraph[] s_cnptSubG;// 概念子图
	public PropertySubGraph[] s_propSubG;// 属性子图
	public String s_baseURI;

	/** ***目标本体***** */
	public int t_cnptNum;// 概念数目

	public int t_propNum;// 属性数目

	public int t_insNum;// 实例数目

	public String[] t_cnptName;// 概念名

	public String[] t_propName;// 属性名

	public String[] t_insName;// 属性名

	public ArrayList t_stmList;// 图
	public ArrayList t_cnptStm;// 合并的概念子图
	public ArrayList t_propStm;// 合并的概念子图
	public ConceptSubGraph[] t_cnptSubG;// 概念子图
	public PropertySubGraph[] t_propSubG;// 属性子图
	public String t_baseURI;

	/** ***匿名资源**** */
	private ArrayList s_AnonCnpt;

	private ArrayList s_AnonProp;

	private ArrayList s_AnonIns;

	private ArrayList t_AnonCnpt;

	private ArrayList t_AnonProp;

	private ArrayList t_AnonIns;

	/** ****相似度矩阵***** */
	public double[][] cnptSimRaw;// 原始概念相似度

	public double[][] cnptSimK0;// k次迭代相似度

	public double[][] cnptSimK1;// k+1次迭代相似度
	
	public double[][] cnptSimRow;

	public double[][] cnptSimCol;
	
	public double[][] propSimRaw;// 原始属性相似度

	public double[][] propSimK0;// k次迭代相似度

	public double[][] propSimK1;// k+1次迭代相似度

	public double[][] insSimRaw;// 原始实例相似度
	
	public double[][] propSimRow;
	
	public double[][] propSimCol;

	/** ****其它相似度矩阵*** */
	// 其它元素间的相似度矩阵
	public ArrayList otElmSim;

	/* 原始相似度的Hash */
	private HashMap cnptSimMap;

	private HashMap propSimMap;

	private HashMap insSimMap;
	
	/* 元素<-->元素相似hash表 */
	private HashMap CCMap;
	private HashMap PPMap;
	private HashMap IIMap;
	private HashMap LLMap;
	private HashMap EEMap;//合并的相似hash表
	
	/* 元素<-->相关三元组hash表 */
	private HashMap sCnptE2Triple;
	private HashMap sPropE2Triple;
	private HashMap tCnptE2Triple;
	private HashMap tPropE2Triple;
	private HashMap[] sSepCnptE2Triple;
	private HashMap[] sSepPropE2Triple;
	private HashMap[] tSepCnptE2Triple;
	private HashMap[] tSepPropE2Triple;
	
	/*可信相似度位置*/
	public Set s_cnptOkSimPos;
	public Set s_propOkSimPos;
	public Set t_cnptOkSimPos;
	public Set t_propOkSimPos;
	
	public int[] pbarValue;
	
	// 本体元信息
	public Set ontLngURI;

	public OWLOntParse ontParse;

	private int maxProgTimes = 8;// 传播最大迭代次数
	
	private boolean sGUpdate;// 子图更新标志
	private boolean localCvg;// 子图相似度稳定
	private boolean globeCvg;// 全图相似度稳定
	private boolean fromCnpt;// 概念子图传播标记
	private boolean fromProp;// 属性子图传播标记
	private double minSim=0.001;
	private int maxLocalUpdateTime=10;
	private int maxGlobleUpdateTime=5;

	/***************************************************************************
	 * 类的主入口
	 **************************************************************************/
	public ArrayList ontSimPg(ArrayList paraList) {
		ArrayList result = new ArrayList();

		/* 解析参数 */
		unPackPara(paraList);
		long start = System.currentTimeMillis();// 开始计时
		globeCvg=false;
		int cn = 0;
		while(!globeCvg && cn < maxGlobleUpdateTime){
			/*数据结构处理*/
			dataStruProc();
			simPropagation();// 相似度传播
			globeCvg=isGlobeConverge();
			cn++;
			pbarValue[2] = pbarValue[2]+ (int)((100.0-pbarValue[2])/3.0); 
		}

		long end = System.currentTimeMillis();// 结束计时
		long costtime = end - start;// 统计算法时间
		//System.out.println("相似度传播算法时间：" + (double) costtime / 1000. + "秒");

		result.add(0, cnptSimRaw);
		result.add(1, propSimRaw);
		return result;
	}

	/***************************************************************************
	 * 接收本体参数
	 **************************************************************************/
	private void unPackPara(ArrayList paraList) {
		m_source = (OntModel) paraList.get(0);
		m_target = (OntModel) paraList.get(1);

		s_cnptNum = ((Integer) paraList.get(2)).intValue();
		s_propNum = ((Integer) paraList.get(3)).intValue();
		s_insNum = ((Integer) paraList.get(4)).intValue();
		t_cnptNum = ((Integer) paraList.get(10)).intValue();
		t_propNum = ((Integer) paraList.get(11)).intValue();
		t_insNum = ((Integer) paraList.get(12)).intValue();

		// 根据得到的number初始化各种数组
		initPara();

		s_cnptName = (String[]) (paraList.get(5));
		s_propName = (String[]) (paraList.get(6));
		s_insName = (String[]) (paraList.get(7));

		s_stmList = (ArrayList) (paraList.get(8));

		s_baseURI = (String) (paraList.get(9));

		t_cnptName = (String[]) (paraList.get(13));
		t_propName = (String[]) (paraList.get(14));
		t_insName = (String[]) (paraList.get(15));

		t_stmList = (ArrayList) (paraList.get(16));

		t_baseURI = (String) (paraList.get(17));

		cnptSimRaw = (double[][]) (paraList.get(18));
		propSimRaw = (double[][]) (paraList.get(19));
		insSimRaw = (double[][]) (paraList.get(20));

		s_AnonCnpt = (ArrayList) (paraList.get(21));
		s_AnonProp = (ArrayList) (paraList.get(22));
		s_AnonIns = (ArrayList) (paraList.get(23));
		t_AnonCnpt = (ArrayList) (paraList.get(24));
		t_AnonProp = (ArrayList) (paraList.get(25));
		t_AnonIns = (ArrayList) (paraList.get(26));
		
		s_cnptSubG = (ConceptSubGraph[]) (paraList.get(27));
		s_propSubG = (PropertySubGraph[]) (paraList.get(28));
		t_cnptSubG = (ConceptSubGraph[]) (paraList.get(29));
		t_propSubG = (PropertySubGraph[]) (paraList.get(30));

		otElmSim = (ArrayList) (paraList.get(31));
		
		s_cnptOkSimPos=(HashSet)(paraList.get(32));
		t_cnptOkSimPos=(HashSet)(paraList.get(33));
		s_propOkSimPos=(HashSet)(paraList.get(34));
		t_propOkSimPos=(HashSet)(paraList.get(35));
		
		/**1.构造完全由语义子图构成的图,同时过滤元语数目>=2的三元组**/
		ArrayList tmpStm=new ArrayList();
		Set used=new HashSet();
		for (int i=0;i<s_cnptNum;i++){
			ArrayList tmpSubStm=new ArrayList();
			for (Iterator it=s_cnptSubG[i].stmList.iterator();it.hasNext();){
				Statement st=(Statement)it.next();
				String key=st.toString();
				if (!used.contains(key) && metaElmInTriple(st) < 2){
					used.add(key);
					tmpStm.add(st);					
				}
				if (metaElmInTriple(st) < 2){
					tmpSubStm.add(st);
				}
			}
			s_cnptSubG[i].stmList=tmpSubStm;
		}
		s_cnptStm=tmpStm;
		tmpStm=new ArrayList();
		used.clear();
		for (int i=0;i<s_propNum;i++){
			ArrayList tmpSubStm=new ArrayList();
			for (Iterator it=s_propSubG[i].stmList.iterator();it.hasNext();){
				Statement st=(Statement)it.next();
				String key=st.toString();
				if (!used.contains(key)&& metaElmInTriple(st) < 2){
					used.add(key);
					tmpStm.add(st);
				}
				if (metaElmInTriple(st) < 2){
					tmpSubStm.add(st);
				}
			}	
			s_propSubG[i].stmList=tmpSubStm;
		}
		s_propStm=tmpStm;
		tmpStm=new ArrayList();
		used.clear();
		for (int i=0;i<t_cnptNum;i++){
			ArrayList tmpSubStm=new ArrayList();
			for (Iterator it=t_cnptSubG[i].stmList.iterator();it.hasNext();){
				Statement st=(Statement)it.next();
				String key=st.toString();
				if (!used.contains(key)&& metaElmInTriple(st) < 2){
					used.add(key);
					tmpStm.add(st);
				}
				if (metaElmInTriple(st) < 2){
					tmpSubStm.add(st);
				}
			}
			t_cnptSubG[i].stmList=tmpSubStm;
		}
		t_cnptStm=tmpStm;
		tmpStm=new ArrayList();
		used.clear();
		for (int i=0;i<t_propNum;i++){
			ArrayList tmpSubStm=new ArrayList();
			for (Iterator it=t_propSubG[i].stmList.iterator();it.hasNext();){
				Statement st=(Statement)it.next();
				String key=st.toString();
				if (!used.contains(key)&& metaElmInTriple(st) < 2){
					used.add(key);
					tmpStm.add(st);
				}
				if (metaElmInTriple(st) < 2){
					tmpSubStm.add(st);
				}
			}
			t_propSubG[i].stmList=tmpSubStm;
		}
		t_propStm=tmpStm;
		tmpStm=new ArrayList();
		used.clear();
		
		/**2.对otsim做过滤处理**/
		/*2.1转化为数组*/
		HashMap otRowName=new HashMap();
		HashMap otColName=new HashMap();
		int otN=0;
		int otM=0;
		for (Iterator it = otElmSim.iterator(); it.hasNext();) {
			GraphElmSim pair = (GraphElmSim) it.next();
			String nameA=pair.elmNameA;
			String nameB=pair.elmNameB;
			if (!otRowName.containsKey(nameA)){
				otRowName.put(nameA,otN);
				otN++;
			}
			if (!otColName.containsKey(nameB)){
				otColName.put(nameB,otM);
				otM++;
			}			
		}
		double[][] otSim=new double[otN][otM];
		for (Iterator it = otElmSim.iterator(); it.hasNext();) {
			GraphElmSim pair = (GraphElmSim) it.next();
			String nameA=pair.elmNameA;
			String nameB=pair.elmNameB;
			double sim=pair.sim;
			int i=(Integer)(otRowName.get(nameA));
			int j=(Integer)(otColName.get(nameB));
			otSim[i][j]=sim;
		}
		/*2.2过滤*/
		otSim= new SimpleFilter().maxValueFilter (otN,otM, otSim, 0.00001);
//		new StableMarriageFilter().run(otSim,otN,otM);
		/*2.3还原*/
		for (Iterator it = otElmSim.iterator(); it.hasNext();) {
			GraphElmSim pair = (GraphElmSim) it.next();
			String nameA=pair.elmNameA;
			String nameB=pair.elmNameB;
			double sim=pair.sim;
			int i=(Integer)(otRowName.get(nameA));
			int j=(Integer)(otColName.get(nameB));
			if (otSim[i][j]<=0.00001){
				pair.sim = 0;
			}
		}
		
		//randomInitialSim(0.90);//打乱初始相似度的测试
		
	}

	/***************************************************************************
	 * 初始化
	 **************************************************************************/
	private void initPara() {
		s_cnptName = new String[s_cnptNum];
		s_propName = new String[s_propNum];
		s_insName = new String[s_insNum];
		t_cnptName = new String[t_cnptNum];
		t_propName = new String[t_propNum];
		t_insName = new String[t_insNum];
		cnptSimRaw = new double[s_cnptNum][t_cnptNum];
		propSimRaw = new double[s_propNum][t_propNum];
		insSimRaw = new double[s_insNum][t_insNum];

		otElmSim = new ArrayList();

		ontParse = new OWLOntParse();
		ontLngURI = ontParse.metaURISet;

		cnptSimK0 = new double[s_cnptNum][t_cnptNum];
		cnptSimK1 = new double[s_cnptNum][t_cnptNum];
		propSimK0 = new double[s_propNum][t_propNum];
		propSimK1 = new double[s_propNum][t_propNum];
		
		cnptSimRow = new double[s_cnptNum][t_cnptNum];
		cnptSimCol = new double[s_cnptNum][t_cnptNum];
		
		s_cnptSubG = new ConceptSubGraph[s_cnptNum];
		s_propSubG = new PropertySubGraph[s_propNum];
		t_cnptSubG = new ConceptSubGraph[t_cnptNum];
		t_propSubG = new PropertySubGraph[t_propNum];
		
		propSimRow = new double[s_propNum][t_propNum];
		propSimCol = new double[s_propNum][t_propNum];

	}

	/***************************************************************************
	 * 相似度的传播
	 **************************************************************************/
	private void simPropagation() {
		/**概念整体子图间相似度传播**/
		fromCnpt=true;
		fromProp=false;
		cnptSimPropagation();
		fromCnpt=false;
		/**属性整体子图间相似度传播**/
		fromProp=true;
		propSimPropagation();
		fromProp=false;
	}
	
	private void cnptSimPropagation() {
		/**1.从source的子图向target合并图传播**/
		for (int i=0;i<s_cnptNum;i++){
			/* 跳过可信的位置 */
			if (s_cnptOkSimPos.contains(i)) {continue;}
			
			HashMap gNodes = new HashMap();
			HashMap gEdges = new HashMap();
			HashMap edgeMap = new HashMap();
			HashMap progWeightMap = new HashMap();
			ArrayList lt = new ArrayList();
			Set goodTPSet = new HashSet();
			
			//System.out.println(i+s_cnptName[i]+"传播");

			/**计算初始候选相似三元组对**/
			Set candiTPSet = new HashSet();
			candiTPSet = getSimTripleCandidate(s_cnptSubG[i].stmList,tCnptE2Triple);
			
			sGUpdate = true;
			localCvg = false;
			
			int cn = 0;
			while (sGUpdate && (cn < maxLocalUpdateTime) && !localCvg) {
				/**构造对偶图**/
				lt = consTriplePairGraph(candiTPSet);
				gNodes = (HashMap) lt.get(0);
				gEdges = (HashMap) lt.get(1);
				edgeMap = (HashMap) lt.get(2);
				progWeightMap = (HashMap) lt.get(3);
				/**相似度传播**/
				propagation(edgeMap, progWeightMap);
				/**更新候选三元组对相似度**/
				goodTPSet=updateTriplePairSim(candiTPSet, edgeMap);
				/**更新候选相似三元组对**/
				updateTripleCandidate(goodTPSet,candiTPSet, edgeMap,sSepCnptE2Triple[i],tCnptE2Triple);
				/**局部相似度是否已经收敛**/
				localCvg=isLocalConverge(edgeMap,i,true);
				cn++;
			}
			
		}
		
		/**2.从target的子图向source合并图传播**/
		for (int i=0;i<t_cnptNum;i++){
			if (t_cnptOkSimPos.contains(i)) {continue;}
			
			HashMap gNodes = new HashMap();
			HashMap gEdges = new HashMap();
			HashMap edgeMap = new HashMap();
			HashMap progWeightMap = new HashMap();
			ArrayList lt = new ArrayList();
			Set goodTPSet = new HashSet();
			
			//System.out.println(i+t_cnptName[i]+"传播");

			/**计算初始候选相似三元组对**/
			Set candiTPSet = new HashSet();
			candiTPSet = getSimTripleCandidate(s_cnptStm,tSepCnptE2Triple[i]);
			
			sGUpdate = true;
			localCvg = false;
			
			int cn = 0;
			while (sGUpdate && (cn < maxLocalUpdateTime) && !localCvg) {
				/**构造对偶图**/
				lt = consTriplePairGraph(candiTPSet);
				gNodes = (HashMap) lt.get(0);
				gEdges = (HashMap) lt.get(1);
				edgeMap = (HashMap) lt.get(2);
				progWeightMap = (HashMap) lt.get(3);
				/**相似度传播**/
				propagation(edgeMap, progWeightMap);
				/**更新候选三元组对相似度**/
				goodTPSet=updateTriplePairSim(candiTPSet, edgeMap);
				/**更新候选相似三元组对**/
				updateTripleCandidate(goodTPSet,candiTPSet, edgeMap,sCnptE2Triple,tSepCnptE2Triple[i]);
				/**局部相似度是否已经收敛**/
				localCvg=isLocalConverge(edgeMap,i,false);
				cn++;
			}
			
		}
	}
	
	private void propSimPropagation() {
		/**1.从source的子图向target合并图传播**/
		for (int i=0;i<s_propNum;i++){
			if (s_propOkSimPos.contains(i)) {continue;}
			
			HashMap gNodes = new HashMap();
			HashMap gEdges = new HashMap();
			HashMap edgeMap = new HashMap();
			HashMap progWeightMap = new HashMap();
			ArrayList lt = new ArrayList();
			Set goodTPSet = new HashSet();
			
			//System.out.println(i+s_propName[i]+"传播");

			/**计算初始候选相似三元组对**/
			Set candiTPSet = new HashSet();
			candiTPSet = getSimTripleCandidate(s_propSubG[i].stmList,tPropE2Triple);
			
			sGUpdate = true;
			localCvg = false;
			
			int cn = 0;
			while (sGUpdate && (cn < maxLocalUpdateTime) && !localCvg) {
				/**构造对偶图**/
				lt = consTriplePairGraph(candiTPSet);
				gNodes = (HashMap) lt.get(0);
				gEdges = (HashMap) lt.get(1);
				edgeMap = (HashMap) lt.get(2);
				progWeightMap = (HashMap) lt.get(3);
				/**相似度传播**/
				propagation(edgeMap, progWeightMap);
				/**更新候选三元组对相似度**/
				goodTPSet=updateTriplePairSim(candiTPSet, edgeMap);
				/**更新候选相似三元组对**/
				updateTripleCandidate(goodTPSet,candiTPSet, edgeMap,sSepPropE2Triple[i],tPropE2Triple);
				/**局部相似度是否已经收敛**/
				localCvg=isLocalConverge(edgeMap,i,true);
				cn++;
			}
			
		}
		
		/**2.从target的子图向source合并图传播**/
		for (int i=0;i<t_propNum;i++){
			if (t_propOkSimPos.contains(i)) {continue;}
			
			HashMap gNodes = new HashMap();
			HashMap gEdges = new HashMap();
			HashMap edgeMap = new HashMap();
			HashMap progWeightMap = new HashMap();
			ArrayList lt = new ArrayList();
			Set goodTPSet = new HashSet();
			
			//System.out.println(i+t_propName[i]+"传播");

			/**计算初始候选相似三元组对**/
			Set candiTPSet = new HashSet();
			candiTPSet = getSimTripleCandidate(s_propStm,tSepPropE2Triple[i]);
			
			sGUpdate = true;
			localCvg = false;
			
			int cn = 0;
			while (sGUpdate && (cn < maxLocalUpdateTime) && !localCvg) {
				/**构造对偶图**/
				lt = consTriplePairGraph(candiTPSet);
				gNodes = (HashMap) lt.get(0);
				gEdges = (HashMap) lt.get(1);
				edgeMap = (HashMap) lt.get(2);
				progWeightMap = (HashMap) lt.get(3);
				/**相似度传播**/
				propagation(edgeMap, progWeightMap);
				/**更新候选三元组对相似度**/
				goodTPSet=updateTriplePairSim(candiTPSet, edgeMap);
				/**更新候选相似三元组对**/
				updateTripleCandidate(goodTPSet,candiTPSet, edgeMap,sPropE2Triple,tSepPropE2Triple[i]);
				/**局部相似度是否已经收敛**/
				localCvg=isLocalConverge(edgeMap,i,false);
				cn++;
			}
			
		}
	}

	/***************************************************************************
	 * 选择候选相似三元组
	 **************************************************************************/
	private Set getSimTripleCandidate(ArrayList stmList, HashMap tE2Triple) {
		long start = System.currentTimeMillis();// 开始计时
		
		ArrayList lt=new ArrayList();
		Set tPairSet = new HashSet();		
		
		double weight = 0;// 区别边的weight
		int ct = 0;
		for (Iterator itx = stmList.iterator(); itx.hasNext();) {
			Statement stA = (Statement) itx.next();
			//System.out.println("选择候选相似三元组" + ct++);
			// 分离三元组
			Resource subA = stA.getSubject();
			Property propA = stA.getPredicate();
			RDFNode objA = stA.getObject();
			String subAName = subA.toString();
			String propAName = propA.toString();
			String objAName = objA.toString();
			String urlSA = null, urlPA = null, urlOA = null;
			if (subA.isURIResource()) {
				urlSA = subA.getNameSpace();
			}
			if (propA.isURIResource()) {
				urlPA = propA.getNameSpace();
			}
			if (objA.isURIResource()) {
				urlOA = objA.asNode().getNameSpace();
			}
			
			/**1.构造当前三元组对应的候选三元组集合**/
			ArrayList t_CandiStm=new ArrayList();
			/**1.1.1求S对应的相似三元组集合**/
			ArrayList SEList=new ArrayList();
			ArrayList STpSet=new ArrayList();
			SEList=(ArrayList)EEMap.get(subAName);//取对应相似元素
			if (SEList!=null){
				for (Iterator ity=SEList.iterator();ity.hasNext();){
					String name=(String)ity.next();
					if (tE2Triple.get(name) ==null){continue;}
					ArrayList tpList=((ArrayList[])tE2Triple.get(name))[0];//取对应位置的三元组
					STpSet.addAll(tpList);
				}
			}			
			/**1.1.2求P对应的相似三元组集合**/
			ArrayList PEList=new ArrayList();
			ArrayList PTpSet=new ArrayList();
			PEList=(ArrayList)EEMap.get(propAName);
			if (PEList!=null){
				for (Iterator ity=PEList.iterator();ity.hasNext();){
					String name=(String)ity.next();
					if (tE2Triple.get(name) ==null){continue;}
					ArrayList tpList=((ArrayList[])tE2Triple.get(name))[1];
					PTpSet.addAll(tpList);					
				}
			}			
			/**1.1.3求O对应的相似三元组集合**/
			ArrayList OEList=new ArrayList();
			ArrayList OTpSet=new ArrayList();
			OEList=(ArrayList)EEMap.get(objAName);
			if (OEList!=null){
				for (Iterator ity=OEList.iterator();ity.hasNext();){
					String name=(String)ity.next();
					if (tE2Triple.get(name) ==null){continue;}
					ArrayList tpList=((ArrayList[])tE2Triple.get(name))[2];
					OTpSet.addAll(tpList);					
				}
			}
			
			/**1.2.1当P,O不是元语时**/
			if (!ontLngURI.contains(urlPA) && !ontLngURI.contains(urlOA)){
				/**求STp交PTp**/
				ArrayList ltSP=(ArrayList)STpSet.clone();
				ltSP.retainAll(PTpSet);
				/**求STp交OTp**/
				ArrayList ltSO=(ArrayList)STpSet.clone();
				ltSO.retainAll(OTpSet);
				/**求PTp交OTp**/
				ArrayList ltPO=(ArrayList)PTpSet.clone();
				ltPO.retainAll(OTpSet);
				/**三个集合的并**/
				t_CandiStm=(ArrayList)ltSP.clone();
				for (Iterator ity=ltSO.iterator();ity.hasNext();){
					Statement st=(Statement)ity.next();
					if (!t_CandiStm.contains(st)){
						t_CandiStm.add(st);
					}
				}
				for (Iterator ity=ltPO.iterator();ity.hasNext();){
					Statement st=(Statement)ity.next();
					if (!t_CandiStm.contains(st)){
						t_CandiStm.add(st);
					}
				}
			}
			else {
				/**1.2.2当P或O是元语时**/
				/*当P为元语,结果为STp并OTp*/
				for (Iterator ity=STpSet.iterator();ity.hasNext();){
					Statement st=(Statement)ity.next();
					if (propAName.equals(st.getPredicate().toString())
						&& !t_CandiStm.contains(st)){
						t_CandiStm.add(st);
					}
				}
				if (ontLngURI.contains(urlPA)){					
					for (Iterator ity=OTpSet.iterator();ity.hasNext();){
						Statement st=(Statement)ity.next();
						if (propAName.equals(st.getPredicate().toString())
							&& !t_CandiStm.contains(st)){
							t_CandiStm.add(st);
						}
					}
				}
				/*当O为元语,结果为STp并PTp*/
				if (ontLngURI.contains(urlOA)){
					for (Iterator ity=PTpSet.iterator();ity.hasNext();){
						Statement st=(Statement)ity.next();
						if (propAName.equals(st.getPredicate().toString())
							&& !t_CandiStm.contains(st)){
							t_CandiStm.add(st);
						}
					}
				}
			}
			
			/**2.寻找相似三元组对**/
			for (Iterator ity = t_CandiStm.iterator(); ity.hasNext();) {
				Statement stB = (Statement) ity.next();
				Resource subB = stB.getSubject();
				Property propB = stB.getPredicate();
				RDFNode objB = stB.getObject();
				String subBName = subB.toString();
				String propBName = propB.toString();
				String objBName = objB.toString();
				String urlSB = null, urlPB = null, urlOB = null;
				if (subB.isURIResource()) {
					urlSB = subB.getNameSpace();
				}
				if (propB.isURIResource()) {
					urlPB = propB.getNameSpace();
				}
				if (objB.isURIResource()) {
					urlOB = objB.asNode().getNameSpace();
				}
				// 判断相似三元组对
				/* (1)不出现两个元语,已经判断 */
				/* (2)至少一对非元语相似 */
				// (a)对应位置成分要对应
				// (b)对应位置要存在相似度
				int typeA, typeB;
				double simS, simP, simO;
				boolean metaS, metaP, metaO;
				int simElmNum = 0;
				// 判断s-s
				typeA = getResourceType(subAName, m_source);
				typeB = getResourceType(subBName, m_target);
				simS = -1.0;
				metaS = false;
				// 判断是不是元语
				if (typeA == typeB) {
					// 相同的类型，需要确定相似度
					simS = getElmSim(subA, subB, typeA);
				} else {
					continue;
				}
				if (simS < minSim ) {
					simS = 0;
				}
				if (simS > 0) {
					simElmNum++;
				}

				// 判断p-p
				typeA = getResourceType(propAName, m_source);
				typeB = getResourceType(propBName, m_target);
				simP = -1.0;
				metaP = false;
				// 判断是不是元语
				if (ontLngURI.contains(urlPA) && ontLngURI.contains(urlPB)) {
					// 是否是相同的元语
					if (propAName.equals(propBName)) {
						simP = 1.0;
						metaP = true;
					} else {
						// 元语如果不相同，直接跳过当前triple-pair
						continue;
					}
				}
				// 都不是元语的情况
				else if (!ontLngURI.contains(urlPA)
						&& !ontLngURI.contains(urlPB)) {
					if (typeA == typeB) {
						// 相同的类型，需要确定相似度
						simP = getElmSim(propA, propB, typeA);
					} else {
						continue;
					}
				}
				if (simP < minSim) {
					simP = 0;
				}
				if (simP > 0) {
					simElmNum++;
				}

				// 判断o-o
				typeA = getResourceType(objAName, m_source);
				typeB = getResourceType(objBName, m_target);
				simO = -1.0;
				metaO = false;
				// 判断是不是元语
				if (ontLngURI.contains(urlOA) && ontLngURI.contains(urlOB)) {
					// 是否是相同的元语
					if (objAName.equals(objBName)) {
						simO = 1.0;
						metaO = true;
					} else {
						// 元语如果不相同，直接跳过当前triple-pair
						continue;
					}
				}
				// 都不是元语的情况
				else if (!ontLngURI.contains(urlOA)
						&& !ontLngURI.contains(urlOB)) {
					if (typeA == typeB) {
						// 相同的类型，需要确定相似度
						simO = getElmSim(objA, objB, typeA);
					} else {
						continue;
					}
				}
				if (simO < minSim) {
					simO = 0;
				}
				if (simO > 0) {
					simElmNum++;
				}

				/* 如果满足相似triple条件，加入相似triple-pair集合 */
				if (simElmNum >= 2) {
					weight += 5.0;
					TriplePair tpair = new TriplePair();
					tpair.tripleA = stA;
					tpair.tripleB = stB;
					tpair.simS = simS;
					tpair.simP = simP;
					tpair.simO = simO;
					tpair.sIsMeta = metaS;
					tpair.pIsMeta = metaP;
					tpair.oIsMeta = metaO;
					tpair.weight = weight;
					tPairSet.add(tpair);
				}
			}
		}
				
		long end = System.currentTimeMillis();// 结束计时
		long costtime = end - start;// 统计算法时间
		//System.out.println("getSimTripleCandidate法时间：" + (double) costtime / 1000. + "秒");
		
		return tPairSet;
	}

	/***************************************************************************
	 * Statement中的meta数目
	 **************************************************************************/
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

	private int getResourceType(String s, OntModel m) {
		Resource r = m.getResource(s);
		if (r == null) {
			return -1;// 不是resource
		} else if (r.isLiteral()) {// 文字
			return 4;
		} else if (ontParse.metaURISet.contains(r.getNameSpace())) {// 元语
		// System.out.println("元语："+r.toString());
			return 5;
		} else {
			OntResource ontr = m.getOntResource(r);
			if (!ontParse.isBlankNode(s)) {
				/* 非匿名 */
				if (ontr.isClass()) {
					return 1;// Class
				} else if (ontr.isIndividual()) {
					return 3;// Individual
				} else if (ontr.isProperty()) {
					return 2;// Property
				} else {
					return 4;// 普通的resource
				}
			} else {
				/* 匿名 */
				ArrayList lt = getAnonResourceWithType(s, m);
				int type = 0;
				type = ((Integer) lt.get(1)).intValue();
				if (type == 0) {
					return 4;
				} else {
					return type;
				}
			}
		}
	}

	private ArrayList getAnonResourceWithType(String name, OntModel m) {
		ArrayList anonCnpt;
		ArrayList anonIns;
		ArrayList anonProp;

		if (m == m_source) {
			anonCnpt = s_AnonCnpt;
			anonProp = s_AnonProp;
			anonIns = s_AnonIns;
		} else {
			anonCnpt = t_AnonCnpt;
			anonProp = t_AnonProp;
			anonIns = t_AnonIns;
		}

		ArrayList result = new ArrayList();
		int type = 0;

		OntClass c = null;
		for (Iterator i = anonCnpt.iterator(); i.hasNext();) {
			OntClass cx = (OntClass) i.next();
			if (name.equals(cx.toString())) {
				c = cx;
				break;
			}
		}
		if (c != null) {
			result.add(0, c);
			type = 1;
		} else {
			Individual d = null;
			for (Iterator i = anonIns.iterator(); i.hasNext();) {
				Individual dx = (Individual) i.next();
				if (name.equals(dx.toString())) {
					d = dx;
					break;
				}
			}
			if (d != null) {
				result.add(0, d);
				type = 3;
			} else {
				Property p = null;
				for (Iterator i = anonProp.iterator(); i.hasNext();) {
					Property px = (Property) i.next();
					if (name.equals(px.toString())) {
						p = px;
						break;
					}
				}
				if (p != null) {
					result.add(0, p);
					type = 2;
				}
			}
		}

		/* 如果不是C,I,P的匿名节点 */

		if (type == 0) {
			Resource r = m.getResource(name);
			result.add(0, r);
		}
		result.add(1, type);
		return result;
	}

	/***************************************************************************
	 * 判断三元组的两元素的相似度
	 **************************************************************************/
	private double getElmSim(Resource elmA, Resource elmB, int type) {
		double sim = -1.0;
		String elmNameA = elmA.getLocalName();
		String elmNameB = elmB.getLocalName();
		String fullNameA = elmA.toString();
		String fullNameB = elmB.toString();
		// 非匿名
		if (!elmA.isAnon() && !elmB.isAnon()) {
			String uriA = elmA.getNameSpace();
			String uriB = elmB.getNameSpace();

			if (type == 1) {
				if (uriA.equals(s_baseURI) && uriB.equals(t_baseURI)) {
					// 查Class全局相似度表
					sim = queryCnptSimRaw(elmNameA, elmNameB);
				} else {
					// 查局部相似度表
					sim = queryLocalSimRaw(fullNameA, fullNameB);
				}
			} else if (type == 2) {
				if (uriA.equals(s_baseURI) && uriB.equals(t_baseURI)) {
					// 查Property全局相似度表
					sim = queryPropSimRaw(elmNameA, elmNameB);
				} else {
					// 查局部相似度表
					sim = queryLocalSimRaw(fullNameA, fullNameB);
				}
			} else if (type == 3) {
				if (uriA.equals(s_baseURI) && uriB.equals(t_baseURI)) {
					// 查Instance全局相似度表
					sim = queryInsSimRaw(elmNameA, elmNameB);
				} else {
					// 查局部相似度表
					sim = queryLocalSimRaw(fullNameA, fullNameB);
				}
			} else {
				// 查局部相似度表
				sim = queryLocalSimRaw(fullNameA, fullNameB);
			}
		}
		// 匿名
		else if (elmA.isAnon() && elmB.isAnon()) {
			// 查Anon本地相似度表
			sim = queryLocalSimRaw(elmA.toString(), elmB.toString());
		}
		return sim;
	}

	private double getElmSim(RDFNode elmA, RDFNode elmB, int type) {
		double sim = -1.0;
		String elmNameA = null;
		String elmNameB = null;
		String uriA = null;
		String uriB = null;
		String fullNameA = elmA.toString();
		String fullNameB = elmB.toString();

		// 非匿名
		if (!elmA.isAnon() && !elmB.isAnon()) {
			if (elmA.isURIResource()) {
				elmNameA = elmA.asNode().getLocalName();
				uriA = elmA.asNode().getNameSpace();
			} else {
				elmNameA = elmA.toString();
			}
			if (elmB.isURIResource()) {
				elmNameB = elmB.asNode().getLocalName();
				uriB = elmB.asNode().getNameSpace();
			} else {
				elmNameB = elmB.toString();
			}

			if (type == 1) {
				if (uriA.equals(s_baseURI) && uriB.equals(t_baseURI)) {
					// 查Class全局相似度表
					sim = queryCnptSimRaw(elmNameA, elmNameB);
				} else {
					// 查局部相似度表
					sim = queryLocalSimRaw(fullNameA, fullNameB);
				}

			} else if (type == 2) {
				if (uriA.equals(s_baseURI) && uriB.equals(t_baseURI)) {
					// 查Property全局相似度表
					sim = queryPropSimRaw(elmNameA, elmNameB);
				} else {
					// 查局部相似度表
					sim = queryLocalSimRaw(fullNameA, fullNameB);
				}
			} else if (type == 3) {
				if (uriA.equals(s_baseURI) && uriB.equals(t_baseURI)) {
					// 查Instance全局相似度表
					sim = queryInsSimRaw(elmNameA, elmNameB);
				} else {
					// 查局部相似度表
					sim = queryLocalSimRaw(fullNameA, fullNameB);
				}
			} else {
				// 查局部相似度表
				sim = queryLocalSimRaw(fullNameA, fullNameB);
			}
		}
		// 匿名
		else if (elmA.isAnon() && elmB.isAnon()) {
			// 查Anon本地相似度表
			sim = queryLocalSimRaw(fullNameA, fullNameB);
		}
		return sim;
	}

	/***************************************************************************
	 * 查询全局概念相似矩阵
	 **************************************************************************/
	private double queryCnptSimRaw(String nameA, String nameB) {
		double sim = 0;
		sim = ((Double) cnptSimMap.get(nameA + nameB)).doubleValue();
		return sim;
	}

	/***************************************************************************
	 * 查询全局属性相似矩阵
	 **************************************************************************/
	private double queryPropSimRaw(String nameA, String nameB) {
		double sim = 0;
		sim = ((Double) propSimMap.get(nameA + nameB)).doubleValue();
		return sim;
	}

	/***************************************************************************
	 * 查询全局实例相似矩阵
	 **************************************************************************/
	private double queryInsSimRaw(String nameA, String nameB) {
		double sim = 0;
		sim = ((Double) insSimMap.get(nameA + nameB)).doubleValue();
		return sim;
	}

	/***************************************************************************
	 * 查询局部相似矩阵
	 **************************************************************************/
	private double queryLocalSimRaw(String nameA, String nameB) {
		double sim = 0;

		for (Iterator it = otElmSim.iterator(); it.hasNext();) {
			GraphElmSim pair = (GraphElmSim) it.next();
			if (pair.elmNameA.equals(nameA) && pair.elmNameB.equals(nameB)) {
				sim = pair.sim;
				break;
			}
		}
		return sim;
	}

	/***************************************************************************
	 * 构造相似三元组对的合并图
	 **************************************************************************/
	@SuppressWarnings("unchecked")
	private ArrayList consTriplePairGraph(Set pairSet) {
		long start = System.currentTimeMillis();// 开始计时
		ArrayList result = new ArrayList();

		/* triple pair graph的点集合和边集合 */
		HashMap tgNodes = new HashMap();
		HashMap tgEdges = new HashMap();
		HashMap edgeMap = new HashMap();// 三元组到边的Hash
		HashMap progWeightMap = new HashMap();// 图中元素传播系数

		/* 直接遍历三元组对，构造图 */
		int ct = 0;
		for (Iterator it = pairSet.iterator(); it.hasNext();) {
			TriplePair pair = (TriplePair) it.next();
			PairGraphRes nodeStar = new PairGraphRes();
			PairGraphRes nodeEnd = new PairGraphRes();
			PairGraphRes gEdge = new PairGraphRes();
			nodeStar.resA = pair.tripleA.getSubject();
			nodeStar.resB = pair.tripleB.getSubject();
			nodeStar.sim0 = pair.simS;
			nodeStar.isMeta = pair.sIsMeta;
			nodeStar.cFlag =false;
			nodeEnd.resA = pair.tripleA.getObject();
			nodeEnd.resB = pair.tripleB.getObject();
			nodeEnd.sim0 = pair.simO;
			nodeEnd.isMeta = pair.oIsMeta;
			nodeEnd.cFlag = false;
			gEdge.resA = pair.tripleA.getPredicate();
			gEdge.resB = pair.tripleB.getPredicate();
			gEdge.sim0 = pair.simP;
			gEdge.isMeta = pair.pIsMeta;
			gEdge.cFlag = false;

			String starName = nodeStar.getString();
			String endName = nodeEnd.getString();
			String edgeName = gEdge.getString();

//			System.out.println("构造相似三元组对的合并图 " + ct++);

			/* 如果包含这个点，直接用原来的点来替代 */
			// 遍历已有点集
			if (tgNodes.containsKey(starName)){
				nodeStar = (PairGraphRes)(tgNodes.get(starName));
			}
			if (tgNodes.containsKey(endName)){
				nodeEnd = (PairGraphRes)(tgNodes.get(endName));
			}
			if (tgNodes.containsKey(edgeName)){
				gEdge = (PairGraphRes)(tgNodes.get(edgeName));
			}
			//遍历已有边集
			if (tgEdges.containsKey(starName)){
				nodeStar = (PairGraphRes)(tgEdges.get(starName));
			}
			if (tgEdges.containsKey(endName)){
				nodeEnd = (PairGraphRes)(tgEdges.get(endName));
			}
			if (tgEdges.containsKey(edgeName)){
				gEdge = (PairGraphRes)(tgEdges.get(edgeName));
			}
			
			// 加入Star Node
			if (!tgNodes.containsValue(nodeStar)) {
				/* 如果图中不包含这个点 */
				tgNodes.put(starName,nodeStar);
			}

			// 加入End Node
			if (!tgNodes.containsValue(nodeEnd)) {
				/* 如果图中不包含这个点 */
				tgNodes.put(endName,nodeEnd);
			}

			// 加入Edge Node
			if (!tgEdges.containsValue(gEdge)) {
				/* 如果图中不包含这个点 */
				tgEdges.put(edgeName,gEdge);
			}

			/* 将边和三元组的对应保存在一个表中，方便查询 */
			ArrayList t2PairRes = new ArrayList();
			t2PairRes.add(0, nodeStar);
			t2PairRes.add(1, nodeEnd);
			t2PairRes.add(2, gEdge);
			edgeMap.put(pair, t2PairRes);

			/* 计算传播系数 */
			String key;
			int value;
			// s--p的权重
			key = starName + edgeName;
			// o不是元语
			if (!nodeEnd.isMeta) {
				if (progWeightMap.containsKey(key)) {
					value = ((Integer) progWeightMap.get(key)).intValue();
				} else {
					value = 0;
				}
				progWeightMap.put(key, value + 1);
			}
			// p--o的权重
			key = edgeName + endName;
			// s不是元语
			if (!nodeStar.isMeta) {
				if (progWeightMap.containsKey(key)) {
					value = ((Integer) progWeightMap.get(key)).intValue();
				} else {
					value = 0;
				}
				progWeightMap.put(key, value + 1);
			}
			// s--o的权重
			key = starName + endName;
			// p不是元语
			if (!gEdge.isMeta) {
				if (progWeightMap.containsKey(key)) {
					value = ((Integer) progWeightMap.get(key)).intValue();
				} else {
					value = 0;
				}
				progWeightMap.put(key, value + 1);
			}
		}

		result.add(0, tgNodes);
		result.add(1, tgEdges);
		result.add(2, edgeMap);
		result.add(3, progWeightMap);
		
		long end = System.currentTimeMillis();// 结束计时
//		System.out.println("consTriplePairGraph时间：" + (double) ( end - start) / 1000. + "秒");
		
		return result;
	}

	/***************************************************************************
	 * 相似度的传播 输入：triple-pair图 输出：相似度传播图
	 **************************************************************************/
	private void propagation(HashMap edgeMap, HashMap progWeightMap) {
		long start = System.currentTimeMillis();// 开始计时
		double delta = 0.5;
		int k = 0;

		/*
		 * 子图元素当前的相似度 当前相似度用sim0表示 传播后的相似度用simk表示
		 */
		while (k < maxProgTimes && delta < 0.995) {

			double max = 0;// 最大相似度
			Set usedSet = new HashSet();// 避免多次归一的标记集合

			/* 1.遍历图的边，计算传播的相似度 */
			usedSet.clear();

			for (Iterator itx = edgeMap.values().iterator(); itx.hasNext();) {
				ArrayList t2PairRes = (ArrayList) itx.next();
				PairGraphRes pairS = (PairGraphRes) t2PairRes.get(0);
				PairGraphRes pairO = (PairGraphRes) t2PairRes.get(1);
				PairGraphRes pairP = (PairGraphRes) t2PairRes.get(2);

				String sName = pairS.getString();
				String oName = pairO.getString();
				String pName = pairP.getString();

				String key;
				double weight;

				// 计算s上的相似度传播,相似度从p-o传入
				if (!pairS.isMeta) {
					if (!usedSet.contains(pairS)) {
						pairS.simk = pairS.sim0;
						usedSet.add(pairS);
					}
					key = pName + oName;
					weight = ((Integer) progWeightMap.get(key)).doubleValue();
					pairS.simk += pairP.sim0 * pairO.sim0 / weight;
					max = Math.max(max, pairS.simk);
					if (pairS.simk > 0 && pairS.sim0 == 0) {
						pairS.cFlag = true;
					}
				}

				// 计算p上的相似度传播,相似度从s-o传入
				if (!pairP.isMeta) {
					if (!usedSet.contains(pairP)) {
						pairP.simk = pairP.sim0;
						usedSet.add(pairP);
					}
					key = sName + oName;
					weight = ((Integer) progWeightMap.get(key)).doubleValue();
					pairP.simk += pairS.sim0 * pairO.sim0 / weight;
					max = Math.max(max, pairP.simk);
					if (pairP.simk > 0 && pairP.sim0 == 0) {
						pairP.cFlag = true;
					}
				}

				// 计算o上的相似度传播,相似度从s-p传入
				if (!pairO.isMeta) {
					if (!usedSet.contains(pairO)) {
						pairO.simk = pairO.sim0;
						usedSet.add(pairO);
					}
					key = sName + pName;
					weight = ((Integer) progWeightMap.get(key)).doubleValue();
					pairO.simk += pairS.sim0 * pairP.sim0 / weight;
					max = Math.max(max, pairO.simk);
					if (pairO.simk > 0 && pairO.sim0 == 0) {
						pairO.cFlag = true;
					}
				}
			}

			/* 相似度归一 */
			usedSet.clear();
			for (Iterator itx = edgeMap.values().iterator(); itx.hasNext();) {
				ArrayList t2PairRes = (ArrayList) itx.next();
				PairGraphRes pairS = (PairGraphRes) t2PairRes.get(0);
				PairGraphRes pairO = (PairGraphRes) t2PairRes.get(1);
				PairGraphRes pairP = (PairGraphRes) t2PairRes.get(2);

				if (!pairS.isMeta) {
					if (!usedSet.contains(pairS)) {
						pairS.simk = pairS.simk / max;
						usedSet.add(pairS);
					}
				}
				if (!pairP.isMeta) {
					if (!usedSet.contains(pairP)) {
						pairP.simk = pairP.simk / max;
						usedSet.add(pairP);
					}
				}
				if (!pairO.isMeta) {
					if (!usedSet.contains(pairO)) {
						pairO.simk = pairO.simk / max;
						usedSet.add(pairO);
					}
				}
			}

			/* 判断相似度收敛 */
			delta = 0;
			ArrayList vA = new ArrayList();
			ArrayList vB = new ArrayList();
			usedSet.clear();
			for (Iterator itx = edgeMap.values().iterator(); itx.hasNext();) {
				ArrayList t2PairRes = (ArrayList) itx.next();
				PairGraphRes pairS = (PairGraphRes) t2PairRes.get(0);
				PairGraphRes pairO = (PairGraphRes) t2PairRes.get(1);
				PairGraphRes pairP = (PairGraphRes) t2PairRes.get(2);

				if (!pairS.isMeta) {
					if (!usedSet.contains(pairS)) {
						vA.add(pairS.sim0);
						vB.add(pairS.simk);
						usedSet.add(pairS);
					}
				}
				if (!pairP.isMeta) {
					if (!usedSet.contains(pairP)) {
						vA.add(pairP.sim0);
						vB.add(pairP.simk);
						usedSet.add(pairP);
					}
				}
				if (!pairO.isMeta) {
					if (!usedSet.contains(pairO)) {
						vA.add(pairO.sim0);
						vB.add(pairO.simk);
						usedSet.add(pairO);
					}
				}
			}

			/* 用向量方法判断迭代是否结束 */
			delta = new TfIdfSim().getTextVectorSim(vA, vB);

			/* 更新k次相似度 */
			usedSet.clear();
//			System.out.println("------" + k + "--------");
			for (Iterator itx = edgeMap.values().iterator(); itx.hasNext();) {
				ArrayList t2PairRes = (ArrayList) itx.next();
				PairGraphRes pairS = (PairGraphRes) t2PairRes.get(0);
				PairGraphRes pairO = (PairGraphRes) t2PairRes.get(1);
				PairGraphRes pairP = (PairGraphRes) t2PairRes.get(2);

				if (!pairS.isMeta) {
					if (!usedSet.contains(pairS)) {
						pairS.sim0 = pairS.simk;
						// System.out.println("<"+pairS.resA.toString()+"--"+pairS.resB.toString()+">---"+pairS.sim0);
						usedSet.add(pairS);
					}
				}
				if (!pairP.isMeta) {
					if (!usedSet.contains(pairP)) {
						pairP.sim0 = pairP.simk;
						// System.out.println("<"+pairP.resA.toString()+"--"+pairP.resB.toString()+">---"+pairP.sim0);
						usedSet.add(pairP);
					}
				}
				if (!pairO.isMeta) {
					if (!usedSet.contains(pairO)) {
						pairO.sim0 = pairO.simk;
						// System.out.println("<"+pairO.resA.toString()+"--"+pairO.resB.toString()+">---"+pairO.sim0);
						usedSet.add(pairO);
					}
				}
			}
			k++;
//			System.out.println("当前迭代结果的向量相似度：" + delta);
		}
		
		long end = System.currentTimeMillis();// 结束计时
//		System.out.println("propagation时间：" + (double) (end - start) / 1000.0 + "秒");
		
	}

	/***************************************************************************
	 * 更新候选三元组对中 的对应相似度
	 **************************************************************************/
	private Set updateTriplePairSim(Set tPairSet, HashMap edgeMap) {
		long start = System.currentTimeMillis();// 开始计时
		Set newtpSet = new HashSet();
		/* 遍历候选三元组对 */
		int ct = 0;
		
		for (Iterator it = tPairSet.iterator(); it.hasNext();) {
			TriplePair tpair = (TriplePair) it.next();
			Statement stA = tpair.tripleA;
			Statement stB = tpair.tripleB;
			Resource subA = stA.getSubject();
			Resource subB = stB.getSubject();
			Property propA = stA.getPredicate();
			Property propB = stB.getPredicate();
			RDFNode objA = stA.getObject();
			RDFNode objB = stB.getObject();

			String subAName = subA.toString();
			String subBName = subB.toString();
			String propAName = propA.toString();
			String propBName = propB.toString();
			String objAName = objA.toString();
			String objBName = objB.toString();

//			System.out.println("更新候选三元组对相似度" + ct++);
			/* 遍历保存新相似度的点对 */
			// for (Iterator itx=nodesSet.iterator();itx.hasNext();){
			// PairGraphRes node=(PairGraphRes)itx.next();
			// if (node.resA.toString().equals(subA.toString())
			// && node.resB.toString().equals(subB.toString())){
			// tpair.simS=node.sim0;
			// }
			// if (node.resA.equals(objA.toString())
			// && node.resB.toString().equals(objB.toString())){
			// tpair.simO=node.sim0;
			// }
			// if (node.resA.equals(propA.toString())
			// && node.resB.toString().equals(propB.toString())){
			// tpair.simP=node.sim0;
			// }
			// }
			
			ArrayList t2PairRes = (ArrayList) edgeMap.get(tpair);
			if (!t2PairRes.isEmpty()){
				PairGraphRes pairS = (PairGraphRes) t2PairRes.get(0);
				PairGraphRes pairO = (PairGraphRes) t2PairRes.get(1);
				PairGraphRes pairP = (PairGraphRes) t2PairRes.get(2);
				String sAName = pairS.resA.toString();
				String sBName = pairS.resB.toString();
				String oAName = pairO.resA.toString();
				String oBName = pairO.resB.toString();
				String pAName = pairP.resA.toString();
				String pBName = pairP.resB.toString();
				if (sAName.equals(subAName) && sBName.equals(subBName)) {
					tpair.simS = pairS.sim0;
				}
				if (oAName.equals(objAName) && oBName.equals(objBName)) {
					tpair.simO = pairO.sim0;
				}
				if (pAName.equals(propAName) && pBName.equals(propBName)) {
					tpair.simP = pairP.sim0;
				}		

				/* 将明显错误的triple pair从图中消除,只保留可信的 */
				if (tpair.simS > 0.005 && tpair.simP > 0.005 && tpair.simO > 0.005) {
					newtpSet.add(tpair);
				}
			}
			
		}
		
		long end = System.currentTimeMillis();// 结束计时
//		System.out.println("updateTriplePairSim时间：" + (double) (end - start) / 1000.0 + "秒");
		
		return newtpSet;// 返回过滤不可信的结果
	}

	/***************************************************************************
	 * 更新候选相似三元组
	 **************************************************************************/
	@SuppressWarnings({"unchecked"})
	private Set updateTripleCandidate(Set goodTPSet, Set tPairSet, HashMap edgeMap, HashMap sE2Triple, HashMap tE2Triple) {
		long start = System.currentTimeMillis();// 开始计时
		int ct=0;
		
		sGUpdate = false;// 假设子图不更新
		double weight = 0;
		/** 提取相似对,为判断新的相似度做准备 **/
		Set rawPairs = new HashSet();
		Set rawTriples = new HashSet();
		HashMap rawPairSim = new HashMap();
		for (Iterator it = tPairSet.iterator(); it.hasNext();) {
			TriplePair tpair = (TriplePair) it.next();
			rawTriples.add(tpair.tripleA.toString() + tpair.tripleB.toString());
			String nameA, nameB;
			double sim = 0;
			nameA = tpair.tripleA.getSubject().toString();
			nameB = tpair.tripleB.getSubject().toString();
			sim = tpair.simS;
			if (!rawPairs.contains(nameA + nameB)) {
				rawPairs.add(nameA + nameB);
				rawPairSim.put(nameA + nameB, sim);
			}
			nameA = tpair.tripleA.getPredicate().toString();
			nameB = tpair.tripleB.getPredicate().toString();
			sim = tpair.simP;
			if (!rawPairs.contains(nameA + nameB)) {
				rawPairs.add(nameA + nameB);
				rawPairSim.put(nameA + nameB, sim);
			}
			nameA = tpair.tripleA.getObject().toString();
			nameB = tpair.tripleB.getObject().toString();
			sim = tpair.simO;
			if (!rawPairs.contains(nameA + nameB)) {
				rawPairs.add(nameA + nameB);
				rawPairSim.put(nameA + nameB, sim);
			}
		}
		
		/**1.寻找传播后产生的新相似元素对**/
		ArrayList newEPairSet= new ArrayList();
		for (Iterator it = goodTPSet.iterator(); it.hasNext();) {
			TriplePair tpPair = (TriplePair) it.next();
			ArrayList t2PairRes = (ArrayList)edgeMap.get(tpPair);			
			PairGraphRes pairS = (PairGraphRes) t2PairRes.get(0);
			PairGraphRes pairO = (PairGraphRes) t2PairRes.get(1);
			PairGraphRes pairP = (PairGraphRes) t2PairRes.get(2);

			/* 判断有没有新相似度对,并记录 */
			if (pairS.cFlag) {newEPairSet.add(pairS);}
			if (pairO.cFlag) {newEPairSet.add(pairO);}
			if (pairP.cFlag) {newEPairSet.add(pairP);}
		}
		
		/**2.遍历新相似元素对，产生新的相似三元组**/
		for (Iterator itx=newEPairSet.iterator();itx.hasNext();){
			PairGraphRes pair=(PairGraphRes)itx.next();
			Object eA=pair.resA;
			Object eB=pair.resB;
			double fixSim=pair.sim0;
			String eAName=eA.toString();
			String eBName=eB.toString();
			
			/**2.1.寻找eA和eB的相关三元组**/
			ArrayList[] eAStm=new ArrayList[3];
			ArrayList[] eBStm=new ArrayList[3];
			eAStm[0]=((ArrayList[])sE2Triple.get(eAName))[0];
			eAStm[1]=((ArrayList[])sE2Triple.get(eAName))[1];
			eAStm[2]=((ArrayList[])sE2Triple.get(eAName))[2];
			eBStm[0]=((ArrayList[])tE2Triple.get(eBName))[0];
			eBStm[1]=((ArrayList[])tE2Triple.get(eBName))[1];
			eBStm[2]=((ArrayList[])tE2Triple.get(eBName))[2];
			
			/**2.2.寻找新产生的相似三元组对**/
			/**依次处理eA和eB出现在S、P和O位置的相似三元组对**/
			for (int pos=0;pos<3;pos++){
				for (Iterator itA=eAStm[pos].iterator();itA.hasNext();){//遍历eAStm
					Statement stA=(Statement)itA.next();
					/*分离三元组*/
					Resource subA = stA.getSubject();
					Property propA = stA.getPredicate();
					RDFNode objA = stA.getObject();
					String subAName = subA.toString();
					String propAName = propA.toString();
					String objAName = objA.toString();
					String urlSA = null, urlPA = null, urlOA = null;
					if (subA.isURIResource()) {urlSA = subA.getNameSpace();}
					if (propA.isURIResource()) {urlPA = propA.getNameSpace();}
					if (objA.isURIResource()) {urlOA = objA.asNode().getNameSpace();}
					String stAName = stA.toString();
					
					for (Iterator itB=eBStm[pos].iterator();itB.hasNext();){//遍历eBStm
						Statement stB=(Statement)itB.next();
						/*分离三元组*/
						Resource subB = stB.getSubject();
						Property propB = stB.getPredicate();
						RDFNode objB = stB.getObject();
						String subBName = subB.toString();
						String propBName = propB.toString();
						String objBName = objB.toString();
						String urlSB = null, urlPB = null, urlOB = null;
						if (subB.isURIResource()) {urlSB = subB.getNameSpace();}
						if (propB.isURIResource()) {urlPB = propB.getNameSpace();}
						if (objB.isURIResource()) {urlOB = objB.asNode().getNameSpace();}
						String stBName = stB.toString();

						/*先找到合法的三元组对*/
						int typeA, typeB;
						double simS, simP, simO;
						boolean metaS, metaP, metaO;
						int simElmNum = 0;
						/*判断S-S*/
						typeA = getResourceType(subAName, m_source);
						typeB = getResourceType(subBName, m_target);
						simS = -1.0;
						metaS = false;
						if (pos==0){
							simS=fixSim;
						}
						else {
							if (typeA == typeB) {
								// 相同的类型，需要确定相似度
								/* 判断当前tpair是否包含该对 */
								if (rawPairs.contains(subAName + subBName)) {
									/* 已经包含,采用计算的相似度 */
									simS = ((Double) rawPairSim.get(subAName+ subBName)).doubleValue();
								} else {
									/* 没有包含,采用 */
									simS = getElmSim(subA, subB, typeA);
								}
							}
						}
						if (simS < 0) {
							simS = 0;
						}
						if (simS > 0) {
							simElmNum++;
						}

						/*判断p-p*/
						typeA = getResourceType(propAName, m_source);
						typeB = getResourceType(propBName, m_target);
						simP = -1.0;
						metaP = false;
						if (pos==1){
							simP=fixSim;
						}
						else{
							if (ontLngURI.contains(urlPA) && ontLngURI.contains(urlPB)) {//判断是不是元语
								if (propAName.equals(propBName)) {// 是否是相同的元语
									simP = 1.0;
									metaP = true;
								} else {
									// 元语如果不相同，直接跳过当前triple-pair
									continue;
								}
							}							
							else if (!ontLngURI.contains(urlPA)	&& !ontLngURI.contains(urlPB)) {
								//都不是元语的情况
								if (typeA == typeB) {// 相同的类型，需要确定相似度
									/* 判断当前tpair是否包含该对 */
									if (rawPairs.contains(propAName + propBName)) {
										/* 已经包含,采用计算的相似度 */
										simP = ((Double) rawPairSim.get(propAName+propBName)).doubleValue();
									} else {
										/* 没有包含,采用 */
										simP = getElmSim(propA, propB, typeA);
									}
								}
							}
						}
						
						if (simP < 0) {
							simP = 0;
						}
						if (simP > 0) {
							simElmNum++;
						}

						// 判断o-o
						typeA = getResourceType(objAName, m_source);
						typeB = getResourceType(objBName, m_target);
						simO = -1.0;
						metaO = false;
						if (pos==2){
							simO=fixSim;
						}
						else {
							if (ontLngURI.contains(urlOA) && ontLngURI.contains(urlOB)) {//判断是不是元语
								if (objAName.equals(objBName)) {// 是否是相同的元语
									simO = 1.0;
									metaO = true;
								} else {
									// 元语如果不相同，直接跳过当前triple-pair
									continue;
								}
							}
							else if (!ontLngURI.contains(urlOA)	&& !ontLngURI.contains(urlOB)) {// 都不是元语的情况
								if (typeA == typeB) {// 相同的类型，需要确定相似度									
									/* 判断当前tpair是否包含该对 */
									if (rawPairs.contains(objAName + objBName)) {
										/* 已经包含,采用计算的相似度 */
										simO = ((Double) rawPairSim.get(objAName+ objBName)).doubleValue();
									} else {
										/* 没有包含,采用 */
										simO = getElmSim(objA, objB, typeA);
									}
								}
							}
						}
						
						if (simO < 0) {
							simO = 0;
						}
						if (simO > 0) {
							simElmNum++;
						}

						/* 如果是新的相似triple，加入triple-pair集合 */
						if (simElmNum >= 2	&& !rawTriples.contains(stAName + stBName)) {
							weight += 5.0;
							TriplePair tpair = new TriplePair();
							tpair.tripleA = stA;
							tpair.tripleB = stB;
							tpair.simS = simS;
							tpair.simP = simP;
							tpair.simO = simO;
							tpair.sIsMeta = metaS;
							tpair.pIsMeta = metaP;
							tpair.oIsMeta = metaO;
							tpair.weight = weight;
							tPairSet.add(tpair);

							rawTriples.add(stAName + stBName);
//							System.out.println("产生新相似三元组对"+ct++);

							// 子图发生更新
							sGUpdate = true;
						}
						
					}
				}
			}
		}	

		long end = System.currentTimeMillis();// 结束计时
//		System.out.println("updateTripleCandidate时间：" + (double) (end - start) / 1000. + "秒");
		
		return tPairSet;
	}

	/***************************************************************************
	 * 判断局部迭代是否结束 false:还没有收敛 true:已经收敛
	 * curPos:当前的子图位置
	 * sDirection:是否为从source到target
	 **************************************************************************/
	private boolean isLocalConverge(HashMap edgeMap, int curPos, boolean sDirection) {
		boolean flag = false;
		double delta = 0;

		// 做一个事先的过滤
		// cnptSimK1 = new
		// StableMarriageFilter().run(cnptSimK1,s_cnptNum,t_cnptNum);
		// propSimK1 = new
		// StableMarriageFilter().run(propSimK1,s_propNum,t_propNum);

		ArrayList vA = new ArrayList();
		ArrayList vB = new ArrayList();
		
		/**1.先构造元素对--相似值的hash表**/
		HashMap EVMap=new HashMap();
		for (Iterator it = edgeMap.values().iterator(); it.hasNext();) {
			ArrayList t2PairRes = (ArrayList) it.next();
			PairGraphRes pairS = (PairGraphRes) t2PairRes.get(0);
			PairGraphRes pairO = (PairGraphRes) t2PairRes.get(1);
			PairGraphRes pairP = (PairGraphRes) t2PairRes.get(2);
			
			if (!EVMap.containsKey(pairS.getString()) && pairS.sim0>0.001){
				EVMap.put(pairS.getString(),pairS.sim0);
			}
			if (!EVMap.containsKey(pairO.getString()) && pairO.sim0>0.001){
				EVMap.put(pairO.getString(),pairO.sim0);
			}
			if (!EVMap.containsKey(pairP.getString()) && pairS.sim0>0.001){
				EVMap.put(pairP.getString(),pairP.sim0);
			}
		}
		/**2.记录当前传播后的相似度值**/
		if (fromCnpt){//处理概念相似
			if (sDirection){//从源本体传播
				for (int j = 0; j < t_cnptNum; j++) {
					String key=s_baseURI + s_cnptName[curPos]+t_baseURI + t_cnptName[j];
					if (EVMap.containsKey(key)){
						vA.add(cnptSimRow[curPos][j]);
						cnptSimRow[curPos][j] = (Double)(EVMap.get(key));
						vB.add(cnptSimRow[curPos][j]);
					}
					else{
						vA.add(cnptSimRow[curPos][j]);
						cnptSimRow[curPos][j] =0;
						vB.add(cnptSimRow[curPos][j]);
					}
				}
			}
			else {//从目标本体传播
				for (int j = 0; j < s_cnptNum; j++) {
					String key=s_baseURI + s_cnptName[j]+t_baseURI + t_cnptName[curPos];
					if (EVMap.containsKey(key)){
						vA.add(cnptSimCol[j][curPos]);
						cnptSimCol[j][curPos] = (Double)(EVMap.get(key));
						vB.add(cnptSimCol[j][curPos]);
					}
					else{
						vA.add(cnptSimCol[j][curPos]);
						cnptSimCol[j][curPos] =0;
						vB.add(cnptSimCol[j][curPos]);
					}
				}
			}
		}
		else {//处理属性相似
			if (sDirection){//从源本体传播
				for (int j = 0; j < t_propNum; j++) {
					String key=s_baseURI + s_propName[curPos]+t_baseURI + t_propName[j];
					if (EVMap.containsKey(key)){
						vA.add(propSimRow[curPos][j]);
						propSimRow[curPos][j] = (Double)(EVMap.get(key));
						vB.add(propSimRow[curPos][j]);
					}
					else{
						vA.add(propSimRow[curPos][j]);
						propSimRow[curPos][j] =0;
						vB.add(propSimRow[curPos][j]);
					}
				}
			}
			else {//从目标本体传播
				for (int j = 0; j < s_propNum; j++) {
					String key=s_baseURI + s_propName[j]+t_baseURI + t_propName[curPos];
					if (EVMap.containsKey(key)){
						vA.add(propSimCol[j][curPos]);
						propSimCol[j][curPos] = (Double)(EVMap.get(key));
						vB.add(propSimCol[j][curPos]);
					}
					else{
						vA.add(propSimCol[j][curPos]);
						propSimCol[j][curPos] =0;
						vB.add(propSimCol[j][curPos]);
					}
				}
			}
		}
		
		/**3.构造向量，判断迭代是否结束**/
		delta = new TfIdfSim().getTextVectorSim(vA, vB);

//		System.out.println("局部相似矩阵的迭代收敛相似度:" + delta);
		if (delta > 0.99) {flag = true;} 
		return flag;
	}
	
	/***************************************************************************
	 * 判断全局迭代是否结束 false:还没有收敛 true:已经收敛
	 **************************************************************************/
	private boolean isGlobeConverge() {
		boolean flag = false;
		double delta = 0;
		
		for (int i=0;i<s_cnptNum;i++){
			int num=0;
			double max=0.00001;
			double sum=0.00001;
			for (int j=0;j<t_cnptNum;j++){
				if (cnptSimRow[i][j]>0.001) {num++;sum+=cnptSimRow[i][j];}
				if (cnptSimRow[i][j]>max){max=cnptSimRow[i][j];}
			}
			double idf=Math.log((t_cnptNum+1.0)/(num+1.0))/Math.log(t_cnptNum+1.0);
			idf=1.0/(1+Math.exp(-3.0*idf));
			for (int j=0;j<t_cnptNum;j++){
				double tf= cnptSimRow[i][j]/max;
//				cnptSimRow[i][j]=cnptSimRow[i][j]*tf*Math.pow(idf,1.0/Math.log(num+1.01)*Math.log(2.0));
				cnptSimRow[i][j]=cnptSimRow[i][j]*tf*idf;
			}
		}
		
		for (int i=0;i<t_cnptNum;i++){
			int num=0;			
			double max=0.00001;
			double sum=0.00001;
			for (int j=0;j<s_cnptNum;j++){
				if (cnptSimCol[j][i]>0.001) {num++;sum+=cnptSimCol[j][i];}
				if (cnptSimCol[j][i]>max){max=cnptSimCol[j][i];}
			}
			double idf=Math.log((s_cnptNum+1.0)/(num+1.0))/Math.log(s_cnptNum+1.0);
			for (int j=0;j<s_cnptNum;j++){
				double tf= cnptSimCol[j][i]/max;
				idf=1.0/(1+Math.exp(-3.0*idf));
				cnptSimCol[j][i]=cnptSimCol[j][i]*tf*idf;
			}
		}
		
		for (int i=0;i<s_propNum;i++){
			int num=0;
			double max=0.00001;
			double sum=0.00001;
			for (int j=0;j<t_propNum;j++){
				if (propSimRow[i][j]>0.001) {num++;sum+=propSimRow[i][j];}
				if (propSimRow[i][j]>max){max=propSimRow[i][j];}
			}
			double idf=Math.log((t_propNum+1.0)/(num+1.0))/Math.log(t_propNum+1.0);
			for (int j=0;j<t_propNum;j++){
				double tf= propSimRow[i][j]/max;
				idf=1.0/(1+Math.exp(-3.0*idf));
				propSimRow[i][j]=propSimRow[i][j]*tf*idf;
			}
		}
		
		for (int i=0;i<t_propNum;i++){
			int num=0;
			double max=0.00001;
			double sum=0.00001;
			for (int j=0;j<s_propNum;j++){
				if (propSimCol[j][i]>0.001) {num++;sum+=propSimCol[j][i];}
				if (propSimCol[j][i]>max){max=propSimCol[j][i];}
			}
			double idf=Math.log((s_propNum+1.0)/(num+1.0))/Math.log(s_propNum+1.0);
			for (int j=0;j<s_propNum;j++){
				double tf= propSimCol[j][i]/max;
				idf=1.0/(1+Math.exp(-3.0*idf));
				propSimCol[j][i]=propSimCol[j][i]*tf*idf;
			}
		}
		
		/** 0.1.合并两个方向的相似度* */
		for (int i=0;i<s_cnptNum;i++){
			for (int j=0;j<t_cnptNum;j++){
			cnptSimK0[i][j]=(cnptSimRow[i][j]+cnptSimCol[i][j])/2.0;
			}
		}
		for (int i=0;i<s_propNum;i++){
			for (int j=0;j<t_propNum;j++){
				propSimK0[i][j]=(propSimRow[i][j]+propSimCol[i][j])/2.0;
			}
		}
		
		/**0.5.对相似结果进行估计处理**/
		/*概念：统计行列的最大值*/
		double[] cmaxrow=new double[s_cnptNum];
		double[] cmaxcol=new double[t_cnptNum];
		double[] cNrow=new double[s_cnptNum];
		double[] cNcol=new double[t_cnptNum];
		for (int i=0;i<s_cnptNum;i++) {cmaxrow[i]=0;cNrow[i]=0;}
		for (int i=0;i<t_cnptNum;i++) {cmaxcol[i]=0;cNcol[i]=0;}
		for (int i=0;i<s_cnptNum;i++){
			for (int j=0;j<t_cnptNum;j++){
				cmaxrow[i]=Math.max(cmaxrow[i],cnptSimRow[i][j]);
				cmaxcol[j]=Math.max(cmaxcol[j],cnptSimCol[i][j]);
				if (cnptSimK0[i][j]>0.001){cNrow[i]++;cNcol[j]++;}
			}
		}
		for (int i=0;i<s_cnptNum;i++){			
			for (int j=0;j<t_cnptNum;j++){
				double tf=cnptSimK0[i][j]/Math.max(cmaxrow[i],cmaxcol[j]);
				double idf=Math.log((s_cnptNum+t_cnptNum+1.0)/(cNrow[i]+cNcol[j]+1.0))/Math.log(s_cnptNum+t_cnptNum+1.0);
				if (cnptSimK0[i][j]>0){
					cnptSimK0[i][j]=cnptSimK0[i][j]*Math.pow(cnptSimK0[i][j]/Math.max(cmaxrow[i],cmaxcol[j]),1.8);
					//      /Math.log(cNrow[i]+cNcol[j])/Math.log(2.0);
//					cnptSimK0[i][j]=cnptSimK0[i][j]*tf*idf;
				}				
			}
		}
		/*属性：统计行列的最大值*/
		double[] pmaxrow=new double[s_propNum];
		double[] pmaxcol=new double[t_propNum];
		double[] pNrow=new double[s_propNum];
		double[] pNcol=new double[t_propNum];
		for (int i=0;i<s_propNum;i++) {pmaxrow[i]=0;pNrow[i]=0;}
		for (int i=0;i<t_propNum;i++) {pmaxcol[i]=0;pNcol[i]=0;}
		for (int i=0;i<s_propNum;i++){
			for (int j=0;j<t_propNum;j++){
				pmaxrow[i]=Math.max(pmaxrow[i],propSimRow[i][j]);
				pmaxcol[j]=Math.max(pmaxcol[j],propSimCol[i][j]);
				if (propSimK0[i][j]>0.0001){pNrow[i]++;pNcol[j]++;}
			}
		}
		for (int i=0;i<s_propNum;i++){
			for (int j=0;j<t_propNum;j++){
				double tf=propSimK0[i][j]/Math.max(pmaxrow[i],pmaxcol[j]);
				double idf=Math.log((s_propNum+t_propNum+1.0)/(pNrow[i]+pNcol[j]+1.0))/Math.log(s_propNum+t_propNum+1.0);
				if (propSimK0[i][j]>0){
					propSimK0[i][j]=propSimK0[i][j]
					      *Math.pow(propSimK0[i][j]/Math.max(pmaxrow[i],pmaxcol[j]),1.8);
					    //  /Math.log(pNrow[i]+pNcol[j]);
//					propSimK0[i][j]=propSimK0[i][j]*tf*idf;
				}				
			}
		}

//		new SimDataVisual().visualize(cnptSimK0, s_cnptNum,t_cnptNum);
//		new SimDataVisual().visualize(propSimK0, s_propNum,t_propNum);
		// 做一个事先的过滤
//		 cnptSimK0 = new StableMarriageFilter().run(cnptSimK0,s_cnptNum,t_cnptNum);
//		 propSimK0 = new StableMarriageFilter().run(propSimK0,s_propNum,t_propNum);
		 cnptSimK0 = new SimpleFilter().maxValueFilter(s_cnptNum,t_cnptNum, cnptSimK0, 0.001);
		 propSimK0 = new SimpleFilter().maxValueFilter(s_propNum,t_propNum, propSimK0, 0.001);

		ArrayList vA = new ArrayList();
		ArrayList vB = new ArrayList();
		/** 1.构造向量，判断迭代是否结束**/
		for (int i = 0; i < s_cnptNum; i++) {
			for (int j = 0; j < t_cnptNum; j++) {
				vA.add(cnptSimK0[i][j]);
				vB.add(cnptSimRaw[i][j]);
				cnptSimRaw[i][j] = cnptSimK0[i][j];// 同时更新相似度
			}
		}

		for (int i = 0; i < s_propNum; i++) {
			for (int j = 0; j < t_propNum; j++) {
				vA.add(propSimK0[i][j]);
				vB.add(propSimRaw[i][j]);
				propSimRaw[i][j] = propSimK0[i][j];// 同时更新相似度
			}
		}

		delta = new TfIdfSim().getTextVectorSim(vA, vB);

//		System.out.println("全局相似矩阵的迭代收敛相似度:" + delta);
		if (delta > 0.99) {flag = true;} 
		return flag;
	}

	/******************
	 * 通过判断是否得到新的相似对，来估计传播过程是否能结束
	 *****************/
	private boolean isSameCTPSet(Set newCTPSet, Set oldCTPSet) {
		if (newCTPSet.size() != oldCTPSet.size()) {//发生更新
			return true;
		}

		boolean result = false;//假设没发生更新
		for (Iterator itx = newCTPSet.iterator(); itx.hasNext();) {
			TriplePair newtpair = (TriplePair) itx.next();
			Statement newstA = newtpair.tripleA;
			Statement newstB = newtpair.tripleB;

			String newName = newstA.toString() + newstB.toString();

			boolean cFlag = false;

			for (Iterator ity = oldCTPSet.iterator(); ity.hasNext();) {
				TriplePair oldtpair = (TriplePair) ity.next();
				Statement oldstA = oldtpair.tripleA;
				Statement oldstB = oldtpair.tripleB;

				String oldName = oldstA.toString() + oldstB.toString();

				if (newName.equals(oldName)) {
					cFlag = true;
					break;
				}
			}
			if (!cFlag) {
				result = true;
				break;
			}
		}

		return result;
	}
	
	/*相似度传播前的数据结构预处理*/
	@SuppressWarnings("unchecked")
	private void dataStruProc()
	{
		ArrayList lt=null;
		
		/**1.克隆原始相似度**/
		cnptSimK0 = cnptSimRaw.clone();
		cnptSimRow = cnptSimRaw.clone();
		cnptSimCol = cnptSimRaw.clone();
		for (int i = 0; i < s_cnptNum; i++) {
			if (cnptSimK0[i] != null) {cnptSimK0[i] = cnptSimRaw[i].clone();}
			if (cnptSimRow[i] != null) {cnptSimRow[i] = cnptSimRaw[i].clone();}
			if (cnptSimCol[i] != null) {cnptSimCol[i] = cnptSimRaw[i].clone();}
		}
		propSimK0 = propSimRaw.clone();
		propSimRow = propSimRaw.clone();
		propSimCol = propSimRaw.clone();
		for (int i = 0; i < s_propNum; i++) {
			if (propSimK0[i] != null) {propSimK0[i] = propSimRaw[i].clone();}
			if (propSimRow[i] != null) {propSimRow[i] = propSimRaw[i].clone();}
			if (propSimCol[i] != null) {propSimCol[i] = propSimRaw[i].clone();}
		}
		
		/**2.1 Hash表：元素<-->相似元素
		 * 2.2 Hash表：相似元素<-->相似度**/
		/**概念相似矩阵Hash表**/
		/**C-C hash table**/
		cnptSimMap = new HashMap();
		CCMap = new HashMap();
		for (int i = 0; i < s_cnptNum; i++) {
			String sName=s_baseURI+s_cnptName[i];
			for (int j = 0; j < t_cnptNum; j++) {
				String tName=t_baseURI+t_cnptName[j];
				cnptSimMap.put(s_cnptName[i] + t_cnptName[j], cnptSimRaw[i][j]);
				if (cnptSimRaw[i][j]>0.01){
					if (!CCMap.containsKey(sName)){
						lt=new ArrayList();
					}
					else {
						lt=(ArrayList)CCMap.get(sName);
					}
					lt.add(tName);
					CCMap.put(sName,lt);
				}
			}
		}
		
		/**关系相似矩阵Hash表**/
		/**P-P hash table**/
		propSimMap = new HashMap();
		PPMap = new HashMap();
		for (int i = 0; i < s_propNum; i++) {
			String sName=s_baseURI+s_propName[i];
			for (int j = 0; j < t_propNum; j++) {
				String tName=t_baseURI+t_propName[j];
				propSimMap.put(s_propName[i] + t_propName[j], propSimRaw[i][j]);
				if (propSimRaw[i][j]>0.01){
					if (!PPMap.containsKey(sName)){
						lt=new ArrayList();
					}
					else {
						lt=(ArrayList)PPMap.get(sName);
					}
					lt.add(tName);
					PPMap.put(sName,lt);
				}
			}
		}
		
		/**实例相似矩阵Hash表**/
		/**I-I hash table**/
		insSimMap = new HashMap();
		IIMap = new HashMap();
		for (int i = 0; i < s_insNum; i++) {
			String sName=s_baseURI+s_insName[i];
			for (int j = 0; j < t_insNum; j++) {
				String tName=t_baseURI+t_insName[j];
				insSimMap.put(s_insName[i] + t_insName[j], insSimRaw[i][j]);
				if (insSimRaw[i][j]>0.01){
					if (!IIMap.containsKey(sName)){
						lt=new ArrayList();
					}
					else {
						lt=(ArrayList)IIMap.get(sName);
					}
					lt.add(tName);
					IIMap.put(sName,lt);
				}
			}
		}
		
		/**OtherE-OtherE hash table**/
		LLMap = new HashMap();
		for (Iterator it = otElmSim.iterator(); it.hasNext();) {
			GraphElmSim pair = (GraphElmSim) it.next();
			if (pair.sim>0.05){
				if (!LLMap.containsKey(pair.elmNameA)){
					lt=new ArrayList();
				}
				else {
					lt=(ArrayList)LLMap.get(pair.elmNameA);
				}
			}
			lt.add(pair.elmNameB);
			LLMap.put(pair.elmNameA,lt);
		}
		
		/**合并“元素--相似元素”Hash表**/
		EEMap = new HashMap();
		EEMap.putAll(CCMap);
		EEMap.putAll(PPMap);
		EEMap.putAll(IIMap);
		EEMap.putAll(LLMap);
		
		/**3. Hash表:元素<-->相关三元组**/
		sCnptE2Triple = new HashMap();
		ArrayList[] trLt;
		for (Iterator itx = s_cnptStm.iterator(); itx.hasNext();) {
			Statement st = (Statement) itx.next();
			// 分离三元组
			Resource sub = st.getSubject();
			Property prop = st.getPredicate();
			RDFNode obj = st.getObject();
			String subName = sub.toString();
			String propName = prop.toString();
			String objName = obj.toString();
			String urlS = null, urlP = null, urlO = null;
			if (sub.isURIResource()) {
				urlS = sub.getNameSpace();
			}
			if (prop.isURIResource()) {
				urlP = prop.getNameSpace();
			}
			if (obj.isURIResource()) {
				urlO = obj.asNode().getNameSpace();
			}
			
			trLt=new ArrayList[3];
			trLt[0]=new ArrayList();
			trLt[1]=new ArrayList();
			trLt[2]=new ArrayList();
			if (!ontLngURI.contains(urlS)){
				if (sCnptE2Triple.containsKey(subName)){
					trLt=(ArrayList[])sCnptE2Triple.get(subName);
				}
				trLt[0].add(st);
				sCnptE2Triple.put(subName,trLt);
			}
			trLt=new ArrayList[3];
			trLt[0]=new ArrayList();
			trLt[1]=new ArrayList();
			trLt[2]=new ArrayList();
			if (!ontLngURI.contains(urlP)){
				if (sCnptE2Triple.containsKey(propName)){
					trLt=(ArrayList[])sCnptE2Triple.get(propName);
				}
				trLt[1].add(st);
				sCnptE2Triple.put(propName,trLt);
			}
			trLt=new ArrayList[3];
			trLt[0]=new ArrayList();
			trLt[1]=new ArrayList();
			trLt[2]=new ArrayList();
			if (!ontLngURI.contains(urlO)){
				if (sCnptE2Triple.containsKey(objName)){
					trLt=(ArrayList[])sCnptE2Triple.get(objName);
				}
				trLt[2].add(st);
				sCnptE2Triple.put(objName,trLt);
			}			
		}
		
		sPropE2Triple = new HashMap();
		for (Iterator itx = s_propStm.iterator(); itx.hasNext();) {
			Statement st = (Statement) itx.next();
			// 分离三元组
			Resource sub = st.getSubject();
			Property prop = st.getPredicate();
			RDFNode obj = st.getObject();
			String subName = sub.toString();
			String propName = prop.toString();
			String objName = obj.toString();
			String urlS = null, urlP = null, urlO = null;
			if (sub.isURIResource()) {
				urlS = sub.getNameSpace();
			}
			if (prop.isURIResource()) {
				urlP = prop.getNameSpace();
			}
			if (obj.isURIResource()) {
				urlO = obj.asNode().getNameSpace();
			}
			
			trLt=new ArrayList[3];
			trLt[0]=new ArrayList();
			trLt[1]=new ArrayList();
			trLt[2]=new ArrayList();
			if (!ontLngURI.contains(urlS)){
				if (sPropE2Triple.containsKey(subName)){
					trLt=(ArrayList[])sPropE2Triple.get(subName);
				}
				trLt[0].add(st);
				sPropE2Triple.put(subName,trLt);
			}
			trLt=new ArrayList[3];
			trLt[0]=new ArrayList();
			trLt[1]=new ArrayList();
			trLt[2]=new ArrayList();
			if (!ontLngURI.contains(urlP)){
				if (sPropE2Triple.containsKey(propName)){
					trLt=(ArrayList[])sPropE2Triple.get(propName);
				}
				trLt[1].add(st);
				sPropE2Triple.put(propName,trLt);
			}
			trLt=new ArrayList[3];
			trLt[0]=new ArrayList();
			trLt[1]=new ArrayList();
			trLt[2]=new ArrayList();
			if (!ontLngURI.contains(urlO)){
				if (sPropE2Triple.containsKey(objName)){
					trLt=(ArrayList[])sPropE2Triple.get(objName);
				}
				trLt[2].add(st);
				sPropE2Triple.put(objName,trLt);
			}			
		}
		
		tCnptE2Triple = new HashMap();
		for (Iterator itx = t_cnptStm.iterator(); itx.hasNext();) {
			Statement st = (Statement) itx.next();
			// 分离三元组
			Resource sub = st.getSubject();
			Property prop = st.getPredicate();
			RDFNode obj = st.getObject();
			String subName = sub.toString();
			String propName = prop.toString();
			String objName = obj.toString();
			String urlS = null, urlP = null, urlO = null;
			if (sub.isURIResource()) {
				urlS = sub.getNameSpace();
			}
			if (prop.isURIResource()) {
				urlP = prop.getNameSpace();
			}
			if (obj.isURIResource()) {
				urlO = obj.asNode().getNameSpace();
			}
			
			trLt=new ArrayList[3];
			trLt[0]=new ArrayList();
			trLt[1]=new ArrayList();
			trLt[2]=new ArrayList();
			if (!ontLngURI.contains(urlS)){
				if (tCnptE2Triple.containsKey(subName)){
					trLt=(ArrayList[])tCnptE2Triple.get(subName);
				}
				trLt[0].add(st);
				tCnptE2Triple.put(subName,trLt);
			}
			trLt=new ArrayList[3];
			trLt[0]=new ArrayList();
			trLt[1]=new ArrayList();
			trLt[2]=new ArrayList();
			if (!ontLngURI.contains(urlP)){
				if (tCnptE2Triple.containsKey(propName)){
					trLt=(ArrayList[])tCnptE2Triple.get(propName);
				}
				trLt[1].add(st);
				tCnptE2Triple.put(propName,trLt);
			}
			trLt=new ArrayList[3];
			trLt[0]=new ArrayList();
			trLt[1]=new ArrayList();
			trLt[2]=new ArrayList();
			if (!ontLngURI.contains(urlO)){
				if (tCnptE2Triple.containsKey(objName)){
					trLt=(ArrayList[])tCnptE2Triple.get(objName);
				}
				trLt[2].add(st);
				tCnptE2Triple.put(objName,trLt);
			}			
		}
		
		tPropE2Triple = new HashMap();
		for (Iterator itx = t_propStm.iterator(); itx.hasNext();) {
			Statement st = (Statement) itx.next();
			// 分离三元组
			Resource sub = st.getSubject();
			Property prop = st.getPredicate();
			RDFNode obj = st.getObject();
			String subName = sub.toString();
			String propName = prop.toString();
			String objName = obj.toString();
			String urlS = null, urlP = null, urlO = null;
			if (sub.isURIResource()) {
				urlS = sub.getNameSpace();
			}
			if (prop.isURIResource()) {
				urlP = prop.getNameSpace();
			}
			if (obj.isURIResource()) {
				urlO = obj.asNode().getNameSpace();
			}
			
			trLt=new ArrayList[3];
			trLt[0]=new ArrayList();
			trLt[1]=new ArrayList();
			trLt[2]=new ArrayList();
			if (!ontLngURI.contains(urlS)){
				if (tPropE2Triple.containsKey(subName)){
					trLt=(ArrayList[])tPropE2Triple.get(subName);
				}
				trLt[0].add(st);
				tPropE2Triple.put(subName,trLt);
			}
			trLt=new ArrayList[3];
			trLt[0]=new ArrayList();
			trLt[1]=new ArrayList();
			trLt[2]=new ArrayList();
			if (!ontLngURI.contains(urlP)){
				if (tPropE2Triple.containsKey(propName)){
					trLt=(ArrayList[])tPropE2Triple.get(propName);
				}
				trLt[1].add(st);
				tPropE2Triple.put(propName,trLt);
			}
			trLt=new ArrayList[3];
			trLt[0]=new ArrayList();
			trLt[1]=new ArrayList();
			trLt[2]=new ArrayList();
			if (!ontLngURI.contains(urlO)){
				if (tPropE2Triple.containsKey(objName)){
					trLt=(ArrayList[])tPropE2Triple.get(objName);
				}
				trLt[2].add(st);
				tPropE2Triple.put(objName,trLt);
			}			
		}
		
		/**4. Hash表:元素<-->所在语义子图相关三元组**/
		sSepCnptE2Triple=new HashMap[s_cnptNum];
		for (int i=0;i<s_cnptNum;i++){
			sSepCnptE2Triple[i]=new HashMap();
			for (Iterator it=s_cnptSubG[i].stmList.iterator();it.hasNext();){
				Statement st=(Statement)it.next();
				// 分离三元组
				Resource sub = st.getSubject();
				Property prop = st.getPredicate();
				RDFNode obj = st.getObject();
				String subName = sub.toString();
				String propName = prop.toString();
				String objName = obj.toString();
				String urlS = null, urlP = null, urlO = null;
				if (sub.isURIResource()) {
					urlS = sub.getNameSpace();
				}
				if (prop.isURIResource()) {
					urlP = prop.getNameSpace();
				}
				if (obj.isURIResource()) {
					urlO = obj.asNode().getNameSpace();
				}
				
				trLt=new ArrayList[3];
				trLt[0]=new ArrayList();
				trLt[1]=new ArrayList();
				trLt[2]=new ArrayList();
				if (!ontLngURI.contains(urlS)){
					if (sSepCnptE2Triple[i].containsKey(subName)){
						trLt=(ArrayList[])sSepCnptE2Triple[i].get(subName);
					}
					trLt[0].add(st);
					sSepCnptE2Triple[i].put(subName,trLt);
				}
				trLt=new ArrayList[3];
				trLt[0]=new ArrayList();
				trLt[1]=new ArrayList();
				trLt[2]=new ArrayList();
				if (!ontLngURI.contains(urlP)){
					if (sSepCnptE2Triple[i].containsKey(propName)){
						trLt=(ArrayList[])sSepCnptE2Triple[i].get(propName);
					}
					trLt[1].add(st);
					sSepCnptE2Triple[i].put(propName,trLt);
				}
				trLt=new ArrayList[3];
				trLt[0]=new ArrayList();
				trLt[1]=new ArrayList();
				trLt[2]=new ArrayList();
				if (!ontLngURI.contains(urlO)){
					if (sSepCnptE2Triple[i].containsKey(objName)){
						trLt=(ArrayList[])sSepCnptE2Triple[i].get(objName);
					}
					trLt[2].add(st);
					sSepCnptE2Triple[i].put(objName,trLt);
				}
			}
		}
		
		sSepPropE2Triple=new HashMap[s_propNum];
		for (int i=0;i<s_propNum;i++){
			sSepPropE2Triple[i]=new HashMap();
			for (Iterator it=s_propSubG[i].stmList.iterator();it.hasNext();){
				Statement st=(Statement)it.next();
				// 分离三元组
				Resource sub = st.getSubject();
				Property prop = st.getPredicate();
				RDFNode obj = st.getObject();
				String subName = sub.toString();
				String propName = prop.toString();
				String objName = obj.toString();
				String urlS = null, urlP = null, urlO = null;
				if (sub.isURIResource()) {
					urlS = sub.getNameSpace();
				}
				if (prop.isURIResource()) {
					urlP = prop.getNameSpace();
				}
				if (obj.isURIResource()) {
					urlO = obj.asNode().getNameSpace();
				}
				
				trLt=new ArrayList[3];
				trLt[0]=new ArrayList();
				trLt[1]=new ArrayList();
				trLt[2]=new ArrayList();
				if (!ontLngURI.contains(urlS)){
					if (sSepPropE2Triple[i].containsKey(subName)){
						trLt=(ArrayList[])sSepPropE2Triple[i].get(subName);
					}
					trLt[0].add(st);
					sSepPropE2Triple[i].put(subName,trLt);
				}
				trLt=new ArrayList[3];
				trLt[0]=new ArrayList();
				trLt[1]=new ArrayList();
				trLt[2]=new ArrayList();
				if (!ontLngURI.contains(urlP)){
					if (sSepPropE2Triple[i].containsKey(propName)){
						trLt=(ArrayList[])sSepPropE2Triple[i].get(propName);
					}
					trLt[1].add(st);
					sSepPropE2Triple[i].put(propName,trLt);
				}
				trLt=new ArrayList[3];
				trLt[0]=new ArrayList();
				trLt[1]=new ArrayList();
				trLt[2]=new ArrayList();
				if (!ontLngURI.contains(urlO)){
					if (sSepPropE2Triple[i].containsKey(objName)){
						trLt=(ArrayList[])sSepPropE2Triple[i].get(objName);
					}
					trLt[2].add(st);
					sSepPropE2Triple[i].put(objName,trLt);
				}
			}
		}
		
		tSepCnptE2Triple=new HashMap[t_cnptNum];
		for (int i=0;i<t_cnptNum;i++){
			tSepCnptE2Triple[i]=new HashMap();
			for (Iterator it=t_cnptSubG[i].stmList.iterator();it.hasNext();){
				Statement st=(Statement)it.next();
				// 分离三元组
				Resource sub = st.getSubject();
				Property prop = st.getPredicate();
				RDFNode obj = st.getObject();
				String subName = sub.toString();
				String propName = prop.toString();
				String objName = obj.toString();
				String urlS = null, urlP = null, urlO = null;
				if (sub.isURIResource()) {
					urlS = sub.getNameSpace();
				}
				if (prop.isURIResource()) {
					urlP = prop.getNameSpace();
				}
				if (obj.isURIResource()) {
					urlO = obj.asNode().getNameSpace();
				}
				
				trLt=new ArrayList[3];
				trLt[0]=new ArrayList();
				trLt[1]=new ArrayList();
				trLt[2]=new ArrayList();
				if (!ontLngURI.contains(urlS)){
					if (tSepCnptE2Triple[i].containsKey(subName)){
						trLt=(ArrayList[])tSepCnptE2Triple[i].get(subName);
					}
					trLt[0].add(st);
					tSepCnptE2Triple[i].put(subName,trLt);
				}
				trLt=new ArrayList[3];
				trLt[0]=new ArrayList();
				trLt[1]=new ArrayList();
				trLt[2]=new ArrayList();
				if (!ontLngURI.contains(urlP)){
					if (tSepCnptE2Triple[i].containsKey(propName)){
						trLt=(ArrayList[])tSepCnptE2Triple[i].get(propName);
					}
					trLt[1].add(st);
					tSepCnptE2Triple[i].put(propName,trLt);
				}
				trLt=new ArrayList[3];
				trLt[0]=new ArrayList();
				trLt[1]=new ArrayList();
				trLt[2]=new ArrayList();
				if (!ontLngURI.contains(urlO)){
					if (tSepCnptE2Triple[i].containsKey(objName)){
						trLt=(ArrayList[])tSepCnptE2Triple[i].get(objName);
					}
					trLt[2].add(st);
					tSepCnptE2Triple[i].put(objName,trLt);
				}
			}
		}
		
		tSepPropE2Triple=new HashMap[t_propNum];
		for (int i=0;i<t_propNum;i++){
			tSepPropE2Triple[i]=new HashMap();
			for (Iterator it=t_propSubG[i].stmList.iterator();it.hasNext();){
				Statement st=(Statement)it.next();
				// 分离三元组
				Resource sub = st.getSubject();
				Property prop = st.getPredicate();
				RDFNode obj = st.getObject();
				String subName = sub.toString();
				String propName = prop.toString();
				String objName = obj.toString();
				String urlS = null, urlP = null, urlO = null;
				if (sub.isURIResource()) {
					urlS = sub.getNameSpace();
				}
				if (prop.isURIResource()) {
					urlP = prop.getNameSpace();
				}
				if (obj.isURIResource()) {
					urlO = obj.asNode().getNameSpace();
				}
				
				trLt=new ArrayList[3];
				trLt[0]=new ArrayList();
				trLt[1]=new ArrayList();
				trLt[2]=new ArrayList();
				if (!ontLngURI.contains(urlS)){
					if (tSepPropE2Triple[i].containsKey(subName)){
						trLt=(ArrayList[])tSepPropE2Triple[i].get(subName);
					}
					trLt[0].add(st);
					tSepPropE2Triple[i].put(subName,trLt);
				}
				trLt=new ArrayList[3];
				trLt[0]=new ArrayList();
				trLt[1]=new ArrayList();
				trLt[2]=new ArrayList();
				if (!ontLngURI.contains(urlP)){
					if (tSepPropE2Triple[i].containsKey(propName)){
						trLt=(ArrayList[])tSepPropE2Triple[i].get(propName);
					}
					trLt[1].add(st);
					tSepPropE2Triple[i].put(propName,trLt);
				}
				trLt=new ArrayList[3];
				trLt[0]=new ArrayList();
				trLt[1]=new ArrayList();
				trLt[2]=new ArrayList();
				if (!ontLngURI.contains(urlO)){
					if (tSepPropE2Triple[i].containsKey(objName)){
						trLt=(ArrayList[])tSepPropE2Triple[i].get(objName);
					}
					trLt[2].add(st);
					tSepPropE2Triple[i].put(objName,trLt);
				}
			}
		}
		
		//System.out.println("数据结构建立完成");	
		
	}
	
	/*********
	 * 构造P=R=F=x%的种子 
	 ********/
	private void randomInitialSim(double rate)
	{
		Random random = new Random();
		/**读入标准的相似度**/
		ArrayList list = new ArrayList();
		MapRecord[] refMapResult = null;
		int refMapNum = 0;
		// 读出标准结果
		try {
			list = new MappingFile().read4xml("./dataset/OAEI2007/bench/benchmarks/248/refalign.rdf");
			refMapNum = ((Integer) list.get(0)).intValue();
			refMapResult = new MapRecord[refMapNum];
			refMapResult = (MapRecord[]) ((ArrayList) list.get(1)).toArray(new MapRecord[0]);
		} catch (MalformedURLException e) {
			System.out.println("Can't open refalign result file!"
					+ e.toString());
		} catch (DocumentException e) {
			System.out.println("Can't open refalign result file!"
					+ e.toString());
		}
		
		/**随机使某些位置相似度为0，直到达到符合比例要求为止**/
		int count=0;
		ArrayList used=new ArrayList();
		while (count<(int)(refMapNum*(1.0-rate))){
			int pos = Math.abs((random.nextInt())%(refMapNum));
			while (used.contains(pos)){
				pos=(pos+1)%(refMapNum);
			}
			used.add(pos);
			count++;
		}
		
//		/**抹去对应位置的相似度**/
//		for (Iterator it=used.iterator();it.hasNext();){
//			int pos = (Integer)it.next();
//			refMapResult[pos].similarity=0;
//		}
		/*使选出的位置变为错误的结果，达到P=R的目的*/
		int len=used.size();
		for (int i=0;i<=len/2;i++){
			int pos1=(Integer)used.get(i);
			int pos2=(Integer)used.get(len-i-1);
			String str=refMapResult[pos1].sourceLabel;
			refMapResult[pos1].sourceLabel=refMapResult[pos2].sourceLabel;
			refMapResult[pos2].sourceLabel=str;			
		}
		
		/**结果赋给当前的相似矩阵**/
		HashMap map=new HashMap();
		for (int i=0;i<refMapNum;i++){
			map.put(refMapResult[i].sourceLabel+refMapResult[i].targetLabel,refMapResult[i].similarity);
		}
		for (int i = 0; i < s_cnptNum; i++) {
			for (int j = 0; j < t_cnptNum; j++) {
				String key=s_cnptName[i]+t_cnptName[j];
				if (map.keySet().contains(key)){
					cnptSimRaw[i][j]=(Double)map.get(key);
				}
				else {
					cnptSimRaw[i][j]=0;
				}
			}
		}
		for (int i = 0; i < s_propNum; i++) {
			for (int j = 0; j < t_propNum; j++) {
				String key=s_propName[i]+t_propName[j];
				if (map.keySet().contains(key)){
					propSimRaw[i][j]=(Double)map.get(key);
				}
				else {
					propSimRaw[i][j]=0;
				}
			}
		}
	}
}
