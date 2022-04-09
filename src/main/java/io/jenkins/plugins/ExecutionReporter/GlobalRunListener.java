package io.jenkins.plugins.executionreporter;

import hudson.Extension;
import hudson.model.listeners.RunListener;
import hudson.model.*;
import hudson.model.Action;

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

        StringBuilder builder = new StringBuilder();
        for (NodeData d : tree_data) {
            builder.append('\n');
            builder.append(stringize(d));
        }
        logger.info(
            String.format("Build tree is:%s", builder.toString())
        );

        // println "---"
        // NNN.class.methods.each {
        // println "- $it.name (${it.parameters.name ?: ''})"
        // }
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
            this.depth = node.getAllEnclosingIds().size();
            this.content = node;
            this.children = new ArrayList<NodeData>();
        }
        @Override
        public int compareTo(NodeData other) {
            // if (this.parent == null || other.parent == null || this.parent == other.parent)
            //     return this.id.compareTo(other.id);
            // return this.parent.compareTo(other.parent);
            Integer i1 = (this.id == null) ? 0 : Integer.valueOf(this.id);
            Integer i2 = (other.id == null) ? 0 : Integer.valueOf(other.id);
            return i1.compareTo(i2);
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
        // String.repeat in java is crazy
        String indent = String.join("", Collections.nCopies(node.depth, "   "));
        // TODO: is explicit upcasting needed? need more java knowledge
        StringBuilder str = new StringBuilder();
        if (node.content instanceof StepStartNode) {
            str.append(stringize_inner((StepStartNode)node.content));
        }
        else if (node.content instanceof StepEndNode) {
            str.append(stringize_inner((StepEndNode)node.content));
        }
        else {
            str.append(stringize_inner(node.content));
        }
        if (node.content instanceof StepEndNode) {
            float dur = calcBlockDuration((StepEndNode)node.content) / 1000.0f;
            str.append(String.format(" (took %.2f s)", dur));
        }
        for (Action act : node.content.getAllActions() ) {
            if (act instanceof TimingAction || act instanceof LabelAction) {
                // these are handled in stringize_inner
                continue;
            }
            str.append(
                String.format("\n%s    +%s", indent, act.getClass().getSimpleName())
            );
        }
        return String.format("%s%s", indent, str.toString());
    }

    String stringize_inner(StepStartNode node) {
        StringBuilder result = new StringBuilder(
            String.format("{{{ #%s (from %s)", node.getId(), node.getEnclosingId())
        );
        LabelAction lbl = node.getAction(LabelAction.class);
        if (lbl != null) {
            result.append(' ');
            result.append(lbl.getDisplayName());
        }
        result.append(String.format(" @ %s", getTime(node)));
        return result.toString();
    }
    String stringize_inner(StepEndNode node) {
        return String.format("}}} %s", node.getStartNode().getId());
    }
    String stringize_inner(FlowNode node) {
        return String.format(
            "--- #%s %s @ %s",
            node.getId(),
            node.getClass().getSimpleName(),
            getTime(node)
        );
    }
}
