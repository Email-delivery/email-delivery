package az.aladdin.emaildelivery.config.security;

import az.aladdin.emaildelivery.model.entity.AdminUser;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Getter
@RequiredArgsConstructor
public class AdminUserDetails implements UserDetails {

    private final AdminUser adminUser;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return adminUser.effectivePermissions().stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

    @Override
    public String getPassword() {
        return adminUser.getPassword();
    }

    @Override
    public String getUsername() {
        return adminUser.getEmail();
    }
}
