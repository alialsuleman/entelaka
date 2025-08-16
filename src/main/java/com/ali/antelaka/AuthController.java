package com.ali.antelaka;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    @GetMapping("/success")
    public ResponseEntity<String> success(@RequestParam String token) {
        return ResponseEntity.ok().body(token);
    }
}