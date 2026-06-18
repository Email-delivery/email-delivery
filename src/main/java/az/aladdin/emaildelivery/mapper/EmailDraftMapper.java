package az.aladdin.emaildelivery.mapper;

import az.aladdin.emaildelivery.model.entity.EmailDraft;
import az.aladdin.emaildelivery.model.entity.EmailDraftRecipient;
import az.aladdin.emaildelivery.model.enums.EmailRecipientType;
import az.aladdin.emaildelivery.model.response.email.EmailDraftRecipientResponse;
import az.aladdin.emaildelivery.model.response.email.EmailDraftResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EmailDraftMapper {

    public EmailDraftResponse toResponse(EmailDraft draft) {
        return EmailDraftResponse.builder()
                .id(draft.getId())
                .subject(draft.getSubject())
                .bodyHtml(draft.getBodyHtml())
                .campaignId(draft.getCampaignId())
                .showCc(draft.isShowCc())
                .showBcc(draft.isShowBcc())
                .to(mapRecipients(draft, EmailRecipientType.TO))
                .cc(mapRecipients(draft, EmailRecipientType.CC))
                .bcc(mapRecipients(draft, EmailRecipientType.BCC))
                .updatedAt(draft.getUpdatedAt())
                .build();
    }

    private List<EmailDraftRecipientResponse> mapRecipients(EmailDraft draft, EmailRecipientType type) {
        return draft.getRecipients().stream()
                .filter(recipient -> recipient.getRecipientType() == type)
                .map(this::toRecipientResponse)
                .toList();
    }

    private EmailDraftRecipientResponse toRecipientResponse(EmailDraftRecipient recipient) {
        return EmailDraftRecipientResponse.builder()
                .email(recipient.getEmail())
                .name(recipient.getName())
                .build();
    }
}
