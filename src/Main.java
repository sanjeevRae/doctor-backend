package com.doctorsaab;
import static spark.Spark.*;
import com.google.gson.*;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class Main {
  // Storage for WebRTC signaling
  private static final Map<String, String> offers = new ConcurrentHashMap<>();
  private static final Map<String, String> answers = new ConcurrentHashMap<>();
  private static final Map<String, JsonArray> iceCandidates = new ConcurrentHashMap<>();

  public static void main(String[] args) {
    port(8080);
    
    // Enable CORS
    options("/*", (req, res) -> {
      String accessControlRequestHeaders = req.headers("Access-Control-Request-Headers");
      if (accessControlRequestHeaders != null) {
        res.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
      }

      String accessControlRequestMethod = req.headers("Access-Control-Request-Method");
      if (accessControlRequestMethod != null) {
        res.header("Access-Control-Allow-Methods", accessControlRequestMethod);
      }

      return "OK";
    });

    before((req, res) -> {
      res.header("Access-Control-Allow-Origin", "*");
      res.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
      res.header("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With, Accept");
      res.type("application/json");
    });

    // Symptom Check API
    post("/symptom-check", (req, res) -> {
      JsonObject body = JsonParser.parseString(req.body()).getAsJsonObject();
      String symptoms = body.get("symptoms").getAsString();

      String diagnosis = SymptomChecker.getDiagnosis(symptoms);
      res.type("application/json");
      return new Gson().toJson(Map.of("diagnosis", diagnosis));
    });

    // First-Aid Instructions API
    get("/first-aid", (req, res) -> {
      String condition = req.queryParams("condition");
      if (condition == null || condition.isEmpty()) {
        res.status(400);
        return new Gson().toJson(Map.of("error", "Condition parameter is required"));
      }

      String advice = FirstAidHandler.getAdvice(condition);
      return new Gson().toJson(Map.of("instructions", advice));
    });

    // WebRTC Signaling Routes
    
    // Store an offer
    post("/webrtc/offer", (req, res) -> {
      JsonObject body = JsonParser.parseString(req.body()).getAsJsonObject();
      String roomId = body.get("roomId").getAsString();
      String offer = body.get("offer").getAsString();
      
      offers.put(roomId, offer);
      // Initialize ICE candidates array for this room
      iceCandidates.put(roomId, new JsonArray());
      
      return new Gson().toJson(Map.of("success", true));
    });
    
    // Get an offer
    get("/webrtc/offer/:roomId", (req, res) -> {
      String roomId = req.params("roomId");
      String offer = offers.get(roomId);
      
      if (offer == null) {
        res.status(404);
        return new Gson().toJson(Map.of("error", "No offer found for room: " + roomId));
      }
      
      return new Gson().toJson(Map.of("offer", offer));
    });
    
    // Store an answer
    post("/webrtc/answer", (req, res) -> {
      JsonObject body = JsonParser.parseString(req.body()).getAsJsonObject();
      String roomId = body.get("roomId").getAsString();
      String answer = body.get("answer").getAsString();
      
      answers.put(roomId, answer);
      
      return new Gson().toJson(Map.of("success", true));
    });
    
    // Get an answer
    get("/webrtc/answer/:roomId", (req, res) -> {
      String roomId = req.params("roomId");
      String answer = answers.get(roomId);
      
      if (answer == null) {
        res.status(404);
        return new Gson().toJson(Map.of("error", "No answer found for room: " + roomId));
      }
      
      return new Gson().toJson(Map.of("answer", answer));
    });
    
    // Add ICE candidate
    post("/webrtc/ice-candidate", (req, res) -> {
      JsonObject body = JsonParser.parseString(req.body()).getAsJsonObject();
      String roomId = body.get("roomId").getAsString();
      JsonObject candidate = body.get("candidate").getAsJsonObject();
      
      JsonArray candidates = iceCandidates.get(roomId);
      if (candidates == null) {
        candidates = new JsonArray();
        iceCandidates.put(roomId, candidates);
      }
      
      candidates.add(candidate);
      
      return new Gson().toJson(Map.of("success", true));
    });
    
    // Get ICE candidates
    get("/webrtc/ice-candidates/:roomId", (req, res) -> {
      String roomId = req.params("roomId");
      JsonArray candidates = iceCandidates.get(roomId);
      
      if (candidates == null) {
        candidates = new JsonArray();
      }
      
      return new Gson().toJson(Map.of("candidates", candidates));
    });

    // Health check endpoint
    get("/health", (req, res) -> {
      return new Gson().toJson(Map.of("status", "UP", "message", "Doctor-Sab API is running"));
    });
  }
}
