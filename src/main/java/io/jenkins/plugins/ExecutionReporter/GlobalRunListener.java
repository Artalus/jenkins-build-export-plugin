package io.jenkins.plugins.executionreporter;

import hudson.Extension;
import hudson.model.listeners.RunListener;
import hudson.model.*;

import java.util.logging.*;

import org.jenkinsci.plugins.workflow.graphanalysis.DepthFirstScanner;
import org.jenkinsci.plugins.workflow.actions.*;
import org.jenkinsci.plugins.workflow.cps.nodes.*;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

@Extension
public class GlobalRunListener extends RunListener<Run<?, ?>> {
    private static final Logger logger = Logger.getLogger(GlobalRunListener.class.getName());

    @Override
    public void onCompleted(Run<?, ?> build, TaskListener listener) {
        listener.getLogger().println("[ExecutionReporter] println ggg");
        logger.log(Level.WARNING, "log ggg");

        if (!(build instanceof WorkflowRun)) {
            String msg = String.format("Ignoring build %s: not a WorkflowRun", build);
            logger.log(Level.INFO, msg);
            return;
        }
        traverse((WorkflowRun)build);
    }

    private void traverse(WorkflowRun b) {
        String msg = String.format("[ExecutionReporter] Traversing build: %s", b);
        logger.log(Level.INFO, msg);
    }
}
