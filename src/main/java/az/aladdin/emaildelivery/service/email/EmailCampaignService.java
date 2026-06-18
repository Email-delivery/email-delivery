package az.aladdin.emaildelivery.service.email;

import az.aladdin.emaildelivery.exception.BadException;
import az.aladdin.emaildelivery.exception.EntityNames;
import az.aladdin.emaildelivery.exception.MessageKeys;
import az.aladdin.emaildelivery.exception.NotFoundException;
import az.aladdin.emaildelivery.mapper.EmailCampaignMapper;
import az.aladdin.emaildelivery.model.entity.EmailCampaign;
import az.aladdin.emaildelivery.model.entity.EmailCampaignContact;
import az.aladdin.emaildelivery.model.request.email.EmailCampaignContactRequest;
import az.aladdin.emaildelivery.model.request.email.UpsertEmailCampaignRequest;
import az.aladdin.emaildelivery.model.response.email.EmailCampaignPageResponse;
import az.aladdin.emaildelivery.model.response.email.EmailCampaignResponse;
import az.aladdin.emaildelivery.repository.EmailCampaignRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailCampaignService {

    private final EmailCampaignRepository emailCampaignRepository;
    private final EmailCampaignMapper emailCampaignMapper;
    private final EmailAddressBookContactService emailAddressBookContactService;
    private final EmailTemplateService emailTemplateService;

    @Transactional(readOnly = true)
    public EmailCampaignPageResponse search(String search, int page, int limit) {
        int normalizedPage = page < 1 ? 1 : page;
        int normalizedLimit = limit < 1 ? 20 : Math.min(limit, 100);
        var normalizedSearch = StringUtils.hasText(search) ? search.trim() : null;

        var result = emailCampaignRepository.search(
                normalizedSearch,
                PageRequest.of(normalizedPage - 1, normalizedLimit, Sort.by(Sort.Direction.DESC, "updatedAt")));

        return EmailCampaignPageResponse.builder()
                .items(result.getContent().stream()
                        .map(campaign -> emailCampaignMapper.toResponse(campaign, false))
                        .toList())
                .total(result.getTotalElements())
                .page(normalizedPage)
                .limit(normalizedLimit)
                .build();
    }

    @Transactional(readOnly = true)
    public EmailCampaignResponse get(Long id) {
        return emailCampaignMapper.toResponse(findById(id), true);
    }

    @Transactional
    public EmailCampaignResponse create(UpsertEmailCampaignRequest request) {
        ensureUniqueName(request.getName(), null);
        var campaign = EmailCampaign.builder()
                .name(request.getName().trim())
                .description(trimToNull(request.getDescription()))
                .defaultSubject(trimToNull(request.getDefaultSubject()))
                .defaultHtmlBody(trimToNull(request.getDefaultHtmlBody()))
                .templateId(resolveTemplateId(request.getTemplateId()))
                .build();
        replaceContacts(campaign, request.getContacts());
        var saved = emailCampaignRepository.save(campaign);
        emailAddressBookContactService.syncFromCampaignContacts(request.getContacts());
        log.info("Created email campaign '{}' with {} contacts", saved.getName(), saved.getContacts().size());
        return emailCampaignMapper.toResponse(saved, true);
    }

    @Transactional
    public EmailCampaignResponse update(Long id, UpsertEmailCampaignRequest request) {
        var campaign = findById(id);
        ensureUniqueName(request.getName(), id);
        campaign.setName(request.getName().trim());
        campaign.setDescription(trimToNull(request.getDescription()));
        campaign.setDefaultSubject(trimToNull(request.getDefaultSubject()));
        campaign.setDefaultHtmlBody(trimToNull(request.getDefaultHtmlBody()));
        campaign.setTemplateId(resolveTemplateId(request.getTemplateId()));
        replaceContacts(campaign, request.getContacts());
        var saved = emailCampaignRepository.save(campaign);
        emailAddressBookContactService.syncFromCampaignContacts(request.getContacts());
        log.info("Updated email campaign '{}'", saved.getName());
        return emailCampaignMapper.toResponse(saved, true);
    }

    @Transactional
    public void delete(Long id) {
        var campaign = findById(id);
        emailCampaignRepository.delete(campaign);
        log.info("Deleted email campaign '{}'", campaign.getName());
    }

    @Transactional(readOnly = true)
    public EmailCampaign findById(Long id) {
        return emailCampaignRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        EntityNames.EMAIL_CAMPAIGN,
                        MessageKeys.EXCEPTION_NOT_FOUND,
                        id));
    }

    private void ensureUniqueName(String name, Long excludeId) {
        var normalized = name.trim();
        var exists = excludeId == null
                ? emailCampaignRepository.existsByNameIgnoreCase(normalized)
                : emailCampaignRepository.existsByNameIgnoreCaseAndIdNot(normalized, excludeId);
        if (exists) {
            throw new BadException(
                    EntityNames.EMAIL_CAMPAIGN,
                    MessageKeys.EXCEPTION_CAMPAIGN_NAME_EXISTS,
                    normalized);
        }
    }

    private void replaceContacts(EmailCampaign campaign, java.util.List<EmailCampaignContactRequest> contacts) {
        campaign.getContacts().clear();
        if (contacts == null || contacts.isEmpty()) {
            return;
        }

        var unique = new LinkedHashMap<String, EmailCampaignContactRequest>();
        for (var contact : contacts) {
            var normalizedEmail = contact.getEmail().trim().toLowerCase(Locale.ROOT);
            unique.putIfAbsent(normalizedEmail, contact);
        }

        for (var contact : unique.values()) {
            campaign.getContacts().add(EmailCampaignContact.builder()
                    .campaign(campaign)
                    .email(contact.getEmail().trim().toLowerCase(Locale.ROOT))
                    .name(trimToNull(contact.getName()))
                    .build());
        }
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private Long resolveTemplateId(Long templateId) {
        if (templateId == null) {
            return null;
        }
        emailTemplateService.findById(templateId);
        return templateId;
    }
}
