package com.environment.quiz.controller;

import com.environment.quiz.dto.dashboard.AdminStatsResponse;
import com.environment.quiz.entity.Topic;
import com.environment.quiz.service.user.AdminService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsResponse> getStats() {
        return ResponseEntity.ok(adminService.getStats());
    }

    @GetMapping("/topics")
    public ResponseEntity<List<Topic>> getAllTopics() {
        return ResponseEntity.ok(adminService.getAllTopics());
    }

    @PostMapping("/topics")
    public ResponseEntity<Topic> createTopic(@RequestBody CreateTopicRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminService.createTopic(request.name(), request.description()));
    }

    @DeleteMapping("/topics/{id}")
    public ResponseEntity<Void> deleteTopic(@PathVariable Long id) {
        adminService.deleteTopic(id);
        return ResponseEntity.noContent().build();
    }

    public record CreateTopicRequest(
            @NotBlank(message = "Topic name is required")
            String name,
            String description
    ) {}
}