/************************************************
 * Source code information
 * -----------------------
 * Original author	 Peng Wang, School of Computer Science & Eng., Southeast University
 * Author email      pwangseu@gmail.com
 * Web               http://ontomapping.googlepages.com
 * Created			 2007-4-27
 * Filename          SimpleStrMatching.java
 * Version           2.0
 * 
 * Last modified on  2007-4-27
 *               by  Peng Wang
 * -----------------------
 * Functions describe:
 * 用字符相似的简单匹配算法来计算ontology matching
 * 该方法仅仅利用到了identify自身的文本信息，速度较快
 ***********************************************/
package lily.onto.mapping.method;

import java.net.MalformedURLException;
import java.util.ArrayList;

import org.dom4j.DocumentException;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import lily.onto.parse.OWLOntParse;
import lily.tool.datastructure.MapRecord;
import lily.tool.strsimilarity.*;
import lily.tool.filter.*;
import lily.tool.mappingfile.*;

/*********************
 * Class information
 * -------------------
 * @author Peng Wang
 * @date   2007-4-27
 * 
 * describe:
 * 简单字符串匹配入口类
 ********************/
public class SimpleStrMatching {
	//源和目标本体的文件
	public String sourceOntFile;

	public String targetOntFile;

	//基准映射文件
	public String refalignFile;

	//源和目标本体的model
	public OntModel m_source;

	public OntModel m_target;

	//概念数目
	public int sourceConceptNum;

	public int targetConceptNum;

	//属性数目
	public int sourcePropNum;

	public int sourceDataPropNum;

	public int sourceObjPropNum;

	public int targetPropNum;

	public int targetDataPropNum;

	public int targetObjPropNum;

	//实例数目
	public int sourceInsNum;

	public int targetInsNum;

	//概念名
	public String[] sourceConceptName;

	public String[] targetConceptName;

	//属性名
	public String[] sourcePropName;

	public String[] sourceDataPropName;

	public String[] sourceObjPropName;

	public String[] targetPropName;

	public String[] targetDataPropName;

	public String[] targetObjPropName;

	//实例名
	public String[] sourceInsName;

	public String[] targetInsName;

	//映射结果数
	public int mappingNum;

	//映射结果
	public MapRecord[] mappingResult;

	//base URI
	public String sourceBaseURI;

	public String targetBaseURI;

	//相似度阀值
	public double simThreshold;

	//结果的文件名
	public String lilyFileName = "";

	//constants
	public int EQUALITY = 0;

	public int GENERAL = 1;

	public int SPECIFIC = 2;

	public static void main(String[] args) {
		new SimpleStrMatching().runSample();
//		new SimpleStrMatching().run2005Bench();
	}

	public void runSample() {
		SimpleStrMatching ontM = new SimpleStrMatching();
		ontM.setOntFile();
		ontM.parseOnt();
		ontM.init();
		ontM.run();
		// ontM.evaluate();
	}

	public void run2005Bench() {
		int fineNum = 51;
		int fileName[] = { 101, 102, 103, 104, 201, 202, 203, 204, 205, 206,
				207, 208, 209, 210, 221, 222, 223, 224, 225, 228, 230, 231,
				232, 233, 236, 237, 238, 239, 240, 241, 246, 247, 248, 249,
				250, 251, 252, 253, 254, 257, 258, 259, 260, 261, 262, 265,
				266, 301, 302, 303, 304 };
		for (int i = 0; i < fineNum; i++) {
			SimpleStrMatching ontM = new SimpleStrMatching();
			ontM.sourceOntFile = "./dataset/OAEI2005/bench/benchmarks/101/onto.rdf";
			ontM.targetOntFile = "./dataset/OAEI2005/bench/benchmarks/"
					+ String.valueOf(fileName[i]) + "/onto.rdf";
			ontM.refalignFile = "./dataset/OAEI2005/bench/benchmarks/"
					+ String.valueOf(fileName[i]) + "/refalign.rdf";
			ontM.parseOnt();
			ontM.init();
			System.out.println(ontM.targetOntFile);
			ontM.run();
			// ontM.evaluate();
			System.out.println("------------------");
		}
	}

	//初始化，主要是基本的参数设置
	public void init() {
		simThreshold = 0.8;
	}

	//设置本体文件
	public void setOntFile() {
		sourceOntFile = new String(
				"./dataset/OAEI2005/bench/benchmarks/101/onto.rdf");
		targetOntFile = new String(
				"./dataset/OAEI2007/bench/benchmarks/101/onto.rdf");
		refalignFile = new String(
				"./dataset/OAEI2007/bench/benchmarks/101/refalign.rdf");
	}

