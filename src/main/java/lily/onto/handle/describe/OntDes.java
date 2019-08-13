/************************************************
 * Source code information
 * -----------------------
 * Original author	 Peng Wang, School of Computer Science & Eng., Southeast University
 * Author email      pwangseu@gmail.com
 * Web               http://ontomapping.googlepages.com
 * Created			 2007-5-14
 * Filename          OntDes.java
 * Version           2.0
 * 
 * Last modified on  2007-5-14
 *               by  Peng Wang
 * -----------------------
 * Functions describe:
 * 计算本体的文本描描述和结构描述，计算范围限定于元素对应的语义子图中
 * 基本原理：根据本体描述元素的方式对描述文本进行划分
 * 技术难点：处理描述中子图里的匿名节点；设定文本合理的权值
 ***********************************************/
package lily.onto.handle.describe;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import lily.onto.parse.OWLOntParse;
import lily.tool.datastructure.ConceptSubGraph;
import lily.tool.datastructure.ConceptWt;
import lily.tool.datastructure.GraphTriple;
import lily.tool.datastructure.InstanceWt;
import lily.tool.datastructure.OntLngMetaWt;
import lily.tool.datastructure.PropertySubGraph;
import lily.tool.datastructure.PropertyWt;
import lily.tool.datastructure.TextDes;
import lily.tool.datastructure.Word;
import lily.tool.textprocess.DelStopWords;
import lily.tool.textprocess.SplitWords;

import com.hp.hpl.jena.ontology.ConversionException;
import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/*******************************************************************************
 * Class information -------------------
 * 
 * @author Peng Wang
 * @date 2007-5-14
 * 
 * describe: 本体元素的文本描述和结构描述
 ******************************************************************************/
@SuppressWarnings("unchecked")
public class OntDes {
	// 基本本体模型
	public OntModel m;

	// 概念数目
	public int conceptNum;

	// 属性数目
	public int propNum;

	// 实例数目
	public int insNum;

	// 概念名
	public String[] conceptName;

	// 属性名
	public String[] propName;

	// 实例名
	public String[] insName;

	// base URI
	public String baseURI;

	// 不局限于baseURI下的本体元素
	public int fullConceptNum;

	public int fullPropNum;

	public int fullInsNum;

	public OntClass[] fullConceptName;

	public OntProperty[] fullPropName;

	public Individual[] fullInsName;

	// 匿名资源
	ArrayList anonCnpt;

	ArrayList anonProp;

	ArrayList anonIns;

	// 基本文本描述
	public TextDes[] cnptBasicTextDes;

	public TextDes[] propBasicTextDes;

	public TextDes[] insBasicTextDes;

	// 概念的文本描述
	public TextDes[] cnptTextDes;

	// 属性的文本描述
	public TextDes[] propTextDes;

	// 实例的文本描述
	public TextDes[] insTextDes;

	// 子图中非基本信息的文本描述
	public ArrayList[] cnptOtTextDes;

	public ArrayList[] propOtTextDes;

	public ArrayList fullOtTextDes;

	// 子图
	public ConceptSubGraph[] cnptSubG;

	public PropertySubGraph[] propSubG;

	public ArrayList curStmList;

	public ArrayList fullGraphStms;

	public boolean isSubProg;

    public static boolean isSkipCnptName = false;

	// 本体元信息
	Set ontLngURI;

	OWLOntParse ontParse;

	// 常量
	public double wLocalName = 1.0;

	public double wLabel = 1.0;

	public double wComment = 0.8;

	public double wSeeAlso = 0.6;

	public double wIsDefineBy = 0.6;

	public double wEQClass = 1.0;

	public double wAnonNode = 0.8;

	public double subClassDec = 0.08;// 衰减系数

	public double superClassDec = 0.1;// 衰减系数

	public double subPropertyDec = 0.1;// 衰减系数

	public double superPropertyDec = 0.15;// 衰减系数

	public double wsiblingClassDec = 0.8;

	public double wsiblingPropertyDec = 0.8;

	public double wDisjClassDec = 0.6;

	public double wCmplClassDec = 0.9;
	
	
	/***************************************************************************
	 * 本体文本描述类的主入口
	 **************************************************************************/
	public ArrayList getOntDes(ArrayList list) {
		ArrayList desResult = new ArrayList();

		/* 解析参数 */
		unPackPara(list);
		/* 文本描述 */
		getOntTextDes();
		/* 结构描述 */
		getOntStructDes();

		/* 保存计算结果 */
		desResult.add(0, cnptTextDes);
		desResult.add(1, propTextDes);
		desResult.add(2, insTextDes);
		desResult.add(3, cnptOtTextDes);
		desResult.add(4, propOtTextDes);
		desResult.add(5, fullOtTextDes);

		return desResult;
	}
	
	public ArrayList getCnptOntDes(ArrayList list, String cnptName)
	{
		ArrayList desResult=new ArrayList();
		
		//解析参数
		unPackPara(list);
		//文本描述
		TextDes td=getOneCnptOntTextDes(cnptName);
		//结构描述
		getOntStructDes();
		
		desResult.add(0,td);

		return desResult;
	}
	
	public ArrayList getPropOntDes(ArrayList list, String propName)
	{
		ArrayList desResult=new ArrayList();
		
		//解析参数
		unPackPara(list);
		//文本描述
		TextDes td=getOnePropOntTextDes(propName);
		//结构描述
		getOntStructDes();
		
		desResult.add(0,td);

		return desResult;
	}
	
	public TextDes getOneCnptOntTextDes(String name)
	{
		/*计算基本描述*/
		basicTextDes();
		/*计算概念的文本描述*/
		TextDes td=cnptTextDes(name);
		return td;
	}
	
	public TextDes getOnePropOntTextDes(String name)
	{
		/*计算基本描述*/
		basicTextDes();
		/*计算概念的文本描述*/
		TextDes td=propTextDes(name);
		return td;
	}
	
	/***************************************************************************
	 * 计算本体的文本描述
	 **************************************************************************/
	public void getOntTextDes() {
		/* 计算基本描述 */
		basicTextDes();
		/* 计算概念的文本描述 */
		cnptTextDes();
		/* 计算属性的文本描述 */
		propTextDes();
		/* 计算实例的文本描述 */
		insTextDes();
		/* 全图其它元素描述 */
		fullOtTextDes = fullOtherTexDes();
	}
	
	/***************************************************************************
	 * 计算基本的文本描述
	 **************************************************************************/
	public void basicTextDes() {
		/* 概念基本描述 */
		cnptBasicTextDes();
		/* 属性基本描述 */
		propBasicTextDes();
		/* 实例基本描述 */
		insBasicTextDes();
		/* 完善概念基本描述 */
		perfectCnptBTDes();
		/* 完善属性基本描述 */
		perfectPropBTDes();
		/* 完善实例基本描述 */
		perfectInsBTDes();

	}
	
	/***************************************************************************
	 * 完善概念的基本文本描述 增加isDefinedby，seeAlso和equivalentClass,sameAs
	 **************************************************************************/
	public void perfectCnptBTDes() {
		/* 先完善匿名概念的基本描述 */
		for (int i = 0; i < fullConceptNum; i++) {
			OntClass c = fullConceptName[i];
			if (c.isAnon()) {
				ArrayList anonList = anonBasicDesInModel(c, 0);
				for (Iterator itx = anonList.iterator(); itx.hasNext();) {
					Word w = (Word) ((Word) itx.next()).clone();
					cnptBasicTextDes[i].text.add(w);
				}
			}
		}
		
		TextDes[] richCnptBasicTextDes = new TextDes[fullConceptNum];
		/* 初始化描述 */
		for (int i = 0; i < fullConceptNum; i++) {
			richCnptBasicTextDes[i] = new TextDes();
			richCnptBasicTextDes[i].name = cnptBasicTextDes[i].name;
			richCnptBasicTextDes[i].text = new ArrayList();
			if (cnptBasicTextDes[i].text != null) {
				richCnptBasicTextDes[i].text = (ArrayList) (cnptBasicTextDes[i].text)
						.clone();
			}
		}

		for (int i = 0; i < fullConceptNum; i++) {
			OntClass c = fullConceptName[i];

			// if (c.isAnon()){//不考虑匿名概念
			//				continue;
			//			}
			
			/*增加seeAlso*/
			Property p = m.getProperty("http://www.w3.org/2000/01/rdf-schema#seeAlso");
			Selector sl = new SimpleSelector(c, p, (RDFNode) null);
			for (StmtIterator it = m.listStatements(sl); it.hasNext();) {
				Statement st = it.next();
				RDFNode o = st.getObject();
				if (!o.isAnon()) {// 非匿名
					OntClass cx = m.getOntClass(o.toString());
					if (cx != null) {// 非空概念
						/* 加入该概念的Des */
						int pos = findCnptPosInFullName(cx.toString());
						for (Iterator it1 = cnptBasicTextDes[pos].text
								.iterator(); it1.hasNext();) {
							Word w = (Word) it1.next();
							w.weight *= wSeeAlso;// 注意操作不能改变原来的word
							richCnptBasicTextDes[i].text.add(w);
						}
					}
				}
			}
	    	
	    	/* 增加isDefinedBy */
			p = m.getProperty("http://www.w3.org/2000/01/rdf-schema#isDefinedBy");
			sl = new SimpleSelector(c, p, (RDFNode) null);
			for (StmtIterator it = m.listStatements(sl); it.hasNext();) {
				Statement st = it.next();
				RDFNode o = st.getObject();
				if (!o.isAnon()) {// 非匿名
					OntClass cx = m.getOntClass(o.toString());
					if (cx != null) {// 非空概念
						/* 加入该概念的Des */
						int pos = findCnptPosInFullName(cx.toString());
						for (Iterator it1 = cnptBasicTextDes[pos].text
								.iterator(); it1.hasNext();) {
							Word w = (Word) it1.next();
							w.weight *= wSeeAlso;// 注意操作不能改变原来的word
							richCnptBasicTextDes[i].text.add(w);
						}
					}
				}
			}
		}

		for (int i = 0; i < fullConceptNum; i++) {
			OntClass c = fullConceptName[i];

			// if (c.isAnon()){//不考虑匿名概念
			// continue;
			// }

			/* 增加equivalentClass */
			Property p = m.getProperty("http://www.w3.org/2002/07/owl#equivalentClass");
			Selector sl = new SimpleSelector(c, p, (RDFNode) null);
			for (StmtIterator it = m.listStatements(sl); it.hasNext();) {
				Statement st = it.next();
				RDFNode o = st.getObject();
				OntClass cx = null;
				if (!o.isAnon()) {
					cx = m.getOntClass(o.toString());
				} else {
					cx = this.findCnptInFullName(o.toString());
				}

				if (!cx.isAnon()) {// 非匿名
					/* 加入该概念的Des */
					int pos = findCnptPosInFullName(cx.toString());
					if (pos >= 0) {
						for (Iterator it1 = cnptBasicTextDes[pos].text
								.iterator(); it1.hasNext();) {
							Word w = (Word) it1.next();
							richCnptBasicTextDes[i].text.add(w);
						}
					}
				} else {// 匿名概念
					/* 得到匿名节点的Des */
					ArrayList anonList = anonBasicDesInModel(cx, 0);
					for (Iterator it1 = anonList.iterator(); it1.hasNext();) {
						Word w = (Word) it1.next();
						richCnptBasicTextDes[i].text.add(w);
					}
				}
			}
		}

		for (int i = 0; i < fullConceptNum; i++) {
			OntClass c = fullConceptName[i];

			// if (c.isAnon()){//不考虑匿名概念
			// continue;
			// }

			/* 增加sameAs */
			Property p = m.getProperty("http://www.w3.org/2002/07/owl#sameAs");
			Selector sl = new SimpleSelector(c, p, (RDFNode) null);
			for (StmtIterator it = m.listStatements(sl); it.hasNext();) {
				Statement st = it.next();
				RDFNode o = st.getObject();
				OntClass cx = null;
				if (!o.isAnon()) {
					cx = m.getOntClass(o.toString());
				} else {
					cx = this.findCnptInFullName(o.toString());
				}
				if (null==cx){continue;}
				
				if (!cx.isAnon()) {// 非匿名
					/* 加入该概念的Des */
					int pos = findCnptPosInFullName(cx.toString());
					if (pos >= 0) {
						for (Iterator it1 = cnptBasicTextDes[pos].text
								.iterator(); it1.hasNext();) {
							Word w = (Word) it1.next();
							richCnptBasicTextDes[i].text.add(w);
						}
					}
				} else {// 匿名概念
					/* 得到匿名节点的Des */
					ArrayList anonList = anonBasicDesInModel(cx, 0);
					for (Iterator it1 = anonList.iterator(); it1.hasNext();) {
						Word w = (Word) it1.next();
						richCnptBasicTextDes[i].text.add(w);
					}
				}
			}
		}

		/* 后置处理 */
		for (int i = 0; i < fullConceptNum; i++) {
			richCnptBasicTextDes[i].text = desPostProcess(richCnptBasicTextDes[i].text);
		}
		/* 还原基本描述 */
		cnptBasicTextDes = richCnptBasicTextDes;
	}
	
	/***************************************************************************
	 * 完善属性的基本文本描述 增加isDefinedby，seeAlso和equivalentProperty,sameAs
	 **************************************************************************/
	public void perfectPropBTDes() {
		TextDes[] richPropBasicTextDes = new TextDes[fullPropNum];
		/* 初始化描述 */
		for (int i = 0; i < fullPropNum; i++) {
			richPropBasicTextDes[i] = new TextDes();
			richPropBasicTextDes[i].name = propBasicTextDes[i].name;
			richPropBasicTextDes[i].text = new ArrayList();
			if (propBasicTextDes[i].text != null) {
				richPropBasicTextDes[i].text = (ArrayList) (propBasicTextDes[i].text)
						.clone();
			}
		}

		for (int i = 0; i < fullPropNum; i++) {
			OntProperty p = fullPropName[i];

			if (p.isAnon()) {// 不考虑匿名属性
				continue;
			}

			/* 增加seeAlso */
			Property pm = m
					.getProperty("http://www.w3.org/2000/01/rdf-schema#seeAlso");
			Selector sl = new SimpleSelector(p, pm, (RDFNode) null);
			for (StmtIterator it = m.listStatements(sl); it.hasNext();) {
				Statement st = it.next();
				RDFNode o = st.getObject();
				if (!o.isAnon()) {// 非匿名
					OntProperty px = m.getOntProperty(o.toString());
					if (px != null) {// 非空属性
						/* 加入该属性的Des */
						int pos = findPropPosInFullName(px.toString());
						for (Iterator it1 = propBasicTextDes[pos].text
								.iterator(); it1.hasNext();) {
							Word w = (Word) it1.next();
							w.weight *= wSeeAlso;// 注意操作不能改变原来的word
							richPropBasicTextDes[i].text.add(w);
						}
					}
				}
			}

			/* 增加isDefinedBy */
			pm = m
					.getProperty("http://www.w3.org/2000/01/rdf-schema#isDefinedBy");
			sl = new SimpleSelector(p, pm, (RDFNode) null);
			for (StmtIterator it = m.listStatements(sl); it.hasNext();) {
				Statement st = it.next();
				RDFNode o = st.getObject();
				if (!o.isAnon()) {// 非匿名
					OntProperty px = m.getOntProperty(o.toString());
					if (px != null) {// 非空属性
						/* 加入该属性的Des */
						int pos = findPropPosInFullName(px.toString());
						for (Iterator it1 = propBasicTextDes[pos].text
								.iterator(); it1.hasNext();) {
							Word w = (Word) it1.next();
							w.weight *= wSeeAlso;// 注意操作不能改变原来的word
							richPropBasicTextDes[i].text.add(w);
						}
					}
				}
			}
		}
		
		for (int i = 0; i < fullPropNum; i++) {
			OntProperty p = fullPropName[i];

			if (p.isAnon()) {// 不考虑匿名属性
				continue;
			}

			/* 增加equivalentProperty */
			Property pm = m
					.getProperty("http://www.w3.org/2002/07/owl#equivalentProperty");
			Selector sl = new SimpleSelector(p, pm, (RDFNode) null);
			for (StmtIterator it = m.listStatements(sl); it.hasNext();) {
				Statement st = it.next();
				RDFNode o = st.getObject();
				OntProperty px = null;
				if (!o.isAnon()) {
					px = m.getOntProperty(o.toString());
				} else {
					px = this.findPropInFullName(o.toString());
				}

				if (!px.isAnon()) {// 非匿名
					/* 加入该属性的Des */
					int pos = findPropPosInFullName(px.toString());
					if (pos >= 0) {
						for (Iterator it1 = propBasicTextDes[pos].text
								.iterator(); it1.hasNext();) {
							Word w = (Word) it1.next();
							richPropBasicTextDes[i].text.add(w);
						}
					}
				} else {// 匿名属性
					/* 得到匿名节点的Des */
					ArrayList anonList = anonBasicDesInModel(px, 0);
					for (Iterator it1 = anonList.iterator(); it1.hasNext();) {
						Word w = (Word) it1.next();
						richPropBasicTextDes[i].text.add(w);
					}
				}
			}
		}
		
		for (int i = 0; i < fullPropNum; i++) {
			OntProperty p = fullPropName[i];

			if (p.isAnon()) {// 不考虑匿名属性
				continue;
			}

			/* 增加sameAs */
			Property pm = m.getProperty("http://www.w3.org/2002/07/owl#sameAs");
			Selector sl = new SimpleSelector(p, pm, (RDFNode) null);
			for (StmtIterator it = m.listStatements(sl); it.hasNext();) {
				Statement st = it.next();
				RDFNode o = st.getObject();
				OntProperty px = null;
				if (!o.isAnon()) {
					px = m.getOntProperty(o.toString());
				} else {
					px = this.findPropInFullName(o.toString());
				}

				if (!px.isAnon()) {// 非匿名
					/* 加入该属性的Des */
					int pos = findPropPosInFullName(px.toString());
					if (pos >= 0) {
						for (Iterator it1 = propBasicTextDes[pos].text
								.iterator(); it1.hasNext();) {
							Word w = (Word) it1.next();
							richPropBasicTextDes[i].text.add(w);
						}
					}
				} else {// 匿名属性
					/* 得到匿名节点的Des */
					ArrayList anonList = anonBasicDesInModel(px, 0);
					for (Iterator it1 = anonList.iterator(); it1.hasNext();) {
						Word w = (Word) it1.next();
						richPropBasicTextDes[i].text.add(w);
					}
				}
			}
		}

		/* 后置处理 */
		for (int i = 0; i < fullPropNum; i++) {
			richPropBasicTextDes[i].text = desPostProcess(richPropBasicTextDes[i].text);
		}
		/* 还原基本描述 */
		propBasicTextDes = richPropBasicTextDes;
	}
	
