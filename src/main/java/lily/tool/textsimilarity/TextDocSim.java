/************************************************
 * Source code information
 * -----------------------
 * Original author	 Peng Wang, School of Computer Science & Eng., Southeast University
 * Author email      pwangseu@gmail.com
 * Web               http://ontomapping.googlepages.com
 * Created			 2007-6-1
 * Filename          TextDocSim.java
 * Version           2.0
 * <p/>
 * Last modified on  2007-6-1
 * by  Peng Wang
 * -----------------------
 * Functions describe:
 * 计算本体文本向量的相似度。
 * 在这个专门的类中进行处理是为了整个框架的清晰性
 ***********************************************/
package lily.tool.textsimilarity;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import lily.tool.datastructure.GraphElmSim;
import lily.tool.datastructure.MultiWord;
import lily.tool.datastructure.TextDes;
import lily.tool.datastructure.Word;
import lily.tool.filter.SimpleFilter;
import lily.tool.parallelcompute.parfor;
import lily.tool.parameters.ParamStore;
import lily.tool.strsimilarity.StrEDSim;

/*********************
 * Class information
 * -------------------
 * @author Peng Wang
 * @date 2007-6-1
 *
 * describe:
 * 文本相似度计算
 ********************/
public class TextDocSim {
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

    // 文本描述信息
    public TextDes[] sourceCnptTextDes;

    public TextDes[] sourcePropTextDes;

    public TextDes[] sourceInsTextDes;

    public TextDes[] targetCnptTextDes;

    public TextDes[] targetPropTextDes;

    public TextDes[] targetInsTextDes;

    public TextDes[] sCBasicDes;// 概念基本

    public TextDes[] sCHSubDes;// 层次

    public TextDes[] sCHSupDes;

    public TextDes[] sCHSblDes;

    public TextDes[] sCHDsjDes;

    public TextDes[] sCHCmpDes;

    public TextDes[] sCPDmnDes;// 属性

    public TextDes[] sCPRngDes;

    public TextDes[] sCInsDes;// 实例

    public TextDes[] sPBasicDes;// 属性基本

    public TextDes[] sPHSubDes;// 层次

    public TextDes[] sPHSupDes;

    public TextDes[] sPHSblDes;

    public TextDes[] sPFDmnDes;// 功能

    public TextDes[] sPFRngDes;

    public TextDes[] sPFChrDes;

    public TextDes[] sPIDmnDes;// 实例

    public TextDes[] sPIRngDes;

    public TextDes[] sDPBasicDes;// Datatype属性基本

    public TextDes[] sDPHSubDes;// 层次

    public TextDes[] sDPHSupDes;

    public TextDes[] sDPHSblDes;

    public TextDes[] sDPFDmnDes;// 功能

    public TextDes[] sDPFRngDes;

    public TextDes[] sDPFChrDes;

    public TextDes[] sDPIDmnDes;// 实例

    public TextDes[] sDPIRngDes;

    public TextDes[] sOPBasicDes;// Object属性基本

    public TextDes[] sOPHSubDes;// 层次

    public TextDes[] sOPHSupDes;

    public TextDes[] sOPHSblDes;

    public TextDes[] sOPFDmnDes;// 功能

    public TextDes[] sOPFRngDes;

    public TextDes[] sOPFChrDes;

    public TextDes[] sOPIDmnDes;// 实例

    public TextDes[] sOPIRngDes;

    public TextDes[] tCBasicDes;

    public TextDes[] tCHSubDes;

    public TextDes[] tCHSupDes;

    public TextDes[] tCHSblDes;

    public TextDes[] tCHDsjDes;

    public TextDes[] tCHCmpDes;

    public TextDes[] tCPDmnDes;

    public TextDes[] tCPRngDes;

    public TextDes[] tCInsDes;

    public TextDes[] tDPBasicDes;

    public TextDes[] tPBasicDes;// 属性基本

    public TextDes[] tPHSubDes;

    public TextDes[] tPHSupDes;

    public TextDes[] tPHSblDes;

    public TextDes[] tPFDmnDes;

    public TextDes[] tPFRngDes;

    public TextDes[] tPFChrDes;

    public TextDes[] tPIDmnDes;

    public TextDes[] tPIRngDes;

    public TextDes[] tDPHSubDes;

    public TextDes[] tDPHSupDes;

    public TextDes[] tDPHSblDes;

    public TextDes[] tDPFDmnDes;

    public TextDes[] tDPFRngDes;

    public TextDes[] tDPFChrDes;

    public TextDes[] tDPIDmnDes;

    public TextDes[] tDPIRngDes;

    public TextDes[] tOPBasicDes;

    public TextDes[] tOPHSubDes;

    public TextDes[] tOPHSupDes;

    public TextDes[] tOPHSblDes;

    public TextDes[] tOPFDmnDes;

    public TextDes[] tOPFRngDes;

    public TextDes[] tOPFChrDes;

    public TextDes[] tOPIDmnDes;

    public TextDes[] tOPIRngDes;

    // 编辑距离相似度阀值
    private double edThreshold = 0.85;

    //相似度
    public double[][] cSimMatrix;

    public double[][] pSimMatrix;

    public double[][] dpSimMatrix;

    public double[][] opSimMatrix;

    public double[][] iSimMatrix;

    //进度条
    public int[] pbBarValue;

    /****************
     * 类的主入口
     ****************/
    // @SuppressWarnings("unchecked")
    public ArrayList getOntTextSim(ArrayList paraList, boolean disctinctOD) {
        ArrayList result = new ArrayList();

		/*解析参数*/
        unPackPara(paraList);
		
		/*读取参数文件*/
        loadConfigFile();
		
		/*整理文本*/
        processDesText();
        pbBarValue[2] = 25; //进度条数值
		/*概念相似*/
        getCnptTextSim(true);
        pbBarValue[2] = 50; //进度条数值
		/*属性相似*/
        getPropTextSim(disctinctOD, true);
        pbBarValue[2] = 60; //进度条数值
		/*实例相似*/
        getInsTextSim();
        pbBarValue[2] = 65; //进度条数值

        result.add(0, cSimMatrix);
        result.add(1, iSimMatrix);
        if (disctinctOD) {
            result.add(2, dpSimMatrix);
            result.add(3, opSimMatrix);
        } else {
            result.add(2, pSimMatrix);
        }

        return result;
    }

