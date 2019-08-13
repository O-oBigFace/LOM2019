/************************************************
 * Source code information
 * -----------------------
 * Original author	 Peng Wang, School of Computer Science & Eng., Southeast University
 * Author email      pwangseu@gmail.com
 * Web               http://ontomapping.googlepages.com
 * Created			 2007-6-23
 * Filename          SimDataVisual.java
 * Version           2.0
 * 
 * Last modified on  2007-6-23
 *               by  Peng Wang, Keyue Li
 * -----------------------
 * Functions describe:
 * 显示相似度数据的图像特征
 ***********************************************/

package lily.tool.threshold;

import javax.swing.*;
import java.awt.*;

/*******************************************************************************
 * Class information -------------------
 * 
 * @author Peng Wang
 * @date 2007-6-23
 * 
 * describe: 显示相似度数据的图像特征
 * 
 ******************************************************************************/
public class SimDataVisual extends JFrame {
	private static final long serialVersionUID = 1L;

	public double[][] matrix;

	private int mNum;

	private int nNum;

	public SimDataVisual() {
		this.setSize(650, 700); // 窗体的大小
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setResizable(false);// 窗体不能改变大小
		this.setTitle("similarity matrix"); // 设置标题
		JPanel contentPanel = new JPanel();
		contentPanel.setBackground(Color.white);
		contentPanel.setPreferredSize(new Dimension(650, 700));
		this.getContentPane().add(contentPanel, BorderLayout.CENTER);
	}

	public void paint(Graphics g) {
		int[] hg = new int[256];
		/* 绘制相似矩阵 */
		super.paint(g);
		int max=0;
		for (int i = 0; i < mNum; i++) {
			for (int j = 0; j < nNum; j++) {
				/* 映射相似度[0-1]到灰度[0-255] */
				double gray = matrix[i][j] * 255.0;
				if (gray > 255) {
					gray = 255;
				}
				hg[(int) gray]++;
				if (hg[(int) gray]>max) {max=hg[(int) gray];}
				/* 对应的颜色 */
				Color c = new Color((int) gray, (int) gray, (int) gray);
				// g.setColor(Color.getHSBColor(0, (float) 0.7,
				// (float)(1.0-matrix[i][j])));
				g.setColor(c);
				g.fillRect(20 + j * 6, 40 + i * 6, 6, 6);
			}
		}
		/* 绘制直方图 */
		/* 坐标 */
		g.setColor(Color.BLACK);
		g.drawLine(20, 640, 20 + 280, 640);
		g.drawLine(20, 640, 20, 640 - 150);
		for (int i = 1; i < 256; i++) {
			/* 直方图 */
			g.drawLine(20 + i, 640, 20 + i, (int)(640 - 50*(double)hg[i]/(double)max * 10));
		}
	}

	public void visualize(double[][] sim, int m, int n) {
		mNum = m;
		nNum = n;
		matrix = new double[m][n];
		matrix = sim.clone();
		for (int i = 0; i < m; i++) {
			if (matrix[i] != null) {
				matrix[i] = sim[i].clone();
			}
		}
		// SimDataVisual kyodaiUI = new SimDataVisual();
		// kyodaiUI.show();
		this.show();
	}
}
