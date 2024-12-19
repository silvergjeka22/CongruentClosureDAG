package CCAlgo;

import CCAlgo.base.*;
import CCAlgorithm.bean.Node;

import java.util.*;

public class Main {

    public static void main(String[] args) {
        // Create a graph given in the above diagram
        int V = 5;
        Graph graph = new Graph(V);
        graph.addEdge(1, 0);
        graph.addEdge(0, 2);
        graph.addEdge(2, 1);
        graph.addEdge(0, 3);
        graph.addEdge(3, 4);

        System.out.println("Following are connected components");
        graph.connectedComponents();
    }
}