    /****************
     * 整理文本
     ****************/
    public void processDesText() {
		/*整理source文本*/
        for (int i = 0; i < sourceConceptNum; i++) {
            ArrayList desList = new ArrayList();

            /**************概念描述***************/
			/*自身描述*/
            sCBasicDes[i] = new TextDes();
            sCBasicDes[i].name = sourceConceptName[i];
            desList = (ArrayList) sourceCnptTextDes[i].text.get(0);
            sCBasicDes[i].text = desList;
			
			/*层次描述*/
            desList = (ArrayList) sourceCnptTextDes[i].text.get(1);
			/*subClass*/
            sCHSubDes[i] = new TextDes();
            sCHSubDes[i].name = sourceConceptName[i];
            sCHSubDes[i].text = ((TextDes) desList.get(0)).text;
			
			/*superClass*/
            sCHSupDes[i] = new TextDes();
            sCHSupDes[i].name = sourceConceptName[i];
            sCHSupDes[i].text = ((TextDes) desList.get(1)).text;
			/*sibling Class*/
            sCHSblDes[i] = new TextDes();
            sCHSblDes[i].name = sourceConceptName[i];
            sCHSblDes[i].text = ((TextDes) desList.get(2)).text;
			/*disjoint Class*/
            sCHDsjDes[i] = new TextDes();
            sCHDsjDes[i].name = sourceConceptName[i];
            sCHDsjDes[i].text = ((TextDes) desList.get(3)).text;
			/*complementOf Class*/
            sCHCmpDes[i] = new TextDes();
            sCHCmpDes[i].name = sourceConceptName[i];
            sCHCmpDes[i].text = ((TextDes) desList.get(4)).text;
			
			/*附加属性描述*/
            desList = (ArrayList) sourceCnptTextDes[i].text.get(2);
            sCPDmnDes[i] = new TextDes();
            sCPDmnDes[i].name = sourceConceptName[i];
            sCPDmnDes[i].text = ((TextDes) desList.get(0)).text;
            sCPRngDes[i] = new TextDes();
            sCPRngDes[i].name = sourceConceptName[i];
            sCPRngDes[i].text = ((TextDes) desList.get(1)).text;
			
			/*实例描述*/
            desList = (ArrayList) sourceCnptTextDes[i].text.get(3);
            sCInsDes[i] = new TextDes();
            sCInsDes[i].name = sourceConceptName[i];
            sCInsDes[i].text = ((TextDes) desList.get(0)).text;
        }
		
		/*整理target文本*/
        for (int i = 0; i < targetConceptNum; i++) {
            ArrayList desList = new ArrayList();

            /**************概念描述***************/
			/*自身描述*/
            tCBasicDes[i] = new TextDes();
            tCBasicDes[i].name = targetConceptName[i];
            desList = (ArrayList) targetCnptTextDes[i].text.get(0);
            tCBasicDes[i].text = desList;
			
			/*层次描述*/
            desList = (ArrayList) targetCnptTextDes[i].text.get(1);
			/*subClass*/
            tCHSubDes[i] = new TextDes();
            tCHSubDes[i].name = targetConceptName[i];
            tCHSubDes[i].text = ((TextDes) desList.get(0)).text;
			
			/*superClass*/
            tCHSupDes[i] = new TextDes();
            tCHSupDes[i].name = targetConceptName[i];
            tCHSupDes[i].text = ((TextDes) desList.get(1)).text;
			/*sibling Class*/
            tCHSblDes[i] = new TextDes();
            tCHSblDes[i].name = targetConceptName[i];
            tCHSblDes[i].text = ((TextDes) desList.get(2)).text;
			/*disjoint Class*/
            tCHDsjDes[i] = new TextDes();
            tCHDsjDes[i].name = targetConceptName[i];
            tCHDsjDes[i].text = ((TextDes) desList.get(3)).text;
			/*complementOf Class*/
            tCHCmpDes[i] = new TextDes();
            tCHCmpDes[i].name = targetConceptName[i];
            tCHCmpDes[i].text = ((TextDes) desList.get(4)).text;
			
			/*附加属性描述*/
            desList = (ArrayList) targetCnptTextDes[i].text.get(2);
            tCPDmnDes[i] = new TextDes();
            tCPDmnDes[i].name = targetConceptName[i];
            tCPDmnDes[i].text = ((TextDes) desList.get(0)).text;
            tCPRngDes[i] = new TextDes();
            tCPRngDes[i].name = targetConceptName[i];
            tCPRngDes[i].text = ((TextDes) desList.get(1)).text;
			
			/*实例描述*/
            desList = (ArrayList) targetCnptTextDes[i].text.get(3);
            tCInsDes[i] = new TextDes();
            tCInsDes[i].name = targetConceptName[i];
            tCInsDes[i].text = ((TextDes) desList.get(0)).text;
        }

        /**************属性描述***************/
		/*整理source文本*/
        for (int i = 0; i < sourcePropNum; i++) {
            ArrayList desList = new ArrayList();
		
			/*自身描述*/
            sPBasicDes[i] = new TextDes();
            sPBasicDes[i].name = sourcePropName[i];
            desList = (ArrayList) sourcePropTextDes[i].text.get(0);
            sPBasicDes[i].text = desList;
			
			/*层次描述*/
            desList = (ArrayList) sourcePropTextDes[i].text.get(1);
			/*subProperty*/
            sPHSubDes[i] = new TextDes();
            sPHSubDes[i].name = sourcePropName[i];
            sPHSubDes[i].text = ((TextDes) desList.get(0)).text;
			/*superProperty*/
            sPHSupDes[i] = new TextDes();
            sPHSupDes[i].name = sourcePropName[i];
            sPHSupDes[i].text = ((TextDes) desList.get(1)).text;
			/*sibling property*/
            sPHSblDes[i] = new TextDes();
            sPHSblDes[i].name = sourcePropName[i];
            sPHSblDes[i].text = ((TextDes) desList.get(2)).text;
			
			/*作用特征描述*/
            desList = (ArrayList) sourcePropTextDes[i].text.get(2);
			/*Domain*/
            sPFDmnDes[i] = new TextDes();
            sPFDmnDes[i].name = sourcePropName[i];
            sPFDmnDes[i].text = ((TextDes) desList.get(0)).text;
			/*Range*/
            sPFRngDes[i] = new TextDes();
            sPFRngDes[i].name = sourcePropName[i];
            sPFRngDes[i].text = ((TextDes) desList.get(1)).text;
			/*性质*/
            sPFChrDes[i] = new TextDes();
            sPFChrDes[i].name = sourcePropName[i];
            sPFChrDes[i].text = ((TextDes) desList.get(2)).text;
			
			/*实例描述*/
            desList = (ArrayList) sourcePropTextDes[i].text.get(3);
			/*Domain*/
            sPIDmnDes[i] = new TextDes();
            sPIDmnDes[i].name = sourcePropName[i];
            sPIDmnDes[i].text = ((TextDes) desList.get(0)).text;
			/*Range*/
            sPIRngDes[i] = new TextDes();
            sPIRngDes[i].name = sourcePropName[i];
            sPIRngDes[i].text = ((TextDes) desList.get(1)).text;
        }
        //分解为两种类型的描述
        for (int i = 0; i < sourceDataPropNum; i++) {
            ArrayList desList = new ArrayList();
            int pos = -1;
			/*正确位置*/
            for (int j = 0; j < sourcePropNum; j++) {
                if (sourcePropName[j].equals(sourceDataPropName[i])) {
                    pos = j;
                }
            }
			
			/*自身描述*/
            sDPBasicDes[i] = new TextDes();
            sDPBasicDes[i].name = sourceDataPropName[i];
            desList = (ArrayList) sourcePropTextDes[pos].text.get(0);
            sDPBasicDes[i].text = desList;
			
			/*层次描述*/
            desList = (ArrayList) sourcePropTextDes[pos].text.get(1);
			/*subProperty*/
            sDPHSubDes[i] = new TextDes();
            sDPHSubDes[i].name = sourceDataPropName[i];
            sDPHSubDes[i].text = ((TextDes) desList.get(0)).text;
			/*superProperty*/
            sDPHSupDes[i] = new TextDes();
            sDPHSupDes[i].name = sourceDataPropName[i];
            sDPHSupDes[i].text = ((TextDes) desList.get(1)).text;
			/*sibling property*/
            sDPHSblDes[i] = new TextDes();
            sDPHSblDes[i].name = sourceDataPropName[i];
            sDPHSblDes[i].text = ((TextDes) desList.get(2)).text;
			
			/*作用特征描述*/
            desList = (ArrayList) sourcePropTextDes[pos].text.get(2);
			/*Domain*/
            sDPFDmnDes[i] = new TextDes();
            sDPFDmnDes[i].name = sourceDataPropName[i];
            sDPFDmnDes[i].text = ((TextDes) desList.get(0)).text;
			/*Range*/
            sDPFRngDes[i] = new TextDes();
            sDPFRngDes[i].name = sourceDataPropName[i];
            sDPFRngDes[i].text = ((TextDes) desList.get(1)).text;
			/*性质*/
            sDPFChrDes[i] = new TextDes();
            sDPFChrDes[i].name = sourceDataPropName[i];
            sDPFChrDes[i].text = ((TextDes) desList.get(2)).text;
			
			/*实例描述*/
            desList = (ArrayList) sourcePropTextDes[pos].text.get(3);
			/*Domain*/
            sDPIDmnDes[i] = new TextDes();
            sDPIDmnDes[i].name = sourceDataPropName[i];
            sDPIDmnDes[i].text = ((TextDes) desList.get(0)).text;
			/*Range*/
            sDPIRngDes[i] = new TextDes();
            sDPIRngDes[i].name = sourceDataPropName[i];
            sDPIRngDes[i].text = ((TextDes) desList.get(1)).text;
        }
        for (int i = 0; i < sourceObjPropNum; i++) {
            ArrayList desList = new ArrayList();
            int pos = -1;
			/*正确位置*/
            for (int j = 0; j < sourcePropNum; j++) {
                if (sourcePropName[j].equals(sourceObjPropName[i])) {
                    pos = j;
                }
            }
			
			/*自身描述*/
            sOPBasicDes[i] = new TextDes();
            sOPBasicDes[i].name = sourceObjPropName[i];
            desList = (ArrayList) sourcePropTextDes[pos].text.get(0);
            sOPBasicDes[i].text = desList;
			
			/*层次描述*/
            desList = (ArrayList) sourcePropTextDes[pos].text.get(1);
			/*subProperty*/
            sOPHSubDes[i] = new TextDes();
            sOPHSubDes[i].name = sourceObjPropName[i];
            sOPHSubDes[i].text = ((TextDes) desList.get(0)).text;
			/*superProperty*/
            sOPHSupDes[i] = new TextDes();
            sOPHSupDes[i].name = sourceObjPropName[i];
            sOPHSupDes[i].text = ((TextDes) desList.get(1)).text;
			/*sibling property*/
            sOPHSblDes[i] = new TextDes();
            sOPHSblDes[i].name = sourceObjPropName[i];
            sOPHSblDes[i].text = ((TextDes) desList.get(2)).text;
			
			/*作用特征描述*/
            desList = (ArrayList) sourcePropTextDes[pos].text.get(2);
			/*Domain*/
            sOPFDmnDes[i] = new TextDes();
            sOPFDmnDes[i].name = sourceObjPropName[i];
            sOPFDmnDes[i].text = ((TextDes) desList.get(0)).text;
			/*Range*/
            sOPFRngDes[i] = new TextDes();
            sOPFRngDes[i].name = sourceObjPropName[i];
            sOPFRngDes[i].text = ((TextDes) desList.get(1)).text;
			/*性质*/
            sOPFChrDes[i] = new TextDes();
            sOPFChrDes[i].name = sourceObjPropName[i];
            sOPFChrDes[i].text = ((TextDes) desList.get(2)).text;
			
			/*实例描述*/
            desList = (ArrayList) sourcePropTextDes[pos].text.get(3);
			/*Domain*/
            sOPIDmnDes[i] = new TextDes();
            sOPIDmnDes[i].name = sourceObjPropName[i];
            sOPIDmnDes[i].text = ((TextDes) desList.get(0)).text;
			/*Range*/
            sOPIRngDes[i] = new TextDes();
            sOPIRngDes[i].name = sourceObjPropName[i];
            sOPIRngDes[i].text = ((TextDes) desList.get(1)).text;
        }
		
		/*整理target文本*/
        for (int i = 0; i < targetPropNum; i++) {
            ArrayList desList = new ArrayList();
		
			/*自身描述*/
            tPBasicDes[i] = new TextDes();
            tPBasicDes[i].name = targetPropName[i];
            desList = (ArrayList) targetPropTextDes[i].text.get(0);
            tPBasicDes[i].text = desList;
			
			/*层次描述*/
            desList = (ArrayList) targetPropTextDes[i].text.get(1);
			/*subProperty*/
            tPHSubDes[i] = new TextDes();
            tPHSubDes[i].name = targetPropName[i];
            tPHSubDes[i].text = ((TextDes) desList.get(0)).text;
			/*superProperty*/
            tPHSupDes[i] = new TextDes();
            tPHSupDes[i].name = targetPropName[i];
            tPHSupDes[i].text = ((TextDes) desList.get(1)).text;
			/*sibling property*/
            tPHSblDes[i] = new TextDes();
            tPHSblDes[i].name = targetPropName[i];
            tPHSblDes[i].text = ((TextDes) desList.get(2)).text;
			
			/*作用特征描述*/
            desList = (ArrayList) targetPropTextDes[i].text.get(2);
			/*Domain*/
            tPFDmnDes[i] = new TextDes();
            tPFDmnDes[i].name = targetPropName[i];
            tPFDmnDes[i].text = ((TextDes) desList.get(0)).text;
			/*Range*/
            tPFRngDes[i] = new TextDes();
            tPFRngDes[i].name = targetPropName[i];
            tPFRngDes[i].text = ((TextDes) desList.get(1)).text;
			/*性质*/
            tPFChrDes[i] = new TextDes();
            tPFChrDes[i].name = targetPropName[i];
            tPFChrDes[i].text = ((TextDes) desList.get(2)).text;
			
			/*实例描述*/
            desList = (ArrayList) targetPropTextDes[i].text.get(3);
			/*Domain*/
            tPIDmnDes[i] = new TextDes();
            tPIDmnDes[i].name = targetPropName[i];
            tPIDmnDes[i].text = ((TextDes) desList.get(0)).text;
			/*Range*/
            tPIRngDes[i] = new TextDes();
            tPIRngDes[i].name = targetPropName[i];
            tPIRngDes[i].text = ((TextDes) desList.get(1)).text;
        }

        //分解为两种类型的描述
        for (int i = 0; i < targetDataPropNum; i++) {
            ArrayList desList = new ArrayList();
            int pos = -1;
			/*正确位置*/
            for (int j = 0; j < targetPropNum; j++) {
                if (targetPropName[j].equals(targetDataPropName[i])) {
                    pos = j;
                }
            }
			
			/*自身描述*/
            tDPBasicDes[i] = new TextDes();
            tDPBasicDes[i].name = targetDataPropName[i];
            desList = (ArrayList) targetPropTextDes[pos].text.get(0);
            tDPBasicDes[i].text = desList;
			
			/*层次描述*/
            desList = (ArrayList) targetPropTextDes[pos].text.get(1);
			/*subProperty*/
            tDPHSubDes[i] = new TextDes();
            tDPHSubDes[i].name = targetDataPropName[i];
            tDPHSubDes[i].text = ((TextDes) desList.get(0)).text;
			/*superProperty*/
            tDPHSupDes[i] = new TextDes();
            tDPHSupDes[i].name = targetDataPropName[i];
            tDPHSupDes[i].text = ((TextDes) desList.get(1)).text;
			/*sibling property*/
            tDPHSblDes[i] = new TextDes();
            tDPHSblDes[i].name = targetDataPropName[i];
            tDPHSblDes[i].text = ((TextDes) desList.get(2)).text;
			
			/*作用特征描述*/
            desList = (ArrayList) targetPropTextDes[pos].text.get(2);
			/*Domain*/
            tDPFDmnDes[i] = new TextDes();
            tDPFDmnDes[i].name = targetDataPropName[i];
            tDPFDmnDes[i].text = ((TextDes) desList.get(0)).text;
			/*Range*/
            tDPFRngDes[i] = new TextDes();
            tDPFRngDes[i].name = targetDataPropName[i];
            tDPFRngDes[i].text = ((TextDes) desList.get(1)).text;
			/*性质*/
            tDPFChrDes[i] = new TextDes();
            tDPFChrDes[i].name = targetDataPropName[i];
            tDPFChrDes[i].text = ((TextDes) desList.get(2)).text;
			
			/*实例描述*/
            desList = (ArrayList) targetPropTextDes[pos].text.get(3);
			/*Domain*/
            tDPIDmnDes[i] = new TextDes();
            tDPIDmnDes[i].name = targetDataPropName[i];
            tDPIDmnDes[i].text = ((TextDes) desList.get(0)).text;
			/*Range*/
            tDPIRngDes[i] = new TextDes();
            tDPIRngDes[i].name = targetDataPropName[i];
            tDPIRngDes[i].text = ((TextDes) desList.get(1)).text;
        }
        for (int i = 0; i < targetObjPropNum; i++) {
            ArrayList desList = new ArrayList();
            int pos = -1;
			/*正确位置*/
            for (int j = 0; j < targetPropNum; j++) {
                if (targetPropName[j].equals(targetObjPropName[i])) {
                    pos = j;
                }
            }
			
			/*自身描述*/
            tOPBasicDes[i] = new TextDes();
            tOPBasicDes[i].name = targetObjPropName[i];
            desList = (ArrayList) targetPropTextDes[pos].text.get(0);
            tOPBasicDes[i].text = desList;
			
			/*层次描述*/
            desList = (ArrayList) targetPropTextDes[pos].text.get(1);
			/*subProperty*/
            tOPHSubDes[i] = new TextDes();
            tOPHSubDes[i].name = targetObjPropName[i];
            tOPHSubDes[i].text = ((TextDes) desList.get(0)).text;
			/*superProperty*/
            tOPHSupDes[i] = new TextDes();
            tOPHSupDes[i].name = targetObjPropName[i];
            tOPHSupDes[i].text = ((TextDes) desList.get(1)).text;
			/*sibling property*/
            tOPHSblDes[i] = new TextDes();
            tOPHSblDes[i].name = targetObjPropName[i];
            tOPHSblDes[i].text = ((TextDes) desList.get(2)).text;
			
			/*作用特征描述*/
            desList = (ArrayList) targetPropTextDes[pos].text.get(2);
			/*Domain*/
            tOPFDmnDes[i] = new TextDes();
            tOPFDmnDes[i].name = targetObjPropName[i];
            tOPFDmnDes[i].text = ((TextDes) desList.get(0)).text;
			/*Range*/
            tOPFRngDes[i] = new TextDes();
            tOPFRngDes[i].name = targetObjPropName[i];
            tOPFRngDes[i].text = ((TextDes) desList.get(1)).text;
			/*性质*/
            tOPFChrDes[i] = new TextDes();
            tOPFChrDes[i].name = targetObjPropName[i];
            tOPFChrDes[i].text = ((TextDes) desList.get(2)).text;
			
			/*实例描述*/
            desList = (ArrayList) targetPropTextDes[pos].text.get(3);
			/*Domain*/
            tOPIDmnDes[i] = new TextDes();
            tOPIDmnDes[i].name = targetObjPropName[i];
            tOPIDmnDes[i].text = ((TextDes) desList.get(0)).text;
			/*Range*/
            tOPIRngDes[i] = new TextDes();
            tOPIRngDes[i].name = targetObjPropName[i];
            tOPIRngDes[i].text = ((TextDes) desList.get(1)).text;
        }
    }

