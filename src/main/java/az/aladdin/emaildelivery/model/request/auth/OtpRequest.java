package az.aladdin.emaildelivery.model.request.auth;

import az.aladdin.emaildelivery.annotation.NoFieldLogging;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoFieldLogging
public class OtpRequest {

    @NotBlank(message = "{validation.email.required}")
    @Email(message = "{validation.email.invalid}")
    String email;

    @NotNull(message = "{validation.otp.code.required}")
    Integer otpCode;
}
