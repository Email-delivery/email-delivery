package az.aladdin.emaildelivery.model.request.auth;

import az.aladdin.emaildelivery.annotation.NoFieldLogging;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@NoFieldLogging
public class VerifyCodeRequest {

    @NotBlank(message = "{validation.email.required}")
    @Size(max = 160, message = "{validation.email.too_long}")
    @Email(message = "{validation.email.invalid}")
    String email;

    @NotBlank(message = "{validation.code.required}")
    @Size(min = 6, max = 6, message = "{validation.code.required}")
    String code;
}
