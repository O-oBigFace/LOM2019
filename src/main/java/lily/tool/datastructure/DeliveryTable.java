/************************************************
 * Source code information
 * -----------------------
 * Original author	 Peng Wang, School of Computer Science & Eng., Southeast University
 * Author email      pwangseu@gmail.com
 * Web               http://ontomapping.googlepages.com
 * Created			 2007-4-26
 * Filename          DeliveryTable.java
 * Version           2.0
 * 
 * Last modified on  2007-4-26
 *               by  Peng Wang
 * -----------------------
 * Functions describe:
 *
 ***********************************************/
package lily.tool.datastructure;

/*********************
 * Class information
 * -------------------
 * @author Peng Wang
 * @date   2007-4-26
 * 
 * describe:
 * Data Structure: delivery matrix table D(v,k)
 ********************/
public class DeliveryTable {
	public double deliveryCurrent;

	public int predV;

	public int predK;// path from s to v
}
