package artalus.plugins.executionreporter;

import java.util.*;

import hudson.model.Action;
import org.jenkinsci.plugins.workflow.graph.*;
import org.jenkinsci.plugins.workflow.cps.nodes.*;
import org.jenkinsci.plugins.workflow.actions.*;
import org.jenkinsci.plugins.workflow.actions.WorkspaceAction;
import org.jenkinsci.plugins.workflow.actions.ArgumentsAction;

// this class is exported as json to the outside world, its fields are allowed to be unread
@edu.umd.cs.findbugs.annotations.SuppressWarnings("URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
class NodeData {
    enum NodeType {
        Unknown,
        FlowStart,
        FlowEnd,
        StepAtom,
        StepStart,
        StepEnd,
    }

    public String id;
    public long timestamp;
    public String enclosing;
    public NodeType type;
    public Integer depth;
    public List<String> parents;
    public List<String> enclosings;
    public String startNode;
    public List<ActionData> actions;

    private NodeData(FlowNode node) {
        this.id = node.getId();
        this.timestamp = TimingAction.getStartTime(node);
        this.enclosing = node.getEnclosingId();
        this.type = parseType(node);
        this.depth = node.getAllEnclosingIds().size();
        List<FlowNode> parents = node.getParents();
        this.parents = new ArrayList<String>(parents.size());
        for (FlowNode p : parents) {
            this.parents.add(p.getId());
        }
        this.enclosings = node.getAllEnclosingIds();
        this.actions = new ArrayList<ActionData>();

        if (node instanceof FlowEndNode || node instanceof StepEndNode) {
            this.startNode = ((BlockEndNode)node).getStartNode().getId();
        }

        for (Action a : node.getAllActions() ) {
            if (a instanceof TimingAction)
                continue;
            if (a instanceof LogAction)
                continue;
            actions.add(new ActionData(a));
        }
    }

    public static ArrayList<NodeData> transform(ArrayList<FlowNode> flow) {
        ArrayList<NodeData> result = new ArrayList<NodeData>(flow.size());
        for (FlowNode f : flow) {
            NodeData n = new NodeData(f);
            result.add(n);
        }
        return result;
    }

    static NodeType parseType(FlowNode n) {
        if (n instanceof FlowStartNode)
            return NodeType.FlowStart;
        if (n instanceof FlowEndNode)
            return NodeType.FlowEnd;
        if (n instanceof StepAtomNode)
            return NodeType.StepAtom;
        if (n instanceof StepStartNode)
            return NodeType.StepStart;
        if (n instanceof StepEndNode)
            return NodeType.StepEnd;
        return NodeType.Unknown;
    }
}

// this class is exported as json to the outside world, its fields are allowed to be unread
@edu.umd.cs.findbugs.annotations.SuppressWarnings("URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
class ActionData {
    public String type;
    public String fullType;
    public Object data;

    public ActionData(Action a) {
        this.type = a.getClass().getSimpleName();
        this.fullType = a.getClass().getCanonicalName();
        fillData(a);
    }

    void fillData(Action a) {
        if (a instanceof WorkspaceAction) {
            String name = ((WorkspaceAction)a).getNode();
            if (name.equals(""))
                name = "master";
            Map<String, Object> m = new HashMap<String, Object>();
            m.put("node", name);
            this.data = m;
            return;
        }
        if (a instanceof ArgumentsAction) {
            // TODO: seems that secrets do not leak, but make sure
            this.data = ((ArgumentsAction)a).getArguments();
            return;
        }
        if (a instanceof LabelAction) {
            this.data = ((LabelAction)a).getDisplayName();
            return;
        }
        if (a instanceof ErrorAction) {
            // Sending more details about ErrorAction could expose secrets in unpredictable way, e.g.
            // pipeline failing in `echo params.PASSWORD` results in such `.getError().getMessage()`:
            //     Could not instantiate {message=SuperSecretText} for o.j.p.w.steps.EchoStep
            // , which is clearly a leak
            this.data = ((ErrorAction)a).getError().getClass().toString();
            return;
        }
    }
}
