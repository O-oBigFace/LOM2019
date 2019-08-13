package edu.seu.ontotuning.adapter;

import lily.onto.mapping.method.LOM;
import lily.tool.parameters.ParamStore;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

public class lilyLOM implements AdapterInterface {

    private static final int nParam = 18;

    private double currParams[] = null;

    @Override
    public int getNumOfParams() {
        return nParam;
    }

    @Override
    public double[][] getRangeOfParams() {
        /* 注：除参数OKSIM和edThreshold外，其余参数的值域的下界均为0 */
        double[][] rangeOfParams = new double[nParam][2];
        for (int i = 0; i < nParam; i++) {
            rangeOfParams[i][0] = 0;
        }
        rangeOfParams[0][1] = 1;   // 实数，(0,1);
        rangeOfParams[1][1] = 1;   // 实数，(0,1);
        rangeOfParams[2][1] = 101 - Double.MIN_VALUE; // 自然数，[0,100];

        rangeOfParams[3][1] = 101 - Double.MIN_VALUE; // 自然数，[0,100];
        rangeOfParams[4][0] = 1;
        rangeOfParams[4][1] = 21 - Double.MIN_VALUE;  // 正整数，[1,20];
        rangeOfParams[5][0] = 1;
        rangeOfParams[5][1] = 6 - Double.MIN_VALUE;   // 正整数，[1,5];
        rangeOfParams[6][1] = 1;   // 实数，(0,1);
        rangeOfParams[7][1] = 1;   // 实数，(0,1);
        rangeOfParams[8][0] = 1;
        rangeOfParams[8][1] = 51 - Double.MIN_VALUE;  // 正整数，[1,50];

        // rangeOfParams[9][1] = 4 - Double.MIN_VALUE;   // 自然数，[0,3];
        rangeOfParams[10 - 1][1] = 1;   // 实数,(0.5,1)
        rangeOfParams[10 - 1][0] = 0.5;
        rangeOfParams[11 - 1][1] = 0.2; // 实数,(0,0.2);
        rangeOfParams[12 - 1][1] = 1;   // 实数,(0.7,1)
        rangeOfParams[12 - 1][0] = 0.7;
        rangeOfParams[13 - 1][1] = 1;   // 实数,(0,1),cfw + csw + ciw = 1;
        rangeOfParams[14 - 1][1] = 1;
        // rangeOfParams[15 - 1][1] = 1;
        rangeOfParams[16 - 1 - 1][1] = 1;   // 实数,(0,1),pfw + psw + piw = 1;
        rangeOfParams[17 - 1 - 1][1] = 1;
        // rangeOfParams[18 - 1][1] = 1;
        rangeOfParams[19 - 1 - 2][1] = 1;   // 实数,(0,1),idpw + iopw + icw =1;
        rangeOfParams[20 - 1 - 2][1] = 1;
        // rangeOfParams[21 - 1][1] = 1;

        return rangeOfParams;
    }

    @Override
    public double[] getDefaultParams() {
        double[] defaultSetting = new double[nParam];

        defaultSetting[0] = 0.05; //Concept_Similarity_Threshold;
        defaultSetting[1] = 0.05; //Property_Similarity_Threshold;
        defaultSetting[2] = 15; //Semantic_SubGraph_Size;
        defaultSetting[3] = 50; //Snap_Size;
        defaultSetting[4] = 4; //Top_k;
        defaultSetting[5] = 3; //Neighbor_Scale;
        defaultSetting[6] = 0.5; //Positive_Anchor_Threshold;
        defaultSetting[7] = 0.001; //Negative_Anchor_Threshold;
        defaultSetting[8] = 8; //SemanticDoc_Threshold;
        // defaultSetting[9] = 3; //Propagation_Strategy;
        defaultSetting[10 - 1] = 0.5; //OKSIM;
        defaultSetting[11 - 1] = 0.01; //BADSIM;
        defaultSetting[12 - 1] = 0.8; //edThreshold;
        defaultSetting[13 - 1] = 0.3; //cfw = 0.3;
        defaultSetting[14 - 1] = 0.5d / 0.7d; //csw = 0.5;
        // defaultSetting[15 - 1] = 0.2; //ciw = 0.2;
        defaultSetting[16 - 1 - 1] = 0.3; //pfw;
        defaultSetting[17 - 1 - 1] = 0.4d / 0.7d; //psw = 0.4;
        // defaultSetting[18 - 1] = 0.3; //piw;
        defaultSetting[19 - 1 - 2] = 0.25; //idpw;
        defaultSetting[20 - 1 - 2] = 0.25d / 0.75d; //iopw = 0.25;
        // defaultSetting[21 - 1] = 0.5; //icw;

        return defaultSetting;
    }

    @Override
    public double[] getCurrentParams() {
        return Arrays.copyOf(currParams, nParam);
    }