    /****************
     * 计算概念文本相似
     ****************/
    public void getCnptTextSim(boolean bTurn) {
        double[][] mBasic = new double[sourceConceptNum][targetConceptNum];
        double[][] mSub = new double[sourceConceptNum][targetConceptNum];
        double[][] mSup = new double[sourceConceptNum][targetConceptNum];
        double[][] mSbl = new double[sourceConceptNum][targetConceptNum];
        double[][] mDsj = new double[sourceConceptNum][targetConceptNum];
        double[][] mCmp = new double[sourceConceptNum][targetConceptNum];
        double[][] mDmn = new double[sourceConceptNum][targetConceptNum];
        double[][] mRng = new double[sourceConceptNum][targetConceptNum];
        double[][] mIns = new double[sourceConceptNum][targetConceptNum];
		
		/*权重*/
        double wB = 0.35;
        double wH = 0.25;
        double wP = 0.2;
        double wI = 0.2;

        double wHsub = 0.3;
        double wHsup = 0.3;
        double wHsbl = 0.2;
        double wHdsj = 0.1;
        double wHcmp = 0.1;

        double wPd = 0.5;
        double wPr = 0.5;

        if (bTurn) {
			/*引入编辑距离处理Vector中的相近词*/
            tuneDesDocbyED_np(sCBasicDes, sourceConceptNum, tCBasicDes, targetConceptNum);
            tuneDesDocbyED_np(sCHSubDes, sourceConceptNum, tCHSubDes, targetConceptNum);
            tuneDesDocbyED_np(sCHSupDes, sourceConceptNum, tCHSupDes, targetConceptNum);
            tuneDesDocbyED_np(sCHSblDes, sourceConceptNum, tCHSblDes, targetConceptNum);
            tuneDesDocbyED_np(sCHDsjDes, sourceConceptNum, tCHDsjDes, targetConceptNum);
            tuneDesDocbyED_np(sCHCmpDes, sourceConceptNum, tCHCmpDes, targetConceptNum);
            tuneDesDocbyED_np(sCPDmnDes, sourceConceptNum, tCPDmnDes, targetConceptNum);
            tuneDesDocbyED_np(sCPRngDes, sourceConceptNum, tCPRngDes, targetConceptNum);
            tuneDesDocbyED_np(sCInsDes, sourceConceptNum, tCInsDes, targetConceptNum);
        }
		
		/*自身描述*/
        mBasic = computeTFIDFSim_np(sCBasicDes, sourceConceptNum, tCBasicDes, targetConceptNum);
		/*层次描述*/
        mSub = computeTFIDFSim_np(sCHSubDes, sourceConceptNum, tCHSubDes, targetConceptNum);
        mSup = computeTFIDFSim_np(sCHSupDes, sourceConceptNum, tCHSupDes, targetConceptNum);
        mSbl = computeTFIDFSim_np(sCHSblDes, sourceConceptNum, tCHSblDes, targetConceptNum);
        mDsj = computeTFIDFSim_np(sCHDsjDes, sourceConceptNum, tCHDsjDes, targetConceptNum);
        mCmp = computeTFIDFSim_np(sCHCmpDes, sourceConceptNum, tCHCmpDes, targetConceptNum);
		/*附加属性描述*/
        mDmn = computeTFIDFSim_np(sCPDmnDes, sourceConceptNum, tCPDmnDes, targetConceptNum);
        mRng = computeTFIDFSim_np(sCPRngDes, sourceConceptNum, tCPRngDes, targetConceptNum);
		/*实例描述*/
        mIns = computeTFIDFSim_np(sCInsDes, sourceConceptNum, tCInsDes, targetConceptNum);
		
		/*合并结果*/
        double wB0, wH0, wP0, wI0, wHsub0, wHsup0, wHsbl0, wHdsj0, wHcmp0, wPd0, wPr0;
        for (int i = 0; i < sourceConceptNum; i++) {
            for (int j = 0; j < targetConceptNum; j++) {
				
				/*重新计算实际的权重*/
                /*****启发式权重计算规则,思想类似于Sigmoid函数，但效率更高*****/
				/*1.层次权重*/
                if (Math.abs(mSub[i][j] - (-1.0)) < 0.00001) {
                    wHsub0 = 0.0;
                } else {
                    wHsub0 = wHsub;
                }
                if (Math.abs(mSup[i][j] - (-1.0)) < 0.00001) {
                    wHsup0 = 0.0;
                } else {
                    wHsup0 = wHsup;
                }
                if (Math.abs(mSbl[i][j] - (-1.0)) < 0.00001) {
                    wHsbl0 = 0.0;
                } else {
                    wHsbl0 = wHsbl;
                }
                if (Math.abs(mDsj[i][j] - (-1.0)) < 0.00001) {
                    wHdsj0 = 0.0;
                } else {
                    wHdsj0 = wHdsj;
                }
                if (Math.abs(mCmp[i][j] - (-1.0)) < 0.00001) {
                    wHcmp0 = 0.0;
                } else {
                    wHcmp0 = wHcmp;
                }
                double wHTotal = wHsub0 + wHsup0 + wHsbl0 + wHdsj0 + wHcmp0;
                if (wHTotal > 0.0) {
                    wHsub0 = wHsub0 / wHTotal;
                    wHsup0 = wHsup0 / wHTotal;
                    wHsbl0 = wHsbl0 / wHTotal;
                    wHdsj0 = wHdsj0 / wHTotal;
                    wHcmp0 = wHcmp0 / wHTotal;
                }
				/*2.属性权重*/
                if (Math.abs(mDmn[i][j] - (-1.0)) < 0.00001) {
                    wPd0 = 0.0;
                } else {
                    wPd0 = wPd;
                }
                if (Math.abs(mRng[i][j] - (-1.0)) < 0.00001) {
                    wPr0 = 0.0;
                } else {
                    wPr0 = wPr;
                }
                double wPTotal = wPd0 + wPr0;
                if (wPTotal > 0.0) {
                    wPd0 = wPd0 / wPTotal;
                    wPr0 = wPr0 / wPTotal;
                }
				/*3.总权重*/
                if (Math.abs(mBasic[i][j] - (-1.0)) < 0.00001) {
                    wB0 = 0.0;
                } else {
                    wB0 = wB;
                }
                if (Math.abs(wHTotal) < 0.00001) {
                    wH0 = 0.0;
                } else {
                    wH0 = wH;
                }
                if (Math.abs(wPTotal) < 0.00001) {
                    wP0 = 0.0;
                } else {
                    wP0 = wP;
                }
                if (Math.abs(mIns[i][j] - (-1.0)) < 0.00001) {
                    wI0 = 0.0;
                } else {
                    wI0 = wI;
                }
                double wTotal = wB0 + wH0 + wP0 + wI0;
                if (wTotal > 0.0) {
                    wB0 = wB0 / wTotal;
                    wH0 = wH0 / wTotal;
                    wP0 = wP0 / wTotal;
                    wI0 = wI0 / wTotal;
                }

                if (Math.abs(wTotal) < 0.00001) {
                    cSimMatrix[i][j] = 0.0;
                } else {
                    cSimMatrix[i][j] = wB0 * mBasic[i][j]
                            + wH0 * (wHsub0 * mSub[i][j] + wHsup0 * mSup[i][j] + wHsbl0 * mSbl[i][j] + wHdsj0 * mDsj[i][j] + wHcmp0 * mCmp[i][j])
                            + wP0 * (wPd0 * mDmn[i][j] + wPr0 * mRng[i][j])
                            + wI0 * mIns[i][j];
                }

                /******Sigmoid方法*******/
//				/*1.层次权重*/
//				if (mSub[i][j]>0.00001) {wHsub0=sigmoid(mSub[i][j]);} else {wHsub0=0.0;}
//				if (mSup[i][j]>0.00001) {wHsup0=sigmoid(mSup[i][j]);} else {wHsup0=0.0;}
//				if (mSbl[i][j]>0.00001) {wHsbl0=sigmoid(mSbl[i][j]);} else {wHsbl0=0.0;}
//				if (mDsj[i][j]>0.00001) {wHdsj0=sigmoid(mDsj[i][j]);} else {wHdsj0=0.0;}
//				if (mCmp[i][j]>0.00001) {wHcmp0=sigmoid(mCmp[i][j]);} else {wHcmp0=0.0;}
//				double wHTotal=(wHsub0+wHsup0+wHsbl0+wHdsj0+wHcmp0)*0.85;
//				if (wHTotal>0.0){
//					wHsub0=wHsub0/wHTotal;
//					wHsup0=wHsup0/wHTotal;
//					wHsbl0=wHsbl0/wHTotal;
//					wHdsj0=wHdsj0/wHTotal;
//					wHcmp0=wHcmp0/wHTotal;
//				}
//				wH0=wHTotal;
//
//				/*2.属性权重*/
//				if (mDmn[i][j]>0.00001) {wPd0=sigmoid(mDmn[i][j]);} else {wPd0=0.0;}
//				if (mRng[i][j]>0.00001) {wPr0=sigmoid(mRng[i][j]);} else {wPr0=0.0;}
//				double wPTotal=(wPd0+wPr0)*0.7;
//				if (wPTotal>0.0){
//					wPd0=wPd0/wPTotal;
//					wPr0=wPr0/wPTotal;
//				}
//				wP0=wPTotal;
//				
//				/*3.实例权重*/
//				if (mIns[i][j]>0.00001) {wI0=sigmoid(mIns[i][j])*0.7;} else {wI0=0.0;}
//
//				/*4.基本权重*/
//				if (mBasic[i][j]>0.00001) {wB0=sigmoid(mBasic[i][j]);} else {wB0=0.0;}
//				
//				/*5.总权重*/
//				double wTotal=wB0+wH0+wP0+wI0;
//				if (wTotal>0.0){
//					wB0=wB0/wTotal;
//					wH0=wH0/wTotal;
//					wP0=wP0/wTotal;
//					wI0=wI0/wTotal;
//				}
//				
//				if (wTotal<0.0001){
//					cSimMatrix[i][j]=0.0;
//				}
//				else {
//					cSimMatrix[i][j]=(wB0*mBasic[i][j]
//					    			 +wH0*(wHsub0*mSub[i][j]+wHsup0*mSup[i][j]+wHsbl0*mSbl[i][j]+wHdsj0*mDsj[i][j]+wHcmp0*mCmp[i][j])
//						    		 +wP0*(wPd0*mDmn[i][j]+wPr0*mRng[i][j])
//							    	 +wI0*mIns[i][j]);
//				}
            }
        }
    }

