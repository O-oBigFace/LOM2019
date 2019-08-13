/************************************************
 * Source code information
 * -----------------------
 * Original author	 Peng Wang, School of Computer Science & Eng., Southeast University
 * Author email      pwangseu@gmail.com
 * Web               http://ontomapping.googlepages.com
 * Created			 2007-6-22
 * Filename          DynamicThreshold.java
 * Version           2.0
 * 
 * Last modified on  2007-6-22
 *               by  Peng Wang
 * -----------------------
 * Functions describe:
 * 动态阀值的选择模块
 ***********************************************/
package lily.tool.threshold;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

/*******************************************************************************
 * Class information -------------------
 * 
 * @author Peng Wang
 * @date 2007-6-22
 * 
 * describe: 试验合适的阀值选择方法
 ******************************************************************************/
public class DynamicThreshold {
	private double lowSimThreshold = 0.001;// 基本阀值

	private double highThreshold = 0.65;// 整体相似度很高的阀值

	private double minInterval = 0.001;// 最小间隔

	/***************************************************************************
	 * 简单方法： 
	 * 根据问题的规模，简单估计阀值
	 **************************************************************************/
	public double naiveThreshold(double[][] simMx, int m, int n) {
		double td = 0;
		double maxSim = 0.0;
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				maxSim = Math.max(maxSim, simMx[i][j]);
			}
		}

		if (maxSim < lowSimThreshold) {
			/* 如果最大相似度达不到基本阀值 */
			td = lowSimThreshold;
		} else {
			/* 计算当前估计的阀值 */
			td = maxSim / Math.max((double) m, (double) n);
		}
		System.out.println("阀值：" + td);
		return td;
	}

	/***************************************************************************
	 * 计算分隔距离
	 **************************************************************************/
	public double getInterval(double[] simMx, int m) {
		double interval = 1.0;
		double[] mx = new double[m];
		mx = simMx.clone();
		Arrays.sort(mx);
		/* 间隔划分 */
		for (int i = 1; i < m; i++) {
			if (mx[i] > 0 && mx[i - 1] > 0) {
				if (Math.abs(mx[i] - mx[i - 1]) > minInterval) {
					interval = Math.min(Math.abs(mx[i] - mx[i - 1]), interval);
				}
			}
		}
//		System.out.println("iterval:" + interval);
		return interval;
	}

	/***************************************************************************
	 * 最大熵方法 
	 * 基于最后的选择结果
	 **************************************************************************/
	public double maxEntropyThreshold(double[] simMx, int m) {
		double interval = 0;
		double max = 0;
		double threshold = 0;
		int sNum = 0;// 区域数目
		int[] freqSum = new int[sNum];// 直到i的总频率
		double[] freqSim = new double[sNum];// i位置的频率对应的相似度
		int[] freqCur = new int[sNum];// i位置处的频率
		int count = 0;
		double ps = 0;
		double pi = 0;
		double Hs = 0;
		double Hn = 0;
		double Kapur = 0;// Kapur分割判定函数

		/* 获得间隔 */
		interval = getInterval(simMx, m);

		/* 计算区域总数 */
		sNum = (int) (1.0 / interval);
		if (1.0 / interval - sNum > 0) {
			sNum++;
		}
		/* 统计每个区域的相似度分布频率 */
		for (int i = 0; i < sNum; i++) {
			int countCur = 0;
			for (int j = 0; j < m; j++) {
				if (simMx[j] >= i * interval && simMx[j] < (i + 1) * interval) {
					count++;
					freqSim[i] = simMx[j];
					countCur++;
				}
			}
			freqCur[i] = countCur;
			freqSum[i] = count;
			System.out.println(freqSum[i]);
		}

		int split = 0;// 分割位置
		for (int i = 0; i < sNum; i++) {
			/* s=i */
			ps = (double) freqSum[i] / (double) m;
			if (ps > 0) {
				/* Hs */
				Hs = 0;
				for (int j = 0; j <= i; j++) {
					pi = (double) freqCur[j] / (double) m;
					if (pi > 0) {
						Hs += (-1.0) * pi * Math.log(pi);
					}
				}
				/* Hn */
				Hn = 0;
				for (int j = i + 1; j < sNum; j++) {
					pi = (double) freqCur[j] / (double) m;
					if (pi > 0) {
						Hn += (-1.0) * pi * Math.log(pi);
					}
				}
				Kapur = Math.log(ps * (1.0 - ps)) + Hs / ps + (Hn - Hs)
						/ (1 - ps);
				if (Kapur > max) {
					max = Kapur;
					split = i;
//					System.out.println("max=" + Kapur + "s=" + i);
				}
			}
		}

		threshold = (double) split * interval * 0.95;
		if (threshold < lowSimThreshold) {
			threshold = lowSimThreshold;
		}
		if (threshold > highThreshold) {
			threshold = highThreshold;
		}

//		System.out.println("分割点：" + split + "  分割位置：" + threshold);

		return threshold;
	}

	/***************************************************************************
	 * 最大熵方法 
	 * 基于最初的相似度矩阵
	 **************************************************************************/
	public double maxEntropyThreshold(double[][] simMx, int m, int n) {
		double[] sim;
		int dataNum = 0;
		double interval = 0;
		double max = 0;
		double threshold = 0;
		int sNum = 0;// 区域数目
		int[] freqSum;// 直到i的总频率
		double[] freqSim;// i位置的频率对应的相似度
		int[] freqCur;// i位置处的频率
		int count = 0;
		double ps = 0;
		double pi = 0;
		double Hs = 0;
		double Hn = 0;
		double Kapur = 0;// Kapur分割判定函数

		/* 初始化 */

		/* 变二维矩阵为一维,不考虑为0的位置 */
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				if (simMx[i][j] > 0) {
					count++;
				}
			}
		}
		dataNum = count;
		sim = new double[dataNum];
		count = 0;
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				if (simMx[i][j] > 0) {
					sim[count] = simMx[i][j];
					count++;
				}
			}
		}
		count = 0;

		/* 获得间隔 */
		interval = getInterval(sim, dataNum);

		/* 计算区域总数 */
		sNum = (int) (1.0 / interval);
		if (1.0 / interval - sNum > 0) {
			sNum++;
		}
		freqSum = new int[sNum];// 直到i的总频率
		freqSim = new double[sNum];// i位置的频率对应的相似度
		freqCur = new int[sNum];// i位置处的频率
		/* 统计每个区域的相似度分布频率 */
		for (int i = 0; i < sNum; i++) {
			int countCur = 0;
			for (int j = 0; j < dataNum; j++) {
				if (sim[j] >= i * interval && sim[j] < (i + 1) * interval) {
					count++;
					freqSim[i] = sim[j];
					countCur++;
				}
			}
			freqCur[i] = countCur;
			freqSum[i] = count;
			// System.out.println(freqSum[i]);
		}

		int split = 0;// 分割位置
		for (int i = 0; i < sNum; i++) {
			/* s=i */
			ps = (double) freqSum[i] / (double) dataNum;
			if (ps > 0) {
				/* Hs */
				Hs = 0;
				for (int j = 0; j <= i; j++) {
					pi = (double) freqCur[j] / (double) dataNum;
					if (pi > 0) {
						Hs += (-1.0) * pi * Math.log(pi);
					}
				}
				/* Hn */
				Hn = 0;
				for (int j = i + 1; j < sNum; j++) {
					pi = (double) freqCur[j] / (double) dataNum;
					if (pi > 0) {
						Hn += (-1.0) * pi * Math.log(pi);
					}
				}
				Kapur = Math.log(ps * (1.0 - ps)) + Hs / ps + (Hn - Hs)
						/ (1 - ps);
				if (Kapur > max) {
					max = Kapur;
					split = i;
//					System.out.println("max=" + Kapur + "s=" + i);
				}
			}
		}

		threshold = (double) split * interval * 0.95;
		if (threshold < lowSimThreshold) {
			threshold = lowSimThreshold;
		}
		if (threshold > highThreshold) {
			threshold = highThreshold;
		}

