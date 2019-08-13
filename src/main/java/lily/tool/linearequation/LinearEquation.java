/************************************************
 * Source code information
 * -----------------------
 * Original author	 Peng Wang, School of Computer Science & Eng., Southeast University
 * Author email      pwangseu@gmail.com
 * Web               http://ontomapping.googlepages.com
 * Created			 2007-4-26
 * Filename          LinearEquation.java
 * Version           2.0
 * 
 * Last modified on  2007-4-26
 *               by  Peng Wang
 * -----------------------
 * Functions describe:
 * 求解线性方程组
 ***********************************************/
package lily.tool.linearequation;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import Jama.Matrix;

/*********************
 * Class information
 * -------------------
 * @author Peng Wang
 * @date   2007-4-26
 * 
 * describe:
 * This class realize a general process to solve a linear system 
 * such as AX=b
 * 
 * I use a package called Jama. It is fast that can solve a linear
 * system with 5000*5000 matrix in a few seconds 
 ********************/
public class LinearEquation {
	public int n;// dimension of the matrix

	public double[] Voltage;

	public double[][] Conduct;

	public double[] SumCdtX;

	public double[][] FinalCoefMatrix;// Matrix A

	public double[][] BVctRight;// Vector b

	public int star, end; // star vertex and end Vertex

	public Matrix x; // result matrix x
	
	public static int count=0;
		
	public void InitlizePara(int Num, int s_star, int s_end)	
	
	{
		n = Num;
		star = s_star;
		end = s_end;
		Voltage = new double[n];
		Conduct = new double[n][n];
		FinalCoefMatrix = new double[n][n];
		SumCdtX = new double[n];
		BVctRight = new double[n][1];
	}
	
	public void SetConductMatrix(double[][]inputMatrix)
	{
		//Given conductance
		Conduct =inputMatrix;

	}
	
	public void PrepareMatrixA_b(double starVolt, double endVolt)
	{
		int i,j;

		//Compute Sum conductance at each vertex
		for (i = 0; i < n; i++)
		{
			SumCdtX[i] = 0;
			for (j = 0; j < n; j++)
			{
				SumCdtX[i] += Conduct[i][j];
			}
		}
		
		//Compute final coefficient used in equations
		
		for (i=0;i<n;i++)
		{
			for(j=0;j<n;j++)
			{
				if (i==star)
				{
					if (j==star)
					{
						FinalCoefMatrix[i][j] = 1.0;
					}
					else
					{
						FinalCoefMatrix[i][j] = 0.0;
					}
				
				}
				else if (i==end)
				{
					if (j==end)
					{
						FinalCoefMatrix[i][j] = 1.0;
					}
					else
					{
						FinalCoefMatrix[i][j] = 0.0;
					}
				}
				else
				{
					if (i==j) {FinalCoefMatrix[i][j] = -1.0;}
					else {FinalCoefMatrix[i][j] = Conduct[i][j]/SumCdtX[i];}
				}
			}
		}

		//construct the b of linear equation
		for (i=0;i<n;i++)
		{
			if (i==star) {
				BVctRight[i][0] = starVolt;
				}
			else {
				BVctRight[i][0] = endVolt;
				}
		}
	}
	
	public void Solve()
	{
		// solve the equation
		Matrix A = new Matrix(FinalCoefMatrix);
		Matrix b = new Matrix(BVctRight);
		// System.out.println("行列式："+(float)A.det());
		x = A.solve(b);
		
//		count++;
//		System.out.println("@@@@@@@@@@@"+count);
//		
//		saveEquation(A, b, x);
		// A.print(3,2);
		// b.print(3,2);
		// x.print(3,2);
		
		/***用mtj实现的迭代解法***
		no.uib.cipr.matrix.DenseMatrix tempMA = new no.uib.cipr.matrix.DenseMatrix(A);
		CompRowMatrix MA=new CompRowMatrix(tempMA);
		DenseVector Vx=new DenseVector(vx);
		DenseVector Vb=new DenseVector(vb);
		
		GMRES solver = new GMRES (Vx);
		try {
			solver.solve(MA, Vb, Vx);
		} catch (IterativeSolverNotConvergedException e) {
			System.err.println("Iterative solver failed to converge");
		}
		**********/
		
	}
	
	private void saveEquation(Matrix A, Matrix b, Matrix x) {
		try {
			FileWriter out = new FileWriter("E:/temp/LEQData/A"+count+".dat");
			BufferedWriter bw = new BufferedWriter(out);
		//写入文件
			bw.write(String.valueOf(n));
			bw.newLine();
			for (int i=0;i<n;i++){
				for (int j=0;j<n;j++){
					bw.write(String.valueOf(A.get(i,j))+" ");
				}
				bw.newLine();
			}
			
		//关闭文件
			bw.close();
			out.close();
		} catch (IOException e) {
   		System.err.println("Can't open result file:\n" + e.toString());
   		System.exit(1);
		}
		
		try {
			FileWriter out = new FileWriter("E:/temp/LEQData/b"+count+".dat");
			BufferedWriter bw = new BufferedWriter(out);
		//写入文件
			bw.write(String.valueOf(n));
			bw.newLine();
			for (int i=0;i<n;i++){
				bw.write(String.valueOf(b.get(i,0))+" ");
				bw.newLine();
			}
			
		//关闭文件
			bw.close();
			out.close();
		} catch (IOException e) {
   		System.err.println("Can't open result file:\n" + e.toString());
   		System.exit(1);
		}
		
		try {
			FileWriter out = new FileWriter("E:/temp/LEQData/x"+count+".dat");
			BufferedWriter bw = new BufferedWriter(out);
		//写入文件
			bw.write(String.valueOf(n));
			bw.newLine();
			for (int i=0;i<n;i++){
				bw.write(String.valueOf(x.get(i,0))+" ");
				bw.newLine();
			}
			
		//关闭文件
			bw.close();
			out.close();
		} catch (IOException e) {
   		System.err.println("Can't open result file:\n" + e.toString());
   		System.exit(1);
		}
	}

	public double[] GetResultMaxtrix()
	{
		double[][] MX = x.getArray();
		int n=x.getRowDimension();
		double[] mat = new double[n];
		for (int i=0;i<n;i++){
			mat[i]=MX[i][0];
		}
		return mat;
	}
	
	public int GetResultMaxtrixRowNum()
	{
		return x.getRowDimension();
	}
}

