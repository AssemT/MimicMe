package recognizer;
import java.util.*;

public class Dictionary {
    private final StringBuilder dictionary = new StringBuilder();
    private final Set<String> words = new HashSet<String>();
    private static final String newLine = System.getProperty("line.separator");

    public void fill(String key, String value) {
        if (!words.contains(key)) {
            dictionary.append(key);
            dictionary.append("  ");
            dictionary.append(value);
            dictionary.append(newLine);
            words.add(key);
        }
    }

    public String toStr(){
        return dictionary.toString();
    }
}
