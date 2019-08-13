/************************************************
 * Source code information
 * -----------------------
 * Original author	 Peng Wang, School of Computer Science & Eng., Southeast University
 * Author email      pwangseu@gmail.com
 * Web               http://ontomapping.googlepages.com
 * Created			 2007-5-14
 * Filename          Word.java
 * Version           2.0
 * 
 * Last modified on  2007-5-14
 *               by  Peng Wang
 * -----------------------
 * Functions describe:
 *
 ***********************************************/
package lily.tool.datastructure;

/*********************
 * Word的数组版本
 ********************/
public class MultiWord implements Cloneable{
	public String[] content;
	public double weight;

	public Object clone() {
		Object o = null;
		try {
			o = super.clone();
		} catch (CloneNotSupportedException e) {
			System.err.println("Can't clone");
		}
		return o;
	}
}
