package edu.seu.ontotuning.adapter;

import lily.onto.mapping.method.Rclm;
import lily.tool.parameters.ParamStore;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

// 每个系统参数的寻找和适配器的制作是所有最难的环节
public class lily implements AdapterInterface {

    private static final int nParam = 13;

    private double currParams[] = null;

//    // @Override
    public int getNumOfParams() {
        return nParam;
    }

//    // @Override
    public double[][] getRangeOfParams() {
        /* 注：除参数OKSIM和edThreshold外，其余参数的值域的下界均为0 */
        double[][] rangeOfParams = new double[nParam][2];
        for (int i = 0; i < nParam; i++) {
            rangeOfParams[i][0] = 0;
        }
        rangeOfParams[0][1] = 1;   // 实数，(0,1);
        rangeOfParams[1][1] = 1;   // 实数，(0,1);
        rangeOfParams[2][1] = 101 - Double.MIN_VALUE; // 自然数，[0,100]; snapSize

        // rangeOfParams[3][1] = 101 - Double.MIN_VALUE; // 自然数，[0,100];
        // rangeOfParams[4][0] = 1;
        // rangeOfParams[4][1] = 21 - Double.MIN_VALUE;  // 正整数，[1,20];
        // rangeOfParams[5][0] = 1;
        // rangeOfParams[5][1] = 6 - Double.MIN_VALUE;   // 正整数，[1,5];
        // rangeOfParams[6][1] = 1;   // 实数，(0,1);
        // rangeOfParams[7][1] = 1;   // 实数，(0,1);
        // rangeOfParams[8][0] = 1;
        // rangeOfParams[8][1] = 51 - Double.MIN_VALUE;  // 正整数，[1,50];

        rangeOfParams[9 - 6][1] = 4 - Double.MIN_VALUE;   // 自然数离散，[0,3]; Propagation_Strategy
        rangeOfParams[10 - 6][1] = 1;   // 实数,(0.5,1)
        rangeOfParams[10 - 6][0] = 0.5; // 可信相似度阈值OKSIM  下界专家指定
        rangeOfParams[11 - 6][1] = 0.2; // 实数,(0,0.2);BADSIM 不可信相似度阈值
        rangeOfParams[12 - 6][1] = 1;   // 实数,(0.7,1)
        rangeOfParams[12 - 6][0] = 0.7; // edThreshold 下界专家给定0.7;
        rangeOfParams[13 - 6][1] = 1;   // 实数,(0,1),cfw + csw + ciw = 1;
        rangeOfParams[14 - 6][1] = 1;
        // rangeOfParams[15 - 6][1] = 1;
        rangeOfParams[16 - 6 - 1][1] = 1;   // 实数,(0,1),pfw + psw + piw = 1;
        rangeOfParams[17 - 6 - 1][1] = 1;
        // rangeOfParams[18 - 6][1] = 1;
        rangeOfParams[19 - 6 - 2][1] = 1;   // 实数,(0,1),idpw + iopw + icw =1;
        rangeOfParams[20 - 6 - 2][1] = 1;
        // rangeOfParams[21 - 6][1] = 1;

        return rangeOfParams;
    }

    // @Override
    public double[] getDefaultParams() {
        double[] defaultSetting = new double[nParam];

        defaultSetting[0] = 0.05; //Concept_Similarity_Threshold;
        defaultSetting[1] = 0.05; //Property_Similarity_Threshold;
        defaultSetting[2] = 15; //Semantic_SubGraph_Size;这里也是标准的离散参数

        // NOTE: the following 6 parameters are for LOM only
        // defaultSetting[3] = 50; //Snap_Size;
        // defaultSetting[4] = 4; //Top_k;
        // defaultSetting[5] = 3; //Neighbor_Scale;
        // defaultSetting[6] = 0.5; //Positive_Anchor_Threshold;
        // defaultSetting[7] = 0.001; //Negative_Anchor_Threshold;
        // defaultSetting[8] = 8; //SemanticDoc_Threshold;

        defaultSetting[9 - 6] = 3; //Propagation_Strategy;  标准的离散型参数[0,1,2,3]
        defaultSetting[10 - 6] = 0.5; //OKSIM;
        defaultSetting[11 - 6] = 0.01; //BADSIM;
        defaultSetting[12 - 6] = 0.8; //edThreshold;
        defaultSetting[13 - 6] = 0.3; //cfw = 0.3;
        defaultSetting[14 - 6] = 0.5d / 0.7d; //csw = 0.5; 这里是是为了配合后面
        // defaultSetting[15 - 6] = 0.2; //ciw = 0.2; 不要ciw因为保证之和为1
        defaultSetting[16 - 6 - 1] = 0.3; //pfw;
        defaultSetting[17 - 6 - 1] = 0.4d / 0.7d; //psw = 0.4;
        // defaultSetting[18 - 6] = 0.3; //piw;
        defaultSetting[19 - 6 - 2] = 0.25; //idpw;
        defaultSetting[20 - 6 - 2] = 0.25d / 0.75d; //iopw = 0.25;
        // defaultSetting[21 - 6] = 0.5; //icw;

        return defaultSetting;
    }

