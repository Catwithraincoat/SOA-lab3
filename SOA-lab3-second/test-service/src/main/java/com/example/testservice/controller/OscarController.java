package com.example.testservice.controller;

import com.example.testservice.service.FirstService;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Collections;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.ResourceAccessException;


@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class OscarController {

    private final FirstService firstService;
    private final Environment environment;
    private static final Logger log = LoggerFactory.getLogger(OscarController.class);

    public OscarController(FirstService firstService, Environment environment) {
        this.firstService = firstService;
        this.environment = environment;
    }

    // @GetMapping("/test")
    // public ResponseEntity<String> getTest() {
        // log.info("Request handled by instance on port: {}", environment.getProperty("server.port"));
        // return firstService.getTest();
    // }

    @GetMapping("/oscar/movies/get-loosers")
    public ResponseEntity<?> getMoviesWithoutOscars() {
        ResponseEntity<List<Map<String, Object>>> response = firstService.getMoviesWithoutOscars();
        List<?> movies = response.getBody();
        return ResponseEntity.ok(Map.of("items", movies));
    }

    @GetMapping("/oscar/directors/get-loosers")
    public ResponseEntity<?> getDirectorsWithoutOscars() {
        ResponseEntity<List<Map<String, Object>>> response = firstService.getAllMovies();
        List<Map<String, Object>> movies = response.getBody();
        
        List<Map<String, String>> directorNames = movies.stream()
                .collect(Collectors.groupingBy(
                        movie -> ((Map<String, Object>)movie.get("director")).get("name").toString(),
                        Collectors.mapping(
                                movie -> movie.get("oscarsCount"),
                                Collectors.maxBy((o1, o2) -> {
                                    if (o1 == null && o2 == null) return 0;
                                    if (o1 == null) return -1;
                                    if (o2 == null) return 1;
                                    return ((Number)o1).intValue() - ((Number)o2).intValue();
                                })
                        )
                ))
                .entrySet()
                .stream()
                .filter(entry -> !entry.getValue().isPresent() || 
                               entry.getValue().get() == null || 
                               ((Number)entry.getValue().get()).intValue() == 0)
                .map(entry -> Collections.singletonMap("directorName", entry.getKey()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of("items", directorNames));
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<?> handleServiceError(ResourceAccessException e) {
        return ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of("message", List.of(
                Map.of("inner_message", e.getMessage())
            )));
    }
} 