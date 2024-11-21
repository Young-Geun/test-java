package com.example.hello;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/hello")
public class HelloController {

    @GetMapping
    public ResponseEntity<Map<String, String>> hello() {
        return ResponseEntity.ok(Map.of(
            "message", "Hello, Spring Boot REST API!",
            "version", "Spring Boot 3.2.1",
            "status", "Running"
        ));
    }
}
