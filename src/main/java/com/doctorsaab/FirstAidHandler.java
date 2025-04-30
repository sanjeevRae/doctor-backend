import java.util.Map;
import java.util.HashMap;
import java.io.Reader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

public class FirstAidHandler {
  // Store first aid instructions in a static map
  private static final Map<String, String> firstAidInstructions = new HashMap<>();
  
  static {
    try {
      // Load data from the JSON file using GSON
      String filePath = Paths.get("backend", "first_aid_data.json").toString();
      Reader reader = new FileReader(filePath);
      Gson gson = new Gson();
      Type type = new TypeToken<Map<String, String>>(){}.getType();
      Map<String, String> loadedInstructions = gson.fromJson(reader, type);
      firstAidInstructions.putAll(loadedInstructions);
      reader.close();
      System.out.println("Successfully loaded " + firstAidInstructions.size() + " first aid instructions.");
    } catch (IOException e) {
      System.err.println("Error loading first aid data: " + e.getMessage());
      // Initialize with minimal fallback instructions in case file can't be loaded
      firstAidInstructions.put("emergency", "Call emergency services immediately for urgent medical situations.");
    }
  }

  public static String getAdvice(String condition) {
    // Convert to lowercase and trim for case-insensitive matching
    String normalizedCondition = condition.toLowerCase().trim();
    
    // Return instructions if found, otherwise return default message
    return firstAidInstructions.getOrDefault(normalizedCondition, 
        "First aid instructions for '" + condition + "' not found. Please consult a medical professional or call emergency services if this is urgent.");
  }
}
