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

    @Value("${build.version}")
    private String projectVersion;

    @GetMapping("/version")
    public ResponseEntity<Map<String, String>> getVersion() {
        return ResponseEntity.ok(Map.of("version", projectVersion));
    }

    // TEST webhook
    @PostMapping("/webhook")
    public ResponseEntity<Map<String, String>> postHandler(@RequestBody WebhookPayload payload) {
        String action = payload.getAction();
        System.out.println("[POST] /webhook action: " + action);
        return responseOK;
    }

    // Payload class for extracting "action" from JSON
    static class WebhookPayload {
        private String action;

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }
    }
}
