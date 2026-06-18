package az.aladdin.emaildelivery.mapper;

import az.aladdin.emaildelivery.model.entity.EmailCampaign;
import az.aladdin.emaildelivery.model.entity.EmailCampaignContact;
import az.aladdin.emaildelivery.model.response.email.EmailCampaignContactResponse;
import az.aladdin.emaildelivery.model.response.email.EmailCampaignResponse;
import org.springframework.stereotype.Component;

@Component
public class EmailCampaignMapper {

    public EmailCampaignResponse toResponse(EmailCampaign campaign, boolean includeContacts) {
        var contacts = includeContacts
                ? campaign.getContacts().stream().map(this::toContactResponse).toList()
                : null;
        return EmailCampaignResponse.builder()
                .id(campaign.getId())
                .name(campaign.getName())
                .description(campaign.getDescription())
                .defaultSubject(campaign.getDefaultSubject())
                .defaultHtmlBody(campaign.getDefaultHtmlBody())
                .templateId(campaign.getTemplateId())
                .contactCount(campaign.getContacts().size())
                .contacts(contacts)
                .createdAt(campaign.getCreatedAt())
                .updatedAt(campaign.getUpdatedAt())
                .build();
    }

    public EmailCampaignContactResponse toContactResponse(EmailCampaignContact contact) {
        return EmailCampaignContactResponse.builder()
                .id(contact.getId())
                .email(contact.getEmail())
                .name(contact.getName())
                .build();
    }
}
