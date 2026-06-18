package az.aladdin.emaildelivery.model.request.email;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpsertEmailDraftRequest {

    @Size(max = 200)
    private String subject;

    @Size(max = 500_000)
    private String bodyHtml;

    private Long campaignId;

    private Boolean showCc;

    private Boolean showBcc;

    @Builder.Default
    @Valid
    private List<EmailDraftRecipientRequest> to = new ArrayList<>();

    @Builder.Default
    @Valid
    private List<EmailDraftRecipientRequest> cc = new ArrayList<>();

    @Builder.Default
    @Valid
    private List<EmailDraftRecipientRequest> bcc = new ArrayList<>();
}
