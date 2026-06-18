package az.aladdin.emaildelivery.controller;

import az.aladdin.emaildelivery.model.response.email.EmailSuppressionPageResponse;
import az.aladdin.emaildelivery.service.email.EmailSuppressionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("admin/v1/email-suppressions")
@RequiredArgsConstructor
public class EmailSuppressionController {

    private final EmailSuppressionService emailSuppressionService;

    @GetMapping
    public EmailSuppressionPageResponse search(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        return emailSuppressionService.search(search, page, limit);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        emailSuppressionService.delete(id);
    }
}
