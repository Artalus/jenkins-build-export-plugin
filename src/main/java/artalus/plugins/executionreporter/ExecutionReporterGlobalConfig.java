package artalus.plugins.executionreporter;

import hudson.Extension;
import hudson.ExtensionList;
import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

/**
 * Example of Jenkins global configuration.
 */
@Extension
public class ExecutionReporterGlobalConfig extends GlobalConfiguration {

    /** @return the singleton instance */
    public static ExecutionReporterGlobalConfig get() {
        return ExtensionList.lookupSingleton(ExecutionReporterGlobalConfig.class);
    }

    private String postUrl;

    public ExecutionReporterGlobalConfig() {
        // When Jenkins is restarted, load any saved configuration from disk.
        load();
    }

    /** @return the currently configured label, if any */
    public String getPostUrl() {
        return postUrl;
    }

    /**
     * Together with {@link #getLabel}, binds to entry in {@code config.jelly}.
     * @param label the new value of this field
     */
    @DataBoundSetter
    public void setPostUrl(String value) {
        this.postUrl = value;
        save();
    }

    public FormValidation doCheckPostUrl(@QueryParameter String value) {
        if (StringUtils.isEmpty(value)) {
            return FormValidation.warning("Please specify a URL.");
        }
        return FormValidation.ok();
    }

}
