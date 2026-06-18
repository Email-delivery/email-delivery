package az.aladdin.emaildelivery.controller;

import az.aladdin.emaildelivery.model.request.email.UpsertEmailDraftRequest;
import az.aladdin.emaildelivery.model.response.email.EmailDraftResponse;
import az.aladdin.emaildelivery.service.email.EmailDraftService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("admin/v1/email-drafts")
@RequiredArgsConstructor
public class EmailDraftController {

    private final EmailDraftService emailDraftService;

    @GetMapping("/current")
    public ResponseEntity<EmailDraftResponse> getCurrent() {
        return emailDraftService.getCurrent()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PutMapping("/current")
    public ResponseEntity<EmailDraftResponse> saveCurrent(@RequestBody @Valid UpsertEmailDraftRequest request) {
        return emailDraftService.saveCurrent(request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @DeleteMapping("/current")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCurrent() {
        emailDraftService.deleteCurrent();
    }
}
