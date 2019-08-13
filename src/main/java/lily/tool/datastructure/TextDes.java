/************************************************
 * Source code information
 * -----------------------
 * Original author	 Peng Wang, School of Computer Science & Eng., Southeast University
 * Author email      pwangseu@gmail.com
 * Web               http://ontomapping.googlepages.com
 * Created			 2007-5-14
 * Filename          TextDes.java
 * Version           2.0
 * 
 * Last modified on  2007-5-14
 *               by  Peng Wang
 * -----------------------
 * Functions describe:
 *
 ***********************************************/
package lily.tool.datastructure;

import java.util.ArrayList;

/*********************
 * Class information
 * -------------------
 * @author Peng Wang
 * @date   2007-5-14
 * 
 * describe:
 * 文本描述数据结构
 ********************/
public class TextDes {
	public String name;

	public ArrayList text;

	public int type;// 增加的变量，记录元素的类型，避免不必要的计算
}
