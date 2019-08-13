/************************************************
 * Source code information
 * -----------------------
 * Original author	 Peng Wang, School of Computer Science & Eng., Southeast University
 * Author email      pwangseu@gmail.com
 * Web               http://ontomapping.googlepages.com
 * Created			 2007-7-18
 * Filename          SimPropagation.java
 * Version           2.0
 * 
 * Last modified on  2007-7-18
 *               by  Peng Wang
 * -----------------------
 * Functions describe:
 * 基本的相似度传播算法
 * 未在效率上进行优化
 * 该传播算法的特点是相似度在语义子图间进行传播，理论上传播后的结果之间不具备相比性，因此该算法不可实用
 ***********************************************/
package lily.onto.handle.propagation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import lily.onto.parse.OWLOntParse;
import lily.tool.datastructure.ConceptSubGraph;
import lily.tool.datastructure.GraphElmSim;
import lily.tool.datastructure.PairGraphRes;
import lily.tool.datastructure.PairSim;
import lily.tool.datastructure.PropertySubGraph;
import lily.tool.datastructure.TextDes;
import lily.tool.datastructure.TriplePair;
import lily.tool.filter.StableMarriageFilter;
import lily.tool.textsimilarity.TfIdfSim;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

/*******************************************************************************
 * Class information -------------------
 * 
 * @author Peng Wang
 * @date 2007-7-18
 * 
 * describe: 处理相似度的传播类
 ******************************************************************************/
public class SepSubSimPropagation {
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

	public ConceptSubGraph[] t_cnptSubG;// 概念子图

	public PropertySubGraph[] t_propSubG;// 属性子图

	public String t_baseURI;

	/** ***匿名资源**** */
	ArrayList s_AnonCnpt;

	ArrayList s_AnonProp;

	ArrayList s_AnonIns;

	ArrayList t_AnonCnpt;

	ArrayList t_AnonProp;

	ArrayList t_AnonIns;

	/** ****相似图信息***** */
	public Set[][] cnptTriplePair;// 子图相似三元组对集合

	public Set[][] propTriplePair;// 子图相似三元组对集合

	/** ****相似度矩阵***** */
	public double[][] cnptSimRaw;// 原始概念相似度

	public double[][] cnptSimK0;// k次迭代相似度

	public double[][] cnptSimK1;// k+1次迭代相似度

	public double[][] cnptSimKr;

	public double[][] propSimRaw;// 原始属性相似度

	public double[][] propSimK0;// k次迭代相似度

	public double[][] propSimK1;// k+1次迭代相似度

	public double[][] propSimKr;

	public double[][] insSimRaw;// 原始实例相似度

	/** ****其它相似度矩阵*** */
	// 概念子图其它元素间的相似度矩阵
	public ArrayList[][] cnptOtElmSim;

	public ArrayList[] cnptCombOESim;

	// 属性子图其它元素间的相似度矩阵
	public ArrayList[][] propOtElmSim;

	public ArrayList[] propCombOESim;

	/* 原始相似度的Hash */
	HashMap cnptSimMap;

	HashMap propSimMap;

	HashMap insSimMap;

	HashMap[] s_cnptCard;

	HashMap[] t_cnptCard;

	HashMap[] s_propCard;

	HashMap[] t_propCard;

	/** ***概念候选三元组集合**** */
	public HashMap[][] cnptCandiTPSet;

	/** ***属性候选三元组集合**** */
	public HashMap[][] propCandiTPSet;

	// 本体元信息
	public Set ontLngURI;

	public OWLOntParse ontParse;

	// 子图更新标志
	private boolean hasGUpdate;

	private boolean updateCnpt[][];

	private boolean updateProp[][];

	// 当前处理子图类型标志
	private boolean flagCnptSugG;

	private String curCnptA, curCnptB;

	private int curCnptIDA, curCnptIDB;

	private boolean flagPropSugG;

	private String curPropA, curPropB;

	private int curPropIDA, curPropIDB;
	
	/* 元素<-->元素相似hash表 */
	private HashMap EEMap;//合并的相似hash表
	/* 元素<-->相关三元组hash表 */
	private HashMap sE2Triple;
	private HashMap tE2Triple;
	

	// 传播最大迭代次数
	int maxProgTimes = 8;

	// 子图更新标志
	boolean sGUpdate;

	/***************************************************************************
	 * 类的主入口
	 **************************************************************************/
	public ArrayList ontSimPg(ArrayList paraList) {
		ArrayList result = new ArrayList();

		/* 解析参数 */
		unPackPara(paraList);
		/*数据结构处理*/
		dataStruProc();

		int times = 0;

		long start = System.currentTimeMillis();// 开始计时

		/* 首先准备计算子图的Card，用于确定传播系数 */
		getSubGraphCard();

		hasGUpdate = true;
		while (hasGUpdate && times < 1) {
			// 概念子图间相似度传播
			flagCnptSugG = true;
			cnptSimPropagation(times);
			flagCnptSugG = false;
			// 属性子图间相似度传播
			flagPropSugG = true;
			propSimPropagation(times);
			flagPropSugG = false;
			// 判断全局迭代是否结束
			hasGUpdate = isGlobalConvergence();
			times++;
		}

		long end = System.currentTimeMillis();// 结束计时
		long costtime = end - start;// 统计算法时间
		System.out.println("相似度传播算法时间：" + (double) costtime / 1000. + "秒");

		cnptSimRaw = cnptSimK0.clone();
		for (int i = 0; i < s_cnptNum; i++) {
			if (cnptSimRaw[i] != null) {
				cnptSimRaw[i] = cnptSimK0[i].clone();
			}
		}
		propSimRaw = propSimK0.clone();
		for (int i = 0; i < s_propNum; i++) {
			if (propSimRaw[i] != null) {
				propSimRaw[i] = propSimK0[i].clone();
			}
		}
		result.add(0, cnptSimRaw);
		result.add(1, propSimRaw);
		return result;
	}

	/***************************************************************************
	 * 图中的Card 为相似度传播系数准备
	 **************************************************************************/
	private void getSubGraphCard() {
		for (int i = 0; i < s_cnptNum; i++) {
			s_cnptCard[i] = computeGraphCard(s_cnptSubG[i].stmList);
		}
		for (int i = 0; i < t_cnptNum; i++) {
			t_cnptCard[i] = computeGraphCard(t_cnptSubG[i].stmList);
		}
		for (int i = 0; i < s_propNum; i++) {
			s_propCard[i] = computeGraphCard(s_propSubG[i].stmList);
		}
		for (int i = 0; i < t_propNum; i++) {
			t_propCard[i] = computeGraphCard(t_propSubG[i].stmList);
		}
	}

	private HashMap computeGraphCard(ArrayList graphStm) {
		HashMap cardMap = new HashMap();
		for (Iterator it = graphStm.iterator(); it.hasNext();) {
			Statement st = (Statement) it.next();
			String subName = st.getSubject().toString();
			String propName = st.getPredicate().toString();
			String objName = st.getObject().toString();

			String key;
			int value;
			// s--p的Card
			key = subName + propName;
			// o不是元语
			if (cardMap.containsKey(key)) {
				value = ((Integer) cardMap.get(key)).intValue();
			} else {
				value = 0;
			}
			cardMap.put(key, value + 1);

			// p--o的权重
			key = propName + objName;
			// s不是元语
			if (cardMap.containsKey(key)) {
				value = ((Integer) cardMap.get(key)).intValue();
			} else {
				value = 0;
			}
			cardMap.put(key, value + 1);
			// s--o的权重
			key = subName + objName;
			// p不是元语
			if (cardMap.containsKey(key)) {
				value = ((Integer) cardMap.get(key)).intValue();
			} else {
				value = 0;
			}
			cardMap.put(key, value + 1);
		}
		return cardMap;
	}

