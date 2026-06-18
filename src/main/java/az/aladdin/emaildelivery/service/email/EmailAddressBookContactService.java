package az.aladdin.emaildelivery.service.email;

import az.aladdin.emaildelivery.exception.BadException;
import az.aladdin.emaildelivery.exception.EntityNames;
import az.aladdin.emaildelivery.exception.MessageKeys;
import az.aladdin.emaildelivery.exception.NotFoundException;
import az.aladdin.emaildelivery.mapper.EmailAddressBookContactMapper;
import az.aladdin.emaildelivery.model.entity.EmailAddressBookContact;
import az.aladdin.emaildelivery.model.request.email.EmailCampaignContactRequest;
import az.aladdin.emaildelivery.model.request.email.UpsertEmailAddressBookContactRequest;
import az.aladdin.emaildelivery.model.response.email.EmailAddressBookContactPageResponse;
import az.aladdin.emaildelivery.model.response.email.EmailAddressBookContactResponse;
import az.aladdin.emaildelivery.repository.EmailAddressBookContactRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailAddressBookContactService {

    private final EmailAddressBookContactRepository emailAddressBookContactRepository;
    private final EmailAddressBookContactMapper emailAddressBookContactMapper;

    @Transactional(readOnly = true)
    public EmailAddressBookContactPageResponse search(String search, int page, int limit) {
        int normalizedPage = page < 1 ? 1 : page;
        int normalizedLimit = limit < 1 ? 20 : Math.min(limit, 100);
        var normalizedSearch = StringUtils.hasText(search) ? search.trim() : null;

        var result = emailAddressBookContactRepository.search(
                normalizedSearch,
                PageRequest.of(normalizedPage - 1, normalizedLimit, Sort.by(Sort.Direction.ASC, "email")));

        return EmailAddressBookContactPageResponse.builder()
                .items(result.getContent().stream().map(emailAddressBookContactMapper::toResponse).toList())
                .total(result.getTotalElements())
                .page(normalizedPage)
                .limit(normalizedLimit)
                .build();
    }

    @Transactional(readOnly = true)
    public EmailAddressBookContactResponse get(Long id) {
        return emailAddressBookContactMapper.toResponse(findById(id));
    }

    @Transactional
    public EmailAddressBookContactResponse create(UpsertEmailAddressBookContactRequest request) {
        var normalizedEmail = normalizeEmail(request.getEmail());
        ensureUniqueEmail(normalizedEmail, null);
        var saved = emailAddressBookContactRepository.save(EmailAddressBookContact.builder()
                .email(normalizedEmail)
                .name(trimToNull(request.getName()))
                .build());
        log.info("Created address book contact {}", saved.getEmail());
        return emailAddressBookContactMapper.toResponse(saved);
    }

    @Transactional
    public EmailAddressBookContactResponse update(Long id, UpsertEmailAddressBookContactRequest request) {
        var contact = findById(id);
        var normalizedEmail = normalizeEmail(request.getEmail());
        ensureUniqueEmail(normalizedEmail, id);
        contact.setEmail(normalizedEmail);
        contact.setName(trimToNull(request.getName()));
        var saved = emailAddressBookContactRepository.save(contact);
        log.info("Updated address book contact {}", saved.getEmail());
        return emailAddressBookContactMapper.toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        var contact = findById(id);
        emailAddressBookContactRepository.delete(contact);
        log.info("Deleted address book contact {}", contact.getEmail());
    }

    @Transactional
    public void syncFromCampaignContacts(List<EmailCampaignContactRequest> contacts) {
        if (contacts == null || contacts.isEmpty()) {
            return;
        }

        var unique = new LinkedHashMap<String, EmailCampaignContactRequest>();
        for (var contact : contacts) {
            if (contact == null || !StringUtils.hasText(contact.getEmail())) {
                continue;
            }
            unique.putIfAbsent(normalizeEmail(contact.getEmail()), contact);
        }

        for (var contact : unique.values()) {
            upsertSilently(normalizeEmail(contact.getEmail()), trimToNull(contact.getName()));
        }
    }

    private void upsertSilently(String email, String name) {
        emailAddressBookContactRepository.findByEmailIgnoreCase(email).ifPresentOrElse(existing -> {
            if (!StringUtils.hasText(existing.getName()) && StringUtils.hasText(name)) {
                existing.setName(name);
                emailAddressBookContactRepository.save(existing);
            }
        }, () -> emailAddressBookContactRepository.save(EmailAddressBookContact.builder()
                .email(email)
                .name(name)
                .build()));
    }

    private EmailAddressBookContact findById(Long id) {
        return emailAddressBookContactRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        EntityNames.EMAIL_ADDRESS_BOOK_CONTACT,
                        MessageKeys.EXCEPTION_NOT_FOUND,
                        id));
    }

    private void ensureUniqueEmail(String email, Long excludeId) {
        var exists = excludeId == null
                ? emailAddressBookContactRepository.existsByEmailIgnoreCase(email)
                : emailAddressBookContactRepository.existsByEmailIgnoreCaseAndIdNot(email, excludeId);
        if (exists) {
            throw new BadException(
                    EntityNames.EMAIL_ADDRESS_BOOK_CONTACT,
                    MessageKeys.EXCEPTION_EMAIL_CONTACT_EXISTS,
                    email);
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
