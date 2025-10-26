

import java.util.*;


public class Graph {
    private final Map<String, List<Neighbor>> adj = new LinkedHashMap<>();
    private final List<Edge> edges = new ArrayList<>();

    public static class Neighbor {
        public final String node;
        public final double weight;
        public Neighbor(String node, double weight) { this.node = node; this.weight = weight; }
    }

    public Graph() {}

    public Graph(Collection<String> vertices) {
        for (String v : vertices) adj.putIfAbsent(v, new ArrayList<>());
    }

    public void addVertex(String v) {
        adj.putIfAbsent(v, new ArrayList<>());
    }

    public void addEdge(String u, String v, double w) {
        addVertex(u);
        addVertex(v);
        adj.get(u).add(new Neighbor(v, w));
        adj.get(v).add(new Neighbor(u, w));
        edges.add(new Edge(u, v, w));
    }

    public Set<String> vertices() { return adj.keySet(); }
    public Collection<List<Neighbor>> adjacencyValues() { return adj.values(); }
    public List<Neighbor> neighbors(String v) { return adj.getOrDefault(v, Collections.emptyList()); }
    public List<Edge> getEdges() { return Collections.unmodifiableList(edges); }
    public int numVertices() { return adj.size(); }
    public int numEdges() { return edges.size(); }

    public boolean containsVertex(String v) { return adj.containsKey(v); }
}
