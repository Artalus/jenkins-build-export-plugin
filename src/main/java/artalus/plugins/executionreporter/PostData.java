package artalus.plugins.executionreporter;
import java.util.*;
import hudson.model.*;

class PostData {
    public String job;
    public String buildName;
    public String buildFullName;
    public int buildNumber;
    public Map<String, String> buildParameters;
    public ArrayList<NodeData> nodes;

    public PostData (Run<?,?> run) {
        this.job = run.getParent().getFullName();
        this.buildName = run.getDisplayName();
        this.buildFullName = run.getFullDisplayName();
        this.buildNumber = run.getNumber();
        this.buildParameters = new HashMap<String, String>();
        ParametersAction p = run.getAction(ParametersAction.class);
        if (p != null) {
            for (ParameterValue pv : p.getAllParameters()) {
                String v = pv.isSensitive() ? "*CENSORED*" : pv.getValue().toString();
                this.buildParameters.put(pv.getName(), v);
            }
        }
    }
}
