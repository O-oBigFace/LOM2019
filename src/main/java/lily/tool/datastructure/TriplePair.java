/************************************************
 * Source code information
 * -----------------------
 * Original author	 Peng Wang, School of Computer Science & Eng., Southeast University
 * Author email      pwangseu@gmail.com
 * Web               http://ontomapping.googlepages.com
 * Created			 2007-7-21
 * Filename          TriplePair.java
 * Version           2.0
 * 
 * Last modified on  2007-7-21
 *               by  Peng Wang
 * -----------------------
 * Functions describe:
 *
 ***********************************************/
package lily.tool.datastructure;

import com.hp.hpl.jena.rdf.model.Statement;

/*********************
 * Class information
 * -------------------
 * @author Peng Wang
 * @date   2007-7-21
 * 
 * describe:
 * 相似三元组对数据结构
 ********************/
public class TriplePair {
	public Statement tripleA;

	public Statement tripleB;

	public double simS;

	public double simP;

	public double simO;

	public double simSr;

	public double simPr;

	public double simOr;

	public boolean sIsMeta;

	public boolean pIsMeta;

	public boolean oIsMeta;

	public double weight;
}