	//解析本体
	public void parseOnt() {
		OWLOntParse ontParse = new OWLOntParse();
		ArrayList list = new ArrayList();

		//源本体----------------------------------------------
		m_source = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
		//The ontology file information
		m_source.getDocumentManager().addAltEntry("http://pengwang/",
				sourceOntFile);
		//Read the reference ontology file
		ontParse.readOntFile(m_source, sourceOntFile);

		//源本体的base URI
		sourceBaseURI = ontParse.getOntBaseURI(m_source);

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

		//目标本体---------------------------------------------
		m_target = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
		//The ontology file information
		m_target.getDocumentManager().addAltEntry("http://LiSun/",
				targetOntFile);

		//Read the target ontology file    	
		ontParse.readOntFile(m_target, targetOntFile);

		//源本体的base URI
		targetBaseURI = ontParse.getOntBaseURI(m_target);

		//Get all Classes of Ontology
		list = ontParse.listAllConceptsFilterBaseURI(m_target, targetBaseURI);
		targetConceptNum = ((Integer) list.get(0)).intValue();
		targetConceptName = new String[targetConceptNum];
		targetConceptName = (String[]) ((ArrayList) list.get(1)).toArray(new String[0]);

		//Get all datatype properties
		list = ontParse.listAllDatatypeRelationsURI(m_target, targetBaseURI);
		targetDataPropNum = ((Integer) list.get(0)).intValue();
		targetDataPropName = new String[targetDataPropNum];
		targetDataPropName = (String[]) ((ArrayList) list.get(1)).toArray(new String[0]);

		//Get all object properties
		list = ontParse.listAllObjectRelationsURI(m_target, targetBaseURI);
		targetObjPropNum = ((Integer) list.get(0)).intValue();
		targetObjPropName = new String[targetObjPropNum];
		targetObjPropName = (String[]) ((ArrayList) list.get(1)).toArray(new String[0]);

		//Get all properties
		targetPropNum = targetDataPropNum + targetObjPropNum;
		targetPropName = new String[targetPropNum];
		for (int i = 0; i < targetDataPropNum; i++) {
			targetPropName[i] = targetDataPropName[i];
		}
		for (int i = 0; i < targetObjPropNum; i++) {
			targetPropName[i + targetDataPropNum] = targetObjPropName[i];
		}

		//get all instances
		list = ontParse.listAllInstances(m_target);
		targetInsNum = ((Integer) list.get(0)).intValue();
		targetInsName = new String[targetInsNum];
		targetInsName = (String[]) ((ArrayList) list.get(1)).toArray(new String[0]);
	}

	public void run() {
		//匹配计算
		long start = System.currentTimeMillis();//开始计时 
		ontMatch();
		long end = System.currentTimeMillis();//结束计时
		long costtime = end - start;//统计算法时间
		System.out.println("简单文本匹配算法时间：" + (double) costtime / 1000. + "秒");
		//--------------------------------------------------------
		//显示映射结果
		showResult(true);
		//保存映射结果
		saveResult();
	}

	public double[][] getOntLiteralMatching(int sNum, String[] sName, int tNum,
			String[] tName) {
		int i, j;
		double[][] sim = new double[sNum][tNum];

		//Mapping concepts
		for (i = 0; i < sNum; i++) {
			for (j = 0; j < tNum; j++) {
				sim[i][j] = new StrEDSim().getNormEDSim(sName[i], tName[j]);
			}
		}
		return sim;
	}

	public void ontMatch() {
		//匹配概念
		double[][] simMxConcept = new double[sourceConceptNum][targetConceptNum];
		simMxConcept = getOntLiteralMatching(sourceConceptNum,
				sourceConceptName, targetConceptNum, targetConceptName);
		// 匹配DatatypeProperty
		double[][] simMxDataProp = new double[sourceDataPropNum][targetDataPropNum];
		simMxDataProp = getOntLiteralMatching(sourceDataPropNum,
				sourceDataPropName, targetDataPropNum, targetDataPropName);
		//匹配ObjectProperty
		double[][] simMxObjProp = new double[sourceObjPropNum][targetObjPropNum];
		simMxObjProp = getOntLiteralMatching(sourceObjPropNum,
				sourceObjPropName, targetObjPropNum, targetObjPropName);

		//结果过滤，用简单的方法
		simMxConcept = new SimpleFilter().maxValueFilter(sourceConceptNum,
				targetConceptNum, simMxConcept, simThreshold);
		simMxDataProp = new SimpleFilter().maxValueFilter(sourceDataPropNum,
				targetDataPropNum, simMxDataProp, simThreshold);
		simMxObjProp = new SimpleFilter().maxValueFilter(sourceObjPropNum,
				targetObjPropNum, simMxObjProp, simThreshold);

		//根据这些相似矩阵，生成映射结果
		mappingResult = new MapRecord[Math.max(sourceConceptNum,
				targetConceptNum)
				+ Math.max(sourceDataPropNum, targetDataPropNum)
				+ Math.max(sourceObjPropNum, targetObjPropNum)];
		generateMapping(simMxConcept, simMxDataProp, simMxObjProp);
	}

