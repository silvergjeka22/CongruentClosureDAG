import org.graphstream.graph.*;
import org.graphstream.graph.implementations.*;
import java.util.*;

import CCAlgorithm.CongruentClosureAlgorithm;
import CCAlgorithm.bean.Node;

public class GraphVisualization {
    public static void createAndDisplayGraph() {
        // Set UI property to use Swing for visualization
        System.setProperty("org.graphstream.ui", "swing");

        // Create a graph object
        Graph graph = new SingleGraph("DAG Visualization");

        // Enable auto-layout (optional)
        graph.setAutoCreate(true);
        graph.setStrict(false);

        // Retrieve the DAG from CongruentClosureAlgorithm
        Map<String, Node> dag = CongruentClosureAlgorithm.getDag();

        //System.out.println("DAG: " + dag);

        // Variables to control spacing
        int horizontalSpacing = 60; // Horizontal distance between nodes
        int verticalSpacing = 80; // Vertical distance between levels

        // Step 1: Determine levels of each node
        Map<String, Integer> levels = new HashMap<>();
        for (String nodeId : dag.keySet()) {
            levels.put(nodeId, -1); // Initialize all levels to -1
        }

        // Assign levels: Leaf nodes (without parents) are at level 0
        for (Map.Entry<String, Node> entry : dag.entrySet()) {
            String nodeId = entry.getKey();
            Node node = entry.getValue();

            if (node.getParents().isEmpty()) {
                levels.put(nodeId, 0); // Leaf nodes at level 0
            }
        }

        // Recursively assign levels for parent nodes
        for (int i = 1; i <= dag.size(); i++) { // Iterative refinement
            for (Map.Entry<String, Node> entry : dag.entrySet()) {
                String nodeId = entry.getKey();
                Node node = entry.getValue();

                int maxChildLevel = 0;
                for (String parent : node.getParents()) {
                    maxChildLevel = Math.max(maxChildLevel, levels.getOrDefault(parent, 0));
                }

                levels.put(nodeId, maxChildLevel + 1); // Parent one level above its children
            }
        }

        // Step 2: Group nodes by level for positioning
        Map<Integer, List<String>> nodesByLevel = new HashMap<>();
        for (Map.Entry<String, Integer> entry : levels.entrySet()) {
            int level = entry.getValue();
            String nodeId = entry.getKey();
            nodesByLevel.computeIfAbsent(level, k -> new ArrayList<>()).add(nodeId);
        }

        // Step 3: Position nodes in a tree layout
        Map<String, Integer> nodeXPositions = new HashMap<>();
        for (Map.Entry<Integer, List<String>> entry : nodesByLevel.entrySet()) {
            int level = entry.getKey();
            List<String> nodes = entry.getValue();

            // Distribute nodes horizontally at this level
            int xPosition = 0;
            for (String nodeId : nodes) {
                nodeXPositions.put(nodeId, xPosition);
                if (graph.getNode(nodeId) == null) {
                    graph.addNode(nodeId).setAttribute("ui.label", nodeId);
                }
                graph.getNode(nodeId).setAttribute("xyz", xPosition * horizontalSpacing, -level * verticalSpacing, 0);
                xPosition++;
            }
        }

        // Step 4: Create edges
        for (Map.Entry<String, Node> entry : dag.entrySet()) {
            String nodeId = entry.getKey();
            Node node = entry.getValue();

            for (String parent : node.getParents()) {
                String edgeId = parent + "-" + nodeId;
                if (graph.getEdge(edgeId) == null) {
                    graph.addEdge(edgeId, parent, nodeId, true); // Directed edge
                }
            }
        }

        // Step 5: Style the graph
        graph.setAttribute("ui.stylesheet",
                "graph { padding: 50px; } " +
                        "node { size: 40px; shape: circle; text-alignment: center; text-size: 20px; fill-color: lightblue; } "
                        +
                        "edge { fill-color: gray; size: 2px; }");

        // Display the graph
        graph.display();
    }
}
