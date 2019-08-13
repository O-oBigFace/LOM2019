/************************************************
 * Source code information
 * -----------------------
 * Original author	 Peng Wang, School of Computer Science & Eng., Southeast University
 * Author email      pwangseu@gmail.com
 * Web               http://ontomapping.googlepages.com
 * Created			 2007-5-22
 * Filename          TfIdfSim.java
 * Version           2.0
 * 
 * Last modified on  2007-5-22
 *               by  Peng Wang
 * -----------------------
 * Functions describe:
 * TIF/IDF处理
 ***********************************************/
package lily.tool.textsimilarity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import lily.tool.datastructure.TextDes;
import lily.tool.datastructure.Word;
import lily.tool.strsimilarity.SimInArray;

/*******************************************************************************
 * Class information -------------------
 * 
 * @author Peng Wang
 * @date 2007-5-22
 * 
 * describe: 利用TF/IDF来求文本描述中的相似度 输入：两个本体的文本 输出：相似度
 ******************************************************************************/
public class TfIdfSim {

	/***************************************************************************
	 * 计算TF 
	 * 输入：List形式表示的Doc 
	 * 输出：List形式表示的TF
	 **************************************************************************/
	public ArrayList getDocTF(ArrayList doc) {
		ArrayList tfList = new ArrayList();

		/* 最大的frequency */
		double maxFre = 0;
		for (Iterator it = doc.iterator(); it.hasNext();) {
			Word w = (Word) it.next();
			maxFre = Math.max(maxFre, w.weight);
		}

		/* 得到TF */
		for (Iterator it = doc.iterator(); it.hasNext();) {
			Word w = (Word) it.next();
			tfList.add(w.weight / maxFre);
		}

		return tfList;
	}

	/***************************************************************************
	 * 计算IDF 
	 * 输入：TextDes[]形式表示的DocA和DocB 
	 * 输出：DocA和DocB的List形式表示的IDF
	 **************************************************************************/
	public ArrayList getDocIDF(TextDes[] docA, int nDocA, TextDes[] docB,int nDocB) {
		ArrayList result = new ArrayList();
		ArrayList[] idfDocA = new ArrayList[nDocA];
		ArrayList[] idfDocB = new ArrayList[nDocB];
		
		/*处理不是很多文档计算的特殊情况*/
		if (nDocA==1 && nDocB==1){
			idfDocA[0] = new ArrayList();
			for (int i=0;i<docA[0].text.size();i++)	{idfDocA[0].add(1.0);}
			idfDocB[0] = new ArrayList();
			for (int i=0;i<docB[0].text.size();i++)	{idfDocB[0].add(1.0);}			
			
			result.add(0, idfDocA);
			result.add(1, idfDocB);
			return result;
		}

		/* 总文档数 */
		int nDoc = nDocA + nDocB;

		/* 先构造term的集合 */
		ArrayList docTerm = new ArrayList();
		for (int i = 0; i < nDocA; i++) {
			ArrayList termList = docA[i].text;
			Set termSet = new HashSet();
			for (Iterator it = termList.iterator(); it.hasNext();) {
				Word w = (Word) it.next();
				termSet.add(w.content);
			}
			docTerm.add(i, termSet);
		}
		for (int i = 0; i < nDocB; i++) {
			ArrayList termList = docB[i].text;
			Set termSet = new HashSet();
			for (Iterator it = termList.iterator(); it.hasNext();) {
				Word w = (Word) it.next();
				termSet.add(w.content);
			}
			docTerm.add(nDocA + i, termSet);
		}

		/* docA的IDF */
		double idf = 0.0;
		for (int i = 0; i < nDocA; i++) {
			ArrayList termList = docA[i].text;// 取term的List
			idfDocA[i] = new ArrayList();
			for (Iterator it = termList.iterator(); it.hasNext();) {
				Word w = (Word) it.next();
				String term = w.content;// 取term
				/* 统计term的文档频率 */
				int df = 0;
				for (Iterator jt = docTerm.iterator(); jt.hasNext();) {
					Set st = (Set) jt.next();
					if (st.contains(term)) {
						df++;
					}
				}
				/* log(N/n)分母加0.01是为了修正出现0的结果。 */
				idf = Math.log(((double) nDoc + 0.01) / (double) df);
				idfDocA[i].add(idf);
			}
		}

		/* docB的IDF */
		for (int i = 0; i < nDocB; i++) {
			ArrayList termList = docB[i].text;// 取term的List
			idfDocB[i] = new ArrayList();
			for (Iterator it = termList.iterator(); it.hasNext();) {
				Word w = (Word) it.next();
				String term = w.content;// 取term
				/* 统计term的文档频率 */
				int df = 0;
				for (Iterator jt = docTerm.iterator(); jt.hasNext();) {
					Set st = (Set) jt.next();
					if (st.contains(term)) {
						df++;
					}
				}
				/* log(N/n)分母加0.01是为了修正出现0的结果。 */
				idf = Math.log(((double) nDoc + 0.01) / (double) df);
				idfDocB[i].add(idf);
			}
		}
		result.add(0, idfDocA);
		result.add(1, idfDocB);
		return result;
	}