	/**********************
	 * 由于这里并不一定只限制1-1的映射，所以只要相似矩阵中的元素不为0，
	 *就认为是符合要求的映射
	 *********************/
	public void generateMapping(double[][] simMxC, double[][] simMxDP,
			double[][] simMxOp) {
		int cMappingNum, dpMappingNum, opMappingNum;//分别记录几种映射的数目

		mappingNum = 0;
		//概念映射结果
		cMappingNum = 0;
		for (int i = 0; i < sourceConceptNum; i++)
			for (int j = 0; j < targetConceptNum; j++) {
				if (simMxC[i][j] >= simThreshold) {
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
		//DatatypeProperty映射结果
		opMappingNum = 0;
		for (int i = 0; i < sourceDataPropNum; i++)
			for (int j = 0; j < targetDataPropNum; j++) {
				if (simMxDP[i][j] >= simThreshold) {
					mappingResult[mappingNum] = new MapRecord();
					mappingResult[mappingNum].sourceLabel = new String(
							sourceDataPropName[i]);
					mappingResult[mappingNum].targetLabel = new String(
							targetDataPropName[j]);
					mappingResult[mappingNum].similarity = simMxDP[i][j];
					mappingResult[mappingNum].relationType = EQUALITY;
					mappingNum++;
					opMappingNum++;
				}
			}
		//ObjectProperty映射结果
		dpMappingNum = 0;
		for (int i = 0; i < sourceObjPropNum; i++)
			for (int j = 0; j < targetObjPropNum; j++) {
				if (simMxOp[i][j] >= simThreshold) {
					mappingResult[mappingNum] = new MapRecord();
					mappingResult[mappingNum].sourceLabel = new String(
							sourceObjPropName[i]);
					mappingResult[mappingNum].targetLabel = new String(
							targetObjPropName[j]);
					mappingResult[mappingNum].similarity = simMxOp[i][j];
					mappingResult[mappingNum].relationType = EQUALITY;
					mappingNum++;
					dpMappingNum++;
				}
			}
	}

	public void showResult(boolean flag) {
		if (!flag)
			return;
		for (int i = 0; i < mappingNum; i++) {
			mappingResult[i].show();
		}
	}

	/**********************
	 * 将结果以两种方式写入文件：
	 * 1.普通文本文件
	 * 2.xml文件
	 * 目标文件夹是target本体的目录
	 *********************/
	public void saveResult() {
		//普通文本文件的方式
		if (lilyFileName.length() == 0) {
			lilyFileName = "lilyResult";
		}
		new MappingFile().save2txt(sourceOntFile, targetOntFile, mappingNum,
				mappingResult, lilyFileName + ".txt");
		//XML文件的方式

	}

	/**********************
	 * 评估映射结果：
	 * 输入映射结果给评估类
	 * 通过和基准结果比较得到结果
	 *********************/
//	public void evaluate() {
//		ArrayList list = new ArrayList();
//		MapRecord[] refMapResult = null;
//		int refMapNum = 0;
//
//		//读出标准结果
//		try {
//			list = new MappingFile().read4xml(refalignFile);
//			refMapNum = ((Integer) list.get(0)).intValue();
//			refMapResult = new MapRecord[refMapNum];
//			refMapResult = (MapRecord[]) ((ArrayList) list.get(1)).toArray(new MapRecord[0]);
//		} catch (MalformedURLException e) {
//			System.out.println("Can't open refalign result file!"
//					+ e.toString());
//		} catch (DocumentException e) {
//			System.out.println("Can't open refalign result file!"
//					+ e.toString());
//		}
//		//输入给评估类
//		list = new EvaluateMapping().getEvaluation(refMapNum, refMapResult,
//				mappingNum, mappingResult);
//	}
}
