package az.aladdin.emaildelivery.mapper;

import az.aladdin.emaildelivery.model.entity.EmailSuppression;
import az.aladdin.emaildelivery.model.response.email.EmailSuppressionResponse;
import org.springframework.stereotype.Component;

@Component
public class EmailSuppressionMapper {

    public EmailSuppressionResponse toResponse(EmailSuppression suppression) {
        return EmailSuppressionResponse.builder()
                .id(suppression.getId())
                .email(suppression.getEmail())
                .unsubscribedAt(suppression.getUnsubscribedAt())
                .source(suppression.getSource())
                .campaignId(suppression.getCampaignId())
                .sentEmailId(suppression.getSentEmailId())
                .build();
    }
}
