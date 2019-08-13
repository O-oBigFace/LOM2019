/************************************************
 * Source code information
 * -----------------------
 * Original author	 Peng Wang, School of Computer Science & Eng., Southeast University
 * Author email      pwangseu@gmail.com
 * Web               http://ontomapping.googlepages.com
 * Created			 2007-4-26
 * Filename          OWLOntParse.java
 * Version           2.0
 * 
 * Last modified on  2007-4-26
 *               by  Peng Wang
 * -----------------------
 * Functions describe:
 * 解析本体模块
 * 完成本体的各种基本操作
 ***********************************************/
package lily.onto.parse;

import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.PropertyNotFoundException;
import com.hp.hpl.jena.util.iterator.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.*;

/*******************************************************************************
 * Class information -------------------
 * 
 * @author Peng Wang
 * @date 2007-4-26
 * 
 * describe: 解析本体的类
 ******************************************************************************/
public class OWLOntParse {
	// 定义通用本体格式的元语URI
	public Set metaURISet = new HashSet();

	public String rdfStr;

	public String rdfsStr;

	public String owlStr;

	public String xsdStr;
	
	public Set SubAnonStatements;
	public boolean isGetSubAnonStm;
	public Set ObjAnonStatements;
	public boolean isGetObjAnonStm;

	// 类参数初始化
	@SuppressWarnings("unchecked")
	public OWLOntParse() {
		rdfStr = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
		rdfsStr = "http://www.w3.org/2000/01/rdf-schema#";
		owlStr = "http://www.w3.org/2002/07/owl#";
		xsdStr = "http://www.w3.org/2001/XMLSchema#";
		metaURISet.add(rdfStr);
		metaURISet.add(rdfsStr);
		metaURISet.add(owlStr);
		metaURISet.add(xsdStr);
		SubAnonStatements=new HashSet();
		ObjAnonStatements=new HashSet();
		isGetSubAnonStm = false;
		isGetObjAnonStm = false;
	}

	/***************************************************************************
	 * Present a class, then read all classes. Use occurs check to prevent
	 * getting stuck in a loop
	 **************************************************************************/
	public int listAllConcepts(String Concepts[], OntModel m) {
		// create an iterator over the root classes that are not anonymous class
		// expressions
		int count = 0;

		Iterator i = m.listClasses();

		while (i.hasNext()) {
			OntProperty c_temp = (OntProperty) i.next();

			// If the class is not anonymous, output it.
			if (!c_temp.isAnon()) {
				Concepts[count] = c_temp.getLocalName();
				count++;
			}
		}
		return count;
	}

	/***************************************************************************
	 * 列举全部的概念，不考虑baseURI和是否匿名
	 **************************************************************************/
	public ArrayList listFullConcepts(OntModel m) {
		// create an iterator over the root classes that are not anonymous class
		// expressions
		ArrayList result = new ArrayList();
		ArrayList cList = new ArrayList();
		int count = 0;

		Iterator i = m.listClasses();

		while (i.hasNext()) {
			OntClass c_temp = (OntClass) i.next();
			// String cName=c_temp.toString();
			cList.add(count, c_temp);
			count++;
		}
		result.add(0, count);
		result.add(1, cList);
		return result;
	}

	/***************************************************************************
	 * 列举全部的概念，不考虑baseURI和是否匿名
	 **************************************************************************/
	public int listAllConceptsFilterBaseURI(String Concepts[], OntModel m,
			String baseURI) {
		// create an iterator over the root classes that are not anonymous class
		// expressions
		int count = 0;

		Iterator i = m.listClasses();

		while (i.hasNext()) {
			OntProperty c_temp = (OntProperty) i.next();

			// If the class is not anonymous, output it.
			if (!c_temp.isAnon() && c_temp.isClass()) {
				if (c_temp.getNameSpace().contains(baseURI)) {
					Concepts[count] = c_temp.getLocalName();
					count++;

					// 为了消除隐式概念声明,这里需要补充对Class显式声明
					Property p = m
							.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
					Object o = m
							.getResource("http://www.w3.org/2002/07/owl#Class");
					if (!m.containsLiteral(c_temp, p, o)) {
						System.out.println("隐式声明Class!");
						m.add(c_temp, p, (RDFNode)o);
					}

				}
			}
		}
		return count;
	}

	/***************************************************************************
	 * 列举全部的概念，用baseURI过滤
	 **************************************************************************/
	@SuppressWarnings("unchecked")
	public ArrayList listAllConceptsFilterBaseURI(OntModel m, String baseURI) {
		// create an iterator over the root classes that are not anonymous class
		// expressions
		ArrayList conceptList = new ArrayList();
		int count = 0;
		ArrayList result = new ArrayList();

		Iterator i = m.listClasses();

		while (i.hasNext()) {
			OntClass c_temp = (OntClass) i.next();

			// If the class is not anonymous, output it.
			if (!c_temp.isAnon() && c_temp.isClass()) {
				if (c_temp.getNameSpace().contains(baseURI)) {
					String lName=c_temp.getLocalName();
					if (m.getOntClass(baseURI+lName)!=null){
						conceptList.add(count, c_temp.getLocalName());
						count++;
					}
					
					// 为了消除隐式概念声明,这里需要补充对Class显式声明
					Property p = m
							.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
					Object o = m
							.getResource("http://www.w3.org/2002/07/owl#Class");
					if (!m.containsLiteral(c_temp, p, o)) {
						System.out.println("隐式声明Class!");
						m.add(c_temp, p, (RDFNode)o);
					}
				}
				/* 处理%2C问题 */
				if (c_temp.toString().contains("%2")) {
					/* 判断真实的localName和baseURL */
					String rUrl = null;
					String rlname = null;
					int cPos = -1;
					int xPos = -1;
					cPos = c_temp.toString().lastIndexOf("%2");
					xPos = c_temp.toString().lastIndexOf("#");
					if (cPos > -1 && xPos > -1 && cPos > xPos) {
						rUrl = c_temp.toString().substring(0, xPos + 1);
						rlname = c_temp.toString().substring(xPos + 1,
								c_temp.toString().length());
						if (baseURI.equalsIgnoreCase(rUrl)) {
							conceptList.add(count, rlname);
							count++;
						}
					}
				}
			}
		}
		result.add(0, count);
		result.add(1, conceptList);
		return result;
	}

	/***************************************************************************
	 * Read all properties. Use occurs check to prevent getting stuck in a loop
	 **************************************************************************/
	public int listAllRelations(String Relations[], OntModel m) {
		// create an iterator over the root properties that are not anonymous
		// property expressions
		int count = 0;
		ExtendedIterator i = m.listObjectProperties();

		// Output all the ObjectProperties
		while (i.hasNext()) {
			OntProperty p1_temp = (OntProperty) i.next();

			// If the property is not anonymous, output it.
			if (!p1_temp.isAnon()) {
				Relations[count] = p1_temp.getLocalName();
				count++;
			}
		}

		// Output all the DatatypeProperties
		ExtendedIterator j = m.listDatatypeProperties();

		while (j.hasNext()) {
			OntProperty p2_temp = (OntProperty) j.next();

			// If the property is not anonymous, output it.
			if (!p2_temp.isAnon()) {
				Relations[count] = p2_temp.getLocalName();
				count++;
			}
		}

		// Output all the FunctionalProperties
		ExtendedIterator k = m.listFunctionalProperties();

		while (k.hasNext()) {
			OntProperty p3_temp = (OntProperty) k.next();

			// If the property is not anonymous, output it.
			if (!p3_temp.isAnon()) {
				Relations[count] = p3_temp.getLocalName();
				count++;
			}
		}
		return count;
	}

	/***************************************************************************
	 * 列举全部的属性，不考虑baseURI和是否匿名
	 **************************************************************************/
	public ArrayList listFullRelations(OntModel m) {
		// create an iterator over the root properties that are not anonymous
		// property expressions
		ArrayList result = new ArrayList();
		ArrayList pList = new ArrayList();
		int count = 0;

		ExtendedIterator i = m.listDatatypeProperties();
		// Output all the Properties
		while (i.hasNext()) {
			OntProperty p_temp = (OntProperty) i.next();
			// String pName;
			// pName=p_temp.toString();
			pList.add(count, p_temp);
			count++;
		}
		i = m.listObjectProperties();
		// Output all the Properties
		while (i.hasNext()) {
			OntProperty p_temp = (OntProperty) i.next();
			// String pName;
			// pName=p_temp.toString();
			pList.add(count, p_temp);
			count++;
		}
		result.add(0, count);
		result.add(1, pList);
		return result;
	}

	/***************************************************************************
	 * 列举全部的DatatypeProperty，不考虑baseURI和是否匿名
	 **************************************************************************/
	public ArrayList listFullDatatypeProperty(OntModel m) {
		// create an iterator over the root properties that are not anonymous
		// property expressions
		ArrayList result = new ArrayList();
		ArrayList pList = new ArrayList();
		int count = 0;
		ExtendedIterator i = m.listDatatypeProperties();

		// Output all the Properties
		while (i.hasNext()) {
			OntProperty p_temp = (OntProperty) i.next();
			// String pName;
			// pName=p_temp.toString();
			pList.add(count, p_temp);
			count++;
		}
		result.add(0, count);
		result.add(1, pList);
		return result;
	}

	/***************************************************************************
	 * 列举全部的ObjectProperty，不考虑baseURI和是否匿名
	 **************************************************************************/
	public ArrayList listFullObjectProperty(OntModel m) {
		// create an iterator over the root properties that are not anonymous
		// property expressions
		ArrayList result = new ArrayList();
		ArrayList pList = new ArrayList();
		int count = 0;
		ExtendedIterator i = m.listObjectProperties();
		// Output all the Properties
		while (i.hasNext()) {
			OntProperty p_temp = (OntProperty) i.next();
			// String pName;
			// pName=p_temp.toString();
			pList.add(count, p_temp);
			count++;
		}
		result.add(0, count);
		result.add(1, pList);
		return result;
	}

	/***************************************************************************
	 * Read all objectproperties. Use occurs check to prevent getting stuck in a
	 * loop
	 **************************************************************************/
	public int listAllObjectRelations(String Relations[], OntModel m) {
		// create an iterator over the root properties that are not anonymous
		// property expressions
		int count = 0;
		ExtendedIterator i = m.listObjectProperties();

		// Output all the ObjectProperties
		while (i.hasNext()) {
			OntProperty p1_temp = (OntProperty) i.next();

			// If the property is not anonymous, output it.
			if (!p1_temp.isAnon()) {
				Relations[count] = p1_temp.getLocalName();
				count++;
			}
		}

		return count;
	}

	/***************************************************************************
	 * 列举全部的关系，用baseURI过滤
	 **************************************************************************/
	public int listAllObjectRelationsURI(String Relations[], OntModel m,
			String baseURI) {
		// create an iterator over the root properties that are not anonymous
		// property expressions
		int count = 0;
		ExtendedIterator i = m.listObjectProperties();

		// Output all the ObjectProperties
		while (i.hasNext()) {
			OntProperty p1_temp = (OntProperty) i.next();

			// If the property is not anonymous, output it.
			if (!p1_temp.isAnon()) {
				if (p1_temp.getNameSpace().contains(baseURI)) {
					Relations[count] = p1_temp.getLocalName();
					count++;
				}
			}
		}

		return count;
	}

	@SuppressWarnings("unchecked")
	public ArrayList listAllObjectRelationsURI(OntModel m, String baseURI) {
		// create an iterator over the root properties that are not anonymous
		// property expressions
		ArrayList relationList = new ArrayList();
		int count = 0;
		ArrayList result = new ArrayList();
		ExtendedIterator i = m.listObjectProperties();

		// Output all the ObjectProperties
		while (i.hasNext()) {
			OntProperty p1_temp = (OntProperty) i.next();

			// If the property is not anonymous, output it.
			if (!p1_temp.isAnon()) {
				if (p1_temp.getNameSpace().contains(baseURI)) {
					relationList.add(count, p1_temp.getLocalName());
					count++;
				}
			}
		}
		result.add(0, count);
		result.add(1, relationList);
		return result;
	}

	/***************************************************************************
	 * Read all datatypeproperties. Use occurs check to prevent getting stuck in
	 * a loop
	 **************************************************************************/
	public int listAllDatatypeRelations(String Relations[], OntModel m) {
		// create an iterator over the root properties that are not anonymous
		// property expressions
		int count = 0;

		// Output all the DatatypeProperties
		ExtendedIterator j = m.listDatatypeProperties();

		while (j.hasNext()) {
			OntProperty p2_temp = (OntProperty) j.next();

			// If the property is not anonymous, output it.
			if (!p2_temp.isAnon()) {
				Relations[count] = p2_temp.getLocalName();
				count++;
			}
		}
		return count;
	}

	/***************************************************************************
	 * 列举全部的DatatypeProperty，用baseURI过滤
	 **************************************************************************/
	public int listAllDatatypeRelationsURI(String Relations[], OntModel m,
			String baseURI) {
		// create an iterator over the root properties that are not anonymous
		// property expressions
		int count = 0;

		// Output all the DatatypeProperties
		ExtendedIterator j = m.listDatatypeProperties();

		while (j.hasNext()) {
			OntProperty p2_temp = (OntProperty) j.next();

			// If the property is not anonymous, output it.
			if (!p2_temp.isAnon()) {
				if (p2_temp.getNameSpace().contains(baseURI)) {
					Relations[count] = p2_temp.getLocalName();
					count++;
				}
			}
		}
		return count;
	}

	@SuppressWarnings("unchecked")
	public ArrayList listAllDatatypeRelationsURI(OntModel m, String baseURI) {
		// create an iterator over the root properties that are not anonymous
		// property expressions
		ArrayList relationList = new ArrayList();
		int count = 0;
		ArrayList result = new ArrayList();

		// Output all the DatatypeProperties
		ExtendedIterator j = m.listDatatypeProperties();

		while (j.hasNext()) {
			OntProperty p2_temp = (OntProperty) j.next();

			// If the property is not anonymous, output it.
			if (!p2_temp.isAnon()) {
				if (p2_temp.getNameSpace().contains(baseURI)) {
					relationList.add(count, p2_temp.getLocalName());
					count++;
				}
			}
		}
		result.add(count);
		result.add(relationList);
		return result;
	}

	/***************************************************************************
	 * Read all instances.
	 **************************************************************************/
	public int listAllInstances(String Instances[], OntModel m) {
		int count = 0;
		// create an iterator over the instances that are not anonymous
		// expressions
		ExtendedIterator i = m.listIndividuals();

		// Output all the ObjectProperties
		while (i.hasNext()) {
			Individual indi_temp = (Individual) i.next();

			// If the instance is not anonymous, output it.
			if (!indi_temp.isAnon()
					&& !indi_temp.toString().equals(
							"http://www.w3.org/1999/02/22-rdf-syntax-ns#nil")) {
				Instances[count] = indi_temp.getLocalName();
				// if (Instances[count].equals("nil"))
				// System.out.println(indi_temp);
				count++;
			}
		}
		return count;
	}

	/***************************************************************************
	 * Read all instances.
	 **************************************************************************/
	@SuppressWarnings("unchecked")
	public ArrayList listAllInstances(OntModel m) {
		ArrayList insList = new ArrayList();
		int count = 0;
		ArrayList result = new ArrayList();

		// create an iterator over the instances that are not anonymous
		// expressions
		ExtendedIterator i = m.listIndividuals();

		// Output all the ObjectProperties
		while (i.hasNext()) {
			Individual indi_temp = (Individual) i.next();

			// If the instance is not anonymous, output it.
			if (!indi_temp.isAnon()
					&& !indi_temp.toString().equals(
							"http://www.w3.org/1999/02/22-rdf-syntax-ns#nil")) {
				insList.add(count, indi_temp.getLocalName());
				count++;
			}
		}
		result.add(0, count);
		result.add(1, insList);
		return result;
	}