	/***************************************************************************
	 * 完善实例的基本文本描述 增加sameAs
	 **************************************************************************/
	public void perfectInsBTDes() {
		TextDes[] richInsBasicTextDes = new TextDes[fullInsNum];
		/* 初始化描述 */
		for (int i = 0; i < fullInsNum; i++) {
			richInsBasicTextDes[i] = new TextDes();
			richInsBasicTextDes[i].name = insBasicTextDes[i].name;
			richInsBasicTextDes[i].text = new ArrayList();
			if (insBasicTextDes[i].text != null) {
				richInsBasicTextDes[i].text = (ArrayList) (insBasicTextDes[i].text)
						.clone();
			}
		}

		for (int i = 0; i < fullInsNum; i++) {
			Individual id = fullInsName[i];

			if (id.isAnon()) {// 不考虑匿名实例
				continue;
			}

			/* 增加sameAs */
			Property pm = m.getProperty("http://www.w3.org/2002/07/owl#sameAs");
			Selector sl = new SimpleSelector(id, pm, (RDFNode) null);
			for (StmtIterator it = m.listStatements(sl); it.hasNext();) {
				Statement st = it.next();
				RDFNode o = st.getObject();
				Individual idx = null;
				if (!o.isAnon()) {
					idx = m.getIndividual(o.toString());
				} else {
					idx = this.findInsInFullName(o.toString());
				}
				if (!idx.isAnon()) {// 非匿名
					/* 加入该实例的Des */
					int pos = findInsPosInFullName(idx.toString());
					if (pos >= 0) {
						for (Iterator it1 = insBasicTextDes[pos].text
								.iterator(); it1.hasNext();) {
							Word w = (Word) it1.next();
							richInsBasicTextDes[i].text.add(w);
						}
					}
				} else {// 匿名实例
					/* 得到匿名节点的Des */
					ArrayList anonList = anonBasicDesInModel(idx, 0);
					for (Iterator it1 = anonList.iterator(); it1.hasNext();) {
						Word w = (Word) it1.next();
						richInsBasicTextDes[i].text.add(w);
					}
				}
			}
		}

		/* 后置处理 */
		for (int i = 0; i < fullInsNum; i++) {
			richInsBasicTextDes[i].text = desPostProcess(richInsBasicTextDes[i].text);
		}
		/* 还原基本描述 */
		insBasicTextDes = richInsBasicTextDes;
	}

	/***************************************************************************
	 * 概念的基本文本描述
	 **************************************************************************/
	public void cnptBasicTextDes() {
		SplitWords spWord = new SplitWords();
		DelStopWords delSWrod = new DelStopWords();
		delSWrod.loadStopWords();

		/* 概念基本描述 */
		for (int i = 0; i < fullConceptNum; i++) {
			cnptBasicTextDes[i] = new TextDes();
			OntClass c = fullConceptName[i];
			cnptBasicTextDes[i].text = new ArrayList();
			if (!c.isAnon()) {// 不是匿名概念
				cnptBasicTextDes[i].name = fullConceptName[i].getLocalName();
				String s = null;
				ArrayList list = new ArrayList();
				/* local name */
				s = c.getLocalName();
				/* 文本预处理 */
				s = delSWrod.removeStopWords(s);
				list = spWord.split(s);
				list = delSWrod.removeStopWords(list);
				/* 添加权重 */
				for (Iterator it = list.iterator(); it.hasNext();) {
					String stemp = (String) it.next();
					Word w = new Word();
					w.content = stemp;
					w.weight = wLocalName;
					if (!isSkipCnptName) // 对于Rclm和LOM区别对待
					    cnptBasicTextDes[i].text.add(w);

				}

				/* label */
				for (ExtendedIterator itx=c.listLabels(null);itx.hasNext();){
					Literal L=(Literal)itx.next();
					s = null;
					list = null;
					s = L.getLexicalForm();
					if (s != null) {
						/* 文本预处理 */
						s = delSWrod.removeStopWords(s);
						list = spWord.split(s);
						list = delSWrod.removeStopWords(list);
						/* 添加权重 */
						for (Iterator it = list.iterator(); it.hasNext();) {
							String stemp = (String) it.next();
							Word w = new Word();
							w.content = stemp;
							w.weight = wLabel;
							cnptBasicTextDes[i].text.add(w);
						}
					}
				}

				/* comment */
				s = null;
				for (ExtendedIterator itx=c.listComments(null);itx.hasNext();){
					Literal L=(Literal)itx.next();
					s = null;
					list = null;
					s = L.getLexicalForm();
					/* 文本预处理 */
					s = delSWrod.removeStopWords(s);
					list = spWord.split(s);
					list = delSWrod.removeStopWords(list);
					/* 添加权重 */
					for (Iterator it = list.iterator(); it.hasNext();) {
						String stemp = (String) it.next();
						Word w = new Word();
						w.content = stemp;
						w.weight = wComment;
						cnptBasicTextDes[i].text.add(w);
					}
				}
				

			} else {
				/* 处理匿名概念 */
				cnptBasicTextDes[i].name = fullConceptName[i].toString();
				// 在全部基本描述完成后进行
			}
		}
	}

	/***************************************************************************
	 * 属性的基本文本描述
	 **************************************************************************/
	public void propBasicTextDes() {
		SplitWords spWord = new SplitWords();
		DelStopWords delSWrod = new DelStopWords();
		delSWrod.loadStopWords();

		/* 属性基本描述 */
		for (int i = 0; i < fullPropNum; i++) {
			propBasicTextDes[i] = new TextDes();
			OntProperty p = fullPropName[i];
			if (!p.isAnon()) {// 不是匿名属性
				propBasicTextDes[i].name = fullPropName[i].getLocalName();
				propBasicTextDes[i].text = new ArrayList();
				String s = null;
				ArrayList list = new ArrayList();
				/* local name */
				s = p.getLocalName();
				/* 文本预处理 */
				s = delSWrod.removeStopWords(s);
				list = spWord.split(s);
				list = delSWrod.removeStopWords(list);
				/* 添加权重 */
				for (Iterator it = list.iterator(); it.hasNext();) {
					String stemp = (String) it.next();
					Word w = new Word();
					w.content = stemp;
					w.weight = wLocalName;
					propBasicTextDes[i].text.add(w);
				}

				/* label */
				for (ExtendedIterator itx=p.listLabels(null);itx.hasNext();){
					Literal L=(Literal)itx.next();
					s = null;
					list = null;
					s = L.getLexicalForm();
					if (s != null) {
						/* 文本预处理 */
						s = delSWrod.removeStopWords(s);
						list = spWord.split(s);
						list = delSWrod.removeStopWords(list);
						/* 添加权重 */
						for (Iterator it = list.iterator(); it.hasNext();) {
							String stemp = (String) it.next();
							Word w = new Word();
							w.content = stemp;
							w.weight = wLabel;
							propBasicTextDes[i].text.add(w);
						}
					}
				}

				/* comment */
				s = null;
				list = null;
				s = p.getComment(null);
				if (s != null) {
					/* 文本预处理 */
					s = delSWrod.removeStopWords(s);
					list = spWord.split(s);
					list = delSWrod.removeStopWords(list);
					/* 添加权重 */
					for (Iterator it = list.iterator(); it.hasNext();) {
						String stemp = (String) it.next();
						Word w = new Word();
						w.content = stemp;
						w.weight = wComment;
						propBasicTextDes[i].text.add(w);
					}
				}
			}
		}
	}

	/***************************************************************************
	 * 实例的基本文本描述
	 **************************************************************************/
	public void insBasicTextDes() {
		SplitWords spWord = new SplitWords();
		DelStopWords delSWrod = new DelStopWords();
		delSWrod.loadStopWords();

		/* 实例基本描述 */
		for (int i = 0; i < fullInsNum; i++) {
			insBasicTextDes[i] = new TextDes();
			Individual d = fullInsName[i];
			if (!d.isAnon()) {// 不是匿名实例
				insBasicTextDes[i].name = fullInsName[i].getLocalName();
				insBasicTextDes[i].text = new ArrayList();
				String s = null;
				ArrayList list = new ArrayList();
				/* local name */
				s = d.getLocalName();
				/* 文本预处理 */
				s = delSWrod.removeStopWords(s);
				list = spWord.split(s);
				list = delSWrod.removeStopWords(list);
				/* 添加权重 */
				for (Iterator it = list.iterator(); it.hasNext();) {
					String stemp = (String) it.next();
					Word w = new Word();
					w.content = stemp;
					w.weight = wLocalName;
					insBasicTextDes[i].text.add(w);
				}

				/* label */
				for (ExtendedIterator itx=d.listLabels(null);itx.hasNext();){
					Literal L=(Literal)itx.next();
					s = null;
					list = null;
					s = L.getLexicalForm();
					if (s != null) {
						/* 文本预处理 */
						s = delSWrod.removeStopWords(s);
						list = spWord.split(s);
						list = delSWrod.removeStopWords(list);
						/* 添加权重 */
						for (Iterator it = list.iterator(); it.hasNext();) {
							String stemp = (String) it.next();
							Word w = new Word();
							w.content = stemp;
							w.weight = wLabel;
							insBasicTextDes[i].text.add(w);
						}
					}
				}

				/* comment */
				s = null;
				list = null;
				/* local name */
				s = d.getComment(null);
				if (s != null) {
					/* 文本预处理 */
					s = delSWrod.removeStopWords(s);
					list = spWord.split(s);
					list = delSWrod.removeStopWords(list);
					/* 添加权重 */
					for (Iterator it = list.iterator(); it.hasNext();) {
						String stemp = (String) it.next();
						Word w = new Word();
						w.content = stemp;
						w.weight = wComment;
						insBasicTextDes[i].text.add(w);
					}
				}
			}
		}
	}
	
	/***************************************************************************
	 * 计算概念的文本描述
	 **************************************************************************/
	public void cnptTextDes() {
		for (int i = 0; i < conceptNum; i++) {
			OntClass c = m.getOntClass(baseURI + conceptName[i]);

			if (i == 30) {
//				System.out.println("debug");
			}

			/* c对应的子图和三元组 */
			int pos = getSubGraph(c);
			DirectedGraph gc = cnptSubG[pos].subGraph;
			// curStmList.clear();
			curStmList = cnptSubG[pos].stmList;

			cnptTextDes[i] = new TextDes();
			cnptTextDes[i].name = conceptName[i];
			cnptTextDes[i].text = new ArrayList();

			/* 计算self描述,在basic描述中已经完成 */
			int basicDespos = findCnptPosInFullName(c.toString());
			ArrayList lt = new ArrayList();
			lt = cnptBasicTextDes[basicDespos].text;
			cnptTextDes[i].text.add(0, lt);

			/* 类层次文本描述 */
			lt = cnptHrcTextDes(c, gc);
			cnptTextDes[i].text.add(1, lt);

			/* 附加属性描述 */
			lt = cnptPropTextDes(c, gc);
			cnptTextDes[i].text.add(2, lt);

			/* 实例特征描述 */
			lt = cnptInsTexDes(c, gc);
			cnptTextDes[i].text.add(3, lt);

			/* 概念子图中其它元素的描述 */
			cnptOtTextDes[i] = cnptOtherTexDes(c, gc);
		}
	}
	
	public TextDes cnptTextDes(String cnptName) {
		TextDes textDes = new TextDes();

		OntClass c = m.getOntClass(baseURI + cnptName);
		/* c对应的子图和三元组 */
		int pos = getSubGraph(c);
		DirectedGraph gc = cnptSubG[pos].subGraph;
		curStmList = cnptSubG[pos].stmList;
		textDes.name = cnptName;
		textDes.text = new ArrayList();

		/* 计算self描述,在basic描述中已经完成 */
		int basicDespos = findCnptPosInFullName(c.toString());
		ArrayList lt = new ArrayList();
		lt = cnptBasicTextDes[basicDespos].text;
		textDes.text.add(0, lt);

		/* 类层次文本描述 */
		lt = cnptHrcTextDes(c, gc);
		textDes.text.add(1, lt);

		/* 附加属性描述 */
		lt = cnptPropTextDes(c, gc);
		textDes.text.add(2, lt);

		/* 实例特征描述 */
		lt = cnptInsTexDes(c, gc);
		textDes.text.add(3, lt);
		return textDes;
	}

	/***************************************************************************
	 * 计算属性的文本描述
	 **************************************************************************/
	public void propTextDes() {
		for (int i = 0; i < propNum; i++) {
			OntProperty p = m.getOntProperty(baseURI + propName[i]);

			if (i == 52) {
//				System.out.println("debug");
			}
			/* p对应的子图和三元组 */
			int pos = getSubGraph(p);
			DirectedGraph gp = propSubG[pos].subGraph;
			// curStmList.clear();
			curStmList = propSubG[pos].stmList;

			propTextDes[i] = new TextDes();
			propTextDes[i].name = propName[i];
			propTextDes[i].text = new ArrayList();

			/* 计算self描述,在basic描述中已经完成 */
			int basicDespos = findPropPosInFullName(p.toString());
			ArrayList lt = new ArrayList();
			lt = propBasicTextDes[basicDespos].text;
			propTextDes[i].text.add(0, lt);

			/* 层次文本描述 */
			lt = propHrcTextDes(p, gp);
			propTextDes[i].text.add(1, lt);

			/* 作用特征描述 */
			lt = propFunctionTextDes(p, gp);
			propTextDes[i].text.add(2, lt);

			/* 实例特征描述 */
			lt = propInsTexDes(p, gp);
			propTextDes[i].text.add(3, lt);

			/* 属性子图中其它元素的描述 */
			propOtTextDes[i] = propOtherTexDes(p, gp);
		}
	}

	public TextDes propTextDes(String propName) {
		TextDes textDes = new TextDes();
		OntProperty p = m.getOntProperty(baseURI + propName);

			/* p对应的子图和三元组 */
			int pos = getSubGraph(p);
			DirectedGraph gp = propSubG[pos].subGraph;
			// curStmList.clear();
			curStmList = propSubG[pos].stmList;
			textDes.name = propName;
			textDes.text = new ArrayList();

			/* 计算self描述,在basic描述中已经完成 */
			int basicDespos = findPropPosInFullName(p.toString());
			ArrayList lt = new ArrayList();
			lt = propBasicTextDes[basicDespos].text;
			textDes.text.add(0, lt);

			/* 层次文本描述 */
			lt = propHrcTextDes(p, gp);
			textDes.text.add(1, lt);

			/* 作用特征描述 */
			lt = propFunctionTextDes(p, gp);
			textDes.text.add(2, lt);

			/* 实例特征描述 */
			lt = propInsTexDes(p, gp);
			textDes.text.add(3, lt);
			
			return textDes;
	}
	
	/***************************************************************************
	 * 计算实例的文本描述 直接从基本描述中取得
	 **************************************************************************/
	public void insTextDes() {
		for (int i = 0; i < insNum; i++) {
			Individual idv = m.getIndividual(baseURI + insName[i]);

			insTextDes[i] = new TextDes();
			insTextDes[i].name = insName[i];
			insTextDes[i].text = new ArrayList();

			if (idv == null) {
				continue;
			}

			/* 计算self描述,在basic描述中已经完成 */
			int basicDespos = findInsPosInFullName(idv.toString());
			insTextDes[i].text = insBasicTextDes[basicDespos].text;
		}
	}	
	
	/***************************************************************************
	 * 计算本体的结构描述
	 **************************************************************************/
	public void getOntStructDes() {
		/* 计算概念的结构描述 */

		/* 计算属性的结构描述 */
	}

	/***************************************************************************
	 * 接收本体参数
	 **************************************************************************/
	public void unPackPara(ArrayList paraList) {
		m = (OntModel) paraList.get(0);
		conceptNum = ((Integer) paraList.get(1)).intValue();
		propNum = ((Integer) paraList.get(2)).intValue();
		insNum = ((Integer) paraList.get(3)).intValue();

		fullConceptNum = ((Integer) paraList.get(7)).intValue();
		fullPropNum = ((Integer) paraList.get(8)).intValue();
		fullInsNum = ((Integer) paraList.get(9)).intValue();

		// 根据得到的number初始化各种数组
		initPara();
		conceptName = (String[]) (paraList.get(4));
		propName = (String[]) (paraList.get(5));
		insName = (String[]) (paraList.get(18));
		fullConceptName = (OntClass[]) (paraList.get(10));
		fullPropName = (OntProperty[]) (paraList.get(11));
		fullInsName = (Individual[]) (paraList.get(12));
		cnptSubG = (ConceptSubGraph[]) (paraList.get(13));
		propSubG = (PropertySubGraph[]) (paraList.get(14));
		baseURI = (String) paraList.get(6);

		// 匿名资源
		anonCnpt = (ArrayList) paraList.get(15);
		anonProp = (ArrayList) paraList.get(16);
		anonIns = (ArrayList) paraList.get(17);

		// 全图三元组
		fullGraphStms = (ArrayList) paraList.get(19);

		// 传播模式
		isSubProg = (Boolean) paraList.get(20);
	}

