package az.aladdin.emaildelivery.service.email;

import az.aladdin.emaildelivery.model.response.email.EmailAnalyticsResponse;
import az.aladdin.emaildelivery.model.response.email.EmailAnalyticsSummaryResponse;
import az.aladdin.emaildelivery.model.response.email.EmailCampaignAnalyticsResponse;
import az.aladdin.emaildelivery.repository.EmailCampaignRepository;
import az.aladdin.emaildelivery.repository.SentEmailRecipientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminEmailAnalyticsService {

    private final SentEmailRecipientRepository sentEmailRecipientRepository;
    private final EmailCampaignRepository emailCampaignRepository;

    @Transactional(readOnly = true)
    public EmailAnalyticsResponse getAnalytics(Instant from, Instant to, Long campaignId) {
        var normalizedFrom = from != null ? from : Instant.now().minus(30, ChronoUnit.DAYS);
        var normalizedTo = to != null ? to : Instant.now();
        if (normalizedFrom.isAfter(normalizedTo)) {
            var swap = normalizedFrom;
            normalizedFrom = normalizedTo;
            normalizedTo = swap;
        }

        var summary = buildSummary(normalizedFrom, normalizedTo, campaignId);
        var byCampaign = campaignId != null
                ? List.<EmailCampaignAnalyticsResponse>of()
                : buildCampaignBreakdown(normalizedFrom, normalizedTo);

        return EmailAnalyticsResponse.builder()
                .summary(summary)
                .byCampaign(byCampaign)
                .build();
    }

    private EmailAnalyticsSummaryResponse buildSummary(Instant from, Instant to, Long campaignId) {
        var emailsSent = sentEmailRecipientRepository.countEmailsInRange(from, to, campaignId);
        var recipientsTotal = sentEmailRecipientRepository.countRecipientsInRange(from, to, campaignId);
        var delivered = sentEmailRecipientRepository.countDeliveredRecipientsInRange(from, to, campaignId);
        var opened = sentEmailRecipientRepository.countOpenedRecipientsInRange(from, to, campaignId);
        var failed = sentEmailRecipientRepository.countFailedRecipientsInRange(from, to, campaignId);
        var attempted = delivered + failed;

        return EmailAnalyticsSummaryResponse.builder()
                .emailsSent(emailsSent)
                .recipientsTotal(recipientsTotal)
                .delivered(delivered)
                .opened(opened)
                .failed(failed)
                .deliveryRate(rate(delivered, attempted))
                .openRate(rate(opened, delivered))
                .build();
    }

    private List<EmailCampaignAnalyticsResponse> buildCampaignBreakdown(Instant from, Instant to) {
        return emailCampaignRepository.findAll().stream()
                .map(campaign -> {
                    var delivered = sentEmailRecipientRepository.countDeliveredRecipientsInRange(
                            from, to, campaign.getId());
                    var opened = sentEmailRecipientRepository.countOpenedRecipientsInRange(
                            from, to, campaign.getId());
                    var failed = sentEmailRecipientRepository.countFailedRecipientsInRange(
                            from, to, campaign.getId());
                    var total = sentEmailRecipientRepository.countRecipientsInRange(from, to, campaign.getId());
                    if (total == 0) {
                        return null;
                    }
                    var attempted = delivered + failed;
                    return EmailCampaignAnalyticsResponse.builder()
                            .campaignId(campaign.getId())
                            .campaignName(campaign.getName())
                            .recipientsTotal(total)
                            .delivered(delivered)
                            .opened(opened)
                            .deliveryRate(rate(delivered, attempted))
                            .openRate(rate(opened, delivered))
                            .build();
                })
                .filter(item -> item != null)
                .sorted(Comparator.comparingLong(EmailCampaignAnalyticsResponse::getRecipientsTotal).reversed())
                .limit(20)
                .toList();
    }

    private double rate(long numerator, long denominator) {
        if (denominator <= 0) {
            return 0.0;
        }
        return Math.round((numerator * 1000.0 / denominator)) / 1000.0;
    }
}
