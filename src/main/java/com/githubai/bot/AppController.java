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

            System.out.println("[GitHub API] PR DIFF:\n" + response.body());

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}

// TEST synchromization
