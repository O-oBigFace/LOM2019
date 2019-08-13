package lily.tool.seals;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

import edu.seu.ontotuning.adapter.lily;
import edu.seu.ontotuning.adapter.lilyLOM;
import eu.sealsproject.platform.res.domain.omt.IOntologyMatchingToolBridge;
import eu.sealsproject.platform.res.tool.api.ToolBridgeException;
import eu.sealsproject.platform.res.tool.api.ToolException;
import eu.sealsproject.platform.res.tool.api.ToolType;
import eu.sealsproject.platform.res.tool.impl.AbstractPlugin;
import lily.tool.parameters.ParamStore;

public class MatcherBridge extends AbstractPlugin implements IOntologyMatchingToolBridge {
    /**
     * Aligns to ontologies specified via their URL and returns the
     * URL of the resulting alignment, which should be stored locally.
     */
    public URL align(URL source, URL target) throws ToolBridgeException, ToolException {
        try {
            System.out.println(source.toString());
            System.out.println("------------------------------------------------------------------------");
            System.out.println(target.toString());
            System.out.println();
            System.out.println();

            /**
             * 这里是针对SEALS平台下每个任务的一个入口程序，不同的任务在这里进行分流。
             */
            if(source.toString().contains("anatomy")){
                lilyLOM adapter = new lilyLOM();
                // doMatching()之前必须要置好参数。。。
                if (source.toString().startsWith("http")) {
                    File sourceFile = File.createTempFile("source", ".rdf");
                    getRemoteFile(source, sourceFile.getAbsolutePath());
                    adapter.setSourceOnto(sourceFile.getAbsolutePath());
                    // sourceFile.deleteOnExit();
                } else if (source.toString().startsWith("file")) {
                    File sourceFile = new File(source.toURI());
                    adapter.setSourceOnto(sourceFile.getAbsolutePath());
                } else
                    adapter.setSourceOnto(source.toString());


                if (target.toString().startsWith("http")) {
                    File targetFile = File.createTempFile("target", ".rdf");
                    getRemoteFile(target, targetFile.getAbsolutePath());
                    adapter.setTargetOnto(targetFile.getAbsolutePath());
                    // targetFile.deleteOnExit();
                } else if (target.toString().startsWith("file")) {
                    File targetFile = new File(target.toURI());
                    adapter.setTargetOnto(targetFile.getAbsolutePath());
                } else
                    adapter.setTargetOnto(source.toString());


                File alignmentFile = File.createTempFile("lilylom", ".rdf");
                ParamStore.ResultFile = alignmentFile.getAbsolutePath();

                // alignmentFile.deleteOnExit();
                try {
                    adapter.useDefaultParams();
                    adapter.doMatching();
                    return alignmentFile.toURI().toURL();
                } catch (Exception e) {
                    throw new ToolException("cannot align", e);
                }
                // 这部分为了简单起见，其实是重复的。
            }else{
                lily adapter = new lily();
                // doMatching()之前必须要置好参数。。。
                if (source.toString().startsWith("http")) {
                    File sourceFile = File.createTempFile("source", ".rdf");
                    getRemoteFile(source, sourceFile.getAbsolutePath());
                    adapter.setSourceOnto(sourceFile.getAbsolutePath());
                    // sourceFile.deleteOnExit();
                } else if (source.toString().startsWith("file")) {
                    File sourceFile = new File(source.toURI());
                    adapter.setSourceOnto(sourceFile.getAbsolutePath());
                } else
                    adapter.setSourceOnto(source.toString());


                if (target.toString().startsWith("http")) {
                    File targetFile = File.createTempFile("target", ".rdf");
                    getRemoteFile(target, targetFile.getAbsolutePath());
                    adapter.setTargetOnto(targetFile.getAbsolutePath());
                    // targetFile.deleteOnExit();
                } else if (target.toString().startsWith("file")) {
                    File targetFile = new File(target.toURI());
                    adapter.setTargetOnto(targetFile.getAbsolutePath());
                } else
                    adapter.setTargetOnto(source.toString());


                File alignmentFile = File.createTempFile("lily", ".rdf");
                ParamStore.ResultFile = alignmentFile.getAbsolutePath();

                // alignmentFile.deleteOnExit();
                try {
                    adapter.useDefaultParams();
                    adapter.doMatching();
                    return alignmentFile.toURI().toURL();
                } catch (Exception e) {
                    throw new ToolException("cannot align", e);
                }
            }
        } catch (Exception e) {
            throw new ToolBridgeException("cannot create file for resulting alignment", e);
        }
    }

    /**
     * This functionality is not supported by the tool. In case
     * it is invoced a ToolException is thrown.
     */
    public URL align(URL source, URL target, URL inputAlignment) throws ToolBridgeException, ToolException {
        throw new ToolException("functionality of called method is not supported");
    }

    /**
     * In our case the DemoMatcher can be executed on the fly. In case
     * prerequesites are required it can be checked here.
     */
    public boolean canExecute() {
        return true;
    }

    /**
     * The DemoMatcher is an ontology matching tool. SEALS supports the
     * evaluation of different tool types like e.g., reasoner and storage systems.
     */
    public ToolType getType() {
        return ToolType.OntologyMatchingTool;
    }


    private boolean getRemoteFile(URL url, String fileName) throws IOException {
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
