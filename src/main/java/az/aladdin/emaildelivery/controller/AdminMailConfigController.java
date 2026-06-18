package az.aladdin.emaildelivery.controller;

import az.aladdin.emaildelivery.model.request.email.AdminMailConfigRequest;
import az.aladdin.emaildelivery.model.request.email.AdminMailTestEmailRequest;
import az.aladdin.emaildelivery.model.response.email.AdminMailConfigResponse;
import az.aladdin.emaildelivery.service.email.AdminMailConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("admin/v1/settings/mail-config")
@RequiredArgsConstructor
public class AdminMailConfigController {

    private final AdminMailConfigService adminMailConfigService;

    @GetMapping
    public AdminMailConfigResponse get() {
        return adminMailConfigService.getMailConfig();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdminMailConfigResponse create(@RequestBody @Valid AdminMailConfigRequest request) {
        return adminMailConfigService.createMailConfig(request);
    }

    @PutMapping
    public AdminMailConfigResponse update(@RequestBody @Valid AdminMailConfigRequest request) {
        return adminMailConfigService.updateMailConfig(request);
    }

    @PostMapping("/test-email")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sendTestEmail(@RequestBody @Valid AdminMailTestEmailRequest request) {
        adminMailConfigService.sendTestEmail(request);
    }
}
