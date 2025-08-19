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

    @Value("${build.version}")
    private String projectVersion;

    @Value("${github.token}")
    private String githubToken;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @GetMapping("/")
    public String index() {
        return "ok";
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return responseOK;
    }

    @GetMapping("/version")
    public ResponseEntity<Map<String, String>> getVersion() {
        return ResponseEntity.ok(Map.of("version", projectVersion));
    }

    @PostMapping("/webhook")
    public ResponseEntity<Map<String, String>> postHandler(@RequestBody WebhookPayload payload) {
        String action = payload.getAction();
        System.out.println("[Webhook] Received action: " + action);

        if (payload.getPullRequest() != null && payload.getRepository() != null) {
            int prNumber = payload.getPullRequest().getNumber();
            String repoName = payload.getRepository().getName();
            String owner = payload.getRepository().getOwner().getLogin();

            System.out.printf("[Webhook] PR #%d in %s/%s%n", prNumber, owner, repoName);

            if ("synchronize".equals(action)) {
                fetchPullRequestContent(owner, repoName, prNumber);
            }
        } else {
            System.out.println("[Webhook] Missing pull request or repository data.");
        }

        return responseOK;
    }

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

            System.out.printf("[GitHub API] PR #%d diff fetched%n", prNumber);

            String review = callGeminiAPI(prDiff);
            if (review != null && !review.isBlank()) {
                System.out.println("[DEBUG] Review is valid, posting comment...");
                postPRComment(owner, repo, prNumber, review);
            } else {
                System.out.println("[DEBUG] Review is null or blank. Skipping comment post.");
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("[GitHub API] Failed to fetch PR diff:");
            e.printStackTrace();
        }
    }

    private String callGeminiAPI(String prDiff) {
        System.out.println("[Gemini API] Sending PR diff for review...");

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

            String rawResponse = response.body();
            System.out.println("[Gemini API] Raw response:");
            System.out.println(rawResponse);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(rawResponse);

            JsonNode content = json.at("/candidates/0/content/parts/0/text");

            if (content.isMissingNode() || content.asText().isBlank()) {
                System.out.println("[Gemini API] No valid review returned.");
                return null;
            }

            String review = content.asText();

            System.out.println("[Gemini API] Review Suggestions:");
            System.out.println(review);

            return review;

        } catch (IOException | InterruptedException e) {
            System.err.println("[Gemini API] Error during request:");
            e.printStackTrace();
            return null;
        }
    }

    private void postPRComment(String owner, String repo, int prNumber, String commentBody) {
        String apiUrl = String.format("https://api.github.com/repos/%s/%s/issues/%d/comments", owner, repo, prNumber);

        System.out.println("[DEBUG] Posting comment to PR..."); // TEST
        System.out.println("[DEBUG] Comment content:"); // TEST
        System.out.println(commentBody);

        try {
            ObjectMapper mapper = new ObjectMapper();
            String jsonBody = mapper.writeValueAsString(Map.of("body", commentBody));

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Authorization", "Bearer " + githubToken)
                .header("Accept", "application/vnd.github.v3+json")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            int statusCode = response.statusCode();
            String responseBody = response.body();

            System.out.println("[GitHub API] Status code: " + statusCode);
            System.out.println("[GitHub API] Response body:");
            System.out.println(responseBody);

            if (statusCode == 201) {
                System.out.println("[GitHub API] Comment successfully posted to PR.");
            } else {
                System.err.println("[GitHub API] Failed to post comment. Check token permissions and repo/PR details.");
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("[GitHub API] Exception occurred while posting comment:");
            e.printStackTrace();
        }
    }
}
