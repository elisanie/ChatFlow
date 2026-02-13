package edu.neu.cs6650.server;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {

    //http get
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}