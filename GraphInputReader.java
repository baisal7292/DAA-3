import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.List;

class EdgeInput {
    int source;
    int destination;
    int weight;
}

class GraphInput {
    String name;
    int vertices;
    List<EdgeInput> edges;
}

class InputData {
    List<GraphInput> graphs;
}

public class GraphInputReader {
    public static InputData readGraphFromJson(String filePath) {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(filePath)) {
            return gson.fromJson(reader, InputData.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