    /****************
     * 计算属性文本相似
     ****************/
    public void getPropTextSim(boolean flag, boolean bTurn) {
		/*权重*/
        double wB = 0.3;
        double wH = 0.1;
        double wF = 0.3;
        double wI = 0.3;

        double wHsub = 0.3;
        double wHsup = 0.3;
        double wHsbl = 0.4;

        double wFd = 0.45;
        double wFr = 0.45;
        double wFc = 0.1;

        double wId = 0.5;
        double wIr = 0.5;

        if (flag) {
            double[][] mDBasic = new double[sourceDataPropNum][targetDataPropNum];
            double[][] mDSub = new double[sourceDataPropNum][targetDataPropNum];
            double[][] mDSup = new double[sourceDataPropNum][targetDataPropNum];
            double[][] mDSbl = new double[sourceDataPropNum][targetDataPropNum];
            double[][] mDDmn = new double[sourceDataPropNum][targetDataPropNum];
            double[][] mDRng = new double[sourceDataPropNum][targetDataPropNum];
            double[][] mDChr = new double[sourceDataPropNum][targetDataPropNum];
            double[][] mDIDmn = new double[sourceDataPropNum][targetDataPropNum];
            double[][] mDIRng = new double[sourceDataPropNum][targetDataPropNum];

            double[][] mOBasic = new double[sourceObjPropNum][targetObjPropNum];
            double[][] mOSub = new double[sourceObjPropNum][targetObjPropNum];
            double[][] mOSup = new double[sourceObjPropNum][targetObjPropNum];
            double[][] mOSbl = new double[sourceObjPropNum][targetObjPropNum];
            double[][] mODmn = new double[sourceObjPropNum][targetObjPropNum];
            double[][] mORng = new double[sourceObjPropNum][targetObjPropNum];
            double[][] mOChr = new double[sourceObjPropNum][targetObjPropNum];
            double[][] mOIDmn = new double[sourceObjPropNum][targetObjPropNum];
            double[][] mOIRng = new double[sourceObjPropNum][targetObjPropNum];

            /**********Datatype Property***********/
            if (bTurn) {
				/*引入编辑距离处理Vector中的相近词*/
                tuneDesDocbyED_np(sDPBasicDes, sourceDataPropNum, tDPBasicDes, targetDataPropNum);
                tuneDesDocbyED_np(sDPHSubDes, sourceDataPropNum, tDPHSubDes, targetDataPropNum);
                tuneDesDocbyED_np(sDPHSupDes, sourceDataPropNum, tDPHSupDes, targetDataPropNum);
                tuneDesDocbyED_np(sDPHSblDes, sourceDataPropNum, tDPHSblDes, targetDataPropNum);
                tuneDesDocbyED_np(sDPFDmnDes, sourceDataPropNum, tDPFDmnDes, targetDataPropNum);
                tuneDesDocbyED_np(sDPFRngDes, sourceDataPropNum, tDPFRngDes, targetDataPropNum);
                tuneDesDocbyED_np(sDPFChrDes, sourceDataPropNum, tDPFChrDes, targetDataPropNum);
                tuneDesDocbyED_np(sDPIDmnDes, sourceDataPropNum, tDPIDmnDes, targetDataPropNum);
                tuneDesDocbyED_np(sDPIRngDes, sourceDataPropNum, tDPIRngDes, targetDataPropNum);
            }
			
			/*自身描述*/
            mDBasic = computeTFIDFSim_np(sDPBasicDes, sourceDataPropNum, tDPBasicDes, targetDataPropNum);
			/*层次描述*/
            mDSub = computeTFIDFSim_np(sDPHSubDes, sourceDataPropNum, tDPHSubDes, targetDataPropNum);
            mDSup = computeTFIDFSim_np(sDPHSupDes, sourceDataPropNum, tDPHSupDes, targetDataPropNum);
            mDSbl = computeTFIDFSim_np(sDPHSblDes, sourceDataPropNum, tDPHSblDes, targetDataPropNum);
			/*特征描述*/
            mDDmn = computeTFIDFSim_np(sDPFDmnDes, sourceDataPropNum, tDPFDmnDes, targetDataPropNum);
            mDRng = computeTFIDFSim_np(sDPFRngDes, sourceDataPropNum, tDPFRngDes, targetDataPropNum);
            mDChr = computeTFIDFSim_np(sDPFChrDes, sourceDataPropNum, tDPFChrDes, targetDataPropNum);
			/*实例描述*/
            mDIDmn = computeTFIDFSim_np(sDPIDmnDes, sourceDataPropNum, tDPIDmnDes, targetDataPropNum);
            mDIRng = computeTFIDFSim_np(sDPIRngDes, sourceDataPropNum, tDPIRngDes, targetDataPropNum);
			
			/*合并结果*/
            double wB0, wH0, wF0, wI0, wHsub0, wHsup0, wHsbl0, wFd0, wFr0, wFc0, wId0, wIr0;
            for (int i = 0; i < sourceDataPropNum; i++) {
                for (int j = 0; j < targetDataPropNum; j++) {
					
					/*重新计算实际的权重*/
					/*1.层次权重*/
                    if (Math.abs(mDSub[i][j] - (-1.0)) < 0.00001) {
                        wHsub0 = 0.0;
                    } else {
                        wHsub0 = wHsub;
                    }
                    if (Math.abs(mDSup[i][j] - (-1.0)) < 0.00001) {
                        wHsup0 = 0.0;
                    } else {
                        wHsup0 = wHsup;
                    }
                    if (Math.abs(mDSbl[i][j] - (-1.0)) < 0.00001) {
                        wHsbl0 = 0.0;
                    } else {
                        wHsbl0 = wHsbl;
                    }
                    double wHTotal = wHsub0 + wHsup0 + wHsbl0;
                    if (wHTotal > 0.0) {
                        wHsub0 = wHsub0 / wHTotal;
                        wHsup0 = wHsup0 / wHTotal;
                        wHsbl0 = wHsbl0 / wHTotal;
                    }
					/*2.功能权重*/
                    if (Math.abs(mDDmn[i][j] - (-1.0)) < 0.00001) {
                        wFd0 = 0.0;
                    } else {
                        wFd0 = wFd;
                    }
                    if (Math.abs(mDRng[i][j] - (-1.0)) < 0.00001) {
                        wFr0 = 0.0;
                    } else {
                        wFr0 = wFr;
                    }
                    if (Math.abs(mDChr[i][j] - (-1.0)) < 0.00001) {
                        wFc0 = 0.0;
                    } else {
                        wFc0 = wFc;
                    }
                    double wFTotal = wFd0 + wFr0 + wFc0;
                    if (wFTotal > 0.0) {
                        wFd0 = wFd0 / wFTotal;
                        wFr0 = wFr0 / wFTotal;
                        wFc0 = wFc0 / wFTotal;
                    }
					/*3.实例权重*/
                    if (Math.abs(mDIDmn[i][j] - (-1.0)) < 0.00001) {
                        wId0 = 0.0;
                    } else {
                        wId0 = wId;
                    }
                    if (Math.abs(mDIRng[i][j] - (-1.0)) < 0.00001) {
                        wIr0 = 0.0;
                    } else {
                        wIr0 = wIr;
                    }
                    double wITotal = wId0 + wIr0;
                    if (wITotal > 0.0) {
                        wId0 = wId0 / wITotal;
                        wIr0 = wIr0 / wITotal;
                    }
					/*4.总权重*/
                    if (Math.abs(mDBasic[i][j] - (-1.0)) < 0.00001) {
                        wB0 = 0.0;
                    } else {
                        wB0 = wB;
                    }
                    if (Math.abs(wHTotal) < 0.00001) {
                        wH0 = 0.0;
                    } else {
                        wH0 = wH;
                    }
                    if (Math.abs(wFTotal) < 0.00001) {
                        wF0 = 0.0;
                    } else {
                        wF0 = wF;
                    }
                    if (Math.abs(wITotal) < 0.00001) {
                        wI0 = 0.0;
                    } else {
                        wI0 = wI;
                    }
                    double wTotal = wB0 + wH0 + wF0 + wI0;
                    if (wTotal > 0.0) {
                        wB0 = wB0 / wTotal;
                        wH0 = wH0 / wTotal;
                        wF0 = wF0 / wTotal;
                        wI0 = wI0 / wTotal;
                    }

                    if (Math.abs(wTotal) < 0.00001) {
                        dpSimMatrix[i][j] = 0.0;
                    } else {
                        dpSimMatrix[i][j] = wB0 * mDBasic[i][j]
                                + wH0 * (wHsub0 * mDSub[i][j] + wHsup0 * mDSup[i][j] + wHsbl0 * mDSbl[i][j])
                                + wF0 * (wFd0 * mDDmn[i][j] + wFr0 * mDRng[i][j] + wFc0 * mDChr[i][j])
                                + wI0 * (wId0 * mDIDmn[i][j] + wIr0 * mDIRng[i][j]);
                    }
                }
            }

            /**********Object Property***********/
			/*引入编辑距离处理Vector中的相近词*/
            tuneDesDocbyED_np(sOPBasicDes, sourceObjPropNum, tOPBasicDes, targetObjPropNum);
            tuneDesDocbyED_np(sOPHSubDes, sourceObjPropNum, tOPHSubDes, targetObjPropNum);
            tuneDesDocbyED_np(sOPHSupDes, sourceObjPropNum, tOPHSupDes, targetObjPropNum);
            tuneDesDocbyED_np(sOPHSblDes, sourceObjPropNum, tOPHSblDes, targetObjPropNum);
            tuneDesDocbyED_np(sOPFDmnDes, sourceObjPropNum, tOPFDmnDes, targetObjPropNum);
            tuneDesDocbyED_np(sOPFRngDes, sourceObjPropNum, tOPFRngDes, targetObjPropNum);
            tuneDesDocbyED_np(sOPFChrDes, sourceObjPropNum, tOPFChrDes, targetObjPropNum);
            tuneDesDocbyED_np(sOPIDmnDes, sourceObjPropNum, tOPIDmnDes, targetObjPropNum);
            tuneDesDocbyED_np(sOPIRngDes, sourceObjPropNum, tOPIRngDes, targetObjPropNum);
			
			/*自身描述*/
            mOBasic = computeTFIDFSim_np(sOPBasicDes, sourceObjPropNum, tOPBasicDes, targetObjPropNum);
			/*层次描述*/
            mOSub = computeTFIDFSim_np(sOPHSubDes, sourceObjPropNum, tOPHSubDes, targetObjPropNum);
            mOSup = computeTFIDFSim_np(sOPHSupDes, sourceObjPropNum, tOPHSupDes, targetObjPropNum);
            mOSbl = computeTFIDFSim_np(sOPHSblDes, sourceObjPropNum, tOPHSblDes, targetObjPropNum);
			/*特征描述*/
            mODmn = computeTFIDFSim_np(sOPFDmnDes, sourceObjPropNum, tOPFDmnDes, targetObjPropNum);
            mORng = computeTFIDFSim_np(sOPFRngDes, sourceObjPropNum, tOPFRngDes, targetObjPropNum);
            mOChr = computeTFIDFSim_np(sOPFChrDes, sourceObjPropNum, tOPFChrDes, targetObjPropNum);
			/*实例描述*/
            mOIDmn = computeTFIDFSim_np(sOPIDmnDes, sourceObjPropNum, tOPIDmnDes, targetObjPropNum);
            mOIRng = computeTFIDFSim_np(sOPIRngDes, sourceObjPropNum, tOPIRngDes, targetObjPropNum);
			
			/*合并结果*/
            double wB1, wH1, wF1, wI1, wHsub1, wHsup1, wHsbl1, wFd1, wFr1, wFc1, wId1, wIr1;
            for (int i = 0; i < sourceObjPropNum; i++) {
                for (int j = 0; j < targetObjPropNum; j++) {
					
					/*重新计算实际的权重*/
					/*1.层次权重*/
                    if (Math.abs(mOSub[i][j] - (-1.0)) < 0.00001) {
                        wHsub1 = 0.0;
                    } else {
                        wHsub1 = wHsub;
                    }
                    if (Math.abs(mOSup[i][j] - (-1.0)) < 0.00001) {
                        wHsup1 = 0.0;
                    } else {
                        wHsup1 = wHsup;
                    }
                    if (Math.abs(mOSbl[i][j] - (-1.0)) < 0.00001) {
                        wHsbl1 = 0.0;
                    } else {
                        wHsbl1 = wHsbl;
                    }
                    double wHTotal = wHsub1 + wHsup1 + wHsbl1;
                    if (wHTotal > 0.0) {
                        wHsub1 = wHsub1 / wHTotal;
                        wHsup1 = wHsup1 / wHTotal;
                        wHsbl1 = wHsbl1 / wHTotal;
                    }
					/*2.功能权重*/
                    if (Math.abs(mODmn[i][j] - (-1.0)) < 0.00001) {
                        wFd1 = 0.0;
                    } else {
                        wFd1 = wFd;
                    }
                    if (Math.abs(mORng[i][j] - (-1.0)) < 0.00001) {
                        wFr1 = 0.0;
                    } else {
                        wFr1 = wFr;
                    }
                    if (Math.abs(mOChr[i][j] - (-1.0)) < 0.00001) {
                        wFc1 = 0.0;
                    } else {
                        wFc1 = wFc;
                    }
                    double wFTotal = wFd1 + wFr1 + wFc1;
                    if (wFTotal > 0.0) {
                        wFd1 = wFd1 / wFTotal;
                        wFr1 = wFr1 / wFTotal;
                        wFc1 = wFc1 / wFTotal;
                    }
					/*3.实例权重*/
                    if (Math.abs(mOIDmn[i][j] - (-1.0)) < 0.00001) {
                        wId1 = 0.0;
                    } else {
                        wId1 = wId;
                    }
                    if (Math.abs(mOIRng[i][j] - (-1.0)) < 0.00001) {
                        wIr1 = 0.0;
                    } else {
                        wIr1 = wIr;
                    }
                    double wITotal = wId1 + wIr1;
                    if (wITotal > 0.0) {
                        wId1 = wId1 / wITotal;
                        wIr1 = wIr1 / wITotal;
                    }
					/*4.总权重*/
                    if (Math.abs(mOBasic[i][j] - (-1.0)) < 0.00001) {
                        wB1 = 0.0;
                    } else {
                        wB1 = wB;
                    }
                    if (Math.abs(wHTotal) < 0.00001) {
                        wH1 = 0.0;
                    } else {
                        wH1 = wH;
                    }
                    if (Math.abs(wFTotal) < 0.00001) {
                        wF1 = 0.0;
                    } else {
                        wF1 = wF;
                    }
                    if (Math.abs(wITotal) < 0.00001) {
                        wI1 = 0.0;
                    } else {
                        wI1 = wI;
                    }
                    double wTotal = wB1 + wH1 + wF1 + wI1;
                    if (wTotal > 0.0) {
                        wB1 = wB1 / wTotal;
                        wH1 = wH1 / wTotal;
                        wF1 = wF1 / wTotal;
                        wI1 = wI1 / wTotal;
                    }

                    if (Math.abs(wTotal) < 0.00001) {
                        opSimMatrix[i][j] = 0.0;
                    } else {
                        opSimMatrix[i][j] = wB1 * mOBasic[i][j]
                                + wH1 * (wHsub1 * mOSub[i][j] + wHsup1 * mOSup[i][j] + wHsbl1 * mOSbl[i][j])
                                + wF1 * (wFd1 * mODmn[i][j] + wFr1 * mORng[i][j] + wFc1 * mOChr[i][j])
                                + wI1 * (wId1 * mOIDmn[i][j] + wIr1 * mOIRng[i][j]);
                    }
                }
            }
        } else {
            double[][] mDBasic = new double[sourcePropNum][targetPropNum];
            double[][] mDSub = new double[sourcePropNum][targetPropNum];
            double[][] mDSup = new double[sourcePropNum][targetPropNum];
            double[][] mDSbl = new double[sourcePropNum][targetPropNum];
            double[][] mDDmn = new double[sourcePropNum][targetPropNum];
            double[][] mDRng = new double[sourcePropNum][targetPropNum];
            double[][] mDChr = new double[sourcePropNum][targetPropNum];
            double[][] mDIDmn = new double[sourcePropNum][targetPropNum];
            double[][] mDIRng = new double[sourcePropNum][targetPropNum];
			
			/*引入编辑距离处理Vector中的相近词*/
            tuneDesDocbyED_np(sPBasicDes, sourcePropNum, tPBasicDes, targetPropNum);
            tuneDesDocbyED_np(sPHSubDes, sourcePropNum, tPHSubDes, targetPropNum);
            tuneDesDocbyED_np(sPHSupDes, sourcePropNum, tPHSupDes, targetPropNum);
            tuneDesDocbyED_np(sPHSblDes, sourcePropNum, tPHSblDes, targetPropNum);
            tuneDesDocbyED_np(sPFDmnDes, sourcePropNum, tPFDmnDes, targetPropNum);
            tuneDesDocbyED_np(sPFRngDes, sourcePropNum, tPFRngDes, targetPropNum);
            tuneDesDocbyED_np(sPFChrDes, sourcePropNum, tPFChrDes, targetPropNum);
            tuneDesDocbyED_np(sPIDmnDes, sourcePropNum, tPIDmnDes, targetPropNum);
            tuneDesDocbyED_np(sPIRngDes, sourcePropNum, tPIRngDes, targetPropNum);
			
			/*自身描述*/
            mDBasic = computeTFIDFSim_np(sPBasicDes, sourcePropNum, tPBasicDes, targetPropNum);
			/*层次描述*/
            mDSub = computeTFIDFSim_np(sPHSubDes, sourcePropNum, tPHSubDes, targetPropNum);
            mDSup = computeTFIDFSim_np(sPHSupDes, sourcePropNum, tPHSupDes, targetPropNum);
            mDSbl = computeTFIDFSim_np(sPHSblDes, sourcePropNum, tPHSblDes, targetPropNum);
			/*特征描述*/
            mDDmn = computeTFIDFSim_np(sPFDmnDes, sourcePropNum, tPFDmnDes, targetPropNum);
            mDRng = computeTFIDFSim_np(sPFRngDes, sourcePropNum, tPFRngDes, targetPropNum);
            mDChr = computeTFIDFSim_np(sPFChrDes, sourcePropNum, tPFChrDes, targetPropNum);
			/*实例描述*/
            mDIDmn = computeTFIDFSim_np(sPIDmnDes, sourcePropNum, tPIDmnDes, targetPropNum);
            mDIRng = computeTFIDFSim_np(sPIRngDes, sourcePropNum, tPIRngDes, targetPropNum);
			
			/*合并结果*/
            double wB0, wH0, wF0, wI0, wHsub0, wHsup0, wHsbl0, wFd0, wFr0, wFc0, wId0, wIr0;
            for (int i = 0; i < sourcePropNum; i++) {
                for (int j = 0; j < targetPropNum; j++) {

					/*重新计算实际的权重*/
					/*1.层次权重*/
                    if (Math.abs(mDSub[i][j] - (-1.0)) < 0.00001) {
                        wHsub0 = 0.0;
                    } else {
                        wHsub0 = wHsub;
                    }
                    if (Math.abs(mDSup[i][j] - (-1.0)) < 0.00001) {
                        wHsup0 = 0.0;
                    } else {
                        wHsup0 = wHsup;
                    }
                    if (Math.abs(mDSbl[i][j] - (-1.0)) < 0.00001) {
                        wHsbl0 = 0.0;
                    } else {
                        wHsbl0 = wHsbl;
                    }
                    double wHTotal = wHsub0 + wHsup0 + wHsbl0;
                    if (wHTotal > 0.0) {
                        wHsub0 = wHsub0 / wHTotal;
                        wHsup0 = wHsup0 / wHTotal;
                        wHsbl0 = wHsbl0 / wHTotal;
                    }
					/*2.功能权重*/
                    if (Math.abs(mDDmn[i][j] - (-1.0)) < 0.00001) {
                        wFd0 = 0.0;
                    } else {
                        wFd0 = wFd;
                    }
                    if (Math.abs(mDRng[i][j] - (-1.0)) < 0.00001) {
                        wFr0 = 0.0;
                    } else {
                        wFr0 = wFr;
                    }
                    if (Math.abs(mDChr[i][j] - (-1.0)) < 0.00001) {
                        wFc0 = 0.0;
                    } else {
                        wFc0 = wFc;
                    }
                    double wFTotal = wFd0 + wFr0 + wFc0;
                    if (wFTotal > 0.0) {
                        wFd0 = wFd0 / wFTotal;
                        wFr0 = wFr0 / wFTotal;
                        wFc0 = wFc0 / wFTotal;
                    }
					/*3.实例权重*/
                    if (Math.abs(mDIDmn[i][j] - (-1.0)) < 0.00001) {
                        wId0 = 0.0;
                    } else {
                        wId0 = wId;
                    }
                    if (Math.abs(mDIRng[i][j] - (-1.0)) < 0.00001) {
                        wIr0 = 0.0;
                    } else {
                        wIr0 = wIr;
                    }
                    double wITotal = wId0 + wIr0;
                    if (wITotal > 0.0) {
                        wId0 = wId0 / wITotal;
                        wIr0 = wIr0 / wITotal;
                    }
					/*4.总权重*/
                    if (Math.abs(mDBasic[i][j] - (-1.0)) < 0.00001) {
                        wB0 = 0.0;
                    } else {
                        wB0 = wB;
                    }
                    if (Math.abs(wHTotal) < 0.00001) {
                        wH0 = 0.0;
                    } else {
                        wH0 = wH;
                    }
                    if (Math.abs(wFTotal) < 0.00001) {
                        wF0 = 0.0;
                    } else {
                        wF0 = wF;
                    }
                    if (Math.abs(wITotal) < 0.00001) {
                        wI0 = 0.0;
                    } else {
                        wI0 = wI;
                    }
                    double wTotal = wB0 + wH0 + wF0 + wI0;
                    if (wTotal > 0.0) {
                        wB0 = wB0 / wTotal;
                        wH0 = wH0 / wTotal;
                        wF0 = wF0 / wTotal;
                        wI0 = wI0 / wTotal;
                    }

                    if (Math.abs(wTotal) < 0.00001) {
                        pSimMatrix[i][j] = 0.0;
                    } else {
                        pSimMatrix[i][j] = wB0 * mDBasic[i][j]
                                + wH0 * (wHsub0 * mDSub[i][j] + wHsup0 * mDSup[i][j] + wHsbl0 * mDSbl[i][j])
                                + wF0 * (wFd0 * mDDmn[i][j] + wFr0 * mDRng[i][j] + wFc0 * mDChr[i][j])
                                + wI0 * (wId0 * mDIDmn[i][j] + wIr0 * mDIRng[i][j]);
                    }
                }
            }
        }
    }

