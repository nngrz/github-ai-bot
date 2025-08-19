package com.githubai.bot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class AppController {

    private static final ResponseEntity<Map<String, String>> responseOK =
        ResponseEntity.ok(Map.of("status", "ok"));

    @GetMapping("/")
    public String index() {
        return "ok";
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return responseOK;
    }

    @Value("${build.version}")
    private String projectVersion;

    @GetMapping("/version")
    public ResponseEntity<Map<String, String>> getVersion() {
        return ResponseEntity.ok(Map.of("version", projectVersion));
    }

    @PostMapping("/webhook")
    public ResponseEntity<Map<String, String>> postHandler(@RequestBody WebhookPayload payload) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(payload);
            System.out.println("[DEBUG] Received WebhookPayload:");
            System.out.println(json);
        } catch (Exception e) {
            System.out.println("[ERROR] Failed to log payload");
            e.printStackTrace();
        }

        String action = payload.getAction();
        System.out.println("[POST] /webhook action: " + action);

        if (payload.getPullRequest() != null && payload.getRepository() != null) {
            int prNumber = payload.getPullRequest().getNumber();
            String repoName = payload.getRepository().getName();
            String owner = payload.getRepository().getOwner().getLogin();

            System.out.printf("[Webhook] PR #%d in repo %s/%s%n", prNumber, owner, repoName);

            // Only fetch PR content for "synchronize" action
            if ("synchronize".equals(action)) {
                fetchPullRequestContent(owner, repoName, prNumber);
            }
        } else {
            System.out.println("[Webhook] Missing pull request or repository data.");
        }

        return responseOK;
    }

    @Value("${github.token}")
    private String githubToken;

    private void fetchPullRequestContent(String owner, String repo, int prNumber) {
        String apiUrl = String.format("https://api.github.com/repos/%s/%s/pulls/%d", owner, repo, prNumber);
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Authorization", "Bearer " + githubToken)
                .header("Accept", "application/vnd.github.v3.diff")
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            String prDiff = response.body();
            System.out.printf("[GitHub API] PR #%d diff:\n%s%n", prNumber, prDiff);
            System.out.println("[DEBUG] Calling Gemini API with PR diff..."); // TEST
            callGeminiAPI(prDiff);
            System.out.println("[DEBUG] Gemini API call finished."); // TEST
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private void callGeminiAPI(String prDiff) {

        System.out.println("[Gemini API] Calling Gemini...");

        String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + geminiApiKey;

        String requestBody = String.format("""
            {
            "contents": [
                {
                "parts": [
                    {
                    "text": "You are a code reviewer. Review the following pull request diff and give suggestions:\\n%s"
                    }
                ]
                }
            ]
            }
            """, prDiff);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(response.body());

            System.out.println("[Gemini API] Code Review Suggestions:");
            System.out.println(json.toPrettyString());

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
