package az.aladdin.emaildelivery.model.request.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateAdminRequest {

    @NotBlank(message = "{validation.first_name.required}")
    @Size(max = 50, message = "{validation.first_name.too_long}")
    String firstName;

    @NotBlank(message = "{validation.last_name.required}")
    @Size(max = 50, message = "{validation.last_name.too_long}")
    String lastName;

    @NotBlank(message = "{validation.role_name.required}")
    @Size(max = 64, message = "{validation.role_name.too_long}")
    String role;

    @NotBlank(message = "{validation.status.required}")
    String status;

    @NotEmpty(message = "{validation.permissions.required}")
    Set<String> permissions;
}
