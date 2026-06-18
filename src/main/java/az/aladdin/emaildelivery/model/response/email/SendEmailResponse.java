package az.aladdin.emaildelivery.model.response.email;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SendEmailResponse {

    private String id;
    private String sentAt;
    private String scheduledAt;
    private String status;
    private int recipientCount;
    private int suppressedCount;
}
