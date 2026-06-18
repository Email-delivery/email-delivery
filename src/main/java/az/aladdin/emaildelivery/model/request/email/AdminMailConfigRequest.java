package az.aladdin.emaildelivery.model.request.email;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminMailConfigRequest {

    @NotBlank(message = "{validation.mail.host.required}")
    @Size(max = 255, message = "{validation.mail.host.max}")
    String host;

    @NotNull(message = "{validation.mail.port.required}")
    @Min(value = 1, message = "{validation.mail.port.min}")
    @Max(value = 65535, message = "{validation.mail.port.max}")
    Integer port;

    @NotBlank(message = "{validation.mail.username.required}")
    @Size(max = 255, message = "{validation.mail.username.max}")
    String username;

    @Size(max = 500, message = "{validation.mail.password.max}")
    String password;
}
