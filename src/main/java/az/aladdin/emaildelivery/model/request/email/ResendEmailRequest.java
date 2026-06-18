package az.aladdin.emaildelivery.model.request.email;

import az.aladdin.emaildelivery.model.enums.EmailResendMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResendEmailRequest {

    @Builder.Default
    private EmailResendMode mode = EmailResendMode.ALL;
}