    private double[] translateParams(double[] rawParams) {
        double[] expectedParams = new double[nParam];
        double[][] rangeOfParams = getRangeOfParams();
        for (int i = 0; i < nParam; ++i) {
            expectedParams[i] = rawParams[i];
            if (expectedParams[i] < rangeOfParams[i][0])
                expectedParams[i] = rangeOfParams[i][0];
            if (expectedParams[i] > rangeOfParams[i][1])
                expectedParams[i] = rangeOfParams[i][1];
        }
        return expectedParams;
    }

    @Override
    public void setParams(double[] expectedParams) {
        loadParams(translateParams(expectedParams));
    }

    @Override
    public void useDefaultParams() {
        loadParams(getDefaultParams());
    }

    @Override
    public void setSourceOnto(String path) {
        ParamStore.SourceOnt = path;
    }

    @Override
    public void setTargetOnto(String path) {
        ParamStore.TargetOnt = path;
    }

    @Override
    public void setOutputFolder(String path) {
        ParamStore.ResultFile = path + "/" + AdapterInterface.FN_ALIGNMENT;
    }

    @Override
    public void setResourceFolder(String path) {
        // no resource file needed for Lily
    }

    @Override
    public void doMatching() {
        new LOM().run();
    }


    private void loadParams(double[] expectedParams) {
        // currParams = expectedParams;
        currParams = Arrays.copyOf(expectedParams, nParam);

        ParamStore.Concept_Similarity_Threshold = expectedParams[0];
        ParamStore.Property_Similarity_Threshold = expectedParams[1];
        ParamStore.Semantic_SubGraph_Size = (int) expectedParams[2];
        ParamStore.Snap_Size = (int) expectedParams[3];
        ParamStore.Top_k = (int) expectedParams[4];
        ParamStore.Neighbor_Scale = (int) expectedParams[5];
        ParamStore.Positive_Anchor_Threshold = expectedParams[6];
        ParamStore.Negative_Anchor_Threshold = expectedParams[7];
        ParamStore.SemanticDoc_Threshold = (int) expectedParams[8];
        // ParamStore.Propagation_Strategy = (int) expectedParams[9];
        ParamStore.OKSIM = expectedParams[10 - 1];
        ParamStore.BADSIM = expectedParams[11 - 1];
        ParamStore.edThreshold = expectedParams[12 - 1];
        ParamStore.cfw = expectedParams[13 - 1];
        ParamStore.csw = expectedParams[14 - 1] * (1 - ParamStore.cfw);
        ParamStore.ciw = 1 - ParamStore.cfw - ParamStore.csw; // expectedParams[15 - 1];
        ParamStore.pfw = expectedParams[16 - 1 - 1];
        ParamStore.psw = expectedParams[17 - 1 - 1] * (1 - ParamStore.pfw);
        ParamStore.piw = 1 - ParamStore.pfw - ParamStore.psw; // expectedParams[18 - 1];
        ParamStore.idpw = expectedParams[19 - 1 - 2];
        ParamStore.iopw = expectedParams[20 - 1 - 2] * (1 - ParamStore.idpw);
        ParamStore.icw = 1 - ParamStore.idpw - ParamStore.iopw; // expectedParams[21 - 1];
    }

    public static void main(String args[]) throws Exception{
        lilyLOM adapter = new lilyLOM();
// http://repositories.seals-project.eu/tdrs/testdata/persistent/conference/conference-v1/suite/cmt-conference/component/source/
        URL sourceURL = new URL("http://repositories.seals-project.eu/tdrs/testdata/persistent/anatomy_track/anatomy_track-anatomy_2015/suite/mouse-human-suite/component/source/");
        File sourceFile = File.createTempFile("source", ".rdf");
        // System.out.println(sourceFile.getAbsolutePath());
        getRemoteFile(sourceURL, sourceFile.getAbsolutePath());
// http://repositories.seals-project.eu/tdrs/testdata/persistent/conference/conference-v1/suite/cmt-conference/component/target/
        URL targetURL = new URL("http://repositories.seals-project.eu/tdrs/testdata/persistent/anatomy_track/anatomy_track-anatomy_2015/suite/mouse-human-suite/component/target/");
        File targetFile = File.createTempFile("target", ".rdf");
        // System.out.println(sourceFile.getAbsolutePath());
        getRemoteFile(targetURL, targetFile.getAbsolutePath());


        adapter.setSourceOnto(sourceFile.getAbsolutePath());
        adapter.setTargetOnto(targetFile.getAbsolutePath());

        File alignmentFile = File.createTempFile("lily", ".rdf");
        ParamStore.ResultFile = alignmentFile.getAbsolutePath();
        // doMatching()之前必须要置好参数。。。
        adapter.useDefaultParams();
        adapter.doMatching();
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
