package az.aladdin.emaildelivery.model.response.email;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SentEmailResponse {

    private String id;
    private String subject;
    private String bodyHtml;
    private String sentBy;
    private String sentByName;
    private String fromEmail;
    private String fromName;
    private String fromLabel;
    private String sentAt;
    private String scheduledAt;
    private String status;
    private List<EmailRecipientStatusResponse> to;
    private List<EmailRecipientStatusResponse> cc;
    private List<EmailRecipientStatusResponse> bcc;
}