    // @Override
    public double[] getCurrentParams() {
        return Arrays.copyOf(currParams, nParam);
    }

    private double[] translateParams(double[] rawParams) {
        double[] expectedParams = new double[nParam];
        double[][] rangeOfParams = getRangeOfParams();
        double[] defaultParams = getDefaultParams();
        for (int i = 0; i < nParam; ++i) {
            expectedParams[i] = rawParams[i];
            // 边界可以取得的，在很多地方取得的影响不大
            if (expectedParams[i] < rangeOfParams[i][0])
                expectedParams[i] = defaultParams[i];
            if (expectedParams[i] > rangeOfParams[i][1])
                expectedParams[i] = defaultParams[i];
        }
        return expectedParams;
    }

    // 极重要的方法,向内部传入参数.
    // @Override
    public void setParams(double[] expectedParams) {
        loadParams(translateParams(expectedParams));
    }

    // @Override
    public void useDefaultParams() {
        loadParams(getDefaultParams());
    }

    // 这里跟之前不太一样，路径需要强行传入.
    // @Override
    public void setSourceOnto(String path) {
        ParamStore.SourceOnt = path;
    }

    // @Override
    public void setTargetOnto(String path) {
        ParamStore.TargetOnt = path;
    }

    // @Override
    public void setOutputFolder(String path) {
        // path只不过是一个Folder不是文件要注意
        ParamStore.ResultFile = path + "/" + AdapterInterface.FN_ALIGNMENT;
    }

    // @Override
    public void setResourceFolder(String path) {
        // no resource file needed for Lily
    }

    // @Override
    public void doMatching() {
        Rclm.runStandard();
    }

    // 王文宇改写的Lily设置参数入口,类似Falcon：从ParamStore中读取参数
    // 注意load到内部之前，一定是经过修正了的
    private void loadParams(double[] expectedParams) {
        // currParams = expectedParams;
        currParams = Arrays.copyOf(expectedParams, nParam);
        // 实现真正地控制，这里要注意load()过程中离散参数如何去处理？
        ParamStore.Concept_Similarity_Threshold = expectedParams[0];
        ParamStore.Property_Similarity_Threshold = expectedParams[1];
        ParamStore.Semantic_SubGraph_Size = (int) expectedParams[2];// 重要的离散化步骤
        // ParamStore.Snap_Size = (int) expectedParams[3];
        // ParamStore.Top_k = (int) expectedParams[4];
        // ParamStore.Neighbor_Scale = (int) expectedParams[5];
        // ParamStore.Positive_Anchor_Threshold = expectedParams[6];
        // ParamStore.Negative_Anchor_Threshold = expectedParams[7];
        // ParamStore.SemanticDoc_Threshold = (int) expectedParams[8];
        ParamStore.Propagation_Strategy = (int) expectedParams[9 - 6];// 重要的离散化步骤
        ParamStore.OKSIM = expectedParams[10 - 6];
        ParamStore.BADSIM = expectedParams[11 - 6];
        ParamStore.edThreshold = expectedParams[12 - 6];
        ParamStore.cfw = expectedParams[13 - 6];// 下面的目的是为了防止cfw=0.8，csw=0.7的这种情况对其进行的修正
        ParamStore.csw = expectedParams[14 - 6] * (1 - ParamStore.cfw);// ParamStore.cfw 事先已经有值了
        ParamStore.ciw = 1 - ParamStore.cfw - ParamStore.csw; // expectedParams[15 - 6];
        ParamStore.pfw = expectedParams[16 - 6 - 1];
        ParamStore.psw = expectedParams[17 - 6 - 1] * (1 - ParamStore.pfw);
        ParamStore.piw = 1 - ParamStore.pfw - ParamStore.psw; // expectedParams[18 - 6];
        ParamStore.idpw = expectedParams[19 - 6 - 2];
        ParamStore.iopw = expectedParams[20 - 6 - 2] * (1 - ParamStore.idpw);
        ParamStore.icw = 1 - ParamStore.idpw - ParamStore.iopw; // expectedParams[21 - 6];

    }

    public static void main(String args[]) throws Exception{
        lily adapter = new lily();
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
