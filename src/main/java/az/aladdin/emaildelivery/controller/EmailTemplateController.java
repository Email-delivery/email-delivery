package az.aladdin.emaildelivery.controller;

import az.aladdin.emaildelivery.model.request.email.UpsertEmailTemplateRequest;
import az.aladdin.emaildelivery.model.response.email.EmailTemplatePageResponse;
import az.aladdin.emaildelivery.model.response.email.EmailTemplateResponse;
import az.aladdin.emaildelivery.service.email.EmailTemplateService;
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
@RequestMapping("admin/v1/email-templates")
@RequiredArgsConstructor
public class EmailTemplateController {

    private final EmailTemplateService emailTemplateService;

    @GetMapping
    public EmailTemplatePageResponse search(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        return emailTemplateService.search(search, page, limit);
    }

    @GetMapping("/{id}")
    public EmailTemplateResponse get(@PathVariable Long id) {
        return emailTemplateService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmailTemplateResponse create(@RequestBody @Valid UpsertEmailTemplateRequest request) {
        return emailTemplateService.create(request);
    }

    @PutMapping("/{id}")
    public EmailTemplateResponse update(@PathVariable Long id, @RequestBody @Valid UpsertEmailTemplateRequest request) {
        return emailTemplateService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        emailTemplateService.delete(id);
    }
}