	/***************************************************************************
	 * 初始化本体的一些数据结构
	 **************************************************************************/
	public void initPara() {
		/* 基本信息 */
		conceptName = new String[conceptNum];
		propName = new String[propNum];
		insName = new String[insNum];
		fullConceptName = new OntClass[fullConceptNum];
		fullPropName = new OntProperty[fullPropNum];
		fullInsName = new Individual[fullInsNum];

		/* 描述信息 */
		cnptBasicTextDes = new TextDes[fullConceptNum];
		propBasicTextDes = new TextDes[fullPropNum];
		insBasicTextDes = new TextDes[fullInsNum];
		cnptTextDes = new TextDes[conceptNum];
		propTextDes = new TextDes[propNum];
		insTextDes = new TextDes[insNum];

		cnptOtTextDes = new ArrayList[conceptNum];
		propOtTextDes = new ArrayList[propNum];
		fullOtTextDes = new ArrayList();

		// 本体子图
		cnptSubG = new ConceptSubGraph[conceptNum];
		propSubG = new PropertySubGraph[propNum];
		curStmList = new ArrayList();

		ontLngURI = new OWLOntParse().getOntLngURI();
		ontParse = new OWLOntParse();
	}

	public int findCnptPosInFullName(String s) {
		int pos = -1;
		for (int i = 0; i < fullConceptNum; i++) {
			if (fullConceptName[i].toString().equals(s)) {
				pos = i;
				break;
			}
		}
		if (pos == -1)
			return -1;
		return pos;
	}

	public OntClass findCnptInFullName(String s) {
		OntClass c = null;
		for (int i = 0; i < fullConceptNum; i++) {
			if (fullConceptName[i].toString().equals(s)) {
				c = fullConceptName[i];
				break;
			}
		}
		return c;
	}

	public int findPropPosInFullName(String s) {
		int pos = -1;
		for (int i = 0; i < fullPropNum; i++) {
			if (fullPropName[i].toString().equals(s)) {
				pos = i;
				break;
			}
		}
		return pos;
	}

	public OntProperty findPropInFullName(String s) {
		OntProperty c = null;
		for (int i = 0; i < fullPropNum; i++) {
			if (fullPropName[i].toString().equals(s)) {
				c = fullPropName[i];
				break;
			}
		}
		return c;
	}

	public int findInsPosInFullName(String s) {
		int pos = -1;
		for (int i = 0; i < fullInsNum; i++) {
			if (fullInsName[i].toString().equals(s)) {
				pos = i;
				break;
			}
		}
		return pos;
	}

	public Individual findInsInFullName(String s) {
		Individual c = null;
		for (int i = 0; i < fullInsNum; i++) {
			if (fullInsName[i].toString().equals(s)) {
				c = fullInsName[i];
				break;
			}
		}
		return c;
	}

	/***************************************************************************
	 * 递归求匿名节点在整个本体中的描述
	 **************************************************************************/
	public ArrayList anonDesInModel(Resource b, int level) {
		ArrayList result = new ArrayList();

		/* 判断它是否有等价对象 */
		boolean hasEqu = false;
		ArrayList eqList = anonEquInModel(b);
		int subType = ((Integer) eqList.get(0)).intValue();
		if (subType >= 1 && subType <= 3) {
			ArrayList member = (ArrayList) eqList.get(1);
			if (subType == 1) {// 类
				for (Iterator jt = member.iterator(); jt.hasNext();) {
					OntClass ctemp = (OntClass) jt.next();
					int tempPos = findCnptPosInFullName(ctemp.toString());
					result.addAll(cnptBasicTextDes[tempPos].text);
					hasEqu = true;
				}
			} else if (subType == 2) {// 属性
				for (Iterator jt = member.iterator(); jt.hasNext();) {
					OntProperty ptemp = (OntProperty) jt.next();
					int tempPos = findPropPosInFullName(ptemp.toString());
					result.addAll(propBasicTextDes[tempPos].text);
					hasEqu = true;
				}
			} else if (subType == 3) {// 实例
				for (Iterator jt = member.iterator(); jt.hasNext();) {
					Individual itemp = (Individual) jt.next();
					int tempPos = findInsPosInFullName(itemp.toString());
					result.addAll(insBasicTextDes[tempPos].text);
					hasEqu = true;
				}
			}
		}

		/* 如果有等价对象，直接返回 */
		if (hasEqu) {
			return result;
		}

		/* 求以b开头的三元组 */
		Selector s = new SimpleSelector(b, null, (RDFNode) null);
		for (StmtIterator it = m.listStatements(s); it.hasNext();) {
			Statement st = it.nextStatement();
			Property p = st.getPredicate();
			RDFNode o = st.getObject();

			if (p.isAnon()) {// p为匿名的情况不考虑
				result = null;
			}

			/* p的Description */
			ArrayList lp = new ArrayList();
			/* 判断p是不是元属性 */
			if (!isMetaOntData(p)) {
				int pPos = findPropPosInFullName(p.toString());
				lp = propBasicTextDes[pPos].text;
			}

			if (lp != null) {
				result.addAll(lp);
			}

			ArrayList lo = new ArrayList();
			/* 递归终止条件 */
			if (!o.isAnon()) {
				/* 过滤元语 */
				if (!isMetaOntData(m.getResource(o.toString()))) {
					/* 判断o的成分 */
					int t = getResourceType(o.toString());
					if (t == 1) {// Class
						int tempPos = findCnptPosInFullName(o.toString());
						/* 得到Description */
						lo = cnptBasicTextDes[tempPos].text;
					} else if (t == 2) {// Property
						int tempPos = findPropPosInFullName(o.toString());
						/* 得到Description */
						lo = propBasicTextDes[tempPos].text;
					} else if (t == 3) {// Instance
						int tempPos = findInsPosInFullName(o.toString());
						/* 得到Description */
						lo = insBasicTextDes[tempPos].text;
					} else if (t == 4) {// Other resource
						/* 直接处理local name */
						String localname = null;
						if (o.isLiteral()) {
							Literal l = (Literal) o;
							localname = l.getValue().toString();
						} else {
							localname = o.asNode().getLocalName();
						}
						ArrayList otherList = simpleStringPreProcess(localname);
						for (Iterator xt = otherList.iterator(); xt.hasNext();) {
							String stemp = (String) xt.next();
							Word w = new Word();
							w.content = stemp;
							w.weight = 1.0;
							lo.add(w);
						}
					}
					/* return终止条件 */
					result.addAll(lo);
				}
			} else {
				/* 递归 */
				lo = anonDesInModel(m.getResource(o.toString()), level + 1);

				/* 乘衰减系数 */
				double downPare = Math.pow(wAnonNode, ((double) level + 1.0));
				for (Iterator itx = lo.iterator(); itx.hasNext();) {
					Word w = (Word) itx.next();
					w.weight *= downPare;
				}
				result.addAll(lo);
			}
		}
		return result;
	}
	
	/***************************************************************************
	 * 递归求匿名节点在整个本体中的基本描述 即只考虑普通的连接
	 **************************************************************************/
	public ArrayList anonBasicDesInModel(Resource b, int level) {
		ArrayList result = new ArrayList();
		/* 求以b开头的三元组 */
		Selector s = new SimpleSelector(b, null, (RDFNode) null);
		for (StmtIterator it = m.listStatements(s); it.hasNext();) {
			Statement st = it.nextStatement();
			Property p = st.getPredicate();
			RDFNode o = st.getObject();

			if (p.isAnon()) {// p为匿名的情况不考虑
				result = null;
			}

			/* p的Description */
			ArrayList lp = new ArrayList();
			boolean hasEqu = false;
			/* 判断p是不是元属性 */
			if (!isMetaOntData(p)) {
				/* 直接处理local name */
				ArrayList otherList = simpleStringPreProcess(p.getLocalName());
				for (Iterator xt = otherList.iterator(); xt.hasNext();) {
					String stemp = (String) xt.next();
					Word w = new Word();
					w.content = stemp;
					w.weight = 1.0;
					lp.add(w);
				}
			} else {
				String strp = p.getLocalName().toString();
				if (strp.equals("equivalentClass")
						|| strp.equals("equivalentProperty")
						|| strp.equals("sameAs")) {
					hasEqu = true;
				}
			}

			if (lp != null) {
				result.addAll(lp);
			}

			/* 递归终止条件 */
			ArrayList lo = new ArrayList();
			if (!hasEqu) {// 没有等价的关系
				if (!o.isAnon()) {
					/* 过滤元语 */
					if (!isMetaOntData(m.getResource(o.toString()))) {
						/* 直接处理local name */
						String ostr = null;
						if (o.isLiteral()) {
							ostr = o.asNode().getLiteralValue().toString();
						} else if (o.isURIResource()) {
							ostr = o.asNode().getLocalName();
						}
						ArrayList otherList = simpleStringPreProcess(ostr);
						for (Iterator xt = otherList.iterator(); xt.hasNext();) {
							String stemp = (String) xt.next();
							Word w = new Word();
							w.content = stemp;
							w.weight = 1.0;
							lo.add(w);
						}
						/* return终止条件 */
						result.addAll(lo);
					}
				} else {
					/* 递归 */
					lo = anonBasicDesInModel(m.getResource(o.toString()),
							level + 1);
					/* 乘衰减系数 */
					double downPare = Math.pow(wAnonNode,
							((double) level + 1.0));
					for (Iterator itx = lo.iterator(); itx.hasNext();) {
						Word w = (Word) itx.next();
						w.weight *= downPare;
					}
					result.addAll(lo);
				}
			}
		}
		return result;
	}

	public ArrayList anonEquInModel(Resource r) {
		ArrayList result = new ArrayList();
		ArrayList member = new ArrayList();
		// 得到sub的类型
		int subType = getResourceType(r.toString());
		result.add(0, subType);
		if (subType == 1) {// Class
			OntClass cx = null;
			cx = this.findCnptInFullName(r.toString());

			/* 判断有没有等价类 */
			for (ExtendedIterator it = cx.listEquivalentClasses(); it.hasNext();) {
				OntClass cy = (OntClass) it.next();
				if (!cy.isAnon()) {
					member.add(cy);
				}
			}
			/* 判断有没有sameAs */
			for (ExtendedIterator it = cx.listSameAs(); it.hasNext();) {
				OntClass cy = (OntClass) it.next();
				if (!cy.isAnon()) {
					member.add(cy);
				}
			}
		} else if (subType == 2) {// Property
			OntProperty px = null;
			px = this.findPropInFullName(r.toString());
			/* 判断有没有等价属性 */
			for (ExtendedIterator it = px.listEquivalentProperties(); it
					.hasNext();) {
				OntProperty cy = (OntProperty) it.next();
				if (!cy.isAnon()) {
					member.add(cy);
				}
			}
			/* 判断有没有sameAs */
			for (ExtendedIterator it = px.listSameAs(); it.hasNext();) {
				OntProperty cy = (OntProperty) it.next();
				if (!cy.isAnon()) {
					member.add(cy);
				}
			}

		} else if (subType == 3) {// Instance
			Individual idx = null;
			idx = this.findInsInFullName(r.toString());
			/* 判断有没有sameAs */
			for (ExtendedIterator it = idx.listSameAs(); it.hasNext();) {
				Individual cy = (Individual) it.next();
				if (!cy.isAnon()) {
					member.add(cy);
				}
			}
		}
		result.add(1, member);
		return result;
	}

	/***************************************************************************
	 * 递归求匿名节点在子图中的描述 只考虑blank node开头的三元组
	 **************************************************************************/
	public ArrayList anonDesInSubGraphSingleDirect(Resource b, int level,
			DirectedGraph g) {
		ArrayList result = new ArrayList();
		/* 求以b开头的三元组 */
		Selector s = new SimpleSelector(b, null, (RDFNode) null);
		for (StmtIterator it = m.listStatements(s); it.hasNext();) {
			Statement st = it.nextStatement();
			Property p = st.getPredicate();
			RDFNode o = st.getObject();

			ArrayList localNamelist = ontParse.getStLocalName(st);
			// String pStr=(String)localNamelist.get(1);
			String oStr = (String) localNamelist.get(2);

			/* 当前三元组在图中的条件必须是o在图中,但这并不是充分条件 */
			if (!g.containsVertex(oStr)) {
				continue;
			}

			if (p.isAnon()) {// p为匿名的情况不考虑
				result = null;
			}

			/* p的Description */
			ArrayList lp = new ArrayList();
			/* 判断p是不是元属性 */
			if (!isMetaOntData(p)) {
				int pPos = findPropPosInFullName(p.toString());
				if (pPos != -1) {
					lp = propBasicTextDes[pPos].text;
				} else {
					/* 直接提取local name */
					String localname = null;
					localname = p.getLocalName();

					ArrayList otherList = simpleStringPreProcess(localname);
					for (Iterator xt = otherList.iterator(); xt.hasNext();) {
						String stemp = (String) xt.next();
						Word w = new Word();
						w.content = stemp;
						w.weight = 1.0;
						lp.add(w);
					}
				}
			}

			if (lp != null) {
				result.addAll(lp);
			}

			/* o的Description */
			ArrayList lo = new ArrayList();
			/* 递归终止条件 */
			if (!ontParse.isBlankNode(o.toString())) {
				/* 过滤元语 */
				if (!isMetaOntData(m.getResource(o.toString()))) {
					/* 判断o的成分 */
					int t = getResourceType(o.toString());
					if (t == 1) {// Class
						int tempPos = findCnptPosInFullName(o.toString());
						lo = cnptBasicTextDes[tempPos].text;
					} else if (t == 2) {// Property
						int tempPos = findPropPosInFullName(o.toString());
						/* 得到Description */
						lo = propBasicTextDes[tempPos].text;
					} else if (t == 3) {// Instance
						int tempPos = findInsPosInFullName(o.toString());
						/* 得到Description */
						if (tempPos == -1) {
							System.out.println("stop");
						}
						lo = insBasicTextDes[tempPos].text;
					} else if (t == 4) {// Other resource
						/* 直接提取local name */
						String localname = null;
						if (o.isLiteral()) {
							Literal l = (Literal) o;
							if (l.getLexicalForm().equals("12")){
								System.out.println("debug Literal");
							}
							localname = l.getLexicalForm();
						} else {
							localname = o.asNode().getLocalName();
						}
						if (localname.equals("")) {
							localname = o.toString();
						}
						ArrayList otherList = simpleStringPreProcess(localname);
						for (Iterator xt = otherList.iterator(); xt.hasNext();) {
							String stemp = (String) xt.next();
							Word w = new Word();
							w.content = stemp;
							w.weight = 1.0;
							lo.add(w);
						}
					}
					/* return终止条件 */
					result.addAll(lo);
				}
			} else {
				/* 递归 */
				lo = anonDesInSubGraphSingleDirect(m.getResource(o.toString()),
						level + 1, g);

				/* 乘衰减系数 */
				double downPare = Math.pow(wAnonNode, ((double) level + 1.0));
				for (Iterator itx = lo.iterator(); itx.hasNext();) {
					Word w = (Word) itx.next();
					w.weight *= downPare;
				}
				result.addAll(lo);
			}
		}
		return result;
	}

