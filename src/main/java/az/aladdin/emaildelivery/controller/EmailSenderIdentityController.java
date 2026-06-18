package az.aladdin.emaildelivery.controller;

import az.aladdin.emaildelivery.model.request.email.UpsertEmailSenderIdentityRequest;
import az.aladdin.emaildelivery.model.response.email.EmailSenderIdentityResponse;
import az.aladdin.emaildelivery.service.email.EmailSenderIdentityService;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("admin/v1/email-sender-identities")
@RequiredArgsConstructor
public class EmailSenderIdentityController {

    private final EmailSenderIdentityService emailSenderIdentityService;

    @GetMapping
    public List<EmailSenderIdentityResponse> list() {
        return emailSenderIdentityService.listActive();
    }

    @GetMapping("/all")
    public List<EmailSenderIdentityResponse> listAll() {
        return emailSenderIdentityService.listAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmailSenderIdentityResponse create(@RequestBody @Valid UpsertEmailSenderIdentityRequest request) {
        return emailSenderIdentityService.create(request);
    }

    @PutMapping("/{id}")
    public EmailSenderIdentityResponse update(
            @PathVariable Long id,
            @RequestBody @Valid UpsertEmailSenderIdentityRequest request) {
        return emailSenderIdentityService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        emailSenderIdentityService.delete(id);
    }
}
