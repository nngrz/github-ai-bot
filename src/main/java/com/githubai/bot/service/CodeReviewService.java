package com.githubaibot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.List;

@Service
public class CodeReviewService {

    @Value("${deepseek.api-key}")
    private String deepseekApiKey;

    @Value("${github.token}")
    private String githubToken;

    private final RestTemplate restTemplate = new RestTemplate();
    private final String GITHUB_API_URL = "https://api.github.com/repos";

    public void reviewPullRequest(String repoName, int prNumber, String commitSha) {
        // Step 1: Fetch the PR code changes
        String codeChanges = fetchCodeChangesFromGitHub(repoName, commitSha);

        // Step 2: Send the code to DeepSeek for AI review
        String reviewComments = analyzeCodeWithDeepSeek(codeChanges);

        // Step 3: Post AI-generated comments back to the PR
        postComment(repoName, prNumber, reviewComments);
    }

    private String fetchCodeChangesFromGitHub(String repoName, String commitSha) {
        String url = GITHUB_API_URL + "/" + repoName + "/commits/" + commitSha;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + githubToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<String> request = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);

        // Extract diff URL and fetch the code changes
        if (response.getStatusCode().is2xxSuccessful()) {
            Map responseBody = response.getBody();
            return responseBody != null ? responseBody.toString() : "No code changes found";
        } else {
            return "Failed to fetch code changes";
        }
    }

    private String analyzeCodeWithDeepSeek(String code) {
        String url = "https://api.deepseek.com/review";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + deepseekApiKey);

        Map<String, String> requestBody = Map.of("code", code);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        return response.getBody() != null ? response.getBody() : "No AI feedback received";
    }

    private void postComment(String repoName, int prNumber, String comment) {
        String url = GITHUB_API_URL + "/" + repoName + "/issues/" + prNumber + "/comments";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + githubToken);

        Map<String, String> requestBody = Map.of("body", comment);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

        restTemplate.postForEntity(url, request, String.class);
        System.out.println("✅ Successfully posted AI-generated comment to PR #" + prNumber);
    }
}
