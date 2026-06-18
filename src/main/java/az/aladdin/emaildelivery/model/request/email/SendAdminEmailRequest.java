package az.aladdin.emaildelivery.model.request.email;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SendAdminEmailRequest {

    @NotEmpty
    private List<@Email @NotBlank String> to;

    private List<@Email @NotBlank String> cc;

    private List<@Email @NotBlank String> bcc;

    @NotBlank
    @Size(max = 200)
    private String subject;

    @NotBlank
    @Size(max = 500_000)
    private String bodyHtml;

    private String bodyText;

    private Long campaignId;

    private Long resendOfEmailId;

    private Instant scheduledAt;

    private Long senderIdentityId;

    private Boolean includeUnsubscribe;

    @Builder.Default
    private List<EmailAttachmentRequest> attachments = new ArrayList<>();
}
