/************************************************
 * Source code information
 * -----------------------
 * Original author	 Peng Wang, School of Computer Science & Eng., Southeast University
 * Author email      pwangseu@gmail.com
 * Web               http://ontomapping.googlepages.com
 * Created			 2007-4-26
 * Filename          StableMarriageFilter.java
 * Version           2.0
 * 
 * Last modified on  2007-4-26
 *               by  Peng Wang
 * -----------------------
 * Functions describe:
 *
 ***********************************************/
package lily.tool.filter;


import java.util.*;
import lily.tool.stablemarriage.*;
/*********************
 * Class information
 * -------------------
 * @author Peng Wang
 * @date   2007-4-26
 * 
 * describe:
 * 用stable marriage方法来过滤图匹配的结果
 ********************/
public class StableMarriageFilter {
	public double[][] run(double[][] sim, int Mnum, int Wnum) 
	{
		boolean change=false;
		StableMarriage s = new StableMarriage();
		
		//每行从大到小排序,结果放入一个LinkedList
		LinkedList[] menPre = new LinkedList[Mnum];
		for (int i = 0;i<Mnum;i++)
		{
			double[] array = new double[Wnum];
			System.arraycopy( sim[i],0,array,0,sim[i].length);//拷贝一行
			this.Sort(array,Wnum);//排序
			//构造LinkedList
			menPre[i] = new LinkedList();
			for(int k=0;k<Wnum;k++)
			{
				for (int j=0;j<Wnum;j++)
				{
					if (array[k]==sim[i][j] && !menPre[i].contains(j))
					{
						//记录LinkedList
						menPre[i].add(j);
						break;
					}
				}
			}
		}
		
		//每列从大到小排序,结果放入一个LinkedList
		LinkedList[] womenPre = new LinkedList[Wnum];
		for (int i = 0;i<Wnum;i++)
		{
			double[] array = new double[Mnum];
			for (int j=0;j<Mnum;j++) {array[j] = sim[j][i];}//拷贝一列
			
			this.Sort(array,Mnum);//排序
			
			//构造LinkedList
			womenPre[i] = new LinkedList();
			for(int k=0;k<Mnum;k++)
			{
				for (int j=0;j<Mnum;j++)
				{
					if (array[k]==sim[j][i] && !womenPre[i].contains(j))
					{
						//记录LinkedList
						womenPre[i].add(j);
						break;
					}
				}
			}
		}
		
		//把小数作为Men,大数作为Women.缺省下Men为行,Women为列
		
		//如果Men>Women,改变行列
		change = (Mnum>Wnum);
			
		if (!change)
		{//没有改变行列
			//初始化Men和Women的人数
			s.setnumber(Mnum,Wnum);
			//把Prefer传入
			s.setpreferences(menPre,womenPre);
		}
		else
		{//改变了行列
			//交换Men和Women
			//初始化Men和Women的人数
			s.setnumber(Wnum,Mnum);
			
			//把Prefer传入
			s.setpreferences(womenPre,menPre);
		}
		
		//开始选择
		s.run();
		
		int[] map = new int[Math.min(Wnum,Mnum)];
		
		//获得结果
		map = s.getresult();
		
		//处理结果
		if(!change)
		{
			//按没有换行列处理
			for (int i = 0;i<Mnum;i++)
			{
				for (int j=0;j<Wnum;j++)
				{
					if (j!=map[i])
					{
						sim[i][j] = 0;
					}
				}
			}
		}
		else
		{
			//按换行列后处理
			for (int col = 0;col<Wnum;col++)
			{
				for (int row=0;row<Mnum;row++)
				{
					if (row!=map[col])
					{
						sim[row][col] = 0;
					}
				}
			}
		}
		
		return sim;
	}
	
	public  void Sort(double[] pData, int Num) {
		int i, j;
		int itemp;
		double max;
		
		for (i=0;i<Num-1;i++)
		{
			max = pData[i];
			itemp = i;
			for (j=i+1;j<Num;j++)
			{
				if (pData[j]>max)
				{
					itemp=j;
					max = pData[j];
				}
			}
			if (itemp!=i)
			{
				pData[itemp]=pData[i];
				pData[i]=max;
			}
		}
	}
}
