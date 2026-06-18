package az.aladdin.emaildelivery.model.request.auth;

import az.aladdin.emaildelivery.annotation.NoFieldLogging;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoFieldLogging
public class AdminLoginRequest {

    @NotBlank(message = "{validation.email.required}")
    String email;

    @NotBlank(message = "{validation.password.required}")
    String password;
}
