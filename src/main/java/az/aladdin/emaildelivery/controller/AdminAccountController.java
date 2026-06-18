package az.aladdin.emaildelivery.controller;

import az.aladdin.emaildelivery.model.request.account.AssignRolesRequest;
import az.aladdin.emaildelivery.model.request.account.CreateAdminRequest;
import az.aladdin.emaildelivery.model.request.account.UpdateAdminRequest;
import az.aladdin.emaildelivery.model.response.account.AdminAccountPageResponse;
import az.aladdin.emaildelivery.model.response.account.AdminAccountResponse;
import az.aladdin.emaildelivery.service.account.AdminAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Manages the admin panel's own user accounts (the admins themselves), backed by this service's database.
 */
@RestController
@RequestMapping("admin/v1/accounts")
@RequiredArgsConstructor
public class AdminAccountController {

    private final AdminAccountService adminAccountService;

    @GetMapping
    public AdminAccountPageResponse list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        return adminAccountService.list(search, role, status, page, limit);
    }

    @GetMapping("/{id}")
    public AdminAccountResponse get(@PathVariable Long id) {
        return adminAccountService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdminAccountResponse create(@RequestBody @Valid CreateAdminRequest request) {
        return adminAccountService.create(request);
    }

    @PutMapping("/{id}")
    public AdminAccountResponse update(@PathVariable Long id, @RequestBody @Valid UpdateAdminRequest request) {
        return adminAccountService.update(id, request);
    }

    @PutMapping("/{id}/roles")
    public AdminAccountResponse assignRoles(@PathVariable Long id, @RequestBody @Valid AssignRolesRequest request) {
        return adminAccountService.assignRoles(id, request);
    }

    @PatchMapping("/{id}/activate")
    public AdminAccountResponse activate(@PathVariable Long id) {
        return adminAccountService.activate(id);
    }

    @PatchMapping("/{id}/deactivate")
    public AdminAccountResponse deactivate(@PathVariable Long id) {
        return adminAccountService.deactivate(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        adminAccountService.delete(id);
    }
}