	/***************************************************************************
	 * 递归求匿名节点在子图中的描述 同时考虑blank node开头和结尾的三元组
	 **************************************************************************/
	public ArrayList anonDesInSubGraphBiDirect(Resource b, int level,
			DirectedGraph g, Set visitedSet) {
		ArrayList result = new ArrayList();

		/* 求以b开头的三元组 */
		for (Iterator it = curStmList.iterator(); it.hasNext();) {
			Statement st = (Statement) it.next();
			Resource s = st.getSubject();
			Property p = st.getPredicate();
			RDFNode o = st.getObject();

			// 是否已经访问过
			if (visitedSet.contains(st)) {
				continue;// 跳过该三元组
			}

			// 判断以b开头
			if (s.toString().equals(b.toString())) {

				// 加入访问过列表
				visitedSet.add(st);

				if (p.isAnon()) {// p为匿名的情况不考虑
					result = null;
				}

				/* p的Description */
				ArrayList lp = new ArrayList();
				/* 判断p是不是元属性 */
				if (!isMetaOntData(p)) {
					int pPos = findPropPosInFullName(p.toString());
					if (pPos != -1) {
						lp = propBasicTextDes[pPos].text;
					} else {
						/* 直接提取local name */
						String localname = null;
						localname = p.getLocalName();

						ArrayList otherList = simpleStringPreProcess(localname);
						for (Iterator xt = otherList.iterator(); xt.hasNext();) {
							String stemp = (String) xt.next();
							Word w = new Word();
							w.content = stemp;
							w.weight = 1.0;
							lp.add(w);
						}
					}
				}

				if (lp != null) {
					result.addAll(lp);
				}

				/* o的Description */
				ArrayList lo = new ArrayList();
				/* 递归终止条件 */
				if (!ontParse.isBlankNode(o.toString())) {
					/* 过滤元语 */
					if (!isMetaOntData(m.getResource(o.toString()))) {
						/* 判断o的成分 */
						int t = getResourceType(o.toString());
						if (t == 1) {// Class
							int tempPos = findCnptPosInFullName(o.toString());
							lo = cnptBasicTextDes[tempPos].text;
						} else if (t == 2) {// Property
							int tempPos = findPropPosInFullName(o.toString());
							/* 得到Description */
							lo = propBasicTextDes[tempPos].text;
						} else if (t == 3) {// Instance
							int tempPos = findInsPosInFullName(o.toString());
							/* 得到Description */
							if (tempPos == -1) {
								System.out.println("stop");
							}
							lo = insBasicTextDes[tempPos].text;
						} else if (t == 4) {// Other resource
							/* 直接提取local name */
							String localname = null;
							if (o.isLiteral()) {
								Literal l = (Literal) o;
								localname = l.getLexicalForm().toString();
							} else {
								localname = o.asNode().getLocalName();
							}
							if (localname.equals("")) {
								localname = o.toString();
							}
							ArrayList otherList = simpleStringPreProcess(localname);
							for (Iterator xt = otherList.iterator(); xt
									.hasNext();) {
								String stemp = (String) xt.next();
								Word w = new Word();
								w.content = stemp;
								w.weight = 1.0;
								lo.add(w);
							}
						}
						/* return终止条件 */
						result.addAll(lo);
					}
				} else {
					/* 递归 */
					lo = anonDesInSubGraphBiDirect(m.getResource(o.toString()),
							level + 1, g, visitedSet);

					/* 乘衰减系数 */
					double downPare = Math.pow(wAnonNode,
							((double) level + 1.0));
					for (Iterator itx = lo.iterator(); itx.hasNext();) {
						Word w = (Word) itx.next();
						w.weight *= downPare;
					}
					result.addAll(lo);
				}
			}
		}

		/* 求以b结尾的三元组 */
		for (Iterator it = curStmList.iterator(); it.hasNext();) {
			Statement st = (Statement) it.next();
			Resource s = st.getSubject();
			Property p = st.getPredicate();
			RDFNode o = st.getObject();

			// 是否已经访问过
			if (visitedSet.contains(st)) {
				continue;// 跳过该三元组
			}

			// 判断以b结尾
			if (o.asNode().toString().equals(b.toString())) {

				// 加入访问过列表
				visitedSet.add(st);

				if (p.isAnon()) {// p为匿名的情况不考虑
					result = null;
				}

				/* p的Description */
				ArrayList lp = new ArrayList();
				/* 判断p是不是元属性 */
				if (!isMetaOntData(p)) {
					int pPos = findPropPosInFullName(p.toString());
					if (pPos != -1) {
						lp = propBasicTextDes[pPos].text;
					} else {
						/* 直接提取local name */
						String localname = null;
						localname = p.getLocalName();

						ArrayList otherList = simpleStringPreProcess(localname);
						for (Iterator xt = otherList.iterator(); xt.hasNext();) {
							String stemp = (String) xt.next();
							Word w = new Word();
							w.content = stemp;
							w.weight = 1.0;
							lp.add(w);
						}
					}
				}

				if (lp != null) {
					result.addAll(lp);
				}

				/* s的Description */
				ArrayList lo = new ArrayList();
				/* 递归终止条件 */
				if (!ontParse.isBlankNode(s.toString())) {
					/* 过滤元语 */
					if (!isMetaOntData(m.getResource(s.toString()))) {
						/* 判断s的成分 */
						int t = getResourceType(s.toString());
						if (t == 1) {// Class
							int tempPos = findCnptPosInFullName(s.toString());
							lo = cnptBasicTextDes[tempPos].text;
						} else if (t == 2) {// Property
							int tempPos = findPropPosInFullName(s.toString());
							/* 得到Description */
							lo = propBasicTextDes[tempPos].text;
						} else if (t == 3) {// Instance
							int tempPos = findInsPosInFullName(s.toString());
							/* 得到Description */
							if (tempPos == -1) {
								System.out.println("stop");
							}
							lo = insBasicTextDes[tempPos].text;
						} else if (t == 4) {// Other resource
							/* 直接提取local name */
							String localname = null;
							if (s.isLiteral()) {
								Literal l = (Literal) s;
								localname = l.getValue().toString();
							} else {
								localname = s.asNode().getLocalName();
							}
							if (localname.equals("")) {
								localname = s.toString();
							}
							ArrayList otherList = simpleStringPreProcess(localname);
							for (Iterator xt = otherList.iterator(); xt
									.hasNext();) {
								String stemp = (String) xt.next();
								Word w = new Word();
								w.content = stemp;
								w.weight = 1.0;
								lo.add(w);
							}
						}
						/* return终止条件 */
						result.addAll(lo);
					}
				} else {
					/* 递归 */
					lo = anonDesInSubGraphBiDirect(m.getResource(s.toString()),
							level + 1, g, visitedSet);

					/* 乘衰减系数 */
					double downPare = Math.pow(wAnonNode,
							((double) level + 1.0));
					for (Iterator itx = lo.iterator(); itx.hasNext();) {
						Word w = (Word) itx.next();
						w.weight *= downPare;
					}
					result.addAll(lo);
				}
			}
		}

		return result;
	}

	/***************************************************************************
	 * 递归求匿名节点在子图中的描述 同时考虑blank node开头和结尾的三元组
	 **************************************************************************/
	public ArrayList anonDesInFullGraphBiDirect(Resource b, int level,
			Set visitedSet) {
		ArrayList result = new ArrayList();

		/* 求以b开头的三元组 */
		for (Iterator it = fullGraphStms.iterator(); it.hasNext();) {
			Statement st = (Statement) it.next();
			Resource s = st.getSubject();
			Property p = st.getPredicate();
			RDFNode o = st.getObject();

			// 是否已经访问过
			if (visitedSet.contains(st)) {
				continue;// 跳过该三元组
			}

			// 判断以b开头
			if (s.toString().equals(b.toString())) {

				// 加入访问过列表
				visitedSet.add(st);

				if (p.isAnon()) {// p为匿名的情况不考虑
					result = null;
				}

				/* p的Description */
				ArrayList lp = new ArrayList();
				/* 判断p是不是元属性 */
				if (!isMetaOntData(p)) {
					int pPos = findPropPosInFullName(p.toString());
					if (pPos != -1) {
						lp = propBasicTextDes[pPos].text;
					} else {
						/* 直接提取local name */
						String localname = null;
						localname = p.getLocalName();

						ArrayList otherList = simpleStringPreProcess(localname);
						for (Iterator xt = otherList.iterator(); xt.hasNext();) {
							String stemp = (String) xt.next();
							Word w = new Word();
							w.content = stemp;
							w.weight = 1.0;
							lp.add(w);
						}
					}
				}

				if (lp != null) {
					result.addAll(lp);
				}

				/* o的Description */
				ArrayList lo = new ArrayList();
				/* 递归终止条件 */
				if (!ontParse.isBlankNode(o.toString())) {
					/* 过滤元语 */
					if (!isMetaOntData(m.getResource(o.toString()))) {
						/* 判断o的成分 */
						int t = getResourceType(o.toString());
						if (t == 1) {// Class
							int tempPos = findCnptPosInFullName(o.toString());
							lo = cnptBasicTextDes[tempPos].text;
						} else if (t == 2) {// Property
							int tempPos = findPropPosInFullName(o.toString());
							/* 得到Description */
							lo = propBasicTextDes[tempPos].text;
						} else if (t == 3) {// Instance
							int tempPos = findInsPosInFullName(o.toString());
							/* 得到Description */
							if (tempPos == -1) {
								System.out.println("stop");
							}
							lo = insBasicTextDes[tempPos].text;
						} else if (t == 4) {// Other resource
							/* 直接提取local name */
							String localname = null;
							if (o.isLiteral()) {
								Literal l = (Literal) o;
								localname = l.getLexicalForm().toString();
							} else {
								localname = o.asNode().getLocalName();
							}
							if (localname.equals("")) {
								localname = o.toString();
							}
							ArrayList otherList = simpleStringPreProcess(localname);
							for (Iterator xt = otherList.iterator(); xt
									.hasNext();) {
								String stemp = (String) xt.next();
								Word w = new Word();
								w.content = stemp;
								w.weight = 1.0;
								lo.add(w);
							}
						}
						/* return终止条件 */
						result.addAll(lo);
					}
				} else {
					/* 递归 */
					lo = anonDesInFullGraphBiDirect(
							m.getResource(o.toString()), level + 1, visitedSet);

					/* 乘衰减系数 */
					double downPare = Math.pow(wAnonNode,
							((double) level + 1.0));
					for (Iterator itx = lo.iterator(); itx.hasNext();) {
						Word w = (Word) itx.next();
						w.weight *= downPare;
					}
					result.addAll(lo);
				}
			}
		}

		/* 求以b结尾的三元组 */
		for (Iterator it = fullGraphStms.iterator(); it.hasNext();) {
			Statement st = (Statement) it.next();
			Resource s = st.getSubject();
			Property p = st.getPredicate();
			RDFNode o = st.getObject();

			// 是否已经访问过
			if (visitedSet.contains(st)) {
				continue;// 跳过该三元组
			}

			// 判断以b结尾
			if (o.asNode().toString().equals(b.toString())) {

				// 加入访问过列表
				visitedSet.add(st);

				if (p.isAnon()) {// p为匿名的情况不考虑
					result = null;
				}

				/* p的Description */
				ArrayList lp = new ArrayList();
				/* 判断p是不是元属性 */
				if (!isMetaOntData(p)) {
					int pPos = findPropPosInFullName(p.toString());
					if (pPos != -1) {
						lp = propBasicTextDes[pPos].text;
					} else {
						/* 直接提取local name */
						String localname = null;
						localname = p.getLocalName();

						ArrayList otherList = simpleStringPreProcess(localname);
						for (Iterator xt = otherList.iterator(); xt.hasNext();) {
							String stemp = (String) xt.next();
							Word w = new Word();
							w.content = stemp;
							w.weight = 1.0;
							lp.add(w);
						}
					}
				}

				if (lp != null) {
					result.addAll(lp);
				}

				/* s的Description */
				ArrayList lo = new ArrayList();
				/* 递归终止条件 */
				if (!ontParse.isBlankNode(s.toString())) {
					/* 过滤元语 */
					if (!isMetaOntData(m.getResource(s.toString()))) {
						/* 判断s的成分 */
						int t = getResourceType(s.toString());
						if (t == 1) {// Class
							int tempPos = findCnptPosInFullName(s.toString());
							lo = cnptBasicTextDes[tempPos].text;
						} else if (t == 2) {// Property
							int tempPos = findPropPosInFullName(s.toString());
							/* 得到Description */
							lo = propBasicTextDes[tempPos].text;
						} else if (t == 3) {// Instance
							int tempPos = findInsPosInFullName(s.toString());
							/* 得到Description */
							if (tempPos == -1) {
								System.out.println("stop");
							}
							lo = insBasicTextDes[tempPos].text;
						} else if (t == 4) {// Other resource
							/* 直接提取local name */
							String localname = null;
							if (s.isLiteral()) {
								Literal l = (Literal) s;
								localname = l.getValue().toString();
							} else {
								localname = s.asNode().getLocalName();
							}
							if (localname.equals("")) {
								localname = s.toString();
							}
							ArrayList otherList = simpleStringPreProcess(localname);
							for (Iterator xt = otherList.iterator(); xt
									.hasNext();) {
								String stemp = (String) xt.next();
								Word w = new Word();
								w.content = stemp;
								w.weight = 1.0;
								lo.add(w);
							}
						}
						/* return终止条件 */
						result.addAll(lo);
					}
				} else {
					/* 递归 */
					lo = anonDesInFullGraphBiDirect(
							m.getResource(s.toString()), level + 1, visitedSet);

					/* 乘衰减系数 */
					double downPare = Math.pow(wAnonNode,
							((double) level + 1.0));
					for (Iterator itx = lo.iterator(); itx.hasNext();) {
						Word w = (Word) itx.next();
						w.weight *= downPare;
					}
					result.addAll(lo);
				}
			}
		}

		return result;
	}

