package com.traveling.travel_backend.dto;

import com.traveling.travel_backend.model.Review;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ReviewResponseDTO {

    private Long id;
    private UserResponseDTO user;
    private String comment;
    private Integer score;
    private OffsetDateTime createdAt;
    private boolean state;
    private List<ReviewResponseDTO> replies;

    public ReviewResponseDTO() {}

    public static ReviewResponseDTO fromEntity(Review review) {
        ReviewResponseDTO dto = new ReviewResponseDTO();
        dto.id        = review.getId();
        dto.user      = review.getUser() != null ? UserResponseDTO.fromEntity(review.getUser()) : null;
        dto.comment   = review.getComment();
        dto.score     = review.getScore();
        dto.createdAt = review.getCreatedAt();
        dto.state     = review.isState();
        dto.replies   = review.getReplies() != null
                ? review.getReplies().stream()
                        .filter(Review::isState)
                        .map(ReviewResponseDTO::fromEntity)
                        .collect(Collectors.toList())
                : Collections.emptyList();
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public UserResponseDTO getUser() { return user; }
    public void setUser(UserResponseDTO user) { this.user = user; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isState() { return state; }
    public void setState(boolean state) { this.state = state; }

    public List<ReviewResponseDTO> getReplies() { return replies; }
    public void setReplies(List<ReviewResponseDTO> replies) { this.replies = replies; }
}
