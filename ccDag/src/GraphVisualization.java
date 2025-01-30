import org.graphstream.graph.*;
import org.graphstream.graph.implementations.*;
import java.util.*;
import CCAlgorithm.CongruentClosureAlgorithm;
import CCAlgorithm.base.Node;

public class GraphVisualization {

    public static void createAndDisplayGraph() {
        // Set UI property to use Swing for visualization
        System.setProperty("org.graphstream.ui", "swing");

        // Create a graph object
        Graph graph = new SingleGraph("DAG Visualization");

        // Retrieve the DAG from CongruentClosureAlgorithm
        Map<String, Node> dag = CongruentClosureAlgorithm.getDag();

        // Process all nodes in the DAG
        Map<String, String> finalRepresentatives = new HashMap<>();
        for (String id : dag.keySet()) {
            try {
                String representative = CongruentClosureAlgorithm.find(id);
                finalRepresentatives.put(id, representative);
            } catch (Exception e) {
                System.err.println("Error finding representative for node " + id + ": " + e.getMessage());
            }
        }

        // Set the stylesheet for the graph
        graph.setAttribute("ui.stylesheet",
                "graph { padding: 50px; } " +
                "node { size: 30px; shape: circle; text-alignment: center; text-size: 15px; fill-color: lightblue; } " +
                "edge { fill-color: gray; size: 2px; }");

        // Add nodes and edges to the graph
        for (Node node : dag.values()) {
            try {
                String nodeId = CongruentClosureAlgorithm.find(node.getId());
                // Add the node to the graph if not already added
                if (graph.getNode(nodeId) == null) {
                    org.graphstream.graph.Node graphNode = graph.addNode(nodeId);
                    graphNode.setAttribute("ui.label", nodeId); // Set label to nodeId
                }

                // Add edges for all parents
                for (String parentId : node.getParents()) {
                    String parentRep = CongruentClosureAlgorithm.find(parentId);
                    if (graph.getNode(parentRep) == null) {
                        org.graphstream.graph.Node parentNode = graph.addNode(parentRep);
                        parentNode.setAttribute("ui.label", parentRep); // Set label for parent node
                    }

                    String edgeId = parentRep + "->" + nodeId;
                    if (graph.getEdge(edgeId) == null) {
                        graph.addEdge(edgeId, parentRep, nodeId, true);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error processing node " + node.getId() + ": " + e.getMessage());
            }
        }

        // Display the graph
        graph.display();
    }
}
