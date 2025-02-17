package com.githubaibot.controller;

import com.githubaibot.model.GithubWebhookPayload;
import com.githubaibot.service.CodeReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhook")
public class GithubWebhookController {

    private final CodeReviewService codeReviewService;

    public GithubWebhookController(CodeReviewService codeReviewService) {
        this.codeReviewService = codeReviewService;
    }

    @PostMapping
    public ResponseEntity<String> handleWebhook(@RequestBody GithubWebhookPayload payload) {
        System.out.println("Received Webhook: " + payload);

        if ("opened".equals(payload.getAction()) || "synchronize".equals(payload.getAction())) {
            int prNumber = payload.getPull_request().getNumber();
            String repoFullName = payload.getPull_request().getHead().getRepo().getFull_name();
            String commitSha = payload.getPull_request().getHead().getSha();

            System.out.println("Processing PR #" + prNumber + " from repo: " + repoFullName);

            // Call the AI review service
            codeReviewService.reviewPullRequest(repoFullName, prNumber, commitSha);
        }

        return ResponseEntity.ok("Webhook received");
    }
}
