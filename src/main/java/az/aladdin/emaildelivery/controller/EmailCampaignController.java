package az.aladdin.emaildelivery.controller;

import az.aladdin.emaildelivery.model.request.email.SendCampaignRequest;
import az.aladdin.emaildelivery.model.request.email.UpsertEmailCampaignRequest;
import az.aladdin.emaildelivery.model.response.email.EmailCampaignPageResponse;
import az.aladdin.emaildelivery.model.response.email.EmailCampaignResponse;
import az.aladdin.emaildelivery.model.response.email.SendEmailResponse;
import az.aladdin.emaildelivery.service.email.AdminEmailService;
import az.aladdin.emaildelivery.service.email.EmailCampaignService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("admin/v1/campaigns")
@RequiredArgsConstructor
public class EmailCampaignController {

    private final EmailCampaignService emailCampaignService;
    private final AdminEmailService adminEmailService;

    @GetMapping
    public EmailCampaignPageResponse search(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        return emailCampaignService.search(search, page, limit);
    }

    @GetMapping("/{id}")
    public EmailCampaignResponse get(@PathVariable Long id) {
        return emailCampaignService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmailCampaignResponse create(@RequestBody @Valid UpsertEmailCampaignRequest request) {
        return emailCampaignService.create(request);
    }

    @PutMapping("/{id}")
    public EmailCampaignResponse update(@PathVariable Long id, @RequestBody @Valid UpsertEmailCampaignRequest request) {
        return emailCampaignService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        emailCampaignService.delete(id);
    }

    @PostMapping("/{id}/send")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public SendEmailResponse send(
            @PathVariable Long id,
            @RequestBody(required = false) @Valid SendCampaignRequest request) {
        return adminEmailService.sendCampaign(id, request);
    }
}
