package az.aladdin.emaildelivery.model.response.account;

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
public class AdminRoleResponse {
    private Long id;
    private String name;
    private String description;
    private boolean systemRole;
    private List<String> permissions;
    private Instant createdAt;
    private Instant updatedAt;
}