	/***************************************************************************
	 * 判断全局迭代是否结束 false:表示迭代结束 true:表示还需要继续迭代
	 **************************************************************************/
	@SuppressWarnings("unchecked")
	private boolean isGlobalConvergence() {
		boolean flag = false;
		double delta = 0;

		/** ***处理传播得到的相似度**** */
		// 1.概念相似度处理
		ArrayList[][] cnptPgSim = new ArrayList[s_cnptNum][t_cnptNum];
		ArrayList[][] cArray = new ArrayList[s_cnptNum][t_cnptNum];
		for (int i = 0; i < s_cnptNum; i++) {
			System.out.println(i);
			for (int j = 0; j < t_cnptNum; j++) {
				cnptPgSim[i][j] = new ArrayList();
				cArray[i][j] = new ArrayList();
				double sim = 0, sup = 0;
				// 1.1遍历概念子图的传播结果
				for (int x = 0; x < s_cnptNum; x++) {
					for (int y = 0; y < t_cnptNum; y++) {
						HashMap edgeMap = cnptCandiTPSet[x][y];
						for (Iterator it = edgeMap.entrySet().iterator(); it
								.hasNext();) {
							java.util.Map.Entry entry = (java.util.Map.Entry) it
									.next();
							ArrayList t2PairRes = (ArrayList) entry.getValue();
							PairGraphRes pairS = (PairGraphRes) t2PairRes
									.get(0);
							PairGraphRes pairO = (PairGraphRes) t2PairRes
									.get(1);
							PairGraphRes pairP = (PairGraphRes) t2PairRes
									.get(2);
							// 判断相似对
							if ((s_baseURI + s_cnptName[i]).equals(pairS.resA
									.toString())
									&& (t_baseURI + t_cnptName[j])
											.equals(pairS.resB.toString())) {
								PairSim ps = new PairSim();
								ps.sim = pairS.sim0;
								ps.support = pairS.simr;
								sim += ps.sim;
								sup += ps.support;
								cnptPgSim[i][j].add(ps);
								break;
							}
							if ((s_baseURI + s_cnptName[i]).equals(pairP.resA
									.toString())
									&& (t_baseURI + t_cnptName[j])
											.equals(pairP.resB.toString())) {
								PairSim ps = new PairSim();
								ps.sim = pairP.sim0;
								ps.support = pairP.simr;
								sim += ps.sim;
								sup += ps.support;
								cnptPgSim[i][j].add(ps);
								break;
							}
							if ((s_baseURI + s_cnptName[i]).equals(pairO.resA
									.toString())
									&& (t_baseURI + t_cnptName[j])
											.equals(pairO.resB.toString())) {
								PairSim ps = new PairSim();
								ps.sim = pairO.sim0;
								ps.support = pairO.simr;
								sim += ps.sim;
								sup += ps.support;
								cnptPgSim[i][j].add(ps);
								break;
							}
						}
					}
				}
				// 1.2遍历属性子图的传播结果
				for (int x = 0; x < s_propNum; x++) {
					for (int y = 0; y < t_propNum; y++) {
						HashMap edgeMap = propCandiTPSet[x][y];
						for (Iterator it = edgeMap.entrySet().iterator(); it
								.hasNext();) {
							java.util.Map.Entry entry = (java.util.Map.Entry) it
									.next();
							ArrayList t2PairRes = (ArrayList) entry.getValue();
							PairGraphRes pairS = (PairGraphRes) t2PairRes
									.get(0);
							PairGraphRes pairO = (PairGraphRes) t2PairRes
									.get(1);
							PairGraphRes pairP = (PairGraphRes) t2PairRes
									.get(2);
							// 判断相似对
							if ((s_baseURI + s_cnptName[i]).equals(pairS.resA
									.toString())
									&& (t_baseURI + t_cnptName[j])
											.equals(pairS.resB.toString())) {
								PairSim ps = new PairSim();
								ps.sim = pairS.sim0;
								ps.support = pairS.simr;
								sim += ps.sim;
								sup += ps.support;
								cnptPgSim[i][j].add(ps);
								break;
							}
							if ((s_baseURI + s_cnptName[i]).equals(pairP.resA
									.toString())
									&& (t_baseURI + t_cnptName[j])
											.equals(pairP.resB.toString())) {
								PairSim ps = new PairSim();
								ps.sim = pairP.sim0;
								ps.support = pairP.simr;
								sim += ps.sim;
								sup += ps.support;
								cnptPgSim[i][j].add(ps);
								break;
							}
							if ((s_baseURI + s_cnptName[i]).equals(pairO.resA
									.toString())
									&& (t_baseURI + t_cnptName[j])
											.equals(pairO.resB.toString())) {
								PairSim ps = new PairSim();
								ps.sim = pairO.sim0;
								ps.support = pairO.simr;
								sim += ps.sim;
								sup += ps.support;
								cnptPgSim[i][j].add(ps);
								break;
							}
						}
					}
				}
				int num = cnptPgSim[i][j].size();
				if (num > 0) {
					cArray[i][j].add(0, sim / (double) num);
					cArray[i][j].add(1, sup / (double) num);
					cArray[i][j].add(2, num);
				}
			}
		}

		// 2.属性相似度处理
		ArrayList[][] propPgSim = new ArrayList[s_propNum][t_propNum];
		ArrayList[][] pArray = new ArrayList[s_propNum][t_propNum];
		for (int i = 0; i < s_propNum; i++) {
			System.out.println(i);
			for (int j = 0; j < t_propNum; j++) {
				propPgSim[i][j] = new ArrayList();
				pArray[i][j] = new ArrayList();
				double sim = 0, sup = 0;
				// 2.1遍历概念子图的传播结果
				for (int x = 0; x < s_cnptNum; x++) {
					for (int y = 0; y < t_cnptNum; y++) {
						HashMap edgeMap = cnptCandiTPSet[x][y];
						for (Iterator it = edgeMap.entrySet().iterator(); it
								.hasNext();) {
							java.util.Map.Entry entry = (java.util.Map.Entry) it
									.next();
							ArrayList t2PairRes = (ArrayList) entry.getValue();
							PairGraphRes pairS = (PairGraphRes) t2PairRes
									.get(0);
							PairGraphRes pairO = (PairGraphRes) t2PairRes
									.get(1);
							PairGraphRes pairP = (PairGraphRes) t2PairRes
									.get(2);
							// 判断相似对
							if ((s_baseURI + s_propName[i]).equals(pairS.resA
									.toString())
									&& (t_baseURI + t_propName[j])
											.equals(pairS.resB.toString())) {
								PairSim ps = new PairSim();
								ps.sim = pairS.sim0;
								ps.support = pairS.simr;
								sim += ps.sim;
								sup += ps.support;
								propPgSim[i][j].add(ps);
								break;
							}
							if ((s_baseURI + s_propName[i]).equals(pairP.resA
									.toString())
									&& (t_baseURI + t_propName[j])
											.equals(pairP.resB.toString())) {
								PairSim ps = new PairSim();
								ps.sim = pairP.sim0;
								ps.support = pairP.simr;
								sim += ps.sim;
								sup += ps.support;
								propPgSim[i][j].add(ps);
								break;
							}
							if ((s_baseURI + s_propName[i]).equals(pairO.resA
									.toString())
									&& (t_baseURI + t_propName[j])
											.equals(pairO.resB.toString())) {
								PairSim ps = new PairSim();
								ps.sim = pairO.sim0;
								ps.support = pairO.simr;
								sim += ps.sim;
								sup += ps.support;
								propPgSim[i][j].add(ps);
								break;
							}
						}
					}
				}
				// 2.2遍历属性子图的传播结果
				for (int x = 0; x < s_propNum; x++) {
					for (int y = 0; y < t_propNum; y++) {
						HashMap edgeMap = propCandiTPSet[x][y];
						for (Iterator it = edgeMap.entrySet().iterator(); it
								.hasNext();) {
							java.util.Map.Entry entry = (java.util.Map.Entry) it
									.next();
							ArrayList t2PairRes = (ArrayList) entry.getValue();
							PairGraphRes pairS = (PairGraphRes) t2PairRes
									.get(0);
							PairGraphRes pairO = (PairGraphRes) t2PairRes
									.get(1);
							PairGraphRes pairP = (PairGraphRes) t2PairRes
									.get(2);
							// 判断相似对
							if ((s_baseURI + s_propName[i]).equals(pairS.resA
									.toString())
									&& (t_baseURI + t_propName[j])
											.equals(pairS.resB.toString())) {
								PairSim ps = new PairSim();
								ps.sim = pairS.sim0;
								ps.support = pairS.simr;
								sim += ps.sim;
								sup += ps.support;
								propPgSim[i][j].add(ps);
								break;
							}
							if ((s_baseURI + s_propName[i]).equals(pairP.resA
									.toString())
									&& (t_baseURI + t_propName[j])
											.equals(pairP.resB.toString())) {
								PairSim ps = new PairSim();
								ps.sim = pairP.sim0;
								ps.support = pairP.simr;
								sim += ps.sim;
								sup += ps.support;
								propPgSim[i][j].add(ps);
								break;
							}
							if ((s_baseURI + s_propName[i]).equals(pairO.resA
									.toString())
									&& (t_baseURI + t_propName[j])
											.equals(pairO.resB.toString())) {
								PairSim ps = new PairSim();
								ps.sim = pairO.sim0;
								ps.support = pairO.simr;
								sim += ps.sim;
								sup += ps.support;
								propPgSim[i][j].add(ps);
								break;
							}
						}
					}
				}

				int num = propPgSim[i][j].size();
				if (num > 0) {
					pArray[i][j].add(0, sim / (double) num);
					pArray[i][j].add(1, sup / (double) num);
					pArray[i][j].add(2, num);
				}
			}
		}

		// //3.预处理概念对之间的相似度
		// ArrayList[][] cArray=new ArrayList[s_cnptNum][t_cnptNum];
		// for (int i = 0; i < s_cnptNum; i++) {
		// for (int j = 0; j < t_cnptNum; j++) {
		// cArray[i][j] = new ArrayList();
		// double sim=0,sup=0;
		// int num=cnptPgSim[i][j].size();
		// for (Iterator it = cnptPgSim[i][j].iterator(); it.hasNext();) {
		// PairSim ps = (PairSim) it.next();
		// sim+=ps.sim;
		// sup+=ps.support;
		// }
		// if (num>0){
		// cArray[i][j].add(0,sim/(double)num);
		// cArray[i][j].add(1,sup/(double)num);
		// cArray[i][j].add(2,num);
		// }
		// }
		// }

		// //4.预处理属性对之间的相似度
		// ArrayList[][] pArray=new ArrayList[s_propNum][t_propNum];
		// for (int i = 0; i < s_propNum; i++) {
		// for (int j = 0; j < t_propNum; j++) {
		// pArray[i][j] = new ArrayList();
		// double sim=0,sup=0;
		// int num=propPgSim[i][j].size();
		// for (Iterator it = propPgSim[i][j].iterator(); it.hasNext();) {
		// PairSim ps = (PairSim) it.next();
		// sim+=ps.sim;
		// sup+=ps.support;
		// }
		// if (num>0){
		// pArray[i][j].add(0,sim/(double)num);
		// pArray[i][j].add(1,sup/(double)num);
		// pArray[i][j].add(2,num);
		// }
		// }
		// }

		// 5.估计概念对之间的相似度
		for (int i = 0; i < s_cnptNum; i++) {
			for (int j = 0; j < t_cnptNum; j++) {
				if (cArray[i][j].isEmpty()) {
					continue;
				}
				// 估计当前值的可信度
				double csim = ((Double) cArray[i][j].get(0)).doubleValue();
				double csup = ((Double) cArray[i][j].get(1)).doubleValue();
				double cnum = ((Integer) cArray[i][j].get(2)).doubleValue();
				double maxsup = 0;
				double maxnum = 0;
				// i行
				for (int k = 0; k < t_cnptNum; k++) {
					if (cArray[i][k].isEmpty()) {
						continue;
					}
					double sup = ((Double) cArray[i][k].get(1)).doubleValue();
					if (sup > maxsup) {
						maxsup = sup;
					}
					double num = ((Integer) cArray[i][k].get(2)).doubleValue();
					if (num > maxnum) {
						maxnum = num;
					}
				}
				// j列
				for (int k = 0; k < s_cnptNum; k++) {
					if (cArray[k][j].isEmpty()) {
						continue;
					}
					double sup = ((Double) cArray[k][j].get(1)).doubleValue();
					if (sup > maxsup) {
						maxsup = sup;
					}
					double num = ((Integer) cArray[k][j].get(2)).doubleValue();
					if (num > maxnum) {
						maxnum = num;
					}
				}
				// 估计
				cnptSimK1[i][j] = csim * Math.pow((csup / maxsup), 0.5)
						* Math.pow((cnum / maxnum), 0.5);
				// 去除明显的错误
				if (cnptSimK1[i][j] < 0.005) {
					cnptSimK1[i][j] = 0;
				}
			}
		}

		// 6.估计属性对之间的相似度
		for (int i = 0; i < s_propNum; i++) {
			for (int j = 0; j < t_propNum; j++) {
				if (pArray[i][j].isEmpty()) {
					continue;
				}
				// 估计当前值的可信度
				double csim = ((Double) pArray[i][j].get(0)).doubleValue();
				double csup = ((Double) pArray[i][j].get(1)).doubleValue();
				double cnum = ((Integer) pArray[i][j].get(2)).doubleValue();
				double maxsup = 0;
				double maxnum = 0;
				// i行
				for (int k = 0; k < t_propNum; k++) {
					if (pArray[i][k].isEmpty()) {
						continue;
					}
					double sup = ((Double) pArray[i][k].get(1)).doubleValue();
					if (sup > maxsup) {
						maxsup = sup;
					}
					double num = ((Integer) pArray[i][k].get(2)).doubleValue();
					if (num > maxnum) {
						maxnum = num;
					}
				}
				// j列
				for (int k = 0; k < s_propNum; k++) {
					if (pArray[k][j].isEmpty()) {
						continue;
					}
					double sup = ((Double) pArray[k][j].get(1)).doubleValue();
					if (sup > maxsup) {
						maxsup = sup;
					}
					double num = ((Integer) pArray[k][j].get(2)).doubleValue();
					if (num > maxnum) {
						maxnum = num;
					}
				}
				// 估计
				propSimK1[i][j] = csim * Math.pow((csup / maxsup), 0.5)
						* Math.pow((cnum / maxnum), 0.5);
				if (propSimK1[i][j] < 0.005) {
					propSimK1[i][j] = 0;
				}
			}
		}

		// double[][] tArray=new double[s_cnptNum][t_cnptNum];
		// tArray=cnptSimKr.clone();
		// for (int i=0;i<s_cnptNum;i++){
		// tArray[i]=(double[])cnptSimKr[i].clone();
		// }
		//		
		// for (int i = 0; i < s_cnptNum; i++) {
		// for (int j = 0; j < t_cnptNum; j++) {
		// // 估计当前值的可信度
		// double max = 0;
		// // i行
		// for (int k = 0; k < t_cnptNum; k++) {
		// if (tArray[i][k] > max) {max=tArray[i][k];}
		// }
		// // j列
		// for (int k = 0; k < s_cnptNum; k++) {
		// if (tArray[k][j] > max) {max=tArray[k][j];}
		// }
		// // 估计
		// if (max>0){
		// cnptSimK1[i][j] = cnptSimK1[i][j]* (tArray[i][j] / max)*(tArray[i][j]
		// / max);
		// }
		// }
		// }
		//		
		// double[][] pArray=new double[s_propNum][t_propNum];
		// pArray=propSimKr.clone();
		// for (int i=0;i<s_propNum;i++){
		// pArray[i]=(double[])propSimKr[i].clone();
		// }
		//		
		// for (int i = 0; i < s_propNum; i++) {
		// for (int j = 0; j < t_propNum; j++) {
		// // 估计当前值的可信度
		// double max = 0;
		// // i行
		// for (int k = 0; k < t_propNum; k++) {
		// if (pArray[i][k] > max) {max=pArray[i][k];}
		// }
		// // j列
		// for (int k = 0; k < s_propNum; k++) {
		// if (pArray[k][j] > max) {max=pArray[k][j];}
		// }
		// // 估计
		// if (max>0){
		// propSimK1[i][j] = propSimK1[i][j]*(pArray[i][j] / max) *(pArray[i][j]
		// / max);
		// }
		// }
		// }

		// 做一个事先的过滤
		// cnptSimK1 = new
		// StableMarriageFilter().run(cnptSimK1,s_cnptNum,t_cnptNum);
		// propSimK1 = new
		// StableMarriageFilter().run(propSimK1,s_propNum,t_propNum);

		ArrayList vA = new ArrayList();
		ArrayList vB = new ArrayList();

		for (int i = 0; i < s_cnptNum; i++) {
			for (int j = 0; j < t_cnptNum; j++) {
				// delta+=Math.abs(cnptSimK0[i][j]-cnptSimK1[i][j]);
				vA.add(cnptSimK0[i][j]);
				vB.add(cnptSimK1[i][j]);
				cnptSimK0[i][j] = cnptSimK1[i][j];
			}
		}

		for (int i = 0; i < s_propNum; i++) {
			for (int j = 0; j < t_propNum; j++) {
				// delta+=Math.abs(propSimK0[i][j]-propSimK1[i][j]);
				vA.add(propSimK0[i][j]);
				vB.add(propSimK1[i][j]);
				propSimK0[i][j] = propSimK1[i][j];
			}
		}

		/* 用向量方法判断迭代是否结束 */
		delta = new TfIdfSim().getTextVectorSim(vA, vB);

		System.out.println("全局相似矩阵的迭代收敛相似度:" + delta);
		if (delta < 0.999) {
			flag = true;
		}

		if (flag) {
			/* 如果还要继续全局计算，重新布置基本相似度HashMap */
			for (int i = 0; i < s_cnptNum; i++) {
				for (int j = 0; j < t_cnptNum; j++) {
					cnptSimMap.put(s_cnptName[i] + t_cnptName[j],
							cnptSimK0[i][j]);
				}
			}
			propSimMap = new HashMap();
			for (int i = 0; i < s_propNum; i++) {
				for (int j = 0; j < t_propNum; j++) {
					propSimMap.put(s_propName[i] + t_propName[j],
							propSimK0[i][j]);
				}
			}
		}

		return flag;
	}

