package com.traveling.travel_backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "system_logs")
public class LogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private String module; 

    @Column(nullable = false)
    private String level; 

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    private Long userId; 


    public LogEntity() {
    }

    public LogEntity(String module, String level, String message, Long userId) {
        this.timestamp = LocalDateTime.now(); 
        this.module = module;
        this.level = level;
        this.message = message;
        this.userId = userId;
    }


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}