//		System.out.println("分割点：" + split + "  分割位置：" + threshold);

		return threshold;
	}

	/***************************************************************************
	 * 最大熵方法，变种A 
	 * 基于过虑后的相似度矩阵
	 **************************************************************************/
	public double maxEntropyThresholdA(double[][] simMx, int m, int n) {
		double[] sim;
		int dataNum = 0;
		double interval = 0;
		double max = 0;
		double min = 1.1;
		double threshold = 0;
		int sNum = 0;// 区域数目
		int[] freqSum;// 直到i的总频率
		double[] freqSim;// i位置的频率对应的相似度
		int[] freqCur;// i位置处的频率
		int count = 0;
		double ps = 0;
		double pi = 0;
		double Hs = 0;
		double Hn = 0;
		double Kapur = 0;// Kapur分割判定函数

		/* 初始化 */

		/* 变二维矩阵为一维 */
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				if (simMx[i][j] > 1.0) {
					simMx[i][j] = 1.0;
				}
				if (simMx[i][j] > 0) {
					dataNum++;
					min = Math.min(min, simMx[i][j]);
					break;
				}
			}
		}

		/* 整体相似度是否很高 */
		if (min > highThreshold) {
			threshold = highThreshold;
			return threshold;
		}

		sim = new double[dataNum];
		count = 0;
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				if (simMx[i][j] > 0) {
					sim[count] = simMx[i][j];
					count++;
					break;
				}
			}
		}
		count = 0;

		/* 获得间隔 */
		interval = getInterval(sim, dataNum);

		/* 计算区域总数 */
		sNum = (int) (1.0 / interval);
		if (1.0 / interval - sNum > 0) {
			sNum++;
		}
		freqSum = new int[sNum];// 直到i的总频率
		freqSim = new double[sNum];// i位置的频率对应的相似度
		freqCur = new int[sNum];// i位置处的频率
		/* 统计每个区域的相似度分布频率 */
		for (int i = 0; i < sNum; i++) {
			int countCur = 0;
			for (int j = 0; j < dataNum; j++) {
				if (sim[j] >= i * interval && sim[j] < (i + 1) * interval) {
					count++;
					freqSim[i] = sim[j];
					countCur++;
				}
			}
			freqCur[i] = countCur;
			freqSum[i] = count;
			// System.out.println(freqSum[i]);
		}

		int split = 0;// 分割位置
		for (int i = 0; i < sNum; i++) {
			/* s=i */
			ps = (double) freqSum[i] / (double) dataNum;
			if (ps > 0) {
				/* Hs */
				Hs = 0;
				for (int j = 0; j <= i; j++) {
					pi = (double) freqCur[j] / (double) dataNum;
					if (pi > 0) {
						Hs += (-1.0) * pi * Math.log(pi);
					}
				}
				/* Hn */
				Hn = 0;
				for (int j = i + 1; j < sNum; j++) {
					pi = (double) freqCur[j] / (double) dataNum;
					if (pi > 0) {
						Hn += (-1.0) * pi * Math.log(pi);
					}
				}
				Kapur = Math.log(ps * (1.0 - ps)) + Hs / ps + (Hn - Hs)
						/ (1 - ps);
				if (Kapur > max) {
					max = Kapur;
					split = i;
//					System.out.println("max=" + Kapur + "s=" + i);
				}
			}
		}

		threshold = (double) split * interval * 0.95;
		if (threshold < lowSimThreshold) {
			threshold = lowSimThreshold;
		}
		if (threshold > highThreshold) {
			threshold = highThreshold;
		}

