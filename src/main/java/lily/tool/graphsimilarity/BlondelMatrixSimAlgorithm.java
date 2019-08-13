/************************************************
 * Source code information
 * -----------------------
 * Original author	 Peng Wang, School of Computer Science & Eng., Southeast University
 * Author email      pwangseu@gmail.com
 * Web               http://ontomapping.googlepages.com
 * Created			 2007-4-26
 * Filename          BlondelMatrixSimAlgorithm.java
 * Version           2.0
 * 
 * Last modified on  2007-4-26
 *               by  Peng Wang
 * -----------------------
 * Functions describe:
 * Blondel文章中图中点相似的算法实现
 ***********************************************/
package lily.tool.graphsimilarity;

/*******************************************************************************
 * Class information -------------------
 * 
 * @author Peng Wang
 * @date 2007-4-26
 * 
 * describe: The algorithm is based on the work by Vincent D. Blondel, et al. A
 * paper describing the algorithm is "A Measure of Similarity Between Graph
 * Vertices: Applications to Synonym Extraction And Web Searching"
 ******************************************************************************/
import Jama.*;
import java.util.Set;

public class BlondelMatrixSimAlgorithm {
	public static Matrix A, At, B, Bt, Sk, Sk1;

	public double[][] solve(int nA, int nB, double[][] arrayA,
			double[][] arrayB, int iter_times) {
		int i, j;

		double[][] arrayS = new double[nB][nA];

		// Initinalize A
		A = new Matrix(arrayA);

		// compute At
		At = A.transpose();

		// initinalize B
		B = new Matrix(arrayB);

		// compute bt
		Bt = B.transpose();

		// initinalize S0
		for (i = 0; i < nB; i++)
			for (j = 0; j < nA; j++) {
				arrayS[i][j] = 1.0;
			}
		Sk = new Matrix(arrayS);

		// iterative
		for (i = 0; i < iter_times; i++) {
			Sk1 = ((B.times(Sk)).times(At)).plus((Bt.times(Sk)).times(A));
			Sk1 = Sk1.times(1 / (Sk1.normF()));
			Sk = Sk1;
		}
		// Sk1.print(12,3);

		// 这里结果不能归1化,归1之后子图之间的差别更看不出来了.
		arrayS = Sk.getArray();

		return arrayS;
	}

	public double[][] solve_block(int nA, int nB, double[][] arrayA,
			double[][] arrayB, Set sourceCPset, Set targetCPset, int iter_times) {
		int i, j;

		double[][] arrayS = new double[nB][nA];

		// Initinalize A
		A = new Matrix(arrayA);

		// compute At
		At = A.transpose();

		// initinalize B
		B = new Matrix(arrayB);

		// compute bt
		Bt = B.transpose();

		// initinalize S0
		for (i = 0; i < nB; i++)
			for (j = 0; j < nA; j++) {
				if (targetCPset.contains(i) && sourceCPset.contains(j)) {
					arrayS[i][j] = 1.0;
				} else if (!targetCPset.contains(i) && !sourceCPset.contains(j)) {
					arrayS[i][j] = 1.0;
				} else {
					arrayS[i][j] = 0.0;
				}

			}
		Sk = new Matrix(arrayS);

		Matrix Ska, Skb;

		// iterative
		for (i = 0; i < iter_times; i++) {
			Ska = new Matrix(arrayS);
			Skb = new Matrix(arrayS);
			Ska = B.times(Sk).times(At);
			Skb = Bt.times(Sk).times(A);
			Sk1 = Ska.plus(Skb);
			// Sk1= ((B.times(Sk)).times(At)).plus((Bt.times(Sk)).times(A));
			Sk1 = Sk1.times(1 / (Sk1.normF()));

			// turning Sk
			arrayS = Sk1.getArray();
			for (i = 0; i < nB; i++)
				for (j = 0; j < nA; j++) {
					if (targetCPset.contains(i) && !sourceCPset.contains(j)) {
						arrayS[i][j] = 0.0;
					} else if (!targetCPset.contains(i)
							&& sourceCPset.contains(j)) {
						arrayS[i][j] = 0.0;
					}
				}
			Sk1 = new Matrix(arrayS);
			Sk = Sk1;
		}
		// Sk1.print(12,3);
		return Sk1.getArray();
	}

