package az.aladdin.emaildelivery.service.email;

import az.aladdin.emaildelivery.exception.NotFoundException;
import az.aladdin.emaildelivery.exception.EntityNames;
import az.aladdin.emaildelivery.exception.MessageKeys;
import az.aladdin.emaildelivery.mapper.EmailSuppressionMapper;
import az.aladdin.emaildelivery.model.response.email.EmailSuppressionPageResponse;
import az.aladdin.emaildelivery.repository.EmailSuppressionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class EmailSuppressionService {

    private final EmailSuppressionRepository emailSuppressionRepository;
    private final EmailSuppressionMapper emailSuppressionMapper;
    private final AdminEmailUnsubscribeService adminEmailUnsubscribeService;

    @Transactional(readOnly = true)
    public EmailSuppressionPageResponse search(String search, int page, int limit) {
        int normalizedPage = page < 1 ? 1 : page;
        int normalizedLimit = limit < 1 ? 20 : Math.min(limit, 100);
        var normalizedSearch = StringUtils.hasText(search) ? search.trim() : null;

        var result = emailSuppressionRepository.search(
                normalizedSearch,
                PageRequest.of(normalizedPage - 1, normalizedLimit, Sort.by(Sort.Direction.DESC, "unsubscribedAt")));

        return EmailSuppressionPageResponse.builder()
                .items(result.getContent().stream().map(emailSuppressionMapper::toResponse).toList())
                .total(result.getTotalElements())
                .page(normalizedPage)
                .limit(normalizedLimit)
                .build();
    }

    @Transactional
    public void delete(Long id) {
        if (!emailSuppressionRepository.existsById(id)) {
            throw new NotFoundException(EntityNames.EMAIL_SUPPRESSION, MessageKeys.EXCEPTION_NOT_FOUND, id);
        }
        adminEmailUnsubscribeService.resubscribe(id);
    }
}
