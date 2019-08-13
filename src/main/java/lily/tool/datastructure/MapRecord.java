/************************************************
 * Source code information
 * -----------------------
 * Original author	 Peng Wang, School of Computer Science & Eng., Southeast University
 * Author email      pwangseu@gmail.com
 * Web               http://ontomapping.googlepages.com
 * Created			 2007-4-26
 * Filename          MapResult.java
 * Version           2.0
 * 
 * Last modified on  2007-4-26
 *               by  Peng Wang
 * -----------------------
 * Functions describe:
 * 映射结果的数据结构
 ***********************************************/
package lily.tool.datastructure;

import java.lang.Comparable;

/*********************
 * Class information
 * -------------------
 * @author Peng Wang
 * @date   2007-4-26
 * 
 * describe:
 * 映射结果数据结构
 ********************/
public class MapRecord implements Comparable<MapRecord> {

	public String sourceLabel;

	public double similarity;

	public String targetLabel;

	public int relationType;
	
	public MapRecord()
	{
		//初始化
		similarity = -1;
		relationType = -1;
	}
	
	public void show()
	{
		char mptype[]={'=','>','<'};
		System.out.println(sourceLabel+mptype[relationType]+targetLabel);
		System.out.println("\t"+similarity);
	}

	public int compareTo(MapRecord arg0) {
		return Double.valueOf(this.similarity).compareTo(arg0.similarity);
	}

}
