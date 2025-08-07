package com.githubai.bot;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class AppController {

    @GetMapping("/")
    public String index() {
        return "ok";
    }

    // TODO(#20): Unify this method with postHandler since both return the same
    // response structure.
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @Value("${build.version}")
    private String projectVersion;

    @GetMapping("/version")
    public ResponseEntity<Map<String, String>> getVersion() {
        return ResponseEntity.ok(Map.of("version", projectVersion));
    }

    @PostMapping("/webhook")
    public ResponseEntity<ResponseMessage> postHandler(@RequestBody WebhookPayload payload) {
        String action = payload.getAction();
        System.out.println("[POST] /webhook action: " + action);
        return ResponseEntity.ok(new ResponseMessage("ok"));
    }

    // Define a simple ResponseMessage class
    static class ResponseMessage {
        private String status;

        public ResponseMessage(String status) {
            this.status = status;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
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
