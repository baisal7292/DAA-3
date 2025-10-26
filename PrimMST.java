import java.util.*;

 class PrimMST {

    public static class Result {
        public final List<Edge> mstEdges;
        public final double totalCost;
        public final OperationCounter ops;
        public final long timeMs;
        public Result(List<Edge> edges, double cost, OperationCounter ops, long timeMs) {
            this.mstEdges = edges; this.totalCost = cost; this.ops = ops; this.timeMs = timeMs;
        }
    }

    public static Result run(Graph g) {
        long t0 = System.nanoTime();
        OperationCounter ops = new OperationCounter();
        List<Edge> mst = new ArrayList<>();
        int V = g.numVertices();
        if (V == 0) return new Result(mst, 0.0, ops, 0);

        Iterator<String> it = g.vertices().iterator();
        String start = it.next();

        Set<String> visited = new HashSet<>();
        visited.add(start);
        ops.inc("visited_marks");

        class Item implements Comparable<Item> {
            final double w; final String from; final String to;
            Item(double w, String from, String to) { this.w = w; this.from = from; this.to = to; }
            public int compareTo(Item o) { return Double.compare(this.w, o.w); }
        }
        PriorityQueue<Item> heap = new PriorityQueue<>();
        for (Graph.Neighbor nb : g.neighbors(start)) {
            heap.add(new Item(nb.weight, start, nb.node));
            ops.inc("heap_push");
            ops.inc("edge_relaxation_attempts");
        }

        double total = 0.0;
        while (!heap.isEmpty() && mst.size() < V-1) {
            ops.inc("heap_pop");
            Item it2 = heap.poll();
            ops.inc("comparisons");
            if (visited.contains(it2.to)) { ops.inc("skipped_visited"); continue; }
            visited.add(it2.to);
            ops.inc("visited_marks");
            mst.add(new Edge(it2.from, it2.to, it2.w));
            total += it2.w;
            ops.inc("mst_edges_added");
            for (Graph.Neighbor nb : g.neighbors(it2.to)) {
                ops.inc("edge_relaxation_attempts");
                ops.inc("comparisons");
                if (!visited.contains(nb.node)) {
                    heap.add(new Item(nb.weight, it2.to, nb.node));
                    ops.inc("heap_push");
                }
            }
        }
        long t1 = System.nanoTime();
        long ms = (t1 - t0) / 1_000_000;
        return new Result(mst, total, ops, ms);
    }
}
