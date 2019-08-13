/************************************************
 * Source code information
 * -----------------------
 * Original author	 Peng Wang, School of Computer Science & Eng., Southeast University
 * Author email      pwangseu@gmail.com
 * Web               http://ontomapping.googlepages.com
 * Created			 2007-5-3
 * Filename          StmComparator.java
 * Version           2.0
 * 
 * Last modified on  2007-5-3
 *               by  Peng Wang
 * -----------------------
 * Functions describe:
 * Statement排序
 ***********************************************/
package lily.tool.datastructure;

import java.util.Comparator;

import com.hp.hpl.jena.rdf.model.Statement;

/*********************
 * Class information
 * -------------------
 * @author Peng Wang
 * @date   2007-5-3
 * 
 * describe:
 * Statement排序
 ********************/
public class StmComparator implements Comparator{
	public int compare(Object o1, Object o2) {
		Statement p1 = (Statement) o1;
		Statement p2 = (Statement) o2;
		if ((p1.toString()).compareTo(p2.toString()) >= 0)
			return 1;
		else
			return 0;
	}
}
