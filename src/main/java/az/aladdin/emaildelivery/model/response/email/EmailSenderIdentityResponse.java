package az.aladdin.emaildelivery.model.response.email;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmailSenderIdentityResponse {

    private String id;
    private String email;
    private String displayName;
    private boolean defaultSender;
    private boolean active;
    /** Formatted label for UI dropdowns, e.g. "Aladdin Hotels <info@ingress.az>" */
    private String label;
}
