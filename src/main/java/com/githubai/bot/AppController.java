package com.githubai.bot;

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

    // TEST
    @GetMapping("/test-token")
    public ResponseEntity<Map<String, String>> testGithubToken() {
        String token = System.getenv("GITHUB_TOKEN");
        String maskedToken = token == null ? "null" : token.substring(0, 4) + "..." + token.substring(token.length() - 4);
        return ResponseEntity.ok(Map.of("token", maskedToken));
    }

    @Value("${build.version}")
    private String projectVersion;

    @Value("${GITHUB_TOKEN}")
    private String githubToken;

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
        } else {
            System.out.println("[Webhook] Missing pull request or repository data.");
        }

        return responseOK;
    }
}
