package edu.seu.ontotuning.adapter;

import com.memetix.mst.translate.Translate;
import com.memetix.mst.language.Language;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class test {
    public static void main(String[] agrs)throws Exception {
//        URL sourceURL = new URL("http://repositories.seals-project.eu/tdrs/testdata/persistent/conference/conference-v1/suite/cmt-conference/component/target/");
//        File sourceFile = File.createTempFile("source", ".rdf");
//        System.out.println(sourceFile.getAbsolutePath());
//        getRemoteFile(sourceURL, sourceFile.getAbsolutePath());
//        ParamStore.SourceOnt = sourceFile.getAbsolutePath();
//        sourceFile.deleteOnExit();


        // 测试一下Bing的翻译API 到底是怎么回事
//        Translate.setClientId("");
//        Translate.setClientSecret("");
//        String translatedText = Translate.execute("Bonjour le monde", Language.FRENCH, Language.ENGLISH);
//        System.out.println(translatedText);

        String str = "http://repositories.seals-project.eu/tdrs/testdata/persistent/anatomy_track/anatomy_track-default/suite/mouse-human-suite/component/source/";
        System.out.println(str.contains("anatomy"));
    }



    private static boolean getRemoteFile(URL url, String fileName) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        DataInputStream input = new DataInputStream(conn.getInputStream());
        DataOutputStream output = new DataOutputStream(new FileOutputStream(fileName));
        byte[] buffer = new byte[1024 * 8];
        int count = 0;
        while ((count = input.read(buffer)) > 0) {
            output.write(buffer, 0, count);
        }
        output.close();
        input.close();
        return true;
    }
}
