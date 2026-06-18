package az.aladdin.emaildelivery.model.response.email;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmailDraftResponse {

    private Long id;
    private String subject;
    private String bodyHtml;
    private Long campaignId;
    private boolean showCc;
    private boolean showBcc;
    private List<EmailDraftRecipientResponse> to;
    private List<EmailDraftRecipientResponse> cc;
    private List<EmailDraftRecipientResponse> bcc;
    private Instant updatedAt;
}
