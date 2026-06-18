package az.aladdin.emaildelivery.controller;

import az.aladdin.emaildelivery.model.request.email.UpsertEmailAddressBookContactRequest;
import az.aladdin.emaildelivery.model.response.email.EmailAddressBookContactPageResponse;
import az.aladdin.emaildelivery.model.response.email.EmailAddressBookContactResponse;
import az.aladdin.emaildelivery.service.email.EmailAddressBookContactService;
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
@RequestMapping("admin/v1/email-contacts")
@RequiredArgsConstructor
public class EmailAddressBookContactController {

    private final EmailAddressBookContactService emailAddressBookContactService;

    @GetMapping
    public EmailAddressBookContactPageResponse search(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        return emailAddressBookContactService.search(search, page, limit);
    }

    @GetMapping("/{id}")
    public EmailAddressBookContactResponse get(@PathVariable Long id) {
        return emailAddressBookContactService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmailAddressBookContactResponse create(@RequestBody @Valid UpsertEmailAddressBookContactRequest request) {
        return emailAddressBookContactService.create(request);
    }

    @PutMapping("/{id}")
    public EmailAddressBookContactResponse update(
            @PathVariable Long id,
            @RequestBody @Valid UpsertEmailAddressBookContactRequest request) {
        return emailAddressBookContactService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        emailAddressBookContactService.delete(id);
    }
}