//		System.out.println("分割点：" + split + "  分割位置：" + threshold);

		return threshold;
	}

	/***************************************************************************
	 * Ostu方法 
	 * 基于过虑后的相似度矩阵
	 **************************************************************************/
	public double ostuThreshold(double[][] simMx, int m, int n) {
		double[] sim;
		int dataNum = 0;
		double interval = 0;
		double max = 0;
		double min = 1.1;
		double threshold = 0;
		int sNum = 0;// 区域数目
		int[] freqSum;// 直到i的总频率
		double[] freqSim;// i位置的频率对应的相似度
		int[] freqCur;// i位置处的频率
		int count = 0;
		double pi = 0;
		double omega0, omega1;
		double sigmaB;
		double mju0, mju1, mjut, mjuT;

		/* 初始化 */

		/* 变二维矩阵为一维 */
		/* 先统计大于0的相似度数目 */
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				if (simMx[i][j] > 1.0) {
					simMx[i][j] = 1.0;
				}
				if (simMx[i][j] > 0) {
					dataNum++;
					min = Math.min(min, simMx[i][j]);
					break;
				}
			}
		}

		/* 整体相似度是否很高 */
		if (min > highThreshold) {
			threshold = highThreshold;
			return threshold;
		}
		/* 变为一维 */
		sim = new double[dataNum];
		count = 0;
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				if (simMx[i][j] > 0) {
					sim[count] = simMx[i][j];
					count++;
					break;
				}
			}
		}
		count = 0;

		/* 获得间隔 */
		interval = getInterval(sim, dataNum);

		/* 计算区域总数 */
		sNum = (int) (1.0 / interval);
		if (1.0 / interval - sNum > 0) {
			sNum++;
		}
		freqSum = new int[sNum];// 直到i的总频率
		freqSim = new double[sNum];// i位置的频率对应的相似度
		freqCur = new int[sNum];// i位置处的频率
		/* 统计每个区域的相似度分布频率 */
		for (int i = 0; i < sNum; i++) {
			int countCur = 0;
			for (int j = 0; j < dataNum; j++) {
				if (sim[j] >= i * interval && sim[j] < (i + 1) * interval) {
					count++;
					freqSim[i] = sim[j];
					countCur++;
				}
			}
			freqCur[i] = countCur;
			freqSum[i] = count;
		}

		/* Ostu方法 */
		/* 计算mjuT */
		mjuT = 0;
		for (int j = 0; j < sNum; j++) {
			pi = (double) freqCur[j] / (double) dataNum;
			mjuT += (double) j * pi;
		}
		int split = 0;// 分割位置
		for (int i = 0; i < sNum; i++) {
			/* split=i */

			/* 计算omega0 */
			omega0 = 0;
			for (int j = 0; j <= i; j++) {
				pi = (double) freqCur[j] / (double) dataNum;
				omega0 += pi;
			}
			/* 计算omega1 */
			omega1 = 0;
			omega1 = 1 - omega0;
			/* 计算mju0 */
			mju0 = 0;
			mjut = 0;
			for (int j = 0; j <= i; j++) {
				pi = (double) freqCur[j] / (double) dataNum;
				mjut += (double) j * pi;
			}
			mju0 = mjut / omega0;
			/* 计算mju1 */
			mju1 = 0;
			mju1 = (mjuT - mjut) / omega1;
			/* 判定标准SigmaB */
			sigmaB = omega0 * omega1 * (mju0 - mju1) * (mju0 - mju1);

			if (sigmaB > max) {
				max = sigmaB;
				split = i;
//				System.out.println("max=" + sigmaB + "s=" + i);
			}
		}

		threshold = (double) split * interval * 0.95;
		if (threshold < lowSimThreshold) {
			threshold = lowSimThreshold;
		}
		if (threshold > highThreshold) {
			threshold = highThreshold;
		}

		System.out.println("分割点：" + split + "  分割位置：" + threshold);

		return threshold;
	}

	/***************************************************************************
	 * Minimum error方法 
	 * 基于过虑后的相似度矩阵
	 **************************************************************************/
	public double miniErrorThreshold(double[][] simMx, int m, int n) {
		double[] sim;
		int dataNum = 0;
		double interval = 0;
		double min = 99999999;
		double threshold = 0;
		int sNum = 0;// 区域数目
		int[] freqSum;// 直到i的总频率
		double[] freqSim;// i位置的频率对应的相似度
		int[] freqCur;// i位置处的频率
		int count = 0;
		double p0, p1;
		double sigma0, sigma1;
		double mju0, mju1;
		double Kittler = 0;

		/* 变二维矩阵为一维 */
		/* 先统计大于0的相似度数目 */
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				if (simMx[i][j] > 1.0) {
					simMx[i][j] = 1.0;
				}
				if (simMx[i][j] > 0) {
					dataNum++;
					min = Math.min(min, simMx[i][j]);
					break;
				}
			}
		}

		/* 整体相似度是否很高 */
		if (min > highThreshold) {
			threshold = highThreshold;
			return threshold;
		}
		/* 变为一维 */
		sim = new double[dataNum];
		count = 0;
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				if (simMx[i][j] > 0) {
					sim[count] = simMx[i][j];
					count++;
					break;
				}
			}
		}
		count = 0;

		/* 获得间隔 */
		interval = getInterval(sim, dataNum);

		/* 计算区域总数 */
		sNum = (int) (1.0 / interval);
		if (1.0 / interval - sNum > 0) {
			sNum++;
		}
		freqSum = new int[sNum];// 直到i的总频率
		freqSim = new double[sNum];// i位置的频率对应的相似度
		freqCur = new int[sNum];// i位置处的频率
		/* 统计每个区域的相似度分布频率 */
		for (int i = 0; i < sNum; i++) {
			int countCur = 0;
			for (int j = 0; j < dataNum; j++) {
				if (sim[j] >= i * interval && sim[j] < (i + 1) * interval) {
					count++;
					freqSim[i] = sim[j];
					countCur++;
				}
			}
			freqCur[i] = countCur;
			freqSum[i] = count;
		}

		/* Minimum error方法 */
		min = 99999999;
		int split = 0;// 分割位置
		for (int i = 0; i < sNum; i++) {
			/* split=i */
			/* 计算P0 */
			p0 = 0;
			for (int j = 0; j <= i; j++) {
				p0 = p0 + (double) freqCur[j];
			}
			/* 计算P1 */
			p1 = 0;
			for (int j = i + 1; j < sNum; j++) {
				p1 = p1 + (double) freqCur[j];
			}
			if (Math.abs(p0 * p1 - 0.0) < 0.0001) {// 避免除数为0
				continue;
			}
			/* 计算mju0 */
			mju0 = 0;
			for (int j = 0; j <= i; j++) {
				mju0 = mju0 + (double) freqCur[j] * (double) j;
			}
			mju0 = mju0 / p0;
			/* 计算mju1 */
			mju1 = 0;
			for (int j = i + 1; j < sNum; j++) {
				mju1 = mju1 + (double) freqCur[j] * (double) j;
			}
			mju1 = mju1 / p1;
			/* 计算sigma0 */
			sigma0 = 0;
			for (int j = 0; j <= i; j++) {
				sigma0 = sigma0 + (double) freqCur[j] * ((double) j - mju0)
						* ((double) j - mju0);
			}
			sigma0 = sigma0 / p0;
			/* 计算sigma1 */
			sigma1 = 0;
			for (int j = i + 1; j < sNum; j++) {
				sigma1 = sigma1 + (double) freqCur[j] * ((double) j - mju1)
						* ((double) j - mju1);
			}
			sigma1 = sigma1 / p1;

			if (Math.abs(sigma0 * sigma1 - 0.0) < 0.0001) {// 避免除数为0
				continue;
			}

			/* 判定标准Kittler */
			Kittler = 1.0 + 2.0
					* (p0 * Math.log(sigma0) + p1 * Math.log(sigma1)) - 2.0
					* (p0 * Math.log(p0) + p1 * Math.log(p1));
			if (Kittler < min) {
				min = Kittler;
				split = i;
				System.out.println("min=" + Kittler + "s=" + i);
			}
		}

		threshold = (double) split * interval * 0.95;
		if (threshold < lowSimThreshold) {
			threshold = lowSimThreshold;
		}
		if (threshold > highThreshold) {
			threshold = highThreshold;
		}

		System.out.println("分割点：" + split + "  分割位置：" + threshold);

		return threshold;
	}

	/***************************************************************************
	 * max correlation方法 
	 * 基于过虑后的相似度矩阵
	 **************************************************************************/
	public double maxCorrelationThreshold(double[][] simMx, int m, int n) {
		double[] sim;
		int dataNum = 0;
		double interval = 0;
		double max = 0;
		double min = 99999999;
		double threshold = 0;
		int sNum = 0;// 区域数目
		int[] freqSum;// 直到i的总频率
		double[] freqSim;// i位置的频率对应的相似度
		int[] freqCur;// i位置处的频率
		int count = 0;
		double gO, gB;
		double pi, pt;
		double tc = 0;

		/* 变二维矩阵为一维 */
		/* 先统计大于0的相似度数目 */
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				if (simMx[i][j] > 1.0) {
					simMx[i][j] = 1.0;
				}
				if (simMx[i][j] > 0) {
					dataNum++;
					min = Math.min(min, simMx[i][j]);
					break;
				}
			}
		}

		/* 整体相似度是否很高 */
		if (min > highThreshold) {
			threshold = highThreshold;
			return threshold;
		}
		/* 变为一维 */
		sim = new double[dataNum];
		count = 0;
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				if (simMx[i][j] > 0) {
					sim[count] = simMx[i][j];
					count++;
					break;
				}
			}
		}
		count = 0;

		/* 获得间隔 */
		interval = getInterval(sim, dataNum);

		/* 计算区域总数 */
		sNum = (int) (1.0 / interval);
		if (1.0 / interval - sNum > 0) {
			sNum++;
		}
		freqSum = new int[sNum];// 直到i的总频率
		freqSim = new double[sNum];// i位置的频率对应的相似度
		freqCur = new int[sNum];// i位置处的频率
		/* 统计每个区域的相似度分布频率 */
		for (int i = 0; i < sNum; i++) {
			int countCur = 0;
			for (int j = 0; j < dataNum; j++) {
				if (sim[j] >= i * interval && sim[j] < (i + 1) * interval) {
					count++;
					freqSim[i] = sim[j];
					countCur++;
				}
			}
			freqCur[i] = countCur;
			freqSum[i] = count;
		}

		/* Max correlation方法 */
		int split = 0;// 分割位置
		for (int i = 0; i < sNum; i++) {
			/* split=i */
			/* 计算go */
			gO = 0;
			for (int j = 0; j <= i; j++) {
				pi = (double) freqCur[j] / (double) dataNum;
				gO = gO + pi * pi;
			}
			/* 计算gB */
			gB = 0;
			for (int j = i + 1; j < sNum; j++) {
				pi = (double) freqCur[j] / (double) dataNum;
				gB = gB + pi * pi;
			}
			/* 计算pt */
			pt = (double) freqSum[i] / (double) dataNum;
			if ((Math.abs(gO * gB) < 0.0001)
					|| (Math.abs(pt * (1.0 - pt)) < 0.0001)) {
				continue;
			}
			/* 判定标准TC */
			tc = -Math.log(gO * gB) + 2.0 * Math.log(pt * (1.0 - pt));

			if (tc > max) {
				max = tc;
				split = i;
//				System.out.println("max=" + tc + "s=" + i);
			}
		}

		threshold = (double) split * interval * 0.95;
		if (threshold < lowSimThreshold) {
			threshold = lowSimThreshold;
		}
		if (threshold > highThreshold) {
			threshold = highThreshold;
		}

		System.out.println("分割点：" + split + "  分割位置：" + threshold);

		return threshold;
	}

	/***************************************************************************
	 * 自己的方法
	 **************************************************************************/
	public double maxWPThreshold(double[][] simMx, int m, int n) {
		double[] sim;
		int dataNum = 0;
		double interval = 0;
		double max = 0;
		double min = 99999999;
		double threshold = 0;
		int sNum = 0;// 区域数目
		int[] freqSum;// 直到i的总频率
		double[] freqSim;// i位置的频率对应的相似度
		int[] freqCur;// i位置处的频率
		int count = 0;
		double pi, pt;
		double wp = 0;

		/* 变二维矩阵为一维 */
		/* 先统计大于0的相似度数目 */
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				if (simMx[i][j] > 1.0) {
					simMx[i][j] = 1.0;
				}
				if (simMx[i][j] > 0) {
					dataNum++;
					min = Math.min(min, simMx[i][j]);
					break;
				}
			}
		}

		/* 整体相似度是否很高 */
		if (min > highThreshold) {
			threshold = highThreshold;
			return threshold;
		}
		/* 变为一维 */
		sim = new double[dataNum];
		count = 0;
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				if (simMx[i][j] > 0) {
					sim[count] = simMx[i][j];
					count++;
					break;
				}
			}
		}
		count = 0;

		/* 获得间隔 */
		interval = getInterval(sim, dataNum);

		/* 计算区域总数 */
		sNum = (int) (1.0 / interval);
		if (1.0 / interval - sNum > 0) {
			sNum++;
		}
		freqSum = new int[sNum];// 直到i的总频率
		freqSim = new double[sNum];// i位置的频率对应的相似度
		freqCur = new int[sNum];// i位置处的频率
		/* 统计每个区域的相似度分布频率 */
		for (int i = 0; i < sNum; i++) {
			int countCur = 0;
			for (int j = 0; j < dataNum; j++) {
				if (sim[j] >= i * interval && sim[j] < (i + 1) * interval) {
					count++;
					freqSim[i] = sim[j];
					countCur++;
				}
			}
			freqCur[i] = countCur;
			freqSum[i] = count;
		}

		/* 测试的方法 */
		int split = 0;// 分割位置
		for (int i = 0; i < sNum; i++) {
			/* 当前分割值 */
			double t = (double) i * interval;
			/* 拷贝原始矩阵 */
			double[][] matrix = new double[m][n];
			matrix = simMx.clone();
			for (int j = 0; j < m; j++) {
				if (matrix[j] != null) {
					matrix[j] = simMx[j].clone();
				}
			}
			/* t把矩阵切除一部分 */
			// for (int k=0;k<m;k++){
			// for (int l=0;l<n;l++){
			// if (matrix[k][l]<=t){
			// matrix[k][l]=0;
			// }
			// }
			// }
			/* 求行，列的均值 */
			double[] avgrow = new double[m];
			double[] avgcol = new double[n];
			for (int k = 0; k < m; k++) {
				avgrow[k] = 0;
				for (int j = 0; j < n; j++) {
					avgrow[k] = avgrow[k] + matrix[k][j];
				}
				// avgrow[i]=avgrow[i]/(double)n;
			}
			for (int k = 0; k < n; k++) {
				avgcol[k] = 0;
				for (int j = 0; j < m; j++) {
					avgcol[k] = avgcol[k] + matrix[j][k];
				}
				// avgcol[i]=avgcol[i]/(double)m;
			}

			/* 计算当前矩阵条件下选择出来的positive位置 */
			ArrayList psList = new ArrayList();
			psList = this.positiveFinder(matrix, m, n);

			/* 计算大于0的位置的优势程度 */
			double[][] predomain = new double[m][n];
			for (int k = 0; k < m; k++) {
				for (int j = 0; j < n; j++) {
					if (matrix[k][j] > 0) {
						double havg = (avgrow[k] + avgcol[j] - matrix[k][j])
								/ (double) (m + n - 1);
						predomain[k][j] = havg * (matrix[k][j] - havg);
						predomain[k][j] = matrix[k][j] * predomain[k][j];
					}
				}
			}

			/* 判定标准WP */
			double positive1 = 0;
			double negative1 = 0;
			double positive2 = 0;
			double negative2 = 0;
			for (int k = 0; k < m; k++) {
				for (int j = 0; j < n; j++) {
					if (matrix[k][j] > t) {
						/* 求positive */
						boolean hasPosi = false;
						for (Iterator ix = psList.iterator(); ix.hasNext();) {
							int rx = ((Integer) ix.next()).intValue();
							int cx = ((Integer) ix.next()).intValue();
							if (rx == k && cx == j) {
								positive1 += predomain[k][j];
								hasPosi = true;
								break;
							}
						}
						/* 求negtive */
						if (!hasPosi) {
							negative1 += predomain[k][j];
						}
					} else {
						/* 求positive */
						boolean hasPosi = false;
						for (Iterator ix = psList.iterator(); ix.hasNext();) {
							int rx = ((Integer) ix.next()).intValue();
							int cx = ((Integer) ix.next()).intValue();
							if (rx == k && cx == j) {
								positive2 += predomain[k][j];
								hasPosi = true;
								break;
							}
						}
						/* 求negtive */
						if (!hasPosi) {
							negative2 += predomain[k][j];
						}
					}
				}
			}
			wp = positive1 - positive2 - negative1;
			if (wp > max) {
				max = wp;
				split = i;
//				System.out.println("max=" + wp + "s=" + i);
			}
		}

		threshold = (double) split * interval * 0.95;
		if (threshold < lowSimThreshold) {
			threshold = lowSimThreshold;
		}
		if (threshold > highThreshold) {
			threshold = highThreshold;
		}

		System.out.println("分割点：" + split + "  分割位置：" + threshold);

		return threshold;
	}

	private ArrayList positiveFinder(double[][] sim, int m, int n) {
		int i, j, k;
		int maxNum = 0;
		int count = 0;
		double[][] simFilter = new double[m][n];
		double[] row = new double[m * n];
		int[] matchA, matchB;
		boolean BREAK;

		ArrayList result = new ArrayList();

		// 结果放入一个数组中
		for (i = 0; i < m; i++)
			for (j = 0; j < n; j++) {
				row[count] = sim[i][j];
				count++;
			}

		Arrays.sort(row);// 排序
		maxNum = Math.max(m, n);
		matchA = new int[maxNum];
		matchB = new int[maxNum];
		for (i = 0; i < maxNum; i++) {
			matchA[i] = -1;
			matchB[i] = -1;
		}

		k = m * n - 1;
		while (k >= 0 && row[k] > 0 && row[k] > 0.0) {
			BREAK = false;
			for (i = 0; i < m; i++) {
				if (BREAK)
					break;
				for (j = 0; j < n; j++) {
					if ((sim[i][j] == row[k]) && (matchA[i] == -1)
							&& (matchB[j] == -1)) {
						matchA[i] = j;
						matchB[j] = i;
						simFilter[i][j] = row[k];

						result.add(i);
						result.add(j);

						BREAK = true;
						break;
					}
				}
			}
			k--;
		}
		return result;
	}
}
