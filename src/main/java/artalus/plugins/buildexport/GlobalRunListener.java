package artalus.plugins.buildexport;

import hudson.Extension;
import hudson.model.listeners.RunListener;
import hudson.model.*;

import java.util.logging.Logger;

import org.jenkinsci.plugins.workflow.job.WorkflowRun;

@Extension
public class GlobalRunListener extends RunListener<Run<?, ?>> {
    private static final Logger logger = Logger.getLogger(GlobalRunListener.class.getName());

    @Override
    public void onCompleted(Run<?, ?> build, TaskListener listener) {
        if (!(build instanceof WorkflowRun)) {
            logger.fine(
                String.format("[BuildExport] Ignoring build %s: not a WorkflowRun", build)
            );
            return;
        }
        PipelineProcessor.doMagic((WorkflowRun)build);
    }
}