	/***************************************************************************
	 * 列举全部的实例，带URI
	 **************************************************************************/
	public ArrayList listFullInstances(OntModel m) {
		ArrayList result = new ArrayList();
		ArrayList insList = new ArrayList();
		int count = 0;

		// create an iterator over the instances that are not anonymous
		// expressions
		ExtendedIterator i = m.listIndividuals();

		// Output all the ObjectProperties
		while (i.hasNext()) {
			Individual indi_temp = (Individual) i.next();

			// If the instance is not anonymous, output it.
			if (!indi_temp.toString().equals(
					"http://www.w3.org/1999/02/22-rdf-syntax-ns#nil")) {
				insList.add(count, indi_temp);
				count++;
			}
		}
		result.add(0, count);
		result.add(1, insList);
		return result;
	}

	/***************************************************************************
	 * Given a class, list its properties
	 **************************************************************************/
	public ArrayList listDomainPropertyOfConcept(OntClass c_Given,
			boolean direct) {
		ArrayList ls = new ArrayList();
		// create an iterator over the properties that associated with the class
		ExtendedIterator i=null;
		try {
			i=c_Given.listDeclaredProperties(direct);
		}
		catch(ConversionException e){
			return ls;
		}

		// output all the properties
		// System.out.println("The properties of " + c_Given.getLocalName() +
		// ":");

		while (i.hasNext()) {
			OntProperty p_temp = (OntProperty) i.next();
			if (!ls.contains(p_temp)) {
				ls.add(p_temp);
			}
		}
		return ls;
	}

	/***************************************************************************
	 * 列举概念的Range Property
	 **************************************************************************/
	public ArrayList listRangePropertyOfConcept(OntClass c_Given) {
		ArrayList ls = new ArrayList();
		OntModel m = c_Given.getOntModel();
		Property p = m
				.getProperty("http://www.w3.org/2000/01/rdf-schema#Range");
		Selector sl = new SimpleSelector(null, p, c_Given);
		for (StmtIterator i = m.listStatements(sl); i.hasNext();) {
			Statement st = i.nextStatement();
			Resource rs = st.getSubject();
			OntProperty p_temp = m.getOntProperty(rs.toString());
			if (!ls.contains(p_temp)) {
				ls.add(p_temp);
			}
		}
		return ls;
	}

	/***************************************************************************
	 * Given a class, list its instances
	 **************************************************************************/
	public ArrayList listInstanceOfConcept(OntClass c_Given) {
		int count = 0;
		ArrayList result = new ArrayList();
		ArrayList insList = new ArrayList();
		if (c_Given!=null){
			// create an iterator over the instances that associated with the class
			ExtendedIterator i = c_Given.listInstances();

			// output all the instances
			// System.out.println("The instances of " + c_Given.getLocalName() +
			// ":");

			while (i.hasNext()) {
				Individual indi_temp = (Individual) i.next();
				insList.add(count, indi_temp);
				count++;
				// if the property is not empty, output it
				// if (!indi_temp.isAnon())
				// {
				// System.out.println(indi_temp.getLocalName());
				// }
			}
		}
		result.add(0, count);
		result.add(1, insList);
		return result;
	}

	/***************************************************************************
	 * 列举属性参与的Property
	 **************************************************************************/
	public ArrayList listInstanceOfProperty(OntModel m, OntProperty p_Given) {
		int count = 0;
		ArrayList result = new ArrayList();
		ArrayList stmList = new ArrayList();
		// create an iterator over the instances that associated with the class
		// p参与的所有三元组，p作为谓词
		Selector s1 = new SimpleSelector(null, p_Given, (RDFNode) null);

		for (StmtIterator i = m.listStatements(s1); i.hasNext();) {
			Statement st = i.nextStatement();
			Resource rs = st.getSubject();
			RDFNode ro = st.getObject();

			if ((m.getIndividual(rs.toString()) != null)// 判断s是instance
					&& (m.getIndividual(ro.toString()) != null || ro
							.isLiteral())) {// 判断o是instance或value
				stmList.add(count, st);
				count++;
			}
		}
		result.add(0, count);
		result.add(1, stmList);
		return result;
	}

	/***************************************************************************
	 * Given a class, list all its subclasses
	 **************************************************************************/
	public int listSubClassOfConcept(int count, String Concept[],
			OntClass c_Given) {
		// create an iterator over the subclasses that associated with the class

		ExtendedIterator i = c_Given.listSubClasses(false);

		// System.out.println("The SubClasses:" + c_Given.getLocalName() + ":");
		while (i.hasNext()) {
			OntClass c_temp = (OntClass) i.next();
			// System.out.println(c_temp.toString());
			if (c_temp != null) {
				// If the class is not anonymous, output it.
				if (!c_temp.isAnon()) {
					// System.out.println(c_temp.getLocalName());
					Concept[count] = c_temp.getLocalName();
					count++;

					// recursive the SubClass
					count = listSubClassOfConcept(count, Concept, c_temp);
				} else {
					if (c_temp.asClass().isUnionClass()) {
						UnionClass tempc = c_temp.asClass().asUnionClass();
						for (ExtendedIterator ExIter = tempc.listOperands(); ExIter
								.hasNext();) {
							OntClass tempind = (OntClass) ExIter.next();
							// System.out.println(tempind.getLocalName().toString());
							Concept[count] = tempind.getLocalName();
							count++;
						}
					}
				}
			}

		}
		return count;
	}

	/***************************************************************************
	 * 列出给定概念的全部SubClass，同时标记出与 给定概念的距离， 这里采用的是递归算法
	 * 数据结构是[[concept,level],[c,level],...]
	 **************************************************************************/
	public ArrayList listSubClassOfConceptWithDistance(OntClass c_Given,
			int level, Set set) {
		int cLevel;
		cLevel = level;
		ArrayList list = new ArrayList();
		ExtendedIterator i = c_Given.listSubClasses(true);
		while (i.hasNext()) {
			ArrayList l0 = new ArrayList();
			ArrayList l = new ArrayList();
			OntClass c_temp = (OntClass) i.next();
			if (set.contains(c_temp)) {
				return l;
			} else {
				set.add(c_temp);
			}
			cLevel = level + 1;
			l0.add(0, c_temp);
			l0.add(1, cLevel);
			list.add(l0);
			l = listSubClassOfConceptWithDistance(c_temp, cLevel, set);
			if (!l.isEmpty()) {
				int subLevel = cLevel + 1;
				for (Iterator j = l.iterator(); j.hasNext();) {
					ArrayList cx = (ArrayList) j.next();
					list.add(cx);
				}
			}
		}
		return list;
	}
	
	public Set listSubClassOfConceptInScale(OntClass c, int scale) {
		Set result=new HashSet();
		for (int i=0;i<scale;i++){
			for (Iterator it=c.listSubClasses(true);it.hasNext();){
				OntClass tc= (OntClass)it.next();
				result.add(tc);
				result.addAll(listSubClassOfConceptInScale(tc,scale-1));
			}
		}
		return result;
	}
	
	public Set listSuperClassOfConceptInScale(OntClass c, int scale) {
		Set result=new HashSet();
		for (int i=0;i<scale;i++){
			for (Iterator it=c.listSuperClasses(true);it.hasNext();){
				OntClass tc= (OntClass)it.next();
				result.add(tc);
				result.addAll(listSuperClassOfConceptInScale(tc,scale-1));
			}
		}
		return result;
	}

	/***************************************************************************
	 * 列出给定属性的全部SubProperty，同时标记出与 给定概念的距离， 这里采用的是递归算法
	 * 数据结构是[[property,level],[p,level],...]
	 **************************************************************************/
	public ArrayList listSubPropertyOfPropertyWithDistance(OntProperty p_Given,
			int level) {
		int pLevel;
		pLevel = level;
		ArrayList list = new ArrayList();
		ExtendedIterator i = p_Given.listSubProperties(true);
		while (i.hasNext()) {
			OntProperty p_temp = (OntProperty) i.next();
			pLevel = level + 1;
			ArrayList l0 = new ArrayList();
			l0.add(0, p_temp);
			l0.add(1, pLevel);
			list.add(l0);
			ArrayList l = new ArrayList();
			l = listSubPropertyOfPropertyWithDistance(p_temp, pLevel);
			if (!l.isEmpty()) {
				int subLevel = pLevel + 1;
				for (Iterator j = l.iterator(); j.hasNext();) {
					ArrayList px = (ArrayList) j.next();
					list.add(px);
				}
			}
		}
		return list;
	}

	/***************************************************************************
	 * 列出给定属性的全部SuperProperty，同时标记出与 给定概念的距离， 这里采用的是递归算法
	 * 数据结构是[[property,level],[p,level],...]
	 **************************************************************************/
	public ArrayList listSuperPropertyOfPropertyWithDistance(
			OntProperty p_Given, int level) {
		int pLevel;
		pLevel = level;
		ExtendedIterator i = null;
		ArrayList list = new ArrayList();
		try{
			i = p_Given.listSuperProperties(true);
		}
		catch (ConversionException e){
			return list;
		}
		
		while (i.hasNext()) {
			OntProperty p_temp = (OntProperty) i.next();
			pLevel = level + 1;
			ArrayList l0 = new ArrayList();
			l0.add(0, p_temp);
			l0.add(1, pLevel);
			list.add(l0);
			ArrayList l = new ArrayList();
			l = listSuperPropertyOfPropertyWithDistance(p_temp, pLevel);
			if (!l.isEmpty()) {
				int subLevel = pLevel + 1;
				for (Iterator j = l.iterator(); j.hasNext();) {
					ArrayList px = (ArrayList) j.next();
					list.add(px);
				}
			}
		}
		return list;
	}

	/***************************************************************************
	 * 列出给定概念的全部Sibling Class
	 **************************************************************************/
	public ArrayList listSiblingsOfConcept(OntClass c_Given) {
		ArrayList list = new ArrayList();
		
		try{
			c_Given.listSuperClasses(true);
		}
		catch (ConversionException e){
			return list;
		}
		
		/* 找到直接superClass */
		ExtendedIterator i = c_Given.listSuperClasses(true);
		while (i.hasNext()) {
			OntClass c_super = (OntClass) i.next();
			/* 得到当前Super Class的直接sub Class */
			ExtendedIterator j = c_super.listSubClasses(true);
			while (j.hasNext()) {
				OntClass cx = (OntClass) j.next();
				if (!list.contains(cx)) {// 保证不重复
					list.add(cx);
				}
			}
		}
		list.remove(c_Given);// 去掉本身
		return list;
	}

	/***************************************************************************
	 * 列出给定属性的全部Sibling Property
	 **************************************************************************/
	public ArrayList listSiblingsOfProperty(OntProperty p_Given) {
		ArrayList list = new ArrayList();
		/* 找到直接superProperty */
		ExtendedIterator i = p_Given.listSubProperties(true);
		while (i.hasNext()) {
			OntProperty p_temp = (OntProperty) i.next();
			/* 得到当前Super Property的直接sub Property */
			ExtendedIterator j = p_temp.listSubProperties(true);
			while (j.hasNext()) {
				OntProperty px = (OntProperty) j.next();
				if (!list.contains(px)) {// 保证不重复
					list.add(px);
				}
			}
		}
		list.remove(p_Given);// 去掉本身
		return list;
	}

	/***************************************************************************
	 * 列出给定概念的全部SuperClass，同时标记出与 给定概念的距离， 这里采用的是递归算法
	 * 数据结构是[[concept,level],[c,level],...]
	 **************************************************************************/
	public ArrayList listSuperClassOfConceptWithDistance(OntClass c_Given,
			int level, Set set) {
		int cLevel;
		cLevel = level;
		ArrayList list = new ArrayList();
		
		try{
			c_Given.listSuperClasses(true);
		}
		catch (ConversionException e){
			return list;
		}

		ExtendedIterator i = c_Given.listSuperClasses(true);
		while (i.hasNext()) {
			ArrayList l0 = new ArrayList();
			ArrayList l = new ArrayList();
			OntClass c_temp = (OntClass) i.next();
			if (set.contains(c_temp)) {
				return l;
			} else {
				set.add(c_temp);
			}
			cLevel = level + 1;
			l0.add(0, c_temp);
			l0.add(1, cLevel);
			list.add(l0);
			l = listSuperClassOfConceptWithDistance(c_temp, cLevel, set);
			if (!l.isEmpty()) {
				int subLevel = cLevel + 1;
				for (Iterator j = l.iterator(); j.hasNext();) {
					ArrayList cx = (ArrayList) j.next();
					list.add(cx);
				}
			}
		}
		return list;
	}

	/***************************************************************************
	 * Given a class, list all its Superclasses
	 **************************************************************************/
	public void listSuperClassOfConcept(PrintStream out, OntClass c_Given) {
		// create an iterator over the Superclasses that associated with the
		// class
		ExtendedIterator i = c_Given.listSuperClasses(false);

		// System.out.println("The SubClasses:" + c_Given.getLocalName() + ":");
		while (i.hasNext()) {
			OntClass c_temp = (OntClass) i.next();

			// If the class is not anonymous, output it.
			if (c_temp != null) {
				// If the class is not anonymous, output it.
				if (!c_temp.isAnon()) {
					System.out.println(c_temp.getLocalName());

					// recursive the SubClass
					listSuperClassOfConcept(System.out, c_temp);
				} else {
					if (c_temp.asClass().isIntersectionClass()) {
						IntersectionClass tempc = c_temp.asClass()
								.asIntersectionClass();
						for (ExtendedIterator ExIter = tempc.listOperands(); ExIter
								.hasNext();) {
							OntProperty tempind = (OntProperty) ExIter.next();
							System.out.println(tempind.getLocalName()
									.toString());
						}
					}
				}
			} else {
				return;
			}
		}
	}

	/***************************************************************************
	 * Given a class, list all its direct subclasses 不考虑是否匿名
	 **************************************************************************/
	public ArrayList listDirectSubClassOfConcept(OntClass c_Given) {
		ArrayList list = new ArrayList();
        if (c_Given == null)
            return list; //TODO: check this error
		// create an iterator over the direct subclasses that associated with
		// the class
		ExtendedIterator i = c_Given.listSubClasses(true);

		// System.out.println("The Direct SubClasses:" + c_Given.getLocalName()
		// + ":");
		while (i.hasNext()) {
			OntClass c_temp = (OntClass) i.next();
			list.add(c_temp);
		}
		return list;
	}
	
	public ArrayList listDirectSuperClassOfConcept(OntClass c_Given) {
		ArrayList list = new ArrayList();
        if (c_Given == null)
            return list; //TODO: check this error
		// create an iterator over the direct subclasses that associated with
		// the class
		ExtendedIterator i = c_Given.listSuperClasses(true);

		// System.out.println("The Direct SubClasses:" + c_Given.getLocalName()
		// + ":");
		while (i.hasNext()) {
			OntClass c_temp = (OntClass) i.next();
			list.add(c_temp);
		}
		return list;
	}
	
	public ArrayList listAllSubClassOfConcept(OntClass c_Given) {
		ArrayList list = new ArrayList();
        if (c_Given == null)
            return list; //TODO: check this error
		// create an iterator over the direct subclasses that associated with
		// the class
		ExtendedIterator i = c_Given.listSubClasses(false);

		// System.out.println("The Direct SubClasses:" + c_Given.getLocalName()
		// + ":");
		while (i.hasNext()) {
			OntClass c_temp = (OntClass) i.next();
			list.add(c_temp);
			list.addAll(listAllSubClassOfConcept(c_temp));
		}
		Set tset=new HashSet();
		tset.addAll((ArrayList)(list.clone()));
		list.clear();
		list.addAll(tset);
		return list;
	}
	