    /****************
     * 计算实例文本相似
     ****************/
    public void getInsTextSim() {
		/*引入编辑距离处理Vector中的相近词*/
        tuneDesDocbyED(sourceInsTextDes, sourceInsNum, targetInsTextDes, targetInsNum);
		
		/*自身描述*/
        iSimMatrix = computeTFIDFSim(sourceInsTextDes, sourceInsNum, targetInsTextDes, targetInsNum);
    }

    public double getSimpleInsTextSim(TextDes sDes, TextDes tDes) {
        if (sDes == null || tDes == null)
            return 0;
        sourceInsNum = 1;
        targetInsNum = 1;
        sourceInsTextDes = new TextDes[sourceInsNum];
        targetInsTextDes = new TextDes[targetInsNum];
        sourceInsTextDes[0] = sDes;
        targetInsTextDes[0] = tDes;


//		/*引入编辑距离处理Vector中的相近词*/
//		tuneDesDocbyED(sourceInsTextDes,sourceInsNum,targetInsTextDes,targetInsNum);
		
		/*自身描述*/
        iSimMatrix = new double[sourceInsNum][targetInsNum];
        iSimMatrix = computeTFIDFSim(sourceInsTextDes, sourceInsNum, targetInsTextDes, targetInsNum);
        return iSimMatrix[0][0];
    }

    /****************
     * 计算子图中其它元素文本描述相似
     ****************/
    public ArrayList getOtTextSim(ArrayList desListA, ArrayList desListB) {
        ArrayList simList = new ArrayList();
		
		/*引入编辑距离处理Vector中的相近词*/
        tuneDesDocbyED(desListA, desListB);
		
		/*自身描述的相似度*/
        simList = computeTFIDFSim(desListA, desListB);
		
		/*转换为相似矩阵，进行过滤处理*/
        simList = filterTFIDFSim(simList);

        return simList;
    }

    /****************
     * 图中其它元素文本描述相似的过滤处理
     ****************/
    private ArrayList filterTFIDFSim(ArrayList simList) {
        ArrayList result = new ArrayList();
        int maxSize = simList.size();//估计相似矩阵最大规模
        double[][] matrix = new double[maxSize][maxSize];
        int n, m;//实际的矩阵大小
        HashMap sMap = new HashMap();//名称和行号的对应
        HashMap tMap = new HashMap();//名称和列号的对应
		
		/*初始化*/
        n = 0;
        m = 0;
		
		/*1.转化相似三元组为矩阵形式*/
        for (Iterator itx = simList.iterator(); itx.hasNext(); ) {
            GraphElmSim simPair = (GraphElmSim) itx.next();
            String sName = simPair.elmNameA;
            String tName = simPair.elmNameB;
            int row = 0;
            int col = 0;

            if (sMap.containsKey(sName)) {//如果已经在矩阵中
                row = (Integer) (sMap.get(sName));
            } else {
                sMap.put(sName, n);
                row = n;
                n++;
            }
            if (tMap.containsKey(tName)) {//如果已经在矩阵中
                col = (Integer) (tMap.get(tName));
            } else {
                tMap.put(tName, m);
                col = m;
                m++;
            }
            matrix[row][col] = simPair.sim;
        }
		
		/*2.过滤相似矩阵,阀值为0.25*/
        double[][] newMx = new double[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                newMx[i][j] = matrix[i][j];
            }
        }
        newMx = new SimpleFilter().maxValueFilter(n, m, newMx, 0.25);
		
