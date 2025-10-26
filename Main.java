import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;


public class Main {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void main(String[] args) throws Exception {
        System.out.println("Current working dir: " + System.getProperty("user.dir"));

        String inputPath = "input.json";
        String outputPath = "output.json";
        String summaryPath = "summary.csv";
        if (args.length >= 1) inputPath = args[0];
        if (args.length >= 2) outputPath = args[1];
        if (args.length >= 3) summaryPath = args[2];

        List<JsonObject> specs = loadInputSpec(inputPath);
        List<JsonObject> results = new ArrayList<>();
        List<Map<String,String>> csvRows = new ArrayList<>();

        for (JsonObject spec : specs) {
            String name = spec.has("name") ? spec.get("name").getAsString() : "graph";
            Graph g = graphFromSpec(spec);
            System.out.printf("Running graph: %s (V=%d, E=%d)%n", name, g.numVertices(), g.numEdges());

            PrimMST.Result p = PrimMST.run(g);
            KruskalMST.Result k = KruskalMST.run(g);

            JsonObject jP = new JsonObject();
            jP.addProperty("graph_name", name);
            jP.addProperty("algorithm", "Prim");
            jP.addProperty("n_vertices", g.numVertices());
            jP.addProperty("n_edges", g.numEdges());
            jP.addProperty("total_cost", p.totalCost);
            jP.add("edges", GSON.toJsonTree(p.mstEdges.stream().map(e -> Arrays.asList(e.u,e.v,e.w)).collect(Collectors.toList())));
            jP.add("ops", GSON.toJsonTree(p.ops.asMap()));
            jP.addProperty("time_ms", p.timeMs);
            results.add(jP);

            JsonObject jK = new JsonObject();
            jK.addProperty("graph_name", name);
            jK.addProperty("algorithm", "Kruskal");
            jK.addProperty("n_vertices", g.numVertices());
            jK.addProperty("n_edges", g.numEdges());
            jK.addProperty("total_cost", k.totalCost);
            jK.add("edges", GSON.toJsonTree(k.mstEdges.stream().map(e -> Arrays.asList(e.u,e.v,e.w)).collect(Collectors.toList())));
            jK.add("ops", GSON.toJsonTree(k.ops.asMap()));
            jK.addProperty("time_ms", k.timeMs);
            results.add(jK);

            csvRows.add(csvRow(name, "Prim", g.numVertices(), g.numEdges(), p.totalCost, p.timeMs, p.ops.asMap().toString()));
            csvRows.add(csvRow(name, "Kruskal", g.numVertices(), g.numEdges(), k.totalCost, k.timeMs, k.ops.asMap().toString()));

            if (Math.abs(p.totalCost - k.totalCost) > 1e-6) {
                System.err.printf("Warning: MST total cost differs for graph %s: Prim=%.3f Kruskal=%.3f%n", name, p.totalCost, k.totalCost);
            }
        }

        JsonObject out = new JsonObject();
        out.add("results", GSON.toJsonTree(results));
        Files.write(Paths.get(outputPath), GSON.toJson(out).getBytes());
        System.out.println("Wrote " + outputPath);


        writeCsv(summaryPath, csvRows);
        System.out.println("Wrote " + summaryPath);
    }

    private static Map<String,String> csvRow(String graph, String alg, int nv, int ne, double total, long ms, String ops) {
        Map<String,String> m = new LinkedHashMap<>();
        m.put("graph", graph);
        m.put("algorithm", alg);
        m.put("n_vertices", String.valueOf(nv));
        m.put("n_edges", String.valueOf(ne));
        m.put("mst_total_cost", String.valueOf(total));
        m.put("time_ms", String.valueOf(ms));
        m.put("ops", ops);
        return m;
    }

    private static void writeCsv(String path, List<Map<String,String>> rows) throws IOException {
        if (rows.isEmpty()) {
            Files.write(Paths.get(path), new byte[0]);
            return;
        }
        List<String> headers = new ArrayList<>(rows.get(0).keySet());
        try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(path))) {
            bw.write(String.join(",", headers));
            bw.newLine();
            for (Map<String,String> r : rows) {
                List<String> vals = headers.stream().map(h -> escapeCsv(r.get(h))).collect(Collectors.toList());
                bw.write(String.join(",", vals));
                bw.newLine();
            }
        }
    }

    private static String escapeCsv(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    private static List<JsonObject> loadInputSpec(String path) throws IOException {
        String txt = new String(Files.readAllBytes(Paths.get(path)));
        JsonObject root = JsonParser.parseString(txt).getAsJsonObject();
        JsonArray arr = root.getAsJsonArray("graphs");
        List<JsonObject> out = new ArrayList<>();
        for (JsonElement e : arr) out.add(e.getAsJsonObject());
        return out;
    }

    private static Graph graphFromSpec(JsonObject spec) {
        Graph g;
        if (spec.has("random")) {
            JsonObject r = spec.getAsJsonObject("random");
            int n = r.get("n").getAsInt();
            double density = r.has("density") ? r.get("density").getAsDouble() : 0.2;
            long seed = r.has("seed") ? r.get("seed").getAsLong() : new Random().nextLong();
            g = generateRandomGraph(n, density, seed);
        } else {
            JsonArray verts = spec.has("vertices") ? spec.getAsJsonArray("vertices") : null;
            if (verts != null) {
                List<String> vlist = new ArrayList<>();
                for (JsonElement ve : verts) vlist.add(ve.getAsString());
                g = new Graph(vlist);
            } else {
                g = new Graph();
            }
            JsonArray edges = spec.has("edges") ? spec.getAsJsonArray("edges") : null;
            if (edges != null) {
                for (JsonElement ee : edges) {
                    JsonArray ar = ee.getAsJsonArray();
                    String u = ar.get(0).getAsString();
                    String v = ar.get(1).getAsString();
                    double w = ar.get(2).getAsDouble();
                    g.addEdge(u, v, w);
                }
            }
        }
        return g;
    }

    private static Graph generateRandomGraph(int n, double density, long seed) {
        Random rnd = new Random(seed);
        List<String> verts = new ArrayList<>();
        for (int i=0;i<n;i++) verts.add("v"+i);
        Graph g = new Graph(verts);

        List<String> nodes = new ArrayList<>(verts);
        Collections.shuffle(nodes, rnd);
        for (int i=1;i<n;i++) {
            String u = nodes.get(i);
            String v = nodes.get(rnd.nextInt(i));
            int w = 1 + rnd.nextInt(100);
            g.addEdge(u, v, w);
        }

        int maxEdges = n*(n-1)/2;
        int target = Math.max(1, (int)(maxEdges * density));
        Set<String> existing = new HashSet<>();
        for (Edge e : g.getEdges()) existing.add(edgeKey(e.u, e.v));
        List<String[]> pairs = new ArrayList<>();
        for (int i=0;i<n;i++) for (int j=i+1;j<n;j++) pairs.add(new String[]{verts.get(i), verts.get(j)});
        Collections.shuffle(pairs, rnd);
        for (String[] p : pairs) {
            if (g.numEdges() >= target) break;
            String k = edgeKey(p[0], p[1]);
            if (existing.contains(k)) continue;
            int w = 1 + rnd.nextInt(100);
            g.addEdge(p[0], p[1], w);
            existing.add(k);
        }
        return g;
    }

    private static String edgeKey(String a, String b) {
        return a.compareTo(b) < 0 ? a + "|" + b : b + "|" + a;
    }
}
