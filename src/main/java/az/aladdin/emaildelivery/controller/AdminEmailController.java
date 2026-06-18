package az.aladdin.emaildelivery.controller;

import az.aladdin.emaildelivery.model.request.email.ResendEmailRequest;
import az.aladdin.emaildelivery.model.request.email.SendAdminEmailRequest;
import az.aladdin.emaildelivery.model.request.email.UpdateEmailScheduleRequest;
import az.aladdin.emaildelivery.model.response.email.SendEmailResponse;
import az.aladdin.emaildelivery.model.response.email.SentEmailResponse;
import az.aladdin.emaildelivery.service.email.AdminEmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("admin/v1/emails")
@RequiredArgsConstructor
public class AdminEmailController {

    private final AdminEmailService adminEmailService;

    @PostMapping("/send")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public SendEmailResponse send(@RequestBody @Valid SendAdminEmailRequest request) {
        return adminEmailService.send(request);
    }

    @PostMapping("/{id}/resend")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public SendEmailResponse resend(
            @PathVariable Long id,
            @RequestBody(required = false) ResendEmailRequest request) {
        return adminEmailService.resend(id, request);
    }

    @GetMapping("/history")
    public List<SentEmailResponse> history() {
        return adminEmailService.history();
    }

    @PatchMapping("/{id}/schedule")
    public SentEmailResponse updateSchedule(
            @PathVariable Long id,
            @RequestBody @Valid UpdateEmailScheduleRequest request) {
        return adminEmailService.updateSchedule(id, request);
    }
}
