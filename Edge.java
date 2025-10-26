public class Edge {
    public final String u;
    public final String v;
    public final double w;

    public Edge(String u, String v, double w) {
        this.u = u;
        this.v = v;
        this.w = w;
    }

    public String other(String node) {
        if (node.equals(u)) return v;
        if (node.equals(v)) return u;
        return null;
    }

    @Override
    public String toString() {
        return String.format("(%s-%s:%.2f)", u, v, w);
    }
}
