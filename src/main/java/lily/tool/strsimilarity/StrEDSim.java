/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Peng Wang
 * Author email       PWangSeu@gmail.com
 * Package            Jena 2.4
 * Web                http://sourceforge.net/projects/jena/
 * Created            12-Aug-2006
 * Filename           $RCSfile: OWLClassParse.java.html,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2006/9/13 09:38:31 $
 *               by   $Author: Peng Wang $
 *
 * (c) Copyright 2006 CS Department, Southeast University
 *****************************************************************************/

package lily.tool.strsimilarity;

import lily.tool.editdistance.*;

/***************************************************************************
 * Edit distance算法
 **************************************************************************/
public class StrEDSim {

	
	// The basic function for edit distance
	public double getBasicEDSim(String source, String target) {
		return (new EditDistance().getLevenshteinDistance (source,target));
	}

	/*******************************************************************
	 * I adapt the method proposed by LI Yu-jian. In the paper [Normalized 
	 * Distance Metrics Between Symbolic Sequences, Journal of Beijing 
	 * University of Technology, 2005, 31(4)], Liu present a formula, which 
	 * can assure that the distance value is between [0,1].
	 * 
     * The formula is defined as follows:
     * DE(x,y) denotes the edit distance between x and y;
     * SE(x,y)=[|x|+|y|-DE(x,y)]/2 denotes the edit similarity between x and y; 
     * The Normalized Edit Distance DNE(x,y) is:
     * DNE(x,y)=DE(x,y)/[DE(x,y)+SE(x,y)]=[|x|+|y|-2*SE(x,y)]/[|x|+|y|-SE(x,y)].
     * when x and y are empty strings, DNE(x,y)=0. 
	 */	
	public double getNormEDSim(String source, String target){
		int de, s_len, t_len;
		double se, dne;
		s_len = source.length();
		t_len = target.length();
		
		if (!(s_len==0 && t_len == 0)){
			de = new EditDistance().getLevenshteinDistance(source, target);
			se = (double)(s_len + t_len - de)/2.0;
			dne = (double)de /(de + se);
			return 1.0-dne;
		}
		else
		{
			return 1.0;
		}

	}

}
