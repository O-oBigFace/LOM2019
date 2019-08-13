package lily.tool.textprocess;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import lily.tool.snowball.SnowballProgram;

/****
 * Tokenizer
 * 这里采用的是Peter Cheng利用snowball写的例程。
 * 功能：以大写字母为标记分词，并提取词干
 * 原文地址：http://blog.csdn.net/petercheng456/archive/2007/03/27/1543297.aspx
 * @author Peter Cheng
 * @reviser Peng Wang
 ***/

public class TokenStem {
	 /****
     * Language
     */
    public static String language = "english";

    /**//* Stemmer */
    private static SnowballProgram stemmer = null;

    /**//* Stem method */
    private static Method stemMethod = null;

    /** *//**
     * Tokenize and stem
     * 
     * @param source
     *            The string to be processed
     * @return All the word stems
     */
    public static ArrayList tokenize(String source){
        if (TokenStem.stemmer == null){
            try{
                Class stemClass = Class.forName("lily.tool.snowball.ext."
                        + TokenStem.language + "Stemmer");
                TokenStem.stemmer = (SnowballProgram) stemClass.newInstance();
                TokenStem.stemMethod = stemClass
                        .getMethod("stem");
            } catch (Exception e){
                System.out.println("Error when initializing Stemmer!");
                System.exit(1);
            }
        }

        /**//* Tokenizer */
        ArrayList tokens = new ArrayList();
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < source.length(); i++){
            char character = source.charAt(i);
            if (Character.isLetter(character)){
                buffer.append(character);
            } else{
                if (buffer.length() > 0) {
                    tokens.add(buffer.toString());
                    buffer = new StringBuffer();
                }
            }
        }
        if (buffer.length() > 0) {
            tokens.add(buffer.toString());
        }

        /**//* All the words */
        ArrayList words = new ArrayList();

        /**//* All the words consisting of capitals */
        ArrayList allTheCapitalWords = new ArrayList();

        /**//* Tokenize according to the capitals */
        nextToken: for (Iterator allTokens = tokens.iterator(); allTokens
                .hasNext();) {
            String token = (String) allTokens.next();

            /**//* The words consisting of capitals */
            boolean allUpperCase = true;
            for (int i = 0; i < token.length(); i++) {
                if (!Character.isUpperCase(token.charAt(i))) {
                    allUpperCase = false;
                }
            }
            if (allUpperCase) {
                allTheCapitalWords.add(token);
                continue nextToken;
            }

            /**//* Other cases */
            int index = 0;
            nextWord: while (index < token.length()) {
                nextCharacter: while (true) {
                    index++;
                    if ((index == token.length())
                            || !Character.isLowerCase(token.charAt(index))) {
                        break nextCharacter;
                    }
                }
                words.add(token.substring(0, index).toLowerCase());
                token = token.substring(index);
                index = 0;
                continue nextWord;
            }
        }

        /**//* Stemming */
        try {
            for (int i = 0; i < words.size(); i++) {
            	TokenStem.stemmer.setCurrent((String) words.get(i));
            	TokenStem.stemMethod.invoke(TokenStem.stemmer);
                words.set(i, TokenStem.stemmer.getCurrent());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        words.addAll(allTheCapitalWords);

        return words;
    }
    public static void main(String args[]) {
    	TokenStem sw = new TokenStem();
        ArrayList list= tokenize("These 232 tables are wordStemming for ARE-Company using only. The I.S.B.N number of J.Smith's book is ISBN302.1.2.");
        //sw.split("123");
        Iterator i=list.iterator();
       while (i.hasNext()){
    	   System.out.print(i.next()+" ");
       }
    }
}
