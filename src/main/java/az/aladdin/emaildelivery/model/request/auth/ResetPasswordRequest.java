package az.aladdin.emaildelivery.model.request.auth;

import az.aladdin.emaildelivery.annotation.NoFieldLogging;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@NoFieldLogging
public class ResetPasswordRequest {

    @NotBlank(message = "{validation.email.required}")
    @Size(max = 160, message = "{validation.email.too_long}")
    @Email(message = "{validation.email.invalid}")
    String email;

    @NotNull(message = "{validation.password.new_required}")
    @Size(min = 6, max = 30, message = "{validation.password.pattern}")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d!@#$%^&*]{6,}$",
            message = "{validation.password.pattern}")
    String newPassword;

    @NotNull(message = "{validation.password.retry_required}")
    String retryPassword;
}
