package com.githubai.bot;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class AppController {

    @GetMapping("/")
    public String index() {
        return "ok";
    }

    @PostMapping("/webhook")
    public ResponseEntity<?> handleWebhook() {
        return ResponseEntity.ok().body("{\"status\": \"ok\"}");
    }
}
