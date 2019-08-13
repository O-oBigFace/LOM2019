package lily.tool.strsimilarity;

import java.util.ArrayList;

/**
 * Created by Fred on 9/25/15.
 */
public class SimInArray {

    private static StrEDSim edsim = new StrEDSim();

    public static int getPosSimInArray(final ArrayList<String> wds, final String wd) {
        double fMaxSim = 0, fCurrSim = 0;
        int nMaxSimPos = -1;
        for (String s : wds) {
            fCurrSim = edsim.getNormEDSim(s, wd);
            if (fCurrSim > fMaxSim) {
                nMaxSimPos = wds.indexOf(s);
                fMaxSim = fCurrSim;
            }
        }
        if (fMaxSim < 0.8)
            nMaxSimPos = -1;
        return nMaxSimPos;
    }

}
