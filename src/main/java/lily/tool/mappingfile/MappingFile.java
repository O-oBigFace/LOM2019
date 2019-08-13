/************************************************
 * Source code information
 * -----------------------
 * Original author	 Peng Wang, School of Computer Science & Eng., Southeast University
 * Author email      pwangseu@gmail.com
 * Web               http://ontomapping.googlepages.com
 * Created			 2007-4-27
 * Filename          MappingFile.java
 * Version           2.0
 *
 * Last modified on  2007-4-27
 *               by  Peng Wang
 * -----------------------
 * Functions describe:
 * 本体映射文件相关的操作
 ***********************************************/
package lily.tool.mappingfile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

import org.dom4j.*;
import org.dom4j.io.*;
import org.dom4j.tree.DefaultNamespace;


import lily.tool.datastructure.*;

/*********************
 * Class information
 * -------------------
 * @author Peng Wang
 * @date 2007-4-27
 *
 * describe:
 * 映射结果文件操作
 ********************/
public class MappingFile {
    public String s_baseURI;

    public String t_baseURI;

    public String s_ontName;

    public String t_ontName;

    public String sType[] = {"=", ">", "<"};

    public void setBaseURI(String urlA, String urlB) {
        this.s_baseURI = urlA;
        this.t_baseURI = urlB;
        if (s_baseURI.length() > 0) {
            s_ontName = s_baseURI.substring(0, s_baseURI.length() - 1);
        } else {
            s_ontName = "";
        }
        if (t_baseURI.length() > 0) {
            t_ontName = t_baseURI.substring(0, t_baseURI.length() - 1);
        } else {
            t_ontName = "";
        }
    }

    public void save2txt(String sourceOnt, String targetOnt, int mapNum, MapRecord[] map, String lilyFileName) {
        //获得目标文件夹的路径
        String filePath = getOntPath(targetOnt);
        if (!lilyFileName.contains(".rdf")) {
            if (lilyFileName.equals("Lily") || lilyFileName.equals("lily")) {
                lilyFileName = filePath + "/" + "Lily.rdf";
            } else {
                lilyFileName = filePath + lilyFileName + ".rdf";
            }
        }

        //构建文件
        try {
            if (!lilyFileName.contains(".txt")) {
                lilyFileName = lilyFileName.replace(".rdf", ".txt");
            }
            FileWriter out = new FileWriter(lilyFileName);
            BufferedWriter bw = new BufferedWriter(out);
            //写入文件
            bw.write("Source:");
            bw.newLine();
            bw.write(sourceOnt);
            bw.newLine();
            bw.write("Target:");
            bw.newLine();
            bw.write(targetOnt);
            bw.newLine();
            bw.write("Correspondences");
            bw.newLine();
            for (int i = 0; i < mapNum; i++) {
                bw.write(map[i].sourceLabel + sType[map[i].relationType]
                        + map[i].targetLabel);
                bw.newLine();
                bw.write(String.valueOf(map[i].similarity));
                bw.newLine();
            }

            //关闭文件
            bw.close();
            out.close();
        } catch (IOException e) {
            System.err.println("Can't open result file:\n" + e.toString());
            System.exit(1);
        }
    }

