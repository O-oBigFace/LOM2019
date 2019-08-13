/************************************************
 * Source code information
 * -----------------------
 * Original author	 Peng Wang, School of Computer Science & Eng., Southeast University
 * Author email      pwangseu@gmail.com
 * Web               http://ontomapping.googlepages.com
 * Created			 2007-4-27
 * Filename          SimpleFilter.java
 * Version           2.0
 * 
 * Last modified on  2007-4-27
 *               by  Peng Wang
 * -----------------------
 * Functions describe:
 * 一般的相似矩阵结果过滤方法
 ***********************************************/
package lily.tool.filter;

import java.util.*;

import cern.colt.list.DoubleArrayList;
import cern.colt.list.IntArrayList;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import javafx.util.Pair;
import org.jblas.DoubleMatrix;

/*********************
 * Class information
 * -------------------
 * @author Peng Wang
 * @date   2007-4-27
 * 
 * describe:
 * 结果过滤处理
 ********************/
public class SimpleFilter {
	private class matValue {
		public int x, y;
		public Double v;
		matValue(final int x, final int y, final double v) {
			this.x = x;
			this.y = y;
			this.v = v;
		}
	}

	Comparator<matValue> matValueComparator = new Comparator<matValue>() {
		public int compare(final matValue s1, final matValue s2) {
			return s2.v.compareTo(s1.v); //注意是负逻辑
		}
	};

	//普通的只取最大值的结果过滤
	//只取大于阀值的行列上的最大值
	//需要排序来做辅助
	public double[][] maxValueFilter(int n,int m, double[][] sim, double threshold)
	{
		int maxNum = 0;
		int count = 0;
		double[][] simFilter=new double[n][m];
		//SparseDoubleMatrix2D sparseMx= new SparseDoubleMatrix2D(n,m);
		//double[] row;
		ArrayList<matValue> row = new ArrayList<>();
		//int[] matchA,matchB;
		//IntArrayList rowList=new IntArrayList();
        //IntArrayList colList=new IntArrayList();
        //DoubleArrayList valueList=new DoubleArrayList();
        
        /*压缩存储相似矩阵*/
        //sparseMx.assign(sim);
        //sparseMx.getNonZeros(rowList,colList,valueList);
		//row=new double[rowList.size()];
		for (int i=0; i<n; ++i)
			for (int j=0; j<m; ++j)
				if (sim[i][j] != 0)
				{
					++count;
					row.add(new matValue(i, j, sim[i][j]));
				}
		/*
		//结果放入一个数组中
		for (int i=0;i<valueList.size();i++){
			row[count]=valueList.get(i);
			count++;
		}
		*/
//		System.out.println("Arrays.sort(row);//排序");
		Collections.sort(row, matValueComparator); //降序排序
		maxNum = Math.max(n,m);
		//matchA= new int[maxNum];
		//matchB= new int[maxNum];
		//for (int i=0;i<maxNum;i++){matchA[i]=-1;matchB[i]=-1;}
		matValue mat;
		boolean[] matchA = new boolean[maxNum];
		boolean[] matchB = new boolean[maxNum];
		for (int k = 0; k < count; ++k) {
			mat = row.get(k);
			if (mat.v > threshold) {
                if (!matchA[mat.x] && !matchB[mat.y]) {
                    matchA[mat.x] = matchB[mat.y] = true;
                    simFilter[mat.x][mat.y] = mat.v;
                }
            } else {
                break;
            }
		}
		/*
		int k = count-1;
		while(k>=0 && row[k]>threshold)
		{
//			System.out.println(k);

			for (int j=0;j<valueList.size();j++){
				int curRow=rowList.get(j);
				int curCol=colList.get(j);
				if (valueList.get(j)==row[k] && (matchA[curRow]==-1) && (matchB[curCol]==-1)){
					matchA[curRow]=curCol;
					matchB[curCol]=curRow;
					simFilter[curRow][curCol]=row[k];
					break;
				}				
			}
			k--;
		}
		*/
		return simFilter; 
	}
	
	public SparseDoubleMatrix2D maxValueFilter(int n,int m, SparseDoubleMatrix2D sim,double threshold)
	{
		int maxNum = 0;
		int count = 0;
		SparseDoubleMatrix2D simFilter= new SparseDoubleMatrix2D(n,m);
		double[] row;
		int[] matchA,matchB;
		IntArrayList rowList=new IntArrayList();
        IntArrayList colList=new IntArrayList();
        DoubleArrayList valueList=new DoubleArrayList();
        HashMap value2Pos = new HashMap();
        
		sim.getNonZeros(rowList,colList,valueList);
		row=new double[rowList.size()];
		
		//结果放入一个数组中
		for (int i=0;i<valueList.size();i++){
			row[count]=valueList.get(i);
			/*构造值和位置对应的HashMap*/
			int[] a=new int[2];
			a[0]=rowList.get(i);
			a[1]=colList.get(i);
			Set tset=new HashSet();
			if (value2Pos.keySet().contains(row[count])){
				tset=(Set)value2Pos.get(row[count]);
			}			
			tset.add(a);
			value2Pos.put(row[count],tset);
			/*下一个位置*/
			count++;
		}
		
//		System.out.println("Arrays.sort(row);//排序");
		Arrays.sort(row);//排序
		maxNum = Math.max(n,m);
		matchA= new int[maxNum];
		matchB= new int[maxNum];
		for (int i=0;i<maxNum;i++){matchA[i]=-1;matchB[i]=-1;}
				
		int k = count-1;
		while(k>=0 && row[k]>threshold)
		{
//			System.out.println(k);
			Set posSet=(Set)value2Pos.get(row[k]);
			for (Iterator it=posSet.iterator();it.hasNext();){
				int[] a= (int[])it.next();
				int curRow=a[0];
				int curCol=a[1];
				if ((matchA[curRow]==-1) && (matchB[curCol]==-1)){
					matchA[curRow]=curCol;
					matchB[curCol]=curRow;
					simFilter.setQuick(curRow,curCol,row[k]);
					break;
				}	
			}
			k--;
		}
		return simFilter; 
	}	

}
