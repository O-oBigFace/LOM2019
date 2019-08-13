/************************************************
 * Source code information
 * -----------------------
 * Original author	 Peng Wang, School of Computer Science & Eng., Southeast University
 * Author email      pwangseu@gmail.com
 * Web               http://ontomapping.googlepages.com
 * Created			 2007-4-26
 * Filename          ConceptSubGraph.java
 * Version           2.0
 * 
 * Last modified on  2007-4-26
 *               by  Peng Wang
 * -----------------------
 * Functions describe:
 * Concept语义子图数据结构
 ***********************************************/
package lily.tool.datastructure;

import java.util.ArrayList;

import org.jgrapht.DirectedGraph;

/*********************
 * Class information
 * -------------------
 * @author Peng Wang
 * @date   2007-4-26
 * 
 * describe:
 * Data Structure: subgraph of the concept
 ********************/
public class ConceptSubGraph {
	public String conceptName;

	public DirectedGraph subGraph;

	public ArrayList stmList;
}
