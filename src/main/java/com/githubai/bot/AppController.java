package com.githubai.bot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

@RestController
public class AppController {

    @GetMapping("/")
    public String index() {
        return "ok";
    }

    // TODO(#20): Unify this method with postHandler since both return the same response structure.
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
    /*
    @GetMapping("/version")
    public ResponseEntity<Map<String, String>> getVersion() {
        String version = "unknown";
        try {
            Properties properties = new Properties();
            properties.load(new ClassPathResource("pom.properties").getInputStream());
            version = properties.getProperty("version", "unknown");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(Map.of("version", version));
    }
    */

    @PostMapping("/webhook")
    public ResponseEntity<ResponseMessage> postHandler() {
        ResponseMessage response = new ResponseMessage("ok");
        return ResponseEntity.ok(response);
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
}
