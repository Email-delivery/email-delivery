package az.aladdin.emaildelivery.model.response.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminAuthResponse {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private List<String> roles;
    private List<String> permissions;
    private String accessToken;
    @Builder.Default
    private String tokenType = "Bearer";
    private Instant expiresAt;

    @JsonIgnore
    private String refreshToken;
}
