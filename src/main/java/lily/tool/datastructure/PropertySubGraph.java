/************************************************
 * Source code information
 * -----------------------
 * Original author	 Peng Wang, School of Computer Science & Eng., Southeast University
 * Author email      pwangseu@gmail.com
 * Web               http://ontomapping.googlepages.com
 * Created			 2007-5-13
 * Filename          PropertySubGraph.java
 * Version           2.0
 * 
 * Last modified on  2007-5-13
 *               by  Peng Wang
 * -----------------------
 * Functions describe:
 *
 ***********************************************/
package lily.tool.datastructure;

import java.util.ArrayList;

import org.jgrapht.DirectedGraph;

/*********************
 * Class information
 * -------------------
 * @author Peng Wang
 * @date   2007-5-13
 * 
 * describe:
 * 属性语义子图
 ********************/
public class PropertySubGraph {
	public String propName;

	public DirectedGraph subGraph;

	public ArrayList stmList;
}
