import org.graphstream.graph.*;
import org.graphstream.graph.implementations.*;

public class GraphVisualization {
    public static void createAndDisplayGraph() {
        // Set UI property to use Swing for visualization
        System.setProperty("org.graphstream.ui", "swing");

        // Create a graph object
        Graph graph = new SingleGraph("DAG Visualization");

        // Enable auto-layout (optional)
        graph.setAutoCreate(true);
        graph.setStrict(false);

        // Add nodes with labels
        graph.addNode("A").setAttribute("ui.label", "A");
        graph.addNode("B").setAttribute("ui.label", "B");
        graph.addNode("C").setAttribute("ui.label", "C");
        graph.addNode("D").setAttribute("ui.label", "D");

        // Add directed edges between the nodes
        graph.addEdge("AB", "A", "B", true);  // Edge from A to B
        graph.addEdge("AC", "A", "C", true);  // Edge from A to C
        graph.addEdge("BD", "B", "D", true);  // Edge from B to D
        graph.addEdge("CD", "C", "D", true);  // Edge from C to D

        // Set node size and text size using ui.style (CSS-like)
        graph.setAttribute("ui.stylesheet", "node { size: 50px; shape: circle; text-alignment: center; text-size: 30px; fill-color: lightblue; }");

        // Set edge style (use forEach() for Stream)
        graph.edges().forEach(edge -> {
            edge.setAttribute("ui.style", "fill-color: gray; size: 3px;");  // Style edges (thicker lines)
        });

        // Display the graph
        graph.display();
    }
}
