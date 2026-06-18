package az.aladdin.emaildelivery.model.request.auth;

import az.aladdin.emaildelivery.annotation.NoFieldLogging;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoFieldLogging
public class RegisterRequest {

    @NotBlank(message = "{validation.first_name.required}")
    @Size(max = 50, message = "{validation.first_name.too_long}")
    String firstName;

    @NotBlank(message = "{validation.last_name.required}")
    @Size(max = 50, message = "{validation.last_name.too_long}")
    String lastName;

    @NotBlank(message = "{validation.email.required}")
    @Size(max = 160, message = "{validation.email.too_long}")
    @Email(message = "{validation.email.invalid}")
    String email;

    @NotBlank(message = "{validation.password.new_required}")
    @Size(min = 6, max = 30, message = "{validation.password.pattern}")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d!@#$%^&*]{6,}$",
            message = "{validation.password.pattern}")
    String password;

    @NotBlank(message = "{validation.password.confirmation_required}")
    String passwordConfirm;
}
