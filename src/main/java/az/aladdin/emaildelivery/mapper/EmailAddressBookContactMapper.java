package az.aladdin.emaildelivery.mapper;

import az.aladdin.emaildelivery.model.entity.EmailAddressBookContact;
import az.aladdin.emaildelivery.model.response.email.EmailAddressBookContactResponse;
import org.springframework.stereotype.Component;

@Component
public class EmailAddressBookContactMapper {

    public EmailAddressBookContactResponse toResponse(EmailAddressBookContact contact) {
        return EmailAddressBookContactResponse.builder()
                .id(contact.getId())
                .email(contact.getEmail())
                .name(contact.getName())
                .createdAt(contact.getCreatedAt())
                .updatedAt(contact.getUpdatedAt())
                .build();
    }
}
