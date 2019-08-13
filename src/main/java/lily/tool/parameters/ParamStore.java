package lily.tool.parameters;

import java.io.FileInputStream;
import java.util.Properties;

/**
 * Created by Fred on 7/12/15.
 */
// public static修饰的就相当于是整个项目的全局变量
public class ParamStore {

    public static String
            SourceOnt = "",
            TargetOnt = "",
            RefAlignFile = "",
            ResultFile = "";

    // 已经填入默认的参数...
    public static Double
            Concept_Similarity_Threshold = 0.05,
            Property_Similarity_Threshold = 0.05,
            OKSIM = 0.5,
            BADSIM = 0.01,
            edThreshold = 0.8,
            cfw = 0.3,
            csw = 0.5,
            ciw = 0.2,
            pfw = 0.3,
            psw = 0.4,
            piw = 0.3,
            idpw = 0.25,
            iopw = 0.25,
            icw = 0.5,
            Positive_Anchor_Threshold = 0.5,
            Negative_Anchor_Threshold = 0.001;

    public static int
            Semantic_SubGraph_Size = 15,
            Snap_Size = 50,
            Top_k = 4,
            Neighbor_Scale = 3,
            SemanticDoc_Threshold = 8,
            Propagation_Strategy = 3;

    public static boolean
            readFromFile = true,
            doEvaluation = true;

    private static Double getDouble(Properties p, String k, Double def)
    {
        return Double.valueOf(p.getProperty(k, def.toString()));
    }

    private static int getInt(Properties p, String k, Integer def)
    {
        return Integer.valueOf(p.getProperty(k, def.toString()));
    }

    // 保留的原本Lily从文件properties的一个通道，但是没有用到.
    public static void loadConfig(String filePath)
    {
        if (!readFromFile)
            return;
        try {
            Properties prop = new Properties(); //属性集合对象
            FileInputStream fis = new FileInputStream(filePath); //属性文件流
            prop.load(fis); //将属性文件流装载到Properties对象中
            fis.close();
            SourceOnt = prop.getProperty("SourceOnt", SourceOnt); //尝试读取。若失败，则保持原值不变。
            TargetOnt = prop.getProperty("TargetOnt", TargetOnt);
            RefAlignFile = prop.getProperty("RefAlignFile", RefAlignFile);
            ResultFile = prop.getProperty("ResultFile", ResultFile);
            Concept_Similarity_Threshold = getDouble(prop, "Concept_Similarity_Threshold", Concept_Similarity_Threshold);
            Property_Similarity_Threshold = getDouble(prop, "Property_Similarity_Threshold", Property_Similarity_Threshold);
            OKSIM = getDouble(prop, "OKSIM", OKSIM);
            BADSIM = getDouble(prop, "BADSIM", BADSIM);
            edThreshold = getDouble(prop, "edThreshold", edThreshold);
            cfw = getDouble(prop, "cfw", cfw);
            csw = getDouble(prop, "csw", csw);
            ciw = getDouble(prop, "ciw", ciw);
            pfw = getDouble(prop, "pfw", pfw);
            psw = getDouble(prop, "psw", psw);
            piw = getDouble(prop, "piw", piw);
            idpw = getDouble(prop, "idpw", idpw);
            iopw = getDouble(prop, "iopw", iopw);
            icw = getDouble(prop, "icw", icw);
            Positive_Anchor_Threshold = getDouble(prop, "Positive_Anchor_Threshold", Positive_Anchor_Threshold);
            Negative_Anchor_Threshold = getDouble(prop, "Negative_Anchor_Threshold", Negative_Anchor_Threshold);
            Semantic_SubGraph_Size = getInt(prop, "Semantic_SubGraph_Size", Semantic_SubGraph_Size);
            Snap_Size = getInt(prop, "Snap_Size", Snap_Size);
            Top_k = getInt(prop, "Top_k", Top_k);
            Neighbor_Scale = getInt(prop, "Neighbor_Scale", Neighbor_Scale);
            SemanticDoc_Threshold = getInt(prop, "SemanticDoc_Threshold", SemanticDoc_Threshold);
            Propagation_Strategy = getInt(prop, "Propagation_Strategy", Propagation_Strategy);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // properties file这个也没有用到,这个是王文宇后来改进的
    public static void loadConfig()
    {
        loadConfig("lily.properties");
    }

}