	/***************************************************************************
	 * 计算doc的if/idf权重 
	 * 输入：TextDes[]形式表示的Doc 
	 * 输出：List形式表示的权重
	 **************************************************************************/
	public ArrayList[] getTFIDFWeight(TextDes[] doc, int nDoc,
			ArrayList[] tfList, ArrayList[] idfList) {
		ArrayList[] weightList = new ArrayList[nDoc];

		for (int i = 0; i < nDoc; i++) {
			weightList[i] = new ArrayList();
			if (tfList[i] == null) {
				continue;
			}
			for (int j = 0; j < tfList[i].size(); j++) {
				/* 得到TF */
				double tf = ((Double) tfList[i].get(j)).doubleValue();
				/* 得到IDF */
				double idf = ((Double) idfList[i].get(j)).doubleValue();
				/* 得到weight */
				weightList[i].add(tf * idf);
			}
		}
		return weightList;
	}

	/***************************************************************************
	 * 构造两个文本的向量 
	 * 输入：List形式表示的dA和dB,以及对应的权重List 
	 * 输出：dA和dB的向量
	 **************************************************************************/
	public ArrayList consTextVector(ArrayList dA, ArrayList dB, ArrayList wdA,
			ArrayList wdB) {
		ArrayList result = new ArrayList();
		ArrayList vA = new ArrayList();
		ArrayList vB = new ArrayList();

		/* 先构造基向量 */
		ArrayList baseV = new ArrayList();

		ArrayList termA = new ArrayList();
		for (Iterator it = dA.iterator(); it.hasNext();) {
			Word w = (Word) it.next();
			termA.add(w.content);
			if (!baseV.contains(w.content)) {
				baseV.add(w.content);
			}
		}
		ArrayList termB = new ArrayList();
		for (Iterator it = dB.iterator(); it.hasNext();) {
			Word w = (Word) it.next();
			termB.add(w.content);
			if (!baseV.contains(w.content)) {
				baseV.add(w.content);
			}
		}

		/* 构造向量A */
		int pos = 0;
		double weight = 0.0;
		for (int i = 0; i < baseV.size(); i++) {
			String term = (String) baseV.get(i);
			if (termA.contains(term)) {// 当前维存在
				pos = termA.indexOf(term);
				weight = ((Double) wdA.get(pos)).doubleValue();
				vA.add(i, weight);
			} else {
                /*pos = SimInArray.getPosSimInArray(termA, term);
                if (pos >= 0) {
                    weight = ((Double) wdA.get(pos)).doubleValue();
                    vA.add(i, weight);
                } else*/
				    vA.add(0.0); // 当前维不存在
			}
		}

		/* 构造向量B */
		for (int i = 0; i < baseV.size(); i++) {
			String term = (String) baseV.get(i);
			if (termB.contains(term)) {// 当前维存在
				pos = termB.indexOf(term);
				weight = ((Double) wdB.get(pos)).doubleValue();
				vB.add(i, weight);
			} else {
                /*pos = SimInArray.getPosSimInArray(termB, term);
                if (pos >= 0) {
                    weight = ((Double) wdB.get(pos)).doubleValue();
                    vB.add(i, weight);
                } else*/
				    vB.add(0.0);// 当前维不存在
			}
		}

		result.add(0, vA);
		result.add(1, vB);
		return result;
	}

	/***************************************************************************
	 * 计算向量相似度 
	 * 输入：TextDes[]形式表示的DocA和DocB 
	 * 输出：DocA和DocB的List形式表示的IDF
	 **************************************************************************/
	public double getTextVectorSim(ArrayList vA, ArrayList vB) {
		double sim = 0.0;

		/* 判断两向量的维数是否相等 */
		if (vA.size() != vB.size()) {
			System.out.println("向量维数不等");
			return -1;
		}

		/* 分子点乘 */
		double aDotb = 0;
		for (int i = 0; i < vA.size(); i++) {
			double x = ((Double) vA.get(i)).doubleValue();
			double y = ((Double) vB.get(i)).doubleValue();
			aDotb = aDotb + x * y;
		}

		/* 分母向量长度 */
		double aDisb = 0;
		double x2 = 0;
		double y2 = 0;
		for (int i = 0; i < vB.size(); i++) {
			double x = ((Double) vA.get(i)).doubleValue();
			double y = ((Double) vB.get(i)).doubleValue();
			x2 = x2 + x * x;
			y2 = y2 + y * y;
		}

		aDisb = (Math.sqrt(x2)) * (Math.sqrt(y2));

		/* 相似度 */
		if (Math.abs(aDisb) < 0.00001) {
			sim = 0.0;
		} else {
			sim = aDotb / aDisb;
		}

		return sim;
	}
}
