package lily.tool.linearequation;

import org.jblas.DoubleMatrix;
import org.jblas.Solve;

public class JBlasWrapper {

    // 这个是Peng Wang以前写的
    public static double[] JBlasSolver(int N, int star, int end, double[][] CMat,
                                       double starVolt, double endVolt) {
        // 计算指定概念的基本子图
        LinearEquation Equation = new LinearEquation();
        // the last node is sink node, it is "sourceVertexNum+1"
        Equation.InitlizePara(N, star, end);
        // 设定电导矩阵
        Equation.SetConductMatrix(CMat);
        // 指定初始电压
        Equation.PrepareMatrixA_b(starVolt, endVolt);
        // 解线性方程组
        Equation.Solve();
        // 得到各个点上电压
        return Equation.GetResultMaxtrix();
    }

    // JBClassSover的重写，使得计算效率要高一些，但是在有些机器上可能运行会产生一点错误提示有些包没有安装。
    public static double[] JBlasSolver0(int N, int star, int end, double[][] CMat,
                                       double starVolt, double endVolt) {
        // 1.参数初始化
        DoubleMatrix A = new DoubleMatrix(N, N);
        DoubleMatrix b = new DoubleMatrix(N);
        double[] SumCdtX = new double[N];
        // 2.设置初始电压
        //Compute Sum conductance at each vertex
        for (int i = 0; i < N; i++) {
            SumCdtX[i] = 0;
            for (int j = 0; j < N; j++) {
                SumCdtX[i] += CMat[i][j];
            }
        }
        //Compute final coefficient used in equations
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (i == star) {
                    if (j == star) {
                        A.put(i, j, 1.0);
                    } else {
                        A.put(i, j, 0);
                    }

                } else if (i == end) {
                    if (j == end) {
                        A.put(i, j, 1.0);
                    } else {
                        A.put(i, j, 0);
                    }
                } else {
                    if (i == j) {
                        A.put(i, j, -1.0);
                    } else {
                        A.put(i, j, CMat[i][j] / SumCdtX[i]);
                    }
                }
            }
        }

        //construct the b of linear equation
        for (int i = 0; i < N; i++) {
            if (i == star) {
                b.put(i, starVolt);
            } else {
                b.put(i, endVolt);
            }
        }

        return Solve.solve(A, b).toArray();
    }

}
