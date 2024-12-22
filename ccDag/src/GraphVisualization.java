import org.graphstream.graph.*;
import org.graphstream.graph.implementations.*;

public class GraphVisualization {
    public static void createAndDisplayGraph() {
        // Set UI property to use Swing for visualization
        System.setProperty("org.graphstream.ui", "swing");

        // Create a graph object
        Graph graph = new SingleGraph("DAG Visualization");

        // Enable auto-layout (optional)
        graph.setAttribute("ui.stylesheet", "url('style.css')");
        graph.setAutoCreate(true);
        graph.setStrict(false);

        // Add nodes
        graph.addNode("A").setAttribute("ui.label", "A");
        graph.addNode("B").setAttribute("ui.label", "B");
        graph.addNode("C").setAttribute("ui.label", "C");
        graph.addNode("D").setAttribute("ui.label", "D");

        // Add edges (directed)
        graph.addEdge("AB", "A", "B", true);
        graph.addEdge("AC", "A", "C", true);
        graph.addEdge("BD", "B", "D", true);
        graph.addEdge("CD", "C", "D", true);

        // Display the graph
        graph.display();
    }
}
