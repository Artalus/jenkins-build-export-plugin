package io.jenkins.plugins.executionreporter;

import hudson.Extension;
import hudson.model.listeners.RunListener;
import hudson.model.*;

import java.util.*;
import java.util.logging.*;

import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.flow.FlowExecution;
import org.jenkinsci.plugins.workflow.graphanalysis.DepthFirstScanner;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

@Extension
public class GlobalRunListener extends RunListener<Run<?, ?>> {
    private static final Logger logger = Logger.getLogger(GlobalRunListener.class.getName());

    @Override
    public void onCompleted(Run<?, ?> build, TaskListener listener) {
        // listener.getLogger().println("[ExecutionReporter] println");

        if (!(build instanceof WorkflowRun)) {
            logger.fine(
                String.format("[ExecutionReporter] Ignoring build %s: not a WorkflowRun", build)
            );
            return;
        }
        traverse((WorkflowRun)build);
    }

    private void traverse(WorkflowRun b) {
        logger.fine(
            String.format("[ExecutionReporter] Traversing build: %s", b)
        );
        FlowExecution e = b.getExecution();
        ArrayList<FlowNode> nodes = new ArrayList<FlowNode>();

        DepthFirstScanner scanner = new DepthFirstScanner();
        if (!scanner.setup(e.getCurrentHeads())) {
            logger.severe(
                String.format("[ExecutionReporter] Failed to setup a scanner for build %s", b)
            );
            return;
        }
        for (FlowNode node : scanner) {
            nodes.add(node);
        }
        PostData pd = new PostData(b);
        // TODO: move all this to constructor?
        pd.nodes = NodeData.transform(nodes);
        Poster.post(
            pd,
            "https://webhook.site/d3972857-2f07-42f4-8ea3-931f6194dd87"
        );
    }
}
