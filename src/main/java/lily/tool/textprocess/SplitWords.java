/************************************************
 * Source code information
 * -----------------------
 * Original author	 Peng Wang, School of Computer Science & Eng., Southeast University
 * Author email      pwangseu@gmail.com
 * Web               http://ontomapping.googlepages.com
 * Created			 2007-5-13
 * Filename          SplitWords.java
 * Version           2.0
 *
 * Last modified on  2007-5-13
 *               by  Peng Wang
 * -----------------------
 * Functions describe:
 * 分词
 ***********************************************/
package lily.tool.textprocess;
import java.util.*;
import java.lang.reflect.Method;
import lily.tool.snowball.*;

/*********************
 * Class information
 * -------------------
 * @author Peng Wang
 * @date   2007-5-13
 *
 * describe:
 * 分词和词干提取程序，采用Peter Cheng的程序
 * 原文：http://blog.csdn.net/petercheng456/archive/2005/07/19/429056.aspx
 *
 ********************/
public class SplitWords {
    /* 分隔符的集合 */
    private final String delimiters = " \t\n\r\f~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./'1234567890";

    /* 语言 */
    private final String language = "english";

    public ArrayList split(String source) {
        //source = source.toLowerCase();
    	/* 提取数字 */
        Vector vectorForNumber = new Vector();
        flag3: for (int i = 0; i < source.length(); i++) {
            char thisChar = source.charAt(i);
            StringBuffer thisNumber = new StringBuffer();
            boolean hasDigit = false;
            if (Character.isDigit(thisChar)) {
                thisNumber.append(thisChar);
                for (++i; i < source.length(); i++) {
                    thisChar = source.charAt(i);
                    if ((thisChar == '.') && !hasDigit) {
                        thisNumber.append(thisChar);
                        hasDigit = true;
                    } else if (Character.isDigit(thisChar)) {
                        thisNumber.append(thisChar);
                    } else {
                        if (thisNumber.length() != 0) {
                            vectorForNumber.addElement(thisNumber.toString());
                            continue flag3;
                        }
                    }
                }
                if (thisNumber.length() != 0) {
                    vectorForNumber.addElement(thisNumber.toString());
                }
            }
        }

        /* 剔除. */
        int positionOfDot;
        StringBuffer tempSource = new StringBuffer(source);
        while ((positionOfDot = tempSource.indexOf(".")) != -1) {
            tempSource.deleteCharAt(positionOfDot);
        }
        source = tempSource.toString();

        /* 根据分隔符分词 */
        StringTokenizer stringTokenizer = new StringTokenizer(source,
                delimiters);

        /* 所有的词 */
        Vector vector = new Vector();

        /* 全大写的词 -- 不用提词干所以单独处理 */
        Vector vectorForAllUpperCase = new Vector();

        /* 根据大写字母分词 */
        flag0: while (stringTokenizer.hasMoreTokens()) {
            String token = stringTokenizer.nextToken();

            /* 全大写的词单独处理 */
            boolean allUpperCase = true;
            for (int i = 0; i < token.length(); i++) {
                if (!Character.isUpperCase(token.charAt(i))) {
                    allUpperCase = false;
                }
            }
            if (allUpperCase) {
                vectorForAllUpperCase.addElement(token.toLowerCase());
                continue flag0;
            }

            /* 非全大写的词 */
            int index = 0;
            boolean lowerFirst;
            flag1: while (index < token.length()) {
                lowerFirst = Character.isLowerCase(token.charAt(index));
                flag2: while (true) {
                    if (++index >= token.length())
                        break flag2;
                    else if (lowerFirst != Character.isLowerCase(token.charAt(index))) {
                        if (index == 1) { //只有一个字符，通常不可能
                            lowerFirst = !lowerFirst;
                        } else {
                            break flag2;
                        }
                    }
                }
                vector.addElement(token.substring(0, index).toLowerCase());
                token = token.substring(index);
                index = 0;
                continue flag1;
            }
        }

        /* 提词干 */
        try {
            Class stemClass = Class.forName("lily.tool.snowball.ext."
                    + language + "Stemmer");
            SnowballProgram stemmer = (SnowballProgram) stemClass.newInstance();
            Method stemMethod = stemClass.getMethod("stem");
            Object[] emptyArgs = new Object[0];
            for (int i = 0; i < vector.size(); i++) {
                stemmer.setCurrent((String) vector.elementAt(i));
                stemMethod.invoke(stemmer, emptyArgs);
                vector.setElementAt(stemmer.getCurrent(), i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        /* 合并 */
        for (int i = 0; i < vectorForAllUpperCase.size(); i++) {
            vector.addElement(vectorForAllUpperCase.elementAt(i));
        }
        for (int i = 0; i < vectorForNumber.size(); i++) {
            vector.addElement(vectorForNumber.elementAt(i));
        }

        // /* 转为数组形式 */
        // String[] array = new String[vector.size()];
        // Enumeration enumeration = vector.elements();
        // int index = 0;
        // while (enumeration.hasMoreElements()) {
        // array[index] = (String) enumeration.nextElement();
        // index++;
        // }
        //
        // /* 打印显示 */
        // for (int i = 0; i < array.length; i++) {
        // System.out.print(array[i] + " ");
        //        }

        /*转化为ArrayList形式*/
        ArrayList wordList=new ArrayList();
        for(Enumeration en=vector.elements();en.hasMoreElements();){
            wordList.add(((String) en.nextElement()).toLowerCase());
        }

        /* 返回 */
        return wordList;
    }

    public static void main(String args[]) {
        String source="don't. can't ve These ours you're yours yourself, you've you've yourselves you've ourselves outside 232 tables are wordStemming for ARE-Company using only. The I.S.B.N number of J.Smith's book is ISBN302.1.2.";
        SplitWords sw = new SplitWords();
        DelStopWords dt= new DelStopWords();
        dt.loadStopWords();
        source=dt.removeStopWords(source);
        ArrayList list=sw.split(source);
        list=dt.removeStopWords(list);
        Iterator i=list.iterator();
        while (i.hasNext()){
            System.out.print(i.next()+" ");
        }
    }
}