	/***************************************************************************
	 * 选择候选相似三元组
	 **************************************************************************/
	private Set getSimTripleCandidate(ArrayList s_Stm, ArrayList t_Stm) {
		Set tPairSet = new HashSet();

		/* 遍历两个三元组集合 */
		double weight = 0;// 区别边的weight
		for (Iterator itx = s_Stm.iterator(); itx.hasNext();) {
			Statement stA = (Statement) itx.next();
			if (metaElmInTriple(stA) >= 2) {
				continue;
			}
			// 分离三元组
			Resource subA = stA.getSubject();
			Property propA = stA.getPredicate();
			RDFNode objA = stA.getObject();
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

			for (Iterator ity = t_Stm.iterator(); ity.hasNext();) {
				Statement stB = (Statement) ity.next();
				if (metaElmInTriple(stB) >= 2) {
					continue;
				}
				Resource subB = stB.getSubject();
				Property propB = stB.getPredicate();
				RDFNode objB = stB.getObject();
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
				typeA = getResourceType(subA.toString(), m_source);
				typeB = getResourceType(subB.toString(), m_target);
				simS = -1.0;
				metaS = false;
				// 判断是不是元语
				if (ontLngURI.contains(urlSA) && ontLngURI.contains(urlSB)) {
					// 是否是相同的元语
					if (subA.toString().equals(subB.toString())) {
						simS = 1.0;
						metaS = true;
					} else {
						// 元语如果不相同，直接跳过当前triple-pair
						continue;
					}
				}
				// 都不是元语的情况
				else if (!ontLngURI.contains(urlSA)
						&& !ontLngURI.contains(urlSB)) {
					if (typeA == typeB) {
						// 相同的类型，需要确定相似度
						simS = getElmSim(subA, subB, typeA);
					}
				}
				if (simS < 0.001) {
					simS = 0;
				}
				if (simS > 0) {
					simElmNum++;
				}

				// 判断p-p
				typeA = getResourceType(propA.toString(), m_source);
				typeB = getResourceType(propB.toString(), m_target);
				simP = -1.0;
				metaP = false;
				// 判断是不是元语
				if (ontLngURI.contains(urlPA) && ontLngURI.contains(urlPB)) {
					// 是否是相同的元语
					if (propA.toString().equals(propB.toString())) {
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
					}
				}
				if (simP < 0.001) {
					simP = 0;
				}
				if (simP > 0) {
					simElmNum++;
				}

				// 判断o-o
				typeA = getResourceType(objA.toString(), m_source);
				typeB = getResourceType(objB.toString(), m_target);
				simO = -1.0;
				metaO = false;
				// 判断是不是元语
				if (ontLngURI.contains(urlOA) && ontLngURI.contains(urlOB)) {
					// 是否是相同的元语
					if (objA.toString().equals(objB.toString())) {
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
					}
				}
				if (simO < 0.001) {
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
		return tPairSet;
	}

	/***************************************************************************
	 * 更新候选相似三元组
	 **************************************************************************/
	@SuppressWarnings("unused")
	private Set updateTripleCandidate(ArrayList s_Stm, ArrayList t_Stm,
			Set tPairSet, HashMap edgeMap) {
		Set upSet = new HashSet();
		double weight = 0;

		// 假设子图不更新
		sGUpdate = false;

		/* 原来的三元组放在tPairSet */
		/* 提取相似对,为判断新的相似度做准备 */
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

		/* 遍历图的边 */
		for (Iterator it = edgeMap.entrySet().iterator(); it.hasNext();) {
			java.util.Map.Entry entry = (java.util.Map.Entry) it.next();
			ArrayList t2PairRes = (ArrayList) entry.getValue();
			PairGraphRes pairS = (PairGraphRes) t2PairRes.get(0);
			PairGraphRes pairO = (PairGraphRes) t2PairRes.get(1);
			PairGraphRes pairP = (PairGraphRes) t2PairRes.get(2);

			/* 判断有没有新相似度对,并记录 */
			if (pairS.cFlag) {
				upSet.add(pairS);
				pairS.cFlag = false;
			}
			if (pairO.cFlag) {
				upSet.add(pairO);
				pairO.cFlag = false;
			}
			if (pairP.cFlag) {
				upSet.add(pairP);
				pairP.cFlag = false;
			}
		}

		/* 更新候选三元组 */
		for (Iterator it = upSet.iterator(); it.hasNext();) {
			PairGraphRes upair = (PairGraphRes) it.next();
			String upA = upair.resA.toString();
			String upB = upair.resB.toString();

			/* 遍历原始图,找到对应的新三元组 */
			for (Iterator itx = s_Stm.iterator(); itx.hasNext();) {
				Statement stA = (Statement) itx.next();
				if (metaElmInTriple(stA) >= 2) {// 跳过2个元语的三元组
					continue;
				}
				// 分离三元组
				Resource subA = stA.getSubject();
				Property propA = stA.getPredicate();
				RDFNode objA = stA.getObject();

				/* 确保包含待更新pair */
				if (!upA.equals(subA.toString())
						&& !upA.equals(propA.toString())
						&& !upA.equals(objA.toString())) {
					continue;
				}

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

				for (Iterator ity = t_Stm.iterator(); ity.hasNext();) {
					Statement stB = (Statement) ity.next();
					if (metaElmInTriple(stB) >= 2) {// 跳过2个元语的三元组
						continue;
					}
					Resource subB = stB.getSubject();
					Property propB = stB.getPredicate();
					RDFNode objB = stB.getObject();

					/* 确保包含待更新pair */
					if (!upB.equals(subB.toString())
							&& !upB.equals(propB.toString())
							&& !upB.equals(objB.toString())) {
						continue;
					}

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

					// 先找到合法的三元组对
					int typeA, typeB;
					double simS, simP, simO;
					boolean metaS, metaP, metaO;
					int simElmNum = 0;
					// 判断s-s
					typeA = getResourceType(subA.toString(), m_source);
					typeB = getResourceType(subB.toString(), m_target);
					simS = -1.0;
					metaS = false;
					// 判断是不是元语
					if (ontLngURI.contains(urlSA) && ontLngURI.contains(urlSB)) {
						// 是否是相同的元语
						if (subA.toString().equals(subB.toString())) {
							simS = 1.0;
							metaS = true;
						} else {
							// 元语如果不相同，直接跳过当前triple-pair
							continue;
						}
					}
					// 都不是元语的情况
					else if (!ontLngURI.contains(urlSA)
							&& !ontLngURI.contains(urlSB)) {
						if (typeA == typeB) {
							// 相同的类型，需要确定相似度
							/* 判断当前tpair是否包含该对 */
							if (rawPairs.contains(subA.toString()
									+ subB.toString())) {
								/* 已经包含,采用计算的相似度 */
								simS = ((Double) rawPairSim.get(subA.toString()
										+ subB.toString())).doubleValue();
							} else {
								/* 没有包含,采用 */
								simS = getElmSim(subA, subB, typeA);
							}
						}

					}
					if (simS < 0.001) {
						simS = 0;
					}
					if (simS > 0) {
						simElmNum++;
					}

					// 判断p-p
					typeA = getResourceType(propA.toString(), m_source);
					typeB = getResourceType(propB.toString(), m_target);
					simP = -1.0;
					metaP = false;
					// 判断是不是元语
					if (ontLngURI.contains(urlPA) && ontLngURI.contains(urlPB)) {
						// 是否是相同的元语
						if (propA.toString().equals(propB.toString())) {
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
							/* 判断当前tpair是否包含该对 */
							if (rawPairs.contains(propA.toString()
									+ propB.toString())) {
								/* 已经包含,采用计算的相似度 */
								simP = ((Double) rawPairSim.get(propA
										.toString()
										+ propB.toString())).doubleValue();
							} else {
								/* 没有包含,采用 */
								simP = getElmSim(propA, propB, typeA);
							}
						}
					}
					if (simP < 0.001) {
						simP = 0;
					}
					if (simP > 0) {
						simElmNum++;
					}

					// 判断o-o
					typeA = getResourceType(objA.toString(), m_source);
					typeB = getResourceType(objB.toString(), m_target);
					simO = -1.0;
					metaO = false;
					// 判断是不是元语
					if (ontLngURI.contains(urlOA) && ontLngURI.contains(urlOB)) {
						// 是否是相同的元语
						if (objA.toString().equals(objB.toString())) {
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
							/* 判断当前tpair是否包含该对 */
							if (rawPairs.contains(objA.toString()
									+ objB.toString())) {
								/* 已经包含,采用计算的相似度 */
								simO = ((Double) rawPairSim.get(objA.toString()
										+ objB.toString())).doubleValue();
							} else {
								/* 没有包含,采用 */
								simO = getElmSim(objA, objB, typeA);
							}
						}
					}
					if (simO < 0.001) {
						simO = 0;
					}
					if (simO > 0) {
						simElmNum++;
					}

					/* 如果是新的相似triple，加入triple-pair集合 */
					if (simElmNum >= 2
							&& !rawTriples.contains(stA.toString()
									+ stB.toString())) {
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

						rawTriples.add(stA.toString() + stB.toString());

						// 子图发生更新
						sGUpdate = true;
					}
				}
			}
		}
		return tPairSet;
	}

	/***************************************************************************
	 * 构造相似三元组对的合并图
	 **************************************************************************/
	@SuppressWarnings("unchecked")
	private ArrayList consTriplePairGraph(Set pairSet) {
		ArrayList result = new ArrayList();

		/* triple pair graph的点集合和边集合 */
		Set tgNodes = new HashSet();
		Set tgEdges = new HashSet();
		HashMap edgeMap = new HashMap();// 三元组到边的Hash
		HashMap progWeightMap = new HashMap();// 图中元素传播系数

		/* 直接遍历三元组对，构造图 */
		for (Iterator it = pairSet.iterator(); it.hasNext();) {
			TriplePair pair = (TriplePair) it.next();
			PairGraphRes nodeStar = new PairGraphRes();
			PairGraphRes nodeEnd = new PairGraphRes();
			PairGraphRes gEdge = new PairGraphRes();
			nodeStar.resA = pair.tripleA.getSubject();
			nodeStar.resB = pair.tripleB.getSubject();
			nodeStar.sim0 = pair.simS;
			nodeStar.simr = pair.simSr;
			nodeStar.isMeta = pair.sIsMeta;
			nodeEnd.resA = pair.tripleA.getObject();
			nodeEnd.resB = pair.tripleB.getObject();
			nodeEnd.sim0 = pair.simO;
			nodeEnd.simr = pair.simOr;
			nodeEnd.isMeta = pair.oIsMeta;
			gEdge.resA = pair.tripleA.getPredicate();
			gEdge.resB = pair.tripleB.getPredicate();
			gEdge.sim0 = pair.simP;
			gEdge.simr = pair.simPr;
			gEdge.isMeta = pair.pIsMeta;

			/* 如果包含这个点，直接用原来的点来替代 */
			// 遍历已有点集
			for (Iterator itx = tgNodes.iterator(); itx.hasNext();) {
				PairGraphRes tp = (PairGraphRes) itx.next();
				if (nodeStar.getString().equals(tp.getString())) {
					nodeStar = tp;
				}
				if (nodeEnd.getString().equals(tp.getString())) {
					nodeEnd = tp;
				}

				if (gEdge.getString().equals(tp.getString())) {
					gEdge = tp;
				}
			}
			// 遍历已有边集
			for (Iterator itx = tgEdges.iterator(); itx.hasNext();) {
				PairGraphRes tp = (PairGraphRes) itx.next();
				if (nodeStar.getString().equals(tp.getString())) {
					nodeStar = tp;
				}

				if (nodeEnd.getString().equals(tp.getString())) {
					nodeEnd = tp;
				}
				if (gEdge.getString().equals(tp.getString())) {
					gEdge = tp;
				}
			}

			// 加入Star Node
			if (!tgNodes.contains(nodeStar)) {
				/* 如果图中不包含这个点 */
				tgNodes.add(nodeStar);
			}

			// 加入End Node
			if (!tgNodes.contains(nodeEnd)) {
				/* 如果图中不包含这个点 */
				tgNodes.add(nodeEnd);
			}

			// 加入Edge Node
			if (!tgEdges.contains(gEdge)) {
				/* 如果图中不包含这个点 */
				tgEdges.add(gEdge);
			}

			/* 将边和三元组的对应保存在一个表中，方便查询 */
			ArrayList t2PairRes = new ArrayList();
			t2PairRes.add(0, nodeStar);
			t2PairRes.add(1, nodeEnd);
			t2PairRes.add(2, gEdge);
			edgeMap.put(pair, t2PairRes);

			/* 计算传播系数 */
			/** ***inverse average传播系数***** */
			// String key,keyA,keyB;
			// int valueA,valueB;
			// //s--p的权重
			// key=nodeStar.getString()+gEdge.getString();
			// keyA=nodeStar.resA.toString()+gEdge.resA.toString();
			// keyB=nodeStar.resB.toString()+gEdge.resB.toString();
			// //o不是元语
			// if (!nodeEnd.isMeta){
			// if (!progWeightMap.containsKey(key)){
			// if (flagCnptSugG){
			// valueA=((Integer)s_cnptCard[curCnptIDA].get(keyA)).intValue();
			// valueB=((Integer)t_cnptCard[curCnptIDB].get(keyB)).intValue();
			// }
			// else{
			// valueA=((Integer)s_propCard[curPropIDA].get(keyA)).intValue();
			// valueB=((Integer)t_propCard[curPropIDB].get(keyB)).intValue();
			// }
			// progWeightMap.put(key,((double)(valueA+valueB))/2.0);
			// }
			// }
			// //p--o的权重
			// key=gEdge.getString()+nodeEnd.getString();
			// keyA=gEdge.resA.toString()+nodeEnd.resA.toString();
			// keyB=gEdge.resB.toString()+nodeEnd.resB.toString();
			// //s不是元语
			// if (!nodeStar.isMeta){
			// if (!progWeightMap.containsKey(key)){
			// if (flagCnptSugG){
			// valueA=((Integer)s_cnptCard[curCnptIDA].get(keyA)).intValue();
			// valueB=((Integer)t_cnptCard[curCnptIDB].get(keyB)).intValue();
			// }
			// else{
			// valueA=((Integer)s_propCard[curPropIDA].get(keyA)).intValue();
			// valueB=((Integer)t_propCard[curPropIDB].get(keyB)).intValue();
			// }
			// progWeightMap.put(key,((double)(valueA+valueB))/2.0);
			// }
			// }
			// //s--o的权重
			// key=nodeStar.getString()+nodeEnd.getString();
			// keyA=nodeStar.resA.toString()+nodeEnd.resA.toString();
			// keyB=nodeStar.resB.toString()+nodeEnd.resB.toString();
			// //p不是元语
			// if (!gEdge.isMeta){
			// if (!progWeightMap.containsKey(key)){
			// if (flagCnptSugG){
			// valueA=((Integer)s_cnptCard[curCnptIDA].get(keyA)).intValue();
			// valueB=((Integer)t_cnptCard[curCnptIDB].get(keyB)).intValue();
			// }
			// else{
			// valueA=((Integer)s_propCard[curPropIDA].get(keyA)).intValue();
			// valueB=((Integer)t_propCard[curPropIDB].get(keyB)).intValue();
			// }
			// progWeightMap.put(key,((double)(valueA+valueB))/2.0);
			// // progWeightMap.put(key,((double)(valueA*valueB)));
			// }
			// }
			/** ***triple-graph传播系数***** */
			String key;
			double value;
			// s--p的权重
			key = nodeStar.getString() + gEdge.getString();
			// o不是元语
			if (!nodeEnd.isMeta) {
				if (progWeightMap.containsKey(key)) {
					value = ((Double) progWeightMap.get(key)).doubleValue();
				} else {
					value = 0;
				}
				progWeightMap.put(key, value + 1);
			}
			// p--o的权重
			key = gEdge.getString() + nodeEnd.getString();
			// s不是元语
			if (!nodeStar.isMeta) {
				if (progWeightMap.containsKey(key)) {
					value = ((Double) progWeightMap.get(key)).doubleValue();
				} else {
					value = 0;
				}
				progWeightMap.put(key, value + 1);
			}
			// s--o的权重
			key = nodeStar.getString() + nodeEnd.getString();
			// p不是元语
			if (!gEdge.isMeta) {
				if (progWeightMap.containsKey(key)) {
					value = ((Double) progWeightMap.get(key)).doubleValue();
				} else {
					value = 0;
				}
				progWeightMap.put(key, value + 1);
			}
			/** *******传播系数结束*********** */

		}

		result.add(0, tgNodes);
		result.add(1, tgEdges);
		result.add(2, edgeMap);
		result.add(3, progWeightMap);
		return result;
	}

	/***************************************************************************
	 * 相似度的传播 输入：triple-pair图 输出：相似度传播图
	 **************************************************************************/
	private void propagation(HashMap edgeMap, HashMap progWeightMap) {
		double delta = 0.5;
		int k = 0;

		/*
		 * 子图元素当前的相似度 当前相似度用sim0表示 传播后的相似度用simk表示
		 */
		while (k < maxProgTimes && (delta > 0.001 && delta < 0.995)) {

			double max = 0;// 最大相似度
			Set flagSet = new HashSet();// 避免多次归一的标记集合

			/* 1.遍历图的边，计算传播的相似度 */
			flagSet.clear();
			edgeMap.keySet();

			for (Iterator itx = edgeMap.entrySet().iterator(); itx.hasNext();) {
				java.util.Map.Entry entry = (java.util.Map.Entry) itx.next();
				ArrayList t2PairRes = (ArrayList) entry.getValue();
				PairGraphRes pairS = (PairGraphRes) t2PairRes.get(0);
				PairGraphRes pairO = (PairGraphRes) t2PairRes.get(1);
				PairGraphRes pairP = (PairGraphRes) t2PairRes.get(2);

				String key;
				double weight;

				// 计算s上的相似度传播,相似度从p-o传入
				if (!pairS.isMeta) {
					if (!flagSet.contains(pairS)) {
						pairS.simk = pairS.sim0;
						flagSet.add(pairS);
					}
					key = pairP.getString() + pairO.getString();
					weight = ((Double) progWeightMap.get(key)).doubleValue();
					pairS.simk += pairP.sim0 * pairO.sim0 / weight;
					max = Math.max(max, pairS.simk);
					if (pairS.simk > 0 && pairS.sim0 == 0) {
						pairS.cFlag = true;
					}
				}

				// 计算p上的相似度传播,相似度从s-o传入
				if (!pairP.isMeta) {
					if (!flagSet.contains(pairP)) {
						pairP.simk = pairP.sim0;
						flagSet.add(pairP);
					}
					key = pairS.getString() + pairO.getString();
					weight = ((Double) progWeightMap.get(key)).doubleValue();
					pairP.simk += pairS.sim0 * pairO.sim0 / weight;
					max = Math.max(max, pairP.simk);
					if (pairP.simk > 0 && pairP.sim0 == 0) {
						pairP.cFlag = true;
					}
				}

				// 计算o上的相似度传播,相似度从s-p传入
				if (!pairO.isMeta) {
					if (!flagSet.contains(pairO)) {
						pairO.simk = pairO.sim0;
						flagSet.add(pairO);
					}
					key = pairS.getString() + pairP.getString();
					weight = ((Double) progWeightMap.get(key)).doubleValue();
					pairO.simk += pairS.sim0 * pairP.sim0 / weight;
					max = Math.max(max, pairO.simk);
					if (pairO.simk > 0 && pairO.sim0 == 0) {
						pairO.cFlag = true;
					}
				}
			}

			/* 相似度归一 */
			flagSet.clear();
			for (Iterator itx = edgeMap.entrySet().iterator(); itx.hasNext();) {
				java.util.Map.Entry entry = (java.util.Map.Entry) itx.next();
				ArrayList t2PairRes = (ArrayList) entry.getValue();
				PairGraphRes pairS = (PairGraphRes) t2PairRes.get(0);
				PairGraphRes pairO = (PairGraphRes) t2PairRes.get(1);
				PairGraphRes pairP = (PairGraphRes) t2PairRes.get(2);

				if (!pairS.isMeta) {
					if (!flagSet.contains(pairS)) {
						pairS.simr = pairS.simk;
						pairS.simk = pairS.simk / max;
						flagSet.add(pairS);
					}
				}
				if (!pairP.isMeta) {
					if (!flagSet.contains(pairP)) {
						pairP.simr = pairP.simk;
						pairP.simk = pairP.simk / max;
						flagSet.add(pairP);
					}
				}
				if (!pairO.isMeta) {
					if (!flagSet.contains(pairO)) {
						pairO.simr = pairO.simk;
						pairO.simk = pairO.simk / max;
						flagSet.add(pairO);
					}
				}
			}

			/* 判断相似度收敛 */
			delta = 0;
			ArrayList vA = new ArrayList();
			ArrayList vB = new ArrayList();
			flagSet.clear();
			for (Iterator itx = edgeMap.entrySet().iterator(); itx.hasNext();) {
				java.util.Map.Entry entry = (java.util.Map.Entry) itx.next();
				ArrayList t2PairRes = (ArrayList) entry.getValue();
				PairGraphRes pairS = (PairGraphRes) t2PairRes.get(0);
				PairGraphRes pairO = (PairGraphRes) t2PairRes.get(1);
				PairGraphRes pairP = (PairGraphRes) t2PairRes.get(2);

				if (!pairS.isMeta) {
					if (!flagSet.contains(pairS)) {
						// delta+=Math.abs(pairS.simk-pairS.sim0);
						vA.add(pairS.sim0);
						vB.add(pairS.simk);
						flagSet.add(pairS);
					}
				}
				if (!pairP.isMeta) {
					if (!flagSet.contains(pairP)) {
						// delta+=Math.abs(pairP.simk-pairP.sim0);
						vA.add(pairP.sim0);
						vB.add(pairP.simk);
						flagSet.add(pairP);
					}
				}
				if (!pairO.isMeta) {
					if (!flagSet.contains(pairO)) {
						// delta+=Math.abs(pairO.simk-pairO.sim0);
						vA.add(pairO.sim0);
						vB.add(pairO.simk);
						flagSet.add(pairO);
					}
				}
			}

			/* 用向量方法判断迭代是否结束 */
			delta = new TfIdfSim().getTextVectorSim(vA, vB);

			/* 更新k次相似度 */
			flagSet.clear();
//			System.out.println("------" + k + "--------");
			for (Iterator itx = edgeMap.entrySet().iterator(); itx.hasNext();) {
				java.util.Map.Entry entry = (java.util.Map.Entry) itx.next();
				ArrayList t2PairRes = (ArrayList) entry.getValue();
				PairGraphRes pairS = (PairGraphRes) t2PairRes.get(0);
				PairGraphRes pairO = (PairGraphRes) t2PairRes.get(1);
				PairGraphRes pairP = (PairGraphRes) t2PairRes.get(2);

				if (!pairS.isMeta) {
					if (!flagSet.contains(pairS)) {
						pairS.sim0 = pairS.simk;
//						System.out.println("<" + pairS.resA.toString() + "--"
//								+ pairS.resB.toString() + ">---" + pairS.sim0);
						flagSet.add(pairS);
					}
				}
				if (!pairP.isMeta) {
					if (!flagSet.contains(pairP)) {
						pairP.sim0 = pairP.simk;
//						System.out.println("<" + pairP.resA.toString() + "--"
//								+ pairP.resB.toString() + ">---" + pairP.sim0);
						flagSet.add(pairP);
					}
				}
				if (!pairO.isMeta) {
					if (!flagSet.contains(pairO)) {
						pairO.sim0 = pairO.simk;
//						System.out.println("<" + pairO.resA.toString() + "--"
//								+ pairO.resB.toString() + ">---" + pairO.sim0);
						flagSet.add(pairO);
					}
				}
			}
			k++;
//			System.out.println("当前迭代结果的向量相似度：" + delta);
		}

	}

	/***************************************************************************
	 * 概念子图间的传播
	 **************************************************************************/
	private void cnptSimPropagation(int times) {
		for (int i = 0; i < s_cnptNum; i++) {
			for (int j = 0; j < t_cnptNum; j++) {
				Set gNodes = new HashSet();
				Set gEdges = new HashSet();
				HashMap edgeMap = new HashMap();
				HashMap progWeightMap = new HashMap();
				ArrayList lt = new ArrayList();

				curCnptIDA = i;
				curCnptIDB = j;

				if (s_cnptName[i].equals("Academic")
						&& t_cnptName[j].equals("zdazsx")) {
					System.out.println("debug");
				}

				System.out.println(s_cnptName[i] + i + "--" + j + t_cnptName[j]
						+ "相似传播：");

				// 候选相似三元组对
				Set candiTPSet = new HashSet();
				candiTPSet = getSimTripleCandidate(s_cnptSubG[i].stmList,
						t_cnptSubG[j].stmList);

				if (!candiTPSet.isEmpty()) {
//					System.out.println("debug");
				}

				/* 如果是多次全局迭代，则需要判断两次迭代的子图是否改变 */
				// if (times>0 && isSameTPSet(candiTPSet,cnptCandiTPSet[i][j])){
				// continue;
				// }
				lt = consTriplePairGraph(candiTPSet);
				gNodes = (HashSet) lt.get(0);
				gEdges = (HashSet) lt.get(1);
				edgeMap = (HashMap) lt.get(2);
				progWeightMap = (HashMap) lt.get(3);

				Set oldCTPSet = new HashSet();// 判断更新的标志集合

				sGUpdate = true;
				int ct = 0;
				while (sGUpdate && ct < 6) {
					// 相似度传播，直到子图不再更新为止
					propagation(edgeMap, progWeightMap);

					// 更新候选三元组对相似度
					candiTPSet = updateTriplePairSim(candiTPSet, edgeMap);

					// 和老的ctp比较
					sGUpdate = isSameCTPSet(candiTPSet, oldCTPSet);

					// 记录当前的ctp
					oldCTPSet = ((Set) ((HashSet) candiTPSet).clone());

					if (sGUpdate && ct < 6) {
						// 更新子图
						updateTripleCandidate(s_cnptSubG[i].stmList,
								t_cnptSubG[j].stmList, candiTPSet, edgeMap);
						lt = consTriplePairGraph(candiTPSet);
						gNodes = (HashSet) lt.get(0);
						gEdges = (HashSet) lt.get(1);
						edgeMap = (HashMap) lt.get(2);
						progWeightMap = (HashMap) lt.get(3);
					}
					ct++;
				}

				/* 保存此次迭代得到的子图 */
				if (times == 0) {
					cnptCandiTPSet[i][j] = new HashMap();
				}
				cnptCandiTPSet[i][j] = edgeMap;

				// /*取出新的相似度*/
				// //通过图的点找到所有的概念相似对
				// cnptSimK1[i][j]=cnptSimK0[i][j];
				// for (Iterator it=gNodes.iterator();it.hasNext();){
				// PairGraphRes pair = (PairGraphRes)it.next();
				// /*由于只能出现在点上，因此不取edgepair*/
				//					
				// //判断相似对
				// if ((s_baseURI+s_cnptName[i]).equals(pair.resA.toString())
				// && (t_baseURI+t_cnptName[j]).equals(pair.resB.toString())){
				// cnptSimK1[i][j]=pair.sim0;
				// cnptSimKr[i][j]=pair.simr;
				// break;
				// }
				// }
			}
		}
	}

	/***************************************************************************
	 * 通过判断是否得到新的相似对，来估计 传播过程是否能结束
	 **************************************************************************/
	private boolean isSameCTPSet(Set newCTPSet, Set oldCTPSet) {
		if (newCTPSet.size() != oldCTPSet.size()) {// 发生更新
			return true;
		}

		boolean result = false;// 假设没发生更新
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

	/***************************************************************************
	 * 更新候选三元组对中 的对应相似度
	 **************************************************************************/
	private Set updateTriplePairSim(Set tPairSet, HashMap edgeMap) {
		Set newtpSet = new HashSet();
		/* 遍历候选三元组对 */
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
			for (Iterator itx = edgeMap.entrySet().iterator(); itx.hasNext();) {
				java.util.Map.Entry entry = (java.util.Map.Entry) itx.next();
				ArrayList t2PairRes = (ArrayList) entry.getValue();
				PairGraphRes pairS = (PairGraphRes) t2PairRes.get(0);
				PairGraphRes pairO = (PairGraphRes) t2PairRes.get(1);
				PairGraphRes pairP = (PairGraphRes) t2PairRes.get(2);

				if (pairS.resA.toString().equals(subA.toString())
						&& pairS.resB.toString().equals(subB.toString())) {
					tpair.simS = pairS.sim0;
					tpair.simSr = pairS.simr;
				}
				if (pairO.resA.toString().equals(objA.toString())
						&& pairO.resB.toString().equals(objB.toString())) {
					tpair.simO = pairO.sim0;
					tpair.simOr = pairO.simr;
				}
				if (pairP.resA.toString().equals(propA.toString())
						&& pairP.resB.toString().equals(propB.toString())) {
					tpair.simP = pairP.sim0;
					tpair.simPr = pairP.simr;
				}
			}

			/* 将明显错误的triple pair从图中消除,只保留可信的 */
			if (tpair.simS > 0.05 && tpair.simP > 0.05 && tpair.simO > 0.05) {
				newtpSet.add(tpair);
			}

		}

		return newtpSet;// 返回过滤不可信的结果

	}

	/***************************************************************************
	 * 属性子图间的传播
	 **************************************************************************/
	private void propSimPropagation(int times) {
		for (int i = 0; i < s_propNum; i++) {
			for (int j = 0; j < t_propNum; j++) {
				Set gNodes = new HashSet();
				Set gEdges = new HashSet();
				HashMap edgeMap = new HashMap();
				HashMap progWeightMap = new HashMap();
				ArrayList lt = new ArrayList();

				curPropIDA = i;
				curPropIDB = j;

				if (s_propName[i].equals("communications")
						&& t_propName[j].equals("organization")) {
					System.out.println("debug");
				}

				// 候选相似三元组
				Set candiTPSet = new HashSet();
				candiTPSet = getSimTripleCandidate(s_propSubG[i].stmList,
						t_propSubG[j].stmList);

				System.out.println(s_propName[i] + i + "--" + j + t_propName[j]
						+ "相似传播：");

				/* 如果是多次全局迭代，则需要判断两次迭代的子图是否改变 */
				// if (times>0 && isSameTPSet(candiTPSet,propCandiTPSet[i][j])){
				// continue;
				// }
				lt = consTriplePairGraph(candiTPSet);
				gNodes = (HashSet) lt.get(0);
				gEdges = (HashSet) lt.get(1);
				edgeMap = (HashMap) lt.get(2);
				progWeightMap = (HashMap) lt.get(3);

				Set oldCTPSet = new HashSet();// 判断更新的标志集合

				sGUpdate = true;
				int ct = 0;
				while (sGUpdate && ct < 8) {
					// 相似度传播，直到子图不再更新为止
					propagation(edgeMap, progWeightMap);

					// 更新候选三元组对相似度
					candiTPSet = updateTriplePairSim(candiTPSet, edgeMap);

					// 和老的ctp比较
					sGUpdate = isSameCTPSet(candiTPSet, oldCTPSet);

					// 记录当前的ctp
					oldCTPSet = ((Set) ((HashSet) candiTPSet).clone());

					if (sGUpdate) {
						// 更新子图
						updateTripleCandidate(s_propSubG[i].stmList,
								t_propSubG[j].stmList, candiTPSet, edgeMap);
						lt = consTriplePairGraph(candiTPSet);
						gNodes = (HashSet) lt.get(0);
						gEdges = (HashSet) lt.get(1);
						edgeMap = (HashMap) lt.get(2);
						progWeightMap = (HashMap) lt.get(3);
					}
					ct++;
				}

				/* 保存此次迭代得到的子图 */
				if (times == 0) {
					propCandiTPSet[i][j] = new HashMap();
				}
				propCandiTPSet[i][j] = edgeMap;

				// /*取出新的相似度*/
				// //通过图的点和边找到所有的属性相似对
				// double tempPSim=0;
				// double tSimr=0;
				// propSimK1[i][j]=propSimK0[i][j];
				// for (Iterator
				// itx=edgeMap.entrySet().iterator();itx.hasNext();){
				// java.util.Map.Entry entry=(java.util.Map.Entry)itx.next();
				// ArrayList t2PairRes=(ArrayList)entry.getValue();
				// PairGraphRes pairS=(PairGraphRes)t2PairRes.get(0);
				// PairGraphRes pairO=(PairGraphRes)t2PairRes.get(1);
				// PairGraphRes pairP=(PairGraphRes)t2PairRes.get(2);
				//					
				// //判断相似对
				// if ((s_baseURI+s_propName[i]).equals(pairS.resA.toString())
				// && (t_baseURI+t_propName[j]).equals(pairS.resB.toString())){
				// tempPSim=Math.max(tempPSim,pairS.sim0);
				// propSimK1[i][j]=tempPSim;
				// tSimr=Math.max(tSimr,pairS.simr);
				// propSimKr[i][j]=tSimr;
				// break;
				// }
				// if ((s_baseURI+s_propName[i]).equals(pairP.resA.toString())
				// && (t_baseURI+t_propName[j]).equals(pairP.resB.toString())){
				// tempPSim=Math.max(tempPSim,pairP.sim0);
				// propSimK1[i][j]=tempPSim;
				// tSimr=Math.max(tSimr,pairP.simr);
				// propSimKr[i][j]=tSimr;
				// break;
				// }
				// if ((s_baseURI+s_propName[i]).equals(pairO.resA.toString())
				// && (t_baseURI+t_propName[j]).equals(pairO.resB.toString())){
				// tempPSim=Math.max(tempPSim,pairO.sim0);
				// propSimK1[i][j]=tempPSim;
				// tSimr=Math.max(tSimr,pairO.simr);
				// propSimKr[i][j]=tSimr;
				// break;
				// }
				// }
			}
		}
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
	 * 初始化
	 **************************************************************************/
	private void initPara() {
		s_cnptName = new String[s_cnptNum];
		s_propName = new String[s_propNum];
		s_insName = new String[s_insNum];
		s_cnptSubG = new ConceptSubGraph[s_cnptNum];
		s_propSubG = new PropertySubGraph[s_propNum];
		t_cnptName = new String[t_cnptNum];
		t_propName = new String[t_propNum];
		t_insName = new String[t_insNum];
		cnptSimRaw = new double[s_cnptNum][t_cnptNum];
		propSimRaw = new double[s_propNum][t_propNum];
		insSimRaw = new double[s_insNum][t_insNum];

		cnptOtElmSim = new ArrayList[s_cnptNum][t_cnptNum];
		propOtElmSim = new ArrayList[s_propNum][t_propNum];
		cnptCombOESim = new ArrayList[s_cnptNum];
		propOtElmSim = new ArrayList[s_propNum][t_propNum];
		propCombOESim = new ArrayList[s_propNum];

		cnptTriplePair = new Set[s_cnptNum][t_cnptNum];
		propTriplePair = new Set[s_propNum][t_propNum];

		cnptCandiTPSet = new HashMap[s_cnptNum][t_cnptNum];
		propCandiTPSet = new HashMap[s_propNum][t_propNum];

		ontParse = new OWLOntParse();
		ontLngURI = ontParse.metaURISet;

		cnptSimK0 = new double[s_cnptNum][t_cnptNum];
		cnptSimK1 = new double[s_cnptNum][t_cnptNum];
		cnptSimKr = new double[s_cnptNum][t_cnptNum];
		propSimK0 = new double[s_propNum][t_propNum];
		propSimK1 = new double[s_propNum][t_propNum];
		propSimKr = new double[s_propNum][t_propNum];

		updateCnpt = new boolean[s_cnptNum][t_cnptNum];
		updateProp = new boolean[s_propNum][t_propNum];

		s_cnptCard = new HashMap[s_cnptNum];
		t_cnptCard = new HashMap[t_cnptNum];
		s_propCard = new HashMap[s_propNum];
		t_propCard = new HashMap[t_propNum];
	}

	/***************************************************************************
	 * 查询全局概念相似矩阵
	 **************************************************************************/
	private double queryCnptSimRaw(String nameA, String nameB) {
		double sim = 0;
		// boolean flag=false;
		// for (int i=0;i<s_cnptNum;i++){
		// if (!nameA.equals(s_cnptName[i])){
		// continue;
		// }
		// for (int j=0;j<t_cnptNum;j++){
		// if (nameB.equals(t_cnptName[j])){
		// sim=cnptSimRaw[i][j];
		// flag=true;
		// break;
		// }
		// }
		// if (flag){break;}
		// }

		sim = ((Double) cnptSimMap.get(nameA + nameB)).doubleValue();

		return sim;
	}

	/***************************************************************************
	 * 查询全局属性相似矩阵
	 **************************************************************************/
	private double queryPropSimRaw(String nameA, String nameB) {
		double sim = 0;
		// boolean flag=false;
		// for (int i=0;i<s_propNum;i++){
		// if (!nameA.equals(s_propName[i])){
		// continue;
		// }
		// for (int j=0;j<t_propNum;j++){
		// if (nameB.equals(t_propName[j])){
		// sim=propSimRaw[i][j];
		// flag=true;
		// break;
		// }
		// }
		// if (flag){break;}
		// }
		sim = ((Double) propSimMap.get(nameA + nameB)).doubleValue();
		return sim;
	}

	/***************************************************************************
	 * 查询全局实例相似矩阵
	 **************************************************************************/
	private double queryInsSimRaw(String nameA, String nameB) {
		double sim = 0;
		// boolean flag=false;
		// for (int i=0;i<s_insNum;i++){
		// if (!nameA.equals(s_insName[i])){
		// continue;
		// }
		// for (int j=0;j<t_insNum;j++){
		// if (nameB.equals(t_insName[j])){
		// sim=insSimRaw[i][j];
		// flag=true;
		// break;
		// }
		// }
		// if (flag){break;}
		// }
		sim = ((Double) insSimMap.get(nameA + nameB)).doubleValue();
		return sim;
	}

	/***************************************************************************
	 * 查询局部相似矩阵
	 **************************************************************************/
	private double queryLocalSimRaw(String nameA, String nameB) {
		double sim = 0;
		ArrayList elmSimList = new ArrayList();

		/* 判断局部环境：概念子图还是属性子图 */
		if (flagCnptSugG) {
			elmSimList = cnptOtElmSim[curCnptIDA][curCnptIDB];
		}
		if (flagPropSugG) {
			elmSimList = propOtElmSim[curPropIDA][curPropIDB];
		}

		for (Iterator it = elmSimList.iterator(); it.hasNext();) {
			GraphElmSim pair = (GraphElmSim) it.next();
			if (pair.elmNameA.equals(nameA) && pair.elmNameB.equals(nameB)) {
				sim = pair.sim;
				break;
			}
		}
		return sim;
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
		t_cnptNum = ((Integer) paraList.get(11)).intValue();
		t_propNum = ((Integer) paraList.get(12)).intValue();
		t_insNum = ((Integer) paraList.get(13)).intValue();

		// 根据得到的number初始化各种数组
		initPara();

		s_cnptName = (String[]) (paraList.get(5));
		s_propName = (String[]) (paraList.get(6));
		s_insName = (String[]) (paraList.get(7));

		s_cnptSubG = (ConceptSubGraph[]) (paraList.get(8));
		s_propSubG = (PropertySubGraph[]) (paraList.get(9));

		s_baseURI = (String) (paraList.get(10));

		t_cnptName = (String[]) (paraList.get(14));
		t_propName = (String[]) (paraList.get(15));
		t_insName = (String[]) (paraList.get(16));

		t_cnptSubG = new ConceptSubGraph[t_cnptNum];
		t_propSubG = new PropertySubGraph[t_propNum];
		t_cnptSubG = (ConceptSubGraph[]) (paraList.get(17));
		t_propSubG = (PropertySubGraph[]) (paraList.get(18));

		t_baseURI = (String) (paraList.get(19));

		cnptSimRaw = (double[][]) (paraList.get(20));
		propSimRaw = (double[][]) (paraList.get(21));
		insSimRaw = (double[][]) (paraList.get(22));

		s_AnonCnpt = (ArrayList) (paraList.get(23));
		s_AnonProp = (ArrayList) (paraList.get(24));
		s_AnonIns = (ArrayList) (paraList.get(25));
		t_AnonCnpt = (ArrayList) (paraList.get(26));
		t_AnonProp = (ArrayList) (paraList.get(27));
		t_AnonIns = (ArrayList) (paraList.get(28));

		cnptOtElmSim = (ArrayList[][]) (paraList.get(29));
		propOtElmSim = (ArrayList[][]) (paraList.get(30));

		//处理合并的other元素相似度
		for (int i = 0; i < s_cnptNum; i++) {
			cnptCombOESim[i] = new ArrayList();
			for (int j = 0; j < t_cnptNum; j++) {
				for (Iterator it = cnptOtElmSim[i][j].iterator(); it.hasNext();) {
					GraphElmSim pair = (GraphElmSim) it.next();
					boolean flag = true;
					for (Iterator itx = cnptCombOESim[i].iterator(); itx.hasNext();) {
						GraphElmSim tp = (GraphElmSim) itx.next();
						if (pair.elmNameA.equals(tp.elmNameA)&& pair.elmNameB.equals(tp.elmNameB)) {
							flag = false;
							break;
						}
					}
					if (flag) {
						cnptCombOESim[i].add(pair);
					}
				}
			}
		}
		for (int i = 0; i < s_propNum; i++) {
			propCombOESim[i] = new ArrayList();
			for (int j = 0; j < t_propNum; j++) {
				for (Iterator it = propOtElmSim[i][j].iterator(); it.hasNext();) {
					GraphElmSim pair = (GraphElmSim) it.next();
					boolean flag = true;
					for (Iterator itx = propCombOESim[i].iterator(); itx.hasNext();) {
						GraphElmSim tp = (GraphElmSim) itx.next();
						if (pair.elmNameA.equals(tp.elmNameA)&& pair.elmNameB.equals(tp.elmNameB)) {
							flag = false;
							break;
						}
					}
					if (flag) {
						propCombOESim[i].add(pair);
					}
				}
			}
		}
	}
	
	/*相似度传播前的数据结构预处理*/
	private void dataStruProc()
	{
		ArrayList lt=null;		
		
		/**1.将不可能的三元组过滤：即非元语>=2，位于相似序列中**/
		for (int i=0;i<s_cnptNum;i++){
			ArrayList tmpStm=new ArrayList();
			for (Iterator itx = s_cnptSubG[i].stmList.iterator(); itx.hasNext();){
				Statement st = (Statement) itx.next();
				if (metaElmInTriple(st) < 2){
					tmpStm.add(st);
				}
			}
			s_cnptSubG[i].stmList=tmpStm;
		}
		for (int i=0;i<s_propNum;i++){
			ArrayList tmpStm=new ArrayList();
			for (Iterator itx = s_propSubG[i].stmList.iterator(); itx.hasNext();){
				Statement st = (Statement) itx.next();
				if (metaElmInTriple(st) < 2){
					tmpStm.add(st);
				}
			}
			s_propSubG[i].stmList=tmpStm;
		}
		for (int i=0;i<t_cnptNum;i++){
			ArrayList tmpStm=new ArrayList();
			for (Iterator itx = t_cnptSubG[i].stmList.iterator(); itx.hasNext();){
				Statement st = (Statement) itx.next();
				if (metaElmInTriple(st) < 2){
					tmpStm.add(st);
				}
			}
			t_cnptSubG[i].stmList=tmpStm;
		}
		for (int i=0;i<t_propNum;i++){
			ArrayList tmpStm=new ArrayList();
			for (Iterator itx = t_propSubG[i].stmList.iterator(); itx.hasNext();){
				Statement st = (Statement) itx.next();
				if (metaElmInTriple(st) < 2){
					tmpStm.add(st);
				}
			}
			t_propSubG[i].stmList=tmpStm;
		}
		
		/**2.克隆原始相似度**/
		cnptSimK0 = cnptSimRaw.clone();
		for (int i = 0; i < s_cnptNum; i++) {
			if (cnptSimK0[i] != null) {
				cnptSimK0[i] = cnptSimRaw[i].clone();
			}
		}
		propSimK0 = propSimRaw.clone();
		for (int i = 0; i < s_propNum; i++) {
			if (propSimK0[i] != null) {
				propSimK0[i] = propSimRaw[i].clone();
			}
		}

		
		
		/**2.1 Hash表：元素<-->相似元素
		 * 2.2 Hash表：相似元素<-->相似度**/
		/**概念相似矩阵Hash表**/
		/**C-C hash table**/
		cnptSimMap = new HashMap();
		HashMap CCMap = new HashMap();
		for (int i = 0; i < s_cnptNum; i++) {
			String sName=s_baseURI+s_cnptName[i];
			for (int j = 0; j < t_cnptNum; j++) {
				String tName=t_baseURI+t_cnptName[j];
				cnptSimMap.put(s_cnptName[i] + t_cnptName[j], cnptSimRaw[i][j]);
				if (cnptSimRaw[i][j]>0.05){
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
		HashMap PPMap = new HashMap();
		for (int i = 0; i < s_propNum; i++) {
			String sName=s_baseURI+s_propName[i];
			for (int j = 0; j < t_propNum; j++) {
				String tName=t_baseURI+t_propName[j];
				propSimMap.put(s_propName[i] + t_propName[j], propSimRaw[i][j]);
				if (propSimRaw[i][j]>0.05){
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
		HashMap IIMap = new HashMap();
		for (int i = 0; i < s_insNum; i++) {
			String sName=s_baseURI+s_insName[i];
			for (int j = 0; j < t_insNum; j++) {
				String tName=t_baseURI+t_insName[j];
				insSimMap.put(s_insName[i] + t_insName[j], insSimRaw[i][j]);
				if (insSimRaw[i][j]>0.05){
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
		HashMap LLMap = new HashMap();
		for (int i = 0; i < s_cnptNum; i++) {
			for (int j = 0; j < t_cnptNum; j++) {
				for (Iterator it = cnptOtElmSim[i][j].iterator(); it.hasNext();) {
					GraphElmSim pair = (GraphElmSim) it.next();
					if (pair.sim>0.1){
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
			}
		}
		
		/**合并“元素--相似元素”Hash表**/
		EEMap = new HashMap();
		EEMap.putAll(CCMap);
		EEMap.putAll(PPMap);
		EEMap.putAll(IIMap);
		EEMap.putAll(LLMap);
		
		/**3. Hash表:元素<-->相关三元组**/
//		sE2Triple = new HashMap();
//		ArrayList[] trLt;
//		for (Iterator itx = s_stmList.iterator(); itx.hasNext();) {
//			Statement st = (Statement) itx.next();
//			// 分离三元组
//			Resource sub = st.getSubject();
//			Property prop = st.getPredicate();
//			RDFNode obj = st.getObject();
//			String subName = sub.toString();
//			String propName = prop.toString();
//			String objName = obj.toString();
//			String urlS = null, urlP = null, urlO = null;
//			if (sub.isURIResource()) {
//				urlS = sub.getNameSpace();
//			}
//			if (prop.isURIResource()) {
//				urlP = prop.getNameSpace();
//			}
//			if (obj.isURIResource()) {
//				urlO = obj.asNode().getNameSpace();
//			}
//			
//			trLt=new ArrayList[3];
//			trLt[0]=new ArrayList();
//			trLt[1]=new ArrayList();
//			trLt[2]=new ArrayList();
//			if (!ontLngURI.contains(urlS)){
//				if (sE2Triple.containsKey(subName)){
//					trLt=(ArrayList[])sE2Triple.get(subName);
//				}
//				trLt[0].add(st);
//				sE2Triple.put(subName,trLt);
//			}
//			trLt=new ArrayList[3];
//			trLt[0]=new ArrayList();
//			trLt[1]=new ArrayList();
//			trLt[2]=new ArrayList();
//			if (!ontLngURI.contains(urlP)){
//				if (sE2Triple.containsKey(propName)){
//					trLt=(ArrayList[])sE2Triple.get(propName);
//				}
//				trLt[1].add(st);
//				sE2Triple.put(propName,trLt);
//			}
//			trLt=new ArrayList[3];
//			trLt[0]=new ArrayList();
//			trLt[1]=new ArrayList();
//			trLt[2]=new ArrayList();
//			if (!ontLngURI.contains(urlO)){
//				if (sE2Triple.containsKey(objName)){
//					trLt=(ArrayList[])sE2Triple.get(objName);
//				}
//				trLt[2].add(st);
//				sE2Triple.put(objName,trLt);
//			}			
//		}
		
//		tE2Triple = new HashMap();
//		for (Iterator itx = t_stmList.iterator(); itx.hasNext();) {
//			Statement st = (Statement) itx.next();
//			// 分离三元组
//			Resource sub = st.getSubject();
//			Property prop = st.getPredicate();
//			RDFNode obj = st.getObject();
//			String subName = sub.toString();
//			String propName = prop.toString();
//			String objName = obj.toString();
//			
//			String urlS = null, urlP = null, urlO = null;
//			if (sub.isURIResource()) {
//				urlS = sub.getNameSpace();
//			}
//			if (prop.isURIResource()) {
//				urlP = prop.getNameSpace();
//			}
//			if (obj.isURIResource()) {
//				urlO = obj.asNode().getNameSpace();
//			}
//			trLt=new ArrayList[3];
//			trLt[0]=new ArrayList();
//			trLt[1]=new ArrayList();
//			trLt[2]=new ArrayList();
//			if (!ontLngURI.contains(urlS)){
//				if (tE2Triple.containsKey(subName)){
//					trLt=(ArrayList[])tE2Triple.get(subName);
//				}
//				trLt[0].add(st);
//				tE2Triple.put(subName,trLt);
//			}
//			trLt=new ArrayList[3];
//			trLt[0]=new ArrayList();
//			trLt[1]=new ArrayList();
//			trLt[2]=new ArrayList();
//			if (!ontLngURI.contains(urlP)){
//				if (tE2Triple.containsKey(propName)){
//					trLt=(ArrayList[])tE2Triple.get(propName);
//				}
//				trLt[1].add(st);
//				tE2Triple.put(propName,trLt);
//			}
//			trLt=new ArrayList[3];
//			trLt[0]=new ArrayList();
//			trLt[1]=new ArrayList();
//			trLt[2]=new ArrayList();
//			if (!ontLngURI.contains(urlO)){
//				if (tE2Triple.containsKey(objName)){
//					trLt=(ArrayList[])tE2Triple.get(objName);
//				}
//				trLt[2].add(st);
//				tE2Triple.put(objName,trLt);
//			}
//		}
//		
//		System.out.println("数据结构建立完成");		
//		
	}
}
