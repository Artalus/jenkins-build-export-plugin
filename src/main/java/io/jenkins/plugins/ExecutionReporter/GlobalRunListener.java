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
import org.jenkinsci.plugins.workflow.support.actions.LogStorageAction;

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
        // TODO: this is slow and stupid
        for (NodeData node : tree_data) {
            if (node.parent == 0)
                continue;
            NodeData pt = tree_data.stream()
                .filter(x -> x.id == node.parent)
                .findAny()
                .orElse(null);
            if (pt == null) {
                logger.warning(
                    String.format("Could not find parent '%s' for node '%s'", node.parent, node.id)
                );
                continue;
            }
            pt.children.add(node);
            node.parent_node = pt;
        }
        Collections.reverse(tree_data);

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
    class NodeData {
        public Long id;
        public Long parent;
        public int depth;
        public ArrayList<NodeData> children;
        public NodeData parent_node;
        public FlowNode content;
        public NodeData(FlowNode node) {
            this.id = intize(node.getId());
            this.parent = intize(node.getEnclosingId());
            this.parent_node = null;
            this.depth = node.getAllEnclosingIds().size();
            this.content = node;
            this.children = new ArrayList<NodeData>();
        }

        private Long intize(String s) {
            return s == null ? 0 : Long.valueOf(s);
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
    static long calcInterval(FlowNode start, FlowNode end) {
        return getTs(end) - getTs(start);
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
            if (act instanceof WorkspaceAction ) {
                String name = ((WorkspaceAction)act).getNode();
                if (name == "")
                    name = "master";
                str.append(String.format(" - %s", name));
                continue;
            }
            if (act instanceof BodyInvocationAction && node.content instanceof StepStartNode) {
                FlowNode pt = node.parent_node.content;
                WorkspaceAction ws = pt.getAction(WorkspaceAction.class);
                if (ws != null) {
                    str.append(String.format(" - node acquired after %.2f s",
                        calcInterval(pt, node.content) / 1000.0f
                    ));
                }
                continue;
            }
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
            "--> #%s %s @ %s",
            node.getId(),
            node.getClass().getSimpleName(),
            getTime(node)
        );
    }
}
