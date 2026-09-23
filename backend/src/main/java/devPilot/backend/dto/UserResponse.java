package devPilot.backend.dto;

import java.util.UUID;

public record UserResponse(
        Long id,
        String githubId,
        String githubUsername,
        String displayName,
        String avatarUrl
) {

    public UserResponse(UUID id2, Long githubId2, String githubUsername2, String displayName2, String avatarUrl2) {
        this(id2.getMostSignificantBits(), String.valueOf(githubId2), githubUsername2, displayName2, avatarUrl2);
    }
}