		/*3.构造新的相似三元组*/
        for (Iterator itx = simList.iterator(); itx.hasNext(); ) {
            GraphElmSim simPair = (GraphElmSim) itx.next();
            String sName = simPair.elmNameA;
            String tName = simPair.elmNameB;
            int row = (Integer) (sMap.get(sName));
            int col = (Integer) (tMap.get(tName));
            if (newMx[row][col] > 0) {
                result.add(simPair);
//				System.out.println(simPair.elmNameA+"<-->"+simPair.elmNameB+":"+simPair.sim);
            }
        }
        return result;
    }

    /****************
     * 通用文本相似计算函数
     * 输入，两组文本
     * 输出，相似矩阵
     ****************/
    private ArrayList[] wA, wB;
    private double[][] matrix;

    public double[][] computeTFIDFSim(final TextDes[] docA, final int nDocA, final TextDes[] docB, final int nDocB) {
        final TfIdfSim tfidf = new TfIdfSim();
        ArrayList[] wATF = new ArrayList[nDocA];
        ArrayList[] wBTF = new ArrayList[nDocB];
        ArrayList[] wAIDF = new ArrayList[nDocA];
        ArrayList[] wBIDF = new ArrayList[nDocB];
        wA = new ArrayList[nDocA];
        wB = new ArrayList[nDocB];
        matrix = new double[nDocA][nDocB];
		/*计算TF*/
        for (int i = 0; i < nDocA; i++) {
            wATF[i] = new ArrayList();
            wATF[i] = tfidf.getDocTF(docA[i].text);
        }
        for (int i = 0; i < nDocB; i++) {
            wBTF[i] = new ArrayList();
            wBTF[i] = tfidf.getDocTF(docB[i].text);
        }
		
		/*计算IDF*/
        ArrayList lt = new ArrayList();
        lt = tfidf.getDocIDF(docA, nDocA, docB, nDocB);
        wAIDF = (ArrayList[]) lt.get(0);
        wBIDF = (ArrayList[]) lt.get(1);
		
		/*计算TF*IDF权重*/
        wA = tfidf.getTFIDFWeight(docA, nDocA, wATF, wAIDF);
        wB = tfidf.getTFIDFWeight(docB, nDocB, wBTF, wBIDF);
		
		/*相似是对称的，但m,n不一定相同*/
        new parfor() {
            //for (int i=0;i<nDocA;i++){
            public void iter(int thread_idx, int i) {
                for (int j = 0; j < nDocB; j++) {
                    ArrayList vA = new ArrayList();
                    ArrayList vB = new ArrayList();
				/*构造向量*/
                    ArrayList lt = tfidf.consTextVector(docA[i].text, docB[j].text, wA[i], wB[j]);
                    vA = (ArrayList) lt.get(0);
                    vB = (ArrayList) lt.get(1);
				/*计算向量相似*/
                    if (vA.isEmpty() && vB.isEmpty()) {
					/*vA vB都为空，说明二者都没有这方面的特征，相似度直接赋值0.0*/
                        matrix[i][j] = 0.0;
                    } else {
                        matrix[i][j] = tfidf.getTextVectorSim(vA, vB);
                    }
                }
            }
        }.execute(0, nDocA);
        return matrix;
    }

    /****************
     * 通用文本相似计算函数（非并行版本）
     * 输入，两组文本
     * 输出，相似矩阵
     ****************/
    public double[][] computeTFIDFSim_np(final TextDes[] docA, final int nDocA, final TextDes[] docB, final int nDocB) {
        ArrayList[] wA, wB;
        double[][] matrix;
        final TfIdfSim tfidf = new TfIdfSim();
        ArrayList[] wATF = new ArrayList[nDocA];
        ArrayList[] wBTF = new ArrayList[nDocB];
        ArrayList[] wAIDF = new ArrayList[nDocA];
        ArrayList[] wBIDF = new ArrayList[nDocB];
        wA = new ArrayList[nDocA];
        wB = new ArrayList[nDocB];
        matrix = new double[nDocA][nDocB];
		/*计算TF*/
        for (int i = 0; i < nDocA; i++) {
            wATF[i] = new ArrayList();
            wATF[i] = tfidf.getDocTF(docA[i].text);
        }
        for (int i = 0; i < nDocB; i++) {
            wBTF[i] = new ArrayList();
            wBTF[i] = tfidf.getDocTF(docB[i].text);
        }

		/*计算IDF*/
        ArrayList lt = new ArrayList();
        lt = tfidf.getDocIDF(docA, nDocA, docB, nDocB);
        wAIDF = (ArrayList[]) lt.get(0);
        wBIDF = (ArrayList[]) lt.get(1);

		/*计算TF*IDF权重*/
        wA = tfidf.getTFIDFWeight(docA, nDocA, wATF, wAIDF);
        wB = tfidf.getTFIDFWeight(docB, nDocB, wBTF, wBIDF);

		/*相似是对称的，但m,n不一定相同*/
        for (int i = 0; i < nDocA; i++) {
            for (int j = 0; j < nDocB; j++) {
                ArrayList vA = new ArrayList();
                ArrayList vB = new ArrayList();
				/*构造向量*/
                lt = tfidf.consTextVector(docA[i].text, docB[j].text, wA[i], wB[j]);
                vA = (ArrayList) lt.get(0);
                vB = (ArrayList) lt.get(1);
				/*计算向量相似*/
                if (vA.isEmpty() && vB.isEmpty()) {
					/*vA vB都为空，说明二者都没有这方面的特征，相似度直接赋值0.0*/
                    matrix[i][j] = 0.0;
                } else {
                    matrix[i][j] = tfidf.getTextVectorSim(vA, vB);
                }
            }
        }
        return matrix;
    }

    /****************
     更为简单的相似矩阵计算函数，仅统计两两之间重叠程度大的关键词
     ****************/
    private double strArrSim(final String[] strA, final String[] strB) {
        if (strA.length != strB.length)
            return 1.0;
        final StrEDSim edsim = new StrEDSim();
        double score = 1.0;
        for (int i = 0; i < strA.length; ++i)
            if (strA[i].length() > 0 && strB[i].length() > 0) //跳过空串
                score *= edsim.getNormEDSim(strA[i], strB[i]);
        return score;
    }

    private String[] strPropA;
    private String[] strPropB;

    public double[][] simpleSim(final TextDes[] docA, final int nDocA, final TextDes[] docB, final int nDocB, final HashMap propOrder) {
        //O(mn)
        matrix = new double[nDocA][nDocB];
        strPropA = new String[nDocA];
        strPropB = new String[nDocB];
        new parfor() {
            //for (int i=0;i<nDocA;i++){
            public void iter(int thread_idx, int i) {
                double tmp;
                Word w1, w2;
                MultiWord tmp1, tmp2;
                ArrayList<MultiWord> wl1, wl2;
                ArrayList termListA = docA[i].text;
                ArrayList termlistB;
                wl1 = new ArrayList<>();
                for (Iterator itx = termListA.iterator(); itx.hasNext(); ) {
                    w1 = (Word) itx.next();
                    tmp1 = new MultiWord();
                    tmp1.content = w1.content.split("\n");
                    tmp1.weight = w1.weight;
                    if (tmp1.content.length == 1)
                        strPropA[i] = tmp1.content[0];
                    else {
                        //if (tmp1.content.length < 5)
                        //continue;
                        wl1.add(tmp1);
                    }
                }
                for (int j = 0; j < nDocB; j++) {
                    wl2 = new ArrayList<>();
                    termlistB = docB[j].text;
                    for (Iterator ity = termlistB.iterator(); ity.hasNext(); ) {
                        w2 = (Word) ity.next();
                        tmp2 = new MultiWord();
                        tmp2.content = w2.content.split("\n");
                        tmp2.weight = w2.weight;
                        if (tmp2.content.length == 1)
                            strPropB[j] = tmp2.content[0];
                        else
                            wl2.add(tmp2);
                    }

                    for (Iterator itx = wl1.iterator(); itx.hasNext(); ) {
                        tmp1 = (MultiWord) itx.next();
                        for (Iterator ity = wl2.iterator(); ity.hasNext(); ) {
                            tmp2 = (MultiWord) ity.next();
                            if ((tmp = strArrSim(tmp1.content, tmp2.content)) > 0.95) //两组关键字很像
                            {
                                matrix[i][j] += tmp1.weight * tmp2.weight;
                                if (tmp < 1.0)
                                    continue;
                            }
                        }
                    }

                }
            }
        }.execute(0, nDocA);
        /*try {
            FileReader in = new FileReader("dataset/OAEI2015/IM/author_dis_sandbox/sim.tsv");
            BufferedReader br = new BufferedReader(in);
            for (int i = 0; i < nDocA; ++i) {
                if (br.ready()) {
                    //读入一个三元组
                    String strLine = br.readLine().trim();
                    if (strLine.length() == 0) //跳过空白行
                        continue;
                    String[] arr = strLine.split("\t");
                    for (int j = 0; j < nDocB; ++j)
                        matrix[i][j] = Double.valueOf(arr[j]);
                }
            }
            in.close();
        } catch (Exception e) {
            //
        }*/
        double[] maxA = new double[nDocA];
        double[] maxB = new double[nDocB];
        for (int i = 0; i < nDocA; ++i)
            for (int j = 0; j < nDocB; ++j) {
                maxA[i] = Math.max(maxA[i], matrix[i][j]);
                maxB[j] = Math.max(maxB[j], matrix[i][j]);
            }
        final StrEDSim edsim = new StrEDSim();
        try {
            BufferedWriter matWriter = new BufferedWriter(new FileWriter(new File("dataset/OAEI2015/IM/author_dis_mainbox/sim.tsv")));
            //BufferedWriter matWriter2 = new BufferedWriter(new FileWriter(new File("dataset/OAEI2015/IM/author_dis_mainbox/sim2.tsv")));
            for (int i = 0; i < nDocA; ++i) {
                for (int j = 0; j < nDocB; ++j) {
                    double score = 1.0;
                    matWriter.write(matrix[i][j] + "\t");
                    if (maxA[i] > 0 && maxB[j] > 0)
                        matrix[i][j] = (matrix[i][j] / maxA[i]) * (matrix[i][j] / maxB[j]); //归一化
                    if (strPropA[i].length() > 0 && strPropB[j].length() > 0 && !strPropA[i].contains(strPropB[j]) && !strPropB[j].contains(strPropA[i])) //跳过空串与串包含的情况
                        score *= edsim.getNormEDSim(strPropA[i], strPropB[j]);
                    //if (i == 744 && j == 760)
                    //score += 0;
                    //if (score > 0.95)
                    //score += 0;
                    //matrix[i][j] *= score;
                    matrix[i][j] = (matrix[i][j] + score) / 2; //相似度混合
                    //matWriter2.write(matrix[i][j] + "\t");
                }
                matWriter.write("\n");
                //matWriter2.write("\n");
            }
            matWriter.close();
            //matWriter2.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return matrix;
    }

    public ArrayList computeTFIDFSim(ArrayList desListA, ArrayList desListB) {
        ArrayList result = new ArrayList();
        int nDocA = desListA.size();
        int nDocB = desListB.size();
        TfIdfSim tfidf = new TfIdfSim();
        ArrayList[] wATF = new ArrayList[nDocA];
        ArrayList[] wBTF = new ArrayList[nDocB];
        ArrayList[] wAIDF = new ArrayList[nDocA];
        ArrayList[] wBIDF = new ArrayList[nDocB];
        ArrayList[] wA = new ArrayList[nDocA];
        ArrayList[] wB = new ArrayList[nDocB];
				
		/*计算TF*/
        int count = 0;
        for (Iterator it = desListA.iterator(); it.hasNext(); ) {
            TextDes des = (TextDes) it.next();
            wATF[count] = new ArrayList();
            wATF[count] = tfidf.getDocTF(des.text);
            count++;
        }
        count = 0;
        for (Iterator it = desListB.iterator(); it.hasNext(); ) {
            TextDes des = (TextDes) it.next();
            wBTF[count] = new ArrayList();
            wBTF[count] = tfidf.getDocTF(des.text);
            count++;
        }
		
		/*计算IDF*/
        ArrayList lt = new ArrayList();
        lt = tfidf.getDocIDF((TextDes[]) desListA.toArray(new TextDes[0]), nDocA, (TextDes[]) desListB.toArray(new TextDes[0]), nDocB);
        wAIDF = (ArrayList[]) lt.get(0);
        wBIDF = (ArrayList[]) lt.get(1);
		
		/*计算TF*IDF权重*/
        wA = tfidf.getTFIDFWeight((TextDes[]) desListA.toArray(new TextDes[0]), nDocA, wATF, wAIDF);
        wB = tfidf.getTFIDFWeight((TextDes[]) desListB.toArray(new TextDes[0]), nDocB, wBTF, wBIDF);
		
		/*相似是对称的，但m,n不一定相同*/
        int counta = 0, countb = 0;
        for (Iterator itx = desListA.iterator(); itx.hasNext(); ) {
            TextDes desA = (TextDes) itx.next();
            countb = 0;
            if (desA.text.isEmpty()) {
                continue;
            }
            for (Iterator ity = desListB.iterator(); ity.hasNext(); ) {
                TextDes desB = (TextDes) ity.next();
                if (desB.text.isEmpty()) {
                    continue;
                }
                if (desA.type != desB.type) {
                    countb++;
                    continue;
                }//跳过不同类型的多余计算
                ArrayList vA = new ArrayList();
                ArrayList vB = new ArrayList();
				/*构造向量*/
                lt = tfidf.consTextVector(desA.text, desB.text, wA[counta], wB[countb]);
                vA = (ArrayList) lt.get(0);
                vB = (ArrayList) lt.get(1);
				/*计算向量相似*/
                if (vA.isEmpty() || vB.isEmpty()) {
					/*vA vB都为空，说明二者都没有这方面的特征，相似度直接赋值-1.0*/
                    //这种情况下不记录
                } else {
                    GraphElmSim simPair = new GraphElmSim();
                    simPair.elmNameA = desA.name;
                    simPair.elmNameB = desB.name;
                    simPair.sim = tfidf.getTextVectorSim(vA, vB);
					
					/*过滤过小的相似度*/
//					if (simPair.sim>0.25){
//						result.add(simPair);
//					}
                    if (simPair.sim > 0.05) {
                        result.add(simPair);
//						System.out.println("调式：其它元素相似度计算，阀值");
//						System.out.println(simPair.elmNameA+"<-->"+simPair.elmNameB+":"+simPair.sim);
                    }
                }
                countb++;
            }
            counta++;
        }
        return result;
    }

    /**********************
     * 接收本体参数
     ********************/
    public void unPackPara(ArrayList paraList) {
        sourceConceptNum = ((Integer) paraList.get(0)).intValue();
        sourcePropNum = ((Integer) paraList.get(1)).intValue();
        sourceDataPropNum = ((Integer) paraList.get(2)).intValue();
        sourceObjPropNum = ((Integer) paraList.get(3)).intValue();
        sourceInsNum = ((Integer) paraList.get(20)).intValue();

        targetConceptNum = ((Integer) paraList.get(10)).intValue();
        targetPropNum = ((Integer) paraList.get(11)).intValue();
        targetDataPropNum = ((Integer) paraList.get(12)).intValue();
        targetObjPropNum = ((Integer) paraList.get(13)).intValue();
        targetInsNum = ((Integer) paraList.get(23)).intValue();

        //根据得到的number初始化各种数组
        initPara();

        sourceConceptName = (String[]) (paraList.get(4));
        sourcePropName = (String[]) (paraList.get(5));
        sourceDataPropName = (String[]) (paraList.get(6));
        sourceObjPropName = (String[]) (paraList.get(7));
        sourceInsName = (String[]) (paraList.get(21));

        sourceCnptTextDes = (TextDes[]) (paraList.get(8));
        sourcePropTextDes = (TextDes[]) (paraList.get(9));
        sourceInsTextDes = (TextDes[]) (paraList.get(22));

        targetConceptName = (String[]) (paraList.get(14));
        targetPropName = (String[]) (paraList.get(15));
        targetDataPropName = (String[]) (paraList.get(16));
        targetObjPropName = (String[]) (paraList.get(17));
        targetInsName = (String[]) (paraList.get(24));

        targetCnptTextDes = (TextDes[]) (paraList.get(18));
        targetPropTextDes = (TextDes[]) (paraList.get(19));
        targetInsTextDes = (TextDes[]) (paraList.get(25));
    }

    /**********************
     * 初始化本体的一些数据结构
     ********************/
    public void initPara() {
		/*基本信息*/
        sourceConceptName = new String[sourceConceptNum];
        sourcePropName = new String[sourcePropNum];
        sourceDataPropName = new String[sourceDataPropNum];
        sourceObjPropName = new String[sourceObjPropNum];
        sourceInsName = new String[sourceInsNum];

        targetConceptName = new String[targetConceptNum];
        targetPropName = new String[targetPropNum];
        targetDataPropName = new String[targetDataPropNum];
        targetObjPropName = new String[targetObjPropNum];
        targetInsName = new String[targetInsNum];
		
		/*描述信息*/
        sourceCnptTextDes = new TextDes[sourceConceptNum];
        sourcePropTextDes = new TextDes[sourcePropNum];
        sourceInsTextDes = new TextDes[sourceInsNum];

        targetCnptTextDes = new TextDes[targetConceptNum];
        targetPropTextDes = new TextDes[targetPropNum];
        targetInsTextDes = new TextDes[targetInsNum];

        sCBasicDes = new TextDes[sourceConceptNum];
        sCHSubDes = new TextDes[sourceConceptNum];
        sCHSupDes = new TextDes[sourceConceptNum];
        sCHSblDes = new TextDes[sourceConceptNum];
        sCHDsjDes = new TextDes[sourceConceptNum];
        sCHCmpDes = new TextDes[sourceConceptNum];
        sCPDmnDes = new TextDes[sourceConceptNum];
        sCPRngDes = new TextDes[sourceConceptNum];
        sCInsDes = new TextDes[sourceConceptNum];

        sPBasicDes = new TextDes[sourcePropNum];
        sPHSubDes = new TextDes[sourcePropNum];
        sPHSupDes = new TextDes[sourcePropNum];
        sPHSblDes = new TextDes[sourcePropNum];
        sPFDmnDes = new TextDes[sourcePropNum];
        sPFRngDes = new TextDes[sourcePropNum];
        sPFChrDes = new TextDes[sourcePropNum];
        sPIDmnDes = new TextDes[sourcePropNum];
        sPIRngDes = new TextDes[sourcePropNum];

        sDPBasicDes = new TextDes[sourceDataPropNum];
        sDPHSubDes = new TextDes[sourceDataPropNum];
        sDPHSupDes = new TextDes[sourceDataPropNum];
        sDPHSblDes = new TextDes[sourceDataPropNum];
        sDPFDmnDes = new TextDes[sourceDataPropNum];
        sDPFRngDes = new TextDes[sourceDataPropNum];
        sDPFChrDes = new TextDes[sourceDataPropNum];
        sDPIDmnDes = new TextDes[sourceDataPropNum];
        sDPIRngDes = new TextDes[sourceDataPropNum];

        sOPBasicDes = new TextDes[sourceObjPropNum];
        sOPHSubDes = new TextDes[sourceObjPropNum];
        sOPHSupDes = new TextDes[sourceObjPropNum];
        sOPHSblDes = new TextDes[sourceObjPropNum];
        sOPFDmnDes = new TextDes[sourceObjPropNum];
        sOPFRngDes = new TextDes[sourceObjPropNum];
        sOPFChrDes = new TextDes[sourceObjPropNum];
        sOPIDmnDes = new TextDes[sourceObjPropNum];
        sOPIRngDes = new TextDes[sourceObjPropNum];

        tCBasicDes = new TextDes[targetConceptNum];
        tCHSubDes = new TextDes[targetConceptNum];
        tCHSupDes = new TextDes[targetConceptNum];
        tCHSblDes = new TextDes[targetConceptNum];
        tCHDsjDes = new TextDes[targetConceptNum];
        tCHCmpDes = new TextDes[targetConceptNum];
        tCPDmnDes = new TextDes[targetConceptNum];
        tCPRngDes = new TextDes[targetConceptNum];
        tCInsDes = new TextDes[targetConceptNum];

        tPBasicDes = new TextDes[targetPropNum];
        tPHSubDes = new TextDes[targetPropNum];
        tPHSupDes = new TextDes[targetPropNum];
        tPHSblDes = new TextDes[targetPropNum];
        tPFDmnDes = new TextDes[targetPropNum];
        tPFRngDes = new TextDes[targetPropNum];
        tPFChrDes = new TextDes[targetPropNum];
        tPIDmnDes = new TextDes[targetPropNum];
        tPIRngDes = new TextDes[targetPropNum];

        tDPBasicDes = new TextDes[targetDataPropNum];
        tDPHSubDes = new TextDes[targetDataPropNum];
        tDPHSupDes = new TextDes[targetDataPropNum];
        tDPHSblDes = new TextDes[targetDataPropNum];
        tDPFDmnDes = new TextDes[targetDataPropNum];
        tDPFRngDes = new TextDes[targetDataPropNum];
        tDPFChrDes = new TextDes[targetDataPropNum];
        tDPIDmnDes = new TextDes[targetDataPropNum];
        tDPIRngDes = new TextDes[targetDataPropNum];

        tOPBasicDes = new TextDes[targetObjPropNum];
        tOPHSubDes = new TextDes[targetObjPropNum];
        tOPHSupDes = new TextDes[targetObjPropNum];
        tOPHSblDes = new TextDes[targetObjPropNum];
        tOPFDmnDes = new TextDes[targetObjPropNum];
        tOPFRngDes = new TextDes[targetObjPropNum];
        tOPFChrDes = new TextDes[targetObjPropNum];
        tOPIDmnDes = new TextDes[targetObjPropNum];
        tOPIRngDes = new TextDes[targetObjPropNum];
		
		/*相似度*/
        cSimMatrix = new double[sourceConceptNum][targetConceptNum];
        pSimMatrix = new double[sourcePropNum][targetPropNum];
        dpSimMatrix = new double[sourceDataPropNum][targetDataPropNum];
        opSimMatrix = new double[sourceObjPropNum][targetObjPropNum];
        iSimMatrix = new double[sourceInsNum][targetInsNum];
    }

    /**********************
     * 结合字符串相似来处理描述文档
     ********************/
    public void tuneDesDocbyED(final TextDes[] docA, final int nDocA, final TextDes[] docB, final int nDocB) {
        final StrEDSim edsim = new StrEDSim();
		/*1.合并同Doc中的相似的term*/
        new parfor() {
            //for (int i=0;i<nDocA;i++){
            public void iter(int thread_idx, int i) {
                ArrayList termList = docA[i].text;
                combineTermIntraDoc(termList);
            }
        }.execute(0, nDocA);
        new parfor() {
            //for (int i=0;i<nDocB;i++){
            public void iter(int thread_idx, int i) {
                ArrayList termList = docB[i].text;
                combineTermIntraDoc(termList);
            }
        }.execute(0, nDocB);
		
		/*2.合并不同Doc中的相似的term*/
        /*****复杂度为O(n^4)的处理方法******/
        //这里不做优化是考虑到最内的两重循环通常较小，因此算法复杂度主要由外两层控制
        new parfor() {
            //for (int i=0;i<nDocA;i++){
            public void iter(int thread_idx, int i) {
                ArrayList termListA = docA[i].text;
                for (int j = 0; j < nDocB; j++) {
                    ArrayList termlistB = docB[j].text;

                    Word w1 = new Word();
                    Word w2 = new Word();
                    for (Iterator itx = termListA.iterator(); itx.hasNext(); ) {
                        w1 = (Word) itx.next();
                        for (Iterator ity = termlistB.iterator(); ity.hasNext(); ) {
                            w2 = (Word) ity.next();
                            if (!w1.content.equals(w2.content) && edsim.getNormEDSim(w1.content, w2.content) > edThreshold) {
                                /*统一term,以A为准*/
                                w2.content = w1.content;
                            }
                        }
                    }
                }
            }
        }.execute(0, nDocA);
        /*****复杂度为O(n^2)的处理方法(未完成)******/
//		Set termSet=new HashSet();
//		for (int i=0;i<nDocA;i++){
//			ArrayList termList=docA[i].text;
//			for (Iterator itx=termList.iterator();itx.hasNext();){
//				Word wd=(Word)itx.next();
//				if (!termSet.contains(wd.content)){
//					termSet.add(wd);
//				}
//			}
//		}
//		for (int i=0;i<nDocB;i++){
//			ArrayList termList=docB[i].text;
//			for (Iterator itx=termList.iterator();itx.hasNext();){
//				Word wd=(Word)itx.next();
//				if (!termSet.contains(wd.content)){
//					termSet.add(wd);
//				}
//			}
//		}
    }

    /**********************
     * 结合字符串相似来处理描述文档（非并行计算版）
     ********************/
    public void tuneDesDocbyED_np(final TextDes[] docA, final int nDocA, final TextDes[] docB, final int nDocB) {
        final StrEDSim edsim = new StrEDSim();
		/*1.合并同Doc中的相似的term*/
        for (int i = 0; i < nDocA; i++) {
            ArrayList termList = docA[i].text;
            combineTermIntraDoc(termList);
        }
        for (int i = 0; i < nDocB; i++) {
            ArrayList termList = docB[i].text;
            combineTermIntraDoc(termList);
        }

		/*2.合并不同Doc中的相似的term*/
        /*****复杂度为O(n^4)的处理方法******/
        //这里不做优化是考虑到最内的两重循环通常较小，因此算法复杂度主要由外两层控制
        for (int i = 0; i < nDocA; i++) {
            ArrayList termListA = docA[i].text;
            for (int j = 0; j < nDocB; j++) {
                ArrayList termlistB = docB[j].text;

                Word w1 = new Word();
                Word w2 = new Word();
                for (Iterator itx = termListA.iterator(); itx.hasNext(); ) {
                    w1 = (Word) itx.next();
                    for (Iterator ity = termlistB.iterator(); ity.hasNext(); ) {
                        w2 = (Word) ity.next();
                        if (!w1.content.equals(w2.content) && edsim.getNormEDSim(w1.content, w2.content) > edThreshold) {
                                /*统一term,以A为准*/
                            w2.content = w1.content;
                        }
                    }
                }
            }
        }
    }

    public void tuneDesDocbyED(ArrayList desListA, ArrayList desListB) {

        StrEDSim edsim = new StrEDSim();
		
		/*1.合并同Doc中的相似的term*/
        for (Iterator it = desListA.iterator(); it.hasNext(); ) {
            TextDes des = (TextDes) it.next();
            ArrayList termList = des.text;
            combineTermIntraDoc(termList);
        }
        for (Iterator it = desListB.iterator(); it.hasNext(); ) {
            TextDes des = (TextDes) it.next();
            ArrayList termList = des.text;
            combineTermIntraDoc(termList);
        }
		
		/*2.合并不同Doc中的相似的term*/
        for (Iterator itx = desListA.iterator(); itx.hasNext(); ) {
            TextDes desA = (TextDes) itx.next();
            ArrayList termListA = desA.text;
            for (Iterator ity = desListB.iterator(); ity.hasNext(); ) {
                TextDes desB = (TextDes) ity.next();
                ArrayList termListB = desB.text;

                Word w1 = new Word();
                Word w2 = new Word();
                for (Iterator jtx = termListA.iterator(); jtx.hasNext(); ) {
                    w1 = (Word) jtx.next();
                    for (Iterator jty = termListB.iterator(); jty.hasNext(); ) {
                        w2 = (Word) jty.next();
                        if (!w1.content.equals(w2.content) && edsim.getNormEDSim(w1.content, w2.content) > edThreshold) {
							/*统一term,以A为准*/
                            w2.content = w1.content;
                        }
                    }
                }
            }
        }
    }

    /**********************
     * 合并同Doc中的相似的term
     ********************/
    // @SuppressWarnings("unchecked")
    public void combineTermIntraDoc(ArrayList termList) {
        StrEDSim edsim = new StrEDSim();
		/*取出当前文档的全部term*/
        Set termSet = new HashSet();
        for (Iterator it = termList.iterator(); it.hasNext(); ) {
            Word w = (Word) it.next();
            termSet.add(w);
        }
		/*合并term，直到没有相似的term为止*/
        boolean hasSimTerm = false;
        while (!hasSimTerm) {
            hasSimTerm = true;
            Word w1 = new Word();
            Word w2 = new Word();
            for (Iterator itx = termSet.iterator(); itx.hasNext(); ) {
                w1 = (Word) itx.next();
                for (Iterator ity = termSet.iterator(); ity.hasNext(); ) {
                    w2 = (Word) ity.next();
                    if (!w1.content.equals(w2.content)
                            && edsim.getNormEDSim(w1.content, w2.content) > edThreshold) {
                        hasSimTerm = false;
                        break;
                    }
                }
                if (!hasSimTerm) {
                    break;
                }
            }
			/*存在相似的词，进行合并：
			 * 把w2合并到w1*/
            if (!hasSimTerm) {
                w1.weight = w1.weight + w2.weight;
                termSet.remove(w2);
            }
        }
		/*合并结束，重新构造doc[i]的文档*/
        termList.clear();
        termList.addAll(termSet);
    }

    private double sigmoid(double x) {
        double f = 1.0 / (1.0 + Math.exp(-5.0 * (x - 0.5)));
        return f;

    }

    /**********************
     * 简单计算两个概念语义文档的相似度
     ********************/
    public double getSimpleCnptTextSim(TextDes sDes, TextDes tDes) {
		/*构造参数*/
        sourceConceptNum = 1;
        targetConceptNum = 1;
        //根据得到的number初始化各种数组
		
		/*基本信息*/
        sourceConceptName = new String[sourceConceptNum];
        targetConceptName = new String[targetConceptNum];
		
		/*描述信息*/
        sourceCnptTextDes = new TextDes[sourceConceptNum];
        targetCnptTextDes = new TextDes[targetConceptNum];

        sCBasicDes = new TextDes[sourceConceptNum];
        sCHSubDes = new TextDes[sourceConceptNum];
        sCHSupDes = new TextDes[sourceConceptNum];
        sCHSblDes = new TextDes[sourceConceptNum];
        sCHDsjDes = new TextDes[sourceConceptNum];
        sCHCmpDes = new TextDes[sourceConceptNum];
        sCPDmnDes = new TextDes[sourceConceptNum];
        sCPRngDes = new TextDes[sourceConceptNum];
        sCInsDes = new TextDes[sourceConceptNum];

        tCBasicDes = new TextDes[targetConceptNum];
        tCHSubDes = new TextDes[targetConceptNum];
        tCHSupDes = new TextDes[targetConceptNum];
        tCHSblDes = new TextDes[targetConceptNum];
        tCHDsjDes = new TextDes[targetConceptNum];
        tCHCmpDes = new TextDes[targetConceptNum];
        tCPDmnDes = new TextDes[targetConceptNum];
        tCPRngDes = new TextDes[targetConceptNum];
        tCInsDes = new TextDes[targetConceptNum];

		/*相似度*/
        cSimMatrix = new double[sourceConceptNum][targetConceptNum];

        sourceConceptName[0] = "sourceCnptName";
        sourceCnptTextDes[0] = sDes;

        targetConceptName[0] = "targetCnptName";
        targetCnptTextDes[0] = tDes;
		
		/*整理文本*/
		/*整理source文本*/
        for (int i = 0; i < sourceConceptNum; i++) {
            ArrayList desList = new ArrayList();

            /**************概念描述***************/
			/*自身描述*/
            sCBasicDes[i] = new TextDes();
            sCBasicDes[i].name = sourceConceptName[i];
            desList = (ArrayList) sourceCnptTextDes[i].text.get(0);
            sCBasicDes[i].text = desList;
			
			/*层次描述*/
            desList = (ArrayList) sourceCnptTextDes[i].text.get(1);
			/*subClass*/
            sCHSubDes[i] = new TextDes();
            sCHSubDes[i].name = sourceConceptName[i];
            sCHSubDes[i].text = ((TextDes) desList.get(0)).text;
			
			/*superClass*/
            sCHSupDes[i] = new TextDes();
            sCHSupDes[i].name = sourceConceptName[i];
            sCHSupDes[i].text = ((TextDes) desList.get(1)).text;
			/*sibling Class*/
            sCHSblDes[i] = new TextDes();
            sCHSblDes[i].name = sourceConceptName[i];
            sCHSblDes[i].text = ((TextDes) desList.get(2)).text;
			/*disjoint Class*/
            sCHDsjDes[i] = new TextDes();
            sCHDsjDes[i].name = sourceConceptName[i];
            sCHDsjDes[i].text = ((TextDes) desList.get(3)).text;
			/*complementOf Class*/
            sCHCmpDes[i] = new TextDes();
            sCHCmpDes[i].name = sourceConceptName[i];
            sCHCmpDes[i].text = ((TextDes) desList.get(4)).text;
			
			/*附加属性描述*/
            desList = (ArrayList) sourceCnptTextDes[i].text.get(2);
            sCPDmnDes[i] = new TextDes();
            sCPDmnDes[i].name = sourceConceptName[i];
            sCPDmnDes[i].text = ((TextDes) desList.get(0)).text;
            sCPRngDes[i] = new TextDes();
            sCPRngDes[i].name = sourceConceptName[i];
            sCPRngDes[i].text = ((TextDes) desList.get(1)).text;
			
			/*实例描述*/
            desList = (ArrayList) sourceCnptTextDes[i].text.get(3);
            sCInsDes[i] = new TextDes();
            sCInsDes[i].name = sourceConceptName[i];
            sCInsDes[i].text = ((TextDes) desList.get(0)).text;
        }
		
		/*整理target文本*/
        for (int i = 0; i < targetConceptNum; i++) {
            ArrayList desList = new ArrayList();

            /**************概念描述***************/
			/*自身描述*/
            tCBasicDes[i] = new TextDes();
            tCBasicDes[i].name = targetConceptName[i];
            desList = (ArrayList) targetCnptTextDes[i].text.get(0);
            tCBasicDes[i].text = desList;
			
			/*层次描述*/
            desList = (ArrayList) targetCnptTextDes[i].text.get(1);
			/*subClass*/
            tCHSubDes[i] = new TextDes();
            tCHSubDes[i].name = targetConceptName[i];
            tCHSubDes[i].text = ((TextDes) desList.get(0)).text;
			
			/*superClass*/
            tCHSupDes[i] = new TextDes();
            tCHSupDes[i].name = targetConceptName[i];
            tCHSupDes[i].text = ((TextDes) desList.get(1)).text;
			/*sibling Class*/
            tCHSblDes[i] = new TextDes();
            tCHSblDes[i].name = targetConceptName[i];
            tCHSblDes[i].text = ((TextDes) desList.get(2)).text;
			/*disjoint Class*/
            tCHDsjDes[i] = new TextDes();
            tCHDsjDes[i].name = targetConceptName[i];
            tCHDsjDes[i].text = ((TextDes) desList.get(3)).text;
			/*complementOf Class*/
            tCHCmpDes[i] = new TextDes();
            tCHCmpDes[i].name = targetConceptName[i];
            tCHCmpDes[i].text = ((TextDes) desList.get(4)).text;
			
			/*附加属性描述*/
            desList = (ArrayList) targetCnptTextDes[i].text.get(2);
            tCPDmnDes[i] = new TextDes();
            tCPDmnDes[i].name = targetConceptName[i];
            tCPDmnDes[i].text = ((TextDes) desList.get(0)).text;
            tCPRngDes[i] = new TextDes();
            tCPRngDes[i].name = targetConceptName[i];
            tCPRngDes[i].text = ((TextDes) desList.get(1)).text;
			
			/*实例描述*/
            desList = (ArrayList) targetCnptTextDes[i].text.get(3);
            tCInsDes[i] = new TextDes();
            tCInsDes[i].name = targetConceptName[i];
            tCInsDes[i].text = ((TextDes) desList.get(0)).text;
        }
		
		/*概念相似*/
        getCnptTextSim(false);

        return cSimMatrix[0][0];
    }

    /**********************
     * 简单计算两个属性语义文档的相似度
     ********************/
    public double getSimplePropTextSim(TextDes sDes, TextDes tDes) { 
		/*构造参数*/
        sourcePropNum = 1;
        targetPropNum = 1;
        //根据得到的number初始化各种数组
		
		/*基本信息*/
        sourcePropName = new String[sourcePropNum];
        targetPropName = new String[targetPropNum];
		
		/*描述信息*/
        sourcePropTextDes = new TextDes[sourcePropNum];
        targetPropTextDes = new TextDes[targetPropNum];

        sPBasicDes = new TextDes[sourcePropNum];
        sPHSubDes = new TextDes[sourcePropNum];
        sPHSupDes = new TextDes[sourcePropNum];
        sPHSblDes = new TextDes[sourcePropNum];
        sPFDmnDes = new TextDes[sourcePropNum];
        sPFRngDes = new TextDes[sourcePropNum];
        sPFChrDes = new TextDes[sourcePropNum];
        sPIDmnDes = new TextDes[sourcePropNum];
        sPIRngDes = new TextDes[sourcePropNum];

        tPBasicDes = new TextDes[targetPropNum];
        tPHSubDes = new TextDes[targetPropNum];
        tPHSupDes = new TextDes[targetPropNum];
        tPHSblDes = new TextDes[targetPropNum];
        tPFDmnDes = new TextDes[targetPropNum];
        tPFRngDes = new TextDes[targetPropNum];
        tPFChrDes = new TextDes[targetPropNum];
        tPIDmnDes = new TextDes[targetPropNum];
        tPIRngDes = new TextDes[targetPropNum];

		/*相似度*/
        pSimMatrix = new double[sourcePropNum][targetPropNum];

        sourcePropName[0] = "sourcePropName";
        sourcePropTextDes[0] = sDes;

        targetPropName[0] = "targetPropName";
        targetPropTextDes[0] = tDes;
		
		/*整理文本*/
		/*整理source文本*/
        for (int i = 0; i < sourcePropNum; i++) {
            ArrayList desList = new ArrayList();

            /**************属性描述***************/
			/*自身描述*/
            sPBasicDes[i] = new TextDes();
            sPBasicDes[i].name = sourcePropName[i];
            desList = (ArrayList) sourcePropTextDes[i].text.get(0);
            sPBasicDes[i].text = desList;
			
			/*层次描述*/
            desList = (ArrayList) sourcePropTextDes[i].text.get(1);
			/*subProperty*/
            sPHSubDes[i] = new TextDes();
            sPHSubDes[i].name = sourcePropName[i];
            sPHSubDes[i].text = ((TextDes) desList.get(0)).text;
			/*superProperty*/
            sPHSupDes[i] = new TextDes();
            sPHSupDes[i].name = sourcePropName[i];
            sPHSupDes[i].text = ((TextDes) desList.get(1)).text;
			/*sibling property*/
            sPHSblDes[i] = new TextDes();
            sPHSblDes[i].name = sourcePropName[i];
            sPHSblDes[i].text = ((TextDes) desList.get(2)).text;
			
			/*作用特征描述*/
            desList = (ArrayList) sourcePropTextDes[i].text.get(2);
			/*Domain*/
            sPFDmnDes[i] = new TextDes();
            sPFDmnDes[i].name = sourcePropName[i];
            sPFDmnDes[i].text = ((TextDes) desList.get(0)).text;
			/*Range*/
            sPFRngDes[i] = new TextDes();
            sPFRngDes[i].name = sourcePropName[i];
            sPFRngDes[i].text = ((TextDes) desList.get(1)).text;
			/*性质*/
            sPFChrDes[i] = new TextDes();
            sPFChrDes[i].name = sourcePropName[i];
            sPFChrDes[i].text = ((TextDes) desList.get(2)).text;
			
			/*实例描述*/
            desList = (ArrayList) sourcePropTextDes[i].text.get(3);
			/*Domain*/
            sPIDmnDes[i] = new TextDes();
            sPIDmnDes[i].name = sourcePropName[i];
            sPIDmnDes[i].text = ((TextDes) desList.get(0)).text;
			/*Range*/
            sPIRngDes[i] = new TextDes();
            sPIRngDes[i].name = sourcePropName[i];
            sPIRngDes[i].text = ((TextDes) desList.get(1)).text;
        }
		
		/*整理target文本*/
        for (int i = 0; i < targetPropNum; i++) {
            ArrayList desList = new ArrayList();

            /**************概念描述***************/
			/*自身描述*/
            tPBasicDes[i] = new TextDes();
            tPBasicDes[i].name = targetPropName[i];
            desList = (ArrayList) targetPropTextDes[i].text.get(0);
            tPBasicDes[i].text = desList;
			
			/*层次描述*/
            desList = (ArrayList) targetPropTextDes[i].text.get(1);
			/*subProperty*/
            tPHSubDes[i] = new TextDes();
            tPHSubDes[i].name = targetPropName[i];
            tPHSubDes[i].text = ((TextDes) desList.get(0)).text;
			/*superProperty*/
            tPHSupDes[i] = new TextDes();
            tPHSupDes[i].name = targetPropName[i];
            tPHSupDes[i].text = ((TextDes) desList.get(1)).text;
			/*sibling property*/
            tPHSblDes[i] = new TextDes();
            tPHSblDes[i].name = targetPropName[i];
            tPHSblDes[i].text = ((TextDes) desList.get(2)).text;
			
			/*作用特征描述*/
            desList = (ArrayList) targetPropTextDes[i].text.get(2);
			/*Domain*/
            tPFDmnDes[i] = new TextDes();
            tPFDmnDes[i].name = targetPropName[i];
            tPFDmnDes[i].text = ((TextDes) desList.get(0)).text;
			/*Range*/
            tPFRngDes[i] = new TextDes();
            tPFRngDes[i].name = targetPropName[i];
            tPFRngDes[i].text = ((TextDes) desList.get(1)).text;
			/*性质*/
            tPFChrDes[i] = new TextDes();
            tPFChrDes[i].name = targetPropName[i];
            tPFChrDes[i].text = ((TextDes) desList.get(2)).text;
			
			/*实例描述*/
            desList = (ArrayList) targetPropTextDes[i].text.get(3);
			/*Domain*/
            tPIDmnDes[i] = new TextDes();
            tPIDmnDes[i].name = targetPropName[i];
            tPIDmnDes[i].text = ((TextDes) desList.get(0)).text;
			/*Range*/
            tPIRngDes[i] = new TextDes();
            tPIRngDes[i].name = targetPropName[i];
            tPIRngDes[i].text = ((TextDes) desList.get(1)).text;
        }
		
		/*概念相似*/
        getPropTextSim(false, false);

        return pSimMatrix[0][0];
    }

    /**读取参数**/
    public void loadConfigFile() {
        edThreshold = ParamStore.edThreshold;
        System.out.println("edThreshold:" + edThreshold);
    }

}
