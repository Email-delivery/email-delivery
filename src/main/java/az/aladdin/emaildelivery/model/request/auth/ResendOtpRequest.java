package az.aladdin.emaildelivery.model.request.auth;

import az.aladdin.emaildelivery.model.enums.OtpPurpose;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResendOtpRequest {

    @NotBlank(message = "{validation.email.required}")
    @Email(message = "{validation.email.invalid}")
    String email;

    @NotNull(message = "{validation.otp.purpose.required}")
    OtpPurpose purpose;
}