	// GMO方法的实现
	public double[][] solve_GMO(int nExA, int nOtA, int nStA, double[][] AEs,
			double[][] As, double[][] AE, double[][] Aop, int nExB, int nOtB,
			int nStB, double[][] BEs, double[][] Bs, double[][] BE,
			double[][] Bop, double[][] EBA, int iter_times) {
		int i, j;

		// *****初始化A的各个分块矩阵******
		Matrix MAEs, MAs, MAst, MAEt, MAop, MAopt;

		MAEs = new Matrix(AEs);
		MAs = new Matrix(As);
		MAst = MAs.transpose();
		MAEt = new Matrix(AE);
		MAEt = MAEt.transpose();
		MAop = new Matrix(Aop);
		MAopt = MAop.transpose();

		// *****初始化B的各个分块矩阵******
		Matrix MBEst, MBs, MBst, MBE, MBop, MBopt;

		MBEst = new Matrix(BEs);
		MBEst = MBEst.transpose();
		MBs = new Matrix(Bs);
		MBst = MBs.transpose();
		MBE = new Matrix(BE);
		MBop = new Matrix(Bop);
		MBopt = MBop.transpose();

		// 初始化EBA
		Matrix MEBA;
		MEBA = new Matrix(EBA);

		// 初始化O1
		Matrix Ok, Ok1;
		double[][] arrayO = new double[nOtB][nOtA];
		for (i = 0; i < nOtB; i++)
			for (j = 0; j < nOtA; j++) {
				arrayO[i][j] = 1.0;
			}
		Ok = new Matrix(arrayO);
		Ok1 = new Matrix(arrayO);

		// 初始化S1
		Matrix Sk, Sk1;
		double[][] arrayS = new double[nStB][nStA];
		for (i = 0; i < nStB; i++)
			for (j = 0; j < nStA; j++) {
				arrayS[i][j] = 1.0;
			}
		Sk = new Matrix(arrayS);
		Sk1 = new Matrix(arrayS);

		System.out.println("开始迭代");

		// 迭代运算
		for (i = 0; i < 8; i++) {
			// 计算Ok1
			Ok1 = (MBs.times(Sk).times(MAst))
					.plus((MBopt.times(Sk).times(MAop)));
			Ok1 = Ok1.timesEquals(1 / (Ok1.normF()));

			// 计算Sk1
			Sk1 = (MBE.times(MEBA).times(MAEt)).plus(
					(MBEst.times(MEBA).times(MAEs))).plus(
					(MBop.times(Ok).times(MAopt))).plus(
					(MBst.times(Ok).times(MAs)));
			Sk1 = Sk1.timesEquals(1 / (Sk1.normF()));

			Ok = Ok1;
			Sk = Sk1;
			System.out.println(i + "次迭代完成");
		}
		System.out.println("迭代完成");

		arrayO = Ok.getArray();

		double max = 0;
		for (i = 0; i < nOtB; i++) {
			for (j = 0; j < nOtA; j++) {
				max = Math.max(max, arrayO[i][j]);
			}
		}

		for (i = 0; i < nOtB; i++) {
			for (j = 0; j < nOtA; j++) {
				arrayO[i][j] = arrayO[i][j] / max;
			}
		}

		// Ok1.print(12,3);
		return arrayO;
	}

	public double[][] GetSelectMatching(double[][] sim, int nB, int nA) {
		int i, j, k;
		int mNum = 0;
		int count = 0;
		double a = 2.0;
		double[][] simmatrix = new double[nB + 150][nA + 150];
		double[] row = new double[(nB + 15) * (nA + 15)];
		int[] PDataNum;
		double threshold;
		int[] match;
		boolean BREAK;
		double SIMILARITY_THRESHOLD = 1.0 / (double) (nA * nB * 10);

		for (i = 0; i < nB; i++)
			for (j = 0; j < nA; j++) {
				row[count] = sim[i][j];
				count++;
			}

		PDataNum = new int[count + 1];

		QuickSort(row, PDataNum, 0, count);
		// SelectSort(row,count);

		if (nA >= nB) {
			mNum = nA;
		} else {
			mNum = nB;
		}
		match = new int[mNum];

		for (i = 0; i < mNum; i++) {
			match[i] = -1;
		}

		k = 0;
		threshold = row[0] * SIMILARITY_THRESHOLD;
		while (row[k] > 0 && row[k] > threshold) {
			BREAK = false;
			for (i = 0; i < nB; i++) {
				if (BREAK)
					break;
				for (j = 0; j < nA; j++) {
					if ((sim[i][j] == row[k]) && (match[i] == -1)
							&& (match[j] == -1)) {
						match[i] = j;
						match[j] = i;
						// simmatrix[i][j]=1/(1+Math.exp(-1.0*a*row[k]/row[0]));
						simmatrix[i][j] = 1 / (1 + Math.exp(-1.0 * a * row[k]));
						simmatrix[j][i] = simmatrix[i][j];
						BREAK = true;
						break;
					}
				}
			}
			k++;
		}

		return simmatrix;
	}

