import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TestMST {

    @Test
    public void testSmallTriangleEqualCost() {
        Graph g = new Graph();
        g.addEdge("A","B",1);
        g.addEdge("B","C",2);
        g.addEdge("A","C",3);

        PrimMST.Result p = PrimMST.run(g);
        KruskalMST.Result k = KruskalMST.run(g);
        assertEquals(p.totalCost, k.totalCost, 1e-9);
        assertEquals(g.numVertices() - 1, p.mstEdges.size());
        assertEquals(g.numVertices() - 1, k.mstEdges.size());
    }

    @Test
    public void testDisconnectedHandled() {
        Graph g = new Graph();
        g.addEdge("A","B",1);
        g.addEdge("C","D",2);
        PrimMST.Result p = PrimMST.run(g);
        KruskalMST.Result k = KruskalMST.run(g);
        assertTrue(p.mstEdges.size() < g.numVertices() - 1 || k.mstEdges.size() < g.numVertices() - 1);
    }

    @Test
    public void testOperationsNonNegative() {
        Graph g = new Graph();
        g.addEdge("A","B",1);
        g.addEdge("B","C",2);
        PrimMST.Result p = PrimMST.run(g);
        KruskalMST.Result k = KruskalMST.run(g);
        assertTrue(p.timeMs >= 0);
        assertTrue(k.timeMs >= 0);
        p.ops.asMap().values().forEach(v -> assertTrue(v >= 0));
        k.ops.asMap().values().forEach(v -> assertTrue(v >= 0));
    }
}
