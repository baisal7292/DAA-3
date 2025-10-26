

import java.util.*;

public class UnionFind {
    private final Map<String,String> parent = new HashMap<>();
    private final Map<String,Integer> rank = new HashMap<>();
    private final OperationCounter ops;

    public UnionFind(Collection<String> nodes, OperationCounter ops) {
        this.ops = ops;
        for (String n : nodes) {
            parent.put(n, n);
            rank.put(n, 0);
        }
    }

    public String find(String x) {
        ops.inc("find_calls");
        String p = parent.get(x);
        if (!p.equals(x)) {
            String root = find(p);
            parent.put(x, root);
            ops.inc("path_compressions");
            return root;
        }
        return p;
    }

    public boolean union(String a, String b) {
        ops.inc("union_calls");
        String ra = find(a);
        String rb = find(b);
        ops.inc("comparisons");
        if (ra.equals(rb)) { ops.inc("unions_skipped"); return false; }
        if (rank.get(ra) < rank.get(rb)) {
            parent.put(ra, rb);
        } else if (rank.get(ra) > rank.get(rb)) {
            parent.put(rb, ra);
        } else {
            parent.put(rb, ra);
            rank.put(ra, rank.get(ra) + 1);
        }
        ops.inc("unions_done");
        return true;
    }
}