	public double[][] GetSelectMatchingRaw(double[][] sim, int nB, int nA) {
		int i, j, k;
		int mNum = 0;
		int count = 0;
		double[][] simmatrix = new double[nB + 150][nA + 150];
		double[] row = new double[(nB + 15) * (nA + 15)];
		int[] PDataNum;
		double threshold;
		int[] match;
		boolean BREAK;
		double SIMILARITY_THRESHOLD = 1.0 / (double) (nA * nB * 10);

		for (i = 0; i < nB; i++)
			for (j = 0; j < nA; j++) {
				row[count] = sim[i][j];
				count++;
			}

		PDataNum = new int[count + 1];

		QuickSort(row, PDataNum, 0, count);
		// SelectSort(row,count);

		if (nA >= nB) {
			mNum = nA;
		} else {
			mNum = nB;
		}
		match = new int[mNum];

		for (i = 0; i < mNum; i++) {
			match[i] = -1;
		}

		k = 0;
		threshold = row[0] * SIMILARITY_THRESHOLD;
		while (row[k] > 0 && row[k] > threshold) {
			BREAK = false;
			for (i = 0; i < nB; i++) {
				if (BREAK)
					break;
				for (j = 0; j < nA; j++) {
					if ((sim[i][j] == row[k]) && (match[i] == -1)
							&& (match[j] == -1)) {
						match[i] = j;
						match[j] = i;
						// simmatrix[i][j]=1/(1+Math.exp(-1.0*a*row[k]/row[0]));
						simmatrix[i][j] = row[k];
						simmatrix[j][i] = simmatrix[i][j];
						BREAK = true;
						break;
					}
				}
			}
			k++;
		}

		return simmatrix;
	}

	public void GetSelectMatching2(double[][] sim, int nB, int nA) {
		int i, j, k;
		double value;
		for (i = 0; i < nB; i++) {
			for (j = 0; j < nA; j++) {
				for (k = 0; k < nB; k++) {
					if (k != i && sim[k][j] > sim[i][j]) {
						sim[i][j] = 0;
						break;
					}
				}
				for (k = 0; k < nA; k++) {
					if (k != j && sim[i][k] > sim[i][j]) {
						sim[i][j] = 0;
						break;
					}
				}
			}
		}
	}

	/***************************************************************************
	 * Quick sort function 
	 * from larger to smaller
	 **************************************************************************/
	public static void QuickSort(double[] pData, int[] pDataNum, int left,
			int right) {
		int i, j;
		int iTemp;
		double middle, strTemp;
		i = left;
		j = right;
		middle = pData[(left + right) / 2];
		do {
			while ((pData[i] > middle) && (i < right))
				i++;
			while ((pData[j] < middle) && (j > left))
				j--;
			if (i <= j) {
				strTemp = pData[i];
				pData[i] = pData[j];
				pData[j] = strTemp;

				iTemp = pDataNum[i];
				pDataNum[i] = pDataNum[j];
				pDataNum[j] = iTemp;

				i++;
				j--;
			}
		} while (i <= j);// 如果两边扫描的下标交错，就停止（完成一次）

		if (left < j)
			QuickSort(pData, pDataNum, left, j);

		if (right > i)
			QuickSort(pData, pDataNum, i, right);
	}

	public static void SelectSort(double[] pData, int Num) {
		int i, j;
		int itemp;
		double max;

		for (i = 0; i < Num - 1; i++) {
			max = pData[i];
			itemp = i;
			for (j = i + 1; j < Num; j++) {
				if (pData[j] > max) {
					itemp = j;
					max = pData[j];
				}
			}
			if (itemp != i) {
				pData[itemp] = pData[i];
				pData[i] = max;
			}
		}
	}
}
