package devPilot.backend.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;

@Entity 
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor
@Table (name = "users")
@Builder

public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
private UUID id;

@Column(name = "github_id", unique = true, nullable = false)
private Long githubId;

@Column(name = "github_username", length=225, nullable = false)
private String githubUsername;

@Column(name = "email", length=320)
private String email;

@Column(name = "display_name", length=500, nullable = false)
private String displayName;

@Column(name = "avatar_url", length=500)
private String avatarUrl;

@Column(name = "access_token", length=500, columnDefinition = "TEXT")
private String accessToken;

@Column(name = "token_scope", length=500)
private String tokenScope;

@Column(name = "created_at", nullable = false, updatable = false)
private Instant createdAt;

@PrePersist 
void onCreate() {
    if(createdAt == null) {
        this.createdAt = Instant.now();
    }
}
}