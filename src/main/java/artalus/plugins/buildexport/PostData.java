package artalus.plugins.buildexport;
import java.util.*;
import hudson.model.*;
import jenkins.model.Jenkins;

// this class is exported as json to the outside world, its fields are allowed to be unread
@edu.umd.cs.findbugs.annotations.SuppressWarnings("URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
class PostData {
    public String job;
    public String buildName;
    public String buildFullName;
    public String buildUrl;
    public String buildResult;
    public int buildNumber;
    public Map<String, String> buildParameters;
    public ArrayList<NodeData> nodes;

    public PostData (Run<?,?> run) {
        this.job = run.getParent().getFullName();
        this.buildName = run.getDisplayName();
        this.buildFullName = run.getFullDisplayName();
        this.buildUrl = String.format("%s%s", Jenkins.get().getRootUrl(), run.getUrl());
        this.buildNumber = run.getNumber();
        this.buildResult = run.getResult().toString();

        this.buildParameters = new HashMap<String, String>();
        ParametersAction p = run.getAction(ParametersAction.class);
        if (p != null) {
            for (ParameterValue pv : p.getAllParameters()) {
                this.buildParameters.put(pv.getName(), censored(pv));
            }
        }
    }

    // TODO: if ParameterValue itself exists, it should have value. am i wrong?
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    private static String censored(ParameterValue pv) {
        return pv.isSensitive() ? "*CENSORED*" : pv.getValue().toString();
    }
}
