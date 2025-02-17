package com.githubaibot.model;

import lombok.Data;

@Data
public class GithubWebhookPayload {
    private String action; // e.g., "opened", "synchronize"
    private PullRequest pull_request;

    @Data
    public static class PullRequest {
        private int number;
        private String url;
        private String diff_url;
        private Head head;

        @Data
        public static class Head {
            private String ref; // Branch name
            private String sha; // Commit hash
            private Repo repo;

            @Data
            public static class Repo {
                private String full_name; // Repository name (e.g., "owner/repo")
            }
        }
    }
}