    /***************************************************************************
     * 以*.rdf格式写入基准映射结果
     **************************************************************************/
    public void save2rdf(String sourceOnt, String targetOnt, int mapNum, MapRecord[] map, String lilyFileName) {
        //获得目标文件夹的路径
        String filePath = getOntPath(targetOnt);
        if (!lilyFileName.contains(".rdf")) {
            if (lilyFileName.equals("Lily") || lilyFileName.equals("lily")) {
                lilyFileName = filePath + "/" + "Lily.rdf";
            } else {
                lilyFileName = filePath + lilyFileName + ".rdf";
            }
        }

        Document document = DocumentHelper.createDocument();

        /*根节点*/
        Element rootElement = document.addElement("rdf:RDF");
        // rootElement.addAttribute("xmlns","http://knowledgeweb.semanticweb.org/heterogeneity/alignment");
        // rootElement.addAttribute("xmlns:rdf","http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        // rootElement.addAttribute("xmlns:xsd","http://www.w3.org/2001/XMLSchema#");
        Namespace spaceDefault = new DefaultNamespace("", "http://knowledgeweb.semanticweb.org/heterogeneity/alignment");
        Namespace spaceRdf = new DefaultNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        Namespace spaceXsd = new DefaultNamespace("xsd", "http://www.w3.org/2001/XMLSchema#");
        rootElement.add(spaceDefault);
        rootElement.add(spaceRdf);
        rootElement.add(spaceXsd);

        Element alignmentElement = rootElement.addElement("Alignment", "http://knowledgeweb.semanticweb.org/heterogeneity/alignment");
        Element xmlElement = alignmentElement.addElement("xml");
        xmlElement.setText("yes");
        Element levelElement = alignmentElement.addElement("level");
        levelElement.setText("0");
        Element typeElement = alignmentElement.addElement("type");
        typeElement.setText("11");
        Element onto1Element = alignmentElement.addElement("onto1");
        onto1Element.setText(s_ontName);
        Element onto2Element = alignmentElement.addElement("onto2");
        onto2Element.setText(t_ontName);
        Element uri1Element = alignmentElement.addElement("uri1");
        uri1Element.setText(s_ontName);
        Element uri2Element = alignmentElement.addElement("uri2");
        uri2Element.setText(t_ontName);

        for (int i = 0; i < mapNum; i++) {
            Element mapElement = alignmentElement.addElement("map");
            Element cellElement = mapElement.addElement("Cell");
            Element entity1Element = cellElement.addElement("entity1");
            entity1Element.addAttribute("rdf:resource", s_baseURI + map[i].sourceLabel);
            Element entity2Element = cellElement.addElement("entity2");
            entity2Element.addAttribute("rdf:resource", t_baseURI + map[i].targetLabel);
            Element measureElement = cellElement.addElement("measure");
            measureElement.addAttribute("rdf:datatype", "http://www.w3.org/2001/XMLSchema#float");
            measureElement.setText(String.valueOf(map[i].similarity));//真实的相似度
//			measureElement.setText(String.valueOf(1.0));//强制为相似度1.0
            Element relationElement = cellElement.addElement("relation");
            if (map[i].relationType < 0) {
                relationElement.setText(sType[0]);
            } else {
                relationElement.setText(sType[map[i].relationType]);
            }
        }
        try {
            OutputFormat format = OutputFormat.createPrettyPrint();
            format.setEncoding("UTF-8");
            XMLWriter output = new XMLWriter(new FileOutputStream(new File(lilyFileName)), format);
            output.write(document);
            output.close();

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /***************************************************************************
     * 以*.rdf格式写入基准映射结果String
     **************************************************************************/
    public String save2rdfstring(String sourceOnt, String targetOnt, int mapNum, MapRecord[] map) {
        String rdfString = new String();

        Document document = DocumentHelper.createDocument();

        /*根节点*/
        Element rootElement = document.addElement("rdf:RDF");
        // rootElement.addAttribute("xmlns","http://knowledgeweb.semanticweb.org/heterogeneity/alignment");
        // rootElement.addAttribute("xmlns:rdf","http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        // rootElement.addAttribute("xmlns:xsd","http://www.w3.org/2001/XMLSchema#");
        Namespace spaceDefault = new DefaultNamespace("", "http://knowledgeweb.semanticweb.org/heterogeneity/alignment");
        Namespace spaceRdf = new DefaultNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        Namespace spaceXsd = new DefaultNamespace("xsd", "http://www.w3.org/2001/XMLSchema#");
        rootElement.add(spaceDefault);
        rootElement.add(spaceRdf);
        rootElement.add(spaceXsd);

        Element alignmentElement = rootElement.addElement("Alignment", "http://knowledgeweb.semanticweb.org/heterogeneity/alignment");
        Element xmlElement = alignmentElement.addElement("xml");
        xmlElement.setText("yes");
        Element levelElement = alignmentElement.addElement("level");
        levelElement.setText("0");
        Element typeElement = alignmentElement.addElement("type");
        typeElement.setText("11");
        Element onto1Element = alignmentElement.addElement("onto1");
        onto1Element.setText(s_ontName);
        Element onto2Element = alignmentElement.addElement("onto2");
        onto2Element.setText(t_ontName);
        Element uri1Element = alignmentElement.addElement("uri1");
        uri1Element.setText(s_ontName);
        Element uri2Element = alignmentElement.addElement("uri2");
        uri2Element.setText(t_ontName);

        for (int i = 0; i < mapNum; i++) {
            Element mapElement = alignmentElement.addElement("map");
            Element cellElement = mapElement.addElement("Cell");
            Element entity1Element = cellElement.addElement("entity1");
            entity1Element.addAttribute("rdf:resource", s_baseURI + map[i].sourceLabel);
            Element entity2Element = cellElement.addElement("entity2");
            entity2Element.addAttribute("rdf:resource", t_baseURI + map[i].targetLabel);
            Element measureElement = cellElement.addElement("measure");
            measureElement.addAttribute("rdf:datatype", "http://www.w3.org/2001/XMLSchema#float");
            measureElement.setText(String.valueOf(map[i].similarity));//真实的相似度
//			measureElement.setText(String.valueOf(1.0));//强制为相似度1.0
            Element relationElement = cellElement.addElement("relation");
            if (map[i].relationType < 0) {
                relationElement.setText(sType[0]);
            } else {
                relationElement.setText(sType[map[i].relationType]);
            }
        }

        //无格式的转换，调用dom4j中的asXML()函数
        //rdfString = document.asXML();

        //带格式的转换过程
        OutputFormat format = OutputFormat.createPrettyPrint();//用这种方式来创建指定格式的format
        StringWriter out = new StringWriter();//创建String输出流
        XMLWriter writer = new XMLWriter(out, format);//包装String流
        try {
            writer.write(document);//将当前的document对象写入底层流out中
            writer.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        rdfString = out.toString();

        return rdfString;
    }

    /***************************************************************************
     * 从*.rdf读入基准映射结果
     * 利用Dom4j解析xml文档
     **************************************************************************/
    @SuppressWarnings("unchecked")
    public ArrayList read4xml(String filePath) throws MalformedURLException, DocumentException {
        ArrayList recordList = new ArrayList();
        int recordNum = 0;

        //读取XML文档
        SAXReader reader = new SAXReader();
        Document doc = reader.read(new File(filePath));
        //取得root节点
        Element root = doc.getRootElement();
        //遍历XML树
        //枚举所有子节点，直接找到map节点
        for (Iterator i1 = root.elementIterator("Alignment"); i1.hasNext(); ) {
            Element alignment = (Element) i1.next();
            //再从Alignment出发去找map
            for (Iterator i2 = alignment.elementIterator("map"); i2.hasNext(); ) {
                Element map = (Element) i2.next();
                //再从map出发去找Cell
                for (Iterator i3 = map.elementIterator("Cell"); i3.hasNext(); ) {
                    Element cell = (Element) i3.next();
                    MapRecord mapRecord = new MapRecord();
                    //得到每个Cell下的具体内容
                    for (Iterator i4 = cell.elementIterator(); i4.hasNext(); ) {
                        Element record = (Element) i4.next();
                        String s;
                        //提取sourceLabel
                        if (record.getName().equals("entity1")) {
                            s = record.attributeValue("resource");
                            s = this.getLocalName(s);
                            mapRecord.sourceLabel = new String(s);
                        }
                        //提取targetLabel
                        if (record.getName().equals("entity2")) {
                            s = record.attributeValue("resource");
                            s = this.getLocalName(s);
                            mapRecord.targetLabel = new String(s);
                        }
                        //提取measure
                        if (record.getName().equals("measure")) {
                            s = record.getStringValue();
                            mapRecord.similarity = Double.parseDouble(s);
                        }
                        //提取relation
                        if (record.getName().equals("relation")) {
                            s = record.getStringValue();
                            mapRecord.relationType = this.type2int(s);
                        }
                    }
                    recordList.add(recordNum, mapRecord);
                    recordNum++;
                }
            }
        }
        //保存并返回得到的结果
        ArrayList result = new ArrayList();
        result.add(0, recordNum);
        result.add(1, recordList);

        return result;
    }

    public ArrayList read4tsv(String filePath) {
        ArrayList recordList = new ArrayList();
        int recordNum = 0;
        try {
            FileReader in = new FileReader(filePath);
            BufferedReader br = new BufferedReader(in);
            if (br != null) {
                while (br.ready()) {
                    MapRecord mapRecord = new MapRecord();
                    //读入一个映射结果
                    String strLine = br.readLine().trim();
                    String a[] = strLine.split("\t");
                    mapRecord.sourceLabel = a[0];
                    mapRecord.targetLabel = a[1];
                    mapRecord.similarity = Double.valueOf(a[2]);
                    recordList.add(recordNum, mapRecord);
                    recordNum++;
                }
            }
            br.close();
        } catch (IOException e) {
            System.err.println("Can't find file:\n" + e.toString());
            System.exit(1);
        }

        // 保存并返回得到的结果
        ArrayList result = new ArrayList();
        result.add(0, recordNum);
        result.add(1, recordList);
        return result;
    }

    public void save2tsv(String targetOnt, int mapNum, MapRecord[] map) {
        //获得目标文件夹的路径
        String lilyFileName = getOntPath(targetOnt) + "/" + "Lily.tsv";

        //构建文件
        try {
            FileWriter out = new FileWriter(lilyFileName);
            BufferedWriter bw = new BufferedWriter(out);
            //写入文件
            for (int i = 0; i < mapNum; i++) {
                bw.write(map[i].sourceLabel + "\t" + map[i].targetLabel + "\t" + String.valueOf(map[i].similarity));
                bw.newLine();
            }

            //关闭文件
            bw.close();
            out.close();
        } catch (IOException e) {
            System.err.println("Can't open result file:\n" + e.toString());
            System.exit(1);
        }
    }

    /***************************************************************************
     * 获取本体路径
     **************************************************************************/
    public String getOntPath(String ontFile) {
        String path = "";
        int pos = ontFile.lastIndexOf('/');
        if (pos > 0) {
            path = ontFile.substring(0, pos + 1);
        }
        return path;
    }

    public String getLocalName(String s) {
        s = s.substring(s.lastIndexOf('#') + 1, s.length());
        return s;
    }

    public int type2int(String s) {
        if (s.equals("=")) {
            return 0;
        } else if (s.equals(">")) {
            return 1;
        } else if (s.equals("<")) {
            return 2;
        } else return -1;
    }
}