	public ArrayList listAllSuperClassOfConcept(OntClass c_Given) {
		ArrayList list = new ArrayList();
		if (c_Given == null)
			return list; //TODO: check this error
		// create an iterator over the direct subclasses that associated with
		// the class
		ExtendedIterator i = c_Given.listSuperClasses(false);

		// System.out.println("The Direct SubClasses:" + c_Given.getLocalName()
		// + ":");
		while (i.hasNext()) {
			OntClass c_temp = (OntClass) i.next();
			list.add(c_temp);
			list.addAll(listAllSuperClassOfConcept(c_temp));
		}

		Set tset=new HashSet();
		tset.addAll((ArrayList)(list.clone()));
		list.clear();
		list.addAll(tset);
		return list;
	}

	/***************************************************************************
	 * Given a class, list all its direct subclasses
	 **************************************************************************/
	public void listDirectSuperClassOfConcept(PrintStream out, OntClass c_Given) {
		// create an iterator over the direct superclasses that associated with
		// the class
		ExtendedIterator i = c_Given.listSuperClasses(true);

		System.out.println("The Direct SuperClasses:" + c_Given.getLocalName()
				+ ":");
		while (i.hasNext()) {
			OntClass c_temp = (OntClass) i.next();

			// If the class is not anonymous, output it.
			if (!c_temp.isAnon()) {
				System.out.println(c_temp.getLocalName());

			}
		}
	}

	/***************************************************************************
	 * Given a class, list all its equivalent classes In Jena, if class A
	 * declare equivalentClass B, then we can get A=B from A, but we can not get
	 * B=A from B, if the decalaration has not in B The following function has
	 * not solved the problem, we can just get the equivalent class from A's
	 * declaration. Last modified on $Date: 2006/8/13 $ by $Author: Peng Wang $
	 **************************************************************************/
	public void listEquivalentClassOfConcept(PrintStream out, OntClass c_Given) {
		// create an iterator over the equivalent that associated with the class
		ExtendedIterator i = c_Given.listEquivalentClasses();

		// System.out.println("The Equivalent Classes:" + c_Given.getLocalName()
		// + ":");
		while (i.hasNext()) {
			OntClass c_temp = (OntClass) i.next();

			// If the class is not anonymous, output it.
			if (!c_temp.isAnon()) {
				System.out.println(c_temp.getLocalName());

				// recursive the equivalent class
				listEquivalentClassOfConcept(System.out, c_temp);
			} else {
				return;
			}
		}
	}

	/***************************************************************************
	 * Given a class, list all its disjoint classes This function does not be
	 * tesed strictly by $Author: Peng Wang $ 匿名的节点也考虑在内
	 **************************************************************************/
	public ArrayList listDisjointClassOfConcept(OntClass c_Given) {
		ArrayList list = new ArrayList();
		// create an iterator over the DisjointWith that associated with the
		// class
		ExtendedIterator i = c_Given.listDisjointWith();

		// System.out.println("The DisjointWith Classes:" +
		// c_Given.getLocalName() + ":");
		while (i.hasNext()) {
			OntClass c_temp = (OntClass) i.next();
			list.add(c_temp);
		}
		return list;
	}

	/***************************************************************************
	 * 得到指定概念的complementOf Classes 通过搜索三元组获得
	 **************************************************************************/
	public ArrayList listComplementClassOfConcept(OntClass c_Given) {
		ArrayList list = new ArrayList();
		OntModel m = c_Given.getOntModel();
		Property p = m
				.getProperty("http://www.w3.org/2002/07/owl#complementOf");
		Selector s = new SimpleSelector(c_Given, p, (RDFNode) null);
		for (StmtIterator it = m.listStatements(s); it.hasNext();) {
			Statement s_temp = it.next();
			RDFNode o_temp = s_temp.getObject();
			OntClass c = m.getOntClass(o_temp.toString());
			if ((c != null) && (!list.contains(c))) {
				list.add(c);
			}
		}
		return list;
	}

	/***************************************************************************
	 * Given a Relation, list all its sub properties
	 **************************************************************************/
	public void listSubPropertiesOfRelation(PrintStream out, OntProperty p_Given) {
		// create an iterator over the sub properties that associated with the
		// relation
		ExtendedIterator i = p_Given.listSubProperties();

		// System.out.println("The Sub Properties of :" + p_Given.getLocalName()
		// + ":");
		while (i.hasNext()) {
			OntProperty p_temp = (OntProperty) i.next();

			// If the property is not anonymous, output it.
			if (!p_temp.isAnon()) {
				System.out.println(p_temp.getLocalName());

				// recursive the SubProperties
				listSubPropertiesOfRelation(System.out, p_temp);
			} else {
				return;
			}
		}
	}

	/***************************************************************************
	 * Given a Relation, list all its super properties
	 **************************************************************************/
	public void listSuperPropertiesOfRelation(PrintStream out,
			OntProperty p_Given) {
		// create an iterator over the super properties that associated with the
		// relation
		ExtendedIterator i = p_Given.listSuperProperties();

		// System.out.println("The Super Properties of :" +
		// p_Given.getLocalName() + ":");
		while (i.hasNext()) {
			OntProperty p_temp = (OntProperty) i.next();

			// If the property is not anonymous, output it.
			if (!p_temp.isAnon()) {
				System.out.println(p_temp.getLocalName());

				// recursive the SuperProperties
				listSuperPropertiesOfRelation(System.out, p_temp);
			} else {
				return;
			}
		}
	}

	/***************************************************************************
	 * Given a Relation, list all its equivalent properties
	 **************************************************************************/
	public void listEquivalentrPropertiesOfRelation(PrintStream out,
			OntProperty p_Given) {
		// create an iterator over the equivalent properties that associated
		// with the relation
		ExtendedIterator i = p_Given.listEquivalentProperties();

		// System.out.println("The Equivalent Properties of :" +
		// p_Given.getLocalName() + ":");
		while (i.hasNext()) {
			OntProperty p_temp = (OntProperty) i.next();

			// If the property is not anonymous, output it.
			if (!p_temp.isAnon()) {
				System.out.println(p_temp.getLocalName());

				// recursive all the Equivalent Properties
				listEquivalentrPropertiesOfRelation(System.out, p_temp);
			} else {
				return;
			}
		}
	}

	/***************************************************************************
	 * Given a Relation, list all its domain entities
	 * 这里做简化，不处理复杂概念
	 **************************************************************************/
	public int listDomainOfRelation(String Domain[], OntProperty p_Given) {
		// create an iterator over the domain that associated with the relation
		int count = 0;
		ExtendedIterator i = p_Given.listDomain();

		// System.out.println("The Domain of Properties " +
		// p_Given.getLocalName() + ":");
		while (i.hasNext()) {
			OntResource res_temp = (OntResource) i.next();
			// If the domain is not empty, output it.
			if (res_temp != null) {
				Domain[count] = res_temp.getLocalName();
				count++;				
//				if (!res_temp.isAnon()) {
//					// System.out.println(res_temp.getLocalName());
//					Domain[count] = res_temp.getLocalName();
//					count++;
//				} else if (res_temp.isClass()){
//					if (res_temp.asClass().isUnionClass()){
//						UnionClass tempc = res_temp.asClass().asUnionClass();
//						for (ExtendedIterator ExIter = tempc.listOperands(); ExIter
//								.hasNext();) {
//							OntClass tempind = (OntClass) ExIter.next();
//							// System.out.println(tempind.getLocalName().toString());
//							Domain[count] = tempind.getLocalName();
//							count++;
//						}
//					}					
//				}
			}

		}
		return count;
	}

	/***************************************************************************
	 * Given a Relation, list all its Range entities
	 **************************************************************************/
	public int listRangeOfRelation(String Range[], OntProperty p_Given) {
		// create an iterator over the Range that associated with the relation
		int count = 0;
		ExtendedIterator i = p_Given.listRange();

		// System.out.println("The Range of Properties " +
		// p_Given.getLocalName() + ":");
		while (i.hasNext()) {
			OntResource res_temp = (OntResource) i.next();

			// If the range is not empty, output it.
			if (res_temp != null) {
				Range[count] = res_temp.getLocalName();
				count++;
//				if (!res_temp.isAnon()) {
//					// System.out.println(res_temp.getLocalName());
//					Range[count] = res_temp.getLocalName();
//					count++;
//				} else {
//					UnionClass tempc = res_temp.asClass().asUnionClass();
//					for (ExtendedIterator ExIter = tempc.listOperands(); ExIter
//							.hasNext();) {
//						OntClass tempind = (OntClass) ExIter.next();
//						// System.out.println(tempind.getLocalName().toString());
//						Range[count] = tempind.getLocalName();
//						count++;
//					}
//				}
			}
		}
		return count;
	}

	/***************************************************************************
	 * Given a Instance, list all the classes it belongs The function just can
	 * get the direct class of a instance, the full class list would call other
	 * functions.
	 **************************************************************************/
	public int listConceptsOfInstance(String Concept[], Individual Indv_Given,
			boolean is_direct) {
		int count = 0;
		// create an iterator over the classes that associated with the instance
		ExtendedIterator i = Indv_Given.listRDFTypes(is_direct);
		Resource Res_temp;

		// System.out.println("The Classes of Instance " +
		// Indv_Given.getLocalName() + ":");
		while (i.hasNext()) {
			Res_temp = (Resource) i.next();
			OntClass c_temp = null;
			try{
				c_temp= Res_temp.as(OntClass.class);
			}
			catch(ConversionException e){
				continue;
			}			

			// If the class is not anonymous, output it
			if (!Res_temp.isAnon()) {
				// System.out.println(c_temp.getLocalName());
				Concept[count] = c_temp.getLocalName();
				count++;
			}
		}
		return count;
	}

	/***************************************************************************
	 * 获得实例的兄弟实例
	 * 这里只记录数目
	 **************************************************************************/
	public ArrayList listSiblingOfInstance(OntModel m, Individual Indv_Given) {
		ArrayList result = new ArrayList();
		int count = 0;
		// create an iterator over the classes that associated with the instance
		ExtendedIterator i = Indv_Given.listRDFTypes(true);

		while (i.hasNext()) {
			Resource res_temp = (Resource) i.next();
			OntClass c_temp = m.getOntClass(res_temp.toString());

			// 再获得Class对应的实例
			ArrayList tl = new ArrayList();
			tl = this.listInstanceOfConcept(c_temp);
			int n = ((Integer) tl.get(0)).intValue();
			count += n;
		}
		result.add(0, count);
		return result;
	}

	/***************************************************************************
	 * Given a Instance, list all the properties it belongs First, construct a
	 * selector, then use it to get all stataments about the instance, finally,
	 * get the properties of the instance Because a instance I maybe occur as
	 * subject or object, so our idea is: 1. Get all statements whose subject is
	 * I, and extract the properties and property value 2. Get all statements
	 * whose object is I, and extract the properties
	 **************************************************************************/
	public void listPropertiesOfInstance(PrintStream out, OntModel m,
			Individual Indv_Given) {
		// First, treat instance as subject
		// create an selector and a iterator over the statements that associated
		// with the instance
		Selector s1 = new SimpleSelector(Indv_Given, null,
				(RDFNode) null);
		StmtIterator i = m.listStatements(s1);

		System.out.println("The properties of Instance "
				+ Indv_Given.getLocalName() + "(As Subject):");
		while (i.hasNext()) {
			Statement s1_temp = i.next();
			Property p1_temp = s1_temp.getPredicate();
			Resource r1_temp = s1_temp.getSubject();
			RDFNode o1_temp = s1_temp.getObject();

			if (!p1_temp.isAnon()) {
				System.out.println("Subject: " + r1_temp.getLocalName());
				System.out.println("Predicate: " + p1_temp.getLocalName());
				System.out.println("Object: " + o1_temp.toString());
			}
		}

		// Second, treat the instance as object
		// create an selector and a iterator over the statements that associated
		// with the instance
		Selector s2 = new SimpleSelector(null, null, Indv_Given);
		i = m.listStatements(s2);

		System.out.println("The properties of Instance "
				+ Indv_Given.getLocalName() + "(As Object):");
		while (i.hasNext()) {
			Statement s2_temp = i.next();
			Property p2_temp = s2_temp.getPredicate();
			Resource r2_temp = s2_temp.getSubject();
			RDFNode o2_temp = s2_temp.getObject();

			if (!p2_temp.isAnon()) {
				System.out.println("Subject: " + r2_temp.getLocalName());
				System.out.println("Predicate: " + p2_temp.getLocalName());
				System.out.println("Object: " + o2_temp.toString());
			}
		}
	}

	/***************************************************************************
	 * 列举实例对应的DatatypeProperty
	 **************************************************************************/
	public ArrayList listDatatypePropertiesOfInstance(OntModel m,
			Individual Indv_Given) {
		int count = 0;
		ArrayList result = new ArrayList();
		ArrayList dpList = new ArrayList();
		// treat instance as subject
		// create an selector and a iterator over the statements that associated
		// with the instance
		Selector s1 = new SimpleSelector(Indv_Given, null,
				(RDFNode) null);
		StmtIterator i = m.listStatements(s1);

		while (i.hasNext()) {
			Statement st = i.next();
			Property rp = st.getPredicate();
			OntResource rx = m.getOntResource(rp.toString());
			// 判断Property是否为DatatypeProperty
			if (rx.isDatatypeProperty()) {
				// 判断是否已经放进结果
				if (!dpList.contains(rx)) {
					dpList.add(count, rx);
					count++;
				}
			}
		}
		result.add(0, count);
		result.add(1, dpList);
		return result;
	}

	/***************************************************************************
	 * 列举实例对应的objectProperty
	 **************************************************************************/
	public ArrayList listObjectPropertiesOfInstance(OntModel m,
			Individual Indv_Given) {
		int count = 0;
		ArrayList result = new ArrayList();
		ArrayList dpList = new ArrayList();
		// treat instance as subject
		// create an selector and a iterator over the statements that associated
		// with the instance
		Selector s1 = new SimpleSelector(Indv_Given, null,
				(RDFNode) null);
		StmtIterator i = m.listStatements(s1);

		while (i.hasNext()) {
			Statement st = i.next();
			Property rp = st.getPredicate();
			OntResource rx = m.getOntResource(rp.toString());
			// 判断Property是否为DatatypeProperty
			if (rx.isObjectProperty()) {
				// 判断是否已经放进结果
				if (!dpList.contains(rx)) {
					dpList.add(count, rx);
					count++;
				}
			}
		}
		result.add(0, count);
		result.add(1, dpList);
		return result;
	}

	/***************************************************************************
	 * Read all functionalproperties. Use occurs check to prevent getting stuck
	 * in a loop
	 **************************************************************************/
	public int listAllFunctionalRelations(String Relations[], OntModel m) {
		// create an iterator over the root properties that are not anonymous
		// property expressions
		int count = 0;
		ExtendedIterator i = m.listFunctionalProperties();

		// Output all the ObjectProperties
		while (i.hasNext()) {
			OntProperty p1_temp = (OntProperty) i.next();

			// If the property is not anonymous, output it.
			if (!p1_temp.isAnon()) {
				Relations[count] = p1_temp.getLocalName();
				count++;
			}
		}

		return count;
	}

