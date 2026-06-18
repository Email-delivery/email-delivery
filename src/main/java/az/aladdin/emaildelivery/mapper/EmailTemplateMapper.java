package az.aladdin.emaildelivery.mapper;

import az.aladdin.emaildelivery.model.entity.EmailTemplate;
import az.aladdin.emaildelivery.model.response.email.EmailTemplateResponse;
import org.springframework.stereotype.Component;

@Component
public class EmailTemplateMapper {

    public EmailTemplateResponse toResponse(EmailTemplate template) {
        return EmailTemplateResponse.builder()
                .id(template.getId())
                .name(template.getName())
                .description(template.getDescription())
                .subject(template.getSubject())
                .htmlBody(template.getHtmlBody())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }
}
