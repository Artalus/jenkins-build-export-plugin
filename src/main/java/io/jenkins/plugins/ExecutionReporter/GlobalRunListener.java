package io.jenkins.plugins.executionreporter;

import hudson.Extension;
import hudson.model.listeners.RunListener;
import hudson.model.*;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.*;

import org.jenkinsci.plugins.workflow.actions.*;
import org.jenkinsci.plugins.workflow.cps.nodes.*;
import org.jenkinsci.plugins.workflow.flow.FlowExecution;
import org.jenkinsci.plugins.workflow.graph.*;
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
        ArrayList<NodeData> tree_data = new ArrayList<NodeData>();

        DepthFirstScanner scanner = new DepthFirstScanner();
        if (!scanner.setup(e.getCurrentHeads())) {
            logger.severe(
                String.format("[ExecutionReporter] Failed to setup a scanner for build %s", b)
            );
            return;
        }
        for (FlowNode node : scanner) {
            tree_data.add(new NodeData(node));
        }
        Collections.sort(tree_data);
        Collections.reverse(tree_data);

        StringBuilder builder = new StringBuilder();
        for (NodeData d : tree_data) {
            builder.append(stringize(d));
        }
        logger.info(
            String.format("Build tree is:\n%s", builder.toString())
        );
        // }
        // //
        // // tree_data.each{ def t ->
        // //     if (t.parent)
        // //         tree_datat[.parent.children] += t
        // // }
        // tree_data.sort { lhs, rhs -> lhs.id <=> rhs.id }
        // tree_data = tree_data.reverse()
        // tree_data.each {
        //     println(stringize(it))
        // }

        // println "---"
        // NNN.class.methods.each {
        // println "- $it.name (${it.parameters.name ?: ''})"
        // }

        // flush()
    }
    class NodeData implements Comparable<NodeData> {
        public String id;
        public String parent;
        public int depth;
        public ArrayList<NodeData> children;
        public FlowNode content;
        public NodeData(FlowNode node) {
            this.id = node.getId();
            this.parent = node.getEnclosingId();
            this.depth = node.getParentIds().size();
            this.content = node;
            this.children = new ArrayList<NodeData>();
        }
        @Override
        public int compareTo(NodeData other) {
            // if (this.parent == null || other.parent == null || this.parent == other.parent)
            //     return this.id.compareTo(other.id);
            // return this.parent.compareTo(other.parent);
            return this.id.compareTo(other.id);
        }
    }

    static long getTs(FlowNode node) {
        return node.getAction(TimingAction.class).getStartTime();
    }

    static String getTime(FlowNode node) {
        long ts = getTs(node);
        Date dateObj = new Date(ts);
        String clean = new SimpleDateFormat("hh:mm:ss").format(dateObj);
        return clean;
    }

    static long calcBlockDuration(StepEndNode end) {
        return getTs(end) - getTs(end.getStartNode());
    }

    String stringize(NodeData node) {
        return String.format("sss: %s", node);
    }
}
