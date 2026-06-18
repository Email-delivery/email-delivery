package az.aladdin.emaildelivery.controller;

import az.aladdin.emaildelivery.model.request.account.CreateRoleRequest;
import az.aladdin.emaildelivery.model.request.account.UpdateRoleRequest;
import az.aladdin.emaildelivery.model.response.account.AdminRoleResponse;
import az.aladdin.emaildelivery.service.account.AdminRoleService;
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
@RequestMapping("admin/v1")
@RequiredArgsConstructor
public class AdminRoleController {

    private final AdminRoleService adminRoleService;

    @GetMapping("/permissions")
    public List<String> listPermissions() {
        return adminRoleService.listPermissions();
    }

    @GetMapping("/roles")
    public List<AdminRoleResponse> listRoles() {
        return adminRoleService.list();
    }

    @GetMapping("/roles/{id}")
    public AdminRoleResponse getRole(@PathVariable Long id) {
        return adminRoleService.get(id);
    }

    @PostMapping("/roles")
    @ResponseStatus(HttpStatus.CREATED)
    public AdminRoleResponse createRole(@RequestBody @Valid CreateRoleRequest request) {
        return adminRoleService.create(request);
    }

    @PutMapping("/roles/{id}")
    public AdminRoleResponse updateRole(@PathVariable Long id, @RequestBody @Valid UpdateRoleRequest request) {
        return adminRoleService.update(id, request);
    }

    @DeleteMapping("/roles/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRole(@PathVariable Long id) {
        adminRoleService.delete(id);
    }
}
