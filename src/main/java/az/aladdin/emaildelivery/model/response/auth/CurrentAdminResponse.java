package az.aladdin.emaildelivery.model.response.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * The identity of the admin currently operating the panel.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrentAdminResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private List<String> roles;
    private List<String> permissions;
}