	/***************************************************************************
	 * 列举全部Function Relation，用BaseURI过滤
	 **************************************************************************/
	public int listAllFunctionalRelationsURI(String Relations[], OntModel m,
			String baseURI) {
		// create an iterator over the root properties that are not anonymous
		// property expressions
		int count = 0;
		ExtendedIterator i = m.listFunctionalProperties();

		// Output all the ObjectProperties
		while (i.hasNext()) {
			OntProperty p1_temp = (OntProperty) i.next();

			// If the property is not anonymous, output it.
			if (!p1_temp.isAnon()) {
				if (p1_temp.getNameSpace().contains(baseURI)) {
					Relations[count] = p1_temp.getLocalName();
					count++;
				}
			}
		}

		return count;
	}

	/***************************************************************************
	 * Read all properties. Use occurs check to prevent getting stuck in a loop
	 **************************************************************************/
	public int listAllRelationsURI(String Relations[], OntModel m,
			String baseURI) {
		// create an iterator over the root properties that are not anonymous
		// property expressions
		int count = 0;
		ExtendedIterator i = m.listObjectProperties();

		// Output all the ObjectProperties
		while (i.hasNext()) {
			OntProperty p1_temp = (OntProperty) i.next();

			// If the property is not anonymous, output it.
			if (!p1_temp.isAnon()) {
				if (p1_temp.getNameSpace().contains(baseURI)) {
					Relations[count] = p1_temp.getLocalName();
					count++;
				}
			}
		}

		// Output all the DatatypeProperties
		ExtendedIterator j = m.listDatatypeProperties();

		while (j.hasNext()) {
			OntProperty p2_temp = (OntProperty) j.next();

			// If the property is not anonymous, output it.
			if (!p2_temp.isAnon()) {
				if (p2_temp.getNameSpace().contains(baseURI)) {
					Relations[count] = p2_temp.getLocalName();
					count++;
				}
			}
		}

		// Output all the FunctionalProperties
		ExtendedIterator k = m.listFunctionalProperties();

		while (k.hasNext()) {
			OntProperty p3_temp = (OntProperty) k.next();

			// If the property is not anonymous, output it.
			if (!p3_temp.isAnon()) {
				if (p3_temp.getNameSpace().contains(baseURI)) {
					Relations[count] = p3_temp.getLocalName();
					count++;
				}
			}
		}
		return count;
	}
	
	/***************************************************************************
	 * 计算概念路径的Max
	 **************************************************************************/
	public int getMaxUpperClassPathCount(PrintStream out, OntClass c, Set set)
	{
		int length = 0;
		int max = 0;
		//create an iterator over the direct superclasses that associated with the class
		ArrayList superList=new ArrayList();
		try{
			superList=(ArrayList)c.listSuperClasses(true).toList();
		}
		catch (ConversionException e){
			superList=null;
		}
		
		
		if (superList==null || superList.isEmpty()){//无父类
			length=1;
		}
		else{//遍历父类
			for(Iterator i=superList.iterator();i.hasNext();){
				OntClass cx = (OntClass) i.next();
				if (set.contains(cx)) {//出现subClassOf循环
					length = 0;
				}
				else{
					set.add(cx);
					int lengthx=0;
					lengthx=getMaxUpperClassPathCount(System.out, cx, set);
					max=Math.max(max,lengthx);
					if (max==0){
						length=0;
					}
					else{
						length=max+1;
					}					
				}
			}
		}
		return length;		
	}

	/***************************************************************************
	 * 对于结构层次很多的大本体，这样的递归算法很可能导致堆栈溢出等错误
	 **************************************************************************/
	public int getMaxLowerClassPathCount(PrintStream out, OntClass c, Set set) {
		int length = 0;
		int max = 0;
		// create an iterator over the direct superclasses that associated with
		// the class
		ArrayList subList = (ArrayList) c.listSubClasses(true).toList();

		if (subList == null || subList.isEmpty()) {// 无父类
			length = 1;
		} else {// 遍历父类
			for (Iterator i = subList.iterator(); i.hasNext();) {
				OntClass cx = (OntClass) i.next();
				if (set.contains(cx)) {// 出现subClassOf循环
					length = 0;
				} else {
					set.add(cx);
					int lengthx = 0;
					lengthx = getMaxLowerClassPathCount(System.out, cx, set);
					max = Math.max(max, lengthx);
					if (max == 0) {
						length = 0;
					} else {
						length = max + 1;
					}
				}
			}
		}
		return length;
	}

	/***************************************************************************
	 * 对于结构层次很多的大本体，这样的递归算法很可能导致堆栈溢出等错误
	 **************************************************************************/
	public int getMaxUpperPropertyPathCount(PrintStream out, OntProperty pr,
			Set set) {
		int length = 0;
		int max = 0;
		// create an iterator over the direct superclasses that associated with
		// the class
		ArrayList superList=new ArrayList();
		try{
			superList= (ArrayList) pr.listSuperProperties(true).toList();
		}
		catch (ConversionException e){
			return length;
		}


		if (superList == null || superList.isEmpty()) {// 无父类
			length = 1;
		} else {// 遍历父类
			for (Iterator i = superList.iterator(); i.hasNext();) {
				OntProperty cx = (OntProperty) i.next();
				if (set.contains(cx)) {// 出现subPropertyOf循环
					length = 0;
				} else {
					set.add(cx);
					int lengthx = 0;
					lengthx = getMaxUpperPropertyPathCount(System.out, cx, set);
					max = Math.max(max, lengthx);
					if (max == 0) {
						length = 0;
					} else {
						length = max + 1;
					}
				}
			}
		}
		return length;
	}

	/***************************************************************************
	 * 计算property路径的Max
	 **************************************************************************/
	public int getMaxLowerPropertyPathCount(PrintStream out, OntProperty pr,
			Set set) {
		int length = 0;
		int max = 0;
		// create an iterator over the direct superclasses that associated with
		// the class
		ArrayList subList = (ArrayList) pr.listSubProperties(true).toList();

		if (subList == null || subList.isEmpty()) {// 无父类
			length = 1;
		} else {// 遍历父类
			for (Iterator i = subList.iterator(); i.hasNext();) {
				OntProperty cx = (OntProperty) i.next();
				if (set.contains(cx)) {// 出现subClassOf循环
					length = 0;
				} else {
					set.add(cx);
					int lengthx = 0;
					lengthx = getMaxLowerPropertyPathCount(System.out, cx, set);
					max = Math.max(max, lengthx);
					if (max == 0) {
						length = 0;
					} else {
						length = max + 1;
					}
				}
			}
		}
		return length;

	}

