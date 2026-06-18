package az.aladdin.emaildelivery.controller;

import az.aladdin.emaildelivery.model.response.email.EmailAnalyticsResponse;
import az.aladdin.emaildelivery.service.email.AdminEmailAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("admin/v1/emails")
@RequiredArgsConstructor
public class AdminEmailAnalyticsController {

    private final AdminEmailAnalyticsService adminEmailAnalyticsService;

    @GetMapping("/analytics")
    public EmailAnalyticsResponse analytics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) Long campaignId) {
        return adminEmailAnalyticsService.getAnalytics(from, to, campaignId);
    }
}
