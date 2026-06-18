package az.aladdin.emaildelivery.service.email;

import az.aladdin.emaildelivery.exception.BadException;
import az.aladdin.emaildelivery.exception.EntityNames;
import az.aladdin.emaildelivery.exception.MessageKeys;
import az.aladdin.emaildelivery.exception.NotFoundException;
import az.aladdin.emaildelivery.mapper.EmailTemplateMapper;
import az.aladdin.emaildelivery.model.entity.EmailTemplate;
import az.aladdin.emaildelivery.model.request.email.UpsertEmailTemplateRequest;
import az.aladdin.emaildelivery.model.response.email.EmailTemplatePageResponse;
import az.aladdin.emaildelivery.model.response.email.EmailTemplateResponse;
import az.aladdin.emaildelivery.repository.EmailCampaignRepository;
import az.aladdin.emaildelivery.repository.EmailTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailTemplateService {

    private final EmailTemplateRepository emailTemplateRepository;
    private final EmailTemplateMapper emailTemplateMapper;
    private final EmailCampaignRepository emailCampaignRepository;

    @Transactional(readOnly = true)
    public EmailTemplatePageResponse search(String search, int page, int limit) {
        int normalizedPage = page < 1 ? 1 : page;
        int normalizedLimit = limit < 1 ? 20 : Math.min(limit, 100);
        var normalizedSearch = StringUtils.hasText(search) ? search.trim() : null;

        var result = emailTemplateRepository.search(
                normalizedSearch,
                PageRequest.of(normalizedPage - 1, normalizedLimit, Sort.by(Sort.Direction.DESC, "updatedAt")));

        return EmailTemplatePageResponse.builder()
                .items(result.getContent().stream().map(emailTemplateMapper::toResponse).toList())
                .total(result.getTotalElements())
                .page(normalizedPage)
                .limit(normalizedLimit)
                .build();
    }

    @Transactional(readOnly = true)
    public EmailTemplateResponse get(Long id) {
        return emailTemplateMapper.toResponse(findById(id));
    }

    @Transactional
    public EmailTemplateResponse create(UpsertEmailTemplateRequest request) {
        ensureUniqueName(request.getName(), null);
        var saved = emailTemplateRepository.save(EmailTemplate.builder()
                .name(request.getName().trim())
                .description(trimToNull(request.getDescription()))
                .subject(trimToNull(request.getSubject()))
                .htmlBody(trimToNull(request.getHtmlBody()))
                .build());
        log.info("Created email template '{}'", saved.getName());
        return emailTemplateMapper.toResponse(saved);
    }

    @Transactional
    public EmailTemplateResponse update(Long id, UpsertEmailTemplateRequest request) {
        var template = findById(id);
        ensureUniqueName(request.getName(), id);
        template.setName(request.getName().trim());
        template.setDescription(trimToNull(request.getDescription()));
        template.setSubject(trimToNull(request.getSubject()));
        template.setHtmlBody(trimToNull(request.getHtmlBody()));
        var saved = emailTemplateRepository.save(template);
        log.info("Updated email template '{}'", saved.getName());
        return emailTemplateMapper.toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        var template = findById(id);
        if (emailCampaignRepository.existsByTemplateId(id)) {
            throw new BadException(EntityNames.EMAIL_TEMPLATE, MessageKeys.EXCEPTION_TEMPLATE_IN_USE);
        }
        emailTemplateRepository.delete(template);
        log.info("Deleted email template '{}'", template.getName());
    }

    @Transactional(readOnly = true)
    public EmailTemplate findById(Long id) {
        return emailTemplateRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        EntityNames.EMAIL_TEMPLATE,
                        MessageKeys.EXCEPTION_NOT_FOUND,
                        id));
    }

    private void ensureUniqueName(String name, Long excludeId) {
        var normalized = name.trim();
        var exists = excludeId == null
                ? emailTemplateRepository.existsByNameIgnoreCase(normalized)
                : emailTemplateRepository.existsByNameIgnoreCaseAndIdNot(normalized, excludeId);
        if (exists) {
            throw new BadException(
                    EntityNames.EMAIL_TEMPLATE,
                    MessageKeys.EXCEPTION_TEMPLATE_NAME_EXISTS,
                    normalized);
        }
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
