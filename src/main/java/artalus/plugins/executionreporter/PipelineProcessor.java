package artalus.plugins.executionreporter;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.flow.FlowExecution;
import org.jenkinsci.plugins.workflow.graphanalysis.DepthFirstScanner;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

public class PipelineProcessor {
    private static final Logger logger = Logger.getLogger(PipelineProcessor.class.getName());

    public static void doMagic(WorkflowRun b) {
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
        // TODO: need postUrl emptiness check
        pd.nodes = NodeData.transform(nodes);
        Poster.post(
            pd,
            ExecutionReporterGlobalConfig.get().getPostUrl()
        );
    }
}