	public int getResourceType(String s) {
		Resource r = m.getResource(s);
        //if (s.contains("weightUnitEnum"))
            //s += "";
		if (r == null) {
			return -1;// 不是resource
		} else if (r.isLiteral()) {// 文字
			return 4;
		} else if (ontLngURI.contains(r.getNameSpace())) {// 元语
			return 4;
		} else {
			OntResource ontr = m.getOntResource(r);
			if (!ontParse.isBlankNode(s)) {
				/* 非匿名 */
                String ss;
                Resource rr = ontr.getRDFType();
                if (rr != null) {
                    ss = rr.toString();
                    if (ss.contains("Datatype")) //TODO: 处理Datatype
                        return 4;
                }
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
				ArrayList lt = getAnonResourceWithType(s);
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

	public boolean isMetaOntData(Resource r) {
		String s = new OWLOntParse().getResourceBaseURI(r.toString());
		return (ontLngURI.contains(s));
	}

	/***************************************************************************
	 * 给定子图中的类层次文本描述
	 **************************************************************************/
	public ArrayList cnptHrcTextDes(OntClass c, DirectedGraph g) {
		ArrayList result = new ArrayList();

		/* subClass文本描述 */
		TextDes subDes = new TextDes();
		subDes = cnptSubClassDes(c, g);
		result.add(0, subDes);

		/* superClass文本描述 */
		TextDes superDes = new TextDes();
		superDes = cnptSuperClassDes(c, g);
		result.add(1, superDes);

		/* sibling Class文本描述 */
		TextDes siblingDes = new TextDes();
		siblingDes = cnptSiblingClassDes(c, g);
		result.add(2, siblingDes);

		/* disjoint Class文本描述 */
		TextDes disjointDes = new TextDes();
		disjointDes = cnptDisjointClassDes(c, g);
		result.add(3, disjointDes);

		/* complementOf Class文本描述 */
		TextDes compDes = new TextDes();
		compDes = cnptComplementClassDes(c, g);
		result.add(4, compDes);

		return result;
	}

	public int getSubGraph(OntClass c) {
		int pos = 0;
		for (int i = 0; i < conceptNum; i++) {
			if (c==null) continue;
			if (cnptSubG[i].conceptName.equals(c.getLocalName())) {
				pos = i;
				break;
			}
		}
		return pos;
	}

	public int getSubGraph(OntProperty p) {
		int pos = 0;
		for (int i = 0; i < propNum; i++) {
			if (propSubG[i].propName.equals(p.getLocalName())) {
				pos = i;
				break;
			}
		}
		return pos;
	}

	/***************************************************************************
	 * subClass文本描述
	 **************************************************************************/
	public TextDes cnptSubClassDes(OntClass c, DirectedGraph g) {
		OWLOntParse parse = new OWLOntParse();
		TextDes subDes = new TextDes();
		subDes.name = c.toString();
		subDes.text = new ArrayList();
		/* 得到全部subClass */
		Set set = new HashSet();
		set.add(c);
		ArrayList subCList = parse.listSubClassOfConceptWithDistance(c, 0, set);
		for (Iterator it = subCList.iterator(); it.hasNext();) {
			ArrayList lt = (ArrayList) it.next();
			OntClass subC = (OntClass) lt.get(0);
			int level = ((Integer) lt.get(1)).intValue();

			/*
			 * 判断当前的subClass是否包含在子图中 严格的判断应该考虑完整的三元组，这里只简单考虑节点是否被包含
			 */
			if (!g.containsVertex(parse.getResourceLocalName(subC))) {
				continue;
			}

			/* 添加该subClass的Description */
			/* 先判断是否是匿名概念 */
			if (!subC.isAnon()) {// 非匿名
				int pos = findCnptPosInFullName(subC.toString());// 位置
				ArrayList lb = cnptBasicTextDes[pos].text;
				for (Iterator itx = lb.iterator(); itx.hasNext();) {
					Word w = (Word) ((Word) itx.next()).clone();
					w.weight *= (1 - subClassDec * level);
					subDes.text.add(w);
				}
			} else {// 匿名
				/* 添加子图中对该匿名节点的描述 */
				Statement sx = ontParse
						.getAStatement(
								m,
								subC,
								m
										.getProperty("http://www.w3.org/2000/01/rdf-schema#subClassOf"),
								c);
				Set setx = new HashSet();
				setx.add(sx);
				ArrayList anonList = anonDesInSubGraphBiDirect(subC, 0, g, setx);
				// ArrayList anonList=anonDesInSubGraphSingleDirect(subC,0,g);
				// ArrayList anonList=this.anonDesInModel(c,0);
				for (Iterator itx = anonList.iterator(); itx.hasNext();) {
					Word w = (Word) ((Word) itx.next()).clone();
					w.weight *= (1 - subClassDec * level);
					subDes.text.add(w);
				}
			}
		}

		/* 后置处理 */
		subDes.text = desPostProcess(subDes.text);
		return subDes;
	}

	/***************************************************************************
	 * superClass文本描述
	 **************************************************************************/
	public TextDes cnptSuperClassDes(OntClass c, DirectedGraph g) {
		OWLOntParse parse = new OWLOntParse();
		TextDes des = new TextDes();
		des.name = c.toString();
		des.text = new ArrayList();
		/* 得到全部superClass */
		Set set = new HashSet();
		set.add(c);
		ArrayList subCList = parse.listSuperClassOfConceptWithDistance(c, 0,
				set);
		for (Iterator it = subCList.iterator(); it.hasNext();) {
			ArrayList lt = (ArrayList) it.next();
			OntClass superC = (OntClass) lt.get(0);
			int level = ((Integer) lt.get(1)).intValue();

			/*
			 * 判断当前的superClass是否包含在子图中 严格的判断应该考虑完整的三元组，这里只简单考虑节点是否被包含
			 */
			if (!g.containsVertex(parse.getResourceLocalName(superC))) {
				continue;
			}

			/* 添加该superClass的Description */
			/* 先判断是否是匿名概念 */
			if (!superC.isAnon()) {// 非匿名
				int pos = findCnptPosInFullName(superC.toString());// 位置
				if (pos >= 0) {
					ArrayList lb = cnptBasicTextDes[pos].text;
					for (Iterator itx = lb.iterator(); itx.hasNext();) {
						Word w = (Word) ((Word) itx.next()).clone();
						w.weight *= (1 - superClassDec * level);
						des.text.add(w);
					}
				}
			} else {// 匿名
				/* 添加子图中对该匿名节点的描述 */
				Statement sx = ontParse
						.getAStatement(
								m,
								c,
								m
										.getProperty("http://www.w3.org/2000/01/rdf-schema#subClassOf"),
								superC);
				Set setx = new HashSet();
				setx.add(sx);
				ArrayList anonList = anonDesInSubGraphBiDirect(superC, 0, g,
						setx);
				// ArrayList anonList=anonDesInSubGraphSingleDirect(superC,0,g);
				// ArrayList anonList=anonDesInModel(c,0);
				for (Iterator itx = anonList.iterator(); itx.hasNext();) {
					Word w = (Word) ((Word) itx.next()).clone();
					w.weight *= (1 - superClassDec * level);
					des.text.add(w);
				}
			}
		}
		/* 后置处理 */
		des.text = desPostProcess(des.text);
		return des;
	}

	/***************************************************************************
	 * sibling Class文本描述
	 **************************************************************************/
	public TextDes cnptSiblingClassDes(OntClass c, DirectedGraph g) {
		OWLOntParse parse = new OWLOntParse();
		TextDes des = new TextDes();
		des.name = c.toString();
		des.text = new ArrayList();
		/* 得到全部sibling Class */
		ArrayList subCList = parse.listSiblingsOfConcept(c);
		for (Iterator it = subCList.iterator(); it.hasNext();) {
			OntClass sibC = (OntClass) it.next();

			/*
			 * 判断当前的siblingClass是否包含在子图中 严格的判断应该考虑完整的三元组，这里只简单考虑节点是否被包含
			 */
			if (!g.containsVertex(parse.getResourceLocalName(sibC))) {
				continue;
			}

			/* 添加该siblingClass的Description */
			/* 先判断是否是匿名概念 */
			if (!sibC.isAnon()) {// 非匿名
				int pos = findCnptPosInFullName(sibC.toString());// 位置
				ArrayList lb = cnptBasicTextDes[pos].text;
				for (Iterator itx = lb.iterator(); itx.hasNext();) {
					Word w = (Word) ((Word) itx.next()).clone();
					w.weight *= wsiblingClassDec;
					des.text.add(w);
				}
			} else {// 匿名
				/* 添加子图中对该匿名节点的描述 */
				Set setx = new HashSet();
				ArrayList anonList = anonDesInSubGraphBiDirect(sibC, 0, g, setx);
				// ArrayList anonList=anonDesInSubGraphSingleDirect(sibC,0,g);
				// ArrayList anonList=anonDesInModel(c,0);
				for (Iterator itx = anonList.iterator(); itx.hasNext();) {
					Word w = (Word) ((Word) itx.next()).clone();
					w.weight *= wsiblingClassDec;
					des.text.add(w);
				}
			}
		}

		/* 后置处理 */
		des.text = desPostProcess(des.text);

		return des;
	}

	/***************************************************************************
	 * disjoint Class文本描述
	 **************************************************************************/
	public TextDes cnptDisjointClassDes(OntClass c, DirectedGraph g) {
		OWLOntParse parse = new OWLOntParse();
		TextDes des = new TextDes();
		des.name = c.toString();
		des.text = new ArrayList();
		/* 得到全部disjoint Class */
		ArrayList disCList = parse.listDisjointClassOfConcept(c);
		for (Iterator it = disCList.iterator(); it.hasNext();) {
			OntClass disjC = (OntClass) it.next();

			/*
			 * 判断当前的disjointClass是否包含在子图中 严格的判断应该考虑完整的三元组，这里只简单考虑节点是否被包含
			 */
			if (!g.containsVertex(parse.getResourceLocalName(disjC))) {
				continue;
			}

			/* 添加该disjointClass的Description */
			/* 先判断是否是匿名概念 */
			if (!disjC.isAnon()) {// 非匿名
				int pos = findCnptPosInFullName(disjC.toString());// 位置
				ArrayList lb = cnptBasicTextDes[pos].text;
				for (Iterator itx = lb.iterator(); itx.hasNext();) {
					Word w = (Word) ((Word) itx.next()).clone();
					w.weight *= wDisjClassDec;
					des.text.add(w);
				}
			} else {// 匿名
				/* 添加子图中对该匿名节点的描述 */
				Set setx = new HashSet();
				ArrayList anonList = anonDesInSubGraphBiDirect(disjC, 0, g,
						setx);
				// ArrayList anonList=anonDesInSubGraphSingleDirect(disjC,0,g);
				// ArrayList anonList=anonDesInModel(c,0);
				for (Iterator itx = anonList.iterator(); itx.hasNext();) {
					Word w = (Word) ((Word) itx.next()).clone();
					w.weight *= wDisjClassDec;
					des.text.add(w);
				}
			}
		}

		/* 后置处理 */
		des.text = desPostProcess(des.text);
		return des;
	}

	/***************************************************************************
	 * complementOf Class文本描述
	 **************************************************************************/
	public TextDes cnptComplementClassDes(OntClass c, DirectedGraph g) {
		OWLOntParse parse = new OWLOntParse();
		TextDes des = new TextDes();
		des.name = c.toString();
		des.text = new ArrayList();
		/* 得到全部complementOf Class */
		ArrayList cmpCList = parse.listComplementClassOfConcept(c);
		for (Iterator it = cmpCList.iterator(); it.hasNext();) {
			OntClass cmplC = (OntClass) it.next();

			/*
			 * 判断当前的complementOf Class是否包含在子图中 严格的判断应该考虑完整的三元组，这里只简单考虑节点是否被包含
			 */
			if (!g.containsVertex(parse.getResourceLocalName(cmplC))) {
				continue;
			}

			/* 添加该complementOf Class的Description */
			/* 先判断是否是匿名概念 */
			if (!cmplC.isAnon()) {// 非匿名
				int pos = findCnptPosInFullName(cmplC.toString());// 位置
				ArrayList lb = cnptBasicTextDes[pos].text;
				for (Iterator itx = lb.iterator(); itx.hasNext();) {
					Word w = (Word) ((Word) itx.next()).clone();
					w.weight *= wCmplClassDec;
					des.text.add(w);
				}
			} else {// 匿名
				/* 添加子图中对该匿名节点的描述 */
				Set setx = new HashSet();
				ArrayList anonList = anonDesInSubGraphBiDirect(cmplC, 0, g,
						setx);
				// ArrayList anonList=anonDesInSubGraphSingleDirect(cmplC,0,g);
				// ArrayList anonList=anonDesInModel(c,0);
				for (Iterator itx = anonList.iterator(); itx.hasNext();) {
					Word w = (Word) ((Word) itx.next()).clone();
					w.weight *= wCmplClassDec;
					des.text.add(w);
				}
			}
		}

		/* 后置处理 */
		des.text = desPostProcess(des.text);
		return des;
	}

	/***************************************************************************
	 * 给定子图中的类的相关属性的文本描述
	 **************************************************************************/
	public ArrayList cnptPropTextDes(OntClass c, DirectedGraph g) {
		ArrayList result = new ArrayList();

		/* Class的Domain文本描述 */
		TextDes domainDes = new TextDes();
		domainDes = cnptDomainDes(c, g);
		result.add(0, domainDes);

		/* Class的Range文本描述 */
		TextDes RangeDes = new TextDes();
		RangeDes = cnptRangeDes(c, g);
		result.add(1, RangeDes);

		return result;
	}

	/***************************************************************************
	 * Class的Domain文本描述
	 **************************************************************************/
	public TextDes cnptDomainDes(OntClass c, DirectedGraph g) {
		OWLOntParse parse = new OWLOntParse();
		TextDes des = new TextDes();
		des.name = c.toString();
		des.text = new ArrayList();

		/* 得到把c作为Domain的Property */
		ArrayList pList = new ArrayList();
		for (Iterator it = curStmList.iterator(); it.hasNext();) {
			Statement st = (Statement) it.next();
			Resource r = st.getSubject();
			Property p = st.getPredicate();
			RDFNode o = st.getObject();
			if (p.toString().equals(
					"http://www.w3.org/2000/01/rdf-schema#domain")
					&& o.toString().equals(c.toString())) {
				OntProperty pt = m.getOntProperty(r.toString());
				if (pt != null) {
					pList.add(pt);
				}
			}
		}

		/* 确定每个property与c的远近程度 */
		/*
		 * 方法：找到c的所有父类，父类中含有这个Domain并且 距离最远的就是该Domain的原始声明处
		 */
		/* c的父类 */
		Set set = new HashSet();
		set.add(c);
		ArrayList fatherList = parse.listSuperClassOfConceptWithDistance(c, 0,
				set);
		for (Iterator jt = pList.iterator(); jt.hasNext();) {
			/* 给定p--Domain--c，确定原始声明概念和c的距离 */
			OntProperty pt = (OntProperty) jt.next();
			OntClass maxC = null;
			int maxLevel = 0;
			for (Iterator kt = fatherList.iterator(); kt.hasNext();) {
				ArrayList lt = (ArrayList) kt.next();
				OntClass superC = (OntClass) lt.get(0);
				int level = ((Integer) lt.get(1)).intValue();
				if (parse.listDomainPropertyOfConcept(superC, true)
						.contains(pt)) {
					if (level >= maxLevel) {
						maxLevel = level;
						maxC = superC;
					}
				}
			}

			/* 加入p的Description */
			if (!pt.isAnon()) {// 非匿名
				int pos = findPropPosInFullName(pt.toString());// 位置
				ArrayList lb = propBasicTextDes[pos].text;
				for (Iterator itx = lb.iterator(); itx.hasNext();) {
					Word w = (Word) ((Word) itx.next()).clone();
					w.weight *= (1.0 / (1.0 + maxLevel));
					des.text.add(w);
				}
			} else {// 匿名
				/* 添加子图中对该匿名节点的描述 */
				Statement sx = ontParse
						.getAStatement(
								m,
								pt,
								m
										.getProperty("http://www.w3.org/2000/01/rdf-schema#domain"),
								c);
				Set setx = new HashSet();
				setx.add(sx);
				ArrayList anonList = anonDesInSubGraphBiDirect(pt, 0, g, setx);
				// ArrayList anonList=anonDesInSubGraphSingleDirect(pt,0,g);
				// ArrayList anonList=anonDesInModel(pt,0);
				for (Iterator itx = anonList.iterator(); itx.hasNext();) {
					Word w = (Word) ((Word) itx.next()).clone();
					w.weight *= (1.0 / (1.0 + maxLevel));
					des.text.add(w);
				}
			}
		}

		/* 后置处理 */
		des.text = desPostProcess(des.text);
		return des;
	}

	/***************************************************************************
	 * Class的Range文本描述
	 **************************************************************************/
	public TextDes cnptRangeDes(OntClass c, DirectedGraph g) {
		OWLOntParse parse = new OWLOntParse();
		TextDes des = new TextDes();
		des.name = c.toString();
		des.text = new ArrayList();

		/* 得到把c作为Range的Property */
		ArrayList pList = new ArrayList();
		for (Iterator it = curStmList.iterator(); it.hasNext();) {
			Statement st = (Statement) it.next();
			Resource r = st.getSubject();
			Property p = st.getPredicate();
			RDFNode o = st.getObject();
			if (p.toString().equals(
					"http://www.w3.org/2000/01/rdf-schema#range")
					&& o.toString().equals(c.toString())) {
				OntProperty pt = m.getOntProperty(r.toString());
				if (pt != null) {
					pList.add(pt);
				}
			}
		}

		/* 确定每个property与c的远近程度 */
		/*
		 * 方法：找到c的所有父类，父类中含有这个Range并且 距离最远的就是该Range的原始声明处
		 */
		/* c的父类 */
		Set set = new HashSet();
		set.add(c);
		ArrayList fatherList = parse.listSuperClassOfConceptWithDistance(c, 0,
				set);
		for (Iterator jt = pList.iterator(); jt.hasNext();) {
			/* 给定p--Range--c，确定原始声明概念和c的距离 */
			OntProperty pt = (OntProperty) jt.next();
			OntClass maxC = null;
			int maxLevel = 0;
			for (Iterator kt = fatherList.iterator(); kt.hasNext();) {
				ArrayList lt = (ArrayList) kt.next();
				OntClass superC = (OntClass) lt.get(0);
				int level = ((Integer) lt.get(1)).intValue();
				if (parse.listRangePropertyOfConcept(superC).contains(pt)) {
					if (level >= maxLevel) {
						maxLevel = level;
						maxC = superC;
					}
				}
			}

			/* 加入p的Description */
			if (!pt.isAnon()) {// 非匿名
				int pos = findPropPosInFullName(pt.toString());// 位置
				ArrayList lb = propBasicTextDes[pos].text;
				for (Iterator itx = lb.iterator(); itx.hasNext();) {
					Word w = (Word) ((Word) itx.next()).clone();
					w.weight *= (1.0 / (1.0 + maxLevel));
					des.text.add(w);
				}
			} else {// 匿名
				/* 添加子图中对该匿名节点的描述 */
				Statement sx = ontParse.getAStatement(m, pt,
						    m.getProperty("http://www.w3.org/2000/01/rdf-schema#range"),c);
				Set setx = new HashSet();
				setx.add(sx);
				ArrayList anonList = anonDesInSubGraphBiDirect(pt, 0, g, setx);
				// ArrayList anonList=anonDesInSubGraphSingleDirect(pt,0,g);
				// ArrayList anonList=anonDesInModel(pt,0);
				for (Iterator itx = anonList.iterator(); itx.hasNext();) {
					Word w = (Word) ((Word) itx.next()).clone();
					w.weight *= (1.0 / (1.0 + maxLevel));
					des.text.add(w);
				}
			}
		}

		/* 后置处理 */
		des.text = desPostProcess(des.text);
		return des;
	}

	/***************************************************************************
	 * 给定子图中的类的相关实例的文本描述
	 **************************************************************************/
	public ArrayList cnptInsTexDes(OntClass c, DirectedGraph g) {
		ArrayList result = new ArrayList();

		TextDes des = new TextDes();
		des.name = c.toString();
		des.text = new ArrayList();

		/* 找到子图中的实例 */
		for (Iterator it = curStmList.iterator(); it.hasNext();) {
			Statement st = (Statement) it.next();
			Resource r = st.getSubject();
			Property p = st.getPredicate();
			RDFNode o = st.getObject();
			if (p.toString().equals(
					"http://www.w3.org/1999/02/22-rdf-syntax-ns#type")
					&& o.toString().equals(c.toString())) {
				Individual nt = null;
				nt = m.getIndividual(r.toString());
				if (nt == null) {
					nt = this.findInsInFullName(r.toString());
				}

				if (nt != null) {
					/* 加入i的Description */
					if (!nt.isAnon()) {// 非匿名
						int pos = findInsPosInFullName(nt.toString());// 位置
						/* 实例的基本描述 */
						ArrayList lb = insBasicTextDes[pos].text;
						for (Iterator itx = lb.iterator(); itx.hasNext();) {
							Word w = (Word) ((Word) itx.next()).clone();
							des.text.add(w);
						}
						/* 实例在子图中的其它描述 */
						Set setx = new HashSet();
						setx.add(st);
						ArrayList insList = insDesInSubGraphBiDirect(nt, 0, g,
								setx);
						for (Iterator itx = insList.iterator(); itx.hasNext();) {
							Word w = (Word) ((Word) itx.next()).clone();
							des.text.add(w);
						}
					} else {// 匿名
						/* 添加子图中对该匿名节点的描述 */
						Set setx = new HashSet();
						setx.add(st);
						ArrayList anonList = anonDesInSubGraphBiDirect(nt, 0,
								g, setx);
						// ArrayList
						// anonList=anonDesInSubGraphSingleDirect(nt,0,g);
						// ArrayList anonList=anonDesInModel(nt,0);
						for (Iterator itx = anonList.iterator(); itx.hasNext();) {
							Word w = (Word) ((Word) itx.next()).clone();
							des.text.add(w);
						}
					}
				}
			}
		}

		/* 后置处理 */
		des.text = desPostProcess(des.text);

		result.add(des);
		return result;
	}

	/***************************************************************************
	 * 给定子图中的类的其它元素的文本描述
	 **************************************************************************/
	public ArrayList cnptOtherTexDes(OntClass c, DirectedGraph g) {
		ArrayList result = new ArrayList();
		ArrayList checkedSet = new ArrayList();// 元素是否被处理过标志
		SplitWords spWord = new SplitWords();
		DelStopWords delSWrod = new DelStopWords();
		delSWrod.loadStopWords();

		/* 遍历子图中的三元组 */
		for (Iterator it = curStmList.iterator(); it.hasNext();) {
			Statement st = (Statement) it.next();
			String localURI = null;
			int elmType = -1;
			Resource r = st.getSubject();
			Property p = st.getPredicate();
			RDFNode o = st.getObject();

			/* 处理s */
			if (r.isURIResource()) {
				localURI = r.getNameSpace();
			}
			if (!ontLngURI.contains(localURI) && !baseURI.equals(localURI)
					&& !checkedSet.contains(r.toString())) {// 不是元语，基本元素，没处理过
				checkedSet.add(r.toString());
				TextDes des = new TextDes();
				des.name = r.toString();
				des.text = new ArrayList();
				if (!r.isAnon()) {// 不是匿名资源
					// 判断类型
					elmType = getResourceType(r.toString());
					des.type = elmType;
					if (elmType == 1) {
						// 查Class表
						int basicDespos = findCnptPosInFullName(r.toString());
						des.text = cnptBasicTextDes[basicDespos].text;
					} else if (elmType == 2) {
						// 查Property表
						int basicDespos = findPropPosInFullName(r.toString());
						des.text = propBasicTextDes[basicDespos].text;
					} else if (elmType == 3) {
						// 查Individual表
						int basicDespos = findInsPosInFullName(r.toString());
						des.text = insBasicTextDes[basicDespos].text;
					} else if (elmType == 4) {
						// 处理其它的value资源
						// 暂时不考虑其它的资源，如果需要，则计算它的基本描述
					}
				} else {
					// 处理匿名资源
					Set setx = new HashSet();
					// setx.add(st);
					// ArrayList anonList=anonDesInSubGraphBiDirect(r,0,g,setx);
					ArrayList anonList = anonDesInSubGraphSingleDirect(r, 0, g);
					for (Iterator itx = anonList.iterator(); itx.hasNext();) {
						Word w = (Word) ((Word) itx.next()).clone();
						des.text.add(w);
					}
				}
				/* 后置处理 */
				des.text = desPostProcess(des.text);
				if (!des.text.isEmpty()) {
					result.add(des);
				}
			}

			/* 处理p */
			localURI = null;
			if (p.isURIResource()) {
				localURI = p.getNameSpace();
			}
			if (!ontLngURI.contains(localURI) && !baseURI.equals(localURI)
					&& !checkedSet.contains(p.toString())) {// 不是元语，基本元素，没处理过
				checkedSet.add(p.toString());
				TextDes des = new TextDes();
				des.name = p.toString();
				des.text = new ArrayList();
				if (!p.isAnon()) {// 不是匿名资源
					// 判断类型
					elmType = getResourceType(p.toString());
					des.type = elmType;
					if (elmType == 1) {
						// 查Class表
						int basicDespos = findCnptPosInFullName(p.toString());
						if (basicDespos >= 0) {
							des.text = cnptBasicTextDes[basicDespos].text;
						}
					} else if (elmType == 2) {
						// 查Property表
						int basicDespos = findPropPosInFullName(p.toString());
						if (basicDespos >= 0) {
							des.text = propBasicTextDes[basicDespos].text;
						}
					} else if (elmType == 3) {
						// 查Individual表
						int basicDespos = findInsPosInFullName(p.toString());
						if (basicDespos >= 0) {
							des.text = insBasicTextDes[basicDespos].text;
						}
					} else if (elmType == 4) {
						// 处理其它的value资源
						// 暂时不考虑其它的资源，如果需要，则计算它的基本描述
					}
				} else {
					// 处理匿名资源
					Set setx = new HashSet();
					// setx.add(st);
					// ArrayList anonList=anonDesInSubGraphBiDirect(p,0,g,setx);
					ArrayList anonList = anonDesInSubGraphSingleDirect(p, 0, g);
					for (Iterator itx = anonList.iterator(); itx.hasNext();) {
						Word w = (Word) ((Word) itx.next()).clone();
						des.text.add(w);
					}
				}
				/* 后置处理 */
				des.text = desPostProcess(des.text);
				if (!des.text.isEmpty()) {
					result.add(des);
				}
			}

			/* 处理o */
			localURI = null;
			if (o.isURIResource()) {
				localURI = o.asNode().getNameSpace();
			}
			if (!ontLngURI.contains(localURI) && !baseURI.equals(localURI)
					&& !checkedSet.contains(o.toString())) {// 不是元语，基本元素，没处理过
				checkedSet.add(o.toString());
				TextDes des = new TextDes();
				des.name = o.toString();
				des.text = new ArrayList();
				if (!o.isAnon()) {// 不是匿名资源
					// 判断类型
					elmType = getResourceType(o.toString());
					des.type = elmType;
					if (elmType == 1) {
						// 查Class表
						int basicDespos = findCnptPosInFullName(o.toString());
						if (basicDespos >= 0) {
							des.text = cnptBasicTextDes[basicDespos].text;
						}

					} else if (elmType == 2) {
						// 查Property表
						int basicDespos = findPropPosInFullName(o.toString());
						if (basicDespos >= 0) {
							des.text = propBasicTextDes[basicDespos].text;
						}
					} else if (elmType == 3) {
						// 查Individual表
						int basicDespos = findInsPosInFullName(o.toString());
						if (basicDespos >= 0) {
							des.text = insBasicTextDes[basicDespos].text;
						}
					} else if (elmType == 4) {
						String str = null;
						if (o.isLiteral()) {
							str = o.asNode().getLiteralLexicalForm();
						} else {
							str = o.toString();
						}
						str = delSWrod.removeStopWords(str);
						ArrayList list = spWord.split(str);
						list = delSWrod.removeStopWords(list);
						/* 添加权重 */
						for (Iterator itx = list.iterator(); itx.hasNext();) {
							String stemp = (String) itx.next();
							Word w = new Word();
							w.content = stemp;
							w.weight = 1.0;
							des.text.add(w);
						}
					}
				} else {
					// 处理匿名资源
					Set setx = new HashSet();
					// setx.add(st);
					// ArrayList
					// anonList=anonDesInSubGraphBiDirect(m.getResource(o.toString()),0,g,setx);
					ArrayList anonList = anonDesInSubGraphSingleDirect(m
							.getResource(o.toString()), 0, g);
					for (Iterator itx = anonList.iterator(); itx.hasNext();) {
						Word w = (Word) ((Word) itx.next()).clone();
						des.text.add(w);
					}
				}
				/* 后置处理 */
				des.text = desPostProcess(des.text);
				if (!des.text.isEmpty()) {
					result.add(des);
				}
			}
		}
		return result;
	}

	/***************************************************************************
	 * 给定子图中的类的其它元素的文本描述
	 **************************************************************************/
	public ArrayList propOtherTexDes(OntProperty pr, DirectedGraph g) {
		ArrayList result = new ArrayList();
		ArrayList checkedSet = new ArrayList();// 元素是否被处理过标志
		SplitWords spWord = new SplitWords();
		DelStopWords delSWrod = new DelStopWords();
		delSWrod.loadStopWords();

		/* 遍历子图中的三元组 */
		for (Iterator it = curStmList.iterator(); it.hasNext();) {
			Statement st = (Statement) it.next();
			String localURI = null;
			int elmType = -1;
			Resource r = st.getSubject();
			Property p = st.getPredicate();
			RDFNode o = st.getObject();

			/* 处理s */
			if (r.isURIResource()) {
				localURI = r.getNameSpace();
			}
			if (!ontLngURI.contains(localURI) && !baseURI.equals(localURI)
					&& !checkedSet.contains(r.toString())) {// 不是元语，基本元素，没处理过
				checkedSet.add(r.toString());
				TextDes des = new TextDes();
				des.name = r.toString();
				des.text = new ArrayList();
				if (!r.isAnon()) {// 不是匿名资源
					// 判断类型
					elmType = getResourceType(r.toString());
					des.type = elmType;
					if (elmType == 1) {
						// 查Class表
						int basicDespos = findCnptPosInFullName(r.toString());
						des.text = cnptBasicTextDes[basicDespos].text;
					} else if (elmType == 2) {
						// 查Property表
						int basicDespos = findPropPosInFullName(r.toString());
						des.text = propBasicTextDes[basicDespos].text;
					} else if (elmType == 3) {
						// 查Individual表
						int basicDespos = findInsPosInFullName(r.toString());
						des.text = insBasicTextDes[basicDespos].text;
					} else if (elmType == 4) {
						// 处理其它的value资源
						// 暂时不考虑其它的资源，如果需要，则计算它的基本描述
					}
				} else {
					// 处理匿名资源
					Set setx = new HashSet();
					// setx.add(st);
					// ArrayList anonList=anonDesInSubGraphBiDirect(r,0,g,setx);
					ArrayList anonList = anonDesInSubGraphSingleDirect(r, 0, g);
					for (Iterator itx = anonList.iterator(); itx.hasNext();) {
						Word w = (Word) ((Word) itx.next()).clone();
						des.text.add(w);
					}
				}
				/* 后置处理 */
				des.text = desPostProcess(des.text);
				if (!des.text.isEmpty()) {
					result.add(des);
				}
			}

			/* 处理p */
			localURI = null;
			if (p.isURIResource()) {
				localURI = p.getNameSpace();
			}
			if (!ontLngURI.contains(localURI) && !baseURI.equals(localURI)
					&& !checkedSet.contains(p.toString())) {// 不是元语，基本元素，没处理过
				checkedSet.add(p.toString());
				TextDes des = new TextDes();
				des.name = p.toString();
				des.text = new ArrayList();
				if (!p.isAnon()) {// 不是匿名资源
					// 判断类型
					elmType = getResourceType(p.toString());
					des.type = elmType;
					if (elmType == 1) {
						// 查Class表
						int basicDespos = findCnptPosInFullName(p.toString());
						if (basicDespos >= 0) {
							des.text = cnptBasicTextDes[basicDespos].text;
						}
					} else if (elmType == 2) {
						// 查Property表
						int basicDespos = findPropPosInFullName(p.toString());
						if (basicDespos >= 0) {
							des.text = propBasicTextDes[basicDespos].text;
						}
					} else if (elmType == 3) {
						// 查Individual表
						int basicDespos = findInsPosInFullName(p.toString());
						if (basicDespos >= 0) {
							des.text = insBasicTextDes[basicDespos].text;
						}
					} else if (elmType == 4) {
						// 处理其它的value资源
						// 暂时不考虑其它的资源，如果需要，则计算它的基本描述
					}
				} else {
					// 处理匿名资源
					Set setx = new HashSet();
					// setx.add(st);
					// ArrayList anonList=anonDesInSubGraphBiDirect(p,0,g,setx);
					ArrayList anonList = anonDesInSubGraphSingleDirect(p, 0, g);
					for (Iterator itx = anonList.iterator(); itx.hasNext();) {
						Word w = (Word) ((Word) itx.next()).clone();
						des.text.add(w);
					}
				}
				/* 后置处理 */
				des.text = desPostProcess(des.text);
				if (!des.text.isEmpty()) {
					result.add(des);
				}
			}

			/* 处理o */
			localURI = null;
			if (o.isURIResource()) {
				localURI = o.asNode().getNameSpace();
			}
			if (!ontLngURI.contains(localURI) && !baseURI.equals(localURI)
					&& !checkedSet.contains(o.toString())) {// 不是元语，基本元素，没处理过
				checkedSet.add(o.toString());
				TextDes des = new TextDes();
				des.name = o.toString();
				des.text = new ArrayList();
				if (!o.isAnon()) {// 不是匿名资源
					// 判断类型
					elmType = getResourceType(o.toString());
					des.type = elmType;
					if (elmType == 1) {
						// 查Class表
						int basicDespos = findCnptPosInFullName(o.toString());
						if (basicDespos >= 0) {
							des.text = cnptBasicTextDes[basicDespos].text;
						}
					} else if (elmType == 2) {
						// 查Property表
						int basicDespos = findPropPosInFullName(o.toString());
						if (basicDespos >= 0) {
							des.text = propBasicTextDes[basicDespos].text;
						}
					} else if (elmType == 3) {
						// 查Individual表
						int basicDespos = findInsPosInFullName(o.toString());
						if (basicDespos >= 0) {
							des.text = insBasicTextDes[basicDespos].text;
						}
					} else if (elmType == 4) {
						String str = null;
						if (o.isLiteral()) {
							str = o.asNode().getLiteralLexicalForm();
						} else {
							str = o.toString();
						}
						str = delSWrod.removeStopWords(str);
						ArrayList list = spWord.split(str);
						list = delSWrod.removeStopWords(list);
						/* 添加权重 */
						for (Iterator itx = list.iterator(); itx.hasNext();) {
							String stemp = (String) itx.next();
							Word w = new Word();
							w.content = stemp;
							w.weight = 1.0;
							des.text.add(w);
						}
					}
				} else {
					// 处理匿名资源
					Set setx = new HashSet();
					// setx.add(st);
					// ArrayList
					// anonList=anonDesInSubGraphBiDirect(m.getResource(o.toString()),0,g,setx);
					ArrayList anonList = anonDesInSubGraphSingleDirect(m
							.getResource(o.toString()), 0, g);
					for (Iterator itx = anonList.iterator(); itx.hasNext();) {
						Word w = (Word) ((Word) itx.next()).clone();
						des.text.add(w);
					}
				}
				/* 后置处理 */
				des.text = desPostProcess(des.text);
				if (!des.text.isEmpty()) {
					result.add(des);
				}
			}
		}
		return result;
	}

	/***************************************************************************
	 * 给定子图中的属性层次文本描述
	 **************************************************************************/
	public ArrayList propHrcTextDes(OntProperty p, DirectedGraph g) {
		ArrayList result = new ArrayList();

		/* subProperty文本描述 */
		TextDes subDes = new TextDes();
		subDes = propSubPropertyDes(p, g);
		result.add(0, subDes);

		/* superProperty文本描述 */
		TextDes superDes = new TextDes();
		superDes = propSuperPropertyDes(p, g);
		result.add(1, superDes);

		/* sibling property文本描述 */
		TextDes siblingDes = new TextDes();
		siblingDes = propSiblingPropertyDes(p, g);
		result.add(2, siblingDes);

		return result;
	}

	/***************************************************************************
	 * subProperty文本描述
	 **************************************************************************/
	public TextDes propSubPropertyDes(OntProperty p, DirectedGraph g) {
		OWLOntParse parse = new OWLOntParse();
		TextDes subDes = new TextDes();
		subDes.name = p.toString();
		subDes.text = new ArrayList();

		/* 得到全部subProperty */
		ArrayList subPList = parse.listSubPropertyOfPropertyWithDistance(p, 0);
		for (Iterator it = subPList.iterator(); it.hasNext();) {
			ArrayList lt = (ArrayList) it.next();
			OntProperty subP = (OntProperty) lt.get(0);
			int level = ((Integer) lt.get(1)).intValue();

			/*
			 * 判断当前的subProperty是否包含在子图中 严格的判断应该考虑完整的三元组，这里只简单考虑节点是否被包含
			 */
			if (!g.containsVertex(parse.getResourceLocalName(subP))) {
				continue;
			}

			/* 添加该subProperty的Description */
			/* 先判断是否是匿名概念 */
			if (!subP.isAnon()) {// 非匿名
				int pos = findPropPosInFullName(subP.toString());// 位置
				ArrayList lb = propBasicTextDes[pos].text;
				for (Iterator itx = lb.iterator(); itx.hasNext();) {
					Word w = (Word) ((Word) itx.next()).clone();
					w.weight *= (1 - subPropertyDec * level);
					subDes.text.add(w);
				}
			} else {// 匿名
				/* 添加子图中对该匿名节点的描述 */
				Statement sx = ontParse
						.getAStatement(
								m,
								subP,
								m
										.getProperty("http://www.w3.org/2000/01/rdf-schema#subPropertyOf"),
								p);
				Set setx = new HashSet();
				setx.add(sx);
				ArrayList anonList = anonDesInSubGraphBiDirect(subP, 0, g, setx);
				// ArrayList anonList=anonDesInSubGraphSingleDirect(subP,0,g);
				// ArrayList anonList=anonDesInModel(p,0);
				for (Iterator itx = anonList.iterator(); itx.hasNext();) {
					Word w = (Word) ((Word) itx.next()).clone();
					w.weight *= (1 - subPropertyDec * level);
					subDes.text.add(w);
				}
			}
		}

		/* 后置处理 */
		subDes.text = desPostProcess(subDes.text);

		return subDes;
	}

	/***************************************************************************
	 * superProperty文本描述
	 **************************************************************************/
	public TextDes propSuperPropertyDes(OntProperty p, DirectedGraph g) {
		OWLOntParse parse = new OWLOntParse();
		TextDes superDes = new TextDes();
		superDes.name = p.toString();
		superDes.text = new ArrayList();

		/* 得到全部superProperty */
		ArrayList superPList = parse.listSuperPropertyOfPropertyWithDistance(p,
				0);
		for (Iterator it = superPList.iterator(); it.hasNext();) {
			ArrayList lt = (ArrayList) it.next();
			OntProperty superP = (OntProperty) lt.get(0);
			int level = ((Integer) lt.get(1)).intValue();

			/*
			 * 判断当前的superProperty是否包含在子图中 严格的判断应该考虑完整的三元组，这里只简单考虑节点是否被包含
			 */
			if (!g.containsVertex(parse.getResourceLocalName(superP))) {
				continue;
			}

			/* 添加该superProperty的Description */
			/* 先判断是否是匿名概念 */
			if (!superP.isAnon()) {// 非匿名
				int pos = findPropPosInFullName(superP.toString());// 位置
				ArrayList lb = propBasicTextDes[pos].text;
				for (Iterator itx = lb.iterator(); itx.hasNext();) {
					Word w = (Word) ((Word) itx.next()).clone();
					w.weight *= (1 - superPropertyDec * level);
					superDes.text.add(w);
				}
			} else {// 匿名
				/* 添加子图中对该匿名节点的描述 */
				Statement sx = ontParse
						.getAStatement(
								m,
								p,
								m
										.getProperty("http://www.w3.org/2000/01/rdf-schema#subPropertyOf"),
								superP);
				Set setx = new HashSet();
				setx.add(sx);
				ArrayList anonList = anonDesInSubGraphBiDirect(superP, 0, g,
						setx);
				// ArrayList anonList=anonDesInSubGraphSingleDirect(superP,0,g);
				// ArrayList anonList=anonDesInModel(p,0);
				for (Iterator itx = anonList.iterator(); itx.hasNext();) {
					Word w = (Word) ((Word) itx.next()).clone();
					w.weight *= (1 - superPropertyDec * level);
					superDes.text.add(w);
				}
			}
		}

		/* 后置处理 */
		superDes.text = desPostProcess(superDes.text);

		return superDes;
	}

	/***************************************************************************
	 * sibling Property文本描述
	 **************************************************************************/
	public TextDes propSiblingPropertyDes(OntProperty p, DirectedGraph g) {
		OWLOntParse parse = new OWLOntParse();
		TextDes des = new TextDes();
		des.name = p.toString();
		des.text = new ArrayList();
		/* 得到全部sibling Property */
		ArrayList subCList = parse.listSiblingsOfProperty(p);
		for (Iterator it = subCList.iterator(); it.hasNext();) {
			OntProperty sibP = (OntProperty) it.next();

			/*
			 * 判断当前的siblingProperty是否包含在子图中 严格的判断应该考虑完整的三元组，这里只简单考虑节点是否被包含
			 */
			if (!g.containsVertex(parse.getResourceLocalName(sibP))) {
				continue;
			}

			/* 添加该siblingProperty的Description */
			/* 先判断是否是匿名概念 */
			if (!sibP.isAnon()) {// 非匿名
				int pos = findPropPosInFullName(sibP.toString());// 位置
				ArrayList lb = propBasicTextDes[pos].text;
				for (Iterator itx = lb.iterator(); itx.hasNext();) {
					Word w = (Word) ((Word) itx.next()).clone();
					w.weight *= wsiblingPropertyDec;
					des.text.add(w);
				}
			} else {// 匿名
				/* 添加子图中对该匿名节点的描述 */
				// ArrayList anonList=anonDesInSubGraph(p,0);
				ArrayList anonList = anonDesInModel(p, 0);
				for (Iterator itx = anonList.iterator(); itx.hasNext();) {
					Word w = (Word) ((Word) itx.next()).clone();
					w.weight *= wsiblingPropertyDec;
					des.text.add(w);
				}
			}
		}

		/* 后置处理 */
		des.text = desPostProcess(des.text);

		return des;
	}

	/***************************************************************************
	 * 给定子图中的类的相关属性的文本描述
	 **************************************************************************/
	public ArrayList propFunctionTextDes(OntProperty p, DirectedGraph g) {
		ArrayList result = new ArrayList();

		/* Property的Domain文本描述 */
		TextDes domainDes = new TextDes();
		domainDes = propDomainDes(p, g);
		result.add(domainDes);

		/* Property的Range文本描述 */
		TextDes rangeDes = new TextDes();
		rangeDes = propRangeDes(p, g);
		result.add(rangeDes);

		/* Property的性质文本描述 */
		TextDes CharDes = new TextDes();
		CharDes = propCharDes(p);
		result.add(CharDes);

		return result;
	}

	/***************************************************************************
	 * Property的Domain文本描述
	 **************************************************************************/
	public TextDes propDomainDes(OntProperty p, DirectedGraph g) {
		OWLOntParse parse = new OWLOntParse();
		TextDes des = new TextDes();
		des.name = p.toString();
		des.text = new ArrayList();

		/* 得到p的Domain */
		ArrayList dList = new ArrayList();
		for (Iterator it = curStmList.iterator(); it.hasNext();) {
			Statement st = (Statement) it.next();
			// System.out.println(st.toString());
			Resource s = st.getSubject();
			Property pr = st.getPredicate();
			RDFNode o = st.getObject();
			if (pr.toString().equals(
					"http://www.w3.org/2000/01/rdf-schema#domain")
					&& s.toString().equals(p.toString())) {
				Resource ot = m.getOntResource(m.getResource(o.toString()));
				String oBaseURI = ontParse.getResourceBaseURI(o.toString());
				if (ot != null && !dList.contains(ot)
						&& !ontParse.metaURISet.contains(oBaseURI)) {
					dList.add(ot);
				}
			}
		}

		for (Iterator jt = dList.iterator(); jt.hasNext();) {
			RDFNode obj = (RDFNode) jt.next();

			/* 判断o的成分 */
			int t = getResourceType(obj.toString());
			if (t == 1) {// Class
				OntClass ctemp = null;
				if (!ontParse.isBlankNode(obj.toString())) {
					ctemp = m.getOntClass(obj.toString());
				} else {
					ctemp = (OntClass) (getAnonResourceWithType(obj.toString())
							.get(0));
				}

				/* c的父类 */
				Set set = new HashSet();
				set.add(ctemp);
				ArrayList fatherList = parse
						.listSuperClassOfConceptWithDistance(ctemp, 0, set);
				int maxLevel = 0;
				for (Iterator kt = fatherList.iterator(); kt.hasNext();) {
					ArrayList lt = (ArrayList) kt.next();
					OntClass superC = (OntClass) lt.get(0);
					int level = ((Integer) lt.get(1)).intValue();
					if (parse.listDomainPropertyOfConcept(superC, true)
							.contains(p)) {
						if (level >= maxLevel) {
							maxLevel = level;
						}
					}
				}

				/* 加入c的Description */
				if (!ctemp.isAnon()) {// 非匿名
					int pos = findCnptPosInFullName(ctemp.toString());// 位置
					if (pos == -1) {
						System.out.println(ctemp.toString());
					}
					ArrayList lb = cnptBasicTextDes[pos].text;
					for (Iterator itx = lb.iterator(); itx.hasNext();) {
						Word w = (Word) ((Word) itx.next()).clone();
						w.weight *= (1.0 / (1.0 + maxLevel));
						des.text.add(w);
					}
				} else {// 匿名
					/* 添加子图中对该匿名节点的描述 */
					Statement sx = ontParse
							.getAStatement(
									m,
									p,
									m
											.getProperty("http://www.w3.org/2000/01/rdf-schema#domain"),
									ctemp);
					Set setx = new HashSet();
					setx.add(sx);
					ArrayList anonList = anonDesInSubGraphBiDirect(ctemp, 0, g,
							setx);
					// ArrayList
					// anonList=anonDesInSubGraphSingleDirect(ctemp,0,g);
					// ArrayList anonList=anonDesInModel(ctemp,0);
					for (Iterator itx = anonList.iterator(); itx.hasNext();) {
						Word w = (Word) ((Word) itx.next()).clone();
						w.weight *= (1.0 / (1.0 + maxLevel));
						des.text.add(w);
					}
				}
			} else if (t == 4) {// Other resource
				/* 直接提取local name */
				Word w = new Word();
				w.content = obj.asNode().getLocalName();
				w.weight = 1.0;
				des.text.add(w);
			}
		}

		/* 后置处理 */
		des.text = desPostProcess(des.text);

		return des;
	}

	/***************************************************************************
	 * Property的Range文本描述
	 **************************************************************************/
	public TextDes propRangeDes(OntProperty p, DirectedGraph g) {
		OWLOntParse parse = new OWLOntParse();
		TextDes des = new TextDes();
		des.name = p.toString();
		des.text = new ArrayList();

		/* 得到p的Range */
		ArrayList dList = new ArrayList();
		for (Iterator it = curStmList.iterator(); it.hasNext();) {
			Statement st = (Statement) it.next();
			Resource s = st.getSubject();
			Property pr = st.getPredicate();
			RDFNode o = st.getObject();
			if (pr.toString().equals(
					"http://www.w3.org/2000/01/rdf-schema#range")
					&& s.toString().equals(p.toString())) {
				Resource ot = m.getOntResource(m.getResource(o.toString()));
				String oBaseURI = ontParse.getResourceBaseURI(o.toString());
				if (ot != null && !dList.contains(ot)
						&& !ontParse.metaURISet.contains(oBaseURI)) {
					dList.add(ot);
				}
			}
		}

		for (Iterator jt = dList.iterator(); jt.hasNext();) {
			RDFNode obj = (RDFNode) jt.next();

			/* 判断o的成分 */
			int t = getResourceType(obj.toString());
			if (t == 1) {// Class
				OntClass ctemp = null;
				ctemp = m.getOntClass(obj.toString());
				if (ctemp == null) {
					try {
						ctemp = m.getOntResource(
								m.getResource(obj.asNode().toString()))
								.asClass();
					} catch (ConversionException e) {
						continue;
					}

				}
				/* c的父类 */
				Set set = new HashSet();
				set.add(ctemp);
				ArrayList fatherList = parse
						.listSuperClassOfConceptWithDistance(ctemp, 0, set);
				int maxLevel = 0;
				for (Iterator kt = fatherList.iterator(); kt.hasNext();) {
					ArrayList lt = (ArrayList) kt.next();
					OntClass superC = (OntClass) lt.get(0);
					int level = ((Integer) lt.get(1)).intValue();
					if (parse.listDomainPropertyOfConcept(superC, true)
							.contains(p)) {
						if (level >= maxLevel) {
							maxLevel = level;
						}
					}
				}

				/* 加入c的Description */
				if (!ctemp.isAnon()) {// 非匿名
					int pos = findCnptPosInFullName(ctemp.toString());// 位置
					if (pos != -1) {
						ArrayList lb = cnptBasicTextDes[pos].text;
						for (Iterator itx = lb.iterator(); itx.hasNext();) {
							Word w = (Word) ((Word) itx.next()).clone();
							w.weight *= (1.0 / (1.0 + maxLevel));
							des.text.add(w);
						}
					} else {// 处理不能列举出来的类
						/* 直接提取local name */
						Word w = new Word();
						w.content = ctemp.getLocalName();
						w.weight = 1.0;
						des.text.add(w);
					}

				} else {// 匿名
					/* 添加子图中对该匿名节点的描述 */
					Statement sx = ontParse
							.getAStatement(
									m,
									p,
									m
											.getProperty("http://www.w3.org/2000/01/rdf-schema#range"),
									ctemp);
					Set setx = new HashSet();
					setx.add(sx);
					ArrayList anonList = anonDesInSubGraphBiDirect(ctemp, 0, g,
							setx);
					// ArrayList
					// anonList=anonDesInSubGraphSingleDirect(ctemp,0,g);
					// ArrayList anonList=anonDesInModel(ctemp,0);
					for (Iterator itx = anonList.iterator(); itx.hasNext();) {
						Word w = (Word) ((Word) itx.next()).clone();
						w.weight *= (1.0 / (1.0 + maxLevel));
						des.text.add(w);
					}
				}
			} else if (t == 4) {// Other resource
				/* 直接提取local name */
				Word w = new Word();
				w.content = obj.asNode().getLocalName();
				w.weight = 1.0;
				des.text.add(w);
			}
		}

		/* 后置处理 */
		des.text = desPostProcess(des.text);

		return des;
	}

	/***************************************************************************
	 * Property的Restriction文本描述 没有使用
	 **************************************************************************/
	public TextDes propRestrDes(OntProperty p, DirectedGraph g) {
		TextDes des = new TextDes();
		des.name = p.toString();
		des.text = new ArrayList();

		return des;
	}

	/***************************************************************************
	 * Property的性质文本描述 构成一个向量空间的形式
	 **************************************************************************/
	public TextDes propCharDes(OntProperty p) {
		TextDes des = new TextDes();
		des.name = p.toString();
		des.text = new ArrayList();

		/* DatatypeProperty */
		Word w1 = new Word();
		w1.content = "DatatypeProperty";
		if (p.isDatatypeProperty()) {
			w1.weight = 1.0;
		}
		des.text.add(0, w1);
		/* ObjectProperty */
		Word w2 = new Word();
		w2.content = "ObjectProperty";
		if (p.isObjectProperty()) {
			w2.weight = 1.0;
		}
		des.text.add(1, w2);
		/* AnnotationProperty */
		Word w3 = new Word();
		w3.content = "AnnotationProperty";
		if (p.isAnnotationProperty()) {
			w3.weight = 1.0;
		}
		des.text.add(2, w3);
		/* FunctionalProperty */
		Word w4 = new Word();
		w4.content = "FunctionalProperty";
		if (p.isFunctionalProperty()) {
			w4.weight = 1.0;
		}
		des.text.add(3, w4);
		/* InverseFunctionalProperty */
		Word w5 = new Word();
		w5.content = "InverseFunctionalProperty";
		if (p.isInverseFunctionalProperty()) {
			w5.weight = 1.0;
		}
		des.text.add(4, w5);
		/* SymmetricProperty */
		Word w6 = new Word();
		w6.content = "SymmetricProperty";
		if (p.isSymmetricProperty()) {
			w6.weight = 1.0;
		}
		des.text.add(5, w6);
		/* TransitiveProperty */
		Word w7 = new Word();
		w7.content = "TransitiveProperty";
		if (p.isTransitiveProperty()) {
			w7.weight = 1.0;
		}
		des.text.add(6, w7);

		/* 不考虑功能文本描述 */
		des.text.clear();

		return des;
	}

	/***************************************************************************
	 * 给定子图中的属性的相关实例的文本描述
	 **************************************************************************/
	public ArrayList propInsTexDes(OntProperty p, DirectedGraph g) {
		ArrayList result = new ArrayList();

		TextDes domainDes = new TextDes();
		domainDes.name = p.toString();
		domainDes.text = new ArrayList();

		TextDes rangeDes = new TextDes();
		rangeDes.name = p.toString();
		rangeDes.text = new ArrayList();

		/* 找到子图中p对应的实例 */
		for (Iterator it = curStmList.iterator(); it.hasNext();) {
			Statement st = (Statement) it.next();
			Resource rSub = st.getSubject();
			Property rPre = st.getPredicate();
			RDFNode rObj = st.getObject();
			int subType = getResourceType(rSub.toString());
			int objType = getResourceType(rObj.toString());
			if (rPre.toString().equals(p.toString()) && (subType == 3)
					&& (objType == 3 || objType == 4)) {

				/* Domain实例的Description */
				Individual idomain = null;
				idomain = m.getIndividual(rSub.toString());
				if (idomain == null) {
					idomain = this.findInsInFullName(rSub.toString());
				}

				if (idomain != null) {
					/* 加入i的Description */
					if (!idomain.isAnon()) {// 非匿名
						int pos = findInsPosInFullName(idomain.toString());// 位置
						ArrayList lb = insBasicTextDes[pos].text;
						for (Iterator itx = lb.iterator(); itx.hasNext();) {
							Word w = (Word) ((Word) itx.next()).clone();
							domainDes.text.add(w);
						}
						/* 实例在子图中的其它描述 */
						Set setx = new HashSet();
						setx.add(st);
						ArrayList insList = insDesInSubGraphBiDirect(idomain,
								0, g, setx);
						for (Iterator itx = insList.iterator(); itx.hasNext();) {
							Word w = (Word) ((Word) itx.next()).clone();
							domainDes.text.add(w);
						}
					} else {// 匿名
						/* 添加子图中对该匿名节点的描述 */
						Set setx = new HashSet();
						setx.add(st);
						ArrayList anonList = anonDesInSubGraphBiDirect(idomain,
								0, g, setx);
						// ArrayList
						// anonList=anonDesInSubGraphSingleDirect(idomain,0,g);
						// ArrayList anonList=anonDesInModel(idomain,0);
						for (Iterator itx = anonList.iterator(); itx.hasNext();) {
							Word w = (Word) ((Word) itx.next()).clone();
							domainDes.text.add(w);
						}
					}
				}

				/* Range实例的Description */
				if (objType == 3) {
					Individual iRange = null;
					iRange = m.getIndividual(rObj.toString());
					if (iRange == null) {
						iRange = this.findInsInFullName(rObj.toString());
					}

					if (iRange != null) {
						/* 加入i的Description */
						if (!iRange.isAnon()) {// 非匿名
							int pos = findInsPosInFullName(iRange.toString());// 位置
							ArrayList lb = insBasicTextDes[pos].text;
							for (Iterator itx = lb.iterator(); itx.hasNext();) {
								Word w = (Word) ((Word) itx.next()).clone();
								rangeDes.text.add(w);
							}
							/* 实例在子图中的其它描述 */
							Set setx = new HashSet();
							setx.add(st);
							ArrayList insList = insDesInSubGraphBiDirect(
									iRange, 0, g, setx);
							for (Iterator itx = insList.iterator(); itx
									.hasNext();) {
								Word w = (Word) ((Word) itx.next()).clone();
								rangeDes.text.add(w);
							}
						} else {// 匿名
							/* 添加子图中对该匿名节点的描述 */
							Set setx = new HashSet();
							setx.add(st);
							ArrayList anonList = anonDesInSubGraphBiDirect(
									iRange, 0, g, setx);
							// ArrayList anonList=anonDesInSubGraph(iRange,0);
							// ArrayList anonList=anonDesInModel(iRange,0);
							for (Iterator itx = anonList.iterator(); itx
									.hasNext();) {
								Word w = (Word) ((Word) itx.next()).clone();
								rangeDes.text.add(w);
							}
						}
					}
				} else {
					Resource rt = m.getResource(rObj.toString());
					if (rt != null) {
						if (rt.isAnon()) {// 匿名
							Set setx = new HashSet();
							setx.add(st);
							ArrayList anonList = anonDesInSubGraphBiDirect(rt,
									0, g, setx);
							// ArrayList anonList=anonDesInSubGraph(rt,0);
							// ArrayList anonList=anonDesInModel(rt,0);
							for (Iterator itx = anonList.iterator(); itx
									.hasNext();) {
								Word w = (Word) ((Word) itx.next()).clone();
								rangeDes.text.add(w);
							}
						} else {// 非匿名
							String localname = "";
							if (rt.isLiteral()){
								localname = idomain.getPropertyValue(p).asNode()
								.getLiteralLexicalForm().toString();
							}
							else if (rt.isURIResource()){
								localname=rt.getLocalName();
							}
							if (localname.equals("")) {
								localname = rt.toString();
							}

							// 属性值进一步处理
							localname = processPVaule(localname);
							// if (rt.asNode().isLiteral()){
							// Literal l=(Literal)rt;
							// localname=l.getValue().toString();
							// }
							// else if (rt.asNode().isURI()){
							// localname=rt.asNode().getLocalName();
							// }
							// else {
							// localname=rt.toString();
							// }
							// if (localname.equals("")){
							// localname=rt.toString();
							// }

							Word w = new Word();
							w.content = localname;
							w.weight = 1.0;
							rangeDes.text.add(w);
						}
					}
				}
			}
		}

		/* 后置处理 */
		domainDes.text = desPostProcess(domainDes.text);
		rangeDes.text = desPostProcess(rangeDes.text);

		result.add(domainDes);
		result.add(rangeDes);
		return result;
	}

	private String processPVaule(String name) {
		// 判断是否包含^^
		String newName = null;
		newName = name;
		int pos = -1;
		pos = name.indexOf("^^");
		if (pos != -1) {
			newName = name.substring(0, pos);
		}
		return newName;
	}

	/***************************************************************************
	 * 单个单词的预处理 可能会被拆分为多个词，所以用List记录结果
	 **************************************************************************/
	public ArrayList simpleStringPreProcess(String s) {
		ArrayList list = new ArrayList();

		SplitWords spWord = new SplitWords();
		DelStopWords delSWrod = new DelStopWords();
		delSWrod.loadStopWords();

		/* 文本预处理 */
		s = delSWrod.removeStopWords(s);
		list = spWord.split(s);
		list = delSWrod.removeStopWords(list);

		return list;
	}

	/***************************************************************************
	 * 描述的后处理工作 这一步是将基本描述中相同的文本进行合并
	 **************************************************************************/
	public ArrayList desPostProcess(ArrayList rawList) {
		if (rawList == null) {// 描述为空的情况
			return null;
		}

		ArrayList newList = new ArrayList();
		Set termSet = new HashSet();
		for (Iterator it = rawList.iterator(); it.hasNext();) {
			Word w = new Word();
			w = (Word) it.next();
			String content = w.content;
			double weight = w.weight;
			/* 当前词是否已经有了 */
			if (termSet.contains(content)) {// 包含
				/* 找到当前词的位置 */
				int pos = 0;
				Word wt = new Word();
				for (Iterator jt = newList.iterator(); jt.hasNext();) {
					wt = (Word) jt.next();
					String ct = wt.content;
					if (ct.equals(content)) {
						pos = newList.indexOf(wt);
						wt.weight += weight;
						break;
					}
				}
				// wt.weight+=weight;

				// newList.set(pos,wt);
			} else {// 不包含
				termSet.add(content);
				newList.add(w.clone());
			}
		}
		return newList;
	}

	public ArrayList getAnonResourceWithType(String name) {
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
	 * 递归求实例在当前子图中的描述 同时考虑blank node开头和结尾的三元组
	 **************************************************************************/
	public ArrayList insDesInSubGraphBiDirect(Resource b, int level,
			DirectedGraph g, Set visitedSet) {
		ArrayList result = new ArrayList();

		/* 求以b开头的三元组 */
		for (Iterator it = curStmList.iterator(); it.hasNext();) {
			Statement st = (Statement) it.next();
			Resource s = st.getSubject();
			Property p = st.getPredicate();
			RDFNode o = st.getObject();

			// 是否已经访问过
			if (visitedSet.contains(st)) {
				continue;// 跳过该三元组
			}

			// 判断以b开头
			if (s.toString().equals(b.toString())) {

				// 加入访问过列表
				visitedSet.add(st);

				if (p.isAnon()) {// p为匿名的情况不考虑
					result = null;
				}

				/* p的Description */
				ArrayList lp = new ArrayList();
				/* 判断p是不是元属性 */
				if (!isMetaOntData(p)) {
					int pPos = findPropPosInFullName(p.toString());
					if (pPos != -1) {
						lp = propBasicTextDes[pPos].text;
					} else {
						/* 直接提取local name */
						String localname = null;
						localname = p.getLocalName();

						ArrayList otherList = simpleStringPreProcess(localname);
						for (Iterator xt = otherList.iterator(); xt.hasNext();) {
							String stemp = (String) xt.next();
							Word w = new Word();
							w.content = stemp;
							w.weight = 1.0;
							lp.add(w);
						}
					}
				}

				if (lp != null) {
					result.addAll(lp);
				}

				/* o的Description */
				ArrayList lo = new ArrayList();
				/* 递归终止条件 */
				if (!ontParse.isBlankNode(o.toString())) {
					/* 过滤元语 */
					if (!isMetaOntData(m.getResource(o.toString()))) {
						/* 判断o的成分 */
						int t = getResourceType(o.toString());
						if (t == 1) {// Class
							// 这种情况不可能
							int tempPos = findCnptPosInFullName(o.toString());
							lo = cnptBasicTextDes[tempPos].text;
						} else if (t == 2) {// Property
							// 这种情况也不可能
							int tempPos = findPropPosInFullName(o.toString());
							/* 得到Description */
							lo = propBasicTextDes[tempPos].text;
						} else if (t == 3) {// Instance
							// o也是实例的情况
							int tempPos = findInsPosInFullName(o.toString());
							/* 得到Description */
							if (tempPos == -1) {
								System.out.println("stop");
							}
							lo = insBasicTextDes[tempPos].text;
						} else if (t == 4) {// Other resource
							/* 直接提取local name */
							String localname = null;
							if (o.isLiteral()) {
								Literal l = (Literal) o;
								localname = l.getLexicalForm().toString();
							} else {
								localname = o.asNode().getLocalName();
							}
							if (localname.equals("")) {
								localname = o.toString();
							}
							ArrayList otherList = simpleStringPreProcess(localname);
							for (Iterator xt = otherList.iterator(); xt
									.hasNext();) {
								String stemp = (String) xt.next();
								Word w = new Word();
								w.content = stemp;
								w.weight = 1.0;
								lo.add(w);
							}
						}
						/* return终止条件 */
						result.addAll(lo);
					}
				} else {
					/* 递归 */
					lo = insDesInSubGraphBiDirect(m.getResource(o.toString()),
							level + 1, g, visitedSet);

					/* 乘衰减系数 */
					double downPare = Math.pow(wAnonNode,
							((double) level + 1.0));
					for (Iterator itx = lo.iterator(); itx.hasNext();) {
						Word w = (Word) itx.next();
						w.weight *= downPare;
					}
					result.addAll(lo);
				}
			}
		}

		/* 求以b结尾的三元组 */
		for (Iterator it = curStmList.iterator(); it.hasNext();) {
			Statement st = (Statement) it.next();
			Resource s = st.getSubject();
			Property p = st.getPredicate();
			RDFNode o = st.getObject();

			// 是否已经访问过
			if (visitedSet.contains(st)) {
				continue;// 跳过该三元组
			}

			// 判断以b结尾
			if (o.asNode().toString().equals(b.toString())) {

				// 加入访问过列表
				visitedSet.add(st);

				if (p.isAnon()) {// p为匿名的情况不考虑
					result = null;
				}

				/* p的Description */
				ArrayList lp = new ArrayList();
				/* 判断p是不是元属性 */
				if (!isMetaOntData(p)) {
					int pPos = findPropPosInFullName(p.toString());
					if (pPos != -1) {
						lp = propBasicTextDes[pPos].text;
					} else {
						/* 直接提取local name */
						String localname = null;
						localname = p.getLocalName();

						ArrayList otherList = simpleStringPreProcess(localname);
						for (Iterator xt = otherList.iterator(); xt.hasNext();) {
							String stemp = (String) xt.next();
							Word w = new Word();
							w.content = stemp;
							w.weight = 1.0;
							lp.add(w);
						}
					}
				}

				if (lp != null) {
					result.addAll(lp);
				}

				/* s的Description */
				ArrayList lo = new ArrayList();
				/* 递归终止条件 */
				if (!ontParse.isBlankNode(s.toString())) {
					/* 过滤元语 */
					if (!isMetaOntData(m.getResource(s.toString()))) {
						/* 判断s的成分 */
						int t = getResourceType(s.toString());
						if (t == 1) {// Class
							int tempPos = findCnptPosInFullName(s.toString());
							lo = cnptBasicTextDes[tempPos].text;
						} else if (t == 2) {// Property
							int tempPos = findPropPosInFullName(s.toString());
							/* 得到Description */
							lo = propBasicTextDes[tempPos].text;
						} else if (t == 3) {// Instance
							int tempPos = findInsPosInFullName(s.toString());
							/* 得到Description */
							if (tempPos == -1) {
								System.out.println("stop");
							}
							lo = insBasicTextDes[tempPos].text;
						} else if (t == 4) {// Other resource
							/* 直接提取local name */
							String localname = null;
							if (s.isLiteral()) {
								Literal l = (Literal) s;
								localname = l.getValue().toString();
							} else {
								localname = s.asNode().getLocalName();
							}
							if (localname.equals("")) {
								localname = s.toString();
							}
							ArrayList otherList = simpleStringPreProcess(localname);
							for (Iterator xt = otherList.iterator(); xt
									.hasNext();) {
								String stemp = (String) xt.next();
								Word w = new Word();
								w.content = stemp;
								w.weight = 1.0;
								lo.add(w);
							}
						}
						/* return终止条件 */
						result.addAll(lo);
					}
				} else {
					/* 递归 */
					lo = insDesInSubGraphBiDirect(m.getResource(s.toString()),
							level + 1, g, visitedSet);

					/* 乘衰减系数 */
					double downPare = Math.pow(wAnonNode,
							((double) level + 1.0));
					for (Iterator itx = lo.iterator(); itx.hasNext();) {
						Word w = (Word) itx.next();
						w.weight *= downPare;
					}
					result.addAll(lo);
				}
			}
		}

		return result;
	}

	/***************************************************************************
	 * 本体图中的其它元素的文本描述
	 **************************************************************************/
	public ArrayList fullOtherTexDes() {
		ArrayList result = new ArrayList();
		ArrayList checkedSet = new ArrayList();// 元素是否被处理过标志
		SplitWords spWord = new SplitWords();
		DelStopWords delSWrod = new DelStopWords();
		delSWrod.loadStopWords();

		/* 遍历子图中的三元组 */
		for (Iterator it = fullGraphStms.iterator(); it.hasNext();) {
			Statement st = (Statement) it.next();
			String localURI = null;
			int elmType = -1;
			Resource r = st.getSubject();
			Property p = st.getPredicate();
			RDFNode o = st.getObject();

			/* 处理s */
			if (r.isURIResource()) {
				localURI = r.getNameSpace();
			}
			if (!ontLngURI.contains(localURI) && !baseURI.equals(localURI)
					&& !checkedSet.contains(r.toString())) {// 不是元语，基本元素，没处理过
				checkedSet.add(r.toString());
				TextDes des = new TextDes();
				des.name = r.toString();
				des.text = new ArrayList();
				if (!r.isAnon()) {// 不是匿名资源
					// 判断类型
					elmType = getResourceType(r.toString());
					des.type = elmType;
					if (elmType == 1) {
						// 查Class表
						int basicDespos = findCnptPosInFullName(r.toString());
						des.text = cnptBasicTextDes[basicDespos].text;
					} else if (elmType == 2) {
						// 查Property表
						int basicDespos = findPropPosInFullName(r.toString());
						if (basicDespos<0){
							des.text=new ArrayList();
						}
						else{
							des.text = propBasicTextDes[basicDespos].text;
						}						
					} else if (elmType == 3) {
						// 查Individual表
						int basicDespos = findInsPosInFullName(r.toString());
						des.text = insBasicTextDes[basicDespos].text;
					} else if (elmType == 4) {
						// 处理其它的value资源
						// 暂时不考虑其它的资源，如果需要，则计算它的基本描述
					}
				} else {
					// 处理匿名资源
					Set setx = new HashSet();
					setx.add(st);
					ArrayList anonList = anonDesInFullGraphBiDirect(r, 0, setx);
					// ArrayList anonList=anonDesInSubGraphSingleDirect(r,0,g);
					for (Iterator itx = anonList.iterator(); itx.hasNext();) {
						Word w = (Word) ((Word) itx.next()).clone();
						des.text.add(w);
					}
				}
				/* 后置处理 */
				des.text = desPostProcess(des.text);
				if (!des.text.isEmpty()) {
					result.add(des);
				}
			}

			/* 处理p */
			localURI = null;
			if (p.isURIResource()) {
				localURI = p.getNameSpace();
			}
			if (!ontLngURI.contains(localURI) && !baseURI.equals(localURI)
					&& !checkedSet.contains(p.toString())) {// 不是元语，基本元素，没处理过
				checkedSet.add(p.toString());
				TextDes des = new TextDes();
				des.name = p.toString();
				des.text = new ArrayList();
				if (!p.isAnon()) {// 不是匿名资源
					// 判断类型
					elmType = getResourceType(p.toString());
					des.type = elmType;
					if (elmType == 1) {
						// 查Class表
						int basicDespos = findCnptPosInFullName(p.toString());
						des.text = cnptBasicTextDes[basicDespos].text;
					} else if (elmType == 2) {
						// 查Property表
						int basicDespos = findPropPosInFullName(p.toString());
						if (basicDespos<0){
							des.text=new ArrayList();
						}
						else{
							des.text = propBasicTextDes[basicDespos].text;
						}						
					} else if (elmType == 3) {
						// 查Individual表
						int basicDespos = findInsPosInFullName(p.toString());
						des.text = insBasicTextDes[basicDespos].text;
					} else if (elmType == 4) {
						// 处理其它的value资源
						// 暂时不考虑其它的资源，如果需要，则计算它的基本描述
					}
				} else {
					// 处理匿名资源
					Set setx = new HashSet();
					setx.add(st);
					ArrayList anonList = anonDesInFullGraphBiDirect(r, 0, setx);
					// ArrayList anonList=anonDesInSubGraphSingleDirect(p,0,g);
					for (Iterator itx = anonList.iterator(); itx.hasNext();) {
						Word w = (Word) ((Word) itx.next()).clone();
						des.text.add(w);
					}
				}
				/*后置处理*/
				des.text = desPostProcess(des.text);
				if (!des.text.isEmpty()) {
					result.add(des);
				}
			}

			/*处理o*/
			localURI = null;
			if (o.isURIResource()) {
				localURI = o.asNode().getNameSpace();
			}
			if (!ontLngURI.contains(localURI) && !baseURI.equals(localURI)
					&& !checkedSet.contains(o.toString())) {// 不是元语，基本元素，没处理过
				checkedSet.add(o.toString());
				TextDes des = new TextDes();
				des.name = o.toString();
				des.text = new ArrayList();
				if (!o.isAnon()) {//不是匿名资源
					//判断类型
					elmType = getResourceType(o.toString());
					des.type = elmType;
					if (elmType == 1) {
						//查Class表
						int basicDespos = findCnptPosInFullName(o.toString());
						if (basicDespos>=0){
							des.text = cnptBasicTextDes[basicDespos].text;
						}						
					} else if (elmType == 2) {
						//查Property表
						int basicDespos = findPropPosInFullName(o.toString());
						des.text = propBasicTextDes[basicDespos].text;
					} else if (elmType == 3) {
						//查Individual表
						int basicDespos = findInsPosInFullName(o.toString());
						des.text = insBasicTextDes[basicDespos].text;
					} else if (elmType == 4) {
						String str = null;
						if (o.isLiteral()) {
							str = o.asNode().getLiteralLexicalForm();
						} else {
							str = o.toString();
						}
						str = delSWrod.removeStopWords(str);
						ArrayList list = spWord.split(str);
						list = delSWrod.removeStopWords(list);
						/*添加权重*/
						for (Iterator itx = list.iterator(); itx.hasNext();) {
							String stemp = (String) itx.next();
							Word w = new Word();
							w.content = stemp;
							w.weight = 1.0;
							des.text.add(w);
						}
					}
				} else {
					//处理匿名资源
					Set setx = new HashSet();
					setx.add(st);
					ArrayList anonList = anonDesInFullGraphBiDirect(m
							.getResource(o.toString()), 0, setx);
					//ArrayList anonList=anonDesInSubGraphSingleDirect(m.getResource(o.toString()),0,g);
					for (Iterator itx = anonList.iterator(); itx.hasNext();) {
						Word w = (Word) ((Word) itx.next()).clone();
						des.text.add(w);
					}
				}
				/*后置处理*/
				des.text = desPostProcess(des.text);
				if (!des.text.isEmpty()) {
					result.add(des);
				}
			}
		}
		return result;
	}
	
	/*****************************
	 * 构造一个实例的语义描述文档
	 *****************************/
	public TextDes getOneInsOntTextDes(OntModel m, Individual ins){
		if (ins == null)
            return null;
		/**1.实例基本描述**/
		SplitWords spWord = new SplitWords();
		DelStopWords delSWrod = new DelStopWords();
		delSWrod.loadStopWords();

		TextDes insBasicTextDes = new TextDes();
		insBasicTextDes=this.getOneInsBasicDes(m,ins);		

		/** 2.完善实例基本描述* */
		TextDes richInsBasicTextDes = new TextDes();
		richInsBasicTextDes.name=insBasicTextDes.name;
		richInsBasicTextDes.text=(ArrayList) (insBasicTextDes.text).clone();
		
		/* 增加sameAs */
		Property pm = m.getProperty("http://www.w3.org/2002/07/owl#sameAs");
		Selector sl = new SimpleSelector(ins, pm, (RDFNode) null);
		for (StmtIterator it = m.listStatements(sl); it.hasNext();) {
			Statement st = it.next();
			RDFNode o = st.getObject();
			Individual idx = null;
			if (!o.isAnon()) {
				idx = m.getIndividual(o.toString());
			} else {
				continue;
			}
			/* 加入该实例的Des */
			TextDes tDes=new TextDes();
			tDes=getOneInsBasicDes(m,idx);
			for (Iterator it1 = tDes.text.iterator(); it1.hasNext();) {
				Word w = (Word) it1.next();
				richInsBasicTextDes.text.add(w);
			}			
		}

	/* 后置处理 */
	richInsBasicTextDes.text = desPostProcess(richInsBasicTextDes.text);

	return richInsBasicTextDes;		
	}
	
	public TextDes getOneInsBasicDes(OntModel m, Individual ins){
		/**1.实例基本描述**/
		SplitWords spWord = new SplitWords();
		DelStopWords delSWrod = new DelStopWords();
		delSWrod.loadStopWords();

		TextDes insBasicTextDes = new TextDes();
        try {
            insBasicTextDes.name = ins.getLocalName();
        } catch (Exception e) {
            e.printStackTrace();
        }
		insBasicTextDes.text = new ArrayList();
		if (!ins.isAnon()) {// 不是匿名实例
			String s = null;
			ArrayList list = new ArrayList();
			/* local name */
			s = ins.getLocalName();
			/* 文本预处理 */
			s = delSWrod.removeStopWords(s);
			list = spWord.split(s);
			list = delSWrod.removeStopWords(list);
			/* 添加权重 */
			for (Iterator it = list.iterator(); it.hasNext();) {
				String stemp = (String) it.next();
				Word w = new Word();
				w.content = stemp;
				w.weight = wLocalName;
				insBasicTextDes.text.add(w);
			}

			/* label */
			for (ExtendedIterator itx=ins.listLabels(null);itx.hasNext();){
				Literal L=(Literal)itx.next();
				s = null;
				list = null;
				s = L.getLexicalForm();
				if (s != null) {
					/* 文本预处理 */
					s = delSWrod.removeStopWords(s);
					list = spWord.split(s);
					list = delSWrod.removeStopWords(list);
					/* 添加权重 */
					for (Iterator it = list.iterator(); it.hasNext();) {
						String stemp = (String) it.next();
						Word w = new Word();
						w.content = stemp;
						w.weight = wLabel;
						insBasicTextDes.text.add(w);
					}
				}
			}

			/* comment */
			s = null;
			list = null;
			/* local name */
			s = ins.getComment(null);
			if (s != null) {
				/* 文本预处理 */
				s = delSWrod.removeStopWords(s);
				list = spWord.split(s);
				list = delSWrod.removeStopWords(list);
				/* 添加权重 */
				for (Iterator it = list.iterator(); it.hasNext();) {
					String stemp = (String) it.next();
					Word w = new Word();
					w.content = stemp;
					w.weight = wComment;
					insBasicTextDes.text.add(w);
				}
			}
		}
		return insBasicTextDes;
	}
}
