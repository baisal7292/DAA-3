import java.util.*;

public class KruskalMST {

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

        List<Edge> edges = new ArrayList<>(g.getEdges());
        // sort edges by weight
        edges.sort(Comparator.comparingDouble(e -> e.w));
        ops.inc("sort_calls");

        UnionFind uf = new UnionFind(g.vertices(), ops);
        List<Edge> mst = new ArrayList<>();
        double total = 0.0;
        for (Edge e : edges) {
            ops.inc("edge_considered");
            ops.inc("comparisons");
            if (!uf.find(e.u).equals(uf.find(e.v))) {
                boolean merged = uf.union(e.u, e.v);
                ops.inc("comparisons");
                if (merged) {
                    mst.add(e);
                    total += e.w;
                    ops.inc("mst_edges_added");
                }
            } else {
                ops.inc("skipped_cycle");
            }
            if (mst.size() == g.numVertices() - 1) break;
        }

        long t1 = System.nanoTime();
        long ms = (t1 - t0) / 1_000_000;
        return new Result(mst, total, ops, ms);
    }
}
