package edu.seu.ontotuning.adapter;

public interface AdapterInterface {

    String FN_ALIGNMENT = "alignment.rdf", FN_RESULT = "result.txt";

    int getNumOfParams();

    double[][] getRangeOfParams();

    double[] getDefaultParams();

    double[] getCurrentParams();

    void setParams(double[] expectedParams);

    void useDefaultParams();

    void setSourceOnto(String path);

    void setTargetOnto(String path);

    void setOutputFolder(String path);

    void setResourceFolder(String path);

    void doMatching();

}
