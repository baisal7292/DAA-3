import java.util.LinkedHashMap;
import java.util.Map;


public class OperationCounter {
    private final Map<String, Long> m = new LinkedHashMap<>();

    public void inc(String key) { inc(key, 1); }
    public void inc(String key, long delta) { m.put(key, m.getOrDefault(key, 0L) + delta); }
    public long get(String key) { return m.getOrDefault(key, 0L); }
    public Map<String, Long> asMap() { return new LinkedHashMap<>(m); }
    @Override public String toString() { return asMap().toString(); }
}