	/***************************************************************************
	 * 读取本体文件
	 **************************************************************************/
	public void readOntFile(OntModel m, String ontFile) {
		String encode = null;
		encode = getOntFileEncoding(ontFile);
		InputStreamReader in;
		try {
			FileInputStream file = new FileInputStream(ontFile);
			in = new InputStreamReader(file, encode);
			m.read(in, null);
			in.close();
		} catch (FileNotFoundException e) {
			System.out
					.println("Can't open the ontology file，I have to terminate!");
			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	/***************************************************************************
	 * 判断本体文件编码格式
	 **************************************************************************/
	public String getOntFileEncoding(String ontFile) {
		String encode = null;
		String[] fileInfo = new String[5];

		Scanner input = null;
		try {
			input = new Scanner(new FileInputStream(ontFile));
		} catch (IOException e) {
			System.err.println("File not opened:\n" + e.toString());
			System.exit(1);
		}

		for (int i = 0; i < 5; i++) {
			if (input.hasNext()) {
				fileInfo[i] = input.nextLine();
			} else {
				fileInfo[i] = "";
			}
			/* 判断本体编码 */
			int spos = fileInfo[i].indexOf("encoding=\"");
			if (spos > -1) {
				int epos = fileInfo[i].indexOf("\"?>");
				if (epos > -1 && epos > spos) {
					encode = fileInfo[i].substring(spos + 10, epos);
					break;
				}
			}
		}
		input.close();
		if (encode == null) {
			encode = "UTF-8";
		}
		return encode;
	}

	/***************************************************************************
	 * 获得本体的base URI
	 **************************************************************************/
	public String getOntBaseURI(OntModel m) {
		String uri = null;
		// 如果有明确定义的base URI，可以这样获得
		uri = m.getNsPrefixURI("");
		if (uri == null) {
			// 如果上面的方法无法得到base URI,就需要用其它的办法来得到
			// 这里用的方法是利用concept来判断
			uri = getPrimaryBaseURI(m);
		}

		return uri;
	}

	/***************************************************************************
	 * 随机取20个概念来判断就行，避免遍历全部的概念
	 **************************************************************************/
	public String getPrimaryBaseURI(OntModel m) {
		ArrayList uriList = new ArrayList();
		ArrayList timeList = new ArrayList();
		int pos = 0;
		int time = 0;
		int total = 0;

		Iterator i = m.listClasses();
		int num = 0;
		while (i.hasNext()) {
			OntClass c = (OntClass) i.next();
			String s;
			// If the class is not anonymous, output it.
			if (!c.isAnon()) {
				total++;
				s = c.getNameSpace();
				if (!uriList.contains(s)) {
					uriList.add(num, s);
					num++;
				}
				time = 1;
				pos = uriList.indexOf(s);
				if (!timeList.isEmpty() && timeList.size() > pos) {
					time = ((Integer) timeList.get(pos)).intValue();
					time++;
					timeList.set(pos, time);
				} else {
					timeList.add(pos, time);
				}

				if (total >= 20) {
					break;
				}
			}
		}

		// 得到隐含的baseURI
		pos = 0;
		int max = 0;
		for (Iterator j = timeList.iterator(); j.hasNext();) {
			int value = ((Integer) j.next()).intValue();
			if (max <= value) {
				pos = timeList.indexOf(value);
				max = value;
			}
		}
		String baseURI = null;
		baseURI = (String) uriList.get(pos);
		return baseURI;
	}

	/***************************************************************************
	 * 通过遍历三元组的方式，得到本体的全部resource 把Literal列举出来意义不大，因此，这里不考虑literal 并除去本体自身的元语
	 **************************************************************************/
	public Set getAllResource(OntModel m) {
		Set set = new HashSet();
		for (StmtIterator it = m.listStatements(); it.hasNext();) {
			Statement s = it.nextStatement();
			Resource r;
			// subject肯定是resource
			r = s.getSubject();
			if (!metaURISet.contains(getResourceBaseURI(r.toString()))) {
				set.add(r);
			}
			// predicate肯定是resource
			r = s.getPredicate();
			if (!metaURISet.contains(getResourceBaseURI(r.toString()))) {
				set.add(r);
			}
			// 判断object
			if (s.getObject().isResource() && !s.getObject().isLiteral()) {
				if (!metaURISet.contains(getResourceBaseURI(s.getObject()
						.asNode().toString()))) {
					set.add(m.getResource(s.getObject().asNode().toString()));
				}
			}
		}
		return set;
	}

	/***************************************************************************
	 * 通过遍历三元组的方式，得到本体的全部涉及SameAs的resource 把Literal列举出来意义不大，
	 * 因此，这里不考虑literal
	 * 并除去本体自身的元语
	 **************************************************************************/
	public Set getAllSameAsResource(OntModel m) {
		Set set = new HashSet();
		Property psame = m.getProperty("http://www.w3.org/2002/07/owl#sameAs");
		Selector sl = new SimpleSelector(null, psame, (RDFNode) null);
		for (StmtIterator it = m.listStatements(sl); it.hasNext();) {
			Statement s = it.nextStatement();
			Resource r;
			// subject肯定是resource
			r = s.getSubject();
			if (!metaURISet.contains(getResourceBaseURI(r.toString()))) {
				set.add(r);
			}
			// predicate肯定是resource
			r = s.getPredicate();
			if (!metaURISet.contains(getResourceBaseURI(r.toString()))) {
				set.add(r);
			}
			// 判断object
			if (s.getObject().isResource() && !s.getObject().isLiteral()) {
				if (!metaURISet.contains(getResourceBaseURI(s.getObject()
						.asNode().toString()))) {
					set.add(m.getResource(s.getObject().asNode().toString()));
				}
			}
		}
		return set;
	}

	/***************************************************************************
	 * 通过遍历三元组的方式，得到本体的全部匿名resource
	 **************************************************************************/
	public Set getAllAnonResource(OntModel m) {
		Set set = new HashSet();
		for (StmtIterator it = m.listStatements(); it.hasNext();) {
			Statement s = it.nextStatement();
			Resource r = null;
			// subject肯定是resource
			r = s.getSubject();
			if (r.isAnon()) {
				set.add(r.toString());
			}
			// predicate肯定是resource
			r = s.getPredicate();
			if (r.isAnon()) {
				set.add(r.toString());
			}
			// 判断object
			if (s.getObject().isAnon()) {
				set.add(m.getResource(s.getObject().toString()).toString());
			}
		}
		return set;
	}
	
	/***************************************************************************
	 * 通过遍历三元组的方式，得到本体的以空节点开头的三元组<bnode,p,o>
	 **************************************************************************/
	public Set getAllSubAnonStatements(OntModel m) {
		if (isGetSubAnonStm) return SubAnonStatements;
		Set set = new HashSet();
		for (StmtIterator it = m.listStatements(); it.hasNext();) {
			Statement s = it.nextStatement();
			Resource r = null;
			if (isBlankNode(s.getSubject().toString())){
				set.add(s);
			}
		}
		SubAnonStatements = set;
		isGetSubAnonStm = true;
		return set;
	}
	
	public Set getAllSubAnonStatements(OntModel m, String subName) {
		Set stmSet = getAllSubAnonStatements(m);
		Set result=new HashSet();
		for (Iterator it = stmSet.iterator(); it.hasNext();) {
			Statement s = (Statement) it.next();
			if (s.getSubject().toString().equals(subName)){
				result.add(s);
			}			
		}
		return result;
	}
	
	public Set getAllSubStatements(OntModel m, Resource sub)
	{
		Set result = new HashSet();
		if (isBlankNode(sub.toString())){
			result = getAllSubAnonStatements(m,sub.toString());
		}
		else
		{
			for (StmtIterator it = m.listStatements(sub, null,(RDFNode) null); it.hasNext();) {
				Statement t = it.nextStatement();
				result.add(t);
			}				
		}
		
		return result;
		
	}
	
	/***************************************************************************
	 * 通过遍历三元组的方式，得到本体的以空节点结尾的三元组<s,p,bnode>
	 **************************************************************************/
	public Set getAllObjAnonStatements(OntModel m) {
		if (isGetObjAnonStm) return ObjAnonStatements;
		Set set = new HashSet();
		for (StmtIterator it = m.listStatements(); it.hasNext();) {
			Statement s = it.nextStatement();
			Resource r = null;
			if (isBlankNode(s.getObject().toString())){
				set.add(s);
			}
		}
		ObjAnonStatements = set;
		isGetObjAnonStm = true;
		return set;
	}
	
	public Set getAllObjAnonStatements(OntModel m, String objName) {
		Set stmSet = getAllObjAnonStatements(m);
		Set result=new HashSet();
		for (Iterator it = stmSet.iterator(); it.hasNext();) {
			Statement s = (Statement) it.next();
			if (s.getObject().toString().equals(objName)){
				result.add(s);
			}			
		}
		return result;
	}
	
	public Set getAllObjStatements(OntModel m, RDFNode obj)
	{
		Set result = new HashSet();
		if (isBlankNode(obj.toString())){
			result = getAllObjAnonStatements(m,obj.toString());
		}
		else
		{
			for (StmtIterator it = m.listStatements(null, null, obj); it.hasNext();) {
				Statement t = it.nextStatement();
				result.add(t);
			}				
		}
		return result;
	}

	/***************************************************************************
	 * 求全部的Bag
	 **************************************************************************/
	public ArrayList getAllContainers(OntModel m) {
		Set resSet = new HashSet();
		resSet = getAllResource(m);

		Set bagSet = new HashSet();
		Set altSet = new HashSet();
		Set seqSet = new HashSet();
		for (Iterator it = resSet.iterator(); it.hasNext();) {
			Resource r0 = (Resource) it.next();
			OntResource r = null;
			r = m.getOntResource(r0);
			Resource rType = r.getRDFType();
						
			if (rType != null) {
				// 判断r是不是Bag
				if (rType.toString().equals(rdfStr + "Bag")) {
					// 加入bagSet
					bagSet.add(r);
				}
				// 判断r是不是Alt
				if (rType.toString().equals(rdfStr + "Alt")) {
					// 加入altSet
					altSet.add(r);
				}
				// 判断r是不是Seq
				if (rType.toString().equals(rdfStr + "Seq")) {
					// 加入altSet
					seqSet.add(r);
				}
			}
		}
		ArrayList list = new ArrayList();
		list.add(0, bagSet);
		list.add(1, altSet);
		list.add(2, seqSet);
		return list;
	}
	
	/***************************************************************************
	 * 求全部的Collection
	 **************************************************************************/
	public ArrayList getAllCollections(OntModel m) {
		/*获得的三种结果，用三个HashMap保存*/
		/*（1）head-->member
		 * 用HashMap来保存Collection，
		 * Key--Collection的头
		 * Value--是一个包含其它成员的集合*/
		/*（2）head-->替换模板
		 * 用HashMap保存head结尾的三元组
		 *（3）head-->声明
		 * 用HashMap保存head的声明*/
		HashMap resultCltMap = new HashMap();
		HashMap resultStmMap = new HashMap();
		HashMap resultDeclMap = new HashMap();
		Set cltHeads = new HashSet();
				
		/*1.找到所有Collection的三元组
		 * 根据：每个Collection都有一个http://www.w3.org/1999/02/22-rdf-syntax-ns#first
		 * 和http://www.w3.org/1999/02/22-rdf-syntax-ns#rest*/
		Set stmSet=new HashSet();
		Set headSet = new HashSet();
		Set tailSet = new HashSet();
		for (StmtIterator it=m.listStatements(null,m.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#first"),(RDFNode)null);it.hasNext();){
			Statement st= it.nextStatement();
			String hName=st.getSubject().toString();
			String tName=st.getObject().toString();
			if (isBlankNode(hName)) headSet.add(hName);
			if (isBlankNode(tName)) tailSet.add(tName);
			stmSet.add(st);
		}
		for (StmtIterator it=m.listStatements(null,m.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#rest"),(RDFNode)null);it.hasNext();){
			Statement st= it.nextStatement();
			String hName=st.getSubject().toString();
			String tName=st.getObject().toString();
			if (isBlankNode(hName)) headSet.add(hName);
			if (isBlankNode(tName)) tailSet.add(tName);
			stmSet.add(st);
		}
		
		/*2.由这些三元组构建HashMap*/
		/*先得到全部head,判断是否公理，同时保留head结尾的三元组*/
		for (Iterator it=headSet.iterator();it.hasNext();){
			String node=(String)it.next();
			if (!tailSet.contains(node)){
				/*过滤去OWL公理产生的Collection*/
				boolean axiomflag=false;
				/*检查head结尾的三元组，判断是不是一个OWL公理*/
				Set headStm=getAllObjStatements(m, m.getResource(node));
				for (Iterator itx=headStm.iterator();itx.hasNext();){
					Statement st=(Statement)itx.next();
					String preNameSpace=st.getPredicate().getNameSpace();
					if (preNameSpace.equals("http://www.w3.org/2002/07/owl#")){
						axiomflag=true;
						break;
					}					
				}
				if (!axiomflag){
					cltHeads.add(node);
					resultStmMap.put(node,headStm);
				}					
			}
		}
		
		/*3.由每个head推出各自的member*/
		for (Iterator it=cltHeads.iterator();it.hasNext();){
			String hName=(String)it.next();
			Set memberSet = new HashSet();
			Set declSet = new HashSet();
			String objStr = "";
			String subStr = hName;
			boolean update=true;
			while (!objStr.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#nil") && update){
				update=false;
				/*寻找first*/
				for (Iterator itx = stmSet.iterator();itx.hasNext();){
					Statement st=(Statement)itx.next();
					String subName=st.getSubject().toString();
					String preName=st.getPredicate().toString();
					String objName=st.getObject().toString();
					if (subName.equals(subStr) && preName.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#first")
						&& !declSet.contains(st)){
						memberSet.add(m.getResource(objName));
						declSet.add(st);
						update=true;
						break;
					}
				}
				/*寻找rest*/
				for (Iterator itx = stmSet.iterator();itx.hasNext();){
					Statement st=(Statement)itx.next();
					String subName=st.getSubject().toString();
					String preName=st.getPredicate().toString();
					String objName=st.getObject().toString();
					if (subName.equals(subStr) && preName.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#rest")
						&& !declSet.contains(st)){
						objStr=objName;
						subStr=objName;
						declSet.add(st);
						update=true;
						break;
					}
				}				
			}
			resultCltMap.put(hName,memberSet);//添加一个List数据结构
			resultDeclMap.put(hName,declSet);//添加一个head->声明 数据结构
		}
		
		ArrayList lt=new ArrayList();
		lt.add(0,resultCltMap);
		lt.add(1,resultStmMap);
		lt.add(2,resultDeclMap);
		return lt;
	}

	/***************************************************************************
	 * 判断Bag包含的成分
	 **************************************************************************/
	public int getBagType(OntModel m, Resource r) {
		int INSTANCE = 1;
		int CLASS = 2;
		boolean flag;

		Bag bag = m.getBag(r);

		if (bag != null) {
			for (NodeIterator it = bag.iterator(); it.hasNext();) {
				Object member = it.next();
				Resource x = m.getResource(member.toString());
				if (!m.getOntResource(x).isIndividual()) {
					INSTANCE = 0;
				}
				if (!m.getOntResource(x).isClass()) {
					CLASS = 0;
				}
			}
		}

		return (INSTANCE + CLASS);
	}

	/***************************************************************************
	 * 得到Bag包含的元素
	 **************************************************************************/
	public ArrayList getBagContent(OntModel m, Resource r) {
		ArrayList list = new ArrayList();
		Bag bag = m.getBag(m.getOntResource(r));
		
		if (!isBlankNode(r.toString())) {//非空节点
			for (NodeIterator it = bag.iterator(); it.hasNext();) {
				Object member = it.next();
				Resource x = m.getResource(member.toString());
				list.add(m.getOntResource(x));
			}
		}
		else
		{//空节点另行处理
			/*遍历所有<r,p,o>三元组*/
			Set annoStms=getAllSubAnonStatements(m,r.toString());
			for (Iterator it = annoStms.iterator(); it.hasNext();) {
				Statement t = (Statement) it.next();
				String preName=t.getPredicate().toString();
				String objName=t.getObject().toString();
				Resource objRs=m.getResource(objName);
				/*取出Bag声明*/
				if (preName.startsWith("http://www.w3.org/1999/02/22-rdf-syntax-ns#li")
					||preName.startsWith("http://www.w3.org/1999/02/22-rdf-syntax-ns#_")){
					list.add(m.getOntResource(objRs));
				}
			}
			
		}
		return (list);
	}
	
	/***************************************************************************
	 * 得到Alt包含的元素
	 **************************************************************************/
	public ArrayList getAltContent(OntModel m, Resource r) {
		ArrayList list = new ArrayList();
		Alt alt = m.getAlt(m.getOntResource(r));
		
		if (!isBlankNode(r.toString())) {//非空节点
			for (NodeIterator it = alt.iterator(); it.hasNext();) {
				Object member = it.next();
				Resource x = m.getResource(member.toString());
				list.add(m.getOntResource(x));
			}
		}
		else
		{//空节点另行处理
			/*遍历所有<r,p,o>三元组*/
			Set annoStms=getAllSubAnonStatements(m,r.toString());
			for (Iterator it = annoStms.iterator(); it.hasNext();) {
				Statement t = (Statement) it.next();
				String preName=t.getPredicate().toString();
				String objName=t.getObject().toString();
				Resource objRs=m.getResource(objName);
				/*取出Alt声明*/
				if (preName.startsWith("http://www.w3.org/1999/02/22-rdf-syntax-ns#li")
					||preName.startsWith("http://www.w3.org/1999/02/22-rdf-syntax-ns#_")){
					list.add(m.getOntResource(objRs));
				}
			}
			
		}
		return (list);
	}
	
	/***************************************************************************
	 * 得到Seq包含的元素
	 **************************************************************************/
	public ArrayList getSeqContent(OntModel m, Resource r) {
		ArrayList list = new ArrayList();
		Seq seq = m.getSeq(m.getOntResource(r));
		
		if (!isBlankNode(r.toString())) {//非空节点
			for (NodeIterator it = seq.iterator(); it.hasNext();) {
				Object member = it.next();
				Resource x = m.getResource(member.toString());
				list.add(m.getOntResource(x));
			}
		}
		else
		{//空节点另行处理
			/*遍历所有<r,p,o>三元组*/
			Set annoStms=getAllSubAnonStatements(m,r.toString());
			for (Iterator it = annoStms.iterator(); it.hasNext();) {
				Statement t = (Statement) it.next();
				String preName=t.getPredicate().toString();
				String objName=t.getObject().toString();
				Resource objRs=m.getResource(objName);
				/*取出Seq声明*/
				if (preName.startsWith("http://www.w3.org/1999/02/22-rdf-syntax-ns#li")
					||preName.startsWith("http://www.w3.org/1999/02/22-rdf-syntax-ns#_")){
					list.add(m.getOntResource(objRs));
				}
			}
			
		}
		return (list);
	}

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

	public String getResourceLocalName(Resource r) {
		String name = null;
		if (r.isAnon()) {
			name = r.toString();
		} else {
			name = r.getLocalName();
		}
		return name;
	}

	/***************************************************************************
	 * 得到本体中的复杂Class
	 **************************************************************************/
	@SuppressWarnings("unchecked")
	public static ArrayList getComplexClass(OntModel m) {
		Set intsctSet = new HashSet();
		Set unionSet = new HashSet();
		ArrayList list = new ArrayList();
		for (ExtendedIterator i = m.listClasses(); i.hasNext();) {
			OntClass c = (OntClass) i.next();
			if (c.isIntersectionClass()) {
				// for(ExtendedIterator
				// j=m.listIntersectionClasses();j.hasNext();){
				// IntersectionClass cu=(IntersectionClass)j.next();
				// if (cu.toString().equals(c.toString())){
				// intsctSet.add(cu);
				// break;
				// }
				// }
				intsctSet.add(c.asIntersectionClass());
			} else if (c.isUnionClass()) {
				// for(ExtendedIterator j=m.listUnionClasses();j.hasNext();){
				// UnionClass cu=(UnionClass)j.next();
				// if (cu.toString().equals(c.toString())){
				// unionSet.add(cu);
				// break;
				// }
				// }
				unionSet.add(c.asUnionClass());
			}
		}
		list.add(0, intsctSet);
		list.add(1, unionSet);
		return list;
	}
	
	/***************************************************************************
	 * 1.处理RDF容器Containers:无论容器是否是空节点，将其中的成员替代容器出现的位置
	 * 2.处理List：将成员替代list出现的位置，删除list三元组
	 **************************************************************************/
	public void reParseContainer_List(OntModel m)
	{
		//Step1.处理RDF容器Containers 
		/* 得到本体中的全部容器，并实现分类好 */
		Set bagSet = new HashSet();
		Set altSet = new HashSet();
		Set seqSet = new HashSet();
		ArrayList list = getAllContainers(m);
		bagSet=(Set)list.get(0);
		altSet=(Set)list.get(1);
		seqSet=(Set)list.get(2);
		ArrayList stmRecord = new ArrayList();
		ArrayList stmRemove = new ArrayList();
		
		/*Bag*/
		for(Iterator it=bagSet.iterator();it.hasNext();){ 
			Resource r=m.getResource(it.next().toString()); 
			ArrayList blist=getBagContent(m,r);
			/*用Bag中的member增加Bag出现的三元组*/
			for (Iterator itx=blist.iterator();itx.hasNext();){
				Resource member=(Resource)itx.next();
			
				/*Bag作为subject的情况*/
				Set stmSet=getAllSubStatements(m,r);
				/*声明Bag的三元组*/
				Set bagDclSet = new HashSet();
				boolean replaceTag=false;//替换声明标记
				
				for (Iterator it3 = stmSet.iterator(); it3.hasNext();) {
					Statement t = (Statement) it3.next();
					String preName=t.getPredicate().toString();
					String objName=t.getObject().toString();
					Resource objRs=m.getResource(objName);
					
					/*记录声明Bag的三元组*/
					if (preName.startsWith("http://www.w3.org/1999/02/22-rdf-syntax-ns#_")
						||objName.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#Bag")){
						if (!bagDclSet.contains(t))bagDclSet.add(t);
					}
					/*排除重复声明和错误声明*/
					if (!blist.contains(objRs) 
						&& !objName.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#Bag")){
						Statement stAdd= m.createStatement(member,t.getPredicate(),t.getObject());
						stmRecord.add(stAdd);
						replaceTag = true;
					}
				}
				/*Bag作为object的情况*/
				stmSet=getAllObjStatements(m, r);
				for (Iterator it3 = stmSet.iterator(); it3.hasNext();) {
					Statement t = (Statement) it3.next();
					/*由于不清楚该情形下何时会产生重复声明，所以未写排除重复声明和错误声明*/
					Statement stAdd= m.createStatement(t.getSubject(),t.getPredicate(),member);
					stmRecord.add(stAdd);
					replaceTag = true;
				}
				/*Bag作为predicate的情况*/
				for (StmtIterator it3 = m.listStatements(null, m.getProperty(r.toString()),(RDFNode)null); it3.hasNext();) {
					Statement t = it3.nextStatement();
					/*由于不清楚该情形下何时会产生重复声明，所以未写排除重复声明和错误声明*/
					Statement stAdd= m.createStatement(t.getSubject(),m.getProperty(member.toString()),t.getObject());
					stmRecord.add(stAdd);
					replaceTag = true;
				}
				
				if (replaceTag){//记录将被删除的声明
					for (Iterator it3 = bagDclSet.iterator();it3.hasNext();){
						Statement t = (Statement)it3.next();
						if (!stmRemove.contains(t)) stmRemove.add(t);
					}	
				}
			}
		}
		
		/*Alt*/
		for(Iterator it=altSet.iterator();it.hasNext();){ 
			Resource r=m.getResource(it.next().toString()); 
			ArrayList blist=getAltContent(m,r);
			/*用Alt中的member增加Alt出现的三元组*/
			for (Iterator itx=blist.iterator();itx.hasNext();){
				Resource member=(Resource)itx.next();
			
				/*Alt作为subject的情况*/
				Set stmSet=getAllSubStatements(m,r);
				/*声明Alt的三元组*/
				Set altDclSet = new HashSet();
				boolean replaceTag=false;//替换声明标记
				
				for (Iterator it3 = stmSet.iterator(); it3.hasNext();) {
					Statement t = (Statement) it3.next();
					String preName=t.getPredicate().toString();
					String objName=t.getObject().toString();
					Resource objRs=m.getResource(objName);
					
					/*记录声明Bag的三元组*/
					if (preName.startsWith("http://www.w3.org/1999/02/22-rdf-syntax-ns#_")
						||objName.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#Alt")){
						if (!altDclSet.contains(t))altDclSet.add(t);
					}
					
					/*排除重复声明和错误声明*/
					if (!blist.contains(objRs) 
						&& !objName.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#Alt")){
						Statement stAdd= m.createStatement(member,t.getPredicate(),t.getObject());
						stmRecord.add(stAdd);
						replaceTag = true;
					}
				}
				/*Alt作为object的情况*/
				stmSet=getAllObjStatements(m, r);
				for (Iterator it3 = stmSet.iterator(); it3.hasNext();) {
					Statement t = (Statement) it3.next();
					/*由于不清楚该情形下何时会产生重复声明，所以未写排除重复声明和错误声明*/
					Statement stAdd= m.createStatement(t.getSubject(),t.getPredicate(),member);
					stmRecord.add(stAdd);
					replaceTag = true;
				}
				/*Bag作为predicate的情况*/
				for (StmtIterator it3 = m.listStatements(null, m.getProperty(r.toString()),(RDFNode)null); it3.hasNext();) {
					Statement t = it3.nextStatement();
					/*由于不清楚该情形下何时会产生重复声明，所以未写排除重复声明和错误声明*/
					Statement stAdd= m.createStatement(t.getSubject(),m.getProperty(member.toString()),t.getObject());
					stmRecord.add(stAdd);
					replaceTag = true;
				}
				if (replaceTag){//记录将被删除的声明
					for (Iterator it3 = altDclSet.iterator();it3.hasNext();){
						Statement t = (Statement)it3.next();
						if (!stmRemove.contains(t)) stmRemove.add(t);
					}	
				}
			}
		}
		
		/*Seq*/
		for(Iterator it=seqSet.iterator();it.hasNext();){ 
			Resource r=m.getResource(it.next().toString()); 
			ArrayList blist=getSeqContent(m,r);
			/*用Seq中的member增加Seq出现的三元组*/
			for (Iterator itx=blist.iterator();itx.hasNext();){
				Resource member=(Resource)itx.next();
			
				/*Seq作为subject的情况*/
				Set stmSet=getAllSubStatements(m,r);
				/*声明Alt的三元组*/
				Set seqDclSet = new HashSet();
				boolean replaceTag=false;//替换声明标记
				
				for (Iterator it3 = stmSet.iterator(); it3.hasNext();) {
					Statement t = (Statement) it3.next();
					String preName=t.getPredicate().toString();
					String objName=t.getObject().toString();
					Resource objRs=m.getResource(objName);
					
					/*记录声明Seq的三元组*/
					if (preName.startsWith("http://www.w3.org/1999/02/22-rdf-syntax-ns#_")
						||objName.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#Seq")){
						if (!seqDclSet.contains(t))seqDclSet.add(t);
					}
					
					/*排除重复声明和错误声明*/
					if (!blist.contains(objRs) 
						&& !objName.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#Seq")){
						Statement stAdd= m.createStatement(member,t.getPredicate(),t.getObject());
						stmRecord.add(stAdd);
						replaceTag = true;
					}
				}
				/*Seq作为object的情况*/
				stmSet=getAllObjStatements(m, r);
				for (Iterator it3 = stmSet.iterator(); it3.hasNext();) {
					Statement t = (Statement) it3.next();
					/*由于不清楚该情形下何时会产生重复声明，所以未写排除重复声明和错误声明*/
					Statement stAdd= m.createStatement(t.getSubject(),t.getPredicate(),member);
					stmRecord.add(stAdd);
					replaceTag = true;
				}
				/*Bag作为predicate的情况*/
				for (StmtIterator it3 = m.listStatements(null, m.getProperty(r.toString()),(RDFNode)null); it3.hasNext();) {
					Statement t = it3.nextStatement();
					/*由于不清楚该情形下何时会产生重复声明，所以未写排除重复声明和错误声明*/
					Statement stAdd= m.createStatement(t.getSubject(),m.getProperty(member.toString()),t.getObject());
					stmRecord.add(stAdd);
					replaceTag = true;
				}
				
				if (replaceTag){//记录将被删除的声明
					for (Iterator it3 = seqDclSet.iterator();it3.hasNext();){
						Statement t = (Statement)it3.next();
						if (!stmRemove.contains(t)) stmRemove.add(t);
					}					
				}
			}
		}
		
		// Step2.处理RDF集合
		// 思路：解析List，然后将List的不同用处的语义明确化
		/*1.获得List的head和对应的member*/
		ArrayList lt=getAllCollections(m);
		HashMap cltMap=(HashMap)lt.get(0);
		HashMap cltStmMap=(HashMap)lt.get(1);
		HashMap cltDeclMap=(HashMap)lt.get(2);
		/*2.判断当前的Map是否是公理*/
		//已经在getAllCollections中判断
		/*3.列举head结尾的三元组，替换*/
		Set cltHeads=cltMap.keySet();
		for (Iterator it=cltHeads.iterator();it.hasNext();){
			String headName=(String)it.next();
			Set stmSet = (Set)cltStmMap.get(headName);
			boolean replaceTag = false;//可替换标记
			for (Iterator itx=stmSet.iterator();itx.hasNext();){
				Statement st=(Statement)itx.next();
				Set memberSet=(Set)cltMap.get(headName);
				for (Iterator ity=memberSet.iterator();ity.hasNext();){
					Resource rmember=(Resource)ity.next();
					Statement stAdd= m.createStatement(st.getSubject(),st.getPredicate(),rmember);
					stmRecord.add(stAdd);
					replaceTag = true;
				}				
			}
			if (replaceTag){
				for (Iterator itx=((Set)(cltDeclMap.get(headName))).iterator();itx.hasNext();){
					Statement t = (Statement)itx.next();
					if (!stmRemove.contains(t)) stmRemove.add(t);
				}
			}
		}
		
		/*将对Container和Collection的处理结果加入Model*/
		for (Iterator it = stmRecord.iterator();it.hasNext();){
			Statement st = (Statement)it.next();
			m.add(st);
		}
		
		/*删除不必要的声明*/
		for (Iterator it = stmRemove.iterator();it.hasNext();){
			Statement st = (Statement)it.next();
			m.remove(st);
		}
		
//		System.out.println("---处理集合和容器--结果--");
//		for (Iterator it = m.listStatements(); it.hasNext();) {
//			Statement st = (Statement) it.next();
//			System.out.println(st.toString());
//		}
	}

	/***************************************************************************
	 * Enrich the ontology through add more statements such as domain, range.
	 * enrich的过程只是添加三元组，不增加本体元素
	 **************************************************************************/
	@SuppressWarnings("unchecked")
	public void enrichOnt(OntModel m, int conceptNum, int propNum, String[] propName, String baseURI) {
		ArrayList list = new ArrayList();

		// Step1.先处理父子属性的domain和range传递性
		// enrich the domain and range by considering the property hierachy
		for (int i = 0; i < propNum; i++) {
			OntProperty p = m.getOntProperty(baseURI + propName[i]);
			OntResource resDomain = p.getDomain();// 父属性的domain
			OntResource resRange = p.getRange();// 父属性的range

			// 这里的原则是：父属性的domain和Range自然传给子属性
			for (ExtendedIterator Iter = p.listSubProperties(false); Iter
					.hasNext();) {
				OntProperty tp = (OntProperty) Iter.next();
				// 传给子属性
				if (resDomain != null) {
					tp.addDomain(resDomain);
				}
				if (resRange != null) {
					tp.addRange(resRange);
				}
			}
		}

		// Step4.处理类的公理
		list.clear();
		list = getComplexClass(m);
		// owl:oneOf，用于枚举的方式定义类，不处理
		// owl:intersectionOf，寻找它的子类，并丰富它
		Set intsctSet = new HashSet();
		intsctSet = (Set) list.get(0);
		for (Iterator it = intsctSet.iterator(); it.hasNext();) {
			// 得到一个IntersectionClass
			IntersectionClass interc = (IntersectionClass) it.next();
			Set mbSet = new HashSet();
			// 列举出对应的members
			for (ExtendedIterator it2 = interc.listOperands(); it2.hasNext();) {
				OntClass tc = (OntClass) it2.next();
				mbSet.add(tc);
			}
			// members应该包含该intersectionClass
			for (Iterator it2 = mbSet.iterator(); it2.hasNext();) {
				((OntClass) it2.next()).addSubClass(interc);
			}
			// 寻找intersectionClass的所有subclass
			for (ExtendedIterator it2 = interc.listSubClasses(false); it2
					.hasNext();) {
				OntClass tc = (OntClass) it2.next();
				// members应该包含这些subclasses
				for (Iterator it3 = mbSet.iterator(); it3.hasNext();) {
					((OntClass) it3.next()).addSubClass(tc);
				}
			}
		}

		// owl:unionOf，寻找它的父类，并丰富它
		Set unionSet = new HashSet();
		unionSet = (Set) list.get(1);
		for (Iterator it = unionSet.iterator(); it.hasNext();) {
			// 得到一个unionClass
			UnionClass unionc = (UnionClass) it.next();
			Set unSet = new HashSet();
			// 列举出对应的members
			for (ExtendedIterator it2 = unionc.listOperands(); it2.hasNext();) {
				OntClass tc=null;
				try {
					tc = (OntClass) it2.next();
				}
				catch (PropertyNotFoundException e){
					break;
				}				
				unSet.add(tc);
			}
			// unionClass应该包含该members
			for (Iterator it2 = unSet.iterator(); it2.hasNext();) {
				unionc.addSubClass((OntClass) it2.next());
			}
			// 寻找unionClass的所有superclass
			for (ExtendedIterator it2 = unionc.listSuperClasses(false); it2
					.hasNext();) {
				OntClass tc = (OntClass) it2.next();
				// superclasses应该包含这些members
				for (Iterator it3 = unSet.iterator(); it3.hasNext();) {
					tc.addSubClass((OntClass) it3.next());
				}
			}
		}

		// owl:equivalentClass
		// 遍历全部Class
		ArrayList stmec = new ArrayList();
		for (ExtendedIterator it = m.listClasses(); it.hasNext();) {
			OntClass tc = (OntClass) it.next();
			// 寻找equivalentClass
			for (ExtendedIterator it2 = tc.listEquivalentClasses(); it2
					.hasNext();) {
				OntClass ec = (OntClass) it2.next();
				// 使得equivalentClass等价
				/* 不能马上加入这些三元组，因为会改变model，使得外层的iterator实效 */
				// c1为Subject的三元组
				for (StmtIterator it3 = m.listStatements(tc, null,
						(RDFNode) null); it3.hasNext();) {
					Statement t = it3.nextStatement();
					ArrayList la = new ArrayList();
					ArrayList lb = new ArrayList();
					la.add(0, ec);
					la.add(1, t);
					lb.add(0, la);
					lb.add(1, 1);
					stmec.add(lb);
					// m.add(ec,t.getPredicate(),t.getObject());
				}
				// c2为Subject的三元组
				for (StmtIterator it3 = m.listStatements(ec, null,
						(RDFNode) null); it3.hasNext();) {
					Statement t = it3.nextStatement();
					ArrayList la = new ArrayList();
					ArrayList lb = new ArrayList();
					la.add(0, tc);
					la.add(1, t);
					lb.add(0, la);
					lb.add(1, 2);
					stmec.add(lb);
					// m.add(tc,t.getPredicate(),t.getObject());
				}
				// c1为Object的三元组
				for (StmtIterator it3 = m.listStatements(null, null, tc); it3
						.hasNext();) {
					Statement t = it3.nextStatement();
					ArrayList la = new ArrayList();
					ArrayList lb = new ArrayList();
					la.add(0, ec);
					la.add(1, t);
					lb.add(0, la);
					lb.add(1, 3);
					stmec.add(lb);
					// m.add(t.getSubject(),t.getPredicate(),ec);
				}
				// c2为Object的三元组
				for (StmtIterator it3 = m.listStatements(null, null, ec); it3
						.hasNext();) {
					Statement t = it3.nextStatement();
					ArrayList la = new ArrayList();
					ArrayList lb = new ArrayList();
					la.add(0, tc);
					la.add(1, t);
					lb.add(0, la);
					lb.add(1, 4);
					stmec.add(lb);
					// m.add(t.getSubject(),t.getPredicate(),tc);
				}
			}
		}
		/* 加入结果 */
		/* notice:这样的处理不能完成A=B=C的多个连续相等的情形 */
		for (Iterator i = stmec.iterator(); i.hasNext();) {
			ArrayList la = (ArrayList) i.next();

			int ty = 0;
			ArrayList lb = (ArrayList) la.get(0);
			ty = ((Integer) la.get(1)).intValue();
			OntClass c = (OntClass) lb.get(0);
			Statement t = (Statement) lb.get(1);
			/* 排除自己等价自己 */
			if (ty == 1 || ty == 2) {
				if (!(t.getPredicate().toString().equals(
						"http://www.w3.org/2002/07/owl#equivalentClass") && c
						.toString().equals(t.getObject().toString()))) {
					m.add(c, t.getPredicate(), t.getObject());
				}
			} else if (ty == 3 || ty == 4) {
				if (!(t.getPredicate().toString().equals(
						"http://www.w3.org/2002/07/owl#equivalentClass") && c
						.toString().equals(t.getSubject().toString()))) {
					m.add(t.getSubject(), t.getPredicate(), c);
				}
			}
		}

		// Step5.处理属性的公理
		// owl:SymmetricProperty
		// 正常的情况下，domain和range是相同的
		for (ExtendedIterator it = m.listOntProperties(); it.hasNext();) {
			OntProperty op = (OntProperty) it.next();
			if (op.isSymmetricProperty()) {
				// 判断domain和Range是否相同
				OntResource rd = op.getDomain();
				OntResource rr = op.getRange();
				if (!rd.equals(rr)) {
					op.addDomain(rr);
					op.addRange(rd);
				}
			}
		}
		// owl:equivalentProperty
		ArrayList stmep = new ArrayList();
		for (ExtendedIterator it = m.listOntProperties(); it.hasNext();) {
			OntProperty p1 = (OntProperty) it.next();
			for (ExtendedIterator it2 = p1.listEquivalentProperties(); it2
					.hasNext();) {
				OntProperty p2 = (OntProperty) it2.next();
				// p1为subject
				for (StmtIterator it3 = m.listStatements(p1, null,
						(RDFNode) null); it3.hasNext();) {
					Statement t = it3.nextStatement();
					ArrayList la = new ArrayList();
					ArrayList lb = new ArrayList();
					la.add(0, p2);
					la.add(1, t);
					lb.add(0, la);
					lb.add(1, 1);
					stmep.add(lb);
					// m.add(p2,t.getPredicate(),t.getObject());
				}
				// p2为subject
				for (StmtIterator it3 = m.listStatements(p2, null,
						(RDFNode) null); it3.hasNext();) {
					Statement t = it3.nextStatement();
					ArrayList la = new ArrayList();
					ArrayList lb = new ArrayList();
					la.add(0, p1);
					la.add(1, t);
					lb.add(0, la);
					lb.add(1, 2);
					stmep.add(lb);
					// m.add(p1,t.getPredicate(),t.getObject());
				}
				// p1为predicate
				for (StmtIterator it3 = m.listStatements(null, p1,
						(RDFNode) null); it3.hasNext();) {
					Statement t = it3.nextStatement();
					ArrayList la = new ArrayList();
					ArrayList lb = new ArrayList();
					la.add(0, p2);
					la.add(1, t);
					lb.add(0, la);
					lb.add(1, 3);
					stmep.add(lb);
					// m.add(t.getSubject(),p2,t.getObject());
				}
				// p2为predicate
				for (StmtIterator it3 = m.listStatements(null, p2,
						(RDFNode) null); it3.hasNext();) {
					Statement t = it3.nextStatement();

					ArrayList la = new ArrayList();
					ArrayList lb = new ArrayList();
					la.add(0, p1);
					la.add(1, t);
					lb.add(0, la);
					lb.add(1, 4);
					stmep.add(lb);
					// m.add(t.getSubject(),p1,t.getObject());
				}
				// p1为object
				for (StmtIterator it3 = m.listStatements(null, null, p1); it3
						.hasNext();) {
					Statement t = it3.nextStatement();
					ArrayList la = new ArrayList();
					ArrayList lb = new ArrayList();
					la.add(0, p2);
					la.add(1, t);
					lb.add(0, la);
					lb.add(1, 5);
					stmep.add(lb);
					// m.add(t.getSubject(),t.getPredicate(),p2);
				}
				// p2为object
				for (StmtIterator it3 = m.listStatements(null, null, p2); it3
						.hasNext();) {
					Statement t = it3.nextStatement();
					ArrayList la = new ArrayList();
					ArrayList lb = new ArrayList();
					la.add(0, p1);
					la.add(1, t);
					lb.add(0, la);
					lb.add(1, 6);
					stmep.add(lb);
					// m.add(t.getSubject(),t.getPredicate(),p1);
				}
			}
		}
		/* 加入结果 */
		for (Iterator i = stmep.iterator(); i.hasNext();) {
			ArrayList la = (ArrayList) i.next();

			int ty = 0;
			ArrayList lb = (ArrayList) la.get(0);
			ty = ((Integer) la.get(1)).intValue();
			OntProperty c = (OntProperty) lb.get(0);
			Statement t = (Statement) lb.get(1);

			/* 排除自己等价自己 */
			if (ty == 1 || ty == 2) {
				if (!(t.getPredicate().toString().equals(
						"http://www.w3.org/2002/07/owl#equivalentProperty") && c
						.toString().equals(t.getObject().toString()))) {
					m.add(c, t.getPredicate(), t.getObject());
				}
			} else if (ty == 3 || ty == 4) {
				if (!(c.toString().equals(
						"http://www.w3.org/2002/07/owl#equivalentProperty") && t
						.getSubject().toString().equals(
								t.getObject().toString()))) {
					m.add(t.getSubject(), c, t.getObject());
				}
			} else if (ty == 5 || ty == 6) {
				if (!(t.getPredicate().toString().equals(
						"http://www.w3.org/2002/07/owl#equivalentProperty") && t
						.getSubject().toString().equals(c.toString()))) {
					m.add(t.getSubject(), t.getPredicate(), c);
				}
			}
		}

		// Step6.处理sameAs
		Set rs = new HashSet();
		rs = getAllSameAsResource(m);
		// 遍历Resource
		ArrayList stmer = new ArrayList();
		for (Iterator it = rs.iterator(); it.hasNext();) {
			Resource r0 = (Resource) it.next();
			OntResource r1 = m.getOntResource(r0);
			for (ExtendedIterator it2 = r1.listSameAs(); it2.hasNext();) {
				OntResource r2 = (OntResource) it2.next();
				// r1为subject
				for (StmtIterator it3 = m.listStatements(r1, null,
						(RDFNode) null); it3.hasNext();) {
					Statement t = it3.nextStatement();
					ArrayList la = new ArrayList();
					ArrayList lb = new ArrayList();
					la.add(0, r2);
					la.add(1, t);
					lb.add(0, la);
					lb.add(1, 1);
					stmer.add(lb);
					// m.add(r2,t.getPredicate(),t.getObject());
				}
				// r2为subject
				for (StmtIterator it3 = m.listStatements(r2, null,
						(RDFNode) null); it3.hasNext();) {
					Statement t = it3.nextStatement();
					ArrayList la = new ArrayList();
					ArrayList lb = new ArrayList();
					la.add(0, r1);
					la.add(1, t);
					lb.add(0, la);
					lb.add(1, 2);
					stmer.add(lb);
					// m.add(r1,t.getPredicate(),t.getObject());
				}
				OntProperty op1 = m.getOntProperty(r1.toString());
				OntProperty op2 = m.getOntProperty(r2.toString());
				if (op1 != null && op2 != null) {
					// r1为predicate
					for (StmtIterator it3 = m.listStatements(null, op1,
							(RDFNode) null); it3.hasNext();) {
						Statement t = it3.nextStatement();
						ArrayList la = new ArrayList();
						ArrayList lb = new ArrayList();
						la.add(0, op2);
						la.add(1, t);
						lb.add(0, la);
						lb.add(1, 3);
						stmer.add(lb);
						// m.add(t.getSubject(),op2,t.getObject());
					}
					// r2为predicate
					for (StmtIterator it3 = m.listStatements(null, op2,
							(RDFNode) null); it3.hasNext();) {
						Statement t = it3.nextStatement();
						ArrayList la = new ArrayList();
						ArrayList lb = new ArrayList();
						la.add(0, op1);
						la.add(1, t);
						lb.add(0, la);
						lb.add(1, 4);
						stmer.add(lb);
						// m.add(t.getSubject(),op1,t.getObject());
					}
				}
				// r1为object
				for (StmtIterator it3 = m.listStatements(null, null, r1); it3
						.hasNext();) {
					Statement t = it3.nextStatement();
					ArrayList la = new ArrayList();
					ArrayList lb = new ArrayList();
					la.add(0, r2);
					la.add(1, t);
					lb.add(0, la);
					lb.add(1, 5);
					stmer.add(lb);
					// m.add(t.getSubject(),t.getPredicate(),r2);
				}
				// r2为object
				for (StmtIterator it3 = m.listStatements(null, null, r2); it3
						.hasNext();) {
					Statement t = it3.nextStatement();
					ArrayList la = new ArrayList();
					ArrayList lb = new ArrayList();
					la.add(0, r1);
					la.add(1, t);
					lb.add(0, la);
					lb.add(1, 6);
					stmer.add(lb);
					// m.add(t.getSubject(),t.getPredicate(),r1);
				}
			}
		}

		/* 加入结果 */
		for (Iterator i = stmer.iterator(); i.hasNext();) {
			ArrayList la = (ArrayList) i.next();

			int ty = 0;
			ArrayList lb = (ArrayList) la.get(0);
			ty = ((Integer) la.get(1)).intValue();
			Resource c = (Resource) lb.get(0);
			Statement t = (Statement) lb.get(1);

			if (ty == 1 || ty == 2) {
				if (!(t.getPredicate().toString().equals(
						"http://www.w3.org/2002/07/owl#sameAs") && c.toString()
						.equals(t.getObject().toString()))) {
					m.add(c, t.getPredicate(), t.getObject());
				}
			} else if (ty == 3 || ty == 4) {
				if (!(c.toString().equals(
						"http://www.w3.org/2002/07/owl#sameAs") && t
						.getSubject().toString().equals(
								t.getObject().toString()))) {
					m.add(t.getSubject(), (Property) lb.get(0), t.getObject());
				}
			} else if (ty == 5 || ty == 6) {
				if (!(t.getPredicate().toString().equals(
						"http://www.w3.org/2002/07/owl#sameAs") && t
						.getSubject().toString().equals(c.toString()))) {
					m.add(t.getSubject(), t.getPredicate(), c);
				}
			}
		}

		// Step7.处理类层次的domain和Range
		int dNum, rNum;
		int subClassNum;
		int maxPreNum=300;
		String[] dName = new String[conceptNum];
		String[] rName = new String[maxPreNum];
		String[] subClassName = new String[maxPreNum];
		for (int i = 0; i < propNum; i++) {
			// get a property
			OntProperty p = m.getOntProperty(baseURI + propName[i]);

			// get its domain
			dNum = listDomainOfRelation(dName, p);

			// enrich domain
			for (int j = 0; j < dNum; j++) {
				String str = new String(baseURI + dName[j]);
				OntResource res = m.getOntResource(str);

				if (res != null) {
					// if the domain is a class
					if (res.isClass()
							&& !(res.toString()
									.equals("http://www.w3.org/2002/07/owl#Thing"))) {
						OntClass tempcs = m.getOntClass(res.toString());
						// enrich all the subclasses
						subClassNum = 0;
						// get all subconcepts
						subClassNum = listSubClassOfConcept(subClassNum,
								subClassName, tempcs);
						for (int k = 0; k < subClassNum; k++) {
							p.addDomain(m.getOntClass(baseURI + subClassName[k]));
						}
					}
				}
			}

			// get its range
			rNum = listRangeOfRelation(rName, p);

			// enrich range
			for (int j = 0; j < rNum; j++) {
				String str = new String(baseURI + rName[j]);
				OntResource res_temp = m.getOntResource(str);
				if (res_temp != null) {
					// if the range is a class
					if (res_temp.isClass()
							&& !(res_temp.toString()
									.equals("http://www.w3.org/2002/07/owl#Thing"))) {
						OntClass tempcs = m.getOntClass(res_temp.toString());
						// enrich all the subclasses
						subClassNum = 0;
						// get all subconcepts
						subClassNum = listSubClassOfConcept(subClassNum,
								subClassName, tempcs);
						for (int k = 0; k < subClassNum; k++) {
							p.addRange(m.getOntClass(baseURI+ subClassName[k]));
						}
					}
				}
			}
		}
		/*----------------*/

		// for (Iterator it=m.listStatements();it.hasNext();){
		// Statement st=(Statement)it.next();
		// System.out.println(st.toString());
		// }
		// System.out.println("在enrich后的本体:"+m.getGraph().size());
	}

	/***************************************************************************
	 * 取得一个Statement的S,P,O的Local Name
	 **************************************************************************/
	public ArrayList getStLocalName(Statement st) {
		ArrayList list = new ArrayList();
		Resource r = st.getSubject();
		Property p = st.getPredicate();
		RDFNode o = st.getObject();
		if (r.isURIResource()) {
			list.add(0, r.getLocalName());
		} else {
			list.add(0, r.toString());
		}

		if (p.isURIResource()) {
			list.add(1, p.getLocalName());
		} else {
			list.add(1, p.toString());
		}

		if (o.isURIResource()) {
			list.add(2, o.asNode().getLocalName());
		} else if (o.isLiteral()) {
			// System.out.println(o.toString());
			list.add(2, o.asNode().getIndexingValue().toString());
		} else {
			list.add(2, o.toString());
		}

		return list;
	}

	/***************************************************************************
	 * 本体语言的基本URI
	 */
	public Set getOntLngURI() {
		Set s = new HashSet();
		// RDF
		s.add("http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		// RDFS
		s.add("http://www.w3.org/2000/01/rdf-schema#");
		// OWL
		s.add("http://www.w3.org/2002/07/owl#");
		// XSD
		s.add("http://www.w3.org/2001/XMLSchema#");
		return s;
	}

	/***************************************************************************
	 * 本体语言的基本元语
	 */
	@SuppressWarnings("unchecked")
	public Set getOntLngMeta() {
		Set s = new HashSet();
		// RDF(S)
		s.add("Resource");
		s.add("type");
		s.add("label");
		s.add("comment");
		s.add("seeAlso");
		s.add("isDefinedBy");
		s.add("value");
		s.add("Literal");
		s.add("Class");
		s.add("subClassOf");
		s.add("Property");
		s.add("subProperty");
		s.add("domain");
		s.add("range");
		s.add("Container");
		s.add("member");
		s.add("Alt");
		s.add("Bag");
		s.add("Seq");
		s.add("ContainerMembershipProperty");
		s.add("List");
		s.add("first");
		s.add("rest");
		s.add("nil");
		s.add("Datatype");
		s.add("Statement");
		s.add("subject");
		s.add("predicate");
		s.add("object");
		// OWL
		s.add("equivalentClass");
		s.add("disjointWith");
		s.add("oneOf");
		s.add("intersectionOf");
		s.add("unionOf");
		s.add("complementOf");
		s.add("Restriction");
		s.add("onProperty");
		s.add("allValuesFrom");
		s.add("someValuesFrom");
		s.add("hasValue");
		s.add("cardinality");
		s.add("maxCardinality");
		s.add("minCardinality");
		s.add("DataRange");
		s.add("DeprecatedClass");
		s.add("DatatypeProperty");
		s.add("ObjectProperty");
		s.add("inverseOf");
		s.add("OntologyProperty");
		s.add("AnnotationProperty");
		s.add("FunctionalProperty");
		s.add("InverseFunctionalProperty");
		s.add("SymmetricProperty");
		s.add("TransitiveProperty");
		s.add("DeprecatedProperty");
		s.add("equivalentProperty");
		s.add("Thing");
		s.add("differentFrom");
		s.add("sameAs");
		s.add("Nothing");
		s.add("AllDifferent");
		s.add("distinctMembers");
		s.add("Ontology");
		s.add("backwardCompatibleWith");
		s.add("imports");
		s.add("incompatibleWith");
		s.add("priorVersion");
		s.add("versionInfo");

		return s;
	}

	/***************************************************************************
	 * 获得当前本体的不局限于baseURI的信息
	 **************************************************************************/
	@SuppressWarnings("unchecked")
	public ArrayList getFullOntInfo(OntModel m) {
		ArrayList result = new ArrayList();

		// 概念信息
		ArrayList listA = listFullConcepts(m);

		// 属性信息
		ArrayList listB = listFullRelations(m);

		// DatatypeProperty
		ArrayList listC = listFullDatatypeProperty(m);

		// ObjectProperty
		// DatatypeProperty
		ArrayList listD = listFullObjectProperty(m);

		// 实例信息
		ArrayList listE = listFullInstances(m);

		result.add(0, listA);
		result.add(1, listB);
		result.add(2, listC);
		result.add(3, listD);
		result.add(4, listE);

		return result;
	}

	/***************************************************************************
	 * 获得指定匿名概念
	 **************************************************************************/
	public OntClass getAnonClass(String name, OntModel m) {
		OntClass c = null;

		Iterator i = m.listClasses();
		while (i.hasNext()) {
			OntClass c_temp = (OntClass) i.next();
			if (c_temp.isAnon() && name.equals(c_temp.toString())) {
				c = c_temp;
				break;
			}
		}
		return c;
	}

	/***************************************************************************
	 * 获得全部匿名概念
	 **************************************************************************/
	public ArrayList getAllAnonClass(OntModel m) {
		ArrayList lt = new ArrayList();

		Iterator i = m.listClasses();
		while (i.hasNext()) {
			OntClass c_temp = (OntClass) i.next();
			if (c_temp.isAnon()) {
				if (!lt.contains(c_temp)) {
					lt.add(c_temp);
				}
			}
		}
		return lt;
	}

	/***************************************************************************
	 * 获得全部匿名属性
	 **************************************************************************/
	public ArrayList getAllAnonProperty(OntModel m) {
		ArrayList lt = new ArrayList();

		Iterator i = m.listDatatypeProperties();
		while (i.hasNext()) {
			OntProperty p_temp = (OntProperty) i.next();
			if (p_temp.isAnon()) {
				if (!lt.contains(p_temp)) {
					lt.add(p_temp);
				}
			}
		}
		Iterator j = m.listObjectProperties();
		while (j.hasNext()) {
			OntProperty p_temp = (OntProperty) j.next();
			if (p_temp.isAnon()) {
				if (!lt.contains(p_temp)) {
					lt.add(p_temp);
				}
			}
		}
		return lt;
	}

	/***************************************************************************
	 * 获得全部匿名实例
	 **************************************************************************/
	public ArrayList getAllAnonIndividual(OntModel m) {
		ArrayList lt = new ArrayList();

		Iterator i = m.listIndividuals();
		while (i.hasNext()) {
			Individual i_temp = (Individual) i.next();
			if (i_temp.isAnon()) {
				if (!lt.contains(i_temp)) {
					lt.add(i_temp);
				}
			}
		}
		return lt;
	}

	/***************************************************************************
	 * 获得指定匿名实例
	 **************************************************************************/
	public Individual getAnonIndividual(String name, OntModel m) {
		Individual x = null;

		Iterator i = m.listIndividuals();
		while (i.hasNext()) {
			Individual c_temp = (Individual) i.next();
			if (c_temp.isAnon() && name.equals(c_temp.toString())) {
				x = c_temp;
				// break;
			}
		}
		return x;
	}

	/***************************************************************************
	 * 获得指定匿名属性
	 **************************************************************************/
	public Property getAnonProperty(String name, OntModel m) {
		Property x = null;

		Iterator i = m.listDatatypeProperties();
		while (i.hasNext()) {
			Property c_temp = (Property) i.next();
			if (c_temp.isAnon() && name.equals(c_temp.toString())) {
				x = c_temp;
				// break;
			}
		}

		i = m.listObjectProperties();
		while (i.hasNext()) {
			Property c_temp = (Property) i.next();
			if (c_temp.isAnon() && name.equals(c_temp.toString())) {
				x = c_temp;
				break;
			}
		}

		return x;
	}

	/***************************************************************************
	 * 获得指定匿名资源
	 **************************************************************************/
	public ArrayList getAnonResource(String name, OntModel m) {
		ArrayList result = new ArrayList();
		int type = 0;

		OntClass c = this.getAnonClass(name, m);
		if (c != null) {
			result.add(0, c);
			type = 1;
		} else {
			Individual d = this.getAnonIndividual(name, m);
			if (d != null) {
				result.add(0, d);
				type = 3;
			} else {
				Property p = this.getAnonProperty(name, m);
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
	 * 判断是否Blank Node
	 **************************************************************************/
	public boolean isBlankNode(String str) {
		int a, b;
		a = 0;
		b = 0;
		if (str == null) {
			return true;
		}
		a = str.indexOf((int) ':');
		b = str.lastIndexOf((int) ':');
		if (a > 0 && b > 0 && ((b - a) == 12)) {
			return true;
		} else {
			return false;
		}
	}

	/***************************************************************************
	 * 获得本体的匿名资源信息
	 **************************************************************************/
	public ArrayList getOntAnonInfo(OntModel m) {
		ArrayList result = new ArrayList();
		ArrayList anonCnpt = new ArrayList();
		ArrayList anonProp = new ArrayList();
		ArrayList anonIns = new ArrayList();
		anonCnpt = getAllAnonClass(m);
		anonProp = getAllAnonProperty(m);
		anonIns = getAllAnonIndividual(m);
		result.add(0, anonCnpt);
		result.add(1, anonProp);
		result.add(2, anonIns);
		return result;
	}

	/***************************************************************************
	 * 获得指定三元组
	 **************************************************************************/
	public Statement getAStatement(OntModel m, Resource s, Property p, RDFNode o) {
		Statement st = null;
		Selector sl = new SimpleSelector(s, p, o);
		for (StmtIterator it = m.listStatements(sl); it.hasNext();) {
			st = it.next();
			if (st != null) {
				break;
			}
		}
		return st;
	}
	
	public ArrayList getAllStatement(OntModel m, Resource s, Property p, RDFNode o) {
		ArrayList lt=new ArrayList();
		Selector sl = new SimpleSelector(s, p, o);
		for (StmtIterator it = m.listStatements(sl); it.hasNext();) {
			Statement st = it.next();
			if (st != null) {
				lt.add(st);
			}
		}
		return lt;
	}
	
	/***************************************************************************
	 * 给据给定的fullName，找到对应的concept
	 **************************************************************************/
	public OntClass findCnptInFullName(String s,OntClass[] fullConceptName, int fullConceptNum)
	{
		OntClass c=null;
		for (int i=0;i<fullConceptNum;i++){
			if (fullConceptName[i].toString().equals(s)){
				c=fullConceptName[i];
				break;
			}
		}
		return c;
	}
	
	/***************************************************************************
	 * 大本体上下文快照提取算法--版本1.0
	 **************************************************************************/
	public OntModel getResSnapshot(Resource r, OntModel orgModel, int size){
		OntModel outModel = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
		OWLOntParse ontParse=new OWLOntParse();
		
		/*初始Seed*/
		Set stmSet=new HashSet();
		Selector selector = new SimpleSelector(orgModel.getResource(r.toString()),null,(RDFNode)null);
    	for (StmtIterator Iter = orgModel.listStatements( selector);Iter.hasNext();)
    	{
    		Statement ts = Iter.next();
    		if (!(ts.getPredicate().toString().equals("http://www.w3.org/2000/01/rdf-schema#subClassOf"))){
        		outModel.add(ts);
        		stmSet.add(ts);
    		}
    	}
//    	selector = new SimpleSelector(null,orgModel.getProperty(r.toString()),(RDFNode)null);
//    	for (StmtIterator Iter = orgModel.listStatements( selector);Iter.hasNext();)
//    	{
//    		Statement ts = (Statement) Iter.next();
//    		outModel.add(ts);
//    		stmSet.add(ts);
//    	}
    	selector = new SimpleSelector(null,null, r);
    	for (StmtIterator Iter = orgModel.listStatements( selector);Iter.hasNext();)
    	{
    		Statement ts = Iter.next();
    		if (!(ts.getPredicate().toString().equals("http://www.w3.org/2000/01/rdf-schema#subClassOf"))){
        		outModel.add(ts);
        		stmSet.add(ts);
    		}
    	}
    	
    	Set newStmSet=new HashSet();
    	boolean update=true;
    	
    	while(outModel.size()<size && update){
    		newStmSet=((Set)((HashSet) stmSet).clone());
    	    
    		for (Iterator itx=stmSet.iterator();itx.hasNext();){
    			Statement ts = (Statement)itx.next();
    			Resource sub=ts.getSubject();
    			Property prop=ts.getPredicate();
    			RDFNode obj=ts.getObject();
    			String suri=null,puri=null,ouri=null;
    			if (sub.isURIResource()){
    				suri=sub.getNameSpace();
    			}
    			if (prop.isURIResource()){
    				puri=prop.getNameSpace();
    			}
    			if (obj.isURIResource()){
    				ouri=obj.asNode().getNameSpace();
    			}
    			
    			int countTotal=0;
    			int count=0;
    			if (!ontParse.metaURISet.contains(suri)){
//    				扩展subject
        			selector = new SimpleSelector(sub,null,(RDFNode)null);
        	    	for (StmtIterator Iter = orgModel.listStatements( selector);Iter.hasNext();)
        	    	{
        	    		Statement tmpst = Iter.next();
        	    		if (!outModel.contains(tmpst)){
        	    			outModel.add(tmpst);
        	    			newStmSet.add(tmpst);
        	    			count++;
        	    			break;
        	    		}    	    		
        	    	}
        	    	if (count<1){
            	    	selector = new SimpleSelector(null,null, sub);
            	    	for (StmtIterator Iter = orgModel.listStatements( selector);Iter.hasNext();)
            	    	{
            	    		Statement tmpst = Iter.next();
            	    		if (!outModel.contains(tmpst)){
            	    			outModel.add(tmpst);
            	    			newStmSet.add(tmpst);
            	    			count++;
            	    			break;
            	    		}    	
            	    	}
        	    	}
        	    	if (count<1){
            	    	selector = new SimpleSelector(null,orgModel.getProperty(sub.toString()),(RDFNode)null);
            	    	for (StmtIterator Iter = orgModel.listStatements( selector);Iter.hasNext();)
            	    	{
            	    		Statement tmpst = Iter.next();
            	    		if (!outModel.contains(tmpst)){
            	    			outModel.add(tmpst);
            	    			newStmSet.add(tmpst);
            	    			count++;
            	    			break;
            	    		}    
            	    	}
        	    	}
        	    	countTotal+=count;
    			}
    			
    			if (!ontParse.metaURISet.contains(ouri)){
//    				//扩展object
        	    	count=0;
        			selector = new SimpleSelector(orgModel.getResource(obj.toString()),null,(RDFNode)null);
        	    	for (StmtIterator Iter = orgModel.listStatements( selector);Iter.hasNext();)
        	    	{
        	    		Statement tmpst = Iter.next();
        	    		if (!outModel.contains(tmpst)){
        	    			outModel.add(tmpst);
        	    			newStmSet.add(tmpst);
        	    			count++;
        	    			break;
        	    		}    	    		
        	    	}
        	    	if (count<1){
            	    	selector = new SimpleSelector(null,null, obj);
            	    	for (StmtIterator Iter = orgModel.listStatements( selector);Iter.hasNext();)
            	    	{
            	    		Statement tmpst = Iter.next();
            	    		if (!outModel.contains(tmpst)){
            	    			outModel.add(tmpst);
            	    			newStmSet.add(tmpst);
            	    			count++;
            	    			break;
            	    		}    	
            	    	}
        	    	}
        	    	if (count<1){
            	    	selector = new SimpleSelector(null,orgModel.getProperty(obj.toString()),(RDFNode)null);
            	    	for (StmtIterator Iter = orgModel.listStatements( selector);Iter.hasNext();)
            	    	{
            	    		Statement tmpst = Iter.next();
            	    		if (!outModel.contains(tmpst)){
            	    			outModel.add(tmpst);
            	    			newStmSet.add(tmpst);
            	    			count++;
            	    			break;
            	    		}    
            	    	}
        	    	}
        	    	countTotal+=count;
    			}
    			
    			if (!ontParse.metaURISet.contains(puri)){
        	    	if (countTotal<2){
            			//扩展property
            	    	count=0;
            			selector = new SimpleSelector(orgModel.getResource(prop.toString()),null,(RDFNode)null);
            	    	for (StmtIterator Iter = orgModel.listStatements( selector);Iter.hasNext();)
            	    	{
            	    		Statement tmpst = Iter.next();
            	    		if (!outModel.contains(tmpst)){
            	    			outModel.add(tmpst);
            	    			newStmSet.add(tmpst);
            	    			count++;
            	    			break;
            	    		}    	    		
            	    	}
            	    	if (count<1){
                	    	selector = new SimpleSelector(null,null, prop);
                	    	for (StmtIterator Iter = orgModel.listStatements( selector);Iter.hasNext();)
                	    	{
                	    		Statement tmpst = Iter.next();
                	    		if (!outModel.contains(tmpst)){
                	    			outModel.add(tmpst);
                	    			newStmSet.add(tmpst);
                	    			count++;
                	    			break;
                	    		}    	
                	    	}
            	    	}
            	    	if (count<1){
                	    	selector = new SimpleSelector(null,prop,(RDFNode)null);
                	    	for (StmtIterator Iter = orgModel.listStatements( selector);Iter.hasNext();)
                	    	{
                	    		Statement tmpst = Iter.next();
                	    		if (!outModel.contains(tmpst)){
                	    			outModel.add(tmpst);
                	    			newStmSet.add(tmpst);
                	    			count++;
                	    			break;
                	    		}    
                	    	}
            	    	}
        	    	}
    			}
    	    }
    		if (newStmSet.size()==stmSet.size()){
    			update=false;
    		}
    		else{
    			/*更新stmSet*/
        		stmSet=((Set)((HashSet) newStmSet).clone());
    		}    		
    	}
    	
    	//修正rdf:type
		for (Iterator itx=stmSet.iterator();itx.hasNext();){
			Statement ts = (Statement)itx.next();
			Resource sub=ts.getSubject();
			Property prop=ts.getPredicate();
			RDFNode obj=ts.getObject();
			String suri=null,puri=null,ouri=null;
			if (sub.isURIResource()){
				suri=sub.getNameSpace();
			}
			if (prop.isURIResource()){
				puri=prop.getNameSpace();
			}
			if (obj.isURIResource()){
				ouri=obj.asNode().getNameSpace();
			}
			
			if (!ontParse.metaURISet.contains(suri)){
				Statement sx=ontParse.getAStatement(orgModel,sub,
						orgModel.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), null);
				if (sx!=null && !outModel.contains(sx)){
					outModel.add(sx);
				}
			}
			if (!ontParse.metaURISet.contains(puri)){
				Statement sx=ontParse.getAStatement(orgModel,orgModel.getResource(prop.toString()),
						orgModel.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), null);
				if (sx!=null && !outModel.contains(sx)){
					outModel.add(sx);
				}
			}
			if (!ontParse.metaURISet.contains(ouri)){
				Statement sx=ontParse.getAStatement(orgModel,orgModel.getResource(obj.toString()),
						orgModel.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), null);
				if (sx!=null && !outModel.contains(sx)){
					outModel.add(sx);
				}
			}
		}
    	
		return outModel;
	}

	public String getOntHead(OntModel m) {
		String headName=null;
		for (StmtIterator it = m.listStatements(null,m.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),m.getResource("http://www.w3.org/2002/07/owl#Ontology"));it.hasNext();){
			Statement t= it.nextStatement();
			headName=t.getSubject().toString();
		}
		return headName;
	}
	
	/**判断概念是否为层次结构中的根节点**/
	public boolean isCnptHRoot(OntModel m, OntClass c){
		boolean flag=true;
		/*判断有没有父概念*/
		try{
			if (!c.listSuperClasses().toList().isEmpty()) {
				/*判断父概念中不全是匿名节点*/
				for (ExtendedIterator it=c.listSuperClasses();it.hasNext();)
				{
					OntClass tc=(OntClass)it.next();
					/*寻找是否有不是匿名节点的*/
					if (!isBlankNode(tc.toString()) && tc.getNameSpace().equals(c.getNameSpace())){
						flag=false;
						break;
					}
				}
			}
			else {
				flag=true;
			}	
		}
		catch (ConversionException e){
			flag=false;
		}	
		return flag;
	}
	
	/**计算概念在层次结构中的度，不统计匿名节点**/
	public int getCnptDegreeInHierarchy(OntClass c){
		int degree=0;
		ArrayList lt=listDirectSuperClassOfConcept(c);
		for (Iterator it=lt.iterator();it.hasNext();){
			OntClass tc=(OntClass)it.next();
			if (!isBlankNode(tc.toString())){
				degree++;
			}
		}
		lt=listDirectSubClassOfConcept(c);
		for (Iterator it=lt.iterator();it.hasNext();){
			OntClass tc=(OntClass)it.next();
			if (!isBlankNode(tc.toString())){
				degree++;
			}
		}
		return degree;
	}
	
	/***********************
	 * 修补本体未明确声明的类型
	 **********************/
	public void repairOntType(OntModel m){
		Set newSt=new HashSet();
		
		/*修补subClassOf声明的概念*/
		Property ISAp=m.getProperty("http://www.w3.org/2000/01/rdf-schema#subClassOf");
		Property TYPEp=m.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		Selector selector = new SimpleSelector(null,ISAp,(RDFNode)null);
		for (StmtIterator itx = m.listStatements(selector);itx.hasNext();)
    	{
			Statement st= itx.next();
			Resource objRes=m.getResource(st.getObject().toString());
			Selector tsl =new SimpleSelector(objRes,TYPEp,(RDFNode)null);
			if ((m.listStatements(tsl)).toList().isEmpty()){
				Statement stAdd= m.createStatement(objRes,TYPEp,m.getResource("http://www.w3.org/2002/07/owl#Class"));
				newSt.add(stAdd);
			}
    	}
		
		for (Iterator itx=newSt.iterator();itx.hasNext();){
			Statement st=(Statement)itx.next();
			m.add(st);
		}		
		
	}
}
