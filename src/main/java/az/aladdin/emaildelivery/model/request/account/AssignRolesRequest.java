package az.aladdin.emaildelivery.model.request.account;

import jakarta.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AssignRolesRequest {

    @NotEmpty(message = "{validation.roles.required}")
    Set<Long> roleIds;
}
