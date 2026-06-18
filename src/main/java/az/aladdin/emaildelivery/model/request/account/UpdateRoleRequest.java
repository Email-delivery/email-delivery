package az.aladdin.emaildelivery.model.request.account;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateRoleRequest {

    @Size(max = 255, message = "{validation.role_description.too_long}")
    String description;

    @NotEmpty(message = "{validation.permissions.required}")
    Set<String> permissions;
}
