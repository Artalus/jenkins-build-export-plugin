package io.jenkins.plugins.executionreporter;

import java.util.*;
import org.jenkinsci.plugins.workflow.graph.FlowNode;

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
